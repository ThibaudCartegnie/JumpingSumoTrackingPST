package imt.pst.jumping_sumo_tracking_pst.detectors;

import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import imt.pst.jumping_sumo_tracking_pst.drone.JumpingSumo;

/**
 * Created by Natu on 20/12/2017.
 */


public class YetAnotherLineDetector implements Detector {

    private Mat mRgb, mGray, thresholded_roi;
    private Rect roi_rect;
    private JumpingSumo mDrone = null;


    private String TAG = "YALD";
    private int HEIGHT_ROI = 10;

    /**
     * Default constructor for the Jumping Sumo Parrot Drone
     */
    public YetAnotherLineDetector() {
        this(640, 480);
    }

    /**
     * Constructor
     *
     * @param height height of the images passed to detectors
     * @param width  width of the images passed to detectors
     */
    public YetAnotherLineDetector(int height, int width) {
        mRgb = new Mat(height, width, CvType.CV_8UC4);
        mGray = new Mat(height, width, CvType.CV_8UC1);

        // height and width are reverted because of reasons I suppose
        roi_rect = new Rect(0, width - 10, height, HEIGHT_ROI);
        thresholded_roi = new Mat(1, width, CvType.CV_8UC1);
    }

    /**
     * detect a BLACK line to follow : scan the firsts rows and calculate the median point of black-ish pixels
     * captures this point in a Queue representing the N last frames, calculate a mean point on these frames and use it to direct the drone
     * @param originalFrame image passed by the drone camera
     * @param outputData    unused byte array
     * @return
     */
    @Override
    public Bitmap detect(Bitmap originalFrame, byte[] outputData) {
        Utils.bitmapToMat(originalFrame, mRgb);
        Imgproc.cvtColor(mRgb, mGray, Imgproc.COLOR_RGB2GRAY, 1);

        Mat roi = new Mat(mGray, roi_rect);
        Core.inRange(roi, new Scalar(0), new Scalar(100), thresholded_roi);

        int[] pos = new int[HEIGHT_ROI];
        int[] looked_lines = new int[]{0,HEIGHT_ROI-1};

        for(int nb_row : looked_lines){
            int nb_black_pixels = 0, counted_black_pixels = 0;

            Mat row = thresholded_roi.row(nb_row);
            nb_black_pixels = Core.countNonZero(row);
            for(int i = 0; i < row.cols(); i++){
                pos[nb_row]++;
                if(row.get(0, i)[0] > 0){
                    counted_black_pixels++;
                }
                if(nb_black_pixels/2 < counted_black_pixels){
                    pos[nb_row] = i;
                    break;
                }
            }
        }

        int pos_to_follow = (pos[0] + pos[HEIGHT_ROI-1]) /2;
        int middle = 640 / 2;
        if (pos_to_follow < 20 || pos_to_follow > 620) {
            mDrone.setSpeed(JumpingSumo.NULL_SPEED);
            mDrone.setTurn(JumpingSumo.NO_TURN);
            mDrone.setFlag(JumpingSumo.FLAG_DO_NOT_RUN);
        } else if (pos_to_follow > middle + 50) {
            //turn right
            mDrone.setSpeed((byte) (5 * JumpingSumo.FORWARD_SPEED));
            mDrone.setTurn((byte) (5 * JumpingSumo.RIGHT_TURN));
            mDrone.setFlag(JumpingSumo.FLAG_RUN);
        } else if (pos_to_follow < middle - 50) {
            //turn left
            mDrone.setSpeed((byte) (5 * JumpingSumo.FORWARD_SPEED));
            mDrone.setTurn((byte) (5 * JumpingSumo.LEFT_TURN));
            mDrone.setFlag(JumpingSumo.FLAG_RUN);
        } else {
            //go straight
            mDrone.setSpeed((byte) (20 * JumpingSumo.FORWARD_SPEED));
            mDrone.setTurn(JumpingSumo.NO_TURN);
            mDrone.setFlag(JumpingSumo.FLAG_RUN);
        }

        Log.d(TAG, "follow x:" + pos_to_follow);

        Imgproc.rectangle(mRgb, roi_rect.tl(), roi_rect.br(), new Scalar(255, 0, 0));
//        Imgproc.arrowedLine(mRgb, new Point(0,0), new Point(640,480), new Scalar(255,0,255));
        Bitmap out = originalFrame.copy(originalFrame.getConfig(), originalFrame.isMutable());
        Utils.matToBitmap(mRgb, out);
        return out;
    }

    @Override
    public void setDrone(JumpingSumo mDrone) {
        this.mDrone = mDrone;
    }
}

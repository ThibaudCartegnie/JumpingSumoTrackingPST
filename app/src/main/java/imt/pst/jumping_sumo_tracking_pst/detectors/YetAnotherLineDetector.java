package imt.pst.jumping_sumo_tracking_pst.detectors;

import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import imt.pst.jumping_sumo_tracking_pst.drone.JumpingSumo;

/**
 * Created by Natu on 20/12/2017.
 */

/**
 * https://lejosnews.wordpress.com/2015/12/03/line-following-with-opencv/
 */
public class YetAnotherLineDetector implements Detector {

    private Mat mRgb;
    private Mat mEdges;
    private Mat mGray_1;
    private Mat mGray_2;
    private Mat array255;
    private Mat distance;
    private Mat mObstacleCircle;
    private int nb_tot_white;
    private Mat roi, thresholded_roi;
    private Rect roi_rect;
    private Mat erode, dilate;
    private JumpingSumo mDrone = null;

    private ArrayBlockingQueue<Integer> middle_pos;

    private String TAG = "YALD";

    public YetAnotherLineDetector(){
        this(640,480);
    }

    public YetAnotherLineDetector(int height, int width){
        mRgb = new Mat(height, width, CvType.CV_8UC4);
        mEdges = new Mat(height, width, CvType.CV_8UC4);
        array255 = new Mat(height, width, CvType.CV_8UC1);
        distance = new Mat(height, width, CvType.CV_8UC1);
        mGray_1 = new Mat(height, width, CvType.CV_8UC1);
        mGray_2 = new Mat(height, width, CvType.CV_8UC1);
        mObstacleCircle = new Mat(height, width, CvType.CV_8UC1);

        roi_rect = new Rect(0, width-10, height, 1);
        thresholded_roi = new Mat(1, width, CvType.CV_8UC1);

        erode = new Mat(3,3,CvType.CV_8UC1, new Scalar(1));
        dilate = new Mat(3,3,CvType.CV_8UC1, new Scalar(1));

        middle_pos = new ArrayBlockingQueue<Integer>(20, true);
    }

    @Override
    public Bitmap detect(Bitmap originalFrame, byte[] outputData) {
        Utils.bitmapToMat(originalFrame, mRgb);
        Imgproc.cvtColor(mRgb, mGray_1, Imgproc.COLOR_RGB2GRAY, 1);

        Log.d(TAG, "roi.y:"+roi_rect.y + ", roi.h:"+roi_rect.height+", m.rows:"+mGray_1.rows());

        roi = new Mat(mGray_1, roi_rect);
        Core.inRange(roi, new Scalar(0), new Scalar(100), thresholded_roi);
        int nb_black_pixels = Core.countNonZero(thresholded_roi);

        Log.d(TAG, "rows: "+thresholded_roi.rows() + ", cols:" + thresholded_roi.cols());

        double[] data = new double[thresholded_roi.cols()];
        int median_pos = 0, nb_black_counted = 0, pos=0;
        while(nb_black_counted <= nb_black_pixels/2 && pos < thresholded_roi.cols()){
            if(thresholded_roi.get(0,pos)[0] > 0){
                nb_black_counted++;
            }
            pos++;
        }

        //at first, fill the queue
        if(middle_pos.remainingCapacity() != 0){
            middle_pos.add(pos);
            Imgproc.rectangle(mRgb, roi_rect.tl(), roi_rect.br(), new Scalar(255,0,0));
            Bitmap out = originalFrame.copy(originalFrame.getConfig(), originalFrame.isMutable());
            Utils.matToBitmap(mRgb, out);
            return out;
        }

        middle_pos.poll();
        middle_pos.add(pos);

        int pos_to_follow = ((Integer)middle_pos.toArray()[0] + (Integer)middle_pos.toArray()[middle_pos.size()-1])/2;
        int middle = 480/2;
        if(pos == 0){
            mDrone.setSpeed(JumpingSumo.NULL_SPEED);
            mDrone.setTurn(JumpingSumo.NO_TURN);
            mDrone.setFlag(JumpingSumo.FLAG_DO_NOT_RUN);
        } else
        if(pos_to_follow > middle+30){
            //turn right
            mDrone.setSpeed((byte) (5*JumpingSumo.FORWARD_SPEED));
            mDrone.setTurn((byte) (5*JumpingSumo.RIGHT_TURN));
            mDrone.setFlag(JumpingSumo.FLAG_RUN);
        } else if( pos_to_follow < middle-30){
            //turn left
            mDrone.setSpeed((byte) (5*JumpingSumo.FORWARD_SPEED));
            mDrone.setTurn((byte) (5*JumpingSumo.LEFT_TURN));
            mDrone.setFlag(JumpingSumo.FLAG_RUN);
        }else{
            //go straight
            mDrone.setSpeed((byte) (25*JumpingSumo.FORWARD_SPEED));
            mDrone.setTurn(JumpingSumo.NO_TURN);
            mDrone.setFlag(JumpingSumo.FLAG_RUN);
        }

        Log.d(TAG, "follow x:" + pos_to_follow);

        Imgproc.rectangle(mRgb, roi_rect.tl(), roi_rect.br(), new Scalar(255,0,0));
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

package imt.pst.jumping_sumo_tracking_pst.detectors;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.parrot.arsdk.arcommands.ARCOMMANDS_JUMPINGSUMO_MEDIARECORDEVENT_PICTUREEVENTCHANGED_ERROR_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_DEVICE_STATE_ENUM;
import com.parrot.arsdk.arcontroller.ARControllerCodec;
import com.parrot.arsdk.arcontroller.ARFrame;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import imt.pst.jumping_sumo_tracking_pst.drone.JumpingSumo;

/**
 * Created by Natu on 15/12/2017.
 */

public class ObstacleDetector implements Detector {

    private Mat mRgb;
    private Mat mEdges;
    private Mat mGray_1;
    private Mat mGray_2;
    private Mat distance;
    private Mat mObstacleCircle;
    private int nb_tot_white;

    private JumpingSumo mDrone = null;

    private String TAG = "OBSTACLES";

    public ObstacleDetector(){
        this(640,480);
    }

    public ObstacleDetector(int width, int height) {
        mRgb = new Mat(height, width, CvType.CV_8UC4);
        mEdges = new Mat(height, width, CvType.CV_8UC4);
        distance = new Mat(height, width, CvType.CV_8UC1);
        mGray_1 = new Mat(height, width, CvType.CV_8UC1);
        mGray_2 = new Mat(height, width, CvType.CV_8UC1);
        mObstacleCircle = new Mat(height, width, CvType.CV_8UC1);

        Imgproc.ellipse(mObstacleCircle,
                new Point(width/2, height), //center
                new Size(width/2, height/3), // size of axes
                180, 180,0.0,  // angle of axis, start of draw, end of draw (referring to the angle of main axis)
                new Scalar(255), // color
                -1); //thickness (0 for coloring the whole ellipse

        nb_tot_white = Core.countNonZero(mObstacleCircle);
        Log.d(TAG, "nn 0: "+nb_tot_white);
    }

    public Bitmap detect(Bitmap originalFrame, byte[] dataOutput){
        Utils.bitmapToMat(originalFrame, mRgb);

        Imgproc.cvtColor(mRgb,mGray_1,Imgproc.COLOR_RGB2GRAY);
        Imgproc.bilateralFilter(mGray_1, mGray_2, 9, 200, 200);

        double high_thresh = Imgproc.threshold(mGray_2, distance,0,255,Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);
        double low_thresh = 0.2 * high_thresh;

        // Calculate the canny edges
        Imgproc.Canny(mGray_2, mGray_2, low_thresh, high_thresh);

        // See the edges which are in the ellipse in front of the drone
        Core.bitwise_and(mGray_2, mObstacleCircle, mGray_1);

        // Calculate the ratio
        int nb_px_obstacle = Core.countNonZero(mGray_1);
        float ratio = 100*((float)nb_px_obstacle/(float)(nb_tot_white==0? 1:nb_tot_white));
        Log.d(TAG, "nb px blanc tot: " + nb_tot_white + "; nb px comptés: " + nb_px_obstacle);
        Log.d(TAG, "ratio : " + ratio +"%");

        // Calculate drone actiob
        avoid(ratio);


        // print it on image
        mEdges.setTo(new Scalar(0));
        mEdges.setTo(new Scalar(0,255,0), mGray_2);
        Core.add(mRgb, mEdges, mRgb);

        mEdges.setTo(new Scalar(0));
        mEdges.setTo(new Scalar(255,0,0), mObstacleCircle);
        Core.add(mRgb, mEdges, mRgb);


        Bitmap out = originalFrame.copy(originalFrame.getConfig(), originalFrame.isMutable());
        Utils.matToBitmap(mRgb, out);

        return out;
    }

    private double lastAvoid = 0.0d;
    private boolean flagOn = false;
    private void avoid(float ratio) {
        if(mDrone == null){
            return;
        }

        // The threshhold of 0.4 is very experimental
        if(ratio > 0.4f && System.currentTimeMillis() - lastAvoid > 3000.0d){
            Log.v(TAG, "Obstacle detected");
            mDrone.setJump(JumpingSumo.LONG_JUMP);
            mDrone.setFlag(JumpingSumo.FLAG_RUN);

            flagOn = true;
            lastAvoid = System.currentTimeMillis();
        } else if(flagOn){
                mDrone.setJump(JumpingSumo.NO_JUMP);
                mDrone.setFlag(JumpingSumo.FLAG_DO_NOT_RUN);
                flagOn = false;
        }
    }

    @Override
    public void setDrone(JumpingSumo drone){
        this.mDrone = drone;
    }


}

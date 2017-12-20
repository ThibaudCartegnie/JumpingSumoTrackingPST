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

        roi_rect = new Rect(0, height-10, width, 10);
        roi = new Mat(mGray_1, roi_rect);
        thresholded_roi = new Mat(roi.rows(), roi.cols(), CvType.CV_8UC1);

        erode = new Mat(3,3,CvType.CV_8UC1, new Scalar(1));
        dilate = new Mat(3,3,CvType.CV_8UC1, new Scalar(1));
    }

    @Override
    public Bitmap detect(Bitmap originalFrame, byte[] outputData) {
        Utils.bitmapToMat(originalFrame, mRgb);

        Imgproc.cvtColor(mRgb, mGray_1, Imgproc.COLOR_RGB2GRAY, 1);
        roi = new Mat(mGray_1, roi_rect);
        Core.inRange(roi, new Scalar(0), new Scalar(100), thresholded_roi);

        Log.d(TAG, "1: "+ roi.dump());
        Log.d(TAG, thresholded_roi.dump());

        Bitmap out = originalFrame.copy(originalFrame.getConfig(), originalFrame.isMutable());
        Utils.matToBitmap(mRgb, out);
        return out;
    }

    @Override
    public void setDrone(JumpingSumo mDrone) {
        this.mDrone = mDrone;
    }
}

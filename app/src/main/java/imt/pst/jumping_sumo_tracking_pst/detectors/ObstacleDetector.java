package imt.pst.jumping_sumo_tracking_pst.detectors;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

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
    private Mat array255;
    private Mat distance;
    private Mat mObstacleCircle;
    private int nb_tot_white;

    private String TAG = "OBSTACLES";

    public ObstacleDetector(){
        this(640,480);
    }

    public ObstacleDetector(int width, int height) {
        mRgb = new Mat(height, width, CvType.CV_8UC4);
        mEdges = new Mat(height, width, CvType.CV_8UC4);
        array255 = new Mat(height, width, CvType.CV_8UC1);
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
        //Imgproc.bilateralFilter(mGray_2, mGray_1, 9, 200, 200);
        //Imgproc.GaussianBlur(mGray, mGray, new Size(9,9),0,0); // Uses a Gaussian filter in the image
        double high_thresh = Imgproc.threshold(mGray_2, distance,0,255,Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);
        double low_thresh = 0.2 * high_thresh;

        Imgproc.Canny(mGray_2, mGray_2, low_thresh, high_thresh);

        Core.bitwise_and(mGray_2, mObstacleCircle, mGray_1);
        int nb_px_obstacle = Core.countNonZero(mGray_1);
        float ratio[] = new float[3];
        ratio[0] = 100*((float)nb_px_obstacle/(float)(nb_tot_white==0? 1:nb_tot_white));
        Log.d(TAG, "nb px blanc tot: " + nb_tot_white + "; nb px comptés: " + nb_px_obstacle);
        Log.d(TAG, "ratio : " + ratio[0] +"%");



        //affichage des arrêtes
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
}

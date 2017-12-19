package imt.pst.jumping_sumo_tracking_pst.detectors;

import android.graphics.Bitmap;
import android.util.Log;

import com.parrot.arsdk.arcommands.ARCommandJumpingSumoSettingsStateProductGPSVersionChangedListener;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import imt.pst.jumping_sumo_tracking_pst.drone.JumpingSumo;

import static java.lang.Math.abs;

/**
 * Created by Natu on 19/12/2017.
 */

public class CircleDetector implements Detector{
        private Mat mRgb;
        private Mat mHsv;
        private Mat mThresholded;
        private Mat array255;
        private Mat distance;
        private JumpingSumo mJSDrone = null;
        private boolean follow;


        private String TAG = "Recog2";

        public CircleDetector(){
            this(640,480);
        }

        public CircleDetector(int width, int height) {
            mRgb = new Mat(height, width, CvType.CV_8UC4);
            mHsv = new Mat(height, width, CvType.CV_8UC4);
            array255 = new Mat(height, width, CvType.CV_8UC1);
            distance = new Mat(height, width, CvType.CV_8UC1);
            mThresholded = new Mat(height, width, CvType.CV_8UC1);

        }

        /**
         * Performs disc detection
         *
         * @param bmp image in which you want the detection to be used
         * @return Bitmap image with a circle drawn at the position of the detected disc
         */
        public Bitmap detect(Bitmap bmp, byte[] out) {

            //List<Mat> lhsv = new ArrayList<>(3);
            Mat circles = new Mat();
            Scalar hsv_min = new Scalar(50, 90, 50, 0);
            Scalar hsv_max = new Scalar(90, 240, 180, 0);

            Utils.bitmapToMat(bmp, mRgb);
            Imgproc.cvtColor(mRgb, mHsv, Imgproc.COLOR_RGB2HSV, 4); // Converts image from Rgb to Hsv

            Core.inRange(mHsv, hsv_min, hsv_max, mThresholded); // Checks if the hsv image lie between the two limits

            Imgproc.GaussianBlur(mThresholded, mThresholded, new Size(9,9),0,0); // Uses a Gaussian filter in the image
            Imgproc.HoughCircles(mThresholded, circles, Imgproc.CV_HOUGH_GRADIENT, 2, mThresholded.height()/4, 500, 50, 0, 0); // Finds circles in a grayscale image using Hough transform
            int rows = circles.rows();
            int elemSize = (int)circles.elemSize();
            float[] data = new float[rows * elemSize/4];
            if (data.length>0){
                circles.get(0, 0, data);

                // into data
                for(int i=0; i<data.length; i=i+3) {
                    Log.v(TAG, "i : "+ (i) +" " + data[i]);
                    Log.v(TAG, "i : "+ (i+1) +" " + data[i+1]);

                    Point center= new Point(data[i], data[i+1]);
                    Imgproc.ellipse( mRgb, center, new Size((double)data[i+2], (double)data[i+2]), 0, 0, 360, new Scalar( 255, 0, 255 ), 4, 8, 0 );
                }
                followCircle(true, data[0], data[1], data[2]);
            } else {
                followCircle(false, .0f,.0f,.0f);
            }



            Utils.matToBitmap(mRgb, bmp);
            return bmp;
        }

        @Override
        public void setDrone(JumpingSumo mDrone){
            mJSDrone = mDrone;
        }

        private void followCircle(boolean isCircleDetected, float x_pos, float y_pos, float diameter){
            if(mJSDrone == null){
                return;
            }
            if (isCircleDetected) {
                mJSDrone.setSpeed((byte) 0);
                mJSDrone.setFlag((byte) 0);
            } else {
                followCenter(x_pos);
                followSize(diameter);

            }
        }

    /**
     * Command the forward and backward movements in relation with the detected object.
     *
     * @param radius of the detected circle
     */
    public void followSize(float radius) {
        float defaultRadius = 60;
        float vitesse = 25;
        int sign;

        if (abs(defaultRadius - radius) > 15) {
            if (radius > defaultRadius) {
                sign = -1;
            } else {
                sign = 1;
            }

            mJSDrone.setSpeed((byte) (sign * vitesse));
            mJSDrone.setFlag(JumpingSumo.FLAG_RUN);
        } else {
            mJSDrone.setSpeed((byte) 0);
            if (!follow)
                mJSDrone.setFlag(JumpingSumo.FLAG_DO_NOT_RUN);
        }
    }

    /**
     * Command the forward and backward movements in relation with the detected object.
     *
     * @param centerX is the horizontal position of the detected circle
     */
    public void followCenter(float centerX) {
        int signRot;
        float defaultCenter = 320;
        float vitRotation = 5;


        if (abs(defaultCenter - centerX) > 50) {
            if (centerX > defaultCenter) {
                signRot = 1;
            } else {
                signRot = -1;
            }
            mJSDrone.setTurn((byte) (signRot * vitRotation));
            mJSDrone.setFlag(JumpingSumo.FLAG_RUN);
            follow = true;
        } else {
            mJSDrone.setTurn((byte) 0);
            mJSDrone.setFlag(JumpingSumo.FLAG_DO_NOT_RUN);
            follow = false;
        }
    }

}

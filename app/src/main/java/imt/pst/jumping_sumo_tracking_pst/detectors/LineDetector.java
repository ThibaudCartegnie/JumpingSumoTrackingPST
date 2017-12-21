package imt.pst.jumping_sumo_tracking_pst.detectors;

import android.graphics.Bitmap;
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
 * Created by Natu on 19/12/2017.
 */

public class LineDetector implements Detector{
    private Mat mRgb, template,mGray,mThresholded,array255,distance,mLines,mEdges;
    private Point a,b,AB;
    private JumpingSumo mJSDrone;
    private final String TAG = "RecogPath";
    private double theta = 0;

    public LineDetector(){
        this(640,480);
    }

    public LineDetector(int width, int height) {
        mRgb = new Mat(height, width, CvType.CV_8UC4);
        mGray = new Mat(height, width, CvType.CV_8UC4);
        mLines = new Mat(height, width, CvType.CV_8UC4);
        mEdges = new Mat(height, width, CvType.CV_8UC4);
        array255 = new Mat(height, width, CvType.CV_8UC1);
        distance = new Mat(height, width, CvType.CV_8UC1);
        mThresholded = new Mat(height, width, CvType.CV_8UC1);
        template = new Mat(height,width,CvType.CV_8UC4); //draws a line to match the path to follow
        a = new Point(width/2,2*height/3);
        b = new Point(width/2, height);
        AB = new Point(b.x - a.x, b.y-a.y);


    }

    /**
     * Performs path detection
     *
     * @param bmp image in which you want the detection to be used
     * @return Bitmap image
     */
    public Bitmap detect(Bitmap bmp, byte[] out) {
        Mat lines = new Mat();
        Bitmap outBmp = bmp.copy(bmp.getConfig(),bmp.isMutable());

        Utils.bitmapToMat(bmp, mRgb);
        float width_tolerance = 80.0f, height_tolerance = 260.0f;
        float width=640, height=480, a=height*3/4;
        //Core.inRange(mRgb,new Scalar(0,0,0), new Scalar(20,20,20), mThresholded2);
        Imgproc.cvtColor(mRgb, mGray, Imgproc.COLOR_RGB2GRAY, 4);
        Imgproc.bilateralFilter(mGray,mThresholded,9,200,200);


        //double high_thresh = Imgproc.threshold(mThresholded, distance,0,255,Imgproc.THRESH_OTSU);
        //double low_thresh = 0.2 * high_thresh;
        //Log.d(TAG, "high_thresh: " + high_thresh);
        Imgproc.Canny(mThresholded,mThresholded,50,150);

        Imgproc.HoughLinesP(mThresholded, lines, 1, Math.PI / 180, 75, 0, 10);//Finds lines in a gray scale
        //Imgproc.HoughLines(mThresholded, lines, 1, Math.PI /180,200);

        theta = .0f;
        int nb = 0;
        for (int i=0;i<lines.rows();i++){
            double [] vec = lines.get(i,0);

            if(vec == null){
                break;
            }
            if(vec.length < 4){
                break;
            }
            Point p1,p2;

            p1=new Point(vec[0],vec[1]);
            p2=new Point(vec[2],vec[3]);
            Point P = new Point(p2.x - p1.x, p2.y - p1.y);
//            if ((a.x-width_tolerance< p1.x)&&(p1.x < a.x+width_tolerance)&&(a.x-width_tolerance < p2.x)&&(p2.x < a.x+width_tolerance)
//                    &&(p2.y>a.y-height_tolerance)&&(p1.y>a.y-height_tolerance)) {

            if(     (p1.x <= width/2 && p1.y >= 2*(a-height)*p1.x/width + height)
                    || (p1.x > width/2 && p1.y >= 2*(height-a)*p1.x/width + 2*a - height)

                    && (p2.x <= width/2 && p2.y >= 2*(a-height)*p2.x/width + height)
                    || (p2.x > width/2 && p2.y >= 2*(height-a)*p2.x/width + 2*a - height))
            {

                theta += Math.toDegrees(Math.atan2(P.y, P.x) - Math.atan2(AB.y, AB.x));
                nb++;

            }
            //Log.v(TAG, "i: "+ i+ "size:" + lines.size().height + ", w:"+lines.size().width+", cols: "+lines.cols() + ", rows: "+lines.rows());
            Imgproc.line(mRgb,p1,p2,new Scalar(0,0,255),1,Core.LINE_AA,0);
        }
        theta = nb == 0? Float.NaN : theta/nb;
        Log.d(TAG, "theta :" + theta + " ,nb:"+nb);

        if(25 < theta && theta <= 180) {
            Log.d(TAG,"turned -5");
            mJSDrone.setSpeed(JumpingSumo.NULL_SPEED);
            mJSDrone.setTurn((byte) (5*JumpingSumo.RIGHT_TURN));
            mJSDrone.setFlag(JumpingSumo.FLAG_RUN);
        } else if (-25 > theta && theta >= -180){
            Log.d(TAG, "turned 5");
            mJSDrone.setSpeed(JumpingSumo.NULL_SPEED);
            mJSDrone.setTurn((byte) (5*JumpingSumo.LEFT_TURN));
            mJSDrone.setFlag(JumpingSumo.FLAG_RUN);
        } else if ((-25<=theta)&&(theta<=25)){
            Log.d(TAG, "go front");
            mJSDrone.setSpeed((byte) (25*JumpingSumo.FORWARD_SPEED));
            mJSDrone.setTurn(JumpingSumo.NO_TURN);
            mJSDrone.setFlag(JumpingSumo.FLAG_RUN);
        } else {
            Log.d(TAG, "do nothing");
            mJSDrone.setSpeed(JumpingSumo.NULL_SPEED);
            mJSDrone.setTurn(JumpingSumo.NO_TURN);
            mJSDrone.setFlag(JumpingSumo.FLAG_DO_NOT_RUN);
        }

        Imgproc.line(template,new Point(320,height_tolerance),b,new Scalar(255),(int)(2*width_tolerance),8,0);
        Core.add(mRgb,template,mRgb);

        Utils.matToBitmap(mRgb, outBmp);
        return outBmp;
    }

    public void setDrone(JumpingSumo mDrone){
        this.mJSDrone = mDrone;
    }
}

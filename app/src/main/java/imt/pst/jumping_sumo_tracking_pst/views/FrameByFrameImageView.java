package imt.pst.jumping_sumo_tracking_pst.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.support.v4.widget.ImageViewCompat;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.parrot.arsdk.arcontroller.ARFrame;

import imt.pst.jumping_sumo_tracking_pst.drone.JumpingSumo;

/**
 * Created by Natu on 15/12/2017.
 */

public class FrameByFrameImageView extends android.support.v7.widget.AppCompatImageView {
    private static final String TAG = "JSVideoView";
    private final Handler mHandler;
    private Bitmap mBmp;
    private JumpingSumo mJSDrone = null;

    public void setmJSDrone(JumpingSumo mJSDrone) {
        this.mJSDrone = mJSDrone;
    }

    public FrameByFrameImageView(Context context) {
        super(context);
        // needed because setImageBitmap should be called on the main thread
        mHandler = new Handler(context.getMainLooper());
        customInit();
    }

    public FrameByFrameImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // needed because setImageBitmap should be called on the main thread
        mHandler = new Handler(context.getMainLooper());
        customInit();
    }

    public FrameByFrameImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // needed because setImageBitmap should be called on the main thread
        mHandler = new Handler(context.getMainLooper());
        customInit();
    }

    private void customInit() {
        setScaleType(ImageView.ScaleType.CENTER_CROP);
    }

    /**
     * Converts a frame into Bitmap and display it
     *
     * @param frame image you take from the camera
     */
    public void displayFrame(final Bitmap bmp) {
        /*byte[] data = frame.getByteData();
        synchronized (this) {
            mBmp = BitmapFactory.decodeByteArray(data, 0, data.length);
        }*/

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                synchronized (this) {
                    setImageBitmap(bmp);
                }
            }
        });
    }
}

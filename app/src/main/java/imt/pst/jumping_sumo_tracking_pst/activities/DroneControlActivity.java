package imt.pst.jumping_sumo_tracking_pst.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import com.parrot.arsdk.arcommands.ARCOMMANDS_JUMPINGSUMO_MEDIARECORDEVENT_PICTUREEVENTCHANGED_ERROR_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_DEVICE_STATE_ENUM;
import com.parrot.arsdk.arcontroller.ARControllerCodec;
import com.parrot.arsdk.arcontroller.ARFeatureRc;
import com.parrot.arsdk.arcontroller.ARFrame;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;

import imt.pst.jumping_sumo_tracking_pst.R;
import imt.pst.jumping_sumo_tracking_pst.TrackingModes;
import imt.pst.jumping_sumo_tracking_pst.detectors.Detector;
import imt.pst.jumping_sumo_tracking_pst.drone.JumpingSumo;
import imt.pst.jumping_sumo_tracking_pst.views.FrameByFrameImageView;

public class DroneControlActivity extends AppCompatActivity {
    private Button[] mCommands = new Button[6];
    private TrackingModes config;
    private ARDiscoveryDeviceService mDroneService;
    private JumpingSumo mDrone;
    private String TAG = "DroneControlActivity";
    private Bitmap currentFrame;
    private Bitmap calculatedFrame;
    private FrameByFrameImageView mFrameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drone_control);

        mFrameView = findViewById(R.id.frameView);

        mCommands[0] = findViewById(R.id.forwardBt);
        mCommands[1] = findViewById(R.id.backwardBt);
        mCommands[2] = findViewById(R.id.leftBt);
        mCommands[3] = findViewById(R.id.rightBt);
        mCommands[4] = findViewById(R.id.highJumpBt);
        mCommands[5] = findViewById(R.id.longJumpBt);

        config = TrackingModes.getTrackingModeById(getIntent().getIntExtra(TrackingModeSelectionActivity.EXTRA_TRACKING_CONFIG,0));
        mDroneService = getIntent().getParcelableExtra(TrackingModeSelectionActivity.EXTRA_DEVICE_SERVICE);

        mDrone = new JumpingSumo(this, mDroneService);
        mDrone.addListener(mJSListener);

        if(config.doesUseCommands()){
            activateCommands();
        } else {
            deactivateCommands();
        }
    }



    @Override
    public void onBackPressed(){
        if(!mDrone.disconnect()){
            finish();
        }
    }

    private final JumpingSumo.Listener mJSListener = new JumpingSumo.Listener() {
        @Override
        public void onDroneConnectionChanged(ARCONTROLLER_DEVICE_STATE_ENUM state) {
            switch (state) {
                case ARCONTROLLER_DEVICE_STATE_RUNNING:
                    break;

                case ARCONTROLLER_DEVICE_STATE_STOPPED:
                    Log.i(TAG, "Drone disconnected");
                    // if the deviceController is stopped, go back to the previous activity
                    finish();
                    break;

                default:
                    break;
            }
        }

        @Override
        public void onFrameReceived(ARFrame frame) {
            byte[] data = frame.getByteData();
            //TODO: give frames to modules, print it
            currentFrame = BitmapFactory.decodeByteArray(data, 0, data.length);
            calculatedFrame = currentFrame.copy(currentFrame.getConfig(), currentFrame.isMutable());

            for(Detector detector : config.getConfig()){
                if(detector != null){
                    calculatedFrame = detector.detect(currentFrame, new byte[5]);
                }
            }

            mFrameView.displayFrame(calculatedFrame);
        }


        @Override
        public void onBatteryChargeChanged(int batteryPercentage) {
            ((TextView)findViewById(R.id.batteryLabel)).setText(String.format("%d%%", batteryPercentage));
        }

        @Override
        public void onPictureTaken(ARCOMMANDS_JUMPINGSUMO_MEDIARECORDEVENT_PICTUREEVENTCHANGED_ERROR_ENUM error) {

        }

        @Override
        public void onAudioStateReceived(boolean inputEnabled, boolean outputEnabled) {

        }

        @Override
        public void configureDecoder(ARControllerCodec codec) {

        }

        @Override
        public void configureAudioDecoder(ARControllerCodec codec) {

        }

        @Override
        public void onAudioFrameReceived(ARFrame frame) {

        }

        @Override
        public void onMatchingMediasFound(int nbMedias) {

        }

        @Override
        public void onDownloadProgressed(String mediaName, int progress) {

        }

        @Override
        public void onDownloadComplete(String mediaName) {

        }
    };

    private void activateCommands(){
        for(Button b: mCommands){
            b.setVisibility(View.VISIBLE);
        }

        findViewById(R.id.forwardBt).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.v(TAG, "" + event.getAction());
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE:
                        v.setPressed(true);
                        mDrone.setSpeed((byte) 100);
                        mDrone.setFlag((byte) 1);
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        mDrone.setSpeed((byte) 0);
                        mDrone.setFlag((byte) 0);
                        break;

                    default:

                        break;
                }

                return true;
            }
        });

        findViewById(R.id.backwardBt).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {

                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE:
                        v.setPressed(true);
                        mDrone.setSpeed((byte) -100);
                        mDrone.setFlag((byte) 1);
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        mDrone.setSpeed((byte) 0);
                        mDrone.setFlag((byte) 0);
                        break;

                    default:

                        break;
                }

                return true;
            }
        });

        findViewById(R.id.leftBt).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE:
                        v.setPressed(true);
                        mDrone.setTurn((byte) -15);
                        mDrone.setFlag((byte) 1);
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        mDrone.setTurn((byte) 0);
                        mDrone.setFlag((byte) 0);
                        break;

                    default:
                        break;
                }

                return true;
            }
        });

        findViewById(R.id.rightBt).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE:
                        v.setPressed(true);
                        mDrone.setTurn((byte) 15);
                        mDrone.setFlag((byte) 1);
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        mDrone.setTurn((byte) 0);
                        mDrone.setFlag((byte) 0);
                        break;

                    default:

                        break;
                }

                return true;
            }
        });

        findViewById(R.id.highJumpBt).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        mDrone.setJump((byte) 1);
                        mDrone.setFlag((byte) 1);
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        mDrone.setJump((byte) 0);
                        mDrone.setFlag((byte) 0);
                        break;

                    default:

                        break;
                }

                return true;
            }
        });

        findViewById(R.id.longJumpBt).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        mDrone.setJump((byte) 2);
                        mDrone.setFlag((byte) 1);
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        mDrone.setJump((byte) 0);
                        mDrone.setFlag((byte) 0);
                        break;

                    default:

                        break;
                }

                return true;
            }
        });
    }

    private void deactivateCommands(){
        for(Button b: mCommands){
            b.setVisibility(View.GONE);
        }

        findViewById(R.id.forwardBt).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        findViewById(R.id.backwardBt).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        findViewById(R.id.leftBt).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        findViewById(R.id.rightBt).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        findViewById(R.id.highJumpBt).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        findViewById(R.id.longJumpBt).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
    }
}

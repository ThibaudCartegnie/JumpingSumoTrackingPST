package imt.pst.jumping_sumo_tracking_pst.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.parrot.arsdk.ardiscovery.ARDISCOVERY_PRODUCT_ENUM;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.parrot.arsdk.ardiscovery.ARDiscoveryService;

import java.util.ArrayList;
import java.util.List;

import imt.pst.jumping_sumo_tracking_pst.drone.JumpingDiscoverer;
import imt.pst.jumping_sumo_tracking_pst.R;
import imt.pst.jumping_sumo_tracking_pst.TrackingModes;

public class TrackingModeSelectionActivity extends AppCompatActivity {

    public static final String EXTRA_DEVICE_SERVICE = "EXTRA_DEVICE_SERVICE";
    public static final String EXTRA_TRACKING_CONFIG = "EXTRA_TRACKING_CONFIG";
    private List<ARDiscoveryDeviceService> mDronesList = new ArrayList<>();
    public JumpingDiscoverer mDroneDiscoverer;
    private boolean isDroneConnected = false;
    private String TAG = "ModeSelectionActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking_mode_selection);

        ListView mList = findViewById(R.id.listView_mainmenu);

        final ListAdapter mAdapter = new ArrayAdapter<TrackingModes>(this, android.R.layout.simple_list_item_1, TrackingModes.values());
        mList.setAdapter(mAdapter);
        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(isDroneConnected){
                    ARDiscoveryDeviceService mDrone = mDronesList.get(0);

                    // Check if Drone is supported
                    ARDISCOVERY_PRODUCT_ENUM product = ARDiscoveryService.getProductFromProductID(mDrone.getProductID());
                    if (product != ARDISCOVERY_PRODUCT_ENUM.ARDISCOVERY_PRODUCT_JS && product != ARDISCOVERY_PRODUCT_ENUM.ARDISCOVERY_PRODUCT_JS_EVO_LIGHT
                            && product != ARDISCOVERY_PRODUCT_ENUM.ARDISCOVERY_PRODUCT_JS_EVO_RACE){
                        Log.e(TAG, "Drone not supported by this app.");
                        AlertDialog alertDialog = new AlertDialog.Builder(TrackingModeSelectionActivity.this).create();
                        alertDialog.setMessage("The connected drone is not supported by this app.");
                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                        alertDialog.show();
                        return;
                    }

                    Intent mIntent = new Intent(TrackingModeSelectionActivity.this, DroneControlActivity.class);
                    mIntent.putExtra(EXTRA_DEVICE_SERVICE, mDronesList.get(0));
                    mIntent.putExtra(EXTRA_TRACKING_CONFIG, ((TrackingModes)mAdapter.getItem(position)).getId());
                    startActivity(mIntent);
                    onStop();
                } else {
                    AlertDialog alertDialog = new AlertDialog.Builder(TrackingModeSelectionActivity.this).create();
                    alertDialog.setMessage("No drone found yet.");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                }
            }
        });

        mDroneDiscoverer = new JumpingDiscoverer(this);
        mDroneDiscoverer.setup();
        mDroneDiscoverer.addListener(mDiscovererListener);
        mDroneDiscoverer.startDiscovering();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v(TAG, "resume");

        checkDroneConnexion();

        mDroneDiscoverer.setup();
        mDroneDiscoverer.addListener(mDiscovererListener);
        mDroneDiscoverer.startDiscovering();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v(TAG, "pause");

        // clean the drone discoverer object
        mDroneDiscoverer.stopDiscovering();
        mDroneDiscoverer.cleanup();
        mDroneDiscoverer.removeListener(mDiscovererListener);
    }

    private final JumpingDiscoverer.Listener mDiscovererListener = new JumpingDiscoverer.Listener() {

        @Override
        public void onDronesListUpdated(List<ARDiscoveryDeviceService> dronesList) {
            mDronesList.clear();
            mDronesList.addAll(dronesList);
            checkDroneConnexion();
        }
    };

    private void checkDroneConnexion(){
        if(mDronesList.size() == 0 && isDroneConnected){
            droneDisconnected();
        } else if (mDronesList.size() != 0){
            isDroneConnected = true;
            droneConnected(mDronesList.get(0).getName());
        }
    }

    private void droneConnected(String drone_name){
        ProgressBar mProgressBar = findViewById(R.id.progressBar_waiting_drone);
        mProgressBar.setVisibility(View.GONE);

        TextView mTextView = findViewById(R.id.textView_drone_connexion);
        mTextView.setText(String.format(getString(R.string.drone_connected), drone_name));
    }

    private void droneDisconnected(){
        ProgressBar mProgressBar = findViewById(R.id.progressBar_waiting_drone);
        mProgressBar.setVisibility(View.VISIBLE);

        TextView mTextView = findViewById(R.id.textView_drone_connexion);
        mTextView.setText(R.string.loading_waiting_drone_connexion);
    }
}

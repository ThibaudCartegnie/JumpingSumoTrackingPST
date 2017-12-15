package imt.pst.jumping_sumo_tracking_pst;

import android.app.Application;
import com.parrot.arsdk.ARSDK;

/**
 * Created by Natu on 14/12/2017.
 */

public final class TrackingDroneJumpingSumoApp extends Application {
    @Override
    public void onCreate(){
        super.onCreate();
        ARSDK.loadSDKLibs();
        System.loadLibrary("opencv_java3");
    }
}

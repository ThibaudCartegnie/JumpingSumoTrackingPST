package imt.pst.jumping_sumo_tracking_pst.detectors;

import android.graphics.Bitmap;

import imt.pst.jumping_sumo_tracking_pst.drone.JumpingSumo;

/**
 * Created by Natu on 15/12/2017.
 */

public interface Detector {
    public Bitmap detect(Bitmap originalFrame, byte[] outputData);

    public void setDrone(JumpingSumo mDrone);
}

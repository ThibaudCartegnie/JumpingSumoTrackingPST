package imt.pst.jumping_sumo_tracking_pst.detectors;

import android.graphics.Bitmap;

import imt.pst.jumping_sumo_tracking_pst.drone.JumpingSumo;

/**
 * Created by Natu on 15/12/2017.
 */


public interface Detector {
    /**
     * main function of the detector, this function should run the required calculations AND run
     * the required actions on the drone
     *
     * @param originalFrame image passed by the drone camera
     * @param outputData    unused byte array
     * @return a modified Bitmap image, this SHOULD be a different
     */
    public Bitmap detect(Bitmap originalFrame, byte[] outputData);

    /**
     * set a JumpingSumo to the detector, needed for it to act on the drone
     * @param mDrone drone instantiated with the ARService
     */
    public void setDrone(JumpingSumo mDrone);
}

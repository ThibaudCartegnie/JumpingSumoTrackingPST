package imt.pst.jumping_sumo_tracking_pst.detectors;

import android.graphics.Bitmap;

/**
 * Created by Natu on 15/12/2017.
 */

public interface Detector {
    public Bitmap detect(Bitmap originalFrame, byte[] outputData);
}

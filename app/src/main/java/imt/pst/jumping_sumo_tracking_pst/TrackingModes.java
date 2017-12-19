package imt.pst.jumping_sumo_tracking_pst;

import android.util.Log;

import java.util.ArrayList;

import imt.pst.jumping_sumo_tracking_pst.detectors.CircleDetector;
import imt.pst.jumping_sumo_tracking_pst.detectors.Detector;
import imt.pst.jumping_sumo_tracking_pst.detectors.LineDetector;
import imt.pst.jumping_sumo_tracking_pst.detectors.ObstacleDetector;

/**
 * Created by Natu on 14/12/2017.
 */

public enum TrackingModes {
    MANUAL(0, "Manual Mode", true),
    CIRCLE_TRACKING(1, "Tracking Circle mode", false),
    OBSTACLE_DETECTION(2, "Detection of obstacles", true),
    LINE_DETECTION(3, "Line Detection", false);

    private static void modesSetup(){
        MANUAL.config.add(null);
        CIRCLE_TRACKING.config.add(new CircleDetector());
        OBSTACLE_DETECTION.config.add(new ObstacleDetector());
        LINE_DETECTION.config.add(new LineDetector());
    }

    static{
        modesSetup();
    }

    private int id;
    private String textView;
    private ArrayList<Detector> config;

    private boolean useCommands;

    TrackingModes(int id, String textView, boolean useCommands){
        this.textView = textView;
        this.config = new ArrayList<>();
        this.id = id;
        this.useCommands = useCommands;
    }

    public String getTextView() {
        return textView;
    }

    public ArrayList<Detector> getConfig() {
        return config;
    }

    public int getId() {
        return id;
    }

    public boolean doesUseCommands() {
        return useCommands;
    }

    @Override
    public String toString() {
        return this.textView;
    }

    public static TrackingModes getTrackingModeById(int id){
        for(TrackingModes mode : TrackingModes.values()){
            if(mode.getId() == id){
                return mode;
            }
        }
        Log.e("getTrackingMode", String.format("Mode '%d' not found, Manual returned instead.", id));
        return MANUAL;
    }
}

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
    /**
     * Manual mode, no image processing just the drone controls
     */
    MANUAL(0, "Manual Mode", true),

    //NEW_MODE(99, "Exemple of new mode withou manual commands", false),

    /**
     * Circle Tracking mode, no drone controls
     */
    CIRCLE_TRACKING(1, "Tracking Circle mode", false),

    /**
     * Obstacle Detection mode, with drone controls
     */
    OBSTACLE_DETECTION(2, "Detection of obstacles", true),

    /**
     * Line Detection mode, no drone controls
     */
    LINE_DETECTION(3, "Line Detection", false);


    /**
     * Modethod use once at setup to initialize the ArrayList of each mode
     */
    private static void modesSetup() {
        MANUAL.config.add(null);

        CIRCLE_TRACKING.config.add(new CircleDetector());

        OBSTACLE_DETECTION.config.add(new ObstacleDetector());

        LINE_DETECTION.config.add(new LineDetector());

        //NEW_MODE.config.add(new SomeDetector());
        //NEW_MODE.config.add(new SomeOtherDetector());
    }

    static {
        modesSetup();
    }

    /**
     * Unique id of the mode
     */
    private int id;
    /**
     * String containing the printed text of the mode
     */
    private String textView;
    /**
     * Detectors of the mode
     */
    private ArrayList<Detector> config;
    /**
     * Boolean whether the mode allow using manual commands
     */
    private boolean useCommands;

    /**
     *
     * @param id Unique id of the mode
     * @param textView String containing the printed text of the mode
     * @param useCommands Boolean whether the mode allow using manual commands
     */
    TrackingModes(int id, String textView, boolean useCommands) {
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

    /**
     *
     * @param id id wanted
     * @return mode wanted
     */
    public static TrackingModes getTrackingModeById(int id) {
        for (TrackingModes mode : TrackingModes.values()) {
            if (mode.getId() == id) {
                return mode;
            }
        }
        Log.e("getTrackingMode", String.format("Mode '%d' not found, Manual returned instead.", id));
        return MANUAL;
    }
}

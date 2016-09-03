package com.thinkmobiles.locationtrackingexample;

public abstract class Constants {

    public static final String ROUTE_UPDATES_KEY = "ROUTE_UPDATES_KEY";
    public static final int REQUEST_CODE_RESOLUTION = 1;

    public static final int UPDATE_INTERVAL = 10000; // 10 sec
    public static final int FASTEST_INTERVAL = 5000; // 5 sec
    public static final int DISPLACEMENT = 10; // 10 meters

    public static final float DEFAULT_ZOOM = 15f;

    public static final float GEOFENCE_RADIUS_IN_METERS = 100f;
    public static final String GEOFENCE_ACTION = "GEOFENCE_ACTION";
    public static final String GEOFENCE_TYPE = "GEOFENCE_TYPE";

}

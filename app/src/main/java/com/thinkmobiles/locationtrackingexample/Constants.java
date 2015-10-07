package com.thinkmobiles.locationtrackingexample;

/**
 * Created by klim on 01.09.15.
 */
public abstract class Constants {

    public static final String LOCATION_UPDATES_KEY = "location updates";
    public static final String ROUTE_UPDATES_KEY = "route updates";
    public static final int REQUEST_CODE_RESOLUTION = 1;

    public static final int UPDATE_INTERVAL = 10000; // 10 sec
    public static final int FASTEST_INTERVAL = 5000; // 5 sec
    public static final int DISPLACEMENT = 10; // 10 meters

    public static final float DEFAULT_ZOOM = 15f;

    public static final float GEOFENCE_RADIUS_IN_METERS = 100f;
    public static final String GEOFENCE_ACTION = "geofence action";
    public static final String GEOFENCE_TYPE = "geofence type";

    public static final Integer [] icons = {
            R.drawable.car,
            R.drawable.walker,
            R.drawable.distance
    };


}

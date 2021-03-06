package com.thinkmobiles.locationtrackingexample.data;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;
import com.thinkmobiles.locationtrackingexample.data.models.RouteInfo;
import com.thinkmobiles.locationtrackingexample.data.models.RouteResponse;
import com.thinkmobiles.locationtrackingexample.data.restapi.RestClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RouteLoader extends AsyncTaskLoader<RouteInfo> {

    public  static final int ID = 10000;
    private static final String LOCATION_PARAMS_KEY = "LOCATION_PARAMS_KEY";
    private static final String START_LOCATION_KEY = "START_LOCATION_KEY";
    private static final String TARGET_LOCATION_KEY = "TARGET_LOCATION_KEY";
    private static final String DIRECTIONS_MODE = "DIRECTIONS_MODE";

    private Map<String, Location> mParams;
    private RouteMode mMode;

    public static Bundle prepareBundle(Location start, Location target, RouteMode _mode) {
        Bundle bundle = new Bundle();
        HashMap<String, Location> map = new HashMap<String, Location>();

        map.put(RouteLoader.START_LOCATION_KEY, start);
        map.put(RouteLoader.TARGET_LOCATION_KEY, target);

        bundle.putSerializable(RouteLoader.LOCATION_PARAMS_KEY, map);
        bundle.putInt(RouteLoader.DIRECTIONS_MODE, _mode.ordinal());
        return bundle;
    }


    public RouteLoader(Context context, Bundle args) {
        super(context);
        mParams = (Map<String, Location>) args.getSerializable(LOCATION_PARAMS_KEY);
        mMode = RouteMode.values()[args.getInt(DIRECTIONS_MODE)];
    }

    @Override
    public RouteInfo loadInBackground() {
        Log.d("LOADER", "loadInBackground");
        Location startLocation = mParams.get(START_LOCATION_KEY);
        Location targetLocation = mParams.get(TARGET_LOCATION_KEY);

        switch (mMode) {
            case DISTANCE:
                return getDistance(startLocation, targetLocation);
            default:
                return getRoute(startLocation, targetLocation, mMode);
        }
    }

    private RouteInfo getDistance(Location start, Location target) {
        List<LatLng> list = new ArrayList<>();

        list.add(new LatLng(start.getLatitude(), start.getLongitude()));
        list.add(new LatLng(target.getLatitude(), target.getLongitude()));

        float dist = (int) start.distanceTo(target);
        String distance;
        if (dist >= 1000) distance = dist / 1000 + " km";
        else distance = dist + " m";

        return new RouteInfo.Builder()
                .setPoints(list)
                .setDistance(distance)
                .build();

    }

    private RouteInfo getRoute(Location start, Location target, RouteMode mode) {

        LatLng startLatLng = new LatLng(start.getLatitude(), start.getLongitude());
        LatLng targetLatLng = new LatLng(target.getLatitude(), target.getLongitude());
        try {
            RouteResponse response = RestClient.getInstance().getRoute(startLatLng, targetLatLng, mode);
            if (response.status.equals("OK")) {
                return new RouteInfo.Builder()
                        .setPoints(PolyUtil.decode(response.getPoints()))
                        .setDuration(response.getDuration())
                        .setDistance(response.getDistance())
                        .build();
            }

        } catch (Exception e) {
            Log.e("RouteLoader", "Error executing...", e);
            return null;
        }

        return null;
    }

}

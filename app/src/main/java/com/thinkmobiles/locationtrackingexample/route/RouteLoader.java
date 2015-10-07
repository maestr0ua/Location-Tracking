package com.thinkmobiles.locationtrackingexample.route;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit.RestAdapter;

/**
 * Created by klim on 16.09.15.
 */
public class RouteLoader extends AsyncTaskLoader<RouteInfo> {
    public static final String LOCATION_PARAMS_KEY = "location params key";
    public static final String START_LOCATION_KEY = "start location key";
    public static final String TARGET_LOCATION_KEY = "target location key";
    public static final String DIRECTIONS_MODE = "directions_mode";

    private Map<String, Location> mParams;
    private String mMode;

    public RouteLoader(Context _context, Bundle _args)
    {
        super(_context);
        mParams = (Map<String, Location>)_args.getSerializable(LOCATION_PARAMS_KEY);
        mMode = _args.getString(DIRECTIONS_MODE);
    }

    @Override
    public RouteInfo loadInBackground() {
        try
        {
            Location startLocation = mParams.get(START_LOCATION_KEY);
            Location targetLocation = mParams.get(TARGET_LOCATION_KEY);

            LatLng fromPosition = new LatLng(startLocation.getLatitude(), startLocation.getLongitude());
            LatLng toPosition = new LatLng(targetLocation.getLatitude(), targetLocation.getLongitude());

            if (mMode.equals("distance")) {
                List<LatLng> list = new ArrayList<>();
                list.add(fromPosition);
                list.add(toPosition);
                float dist = (int)startLocation.distanceTo(targetLocation);
                String distance;
                if (dist >= 1000) distance = dist/1000 + " km";
                else distance = dist + " m";

                RouteInfo routeInfo = new RouteInfo.Builder()
                        .setDirectionPoints(list)
                        .setDistance(distance)
                        .build();
                return routeInfo;
            } else {
                return getRoute(fromPosition, toPosition, mMode);
            }

        }
        catch (Exception e)
        {
            return null;
        }
    }

    private RouteInfo getRoute(LatLng _origin, LatLng _dest, String _mode) throws IOException {
        RouteInfo result = null;

        String str_origin = _origin.latitude + "," +_origin.longitude;
        String str_dest = _dest.latitude + "," + _dest.longitude;

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setEndpoint("https://maps.googleapis.com")
                .build();
        RouteApi routeService = restAdapter.create(RouteApi.class);
        RouteResponse routeResponse = routeService.getRoute(str_origin, str_dest, "metric", _mode);
        if (routeResponse.status.equals("OK")) {
            List<LatLng> points = PolyUtil.decode(routeResponse.getPoints());
            result = new RouteInfo.Builder()
                    .setDirectionPoints(points)
                    .setDistance(routeResponse.getDistance())
                    .setDuration(routeResponse.getDuration())
                    .build();
        }
        return result;
    }

}

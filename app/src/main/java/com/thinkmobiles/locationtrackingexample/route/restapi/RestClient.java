package com.thinkmobiles.locationtrackingexample.route.restapi;

import com.google.android.gms.maps.model.LatLng;
import com.thinkmobiles.locationtrackingexample.route.RouteMode;
import com.thinkmobiles.locationtrackingexample.route.models.RouteResponse;

import retrofit.RestAdapter;

public class RestClient {

    private static volatile RestClient instance;
    private RouteApi mRouteApi;
    private RestAdapter mAdapter;

    public static RestClient getInstance() {
        RestClient localInstance = instance;
        if (localInstance == null) {
            synchronized (RestClient.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new RestClient();
                }
            }
        }
        return localInstance;
    }

    private RestClient() {
        mAdapter = new RestAdapter.Builder()
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setEndpoint("https://maps.googleapis.com")
                .build();

        mRouteApi = mAdapter.create(RouteApi.class);
    }

    public RouteResponse getRoute(LatLng _origin, LatLng _dest, RouteMode _mode) {
        String str_origin = _origin.latitude + "," + _origin.longitude;
        String str_dest = _dest.latitude + "," + _dest.longitude;

        return mRouteApi.getRoute(str_origin, str_dest, "metric", _mode.getId());
    }

}

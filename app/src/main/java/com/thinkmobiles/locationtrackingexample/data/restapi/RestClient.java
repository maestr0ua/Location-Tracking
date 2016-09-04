package com.thinkmobiles.locationtrackingexample.data.restapi;

import com.google.android.gms.maps.model.LatLng;
import com.thinkmobiles.locationtrackingexample.Constants;
import com.thinkmobiles.locationtrackingexample.data.RouteMode;
import com.thinkmobiles.locationtrackingexample.data.models.RouteResponse;

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
                .setEndpoint(Constants.GOOGLE_API_ENDPOINT)
                .build();

        mRouteApi = mAdapter.create(RouteApi.class);
    }

    public RouteResponse getRoute(LatLng origin, LatLng dest, RouteMode mode) {
        String str_origin = origin.latitude + "," + origin.longitude;
        String str_dest = dest.latitude + "," + dest.longitude;

        return mRouteApi.getRoute(str_origin, str_dest, Constants.SYSTEM_OF_MEASUREMENT, mode.getId());
    }

}

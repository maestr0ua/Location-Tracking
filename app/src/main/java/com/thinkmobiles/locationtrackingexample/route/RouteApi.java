package com.thinkmobiles.locationtrackingexample.route;

import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by klim on 17.09.15.
 */
public interface RouteApi {

    @GET("/maps/api/directions/json")
    RouteResponse getRoute(
            @Query("origin") String position,
            @Query("destination") String destination,
            @Query("units") String units,
            @Query("mode") String mode);
}

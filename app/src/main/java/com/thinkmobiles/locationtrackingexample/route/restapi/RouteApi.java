package com.thinkmobiles.locationtrackingexample.route.restapi;

import com.thinkmobiles.locationtrackingexample.route.models.RouteResponse;

import retrofit.http.GET;
import retrofit.http.Query;

interface RouteApi {

    @GET("/maps/api/directions/json")
    RouteResponse getRoute(
            @Query("origin") String position,
            @Query("destination") String destination,
            @Query("units") String units,
            @Query("mode") String mode);
}

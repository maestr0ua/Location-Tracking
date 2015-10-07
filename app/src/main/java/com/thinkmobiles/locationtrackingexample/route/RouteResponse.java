package com.thinkmobiles.locationtrackingexample.route;

import java.util.List;

/**
 * Created by klim on 17.09.15.
 */
public final class RouteResponse {

    public List<Route> routes;
    public String status;

    class Route {
        OverviewPolyline overview_polyline;
        List<Leg> legs;
    }

    class Leg {
        legDetail distance;
        legDetail duration;
    }

    class OverviewPolyline {
        String points;
    }

    class legDetail {
        String text;
    }

    public final String getPoints() {
        return this.routes.get(0).overview_polyline.points;
    }

    public final String getDistance() {
        return routes.get(0).legs.get(0).distance.text;
    }

    public final String getDuration() {
        return routes.get(0).legs.get(0).duration.text;
    }

}
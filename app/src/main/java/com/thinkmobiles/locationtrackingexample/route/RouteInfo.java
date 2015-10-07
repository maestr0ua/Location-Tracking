package com.thinkmobiles.locationtrackingexample.route;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Created by klim on 17.09.15.
 */
public final class RouteInfo {
    private List<LatLng> mDirectionPoints;
    private String mDistance;
    private String mDuration;

    public final List<LatLng> getDirectionPoints() {
        return mDirectionPoints;
    }

    public final String getDistance() {
        return mDistance;
    }

    public final String getDuration() {
        return mDuration;
    }

    public static class Builder {
        List<LatLng> directionPoints;
        String distance;
        String duration;

        public Builder setDirectionPoints(List<LatLng> _directionPoints) {
            directionPoints = _directionPoints;
            return this;
        }

        public Builder setDuration(String _duration) {
            duration = _duration;
            return this;
        }

        public Builder setDistance(String _distance) {
            distance = _distance;
            return this;
        }

        public RouteInfo build() {
            RouteInfo routeInfo = new RouteInfo(this);
            return routeInfo;
        }

    }

    private RouteInfo(Builder _builder) {
        mDirectionPoints = _builder.directionPoints;
        mDistance = _builder.distance;
        mDuration = _builder.duration;
    }

}

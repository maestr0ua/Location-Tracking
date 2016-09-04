package com.thinkmobiles.locationtrackingexample.data.models;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public final class RouteInfo {

    private List<LatLng> mDirectionPoints;
    private String mDistance;
    private String mDuration;

    private RouteInfo(Builder builder) {
        mDirectionPoints = builder.points;
        mDistance = builder.distance;
        mDuration = builder.duration;
    }

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
        List<LatLng> points;
        String distance;
        String duration;

        public Builder setPoints(List<LatLng> points) {
            this.points = points;
            return this;
        }

        public Builder setDuration(String duration) {
            this.duration = duration;
            return this;
        }

        public Builder setDistance(String distance) {
            this.distance = distance;
            return this;
        }

        public RouteInfo build() {
            RouteInfo routeInfo = new RouteInfo(this);
            return routeInfo;
        }

    }

}

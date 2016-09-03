package com.thinkmobiles.locationtrackingexample.ui;

import android.graphics.Color;
import android.location.Location;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.thinkmobiles.locationtrackingexample.Constants;
import com.thinkmobiles.locationtrackingexample.R;

import java.util.ArrayList;
import java.util.List;

public final class MapController {

    private GoogleMap mMap;
    private List<Circle> mCircleList = new ArrayList();
    private Marker mStartMarker, mTargetMarker;
    private Polyline mRoutePolyline;

    public MapController(final GoogleMap _map) {
        mMap = _map;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setOnInfoWindowClickListener(Marker::hideInfoWindow);
    }

    public final void drawCircle(final LatLng _position) {
        double radiusInMeters = 100.0;
        int strokeColor = 0xffff0000;
        int shadeColor = 0x44ff0000;

        final CircleOptions circleOptions = new CircleOptions()
                .center(_position)
                .radius(radiusInMeters)
                .fillColor(shadeColor)
                .strokeColor(strokeColor)
                .strokeWidth(8);

        Circle circle = mMap.addCircle(circleOptions);
        mCircleList.add(circle);
    }

    private MarkerOptions prepareMarker(Location location, boolean isStartLocation) {
        final LatLng myLocation = new LatLng(location.getLatitude(), location.getLongitude());

        MarkerOptions marker = new MarkerOptions().position(myLocation);
        marker.title("Latitude: " + location.getLatitude() + ", Longitude: " + location.getLongitude());
        marker.snippet("Altitude: " + String.format("%.2f", location.getAltitude()) + ", Accuracy: " + location.getAccuracy());

        if (isStartLocation)
            marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker));
        return marker;
    }

    private void moveCamera(Location location) {
        float currentZoomLevel = Constants.DEFAULT_ZOOM;
        if (mMap.getCameraPosition().zoom > Constants.DEFAULT_ZOOM) {
            currentZoomLevel = mMap.getCameraPosition().zoom;
        }
        final LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, currentZoomLevel);
        mMap.animateCamera(cameraUpdate);
    }

    public final void drawTargetMarker(LatLng latLng) {
        if (mTargetMarker == null) {
            Location location = new Location("TargetLocation");
            location.setLatitude(latLng.latitude);
            location.setLongitude(latLng.longitude);
            mTargetMarker = mMap.addMarker(prepareMarker(location, false));
        } else {
            mTargetMarker.setPosition(latLng);
        }

    }

    public final void drawStartMarker(Location location) {
        if (mStartMarker == null) {
            mStartMarker = mMap.addMarker(prepareMarker(location, true));
        } else {
            final LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

            mStartMarker.setPosition(latLng);

            mStartMarker.setTitle("Latitude: " + location.getLatitude() + ", Longitude: " +
                    location.getLongitude());

            mStartMarker.setSnippet("Altitude: " + String.format("%.2f", location.getAltitude()) +
                    ", Accuracy: " + location.getAccuracy());
        }
        moveCamera(location);
    }

    public final void showRoute(List<LatLng> _points) {
        if (mRoutePolyline == null) {
            PolylineOptions rectLine = new PolylineOptions().width(8).color(Color.BLUE);
            rectLine.addAll(_points);
            mRoutePolyline = mMap.addPolyline(rectLine);
        } else {
            mRoutePolyline.setPoints(_points);
        }

    }

    public final void deleteCircles() {
        for (Circle circle : mCircleList) {
            circle.remove();
        }
        mCircleList.clear();
    }

    public final void deleteRoute() {
        if (mTargetMarker != null)
            mTargetMarker.remove();
        mTargetMarker = null;

        if (mRoutePolyline != null) {
            mRoutePolyline.remove();
            mRoutePolyline = null;
        }
    }

    public final Location getStart() {
        if (mStartMarker == null)
            return null;

        Location location = new Location("");
        location.setLatitude(mStartMarker.getPosition().latitude);
        location.setLongitude(mStartMarker.getPosition().longitude);
        return location;
    }

    public final Location getTarget() {
        if (mTargetMarker == null)
            return null;

        Location location = new Location("");
        location.setLatitude(mTargetMarker.getPosition().latitude);
        location.setLongitude(mTargetMarker.getPosition().longitude);
        return location;
    }

    public boolean hasTarget() {
        return getTarget() != null;
    }

    public boolean hasStart() {
        return getStart() != null;
    }

}

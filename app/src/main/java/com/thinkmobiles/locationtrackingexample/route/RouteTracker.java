package com.thinkmobiles.locationtrackingexample.route;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.thinkmobiles.locationtrackingexample.Constants;
import com.thinkmobiles.locationtrackingexample.route.location.GeofenceTransitionsIntentService;
import com.thinkmobiles.locationtrackingexample.route.location.LocationChangedListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class RouteTracker implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleApiClient mGoogleApiClient;
    private Activity mActivity;

    private boolean mLocationUpdatesStarted = false;
    private boolean mGeofenceUpdatesStarted = false;

    private boolean mIsInResolution;
    private LocationRequest mLocationRequest;
    private LocationChangedListener mCallBack;
    private List<Geofence> mGeofencesList = new ArrayList<>();
    private PendingIntent mGeofencePendingIntent;
    private CopyOnWriteArrayList<LatLng> mGeofencesCoordinates = new CopyOnWriteArrayList<>();



    public final void connect(Activity activity) {

        this.mActivity = activity;

        mGoogleApiClient = new GoogleApiClient.Builder(mActivity)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        if (mGoogleApiClient != null)
            mGoogleApiClient.connect();
    }

    public final void disconnect() {
        if (mGoogleApiClient != null) {
            if (mLocationUpdatesStarted) {
                stopLocationUpdates();
            }
            if (mGeofenceUpdatesStarted) {
                stopGeofenceMonitoring();
            }
            mGoogleApiClient.disconnect();
        }
    }

    public final void retryConnecting() {
        mIsInResolution = false;
        if (!mGoogleApiClient.isConnecting()) {
            mGoogleApiClient.connect();
        }
    }

    public final void setOnLocationChangedListener(LocationChangedListener callBack) {
        mCallBack = callBack;
    }

    @Override
    public final void onConnected(Bundle bundle) {
        createLocationRequest();
        startLocationUpdates();

        if (mGeofenceUpdatesStarted) {
            stopGeofenceMonitoring();
        }

        for (LatLng latLng : mGeofencesCoordinates) {
            addGeofence(latLng);
        }

    }

    @Override
    public final void onConnectionSuspended(int i) {
        retryConnecting();
    }

    @Override
    public final void onConnectionFailed(ConnectionResult _result) {
        if (!_result.hasResolution()) {
            GooglePlayServicesUtil.getErrorDialog(
                    _result.getErrorCode(), mActivity, 0, dialog -> retryConnecting()).show();
            return;
        }
        if (mIsInResolution) {
            return;
        }
        mIsInResolution = true;
        try {
            _result.startResolutionForResult(mActivity, Constants.REQUEST_CODE_RESOLUTION);
        } catch (IntentSender.SendIntentException e) {
            retryConnecting();
        }

    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(Constants.UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(Constants.FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(Constants.DISPLACEMENT);
    }

    public final void startLocationUpdates() {
        mLocationUpdatesStarted = true;
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    public final void stopLocationUpdates() {
        mLocationUpdatesStarted = false;
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    public final void onLocationChanged(Location location) {
        if (mCallBack != null) {
            mCallBack.onLocationChanged(location);
        }
    }

    public void setGeofences(List<LatLng> geofences) {
        mGeofencesCoordinates.clear();
        mGeofencesCoordinates.addAll(geofences);
    }

    private void addGeofence(LatLng location) {
        mGeofenceUpdatesStarted = true;
        if (location != null) {
            mGeofencesList.add(new Geofence.Builder()
                    .setRequestId("geofence " + mGeofencesList.size())
                    .setCircularRegion(
                            location.latitude,
                            location.longitude,
                            Constants.GEOFENCE_RADIUS_IN_METERS)
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build());
        }

        LocationServices.GeofencingApi.addGeofences(
                mGoogleApiClient,
                getGeofencingRequest(),
                getGeofencePendingIntent()
        );
    }

    public final void startGeofenceMonitoring(LatLng location) {
        mGeofencesCoordinates.add(location);
        addGeofence(location);
    }

    public final void stopGeofenceMonitoring() {
        mGeofenceUpdatesStarted = false;
        LocationServices.GeofencingApi.removeGeofences(
                mGoogleApiClient,
                getGeofencePendingIntent()
        );
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofencesList);
        return builder.build();
    }

    private PendingIntent getGeofencePendingIntent() {
        if (mGeofencePendingIntent == null) {
            Intent intent = new Intent(mActivity, GeofenceTransitionsIntentService.class);
            mGeofencePendingIntent = PendingIntent.getService(mActivity, 0, intent, PendingIntent.
                    FLAG_UPDATE_CURRENT);
        }
        return mGeofencePendingIntent;

    }

    public void deleteGeofences() {
        mGeofencesList.clear();
    }

    public ArrayList<LatLng> getGeofences() {
        return new ArrayList<>(mGeofencesCoordinates);
    }
}

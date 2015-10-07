package com.thinkmobiles.locationtrackingexample.location;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.DialogInterface;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by klim on 02.09.15.
 */
public final class TrackingService implements GoogleApiClient.ConnectionCallbacks,
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

    public TrackingService(Activity _activity) {
        mActivity = _activity;

        mGoogleApiClient = new GoogleApiClient.Builder(mActivity)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    public final void connect() {
        if (mGoogleApiClient != null)
            mGoogleApiClient.connect();

    }

    public final void disconnect() {
        if (mGoogleApiClient != null) {
            if (mLocationUpdatesStarted) {
                stopLocationUpdates();
                mLocationUpdatesStarted = true;
            }
            if (mGeofenceUpdatesStarted) {
                stopGeofenceMonitoring();
                mGeofenceUpdatesStarted = true;
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

    public final void setOnLocationChangedListener(LocationChangedListener _callBack) {
        mCallBack = _callBack;
    }

    @Override
    public final void onConnected(Bundle bundle) {
        createLocationRequest();
        if (mLocationUpdatesStarted) {
            startLocationUpdates();
        }
        if (mGeofenceUpdatesStarted) {
            startGeofenceMonitoring(null);
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
                    _result.getErrorCode(), mActivity, 0, new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            retryConnecting();
                        }
                    }).show();
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
    public final void onLocationChanged(Location _location) {
        if (mCallBack != null) {
            mCallBack.onLocationChanged(_location);
        }
    }

    public final void startGeofenceMonitoring(LatLng _location) {
        mGeofenceUpdatesStarted = true;
        if (_location != null) {
            mGeofencesList.add(new Geofence.Builder()
                    .setRequestId("geofence " + mGeofencesList.size())

                    .setCircularRegion(
                            _location.latitude,
                            _location.longitude,
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
}

package com.thinkmobiles.locationtrackingexample.route.location;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.thinkmobiles.locationtrackingexample.Constants;

public class GeofenceTransitionsIntentService extends IntentService {

    private final static String TAG = "GeofenceService";

    public GeofenceTransitionsIntentService() {
        super("GeofenceTransitionsIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            String errorMessage = GeofenceErrorMessages.getErrorString(this,
                    geofencingEvent.getErrorCode());
            Log.e(TAG, errorMessage);
            return;
        }

        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            Intent i = new Intent();
            i.setAction(Constants.GEOFENCE_ACTION);
            i.putExtra(Constants.GEOFENCE_TYPE, geofenceTransition);
            sendBroadcast(i);
        }
    }

}

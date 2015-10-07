package com.thinkmobiles.locationtrackingexample.location;

import android.content.Context;
import android.content.res.Resources;

import com.google.android.gms.location.GeofenceStatusCodes;
import com.thinkmobiles.locationtrackingexample.R;

/**
 * Created by klim on 22.09.15.
 */
public final class GeofenceErrorMessages {

    /**
     * Returns the error string for a geofencing error code.
     */
    public static final String getErrorString(Context _context, int _errorCode) {
        Resources mResources = _context.getResources();
        switch (_errorCode) {
            case GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE:
                return mResources.getString(R.string.geofence_not_available);
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES:
                return mResources.getString(R.string.geofence_too_many_geofences);
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
                return mResources.getString(R.string.geofence_too_many_pending_intents);
            default:
                return mResources.getString(R.string.unknown_geofence_error);
        }
    }
}

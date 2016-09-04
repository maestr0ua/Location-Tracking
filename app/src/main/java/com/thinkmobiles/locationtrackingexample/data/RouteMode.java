package com.thinkmobiles.locationtrackingexample.data;

import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;

import com.thinkmobiles.locationtrackingexample.R;

public enum RouteMode {

    DISTANCE("distance"), DRIVING("driving"), WALKING("walking");

    private final String id;

    RouteMode(String s) {
        id = s;
    }

    public String getId() {
        return id;
    }

    @StringRes
    public static int getTitle(int mode) {

        switch (values()[mode]) {
            case DISTANCE:
                return R.string.distance;
            case WALKING:
                return R.string.walking;
            case DRIVING:
                return R.string.driving;
            default:
                return 0;
        }
    }

    @DrawableRes
    public static int getIcon(int mode) {

        switch (values()[mode]) {
            case DISTANCE:
                return R.drawable.ic_distance;
            case WALKING:
                return R.drawable.ic_walker;
            case DRIVING:
                return R.drawable.ic_car;
            default:
                return 0;
        }
    }

}

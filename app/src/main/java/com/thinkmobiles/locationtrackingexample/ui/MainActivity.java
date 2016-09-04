package com.thinkmobiles.locationtrackingexample.ui;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.thinkmobiles.locationtrackingexample.Constants;
import com.thinkmobiles.locationtrackingexample.R;
import com.thinkmobiles.locationtrackingexample.data.location.LocationChangedListener;
import com.thinkmobiles.locationtrackingexample.data.RouteTracker;
import com.thinkmobiles.locationtrackingexample.data.models.RouteInfo;
import com.thinkmobiles.locationtrackingexample.data.RouteLoader;
import com.thinkmobiles.locationtrackingexample.data.RouteMode;
import com.thinkmobiles.locationtrackingexample.ui.spinner.SpinnerAdapter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
        GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener,
        LocationChangedListener, LoaderManager.LoaderCallbacks<RouteInfo> {

    private GeofenceReceiver mGeofenceReceiver;

    private GoogleMap mMap;
    private Toolbar mToolbar;
    private ViewGroup vgRouteInfo;
    private TextView tvDistance, tvDuration;
    private Spinner mRouteSpinner;

    private RouteMode mCurrentMode = RouteMode.DRIVING;

    private MapController mMapController;
    private RouteTracker mRouteTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRouteTracker = new RouteTracker();
        mRouteTracker.setOnLocationChangedListener(this);

        findUI();
        setupUI();
    }

    private void findUI() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        vgRouteInfo = (ViewGroup) findViewById(R.id.route_info_layout);
        tvDistance = (TextView) findViewById(R.id.tvDistance);
        tvDuration = (TextView) findViewById(R.id.tvDuration);
    }

    private void setupUI() {
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayShowTitleEnabled(false);

        vgRouteInfo.setVisibility(View.GONE);
        setupMap();
        setupModeSpinner();
    }

    private void setupModeSpinner() {
        View spinnerContainer = LayoutInflater.from(this).inflate(R.layout.toolbar_spinner,
                mToolbar, false);
        ActionBar.LayoutParams lp = new ActionBar.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mToolbar.addView(spinnerContainer, lp);

        SpinnerAdapter spinnerAdapter = new SpinnerAdapter(this);

        List<RouteMode> modes = new ArrayList<>();
        modes.add(RouteMode.DRIVING);
        modes.add(RouteMode.WALKING);
        modes.add(RouteMode.DISTANCE);

        spinnerAdapter.addItems(modes);
        mRouteSpinner = (Spinner) spinnerContainer.findViewById(R.id.toolbar_spinner);
        mRouteSpinner.setAdapter(spinnerAdapter);
        mRouteSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mCurrentMode = RouteMode.values()[i];
                updateRoute(true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        mRouteSpinner.setSelection(mCurrentMode.ordinal());
    }

    private void setupMap() {
        mMap = ((SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_AM))
                .getMap();

        mMap.setOnMapClickListener(this);
        mMap.setOnMapLongClickListener(this);
        mMapController = new MapController(mMap);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(Constants.ROUTE_MODE_KEY, mCurrentMode.ordinal());
        outState.putParcelable(Constants.START_LOCATION_KEY, mMapController.getStart());
        outState.putParcelable(Constants.TARGET_LOCATION_KEY, mMapController.getTarget());
        outState.putString(Constants.DISTANCE_KEY, tvDistance.getText().toString());
        outState.putString(Constants.DURATION_KEY, tvDuration.getText().toString());
        outState.putParcelableArrayList(Constants.GEOFENCES_KEY, mRouteTracker.getGeofences());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState != null) {
            mCurrentMode = RouteMode.values()[savedInstanceState.getInt(Constants.ROUTE_MODE_KEY)];
            tvDistance.setText(savedInstanceState.getString(Constants.DISTANCE_KEY));
            tvDuration.setText(savedInstanceState.getString(Constants.DURATION_KEY));

            Location start = savedInstanceState.getParcelable(Constants.START_LOCATION_KEY);
            if (start != null) {
                mMapController.setStart(start);
            }

            Location target = savedInstanceState.getParcelable(Constants.TARGET_LOCATION_KEY);
            if (target != null) {
                mMapController.setTarget(new LatLng(target.getLatitude(), target.getLongitude()));
            }

            List<LatLng> geofences = savedInstanceState.getParcelableArrayList(Constants.GEOFENCES_KEY);
            mRouteTracker.setGeofences(geofences);
            for (LatLng latLng : geofences) {
                mMapController.drawCircle(latLng);
            }

        }

        vgRouteInfo.setVisibility(mMapController.hasTarget() ? View.VISIBLE : View.GONE);
        updateRoute(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_clear_map:
                clearMap();
                return true;
            case R.id.menu_clear_geofences:
                clearGeofences();
                return true;
            case R.id.menu_clear_route:
                clearRoute();
                return true;
        }
        return false;
    }

    @Override
    public void onStart() {
        super.onStart();
        mRouteTracker.connect(this);
        mGeofenceReceiver = new GeofenceReceiver();
        IntentFilter intentFilter = new IntentFilter(Constants.GEOFENCE_ACTION);
        registerReceiver(mGeofenceReceiver, intentFilter);

    }

    @Override
    public void onStop() {
        mRouteTracker.disconnect();
        if (mGeofenceReceiver != null) {
            unregisterReceiver(mGeofenceReceiver);
            mGeofenceReceiver = null;
        }
        super.onStop();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Constants.REQUEST_CODE_RESOLUTION:
                mRouteTracker.retryConnecting();
                break;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mMapController.setStart(location);
        if (mMapController.hasStart() & mMapController.hasTarget()) {
            updateRoute(true);
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {
        mMapController.deleteRoute();
        mMapController.setTarget(latLng);
        updateRoute(true);
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        mRouteTracker.startGeofenceMonitoring(latLng);
        mMapController.drawCircle(latLng);
    }

    @Override
    public Loader<RouteInfo> onCreateLoader(int id, Bundle args) {
        return new RouteLoader(this, args);
    }

    @Override
    public void onLoadFinished(Loader<RouteInfo> loader, RouteInfo data) {
        if (data != null) {
            showRouteInfo(data);
        } else
            Toast.makeText(this, R.string.route_error, Toast.LENGTH_SHORT);
    }

    @Override
    public void onLoaderReset(Loader<RouteInfo> _loader) {

    }


    private void updateRoute(boolean restart) {
        if (mMapController.hasStart() & mMapController.hasTarget()) {

            Bundle args = RouteLoader.prepareBundle(mMapController.getStart(),
                    mMapController.getTarget(),
                    RouteMode.values()[mRouteSpinner.getSelectedItemPosition()]);

            if (restart) {
                getSupportLoaderManager()
                        .restartLoader(RouteLoader.ID, args, MainActivity.this)
                        .forceLoad();
            } else {
                getSupportLoaderManager()
                        .initLoader(RouteLoader.ID, args, MainActivity.this)
                        .forceLoad();
            }
        }
    }


    private void showRouteInfo(RouteInfo route) {
        mMapController.showRoute(route.getDirectionPoints());
        vgRouteInfo.setVisibility(View.VISIBLE);

        tvDistance.setText(route.getDistance());

        if (!TextUtils.isEmpty(route.getDuration())) {
            tvDuration.setText(" ~" + route.getDuration());
        } else {
            tvDuration.setText("");
        }
    }

    private void clearMap() {
        clearRoute();
        clearGeofences();
    }

    private void clearGeofences() {
        mMapController.deleteCircles();
        mRouteTracker.stopGeofenceMonitoring();
    }

    private void clearRoute() {
        mMapController.deleteRoute();
        vgRouteInfo.setVisibility(View.GONE);
    }

    private class GeofenceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constants.GEOFENCE_ACTION)) {
                final int geofenceTransition = intent.getIntExtra(Constants.GEOFENCE_TYPE, -1);

                if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                    Toast.makeText(MainActivity.this, "Enter to geofence", Toast.LENGTH_SHORT).show();
                }
                if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
                    Toast.makeText(MainActivity.this, "Exit from geofence", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
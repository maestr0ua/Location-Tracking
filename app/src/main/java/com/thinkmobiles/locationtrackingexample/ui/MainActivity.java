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
import com.thinkmobiles.locationtrackingexample.route.location.LocationChangedListener;
import com.thinkmobiles.locationtrackingexample.route.RouteTracker;
import com.thinkmobiles.locationtrackingexample.route.models.RouteInfo;
import com.thinkmobiles.locationtrackingexample.route.RouteLoader;
import com.thinkmobiles.locationtrackingexample.route.RouteMode;
import com.thinkmobiles.locationtrackingexample.ui.spinner.SpinnerAdapter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
        GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener,
        LocationChangedListener, LoaderManager.LoaderCallbacks<RouteInfo> {

    private GoogleMap mMap;
    private RouteTracker mRouteTracker;

    private Toolbar mToolbar;
    private ViewGroup vRouteInfo;
    private TextView tvDistance, tvDuration;
    private Spinner mRouteSpinner;

    private boolean mRouteModeStarted = false;
    private GeofenceReceiver mGeofenceReceiver;
    private MapController mMapController;

    @Override
    protected void onCreate(Bundle _savedInstanceState) {
        super.onCreate(_savedInstanceState);
        setContentView(R.layout.activity_main);

        if (_savedInstanceState != null) {
            mRouteModeStarted = _savedInstanceState.getBoolean(Constants.ROUTE_UPDATES_KEY);
        } else {
            mRouteTracker = new RouteTracker();
        }

        findUI();
        setUpUI();

        mRouteTracker.setOnLocationChangedListener(this);
    }

    private void findUI() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        vRouteInfo = (ViewGroup) findViewById(R.id.route_info_layout);

        tvDistance = (TextView) findViewById(R.id.tvDistance);
        tvDuration = (TextView) findViewById(R.id.tvDuration);

    }

    private void setUpUI() {
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayShowTitleEnabled(false);

        vRouteInfo.setVisibility(View.GONE);
        setupModeSpinner();
        setUpMapIfNeeded();
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
        mRouteSpinner.setSelection(0);
        mRouteSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (mRouteModeStarted) getRoute();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void setUpMapIfNeeded() {
        if (mMap == null) {
            mMap = ((SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map_AM))
                    .getMap();

            mMap.setOnMapClickListener(this);
            mMap.setOnMapLongClickListener(this);

            mMapController = new MapController(mMap);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(Constants.ROUTE_UPDATES_KEY, mRouteModeStarted);
        super.onSaveInstanceState(outState);
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
    public void onActivityResult(int _requestCode, int _resultCode, Intent _data) {
        super.onActivityResult(_requestCode, _resultCode, _data);
        switch (_requestCode) {
            case Constants.REQUEST_CODE_RESOLUTION:
                mRouteTracker.retryConnecting();
                break;
        }
    }

    @Override
    public void onLocationChanged(Location _location) {
        mMapController.drawStartMarker(_location);
        if (mRouteModeStarted) {
            getRoute();
        }
    }

    @Override
    public void onMapClick(LatLng _latLng) {
        mMapController.deleteRoute();
        mMapController.drawEndMarker(_latLng);
        mRouteModeStarted = true;
        getRoute();
    }

    @Override
    public void onMapLongClick(LatLng _latLng) {
        mRouteTracker.startGeofenceMonitoring(_latLng);
        mMapController.drawCircle(_latLng);
    }

    private void getRoute() {
        Bundle bundle = RouteLoader.prepareBundle(mMapController.getStartPosition(),
                mMapController.getEndPosition(),
                RouteMode.values()[mRouteSpinner.getSelectedItemPosition()]);

        getSupportLoaderManager()
                .restartLoader(1, bundle, this)
                .forceLoad();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem _item) {
        switch (_item.getItemId()) {
            case R.id.menu_clear_map:
                mRouteModeStarted = false;
                clearMap();
                return true;
            case R.id.menu_clear_geofences:
                clearGeofences();
                return true;
            case R.id.menu_clear_route:
                mRouteModeStarted = false;
                clearRoute();
                return true;
        }
        return false;
    }

    private void clearMap() {
        clearRoute();
        clearGeofences();
    }

    private void clearGeofences() {
        mMapController.deleteCircles();
        mRouteTracker.stopGeofenceMonitoring();
        mRouteTracker.deleteGeofences();
    }

    private void clearRoute() {
        mMapController.deleteRoute();
        vRouteInfo.setVisibility(View.GONE);
    }

    @Override
    public Loader<RouteInfo> onCreateLoader(int _id, Bundle _args) {
        return new RouteLoader(this, _args);
    }

    @Override
    public void onLoadFinished(Loader<RouteInfo> loader, RouteInfo data) {
        if (data != null) {
            showRouteInfo(data);
        }  else
            Toast.makeText(this, R.string.route_error, Toast.LENGTH_SHORT);
    }

    @Override
    public void onLoaderReset(Loader<RouteInfo> _loader) {

    }

    private void showRouteInfo(RouteInfo route) {
        mMapController.showRoute(route.getDirectionPoints());
        vRouteInfo.setVisibility(View.VISIBLE);

        tvDistance.setText(route.getDistance());

        if (!TextUtils.isEmpty(route.getDuration())) {
            tvDuration.setText(" ~" + route.getDuration());
        } else {
            tvDuration.setText("");
        }
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
package com.thinkmobiles.locationtrackingexample;


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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.thinkmobiles.locationtrackingexample.location.LocationChangedListener;
import com.thinkmobiles.locationtrackingexample.location.TrackingService;
import com.thinkmobiles.locationtrackingexample.route.RouteInfo;
import com.thinkmobiles.locationtrackingexample.route.RouteLoader;
import com.thinkmobiles.locationtrackingexample.spinner.SpinnerAdapter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import rx.Observable;
import rx.Subscriber;

public class MainActivity extends AppCompatActivity implements
        GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener,
        LocationChangedListener, LoaderManager.LoaderCallbacks<RouteInfo> {

    private GoogleMap mMap;
    private TrackingService mTrackingService;
    private View vRouteInfo;
    private TextView tvDistance, tvDuration;
    private Spinner mRouteSpinner;
    private MenuItem mMenuItemRoute;

    private boolean mRouteModeStarted = false;
    private boolean mLocationUpdatesStarted = false;
    private GeofenceReciever mGeofenceReciever;
    private MapHelper mMapHelper;

    @Override
    protected void onCreate(Bundle _savedInstanceState) {
        super.onCreate(_savedInstanceState);
        setContentView(R.layout.activity_main);

        if (_savedInstanceState != null) {
            mLocationUpdatesStarted = _savedInstanceState.getBoolean(Constants.LOCATION_UPDATES_KEY);
            mRouteModeStarted = _savedInstanceState.getBoolean(Constants.ROUTE_UPDATES_KEY);
        } else {
            mTrackingService = new TrackingService(this);
        }
        setUpUI();

        mTrackingService.setOnLocationChangedListener(this);

    }

    private void setUpUI() {
        setUpMapIfNeeded();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayShowTitleEnabled(false);

        vRouteInfo = findViewById(R.id.route_info_layout);
        vRouteInfo.setVisibility(View.GONE);
        tvDistance = (TextView) findViewById(R.id.tvDistance);
        tvDuration = (TextView) findViewById(R.id.tvDuration);

        View spinnerContainer = LayoutInflater.from(this).inflate(R.layout.toolbar_spinner,
                toolbar, false);
        ActionBar.LayoutParams lp = new ActionBar.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        toolbar.addView(spinnerContainer, lp);

        SpinnerAdapter spinnerAdapter = new SpinnerAdapter(this);
        spinnerAdapter.addItems(Arrays.asList(getResources().getStringArray(R.array.route_mode_name)));
        mRouteSpinner = (Spinner) spinnerContainer.findViewById(R.id.toolbar_spinner);
        mRouteSpinner.setAdapter(spinnerAdapter);
    }

    private void setUpMapIfNeeded() {
        if (mMap == null) {
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_AM))
                    .getMap();
            mMap.setOnMapClickListener(this);
            mMap.setOnMapLongClickListener(this);
            mMapHelper = new MapHelper(mMap);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(Constants.LOCATION_UPDATES_KEY, mLocationUpdatesStarted);
        outState.putBoolean(Constants.ROUTE_UPDATES_KEY, mRouteModeStarted);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onStart() {
        super.onStart();
        mTrackingService.connect();
        if (mGeofenceReciever == null) {
            mGeofenceReciever = new GeofenceReciever();
            IntentFilter intentFilter = new IntentFilter(Constants.GEOFENCE_ACTION);
            registerReceiver(mGeofenceReciever, intentFilter);
        }
    }

    @Override
    public void onStop() {
        mTrackingService.disconnect();
        if (mGeofenceReciever != null) {
            unregisterReceiver(mGeofenceReciever);
            mGeofenceReciever = null;
        }
        super.onStop();
    }

    @Override
    public void onActivityResult(int _requestCode, int _resultCode, Intent _data) {
        super.onActivityResult(_requestCode, _resultCode, _data);
        switch (_requestCode) {
            case Constants.REQUEST_CODE_RESOLUTION:
                mTrackingService.retryConnecting();
                break;
        }
    }

    @Override
    public void onLocationChanged(Location _location) {
        mMapHelper.drawStartMarker(_location);
        if (mRouteModeStarted) {
            getRoute();
        }
    }

    @Override
    public void onMapClick(LatLng _latLng) {
        mMapHelper.drawEndMarker(_latLng);
        mRouteModeStarted = false;
        mMenuItemRoute.setVisible(true);
    }

    @Override
    public void onMapLongClick(LatLng _latLng) {

        mTrackingService.startGeofenceMonitoring(_latLng);
        mMapHelper.drawCircle(_latLng);
    }

    private void getRoute() {
        String[] routeModeArray = getResources().getStringArray(R.array.route_mode_name);
        String mode = routeModeArray[mRouteSpinner.getSelectedItemPosition()];

        Bundle bundle = prepareRouteData(mMapHelper.getStartPosition(),
                                        mMapHelper.getEndPosition(),
                                        mode);

        getSupportLoaderManager().restartLoader(1, bundle, this).forceLoad();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu _menu) {
        getMenuInflater().inflate(R.menu.main_menu, _menu);
        _menu.findItem(R.id.menu_location_updates).setIcon(mLocationUpdatesStarted? R.drawable.stop: R.drawable.start);
        mMenuItemRoute = _menu.findItem(R.id.menu_route);
        mMenuItemRoute.setVisible(false);
        return super.onCreateOptionsMenu(_menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem _item) {
        switch (_item.getItemId()) {
            case R.id.menu_location_updates:
                if (mLocationUpdatesStarted) {
                    mLocationUpdatesStarted = false;
                    _item.setIcon(R.drawable.start);
                    mTrackingService.stopLocationUpdates();
                } else {
                    mLocationUpdatesStarted = true;
                    _item.setIcon(R.drawable.stop);
                    mTrackingService.startLocationUpdates();
                }
                break;
            case R.id.menu_route:
                mRouteModeStarted = true;
                mMenuItemRoute.setVisible(false);
                getRoute();
                break;
            case R.id.menu_clear_map:
                mMapHelper.deleteAll();
                mTrackingService.stopLocationUpdates();
                mTrackingService.stopGeofenceMonitoring();
                vRouteInfo.setVisibility(View.GONE);
                tvDistance.setText("");
                tvDuration.setText("");
                mMenuItemRoute.setVisible(false);
                mRouteModeStarted = false;
                break;
            case R.id.menu_clear_geofences:
                mMapHelper.deleteCircles();
                mTrackingService.stopGeofenceMonitoring();
                mTrackingService.deleteGeofences();
                break;
            case R.id.menu_clear_route:
                mMapHelper.deleteRoute();
                mRouteModeStarted = false;
                mMenuItemRoute.setVisible(true);
                break;
        }
        return true;
    }

    public Bundle prepareRouteData(Location _start, Location _end, String _mode) {
        Bundle bundle = new Bundle();
        HashMap<String, Location> map = new HashMap<String, Location>();

        map.put(RouteLoader.START_LOCATION_KEY, _start);
        map.put(RouteLoader.TARGET_LOCATION_KEY, _end);

        bundle.putSerializable(RouteLoader.LOCATION_PARAMS_KEY, map);
        bundle.putString(RouteLoader.DIRECTIONS_MODE, _mode);
        return bundle;
    }

    @Override
    public Loader<RouteInfo> onCreateLoader(int _id, Bundle _args) {
        return new RouteLoader(this, _args);
    }

    @Override
    public void onLoadFinished(Loader<RouteInfo> _loader, RouteInfo _data) {
        if (_data != null) {
            showRouteInfo(_data.getDirectionPoints(), _data.getDistance(), _data.getDuration());
        } else {
            mMenuItemRoute.setVisible(true);
        }
    }

    @Override
    public void onLoaderReset(Loader<RouteInfo> _loader) {

    }

    private void showRouteInfo(List<LatLng> _points, String _distance, String _duration) {
        mMapHelper.showRoute(_points);
        vRouteInfo.setVisibility(View.VISIBLE);
        tvDistance.setText(_distance);
        tvDuration.setText("~" + _duration);
    }

    private class GeofenceReciever extends BroadcastReceiver {

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
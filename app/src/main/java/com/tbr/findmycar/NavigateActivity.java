package com.tbr.findmycar;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


public class NavigateActivity extends FragmentActivity implements SensorEventListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    private GoogleApiClient mGoogleApiClient = null;
    private static final String dbgTag = "NavigateActivity";
    public static final double RADIAN_TO_DEGREE = 57.2957795;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;
    private TextView mSensorInfo;

    private float[] mGravity;
    private float[] mGeomagnetic;
    private float mAzimut;

    private GoogleMap mMapFragment;

    private SharedPreferences mSP;
    private Location mSavedLocation;
    private Marker mCurrentLocationMarker;
    private Marker mSavedLocationMarker;
    private Location mCurrentLocation;
    private LocationRequest mLocationRequest;

    public void onSensorChanged(SensorEvent event){
        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            mGravity = event.values;
        if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            mGeomagnetic = event.values;

        if(mGravity != null && mGeomagnetic != null){
            float R[] = new float[9];
            float I[] = new float[9];
            boolean flag = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
            if(flag){
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                mAzimut = orientation[0];
                mSensorInfo.setText("Result: " + mAzimut);
                setArrowDirection();
            }
        }
    }

    public void buildGoogleApiClient() {
        Log.i(dbgTag, "Build google api client start");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks((GoogleApiClient.ConnectionCallbacks) this)
                .addOnConnectionFailedListener((GoogleApiClient.OnConnectionFailedListener) this)
                .addApi(LocationServices.API)
                .build();

        Log.i(dbgTag, "Build google api client done " + mGoogleApiClient.toString());
    }

    private void setArrowDirection() {
//        mDirectionArrowImageView.setRotation((float) (mAzimut * 57.2957795));
        if (mCurrentLocationMarker != null) {
            Log.i(dbgTag, "before marker set rotation");
            mCurrentLocationMarker.setRotation((float) (mAzimut * RADIAN_TO_DEGREE));
            Log.i(dbgTag, "after marker set rotation");
        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy){
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigate);

        buildGoogleApiClient();
        mSP = getSharedPreferences("FindMyCar", Context.MODE_PRIVATE);
        mSensorInfo = (TextView) findViewById(R.id.sensorInfo);

        // deal with sensor manager
        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        // deal with mMapFragment fragment
        mMapFragment = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

    }


    @Override
    protected void onResume(){
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause(){
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    private void initializeMap() {
        // Enable or disable current position

        // Initialize type of mMapFragment
        mMapFragment.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        // Initialize traffic overlay
        mMapFragment.setTrafficEnabled(false);

        // Enable rotation gestures
        mMapFragment.getUiSettings().setRotateGesturesEnabled(true);

        // change camera to current position
        LatLng loc = getLatLngFromLocation(LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient));
        mMapFragment.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 18));

        // enable my loc
        // mMapFragment.setMyLocationEnabled(true);
        putMarkersForCurrentAndSavedLocation();
    }

    private LatLng getLatLngFromLocation(Location location) {
        return new LatLng(location.getLatitude(), location.getLongitude());
    }

    private void putMarkersForCurrentAndSavedLocation() {
        mSavedLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        mSavedLocationMarker = mMapFragment.addMarker(new MarkerOptions()
                        .position(getLatLngFromLocation(mSavedLocation))
                        .title("MyCar")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.car))
        );
        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        mCurrentLocationMarker = mMapFragment.addMarker(new MarkerOptions()
                .position(getLatLngFromLocation(mSavedLocation))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.direction_arrow_32)));
    }


    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(4000);
        mLocationRequest.setFastestInterval(1200);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (mMapFragment != null) {
            initializeMap();
        }
        if (mLocationRequest == null) {
            createLocationRequest();
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, (com.google.android.gms.location.LocationListener) this);
        }
    }


    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(dbgTag, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }


    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(dbgTag, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();

    }

    @Override
    protected void onStop() {
        super.onStop();
//        if (mGoogleApiClient.isConnected()) {
//            mGoogleApiClient.disconnect();
//        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        LatLng loc = getLatLngFromLocation(mCurrentLocation);
        mCurrentLocationMarker.setPosition(loc);
    }
}

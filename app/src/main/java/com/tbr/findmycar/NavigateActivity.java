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
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;


public class NavigateActivity extends FragmentActivity implements SensorEventListener{

    private static final String dbgTag = "NavigateActivity";
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;
    private TextView mSensorInfo;

    private float[] mGravity;
    private float[] mGeomagnetic;
    private float mAzimut;

    private GoogleMap map;
    private ImageView mDirectionArrowImageView;
    private TextView mSavedLocationAndLevelTextView;
    private SharedPreferences mSP;
    private Location mLastLocation;

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

    private void setArrowDirection() {
        mDirectionArrowImageView.setRotation((float) (mAzimut * 57.2957795));
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy){
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigate);

        mSP = getSharedPreferences("FindMyCar", Context.MODE_PRIVATE);
        mLastLocation = getIntent().getExtras().getParcelable("lastLocation");
        mSavedLocationAndLevelTextView = (TextView) findViewById(R.id.savedLocationAndLevelTextView);
        setSavedLocationAndLevelTextViewText();
        mDirectionArrowImageView = (ImageView) findViewById(R.id.directionArrowImageView);
        mSensorInfo = (TextView) findViewById(R.id.sensorInfo);

        // deal with sensor manager
        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        // deal with map fragment
        /*
        map = ((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMap();
        if(map != null){
            initializeMap();
        }*/
    }

    private void setSavedLocationAndLevelTextViewText() {
        String savedLocationAndLevelText = "Level: " + mSP.getString("level", "not found") + "\n"
                + "Longitude: " + mLastLocation.getLongitude() + "\n"
                + "Latitude: " + mLastLocation.getLatitude();

        mSavedLocationAndLevelTextView.setText(savedLocationAndLevelText);

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_navigate, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initializeMap(){
        // Enable or disable current position

        // Initialize type of map
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        // Initialize traffic overlay
        map.setTrafficEnabled(false);

        // Enable rotation gestures
        map.getUiSettings().setRotateGesturesEnabled(true);
    }
}

package com.tbr.findmycar;

import android.content.Intent;
import android.location.Location;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;


public class StoreLocationActivity extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient = null;
    private static final String dbgTag = "StoreLocationActivity";
    private Location mLastLocation = null;
    private String mSavedLevel = null;
    private Button mSaveLocationButton;
    private Spinner mChooseLevelSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_location);
        mSaveLocationButton = (Button) findViewById(R.id.saveLocationButton);
        mChooseLevelSpinner = (Spinner) findViewById(R.id.chooseLevelSpinner);
        setupChooseLevelSpinner();
        buildGoogleApiClient();
        setupSaveLocationButton();

    }

    private void setupChooseLevelSpinner() {
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.parkingLotLevels, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mChooseLevelSpinner.setAdapter(adapter);
    }

    protected void buildGoogleApiClient() {
        Log.i(dbgTag, "Build google api client start");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        Log.i(dbgTag, "Build google api client done " + mGoogleApiClient.toString());
    }

    private void setupSaveLocationButton() {
        Log.i(dbgTag, "Setup save location button start");
        mSaveLocationButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //TODO: display spinner
                StoreLocationActivity.this.storeLocationAndLevel();
                StoreLocationActivity.this.proceedToNavigateScreen();
            }
        });
        Log.i(dbgTag, "Setup save location button done");
    }

    private void proceedToNavigateScreen() {
        Log.i(dbgTag, "in proceedToNavigateScreen");
        Intent intent = new Intent();
        intent.setClass(this, NavigateActivity.class);
        startActivity(intent);
    }

    private void storeLocationAndLevel() {
        storeLocationAndLevelToAttribute();
        storeLocationAndLevelToSharedPreference();
    }

    private void storeLocationAndLevelToSharedPreference() {
        //TODO
    }

    private void storeLocationAndLevelToAttribute() {
        mSavedLevel = mChooseLevelSpinner.getSelectedItem().toString();
        Log.i(dbgTag, "Level saved " + mSavedLevel);
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            Log.i(dbgTag, "Location retrieve success");
            Log.i(dbgTag, "Latitude" + String.valueOf(mLastLocation.getLatitude()));
            Log.i(dbgTag, "Longitude" + String.valueOf(mLastLocation.getLongitude()));
        }
        else {
            Log.i(dbgTag, "Location retrieve failed");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_store_location, menu);
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

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(dbgTag, "Connected");
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
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }
}

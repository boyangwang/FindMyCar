package com.tbr.findmycar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;


public class StoreLocationActivity extends Activity {


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
        mSavedLevel = mChooseLevelSpinner.getSelectedItem().toString();
        SharedPreferences sp = getSharedPreferences("FindMyCar", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("level", mSavedLevel);
        editor.apply();
    }


}

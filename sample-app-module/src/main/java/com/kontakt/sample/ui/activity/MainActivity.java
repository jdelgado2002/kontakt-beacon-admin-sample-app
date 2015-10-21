package com.kontakt.sample.ui.activity;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.kontakt.sample.R;
import com.kontakt.sample.ui.activity.monitor.AllBeaconsMonitorActivity;
import com.kontakt.sample.ui.activity.range.IBeaconRangeActivity;
import com.kontakt.sample.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class MainActivity extends BaseActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final int REQUEST_CODE_ENABLE_BLUETOOTH = 121;
    private static final String API_ENDPOINT = "https://sheetsu.com/apis/7682b5db";
    /* Client used to interact with Google APIs. */
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private String mLongitudeText = "";
    private String mLatitudeText = "";


    private String getLastKnownLocation() {return (this.mLatitudeText + ", " + this.mLongitudeText);}

    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        ButterKnife.inject(this);

        setUpActionBar(toolbar);
        setUpActionBarTitle(getString(R.string.app_name));
        //creates google api client instance to get location services
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mGoogleApiClient.connect();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_CODE_ENABLE_BLUETOOTH && resultCode == RESULT_OK) {
            startActivity(new Intent(MainActivity.this, BackgroundScanActivity.class));
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.reset(this);
    }

    @OnClick(R.id.show_lastknown_location)
    void showListOfLastKnownLocationForBeacons(){

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, API_ENDPOINT, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Utils.showToast(getApplicationContext(),"Response: " + response.getJSONArray("result").toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub

                    }
                });

// Add the request to the queue
        Volley.newRequestQueue(this).add(jsObjRequest);
    }

    @OnClick(R.id.range_beacons)
    void startRanging() {
        Intent intent = new Intent(MainActivity.this, IBeaconRangeActivity.class);
        Bundle bundle = getLastKnownLocationBundle();
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @OnClick(R.id.range_all_beacons)
    void startRangingAllBeacons() {
        Intent intent = new Intent(MainActivity.this, AllBeaconsMonitorActivity.class);
        Bundle bundle = getLastKnownLocationBundle();
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @NonNull
    private Bundle getLastKnownLocationBundle() {
        Bundle bundle = new Bundle();
        bundle.putString("LastKnownLocation", this.getLastKnownLocation());
        return bundle;
    }

    @Override
    public void onConnected(Bundle bundle) {
        boolean isConnected = mGoogleApiClient.isConnected();
        if (!isConnected){mGoogleApiClient.connect();}

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);

        if (mLastLocation != null) {
            mLatitudeText = (String.valueOf(mLastLocation.getLatitude()));
            mLongitudeText = (String.valueOf(mLastLocation.getLongitude()));
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}

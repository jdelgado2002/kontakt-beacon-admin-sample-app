package com.kontakt.sample.ui.activity;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.kontakt.sample.R;
import com.kontakt.sample.ui.activity.monitor.AllBeaconsMonitorActivity;
import com.kontakt.sample.ui.activity.monitor.EddystoneMonitorActivity;
import com.kontakt.sample.ui.activity.monitor.IBeaconMonitorActivity;
import com.kontakt.sample.ui.activity.range.BeaconRangeSyncableActivity;
import com.kontakt.sample.ui.activity.range.EddystoneBeaconRangeActivity;
import com.kontakt.sample.ui.activity.range.IBeaconRangeActivity;
import com.kontakt.sdk.android.ble.util.BluetoothUtils;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class MainActivity extends BaseActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final int REQUEST_CODE_ENABLE_BLUETOOTH = 121;
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

    @OnClick(R.id.range_beacons)
    void startRanging() {
        Intent intent = new Intent(MainActivity.this, IBeaconRangeActivity.class);
        Bundle bundle = getLastKnownLocationBundle();
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @OnClick(R.id.monitor_beacons)
    void startMonitoring() {
        startActivity(new Intent(MainActivity.this, IBeaconMonitorActivity.class));
    }

    @OnClick(R.id.multiple_proximity_manager)
    void startSimultaneousScans() {
        startActivity(new Intent(MainActivity.this, SimultaneousScanActivity.class));
    }

    @OnClick(R.id.syncable_connection)
    void startRangeWithSyncableConnection() {
        startActivity(new Intent(MainActivity.this, BeaconRangeSyncableActivity.class));
    }

    @OnClick(R.id.background_scan)
    void startForegroundBackgroundScan() {

        if (BluetoothUtils.isBluetoothEnabled()) {
            startActivity(new Intent(MainActivity.this, BackgroundScanActivity.class));
        } else {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_CODE_ENABLE_BLUETOOTH);
        }
    }

    @OnClick(R.id.range_eddystone)
    void startRangingEddystone() {
        startActivity(new Intent(MainActivity.this, EddystoneBeaconRangeActivity.class));
    }

    @OnClick(R.id.monitor_eddystone)
    void startMonitorEddystone() {
        startActivity(new Intent(MainActivity.this, EddystoneMonitorActivity.class));
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

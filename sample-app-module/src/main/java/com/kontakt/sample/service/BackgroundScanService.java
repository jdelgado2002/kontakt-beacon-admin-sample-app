package com.kontakt.sample.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import com.kontakt.sample.ui.activity.BackgroundScanActivity;
import com.kontakt.sdk.android.ble.configuration.BeaconActivityCheckConfiguration;
import com.kontakt.sdk.android.ble.configuration.ForceScanConfiguration;
import com.kontakt.sdk.android.ble.configuration.ScanContext;
import com.kontakt.sdk.android.ble.connection.OnServiceBoundListener;
import com.kontakt.sdk.android.ble.device.IBeaconDevice;
import com.kontakt.sdk.android.ble.device.IRegion;
import com.kontakt.sdk.android.ble.discovery.IBeaconAdvertisingPacket;
import com.kontakt.sdk.android.ble.filter.CustomFilter;
import com.kontakt.sdk.android.ble.manager.BeaconManager;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class BackgroundScanService extends Service implements BeaconManager.MonitoringListener {

    public static final String BROADCAST = String.format("%s.%s", BackgroundScanService.class.getName(), "BROADCAST");

    public static final String EXTRA_BEACON = "extra_new_beacon";

    public static final String EXTRA_INFO = "extra_info";

    public static final String EXTRA_REGION = "extra_region";

    public static final int INFO_REGION_ENTERED = 100;
    public static final int INFO_REGION_ABANDONED = 121;
    public static final int INFO_BEACON_APPEARED = 144;
    public static final int INFO_SCAN_STARTED = 169;
    public static final int INFO_SCAN_STOPPED = 196;

    public static final List<Integer> INFO_LIST;

    private static final double ACCEPT_DISTANCE = 1.5;//[m]

    static {
        INFO_LIST = Collections.unmodifiableList(Arrays.asList(INFO_BEACON_APPEARED,
                INFO_REGION_ABANDONED,
                INFO_REGION_ENTERED,
                INFO_SCAN_STARTED,
                INFO_SCAN_STOPPED));
    }

    private final Messenger serviceMessenger = new Messenger(new ServiceHandler());

    private BeaconManager deviceManager;

    private final ScanContext scanContext = new ScanContext.Builder()
            .setScanMode(BeaconManager.SCAN_MODE_BALANCED)
            .setBeaconActivityCheckConfiguration(BeaconActivityCheckConfiguration.DEFAULT)
            .setForceScanConfiguration(ForceScanConfiguration.DEFAULT)
            .addIBeaconFilter(new CustomFilter() {
                @Override
                public boolean apply(IBeaconAdvertisingPacket iBeaconAdvertisingPacket) {
                    final UUID proximityUUID = iBeaconAdvertisingPacket.getProximityUUID();
                    final double distance = iBeaconAdvertisingPacket.getDistance();

                    return proximityUUID.equals(BeaconManager.DEFAULT_KONTAKT_BEACON_PROXIMITY_UUID) && distance <= ACCEPT_DISTANCE;
                }
            }).build();
    ;

    @Override
    public void onCreate() {
        super.onCreate();

        deviceManager = new BeaconManager(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return serviceMessenger.getBinder();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        deviceManager.disconnect();
        deviceManager = null;
    }

    private void startMonitoring() {
        if (!deviceManager.isConnected()) {
            deviceManager.connect(new OnServiceBoundListener() {
                @Override
                public void onServiceBound() throws RemoteException {
                    deviceManager.attachListener(BackgroundScanService.this);
                    deviceManager.initializeScan();
                }
            });
        }
    }

    @Override
    public void onMonitorStart() {
        scheduleBroadcast(new BroadcastBuilder()
                .setInfo(INFO_SCAN_STARTED)
                .build());
    }

    @Override
    public void onMonitorStop() {
        scheduleBroadcast(new BroadcastBuilder()
                .setInfo(INFO_SCAN_STOPPED)
                .build());
    }

    @Override
    public void onIBeaconsUpdated(IRegion region, List<IBeaconDevice> beaconDevices) {
        /*You can send broadcast with entire list of Beacon devices.
        * However, please be aware of Bundle limitations.*/
    }

    @Override
    public void onIBeaconAppeared(IRegion region, IBeaconDevice beaconDevice) {
        scheduleBroadcast(new BroadcastBuilder()
                .setInfo(INFO_BEACON_APPEARED)
                .setBeaconDevice(beaconDevice)
                .setRegion(region)
                .build());
    }

    @Override
    public void onRegionEntered(IRegion region) {
        scheduleBroadcast(new BroadcastBuilder()
                .setInfo(INFO_REGION_ENTERED)
                .setRegion(region)
                .build());
    }

    @Override
    public void onRegionAbandoned(IRegion region) {
        scheduleBroadcast(new BroadcastBuilder()
                .setInfo(INFO_REGION_ABANDONED)
                .setRegion(region)
                .build());
    }

    private void stopMonitoring() {
        try {
            deviceManager.detachListener(this);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        deviceManager.finishScan();
    }

    private void scheduleBroadcast(Intent intent) {
        sendOrderedBroadcast(intent, null);
    }

    private class ServiceHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {

            final int messageCode = msg.what;

            switch (messageCode) {

                case BackgroundScanActivity.MESSAGE_START_SCAN:
                    startMonitoring();
                    break;

                case BackgroundScanActivity.MESSAGE_STOP_SCAN:
                    stopMonitoring();
                    break;

                default:
                    throw new IllegalArgumentException("Unsupported message Id: " + messageCode);
            }
        }
    }

    private static class BroadcastBuilder {
        private int info;
        private IRegion region;
        private IBeaconDevice beaconDevice;

        public BroadcastBuilder setInfo(int info) {
            this.info = info;
            return this;
        }

        public BroadcastBuilder setBeaconDevice(IBeaconDevice beaconDevice) {
            this.beaconDevice = beaconDevice;
            return this;
        }

        public BroadcastBuilder setRegion(IRegion region) {
            this.region = region;
            return this;
        }

        Intent build() {
            final Intent broadcast = new Intent(BROADCAST);
            broadcast.putExtra(EXTRA_BEACON, beaconDevice);
            broadcast.putExtra(EXTRA_INFO, info);
            broadcast.putExtra(EXTRA_REGION, region);

            return broadcast;
        }
    }
}

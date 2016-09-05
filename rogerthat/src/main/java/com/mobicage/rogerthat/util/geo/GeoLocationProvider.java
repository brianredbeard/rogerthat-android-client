/*
 * Copyright 2016 Mobicage NV
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @@license_version:1.1@@
 */

package com.mobicage.rogerthat.util.geo;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.telephony.NeighboringCellInfo;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;

import com.mobicage.rogerthat.MainService;
import com.mobicage.rogerthat.config.Configuration;
import com.mobicage.rogerthat.config.ConfigurationProvider;
import com.mobicage.rogerthat.config.Reconfigurable;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.system.SystemUtils;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rpc.SDCardLogger;
import com.mobicage.to.activity.CellTowerTO;
import com.mobicage.to.activity.GeoPointWithTimestampTO;
import com.mobicage.to.activity.LocationRecordTO;
import com.mobicage.to.activity.RawLocationInfoTO;
import com.mobicage.to.system.SettingsTO;

// XXX: check that location feature is available on device
public class GeoLocationProvider implements Reconfigurable, Closeable {

    public static final int LOCATION_FACTOR = 1000000;

    private final static String CONFIGKEY = "com.mobicage.rogerthat.util.geo.GeoLocationProvider";

    private final static String CONFIG_USE_GPS_WHILE_ON_BATTERY_KEY = "useGPSWhileOnBattery";
    private final static boolean CONFIG_USE_GPS_WHILE_ON_BATTERY_DEFAULTVALUE = false;

    private final static String CONFIG_USE_GPS_WHILE_CHARGING_KEY = "useGPSWhileCharging";
    private final static boolean CONFIG_USE_GPS_WHILE_CHARGING_DEFAULTVALUE = true;

    private final static String WAKELOCK_TAG = "com.mobicage.rogerthat.util.geo.GeoLocationProvider";

    private final static long WAKELOCK_TIMEOUT_MILLIS = 30 * 1000;

    private final static int GSMCELLLOCATION_ILLEGAL_CID = -1;
    private final static int GSMCELLLOCATION_ILLEGAL_LAC = -1;

    private final LocationManager mLocationManager;
    private final GeoLocationListener mGPSGeoLocationListener;
    private final GeoLocationListener mNetworkGeoLocationListener;
    private final TelephonyManager mTelephonyManager;
    private int mPhoneType;
    private final MainService mService;
    private final PowerManager mPowerManager;
    private final ConfigurationProvider mConfigProvider;
    private final List<GeoLocationCallback> mGeoLocationCallbacks;
    private WakeLock mWakeLock;

    // All members owned by UI thread
    private boolean mUseGPSWhileOnBattery = false;
    private boolean mUseGPSWhileCharging = false;
    private boolean mMustUseGPS = false;
    private Location mCachedNetworkLocation;
    private boolean mWaitingForGPSLocation = false;
    private boolean mWaitingForNetworkLocation = false;
    private boolean mLookupInProgress = false;
    private SDCardLogger mSDCardLogger;

    public GeoLocationProvider(MainService pService, ConfigurationProvider pConfigProvider, SDCardLogger pLogger) {
        T.UI();
        mService = pService;
        mConfigProvider = pConfigProvider;
        mLocationManager = (LocationManager) mService.getSystemService(Context.LOCATION_SERVICE);
        mTelephonyManager = (TelephonyManager) mService.getSystemService(Context.TELEPHONY_SERVICE);
        mPowerManager = (PowerManager) mService.getSystemService(Context.POWER_SERVICE);
        mGeoLocationCallbacks = new ArrayList<GeoLocationCallback>();
        mGPSGeoLocationListener = new GeoLocationListener(mGPSGeoLocationReceiver);
        mNetworkGeoLocationListener = new GeoLocationListener(mNetworkGeoLocationReceiver);
        mSDCardLogger = pLogger;

        initialize();
    }

    protected void bugLog(String s) {
        if (mSDCardLogger == null) {
            L.bug(s);
        } else {
            mSDCardLogger.bug(s);
        }
    }

    protected void bugLog(Exception e) {
        if (mSDCardLogger == null) {
            L.bug(e);
        } else {
            mSDCardLogger.bug(e);
        }
    }

    protected void bugLog(String s, Exception e) {
        if (mSDCardLogger == null) {
            L.bug(s, e);
        } else {
            mSDCardLogger.bug(s, e);
        }
    }

    protected void debugLog(String s, Exception e) {
        if (mSDCardLogger == null) {
            L.d(s, e);
        } else {
            mSDCardLogger.d(s, e);
        }
    }

    protected void debugLog(String s) {
        if (mSDCardLogger == null) {
            L.d(s);
        } else {
            mSDCardLogger.d(s);
        }
    }

    protected void debugLog(Exception e) {
        if (mSDCardLogger == null) {
            L.d(e);
        } else {
            mSDCardLogger.d(e);
        }
    }

    private void initialize() {
        T.UI();

        reconfigure();

        if (SystemUtils.isRunningOnRealDevice(mService)) {
            if (mTelephonyManager != null) {
                mPhoneType = mTelephonyManager.getPhoneType();
            } else {
                mPhoneType = TelephonyManager.PHONE_TYPE_NONE;
            }
            mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKELOCK_TAG);
        } else {
            // Do nothing
            // Could maybe initialize lastLocation with some valid test location
            // Or try to connect to DDMS
            mPhoneType = TelephonyManager.PHONE_TYPE_NONE;
        }

        mConfigProvider.registerListener(CONFIGKEY, this);
    }

    @Override
    public void close() {
        T.UI();
        mConfigProvider.unregisterListener(CONFIGKEY, this);
        clearCallbacks();
    }

    public GeoPointWithTimestampTO getLastKnownGeoPointWithTimestampTO() {

        Location gpsLocation;
        try {
            gpsLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        } catch (SecurityException e) {
            L.bug(e);
            return null;
        }
        Location networkLocation;
        try {
            networkLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        } catch (SecurityException e) {
            L.bug(e);
            return null;
        }

        Location chosenLocation;
        if (gpsLocation != null) {
            if (networkLocation != null) {
                chosenLocation = (gpsLocation.getTime() > networkLocation.getTime()) ? gpsLocation : networkLocation;
            } else {
                chosenLocation = gpsLocation;
            }
        } else {
            chosenLocation = networkLocation;
        }

        if (chosenLocation != null) {
            final GeoPointWithTimestampTO g = new GeoPointWithTimestampTO();
            g.accuracy = (long) chosenLocation.getAccuracy();
            g.latitude = (long) (chosenLocation.getLatitude() * 1000000);
            g.longitude = (long) (chosenLocation.getLongitude() * 1000000);
            g.timestamp = chosenLocation.getTime();
            return g;
        } else {
            return null;
        }

    }

    public void requestLocationUpdate(GeoLocationCallback callback) {
        T.UI();
        if (SystemUtils.isRunningOnRealDevice(mService) && mLocationManager != null) {
            mGeoLocationCallbacks.add(callback);
            if (!mLookupInProgress) {
                mLookupInProgress = true;
                requestGuaranteedLocationUpdate();
            }
        }
    }

    private void clearCallbacks() {
        T.UI();
        mGeoLocationCallbacks.clear();
        mLookupInProgress = false;
    }

    private void notifyCallbacksSuccess(LocationRecordTO locationRecordTO) {
        T.UI();
        debugLog("GeoLocationProvider notifyCallbacksSuccess");
        for (final GeoLocationCallback callback : mGeoLocationCallbacks) {
            try {
                callback.onLocationReceived(locationRecordTO);
            } catch (Exception e) {
                bugLog(e);
            }
        }
        clearCallbacks();
    }

    private void notifyCallbacksError() {
        T.UI();
        debugLog("GeoLocationProvider notifyCallbacksError");
        for (final GeoLocationCallback callback : mGeoLocationCallbacks) {
            try {
                callback.onError();
            } catch (Exception e) {
                bugLog(e);
            }
        }
        clearCallbacks();
    }

    private void setGPSUsage(final boolean pUseGPSWhileOnBattery, final boolean pUseGPSWhileCharging) {
        T.UI();
        mUseGPSWhileOnBattery = pUseGPSWhileOnBattery;
        mUseGPSWhileCharging = pUseGPSWhileCharging;
        calculateGPSUsage();
    }

    private void calculateGPSUsage() {
        T.UI();
        boolean batteryPowered = mService.isBatteryPowered();
        mMustUseGPS = ((mUseGPSWhileOnBattery && batteryPowered) || (mUseGPSWhileCharging && !batteryPowered));
    }

    private void requestGuaranteedLocationUpdate() {
        // We know that mLocationManager != null
        T.UI();
        debugLog("GeoLocationProvider requestGuaranteedLocationUpdate");

        mCachedNetworkLocation = null;

        if (mMustUseGPS && mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            //noinspection ResourceType
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mGPSGeoLocationListener);
            mWaitingForGPSLocation = true;
        } else {
            cancelGPSUpdates();
            mWaitingForGPSLocation = false;
        }

        // XXX: assumption is that if there is no mobile data and no wifi
        // connection, that
        // this provider will not be enabled. Should be checked though.
        if (mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            //noinspection ResourceType
            mLocationManager
                .requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mNetworkGeoLocationListener);
            mWaitingForNetworkLocation = true;
        } else {
            cancelNetworkLocationUpdates();
            mWaitingForNetworkLocation = false;
        }

        if (mWaitingForGPSLocation || mWaitingForNetworkLocation) {
            mWakeLock.acquire(); // Lock will be released by callbacks or by
            // timeout
            mService.postDelayedOnUIHandler(mWakeLockReleaser, WAKELOCK_TIMEOUT_MILLIS);
        } else {
            // Synchronous path; no lock needed
            debugLog("No location providers available. Reverting to raw location");
            submitRawLocationInfo();
        }

    }

    private final GeoLocationReceiver mGPSGeoLocationReceiver = new GeoLocationReceiver() {
        @Override
        public void onLocationError() {
            T.UI();
            debugLog("GeoLocationProvider mGPSGeoLocationReceiver.onLocationError");
            // I received an error during GPS location info retrieval
            // If I dont wait for network anymore and there is a cached network
            // location, submit it
            // If I dont wait for network anymore but there is no cached network
            // location, submit raw location info
            mWaitingForGPSLocation = false;
            cancelGPSUpdates();
            if (!mWaitingForNetworkLocation) {
                if (mCachedNetworkLocation != null) {
                    submitReceivedLocation(mCachedNetworkLocation);
                } else {
                    submitRawLocationInfo();
                }
                releaseWakeLockIfHeld();
            }
        }

        @Override
        public void onLocationReceived(Location location) {
            T.UI();
            debugLog("GeoLocationProvider mGPSGeoLocationReceiver.onLocationReceived");
            mWaitingForGPSLocation = false;
            cancelGPSUpdates();
            if (location != null) {
                // I received a good GPS location
                // Submit it right away
                mWaitingForNetworkLocation = false;
                cancelNetworkLocationUpdates();
                submitReceivedLocation(location);
                releaseWakeLockIfHeld();
            } else {
                onLocationError();
            }
        }
    };

    private final GeoLocationReceiver mNetworkGeoLocationReceiver = new GeoLocationReceiver() {

        @Override
        public void onLocationError() {
            T.UI();
            debugLog("GeoLocationProvider mNetworkGeoLocationReceiver.onLocationError");
            // Error receiving network location
            // If I dont wait for GPS, do a raw location post
            mWaitingForNetworkLocation = false;
            cancelNetworkLocationUpdates();
            if (!mWaitingForGPSLocation) {
                submitRawLocationInfo();
                releaseWakeLockIfHeld();
            }
        }

        @Override
        public void onLocationReceived(Location location) {
            T.UI();
            debugLog("GeoLocationProvider mNetworkGeoLocationReceiver.onLocationReceived");
            if (location != null) {
                // I receive a good network location
                // If I dont wait for GPS, then submit it
                mWaitingForNetworkLocation = false;
                cancelNetworkLocationUpdates();
                mCachedNetworkLocation = location;
                if (!mWaitingForGPSLocation) {
                    submitReceivedLocation(mCachedNetworkLocation);
                    releaseWakeLockIfHeld();
                }
            } else {
                onLocationError();
            }
        }
    };

    private void submitReceivedLocation(Location location) {
        T.UI();
        debugLog("GeoLocationProvider submitReceivedLocation");
        debugLog("Updated location lat=" + location.getLatitude() + "/long=" + location.getLongitude() + "/acc="
            + location.getAccuracy());

        com.mobicage.to.activity.GeoPointTO p = new com.mobicage.to.activity.GeoPointTO();
        p.longitude = (long) (location.getLongitude() * LOCATION_FACTOR);
        p.latitude = (long) (location.getLatitude() * LOCATION_FACTOR);
        p.accuracy = (long) location.getAccuracy();

        LocationRecordTO locationRecordTO = new LocationRecordTO();
        locationRecordTO.geoPoint = p;
        locationRecordTO.timestamp = location.getTime() / 1000;
        notifyCallbacksSuccess(locationRecordTO);
    }

    private void cancelGPSUpdates() {
        T.UI();
        try {
            mLocationManager.removeUpdates(mGPSGeoLocationListener);
        } catch (SecurityException e) {
            L.bug(e);
        }
    }

    private void cancelNetworkLocationUpdates() {
        T.UI();
        try{
            mLocationManager.removeUpdates(mNetworkGeoLocationListener);
        } catch (SecurityException e) {
            L.bug(e);
        }
    }

    private void releaseWakeLockIfHeld() {
        T.UI();
        mService.removeFromUIHandler(mWakeLockReleaser);
        if (mWakeLock.isHeld()) {
            mWakeLock.release();
        }
    }

    private SafeRunnable mWakeLockReleaser = new SafeRunnable() {
        @Override
        public void safeRun() {
            T.UI();
            try {
                debugLog("GeoLocationProvider mWakeLockReleaser.safeRun");
                debugLog("No location received; wakelock timed out!");

                cancelGPSUpdates();
                cancelNetworkLocationUpdates();

                if (mCachedNetworkLocation != null) {
                    submitReceivedLocation(mCachedNetworkLocation);
                    mCachedNetworkLocation = null;
                } else {
                    submitRawLocationInfo();
                }
                releaseWakeLockIfHeld();
            } catch (Exception e) {
                notifyCallbacksError();
            }
        }
    };

    private void submitRawLocationInfo() {
        T.UI();
        debugLog("GeoLocationProvider submitRawLocationInfo");
        long now = mService.currentTimeMillis();
        RawLocationInfoTO rawLocationInfoTO = getRawGSMLocationInfo();
        if (rawLocationInfoTO != null) {
            debugLog("Updated raw location cid=" + rawLocationInfoTO.cid + " lac=" + rawLocationInfoTO.lac
                + " strength=" + rawLocationInfoTO.signalStrength);
            LocationRecordTO rawLocationRecordTO = new LocationRecordTO();
            rawLocationRecordTO.timestamp = now / 1000;
            rawLocationRecordTO.rawLocation = rawLocationInfoTO;
            notifyCallbacksSuccess(rawLocationRecordTO);
        } else {
            debugLog("Error - cannot retrieve any location");
            notifyCallbacksError();
        }

    }

    /**
     * Get raw GSM location I.e. the net, cid, lac and optionally a number of cell towers and their strength. See
     * {@link http://eng.xakep.ru/link/50814/}
     */
    private RawLocationInfoTO getRawGSMLocationInfo() {
        T.UI();
        debugLog("GeoLocationProvider getRawGSMLocationInfo");
        if (mPhoneType == TelephonyManager.PHONE_TYPE_GSM) {
            try {
                // We are certain that (mTelephonyManager != null) since (mPhoneType == PHONE_TYPE_GSM)
                RawLocationInfoTO rawLocationInfoTO = new RawLocationInfoTO();
                GsmCellLocation cell = (GsmCellLocation) mTelephonyManager.getCellLocation();
                if (cell != null) {
                    rawLocationInfoTO.cid = cell.getCid();
                    rawLocationInfoTO.lac = cell.getLac();
                    if ((rawLocationInfoTO.cid != GSMCELLLOCATION_ILLEGAL_CID)
                        && (rawLocationInfoTO.lac != GSMCELLLOCATION_ILLEGAL_LAC)) {
                        rawLocationInfoTO.net = Integer.valueOf(mTelephonyManager.getNetworkOperator());
                        rawLocationInfoTO.mobileDataType = mTelephonyManager.getNetworkType();
                        rawLocationInfoTO.signalStrength = -1;

                        List<NeighboringCellInfo> cellTowersInfo = mTelephonyManager.getNeighboringCellInfo();

                        // Filter out celltowers with UNKNOWN_CID
                        List<NeighboringCellInfo> goodCellTowers = new ArrayList<NeighboringCellInfo>();
                        for (NeighboringCellInfo cellTowerInfo : cellTowersInfo) {
                            if (cellTowerInfo.getCid() != NeighboringCellInfo.UNKNOWN_CID) {
                                goodCellTowers.add(cellTowerInfo);
                            }
                        }

                        CellTowerTO[] towerTOArray = new CellTowerTO[goodCellTowers.size()];

                        int i = 0;
                        for (NeighboringCellInfo cellTowerInfo : goodCellTowers) {
                            CellTowerTO towerTO = new CellTowerTO();
                            towerTO.cid = cellTowerInfo.getCid();
                            towerTO.strength = cellTowerInfo.getRssi();
                            towerTOArray[i] = towerTO;
                            i++;
                        }
                        rawLocationInfoTO.towers = towerTOArray;
                        return rawLocationInfoTO;
                    }
                }

            } catch (Exception e) {
                debugLog(e);
            }
        }
        return null;
    }

    @Override
    public void reconfigure() {
        T.UI();
        final Configuration cfg = mConfigProvider.getConfiguration(CONFIGKEY);
        final boolean useGPSWhileOnBattery = cfg.get(CONFIG_USE_GPS_WHILE_ON_BATTERY_KEY,
            CONFIG_USE_GPS_WHILE_ON_BATTERY_DEFAULTVALUE);
        final boolean useGPSWhileCharging = cfg.get(CONFIG_USE_GPS_WHILE_CHARGING_KEY,
            CONFIG_USE_GPS_WHILE_CHARGING_DEFAULTVALUE);
        setGPSUsage(useGPSWhileOnBattery, useGPSWhileCharging);
    }

    @Override
    public void processSettings(SettingsTO settings) {
        T.UI();
        Configuration cfg = new Configuration();
        cfg.put(CONFIG_USE_GPS_WHILE_ON_BATTERY_KEY, settings.useGPSBattery);
        cfg.put(CONFIG_USE_GPS_WHILE_CHARGING_KEY, settings.useGPSCharging);
        mConfigProvider.updateConfigurationLater(CONFIGKEY, cfg);
    }

}

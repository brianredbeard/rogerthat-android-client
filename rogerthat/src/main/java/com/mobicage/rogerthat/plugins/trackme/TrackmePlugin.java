/*
 * Copyright 2017 GIG Technology NV
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
 * @@license_version:1.3@@
 */

package com.mobicage.rogerthat.plugins.trackme;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.BleNotAvailableException;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.logging.LogManager;
import org.altbeacon.beacon.logging.Loggers;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.os.RemoteException;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.mobicage.api.location.Rpc;
import com.mobicage.rogerthat.MainService;
import com.mobicage.rogerthat.config.Configuration;
import com.mobicage.rogerthat.config.ConfigurationProvider;
import com.mobicage.rogerthat.plugins.MobicagePlugin;
import com.mobicage.rogerthat.plugins.friends.FriendsPlugin;
import com.mobicage.rogerthat.util.db.DatabaseManager;
import com.mobicage.rogerthat.util.geo.GeoLocationCallback;
import com.mobicage.rogerthat.util.geo.GeoLocationProvider;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeBroadcastReceiver;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.system.SystemUtils;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.time.TimeUtils;
import com.mobicage.rpc.CallReceiver;
import com.mobicage.rpc.IJSONable;
import com.mobicage.rpc.IncompleteMessageException;
import com.mobicage.rpc.ResponseHandler;
import com.mobicage.rpc.SDCardLogger;
import com.mobicage.rpc.config.CloudConstants;
import com.mobicage.to.activity.GeoPointTO;
import com.mobicage.to.activity.LocationRecordTO;
import com.mobicage.to.activity.LogLocationRecipientTO;
import com.mobicage.to.activity.LogLocationsRequestTO;
import com.mobicage.to.activity.LogLocationsResponseTO;
import com.mobicage.to.beacon.GetBeaconRegionsRequestTO;
import com.mobicage.to.beacon.UpdateBeaconRegionsRequestTO;
import com.mobicage.to.beacon.UpdateBeaconRegionsResponseTO;
import com.mobicage.to.location.BeaconDiscoveredRequestTO;
import com.mobicage.to.location.BeaconInReachRequestTO;
import com.mobicage.to.location.BeaconInReachResponseTO;
import com.mobicage.to.location.BeaconOutOfReachRequestTO;
import com.mobicage.to.location.BeaconOutOfReachResponseTO;
import com.mobicage.to.location.DeleteBeaconDiscoveryRequestTO;
import com.mobicage.to.location.DeleteBeaconDiscoveryResponseTO;
import com.mobicage.to.location.GetLocationErrorTO;
import com.mobicage.to.location.GetLocationRequestTO;
import com.mobicage.to.location.GetLocationResponseTO;
import com.mobicage.to.location.LocationResultRequestTO;
import com.mobicage.to.location.LocationResultResponseTO;
import com.mobicage.to.location.TrackLocationRequestTO;
import com.mobicage.to.location.TrackLocationResponseTO;
import com.mobicage.to.system.SettingsTO;

public class TrackmePlugin implements MobicagePlugin, com.mobicage.capi.location.IClientRpc, ConnectionCallbacks,
    OnConnectionFailedListener {

    private static final class Tracker implements IJSONable {
        private TrackLocationRequestTO info;
        private LocationRequest locationRequest;
        private LocationListener locationListener;

        private static Tracker create(TrackLocationRequestTO request) {
            if (request.until <= System.currentTimeMillis() / 1000) {
                return null;
            }

            final Tracker tracker = new Tracker();
            tracker.info = request;
            tracker.locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    final GeoPointTO p = new GeoPointTO();
                    p.longitude = (long) (location.getLongitude() * GeoLocationProvider.LOCATION_FACTOR);
                    p.latitude = (long) (location.getLatitude() * GeoLocationProvider.LOCATION_FACTOR);
                    p.accuracy = (long) location.getAccuracy();

                    final LocationRecordTO locationRecordTO = new LocationRecordTO();
                    locationRecordTO.geoPoint = p;
                    locationRecordTO.timestamp = location.getTime() / 1000;

                    final LogLocationRecipientTO recipient = new LogLocationRecipientTO();
                    recipient.friend = tracker.info.friend;
                    recipient.target = tracker.info.target;

                    final LogLocationsRequestTO request = new LogLocationsRequestTO();
                    request.records = new LocationRecordTO[] { locationRecordTO };
                    request.recipients = new LogLocationRecipientTO[] { recipient };
                    try {
                        com.mobicage.api.activity.Rpc.logLocations(new ResponseHandler<LogLocationsResponseTO>(),
                            request);
                    } catch (Exception e) {
                        L.bug(e);
                    }
                }
            };
            return tracker;
        }

        private void requestLocationUpdates(GoogleApiClient googleApiClient) {
            locationRequest = new LocationRequest();
            locationRequest.setInterval(TRACKING_UPDATE_INTERVAL);
            locationRequest.setFastestInterval(FASTEST_TRACKING_UPDATE_INTERVAL);
            locationRequest.setSmallestDisplacement(info.distance_filter);
            locationRequest.setExpirationTime(1000 * info.until);
            locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
            L.d(getKey() + ": Requesting location updates. LocationRequest = " + locationRequest);
            LocationServices.FusedLocationApi
                .requestLocationUpdates(googleApiClient, locationRequest, locationListener);
        }

        private String getKey() {
            return createKey(info.friend, info.target);
        }

        private static String createKey(String email, long target) {
            return target + " - " + email;
        }

        @Override
        public Map<String, Object> toJSONMap() {
            return info.toJSONMap();
        }
    }

    public static final String TRACKME_PLUGIN_MUST_GET_BEACON_REGIONS = "com.mobicage.rogerthat.plugins.trackme.TRACKME_PLUGIN_MUST_GET_BEACON_REGIONS";
    public static final String BEACON_REGIONS_UPDATED = "com.mobicage.rogerthat.plugins.trackme.BEACON_REGIONS_UPDATED";

    private final static String CONFIGKEY = "com.mobicage.rogerthat.plugins.trackme";

    private final static String CONFIG_GEO_LOCATION_TRACKING_ENABLED_KEY = "geoTrackingEnabled";
    private final static boolean CONFIG_GEO_LOCATION_TRACKING_ENABLED_DEFAULTVALUE = true;

    private final static String CONFIG_GEO_LOCATION_TRACKING_FROM_TIME_SECONDS_KEY = "geoLocationTrackingFromTimeSeconds";
    private final static long CONFIG_GEO_LOCATION_TRACKING_FROM_TIME_SECONDS_DEFAULTVALUE = 0;

    private final static String CONFIG_GEO_LOCATION_TRACKING_TILL_TIME_SECONDS_KEY = "geoLocationTrackingTillTimeSeconds";
    private final static long CONFIG_GEO_LOCATION_TRACKING_TILL_TIME_SECONDS_DEFAULTVALUE = 86399;

    private final static String CONFIG_GEO_LOCATION_TRACKING_DAYS_KEY = "geoLocationTrackingDays";
    private final static long CONFIG_GEO_LOCATION_TRACKING_DAYS_DEFAULTVALUE = 0x7f; // all days enabled

    private final static String TRACKING_CONFIGKEY = "com.mobicage.rogerthat.plugins.trackme.tracking";
    private final static String CONFIG_CURRENTLY_TRACKING = "currentlyTrackingMyLocation";

    public static int TARGET_WEB = 1;
    public static int TARGET_MOBILE = 2;
    public static int TARGET_MOBILE_FRIENDS_ON_MAP = 3;
    public static int TARGET_MOBILE_FIRST_REQUEST_AFTER_GRANT = 4;
    public static int TARGET_SERVICE_LOCATION_TRACKER = 1001;

    public static int STATUS_INVISIBLE = 1;  // when INVISBLE MODE is switched on in settings
    public static int STATUS_TRACKING_POLICY = 2;  // denied because of location tracking settings in WEB
    public static int STATUS_AUTHORIZATION_DENIED = 3;  // PERMISSION_DENIED
    public static int STATUS_UNAVAILABLE = 6;
    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    public static final long TRACKING_UPDATE_INTERVAL = 10000; // in milliseconds
    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent than this value.
     */
    public static final long FASTEST_TRACKING_UPDATE_INTERVAL = 5000; // in milliseconds

    /**
     * Provides the entry point to Google Play services.
     */
    private GoogleApiClient mGoogleApiClient;
    private boolean mGoogleApiClientConnected = false;

    private final Map<String, Tracker> mCurrentlyTracking = new HashMap<String, Tracker>();

    private final ConfigurationProvider mConfigProvider;
    private final GeoLocationProvider mGeoLocationProvider;
    private final MainService mMainService;

    // All members are owned by UI Thread
    private long mGeoLocationTrackingTillTimeSeconds;
    private long mGeoLocationTrackingFromTimeSeconds;
    private long mGeoLocationTrackingDays;
    private boolean mEnabled = false;
    private SDCardLogger mSDCardLogger;

    private final ArrayList<LogLocationRecipientTO> mLocationRecipients = new ArrayList<LogLocationRecipientTO>();

    private boolean mTracking = false;

    private BeaconManager mBeaconManager;
    private final Map<String, DiscoveredBeaconProximity> mBeaconsInProximity = new HashMap<String, DiscoveredBeaconProximity>();
    private final Map<String, ArrayList<BeaconProximity>> mLastBeaconsInProximity = new HashMap<String, ArrayList<BeaconProximity>>();
    private final Map<String, Long> mDetectedBeaconsHistory = new HashMap<String, Long>();
    private final Map<String, Long> mKnownBeacons = new HashMap<String, Long>();

    private final TrackmeStore mStore;

    private MonitorNotifier mBeaconMonitorNotifier = null;
    private RangeNotifier mBeaconRangeNotifier = null;
    private boolean mBeaconManagerStarted = false;
    private List<BeaconRegion> mCurrentBeaconRegions = null;

    @TargetApi(18)
    public TrackmePlugin(final ConfigurationProvider pConfigProvider, final MainService service,
        final GeoLocationProvider pGeoLocationProvider, final SDCardLogger pLogger, final DatabaseManager dbManager) {

        T.UI();
        mMainService = service;
        mConfigProvider = pConfigProvider;
        mStore = new TrackmeStore(mMainService, dbManager);

        mGeoLocationProvider = pGeoLocationProvider;
        CallReceiver.comMobicageCapiLocationIClientRpc = this;
        mSDCardLogger = pLogger;
    }

    private void debugLog(String s) {
        if (mSDCardLogger == null) {
            L.d(s);
        } else {
            mSDCardLogger.d(s);
        }
    }

    private void bugLog(String s) {
        if (mSDCardLogger == null) {
            L.bug(s);
        } else {
            mSDCardLogger.bug(s);
        }
    }

    private void bugLog(Exception e) {
        if (mSDCardLogger == null) {
            L.bug(e);
        } else {
            mSDCardLogger.bug(e);
        }
    }

    @Override
    public void destroy() {
        T.UI();
        if (mBeaconManager != null) {
            mBeaconManager.unbind(mMainService);
        }
        mConfigProvider.unregisterListener(CONFIGKEY, this);
        mMainService.unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    public void processSettings(final SettingsTO settings) {
        T.UI();

        final Configuration cfg = new Configuration();

        final boolean enabled = settings.geoLocationTracking;
        cfg.put(CONFIG_GEO_LOCATION_TRACKING_ENABLED_KEY, enabled);

        final long[] geoLocationTrackingTimeslot = settings.geoLocationTrackingTimeslot;
        if (geoLocationTrackingTimeslot.length == 2) {
            cfg.put(CONFIG_GEO_LOCATION_TRACKING_FROM_TIME_SECONDS_KEY, geoLocationTrackingTimeslot[0]);
            cfg.put(CONFIG_GEO_LOCATION_TRACKING_TILL_TIME_SECONDS_KEY, geoLocationTrackingTimeslot[1]);
        } else {
            bugLog("geoLocationTrackingTimeslot.length != 2");
        }

        final long geoLocationTrackingDays = settings.geoLocationTrackingDays;
        cfg.put(CONFIG_GEO_LOCATION_TRACKING_DAYS_KEY, geoLocationTrackingDays);

        mConfigProvider.updateConfigurationLater(CONFIGKEY, cfg);
    }

    @Override
    public void reconfigure() {
        T.UI();
        final Configuration cfg = mConfigProvider.getConfiguration(CONFIGKEY);

        mGeoLocationTrackingFromTimeSeconds = cfg.get(CONFIG_GEO_LOCATION_TRACKING_FROM_TIME_SECONDS_KEY,
            CONFIG_GEO_LOCATION_TRACKING_FROM_TIME_SECONDS_DEFAULTVALUE);

        mGeoLocationTrackingTillTimeSeconds = cfg.get(CONFIG_GEO_LOCATION_TRACKING_TILL_TIME_SECONDS_KEY,
            CONFIG_GEO_LOCATION_TRACKING_TILL_TIME_SECONDS_DEFAULTVALUE);

        mGeoLocationTrackingDays = cfg.get(CONFIG_GEO_LOCATION_TRACKING_DAYS_KEY,
            CONFIG_GEO_LOCATION_TRACKING_DAYS_DEFAULTVALUE);

        mEnabled = cfg.get(CONFIG_GEO_LOCATION_TRACKING_ENABLED_KEY, CONFIG_GEO_LOCATION_TRACKING_ENABLED_DEFAULTVALUE);
    }

    private boolean isEnabledNowByTimePolicy() {
        final TimeZone tz = TimeZone.getDefault();
        final long now = System.currentTimeMillis();

        final TimeUtils.TimeStatus ts = TimeUtils.getAuthorizedTimeInfo(tz, now, mGeoLocationTrackingDays,
            mGeoLocationTrackingFromTimeSeconds, mGeoLocationTrackingTillTimeSeconds);
        return ts.mIsEnabledNow;
    }

    public void doTracking() {
        T.UI();
        mTracking = true;
        mGeoLocationProvider.requestLocationUpdate(new GeoLocationCallback() {

            @Override
            public void onError() {
                T.UI();
                mTracking = false;
                debugLog("TrackmePlugin: failed to receive new location");
            }

            @Override
            public void onLocationReceived(LocationRecordTO locationRecordTO) {
                T.UI();
                mTracking = false;
                try {
                    final LocationRecordTO[] records = new LocationRecordTO[]{locationRecordTO};
                    final LogLocationsRequestTO request = new LogLocationsRequestTO();
                    request.records = records;
                    request.recipients = mLocationRecipients.toArray(new LogLocationRecipientTO[mLocationRecipients
                            .size()]);
                    com.mobicage.api.activity.Rpc.logLocations(new ResponseHandler<LogLocationsResponseTO>(), request);
                    final FriendsPlugin friendsPlugin = mMainService.getPlugin(FriendsPlugin.class);
                    for (LogLocationRecipientTO recipient : mLocationRecipients) {
                        friendsPlugin.getHistory().putSendLocationInHistory(recipient.friend);
                    }
                    mLocationRecipients.clear();
                } catch (Exception e) {
                    bugLog(e);
                }
            }

        });
    }

    public TrackmeStore getStore() {
        T.dontCare();
        return mStore;
    }

    private final BroadcastReceiver mBroadcastReceiver = new SafeBroadcastReceiver() {
        @Override
        public String[] onSafeReceive(Context context, Intent intent) {
            T.UI();
            if (MainService.INTENT_BEACON_SERVICE_CONNECTED.equals(intent.getAction())) {
                mBeaconManager.setBackgroundMode(!mMainService.getScreenIsOn());
                if (!mBeaconManagerStarted)
                    startMonitoringBeaconRegions();
                return null;
            } else if (BEACON_REGIONS_UPDATED.equals(intent.getAction())) {
                if (mBeaconManagerStarted)
                    startMonitoringBeaconRegions();
                return new String[] { intent.getAction() };
            } else if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                if (mBeaconManager != null) {
                    mBeaconManager.setBackgroundMode(true);
                }
                return null;
            } else if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
                if (mBeaconManager != null) {
                    mBeaconManager.setBackgroundMode(false);
                }
                return null;
            }
            return null;
        }
    };

    @SuppressWarnings("unchecked")
    @Override
    public void initialize() {
        T.UI();
        reconfigure();
        mConfigProvider.registerListener(CONFIGKEY, this);

        mMainService.postOnUIHandler(new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                T.UI();
                mMainService.postOnBIZZHandler(new SafeRunnable() {
                    @Override
                    protected void safeRun() throws Exception {
                        if (mMainService.getPluginDBUpdates(TrackmePlugin.class).contains(
                            TRACKME_PLUGIN_MUST_GET_BEACON_REGIONS)) {
                            requestBeaconRegions();
                            mMainService.clearPluginDBUpdate(TrackmePlugin.class,
                                TRACKME_PLUGIN_MUST_GET_BEACON_REGIONS);
                        }
                    }
                });
            }
        });

        final IntentFilter intentFilter = new IntentFilter(BEACON_REGIONS_UPDATED);
        intentFilter.addAction(MainService.INTENT_BEACON_SERVICE_CONNECTED);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        mMainService.registerReceiver(mBroadcastReceiver, intentFilter);

        if (mMainService.isPermitted(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            if (CloudConstants.DEBUG_LOGGING) {
                LogManager.setLogger(Loggers.verboseLogger());
                LogManager.setVerboseLoggingEnabled(true);
            } else {
                LogManager.setLogger(Loggers.empty());
                LogManager.setVerboseLoggingEnabled(false);
            }

            mBeaconManager = BeaconManager.getInstanceForApplication(mMainService);
            if (!mBeaconManager.isAnyConsumerBound()) {
                mBeaconManager.getBeaconParsers().add(
                        new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));
            }

            try {
                if (!mBeaconManager.checkAvailability()) {
                    L.d("Bluetooth is not enabled");
                }
                mBeaconManager.bind(mMainService);
            } catch (NullPointerException ex) {
                L.i("BLE not available", ex);
            } catch (BleNotAvailableException ex) {
                L.d(ex.getMessage());
            } catch (SecurityException ex) {
                L.bug(ex);
            }
        }

        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(mMainService) == ConnectionResult.SUCCESS) {
            final Configuration cfg = mConfigProvider.getConfiguration(TRACKING_CONFIGKEY);
            final String trackersJSON = cfg.get(CONFIG_CURRENTLY_TRACKING, null);
            if (trackersJSON != null) {
                JSONArray trackers = (JSONArray) JSONValue.parse(trackersJSON);
                for (Object trackerDict : trackers) {
                    try {
                        final TrackLocationRequestTO request = new TrackLocationRequestTO(
                            (Map<String, Object>) trackerDict);
                        final Tracker tracker = Tracker.create(request);
                        if (tracker != null) {
                            mCurrentlyTracking.put(tracker.getKey(), tracker);
                        }
                    } catch (IncompleteMessageException e) {
                        L.bug(e);
                    }
                }

                if (mCurrentlyTracking.size() > 0) {
                    buildGoogleApiClient();
                }
            }
        }
    }

    public void setEnabled(final boolean enabled) {
        T.UI();
        final Configuration cfg = mConfigProvider.getConfiguration(CONFIGKEY);
        cfg.put(CONFIG_GEO_LOCATION_TRACKING_ENABLED_KEY, enabled);
        mConfigProvider.updateConfigurationLater(CONFIGKEY, cfg);
    }

    public boolean isLocationSharingEnabled() {
        T.dontCare();
        return mEnabled;
    }

    @Override
    public GetLocationResponseTO getLocation(final GetLocationRequestTO request) throws Exception {
        T.BIZZ();
        final GetLocationResponseTO response = new GetLocationResponseTO();

        if (!mMainService.isPermitted(Manifest.permission.ACCESS_FINE_LOCATION)) {
            response.error = new GetLocationErrorTO();
            response.error.status = STATUS_AUTHORIZATION_DENIED;
        } else {
            boolean locationSharingEnabled = request.high_prio || isLocationSharingEnabled();
            boolean enabledNowByTimePolicy = request.high_prio || isEnabledNowByTimePolicy();
            if (locationSharingEnabled && enabledNowByTimePolicy) {
                mMainService.postOnUIHandler(new SafeRunnable() {
                    @Override
                    protected void safeRun() throws Exception {
                        T.UI();
                        final LogLocationRecipientTO recipient = new LogLocationRecipientTO();
                        recipient.friend = request.friend;
                        recipient.target = request.target;
                        mLocationRecipients.add(recipient);
                        debugLog(recipient.friend + " requests my location");
                        if (!mTracking)
                            doTracking();
                    }
                });
            } else if (request.target == TARGET_MOBILE || request.target == TARGET_MOBILE_FIRST_REQUEST_AFTER_GRANT) {
                response.error = new GetLocationErrorTO();
                if (!locationSharingEnabled) {
                    response.error.status = STATUS_INVISIBLE;
                } else {
                    response.error.status = STATUS_TRACKING_POLICY;
                }
            }
        }

        return response;
    }

    @Override
    public TrackLocationResponseTO trackLocation(final TrackLocationRequestTO request) throws Exception {
        T.BIZZ();
        final TrackLocationResponseTO response = new TrackLocationResponseTO();
        if (!mMainService.isPermitted(Manifest.permission.ACCESS_FINE_LOCATION)) {
            response.error = new GetLocationErrorTO();
            response.error.status = STATUS_AUTHORIZATION_DENIED;
        } else if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(mMainService) != ConnectionResult.SUCCESS) {
            response.error = new GetLocationErrorTO();
            response.error.message = "GooglePlayServices is not available";
            response.error.status = STATUS_UNAVAILABLE;
            L.w(response.error.message);
        } else {
            buildGoogleApiClient();

            final String trackerKey = Tracker.createKey(request.friend, request.target);
            Tracker oldTracker = mCurrentlyTracking.remove(trackerKey);
            if (oldTracker != null) {
                L.d(trackerKey + ": removing location updates");
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, oldTracker.locationListener);
            }

            final Tracker tracker = Tracker.create(request);
            if (tracker != null) {
                L.d(trackerKey + ": adding to currentlyTracking");
                mCurrentlyTracking.put(trackerKey, tracker);
            } // else: request.until is in the past

            storeTrackers();
            restartTracking();
        }

        return response;
    }

    @Override
    public LocationResultResponseTO locationResult(final LocationResultRequestTO request) throws Exception {
        T.BIZZ();
        final Intent intent = new Intent(FriendsPlugin.FRIEND_LOCATION_RECEIVED_INTENT);
        if (request.location != null) {
            final String location = JSONValue.toJSONString(request.toJSONMap());
            intent.putExtra("location", location);
        } else {
            intent.putExtra("location", "");
        }
        intent.putExtra("email", request.friend);
        mMainService.sendBroadcast(intent);
        return new LocationResultResponseTO();
    }

    private void removeBeaconFromCaches(String key) {
        mLastBeaconsInProximity.remove(key);
        mBeaconsInProximity.remove(key);
        mDetectedBeaconsHistory.remove(key);
        mKnownBeacons.remove(key);
    }

    public void deleteBeaconDiscovery(String friendEmail) {
        L.d("Received delete beaconDiscovery for friend: " + friendEmail);
        mStore.deleteBeaconDiscovery(friendEmail);
        for (DiscoveredBeaconProximity dbp : getBeaconsInReach(friendEmail)) {
            String key = getBeaconProximityKey(dbp.uuid, getBeaconName(dbp.major, dbp.minor));
            removeBeaconFromCaches(key);
        }
    }

    @Override
    public DeleteBeaconDiscoveryResponseTO deleteBeaconDiscovery(DeleteBeaconDiscoveryRequestTO request)
        throws Exception {
        L.d("Received delete beaconDiscovery with uuid: " + request.uuid + " name: " + request.name);
        mStore.deleteBeaconDiscovery(request.uuid, request.name);
        String key = getBeaconProximityKey(request.uuid, request.name);
        removeBeaconFromCaches(key);
        return new DeleteBeaconDiscoveryResponseTO();
    }

    @Override
    public UpdateBeaconRegionsResponseTO updateBeaconRegions(UpdateBeaconRegionsRequestTO request) throws Exception {
        requestBeaconRegions();
        return new UpdateBeaconRegionsResponseTO();
    }

    public boolean requestBeaconRegions() {
        T.dontCare();
        GetBeaconRegionsRequestTO request = new GetBeaconRegionsRequestTO();
        try {
            Rpc.getBeaconRegions(new GetBeaconRegionsResponseHandler(), request);
        } catch (Exception e) {
            L.bug("Error while sending get beacon regions rpc request", e);
            return false;
        }
        return true;
    }

    public static String getBeaconProximityKey(String uuid, String beaconName) {
        return uuid + "|" + beaconName;
    }

    public static String getBeaconName(int major, int minor) {
        return major + "|" + minor;
    }

    private boolean shouldNotifyBeaconInReach(String uuid, int major, int minor, int proximity) {
        String beaconName = getBeaconName(major, minor);
        String key = getBeaconProximityKey(uuid, beaconName);
        L.v("shouldNotifyBeaconInReach: " + key + " => " + proximity);

        BeaconProximity bp = new BeaconProximity();
        bp.uuid = uuid;
        bp.major = major;
        bp.minor = minor;
        bp.proximity = proximity;

        ArrayList<BeaconProximity> lastBeacons = mLastBeaconsInProximity.get(key);
        if (lastBeacons == null) {
            lastBeacons = new ArrayList<BeaconProximity>();
            mLastBeaconsInProximity.put(key, lastBeacons);
        } else if (lastBeacons.size() > 2) {
            lastBeacons.remove(0);
        }
        lastBeacons.add(bp);

        DiscoveredBeaconProximity bpLive = mBeaconsInProximity.get(key);
        if (bpLive != null) {
            if (proximity == bpLive.proximity || lastBeacons.size() < 3) {
                return false;
            } else {
                for (BeaconProximity bpp : lastBeacons) {
                    if (bpp.proximity != proximity) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private void notifyBeaconInReach(String uuid, int major, int minor, int proximity) {
        String beaconName = getBeaconName(major, minor);
        Map<String, Object> beaconInfo = mStore.getFriendConnectedToBeaconDiscovery(uuid, beaconName);
        if (beaconInfo != null) {
            L.d("Beacon in reach with uuid: " + uuid + " Major: " + major + " Minor: " + minor);

            String friendEmail = (String) beaconInfo.get("email");
            String tag = (String) beaconInfo.get("tag");
            long callbacks = (Long) beaconInfo.get("callbacks");

            DiscoveredBeaconProximity bp = new DiscoveredBeaconProximity();
            bp.uuid = uuid;
            bp.major = major;
            bp.minor = minor;
            bp.friendEmail = friendEmail;
            bp.tag = tag;
            bp.proximity = proximity;

            String key = getBeaconProximityKey(uuid, beaconName);
            mBeaconsInProximity.put(key, bp);

            Intent intent = new Intent(FriendsPlugin.BEACON_IN_REACH);
            intent.putExtra("email", friendEmail);
            intent.putExtra("uuid", uuid);
            intent.putExtra("major", major);
            intent.putExtra("minor", minor);
            intent.putExtra("tag", tag);
            intent.putExtra("proximity", proximity);
            mMainService.sendBroadcast(intent);

            if (SystemUtils.isFlagEnabled(callbacks, FriendsPlugin.SERVICE_CALLBACK_FRIEND_IN_REACH)) {
                BeaconInReachRequestTO request = new BeaconInReachRequestTO();
                request.uuid = uuid;
                request.name = beaconName;
                request.friend_email = friendEmail;
                request.proximity = proximity;

                try {
                    com.mobicage.api.location.Rpc
                        .beaconInReach(new ResponseHandler<BeaconInReachResponseTO>(), request);
                } catch (Exception e) {
                    L.bug(e);
                    return;
                }
            }
        }
    }

    private void notifyBeaconOutOfReach(String uuid, int major, int minor) {
        String beaconName = getBeaconName(major, minor);
        String beaconKey = getBeaconProximityKey(uuid, beaconName);

        if (mBeaconsInProximity.containsKey(beaconKey)) {
            mBeaconsInProximity.remove(beaconKey);
            mLastBeaconsInProximity.remove(beaconKey);

            Map<String, Object> beaconInfo = mStore.getFriendConnectedToBeaconDiscovery(uuid, beaconName);
            if (beaconInfo != null) {
                L.d("Beacon out of reach with uuid: " + uuid + " Major: " + major + " Minor: " + minor);
                String friendEmail = (String) beaconInfo.get("email");
                String tag = (String) beaconInfo.get("tag");
                long callbacks = (Long) beaconInfo.get("callbacks");

                Intent intent = new Intent(FriendsPlugin.BEACON_OUT_OF_REACH);
                intent.putExtra("email", friendEmail);
                intent.putExtra("uuid", uuid);
                intent.putExtra("major", major);
                intent.putExtra("minor", minor);
                intent.putExtra("tag", tag);
                intent.putExtra("proximity", BeaconProximity.PROXIMITY_UNKNOWN);
                mMainService.sendBroadcast(intent);

                if (SystemUtils.isFlagEnabled(callbacks, FriendsPlugin.SERVICE_CALLBACK_FRIEND_OUT_OF_REACH)) {
                    BeaconOutOfReachRequestTO request = new BeaconOutOfReachRequestTO();
                    request.uuid = uuid;
                    request.name = beaconName;
                    request.friend_email = friendEmail;

                    try {
                        com.mobicage.api.location.Rpc.beaconOutOfReach(
                            new ResponseHandler<BeaconOutOfReachResponseTO>(), request);
                    } catch (Exception e) {
                        L.bug(e);
                        return;
                    }
                }
            }
        }
    }

    public List<DiscoveredBeaconProximity> getBeaconsInReach(String friendEmail) {
        List<DiscoveredBeaconProximity> beaconsInReach = new ArrayList<DiscoveredBeaconProximity>();
        for (DiscoveredBeaconProximity bp : mBeaconsInProximity.values()) {
            if (friendEmail.equals(bp.friendEmail)) {
                beaconsInReach.add(bp);
            }
        }
        return beaconsInReach;
    }

    public void startMonitoringBeaconRegions() {
        mBeaconManagerStarted = true;
        mBeaconManager.setMonitorNotifier(getBeaconMonitorNotifier());
        mBeaconManager.setRangeNotifier(getBeaconRangeNotifier());

        try {
            List<BeaconRegion> beaconRegions = mStore.getBeaconRegions();
            if (mCurrentBeaconRegions != null) {
                for (BeaconRegion br : mCurrentBeaconRegions) {
                    if (!beaconRegions.contains(br)) {
                        final Region r = new Region(br.getUniqueRegionId(), BeaconRegion.getId1(br),
                            BeaconRegion.getId2(br), BeaconRegion.getId3(br));
                        L.d("Stop monitoring region: " + r.getUniqueId());
                        mBeaconManager.stopRangingBeaconsInRegion(r);
                        mBeaconManager.stopMonitoringBeaconsInRegion(r);
                    }
                }
            }

            for (BeaconRegion br : beaconRegions) {
                if (mCurrentBeaconRegions == null || !mCurrentBeaconRegions.contains(br)) {
                    final Region r = new Region(br.getUniqueRegionId(), BeaconRegion.getId1(br),
                        BeaconRegion.getId2(br), BeaconRegion.getId3(br));
                    L.d("Start monitoring region: " + r.getUniqueId());
                    mBeaconManager.startMonitoringBeaconsInRegion(r);
                }
            }
            mCurrentBeaconRegions = beaconRegions;
        } catch (RemoteException e) {
            L.e(e);
        }
    }

    private MonitorNotifier getBeaconMonitorNotifier() {
        if (mBeaconMonitorNotifier == null) {
            mBeaconMonitorNotifier = new MonitorNotifier() {

                @Override
                public void didEnterRegion(Region region) {
                    L.d("didEnterRegion: " + region.getUniqueId());
                    try {
                        mBeaconManager.startRangingBeaconsInRegion(region);
                    } catch (RemoteException e) {
                        L.e(e);
                    }
                }

                @Override
                public void didExitRegion(Region region) {
                    L.d("didExitRegion: " + region.getUniqueId());
                    try {
                        mBeaconManager.stopRangingBeaconsInRegion(region);
                    } catch (RemoteException e) {
                        L.e(e);
                    }
                }

                @Override
                public void didDetermineStateForRegion(int state, Region region) {
                }
            };
        }
        return mBeaconMonitorNotifier;
    }

    private RangeNotifier getBeaconRangeNotifier() {
        if (mBeaconRangeNotifier == null) {
            mBeaconRangeNotifier = new RangeNotifier() {

                @Override
                public void didRangeBeaconsInRegion(final Collection<Beacon> iBeacons, final Region region) {
                    L.v("\n- Current beacons in region: " + region.getUniqueId() + " (" + iBeacons.size() + "):");
                    final long now = System.currentTimeMillis();

                    mMainService.postOnBIZZHandler(new SafeRunnable() {
                        @Override
                        protected void safeRun() throws Exception {
                            T.BIZZ();
                            for (Beacon b : iBeacons) {
                                String uuid = b.getId1().toUuid().toString();
                                int major = b.getId2().toInt();
                                int minor = b.getId3().toInt();
                                String beaconName = getBeaconName(major, minor);
                                int proximity = BeaconProximity.calculateProximity(b.getDistance());
                                String beaconKey = getBeaconProximityKey(uuid, beaconName);

                                boolean exists = mKnownBeacons.containsKey(beaconKey)
                                    || mStore.beaconDiscoveryExists(uuid, beaconName);

                                if (exists) {
                                    mKnownBeacons.put(beaconKey, now);
                                    if (shouldNotifyBeaconInReach(uuid, major, minor, proximity)) {
                                        notifyBeaconInReach(uuid, major, minor, proximity);
                                    }
                                } else {
                                    BeaconDiscoveredRequestTO request = new BeaconDiscoveredRequestTO();
                                    request.uuid = uuid;
                                    request.name = beaconName;

                                    final BeaconDiscoveredResponseHandler responseHandler = new BeaconDiscoveredResponseHandler();
                                    responseHandler.setUUID(uuid);
                                    responseHandler.setMajor(major);
                                    responseHandler.setMinor(minor);
                                    responseHandler.setProximity(proximity);

                                    try {
                                        com.mobicage.api.location.Rpc.beaconDiscovered(responseHandler, request);
                                    } catch (Exception e) {
                                        L.bug(e);
                                        return;
                                    }

                                    mStore.saveBeaconDiscovery(uuid, beaconName);
                                }
                                mDetectedBeaconsHistory.put(beaconKey, now);
                            }

                            // TODO: does this work as expected with more beaconRegions?
                            ArrayList<String> beaconsOutOfReach = new ArrayList<String>();
                            for (String beaconKey : mDetectedBeaconsHistory.keySet()) {
                                long timestamp = mDetectedBeaconsHistory.get(beaconKey);
                                if (timestamp < now - 10000) {
                                    beaconsOutOfReach.add(beaconKey);
                                }
                            }
                            for (String boor : beaconsOutOfReach) {
                                mDetectedBeaconsHistory.remove(boor);
                                String[] beacon = boor.split("\\|");
                                notifyBeaconOutOfReach(beacon[0], Integer.parseInt(beacon[1]),
                                    Integer.parseInt(beacon[2]));
                            }
                        }
                    });

                }
            };
        }
        return mBeaconRangeNotifier;
    }

    private void buildGoogleApiClient() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(mMainService).addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        L.i("Connected to GoogleApiClient");
        mGoogleApiClientConnected = true;
        restartTracking();
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        L.i("GoogleApiClient connection suspended with cause " + cause);
        mGoogleApiClientConnected = false;
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        L.i("Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
        mGoogleApiClientConnected = false;
    }

    @SuppressWarnings("unchecked")
    private void storeTrackers() {
        JSONArray trackers = new JSONArray();
        for (Tracker tracker : mCurrentlyTracking.values()) {
            trackers.add(tracker.toJSONMap());
        }

        Configuration cfg = new Configuration();
        cfg.put(CONFIG_CURRENTLY_TRACKING, JSONValue.toJSONString(trackers));
        mConfigProvider.updateConfigurationNow(TRACKING_CONFIGKEY, cfg);
    }

    private void restartTracking() {
        if (mGoogleApiClientConnected) {
            for (Tracker tracker : mCurrentlyTracking.values()) {
                if (tracker.locationRequest == null) {
                    tracker.requestLocationUpdates(mGoogleApiClient);
                }
            }
        }
    }
}

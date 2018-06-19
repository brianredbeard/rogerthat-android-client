/*
 * Copyright 2018 GIG Technology NV
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
 * @@license_version:1.4@@
 */

package com.mobicage.rogerthat.plugins.trackme;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.mobicage.rogerthat.MainService;
import com.mobicage.rogerthat.config.Configuration;
import com.mobicage.rogerthat.config.ConfigurationProvider;
import com.mobicage.rogerthat.plugins.MobicagePlugin;
import com.mobicage.rogerthat.plugins.friends.FriendsPlugin;
import com.mobicage.rogerthat.util.db.DatabaseManager;
import com.mobicage.rogerthat.util.geo.GeoLocationCallback;
import com.mobicage.rogerthat.util.geo.GeoLocationProvider;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.time.TimeUtils;
import com.mobicage.rpc.CallReceiver;
import com.mobicage.rpc.IJSONable;
import com.mobicage.rpc.IncompleteMessageException;
import com.mobicage.rpc.ResponseHandler;
import com.mobicage.rpc.SDCardLogger;
import com.mobicage.to.activity.GeoPointTO;
import com.mobicage.to.activity.LocationRecordTO;
import com.mobicage.to.activity.LogLocationRecipientTO;
import com.mobicage.to.activity.LogLocationsRequestTO;
import com.mobicage.to.activity.LogLocationsResponseTO;
import com.mobicage.to.location.GetLocationErrorTO;
import com.mobicage.to.location.GetLocationRequestTO;
import com.mobicage.to.location.GetLocationResponseTO;
import com.mobicage.to.location.LocationResultRequestTO;
import com.mobicage.to.location.LocationResultResponseTO;
import com.mobicage.to.location.TrackLocationRequestTO;
import com.mobicage.to.location.TrackLocationResponseTO;
import com.mobicage.to.system.SettingsTO;

import org.json.simple.JSONArray;
import org.json.simple.JSONValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

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

    @TargetApi(18)
    public TrackmePlugin(final ConfigurationProvider pConfigProvider, final MainService service,
        final GeoLocationProvider pGeoLocationProvider, final SDCardLogger pLogger, final DatabaseManager dbManager) {

        T.UI();
        mMainService = service;
        mConfigProvider = pConfigProvider;

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
        mConfigProvider.unregisterListener(CONFIGKEY, this);
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

    @SuppressWarnings("unchecked")
    @Override
    public void initialize() {
        T.UI();
        reconfigure();
        mConfigProvider.registerListener(CONFIGKEY, this);

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
    public void onConnectionFailed(@NonNull ConnectionResult result) {
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

/*
 * Copyright 2017 Mobicage NV
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
 * @@license_version:1.2@@
 */

/*
 COPYRIGHT (C) 2011 MOBICAGE NV
 * ALL RIGHTS RESERVED.
 *
 * ALTHOUGH YOU MAY BE ABLE TO READ THE CONTENT OF THIS FILE, THIS FILE
 * CONTAINS CONFIDENTIAL INFORMATION OF MOBICAGE NV. YOU ARE NOT ALLOWED
 * TO MODIFY, REPRODUCE, DISCLOSE, PUBLISH OR DISTRIBUTE ITS CONTENT,
 * EMBED IT IN OTHER SOFTWARE, OR CREATE DERIVATIVE WORKS, UNLESS PRIOR
 * WRITTEN PERMISSION IS OBTAINED FROM MOBICAGE NV.
 *
 * THE COPYRIGHT NOTICE ABOVE DOES NOT EVIDENCE ANY ACTUAL OR INTENDED
 * PUBLICATION OF SUCH SOURCE CODE.
 *
 * @@license_version:1.4@@
 */

package com.mobicage.rogerthat.plugins.messaging.widgets;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;

import com.mobicage.api.messaging.Rpc;
import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.plugins.messaging.Message;
import com.mobicage.rogerthat.plugins.messaging.MessagingPlugin;
import com.mobicage.rogerthat.plugins.messaging.ServiceMessageDetailActivity;
import com.mobicage.rogerthat.plugins.trackme.MapDetailActivity;
import com.mobicage.rogerthat.util.geo.GeoLocationListener;
import com.mobicage.rogerthat.util.geo.GeoLocationReceiver;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeDialogClick;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.system.SafeViewOnClickListener;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.ui.UIUtils;
import com.mobicage.rpc.IncompleteMessageException;
import com.mobicage.rpc.ResponseHandler;
import com.mobicage.to.messaging.forms.LocationWidgetResultTO;
import com.mobicage.to.messaging.forms.SubmitGPSLocationFormRequestTO;
import com.mobicage.to.messaging.forms.SubmitGPSLocationFormResponseTO;

import java.util.Locale;
import java.util.Map;

public class GPSLocationWidget extends Widget {

    private final static int LOCATION_SETTINGS = 1;

    private final static String WAKELOCK_TAG = "com.mobicage.rogerthat.plugins.messaging.widgets.GPSLocationWidgets";
    private final static long WAKELOCK_TIMEOUT_MILLIS = 30 * 1000;

    private Button mGetLocation;
    private Button mShowLocation;

    private boolean mUseGPS;

    private LocationManager mLocationManager;
    private boolean mWaitingForGPSLocation = false;
    private boolean mWaitingForNetworkLocation = false;
    private WakeLock mWakeLock;
    private GeoLocationListener mGPSGeoLocationListener;
    private GeoLocationListener mNetworkGeoLocationListener;
    private LocationWidgetResultTO mLocationResult = null;
    private ProgressDialog mProgressDialog;

    public GPSLocationWidget(Context context) {
        super(context);
    }

    public GPSLocationWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void initializeWidget() {
        T.UI();
        L.d("GPSLocationWidget initializeWidget");
        @SuppressWarnings("unchecked")
        final Map<String, Object> value = (Map<String, Object>) mWidgetMap.get("value");
        if (value != null) {
            try {
                mLocationResult = new LocationWidgetResultTO(value);
            } catch (IncompleteMessageException e) {
                L.bug(e);
            }
        }

        mGetLocation = (Button) findViewById(R.id.get_location);
        mShowLocation = (Button) findViewById(R.id.show_location);

        mUseGPS = Boolean.TRUE.equals(mWidgetMap.get("gps"));

        mGPSGeoLocationListener = new GeoLocationListener(mGPSGeoLocationReceiver);
        mNetworkGeoLocationListener = new GeoLocationListener(mNetworkGeoLocationReceiver);

        PowerManager powerManager = (PowerManager) mActivity.getSystemService(Context.POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKELOCK_TAG);

        final String message = mActivity.getString(R.string.updating_location);
        mProgressDialog = UIUtils.showProgressDialog(mActivity, null, message, true, false, null, ProgressDialog
                .STYLE_SPINNER, false);

        mGetLocation.setOnClickListener(new SafeViewOnClickListener() {
            @Override
            public void safeOnClick(View v) {
                final SafeRunnable onGranted = new SafeRunnable() {
                    @Override
                    protected void safeRun() throws Exception {
                        getMyLocation();
                    }
                };
                final SafeRunnable onDenied = new SafeRunnable() {
                    @Override
                    protected void safeRun() throws Exception {
                        String title = mActivity.getString(R.string.need_location_permission_title);
                        String message = mActivity.getString(R.string.need_location_permission);
                        SafeDialogClick onPositiveClick = new SafeDialogClick
                                () {
                            @Override
                            public void safeOnClick(DialogInterface dialog, int id) {
                                Intent intent = new Intent();
                                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", mActivity.getPackageName(), null);
                                intent.setData(uri);
                                mActivity.startActivity(intent);
                            }
                        };
                        UIUtils.showDialog(mActivity, title, message, R.string.go_to_app_settings, onPositiveClick, R
                                        .string.cancel,
                                null);
                    }
                };
                if (!mActivity.askPermissionIfNeeded(Manifest.permission.ACCESS_FINE_LOCATION,
                        ServiceMessageDetailActivity.PERMISSION_REQUEST_GPS_LCOATION_WIDGET, onGranted, onDenied)) {
                    getMyLocation();
                }
            }
        });

        mShowLocation.setEnabled(mLocationResult != null);
        mShowLocation.setOnClickListener(new SafeViewOnClickListener() {
            @Override
            public void safeOnClick(View v) {
                T.UI();
                L.d("showing location: " + mLocationResult.toJSONMap());

                Intent intent = new Intent(mActivity, MapDetailActivity.class);
                intent.putExtra(MapDetailActivity.LATITUDE, mLocationResult.latitude);
                intent.putExtra(MapDetailActivity.LONGITUDE, mLocationResult.longitude);
                mActivity.startActivity(intent);
            }
        });
    }

    private void getMyLocation() {
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) mActivity.getSystemService(Context.LOCATION_SERVICE);
        }
        final boolean gpsEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        final boolean networkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        L.d("getting location with gps: " + mUseGPS);
        L.d("GPS_PROVIDER enabled: " + gpsEnabled);
        L.d("NETWORK_PROVIDER enabled: " + networkEnabled);
        mWaitingForGPSLocation = false;
        mWaitingForNetworkLocation = false;

        if (mUseGPS) {
            if (!gpsEnabled) {
                cancelGPSUpdates();
                showGotoLocationSettings();
                return;
            }

            try {
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mGPSGeoLocationListener);
            } catch (SecurityException e) {
                L.bug(e); // Should never happen
            }
            mWaitingForGPSLocation = true;
        } else {
            if (!gpsEnabled && !networkEnabled) {
                cancelGPSUpdates();
                cancelNetworkLocationUpdates();
                showGotoLocationSettings();
                return;
            }

            if (gpsEnabled) {
                try {
                    mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
                        mGPSGeoLocationListener);
                } catch (SecurityException e) {
                    L.bug(e); // Should never happen
                }
                mWaitingForGPSLocation = true;
            } else {
                cancelGPSUpdates();
            }

            if (networkEnabled) {
                try {
                    mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0,
                        mNetworkGeoLocationListener);
                } catch (SecurityException e) {
                    L.bug(e); // Should never happen
                }
                mWaitingForNetworkLocation = true;
            } else {
                cancelNetworkLocationUpdates();
            }
        }

        mProgressDialog.show();
        mWakeLock.acquire(); // Lock will be released by callbacks or by timeout
        mActivity.getMainService().postDelayedOnUIHandler(mWakeLockReleaser, WAKELOCK_TIMEOUT_MILLIS);
    }

    private void showGotoLocationSettings() {
        String message = mActivity.getString(R.string.gps_is_not_enabled);
        SafeDialogClick onPositiveClick = new SafeDialogClick() {
            @Override
            public void safeOnClick(DialogInterface dialog, int id) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mActivity.startActivityForResult(intent, LOCATION_SETTINGS);
            }
        };
        UIUtils.showDialog(mActivity, null, message, R.string.yes, onPositiveClick, R.string.no, null);
    }

    private LocationWidgetResultTO createLocationResult(Location location) {
        LocationWidgetResultTO locationResult = new LocationWidgetResultTO();
        locationResult.altitude = (float) location.getAltitude();
        locationResult.latitude = (float) location.getLatitude();
        locationResult.longitude = (float) location.getLongitude();
        locationResult.horizontal_accuracy = location.getAccuracy();
        locationResult.vertical_accuracy = -1;
        locationResult.timestamp = location.getTime() / 1000;
        return locationResult;
    }

    private void releaseWakeLockIfHeld(boolean showError) {
        T.UI();
        mActivity.getMainService().removeFromUIHandler(mWakeLockReleaser);
        if (mWakeLock.isHeld()) {
            mWakeLock.release();
        }
        if (showError) {
            UIUtils.showErrorPleaseRetryDialog(mActivity);
        }
    }

    private SafeRunnable mWakeLockReleaser = new SafeRunnable() {
        @Override
        public void safeRun() {
            T.UI();
            try {
                L.d("GeoLocationProvider mWakeLockReleaser.safeRun");
                L.d("No location received; wakelock timed out!");
                mProgressDialog.dismiss();
                cancelGPSUpdates();
                cancelNetworkLocationUpdates();
                releaseWakeLockIfHeld(true);
            } catch (Exception e) {
                L.d(e);
            }
        }
    };

    private final GeoLocationReceiver mGPSGeoLocationReceiver = new GeoLocationReceiver() {
        @Override
        public void onLocationError() {
            T.UI();
            mProgressDialog.dismiss();
            L.d("GeoLocationProvider mGPSGeoLocationReceiver.onLocationError");
            mWaitingForGPSLocation = false;
            cancelGPSUpdates();
            if (!mWaitingForNetworkLocation) {
                releaseWakeLockIfHeld(true);
            }
        }

        @Override
        public void onLocationReceived(Location location) {
            T.UI();
            mProgressDialog.dismiss();
            L.d("GeoLocationProvider mGPSGeoLocationReceiver.onLocationReceived");
            mWaitingForGPSLocation = false;
            cancelGPSUpdates();
            if (location != null) {
                mWaitingForNetworkLocation = false;
                cancelNetworkLocationUpdates();
                mLocationResult = createLocationResult(location);
                releaseWakeLockIfHeld(false);
                mShowLocation.setEnabled(true);
            } else {
                onLocationError();
            }
        }
    };

    private final GeoLocationReceiver mNetworkGeoLocationReceiver = new GeoLocationReceiver() {

        @Override
        public void onLocationError() {
            T.UI();
            mProgressDialog.dismiss();
            L.d("GeoLocationProvider mNetworkGeoLocationReceiver.onLocationError");
            // Error receiving network location
            // If I dont wait for GPS, do a raw location post
            mWaitingForNetworkLocation = false;
            cancelNetworkLocationUpdates();
            if (!mWaitingForGPSLocation) {
                releaseWakeLockIfHeld(true);
            }
        }

        @Override
        public void onLocationReceived(Location location) {
            T.UI();
            mProgressDialog.dismiss();
            L.d("GeoLocationProvider mNetworkGeoLocationReceiver.onLocationReceived");
            if (location != null) {
                // I receive a good network location
                // If I dont wait for GPS, then submit it
                mWaitingForNetworkLocation = false;
                cancelNetworkLocationUpdates();
                mLocationResult = createLocationResult(location);
                releaseWakeLockIfHeld(false);
                mShowLocation.setEnabled(true);
            } else {
                onLocationError();
            }
        }
    };

    private void cancelGPSUpdates() {
        T.UI();
        if (mLocationManager != null) {
            try {
                mLocationManager.removeUpdates(mGPSGeoLocationListener);
            } catch (SecurityException e) {
                L.bug(e); // Should never happen
            }
        }
    }

    private void cancelNetworkLocationUpdates() {
        T.UI();
        if (mLocationManager != null) {
            try {
                mLocationManager.removeUpdates(mNetworkGeoLocationListener);
            } catch (SecurityException e) {
                L.bug(e); // Should never happen
            }
        }
    }

    @Override
    public void putValue() {
        mWidgetMap.put("value", mLocationResult == null ? null : mLocationResult.toJSONMap());
    }

    @Override
    public LocationWidgetResultTO getWidgetResult() {
        return mLocationResult;
    }

    @Override
    public boolean proceedWithSubmit(final String buttonId) {
        if (Message.POSITIVE.equals(buttonId)) {
            if (mLocationResult == null) {
                String title = mActivity.getString(R.string.no_gps_location_fetched_title);
                String message = mActivity.getString(R.string.no_gps_location_fetched_summary);
                UIUtils.showDialog(mActivity, title, message);
                return false;
            }
        }
        return true;
    }

    @Override
    public void submit(final String buttonId, long timestamp) throws Exception {
        T.UI();
        cancelGPSUpdates();
        cancelNetworkLocationUpdates();

        final SubmitGPSLocationFormRequestTO request = new SubmitGPSLocationFormRequestTO();
        request.button_id = buttonId;
        request.message_key = mMessage.key;
        request.parent_message_key = mMessage.parent_key;
        request.timestamp = timestamp;

        if (Message.POSITIVE.equals(buttonId)) {
            L.d("Submit location " + mWidgetMap);
            request.result = mLocationResult;
        }

        final boolean isSentByJSMFR = (mMessage.flags & MessagingPlugin.FLAG_SENT_BY_JSMFR) == MessagingPlugin.FLAG_SENT_BY_JSMFR;
        if (isSentByJSMFR) {
            mPlugin.answerJsMfrMessage(mMessage, request.toJSONMap(),
                "com.mobicage.api.messaging.submitGPSLocationForm", mActivity, mParentView);
        } else {
            Rpc.submitGPSLocationForm(new ResponseHandler<SubmitGPSLocationFormResponseTO>(), request);
        }
    }

    public static String valueString(Context context, Map<String, Object> widget) {
        @SuppressWarnings("unchecked")
        Map<String, Object> locationResult = (Map<String, Object>) widget.get("value");
        return String.format(Locale.US, "<%.03f, %.03f> ± %.02fm", locationResult.get("latitude"),
            locationResult.get("longitude"), locationResult.get("horizontal_accuracy"));
    }
}

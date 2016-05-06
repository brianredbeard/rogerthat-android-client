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
package com.mobicage.rogerthat;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONValue;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.plugins.friends.DrawableItemizedOverlay;
import com.mobicage.rogerthat.plugins.friends.Friend;
import com.mobicage.rogerthat.plugins.friends.FriendsPlugin;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeBroadcastReceiver;
import com.mobicage.rogerthat.util.system.SafeDialogInterfaceOnClickListener;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.ui.UIUtils;
import com.mobicage.rpc.IncompleteMessageException;
import com.mobicage.to.activity.GeoPointWithTimestampTO;
import com.mobicage.to.location.LocationResultRequestTO;

public class FriendsLocationActivity extends ServiceBoundMapActivity {

    private static final int PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 1;

    private FriendsPlugin mFriendsPlugin;
    private MapView mFriendMap;
    private RelativeLayout mLoadingLayout;
    private ProgressBar mLoadingProgressbar;
    private TextView mNoLocationsFoundTextView;
    private List<LocationResultRequestTO> mLocations;
    private List<String> mAdded;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);
        mLocations = new ArrayList<LocationResultRequestTO>();
        mAdded = new ArrayList<String>();
        mFriendMap = (MapView) findViewById(R.id.friend_map);
        mFriendMap.setBuiltInZoomControls(true);

        mLoadingLayout = (RelativeLayout) findViewById(R.id.loading_layout);
        mLoadingProgressbar = (ProgressBar) findViewById(R.id.loading_progressbar);
        mNoLocationsFoundTextView = (TextView) findViewById(R.id.no_friend_locations_found);
    }

    private BroadcastReceiver mBroadcastReceiver = new SafeBroadcastReceiver() {
        @Override
        public String[] onSafeReceive(Context context, Intent intent) {
            T.UI();
            if (FriendsPlugin.FRIEND_LOCATION_RECEIVED_INTENT.equals(intent.getAction())) {
                String locationStr = intent.getStringExtra("location");

                try {
                    @SuppressWarnings("unchecked")
                    LocationResultRequestTO location = new LocationResultRequestTO(
                        (Map<String, Object>) JSONValue.parse(locationStr));
                    mLocations.add(location);

                    displayLocations();
                } catch (IncompleteMessageException e) {
                    L.bug(e);
                }

            }
            return new String[] { intent.getAction() };
        }

    };
    private LocationManager mLocationManager;
    private MyIdentity mMyIdentity;

    private void displayLocations() {
        T.UI();
        mLoadingProgressbar.setVisibility(View.GONE);
        mLoadingLayout.setVisibility(View.GONE);
        mFriendMap.setVisibility(View.VISIBLE);
        // Calculate bounds
        double maxLat = Double.MIN_VALUE;
        double minLat = Double.MAX_VALUE;
        double maxLon = Double.MIN_VALUE;
        double minLon = Double.MAX_VALUE;
        for (LocationResultRequestTO fl : mLocations) {
            if (fl.location.latitude > maxLat)
                maxLat = fl.location.latitude;
            if (fl.location.latitude < minLat)
                minLat = fl.location.latitude;
            if (fl.location.longitude > maxLon)
                maxLon = fl.location.longitude;
            if (fl.location.longitude < minLon)
                minLon = fl.location.longitude;
        }
        mFriendMap.getController().zoomToSpan((int) ((maxLat - minLat)), (int) ((maxLon - minLon)));
        GeoPoint center = new GeoPoint((int) ((maxLat + minLat) / 2), (int) ((maxLon + minLon) / 2));
        mFriendMap.getController().setCenter(center);
        for (LocationResultRequestTO fl : mLocations) {
            if (mAdded.contains(fl.friend))
                continue;
            mAdded.add(fl.friend);

            if (fl.friend.equals(mMyIdentity.getEmail())) {
                DrawableItemizedOverlay overlay = new DrawableItemizedOverlay(getAvatar(mMyIdentity), this);
                GeoPoint point = new GeoPoint((int) (fl.location.latitude), (int) (fl.location.longitude));
                Date date = new Date(fl.location.timestamp * 1000);
                OverlayItem overlayitem = new OverlayItem(point, mMyIdentity.getName(), getString(
                    R.string.friend_map_marker, DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                        .format(date), fl.location.accuracy));
                overlay.addOverlay(overlayitem);
                mFriendMap.getOverlays().add(overlay);
            } else {
                Friend friend = mFriendsPlugin.getStore().getExistingFriend(fl.friend);

                DrawableItemizedOverlay overlay = new DrawableItemizedOverlay(getAvatar(friend), this);
                GeoPoint point = new GeoPoint((int) (fl.location.latitude), (int) (fl.location.longitude));
                Date date = new Date(fl.location.timestamp * 1000);
                OverlayItem overlayitem = new OverlayItem(point, friend.getDisplayName(), getString(
                    R.string.friend_map_marker, DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                        .format(date), fl.location.accuracy));
                overlay.addOverlay(overlayitem);
                mFriendMap.getOverlays().add(overlay);
            }
        }
        if (mAdded.size() == 1 && mAdded.get(0).equals(mMyIdentity.getEmail()))
            UIUtils.showLongToast(this,
                    getString(R.string.your_location_displayed_friend_have_contacted_for_their_location));
    }

    @Override
    protected void onServiceBound() {
        T.UI();
        mFriendsPlugin = mService.getPlugin(FriendsPlugin.class);
        if (!mFriendsPlugin.scheduleAllFriendsLocationRetrieval()) {
            new AlertDialog.Builder(this).setMessage(getString(R.string.get_friends_location_failed))
                .setPositiveButton(R.string.ok, new SafeDialogInterfaceOnClickListener() {
                    @Override
                    public void safeOnClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        mLoadingProgressbar.setVisibility(View.GONE);
                        mNoLocationsFoundTextView.setVisibility(View.VISIBLE);
                    }
                }).create().show();
        }
        IntentFilter filter = new IntentFilter(FriendsPlugin.FRIEND_LOCATION_RECEIVED_INTENT);
        registerReceiver(mBroadcastReceiver, filter);

        mMyIdentity = mService.getIdentityStore().getIdentity();

        if (mService.isPermitted(Manifest.permission.ACCESS_FINE_LOCATION)) {
            getMyLocation();
        } else {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getMyLocation();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void getMyLocation() {
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (mLocationManager != null) {
            String bestProvider = mLocationManager.getBestProvider(new Criteria(), true);
            if (bestProvider != null) {
                try {
                    mLocationManager.requestLocationUpdates(bestProvider, 0, 0, mLocationListener);
                } catch (SecurityException e) {
                    L.bug(e);
                }
            }
        }
    }

    @Override
    protected void onServiceUnbound() {
        unregisterReceiver(mBroadcastReceiver);
        if (mLocationManager != null) {
            try {
                mLocationManager.removeUpdates(mLocationListener);
            } catch (SecurityException e) {
                L.bug(e);
            }
        }
    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }

    private LocationListener mLocationListener = new LocationListener() {

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onLocationChanged(Location location) {
            mLocationManager.removeUpdates(mLocationListener);
            LocationResultRequestTO locationResult = new LocationResultRequestTO();
            locationResult.location = new GeoPointWithTimestampTO();
            locationResult.location.accuracy = (int) location.getAccuracy();
            locationResult.location.latitude = (int) (location.getLatitude() * 1000000);
            locationResult.location.longitude = (int) (location.getLongitude() * 1000000);
            locationResult.location.timestamp = System.currentTimeMillis() / 1000;
            locationResult.friend = mMyIdentity.getEmail();
            mLocations.add(locationResult);
            displayLocations();
        }
    };

}
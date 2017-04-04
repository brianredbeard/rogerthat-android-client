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
package com.mobicage.rogerthat;

import android.Manifest;
import android.app.ProgressDialog;
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
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.plugins.friends.Friend;
import com.mobicage.rogerthat.plugins.friends.FriendsPlugin;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeBroadcastReceiver;
import com.mobicage.rogerthat.util.system.SafeDialogClick;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.ui.UIUtils;
import com.mobicage.rpc.IncompleteMessageException;
import com.mobicage.to.activity.GeoPointWithTimestampTO;
import com.mobicage.to.location.LocationResultRequestTO;

import org.json.simple.JSONValue;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class FriendsLocationActivity extends ServiceBoundMapActivity {

    private FriendsPlugin mFriendsPlugin;
    private FrameLayout mFriendMapLayout;
    private RelativeLayout mNoFriendsLocationFoundLayout;
    private List<LocationResultRequestTO> mLocations;
    private List<String> mAdded;
    private LocationListener mLocationListener = null;
    private ProgressDialog mProgressDialog;
    private ArrayList<Marker> mMarkers;
    private Toast mToast;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocations = new ArrayList<>();
        setContentView(R.layout.map);
        mAdded = new ArrayList<>();
        mMarkers = new ArrayList<>();
        mFriendMapLayout = (FrameLayout) findViewById(R.id.friend_map_layout);
        mNoFriendsLocationFoundLayout = (RelativeLayout) findViewById(R.id.no_friend_locations_found);

        // Show progress dialog until one or more locations are fetched.
        String title = getString(R.string.updating_location);
        UIUtils.showProgressDialog(this, title, null, true, true, null, ProgressDialog.STYLE_HORIZONTAL, true);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.friend_map);
        mapFragment.getMapAsync(this);
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
            return new String[]{intent.getAction()};
        }

    };
    private LocationManager mLocationManager;
    private MyIdentity mMyIdentity;

    @Override
    public void onMapReady(GoogleMap map) {
        super.onMapReady(map);
        // Use a custom info window adapter to handle multiple lines of text in the info window contents.
        map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker marker) {
                // Return null here, so that getInfoContents() is called next.
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                // Inflate the layouts for the info window, title and snippet.
                View infoWindow = getLayoutInflater().inflate(R.layout.maps_custom_info_contents, null);

                TextView title = (TextView) infoWindow.findViewById(R.id.title);
                title.setText(marker.getTitle());

                TextView snippet = (TextView) infoWindow.findViewById(R.id.snippet);
                snippet.setText(marker.getSnippet());

                return infoWindow;
            }
        });
    }

    private void displayLocations() {
        T.UI();
        mFriendMapLayout.setVisibility(View.VISIBLE);
        mProgressDialog.hide();
        LatLngBounds.Builder coordinateBounds = new LatLngBounds.Builder();

        // One marker per friend, and one for yourself
        outerloop:
        for (LocationResultRequestTO locationResult : mLocations) {
            double lat = (double) locationResult.location.latitude / 1000000;
            double lon = (double) locationResult.location.longitude / 1000000;
            LatLng coordinate = new LatLng(lat, lon);
            coordinateBounds.include(coordinate);
            // For our own location, simply update the marker's position
            if (mAdded.contains(locationResult.friend) && locationResult.friend.equals(mMyIdentity.getEmail())) {
                for (Marker marker : mMarkers) {
                    if (marker.getTitle().equals(mMyIdentity.getDisplayName())) {
                        marker.setPosition(coordinate);
                        continue outerloop;
                    }
                }
            } else if (mAdded.contains(locationResult.friend)) {
                continue;
            }
            mAdded.add(locationResult.friend);
            BitmapDescriptor avatar;
            String title;
            Date date = new Date(locationResult.location.timestamp * 1000);
            String dateString = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(date);
            String trackedAtText = getString(R.string.friend_map_marker, dateString, locationResult.location.accuracy);
            if (locationResult.friend.equals(mMyIdentity.getEmail())) {
                avatar = getAvatarBitmapDescriptor(mMyIdentity);
                title = mMyIdentity.getDisplayName();
            } else {
                Friend friend = mFriendsPlugin.getStore().getExistingFriend(locationResult.friend);
                avatar = getAvatarBitmapDescriptor(friend);
                title = friend.name;
            }
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(coordinate)
                    .title(title)
                    .snippet(trackedAtText)
                    .icon(avatar);
            mMarkers.add(mMap.addMarker(markerOptions));

        }

        if (mAdded.size() == 1 && mAdded.get(0).equals(mMyIdentity.getEmail())) {
            if (mToast == null) {
                mToast = UIUtils.showLongToast(this,
                        getString(R.string.your_location_displayed_friend_have_contacted_for_their_location));
            }
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mMarkers.get(0).getPosition(), 14));
        } else {
            // Zoom in/out to fit all positions on the screen when a position of one of our friends is updated
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(coordinateBounds.build(), 250));
        }
    }

    @Override
    protected void onServiceBound() {
        T.UI();
        mFriendsPlugin = mService.getPlugin(FriendsPlugin.class);
        if (!mFriendsPlugin.scheduleAllFriendsLocationRetrieval()) {
            String message = getString(R.string.get_friends_location_failed);
            String positiveCaption = getString(R.string.ok);
            SafeDialogClick positiveClick = new SafeDialogClick() {
                @Override
                public void safeOnClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                    mProgressDialog.hide();
                    mNoFriendsLocationFoundLayout.setVisibility(View.VISIBLE);
                    mFriendMapLayout.setVisibility(View.GONE);
                }
            };
            UIUtils.showDialog(this, null, message, positiveCaption, positiveClick, null, null);
        }
        IntentFilter filter = new IntentFilter(FriendsPlugin.FRIEND_LOCATION_RECEIVED_INTENT);
        registerReceiver(mBroadcastReceiver, filter);

        mMyIdentity = mService.getIdentityStore().getIdentity();

        if (mService.isPermitted(Manifest.permission.ACCESS_FINE_LOCATION)) {
            getMyLocation();
        } else {
            enableMyLocation();
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
        if (mLocationListener == null) {
            mLocationListener = new LocationListener() {

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                }

                @Override
                public void onProviderEnabled(String provider) {
                }

                @Override
                public void onProviderDisabled(String provider) {
                }

                @SuppressWarnings("MissingPermission")
                @Override
                public void onLocationChanged(Location location) {
                    LocationResultRequestTO locationResult = new LocationResultRequestTO();
                    locationResult.location = new GeoPointWithTimestampTO();
                    locationResult.location.accuracy = (int) location.getAccuracy();
                    locationResult.location.latitude = (int) (location.getLatitude() * 1000000);
                    locationResult.location.longitude = (int) (location.getLongitude() * 1000000);
                    locationResult.location.timestamp = System.currentTimeMillis() / 1000;
                    locationResult.friend = mMyIdentity.getEmail();
                    boolean modified = false;
                    for (LocationResultRequestTO result : mLocations) {
                        if (result.friend.equals(mMyIdentity.getEmail())) {
                            result.location = locationResult.location;
                            modified = true;
                        }
                    }
                    if (!modified) {
                        mLocations.add(locationResult);
                    }
                    enableMyLocation();
                    displayLocations();
                    if (modified && locationResult.location.accuracy < 20) {
                        // accurate enough, stop updates
                        mLocationManager.removeUpdates(mLocationListener);
                        mMap.setMyLocationEnabled(false);
                    }
                }
            };
        }
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
    public boolean onMyLocationButtonClick() {
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }
}

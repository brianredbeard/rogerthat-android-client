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

package com.mobicage.rogerthat.plugins.trackme;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.MyIdentity;
import com.mobicage.rogerthat.ServiceBoundMapActivity;
import com.mobicage.rogerthat.util.system.SafeViewOnClickListener;
import com.mobicage.rogerthat.util.system.T;

public class MapDetailActivity extends ServiceBoundMapActivity {

    public static String LATITUDE = "latitude";
    public static String LONGITUDE = "longitude";
    public static String VERIFY = "verify";
    public static String VERIFIED = "verified";

    private float mLocationLatitude;
    private float mLocationLongitude;
    private MyIdentity mIdentity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.map_dialog);
        setTitle(R.string.friends_map);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onServiceBound() {
        T.UI();
        mIdentity = mService.getIdentityStore().getIdentity();
        final Intent intent = getIntent();
        mLocationLatitude = intent.getFloatExtra(LATITUDE, -1);
        mLocationLongitude = intent.getFloatExtra(LONGITUDE, -1);
        zoomToPosition();

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (intent.getBooleanExtra(VERIFY, false)) {

            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });

            LinearLayout mapVerify = (LinearLayout) findViewById(R.id.map_verify);
            mapVerify.setVisibility(View.VISIBLE);
            setTitle(R.string.validate_discovered_location);
            Button mapYes = (Button) findViewById(R.id.map_yes);
            Button mapNo = (Button) findViewById(R.id.map_no);

            mapYes.setOnClickListener(new SafeViewOnClickListener() {
                @Override
                public void safeOnClick(View v) {
                    Intent intent = getIntent();
                    intent.putExtra(LATITUDE, mLocationLatitude);
                    intent.putExtra(LONGITUDE, mLocationLongitude);
                    intent.putExtra(VERIFIED, true);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            });

            mapNo.setOnClickListener(new SafeViewOnClickListener() {
                @Override
                public void safeOnClick(View v) {
                    Intent intent = getIntent();
                    intent.putExtra(LATITUDE, mLocationLatitude);
                    intent.putExtra(LONGITUDE, mLocationLongitude);
                    intent.putExtra(VERIFIED, false);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            });
        } else {
            toolbar.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onServiceUnbound() {
        T.UI();
    }

    @Override
    public boolean onMyLocationButtonClick() {
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    private void zoomToPosition() {
        if (mMap != null) {
            mMap.clear();
            LatLng position = new LatLng(mLocationLatitude, mLocationLongitude);
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(position, 14);
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(position)
                    .title(getString(R.string.you_are_here))
                    .icon(getAvatarBitmapDescriptor(mIdentity));
            mMap.addMarker(markerOptions);
            mMap.animateCamera(cameraUpdate);
        }
    }

    public void onMapReady(GoogleMap map) {
        super.onMapReady(map);
        zoomToPosition();
    }

}

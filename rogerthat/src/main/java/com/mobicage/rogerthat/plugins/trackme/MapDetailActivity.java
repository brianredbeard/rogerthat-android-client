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

package com.mobicage.rogerthat.plugins.trackme;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.MyIdentity;
import com.mobicage.rogerthat.ServiceBoundMapActivity;
import com.mobicage.rogerthat.plugins.friends.DrawableItemizedOverlay;
import com.mobicage.rogerthat.util.system.SafeViewOnClickListener;
import com.mobicage.rogerthat.util.system.T;

public class MapDetailActivity extends ServiceBoundMapActivity {

    public static String LATITUDE = "latitude";
    public static String LONGITUDE = "longitude";
    public static String VERIFY = "verify";
    public static String VERIFIED = "verified";

    private MapView mMapView;
    private float mLocationLatitude;
    private float mLocationLongitude;
    private MyIdentity mIdentity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.map_dialog);

        mMapView = (MapView) findViewById(R.id.map);

        mMapView.setBuiltInZoomControls(true);
        mMapView.getController().setZoom(14);
        mMapView.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onServiceBound() {
        T.UI();
        mIdentity = mService.getIdentityStore().getIdentity();
        final Intent intent = getIntent();
        mLocationLatitude = intent.getFloatExtra(LATITUDE, -1);
        mLocationLongitude = intent.getFloatExtra(LONGITUDE, -1);

        mMapView.getOverlays().clear();
        DrawableItemizedOverlay overlay = new DrawableItemizedOverlay(getOverlayAvatar(mIdentity), this);

        GeoPoint point = new GeoPoint((int) (mLocationLatitude * 1000000), (int) (mLocationLongitude * 1000000));
        OverlayItem overlayitem = new OverlayItem(point, getString(R.string.you_are_here), null);
        overlay.addOverlay(overlayitem);
        mMapView.getOverlays().add(overlay);
        mMapView.getController().setCenter(point);

        if (intent.getBooleanExtra(VERIFY, false)) {
            LinearLayout mapVerify = (LinearLayout) findViewById(R.id.map_verify);
            mapVerify.setVisibility(View.VISIBLE);
            TextView title = (TextView) findViewById(R.id.title);
            title.setVisibility(View.VISIBLE);
            View titleDivider = findViewById(R.id.title_divider);
            titleDivider.setVisibility(View.VISIBLE);
            Button mapYes = (Button) findViewById(R.id.map_yes);
            mapYes.setVisibility(View.VISIBLE);
            Button mapNo = (Button) findViewById(R.id.map_no);
            mapNo.setVisibility(View.VISIBLE);

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
        }
    }

    @Override
    protected void onServiceUnbound() {
        T.UI();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void finish() {
        super.finish();
    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }

}
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
import java.util.Date;

import android.content.Intent;
import android.location.Location;
import android.view.View;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.plugins.friends.DrawableItemizedOverlay;
import com.mobicage.rogerthat.plugins.friends.Friend;
import com.mobicage.rogerthat.plugins.friends.FriendsPlugin;

public class FriendLocationActivity extends ServiceBoundMapActivity {

    @Override
    protected void onServiceBound() {
        Intent intent = getIntent();
        displayLocation((Location) intent.getParcelableExtra("friend_location"), intent.getStringExtra("friend"),
            (Location) intent.getParcelableExtra("my_location"));
    }

    private void displayLocation(Location friendLocation, String friendEmail, Location myLocation) {
        FriendsPlugin friendsPlugin = mService.getPlugin(FriendsPlugin.class);
        Friend friend = friendsPlugin.getStore().getExistingFriend(friendEmail);
        setContentView(R.layout.map);
        MapView mFriendMap = (MapView) findViewById(R.id.friend_map);
        mFriendMap.setBuiltInZoomControls(true);
        mFriendMap.setVisibility(View.VISIBLE);
        mFriendMap.getOverlays().clear();

        // Add me to map
        GeoPoint myPoint = null;
        if (myLocation != null) {
            DrawableItemizedOverlay myOverlay = new DrawableItemizedOverlay(getAvatar(mService.getIdentityStore()
                .getIdentity()), this);
            myPoint = new GeoPoint((int) (myLocation.getLatitude() * 1000000),
                (int) (myLocation.getLongitude() * 1000000));
            OverlayItem myOverlayitem = new OverlayItem(myPoint, mService.getIdentityStore().getIdentity().getName(),
                getString(
                    R.string.friend_map_marker,
                    DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(
                        new Date(myLocation.getTime())), (int) myLocation.getAccuracy()));
            myOverlay.addOverlay(myOverlayitem);
            mFriendMap.getOverlays().add(myOverlay);
        }
        // Add friend to map
        DrawableItemizedOverlay friendOverlay = new DrawableItemizedOverlay(getAvatar(friend), this);
        GeoPoint friendPoint = new GeoPoint((int) (friendLocation.getLatitude() * 1000000),
            (int) (friendLocation.getLongitude() * 1000000));
        OverlayItem friendOverlayitem = new OverlayItem(friendPoint, friendsPlugin.getName(friendEmail), getString(
            R.string.friend_map_marker,
            DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(
                new Date(friendLocation.getTime())), (int) friendLocation.getAccuracy()));
        friendOverlay.addOverlay(friendOverlayitem);
        mFriendMap.getOverlays().add(friendOverlay);

        if (myLocation != null && myPoint != null) {
            GeoPoint center = new GeoPoint((myPoint.getLatitudeE6() + friendPoint.getLatitudeE6()) / 2,
                (myPoint.getLongitudeE6() + friendPoint.getLongitudeE6()) / 2);
            mFriendMap.getController().zoomToSpan(Math.abs(myPoint.getLatitudeE6() - friendPoint.getLatitudeE6()),
                Math.abs((myPoint.getLongitudeE6() - friendPoint.getLongitudeE6())));
            mFriendMap.getController().setCenter(center);
        } else {
            mFriendMap.getController().setZoom(11);
            mFriendMap.getController().setCenter(friendPoint);
        }
    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }

    @Override
    protected void onServiceUnbound() {
        // do nothing
    }

}
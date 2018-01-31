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

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.plugins.friends.ActionScreenActivity;
import com.mobicage.rogerthat.plugins.friends.Friend;
import com.mobicage.rogerthat.plugins.friends.FriendStore;
import com.mobicage.rogerthat.plugins.friends.FriendsPlugin;
import com.mobicage.rogerthat.plugins.friends.FullscreenActionScreenActivity;
import com.mobicage.rogerthat.plugins.messaging.BrandingFailureException;
import com.mobicage.rogerthat.plugins.messaging.BrandingMgr;
import com.mobicage.rogerthat.plugins.system.JSEmbedding;
import com.mobicage.rogerthat.plugins.system.SystemPlugin;
import com.mobicage.rogerthat.util.TextUtils;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeBroadcastReceiver;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rpc.config.AppConstants;

import java.util.Map;

public class HomeBrandingActivity extends ServiceBoundActivity {

    public static final String SERVICE_EMAIL = "email";
    private String mServiceEmail;
    private boolean mOpening = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        T.UI();
        L.i("HomeBrandingActivity.onCreate");
        setContentViewWithoutNavigationBar(R.layout.home_branding);

        Intent intent = getIntent();
        mServiceEmail = intent.getStringExtra(SERVICE_EMAIL);
        if (TextUtils.isEmptyOrWhitespace(mServiceEmail)) {
            mServiceEmail = AppConstants.APP_EMAIL;
        }
        L.d("Service: " + mServiceEmail);
        if (TextUtils.isEmptyOrWhitespace(mServiceEmail)) {
            finish();
        }
    }

    @Override
    protected void onServiceBound() {
        T.UI();
        L.d("HomeBrandingActivity.onServiceBound()");
        final String[] receivingIntents = new String[]{FriendsPlugin.FRIENDS_LIST_REFRESHED,
                FriendsPlugin.FRIEND_ADDED_INTENT,
                FriendsPlugin.FRIEND_UPDATE_INTENT,
                FriendsPlugin.FRIEND_REMOVED_INTENT,
                BrandingMgr.SERVICE_BRANDING_AVAILABLE_INTENT,
                BrandingMgr.GENERIC_BRANDING_AVAILABLE_INTENT,
                BrandingMgr.JS_EMBEDDING_AVAILABLE_INTENT};

        IntentFilter filter = new IntentFilter();
        for (String action : receivingIntents)
            filter.addAction(action);
        registerReceiver(mBroadcastReceiver, filter);

        showHomeBranding();
    }

    @Override
    protected void onServiceUnbound() {
        T.UI();
        unregisterReceiver(mBroadcastReceiver);
    }

    private final SafeBroadcastReceiver mBroadcastReceiver = new SafeBroadcastReceiver() {
        @Override
        public String[] onSafeReceive(Context context, Intent intent) {
            T.UI();
            showHomeBranding();
            return new String[] { intent.getAction() };
        };
    };

    private void showHomeBranding() {
        T.UI();
        final FriendsPlugin friendsPlugin = mService.getPlugin(FriendsPlugin.class);
        final FriendStore friendStore = friendsPlugin.getStore();
        final SystemPlugin systemPlugin = mService.getPlugin(SystemPlugin.class);

        Friend friend = friendStore.getFriend(mServiceEmail);
        if (friend == null) {
            L.i("Service not available yet");
            return;
        }
        if (friend.homeBrandingHash == null) {
            L.i("Service has no home branding");
            return;
        }

        BrandingMgr brandingMgr = friendsPlugin.getBrandingMgr();
        boolean brandingAvailable = false;
        try {
            brandingAvailable = brandingMgr.isBrandingAvailable(friend.homeBrandingHash);
        } catch (BrandingFailureException e) {
            // ignore
        }

        if (!brandingAvailable) {
            L.i("Home branding not available yet");
            brandingMgr.queueGenericBranding(friend.homeBrandingHash);
            return;
        }

        boolean packetsAvailable = true;
        Map<String, JSEmbedding> packets = systemPlugin.getJSEmbeddedPackets();
        if (packets.size() == 0) {
            packetsAvailable = false;
        }
        for (JSEmbedding packet : packets.values()) {
            if (packet.getStatus() == JSEmbedding.STATUS_UNAVAILABLE) {
                packetsAvailable = false;
            }
        }

        if (!packetsAvailable) {
            L.i("JS Embedding packets not available yet");
            return;
        }
        if (mOpening) {
            return;
        }
        mOpening = true;
        L.i("Showing home branding");

        Class clazz = brandingMgr.getFullscreenActionScreenActivityClass(friend.homeBrandingHash);
        Intent intent = new Intent(this, clazz);
        intent.setFlags(MainActivity.FLAG_CLEAR_STACK_SINGLE_TOP);
        intent.putExtra(ActionScreenActivity.BRANDING_TYPE, ActionScreenActivity.BRANDING_TYPE_HOME);
        intent.putExtra(ActionScreenActivity.BRANDING_KEY, friend.homeBrandingHash);
        intent.putExtra(ActionScreenActivity.SERVICE_EMAIL, friend.email);
        intent.putExtra(ActionScreenActivity.ITEM_TAG_HASH, "");
        intent.putExtra(ActionScreenActivity.ITEM_LABEL, "");
        intent.putExtra(ActionScreenActivity.ITEM_COORDS, new long[] { 0, 0, 0 });
        intent.putExtra(ActionScreenActivity.CONTEXT_MATCH, "");
        intent.putExtra(ActionScreenActivity.RUN_IN_BACKGROUND, false);
        startActivity(intent);
        finish();
    }
}

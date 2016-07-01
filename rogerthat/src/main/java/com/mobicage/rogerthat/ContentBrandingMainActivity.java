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

import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.plugins.friends.ActionScreenActivity;
import com.mobicage.rogerthat.plugins.friends.ContentBrandingActionScreenActivity;
import com.mobicage.rogerthat.plugins.friends.Friend;
import com.mobicage.rogerthat.plugins.friends.FriendStore;
import com.mobicage.rogerthat.plugins.friends.FriendsPlugin;
import com.mobicage.rogerthat.plugins.messaging.BrandingFailureException;
import com.mobicage.rogerthat.plugins.messaging.BrandingMgr;
import com.mobicage.rogerthat.plugins.system.JSEmbedding;
import com.mobicage.rogerthat.plugins.system.SystemPlugin;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeBroadcastReceiver;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rpc.config.AppConstants;
import com.mobicage.rpc.config.CloudConstants;

public class ContentBrandingMainActivity extends ServiceBoundActivity {
    private ProgressBar mProgressBar;
    private Timer mTimer;
    private Handler mUIHandler;

    private final SafeBroadcastReceiver mBroadcastReceiver = new SafeBroadcastReceiver() {
        @Override
        public String[] onSafeReceive(Context context, Intent intent) {
            T.UI();
            launchOsaSlideshowActivityAndFinish();
            return new String[] { intent.getAction() };
        };
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        T.UI();
        if (CloudConstants.isContentBrandingApp()) {
            L.bug("OsaLoyaltyMainActivity should only be used by APP_TYPE_CONTENT_BRANDING");
        }
        setContentView(R.layout.registration_for_content_branding);
        mUIHandler = new Handler();
        LinearLayout progressContainer = (LinearLayout) findViewById(R.id.progress_container);
        progressContainer.setVisibility(View.VISIBLE);
        TextView statusLbl = (TextView) findViewById(R.id.status);
        statusLbl.setText(R.string.initializing);

        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mProgressBar.setIndeterminate(false);
        mProgressBar.setProgress(0);

        mTimer = new Timer(false);

        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                mUIHandler.post(new SafeRunnable() {
                    @Override
                    protected void safeRun() throws Exception {
                        T.UI();
                        mProgressBar.setProgress(mProgressBar.getProgress() + 1);
                    }
                });
            }
        };
        mTimer.scheduleAtFixedRate(timerTask, 250, 250);
    }

    @Override
    protected void onServiceBound() {
        T.UI();
        final String[] receivingIntents = new String[] { FriendsPlugin.FRIENDS_LIST_REFRESHED,
            FriendsPlugin.FRIEND_ADDED_INTENT, BrandingMgr.SERVICE_BRANDING_AVAILABLE_INTENT,
            BrandingMgr.GENERIC_BRANDING_AVAILABLE_INTENT, BrandingMgr.JS_EMBEDDING_AVAILABLE_INTENT };

        IntentFilter filter = new IntentFilter();
        for (String action : receivingIntents)
            filter.addAction(action);
        registerReceiver(mBroadcastReceiver, filter);

        launchOsaSlideshowActivityAndFinish();
    }

    @Override
    protected void onServiceUnbound() {
        T.UI();
        unregisterReceiver(mBroadcastReceiver);
    }

    private void launchOsaSlideshowActivityAndFinish() {
        T.UI();
        final FriendsPlugin friendsPlugin = mService.getPlugin(FriendsPlugin.class);
        final FriendStore friendStore = friendsPlugin.getStore();
        final SystemPlugin systemPlugin = mService.getPlugin(SystemPlugin.class);
        List<String> friends = friendStore.getEmails();
        if (friends.size() == 0) {
            L.i("Service not available yet");
            return;
        }
        if (friends.size() == 1) {
            int progress = mProgressBar.getProgress();
            if (progress < 25) {
                mProgressBar.setProgress(25);
            } else {
                mProgressBar.setProgress(progress + 1);
            }
            Friend friend = friendStore.getFriend(friends.get(0));

            boolean brandingAvailable = false;
            try {
                brandingAvailable = friendsPlugin.getBrandingMgr().isBrandingAvailable(friend.contentBrandingHash);
            } catch (BrandingFailureException e) {
                // ignore
            }

            if (!brandingAvailable) {
                L.i("Content branding not available yet");
                return;
            }
            if (progress < 50) {
                mProgressBar.setProgress(50);
            } else {
                mProgressBar.setProgress(progress + 1);
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

            if (progress < 100) {
                mProgressBar.setProgress(100);
            } else {
                mProgressBar.setProgress(progress + 1);
            }

            mTimer.cancel();

            Intent intent = new Intent(ContentBrandingMainActivity.this, ContentBrandingActionScreenActivity.class);
            intent.setFlags(MainActivity.FLAG_CLEAR_STACK);
            intent.putExtra(ActionScreenActivity.BRANDING_KEY, friend.contentBrandingHash);
            intent.putExtra(ActionScreenActivity.SERVICE_EMAIL, friend.email);
            intent.putExtra(ActionScreenActivity.ITEM_TAG_HASH, "");
            intent.putExtra(ActionScreenActivity.ITEM_LABEL, "");
            intent.putExtra(ActionScreenActivity.ITEM_COORDS, new long[] { 0, 0, 0 });
            intent.putExtra(ActionScreenActivity.CONTEXT_MATCH, "");
            intent.putExtra(ActionScreenActivity.RUN_IN_BACKGROUND, false);
            startActivity(intent);
            finish();
        } else {
            L.bug("OSA Loyalty user has more than 1 friend");
        }
    }
}

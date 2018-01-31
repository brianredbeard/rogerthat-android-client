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

package com.mobicage.rogerthat.plugins.scan;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.mobicage.rogerthat.FriendDetailActivity;
import com.mobicage.rogerthat.plugins.friends.Friend;
import com.mobicage.rogerthat.plugins.friends.FriendsPlugin;
import com.mobicage.rogerthat.plugins.messaging.BrandingMgr;
import com.mobicage.rogerthat.plugins.messaging.FriendsThreadActivity;
import com.mobicage.rogerthat.plugins.messaging.MessagingPlugin;
import com.mobicage.rogerthat.plugins.messaging.ServiceMessageDetailActivity;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeBroadcastReceiver;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.ui.UIUtils;

import org.jivesoftware.smack.util.Base64;

public class ServiceActionActivity extends FriendDetailActivity {

    private String mPokeAction;
    private String mEmail;
    private String mStaticFlow;
    private String mStaticFlowHash;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Menu not visible
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Menu not visible
        return false;
    }


    @Override
    protected int getMenu() {
        // Menu not visible
        return 0;
    }

    @Override
    protected int getHeaderVisibility() {
        return View.VISIBLE;
    }

    @Override
    protected int getUnfriendMessage() {
        // Menu not visible
        return 0;
    }

    @Override
    protected int getRemoveFailedMessage() {
        // Menu not visible
        return 0;
    }

    @Override
    protected int getFriendAreaVisibility() {
        return View.GONE;
    }

    @Override
    protected int getServiceAreaVisibility() {
        return View.VISIBLE;
    }

    @Override
    protected int getPassportVisibility() {
        return View.GONE;
    }

    @Override
    protected Friend loadFriend(Intent intent) {
        if (!intent.getAction().equals(FriendsPlugin.SERVICE_ACTION_INFO_RECEIVED_INTENT)) {
            L.d("Expected intent with action " + FriendsPlugin.SERVICE_ACTION_INFO_RECEIVED_INTENT + ", but got "
                + intent.getAction());
            return null;
        }

        mPokeAction = intent.getStringExtra(ProcessScanActivity.POKE_ACTION);
        mEmail = intent.getStringExtra(ProcessScanActivity.EMAIL);
        mStaticFlow = intent.getStringExtra(ProcessScanActivity.STATIC_FLOW);
        mStaticFlowHash = intent.getStringExtra(ProcessScanActivity.STATIC_FLOW_HASH);

        Friend friend = new Friend();
        friend.email = mEmail;
        friend.avatar = Base64.decode(intent.getStringExtra(ProcessScanActivity.AVATAR));
        friend.description = intent.getStringExtra(ProcessScanActivity.DESCRIPTION);
        friend.descriptionBranding = intent.getStringExtra(ProcessScanActivity.DESCRIPTION_BRANDING);
        friend.name = intent.getStringExtra(ProcessScanActivity.NAME);
        friend.pokeDescription = intent.getStringExtra(ProcessScanActivity.POKE_DESCRIPTION);
        friend.qualifiedIdentifier = intent.getStringExtra(ProcessScanActivity.QUALIFIED_IDENTIFIER);
        friend.type = FriendsPlugin.FRIEND_TYPE_SERVICE;

        return friend;
    }

    @Override
    protected String getPokeAction() {
        return mPokeAction;
    }

    @Override
    protected String getStaticFlow() {
        return mStaticFlow;
    }

    @Override
    protected String getStaticFlowHash() {
        return mStaticFlowHash;
    }

    @Override
    protected int getPokeVisibility() {
        return View.VISIBLE;
    }

    @Override
    protected IntentFilter getIntentFilter() {
        // IntentFilter for FriendDetailActivity
        IntentFilter filter = new IntentFilter(MessagingPlugin.NEW_MESSAGE_RECEIVED_INTENT);
        filter.addAction(BrandingMgr.SERVICE_BRANDING_AVAILABLE_INTENT);
        filter.addAction(BrandingMgr.GENERIC_BRANDING_AVAILABLE_INTENT);
        return filter;
    }

    private SafeBroadcastReceiver mNewMessageBroadcastReceiver = new SafeBroadcastReceiver() {

        @Override
        public String[] onSafeReceive(final Context context, final Intent intent) {
            if (MessagingPlugin.NEW_MESSAGE_RECEIVED_INTENT.equals(intent.getAction())) {
                if (mContextMatch != null && mContextMatch.equals(intent.getStringExtra("context")) && isTransmitting()) {
                    mContextMatch = "";
                    completeTransmit(new SafeRunnable() {
                        @Override
                        protected void safeRun() throws Exception {
                            final String messageKey = intent.getStringExtra("message");
                            long flags = intent.getLongExtra("flags", 0);
                            final Intent i;
                            if ((flags & MessagingPlugin.FLAG_DYNAMIC_CHAT) == MessagingPlugin.FLAG_DYNAMIC_CHAT) {
                                i = new Intent(ServiceActionActivity.this, FriendsThreadActivity.class);
                                final String parentKey = intent.getStringExtra("parent");
                                i.putExtra(FriendsThreadActivity.PARENT_MESSAGE_KEY, parentKey == null ? messageKey
                                    : parentKey);
                                i.putExtra(FriendsThreadActivity.MESSAGE_FLAGS, flags);
                            } else {
                                i = new Intent(ServiceActionActivity.this, ServiceMessageDetailActivity.class);
                                i.putExtra("message", messageKey);
                            }
                            startActivity(i);
                            finish();
                        }
                    });
                    return new String[] { intent.getAction() };
                }
            }
            if (MessagingPlugin.MESSAGE_JSMFR_ERROR.equals(intent.getAction())) {
                if (mContextMatch != null && mContextMatch.equals(intent.getStringExtra("context")) && isTransmitting()) {
                    mContextMatch = "";
                    completeTransmit(new SafeRunnable() {
                        @Override
                        protected void safeRun() throws Exception {
                            UIUtils.showErrorPleaseRetryDialog(ServiceActionActivity.this);
                        }
                    });
                    return new String[] { intent.getAction() };
                }
            }
            return null;
        }
    };

    @Override
    protected void onServiceBound() {
        super.onServiceBound();
        final IntentFilter intentFilter = new IntentFilter(MessagingPlugin.NEW_MESSAGE_RECEIVED_INTENT);
        intentFilter.addAction(MessagingPlugin.MESSAGE_JSMFR_ERROR);
        registerReceiver(mNewMessageBroadcastReceiver, intentFilter);
    }

    @Override
    protected void onServiceUnbound() {
        super.onServiceUnbound();
        if (mNewMessageBroadcastReceiver != null)
            unregisterReceiver(mNewMessageBroadcastReceiver);
    }

}

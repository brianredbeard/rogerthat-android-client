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

package com.mobicage.rogerthat.plugins.friends;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.AbstractHomeActivity;
import com.mobicage.rogerthat.MainService;
import com.mobicage.rogerthat.ServiceBoundActivity;
import com.mobicage.rogerthat.plugins.messaging.FriendsThreadActivity;
import com.mobicage.rogerthat.plugins.messaging.MessagingPlugin;
import com.mobicage.rogerthat.plugins.messaging.ServiceMessageDetailActivity;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeBroadcastReceiver;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.system.SystemUtils;
import com.mobicage.rogerthat.util.ui.UIUtils;

import java.util.UUID;

public class Poker<T extends Activity & PokingActivity> extends SafeBroadcastReceiver {

    public static class ResultHandler extends MenuItemPresser.ResultHandler {
    }

    // fields set by the constructor
    private final ResultHandler mDefaultResultHandler = new ResultHandler();
    private final T mActivity;
    private final MainService mService;
    private final String mEmail;

    // fields set by poke
    private ResultHandler mResultHandler = mDefaultResultHandler; // mResultHandler is never null for simplicity
    private String mContextMatch = "";

    public Poker(T activity, String email) {
        mActivity = activity;
        mService = mActivity.getMainService();
        mEmail = email;
    }

    public void poke(final String tag, final ResultHandler resultHandler) {
        mResultHandler = resultHandler == null ? mDefaultResultHandler : resultHandler;
        mActivity.showTransmitting(null);

        mContextMatch = "SP_" + UUID.randomUUID().toString();
        final IntentFilter filter = new IntentFilter(MessagingPlugin.NEW_MESSAGE_RECEIVED_INTENT);
        mActivity.registerReceiver(this, filter);

        final FriendsPlugin friendsPlugin = mService.getPlugin(FriendsPlugin.class);
        final boolean success = friendsPlugin.pokeService(mEmail, tag, mContextMatch);
        if (!success) {
            mContextMatch = "";
            mActivity.completeTransmit(new SafeRunnable() {
                @Override
                protected void safeRun() throws Exception {
                    UIUtils.showAlertDialog(mActivity, null, R.string.scanner_communication_failure);
                    mResultHandler.onTimeout();
                    stop();
                }
            });
            mResultHandler.onError();
            stop();
        }
    }

    public void stop() {
        try {
            mActivity.unregisterReceiver(this);
        } catch (IllegalArgumentException e) {
            // receiver was not registered
        }
    }

    @Override
    public String[] onSafeReceive(final Context context, final Intent intent) {
        final String action = intent.getAction();
        if (MessagingPlugin.NEW_MESSAGE_RECEIVED_INTENT.equals(action)) {
            if (mContextMatch.equals(intent.getStringExtra("context")) && mActivity.isTransmitting()) {
                mContextMatch = "";
                mActivity.completeTransmit(new SafeRunnable() {
                    @Override
                    protected void safeRun() throws Exception {
                        final String messageKey = intent.getStringExtra("message");
                        long flags = intent.getLongExtra("flags", 0);
                        final Intent i;
                        if (SystemUtils.isFlagEnabled(flags, MessagingPlugin.FLAG_DYNAMIC_CHAT)) {
                            i = new Intent(context, FriendsThreadActivity.class);
                            final String parentKey = intent.getStringExtra("parent");
                            i.putExtra(FriendsThreadActivity.PARENT_MESSAGE_KEY, parentKey == null ? messageKey
                                    : parentKey);
                            i.putExtra(FriendsThreadActivity.MESSAGE_FLAGS, flags);
                        } else {
                            i = new Intent(context, ServiceMessageDetailActivity.class);
                            i.putExtra("message", messageKey);
                        }
                        mActivity.startActivity(i);
                        mResultHandler.onSuccess();
                        stop();
                    }
                });
                return new String[]{action};
            }
        }

        return null;
    }
}

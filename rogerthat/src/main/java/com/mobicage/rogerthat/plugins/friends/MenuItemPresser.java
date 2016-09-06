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
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.mobicage.api.services.Rpc;
import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.AbstractHomeActivity;
import com.mobicage.rogerthat.MainService;
import com.mobicage.rogerthat.ServiceBoundActivity;
import com.mobicage.rogerthat.plugins.messaging.BrandingFailureException;
import com.mobicage.rogerthat.plugins.messaging.FriendsThreadActivity;
import com.mobicage.rogerthat.plugins.messaging.MessagingPlugin;
import com.mobicage.rogerthat.plugins.messaging.ServiceMessageDetailActivity;
import com.mobicage.rogerthat.plugins.messaging.mfr.EmptyStaticFlowException;
import com.mobicage.rogerthat.plugins.messaging.mfr.JsMfr;
import com.mobicage.rogerthat.plugins.messaging.mfr.MessageFlowRun;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeBroadcastReceiver;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.ui.UIUtils;
import com.mobicage.rpc.ResponseHandler;
import com.mobicage.to.friends.ServiceMenuItemTO;
import com.mobicage.to.service.PressMenuIconRequestTO;
import com.mobicage.to.service.PressMenuIconResponseTO;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MenuItemPresser<T extends Activity & MenuItemPressingActivity> extends SafeBroadcastReceiver {

    public static class ResultHandler {

        /**
         * Called when the new activity is started
         */
        public void onSuccess() {
        }

        /**
         * Called when the activity couldn't be started (Eg. menu item not found, message flow failure...)
         */
        public void onError() {
        }

        /**
         * Called when the conditions are not right to start the activity (Eg. internet check not passed, menu item
         * double clicked on...)
         */
        public void onCancel() {
        }

        /**
         * Called when the activity couldn't be started in time (Eg. poke timeout, message flow timeout...)
         */
        public void onTimeout() {
        }

    }

    // fields set by the constructor
    private final ResultHandler mDefaultResultHandler = new ResultHandler();
    private final T mActivity;
    private final MainService mService;
    private final String mEmail;

    // fields set by itemPressed
    private ResultHandler mResultHandler = mDefaultResultHandler; // mResultHandler is never null for simplicity
    private String mContextMatch = "";
    private long mLastTimeClicked = 0;

    public MenuItemPresser(T activity, String email) {
        mActivity = activity;
        mService = mActivity.getMainService();
        mEmail = email;
    }

    public void itemPressed(final String tag, final ResultHandler resultHandler) {
        final FriendStore friendStore = mService.getPlugin(FriendsPlugin.class).getStore();
        final ServiceMenuItemDetails smi = friendStore.getMenuItem(mEmail, tag);
        if (smi == null) {
            if (resultHandler != null)
                resultHandler.onError();
            return;
        }
        itemPressed(smi, smi.menuGeneration, resultHandler);
    }

    public void itemPressed(final ServiceMenuItemTO item, final long menuGeneration, final ResultHandler
            resultHandler) {
        mResultHandler = resultHandler == null ? mDefaultResultHandler : resultHandler;

        long currentTime = System.currentTimeMillis();
        if (mLastTimeClicked != 0 && (currentTime < (mLastTimeClicked + ServiceBoundActivity.DOUBLE_CLICK_TIMESPAN))) {
            L.d("ignoring click on smi [" + item.coords[0] + "," + item.coords[1] + "," + item.coords[2] + "]");
            mResultHandler.onCancel();
            stop();
            return;
        }
        mLastTimeClicked = currentTime;

        if (item.requiresWifi && !mActivity.checkConnectivityIsWifi()) {
            UIUtils.showLongToast(mService, mService.getString(R.string.failed_to_show_action_screen_no_wifi));
            mResultHandler.onCancel();
            stop();
            return;
        }

        mContextMatch = "MENU_" + UUID.randomUUID().toString();
        PressMenuIconRequestTO request = new PressMenuIconRequestTO();
        request.coords = item.coords;
        request.service = mEmail;
        request.context = mContextMatch;
        request.generation = menuGeneration;
        request.hashed_tag = item.hashedTag;
        request.timestamp = System.currentTimeMillis() / 1000;

        final IntentFilter filter = new IntentFilter(MessagingPlugin.NEW_MESSAGE_RECEIVED_INTENT);
        filter.addAction(MessagingPlugin.MESSAGE_JSMFR_ERROR);
        mActivity.registerReceiver(this, filter);

        if (item.staticFlowHash == null) {
            try {
                Rpc.pressMenuItem(new ResponseHandler<PressMenuIconResponseTO>(), request);
            } catch (Exception e) {
                L.bug(e);
                mResultHandler.onError();
                stop();
            }
        }

        if (item.screenBranding != null) {
            openBranding(item);
        } else if (item.staticFlowHash != null) {
            startLocalFlow(item, request);
        } else {
            poked();
        }
    }

    private void poked() {
        if (mActivity.checkConnectivity()) {
            mActivity.showTransmitting(new SafeRunnable() {
                @Override
                protected void safeRun() throws Exception {
                    mResultHandler.onTimeout();
                    stop();
                }
            });
        } else {
            mActivity.showActionScheduledDialog();
        }
    }

    private void startLocalFlow(ServiceMenuItemTO item, PressMenuIconRequestTO request) {
        mActivity.showTransmitting(new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                mResultHandler.onTimeout();
                stop();
            }
        });
        request.static_flow_hash = item.staticFlowHash;
        Map<String, Object> userInput = new HashMap<>();
        userInput.put("request", request.toJSONMap());
        userInput.put("func", "com.mobicage.api.services.pressMenuItem");

        MessageFlowRun mfr = new MessageFlowRun();
        mfr.staticFlowHash = item.staticFlowHash;
        try {
            JsMfr.executeMfr(mfr, userInput, mService, true);
        } catch (EmptyStaticFlowException ex) {
            mActivity.completeTransmit(null);
            AlertDialog.Builder builder = new AlertDialog.Builder(mService);
            builder.setMessage(ex.getMessage());
            builder.setPositiveButton(R.string.rogerthat, null);
            builder.create().show();
            mService.getPlugin(FriendsPlugin.class).requestStaticFlow(request.service, item);
        }
    }

    private void openBranding(ServiceMenuItemTO item) {
        final MessagingPlugin messagingPlugin = mService.getPlugin(MessagingPlugin.class);

        boolean brandingAvailable = false;
        try {
            brandingAvailable = messagingPlugin.getBrandingMgr().isBrandingAvailable(item.screenBranding);
        } catch (BrandingFailureException e) {
            // ignore
        }
        if (!brandingAvailable) {
            final FriendsPlugin friendsPlugin = mService.getPlugin(FriendsPlugin.class);
            final Friend friend = friendsPlugin.getStore().getExistingFriend(mEmail);
            friendsPlugin.getStore().addMenuDetails(friend);
            messagingPlugin.getBrandingMgr().queue(friend);
        }

        Intent intent = new Intent(mService, ActionScreenActivity.class);
        intent.putExtra(ActionScreenActivity.BRANDING_KEY, item.screenBranding);
        intent.putExtra(ActionScreenActivity.SERVICE_EMAIL, mEmail);
        intent.putExtra(ActionScreenActivity.ITEM_TAG_HASH, item.hashedTag);
        intent.putExtra(ActionScreenActivity.ITEM_LABEL, item.label);
        intent.putExtra(ActionScreenActivity.ITEM_COORDS, item.coords);
        intent.putExtra(ActionScreenActivity.CONTEXT_MATCH, mContextMatch);
        intent.putExtra(ActionScreenActivity.RUN_IN_BACKGROUND, item.runInBackground);
        mActivity.startActivity(intent);
        mResultHandler.onSuccess();
        stop();
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
                        if ((flags & MessagingPlugin.FLAG_DYNAMIC_CHAT) == MessagingPlugin.FLAG_DYNAMIC_CHAT) {
                            i = new Intent(context, FriendsThreadActivity.class);
                            final String parentKey = intent.getStringExtra("parent");
                            i.putExtra(FriendsThreadActivity.PARENT_MESSAGE_KEY, parentKey == null ? messageKey
                                    : parentKey);
                            i.putExtra(FriendsThreadActivity.MESSAGE_FLAGS, flags);
                        } else {
                            i = new Intent(context, ServiceMessageDetailActivity.class);
                            if (mActivity instanceof AbstractHomeActivity) {
                                i.putExtra(ServiceMessageDetailActivity.JUMP_TO_SERVICE_HOME_SCREEN, false);
                            }
                            i.putExtra("message", messageKey);
                        }
                        mActivity.startActivity(i);
                        mResultHandler.onSuccess();
                        stop();
                    }
                });
                return new String[]{action};
            }
        } else if (MessagingPlugin.MESSAGE_JSMFR_ERROR.equals(action)) {
            if (mContextMatch.equals(intent.getStringExtra("context")) && mActivity.isTransmitting()) {
                mContextMatch = "";
                mActivity.completeTransmit(new SafeRunnable() {
                    @Override
                    protected void safeRun() throws Exception {
                        UIUtils.showAlertDialog(mService, null, R.string.error_please_try_again);
                    }
                });
                mResultHandler.onError();
                stop();
            }
        }
        return null;
    }

    public void stop() {
        try {
            mActivity.unregisterReceiver(this);
        } catch (IllegalArgumentException e) {
            // receiver was not registered
        }
    }

}
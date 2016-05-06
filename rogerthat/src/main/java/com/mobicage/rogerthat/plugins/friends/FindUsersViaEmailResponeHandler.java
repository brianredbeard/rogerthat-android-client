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

import org.json.simple.JSONValue;

import android.content.Intent;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.AddFriendsActivity;
import com.mobicage.rogerthat.MainActivity;
import com.mobicage.rogerthat.config.Configuration;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.ui.UIUtils;
import com.mobicage.rpc.IResponse;
import com.mobicage.rpc.ResponseHandler;
import com.mobicage.to.friends.FindRogerthatUsersViaEmailResponseTO;

public class FindUsersViaEmailResponeHandler extends ResponseHandler<FindRogerthatUsersViaEmailResponseTO> {

    private boolean isAddFriendsVisisble() {
        T.UI();
        return UIUtils.getTopActivity(mMainService) instanceof AddFriendsActivity;
    }

    private void doNotification() {
        String title = mMainService.getString(R.string.app_name);
        String message = mMainService.getString(R.string.add_via_contacts_notification,
            mMainService.getString(R.string.app_name));
        int notificationId = R.integer.ab_scan_finished;
        boolean withSound = false;
        boolean withVibration = true;
        boolean withLight = false;
        boolean autoCancel = true;
        int icon = R.drawable.notification_icon;
        int notificationNumber = 0;
        String extra = AddFriendsActivity.INTENT_KEY_LAUNCHINFO;
        String extraData = AddFriendsActivity.INTENT_VALUE_SHOW_CONTACTS;
        String tickerText = null;
        long timestamp = mMainService.currentTimeMillis();

        UIUtils.doNotification(mMainService, title, message, notificationId,
            MainActivity.ACTION_NOTIFICATION_ADDRESSBOOK_SCAN, withSound, withVibration, withLight, autoCancel, icon,
            notificationNumber, extra, extraData, tickerText, timestamp);
    }

    @Override
    public void handle(IResponse<FindRogerthatUsersViaEmailResponseTO> result) {
        T.BIZZ();
        final FindRogerthatUsersViaEmailResponseTO response;
        try {
            response = result.getResponse();
        } catch (Exception e) {
            L.d("FindRogerthatUsersViaEmail api call failed", e);
            mMainService.postOnUIHandler(new SafeRunnable() {
                @Override
                protected void safeRun() throws Exception {
                    if (isAddFriendsVisisble()) {
                        Intent intent = new Intent(FriendsPlugin.ADDRESSBOOK_SCAN_FAILED_INTENT);
                        mMainService.sendBroadcast(intent);
                    } else {
                        doNotification();
                    }
                }
            });
            return;
        }

        mMainService.postOnUIHandler(new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                final Configuration cfg = mMainService.getConfigurationProvider().getConfiguration(
                    AddFriendsActivity.CONFIG);
                cfg.put(AddFriendsActivity.AB_RESULT, JSONValue.toJSONString(response.toJSONMap()));
                mMainService.getConfigurationProvider().updateConfigurationNow(AddFriendsActivity.CONFIG, cfg);

                if (isAddFriendsVisisble()) {
                    Intent intent = new Intent(FriendsPlugin.ADDRESSBOOK_SCANNED_INTENT);
                    mMainService.sendBroadcast(intent);
                } else {
                    doNotification();
                }
            }
        });

    }

}
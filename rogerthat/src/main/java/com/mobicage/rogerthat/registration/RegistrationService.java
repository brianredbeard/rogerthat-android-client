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

package com.mobicage.rogerthat.registration;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.MainActivity;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.ui.UIUtils;

public class RegistrationService extends Service {

    public static final String INTENT_SHOW_NOTIFICATION = "com.mobicage.rogerthat.registration.ShowNotification";
    public static final String INTENT_HIDE_NOTIFICATION = "com.mobicage.rogerthat.registration.hideNotification";

    private BroadcastReceiver mBroadcastReceiver;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        final IntentFilter filter = new IntentFilter();
        filter.addAction(INTENT_SHOW_NOTIFICATION);
        filter.addAction(INTENT_HIDE_NOTIFICATION);

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(INTENT_SHOW_NOTIFICATION)) {
                    UIUtils.doNotification(RegistrationService.this, getString(R.string.app_name),
                        getString(R.string.registration_enter_pin), R.integer.enter_pin,
                        MainActivity.ACTION_NOTIFICATION_ENTER_PIN, false, false, false, false,
                        R.drawable.notification_icon, 0, null, null, getString(R.string.registration_enter_pin),
                        System.currentTimeMillis());
                } else if (intent.getAction().equals(INTENT_HIDE_NOTIFICATION)) {
                    UIUtils.cancelNotification(RegistrationService.this, R.integer.enter_pin);
                } else {
                    L.bug("Unkown intent for registration service: " + intent.getAction());
                }
            }
        };
        registerReceiver(mBroadcastReceiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
    }
}
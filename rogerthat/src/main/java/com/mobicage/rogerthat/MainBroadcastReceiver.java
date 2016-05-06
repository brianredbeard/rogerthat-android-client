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

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeBroadcastReceiver;
import com.mobicage.rogerthat.util.system.T;

public class MainBroadcastReceiver extends SafeBroadcastReceiver {

    @Override
    public String[] onSafeReceive(final Context context, final Intent intent) {

        T.setUIThread("BootCompletedBroadcastReceiver.onReceive()");

        final String action = intent.getAction();
        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            L.d("Received ACTION_BOOT_COMPLETED");
            final Intent launchServiceIntent = new Intent(context, MainService.class);
            launchServiceIntent.putExtra(MainService.START_INTENT_BOOTTIME_EXTRAS_KEY, true);
            context.startService(launchServiceIntent);
        } else if (action.equals(ConnectivityManager.ACTION_BACKGROUND_DATA_SETTING_CHANGED)) {
            L.d("Received ACTION_BACKGROUND_DATA_SETTING_CHANGED");
            final Intent launchServiceIntent = new Intent(context, MainService.class);
            launchServiceIntent.putExtra(MainService.START_INTENT_BACKGROUND_DATA_SETTING_CHANGED_EXTRAS_KEY, true);
            context.startService(launchServiceIntent);
        }

        return null;
    }

}
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
package com.mobicage.rogerthat.util.sms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.mobicage.rogerthat.MainService;

public class SMSReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent rp_intent = new Intent(SMSManager.SMS_RECEIVED_INTENT);
        rp_intent.putExtras(intent);
        context.sendBroadcast(rp_intent, MainService.getInternalIntentPermission(context));
    }

}

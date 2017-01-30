/*
 * Copyright 2017 Mobicage NV
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
 * @@license_version:1.2@@
 */

package com.mobicage.rogerthat.util.system;

import java.util.Map;

import org.jivesoftware.smack.util.Cache;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.mobicage.rogerthat.util.logging.L;

public abstract class SafeBroadcastReceiver extends BroadcastReceiver {

    public static final String INTENT_CREATION_TIMESTAMP = "__CREATION_TIMESTAMP";

    private Map<String, Long> mIgnoredActionsMap = new Cache<String, Long>(1000, 1000 * 10);

    /**
     * This method is called when the BroadcastReceiver is receiving an Intent broadcast.
     * 
     * @param context
     *            The Context in which the receiver is running.
     * @param intent
     *            The Intent being received.
     * @return An array of intent actions you want to ignore <b>IF</b> they have an older creation timestamp than this
     *         intent
     */
    public abstract String[] onSafeReceive(Context context, Intent intent);

    @Override
    final public void onReceive(Context context, Intent intent) {
        try {
            String action = intent.getAction();
            long creationTimestamp = intent.getLongExtra(INTENT_CREATION_TIMESTAMP, 0);

            Long lastProcessedIntentTimestamp = mIgnoredActionsMap.get(action);
            if (lastProcessedIntentTimestamp != null && lastProcessedIntentTimestamp >= creationTimestamp) {
                L.d(this.getClass().getName() + " - already processed a more recent intent than this one: " + action);
                return;
            }

            String[] actions = onSafeReceive(context, intent);
            if (actions != null) {
                for (String a : actions) {
                    mIgnoredActionsMap.put(a, creationTimestamp);
                }
            }
        } catch (Exception e) {
            L.bug("Error in SafeBroadcastReceiver", e);
        }
    }
}

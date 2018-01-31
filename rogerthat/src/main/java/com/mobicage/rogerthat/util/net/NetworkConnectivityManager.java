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

package com.mobicage.rogerthat.util.net;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.mobicage.rogerthat.MainService;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeBroadcastReceiver;
import com.mobicage.rogerthat.util.system.T;

public class NetworkConnectivityManager {

    public final static String INTENT_NETWORK_UP = "com.mobicage.rogerthat.util.net.NETWORK_UP";
    public final static String INTENT_NETWORK_DOWN = "com.mobicage.rogerthat.util.net.NETWORK_DOWN";

    private final ConnectivityManager mConnectivityManager;
    private final MainService mMainService;
    private final ConnectivityBroadcastReceiver mReceiver;

    private boolean mIsListening = false;

    private class ConnectivityBroadcastReceiver extends SafeBroadcastReceiver {

        @Override
        public String[] onSafeReceive(Context context, Intent intent) {
            T.UI();

            final String action = intent.getAction();

            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {

                logNetworkState();

                if (isConnected()) {
                    L.d("NetworkConnectivityManager status change: up");
                    mMainService.sendBroadcast(new Intent(INTENT_NETWORK_UP));
                } else {
                    L.d("NetworkConnectivityManager status change: down");
                    mMainService.sendBroadcast(new Intent(INTENT_NETWORK_DOWN));
                }
            }
            return new String[] { intent.getAction() };
        }
    }

    public String getNetworkState() {
        T.dontCare();
        final StringBuilder sb = new StringBuilder();
        try {
            sb.append(mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).toString());
        } catch (Exception e) {
            sb.append("Cannot retrieve WIFI state");
        }
        sb.append('\n');
        try {
            sb.append(mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).toString());
        } catch (Exception e) {
            sb.append("Cannot retrieve mobile data state");
        }
        sb.append('\n');
        return sb.toString();
    }

    private void logNetworkState() {
        T.dontCare();
        L.highlight(getNetworkState());
    }

    public boolean isConnected() {
        T.dontCare();
        return isWifiConnected() || isMobileDataConnected();
    }

    public boolean isWifiConnected() {
        T.dontCare();
        final NetworkInfo info = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return (info != null && info.isConnected());
    }

    public boolean isMobileDataConnected() {
        T.dontCare();
        final NetworkInfo info = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        return (info != null && info.isConnected());
    }

    public NetworkConnectivityManager(MainService mainService) {
        T.UI();
        mMainService = mainService;
        mConnectivityManager = (ConnectivityManager) mMainService.getSystemService(Context.CONNECTIVITY_SERVICE);
        mReceiver = new ConnectivityBroadcastReceiver();
    }

    public void teardown() {
        T.UI();
        stopListening();
    }

    public void startListening() {
        T.UI();
        if (!mIsListening) {
            final IntentFilter filter = new IntentFilter();
            filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            mMainService.registerReceiver(mReceiver, filter);
            mIsListening = true;
        }
    }

    private void stopListening() {
        T.UI();
        if (mIsListening) {
            mMainService.unregisterReceiver(mReceiver);
            mIsListening = false;
        }
    }

}

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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.plugins.friends.FriendsPlugin;
import com.mobicage.rogerthat.util.ActivityUtils;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeBroadcastReceiver;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rpc.config.AppConstants;

public class QRCodeActivity extends ServiceBoundActivity {

    protected String mAction;

    private static final String[] UPDATE_VIEW_INTENTS = new String[]{FriendsPlugin.FRIENDS_LIST_REFRESHED,
            FriendsPlugin.FRIEND_ADDED_INTENT, FriendsPlugin.FRIEND_MARKED_FOR_REMOVAL_INTENT,
            FriendsPlugin.FRIEND_REMOVED_INTENT};

    private BroadcastReceiver mBroadcastReceiver;

    private IdentityStore mIdentityStore;
    private MyIdentity mIdentity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        mAction = intent.getStringExtra(ServiceActionsOfflineActivity.ACTION);

        setContentView(R.layout.qr_code);
        setActivityName("qrcode");
        setTitle(getString(R.string.passport, getString(R.string.app_name)));
    }

    @Override
    protected void onServiceBound() {
        mIdentityStore = mService.getIdentityStore();
        mBroadcastReceiver = getBroadCastReceiver();

        IntentFilter filter = new IntentFilter(IdentityStore.IDENTITY_CHANGED_INTENT);
        registerReceiver(mBroadcastReceiver, filter);

        final TextView headerTextView = (TextView) findViewById(R.id.loyalty_text);
        headerTextView.setText(getString(AppConstants.HOMESCREEN_QRCODE_HEADER, getString(R.string.app_name)));

        updateView();
    }

    @Override
    protected void onServiceUnbound() {
        unregisterReceiver(mBroadcastReceiver);
    }


    private BroadcastReceiver getBroadCastReceiver() {
        return new SafeBroadcastReceiver() {

            @Override
            public String[] onSafeReceive(Context context, Intent intent) {
                String action = intent.getAction();
                L.i("onSafeReceive: " + action);
                updateView();
                return null;
            }

        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        T.UI();
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.qrcode_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        T.UI();

        switch (item.getItemId()) {
            case R.id.scan:
                ActivityUtils.goToScanActivity(QRCodeActivity.this, false, null);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateView() {
        mIdentity = mIdentityStore.getIdentity();

        ImageView imageView = ((ImageView) findViewById(R.id.qrcode));
        final Bitmap qrBitmap = mIdentity.getQRBitmap();
        if (qrBitmap != null) {
            imageView.setImageBitmap(qrBitmap);
        }
    }
}

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

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.plugins.friends.FriendSearchActivity;
import com.mobicage.rogerthat.plugins.friends.FriendsPlugin;
import com.mobicage.rogerthat.plugins.history.HistoryListActivity;
import com.mobicage.rogerthat.plugins.messaging.Message;
import com.mobicage.rogerthat.plugins.messaging.MessagingActivity;
import com.mobicage.rogerthat.plugins.messaging.MessagingPlugin;
import com.mobicage.rogerthat.plugins.scan.ProcessScanActivity;
import com.mobicage.rogerthat.plugins.scan.ScanTabActivity;
import com.mobicage.rogerthat.util.RegexPatterns;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rpc.config.AppConstants;

import java.util.ArrayList;

public class HomeActivity extends ServiceBoundTabActivity {

    private static final String INTENT_PROCESSED = "processed";
    private final static int MESSAGING_TAB = 0;
    @SuppressWarnings("unused")
    private final static int SERVICES_TAB = 1;
    private final static int FRIENDS_TAB = 2;
    @SuppressWarnings("unused")
    private final static int HISTORY_TAB = 3;
    private final static int SCAN_TAB = 4;

    public final static String INTENT_KEY_LAUNCHINFO = "launchInfo";
    public final static String INTENT_VALUE_SHOW_MESSAGES = "showMessages";
    public final static String INTENT_VALUE_SHOW_NEW_MESSAGES = "showNewMessages";
    public final static String INTENT_VALUE_SHOW_UPDATED_MESSAGES = "showUpdatedMessages";
    public final static String INTENT_VALUE_SHOW_FRIENDS = "showFriends";
    public final static String INTENT_VALUE_SHOW_SCANTAB = "showScanTab";
    public final static String INTENT_KEY_MESSAGE = "messageKey";
    private MessagingPlugin mMessagingPlugin;
    private Intent mNotYetProcessedIntent = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        T.UI();

        super.onCreate(savedInstanceState);
        initUI();

        final TabHost tabHost = getTabHost(); // The activity TabHost
        tabHost.setCurrentTab(MESSAGING_TAB);

        Intent intent = getIntent();
        if (mService == null) {
            mNotYetProcessedIntent = intent;
        } else {
            processIntent(intent);
        }
    }

    private void initUI() {
        setContentView(AppConstants.HOME_ACTIVITY_LAYOUT);

        Resources res = getResources(); // Resource object to get Drawables
        final TabHost tabHost = getTabHost(); // The activity TabHost
        TabHost.TabSpec spec; // Reusable TabSpec for each tab

        spec = tabHost.newTabSpec("messaging")
            .setIndicator(getString(R.string.tab_messaging), res.getDrawable(R.drawable.ic_tab_messaging_icons_set))
            .setContent(new Intent(this, MessagingActivity.class));
        tabHost.addTab(spec);

        spec = tabHost.newTabSpec("services")
            .setIndicator(getString(R.string.tab_services), res.getDrawable(R.drawable.ic_tab_services_icons_set))
            .setContent(new Intent(this, ServiceFriendsActivity.class));
        tabHost.addTab(spec);

        final int text;
        switch (AppConstants.FRIENDS_CAPTION) {
        case COLLEAGUES:
            text = R.string.colleagues;
            break;
        case CONTACTS:
            text = R.string.contacts;
            break;
        case FRIENDS:
        default:
            text = R.string.tab_friends;
            break;
        }
        spec = tabHost
            .newTabSpec("friends")
            .setIndicator(getString(text), res.getDrawable(R.drawable.ic_tab_friends_icons_set))
            .setContent(
                new Intent(this, AppConstants.FRIENDS_ENABLED ? UserFriendsActivity.class : FriendSearchActivity.class));
        tabHost.addTab(spec);

        spec = tabHost.newTabSpec("stream")
            .setIndicator(getString(R.string.tab_stream), res.getDrawable(R.drawable.ic_tab_activity_icons_set))
            .setContent(new Intent(this, HistoryListActivity.class));
        tabHost.addTab(spec);

        spec = tabHost.newTabSpec("scan")
            .setIndicator(getString(R.string.scan), res.getDrawable(R.drawable.ic_tab_scan_icons_set))
            .setContent(new Intent(this, ScanTabActivity.class));
        tabHost.addTab(spec);

    }

    public static void startWithLaunchInfo(final Activity activity, final String launchInfo) {
        final Intent intent = new Intent(activity, HomeActivity.class);
        intent.putExtra(INTENT_KEY_LAUNCHINFO, launchInfo);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        activity.startActivity(intent);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        L.d("HomeActivity.onNewIntent()");
        if (mService == null) {
            mNotYetProcessedIntent = intent;
        } else {
            processIntent(intent);
        }
    }

    private void goToMessagingActivity() {
        Intent i = new Intent(this, HomeActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }

    private void processIntent(Intent intent) {
        final String url = intent.getDataString();
        if (intent.getBooleanExtra(INTENT_PROCESSED, false))
            return;
        if (url != null) {
            goToMessagingActivity();
            processUrl(url);
        } else if (intent.hasExtra(INTENT_KEY_LAUNCHINFO)) {
            String value = intent.getStringExtra(INTENT_KEY_LAUNCHINFO);
            if (INTENT_VALUE_SHOW_FRIENDS.equals(value)) {
                getTabHost().setCurrentTab(FRIENDS_TAB);

            } else if (INTENT_VALUE_SHOW_MESSAGES.equals(value)) {
                goToMessagingActivity();

            } else if (INTENT_VALUE_SHOW_NEW_MESSAGES.equals(value)) {
                if (intent.hasExtra(INTENT_KEY_MESSAGE)) {
                    String messageKey = intent.getStringExtra(INTENT_KEY_MESSAGE);
                    goToMessageDetail(messageKey);
                } else {
                    goToMessagingActivity();
                }

            } else if (INTENT_VALUE_SHOW_UPDATED_MESSAGES.equals(value)) {
                if (intent.hasExtra(INTENT_KEY_MESSAGE)) {
                    String messageKey = intent.getStringExtra(INTENT_KEY_MESSAGE);
                    goToMessageDetail(messageKey);
                } else {
                    goToMessagingActivity();
                }

            } else if (INTENT_VALUE_SHOW_SCANTAB.equals(value)) {
                getTabHost().setCurrentTab(SCAN_TAB);

            } else {
                L.bug("Unexpected (key, value) for HomeActivity intent: (" + INTENT_KEY_LAUNCHINFO + ", " + value + ")");
            }
            if (mMessagingPlugin != null)
                mMessagingPlugin.updateMessagesNotification(false, false, false);
        }
        intent.putExtra(INTENT_PROCESSED, true);
    }

    private void goToMessageDetail(final String messageKey) {
        Message message = mMessagingPlugin.getStore().getPartialMessageByKey(messageKey);
        mMessagingPlugin.showMessage(this, message, null);
    }

    private void processUrl(final String url) {
        T.UI();
        if (RegexPatterns.OPEN_HOME_URL.matcher(url).matches())
            return;
        else if (RegexPatterns.FRIEND_INVITE_URL.matcher(url).matches()
            || RegexPatterns.SERVICE_INTERACT_URL.matcher(url).matches()) {
            final Intent launchIntent = new Intent(this, ProcessScanActivity.class);
            launchIntent.putExtra(ProcessScanActivity.URL, url);
            launchIntent.putExtra(ProcessScanActivity.SCAN_RESULT, false);
            startActivity(launchIntent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        T.UI();
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.home_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        T.UI();
        switch (item.getItemId()) {
        case R.id.about: {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
            break;
        }
        case R.id.settings: {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            break;
        }
        default:
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        L.d(this.getClass().getName() + ".onDestroy()");
    }

    @Override
    protected void onServiceBound() {
        mService.addHighPriorityIntent(FriendsPlugin.SERVICE_ACTION_INFO_RECEIVED_INTENT);
        mMessagingPlugin = mService.getPlugin(MessagingPlugin.class);
        if (mNotYetProcessedIntent != null) {
            processIntent(mNotYetProcessedIntent);
            mNotYetProcessedIntent = null;
        }
    }

    @Override
    protected void onServiceUnbound() {
        mMessagingPlugin.updateMessagesNotification(false, false, false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        T.UI();
        L.i("onRequestPermissionsResult");
        if (requestCode == ScanTabActivity.MY_PERMISSIONS_REQUEST_CAMERA) {
            Intent broadcast = new Intent(ScanTabActivity.PERMISSION_CAMERA_UPDATED);
            broadcast.putExtra("permissions", permissions);
            broadcast.putExtra("grantResults", grantResults);
            mService.sendBroadcast(broadcast, true, true);
        }
    }

    @Override
    public MainService getMainService() {
        return mService;
    }

}

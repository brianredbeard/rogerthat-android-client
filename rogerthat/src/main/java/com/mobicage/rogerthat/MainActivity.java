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

import java.util.List;
import java.util.Map;

import org.json.simple.JSONValue;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.config.Configuration;
import com.mobicage.rogerthat.config.ConfigurationProvider;
import com.mobicage.rogerthat.plugins.friends.Friend;
import com.mobicage.rogerthat.plugins.friends.FriendStore;
import com.mobicage.rogerthat.plugins.friends.FriendsPlugin;
import com.mobicage.rogerthat.plugins.friends.ServiceActionMenuActivity;
import com.mobicage.rogerthat.plugins.messaging.BrandingFailureException;
import com.mobicage.rogerthat.plugins.messaging.BrandingMgr;
import com.mobicage.rogerthat.plugins.messaging.ServiceMessageDetailActivity;
import com.mobicage.rogerthat.plugins.scan.ProcessScanActivity;
import com.mobicage.rogerthat.plugins.scan.ProfileActivity;
import com.mobicage.rogerthat.plugins.scan.ScanTabActivity;
import com.mobicage.rogerthat.registration.ContentBrandingRegistrationActivity;
import com.mobicage.rogerthat.registration.DetectedBeaconActivity;
import com.mobicage.rogerthat.registration.RegistrationActivity2;
import com.mobicage.rogerthat.registration.RegistrationWizard2;
import com.mobicage.rogerthat.registration.YSAAARegistrationActivity;
import com.mobicage.rogerthat.util.TextUtils;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeBroadcastReceiver;
import com.mobicage.rogerthat.util.system.SafeDialogInterfaceOnClickListener;
import com.mobicage.rogerthat.util.system.SystemUtils;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.ui.UIUtils;
import com.mobicage.rogerthat.widget.SendCannedMessageActivity;
import com.mobicage.rpc.config.AppConstants;
import com.mobicage.rpc.config.CloudConstants;
import com.mobicage.to.friends.ServiceMenuItemTO;

public class MainActivity extends ServiceBoundActivity {

    public static final int FLAG_CLEAR_STACK = Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP
        | Intent.FLAG_ACTIVITY_SINGLE_TOP;

    public static final String ACTION_WIDGET_MAIN = "ROGERTHAT_ACTION_WIDGET_MAIN";
    public static final String ACTION_WIDGET_SCAN = "ROGERTHAT_ACTION_WIDGET_SCAN";
    public static final String ACTION_WIDGET_COMPOSE = "ROGERTHAT_ACTION_WIDGET_COMPOSE";
    public static final String ACTION_WIDGET_SEND_CANNED_MSG = "ROGERTHAT_ACTION_WIDGET_SEND_CANNED_MSG";

    public static final String ACTION_NOTIFICATION_ENTER_PIN = "ROGERTHAT_ACTION_NOTIFICATION_ENTER_PIN";
    public static final String ACTION_NOTIFICATION_MESSAGE_UPDATES = "ROGERTHAT_ACTION_NOTIFICATION_MESSAGE_UPDATES";
    public static final String ACTION_NOTIFICATION_ADDRESSBOOK_SCAN = "ROGERTHAT_ACTION_NOTIFICATION_SCAN_AB_RESULT ";
    public static final String ACTION_NOTIFICATION_FACEBOOK_SCAN = "ROGERTHAT_ACTION_NOTIFICATION_SCAN_FB_RESULT ";
    public static final String ACTION_NOTIFICATION_PHOTO_UPLOAD_DONE = "ROGERTHAT_ACTION_NOTIFICATION_PHOTO_UPLOAD_DONE";
    public static final String ACTION_NOTIFICATION_OPEN_APP = "ROGERTHAT_ACTION_NOTIFICATION_OPEN_APP";

    public static final String ACTION_REGISTERED = "ROGERTHAT_ACTION_REGISTERED";
    public static final String ACTION_COMPLETE_PROFILE = "ROGERTHAT_ACTION_COMPLETE_PROFILE";
    public static final String ACTION_COMPLETE_PROFILE_FINISHED = "ROGERTHAT_ACTION_COMPLETE_PROFILE_FINISHED";
    public static final String ACTION_SHOW_DETECTED_BEACONS = "ROGERTHAT_ACTION_SHOW_DETECTED_BEACONS";

    private ProgressDialog mDialog;
    private AlertDialog mRegistrationCompleteDialog = null;

    private final SafeBroadcastReceiver mBroadcastReceiver = new SafeBroadcastReceiver() {
        @Override
        public String[] onSafeReceive(Context context, Intent intent) {
            T.UI();
            launchYSAAAActivityAndFinish();
            return new String[] { intent.getAction() };
        };
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        T.UI();
        setContentView(R.layout.blank);
    }

    @Override
    protected void onServiceBound() {
        T.UI();
        processIntent();

        if (CloudConstants.isYSAAA()) {
            final String[] receivingIntents = new String[] { FriendsPlugin.FRIENDS_LIST_REFRESHED,
                    FriendsPlugin.FRIEND_UPDATE_INTENT, FriendsPlugin.FRIEND_ADDED_INTENT,
                    BrandingMgr.SERVICE_BRANDING_AVAILABLE_INTENT, BrandingMgr.GENERIC_BRANDING_AVAILABLE_INTENT };

            IntentFilter filter = new IntentFilter();
            for (String action : receivingIntents)
                filter.addAction(action);
            registerReceiver(mBroadcastReceiver, filter);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        SystemUtils.logIntentFlags(getIntent());
        if (mService != null)
            processIntent();
    }

    private void processIntent() {
        boolean hasRegistered = mService.getRegisteredFromConfig();

        final Intent intent = getIntent();
        final String intentAction = intent.getAction();
        L.d("MainActivity processIntent: " + intentAction);
        L.d("Extras:");
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            for (String key : bundle.keySet()) {
                Object value = bundle.get(key);
                L.d("- " + String.format("%s %s (%s)", key, value.toString(), value.getClass().getName()));
            }
        }

        if (ACTION_WIDGET_SCAN.equals(intentAction) || ACTION_WIDGET_COMPOSE.equals(intentAction)
            || ACTION_WIDGET_SEND_CANNED_MSG.equals(intentAction)) {

            // Started via one of the last 3 widget buttons
            processWidgetIntent(intent, hasRegistered);

        } else if (ACTION_NOTIFICATION_ADDRESSBOOK_SCAN.equals(intentAction)
            || ACTION_NOTIFICATION_FACEBOOK_SCAN.equals(intentAction)) {
            processAddFriendsIntent(intent, hasRegistered);

        } else if (ACTION_NOTIFICATION_PHOTO_UPLOAD_DONE.equals(intentAction)) {
            processPhotoUploadDoneIntent(intent, hasRegistered);

        } else if (ACTION_NOTIFICATION_ENTER_PIN.equals(intentAction)) {
            launchRegistrationActivityAndFinish(null, FLAG_CLEAR_STACK);

        } else if (ACTION_NOTIFICATION_MESSAGE_UPDATES.equals(intentAction)) {
            processMessageUpdatesIntent(intent, hasRegistered);

        } else if (ACTION_REGISTERED.equals(intentAction)) {
            showRegistrationCompleteDialogAndGoToHomeActivity();

        } else if (ACTION_COMPLETE_PROFILE.equals(intentAction)) {
            launchProfileActivityAndFinish(null, FLAG_CLEAR_STACK, false);

        } else if (ACTION_COMPLETE_PROFILE_FINISHED.equals(intentAction)) {
            showRegistrationCompleteDialogAndGoToHomeActivity();

        } else if (ACTION_SHOW_DETECTED_BEACONS.equals(intentAction)) {
            launchDetectedBeaconsAndFinish(null, FLAG_CLEAR_STACK,
                intent.getStringExtra(DetectedBeaconActivity.EXTRA_DETECTED_BEACONS),
                intent.getBooleanExtra(DetectedBeaconActivity.EXTRA_AGE_AND_GENDER_SET, true));
        } else {
            final Uri qrUri;
            final int flags;

            if (Intent.ACTION_VIEW.equals(intentAction) && intent.getData() != null) {
                if (intent.getData().getScheme().equals("mdp-" + CloudConstants.APP_ID)) {
                    // Redirected via MYDIGIPASS
                    qrUri = null;
                    flags = 0;
                } else {
                    // Started via rogerthat://
                    qrUri = intent.getData();
                    flags = FLAG_CLEAR_STACK;
                }

            } else {
                // Started via Launcher
                // Started via Recents (long-press home button)
                // Started via Eclipse
                // Started via Google play
                // Started via App widget

                // We do not check action, it could be MAIN or WIDGET_MAIN
                if (!isTaskRoot()) {
                    if ((intent.getFlags() & Intent.FLAG_ACTIVITY_NEW_TASK) == 0) {
                        // We are launched from another app, and it did not set the NEW_TASK flag
                        L.d("MainActivity on existing stack - without FLAG_ACTIVITY_NEW_TASK");
                        qrUri = null;
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED;
                    } else {
                        L.d("MainActivity on existing stack - finish right away");
                        finish();
                        return;
                    }
                } else {
                    L.d("MainActivity - creating new task stack");
                    qrUri = null;
                    flags = 0;
                }
            }

            if (hasRegistered) {
                if (CloudConstants.isContentBrandingApp()) {
                    launchContentBrandingMainActivityAndFinish();
                } else if (CloudConstants.isYSAAA()) {
                    launchYSAAAActivityAndFinish();
                } else {
                    launchHomeActivityAndFinish(qrUri, flags);
                }
            } else {
                launchRegistrationActivityAndFinish(qrUri, flags);
            }
        }
    }

    private void processWidgetIntent(Intent intent, boolean hasRegistered) {
        if (hasRegistered) {
            launchHomeActivityAndFinish(null, FLAG_CLEAR_STACK);

            if (ACTION_WIDGET_SCAN.equals(intent.getAction())) {
                startScanner();

            } else if (ACTION_WIDGET_COMPOSE.equals(intent.getAction())) {
                startSendMessageWizard();

            } else if (ACTION_WIDGET_SEND_CANNED_MSG.equals(intent.getAction())) {
                startSendCannedMessage();
            }
        } else {
            alertMustRegisterFirst();
        }
    }

    private void processAddFriendsIntent(Intent intent, boolean hasRegistered) {
        if (hasRegistered) {
            launchHomeActivityAndFinish(null, FLAG_CLEAR_STACK);

            Intent i = new Intent(this, AddFriendsActivity.class);
            i.putExtras(intent.getExtras());
            startActivity(i);
        } else {
            alertMustRegisterFirst();
        }
    }

    private void processPhotoUploadDoneIntent(Intent intent, boolean hasRegistered) {
        if (hasRegistered) {
            UIUtils.cancelNotification(mService, R.integer.transfer_complete_continue);
            launchHomeActivityAndFinish(null, FLAG_CLEAR_STACK);

            Intent i = new Intent(this, ServiceMessageDetailActivity.class);
            String jsonString = intent.getStringExtra("data");
            if (jsonString != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) JSONValue.parse(jsonString);
                Bundle bundle = new Bundle();
                for (Map.Entry<String, Object> entry : data.entrySet()) {
                    if (entry.getValue() instanceof String)
                        bundle.putString(entry.getKey(), (String) entry.getValue());
                }
                i.putExtras(bundle);
            }

            i.setAction(ACTION_NOTIFICATION_PHOTO_UPLOAD_DONE);
            i.putExtra("message", intent.getStringExtra("message_key"));
            i.putExtra("threadKey", intent.getStringExtra("threadKey"));
            i.putExtra("submitToJSMFR", intent.getStringExtra("submitToJSMFR"));
            startActivity(i);
            finish();
        } else {
            alertMustRegisterFirst();
        }
    }

    private void processMessageUpdatesIntent(Intent intent, boolean hasRegistered) {
        if (hasRegistered) {
            if (CloudConstants.isYSAAA()) {
                final FriendsPlugin friendsPlugin = mService.getPlugin(FriendsPlugin.class);
                final FriendStore friendStore = friendsPlugin.getStore();
                List<String> friends = friendStore.getEmails();
                if (friends.size() == 1) {
                    Friend friend = friendStore.getFriend(friends.get(0));
                    Intent i = new Intent(this, ServiceActionMenuActivity.class);
                    i.setFlags(FLAG_CLEAR_STACK);
                    intent.putExtra(ServiceActionMenuActivity.SERVICE_EMAIL, friend.email);
                    intent.putExtra(ServiceActionMenuActivity.MENU_PAGE, 0);
                    i.putExtras(intent.getExtras());
                    startActivity(i);
                    finish();
                }
            } else {
                Intent i = new Intent(this, HomeActivity.class);
                i.setFlags(FLAG_CLEAR_STACK);
                i.putExtras(intent.getExtras());
                startActivity(i);
                finish();
            }
        } else {
            alertMustRegisterFirst();
        }
    }

    private void alertMustRegisterFirst() {
        // User must register first
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.register_first);
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                launchRegistrationActivityAndFinish(null, FLAG_CLEAR_STACK);
            }
        });
        builder.setPositiveButton(R.string.rogerthat, new SafeDialogInterfaceOnClickListener() {
            @Override
            public void safeOnClick(DialogInterface dialog, int which) {
                launchRegistrationActivityAndFinish(null, FLAG_CLEAR_STACK);
            }
        });
        builder.create().show();
    }

    @Override
    protected void onServiceUnbound() {
        T.UI();
        if (CloudConstants.isYSAAA()) {
            unregisterReceiver(mBroadcastReceiver);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        T.UI();
        if (mDialog != null)
            mDialog.dismiss();
    }

    public static Friend getFriendForYSAAAWhenReady(MainService service) {
        L.i("launchMainActivityAndFinishIfAppReady");
        final FriendsPlugin friendsPlugin = service.getPlugin(FriendsPlugin.class);
        final FriendStore friendStore = friendsPlugin.getStore();
        List<String> friends = friendStore.getEmails();
        if (friends.size() == 0) {
            L.i("Service not available yet");
            return null;
        }
        if (friends.size() == 1) {
            Friend friend = friendStore.getFriend(friends.get(0));
            friendStore.addMenuDetails(friend);

            if (friend.descriptionBranding != null) {
                boolean brandingAvailable = false;
                try {
                    brandingAvailable = friendsPlugin.getBrandingMgr().isBrandingAvailable(friend.descriptionBranding);
                } catch (BrandingFailureException e) {
                    // ignore
                }
                if (!brandingAvailable) {
                    L.i("Description branding not available yet");
                    return null;
                }
            }

            if (friend.actionMenu == null) {
                L.i("Friend does not have an actionMenu yet");
                return null;
            } else {
                if (friend.actionMenu.branding != null) {
                    boolean brandingAvailable = false;
                    try {
                        brandingAvailable = friendsPlugin.getBrandingMgr().isBrandingAvailable(
                            friend.actionMenu.branding);
                    } catch (BrandingFailureException e) {
                        // ignore
                    }
                    if (!brandingAvailable) {
                        L.i("Action menu branding not available yet");
                        return null;
                    }
                } else if (friend.actionMenu == null) {
                    L.i("Action menu not available yet");
                    return null;
                }

                boolean hasMenuIconsToDownload = false;
                for (ServiceMenuItemTO item : friend.actionMenu.items) {
                    if (item.iconHash != null && !friendsPlugin.isMenuIconAvailable(item.iconHash)) {
                        hasMenuIconsToDownload = true;
                        break;
                    }
                }
                if (hasMenuIconsToDownload) {
                    L.i("Still has icons to download");
                    return null;
                }

                boolean hasStaticFlowsToDownload = false;
                for (ServiceMenuItemTO item : friend.actionMenu.items) {
                    if (!TextUtils.isEmptyOrWhitespace(item.staticFlowHash)) {
                        if (!friendsPlugin.isStaticFlowAvailable(item.staticFlowHash)) {
                            hasStaticFlowsToDownload = true;
                            break;
                        }
                    }
                }
                if (hasStaticFlowsToDownload) {
                    L.i("Still has static flows to download");
                    return null;
                }

                boolean hasBrandingsToDownload = false;
                for (ServiceMenuItemTO item : friend.actionMenu.items) {
                    if (!TextUtils.isEmptyOrWhitespace(item.screenBranding)) {
                        try {
                            if (!friendsPlugin.getBrandingMgr().isBrandingAvailable(friend.actionMenu.branding)) {
                                hasBrandingsToDownload = true;
                                break;
                            }
                        } catch (BrandingFailureException e) {
                            // ignore
                        }
                    }
                }
                if (hasBrandingsToDownload) {
                    L.i("Still has brandings to download");
                    return null;
                }
            }
            return friend;
        } else {
            L.bug("YSAAA user has more than 1 friend");
            return null;
        }
    }

    private void launchContentBrandingMainActivityAndFinish() {
        T.UI();
        Intent intent = new Intent(this, ContentBrandingMainActivity.class);
        intent.setFlags(MainActivity.FLAG_CLEAR_STACK);
        startActivity(intent);
        this.finish();
    }

    private void launchYSAAAActivityAndFinish() {
        T.UI();
        L.i("launchYSAAAActivityAndFinish");

        Friend f = MainActivity.getFriendForYSAAAWhenReady(mService);
        if (f != null) {
            if (mDialog != null)
                mDialog.dismiss();

            Intent intent = new Intent(this, ServiceActionMenuActivity.class);
            intent.setFlags(FLAG_CLEAR_STACK);
            intent.putExtra(ServiceActionMenuActivity.SERVICE_EMAIL, f.email);
            intent.putExtra(ServiceActionMenuActivity.MENU_PAGE, 0);
            startActivity(intent);
            finish();
        } else {
            showDownloadingDialog();
        }
    }

    private void showDownloadingDialog() {
        if (mDialog == null) {
            mDialog = new ProgressDialog(this);
            mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mDialog.setMessage(getString(R.string.downloading));
            mDialog.setCancelable(false);
            mDialog.show();
        }
    }

    private void launchRegistrationActivityAndFinish(final Uri qrUri, final int flags) {
        T.UI();
        if (CloudConstants.isContentBrandingApp()) {
            final Intent intent = new Intent(this, ContentBrandingRegistrationActivity.class);
            intent.setData(qrUri);
            intent.setFlags(flags);
            L.d("Starting ContentBrandingRegistrationActivity");
            startActivity(intent);
            finish();
        } else if (CloudConstants.isYSAAA()) {
            final Intent intent = new Intent(this, YSAAARegistrationActivity.class);
            intent.setData(qrUri);
            intent.setFlags(flags);
            L.d("Starting YSAAARegistrationActivity");
            startActivity(intent);
            finish();
        } else {
            final Intent intent = new Intent(this, RegistrationActivity2.class);
            intent.setData(qrUri);
            intent.setFlags(flags);
            L.d("Starting RegistrationActivity2");
            startActivity(intent);
            finish();
        }
    }

    private void showRegistrationCompleteDialogAndGoToHomeActivity() {
        if (CloudConstants.isRogerthatApp()) {
            Configuration cfg = mService.getConfigurationProvider().getConfiguration(RegistrationWizard2.CONFIGKEY);
            if (cfg != null && cfg.get(RegistrationActivity2.OPENED_URL_CONFIGKEY, null) != null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setCancelable(true);
                builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        try {
                            launchHomeActivityAndFinish(null, FLAG_CLEAR_STACK);
                        } catch (Exception e) {
                            L.bug(e);
                        }
                    }
                });
                // User pressed invitation/poke without secret
                builder.setTitle(R.string.registration_success_title);
                builder.setMessage(getString(R.string.registration_success_without_invitation,
                    getString(R.string.app_name)));
                builder.setPositiveButton(R.string.rogerthat, new SafeDialogInterfaceOnClickListener() {
                    @Override
                    public void safeOnClick(DialogInterface dialog, int which) {
                        launchHomeActivityAndFinish(null, FLAG_CLEAR_STACK);
                    }
                });
                mRegistrationCompleteDialog = builder.create();
                mRegistrationCompleteDialog.show();
            } else {
                // All other cases
                launchHomeActivityAndFinish(null, FLAG_CLEAR_STACK);
            }
        } else if (CloudConstants.isContentBrandingApp()) {
            launchContentBrandingMainActivityAndFinish();
        } else if (CloudConstants.isYSAAA()) {
            launchYSAAAActivityAndFinish();
        } else {
            launchHomeActivityAndFinish(null, FLAG_CLEAR_STACK);
        }
    }

    private void launchHomeActivityAndFinish(final Uri qrUri, final int flags) {
        T.UI();

        final Intent homeActivityIntent;
        if (AppConstants.HOME_ACTIVITY_LAYOUT == R.layout.homescreen_news) {
            homeActivityIntent  = new Intent(this, NewsHomeActivity.class);
        } else {
            homeActivityIntent  = new Intent(this, HomeActivity.class);
        }

        homeActivityIntent.setFlags(flags);
        homeActivityIntent.setData(qrUri);

        if (qrUri == null) {
            ConfigurationProvider cfgProvider = mService.getConfigurationProvider();
            Configuration cfg = cfgProvider.getConfiguration(RegistrationWizard2.CONFIGKEY);

            String caughtUrlDuringRegistration = cfg.get(RegistrationActivity2.OPENED_URL_CONFIGKEY, null);
            if (!TextUtils.isEmptyOrWhitespace(caughtUrlDuringRegistration)) {
                L.d("Starting HomeActivity");
                startActivity(homeActivityIntent);
                finish();

                startProcessScan(caughtUrlDuringRegistration);

                cfg.put(RegistrationActivity2.OPENED_URL_CONFIGKEY, "");
                cfgProvider.updateConfigurationNow(RegistrationWizard2.CONFIGKEY, cfg);
                return;

            } else if (cfg.get(RegistrationActivity2.QRSCAN_CONFIGKEY, false)) {
                L.d("Starting HomeActivity");
                startActivity(homeActivityIntent);
                finish();

                startScanner();

                cfg.put(RegistrationActivity2.QRSCAN_CONFIGKEY, false);
                cfgProvider.updateConfigurationNow(RegistrationWizard2.CONFIGKEY, cfg);
                return;

            }
        }
        L.d("Starting HomeActivity");
        startActivity(homeActivityIntent);
        finish();
    }

    private void launchDetectedBeaconsAndFinish(final Uri qrUri, final int flags, final String discoveredBeacons,
        final boolean ageGenderSet) {
        T.UI();
        final Intent intent = new Intent(this, DetectedBeaconActivity.class);
        intent.setData(qrUri);
        intent.setFlags(flags);
        intent.putExtra(DetectedBeaconActivity.EXTRA_DETECTED_BEACONS, discoveredBeacons);
        intent.putExtra(DetectedBeaconActivity.EXTRA_AGE_AND_GENDER_SET, ageGenderSet);
        L.d("Starting DetectedBeaconActivity");
        startActivity(intent);
    }

    private void launchProfileActivityAndFinish(final Uri qrUri, final int flags, final boolean ageGenderSet) {
        T.UI();
        final Intent intent = new Intent(this, ProfileActivity.class);
        intent.setData(qrUri);
        intent.setFlags(flags);
        intent.putExtra(ProfileActivity.INTENT_KEY_COMPLETE_PROFILE, ageGenderSet);
        L.d("Starting ProfileActivity");
        startActivity(intent);
    };

    private void startProcessScan(String caughtUrlDuringRegistration) {
        L.d("Starting ProcessScanActivity");
        Intent intent = new Intent(this, ProcessScanActivity.class);
        intent.putExtra(ProcessScanActivity.URL, caughtUrlDuringRegistration);
        intent.putExtra(ProcessScanActivity.SCAN_RESULT, false);
        startActivity(intent);
    }

    private void startScanner() {
        L.d("Starting ScanTabActivity");
        Intent scanIntent = new Intent(this, ScanTabActivity.class);
        scanIntent.setAction(ScanTabActivity.START_SCANNER_INTENT_ACTION);
        startActivity(scanIntent);
    }

    private void startSendCannedMessage() {
        L.d("Starting SendCannedMessageActivity");
        startActivity(new Intent(this, SendCannedMessageActivity.class));
    }

    private void startSendMessageWizard() {
        L.d("Starting SendMessageWizardActivity");
        startActivity(new Intent(this, SendMessageWizardActivity.class));
    }

}
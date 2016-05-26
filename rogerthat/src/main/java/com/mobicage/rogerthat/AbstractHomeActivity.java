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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.util.SparseIntArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.mobicage.api.services.Rpc;
import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.plugins.friends.ActionScreenActivity;
import com.mobicage.rogerthat.plugins.friends.Friend;
import com.mobicage.rogerthat.plugins.friends.FriendSearchActivity;
import com.mobicage.rogerthat.plugins.friends.FriendStore;
import com.mobicage.rogerthat.plugins.friends.FriendsPlugin;
import com.mobicage.rogerthat.plugins.friends.ServiceActionMenuActivity;
import com.mobicage.rogerthat.plugins.friends.ServiceMenu;
import com.mobicage.rogerthat.plugins.friends.ServiceMenuItem;
import com.mobicage.rogerthat.plugins.messaging.BrandingFailureException;
import com.mobicage.rogerthat.plugins.messaging.Message;
import com.mobicage.rogerthat.plugins.messaging.MessageStore;
import com.mobicage.rogerthat.plugins.messaging.MessagingActivity;
import com.mobicage.rogerthat.plugins.messaging.MessagingPlugin;
import com.mobicage.rogerthat.plugins.messaging.ServiceMessageDetailActivity;
import com.mobicage.rogerthat.plugins.messaging.mfr.EmptyStaticFlowException;
import com.mobicage.rogerthat.plugins.messaging.mfr.JsMfr;
import com.mobicage.rogerthat.plugins.messaging.mfr.MessageFlowRun;
import com.mobicage.rogerthat.plugins.scan.ProcessScanActivity;
import com.mobicage.rogerthat.plugins.scan.ProfileActivity;
import com.mobicage.rogerthat.plugins.scan.ScanTabActivity;
import com.mobicage.rogerthat.util.RegexPatterns;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeBroadcastReceiver;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.system.SafeViewOnClickListener;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.ui.ImageHelper;
import com.mobicage.rogerthat.util.ui.UIUtils;
import com.mobicage.rpc.ResponseHandler;
import com.mobicage.rpc.config.AppConstants;
import com.mobicage.rpc.config.CloudConstants;
import com.mobicage.to.friends.ServiceMenuItemTO;
import com.mobicage.to.service.PressMenuIconRequestTO;
import com.mobicage.to.service.PressMenuIconResponseTO;

import fr.castorflex.android.verticalviewpager.VerticalViewPager;

/**
 * Super class for HomeActivity | Only used in City & Enterprise Apps
 */
public abstract class AbstractHomeActivity extends ServiceBoundActivity {

    protected static class ItemDef {
        private int iconId;
        private int labelId;
        private int labelTextId;
        private SafeViewOnClickListener clickListener;

        public ItemDef(int iconId, int labelId, int labelTextId, SafeViewOnClickListener clickListener) {
            super();
            this.iconId = iconId;
            this.labelId = labelId;
            this.labelTextId = labelTextId;
            this.clickListener = clickListener;
        }
    }

    protected static final String INTENT_PROCESSED = "processed";

    public final static String INTENT_KEY_LAUNCHINFO = "launchInfo";
    public final static String INTENT_VALUE_SHOW_MESSAGES = "showMessages";
    public final static String INTENT_VALUE_SHOW_NEW_MESSAGES = "showNewMessages";
    public final static String INTENT_VALUE_SHOW_UPDATED_MESSAGES = "showUpdatedMessages";
    public final static String INTENT_VALUE_SHOW_FRIENDS = "showFriends";
    public final static String INTENT_VALUE_SHOW_SCANTAB = "showScanTab";
    public final static String INTENT_KEY_MESSAGE = "messageKey";

    protected Intent mNotYetProcessedIntent = null;
    protected FriendsPlugin mFriendsPlugin;
    protected FriendStore mFriendStore;
    protected MessagingPlugin mMessagingPlugin;
    protected MessageStore mMessageStore;

    protected TextView mBadgeMessages;

    protected String mContextMatch = "";

    protected SparseIntArray mServiceCountByOrganizationType = new SparseIntArray();

    private VerticalViewPager mPager;

    abstract ItemDef[] getItemDefs();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        T.UI();
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = getLayoutInflater();

        setContentView(AppConstants.HOME_ACTIVITY_LAYOUT);

        if (AppConstants.HOME_ACTIVITY_LAYOUT == R.layout.homescreen_3x3_with_qr_code ||
                AppConstants.HOME_ACTIVITY_LAYOUT == R.layout.homescreen_3x3) {
            FrameLayout mainLayer = (FrameLayout) findViewById(R.id.master);
            inflater.inflate(R.layout.homescreen_3x3_watermark, mainLayer, true);
            inflater.inflate(R.layout.homescreen_3x3_holder, mainLayer, true);
        }

        if (AppConstants.HOME_ACTIVITY_LAYOUT == R.layout.homescreen_3x3_with_qr_code) {
            if (AppConstants.SHOW_HOMESCREEN_FOOTER) {
                ((TextView) findViewById(R.id.loyalty_text)).setTextColor(getResources().getColor(R.color
                        .mc_homescreen_background));
            }

            mPager = (VerticalViewPager) findViewById(R.id.view_pager);
            mPager.setAdapter(new PagerAdapter() {
                @Override
                public Object instantiateItem(ViewGroup container, int position) {
                    return mPager.getChildAt(position);
                }

                @Override
                public int getCount() {
                    return 2;
                }

                @Override
                public boolean isViewFromObject(View arg0, Object arg1) {
                    return arg0 == arg1;
                }
            });

            findViewById(R.id.scan_btn).setOnClickListener(new SafeViewOnClickListener() {
                @Override
                public void safeOnClick(View v) {
                    goToScanActivity();
                }
            });
        }

        if (!AppConstants.SHOW_HOMESCREEN_FOOTER) {
            for (int id : new int[]{R.id.homescreen_footer, R.id.invisible_homescreen_footer}) {
                findViewById(id).setVisibility(View.GONE);
            }

            final View secondSpacerView = findViewById(R.id.second_spacerview);
            if (secondSpacerView != null) {
                secondSpacerView.setVisibility(View.VISIBLE);
            }
        }

        if (AppConstants.FULL_WIDTH_HEADERS) {
            findViewById(R.id.homescreen_header_container).setVisibility(View.GONE);
            findViewById(R.id.homescreen_header_spacer_view).setVisibility(View.GONE);
            findViewById(R.id.full_width_homescreen_header).setVisibility(View.VISIBLE);
        }
    }

    private void loadServiceCount() {
        mServiceCountByOrganizationType = mFriendStore.countServicesGroupedByOrganizationType();
    }

    private void initUI() {
        mBadgeMessages = (TextView) findViewById(R.id.badge_0x0);

        for (ItemDef i : getItemDefs()) {
            final ImageView icon = (ImageView) findViewById(i.iconId);
            icon.setOnClickListener(i.clickListener);
            final TextView label = (TextView) findViewById(i.labelId);
            label.setOnClickListener(i.clickListener);
            label.setText(i.labelTextId);
        }

        loadQR();
    }

    private void loadQR() {
        final ImageView imageView = (ImageView) findViewById(R.id.qrcode);
        if (imageView != null) {
            final TextView headerTextView = (TextView) findViewById(R.id.loyalty_text);
            headerTextView.setText(getString(AppConstants.HOMESCREEN_QRCODE_HEADER, getString(R.string.app_name)));
            final Bitmap qrBitmap = mService.getIdentityStore().getIdentity().getQRBitmap();
            if (qrBitmap != null) {
                imageView.setImageBitmap(ImageHelper.getRoundedCornerBitmap(qrBitmap,
                    UIUtils.convertDipToPixels(this, 5)));
            }
        }
    }

    public static void startWithLaunchInfo(final Activity activity, final String launchInfo) {
        final Intent intent = new Intent(activity, HomeActivity.class);
        intent.putExtra(INTENT_KEY_LAUNCHINFO, launchInfo);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        activity.startActivity(intent);
    }

    private void handleBadgeMessages() {
        if (mBadgeMessages == null) {
            return;
        }
        long unprocessed = mMessageStore.getDirtyThreadsCount();
        if (unprocessed > 0) {
            if (unprocessed > 9)
                mBadgeMessages.setText("9+");
            else
                mBadgeMessages.setText(Long.toString(unprocessed));
            mBadgeMessages.setVisibility(View.VISIBLE);
        } else {
            mBadgeMessages.setVisibility(View.GONE);
        }
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

    protected void goToMessagingActivity() {
        Intent i = new Intent(this, MessagingActivity.class);
        startActivity(i);
    }

    protected void goToUserFriendsActivity() {
        Intent launchIntent = new Intent(this, UserFriendsActivity.class);
        startActivity(launchIntent);
    }

    protected void goToServicesActivity(int organizationType, boolean collapse) {
        if (collapse) {
            int serviceCount = 0;
            if (organizationType == FriendStore.SERVICE_ORGANIZATION_TYPE_UNSPECIFIED) {
                for (int i = 0; i < mServiceCountByOrganizationType.size(); i++) {
                    serviceCount += mServiceCountByOrganizationType.valueAt(i);
                }
            } else {
                serviceCount = mServiceCountByOrganizationType.get(organizationType); // 0 if type not in mapping
            }

            if (serviceCount == 1) {
                Cursor cursor = mFriendStore.getServiceFriendListCursor(organizationType);
                if (cursor.moveToFirst()) {
                    String serviceEmail = cursor.getString(1);
                    Intent intent = new Intent(this, ServiceActionMenuActivity.class);
                    intent.putExtra(ServiceActionMenuActivity.SERVICE_EMAIL, serviceEmail);
                    intent.putExtra(ServiceActionMenuActivity.MENU_PAGE, 0);
                    startActivity(intent);
                    return;
                }
            }
        }

        final Intent launchIntent = new Intent(this, ServiceFriendsActivity.class);
        launchIntent.putExtra(ServiceFriendsActivity.ORGANIZATION_TYPE, organizationType);
        startActivity(launchIntent);
    }

    protected void goToScanActivity() {
        Intent launchIntent = new Intent(this, ScanTabActivity.class);
        startActivity(launchIntent);
    }

    protected void goToMoreActivity() {
        final Intent launchIntent = new Intent(this, MoreActivity.class);
        startActivity(launchIntent);
    }

    protected void goToProfileActivity() {
        final Intent launchIntent = new Intent(this, ProfileActivity.class);
        startActivity(launchIntent);
    }

    protected void goToFriendSearchActivity() {
        final Intent serviceSearch = new Intent(this, FriendSearchActivity.class);
        startActivity(serviceSearch);
    }

    protected void simulateMenuItemPress(String serviceEmail, long[] serviceCoords) {
        Friend service = mFriendStore.getExistingFriend(serviceEmail);
        if (service != null) {
            mFriendStore.addMenuDetails(service);

            ServiceMenuItemTO smi = null;
            for (ServiceMenuItem i : ((ServiceMenu) service.actionMenu).itemList) {
                if ((int) i.coords[0] == serviceCoords[0] && (int) i.coords[1] == serviceCoords[1]
                    && (int) i.coords[2] == serviceCoords[2]) {
                    smi = i;
                    break;
                }
            }
            if (smi != null) {
                if (smi.requiresWifi && !checkConnectivityIsWifi()) {
                    UIUtils.showLongToast(mService, getString(R.string.failed_to_show_action_screen_no_wifi));
                    return;
                }

                mContextMatch = "MENU_" + UUID.randomUUID().toString();
                PressMenuIconRequestTO request = new PressMenuIconRequestTO();
                request.coords = serviceCoords;
                request.service = service.email;
                request.context = mContextMatch;
                request.generation = service.generation;
                request.hashed_tag = smi.hashedTag;
                request.timestamp = System.currentTimeMillis() / 1000;
                try {
                    if (smi.staticFlowHash == null) {
                        Rpc.pressMenuItem(new ResponseHandler<PressMenuIconResponseTO>(), request);

                        if (smi.screenBranding != null) {
                            boolean brandingAvailable = false;
                            try {
                                brandingAvailable = mMessagingPlugin.getBrandingMgr().isBrandingAvailable(
                                    smi.screenBranding);
                            } catch (BrandingFailureException e) {
                                // ignore
                            }
                            if (!brandingAvailable) {
                                mMessagingPlugin.getBrandingMgr().queue(service);
                            }

                            Intent intent = new Intent(this, ActionScreenActivity.class);
                            intent.putExtra(ActionScreenActivity.BRANDING_KEY, smi.screenBranding);
                            intent.putExtra(ActionScreenActivity.SERVICE_EMAIL, service.email);
                            intent.putExtra(ActionScreenActivity.ITEM_TAG_HASH, smi.hashedTag);
                            intent.putExtra(ActionScreenActivity.ITEM_LABEL, smi.label);
                            intent.putExtra(ActionScreenActivity.ITEM_COORDS, smi.coords);
                            intent.putExtra(ActionScreenActivity.CONTEXT_MATCH, mContextMatch);
                            intent.putExtra(ActionScreenActivity.RUN_IN_BACKGROUND, smi.runInBackground);
                            startActivity(intent);
                        } else {
                            if (checkConnectivity())
                                showTransmitting(null);
                            else
                                showActionScheduledDialog();
                        }
                    } else {
                        showTransmitting(null);
                        request.static_flow_hash = smi.staticFlowHash;
                        Map<String, Object> userInput = new HashMap<String, Object>();
                        userInput.put("request", request.toJSONMap());
                        userInput.put("func", "com.mobicage.api.services.pressMenuItem");

                        MessageFlowRun mfr = new MessageFlowRun();
                        mfr.staticFlowHash = smi.staticFlowHash;
                        try {
                            JsMfr.executeMfr(mfr, userInput, mService, true);
                        } catch (EmptyStaticFlowException ex) {
                            completeTransmit(null);
                            AlertDialog.Builder builder = new AlertDialog.Builder(this);
                            builder.setMessage(ex.getMessage());
                            builder.setPositiveButton(R.string.rogerthat, null);
                            AlertDialog dialog = builder.create();
                            dialog.show();
                            return;
                        }
                    }
                } catch (Exception e) {
                    L.bug(e);
                }

            } else {
                L.d("SMI NOT FOUND");
            }
        } else {
            L.d("NOT READY YET");
        }
    }

    protected void goToActivity(String activityName, boolean collapse) {
        if ("messages".equals(activityName)) {
            goToMessagingActivity();
        } else if ("scan".equals(activityName)) {
            goToScanActivity();
        } else if ("services".equals(activityName)) {
            goToServicesActivity(FriendStore.SERVICE_ORGANIZATION_TYPE_UNSPECIFIED, collapse);
        } else if ("friends".equals(activityName)) {
            goToUserFriendsActivity();
        } else if ("directory".equals(activityName)) {
            goToFriendSearchActivity();
        } else if ("profile".equals(activityName)) {
            goToProfileActivity();
        } else if ("more".equals(activityName)) {
            goToMoreActivity();
        } else if ("community_services".equals(activityName)) {
            goToServicesActivity(FriendStore.SERVICE_ORGANIZATION_TYPE_CITY, collapse);
        } else if ("merchants".equals(activityName)) {
            goToServicesActivity(FriendStore.SERVICE_ORGANIZATION_TYPE_PROFIT, collapse);
        } else if ("associations".equals(activityName)) {
            goToServicesActivity(FriendStore.SERVICE_ORGANIZATION_TYPE_NON_PROFIT, collapse);
        } else if ("emergency_services".equals(activityName)) {
            goToServicesActivity(FriendStore.SERVICE_ORGANIZATION_TYPE_EMERGENCY, collapse);
        } else {
            L.bug("unknown goToActivity: " + activityName);
        }
    }

    private void processIntent(final Intent intent) {
        final String url = intent.getDataString();
        if (intent.getBooleanExtra(INTENT_PROCESSED, false))
            return;
        if (url != null) {
            goToMessagingActivity();
            processUrl(url);
        } else if (intent.hasExtra(INTENT_KEY_LAUNCHINFO)) {
            String value = intent.getStringExtra(INTENT_KEY_LAUNCHINFO);
            if (INTENT_VALUE_SHOW_FRIENDS.equals(value)) {
                // goToUserFriendsActivity();

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
                goToScanActivity();
            } else {
                L.bug("Unexpected (key, value) for HomeActivity intent: (" + INTENT_KEY_LAUNCHINFO + ", " + value + ")");
            }
            if (mMessagingPlugin != null) {
                mMessagingPlugin.updateMessagesNotification(false, false, false);
                handleBadgeMessages();
            }
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

        if (RegexPatterns.FRIEND_INVITE_URL.matcher(url).matches()
            || RegexPatterns.SERVICE_INTERACT_URL.matcher(url).matches()) {
            final Intent launchIntent = new Intent(this, ProcessScanActivity.class);
            launchIntent.putExtra(ProcessScanActivity.URL, url);
            launchIntent.putExtra(ProcessScanActivity.SCAN_RESULT, false);
            startActivity(launchIntent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mService != null)
            handleBadgeMessages();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        L.d(this.getClass().getName() + ".onDestroy()");
    }

    private final BroadcastReceiver mBroadcastReceiver = new SafeBroadcastReceiver() {
        @Override
        public String[] onSafeReceive(Context context, final Intent intent) {
            T.UI();
            if (FriendsPlugin.FRIEND_ADDED_INTENT.equals(intent.getAction())
                || FriendsPlugin.FRIEND_REMOVED_INTENT.equals(intent.getAction())
                || FriendsPlugin.FRIENDS_LIST_REFRESHED.equals(intent.getAction())) {
                loadServiceCount();
                return null;
            }

            handleBadgeMessages();

            if (MessagingPlugin.NEW_MESSAGE_RECEIVED_INTENT.equals(intent.getAction())) {

                if (mContextMatch.equals(intent.getStringExtra("context")) && isTransmitting()) {
                    mContextMatch = "";
                    completeTransmit(new SafeRunnable() {
                        @Override
                        protected void safeRun() throws Exception {
                            final Intent i = new Intent(AbstractHomeActivity.this, ServiceMessageDetailActivity.class);
                            i.putExtra("message", intent.getStringExtra("message"));
                            startActivity(i);
                        }
                    });
                }
            }

            if (IdentityStore.IDENTITY_CHANGED_INTENT.equals(intent.getAction())) {
                loadQR();
            }

            return new String[] { intent.getAction() };
        }
    };

    @Override
    protected void onServiceBound() {
        initUI();
        mService.addHighPriorityIntent(FriendsPlugin.SERVICE_ACTION_INFO_RECEIVED_INTENT);

        mMessagingPlugin = mService.getPlugin(MessagingPlugin.class);
        mMessageStore = mMessagingPlugin.getStore();

        mFriendsPlugin = mService.getPlugin(FriendsPlugin.class);
        mFriendStore = mFriendsPlugin.getStore();

        if (mNotYetProcessedIntent != null) {
            processIntent(mNotYetProcessedIntent);
            mNotYetProcessedIntent = null;
        } else {
            Intent intent = getIntent();
            processIntent(intent);
        }

        handleBadgeMessages();

        IntentFilter intentFilter = new IntentFilter(MessagingPlugin.NEW_MESSAGE_RECEIVED_INTENT);
        intentFilter.addAction(MessagingPlugin.MESSAGE_MEMBER_STATUS_UPDATE_RECEIVED_INTENT);
        intentFilter.addAction(MessagingPlugin.MESSAGE_PROCESSED_INTENT);
        intentFilter.addAction(MessagingPlugin.THREAD_DELETED_INTENT);
        intentFilter.addAction(FriendsPlugin.FRIEND_ADDED_INTENT);
        intentFilter.addAction(FriendsPlugin.FRIEND_REMOVED_INTENT);
        intentFilter.addAction(FriendsPlugin.FRIENDS_LIST_REFRESHED);
        if (CloudConstants.isCityApp())
            intentFilter.addAction(IdentityStore.IDENTITY_CHANGED_INTENT);
        registerReceiver(mBroadcastReceiver, intentFilter);

        loadServiceCount();
    }

    @Override
    protected void onServiceUnbound() {
        mMessagingPlugin.updateMessagesNotification(false, false, false);
        handleBadgeMessages();
        unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    public MainService getMainService() {
        return mService;
    }

    private boolean resetPager() {
        if (mPager != null && mPager.getCurrentItem() == 1) {
            mPager.setCurrentItem(0, true);
            return true;
        }
        return false;
    }

    @Override
    public boolean onKeyDown(final int keyCode, final KeyEvent event) {
        return keyCode == KeyEvent.KEYCODE_BACK && resetPager() || super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onPause() {
        resetPager();
        super.onPause();
    }

}

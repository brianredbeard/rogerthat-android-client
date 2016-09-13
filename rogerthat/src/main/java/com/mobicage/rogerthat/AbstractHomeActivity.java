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

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.plugins.friends.Friend;
import com.mobicage.rogerthat.plugins.friends.FriendSearchActivity;
import com.mobicage.rogerthat.plugins.friends.FriendStore;
import com.mobicage.rogerthat.plugins.friends.FriendsPlugin;
import com.mobicage.rogerthat.plugins.friends.MenuItemPresser;
import com.mobicage.rogerthat.plugins.friends.MenuItemPressingActivity;
import com.mobicage.rogerthat.plugins.friends.ServiceActionMenuActivity;
import com.mobicage.rogerthat.plugins.friends.ServiceMenu;
import com.mobicage.rogerthat.plugins.friends.ServiceMenuItem;
import com.mobicage.rogerthat.plugins.messaging.Message;
import com.mobicage.rogerthat.plugins.messaging.MessageStore;
import com.mobicage.rogerthat.plugins.messaging.MessagingActivity;
import com.mobicage.rogerthat.plugins.messaging.MessagingPlugin;
import com.mobicage.rogerthat.plugins.scan.ProcessScanActivity;
import com.mobicage.rogerthat.plugins.scan.ProfileActivity;
import com.mobicage.rogerthat.plugins.scan.ScanTabActivity;
import com.mobicage.rogerthat.util.ActivityUtils;
import com.mobicage.rogerthat.util.RegexPatterns;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeBroadcastReceiver;
import com.mobicage.rogerthat.util.system.SafeViewOnClickListener;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.ui.ImageHelper;
import com.mobicage.rogerthat.util.ui.UIUtils;
import com.mobicage.rpc.config.AppConstants;
import com.mobicage.rpc.config.CloudConstants;
import com.mobicage.to.friends.ServiceMenuItemTO;

import fr.castorflex.android.verticalviewpager.VerticalViewPager;

/**
 * Super class for HomeActivity | Only used in City & Enterprise Apps
 */
public abstract class AbstractHomeActivity extends ServiceBoundActivity implements MenuItemPressingActivity {

    public static class ItemDef {
        public int iconId;
        public int labelId;
        public int labelTextId;
        public SafeViewOnClickListener clickListener;

        public ItemDef(int iconId, int labelId, int labelTextId, SafeViewOnClickListener clickListener) {
            super();
            this.iconId = iconId;
            this.labelId = labelId;
            this.labelTextId = labelTextId;
            this.clickListener = clickListener;
        }
    }

    public static final String INTENT_PROCESSED = "processed";

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

    private MenuItemPresser mMenuItemPresser;

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
                    ActivityUtils.goToScanActivity(AbstractHomeActivity.this, false);
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

        ActivityUtils.goToServicesActivity(this, organizationType, false);
    }

    protected void simulateMenuItemPress(String serviceEmail, long[] serviceCoords) {
        Friend service = mFriendStore.getExistingFriend(serviceEmail);
        if (service == null) {
            L.d("NOT READY YET");
            return;
        }

        mFriendStore.addMenuDetails(service);

        ServiceMenuItemTO smi = null;
        for (ServiceMenuItem i : ((ServiceMenu) service.actionMenu).itemList) {
            if ((int) i.coords[0] == serviceCoords[0] && (int) i.coords[1] == serviceCoords[1]
                    && (int) i.coords[2] == serviceCoords[2]) {
                smi = i;
                break;
            }
        }

        if (smi == null) {
            L.d("SMI NOT FOUND");
            return;
        }

        if (mMenuItemPresser == null) {
            mMenuItemPresser = new MenuItemPresser(this, serviceEmail);
        }
        mMenuItemPresser.itemPressed(smi, service.generation, null);
    }

    protected void goToActivity(String activityName, boolean collapse) {
        if ("messages".equals(activityName)) {
            ActivityUtils.goToMessagingActivity(this, false);
        } else if ("scan".equals(activityName)) {
            ActivityUtils.goToScanActivity(this, false);
        } else if ("services".equals(activityName)) {
            goToServicesActivity(FriendStore.SERVICE_ORGANIZATION_TYPE_UNSPECIFIED, collapse);
        } else if ("friends".equals(activityName)) {
            ActivityUtils.goToUserFriendsActivity(this, false);
        } else if ("directory".equals(activityName)) {
            ActivityUtils.goToFriendSearchActivity(this, false);
        } else if ("profile".equals(activityName)) {
            ActivityUtils.goToProfileActivity(this, false);
        } else if ("more".equals(activityName)) {
            ActivityUtils.goToMoreActivity(this, false);
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
            ActivityUtils.goToMessagingActivity(this, false);
            processUrl(url);
        } else if (intent.hasExtra(INTENT_KEY_LAUNCHINFO)) {
            String value = intent.getStringExtra(INTENT_KEY_LAUNCHINFO);
            if (INTENT_VALUE_SHOW_FRIENDS.equals(value)) {
                // goToUserFriendsActivity();

            } else if (INTENT_VALUE_SHOW_MESSAGES.equals(value)) {
                ActivityUtils.goToMessagingActivity(this, false);

            } else if (INTENT_VALUE_SHOW_NEW_MESSAGES.equals(value)) {
                if (intent.hasExtra(INTENT_KEY_MESSAGE)) {
                    String messageKey = intent.getStringExtra(INTENT_KEY_MESSAGE);
                    goToMessageDetail(messageKey);
                } else {
                    ActivityUtils.goToMessagingActivity(this, false);
                }

            } else if (INTENT_VALUE_SHOW_UPDATED_MESSAGES.equals(value)) {
                if (intent.hasExtra(INTENT_KEY_MESSAGE)) {
                    String messageKey = intent.getStringExtra(INTENT_KEY_MESSAGE);
                    goToMessageDetail(messageKey);
                } else {
                    ActivityUtils.goToMessagingActivity(this, false);
                }

            } else if (INTENT_VALUE_SHOW_SCANTAB.equals(value)) {
                ActivityUtils.goToScanActivity(this, false);
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

        if (mMenuItemPresser != null) {
            mMenuItemPresser.stop();
            mMenuItemPresser = null;
        }
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

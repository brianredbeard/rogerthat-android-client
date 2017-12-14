/*
 * Copyright 2017 GIG Technology NV
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
 * @@license_version:1.3@@
 */
package com.mobicage.rogerthat.plugins.friends;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;
import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.FriendDetailActivity;
import com.mobicage.rogerthat.HomeActivity;
import com.mobicage.rogerthat.ServiceBoundActivity;
import com.mobicage.rogerthat.ServiceDetailActivity;
import com.mobicage.rogerthat.plugins.messaging.BrandingFailureException;
import com.mobicage.rogerthat.plugins.messaging.BrandingMgr;
import com.mobicage.rogerthat.plugins.messaging.BrandingMgr.BrandingResult;
import com.mobicage.rogerthat.plugins.messaging.BrandingMgr.ColorScheme;
import com.mobicage.rogerthat.plugins.messaging.MessagingFilterActivity;
import com.mobicage.rogerthat.plugins.messaging.MessagingPlugin;
import com.mobicage.rogerthat.plugins.scan.ProcessScanActivity;
import com.mobicage.rogerthat.plugins.scan.ScanTabActivity;
import com.mobicage.rogerthat.util.TextUtils;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeBroadcastReceiver;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.system.SafeViewOnClickListener;
import com.mobicage.rogerthat.util.system.SystemUtils;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.ui.ServiceHeader;
import com.mobicage.rogerthat.util.ui.Slider;
import com.mobicage.rogerthat.util.ui.UIUtils;
import com.mobicage.rogerthat.util.ui.WrapContentViewPager;
import com.mobicage.rpc.config.CloudConstants;
import com.mobicage.to.friends.ServiceMenuItemTO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServiceActionMenuActivity extends ServiceBoundActivity {

    public static final int MY_PERMISSIONS_REQUEST_CAMERA = 1;
    public static final String SERVICE_EMAIL = "email";
    public static final String MENU_PAGE = "page";
    public static final String SHOW_ERROR_POPUP = "com.mobicage.rogerthat.plugins.friends.ServiceActionMenuActivity.SHOW_ERROR_POPUP";

    private class Cell {
        ImageView icon;
        TextView label;
    }

    private boolean mUseDarkScheme = false;
    private Integer mDefaultMenuItemColor = null;
    private Integer mBrandingBackgroundColor = null;

    private WebView brandingWebview;
    private LinearLayout mHeaderContainer;
    private WrapContentViewPager mPager;
    private MyPagerAdapter mPagerAdapter;
    private LinearLayout pages;
    private String email;
    private String menuBrandingHash;
    private int page;
    private RelativeLayout activity;
    private int darkSchemeTextColor;
    private TextView badge;
    private MenuItemPresser mMenuItemPresser;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        goToMessagingActivityIfNeeded(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        L.i("ServiceActionMenuActivity.onCreate");
        setContentView(R.layout.service_action_menu);
        Intent intent = getIntent();
        email = intent.getStringExtra(SERVICE_EMAIL);
        L.d("Service: " + email);
        setActivityName(email);
        page = intent.getIntExtra(MENU_PAGE, 0);
        activity = (RelativeLayout) findViewById(R.id.activity);
        mHeaderContainer = (LinearLayout) findViewById(R.id.header_container);
        brandingWebview = (WebView) findViewById(R.id.branding);
        brandingWebview.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("tel:")) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                    return true;
                }
                return false;
            }

        });

        darkSchemeTextColor = ContextCompat.getColor(this, android.R.color.primary_text_dark);

        mPager = (WrapContentViewPager) findViewById(R.id.pager);
        mPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                mPager.requestLayout();
                page = position;
                handlePage();
            }
        });

        //A little space between pages
        //Disable clipping of children so non-selected pages are visible
        mPager.setClipChildren(false);

        //Child clipping doesn't work with hardware acceleration in Android 3.x/4.x
        //You need to set this value here if using hardware acceleration in an
        // application targeted at these releases.
        mPager.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        pages = (LinearLayout) findViewById(R.id.pages);

        if (intent.getBooleanExtra(SHOW_ERROR_POPUP, false))
            UIUtils.showErrorPleaseRetryDialog(this);

        goToMessagingActivityIfNeeded(intent);
    }

    private void goToMessagingActivityIfNeeded(Intent intent) {
        if (intent.hasExtra(HomeActivity.INTENT_KEY_LAUNCHINFO)) {
            String value = intent.getStringExtra(HomeActivity.INTENT_KEY_LAUNCHINFO);
            if (HomeActivity.INTENT_VALUE_SHOW_MESSAGES.equals(value)
                || HomeActivity.INTENT_VALUE_SHOW_NEW_MESSAGES.equals(value)
                || HomeActivity.INTENT_VALUE_SHOW_UPDATED_MESSAGES.equals(value)) {
                goToMessagingActivity();
            }
        }
    }

    private void goToMessagingActivity() {
        final Intent viewMessages = new Intent(ServiceActionMenuActivity.this, MessagingFilterActivity.class);
        viewMessages.putExtra(MessagingPlugin.MEMBER_FILTER, email);
        startActivity(viewMessages);
    }

    @Override
    protected void onServiceBound() {
        L.d("ServiceActionMenuActivity onServiceBound()");
        final FriendsPlugin friendsPlugin = mService.getPlugin(FriendsPlugin.class);
        final ServiceMenu menu = friendsPlugin.getStore().getMenu(email, null);

        populateScreen(menu);
        IntentFilter filter = new IntentFilter(FriendsPlugin.FRIEND_UPDATE_INTENT);
        filter.addAction(FriendsPlugin.FRIEND_REMOVED_INTENT);
        filter.addAction(FriendsPlugin.FRIEND_MARKED_FOR_REMOVAL_INTENT);
        filter.addAction(MessagingPlugin.NEW_MESSAGE_RECEIVED_INTENT);
        filter.addAction(MessagingPlugin.MESSAGE_PROCESSED_INTENT);
        filter.addAction(MessagingPlugin.MESSAGE_LOCKED_INTENT);
        filter.addAction(MessagingPlugin.MESSAGE_DIRTY_CLEANED_INTENT);
        filter.addAction(MessagingPlugin.THREAD_DELETED_INTENT);
        filter.addAction(MessagingPlugin.THREAD_RECOVERED_INTENT);
        filter.addAction(BrandingMgr.SERVICE_BRANDING_AVAILABLE_INTENT);
        filter.addAction(BrandingMgr.GENERIC_BRANDING_AVAILABLE_INTENT);
        registerReceiver(mBroadcastReceiver, filter);
    }

    private void setBrandingHeight(int h) {
        L.d("Setting brandingWebview height: " + h);
        brandingWebview.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, h));
        brandingWebview.setVisibility(View.VISIBLE);
    }

    private void populateScreen() {
        final FriendsPlugin friendsPlugin = mService.getPlugin(FriendsPlugin.class);
        populateScreen(friendsPlugin.getStore().getMenu(email, null));
    }

    private void populateScreen(final ServiceMenu menu) {
        setTitle(menu.name);
        menuBrandingHash = menu.branding;
        final MessagingPlugin messagingPlugin = mService.getPlugin(MessagingPlugin.class);
        final FriendsPlugin friendsPlugin = mService.getPlugin(FriendsPlugin.class);
        final FriendStore friendStore = friendsPlugin.getStore();

        if (menu.branding != null) {
            try {
                BrandingMgr brandingMgr = messagingPlugin.getBrandingMgr();
                if (brandingMgr.isBrandingAvailable(menu.branding)) {
                    final int displayWidth = UIUtils.getDisplayWidth(this);
                    BrandingResult br = brandingMgr.prepareBranding(menu.branding, null, false);
                    mBrandingBackgroundColor = br.color;
                    mDefaultMenuItemColor = br.menuItemColor;
                    if (br.displayType.equals(BrandingMgr.DisplayType.NATIVE)) {
                        ServiceHeader.setupNative(mService, br, mHeaderContainer);
                        brandingWebview.setVisibility(View.GONE);
                    } else {
                        setupWebView(br, displayWidth);
                    }

                    if (br.color != null) {
                        activity.setBackgroundColor(mBrandingBackgroundColor);
                    }
                    if (br.scheme == ColorScheme.DARK) {
                        mUseDarkScheme = true;
                    }

                    final ImageView watermarkView = (ImageView) findViewById(R.id.watermark);
                    if (br.watermark != null) {
                        BitmapDrawable watermark = new BitmapDrawable(getResources(),
                                BitmapFactory.decodeFile(br.watermark.getAbsolutePath()));
                        watermark.setGravity(Gravity.BOTTOM | Gravity.RIGHT);

                        watermarkView.setImageDrawable(watermark);
                        final LayoutParams layoutParams = watermarkView.getLayoutParams();
                        layoutParams.width = layoutParams.height = displayWidth;
                    } else {
                        watermarkView.setImageDrawable(null);
                    }

                } else {
                    Friend friend = friendStore.getExistingFriend(email);
                    friend.actionMenu = menu;
                    friend.actionMenu.items = menu.itemList.toArray(new ServiceMenuItemTO[] {});
                    brandingMgr.queue(friend);
                }
            } catch (BrandingFailureException e) {
                L.bug("Could not display service action menu with branding.", e);
            }
        }
        if (mDefaultMenuItemColor == null)
            mDefaultMenuItemColor = ContextCompat.getColor(this, R.color.mc_page_light);
        if (mBrandingBackgroundColor == null) {
            mBrandingBackgroundColor = ContextCompat.getColor(this, R.color.mc_page_light);
        }
        mPagerAdapter = new MyPagerAdapter();
        mPagerAdapter.updateServiceMenu(menu);

        mPager.setAdapter(mPagerAdapter);
        //Necessary or the pager will only have one extra page to show
        // make this at least however many pages you can see
        mPager.setOffscreenPageLimit(mPagerAdapter.getCount());

        handleBadge(friendStore);
        handlePage();

        if (page != mPager.getCurrentItem()) {
            mPager.setCurrentItem(page);
        }
    }

    private void setupWebView(BrandingResult brandingResult, int displayWidth) {
        WebSettings settings = brandingWebview.getSettings();
        settings.setJavaScriptEnabled(false);
        settings.setBlockNetworkImage(false);
        brandingWebview.setVerticalScrollBarEnabled(false);
        brandingWebview.setVisibility(View.VISIBLE);
        mHeaderContainer.setVisibility(View.GONE);

        final int calculatedHeight = BrandingMgr.calculateHeight(brandingResult, displayWidth);
        setBrandingHeight(calculatedHeight);

        brandingWebview.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                brandingWebview.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                int height = brandingWebview.getMeasuredHeight();
                if (calculatedHeight > 0 && height > calculatedHeight * 90 / 100) {
                    setBrandingHeight(height);
                } else {
                    mService.postDelayedOnUIHandler(new SafeRunnable() {
                        @Override
                        protected void safeRun() throws Exception {
                            setBrandingHeight(brandingWebview.getMeasuredHeight());
                        }
                    }, 1000);
                }
            }
        });

        brandingWebview.loadUrl("file://" + brandingResult.file.getAbsolutePath());
        if (brandingResult.color != null) {
            brandingWebview.setBackgroundColor(brandingResult.color);
        }
    }

    private final BroadcastReceiver mBroadcastReceiver = new SafeBroadcastReceiver() {

        @Override
        public String[] onSafeReceive(final Context context, final Intent intent) {
            T.UI();
            String action = intent.getAction();
            if (FriendsPlugin.FRIEND_UPDATE_INTENT.equals(action)) {
                if (email.equals(intent.getStringExtra("email"))) {
                    populateScreen();

                    return new String[] { FriendsPlugin.FRIEND_UPDATE_INTENT,
                        BrandingMgr.SERVICE_BRANDING_AVAILABLE_INTENT, BrandingMgr.GENERIC_BRANDING_AVAILABLE_INTENT };
                }
            } else if (BrandingMgr.SERVICE_BRANDING_AVAILABLE_INTENT.equals(action)
                || BrandingMgr.GENERIC_BRANDING_AVAILABLE_INTENT.equals(action)) {
                if (menuBrandingHash != null
                    && menuBrandingHash.equals(intent.getStringExtra(BrandingMgr.BRANDING_KEY))) {
                    populateScreen();

                    return new String[] { FriendsPlugin.FRIEND_UPDATE_INTENT,
                        BrandingMgr.SERVICE_BRANDING_AVAILABLE_INTENT, BrandingMgr.GENERIC_BRANDING_AVAILABLE_INTENT };
                }
            } else if ((FriendsPlugin.FRIEND_REMOVED_INTENT.equals(action) || FriendsPlugin.FRIEND_MARKED_FOR_REMOVAL_INTENT
                .equals(action))) {
                if (email.equals(intent.getStringExtra("email"))) {
                    if (!isFinishing()) {
                        finish();
                    }
                    return new String[] { FriendsPlugin.FRIEND_REMOVED_INTENT,
                        FriendsPlugin.FRIEND_MARKED_FOR_REMOVAL_INTENT };
                }
            } else if (MessagingPlugin.NEW_MESSAGE_RECEIVED_INTENT.equals(action)) {
                if (mService != null)
                    handleBadge(mService.getPlugin(FriendsPlugin.class).getStore());
            } else if (MessagingPlugin.MESSAGE_PROCESSED_INTENT.equals(action)
                || MessagingPlugin.MESSAGE_LOCKED_INTENT.equals(action)
                || MessagingPlugin.MESSAGE_DIRTY_CLEANED_INTENT.equals(action)
                || MessagingPlugin.THREAD_DELETED_INTENT.equals(action)
                || MessagingPlugin.THREAD_RECOVERED_INTENT.equals(action)) {
                if (mService != null)
                    handleBadge(mService.getPlugin(FriendsPlugin.class).getStore());

                return new String[] { MessagingPlugin.MESSAGE_PROCESSED_INTENT, MessagingPlugin.MESSAGE_LOCKED_INTENT,
                    MessagingPlugin.MESSAGE_DIRTY_CLEANED_INTENT, MessagingPlugin.THREAD_DELETED_INTENT,
                    MessagingPlugin.THREAD_RECOVERED_INTENT };
            }
            return null;
        }

    };

    private void addAboutHandler(final String aboutLabel) {
        final Cell cell = mPagerAdapter.createCell(0,0, 0);
        View p = (View) cell.icon.getParent();
        cell.label.setText(TextUtils.isEmptyOrWhitespace(aboutLabel) ? getString(R.string.about) : aboutLabel);
        if (mUseDarkScheme) {
            cell.label.setTextColor(darkSchemeTextColor);
            cell.label.setShadowLayer(2, 1, 1, Color.BLACK);
            final Drawable d = getResources().getDrawable(R.drawable.mc_smi_background_light);
            p.setBackground(d);
        }

        cell.icon.setImageDrawable(new IconicsDrawable(this, FontAwesome.Icon.faw_info).color(mBrandingBackgroundColor).sizeDp(24).paddingDp(5));
        UIUtils.setBackgroundColor(cell.icon, mDefaultMenuItemColor);

        final View.OnClickListener onClickListener = new SafeViewOnClickListener() {
            @Override
            public void safeOnClick(View v) {
                long currentTime = System.currentTimeMillis();
                if (getLastTimeClicked() != 0
                    && (currentTime < (getLastTimeClicked() + ServiceBoundActivity.DOUBLE_CLICK_TIMESPAN))) {
                    L.d("ignoring click on about");
                    return;
                }
                setLastTimeClicked(currentTime);

                final Intent friendDetails = new Intent(ServiceActionMenuActivity.this, ServiceDetailActivity.class);
                friendDetails.putExtra(FriendDetailActivity.EMAIL, email);
                startActivity(friendDetails);
            }
        };
        p.setOnClickListener(onClickListener);
        p.setVisibility(View.VISIBLE);
    }

    private void addHistoryHandler(final String messagesLabel) {
        final Cell cell = mPagerAdapter.createCell(1, 0, 0);
        View p = (View) cell.icon.getParent();
        cell.label.setText(TextUtils.isEmptyOrWhitespace(messagesLabel) ? getString(R.string.message_history)
            : messagesLabel);
        if (mUseDarkScheme) {
            cell.label.setTextColor(darkSchemeTextColor);
            cell.label.setShadowLayer(2, 1, 1, Color.BLACK);
            final Drawable d = getResources().getDrawable(R.drawable.mc_smi_background_light);
            p.setBackground(d);
        }

        cell.icon.setImageDrawable(new IconicsDrawable(this, FontAwesome.Icon.faw_envelope).color(mBrandingBackgroundColor).sizeDp(24).paddingDp(5));
        UIUtils.setBackgroundColor(cell.icon, mDefaultMenuItemColor);

        final View.OnClickListener onClickListener = new SafeViewOnClickListener() {
            @Override
            public void safeOnClick(View v) {
                long currentTime = System.currentTimeMillis();
                if (getLastTimeClicked() != 0
                    && (currentTime < (getLastTimeClicked() + ServiceBoundActivity.DOUBLE_CLICK_TIMESPAN))) {
                    L.d("ignoring click on history");
                    return;
                }
                setLastTimeClicked(currentTime);
                final Intent viewMessages = new Intent(ServiceActionMenuActivity.this, MessagingFilterActivity.class);
                viewMessages.putExtra(MessagingPlugin.MEMBER_FILTER, email);
                startActivity(viewMessages);
            }
        };
        p.setOnClickListener(onClickListener);
        p.setVisibility(View.VISIBLE);
    }

    private void handleBadge(final FriendStore friendStore) {
        if (badge == null) {
            return;
        }
        long unprocessed = friendStore.getUnprocessedMessagesForSender(email);
        if (unprocessed > 0) {
            if (unprocessed > 9)
                badge.setText("9+");
            else
                badge.setText("" + unprocessed);
            badge.setVisibility(View.VISIBLE);
        } else {
            badge.setVisibility(View.GONE);
        }
    }

    private void handlePage() {
        pages.removeAllViews();
        pages.setVisibility(View.GONE);

        if (mPagerAdapter.getCount() > 1) {
            for (int i = 0; i < mPagerAdapter.getCount(); i++) {
                ImageView circle = (ImageView) getLayoutInflater().inflate(R.layout.page, pages, false);
                if (page == i) {
                    if (mUseDarkScheme) {
                        circle.setImageResource(R.drawable.current_page_dark);
                    } else {
                        circle.setImageResource(R.drawable.current_page_light);
                    }
                } else {
                    if (mUseDarkScheme) {
                        circle.setImageResource(R.drawable.other_page_dark);
                    } else {
                        circle.setImageResource(R.drawable.other_page_light);
                    }
                }
                pages.addView(circle);
            }
            pages.setVisibility(View.VISIBLE);
        }
    }

    private void addCallHandler(final ServiceMenu menu, final String callLabel) {
        if (menu.phoneNumber != null) {
            final Cell cell = mPagerAdapter.createCell(2,0,0);
            View p = (View) cell.icon.getParent();
            cell.label.setText(TextUtils.isEmptyOrWhitespace(callLabel) ? getString(R.string.call_service) : callLabel);
            if (mUseDarkScheme) {
                cell.label.setTextColor(darkSchemeTextColor);
                cell.label.setShadowLayer(2, 1, 1, Color.BLACK);
                final Drawable d = getResources().getDrawable(R.drawable.mc_smi_background_light);
                p.setBackground(d);
            }

            cell.icon.setImageDrawable(new IconicsDrawable(this, FontAwesome.Icon.faw_phone).color(mBrandingBackgroundColor).sizeDp(24).paddingDp(5));
            UIUtils.setBackgroundColor(cell.icon, mDefaultMenuItemColor);

            final View.OnClickListener onClickListener = new SafeViewOnClickListener() {
                @Override
                public void safeOnClick(View v) {
                    long currentTime = System.currentTimeMillis();
                    if (getLastTimeClicked() != 0
                        && (currentTime < (getLastTimeClicked() + ServiceBoundActivity.DOUBLE_CLICK_TIMESPAN))) {
                        L.d("ignoring click on call");
                        return;
                    }
                    setLastTimeClicked(currentTime);
                    Intent callIntent = SystemUtils.getActionDialIntent(ServiceActionMenuActivity.this);
                    if (callIntent == null) {
                        // No phone ability on device
                        String title = getString(R.string.call_service);
                        String message = TextUtils.isEmptyOrWhitespace(menu.callConfirmation) ?
                                getString(R.string.caption_call, menu.phoneNumber) : menu.callConfirmation;
                        UIUtils.showDialog(ServiceActionMenuActivity.this, title, message);
                    } else {
                        callIntent.setData(Uri.parse("tel://" + menu.phoneNumber));
                        startActivity(callIntent);
                    }
                }
            };
            p.setOnClickListener(onClickListener);
            p.setVisibility(View.VISIBLE);
        }
    }

    private void addShareHandler(final ServiceMenu menu, final String shareLabel) {
        if (menu.share) {
            final Cell cell;
            if (mPagerAdapter.hasCell(2,0,0)) {
                cell = mPagerAdapter.createCell(3,0,0);
            } else {
                cell = mPagerAdapter.createCell(2,0,0);
            }

            View p = (View) cell.icon.getParent();
            cell.label.setText(TextUtils.isEmptyOrWhitespace(shareLabel) ? getString(R.string.recommend_service) : shareLabel);
            if (mUseDarkScheme) {
                cell.label.setTextColor(darkSchemeTextColor);
                cell.label.setShadowLayer(2, 1, 1, Color.BLACK);
                final Drawable d = getResources().getDrawable(R.drawable.mc_smi_background_light);
                p.setBackground(d);
            }

            cell.icon.setImageDrawable(new IconicsDrawable(this, FontAwesome.Icon.faw_thumbs_up).color(mBrandingBackgroundColor).sizeDp(24).paddingDp(5));
            UIUtils.setBackgroundColor(cell.icon, mDefaultMenuItemColor);

            final View.OnClickListener onClickListener = new SafeViewOnClickListener() {
                @Override
                public void safeOnClick(View v) {
                    long currentTime = System.currentTimeMillis();
                    if (getLastTimeClicked() != 0
                        && (currentTime < (getLastTimeClicked() + ServiceBoundActivity.DOUBLE_CLICK_TIMESPAN))) {
                        L.d("ignoring click on share");
                        return;
                    }
                    setLastTimeClicked(currentTime);
                    Intent intent = new Intent(ServiceActionMenuActivity.this, RecommendServiceActivity.class);
                    intent.putExtra(RecommendServiceActivity.SERVICE_EMAIL, email);
                    intent.putExtra(RecommendServiceActivity.SHARE_IMAGE_URL, menu.shareImageUrl);
                    intent.putExtra(RecommendServiceActivity.SHARE_DESCRIPTION, menu.shareDescription);
                    intent.putExtra(RecommendServiceActivity.SHARE_CAPTION, menu.shareCaption);
                    intent.putExtra(RecommendServiceActivity.SHARE_LINK_URL, menu.shareLinkUrl);
                    startActivity(intent);
                }
            };
            p.setOnClickListener(onClickListener);
            p.setVisibility(View.VISIBLE);
        }
    }

    private void addScanHandler(final ServiceMenu menu, final String scanLabel) {
        final Cell cell;
        if (mPagerAdapter.hasCell(2,0,0)) {
            cell = mPagerAdapter.createCell(3,0,0);
        } else {
            cell = mPagerAdapter.createCell(2,0,0);
        }

        View p = (View) cell.icon.getParent();
        cell.label.setText(TextUtils.isEmptyOrWhitespace(scanLabel) ? getString(R.string.scan) : scanLabel);
        if (mUseDarkScheme) {
            cell.label.setTextColor(darkSchemeTextColor);
            cell.label.setShadowLayer(2, 1, 1, Color.BLACK);
            final Drawable d = getResources().getDrawable(R.drawable.mc_smi_background_light);
            p.setBackground(d);
        }

        cell.icon.setImageDrawable(new IconicsDrawable(this, FontAwesome.Icon.faw_qrcode).color(mBrandingBackgroundColor).sizeDp(24).paddingDp(5));
        UIUtils.setBackgroundColor(cell.icon, mDefaultMenuItemColor);

        int cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
            return;
        }

        final View.OnClickListener onClickListener = new SafeViewOnClickListener() {
            @Override
            public void safeOnClick(View v) {
                SystemUtils.showZXingActivity(ServiceActionMenuActivity.this, ScanTabActivity.MARKET_INSTALL_RESULT,
                    ScanTabActivity.ZXING_SCAN_RESULT);
            }
        };
        p.setOnClickListener(onClickListener);
        p.setVisibility(View.VISIBLE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        T.UI();
        if (requestCode == MY_PERMISSIONS_REQUEST_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                SystemUtils.showZXingActivity(ServiceActionMenuActivity.this, ScanTabActivity.MARKET_INSTALL_RESULT,
                        ScanTabActivity.ZXING_SCAN_RESULT);
            }
        }
    }

    @Override
    protected void onServiceUnbound() {
        unregisterReceiver(mBroadcastReceiver);
        if (mMenuItemPresser != null) {
            mMenuItemPresser.stop();
            mMenuItemPresser = null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == RESULT_OK) {
            if (requestCode == ScanTabActivity.MARKET_INSTALL_RESULT) {
                // No need to do anything
            } else if (requestCode == ScanTabActivity.ZXING_SCAN_RESULT) {
                final String rawScanResult = intent.getStringExtra("SCAN_RESULT");
                if (rawScanResult != null) {
                    final Intent launchIntent = new Intent(this, ProcessScanActivity.class);
                    launchIntent.putExtra(ProcessScanActivity.URL, rawScanResult);
                    launchIntent.putExtra(ProcessScanActivity.SCAN_RESULT, true);
                    startActivity(launchIntent);
                } else {
                    UIUtils.showLongToast(this, getString(R.string.scanner_failure));
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }


    private class MyPagerAdapter extends PagerAdapter {

        private List<String> mUsedCells = new ArrayList<>();
        private List<View> mPages = new ArrayList<>();
        private int mTableWidth = 0;

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View page = mPages.get(position);
            container.addView(page, 0);
            return page;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View)object);
        }

        @Override
        public int getCount() {
            return mPages.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return (view == object);
        }

        private String createCellKey(final int x, final int y, final int z) {
            return x + "x" + y + "x" + z;
        }

        private void addCell(final int x, final int y, final int z) {
            mUsedCells.add(createCellKey(x, y ,z));
        }

        public boolean hasCell(final int x, final int y, final int z) {
            return mUsedCells.contains(createCellKey(x, y ,z));
        }

        private void createPage(final int z) {
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.service_action_menu_page, null);
            if (z == 0) {
                TableLayout menu_table = (TableLayout) view.findViewById(R.id.menu_table);
                mTableWidth = menu_table.getWidth();
                badge = (TextView) view.findViewById(R.id.badge);
            }
            mPages.add(z, view);
        }

        public Cell createCell(final int x, final int y, final int z) {
            if (mPages.size() < z + 1) {
                createPage(z);
            }
            addCell(x, y ,z);
            View page = mPages.get(z);

            TableRow row = (TableRow) page.findViewById(getResources().getIdentifier("row" + y, "id", getPackageName()));
            row.setVisibility(View.VISIBLE);

            android.view.ViewGroup cellLayout = (android.view.ViewGroup) page.findViewById(getResources().getIdentifier(
                    "menu_" + x + "x" + y, "id", getPackageName()));
            cellLayout.setVisibility(View.VISIBLE);
            android.view.ViewGroup.LayoutParams lp = cellLayout.getLayoutParams();
            lp.width = mTableWidth / 4;
            cellLayout.setLayoutParams(lp);
            Cell cell = new Cell();
            cell.icon = (ImageView) cellLayout.findViewById(R.id.icon);
            cell.label = (TextView) cellLayout.findViewById(R.id.label);
            return cell;
        }

        private void addMenuItemToCell(final long generation, final ServiceMenuItem item, final int x,
                                       final int y, final int z) {
            final Cell cell = mPagerAdapter.createCell(x, y, z);
            View p = (View) cell.icon.getParent();

            View.OnClickListener onClickListener = new SafeViewOnClickListener() {
                @Override
                public void safeOnClick(View v) {
                    if (mMenuItemPresser == null) {
                        mMenuItemPresser = new MenuItemPresser(ServiceActionMenuActivity.this, email);
                    }
                    mMenuItemPresser.itemPressed(item, generation, null);
                }
            };
            p.setOnClickListener(onClickListener);
            int menuItemColor = mDefaultMenuItemColor;
            if (item.iconColor != null) {
                menuItemColor = Color.parseColor("#" + item.iconColor);
            }
            if (UIUtils.isSupportedFontawesomeIcon(item.iconName)) {
                Drawable icon = UIUtils.getIconFromString(ServiceActionMenuActivity.this, item.iconName).color(mBrandingBackgroundColor).sizeDp(24).paddingDp(5);
                cell.icon.setImageDrawable(icon);
                UIUtils.setBackgroundColor(cell.icon, menuItemColor);
            } else if (item.icon == null) {
                L.d(String.format("Font awesome icon not set and icon content not found." +
                        "\nService: %s, iconName: %s", email, item.iconName));
            } else {
                cell.icon.setImageBitmap(BitmapFactory.decodeByteArray(item.icon, 0, item.icon.length));
                UIUtils.setBackgroundColor(cell.icon, mBrandingBackgroundColor);
            }
            p.setVisibility(View.VISIBLE);
            cell.label.setText(item.label);
            if (mUseDarkScheme) {
                cell.label.setTextColor(darkSchemeTextColor);
                cell.label.setShadowLayer(2, 1, 1, Color.BLACK);

                final Drawable d = getResources().getDrawable(R.drawable.mc_smi_background_light);
                p.setBackground(d);
            }
        }

        public void updateServiceMenu(final ServiceMenu menu) {
            mUsedCells = new ArrayList<>();
            mPages = new ArrayList<>();

            addAboutHandler(menu.aboutLabel);
            addHistoryHandler(menu.messagesLabel);
            addCallHandler(menu, menu.callLabel);
            if (CloudConstants.isYSAAA()) {
                addScanHandler(menu, null);
            } else {
                addShareHandler(menu, menu.shareLabel);
            }

            List<ServiceMenuItem> order = new ArrayList<>();
            for (final ServiceMenuItem item : menu.itemList) {
                if (item.fallThrough) {
                    order.add(item);
                    continue;
                }
                addMenuItemToCell(menu.generation, item, (int) item.coords[0],
                        (int) item.coords[1], (int) item.coords[2]);

            }
            Collections.sort(order, comparator);
            for (final ServiceMenuItem item : order) {
                boolean added = false;
                int z = 0;
                while (true) {
                    for (int y = 0; y < 3; y++) {
                        for (int x = 0; x < 4; x++) {
                            if (added) {
                                continue;
                            }
                            if (hasCell(x, y, z)) {
                                continue;
                            }
                            added = true;
                            addMenuItemToCell(menu.generation, item, x, y, z);
                        }
                    }
                    if (added) {
                        break;
                    }
                    z += 1;
                }
            }
            notifyDataSetChanged();
        }
    }

    private final Comparator<ServiceMenuItem> comparator = new Comparator<ServiceMenuItem>() {
        @Override
        public int compare(ServiceMenuItem item1, ServiceMenuItem item2) {
            if (item1.coords[2] != item2.coords[2]) {
                return item1.coords[2] > item2.coords[2] ? 1 : -1; // z

            } else if (item1.coords[1] != item2.coords[1]) {
                return item1.coords[1] > item2.coords[1] ? 1 : -1; // y

            } else if (item1.coords[0] != item2.coords[0]) {
                return item1.coords[0] > item2.coords[0] ? 1 : -1; // x
            }
            return 0;
        }
    };
}

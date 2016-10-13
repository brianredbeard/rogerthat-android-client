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
package com.mobicage.rogerthat.plugins.friends;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
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
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnPreDrawListener;
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
import com.mobicage.rogerthat.util.system.SafeDialogInterfaceOnClickListener;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.system.SafeViewOnClickListener;
import com.mobicage.rogerthat.util.system.SystemUtils;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.ui.Slider;
import com.mobicage.rogerthat.util.ui.UIUtils;
import com.mobicage.rpc.config.CloudConstants;
import com.mobicage.to.friends.ServiceMenuItemTO;

import java.util.ArrayList;
import java.util.List;

public class ServiceActionMenuActivity extends ServiceBoundActivity {

    public static final int MY_PERMISSIONS_REQUEST_CAMERA = 1;
    public static final String SERVICE_EMAIL = "email";
    public static final String MENU_PAGE = "page";
    public static final String SHOW_ERROR_POPUP = "com.mobicage.rogerthat.plugins.friends.ServiceActionMenuActivity.SHOW_ERROR_POPUP";

    private class Cell {
        ImageView icon;
        TextView label;
    }

    private TextView title;
    private WebView branding;
    private LinearLayout pages;
    private final Cell[][] cells = new Cell[4][3];
    private final TableRow[] tableRows = new TableRow[3];
    private String email;
    private String menuBrandingHash;
    private int page;
    private RelativeLayout activity;
    private int darkSchemeTextColor;
    private int lightSchemeTextColor;
    private GestureDetector mGestureScanner;
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
        setActivityName(email);
        page = intent.getIntExtra(MENU_PAGE, 0);
        activity = (RelativeLayout) findViewById(R.id.activity);
        title = (TextView) findViewById(R.id.title);
        badge = (TextView) findViewById(R.id.badge);
        branding = (WebView) findViewById(R.id.branding);
        branding.setWebViewClient(new WebViewClient() {

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

        pages = (LinearLayout) findViewById(R.id.pages);
        darkSchemeTextColor = ContextCompat.getColor(this, android.R.color.primary_text_dark);
        lightSchemeTextColor = ContextCompat.getColor(this, android.R.color.primary_text_light);

        if (intent.getBooleanExtra(SHOW_ERROR_POPUP, false))
            UIUtils.showAlertDialog(this, null, R.string.error_please_try_again);

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

    private void clearScreen() {
        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 3; y++) {
                Cell cell = cells[x][y];
                ((View) cell.icon.getParent()).setOnClickListener(null);
                cell.icon.setVisibility(View.INVISIBLE);
                cell.label.setVisibility(View.INVISIBLE);
                cell.label.setTextColor(lightSchemeTextColor);
                cell.label.setShadowLayer(2, 1, 1, Color.WHITE);
                cells[x][y] = cell;
            }
        }
        pages.removeAllViews();
        pages.setVisibility(View.GONE);
        title.setVisibility(View.GONE);
        branding.setVisibility(View.GONE);
        activity.setBackgroundResource(R.color.mc_background_color);
        for (TableRow row : tableRows) {
            row.setVisibility(View.VISIBLE);
        }
        badge.setVisibility(View.GONE);
    }

    @Override
    protected void onServiceBound() {
        L.d("ServiceActionMenuActivity onServiceBound()");
        final FriendsPlugin friendsPlugin = mService.getPlugin(FriendsPlugin.class);
        final ServiceMenu menu = friendsPlugin.getStore().getMenu(email, page);

        TableLayout menu_table = (TableLayout) findViewById(R.id.menu_table);
        int tableWidth = menu_table.getWidth();
        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 3; y++) {
                android.view.ViewGroup cellLayout = (android.view.ViewGroup) findViewById(getResources().getIdentifier(
                    "menu_" + x + "x" + y, "id", getPackageName()));
                android.view.ViewGroup.LayoutParams lp = cellLayout.getLayoutParams();
                lp.width = tableWidth / 4;
                cellLayout.setLayoutParams(lp);
                Cell cell = new Cell();
                cell.icon = (ImageView) cellLayout.findViewById(R.id.icon);
                cell.label = (TextView) cellLayout.findViewById(R.id.label);
                cells[x][y] = cell;
                int iconBackgroundColor = ContextCompat.getColor(this, R.color.mc_page_light);
                int iconColor = ContextCompat.getColor(this, R.color.mc_white);
                if (y == 0) {
                    switch (x) {
                    case 0:
                        cell.icon.setImageDrawable(new IconicsDrawable(this, FontAwesome.Icon.faw_info).color(iconColor).sizeDp(24).paddingDp(5));
                        UIUtils.setIconBackground(cell.icon, iconBackgroundColor);
                        break;
                    case 1:
                        cell.icon.setImageDrawable(new IconicsDrawable(this, FontAwesome.Icon.faw_envelope).color(iconColor).sizeDp(24).paddingDp(5));
                        UIUtils.setIconBackground(cell.icon, iconBackgroundColor);
                        break;
                    case 2:
                        cell.icon.setImageDrawable(new IconicsDrawable(this, FontAwesome.Icon.faw_phone).color(iconColor).sizeDp(24).paddingDp(5));
                        UIUtils.setIconBackground(cell.icon, iconBackgroundColor);
                        break;
                    case 3:
                        cell.icon.setVisibility(View.INVISIBLE);
                        FontAwesome.Icon iconName = FontAwesome.Icon.faw_thumbs_up;
                        if (CloudConstants.isYSAAA()) {
                            iconName = FontAwesome.Icon.faw_qrcode;
                        }
                        cell.icon.setImageDrawable(new IconicsDrawable(this, iconName).color(iconColor).sizeDp(24).paddingDp(5));
                        UIUtils.setIconBackground(cell.icon, iconBackgroundColor);
                        break;

                    default:
                        break;
                    }
                }
            }
        }
        for (int y = 0; y < 3; y++) {
            TableRow row = (TableRow) findViewById(getResources().getIdentifier("row" + y, "id", getPackageName()));
            tableRows[y] = row;
        }
        clearScreen();

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
        L.d("Setting branding height: " + h);
        branding.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, h));
        branding.setVisibility(View.VISIBLE);
    }

    private void populateScreen() {
        final FriendsPlugin friendsPlugin = mService.getPlugin(FriendsPlugin.class);
        populateScreen(friendsPlugin.getStore().getMenu(email, page));
    }

    private void populateScreen(final ServiceMenu menu) {
        menuBrandingHash = menu.branding;
        final FriendsPlugin friendsPlugin = mService.getPlugin(FriendsPlugin.class);
        final MessagingPlugin messagingPlugin = mService.getPlugin(MessagingPlugin.class);
        final FriendStore store = friendsPlugin.getStore();

        List<Cell> usedCells = new ArrayList<Cell>();
        if (page == 0) {
            addAboutHandler(usedCells, menu.aboutLabel);
            addHistoryHandler(usedCells, store, menu.messagesLabel);
            addCallHandler(menu, usedCells, menu.callLabel);
            if (CloudConstants.isYSAAA()) {
                addScanHandler(menu, usedCells, null);
            } else {
                addShareHandler(menu, usedCells, menu.shareLabel);
            }
        }
        boolean[] rows = new boolean[] { false, false, false };
        if (page == 0)
            rows[0] = true;

        boolean showBranded = false;
        boolean useDarkScheme = false;
        Integer menuItemColor = null;
        Integer brandingBackgroundColor = null;
        if (menu.branding != null) {
            try {
                BrandingMgr brandingMgr = messagingPlugin.getBrandingMgr();
                if (brandingMgr.isBrandingAvailable(menu.branding)) {
                    BrandingResult br = brandingMgr.prepareBranding(menu.branding, null, false);
                    WebSettings settings = branding.getSettings();
                    settings.setJavaScriptEnabled(false);
                    settings.setBlockNetworkImage(false);
                    branding.setVisibility(View.VISIBLE);
                    branding.setVerticalScrollBarEnabled(false);

                    final int displayWidth = UIUtils.getDisplayWidth(this);
                    final int calculatedHeight = BrandingMgr.calculateHeight(br, displayWidth);
                    final long start = System.currentTimeMillis();
                    branding.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
                        @Override
                        public boolean onPreDraw() {
                            int height = branding.getMeasuredHeight();
                            if (height > calculatedHeight * 90 / 100 || System.currentTimeMillis() - start > 3000) {
                                if (calculatedHeight > 0) {
                                    setBrandingHeight(height);
                                } else {
                                    mService.postDelayedOnUIHandler(new SafeRunnable() {
                                        @Override
                                        protected void safeRun() throws Exception {
                                            setBrandingHeight(branding.getMeasuredHeight());
                                        }
                                    }, 100);
                                }
                                branding.getViewTreeObserver().removeOnPreDrawListener(this);
                            }
                            return false;
                        }
                    });
                    branding.loadUrl("file://" + br.file.getAbsolutePath());

                    if (br.color != null) {
                        branding.setBackgroundColor(br.color);
                        activity.setBackgroundColor(br.color);
                    }
                    if (br.scheme == ColorScheme.DARK) {
                        for (Cell cell : usedCells) {
                            cell.label.setTextColor(darkSchemeTextColor);
                            cell.label.setShadowLayer(2, 1, 1, Color.BLACK);
                        }
                        useDarkScheme = true;
                    }
                    menuItemColor = br.menuItemColor;
                    brandingBackgroundColor = br.color;

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

                    showBranded = true;
                } else {
                    Friend friend = store.getExistingFriend(email);
                    friend.actionMenu = menu;
                    friend.actionMenu.items = menu.itemList.toArray(new ServiceMenuItemTO[] {});
                    brandingMgr.queue(friend);
                }
            } catch (BrandingFailureException e) {
                L.bug("Could not display service action menu with branding.", e);
            }
        }
        if (menuItemColor == null)
            menuItemColor = ContextCompat.getColor(this, R.color.mc_page_light);
        if (brandingBackgroundColor == null) {
            brandingBackgroundColor = ContextCompat.getColor(this, R.color.mc_page_light);
        }
        for (final ServiceMenuItem item : menu.itemList) {
            rows[(int) item.coords[1]] = true;
            final Cell cell = cells[(int) item.coords[0]][(int) item.coords[1]];
            View.OnClickListener onClickListener = new SafeViewOnClickListener() {
                @Override
                public void safeOnClick(View v) {
                    if (mMenuItemPresser == null) {
                        mMenuItemPresser = new MenuItemPresser(ServiceActionMenuActivity.this, email);
                    }
                    mMenuItemPresser.itemPressed(item, menu.generation, null);
                }
            };
            ((View) cell.icon.getParent()).setOnClickListener(onClickListener);
            if (UIUtils.isSupportedFontawesomeIcon(item.iconName)) {
                Drawable icon = UIUtils.getIconFromString(this, item.iconName).color(brandingBackgroundColor).sizeDp(24).paddingDp(5);
                cell.icon.setImageDrawable(icon);
                UIUtils.setIconBackground(cell.icon, menuItemColor);
            } else if( item.icon == null) {
                L.bug(String.format("Font awesome icon not set and icon content not found." +
                        "\nService: %s, iconName: %s",email, item.iconName));
            }else{
                cell.icon.setImageBitmap(BitmapFactory.decodeByteArray(item.icon, 0, item.icon.length));
                UIUtils.setIconBackground(cell.icon, brandingBackgroundColor);
            }
            cell.icon.setVisibility(View.VISIBLE);
            cell.label.setText(item.label);
            if (useDarkScheme) {
                cell.label.setTextColor(darkSchemeTextColor);
                cell.label.setShadowLayer(2, 1, 1, Color.BLACK);
            }
            cell.label.setVisibility(View.VISIBLE);
            usedCells.add(cell);
        }
        for (int i = 2; i >= 0; i--) {
            if (rows[i])
                break;
            tableRows[i].setVisibility(View.GONE);
        }

        setTitle(menu.name);
        if (!showBranded) {
            title.setVisibility(View.GONE);
            title.setText(menu.name);
        }

        if (useDarkScheme) {
            for (final Cell cell : usedCells) {
                final View p = (View) cell.icon.getParent();
                final Drawable d = getResources().getDrawable(R.drawable.mc_smi_background_light);
                p.setBackground(d);
            }
        }

        if (page == 0) {
            cells[0][0].icon.setImageDrawable(new IconicsDrawable(this, FontAwesome.Icon.faw_info).color(brandingBackgroundColor).sizeDp(24).paddingDp(5));
            UIUtils.setIconBackground(cells[0][0].icon, menuItemColor);
            cells[1][0].icon.setImageDrawable(new IconicsDrawable(this, FontAwesome.Icon.faw_envelope).color(brandingBackgroundColor).sizeDp(24).paddingDp(5));
            UIUtils.setIconBackground(cells[1][0].icon, menuItemColor);
            cells[2][0].icon.setImageDrawable(new IconicsDrawable(this, FontAwesome.Icon.faw_phone).color(brandingBackgroundColor).sizeDp(24).paddingDp(5));
            UIUtils.setIconBackground(cells[2][0].icon, menuItemColor);
            if (CloudConstants.isYSAAA()) {
                cells[3][0].icon.setImageDrawable(new IconicsDrawable(this, FontAwesome.Icon.faw_qrcode).color(brandingBackgroundColor).sizeDp(24).paddingDp(5));
            } else {
                cells[3][0].icon.setImageDrawable(new IconicsDrawable(this, FontAwesome.Icon.faw_thumbs_up).color(brandingBackgroundColor).sizeDp(24).paddingDp(5));
            }
            UIUtils.setIconBackground(cells[3][0].icon, menuItemColor);
        }

        if (menu.maxPage > 0) {
            for (int i = 0; i <= menu.maxPage; i++) {
                ImageView bolleke = (ImageView) getLayoutInflater().inflate(R.layout.page, pages, false);
                if (page == i) {
                    if (useDarkScheme) {
                        bolleke.setImageResource(R.drawable.current_page_dark);
                    } else {
                        bolleke.setImageResource(R.drawable.current_page_light);
                    }
                } else {
                    if (useDarkScheme) {
                        bolleke.setImageResource(R.drawable.other_page_dark);
                    } else {
                        bolleke.setImageResource(R.drawable.other_page_light);
                    }
                }
                pages.addView(bolleke);
            }
            pages.setVisibility(View.VISIBLE);
        }
        final int leftPage = page - 1;
        final int rightPage = page + 1;
        final String service = email;
        Slider instance = new Slider(this, this, page == menu.maxPage ? null : new Slider.Swiper() {
            @Override
            public Intent onSwipe() {
                return new Intent(ServiceActionMenuActivity.this, ServiceActionMenuActivity.class).putExtra(
                    SERVICE_EMAIL, service).putExtra(MENU_PAGE, rightPage);
            }
        }, page == 0 ? null : new Slider.Swiper() {
            @Override
            public Intent onSwipe() {
                return new Intent(ServiceActionMenuActivity.this, ServiceActionMenuActivity.class).putExtra(
                    SERVICE_EMAIL, service).putExtra(MENU_PAGE, leftPage);
            }
        });
        mGestureScanner = new GestureDetector(this, instance);
    }

    private final BroadcastReceiver mBroadcastReceiver = new SafeBroadcastReceiver() {

        @Override
        public String[] onSafeReceive(final Context context, final Intent intent) {
            T.UI();
            String action = intent.getAction();
            if (FriendsPlugin.FRIEND_UPDATE_INTENT.equals(action)) {
                if (email.equals(intent.getStringExtra("email"))) {
                    clearScreen();
                    populateScreen();

                    return new String[] { FriendsPlugin.FRIEND_UPDATE_INTENT,
                        BrandingMgr.SERVICE_BRANDING_AVAILABLE_INTENT, BrandingMgr.GENERIC_BRANDING_AVAILABLE_INTENT };
                }
            } else if (BrandingMgr.SERVICE_BRANDING_AVAILABLE_INTENT.equals(action)
                || BrandingMgr.GENERIC_BRANDING_AVAILABLE_INTENT.equals(action)) {
                if (menuBrandingHash != null
                    && menuBrandingHash.equals(intent.getStringExtra(BrandingMgr.BRANDING_KEY))) {
                    clearScreen();
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

    private void addAboutHandler(final List<Cell> usedCells, final String aboutLabel) {
        final Cell cell = cells[0][0];
        cell.label.setText(TextUtils.isEmptyOrWhitespace(aboutLabel) ? getString(R.string.about) : aboutLabel);

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
        ((View) cell.icon.getParent()).setOnClickListener(onClickListener);
        cell.icon.setVisibility(View.VISIBLE);
        cell.label.setVisibility(View.VISIBLE);
        usedCells.add(cell);
    }

    private void addHistoryHandler(final List<Cell> usedCells, final FriendStore friendStore, final String messagesLabel) {
        final Cell cell = cells[1][0];
        cell.label.setText(TextUtils.isEmptyOrWhitespace(messagesLabel) ? getString(R.string.message_history)
            : messagesLabel);
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
        ((View) cell.icon.getParent()).setOnClickListener(onClickListener);
        cell.icon.setVisibility(View.VISIBLE);
        cell.label.setVisibility(View.VISIBLE);
        usedCells.add(cell);
        handleBadge(friendStore);
    }

    private void handleBadge(final FriendStore friendStore) {
        if (page == 0) {
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
        } else {
            badge.setVisibility(View.GONE);
        }
    }

    private void addCallHandler(final ServiceMenu menu, final List<Cell> usedCells, final String callLabel) {
        if (menu.phoneNumber != null) {
            final Cell cell = cells[2][0];
            cell.label.setText(TextUtils.isEmptyOrWhitespace(callLabel) ? getString(R.string.call_service) : callLabel);
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
                        AlertDialog.Builder builder = new AlertDialog.Builder(ServiceActionMenuActivity.this);
                        builder.setMessage(TextUtils.isEmptyOrWhitespace(menu.callConfirmation) ? getString(
                            R.string.caption_call, menu.phoneNumber) : menu.callConfirmation);
                        builder.setCancelable(true);
                        builder.setTitle(R.string.call_service);
                        builder.setPositiveButton(R.string.rogerthat, new SafeDialogInterfaceOnClickListener() {
                            @Override
                            public void safeOnClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        builder.create().show();
                    } else {
                        callIntent.setData(Uri.parse("tel://" + menu.phoneNumber));
                        startActivity(callIntent);
                    }
                }
            };
            ((View) cell.icon.getParent()).setOnClickListener(onClickListener);
            cell.icon.setVisibility(View.VISIBLE);
            cell.label.setVisibility(View.VISIBLE);
            usedCells.add(cell);
        }
    }

    private void addShareHandler(final ServiceMenu menu, final List<Cell> usedCells, final String shareLabel) {
        if (menu.share) {
            final Cell cell = cells[3][0];
            cell.label.setText(TextUtils.isEmptyOrWhitespace(shareLabel) ? getString(R.string.recommend_service)
                : shareLabel);
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
            ((View) cell.icon.getParent()).setOnClickListener(onClickListener);
            cell.icon.setVisibility(View.VISIBLE);
            cell.label.setVisibility(View.VISIBLE);
            usedCells.add(cell);
        }
    }

    private void addScanHandler(final ServiceMenu menu, final List<Cell> usedCells, final String scanLabel) {
        final Cell cell = cells[3][0];
        cell.label.setText(TextUtils.isEmptyOrWhitespace(scanLabel) ? getString(R.string.scan) : scanLabel);

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
        ((View) cell.icon.getParent()).setOnClickListener(onClickListener);
        cell.icon.setVisibility(View.VISIBLE);
        cell.label.setVisibility(View.VISIBLE);
        usedCells.add(cell);
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
    public boolean dispatchTouchEvent(MotionEvent e) {
        if (mGestureScanner != null) {
            mGestureScanner.onTouchEvent(e);
        }
        return super.dispatchTouchEvent(e);
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

}

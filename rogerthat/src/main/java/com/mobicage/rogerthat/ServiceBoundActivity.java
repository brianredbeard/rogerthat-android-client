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

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.LayoutInflaterCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.facebook.CallbackManager;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.context.IconicsLayoutInflater;
import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.plugins.friends.MenuItemPresser;
import com.mobicage.rogerthat.plugins.friends.MenuItemPressingActivity;
import com.mobicage.rogerthat.plugins.friends.PokingActivity;
import com.mobicage.rogerthat.plugins.friends.ServiceSearchActivity;
import com.mobicage.rogerthat.util.ActivityUtils;
import com.mobicage.rogerthat.util.Security;
import com.mobicage.rogerthat.util.TextUtils;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeBroadcastReceiver;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.system.SafeViewOnClickListener;
import com.mobicage.rogerthat.util.system.SystemUtils;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.ui.Pausable;
import com.mobicage.rogerthat.util.ui.UIUtils;
import com.mobicage.rpc.config.AppConstants;
import com.mobicage.rpc.config.NavigationConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public abstract class ServiceBoundActivity extends AppCompatActivity implements Pausable, ServiceBound,
        MenuItemPressingActivity, PokingActivity {

    public static final long MAX_TRANSMIT = 10 * 1000;

    protected volatile MainService mService;
    protected boolean mServiceIsBound = false; // Owned by UI thread
    private boolean mPaused = false; // Owned by UI thread
    final private Queue<SafeRunnable> mWorkQueue = new LinkedList<SafeRunnable>();

    private final BroadcastReceiver intentListener = new SafeBroadcastReceiver() {
        @Override
        public String[] onSafeReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (MainService.CLOSE_ACTIVITY_INTENT.equals(action)) {
                L.d("Received CLOSE_ACTIVITY_INTENT");
                finish();
            } else if (MainService.UPDATE_BADGE_INTENT.equals(action)) {
                String key = intent.getStringExtra("key");
                long count = intent.getLongExtra("count", 0);
                updateBadgeCount(key, count);
            }

            return null;
        }
    };
    private AlertDialog mTransmitProgressDialog;
    private ProgressBar mTransmitProgressBar;
    private long mTransmitStart = 0;

    private SafeRunnable mTransmitTimeoutRunnable;

    private ConnectivityManager mConnectivityManager;
    private List<SafeRunnable> mOnServiceBoundRunnables;

    private long mLastTimeClicked = 0;
    public static final long DOUBLE_CLICK_TIMESPAN = 1000;

    private boolean mWasPaused = false;
    private Map<Integer, SafeRunnable[]> mPermissionRequests = new HashMap<>();

    private CallbackManager mFBCallbackMgr;

    private Toolbar mToolbar;
    private ActionBarDrawerToggle mDrawerToggle;
    private String mActivityName;

    public MenuItemPresser menuItemPresser;
    private boolean mShowDrawer = false;
    private boolean mShowDrawerIcon = false;

    private Map<String, Integer> mBadgePositionsNavigationDrawer = new HashMap<>();
    private Map<String, Integer> mBadgePositionsNavigationFooter = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        T.setUIThread("ServiceBoundActivity.onCreate()");

        ServiceBoundActivity.NavigationItem[] navigationItems = NavigationConstants.getNavigationItems();
        for (int i = 0; i < navigationItems.length; i++) {
            if (navigationItems[i].actionType == null) {
                mBadgePositionsNavigationDrawer.put(navigationItems[i].action, i);
            } else {
                mBadgePositionsNavigationDrawer.put(navigationItems[i].actionType + "|" + navigationItems[i].action, i);
            }
        }

        ServiceBoundActivity.NavigationItem[] navigationFooterItems = NavigationConstants.getNavigationFooterItems();
        for (int i = 0; i < navigationFooterItems.length; i++) {
            if (navigationFooterItems[i].actionType == null) {
                mBadgePositionsNavigationFooter.put(navigationFooterItems[i].action, i);
            } else {
                mBadgePositionsNavigationFooter.put(navigationFooterItems[i].actionType + "|" + navigationFooterItems[i].action, i);
            }
        }

        LayoutInflaterCompat.setFactory(getLayoutInflater(), new IconicsLayoutInflater(getDelegate()));
        super.onCreate(savedInstanceState);
        logMethod("onCreate");
        SystemUtils.logIntentFlags(getIntent());

        IntentFilter filter = new IntentFilter(MainService.CLOSE_ACTIVITY_INTENT);
        filter.addAction(MainService.UPDATE_BADGE_INTENT);
        registerReceiver(intentListener, filter);
        doBindService();

        mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        View progressDialg = getLayoutInflater().inflate(R.layout.progressdialog, null);
        mTransmitProgressDialog = new AlertDialog.Builder(this).setTitle(R.string.transmitting).setView(progressDialg).create();
        mTransmitProgressBar = (ProgressBar) progressDialg.findViewById(R.id.progress_bar);
        mTransmitProgressDialog.setCancelable(true);
        mTransmitProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                completeTransmit(null);
            }
        });
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }


    @Override
    protected void onDestroy() {
        T.UI();
        logMethod("onDestroy");
        super.onDestroy();
        unregisterReceiver(intentListener);
        doUnbindService();
    }

    public void showTransmitting(SafeRunnable onTimeout) {
        T.UI();
        mTransmitTimeoutRunnable = onTimeout;
        mTransmitStart = System.currentTimeMillis();
        mTransmitProgressBar.setProgress(0);
        mTransmitProgressDialog.show();
        mService.postDelayedOnUIHandler(mIncreaseProgress, 100);
    }

    public boolean isTransmitting() {
        T.UI();
        return mTransmitProgressDialog.isShowing();
    }

    public void completeTransmit(final SafeRunnable afterComplete) {
        T.UI();
        mTransmitProgressDialog.dismiss();
        mService.removeFromUIHandler(mIncreaseProgress);
        mTransmitProgressBar.setProgress(100);
        if (afterComplete != null && mService != null) {
            mService.postDelayedOnUIHandler(new SafeRunnable() {
                @Override
                protected void safeRun() throws Exception {
                    afterComplete.run();
                }
            }, 200);
        }
    }

    private final SafeRunnable mIncreaseProgress = new SafeRunnable() {
        @Override
        protected void safeRun() throws Exception {
            T.UI();
            if (mTransmitProgressDialog.isShowing()) {
                int completenessLevel = (int) ((System.currentTimeMillis() - mTransmitStart) * 100 / MAX_TRANSMIT);
                if (completenessLevel < 100) {
                    mTransmitProgressBar.setProgress(completenessLevel);
                    if (mService != null)
                        mService.postDelayedOnUIHandler(mIncreaseProgress, 100);
                } else {
                    mTransmitProgressDialog.dismiss();
                    showActionScheduledDialog();
                }
            }
        }
    };

    public void showActionScheduledDialog() {
        new AlertDialog.Builder(ServiceBoundActivity.this).setMessage(R.string.action_scheduled)
                .setPositiveButton(R.string.rogerthat, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (mTransmitTimeoutRunnable != null)
                            mTransmitTimeoutRunnable.run();
                    }
                }).create().show();
    }

    public boolean checkConnectivity() {
        NetworkInfo activeNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    public boolean checkConnectivityIsWifi() {
        return mService.getNetworkConnectivityManager().isWifiConnected();
    }

    private void doBindService() {
        T.UI();
        if (!mServiceIsBound) {
            Intent intent = new Intent(this, MainService.class);
            intent.putExtra("clazz", this.getClass().getName());
            boolean success = getApplicationContext().bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
            logMethod("doBindService : " + success);
        }
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            T.UI();
            mService = ((MainService.MainBinder) service).getService();
            if (mShowDrawerIcon) {
                setupBadges();
            }

            try {
                onServiceBound();
                mServiceIsBound = true;
            } catch (Exception e) {
                L.bug(e);
                finish();
            }
            if (mOnServiceBoundRunnables != null) {
                for (SafeRunnable runnable : mOnServiceBoundRunnables) {
                    runnable.run();
                }
                mOnServiceBoundRunnables = null;
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            T.UI();
            logMethod("onServiceDisconnected");
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // Since we use a local service, which is running
            // in our own process, we should never see this happen.
        }
    };

    protected void addOnServiceBoundRunnable(SafeRunnable runnable) {
        if (mOnServiceBoundRunnables == null) {
            mOnServiceBoundRunnables = new ArrayList<SafeRunnable>();
        }
        mOnServiceBoundRunnables.add(runnable);
    }

    private void doUnbindService() {
        T.UI();
        if (mServiceIsBound) {
            if (menuItemPresser != null) {
                menuItemPresser.stop();
                menuItemPresser = null;
            }

            try {
                onServiceUnbound();
            } catch (Exception e) {
                L.bug(e);
                finish();
            }
            getApplicationContext().unbindService(mServiceConnection);
            mServiceIsBound = false;
            mService = null;
        }
    }

    protected abstract void onServiceBound();

    protected abstract void onServiceUnbound();

    @Override
    protected void onPause() {
        T.UI();
        super.onPause();
        mPaused = true;
        mWasPaused = true;
    }

    @Override
    protected void onResume() {
        T.UI();
        super.onResume();
        mPaused = false;
        SafeRunnable runnable = mWorkQueue.poll();
        // XXX: limit execution time to 9 seconds
        while (runnable != null) {
            runnable.run();
            runnable = mWorkQueue.poll();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        UIUtils.onActivityStop(this);
    }

    @Override
    protected void onStart() {
        UIUtils.onActivityStart(this);
        super.onStart();
    }

    @Override
    public boolean getPaused() {
        T.UI();
        return mPaused;
    }

    public boolean getWasPaused() {
        T.UI();
        return mWasPaused;
    }

    @Override
    public void queue(SafeRunnable runnable) {
        T.UI();
        mWorkQueue.add(runnable);
    }

    protected void logMethod(String method) {
        L.d(getClass().getName() + "(ServiceBoundActivity)." + method);
    }

    @Override
    public MainService getMainService() {
        return mService;
    }

    public void setNavigationBarBurgerVisible(boolean isVisible) {
        setNavigationBarBurgerVisible(isVisible, false);
    }

    public void setNavigationBarBurgerVisible(boolean isVisible, boolean clickable) {
        if (isVisible) {
            getDrawer().setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            mDrawerToggle.setDrawerIndicatorEnabled(true);
            mDrawerToggle.setToolbarNavigationClickListener(null);
        } else {
            getDrawer().setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            mDrawerToggle.setDrawerIndicatorEnabled(false);
            if (clickable) {
                mDrawerToggle.setToolbarNavigationClickListener(new SafeViewOnClickListener() {
                    @Override
                    public void safeOnClick(View v) {
                        onBackPressed();
                    }
                });
            } else {
                mDrawerToggle.setToolbarNavigationClickListener(null);
            }
        }
    }

    public void setNavigationBarIcon(int resId) {
        setNavigationBarIcon(getResources().getDrawable(resId));
    }

    public void setNavigationBarIcon(@Nullable Drawable icon) {
        mToolbar.setNavigationIcon(icon);
    }

    public void setLastTimeClicked(final long ts) {
        mLastTimeClicked = ts;
    }

    public long getLastTimeClicked() {
        return mLastTimeClicked;
    }

    public void setContentViewWithoutNavigationBar(int layoutResID) {
        super.setContentView(layoutResID);
    }

    @Override
    public void setContentView(int layoutResID) {
        setContentView(getLayoutInflater().inflate(R.layout.navigation_view, null));

        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mToolbar.setOnClickListener(new SafeViewOnClickListener() {
            @Override
            public void safeOnClick(View v) {
                onToolbarClicked();
            }
        });

        LinearLayout item = (LinearLayout) findViewById(R.id.linear_layout);
        View child = getLayoutInflater().inflate(layoutResID, null);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

        item.addView(child, layoutParams);

        final DrawerLayout drawer = getDrawer();
        mDrawerToggle = new ActionBarDrawerToggle(this, drawer, mToolbar, R.string
                .navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        drawer.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        Intent intent = getIntent();
        mShowDrawer = intent.getBooleanExtra("show_drawer", false);
        mShowDrawerIcon = intent.getBooleanExtra("show_drawer_icon", false);

        if (mShowDrawer) {
            openNavigationView();
        }

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        final Menu menu = navigationView.getMenu();
        ServiceBoundActivity.NavigationItem[] navigationItems = NavigationConstants.getNavigationItems();
        for (int i = 0; i < navigationItems.length; i++) {
            final NavigationItem ni = navigationItems[i];
            menu.add(i, i, i, ni.labelTextId).setIcon(ni.iconId).setCheckable(true).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    simulateNavigationItemClick(ni);
                    return true;
                }
            }).setActionView(R.layout.navigation_menu_counter);
        }
        // Adding 2 spacer items such that the footer view doesn't overlap the last item(s)
        menu.add(navigationItems.length, navigationItems.length, navigationItems.length, "").setCheckable(false);
        menu.add(navigationItems.length, navigationItems.length + 1, navigationItems.length + 1, "").setCheckable(false);
        navigationView.setItemIconTintList(null);

        LinearLayout navigationFooter = (LinearLayout) findViewById(R.id.nav_view_footer);
        ServiceBoundActivity.NavigationItem[] navigationFooterItems = NavigationConstants.getNavigationFooterItems();
        if (navigationFooterItems.length > 0) {
            navigationFooter.removeAllViews();
            LayoutInflater li = getLayoutInflater();
            for (final NavigationItem ni : navigationFooterItems) {
                View footerItem = li.inflate(R.layout.navigation_footer_item, navigationFooter, false);
                if (ni.actionType == null) {
                    footerItem.setTag(ni.action);
                } else {
                    footerItem.setTag(ni.actionType + "|" + ni.action);
                }
                ImageButton imageButton = (ImageButton) footerItem.findViewById(R.id.image);
                imageButton.setOnClickListener(new SafeViewOnClickListener() {
                    @Override
                    public void safeOnClick(View v) {
                        simulateNavigationItemClick(ni);
                    }
                });

                imageButton.setImageDrawable(new IconicsDrawable(this, ni.icon).color(Color.WHITE).sizeDp(20));
                navigationFooter.addView(footerItem);
            }

            navigationFooter.setVisibility(View.VISIBLE);
        } else {
            navigationFooter.setVisibility(View.GONE);
        }

        if (mShowDrawerIcon) {
            setupBadges();
        } else {
            setNavigationBarBurgerVisible(false, true);
        }
    }

    private void simulateNavigationItemClick(NavigationItem ni) {
        String activityName = ni.action;
        if (ni.actionType != null) {
            activityName = ni.actionType + "|" + ni.action;
        }
        if (!activityName.equals(mActivityName)) {
            mService.postDelayedOnUIHandler(new SafeRunnable() {
                @Override
                protected void safeRun() throws Exception {
                    activateCurrentNavigationItem();
                }
            }, 250);
        }

        closeNavigationView();

        if (ni.actionType == null) {
            Bundle extras = new Bundle();
            extras.putBoolean("show_drawer_icon", true);
            ActivityUtils.goToActivity(ServiceBoundActivity.this, ni.action, true, ni.collapse, extras);
        } else if ("action".equals(ni.actionType)) {
            Class clazz;
            if (mService.getNetworkConnectivityManager().isConnected()) {
                clazz = ServiceSearchActivity.class;
            } else {
                clazz = ServiceActionsOfflineActivity.class;
            }

            final Intent i = new Intent(ServiceBoundActivity.this, clazz);
            i.putExtra(ServiceActionsOfflineActivity.ACTION, ni.action);
            i.putExtra(ServiceActionsOfflineActivity.TITLE, ni.labelTextId);
            i.putExtra("show_drawer_icon", true);
            i.addFlags(MainActivity.FLAG_CLEAR_STACK);
            ServiceBoundActivity.this.startActivity(i);

        } else if ("click".equals(ni.actionType)) {
            if (TextUtils.isEmptyOrWhitespace(AppConstants.APP_EMAIL)) {
                L.bug("simulateNavigationItemClick click but app_email was nog set");
                return;
            }
            String hashedTag = Security.sha256Lower(ni.action);
            ActivityUtils.goToActivityBehindTag(ServiceBoundActivity.this, AppConstants.APP_EMAIL, hashedTag);
        } else {
            L.bug("ignoring simulateNavigationItemClick: " + ni.actionType + "|" + ni.action);
        }
    }

    public static class NavigationItem {
        public FontAwesome.Icon icon;
        public int iconId;
        public String actionType;
        public String action;
        public int labelTextId;
        public boolean collapse;

        public NavigationItem(FontAwesome.Icon icon, String actionType, String action, int labelTextId, boolean collapse) {
            super();
            this.icon = icon;
            this.iconId = 0;
            this.actionType = actionType;
            this.action = action;
            this.labelTextId = labelTextId;
            this.collapse = collapse;
        }

        public NavigationItem(int iconId, String actionType, String action, int labelTextId, boolean collapse) {
            super();
            this.icon = null;
            this.iconId = iconId;
            this.actionType = actionType;
            this.action = action;
            this.labelTextId = labelTextId;
            this.collapse = collapse;
        }
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(view, params);
    }

    public boolean askPermissionIfNeeded(final String permission, final int requestCode, final SafeRunnable onGranted,
                                         final SafeRunnable onDenied) {
        final boolean granted = mService.isPermitted(permission);
        if (!granted) {
            L.i("Requesting permission: " + permission);
            mPermissionRequests.put(requestCode, new SafeRunnable[]{onGranted, onDenied});

            ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
        }
        return !granted;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        final SafeRunnable[] runnables = mPermissionRequests.remove(requestCode);
        if (runnables == null) {
            L.d("Unknown permissionResult requestCode in " + getLocalClassName() + ": " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        final boolean granted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
        final SafeRunnable runnable = runnables[granted ? 0 : 1];
        if (runnable != null) {
            runnable.run();
        }
    }

    public CallbackManager getFacebookCallbackManager() {
        if (mFBCallbackMgr == null) {
            mFBCallbackMgr = CallbackManager.Factory.create();
        }
        return mFBCallbackMgr;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mFBCallbackMgr != null ) {
            mFBCallbackMgr.onActivityResult(requestCode, resultCode, data);
        }
    }


    public DrawerLayout getDrawer() {
        return (DrawerLayout) findViewById(R.id.drawer_layout);
    }

    public void openNavigationView() {
        getDrawer().openDrawer(GravityCompat.START);
    }

    public void closeNavigationView() {
        getDrawer().closeDrawer(GravityCompat.START);
    }

    public void setActivityName(String activityName) {
        mActivityName = activityName;
        activateCurrentNavigationItem();
    }

    private void activateCurrentNavigationItem() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView == null)
            return;

        Menu menu = navigationView.getMenu();
        if (menu == null)
            return;

        int order = -1;
        if (mActivityName != null) {
            ServiceBoundActivity.NavigationItem[] navigationItems = NavigationConstants.getNavigationItems();
            for (int i = 0; i < navigationItems.length; i++) {
                final NavigationItem ni = navigationItems[i];
                if (ni.actionType == null && ni.action.equals(mActivityName)) {
                    order = i;
                    break;
                }
            }
        }
        if (order >= 0) {
            menu.getItem(order).setChecked(true);
        } else {
            for (int i = 0; i < menu.size(); i++) {
                menu.getItem(i).setChecked(false);
            }
        }

        if (TextUtils.isEmptyOrWhitespace(mActivityName))
            return;

        LinearLayout navigationFooter = (LinearLayout) findViewById(R.id.nav_view_footer);
        if (navigationFooter.getVisibility() == View.GONE)
            return;

        for (int i = 0; i < navigationFooter.getChildCount(); i++) {
            View child = navigationFooter.getChildAt(i);
            if (child instanceof FrameLayout) {
                String activityName = (String) child.getTag();
                FrameLayout ib = (FrameLayout) child;
                if (mActivityName.equals(activityName)) {
                    ib.setBackgroundColor(ContextCompat.getColor(this, R.color.mc_navigation_footer_active));
                } else {
                    ib.setBackground(null);
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public void onToolbarClicked() {
        // override this method if you want to subscribe to this click
    }

    public void onNavigationHeaderBurgerClicked(View v) {
        closeNavigationView();
    }

    public void onNavigationHeaderImageClicked(View v) {
        closeNavigationView();

        Bundle extras = new Bundle();
        extras.putBoolean("show_drawer_icon", true);
        if (AppConstants.HOME_ACTIVITY_LAYOUT == R.layout.messaging) {
            ActivityUtils.goToActivity(ServiceBoundActivity.this, "messages", true, false, extras);
        } else {
            ActivityUtils.goToActivity(ServiceBoundActivity.this, "news", true, false, extras);
        }
    }

    private void setupBadges() {
        if (mService == null)
            return;

        for (Map.Entry<String, Long> entry : mService.badges.entrySet()) {
            updateBadge(entry.getKey(), entry.getValue());
        }
    }

    public void updateBadgeCount(String key, long count) {
        if (mService == null) {
            L.e("called updateBadgeCount to early service not ready yet");
            return;
        }
        mService.badges.put(key, count);
        updateBadge(key, count);
    }

    private void updateBadge(String key, long count) {
        if (!mShowDrawerIcon)
            return;

        boolean foundBadge = false;
        if (mBadgePositionsNavigationDrawer.containsKey(key)) {
            foundBadge = true;

            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            if (navigationView == null)
                return;

            Menu menu = navigationView.getMenu();
            if (menu == null)
                return;

            int index = mBadgePositionsNavigationDrawer.get(key);
            if (menu.size() <= index)
                return;

            View v = menu.getItem(index).getActionView();
            TextView badge = (TextView) v.findViewById(R.id.badge);
            if (count > 0) {
                badge.setText(count > 9 ? "9+" : String.valueOf(count));
                badge.setVisibility(View.VISIBLE);
            } else {
                badge.setVisibility(View.GONE);
            }
        }

        if (mBadgePositionsNavigationFooter.containsKey(key)) {
            foundBadge = true;

            LinearLayout navigationFooter = (LinearLayout) findViewById(R.id.nav_view_footer);
            if (navigationFooter == null)
                return;

            int index = mBadgePositionsNavigationFooter.get(key);
            if (navigationFooter.getChildCount() <= index)
                return;

            TextView badge = (TextView) navigationFooter.getChildAt(index).findViewById(R.id.badge);
            if (count > 0) {
                badge.setText(count > 9 ? "9+" : String.valueOf(count));
                badge.setVisibility(View.VISIBLE);
            } else {
                badge.setVisibility(View.GONE);
            }
        }

        if (!foundBadge) {
            L.e("updateBadge failed for: " + key);
        }
    }
}

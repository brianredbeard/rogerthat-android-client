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

package com.mobicage.rogerthat;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
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
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.CallbackManager;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.context.IconicsLayoutInflater;
import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.plugins.friends.MenuItemPresser;
import com.mobicage.rogerthat.plugins.friends.MenuItemPressingActivity;
import com.mobicage.rogerthat.plugins.friends.PokingActivity;
import com.mobicage.rogerthat.plugins.system.SystemPlugin;
import com.mobicage.rogerthat.util.ActivityUtils;
import com.mobicage.rogerthat.util.TextUtils;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeBroadcastReceiver;
import com.mobicage.rogerthat.util.system.SafeDialogClick;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.system.SafeViewOnClickListener;
import com.mobicage.rogerthat.util.system.SystemUtils;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.ui.Pausable;
import com.mobicage.rogerthat.util.ui.UIUtils;
import com.mobicage.rpc.config.LookAndFeelConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import me.leolin.shortcutbadger.ShortcutBadgeException;
import me.leolin.shortcutbadger.ShortcutBadger;

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
            } else if (SystemPlugin.ASSET_AVAILABLE_INTENT.equals(intent.getAction())) {
                String kind = intent.getStringExtra(SystemPlugin.ASSET_KIND);
                if (LookAndFeelConstants.getAssetKindOfHeaderImage().equals(kind)) {
                    setHeaderImage();
                }
            } else if (SystemPlugin.LOOK_AND_FEEL_UPDATED_INTENT.equals(intent.getAction())) {
                mNavigationListViewAdapter.setmNavigationItems(LookAndFeelConstants.getNavigationItems(context));
                mNavigationListViewAdapter.notifyDataSetChanged();
                showNavigationToolbar();
            }

            return null;
        }
    };
    private ProgressDialog mTransmitProgressDialog;
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
    private boolean mShowDrawerIcon = false;

    private Map<String, Long> mBadgeCountNavigationDrawer = new HashMap<>();
    private Map<String, Integer> mBadgePositionsNavigationFooter = new HashMap<>();
    private NavigationListViewAdapter mNavigationListViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        T.setUIThread("ServiceBoundActivity.onCreate()");
        mNavigationListViewAdapter = new NavigationListViewAdapter(this);
        mNavigationListViewAdapter.setmNavigationItems(LookAndFeelConstants.getNavigationItems(this));
        NavigationItem[] navigationFooterItems = LookAndFeelConstants.getNavigationFooterItems(this);
        for (int i = 0; i < navigationFooterItems.length; i++) {
            mBadgePositionsNavigationFooter.put(navigationFooterItems[i].actionWithType(), i);
        }

        LayoutInflaterCompat.setFactory(getLayoutInflater(), new IconicsLayoutInflater(getDelegate()));
        super.onCreate(savedInstanceState);
        logMethod("onCreate");
        SystemUtils.logIntentFlags(getIntent());

        IntentFilter filter = new IntentFilter(MainService.CLOSE_ACTIVITY_INTENT);
        filter.addAction(MainService.UPDATE_BADGE_INTENT);
        filter.addAction(SystemPlugin.ASSET_AVAILABLE_INTENT);
        filter.addAction(SystemPlugin.LOOK_AND_FEEL_UPDATED_INTENT);
        registerReceiver(intentListener, filter);
        doBindService();

        mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        DialogInterface.OnCancelListener onCancelListener = new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                completeTransmit(null);
            }
        };
        mTransmitProgressDialog = UIUtils.showProgressDialog(this, null, getString(R.string.transmitting), true,
                true, onCancelListener, ProgressDialog.STYLE_HORIZONTAL, false);
    }

    @Override
    protected void onDestroy() {
        T.UI();
        logMethod("onDestroy");
        super.onDestroy();
        unregisterReceiver(intentListener);
        doUnbindService();
    }

    /**
     * TODO: this is mildly infuriating UX. Should be an indeterminate spinner
     *
     * @param onTimeout function to execute on timeout
     */
    public void showTransmitting(final SafeRunnable onTimeout) {
        runOnUiThread(new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                T.UI();
                mTransmitTimeoutRunnable = onTimeout;
                mTransmitStart = System.currentTimeMillis();
                mTransmitProgressDialog.setProgress(0);
                mTransmitProgressDialog.show();
                mService.postDelayedOnUIHandler(mIncreaseProgress, 100);
            }
        });
    }

    public boolean isTransmitting() {
        T.UI();
        return mTransmitProgressDialog.isShowing();
    }

    public void completeTransmit(final SafeRunnable afterComplete) {
        runOnUiThread(new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                T.UI();
                mTransmitProgressDialog.dismiss();
                mService.removeFromUIHandler(mIncreaseProgress);
                mTransmitProgressDialog.setProgress(100);
                if (afterComplete != null && mService != null) {
                    mService.postDelayedOnUIHandler(new SafeRunnable() {
                        @Override
                        protected void safeRun() throws Exception {
                            afterComplete.run();
                        }
                    }, 200);
                }
            }
        });
    }

    private final SafeRunnable mIncreaseProgress = new SafeRunnable() {
        @Override
        protected void safeRun() throws Exception {
            T.UI();
            if (mTransmitProgressDialog.isShowing()) {
                int completenessLevel = (int) ((System.currentTimeMillis() - mTransmitStart) * 100 / MAX_TRANSMIT);
                if (completenessLevel < 100) {
                    mTransmitProgressDialog.setProgress(completenessLevel);
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
        String message = getString(R.string.action_scheduled);
        SafeDialogClick onPositiveClick = new SafeDialogClick() {
            @Override
            public void safeOnClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                if (mTransmitTimeoutRunnable != null)
                    mTransmitTimeoutRunnable.run();
            }
        };
        UIUtils.showDialog(ServiceBoundActivity.this, null, message, onPositiveClick);
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
            UIUtils.onActivityBound(mService);
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
        UIUtils.onActivityStop(this, mService);
    }

    @Override
    protected void onStart() {
        UIUtils.onActivityStart(this, mService);
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
        mActivityName = null;
        setContentView(getLayoutInflater().inflate(R.layout.navigation_view, null));

        mToolbar = findViewById(R.id.toolbar);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mToolbar.setOnClickListener(new SafeViewOnClickListener() {
            @Override
            public void safeOnClick(View v) {
                onToolbarClicked();
            }
        });

        LinearLayout item = findViewById(R.id.linear_layout);
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
        boolean showDrawer = intent.getBooleanExtra("show_drawer", false);
        mShowDrawerIcon = intent.getBooleanExtra("show_drawer_icon", false);

        if (showDrawer) {
            openNavigationView();
        }
        setHeaderImage();
        ListView menuItemListview = findViewById(R.id.navigation_items_listview);
        menuItemListview.setAdapter(mNavigationListViewAdapter);
        menuItemListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                NavigationItem navigationItem = (NavigationItem) parent.getItemAtPosition(position);
                simulateNavigationItemClick(navigationItem);
            }
        });

        showNavigationToolbar();

        if (mShowDrawerIcon) {
            setupBadges();
        } else {
            setNavigationBarBurgerVisible(false, true);
        }
    }

    private void showNavigationToolbar() {
        LinearLayout navigationFooter = findViewById(R.id.nav_view_footer);
        if (navigationFooter == null) {
            return;
        }

        GradientDrawable shape = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                new int[]{LookAndFeelConstants.getPrimaryColorDark(this), LookAndFeelConstants.getPrimaryColor(this)});
        shape.setShape(GradientDrawable.RECTANGLE);
        navigationFooter.setBackground(shape);

        NavigationItem[] navigationFooterItems = LookAndFeelConstants.getNavigationFooterItems(this);
        if (navigationFooterItems.length > 0) {
            navigationFooter.removeAllViews();
            LayoutInflater li = getLayoutInflater();
            for (final NavigationItem ni : navigationFooterItems) {
                View footerItem = li.inflate(R.layout.navigation_footer_item, navigationFooter, false);
                footerItem.setTag(ni.actionWithType());
                ImageButton imageButton = footerItem.findViewById(R.id.image);
                imageButton.setOnClickListener(new SafeViewOnClickListener() {
                    @Override
                    public void safeOnClick(View v) {
                        simulateNavigationItemClick(ni);
                    }
                });
                footerItem.setOnClickListener(new SafeViewOnClickListener() {
                    @Override
                    public void safeOnClick(View v) {
                        simulateNavigationItemClick(ni);
                    }
                });

                imageButton.setImageDrawable(ni.getFooterIcon(this));
                navigationFooter.addView(footerItem);
            }

            navigationFooter.setVisibility(View.VISIBLE);
        } else {
            navigationFooter.setVisibility(View.GONE);
        }
    }

    private void simulateNavigationItemClick(NavigationItem ni) {
        String activityName = ni.actionWithType();
        closeNavigationView();
        if (activityName.equals(mActivityName)) {
            return;
        }
        Bundle extras = new Bundle();
        extras.putBoolean("show_drawer_icon", true);
        extras.putString("title", ni.getLabel(this));
        ActivityUtils.goToActivity(ServiceBoundActivity.this, ni, true, extras);
    }

    private void setHeaderImage() {
        String kind = LookAndFeelConstants.getAssetKindOfHeaderImage();

        Bitmap background = SystemPlugin.getAppAsset(this, kind);
        ImageView headerImage = findViewById(R.id.nav_header);
        if (headerImage == null) {
            return;
        }
        if (background == null) {
            headerImage.setImageResource(R.drawable.homescreen_header);
        } else {
            headerImage.setImageBitmap(background);
        }
    }

    public SafeRunnable showMandatoryPermissionPopup(final Activity activity, final String permission) {
        return new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                    return;
                }
                String message = activity.getString(R.string.mandatory_permission);
                SafeDialogClick onPositiveClick = new SafeDialogClick() {
                    @Override
                    public void safeOnClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
                        intent.setData(uri);
                        activity.startActivity(intent);
                    }
                };
                UIUtils.showDialog(activity, null, message, R.string.go_to_app_settings, onPositiveClick,
                        R.string.cancel, null);
            }
        };
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
        if (mFBCallbackMgr != null) {
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

    public String getActivityName() {
        return mActivityName;
    }

    public void setActivityName(String activityName) {
        mActivityName = activityName;
        activateCurrentNavigationItem();
    }

    private void activateCurrentNavigationItem() {
        if (TextUtils.isEmptyOrWhitespace(mActivityName))
            return;

        LinearLayout navigationFooter = findViewById(R.id.nav_view_footer);
        if (navigationFooter == null)
            return;

        mNavigationListViewAdapter.notifyDataSetChanged();

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
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public void onToolbarClicked() {
        // override this method if you want to subscribe to this click
    }

    public void onNavigationHeaderImageClicked(View v) {
        closeNavigationView();

        Bundle extras = new Bundle();
        extras.putBoolean("show_drawer_icon", true);

        int homeActivityLayout = LookAndFeelConstants.getHomeActivityLayout(this);
        if (homeActivityLayout == R.layout.home_branding) {
            ActivityUtils.goToActivity(ServiceBoundActivity.this, "home_branding", true, false, extras);
        } else  if (homeActivityLayout == R.layout.messaging) {
            ActivityUtils.goToActivity(ServiceBoundActivity.this, "messages", true, false, extras);
        } else {
            ActivityUtils.goToActivity(ServiceBoundActivity.this, "news", true, false, extras);
        }
    }

    private void updateShortcutBadgeCount() {
        int count = 0;
        for (Long c : mService.badges.values()) {
            count += c.longValue();
        }
        try {
            ShortcutBadger.applyCountOrThrow(mService, count);
        } catch (ShortcutBadgeException ignored) {
            // Probably not supported, ignore error
        }
    }

    private void setupBadges() {
        if (mService == null)
            return;

        for (Map.Entry<String, Long> entry : mService.badges.entrySet()) {
            updateBadge(entry.getKey(), entry.getValue());
        }
        updateShortcutBadgeCount();
    }

    public void updateBadgeCount(String key, long count) {
        if (mService == null) {
            L.e("called updateBadgeCount to early service not ready yet");
            return;
        }
        mService.badges.put(key, count);
        updateBadge(key, count);
        updateShortcutBadgeCount();
    }

    public Long getBadgeCount(String key) {
        if (mBadgeCountNavigationDrawer.containsKey(key)) {
            return mBadgeCountNavigationDrawer.get(key);
        } else {
            return 0L;
        }
    }

    private void updateBadge(String key, long count) {
        if (!mShowDrawerIcon)
            return;

        mBadgeCountNavigationDrawer.put(key, count);
        mNavigationListViewAdapter.notifyDataSetChanged();

        if (mBadgePositionsNavigationFooter.containsKey(key)) {
            LinearLayout navigationFooter = findViewById(R.id.nav_view_footer);
            if (navigationFooter == null)
                return;

            int index = mBadgePositionsNavigationFooter.get(key);
            if (navigationFooter.getChildCount() <= index)
                return;

            TextView badge = navigationFooter.getChildAt(index).findViewById(R.id.badge);
            if (count > 0) {
                badge.setText(count > 9 ? "9+" : String.valueOf(count));
                badge.setTextColor(LookAndFeelConstants.getPrimaryColor(this));
                badge.setVisibility(View.VISIBLE);
            } else {
                badge.setVisibility(View.GONE);
            }
        }
    }

    public Toolbar getToolbar() {
        return mToolbar;
    }

    public boolean addIconToMenuItem(Menu menu, int itemId, FontAwesome.Icon faIcon) {
        MenuItem item = menu.findItem(itemId);
        if (item == null) {
            return false;
        }

        addIconToMenuItem(item, faIcon);
        return true;
    }

    public void addIconToMenuItem(MenuItem item, FontAwesome.Icon faIcon) {
        item.setIcon(new IconicsDrawable(this).icon(faIcon).color(Color.DKGRAY).sizeDp(18));
    }
}

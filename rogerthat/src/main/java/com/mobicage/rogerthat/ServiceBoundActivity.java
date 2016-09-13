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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.NavigationView;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.LinearLayout;
import android.widget.ProgressBar;
import com.facebook.CallbackManager;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;
import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.plugins.friends.MenuItemPressingActivity;
import com.mobicage.rogerthat.util.ActivityUtils;
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
import com.mobicage.rpc.config.CloudConstants;

public abstract class ServiceBoundActivity extends AppCompatActivity implements Pausable, ServiceBound, MenuItemPressingActivity {

    public static final long MAX_TRANSMIT = 10 * 1000;

    protected volatile MainService mService;
    protected boolean mServiceIsBound = false; // Owned by UI thread
    private boolean mPaused = false; // Owned by UI thread
    final private Queue<SafeRunnable> mWorkQueue = new LinkedList<SafeRunnable>();

    private final BroadcastReceiver closeActivityListener = new SafeBroadcastReceiver() {
        @Override
        public String[] onSafeReceive(Context context, Intent intent) {
            L.d("Received CLOSE_ACTIVITY_INTENT");
            finish();
            return null;
        }
    };
    private Dialog mTransmitProgressDialog;
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

    private ActionBarDrawerToggle mDrawerToggle;
    private String mActivityName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        T.setUIThread("ServiceBoundActivity.onCreate()");
        super.onCreate(savedInstanceState);
        logMethod("onCreate");
        SystemUtils.logIntentFlags(getIntent());
        IntentFilter filter = new IntentFilter(MainService.CLOSE_ACTIVITY_INTENT);
        registerReceiver(closeActivityListener, filter);
        doBindService();
        mTransmitProgressDialog = new Dialog(this);
        mTransmitProgressDialog.setContentView(R.layout.progressdialog);
        mTransmitProgressDialog.setTitle(R.string.transmitting);
        mTransmitProgressBar = (ProgressBar) mTransmitProgressDialog.findViewById(R.id.progress_bar);
        mTransmitProgressDialog.setCancelable(true);
        mTransmitProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                completeTransmit(null);
            }
        });
        mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    @Override
    protected void onDestroy() {
        T.UI();
        logMethod("onDestroy");
        super.onDestroy();
        unregisterReceiver(closeActivityListener);
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
        if (isVisible) {
            getDrawer().setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            mDrawerToggle.setDrawerIndicatorEnabled(true);
        } else {
            getDrawer().setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            mDrawerToggle.setDrawerIndicatorEnabled(false);
        }
    }

    public void setLastTimeClicked(final long ts) {
        mLastTimeClicked = ts;
    }

    public long getLastTimeClicked() {
        return mLastTimeClicked;
    }

    public void setContentViewWithoutNavigationBar(int layoutResID) {
        super.setContentView(layoutResID);
        TextUtils.overrideFonts(this, findViewById(android.R.id.content));
    }

    public void setContentViewWithoutNavigationBar(View view) {
        super.setContentView(view);
        TextUtils.overrideFonts(this, findViewById(android.R.id.content));
    }

    @Override
    public void setContentView(int layoutResID) {
        setContentView(getLayoutInflater().inflate(R.layout.navigation_view, null));

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.more_id));
        toolbar.setNavigationOnClickListener(new SafeViewOnClickListener() {
            @Override
            public void safeOnClick(View v) {
                L.w("test toolbar");
            }
        });
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        LinearLayout item = (LinearLayout) findViewById(R.id.linear_layout);
        View child = getLayoutInflater().inflate(layoutResID, null);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

        item.addView(child, layoutParams);

        final DrawerLayout drawer = getDrawer();
        mDrawerToggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string
                .navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        drawer.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(final MenuItem item) {
                int order = item.getOrder();
                String activityName = AppConstants.NAVIGATION_CLICKS[order];
                if (activityName != mActivityName) {
                    mService.postDelayedOnUIHandler(new SafeRunnable() {
                        @Override
                        protected void safeRun() throws Exception {
                            //item.setChecked(false);
                            activateCurrentNavigationItem();
                        }
                    }, 250);
                }

                if (AppConstants.NAVIGATION_CLICKS.length <= order) {
                    L.bug("ignoring navigation item clicked NAVIGATION_CLICKS.length <= order: " + order);
                } else if (AppConstants.NAVIGATION_CLICKS[order] != null) {
                    if (activityName != null && !activityName.equals(mActivityName)) {
                        ActivityUtils.goToActivity(ServiceBoundActivity.this, activityName, true);
                    }
                } else if (AppConstants.NAVIGATION_TAGS[order] != null) {
                    ActivityUtils.goToActivityBehindTag(ServiceBoundActivity.this, AppConstants.APP_EMAIL, AppConstants.NAVIGATION_TAGS[order]);
                } else {
                    L.bug("ignoring navigation item clicked for order: " + order);
                }
                closeNavigationView();
                return true;
            }
        });
        navigationView.setItemIconTintList(null);

        if (!CloudConstants.isCityApp()) {
            LinearLayout navigationFooter = (LinearLayout) findViewById(R.id.nav_view_footer);
            navigationFooter.setVisibility(View.GONE);
        }
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        TextUtils.overrideFonts(this, findViewById(android.R.id.content));
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(view, params);
        TextUtils.overrideFonts(this, findViewById(android.R.id.content));
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
        int order = Arrays.asList(AppConstants.NAVIGATION_CLICKS).indexOf(mActivityName);
        if (order >= 0) {
            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            navigationView.getMenu().getItem(order).setChecked(true);
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

    public void onOptionNavigationViewToolbarSelected(View v) {
        String activityName = (String) v.getTag();
        if (activityName != null && !activityName.equals(mActivityName)) {
            ActivityUtils.goToActivity(this, activityName, true);
        }
        closeNavigationView();
    }
}

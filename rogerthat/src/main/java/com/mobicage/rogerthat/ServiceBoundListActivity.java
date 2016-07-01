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

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.melnykov.fab.FloatingActionButton;
import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeBroadcastReceiver;
import com.mobicage.rogerthat.util.system.SafeViewOnClickListener;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.ui.UIUtils;

public abstract class ServiceBoundListActivity extends ListActivity implements ServiceBound {

    protected MainService mService; // Owned by UI thread
    protected boolean mServiceIsBound = false; // Owned by UI thread
    private FloatingActionButton mFAB;

    private BroadcastReceiver closeActivityListener = new SafeBroadcastReceiver() {
        @Override
        public String[] onSafeReceive(Context context, Intent intent) {
            L.d("Received CLOSE_ACTIVITY_INTENT");
            finish();
            return null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        T.setUIThread("ServiceBoundActivity.onCreate()");
        super.onCreate(savedInstanceState);
        IntentFilter filter = new IntentFilter(MainService.CLOSE_ACTIVITY_INTENT);
        registerReceiver(closeActivityListener, filter);
        doBindService();
    }

    @Override
    protected void onDestroy() {
        T.UI();
        L.d("Activity.onDestroy for " + getClass().getName());
        super.onDestroy();
        unregisterReceiver(closeActivityListener);
        doUnbindService();
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

    private void doBindService() {
        T.UI();
        if (!mServiceIsBound) {
            Intent intent = new Intent(this, MainService.class);
            intent.putExtra("clazz", this.getClass().getName());
            boolean success = getApplicationContext().bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
            L.d("doBindService : " + success);
        }
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
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
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            T.UI();
            L.bug("ServiceBoundActivity.onServiceDisconnected");
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // Since we use a local service, which is running
            // in our own process, we should never see this happen.
        }
    };

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

    protected View wrapViewWithFab(View view) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH || !showFABMenu())
            return view;

        FrameLayout frameLayout = new FrameLayout(this);
        frameLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
        frameLayout.addView(view);

        mFAB = new FloatingActionButton(this);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = getFabGravity();
        int m = UIUtils.convertDipToPixels(this, 16);
        layoutParams.setMargins(m, m, m, m);
        mFAB.setLayoutParams(layoutParams);
        mFAB.setImageResource(R.drawable.ic_menu);
        mFAB.setColorNormalResId(R.color.mc_homescreen_background);
        mFAB.setColorPressedResId(R.color.mc_homescreen_background);
        mFAB.setColorRipple(R.color.mc_homescreen_background);
        mFAB.setOnClickListener(new SafeViewOnClickListener() {
            @Override
            public void safeOnClick(View v) {
                openOptionsMenu();
            }
        });
        mFAB.setVisibility(View.VISIBLE);
        frameLayout.addView(mFAB);
        return frameLayout;
    }

    protected int getFabGravity() {
        return Gravity.BOTTOM | Gravity.RIGHT;
    }

    protected boolean showFABMenu() {
        return false;
    }

    @Override
    public void setContentView(int layoutResID) {
        setContentView(getLayoutInflater().inflate(layoutResID, null));
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(wrapViewWithFab(view));
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(wrapViewWithFab(view), params);
    }

}

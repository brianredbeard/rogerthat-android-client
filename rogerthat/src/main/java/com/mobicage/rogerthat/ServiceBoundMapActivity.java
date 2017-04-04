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

package com.mobicage.rogerthat;

import android.Manifest;
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
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.plugins.friends.Friend;
import com.mobicage.rogerthat.plugins.friends.FriendAvatar;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeBroadcastReceiver;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.system.SystemUtils;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.ui.Pausable;
import com.mobicage.rogerthat.util.ui.UIUtils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public abstract class ServiceBoundMapActivity extends AppCompatActivity implements Pausable, ServiceBound,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback,
        OnMyLocationButtonClickListener {

    /**
     * Request code for location permission request.
     *
     * @see #onRequestPermissionsResult(int, String[], int[])
     */
    protected static final int PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 1;
    public static final long MAX_TRANSMIT = 10 * 1000;

    private Drawable mUnknownAvatar;

    protected MainService mService; // Owned by UI thread
    protected boolean mServiceIsBound = false; // Owned by UI thread
    private boolean mPaused = false; // Owned by UI thread
    final private Queue<SafeRunnable> mWorkQueue = new LinkedList<>();
    private Map<Integer, SafeRunnable[]> mPermissionRequests = new HashMap<>();

    private BroadcastReceiver closeActivityListener = new SafeBroadcastReceiver() {
        @Override
        public String[] onSafeReceive(Context context, Intent intent) {
            L.d("Received CLOSE_ACTIVITY_INTENT");
            finish();
            return null;
        }
    };

    private ProgressDialog mTransmitProgressDialog;
    private long mTransmitStart = 0;

    private ConnectivityManager mConnectivityManager;

    public GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        T.setUIThread("ServiceBoundMapActivity.onCreate()");
        super.onCreate(savedInstanceState);
        logMethod("onCreate");
        SystemUtils.logIntentFlags(getIntent());
        IntentFilter filter = new IntentFilter(MainService.CLOSE_ACTIVITY_INTENT);
        registerReceiver(closeActivityListener, filter);
        mUnknownAvatar = getResources().getDrawable(R.drawable.unknown_avatar);
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
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onDestroy() {
        T.UI();
        logMethod("onDestroy");
        super.onDestroy();
        unregisterReceiver(closeActivityListener);
        doUnbindService();
    }

    public boolean isTransmitting() {
        T.UI();
        return mTransmitProgressDialog.isShowing();
    }

    public void completeTransmit(final SafeRunnable afterComplete) {
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
        UIUtils.showDialog(ServiceBoundMapActivity.this, null, message);
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
            L.d("doBindService : " + success);
        }
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            T.UI();
            mService = ((MainService.MainBinder) service).getService();
            UIUtils.onActivityBound(mService);
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

    protected BitmapDescriptor getAvatarBitmapDescriptor(Friend friend) {
        Drawable avatar;
        if (friend.avatar == null) {
            avatar = mUnknownAvatar;
        } else {
            avatar = FriendAvatar.getAvatar(this, friend);
        }
        return getBitmapDescriptor(avatar);

    }

    protected BitmapDescriptor getAvatarBitmapDescriptor(MyIdentity identity) {
        Drawable avatar;
        if (identity.getAvatar() == null) {
            avatar = mUnknownAvatar;
        } else {
            avatar = FriendAvatar.getAvatar(this, identity);
        }
        return getBitmapDescriptor(avatar);
    }

    private BitmapDescriptor getBitmapDescriptor(Drawable drawable) {
        Bitmap b = ((BitmapDrawable) drawable).getBitmap();
        return BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(b, 100, 100, false));
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
    protected void onPause() {
        T.UI();
        super.onPause();
        mPaused = true;
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
    public boolean getPaused() {
        T.UI();
        return mPaused;
    }

    @Override
    public void queue(SafeRunnable runnable) {
        T.UI();
        mWorkQueue.add(runnable);
    }

    protected void logMethod(String method) {
        L.d(getClass().getName() + "(ServiceBoundMapActivity)." + method);
    }

    @Override
    public MainService getMainService() {
        return mService;
    }

    @Override
    public void setContentView(int layoutResID) {
        setContentView(getLayoutInflater().inflate(layoutResID, null));
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(view, params);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST_ACCESS_FINE_LOCATION);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;

        mMap.setOnMyLocationButtonClickListener(this);
        enableMyLocation();
    }

}

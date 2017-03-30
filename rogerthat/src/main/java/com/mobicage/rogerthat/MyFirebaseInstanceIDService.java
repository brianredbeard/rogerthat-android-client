package com.mobicage.rogerthat;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.mobicage.rogerthat.plugins.scan.ScanTabActivity;
import com.mobicage.rogerthat.util.GoogleServicesUtils;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.system.SystemUtils;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.ui.UIUtils;

import java.util.ArrayList;
import java.util.List;


public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService implements ServiceBound {

    private volatile MainService mService;
    private boolean mServiceIsBound = false;
    private List<SafeRunnable> mOnServiceBoundRunnables;

    @Override
    public void onTokenRefresh() {
        SafeRunnable runnable = new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                T.UI();
                GoogleServicesUtils.registerFirebaseRegistrationId(mService);
            }
        };

        if (mServiceIsBound) {
            mService.postOnIOHandler(runnable);
        } else {
            addOnServiceBoundRunnable(runnable);
            doBindService();
        }
    }

    protected void logMethod(String method) {
        L.d(getClass().getName() + "." + method);
    }

    private void doBindService() {
        if (!mServiceIsBound) {
            Intent intent = new Intent(this, MainService.class);
            intent.putExtra("clazz", this.getClass().getName());
            boolean success = getApplicationContext().bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
            logMethod("doBindService: " + success);
        }
    }

    private void addOnServiceBoundRunnable(SafeRunnable runnable) {
        if (mOnServiceBoundRunnables == null) {
            mOnServiceBoundRunnables = new ArrayList<>();
        }
        mOnServiceBoundRunnables.add(runnable);
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            T.UI();
            mService = ((MainService.MainBinder) service).getService();
            mServiceIsBound = true;
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

    @Override
    public MainService getMainService() {
        return mService;
    }
}

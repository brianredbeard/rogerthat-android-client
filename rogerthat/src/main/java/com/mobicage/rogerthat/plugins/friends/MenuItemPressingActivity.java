package com.mobicage.rogerthat.plugins.friends;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;

import com.mobicage.rogerthat.ServiceBound;
import com.mobicage.rogerthat.util.system.SafeRunnable;

public interface MenuItemPressingActivity extends ServiceBound {
    void showTransmitting(SafeRunnable onTimeout);

    boolean isTransmitting();

    void completeTransmit(final SafeRunnable afterComplete);

    void showActionScheduledDialog();

    boolean checkConnectivity();

    boolean checkConnectivityIsWifi();
}

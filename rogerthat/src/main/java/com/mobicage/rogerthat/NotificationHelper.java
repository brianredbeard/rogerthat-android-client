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

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.util.TextUtils;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.T;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Helper class to manage notification channels, and create notifications.
 */
public class NotificationHelper extends ContextWrapper {
    private NotificationManager manager;
    private Context mContext;
    public static final String NOTIFICATION_TIMESTAMP = "NOTIFICATION_TIMESTAMP";
    private static Map<Object, Integer> notificationIds = new HashMap<>();

    /**
     * Registers notification channels, which can be used later by individual notifications.
     *
     * @param ctx The application context
     */
    public NotificationHelper(Context ctx) {
        super(ctx);
        this.mContext = ctx;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel defaultChannel = new NotificationChannel(NotificationChannelId.DEFAULT.toString(),
                    getString(R.string.notifications), NotificationManager.IMPORTANCE_HIGH);
            getManager().createNotificationChannel(defaultChannel);

            NotificationChannel newsChannel = new NotificationChannel(NotificationChannelId.NEWS.toString(),
                    getString(R.string.news), NotificationManager.IMPORTANCE_DEFAULT);
            getManager().createNotificationChannel(newsChannel);
            NotificationChannel chatMessagesChannel = new NotificationChannel(NotificationChannelId.CHAT_MESSAGE.toString(),
                    getString(R.string.tab_messaging), NotificationManager.IMPORTANCE_MAX);
            getManager().createNotificationChannel(chatMessagesChannel);
        }
    }

    /**
     * Get the notification manager.
     * <p>
     * Utility method as this helper works with it a lot.
     *
     * @return The system service NotificationManager
     */
    private NotificationManager getManager() {
        if (manager == null) {
            manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return manager;
    }

    public void doNotification(String title, String message, int notificationId, String action,
                               boolean withSound, boolean withVibration, boolean withLight, boolean autoCancel,
                               int icon, int notificationNumber, Bundle extras, String tickerText,
                               long timestamp, int priority, List<NotificationCompat.Action> actionButtons,
                               String longNotificationText, Bitmap largeIcon, String category, NotificationChannelId channelId) {
        T.dontCare();
        SharedPreferences options = PreferenceManager.getDefaultSharedPreferences(mContext);
        final boolean pushNotifications = options.getBoolean(MainService.PREFERENCE_PUSH_NOTIFICATIONS, false);
        if (!pushNotifications) {
            L.d("push notifications are disabled");
            return;
        }

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, channelId.toString());
        int defaults = 0;
        if (withSound) {
            defaults |= Notification.DEFAULT_SOUND;
        }
        if (withVibration) {
            defaults |= Notification.DEFAULT_VIBRATE;
        }
        builder.setDefaults(defaults);

        if (withLight) {
            builder.setLights(0xff00ff00, 300, 3000);
        }

        builder.setAutoCancel(autoCancel);
        builder.setOngoing(!autoCancel);
        builder.setCategory(category);

        if (message != null) {
            builder.setSmallIcon(icon);
            if (largeIcon != null) {
                builder.setLargeIcon(largeIcon);
            }
            if (tickerText != null) {
                builder.setTicker(tickerText);
            }
            builder.setWhen(System.currentTimeMillis());
            final Intent intent = new Intent(action, null, mContext, MainActivity.class);
            intent.addFlags(MainActivity.FLAG_CLEAR_STACK_SINGLE_TOP);
            if (extras != null)
                intent.putExtras(extras);
            intent.putExtra(NOTIFICATION_TIMESTAMP, timestamp);

            final PendingIntent pi = PendingIntent.getActivity(mContext, NotificationID.next(), intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            builder.setContentIntent(pi);
            builder.setContentText(message);
            builder.setContentTitle(title);
            if (!TextUtils.isEmptyOrWhitespace(longNotificationText)) {
                builder.setStyle(new NotificationCompat.BigTextStyle().bigText(longNotificationText));
            }
            builder.setPriority(priority);
            if (actionButtons != null) {
                for (NotificationCompat.Action actionButton : actionButtons) {
                    builder.addAction(actionButton);
                }
            }
        }

        builder.setNumber(notificationNumber);

        getManager().notify(notificationId, builder.build());
    }

    public int getNotificationId(final Object notificationKey, boolean createIfNotExists) {
        if (notificationIds.containsKey(notificationKey)) {
            if (createIfNotExists) {
                return notificationIds.get(notificationKey);
            } else {
                return notificationIds.remove(notificationKey);
            }
        }
        if (!createIfNotExists) {
            return -1;
        }
        int notificationID = NotificationID.next();
        notificationIds.put(notificationKey, notificationID);
        return notificationID;
    }

    public void cancelNotification(final int pNotificationId) {
        T.dontCare();
        getManager().cancel(pNotificationId);
    }

    public void cancelNotification(final Object notificationKey) {
        int notificationId = getNotificationId(notificationKey, false);
        if (notificationId != -1) {
            cancelNotification(notificationId);
        }
    }
}

class NotificationID {
    private final static AtomicInteger c = new AtomicInteger(1000);

    public static int next() {
        return c.incrementAndGet();
    }
}

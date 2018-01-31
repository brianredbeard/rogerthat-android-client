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

package com.mobicage.rogerthat.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.MainActivity;

public class RogerthatWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.rogerthat_widget);

        remoteViews.setOnClickPendingIntent(R.id.btn_launch_app,
            createPendingIntent(context, MainActivity.ACTION_WIDGET_MAIN));

        remoteViews.setOnClickPendingIntent(R.id.btn_scan_qr,
            createPendingIntent(context, MainActivity.ACTION_WIDGET_SCAN));

        remoteViews.setOnClickPendingIntent(R.id.btn_send_msg,
            createPendingIntent(context, MainActivity.ACTION_WIDGET_COMPOSE));

        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
    }

    private PendingIntent createPendingIntent(Context context, String action) {
        Intent i = new Intent(action, null, context, MainActivity.class);
        return PendingIntent.getActivity(context, 1000, i, 0);
    }

}

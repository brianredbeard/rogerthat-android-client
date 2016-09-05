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

package com.mobicage.rogerthat.util;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import com.mobicage.rogerthat.MoreActivity;
import com.mobicage.rogerthat.ServiceFriendsActivity;
import com.mobicage.rogerthat.SettingsActivity;
import com.mobicage.rogerthat.UserFriendsActivity;
import com.mobicage.rogerthat.plugins.friends.FriendSearchActivity;
import com.mobicage.rogerthat.plugins.friends.FriendStore;
import com.mobicage.rogerthat.plugins.friends.ServiceActionMenuActivity;
import com.mobicage.rogerthat.plugins.messaging.MessagingActivity;
import com.mobicage.rogerthat.plugins.scan.ProfileActivity;
import com.mobicage.rogerthat.plugins.scan.ScanTabActivity;
import com.mobicage.rogerthat.util.logging.L;

public class ActivityUtils {

    public static void goToActivity(Context context, String activityName) {
        // todo ruben implement collapse
        if ("messages".equals(activityName)) {
            goToMessagingActivity(context);
        } else if ("scan".equals(activityName)) {
            goToScanActivity(context);
        } else if ("services".equals(activityName)) {
            goToServicesActivity(context, FriendStore.SERVICE_ORGANIZATION_TYPE_UNSPECIFIED);
        } else if ("friends".equals(activityName)) {
            goToUserFriendsActivity(context);
        } else if ("directory".equals(activityName)) {
            goToFriendSearchActivity(context);
        } else if ("profile".equals(activityName)) {
            goToProfileActivity(context);
        } else if ("more".equals(activityName)) {
            goToMoreActivity(context);
        } else if ("settings".equals(activityName)) {
            goToSettingsActivity(context);
        } else if ("community_services".equals(activityName)) {
            goToServicesActivity(context, FriendStore.SERVICE_ORGANIZATION_TYPE_CITY);
        } else if ("merchants".equals(activityName)) {
            goToServicesActivity(context, FriendStore.SERVICE_ORGANIZATION_TYPE_PROFIT);
        } else if ("associations".equals(activityName)) {
            goToServicesActivity(context, FriendStore.SERVICE_ORGANIZATION_TYPE_NON_PROFIT);
        } else if ("emergency_services".equals(activityName)) {
            goToServicesActivity(context, FriendStore.SERVICE_ORGANIZATION_TYPE_EMERGENCY);
        } else {
            L.bug("unknown goToActivity: " + activityName);
        }
    }

    public static void goToMessagingActivity(Context context) {
        Intent i = new Intent(context, MessagingActivity.class);
        context.startActivity(i);
    }

    public static void goToUserFriendsActivity(Context context) {
        Intent launchIntent = new Intent(context, UserFriendsActivity.class);
        context.startActivity(launchIntent);
    }

    public static void goToServicesActivity(Context context, int organizationType) {
        final Intent launchIntent = new Intent(context, ServiceFriendsActivity.class);
        launchIntent.putExtra(ServiceFriendsActivity.ORGANIZATION_TYPE, organizationType);
        context.startActivity(launchIntent);
    }

    public static void goToScanActivity(Context context) {
        Intent launchIntent = new Intent(context, ScanTabActivity.class);
        context.startActivity(launchIntent);
    }

    public static void goToMoreActivity(Context context) {
        final Intent launchIntent = new Intent(context, MoreActivity.class);
        context.startActivity(launchIntent);
    }

    public static void goToProfileActivity(Context context) {
        final Intent launchIntent = new Intent(context, ProfileActivity.class);
        context.startActivity(launchIntent);
    }

    public static void goToFriendSearchActivity(Context context) {
        final Intent serviceSearch = new Intent(context, FriendSearchActivity.class);
        context.startActivity(serviceSearch);
    }

    public static void goToSettingsActivity(Context context) {
        final Intent launchIntent = new Intent(context, SettingsActivity.class);
        context.startActivity(launchIntent);
    }
}

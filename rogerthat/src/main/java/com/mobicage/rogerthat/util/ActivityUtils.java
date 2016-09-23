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

import com.mobicage.rogerthat.MainActivity;
import com.mobicage.rogerthat.MoreActivity;
import com.mobicage.rogerthat.NewsActivity;
import com.mobicage.rogerthat.QRCodeActivity;
import com.mobicage.rogerthat.ServiceBoundActivity;
import com.mobicage.rogerthat.ServiceFriendsActivity;
import com.mobicage.rogerthat.SettingsActivity;
import com.mobicage.rogerthat.UserFriendsActivity;
import com.mobicage.rogerthat.plugins.friends.FriendSearchActivity;
import com.mobicage.rogerthat.plugins.friends.FriendStore;
import com.mobicage.rogerthat.plugins.friends.FriendsPlugin;
import com.mobicage.rogerthat.plugins.friends.MenuItemPresser;
import com.mobicage.rogerthat.plugins.friends.MenuItemPressingActivity;
import com.mobicage.rogerthat.plugins.friends.ServiceActionMenuActivity;
import com.mobicage.rogerthat.plugins.history.HistoryListActivity;
import com.mobicage.rogerthat.plugins.messaging.MessagingActivity;
import com.mobicage.rogerthat.plugins.scan.ProfileActivity;
import com.mobicage.rogerthat.plugins.scan.ScanTabActivity;
import com.mobicage.rogerthat.util.logging.L;

public class ActivityUtils {

    public static void goToActivity(Context context, Class clazz, boolean clearStack) {
        Intent i = new Intent(context, clazz);
        if (clearStack) {
            i.addFlags(MainActivity.FLAG_CLEAR_STACK);
        }
        context.startActivity(i);
    }

    public static boolean goToActivity(ServiceBoundActivity context, String activityName, boolean clearStack, boolean collapse) {
        if ("news".equals(activityName)) {
            goToActivity(context, NewsActivity.class, clearStack);
        } else if ("messages".equals(activityName)) {
            goToActivity(context, MessagingActivity.class, clearStack);
        } else if ("scan".equals(activityName)) {
            goToActivity(context, ScanTabActivity.class, clearStack);
        } else if ("services".equals(activityName)) {
            goToServicesActivity(context, FriendStore.SERVICE_ORGANIZATION_TYPE_UNSPECIFIED, clearStack, collapse);
        } else if ("friends".equals(activityName)) {
            goToUserFriendsActivity(context, clearStack);
        } else if ("directory".equals(activityName)) {
            goToFriendSearchActivity(context, clearStack);
        } else if ("profile".equals(activityName)) {
            goToActivity(context, ProfileActivity.class, clearStack);
        } else if ("more".equals(activityName)) {
            goToActivity(context, MoreActivity.class, clearStack);
        } else if ("settings".equals(activityName)) {
            goToActivity(context, SettingsActivity.class, clearStack);
        } else if ("community_services".equals(activityName)) {
            goToServicesActivity(context, FriendStore.SERVICE_ORGANIZATION_TYPE_CITY, clearStack, collapse);
        } else if ("merchants".equals(activityName)) {
            goToServicesActivity(context, FriendStore.SERVICE_ORGANIZATION_TYPE_PROFIT, clearStack, collapse);
        } else if ("associations".equals(activityName)) {
            goToServicesActivity(context, FriendStore.SERVICE_ORGANIZATION_TYPE_NON_PROFIT, clearStack, collapse);
        } else if ("emergency_services".equals(activityName)) {
            goToServicesActivity(context, FriendStore.SERVICE_ORGANIZATION_TYPE_EMERGENCY, clearStack, collapse);
        } else if ("stream".equals(activityName)) {
            goToActivity(context, HistoryListActivity.class, clearStack);
        } else if ("qrcode".equals(activityName)) {
            goToActivity(context, QRCodeActivity.class, clearStack);
        } else {
            L.bug("unknown goToActivity: " + activityName);
            return false;
        }
        return true;
    }

    public static void goToMessagingActivity(Context context, boolean clearStack) {
        goToActivity(context, MessagingActivity.class, clearStack);
    }

    public static void goToUserFriendsActivity(Context context, boolean clearStack) {
        goToActivity(context, UserFriendsActivity.class, clearStack);
    }

    public static void goToServicesActivity(final ServiceBoundActivity context, int organizationType, boolean clearStack, boolean collapse) {
        if (collapse) {
            FriendStore friendStore = context.getMainService().getPlugin(FriendsPlugin.class).getStore();
            long count = 0;
            if (organizationType == FriendStore.SERVICE_ORGANIZATION_TYPE_UNSPECIFIED) {
                count = friendStore.countServices();
            } else {
                count = friendStore.countServicesByOrganizationType(organizationType);
            }
            if (count == 1) {
                Cursor cursor = friendStore.getServiceFriendListCursor(organizationType);
                if (cursor.moveToFirst()) {
                    String serviceEmail = cursor.getString(1);
                    Intent intent = new Intent(context, ServiceActionMenuActivity.class);
                    intent.putExtra(ServiceActionMenuActivity.SERVICE_EMAIL, serviceEmail);
                    intent.putExtra(ServiceActionMenuActivity.MENU_PAGE, 0);
                    context.startActivity(intent);
                    return;
                }
            }
        }

        final Intent i = new Intent(context, ServiceFriendsActivity.class);
        i.putExtra(ServiceFriendsActivity.ORGANIZATION_TYPE, organizationType);
        if (clearStack) {
            i.addFlags(MainActivity.FLAG_CLEAR_STACK);
        }
        context.startActivity(i);
    }

    public static void goToScanActivity(Context context, boolean clearStack) {
        goToActivity(context, ScanTabActivity.class, clearStack);
    }

    public static void goToMoreActivity(Context context, boolean clearStack) {
        goToActivity(context, MoreActivity.class, clearStack);
    }

    public static void goToProfileActivity(Context context, boolean clearStack) {
        goToActivity(context, ProfileActivity.class, clearStack);
    }

    public static void goToFriendSearchActivity(Context context, boolean clearStack) {
        goToActivity(context, FriendSearchActivity.class, clearStack);
    }

    public static void goToActivityBehindTag(final ServiceBoundActivity context, final String serviceEmail, final String tag) {
        L.d("goToActivityBehindTag called with context: " + context);

        if (context instanceof MenuItemPressingActivity) {
            MenuItemPresser menuItemPresser = new MenuItemPresser(context, serviceEmail);
            menuItemPresser.itemPressed(tag, new MenuItemPresser.ResultHandler() {
                @Override
                public void onError() {
                    L.e("SMI with tag " + tag + " not found!"); // XXX: log error to message.sender
                    onTimeout();
                }
            });
        } else {
            L.bug("goToActivityBehindTag called from wrong context: " + context);
        }
    }
}

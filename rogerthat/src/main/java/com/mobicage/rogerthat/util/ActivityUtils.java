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
import android.net.Uri;
import android.os.Bundle;
import android.os.ResultReceiver;

import com.commonsware.cwac.cam2.AbstractCameraActivity;
import com.commonsware.cwac.cam2.CameraActivity;
import com.commonsware.cwac.cam2.Facing;
import com.commonsware.cwac.cam2.FlashMode;
import com.commonsware.cwac.cam2.VideoRecorderActivity;
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

    public static void goToActivity(Context context, Class clazz, boolean clearStack, Bundle extras) {
        Intent i = new Intent(context, clazz);
        if (clearStack) {
            i.addFlags(MainActivity.FLAG_CLEAR_STACK);
        }
        if (extras != null) {
            i.putExtras(extras);
        }
        context.startActivity(i);
    }

    public static boolean goToActivity(ServiceBoundActivity context, String activityName, boolean clearStack, boolean collapse, Bundle extras) {
        if ("news".equals(activityName)) {
            goToActivity(context, NewsActivity.class, clearStack, extras);
        } else if ("messages".equals(activityName)) {
            goToActivity(context, MessagingActivity.class, clearStack, extras);
        } else if ("scan".equals(activityName)) {
            goToActivity(context, ScanTabActivity.class, clearStack, extras);
        } else if ("services".equals(activityName)) {
            goToServicesActivity(context, FriendStore.SERVICE_ORGANIZATION_TYPE_UNSPECIFIED, clearStack, collapse, extras);
        } else if ("friends".equals(activityName)) {
            goToUserFriendsActivity(context, clearStack, extras);
        } else if ("directory".equals(activityName)) {
            goToFriendSearchActivity(context, clearStack, extras);
        } else if ("profile".equals(activityName)) {
            goToActivity(context, ProfileActivity.class, clearStack, extras);
        } else if ("more".equals(activityName)) {
            goToActivity(context, MoreActivity.class, clearStack, extras);
        } else if ("settings".equals(activityName)) {
            goToActivity(context, SettingsActivity.class, clearStack, extras);
        } else if ("community_services".equals(activityName)) {
            goToServicesActivity(context, FriendStore.SERVICE_ORGANIZATION_TYPE_CITY, clearStack, collapse, extras);
        } else if ("merchants".equals(activityName)) {
            goToServicesActivity(context, FriendStore.SERVICE_ORGANIZATION_TYPE_PROFIT, clearStack, collapse, extras);
        } else if ("associations".equals(activityName)) {
            goToServicesActivity(context, FriendStore.SERVICE_ORGANIZATION_TYPE_NON_PROFIT, clearStack, collapse, extras);
        } else if ("emergency_services".equals(activityName)) {
            goToServicesActivity(context, FriendStore.SERVICE_ORGANIZATION_TYPE_EMERGENCY, clearStack, collapse, extras);
        } else if ("stream".equals(activityName)) {
            goToActivity(context, HistoryListActivity.class, clearStack, extras);
        } else if ("qrcode".equals(activityName)) {
            goToActivity(context, QRCodeActivity.class, clearStack, extras);
        } else {
            L.bug("unknown goToActivity: " + activityName);
            return false;
        }
        return true;
    }

    public static void goToMessagingActivity(Context context, boolean clearStack, Bundle extras) {
        goToActivity(context, MessagingActivity.class, clearStack, extras);
    }

    public static void goToUserFriendsActivity(Context context, boolean clearStack, Bundle extras) {
        goToActivity(context, UserFriendsActivity.class, clearStack, extras);
    }

    public static void goToServicesActivity(final ServiceBoundActivity context, int organizationType, boolean clearStack, boolean collapse, Bundle extras) {
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
                    if (clearStack) {
                        intent.addFlags(MainActivity.FLAG_CLEAR_STACK);
                    }
                    if (extras != null) {
                        intent.putExtras(extras);
                    }
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
        if (extras != null) {
            i.putExtras(extras);
        }
        context.startActivity(i);
    }

    public static void goToScanActivity(Context context, boolean clearStack, Bundle extras) {
        goToActivity(context, ScanTabActivity.class, clearStack, extras);
    }

    public static void goToMoreActivity(Context context, boolean clearStack, Bundle extras) {
        goToActivity(context, MoreActivity.class, clearStack, extras);
    }

    public static void goToProfileActivity(Context context, boolean clearStack, Bundle extras) {
        goToActivity(context, ProfileActivity.class, clearStack, extras);
    }

    public static void goToFriendSearchActivity(Context context, boolean clearStack, Bundle extras) {
        goToActivity(context, FriendSearchActivity.class, clearStack, extras);
    }

    private static void setupMenuItemPresser(final ServiceBoundActivity context, final String serviceEmail) {
        if (context.menuItemPresser != null) {
            context.menuItemPresser.stop();
            context.menuItemPresser = null;
        }
        context.menuItemPresser = new MenuItemPresser(context, serviceEmail);
    }

    public static boolean goToActivityBehindTagWhenReady(final ServiceBoundActivity context, final String serviceEmail, final String tag) {
        if (context instanceof MenuItemPressingActivity) {
            setupMenuItemPresser(context, serviceEmail);

            if (!context.menuItemPresser.itemReady(tag)) {
                return false;
            }

            context.menuItemPresser.itemPressed(tag, new MenuItemPresser.ResultHandler() {
                @Override
                public void onError() {
                    L.e("SMI with tag " + tag + " not found!"); // XXX: log error to message.sender
                    onTimeout();
                }
            });
            return true;
        } else {
            L.bug("goToActivityBehindTag called from wrong context: " + context);
            return false;
        }
    }

    public static void goToActivityBehindTag(final ServiceBoundActivity context, final String serviceEmail, final String tag) {
        if (context instanceof MenuItemPressingActivity) {
            setupMenuItemPresser(context, serviceEmail);

            context.menuItemPresser.itemPressed(tag, new MenuItemPresser.ResultHandler() {
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

    public static Intent buildTakePictureIntent(Context context, Uri saveTo, Facing facing) {
        return new CameraActivity.IntentBuilder(context)
                .allowSwitchFlashMode()
                .facing(facing)
                .to(saveTo)
                .flashModes(FlashMode.values())
                .updateMediaStore()
                .onError(new ResultReceiver(null) {
                    protected void onReceiveResult (int resultCode,
                                                    Bundle resultData) {
                        L.bug("Failure in CameraActivity.\nError: r" + resultData.toString());
                    }
                })
                .skipOrientationNormalization()
                .skipConfirm()
                .build();
    }

    public static Intent buildMakeVideoIntent(Context context, Uri saveTo) {
        return new VideoRecorderActivity.IntentBuilder(context)
                .facing(Facing.BACK)
                .to(saveTo)
                .updateMediaStore()
                .quality(AbstractCameraActivity.Quality.LOW)
                .onError(new ResultReceiver(null) {
                    protected void onReceiveResult (int resultCode,
                                                    Bundle resultData) {
                        L.bug("Failure in CameraActivity.\nError: r" + resultData.toString());
                    }
                })
                .build();
    }
}

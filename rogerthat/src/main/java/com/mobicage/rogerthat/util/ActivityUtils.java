/*
 * Copyright 2017 Mobicage NV
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
 * @@license_version:1.2@@
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
import com.mobicage.rogerthat.MainService;
import com.mobicage.rogerthat.MoreActivity;
import com.mobicage.rogerthat.NavigationItem;
import com.mobicage.rogerthat.NewsActivity;
import com.mobicage.rogerthat.QRCodeActivity;
import com.mobicage.rogerthat.QRCodesActivity;
import com.mobicage.rogerthat.ServiceActionsOfflineActivity;
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
import com.mobicage.rogerthat.plugins.friends.ServiceMenuItemDetails;
import com.mobicage.rogerthat.plugins.friends.ServiceSearchActivity;
import com.mobicage.rogerthat.plugins.history.HistoryListActivity;
import com.mobicage.rogerthat.plugins.messaging.MessagingActivity;
import com.mobicage.rogerthat.plugins.scan.ProfileActivity;
import com.mobicage.rogerthat.plugins.scan.ScanTabActivity;
import com.mobicage.rogerthat.plugins.system.SystemPlugin;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rpc.config.AppConstants;

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

    public static String canOpenNavigationItem(ServiceBoundActivity context, NavigationItem ni) {
        if (ni.actionType == null) {
            if ("news".equals(ni.action)) {
            } else if ("messages".equals(ni.action)) {
            } else if ("scan".equals(ni.action)) {
            } else if ("services".equals(ni.action)) {
            } else if ("friends".equals(ni.action)) {
            } else if ("directory".equals(ni.action)) {
            } else if ("profile".equals(ni.action)) {
            } else if ("more".equals(ni.action)) {
            } else if ("settings".equals(ni.action)) {
            } else if ("community_services".equals(ni.action)) {
            } else if ("merchants".equals(ni.action)) {
            } else if ("associations".equals(ni.action)) {
            } else if ("emergency_services".equals(ni.action)) {
            } else if ("stream".equals(ni.action)) {
            } else if ("qrcode".equals(ni.action)) {
            } else {
                return "Unknown action";
            }
        } else if ("action".equals(ni.actionType)) {
        } else if ("click".equals(ni.actionType)) {
            if (TextUtils.isEmptyOrWhitespace(AppConstants.APP_EMAIL)) {
                return "Unknown email";
            }
            final FriendStore friendStore = context.getMainService().getPlugin(FriendsPlugin.class).getStore();
            final ServiceMenuItemDetails smi = friendStore.getMenuItem(AppConstants.APP_EMAIL, ni.action);
            if (smi == null) {
                return "ServiceMenuItem not found";
            }
        } else {
            return "Unknown action_type";
        }
        return null;
    }

    public static boolean goToActivity(final ServiceBoundActivity context, final NavigationItem ni, final boolean clearStack, final Bundle extras) {
        if (ni.actionType == null) {
            ActivityUtils.goToActivity(context, ni.action, clearStack, ni.collapse, extras);
        } else if ("action".equals(ni.actionType)) {
            Class clazz;
            if (context.getMainService().getNetworkConnectivityManager().isConnected()) {
                clazz = ServiceSearchActivity.class;
            } else {
                clazz = ServiceActionsOfflineActivity.class;
            }

            final Intent i = new Intent(context, clazz);
            extras.putString(ServiceActionsOfflineActivity.ACTION, ni.action);
            if (ni.labelTextId > 0) {
                extras.putString(ServiceActionsOfflineActivity.TITLE, context.getString(ni.labelTextId));
            } else {
                extras.putString(ServiceActionsOfflineActivity.TITLE, ni.labelText);
            }

            i.putExtras(extras);
            i.addFlags(MainActivity.FLAG_CLEAR_STACK);
            context.startActivity(i);

        } else if ("click".equals(ni.actionType)) {

            final String serviceEmail;
            if (!TextUtils.isEmptyOrWhitespace(ni.serviceEmail)) {
                serviceEmail = ni.serviceEmail;
            } else if (!TextUtils.isEmptyOrWhitespace(AppConstants.APP_EMAIL)) {
                serviceEmail = AppConstants.APP_EMAIL;
            } else {
                L.bug("simulateNavigationItemClick click but AppConstants.APP_EMAIL was empty");
                return false;
            }

            context.runOnUiThread(new SafeRunnable() {
                @Override
                protected void safeRun() throws Exception {
                    ActivityUtils.goToActivityBehindTag(context, serviceEmail, ni.action, extras);
                }
            });

        } else {
            L.bug("ignoring simulateNavigationItemClick: " + ni.actionType + "|" + ni.action);
            return false;
        }
        return true;
    }

    public static boolean goToActivity(ServiceBoundActivity context, String activityName, boolean clearStack,
                                       boolean collapse, Bundle extras) {
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
            goToQRActivity(context, clearStack, extras);
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

    private static void goToQRActivity(ServiceBoundActivity context, boolean clearStack, Bundle extras) {
        Class<?> cls = QRCodeActivity.class;
        MainService mainService = context.getMainService();
        if (mainService != null) {
            final SystemPlugin systemPlugin = mainService.getPlugin(SystemPlugin.class);
            if (systemPlugin.listQRCodes().size() > 0) {
                cls = QRCodesActivity.class;
            }
        }
        goToActivity(context, cls, clearStack, extras);
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
        goToActivityBehindTag(context, serviceEmail, tag, null);
    }

    public static void goToActivityBehindTag(final ServiceBoundActivity context, final String serviceEmail, final String tag, Bundle extras) {
        if (context instanceof MenuItemPressingActivity) {
            setupMenuItemPresser(context, serviceEmail);

            context.menuItemPresser.itemPressed(tag, extras, null, new MenuItemPresser.ResultHandler() {
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
                    protected void onReceiveResult(int resultCode,
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
                    protected void onReceiveResult(int resultCode,
                                                   Bundle resultData) {
                        L.bug("Failure in CameraActivity.\nError: r" + resultData.toString());
                    }
                })
                .build();
    }
}

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

package com.mobicage.rogerthat.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.customtabs.CustomTabsIntent;

import com.commonsware.cwac.cam2.AbstractCameraActivity;
import com.commonsware.cwac.cam2.CameraActivity;
import com.commonsware.cwac.cam2.Facing;
import com.commonsware.cwac.cam2.FlashMode;
import com.commonsware.cwac.cam2.VideoRecorderActivity;
import com.mobicage.rogerthat.HomeBrandingActivity;
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
import com.mobicage.rogerthat.cordova.CordovaActionScreenActivity;
import com.mobicage.rogerthat.cordova.CordovaSettings;
import com.mobicage.rogerthat.plugins.friends.FriendSearchActivity;
import com.mobicage.rogerthat.plugins.friends.FriendStore;
import com.mobicage.rogerthat.plugins.friends.FriendsPlugin;
import com.mobicage.rogerthat.plugins.friends.MenuItemPresser;
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActivityUtils {

    private static final Map<String, Integer> SERVICE_ACTIONS = new HashMap<>();
    private static final Map<String, Class<?>> SIMPLE_ACTIONS = new HashMap<>();
    private static final List<String> COMPLEX_ACTIONS = Arrays.asList("qrcode");
    public static final List<String> CUSTOM_TABS_SCHEMES = Arrays.asList("http", "https", "tel", "sms", "mailto");

    static {
        SIMPLE_ACTIONS.put("news", NewsActivity.class);
        SIMPLE_ACTIONS.put("messages", MessagingActivity.class);
        SIMPLE_ACTIONS.put("scan", ScanTabActivity.class);
        SIMPLE_ACTIONS.put("friends", UserFriendsActivity.class);
        SIMPLE_ACTIONS.put("profile", ProfileActivity.class);
        SIMPLE_ACTIONS.put("more", MoreActivity.class);
        SIMPLE_ACTIONS.put("settings", SettingsActivity.class);
        SIMPLE_ACTIONS.put("stream", HistoryListActivity.class);
        SIMPLE_ACTIONS.put("directory", FriendSearchActivity.class);
        SIMPLE_ACTIONS.put("home_branding", HomeBrandingActivity.class);

        SERVICE_ACTIONS.put("services", FriendStore.SERVICE_ORGANIZATION_TYPE_UNSPECIFIED);
        SERVICE_ACTIONS.put("community_services", FriendStore.SERVICE_ORGANIZATION_TYPE_CITY);
        SERVICE_ACTIONS.put("merchants", FriendStore.SERVICE_ORGANIZATION_TYPE_PROFIT);
        SERVICE_ACTIONS.put("associations", FriendStore.SERVICE_ORGANIZATION_TYPE_NON_PROFIT);
        SERVICE_ACTIONS.put("emergency_services", FriendStore.SERVICE_ORGANIZATION_TYPE_EMERGENCY);
    }

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
            if (!SIMPLE_ACTIONS.containsKey(ni.action)
                    && !SERVICE_ACTIONS.containsKey(ni.action)
                    && !COMPLEX_ACTIONS.contains(ni.action)) {
                return "Unknown action: " + ni.action;
            }
        } else if ("action".equals(ni.actionType)) {
        } else if ("cordova".equals(ni.actionType)) {
            if (!CordovaSettings.APPS.contains(ni.action)) {
                return "Unknown cordova-based app: " + ni.action;
            }
        } else if ("click".equals(ni.actionType)) {
            final String serviceEmail;
            if (!TextUtils.isEmptyOrWhitespace(ni.serviceEmail)) {
                serviceEmail = ni.serviceEmail;
            } else if (!TextUtils.isEmptyOrWhitespace(AppConstants.APP_EMAIL)) {
                serviceEmail = AppConstants.APP_EMAIL;
            } else {
                return "Unknown email";
            }
            final FriendStore friendStore = context.getMainService().getPlugin(FriendsPlugin.class).getStore();
            final ServiceMenuItemDetails smi = friendStore.getMenuItem(serviceEmail, ni.action);
            if (smi == null) {
                return "ServiceMenuItem not found";
            }
        } else if ("open".equals(ni.actionType)) {
            if ("app".equals(ni.action)) {
                String packageName = (String) ni.getParam("android_app_id");
                if (TextUtils.isEmptyOrWhitespace(packageName)) {
                    return "App id not provided";
                }
            } else {
                return "Unknown action";
            }
        } else {
            return "Unknown action_type: " + ni.actionType;
        }
        return null;
    }

    public static boolean goToActivity(final ServiceBoundActivity context, final NavigationItem ni, final boolean clearStack, final Bundle extras) {
        if (ni.actionType == null) {
            if (ni.action.equals("news")){
                extras.putString("feed_name", ni.feedName());
            }
            ActivityUtils.goToActivity(context, ni.action, clearStack, ni.isCollapsible(), extras);
        } else if ("action".equals(ni.actionType)) {
            Class clazz;
            if (context.getMainService().getNetworkConnectivityManager().isConnected()) {
                clazz = ServiceSearchActivity.class;
            } else {
                clazz = ServiceActionsOfflineActivity.class;
            }

            final Intent i = new Intent(context, clazz);
            extras.putString(ServiceActionsOfflineActivity.ACTION, ni.action);
            extras.putString(ServiceActionsOfflineActivity.TITLE, ni.getLabel(context));
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

        } else if ("cordova".equals(ni.actionType)) {
            final Intent i = new Intent(context, CordovaActionScreenActivity.class);
            extras.putString(CordovaActionScreenActivity.EMBEDDED_APP, ni.action);
            extras.putString(CordovaActionScreenActivity.TITLE, ni.getLabel(context));
            i.putExtras(extras);
            i.addFlags(MainActivity.FLAG_CLEAR_STACK);
            context.startActivity(i);

        } else if ("open".equals(ni.actionType) && "app".equals(ni.action)) {
            String packageName = (String) ni.getParam("android_app_id");
            String scheme = (String) ni.getParam("android_scheme");

            if (TextUtils.isEmptyOrWhitespace(scheme)) {
                openApp(context, packageName);
            } else {
                openScheme(context, packageName, scheme);
            }

        } else {
            L.bug("ignoring simulateNavigationItemClick: " + ni.actionType + "|" + ni.action);
            return false;
        }
        return true;
    }

    public static boolean goToActivity(ServiceBoundActivity context, String activityName, boolean clearStack,
                                       boolean collapse, Bundle extras) {
        if (SIMPLE_ACTIONS.containsKey(activityName)) {
            goToActivity(context, SIMPLE_ACTIONS.get(activityName), clearStack, extras);
        } else if (SERVICE_ACTIONS.containsKey(activityName)) {
            goToServicesActivity(context, SERVICE_ACTIONS.get(activityName), clearStack, collapse, extras);
        } else if ("qrcode".equals(activityName)) {
            goToQRActivity(context, clearStack, extras);
        } else {
            L.bug("unknown goToActivity: " + activityName);
            return false;
        }

        return true;
    }

    private static void openStore(Context context, String appPackage) {
        try {
            context.startActivity(
                    new Intent(Intent.ACTION_VIEW,
                               Uri.parse("market://details?id=" + appPackage)));
        } catch (android.content.ActivityNotFoundException anfe) {
            context.startActivity(
                    new Intent(Intent.ACTION_VIEW,
                               Uri.parse("http://play.google.com/store/apps/details?id=" + appPackage)));
        }
    }

    private static Intent appSchemeIntent(Context context, String scheme) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(scheme));
        PackageManager packageManager = context.getPackageManager();
        if (intent.resolveActivity(packageManager) != null) {
            return intent;
        }
        return null;
    }

    private static void openScheme(Context context, String appPackage, String scheme) {
        Intent intent = appSchemeIntent(context, scheme);
        if (intent == null) {
            openApp(context, appPackage);
        } else {
            context.startActivity(intent);
        }
    }

    private static void openApp(Context context, String appPackage) {
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(appPackage);
        if (intent == null) {
            openStore(context, appPackage);
        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    public static void goToMessagingActivity(Context context, boolean clearStack, Bundle extras) {
        goToActivity(context, MessagingActivity.class, clearStack, extras);
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
        context.menuItemPresser = new MenuItemPresser<>(context, serviceEmail);
    }

    public static boolean goToActivityBehindTagWhenReady(final ServiceBoundActivity context, final String serviceEmail, final String tag) {
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
    }

    public static void goToActivityBehindTag(final ServiceBoundActivity context, final String serviceEmail, final String tag) {
        goToActivityBehindTag(context, serviceEmail, tag, null);
    }

    public static void goToActivityBehindTag(final ServiceBoundActivity context, final String serviceEmail, final String tag, Bundle extras) {
        setupMenuItemPresser(context, serviceEmail);

        context.menuItemPresser.itemPressed(tag, extras, null, new MenuItemPresser.ResultHandler() {
            @Override
            public void onError() {
                L.e("SMI with tag " + tag + " not found!"); // XXX: log error to message.sender
                onTimeout();
            }
        });
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

    /**
     * Opens a url in a chrome custom tab if it's supported, else opens it via a regular intent.
     *
     * @return Whether or not the url was opened in a custom tab
     */
    public static boolean openUrl(Context context, String url, String intentAction) {
        Uri uri = Uri.parse(url);
        if (CUSTOM_TABS_SCHEMES.contains(uri.getScheme())) {
            final CustomTabsIntent.Builder customTabsBuilder = new CustomTabsIntent.Builder();
            final CustomTabsIntent customTabsIntent = customTabsBuilder.build();
            customTabsIntent.launchUrl(context, uri);
            return true;
        } else {
            final Intent intent = new Intent(intentAction, uri);
            context.startActivity(intent);
            return false;
        }
    }

}

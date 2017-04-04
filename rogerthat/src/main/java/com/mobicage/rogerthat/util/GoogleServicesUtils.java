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
package com.mobicage.rogerthat.util;

import java.io.IOException;

import android.app.Activity;
import android.content.pm.PackageManager.NameNotFoundException;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.mobicage.rogerthat.MainService;
import com.mobicage.rogerthat.config.Configuration;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rpc.ResponseHandler;
import com.mobicage.rpc.config.CloudConstants;
import com.mobicage.to.system.UpdateApplePushDeviceTokenRequestTO;
import com.mobicage.to.system.UpdateApplePushDeviceTokenResponseTO;

public class GoogleServicesUtils {

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    public static boolean checkPlayServices(Activity activity) {
        return checkPlayServices(activity, false);
    }

    public static boolean checkPlayServices(Activity activity, boolean justCheck) {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (!justCheck) {
                if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                    GooglePlayServicesUtil.getErrorDialog(resultCode, activity, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
                } else {
                    L.d("This device is not supported.");
                    activity.finish();
                }
            }
            return false;
        }
        return true;
    }

    public interface GCMRegistrationIdFoundCallback {

        void idFound(String registrationId);

    }

    public static void registerGCMRegistrationId(final MainService mainService,
        final GCMRegistrationIdFoundCallback gcmRegistrationIdFoundCallback) {
        T.UI();
        if (!CloudConstants.USE_GCM_KICK_CHANNEL)
            return;
        final Configuration config = mainService.getConfigurationProvider().getConfiguration(MainService.CONFIG_GCM);
        String configRegistrationId = config.get(MainService.CONFIG_GCM_REGISTRATION_ID_KEY, "");
        String configAppVersion = config.get(MainService.CONFIG_GCM_APP_VERSION_KEY, "");
        final String appVersion = getAppVersion(mainService);
        boolean registrationNeeded = "".equals(configRegistrationId) || !configAppVersion.equals(appVersion);
        if (!registrationNeeded)
            return;

        SafeRunnable register = new SafeRunnable() {

            @Override
            protected void safeRun() throws Exception {
                GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(mainService);
                try {
                    final String registrationId = gcm.register(CloudConstants.GCM_SENDER_ID);
                    if (gcmRegistrationIdFoundCallback != null) {
                        // If a callback is supplied, just execute the callback on the UI thread and do nothing else.
                        mainService.postOnUIHandler(new SafeRunnable() {
                            @Override
                            protected void safeRun() throws Exception {
                                gcmRegistrationIdFoundCallback.idFound(registrationId);
                            }
                        });
                        return;
                    }
                    // No callback supplied, save the registration id to the server and the configuration provider.
                    mainService.postOnUIHandler(new SafeRunnable() {
                        @Override
                        protected void safeRun() throws Exception {
                            UpdateApplePushDeviceTokenRequestTO request = new UpdateApplePushDeviceTokenRequestTO();
                            request.token = registrationId;
                            com.mobicage.api.system.Rpc.updateApplePushDeviceToken(
                                new ResponseHandler<UpdateApplePushDeviceTokenResponseTO>(), request);
                            saveGCMRegistrationId(mainService, registrationId);
                        }
                    });
                } catch (IOException e) {
                    L.d("Registration failed, retrying in 5 seconds");
                    mainService.postDelayedOnBIZZHandler(this, 5000);
                }
            }
        };
        mainService.postOnBIZZHandler(register);
    }

    public static String getAppVersion(final MainService mainService) {
        try {
            return mainService.getPackageManager().getPackageInfo(mainService.getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            L.bug("Could not determine appVersion");
            return "1.0";
        }
    }

    public static void saveGCMRegistrationId(final MainService mainService, final String registrationId) {
        final Configuration config = mainService.getConfigurationProvider().getConfiguration(MainService.CONFIG_GCM);
        final String appVersion = getAppVersion(mainService);
        config.put(MainService.CONFIG_GCM_REGISTRATION_ID_KEY, registrationId);
        config.put(MainService.CONFIG_GCM_APP_VERSION_KEY, appVersion);
        mainService.getConfigurationProvider().updateConfigurationLater(MainService.CONFIG_GCM, config);
    }

}

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

import android.content.pm.PackageManager.NameNotFoundException;

import com.google.firebase.iid.FirebaseInstanceId;
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

    public static void registerFirebaseRegistrationId(final MainService mainService) {
        T.UI();
        L.i("registerFirebaseRegistrationId: 1");
        if (!CloudConstants.USE_FIREBASE_KICK_CHANNEL)
            return;
        L.i("registerFirebaseRegistrationId: 2");
        final Configuration config = mainService.getConfigurationProvider().getConfiguration(MainService.CONFIG_FIREBASE);
        String configRegistrationId = config.get(MainService.CONFIG_FIREBASE_REGISTRATION_ID_KEY, "");
        String configAppVersion = config.get(MainService.CONFIG_FIREBASE_APP_VERSION_KEY, "");
        final String appVersion = getAppVersion(mainService);
        boolean registrationNeeded = "".equals(configRegistrationId) || !configAppVersion.equals(appVersion);
        if (!registrationNeeded)
            return;
        L.i("registerFirebaseRegistrationId: 3");
        SafeRunnable register = new SafeRunnable() {

            @Override
            protected void safeRun() throws Exception {
                final String registrationId = FirebaseInstanceId.getInstance().getToken();
                L.i("registerFirebaseRegistrationId: 4 - " + registrationId);
                mainService.postOnUIHandler(new SafeRunnable() {
                    @Override
                    protected void safeRun() throws Exception {
                        UpdateApplePushDeviceTokenRequestTO request = new UpdateApplePushDeviceTokenRequestTO();
                        request.token = registrationId;
                        com.mobicage.api.system.Rpc.updateApplePushDeviceToken(
                            new ResponseHandler<UpdateApplePushDeviceTokenResponseTO>(), request);
                        saveFirebaseRegistrationId(mainService, registrationId);
                    }
                });
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

    public static void saveFirebaseRegistrationId(final MainService mainService, final String registrationId) {
        if (registrationId == null)
            return;
        final Configuration config = mainService.getConfigurationProvider().getConfiguration(MainService.CONFIG_FIREBASE);
        final String appVersion = getAppVersion(mainService);
        config.put(MainService.CONFIG_FIREBASE_REGISTRATION_ID_KEY, registrationId);
        config.put(MainService.CONFIG_FIREBASE_APP_VERSION_KEY, appVersion);
        mainService.getConfigurationProvider().updateConfigurationLater(MainService.CONFIG_FIREBASE, config);
    }

}

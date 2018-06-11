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

package com.mobicage.rogerthat.util.consent;

import com.mobicage.rogerthat.config.Configuration;
import com.mobicage.rogerthat.config.ConfigurationProvider;
import com.mobicage.rogerthat.config.Reconfigurable;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.to.system.SettingsTO;

import java.io.Closeable;

public class ConsentProvider implements Reconfigurable, Closeable {

    public static final String TOS_AGE_16 = "16+";
    public static final String TOS_AGE_PARENTAL = "parental";

    private final static String CONFIGKEY = "com.mobicage.rogerthat.util.consent.ConsentProvider";

    private final static String CONFIG_ASK_TOS_KEY = "ask_tos";
    private final static String CONFIG_ASK_PUSH_NOTIFICATIONS_KEY = "ask_push_notifications";

    private final ConfigurationProvider mConfigProvider;

    // All members owned by UI thread
    private boolean mAskTOS = false;
    private boolean mAskPushNotifications = false;

    public ConsentProvider(ConfigurationProvider pConfigProvider) {
        T.UI();
        mConfigProvider = pConfigProvider;
        initialize();
    }

    @Override
    public void close() {
        T.UI();
        mConfigProvider.unregisterListener(CONFIGKEY, this);
    }

    @Override
    public void reconfigure() {
        T.UI();
        final Configuration cfg = mConfigProvider.getConfiguration(CONFIGKEY);
        final boolean askTOS = cfg.get(CONFIG_ASK_TOS_KEY, false);
        final boolean askPushNotifications = cfg.get(CONFIG_ASK_PUSH_NOTIFICATIONS_KEY, false);
        setConsent(askTOS, askPushNotifications);
    }

    @Override
    public void processSettings(SettingsTO settings) {
        T.UI();
        if (settings.consent == null) {
            return;
        }
        Configuration cfg = new Configuration();
        cfg.put(CONFIG_ASK_TOS_KEY, settings.consent.ask_tos);
        cfg.put(CONFIG_ASK_PUSH_NOTIFICATIONS_KEY, settings.consent.ask_push_notifications);
        mConfigProvider.updateConfigurationLater(CONFIGKEY, cfg);
    }

    private void initialize() {
        T.UI();
        reconfigure();
        mConfigProvider.registerListener(CONFIGKEY, this);
    }

    private void setConsent(final boolean askTOS, final boolean askPushNotifications) {
        T.UI();
        mAskTOS = askTOS;
        mAskPushNotifications = askPushNotifications;
    }

    public boolean shouldAskConsentForTOS() {
        T.UI();
        return mAskTOS;
    }

    public void saveConsentForTOS() {
        T.UI();
        mAskTOS = false;
        Configuration cfg = new Configuration();
        cfg.put(CONFIG_ASK_TOS_KEY, mAskTOS);
        mConfigProvider.updateConfigurationNow(CONFIGKEY, cfg);
    }

    public boolean shouldAskConsentForPushNotifications() {
        T.UI();
        return mAskPushNotifications;
    }

    public void saveConsentForPushNotifications() {
        T.UI();
        mAskPushNotifications = false;
        Configuration cfg = new Configuration();
        cfg.put(CONFIG_ASK_PUSH_NOTIFICATIONS_KEY, mAskPushNotifications);
        mConfigProvider.updateConfigurationNow(CONFIGKEY, cfg);
    }
}

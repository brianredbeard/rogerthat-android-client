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

package com.mobicage.rogerthat.plugins.security;


import com.mobicage.rogerthat.MainService;
import com.mobicage.rogerthat.config.ConfigurationProvider;
import com.mobicage.rogerthat.plugins.MobicagePlugin;

import com.mobicage.rogerthat.util.db.DatabaseManager;
import com.mobicage.rogerthat.util.db.TransactionHelper;
import com.mobicage.rogerthat.util.db.TransactionWithoutResult;
import com.mobicage.rogerthat.util.system.T;

import com.mobicage.to.system.SettingsTO;

import java.util.regex.Pattern;

public class SecurityPlugin implements MobicagePlugin {

    private static final String CONFIGKEY = "com.mobicage.rogerthat.plugins.security";

    private final ConfigurationProvider mConfigProvider;
    private final MainService mMainService;

    private final SecurityStore mStore;

    public SecurityPlugin(final MainService mainService, ConfigurationProvider pConfigProvider, final DatabaseManager dbManager) {
        T.UI();
        mMainService = mainService;
        mConfigProvider = pConfigProvider;
        mStore = new SecurityStore(mainService, dbManager);
    }

    @Override
    public void destroy() {
        T.UI();
        mConfigProvider.unregisterListener(CONFIGKEY, this);
    }

    @Override
    public void processSettings(SettingsTO settings) {
        // not used
    }

    @Override
    public void reconfigure() {
        T.UI();
    }

    public SecurityStore getStore() {
        T.dontCare();
        return mStore;
    }

    @Override
    public void initialize() {
        T.UI();
        reconfigure();
        mConfigProvider.registerListener(CONFIGKEY, this);
    }

    public static String getIndexString(final Long index) {
        if (index == null) {
            return "root";
        }
        return index.toString();
    }

    public boolean isValidName(String name) {
        Pattern p = Pattern.compile("[^a-zA-Z0-9]");
        return !p.matcher(name).find();
    }

    public boolean hasSecurityKey(final String type, final String algorithm, final String name, final Long index) {
        return getSecurityKey(type, algorithm, name, index) != null;
    }

    public String getSecurityKey(final String type, final String algorithm, final String name, final Long index) {
        return mStore.getSecurityKey(type, algorithm, name, getIndexString(index));
    }

    public void saveSecurityKey(final String algorithm, final String name, final Long index,
                                final String publicKeyData, final String privateKeyData,
                                final String seedData, final String address) {

        TransactionHelper.runInTransaction(mStore.getDatabase(), "saveSecurityKey", new TransactionWithoutResult() {
            @Override
            protected void run() {
                if (address == null) {
                    mStore.deleteSecurityGroup(algorithm, name);
                }

                mStore.saveSecurityKey("public", algorithm, name, getIndexString(index), publicKeyData);
                mStore.saveSecurityKey("private", algorithm, name, getIndexString(index), privateKeyData);
                if (seedData != null) {
                    mStore.saveSecurityKey("seed", algorithm, name, getIndexString(index), seedData);
                }
                if (address != null) {
                    mStore.saveSecurityKey("address", algorithm, name, getIndexString(index), address);
                }
            }
        });
    }
}

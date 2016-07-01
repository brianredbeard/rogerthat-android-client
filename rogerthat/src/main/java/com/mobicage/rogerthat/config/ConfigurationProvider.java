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

package com.mobicage.rogerthat.config;

import java.io.Closeable;
import java.util.HashMap;
import java.util.Map;

import com.mobicage.rogerthat.MainService;
import com.mobicage.rogerthat.util.db.CategorizedKeyValueDatabase;
import com.mobicage.rogerthat.util.db.DatabaseManager;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.to.system.SettingsTO;

// XXX: caching
// XXX: deletion of key/value pairs
// XXX: use builtin android Preferences classes?

public class ConfigurationProvider implements Closeable {

    private final static String TABLENAME = "ConfigurationProvider";

    private final CategorizedKeyValueDatabase mDb;
    private final Map<String, Reconfigurable> mConfigMap;
    private final MainService mService;

    public ConfigurationProvider(final MainService service, final DatabaseManager pDatabaseManager) {
        T.UI();
        mService = service;
        mDb = new CategorizedKeyValueDatabase(pDatabaseManager, TABLENAME);
        mConfigMap = new HashMap<String, Reconfigurable>();
    }

    public void registerListener(String configkey, Reconfigurable reconfigurable) {
        T.UI();
        mConfigMap.put(configkey, reconfigurable);
    }

    public void unregisterListener(String configkey, Reconfigurable reconfigurable) {
        T.UI();
        Reconfigurable svc = mConfigMap.get(configkey);
        if (svc == reconfigurable) {
            mConfigMap.remove(configkey);
        }
    }

    public void dispatchNewSettings(SettingsTO settings) {
        T.UI();
        for (Reconfigurable reconfigurable : mConfigMap.values()) {
            try {
                reconfigurable.processSettings(settings);
            } catch (Exception e) {
                L.bug(e);
            }
        }
    }

    public Configuration getConfiguration(final String configkey) {
        T.dontCare();
        Configuration cfg = new Configuration(mDb.getBooleanEntries(configkey), mDb.getLongEntries(configkey),
            mDb.getStringEntries(configkey));
        return cfg;
    }

    public void updateConfigurationLater(String configkey, Configuration cfg) {
        T.UI();
        updateConfigurationToDB(configkey, cfg);
        notifyAsync(configkey);
    }

    public void updateConfigurationNow(String configkey, Configuration cfg) {
        T.dontCare();
        updateConfigurationToDB(configkey, cfg);
        notifyNow(configkey);
    }

    private void notifyNow(String configkey) {
        T.dontCare();
        final Reconfigurable reconfigurable = mConfigMap.get(configkey);
        if (reconfigurable != null) {
            reconfigurable.reconfigure();
        }
    }

    private void notifyAsync(String configkey) {
        T.dontCare();
        final Reconfigurable reconfigurable = mConfigMap.get(configkey);
        if (reconfigurable != null) {
            mService.postOnUIHandler(new SafeRunnable() {
                @Override
                public void safeRun() {
                    T.dontCare();
                    reconfigurable.reconfigure();
                }
            });
        }
    }

    private synchronized void updateConfigurationToDB(String configkey, Configuration cfg) {
        T.dontCare();
        for (final Map.Entry<String, Boolean> entry : cfg.mBooleanSettings.entrySet()) {
            mDb.putBoolean(configkey, entry.getKey(), entry.getValue());
        }
        for (final Map.Entry<String, Long> entry : cfg.mLongSettings.entrySet()) {
            mDb.putLong(configkey, entry.getKey(), entry.getValue());
        }
        for (final Map.Entry<String, String> entry : cfg.mStringSettings.entrySet()) {
            mDb.putString(configkey, entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void close() {
        T.UI();
        if (mConfigMap.size() != 0) {
            final StringBuilder sb = new StringBuilder(
                "Some plugins have not unregistered their configuration listener - ");
            for (final String configKey : mConfigMap.keySet()) {
                sb.append(configKey);
                sb.append('.');
            }
            L.bug(sb.toString());
        }
        mDb.close();
    }

}

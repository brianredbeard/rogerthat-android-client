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

/*
 * Container for <key, value> configuration items
 * Support for boolean, long, String value types
 * 
 * Note: keys are unique for a given value type, not across value types
 */

package com.mobicage.rogerthat.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.mobicage.rogerthat.util.system.T;

public class Configuration {

    final protected Map<String, Boolean> mBooleanSettings;
    final protected Map<String, Long> mLongSettings;
    final protected Map<String, String> mStringSettings;

    public Configuration() {
        T.dontCare();
        mBooleanSettings = new HashMap<String, Boolean>();
        mLongSettings = new HashMap<String, Long>();
        mStringSettings = new HashMap<String, String>();
    }

    public Configuration(final Map<String, Boolean> pBooleanSettings, final Map<String, Long> pLongSettings,
        final Map<String, String> pStringSettings) {
        T.dontCare();
        mBooleanSettings = pBooleanSettings;
        mLongSettings = pLongSettings;
        mStringSettings = pStringSettings;
    }

    public void put(final String key, final boolean value) {
        T.dontCare();
        mBooleanSettings.put(key, value);
    }

    public boolean get(final String key, final boolean defaultValue) {
        T.dontCare();
        if (mBooleanSettings.containsKey(key)) {
            return mBooleanSettings.get(key);
        } else {
            return defaultValue;
        }
    }

    public void put(final String key, final String value) {
        T.dontCare();
        mStringSettings.put(key, value);
    }

    public String get(final String key, final String defaultValue) {
        T.dontCare();
        if (mStringSettings.containsKey(key)) {
            return mStringSettings.get(key);
        } else {
            return defaultValue;
        }
    }

    public void put(final String key, final long value) {
        T.dontCare();
        mLongSettings.put(key, value);
    }

    public long get(final String key, final long defaultValue) {
        T.dontCare();
        if (mLongSettings.containsKey(key)) {
            return mLongSettings.get(key);
        } else {
            return defaultValue;
        }
    }

    public Set<String> getStringKeys() {
        T.dontCare();
        return mStringSettings.keySet();
    }

    public Set<String> getBooleanKeys() {
        T.dontCare();
        return mBooleanSettings.keySet();
    }

    @Override
    public String toString() {
        T.dontCare();
        final StringBuilder sb = new StringBuilder();
        sb.append("BOOLEANS\n");
        for (final Map.Entry<String, Boolean> entry : mBooleanSettings.entrySet()) {
            sb.append(entry.getKey());
            sb.append(": ");
            sb.append(entry.getValue());
            sb.append("\n");
        }
        sb.append("\nLONGS\n");
        for (final Map.Entry<String, Long> entry : mLongSettings.entrySet()) {
            sb.append(entry.getKey());
            sb.append(": ");
            sb.append(entry.getValue());
            sb.append("\n");
        }
        sb.append("\nSTRINGS\n");
        for (final Map.Entry<String, String> entry : mStringSettings.entrySet()) {
            sb.append(entry.getKey());
            sb.append(": ");
            sb.append(entry.getValue());
            sb.append("\n");
        }
        return sb.toString();
    }
}
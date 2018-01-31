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

package com.mobicage.rogerthat.plugins.system;

import java.util.HashMap;
import java.util.Map;

import android.text.TextUtils;

import com.mobicage.rpc.IJSONable;
import com.mobicage.rpc.config.CloudConstants;

public class MobileInfo implements IJSONable {

    public static final int VERSION = 2;

    public CarrierInfo sim = new CarrierInfo();
    public CarrierInfo network = new CarrierInfo();
    public LocaleInfo locale = new LocaleInfo();
    public TimeZoneInfo timeZone = new TimeZoneInfo();
    public DeviceInfo device = new DeviceInfo();
    public ApplicationInfo app = new ApplicationInfo();

    public String getFingerPrint() {
        return TextUtils.join(" | ",
            new String[] { String.valueOf(CloudConstants.DEBUG_LOGGING), sim.getFingerPrint(),
                network.getFingerPrint(), locale.getFingerPrint(), timeZone.getFingerPrint(), device.getFingerPrint(),
                app.getFingerPrint() });
    }

    public class CarrierInfo {
        public String isoCountryCode;
        public String mobileCountryCode;
        public String mobileNetworkCode;
        public String carrierName;

        public String getFingerPrint() {
            return TextUtils.join(" | ", new String[] { isoCountryCode, mobileCountryCode, mobileNetworkCode,
                carrierName });
        }
    }

    public class LocaleInfo {
        public String language;
        public String country;

        public String getFingerPrint() {
            return TextUtils.join(" | ", new String[] { language, country });
        }
    }

    public class TimeZoneInfo {
        public String abbrevation;
        public int secondsFromGMT;

        public String getFingerPrint() {
            return TextUtils.join(" | ", new String[] { abbrevation, secondsFromGMT + "" });
        }
    }

    public class DeviceInfo {
        public String modelName;
        public String osVersion;

        public String getFingerPrint() {
            return TextUtils.join(" | ", new String[] { modelName, osVersion });
        }
    }

    public class ApplicationInfo {
        public String name;
        public int type;
        public int minorVersion;
        public int majorVersion;

        public String getFingerPrint() {
            return TextUtils.join(" | ", new String[] { name, type + "", minorVersion + "", majorVersion + "" });
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> json = new HashMap<String, Object>();
        json.put("version", VERSION);
        json.put("app_type", app.type);
        json.put("app_major_version", app.majorVersion);
        json.put("app_minor_version", app.minorVersion);
        json.put("device_model_name", device.modelName);
        json.put("device_os_version", device.osVersion);
        json.put("sim_country", sim.isoCountryCode);
        json.put("sim_country_code", sim.mobileCountryCode);
        json.put("sim_carrier_code", sim.mobileNetworkCode);
        json.put("sim_carrier_name", sim.carrierName);
        json.put("net_country", network.isoCountryCode);
        json.put("net_country_code", network.mobileCountryCode);
        json.put("net_carrier_code", network.mobileNetworkCode);
        json.put("net_carrier_name", network.carrierName);
        json.put("locale_language", locale.language);
        json.put("locale_country", locale.country);
        json.put("timezone", timeZone.abbrevation);
        json.put("timezone_delta_gmt", timeZone.secondsFromGMT);
        return json;
    }
}

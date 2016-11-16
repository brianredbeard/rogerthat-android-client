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

package com.mobicage.to.system;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class HeartBeatRequestTO implements com.mobicage.rpc.IJSONable {

    public String SDKVersion;
    public long appType;
    public String buildFingerPrint;
    public String deviceModelName;
    public boolean flushBackLog;
    public String localeCountry;
    public String localeLanguage;
    public long majorVersion;
    public long minorVersion;
    public String netCarrierCode;
    public String netCarrierName;
    public String netCountry;
    public String netCountryCode;
    public String networkState;
    public String product;
    public String simCarrierCode;
    public String simCarrierName;
    public String simCountry;
    public String simCountryCode;
    public long timestamp;
    public String timezone;
    public long timezoneDeltaGMT;

    public HeartBeatRequestTO() {
    }

    public HeartBeatRequestTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("SDKVersion")) {
            Object val = json.get("SDKVersion");
            this.SDKVersion = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.system.HeartBeatRequestTO object is missing field 'SDKVersion'");
        }
        if (json.containsKey("appType")) {
            Object val = json.get("appType");
            if (val instanceof Integer) {
                this.appType = ((Integer) val).longValue();
            } else {
                this.appType = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.system.HeartBeatRequestTO object is missing field 'appType'");
        }
        if (json.containsKey("buildFingerPrint")) {
            Object val = json.get("buildFingerPrint");
            this.buildFingerPrint = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.system.HeartBeatRequestTO object is missing field 'buildFingerPrint'");
        }
        if (json.containsKey("deviceModelName")) {
            Object val = json.get("deviceModelName");
            this.deviceModelName = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.system.HeartBeatRequestTO object is missing field 'deviceModelName'");
        }
        if (json.containsKey("flushBackLog")) {
            Object val = json.get("flushBackLog");
            this.flushBackLog = ((Boolean) val).booleanValue();
        } else {
            throw new IncompleteMessageException("com.mobicage.to.system.HeartBeatRequestTO object is missing field 'flushBackLog'");
        }
        if (json.containsKey("localeCountry")) {
            Object val = json.get("localeCountry");
            this.localeCountry = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.system.HeartBeatRequestTO object is missing field 'localeCountry'");
        }
        if (json.containsKey("localeLanguage")) {
            Object val = json.get("localeLanguage");
            this.localeLanguage = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.system.HeartBeatRequestTO object is missing field 'localeLanguage'");
        }
        if (json.containsKey("majorVersion")) {
            Object val = json.get("majorVersion");
            if (val instanceof Integer) {
                this.majorVersion = ((Integer) val).longValue();
            } else {
                this.majorVersion = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.system.HeartBeatRequestTO object is missing field 'majorVersion'");
        }
        if (json.containsKey("minorVersion")) {
            Object val = json.get("minorVersion");
            if (val instanceof Integer) {
                this.minorVersion = ((Integer) val).longValue();
            } else {
                this.minorVersion = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.system.HeartBeatRequestTO object is missing field 'minorVersion'");
        }
        if (json.containsKey("netCarrierCode")) {
            Object val = json.get("netCarrierCode");
            this.netCarrierCode = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.system.HeartBeatRequestTO object is missing field 'netCarrierCode'");
        }
        if (json.containsKey("netCarrierName")) {
            Object val = json.get("netCarrierName");
            this.netCarrierName = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.system.HeartBeatRequestTO object is missing field 'netCarrierName'");
        }
        if (json.containsKey("netCountry")) {
            Object val = json.get("netCountry");
            this.netCountry = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.system.HeartBeatRequestTO object is missing field 'netCountry'");
        }
        if (json.containsKey("netCountryCode")) {
            Object val = json.get("netCountryCode");
            this.netCountryCode = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.system.HeartBeatRequestTO object is missing field 'netCountryCode'");
        }
        if (json.containsKey("networkState")) {
            Object val = json.get("networkState");
            this.networkState = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.system.HeartBeatRequestTO object is missing field 'networkState'");
        }
        if (json.containsKey("product")) {
            Object val = json.get("product");
            this.product = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.system.HeartBeatRequestTO object is missing field 'product'");
        }
        if (json.containsKey("simCarrierCode")) {
            Object val = json.get("simCarrierCode");
            this.simCarrierCode = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.system.HeartBeatRequestTO object is missing field 'simCarrierCode'");
        }
        if (json.containsKey("simCarrierName")) {
            Object val = json.get("simCarrierName");
            this.simCarrierName = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.system.HeartBeatRequestTO object is missing field 'simCarrierName'");
        }
        if (json.containsKey("simCountry")) {
            Object val = json.get("simCountry");
            this.simCountry = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.system.HeartBeatRequestTO object is missing field 'simCountry'");
        }
        if (json.containsKey("simCountryCode")) {
            Object val = json.get("simCountryCode");
            this.simCountryCode = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.system.HeartBeatRequestTO object is missing field 'simCountryCode'");
        }
        if (json.containsKey("timestamp")) {
            Object val = json.get("timestamp");
            if (val instanceof Integer) {
                this.timestamp = ((Integer) val).longValue();
            } else {
                this.timestamp = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.system.HeartBeatRequestTO object is missing field 'timestamp'");
        }
        if (json.containsKey("timezone")) {
            Object val = json.get("timezone");
            this.timezone = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.system.HeartBeatRequestTO object is missing field 'timezone'");
        }
        if (json.containsKey("timezoneDeltaGMT")) {
            Object val = json.get("timezoneDeltaGMT");
            if (val instanceof Integer) {
                this.timezoneDeltaGMT = ((Integer) val).longValue();
            } else {
                this.timezoneDeltaGMT = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.system.HeartBeatRequestTO object is missing field 'timezoneDeltaGMT'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("SDKVersion", this.SDKVersion);
        obj.put("appType", this.appType);
        obj.put("buildFingerPrint", this.buildFingerPrint);
        obj.put("deviceModelName", this.deviceModelName);
        obj.put("flushBackLog", this.flushBackLog);
        obj.put("localeCountry", this.localeCountry);
        obj.put("localeLanguage", this.localeLanguage);
        obj.put("majorVersion", this.majorVersion);
        obj.put("minorVersion", this.minorVersion);
        obj.put("netCarrierCode", this.netCarrierCode);
        obj.put("netCarrierName", this.netCarrierName);
        obj.put("netCountry", this.netCountry);
        obj.put("netCountryCode", this.netCountryCode);
        obj.put("networkState", this.networkState);
        obj.put("product", this.product);
        obj.put("simCarrierCode", this.simCarrierCode);
        obj.put("simCarrierName", this.simCarrierName);
        obj.put("simCountry", this.simCountry);
        obj.put("simCountryCode", this.simCountryCode);
        obj.put("timestamp", this.timestamp);
        obj.put("timezone", this.timezone);
        obj.put("timezoneDeltaGMT", this.timezoneDeltaGMT);
        return obj;
    }

}

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

public class SettingsTO implements com.mobicage.rpc.IJSONable {

    public long[] backgroundFetchTimestamps;
    public long geoLocationSamplingIntervalBattery;
    public long geoLocationSamplingIntervalCharging;
    public boolean geoLocationTracking;
    public long geoLocationTrackingDays;
    public long[] geoLocationTrackingTimeslot;
    public long operatingVersion;
    public boolean recordGeoLocationWithPhoneCalls;
    public boolean recordPhoneCalls;
    public long recordPhoneCallsDays;
    public long[] recordPhoneCallsTimeslot;
    public boolean useGPSBattery;
    public boolean useGPSCharging;
    public long version;
    public boolean wifiOnlyDownloads;
    public long xmppReconnectInterval;

    public SettingsTO() {
    }

    public SettingsTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("backgroundFetchTimestamps")) {
            org.json.simple.JSONArray val_arr = (org.json.simple.JSONArray) json.get("backgroundFetchTimestamps");
            if (val_arr == null) {
                this.backgroundFetchTimestamps = null;
            } else {
                this.backgroundFetchTimestamps = new long[val_arr.size()];
                for (int i=0; i < val_arr.size(); i++) {
                    this.backgroundFetchTimestamps[i] = ((Long) val_arr.get(i)).longValue();
                }
            }
        } else {
            this.backgroundFetchTimestamps = new long[0];
        }
        if (json.containsKey("geoLocationSamplingIntervalBattery")) {
            Object val = json.get("geoLocationSamplingIntervalBattery");
            this.geoLocationSamplingIntervalBattery = ((Long) val).longValue();
        } else {
            throw new IncompleteMessageException("com.mobicage.to.system.SettingsTO object is missing field 'geoLocationSamplingIntervalBattery'");
        }
        if (json.containsKey("geoLocationSamplingIntervalCharging")) {
            Object val = json.get("geoLocationSamplingIntervalCharging");
            this.geoLocationSamplingIntervalCharging = ((Long) val).longValue();
        } else {
            throw new IncompleteMessageException("com.mobicage.to.system.SettingsTO object is missing field 'geoLocationSamplingIntervalCharging'");
        }
        if (json.containsKey("geoLocationTracking")) {
            Object val = json.get("geoLocationTracking");
            this.geoLocationTracking = ((Boolean) val).booleanValue();
        } else {
            throw new IncompleteMessageException("com.mobicage.to.system.SettingsTO object is missing field 'geoLocationTracking'");
        }
        if (json.containsKey("geoLocationTrackingDays")) {
            Object val = json.get("geoLocationTrackingDays");
            this.geoLocationTrackingDays = ((Long) val).longValue();
        } else {
            throw new IncompleteMessageException("com.mobicage.to.system.SettingsTO object is missing field 'geoLocationTrackingDays'");
        }
        if (json.containsKey("geoLocationTrackingTimeslot")) {
            org.json.simple.JSONArray val_arr = (org.json.simple.JSONArray) json.get("geoLocationTrackingTimeslot");
            if (val_arr == null) {
                this.geoLocationTrackingTimeslot = null;
            } else {
                this.geoLocationTrackingTimeslot = new long[val_arr.size()];
                for (int i=0; i < val_arr.size(); i++) {
                    this.geoLocationTrackingTimeslot[i] = ((Long) val_arr.get(i)).longValue();
                }
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.system.SettingsTO object is missing field 'geoLocationTrackingTimeslot'");
        }
        if (json.containsKey("operatingVersion")) {
            Object val = json.get("operatingVersion");
            this.operatingVersion = ((Long) val).longValue();
        } else {
            throw new IncompleteMessageException("com.mobicage.to.system.SettingsTO object is missing field 'operatingVersion'");
        }
        if (json.containsKey("recordGeoLocationWithPhoneCalls")) {
            Object val = json.get("recordGeoLocationWithPhoneCalls");
            this.recordGeoLocationWithPhoneCalls = ((Boolean) val).booleanValue();
        } else {
            throw new IncompleteMessageException("com.mobicage.to.system.SettingsTO object is missing field 'recordGeoLocationWithPhoneCalls'");
        }
        if (json.containsKey("recordPhoneCalls")) {
            Object val = json.get("recordPhoneCalls");
            this.recordPhoneCalls = ((Boolean) val).booleanValue();
        } else {
            throw new IncompleteMessageException("com.mobicage.to.system.SettingsTO object is missing field 'recordPhoneCalls'");
        }
        if (json.containsKey("recordPhoneCallsDays")) {
            Object val = json.get("recordPhoneCallsDays");
            this.recordPhoneCallsDays = ((Long) val).longValue();
        } else {
            throw new IncompleteMessageException("com.mobicage.to.system.SettingsTO object is missing field 'recordPhoneCallsDays'");
        }
        if (json.containsKey("recordPhoneCallsTimeslot")) {
            org.json.simple.JSONArray val_arr = (org.json.simple.JSONArray) json.get("recordPhoneCallsTimeslot");
            if (val_arr == null) {
                this.recordPhoneCallsTimeslot = null;
            } else {
                this.recordPhoneCallsTimeslot = new long[val_arr.size()];
                for (int i=0; i < val_arr.size(); i++) {
                    this.recordPhoneCallsTimeslot[i] = ((Long) val_arr.get(i)).longValue();
                }
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.system.SettingsTO object is missing field 'recordPhoneCallsTimeslot'");
        }
        if (json.containsKey("useGPSBattery")) {
            Object val = json.get("useGPSBattery");
            this.useGPSBattery = ((Boolean) val).booleanValue();
        } else {
            throw new IncompleteMessageException("com.mobicage.to.system.SettingsTO object is missing field 'useGPSBattery'");
        }
        if (json.containsKey("useGPSCharging")) {
            Object val = json.get("useGPSCharging");
            this.useGPSCharging = ((Boolean) val).booleanValue();
        } else {
            throw new IncompleteMessageException("com.mobicage.to.system.SettingsTO object is missing field 'useGPSCharging'");
        }
        if (json.containsKey("version")) {
            Object val = json.get("version");
            this.version = ((Long) val).longValue();
        } else {
            throw new IncompleteMessageException("com.mobicage.to.system.SettingsTO object is missing field 'version'");
        }
        if (json.containsKey("wifiOnlyDownloads")) {
            Object val = json.get("wifiOnlyDownloads");
            this.wifiOnlyDownloads = ((Boolean) val).booleanValue();
        } else {
            this.wifiOnlyDownloads = false;
        }
        if (json.containsKey("xmppReconnectInterval")) {
            Object val = json.get("xmppReconnectInterval");
            this.xmppReconnectInterval = ((Long) val).longValue();
        } else {
            throw new IncompleteMessageException("com.mobicage.to.system.SettingsTO object is missing field 'xmppReconnectInterval'");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        if (this.backgroundFetchTimestamps == null) {
            obj.put("backgroundFetchTimestamps", null);
        } else {
            org.json.simple.JSONArray arr = new org.json.simple.JSONArray();
            for (int i=0; i < this.backgroundFetchTimestamps.length; i++) {
                arr.add(this.backgroundFetchTimestamps[i]);
            }
            obj.put("backgroundFetchTimestamps", arr);
        }
        obj.put("geoLocationSamplingIntervalBattery", this.geoLocationSamplingIntervalBattery);
        obj.put("geoLocationSamplingIntervalCharging", this.geoLocationSamplingIntervalCharging);
        obj.put("geoLocationTracking", this.geoLocationTracking);
        obj.put("geoLocationTrackingDays", this.geoLocationTrackingDays);
        if (this.geoLocationTrackingTimeslot == null) {
            obj.put("geoLocationTrackingTimeslot", null);
        } else {
            org.json.simple.JSONArray arr = new org.json.simple.JSONArray();
            for (int i=0; i < this.geoLocationTrackingTimeslot.length; i++) {
                arr.add(this.geoLocationTrackingTimeslot[i]);
            }
            obj.put("geoLocationTrackingTimeslot", arr);
        }
        obj.put("operatingVersion", this.operatingVersion);
        obj.put("recordGeoLocationWithPhoneCalls", this.recordGeoLocationWithPhoneCalls);
        obj.put("recordPhoneCalls", this.recordPhoneCalls);
        obj.put("recordPhoneCallsDays", this.recordPhoneCallsDays);
        if (this.recordPhoneCallsTimeslot == null) {
            obj.put("recordPhoneCallsTimeslot", null);
        } else {
            org.json.simple.JSONArray arr = new org.json.simple.JSONArray();
            for (int i=0; i < this.recordPhoneCallsTimeslot.length; i++) {
                arr.add(this.recordPhoneCallsTimeslot[i]);
            }
            obj.put("recordPhoneCallsTimeslot", arr);
        }
        obj.put("useGPSBattery", this.useGPSBattery);
        obj.put("useGPSCharging", this.useGPSCharging);
        obj.put("version", this.version);
        obj.put("wifiOnlyDownloads", this.wifiOnlyDownloads);
        obj.put("xmppReconnectInterval", this.xmppReconnectInterval);
        return obj;
    }

}

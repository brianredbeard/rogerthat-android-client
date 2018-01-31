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

package com.mobicage.to.activity;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class CallRecordTO implements com.mobicage.rpc.IJSONable {

    public com.mobicage.to.activity.GeoPointTO geoPoint;
    public com.mobicage.to.activity.RawLocationInfoTO rawLocation;
    public String countrycode;
    public long duration;
    public long id;
    public String phoneNumber;
    public long starttime;
    public long type;

    public CallRecordTO() {
    }

    @SuppressWarnings("unchecked")
    public CallRecordTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("geoPoint")) {
            Object val = json.get("geoPoint");
            this.geoPoint = val == null ? null : new com.mobicage.to.activity.GeoPointTO((Map<String, Object>) val);
        } else {
            throw new IncompleteMessageException("com.mobicage.to.activity.CallRecordTO object is missing field 'geoPoint'");
        }
        if (json.containsKey("rawLocation")) {
            Object val = json.get("rawLocation");
            this.rawLocation = val == null ? null : new com.mobicage.to.activity.RawLocationInfoTO((Map<String, Object>) val);
        } else {
            throw new IncompleteMessageException("com.mobicage.to.activity.CallRecordTO object is missing field 'rawLocation'");
        }
        if (json.containsKey("countrycode")) {
            Object val = json.get("countrycode");
            this.countrycode = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.activity.CallRecordTO object is missing field 'countrycode'");
        }
        if (json.containsKey("duration")) {
            Object val = json.get("duration");
            if (val instanceof Integer) {
                this.duration = ((Integer) val).longValue();
            } else {
                this.duration = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.activity.CallRecordTO object is missing field 'duration'");
        }
        if (json.containsKey("id")) {
            Object val = json.get("id");
            if (val instanceof Integer) {
                this.id = ((Integer) val).longValue();
            } else {
                this.id = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.activity.CallRecordTO object is missing field 'id'");
        }
        if (json.containsKey("phoneNumber")) {
            Object val = json.get("phoneNumber");
            this.phoneNumber = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.activity.CallRecordTO object is missing field 'phoneNumber'");
        }
        if (json.containsKey("starttime")) {
            Object val = json.get("starttime");
            if (val instanceof Integer) {
                this.starttime = ((Integer) val).longValue();
            } else {
                this.starttime = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.activity.CallRecordTO object is missing field 'starttime'");
        }
        if (json.containsKey("type")) {
            Object val = json.get("type");
            if (val instanceof Integer) {
                this.type = ((Integer) val).longValue();
            } else {
                this.type = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.activity.CallRecordTO object is missing field 'type'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("geoPoint", this.geoPoint == null ? null : this.geoPoint.toJSONMap());
        obj.put("rawLocation", this.rawLocation == null ? null : this.rawLocation.toJSONMap());
        obj.put("countrycode", this.countrycode);
        obj.put("duration", this.duration);
        obj.put("id", this.id);
        obj.put("phoneNumber", this.phoneNumber);
        obj.put("starttime", this.starttime);
        obj.put("type", this.type);
        return obj;
    }

}

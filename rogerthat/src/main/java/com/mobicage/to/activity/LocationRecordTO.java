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

public class LocationRecordTO implements com.mobicage.rpc.IJSONable {

    public com.mobicage.to.activity.GeoPointTO geoPoint;
    public com.mobicage.to.activity.RawLocationInfoTO rawLocation;
    public long timestamp;

    public LocationRecordTO() {
    }

    @SuppressWarnings("unchecked")
    public LocationRecordTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("geoPoint")) {
            Object val = json.get("geoPoint");
            this.geoPoint = val == null ? null : new com.mobicage.to.activity.GeoPointTO((Map<String, Object>) val);
        } else {
            throw new IncompleteMessageException("com.mobicage.to.activity.LocationRecordTO object is missing field 'geoPoint'");
        }
        if (json.containsKey("rawLocation")) {
            Object val = json.get("rawLocation");
            this.rawLocation = val == null ? null : new com.mobicage.to.activity.RawLocationInfoTO((Map<String, Object>) val);
        } else {
            throw new IncompleteMessageException("com.mobicage.to.activity.LocationRecordTO object is missing field 'rawLocation'");
        }
        if (json.containsKey("timestamp")) {
            Object val = json.get("timestamp");
            if (val instanceof Integer) {
                this.timestamp = ((Integer) val).longValue();
            } else {
                this.timestamp = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.activity.LocationRecordTO object is missing field 'timestamp'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("geoPoint", this.geoPoint == null ? null : this.geoPoint.toJSONMap());
        obj.put("rawLocation", this.rawLocation == null ? null : this.rawLocation.toJSONMap());
        obj.put("timestamp", this.timestamp);
        return obj;
    }

}

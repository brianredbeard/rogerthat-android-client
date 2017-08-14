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

package com.mobicage.to.messaging.forms;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class LocationWidgetResultTO implements com.mobicage.rpc.IJSONable {

    public float altitude;
    public float horizontal_accuracy;
    public float latitude;
    public float longitude;
    public long timestamp;
    public float vertical_accuracy;

    public LocationWidgetResultTO() {
    }

    public LocationWidgetResultTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("altitude")) {
            Object val = json.get("altitude");
            if (val instanceof Float) {
                this.altitude = ((Float) val).floatValue();
            } else if (val instanceof Double) {
                this.altitude = new Float((Double) val).floatValue();
            } else {
                this.altitude = new Float((Long) val).floatValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.forms.LocationWidgetResultTO object is missing field 'altitude'");
        }
        if (json.containsKey("horizontal_accuracy")) {
            Object val = json.get("horizontal_accuracy");
            if (val instanceof Float) {
                this.horizontal_accuracy = ((Float) val).floatValue();
            } else if (val instanceof Double) {
                this.horizontal_accuracy = new Float((Double) val).floatValue();
            } else {
                this.horizontal_accuracy = new Float((Long) val).floatValue();
            }
        } else {
            this.horizontal_accuracy = -1;
        }
        if (json.containsKey("latitude")) {
            Object val = json.get("latitude");
            if (val instanceof Float) {
                this.latitude = ((Float) val).floatValue();
            } else if (val instanceof Double) {
                this.latitude = new Float((Double) val).floatValue();
            } else {
                this.latitude = new Float((Long) val).floatValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.forms.LocationWidgetResultTO object is missing field 'latitude'");
        }
        if (json.containsKey("longitude")) {
            Object val = json.get("longitude");
            if (val instanceof Float) {
                this.longitude = ((Float) val).floatValue();
            } else if (val instanceof Double) {
                this.longitude = new Float((Double) val).floatValue();
            } else {
                this.longitude = new Float((Long) val).floatValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.forms.LocationWidgetResultTO object is missing field 'longitude'");
        }
        if (json.containsKey("timestamp")) {
            Object val = json.get("timestamp");
            if (val instanceof Integer) {
                this.timestamp = ((Integer) val).longValue();
            } else {
                this.timestamp = ((Long) val).longValue();
            }
        } else {
            this.timestamp = 0;
        }
        if (json.containsKey("vertical_accuracy")) {
            Object val = json.get("vertical_accuracy");
            if (val instanceof Float) {
                this.vertical_accuracy = ((Float) val).floatValue();
            } else if (val instanceof Double) {
                this.vertical_accuracy = new Float((Double) val).floatValue();
            } else {
                this.vertical_accuracy = new Float((Long) val).floatValue();
            }
        } else {
            this.vertical_accuracy = -1;
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("altitude", this.altitude);
        obj.put("horizontal_accuracy", this.horizontal_accuracy);
        obj.put("latitude", this.latitude);
        obj.put("longitude", this.longitude);
        obj.put("timestamp", this.timestamp);
        obj.put("vertical_accuracy", this.vertical_accuracy);
        return obj;
    }

}
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

package com.mobicage.to.activity;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class GeoPointTO implements com.mobicage.rpc.IJSONable {

    public long accuracy;
    public long latitude;
    public long longitude;

    public GeoPointTO() {
    }

    public GeoPointTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("accuracy")) {
            Object val = json.get("accuracy");
            this.accuracy = ((Long) val).longValue();
        } else {
            throw new IncompleteMessageException("com.mobicage.to.activity.GeoPointTO object is missing field 'accuracy'");
        }
        if (json.containsKey("latitude")) {
            Object val = json.get("latitude");
            this.latitude = ((Long) val).longValue();
        } else {
            throw new IncompleteMessageException("com.mobicage.to.activity.GeoPointTO object is missing field 'latitude'");
        }
        if (json.containsKey("longitude")) {
            Object val = json.get("longitude");
            this.longitude = ((Long) val).longValue();
        } else {
            throw new IncompleteMessageException("com.mobicage.to.activity.GeoPointTO object is missing field 'longitude'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("accuracy", this.accuracy);
        obj.put("latitude", this.latitude);
        obj.put("longitude", this.longitude);
        return obj;
    }

}
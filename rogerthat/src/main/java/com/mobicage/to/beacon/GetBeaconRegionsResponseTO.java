/*
 * Copyright 2017 Mobicage NV
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
 * @@license_version:1.2@@
 */

package com.mobicage.to.beacon;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class GetBeaconRegionsResponseTO implements com.mobicage.rpc.IJSONable {

    public com.mobicage.to.beacon.BeaconRegionTO[] regions;

    public GetBeaconRegionsResponseTO() {
    }

    @SuppressWarnings("unchecked")
    public GetBeaconRegionsResponseTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("regions")) {
            org.json.simple.JSONArray val_arr = (org.json.simple.JSONArray) json.get("regions");
            if (val_arr == null) {
                this.regions = null;
            } else {
                this.regions = new com.mobicage.to.beacon.BeaconRegionTO[val_arr.size()];
                for (int i=0; i < val_arr.size(); i++) {
                    Object item = val_arr.get(i);
                    if (item != null) {
                        this.regions[i] = new com.mobicage.to.beacon.BeaconRegionTO((Map<String, Object>) item);
                    }
                }
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.beacon.GetBeaconRegionsResponseTO object is missing field 'regions'");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        if (this.regions == null) {
            obj.put("regions", null);
        } else {
            org.json.simple.JSONArray arr = new org.json.simple.JSONArray();
            for (int i=0; i < this.regions.length; i++) {
                arr.add(this.regions[i].toJSONMap());
            }
            obj.put("regions", arr);
        }
        return obj;
    }

}

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

package com.mobicage.to.service;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class GetMenuIconRequestTO implements com.mobicage.rpc.IJSONable {

    public long[] coords;
    public String service;
    public long size;

    public GetMenuIconRequestTO() {
    }

    public GetMenuIconRequestTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("coords")) {
            org.json.simple.JSONArray val_arr = (org.json.simple.JSONArray) json.get("coords");
            if (val_arr == null) {
                this.coords = null;
            } else {
                this.coords = new long[val_arr.size()];
                for (int i=0; i < val_arr.size(); i++) {
                    this.coords[i] = ((Long) val_arr.get(i)).longValue();
                }
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.service.GetMenuIconRequestTO object is missing field 'coords'");
        }
        if (json.containsKey("service")) {
            Object val = json.get("service");
            this.service = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.service.GetMenuIconRequestTO object is missing field 'service'");
        }
        if (json.containsKey("size")) {
            Object val = json.get("size");
            if (val instanceof Integer) {
                this.size = ((Integer) val).longValue();
            } else {
                this.size = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.service.GetMenuIconRequestTO object is missing field 'size'");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        if (this.coords == null) {
            obj.put("coords", null);
        } else {
            org.json.simple.JSONArray arr = new org.json.simple.JSONArray();
            for (int i=0; i < this.coords.length; i++) {
                arr.add(this.coords[i]);
            }
            obj.put("coords", arr);
        }
        obj.put("service", this.service);
        obj.put("size", this.size);
        return obj;
    }

}

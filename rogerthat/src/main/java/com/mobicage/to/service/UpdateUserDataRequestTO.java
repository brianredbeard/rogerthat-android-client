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

package com.mobicage.to.service;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class UpdateUserDataRequestTO implements com.mobicage.rpc.IJSONable {

    public String app_data;
    public String[] keys;
    public String service;
    public String type;
    public String user_data;
    public String[] values;

    public UpdateUserDataRequestTO() {
    }

    public UpdateUserDataRequestTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("app_data")) {
            Object val = json.get("app_data");
            this.app_data = (String) val;
        } else {
            this.app_data = null;
        }
        if (json.containsKey("keys")) {
            org.json.simple.JSONArray val_arr = (org.json.simple.JSONArray) json.get("keys");
            if (val_arr == null) {
                this.keys = null;
            } else {
                this.keys = new String[val_arr.size()];
                for (int i=0; i < val_arr.size(); i++) {
                    this.keys[i] = (String) val_arr.get(i);
                }
            }
        } else {
            this.keys = new String[0];
        }
        if (json.containsKey("service")) {
            Object val = json.get("service");
            this.service = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.service.UpdateUserDataRequestTO object is missing field 'service'");
        }
        if (json.containsKey("type")) {
            Object val = json.get("type");
            this.type = (String) val;
        } else {
            this.type = null;
        }
        if (json.containsKey("user_data")) {
            Object val = json.get("user_data");
            this.user_data = (String) val;
        } else {
            this.user_data = null;
        }
        if (json.containsKey("values")) {
            org.json.simple.JSONArray val_arr = (org.json.simple.JSONArray) json.get("values");
            if (val_arr == null) {
                this.values = null;
            } else {
                this.values = new String[val_arr.size()];
                for (int i=0; i < val_arr.size(); i++) {
                    this.values[i] = (String) val_arr.get(i);
                }
            }
        } else {
            this.values = new String[0];
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("app_data", this.app_data);
        if (this.keys == null) {
            obj.put("keys", null);
        } else {
            org.json.simple.JSONArray arr = new org.json.simple.JSONArray();
            for (int i=0; i < this.keys.length; i++) {
                arr.add(this.keys[i]);
            }
            obj.put("keys", arr);
        }
        obj.put("service", this.service);
        obj.put("type", this.type);
        obj.put("user_data", this.user_data);
        if (this.values == null) {
            obj.put("values", null);
        } else {
            org.json.simple.JSONArray arr = new org.json.simple.JSONArray();
            for (int i=0; i < this.values.length; i++) {
                arr.add(this.values[i]);
            }
            obj.put("values", arr);
        }
        return obj;
    }

}

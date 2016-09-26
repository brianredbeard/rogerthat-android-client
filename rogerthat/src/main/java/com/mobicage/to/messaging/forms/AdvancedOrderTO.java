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

package com.mobicage.to.messaging.forms;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class AdvancedOrderTO implements com.mobicage.rpc.IJSONable {

    public com.mobicage.models.properties.forms.AdvancedOrderCategory[] categories;
    public String currency;
    public long leap_time;

    public AdvancedOrderTO() {
    }

    @SuppressWarnings("unchecked")
    public AdvancedOrderTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("categories")) {
            org.json.simple.JSONArray val_arr = (org.json.simple.JSONArray) json.get("categories");
            if (val_arr == null) {
                this.categories = null;
            } else {
                this.categories = new com.mobicage.models.properties.forms.AdvancedOrderCategory[val_arr.size()];
                for (int i=0; i < val_arr.size(); i++) {
                    Object item = val_arr.get(i);
                    if (item != null) {
                        this.categories[i] = new com.mobicage.models.properties.forms.AdvancedOrderCategory((Map<String, Object>) item);
                    }
                }
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.forms.AdvancedOrderTO object is missing field 'categories'");
        }
        if (json.containsKey("currency")) {
            Object val = json.get("currency");
            this.currency = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.forms.AdvancedOrderTO object is missing field 'currency'");
        }
        if (json.containsKey("leap_time")) {
            Object val = json.get("leap_time");
            if (val instanceof Integer) {
                this.leap_time = ((Integer) val).longValue();
            } else {
                this.leap_time = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.forms.AdvancedOrderTO object is missing field 'leap_time'");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        if (this.categories == null) {
            obj.put("categories", null);
        } else {
            org.json.simple.JSONArray arr = new org.json.simple.JSONArray();
            for (int i=0; i < this.categories.length; i++) {
                arr.add(this.categories[i].toJSONMap());
            }
            obj.put("categories", arr);
        }
        obj.put("currency", this.currency);
        obj.put("leap_time", this.leap_time);
        return obj;
    }

}

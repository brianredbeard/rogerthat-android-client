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

public class PayTO implements com.mobicage.rpc.IJSONable {

    public com.mobicage.models.properties.forms.PaymentMethod[] methods;
    public String memo;
    public String target;

    public PayTO() {
    }

    @SuppressWarnings("unchecked")
    public PayTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("methods")) {
            org.json.simple.JSONArray val_arr = (org.json.simple.JSONArray) json.get("methods");
            if (val_arr == null) {
                this.methods = null;
            } else {
                this.methods = new com.mobicage.models.properties.forms.PaymentMethod[val_arr.size()];
                for (int i=0; i < val_arr.size(); i++) {
                    Object item = val_arr.get(i);
                    if (item != null) {
                        this.methods[i] = new com.mobicage.models.properties.forms.PaymentMethod((Map<String, Object>) item);
                    }
                }
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.forms.PayTO object is missing field 'methods'");
        }
        if (json.containsKey("memo")) {
            Object val = json.get("memo");
            this.memo = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.forms.PayTO object is missing field 'memo'");
        }
        if (json.containsKey("target")) {
            Object val = json.get("target");
            this.target = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.forms.PayTO object is missing field 'target'");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        if (this.methods == null) {
            obj.put("methods", null);
        } else {
            org.json.simple.JSONArray arr = new org.json.simple.JSONArray();
            for (int i=0; i < this.methods.length; i++) {
                arr.add(this.methods[i].toJSONMap());
            }
            obj.put("methods", arr);
        }
        obj.put("memo", this.memo);
        obj.put("target", this.target);
        return obj;
    }

}

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

package com.mobicage.to.messaging.forms;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class MultiSelectTO implements com.mobicage.rpc.IJSONable {

    public com.mobicage.to.messaging.forms.ChoiceTO[] choices;
    public String[] values;

    public MultiSelectTO() {
    }

    @SuppressWarnings("unchecked")
    public MultiSelectTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("choices")) {
            org.json.simple.JSONArray val_arr = (org.json.simple.JSONArray) json.get("choices");
            if (val_arr == null) {
                this.choices = null;
            } else {
                this.choices = new com.mobicage.to.messaging.forms.ChoiceTO[val_arr.size()];
                for (int i=0; i < val_arr.size(); i++) {
                    Object item = val_arr.get(i);
                    if (item != null) {
                        this.choices[i] = new com.mobicage.to.messaging.forms.ChoiceTO((Map<String, Object>) item);
                    }
                }
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.forms.MultiSelectTO object is missing field 'choices'");
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
            throw new IncompleteMessageException("com.mobicage.to.messaging.forms.MultiSelectTO object is missing field 'values'");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        if (this.choices == null) {
            obj.put("choices", null);
        } else {
            org.json.simple.JSONArray arr = new org.json.simple.JSONArray();
            for (int i=0; i < this.choices.length; i++) {
                arr.add(this.choices[i].toJSONMap());
            }
            obj.put("choices", arr);
        }
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

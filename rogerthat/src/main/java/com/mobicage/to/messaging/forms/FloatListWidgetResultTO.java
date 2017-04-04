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

public class FloatListWidgetResultTO implements com.mobicage.rpc.IJSONable {

    public float[] values;

    public FloatListWidgetResultTO() {
    }

    public FloatListWidgetResultTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("values")) {
            org.json.simple.JSONArray val_arr = (org.json.simple.JSONArray) json.get("values");
            if (val_arr == null) {
                this.values = null;
            } else {
                this.values = new float[val_arr.size()];
                for (int i=0; i < val_arr.size(); i++) {
                    Object val = val_arr.get(i);
                    if (val instanceof Float) {
                        this.values[i] = ((Float) val).floatValue();
                    } else if (val instanceof Double) {
                        this.values[i] = new Float((Double) val).floatValue();
                    } else {
                        this.values[i] = new Float((Long) val).floatValue();
                    }
                }
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.forms.FloatListWidgetResultTO object is missing field 'values'");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
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

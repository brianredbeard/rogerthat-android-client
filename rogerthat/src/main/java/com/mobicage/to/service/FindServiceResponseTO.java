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

public class FindServiceResponseTO implements com.mobicage.rpc.IJSONable {

    public com.mobicage.to.service.FindServiceCategoryTO[] matches;
    public String error_string;

    public FindServiceResponseTO() {
    }

    @SuppressWarnings("unchecked")
    public FindServiceResponseTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("matches")) {
            org.json.simple.JSONArray val_arr = (org.json.simple.JSONArray) json.get("matches");
            if (val_arr == null) {
                this.matches = null;
            } else {
                this.matches = new com.mobicage.to.service.FindServiceCategoryTO[val_arr.size()];
                for (int i=0; i < val_arr.size(); i++) {
                    Object item = val_arr.get(i);
                    if (item != null) {
                        this.matches[i] = new com.mobicage.to.service.FindServiceCategoryTO((Map<String, Object>) item);
                    }
                }
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.service.FindServiceResponseTO object is missing field 'matches'");
        }
        if (json.containsKey("error_string")) {
            Object val = json.get("error_string");
            this.error_string = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.service.FindServiceResponseTO object is missing field 'error_string'");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        if (this.matches == null) {
            obj.put("matches", null);
        } else {
            org.json.simple.JSONArray arr = new org.json.simple.JSONArray();
            for (int i=0; i < this.matches.length; i++) {
                arr.add(this.matches[i].toJSONMap());
            }
            obj.put("matches", arr);
        }
        obj.put("error_string", this.error_string);
        return obj;
    }

}

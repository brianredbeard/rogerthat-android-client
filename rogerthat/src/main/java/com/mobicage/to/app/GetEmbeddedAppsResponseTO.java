/*
 * Copyright 2018 GIG Technology NV
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
 * @@license_version:1.4@@
 */

package com.mobicage.to.app;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class GetEmbeddedAppsResponseTO implements com.mobicage.rpc.IJSONable {

    public com.mobicage.to.app.EmbeddedAppTO[] embedded_apps;

    public GetEmbeddedAppsResponseTO() {
    }

    @SuppressWarnings("unchecked")
    public GetEmbeddedAppsResponseTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("embedded_apps")) {
            org.json.simple.JSONArray val_arr = (org.json.simple.JSONArray) json.get("embedded_apps");
            if (val_arr == null) {
                this.embedded_apps = null;
            } else {
                this.embedded_apps = new com.mobicage.to.app.EmbeddedAppTO[val_arr.size()];
                for (int i=0; i < val_arr.size(); i++) {
                    Object item = val_arr.get(i);
                    if (item != null) {
                        this.embedded_apps[i] = new com.mobicage.to.app.EmbeddedAppTO((Map<String, Object>) item);
                    }
                }
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.app.GetEmbeddedAppsResponseTO object is missing field 'embedded_apps'");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        if (this.embedded_apps == null) {
            obj.put("embedded_apps", null);
        } else {
            org.json.simple.JSONArray arr = new org.json.simple.JSONArray();
            for (int i=0; i < this.embedded_apps.length; i++) {
                arr.add(this.embedded_apps[i].toJSONMap());
            }
            obj.put("embedded_apps", arr);
        }
        return obj;
    }

}

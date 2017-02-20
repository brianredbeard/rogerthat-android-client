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

package com.mobicage.to.app;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class HomeScreenSettingsTO implements com.mobicage.rpc.IJSONable {

    public com.mobicage.to.app.NavigationItemTO[] items;
    public String color;
    public String header_image_url;
    public String style;

    public HomeScreenSettingsTO() {
    }

    @SuppressWarnings("unchecked")
    public HomeScreenSettingsTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("items")) {
            org.json.simple.JSONArray val_arr = (org.json.simple.JSONArray) json.get("items");
            if (val_arr == null) {
                this.items = null;
            } else {
                this.items = new com.mobicage.to.app.NavigationItemTO[val_arr.size()];
                for (int i=0; i < val_arr.size(); i++) {
                    Object item = val_arr.get(i);
                    if (item != null) {
                        this.items[i] = new com.mobicage.to.app.NavigationItemTO((Map<String, Object>) item);
                    }
                }
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.app.HomeScreenSettingsTO object is missing field 'items'");
        }
        if (json.containsKey("color")) {
            Object val = json.get("color");
            this.color = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.app.HomeScreenSettingsTO object is missing field 'color'");
        }
        if (json.containsKey("header_image_url")) {
            Object val = json.get("header_image_url");
            this.header_image_url = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.app.HomeScreenSettingsTO object is missing field 'header_image_url'");
        }
        if (json.containsKey("style")) {
            Object val = json.get("style");
            this.style = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.app.HomeScreenSettingsTO object is missing field 'style'");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        if (this.items == null) {
            obj.put("items", null);
        } else {
            org.json.simple.JSONArray arr = new org.json.simple.JSONArray();
            for (int i=0; i < this.items.length; i++) {
                arr.add(this.items[i].toJSONMap());
            }
            obj.put("items", arr);
        }
        obj.put("color", this.color);
        obj.put("header_image_url", this.header_image_url);
        obj.put("style", this.style);
        return obj;
    }

}

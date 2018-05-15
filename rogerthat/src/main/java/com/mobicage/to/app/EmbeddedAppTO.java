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

public class EmbeddedAppTO implements com.mobicage.rpc.IJSONable {

    public String description;
    public String name;
    public String serving_url;
    public String title;
    public String[] types;
    public String[] url_regexes;
    public long version;

    public EmbeddedAppTO() {
    }

    public EmbeddedAppTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("description")) {
            Object val = json.get("description");
            this.description = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.app.EmbeddedAppTO object is missing field 'description'");
        }
        if (json.containsKey("name")) {
            Object val = json.get("name");
            this.name = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.app.EmbeddedAppTO object is missing field 'name'");
        }
        if (json.containsKey("serving_url")) {
            Object val = json.get("serving_url");
            this.serving_url = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.app.EmbeddedAppTO object is missing field 'serving_url'");
        }
        if (json.containsKey("title")) {
            Object val = json.get("title");
            this.title = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.app.EmbeddedAppTO object is missing field 'title'");
        }
        if (json.containsKey("types")) {
            org.json.simple.JSONArray val_arr = (org.json.simple.JSONArray) json.get("types");
            if (val_arr == null) {
                this.types = null;
            } else {
                this.types = new String[val_arr.size()];
                for (int i=0; i < val_arr.size(); i++) {
                    this.types[i] = (String) val_arr.get(i);
                }
            }
        } else {
            this.types = new String[0];
        }
        if (json.containsKey("url_regexes")) {
            org.json.simple.JSONArray val_arr = (org.json.simple.JSONArray) json.get("url_regexes");
            if (val_arr == null) {
                this.url_regexes = null;
            } else {
                this.url_regexes = new String[val_arr.size()];
                for (int i=0; i < val_arr.size(); i++) {
                    this.url_regexes[i] = (String) val_arr.get(i);
                }
            }
        } else {
            this.url_regexes = new String[0];
        }
        if (json.containsKey("version")) {
            Object val = json.get("version");
            if (val instanceof Integer) {
                this.version = ((Integer) val).longValue();
            } else {
                this.version = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.app.EmbeddedAppTO object is missing field 'version'");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("description", this.description);
        obj.put("name", this.name);
        obj.put("serving_url", this.serving_url);
        obj.put("title", this.title);
        if (this.types == null) {
            obj.put("types", null);
        } else {
            org.json.simple.JSONArray arr = new org.json.simple.JSONArray();
            for (int i=0; i < this.types.length; i++) {
                arr.add(this.types[i]);
            }
            obj.put("types", arr);
        }
        if (this.url_regexes == null) {
            obj.put("url_regexes", null);
        } else {
            org.json.simple.JSONArray arr = new org.json.simple.JSONArray();
            for (int i=0; i < this.url_regexes.length; i++) {
                arr.add(this.url_regexes[i]);
            }
            obj.put("url_regexes", arr);
        }
        obj.put("version", this.version);
        return obj;
    }

}

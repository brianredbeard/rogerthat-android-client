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

public class UserDetailsTO implements com.mobicage.rpc.IJSONable {

    public com.mobicage.models.properties.profiles.PublicKeyTO[] public_keys;
    public String app_id;
    public String avatar_url;
    public String email;
    public String language;
    public String name;
    public String public_key;

    public UserDetailsTO() {
    }

    @SuppressWarnings("unchecked")
    public UserDetailsTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("public_keys")) {
            org.json.simple.JSONArray val_arr = (org.json.simple.JSONArray) json.get("public_keys");
            if (val_arr == null) {
                this.public_keys = null;
            } else {
                this.public_keys = new com.mobicage.models.properties.profiles.PublicKeyTO[val_arr.size()];
                for (int i=0; i < val_arr.size(); i++) {
                    Object item = val_arr.get(i);
                    if (item != null) {
                        this.public_keys[i] = new com.mobicage.models.properties.profiles.PublicKeyTO((Map<String, Object>) item);
                    }
                }
            }
        } else {
            this.public_keys = new com.mobicage.models.properties.profiles.PublicKeyTO[0];
        }
        if (json.containsKey("app_id")) {
            Object val = json.get("app_id");
            this.app_id = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.service.UserDetailsTO object is missing field 'app_id'");
        }
        if (json.containsKey("avatar_url")) {
            Object val = json.get("avatar_url");
            this.avatar_url = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.service.UserDetailsTO object is missing field 'avatar_url'");
        }
        if (json.containsKey("email")) {
            Object val = json.get("email");
            this.email = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.service.UserDetailsTO object is missing field 'email'");
        }
        if (json.containsKey("language")) {
            Object val = json.get("language");
            this.language = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.service.UserDetailsTO object is missing field 'language'");
        }
        if (json.containsKey("name")) {
            Object val = json.get("name");
            this.name = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.service.UserDetailsTO object is missing field 'name'");
        }
        if (json.containsKey("public_key")) {
            Object val = json.get("public_key");
            this.public_key = (String) val;
        } else {
            this.public_key = null;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        if (this.public_keys == null) {
            obj.put("public_keys", null);
        } else {
            org.json.simple.JSONArray arr = new org.json.simple.JSONArray();
            for (int i=0; i < this.public_keys.length; i++) {
                arr.add(this.public_keys[i].toJSONMap());
            }
            obj.put("public_keys", arr);
        }
        obj.put("app_id", this.app_id);
        obj.put("avatar_url", this.avatar_url);
        obj.put("email", this.email);
        obj.put("language", this.language);
        obj.put("name", this.name);
        obj.put("public_key", this.public_key);
        return obj;
    }

}

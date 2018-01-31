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

package com.mobicage.to.friends;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class GetUserInfoResponseTO implements com.mobicage.rpc.IJSONable {

    public com.mobicage.to.friends.ErrorTO error;
    public String app_id;
    public String avatar;
    public long avatar_id;
    public String description;
    public String descriptionBranding;
    public String email;
    public String name;
    public String profileData;
    public String qualifiedIdentifier;
    public long type;

    public GetUserInfoResponseTO() {
    }

    @SuppressWarnings("unchecked")
    public GetUserInfoResponseTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("error")) {
            Object val = json.get("error");
            this.error = val == null ? null : new com.mobicage.to.friends.ErrorTO((Map<String, Object>) val);
        } else {
            this.error = null;
        }
        if (json.containsKey("app_id")) {
            Object val = json.get("app_id");
            this.app_id = (String) val;
        } else {
            this.app_id = null;
        }
        if (json.containsKey("avatar")) {
            Object val = json.get("avatar");
            this.avatar = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.friends.GetUserInfoResponseTO object is missing field 'avatar'");
        }
        if (json.containsKey("avatar_id")) {
            Object val = json.get("avatar_id");
            if (val instanceof Integer) {
                this.avatar_id = ((Integer) val).longValue();
            } else {
                this.avatar_id = ((Long) val).longValue();
            }
        } else {
            this.avatar_id = -1;
        }
        if (json.containsKey("description")) {
            Object val = json.get("description");
            this.description = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.friends.GetUserInfoResponseTO object is missing field 'description'");
        }
        if (json.containsKey("descriptionBranding")) {
            Object val = json.get("descriptionBranding");
            this.descriptionBranding = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.friends.GetUserInfoResponseTO object is missing field 'descriptionBranding'");
        }
        if (json.containsKey("email")) {
            Object val = json.get("email");
            this.email = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.friends.GetUserInfoResponseTO object is missing field 'email'");
        }
        if (json.containsKey("name")) {
            Object val = json.get("name");
            this.name = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.friends.GetUserInfoResponseTO object is missing field 'name'");
        }
        if (json.containsKey("profileData")) {
            Object val = json.get("profileData");
            this.profileData = (String) val;
        } else {
            this.profileData = null;
        }
        if (json.containsKey("qualifiedIdentifier")) {
            Object val = json.get("qualifiedIdentifier");
            this.qualifiedIdentifier = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.friends.GetUserInfoResponseTO object is missing field 'qualifiedIdentifier'");
        }
        if (json.containsKey("type")) {
            Object val = json.get("type");
            if (val instanceof Integer) {
                this.type = ((Integer) val).longValue();
            } else {
                this.type = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.friends.GetUserInfoResponseTO object is missing field 'type'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("error", this.error == null ? null : this.error.toJSONMap());
        obj.put("app_id", this.app_id);
        obj.put("avatar", this.avatar);
        obj.put("avatar_id", this.avatar_id);
        obj.put("description", this.description);
        obj.put("descriptionBranding", this.descriptionBranding);
        obj.put("email", this.email);
        obj.put("name", this.name);
        obj.put("profileData", this.profileData);
        obj.put("qualifiedIdentifier", this.qualifiedIdentifier);
        obj.put("type", this.type);
        return obj;
    }

}

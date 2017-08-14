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

package com.mobicage.to.system;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class EditProfileRequestTO implements com.mobicage.rpc.IJSONable {

    public String access_token;
    public String avatar;
    public long birthdate;
    public String extra_fields;
    public long gender;
    public boolean has_birthdate;
    public boolean has_gender;
    public String name;

    public EditProfileRequestTO() {
    }

    public EditProfileRequestTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("access_token")) {
            Object val = json.get("access_token");
            this.access_token = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.system.EditProfileRequestTO object is missing field 'access_token'");
        }
        if (json.containsKey("avatar")) {
            Object val = json.get("avatar");
            this.avatar = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.system.EditProfileRequestTO object is missing field 'avatar'");
        }
        if (json.containsKey("birthdate")) {
            Object val = json.get("birthdate");
            if (val instanceof Integer) {
                this.birthdate = ((Integer) val).longValue();
            } else {
                this.birthdate = ((Long) val).longValue();
            }
        } else {
            this.birthdate = 0;
        }
        if (json.containsKey("extra_fields")) {
            Object val = json.get("extra_fields");
            this.extra_fields = (String) val;
        } else {
            this.extra_fields = null;
        }
        if (json.containsKey("gender")) {
            Object val = json.get("gender");
            if (val instanceof Integer) {
                this.gender = ((Integer) val).longValue();
            } else {
                this.gender = ((Long) val).longValue();
            }
        } else {
            this.gender = 0;
        }
        if (json.containsKey("has_birthdate")) {
            Object val = json.get("has_birthdate");
            this.has_birthdate = ((Boolean) val).booleanValue();
        } else {
            this.has_birthdate = false;
        }
        if (json.containsKey("has_gender")) {
            Object val = json.get("has_gender");
            this.has_gender = ((Boolean) val).booleanValue();
        } else {
            this.has_gender = false;
        }
        if (json.containsKey("name")) {
            Object val = json.get("name");
            this.name = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.system.EditProfileRequestTO object is missing field 'name'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("access_token", this.access_token);
        obj.put("avatar", this.avatar);
        obj.put("birthdate", this.birthdate);
        obj.put("extra_fields", this.extra_fields);
        obj.put("gender", this.gender);
        obj.put("has_birthdate", this.has_birthdate);
        obj.put("has_gender", this.has_gender);
        obj.put("name", this.name);
        return obj;
    }

}
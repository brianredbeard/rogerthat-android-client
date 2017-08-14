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

public class FindServiceItemTO implements com.mobicage.rpc.IJSONable {

    public String avatar;
    public long avatar_id;
    public String description;
    public String description_branding;
    public String detail_text;
    public String email;
    public String name;
    public String qualified_identifier;

    public FindServiceItemTO() {
    }

    public FindServiceItemTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("avatar")) {
            Object val = json.get("avatar");
            this.avatar = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.service.FindServiceItemTO object is missing field 'avatar'");
        }
        if (json.containsKey("avatar_id")) {
            Object val = json.get("avatar_id");
            if (val instanceof Integer) {
                this.avatar_id = ((Integer) val).longValue();
            } else {
                this.avatar_id = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.service.FindServiceItemTO object is missing field 'avatar_id'");
        }
        if (json.containsKey("description")) {
            Object val = json.get("description");
            this.description = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.service.FindServiceItemTO object is missing field 'description'");
        }
        if (json.containsKey("description_branding")) {
            Object val = json.get("description_branding");
            this.description_branding = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.service.FindServiceItemTO object is missing field 'description_branding'");
        }
        if (json.containsKey("detail_text")) {
            Object val = json.get("detail_text");
            this.detail_text = (String) val;
        } else {
            this.detail_text = null;
        }
        if (json.containsKey("email")) {
            Object val = json.get("email");
            this.email = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.service.FindServiceItemTO object is missing field 'email'");
        }
        if (json.containsKey("name")) {
            Object val = json.get("name");
            this.name = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.service.FindServiceItemTO object is missing field 'name'");
        }
        if (json.containsKey("qualified_identifier")) {
            Object val = json.get("qualified_identifier");
            this.qualified_identifier = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.service.FindServiceItemTO object is missing field 'qualified_identifier'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("avatar", this.avatar);
        obj.put("avatar_id", this.avatar_id);
        obj.put("description", this.description);
        obj.put("description_branding", this.description_branding);
        obj.put("detail_text", this.detail_text);
        obj.put("email", this.email);
        obj.put("name", this.name);
        obj.put("qualified_identifier", this.qualified_identifier);
        return obj;
    }

}
/*
 * Copyright 2016 Mobicage NV
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
 * @@license_version:1.1@@
 */

package com.mobicage.to.system;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class IdentityTO implements com.mobicage.rpc.IJSONable {

    public long avatarId;
    public long birthdate;
    public String email;
    public long gender;
    public boolean hasBirthdate;
    public boolean hasGender;
    public String name;
    public String profileData;
    public String qualifiedIdentifier;

    public IdentityTO() {
    }

    public IdentityTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("avatarId")) {
            Object val = json.get("avatarId");
            this.avatarId = ((Long) val).longValue();
        } else {
            throw new IncompleteMessageException("com.mobicage.to.system.IdentityTO object is missing field 'avatarId'");
        }
        if (json.containsKey("birthdate")) {
            Object val = json.get("birthdate");
            this.birthdate = ((Long) val).longValue();
        } else {
            this.birthdate = 0;
        }
        if (json.containsKey("email")) {
            Object val = json.get("email");
            this.email = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.system.IdentityTO object is missing field 'email'");
        }
        if (json.containsKey("gender")) {
            Object val = json.get("gender");
            this.gender = ((Long) val).longValue();
        } else {
            this.gender = 0;
        }
        if (json.containsKey("hasBirthdate")) {
            Object val = json.get("hasBirthdate");
            this.hasBirthdate = ((Boolean) val).booleanValue();
        } else {
            this.hasBirthdate = false;
        }
        if (json.containsKey("hasGender")) {
            Object val = json.get("hasGender");
            this.hasGender = ((Boolean) val).booleanValue();
        } else {
            this.hasGender = false;
        }
        if (json.containsKey("name")) {
            Object val = json.get("name");
            this.name = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.system.IdentityTO object is missing field 'name'");
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
            throw new IncompleteMessageException("com.mobicage.to.system.IdentityTO object is missing field 'qualifiedIdentifier'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("avatarId", this.avatarId);
        obj.put("birthdate", this.birthdate);
        obj.put("email", this.email);
        obj.put("gender", this.gender);
        obj.put("hasBirthdate", this.hasBirthdate);
        obj.put("hasGender", this.hasGender);
        obj.put("name", this.name);
        obj.put("profileData", this.profileData);
        obj.put("qualifiedIdentifier", this.qualifiedIdentifier);
        return obj;
    }

}

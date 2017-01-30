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
package com.mobicage.rogerthat.plugins.friends;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jivesoftware.smack.util.Base64;

import com.mobicage.rpc.IncompleteMessageException;

public class Group implements com.mobicage.rpc.IJSONable {

    public String avatarHash;
    public String guid;
    public List<String> members;
    public String name;
    public byte[] avatar;

    public Group() {
    }

    @Override
    public boolean equals(Object other) {
        if (other == null || !(other instanceof Group))
            return false;
        return guid.equals(((Group) other).guid);
    }

    @Override
    public int hashCode() {
        return guid == null ? 0 : guid.hashCode();
    }

    public Group(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("guid")) {
            Object val = json.get("guid");
            this.guid = (String) val;
        } else {
            throw new IncompleteMessageException(
                "com.mobicage.rogerthat.plugins.friends.Group object is missing field 'guid'");
        }
        if (json.containsKey("name")) {
            Object val = json.get("name");
            this.name = (String) val;
        } else {
            throw new IncompleteMessageException(
                "com.mobicage.rogerthat.plugins.friends.Group object is missing field 'name'");
        }
        if (json.containsKey("members")) {
            org.json.simple.JSONArray val_arr = (org.json.simple.JSONArray) json.get("members");
            this.members = new ArrayList<String>();
            for (int i = 0; i < val_arr.size(); i++) {
                this.members.add((String) val_arr.get(i));
            }
        } else {
            throw new IncompleteMessageException(
                "com.mobicage.rogerthat.plugins.friends.Group object is missing field 'members'");
        }

        if (json.containsKey("avatar")) {
            Object val = json.get("avatar");
            this.avatar = Base64.decode((String) val);
        } else {
            this.avatar = null;
        }

        this.avatarHash = (String) json.get("avatarHash");
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("guid", this.guid);
        obj.put("name", this.name);
        if (this.members == null) {
            obj.put("members", null);
        } else {
            org.json.simple.JSONArray arr = new org.json.simple.JSONArray();
            for (int i = 0; i < this.members.size(); i++) {
                arr.add(this.members.get(i));
            }
            obj.put("members", arr);
        }

        if (this.avatar == null) {
            obj.put("avatar", null);
        } else {
            obj.put("avatar", Base64.encodeBytes(this.avatar, Base64.DONT_BREAK_LINES));
        }

        obj.put("avatarHash", this.avatarHash);
        return obj;
    }
}

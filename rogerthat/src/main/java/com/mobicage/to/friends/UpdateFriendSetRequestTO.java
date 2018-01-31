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

public class UpdateFriendSetRequestTO implements com.mobicage.rpc.IJSONable {

    public com.mobicage.to.friends.FriendTO added_friend;
    public String[] friends;
    public long version;

    public UpdateFriendSetRequestTO() {
    }

    @SuppressWarnings("unchecked")
    public UpdateFriendSetRequestTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("added_friend")) {
            Object val = json.get("added_friend");
            this.added_friend = val == null ? null : new com.mobicage.to.friends.FriendTO((Map<String, Object>) val);
        } else {
            throw new IncompleteMessageException("com.mobicage.to.friends.UpdateFriendSetRequestTO object is missing field 'added_friend'");
        }
        if (json.containsKey("friends")) {
            org.json.simple.JSONArray val_arr = (org.json.simple.JSONArray) json.get("friends");
            if (val_arr == null) {
                this.friends = null;
            } else {
                this.friends = new String[val_arr.size()];
                for (int i=0; i < val_arr.size(); i++) {
                    this.friends[i] = (String) val_arr.get(i);
                }
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.friends.UpdateFriendSetRequestTO object is missing field 'friends'");
        }
        if (json.containsKey("version")) {
            Object val = json.get("version");
            if (val instanceof Integer) {
                this.version = ((Integer) val).longValue();
            } else {
                this.version = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.friends.UpdateFriendSetRequestTO object is missing field 'version'");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("added_friend", this.added_friend == null ? null : this.added_friend.toJSONMap());
        if (this.friends == null) {
            obj.put("friends", null);
        } else {
            org.json.simple.JSONArray arr = new org.json.simple.JSONArray();
            for (int i=0; i < this.friends.length; i++) {
                arr.add(this.friends[i]);
            }
            obj.put("friends", arr);
        }
        obj.put("version", this.version);
        return obj;
    }

}

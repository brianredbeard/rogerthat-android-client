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

package com.mobicage.to.friends;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class GetFriendEmailsResponseTO implements com.mobicage.rpc.IJSONable {

    public String[] emails;
    public long friend_set_version;
    public long generation;

    public GetFriendEmailsResponseTO() {
    }

    public GetFriendEmailsResponseTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("emails")) {
            org.json.simple.JSONArray val_arr = (org.json.simple.JSONArray) json.get("emails");
            if (val_arr == null) {
                this.emails = null;
            } else {
                this.emails = new String[val_arr.size()];
                for (int i=0; i < val_arr.size(); i++) {
                    this.emails[i] = (String) val_arr.get(i);
                }
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.friends.GetFriendEmailsResponseTO object is missing field 'emails'");
        }
        if (json.containsKey("friend_set_version")) {
            Object val = json.get("friend_set_version");
            if (val instanceof Integer) {
                this.friend_set_version = ((Integer) val).longValue();
            } else {
                this.friend_set_version = ((Long) val).longValue();
            }
        } else {
            this.friend_set_version = 0;
        }
        if (json.containsKey("generation")) {
            Object val = json.get("generation");
            if (val instanceof Integer) {
                this.generation = ((Integer) val).longValue();
            } else {
                this.generation = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.friends.GetFriendEmailsResponseTO object is missing field 'generation'");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        if (this.emails == null) {
            obj.put("emails", null);
        } else {
            org.json.simple.JSONArray arr = new org.json.simple.JSONArray();
            for (int i=0; i < this.emails.length; i++) {
                arr.add(this.emails[i]);
            }
            obj.put("emails", arr);
        }
        obj.put("friend_set_version", this.friend_set_version);
        obj.put("generation", this.generation);
        return obj;
    }

}

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

package com.mobicage.to.friends;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class GetFriendsListResponseTO implements com.mobicage.rpc.IJSONable {

    public com.mobicage.to.friends.FriendTO[] friends;
    public long generation;

    public GetFriendsListResponseTO() {
    }

    @SuppressWarnings("unchecked")
    public GetFriendsListResponseTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("friends")) {
            org.json.simple.JSONArray val_arr = (org.json.simple.JSONArray) json.get("friends");
            if (val_arr == null) {
                this.friends = null;
            } else {
                this.friends = new com.mobicage.to.friends.FriendTO[val_arr.size()];
                for (int i=0; i < val_arr.size(); i++) {
                    Object item = val_arr.get(i);
                    if (item != null) {
                        this.friends[i] = new com.mobicage.to.friends.FriendTO((Map<String, Object>) item);
                    }
                }
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.friends.GetFriendsListResponseTO object is missing field 'friends'");
        }
        if (json.containsKey("generation")) {
            Object val = json.get("generation");
            if (val instanceof Integer) {
                this.generation = ((Integer) val).longValue();
            } else {
                this.generation = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.friends.GetFriendsListResponseTO object is missing field 'generation'");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        if (this.friends == null) {
            obj.put("friends", null);
        } else {
            org.json.simple.JSONArray arr = new org.json.simple.JSONArray();
            for (int i=0; i < this.friends.length; i++) {
                arr.add(this.friends[i].toJSONMap());
            }
            obj.put("friends", arr);
        }
        obj.put("generation", this.generation);
        return obj;
    }

}

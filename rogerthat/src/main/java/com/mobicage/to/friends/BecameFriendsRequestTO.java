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

public class BecameFriendsRequestTO implements com.mobicage.rpc.IJSONable {

    public com.mobicage.to.friends.FriendRelationTO friend;
    public String user;

    public BecameFriendsRequestTO() {
    }

    @SuppressWarnings("unchecked")
    public BecameFriendsRequestTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("friend")) {
            Object val = json.get("friend");
            this.friend = val == null ? null : new com.mobicage.to.friends.FriendRelationTO((Map<String, Object>) val);
        } else {
            throw new IncompleteMessageException("com.mobicage.to.friends.BecameFriendsRequestTO object is missing field 'friend'");
        }
        if (json.containsKey("user")) {
            Object val = json.get("user");
            this.user = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.friends.BecameFriendsRequestTO object is missing field 'user'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("friend", this.friend == null ? null : this.friend.toJSONMap());
        obj.put("user", this.user);
        return obj;
    }

}

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

public class BreakFriendshipRequestTO implements com.mobicage.rpc.IJSONable {

    public String friend;

    public BreakFriendshipRequestTO() {
    }

    public BreakFriendshipRequestTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("friend")) {
            Object val = json.get("friend");
            this.friend = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.friends.BreakFriendshipRequestTO object is missing field 'friend'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("friend", this.friend);
        return obj;
    }

}
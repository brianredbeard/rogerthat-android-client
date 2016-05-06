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

public class FindFriendRequestTO implements com.mobicage.rpc.IJSONable {

    public long avatar_size;
    public String cursor;
    public String search_string;

    public FindFriendRequestTO() {
    }

    public FindFriendRequestTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("avatar_size")) {
            Object val = json.get("avatar_size");
            this.avatar_size = ((Long) val).longValue();
        } else {
            this.avatar_size = 50;
        }
        if (json.containsKey("cursor")) {
            Object val = json.get("cursor");
            this.cursor = (String) val;
        } else {
            this.cursor = null;
        }
        if (json.containsKey("search_string")) {
            Object val = json.get("search_string");
            this.search_string = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.friends.FindFriendRequestTO object is missing field 'search_string'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("avatar_size", this.avatar_size);
        obj.put("cursor", this.cursor);
        obj.put("search_string", this.search_string);
        return obj;
    }

}
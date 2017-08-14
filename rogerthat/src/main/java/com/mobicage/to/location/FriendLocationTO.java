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

package com.mobicage.to.location;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class FriendLocationTO implements com.mobicage.rpc.IJSONable {

    public com.mobicage.to.activity.GeoPointWithTimestampTO location;
    public String email;

    public FriendLocationTO() {
    }

    @SuppressWarnings("unchecked")
    public FriendLocationTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("location")) {
            Object val = json.get("location");
            this.location = val == null ? null : new com.mobicage.to.activity.GeoPointWithTimestampTO((Map<String, Object>) val);
        } else {
            throw new IncompleteMessageException("com.mobicage.to.location.FriendLocationTO object is missing field 'location'");
        }
        if (json.containsKey("email")) {
            Object val = json.get("email");
            this.email = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.location.FriendLocationTO object is missing field 'email'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("location", this.location == null ? null : this.location.toJSONMap());
        obj.put("email", this.email);
        return obj;
    }

}
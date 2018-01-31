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

package com.mobicage.to.location;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class BeaconOutOfReachRequestTO implements com.mobicage.rpc.IJSONable {

    public String friend_email;
    public String name;
    public String uuid;

    public BeaconOutOfReachRequestTO() {
    }

    public BeaconOutOfReachRequestTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("friend_email")) {
            Object val = json.get("friend_email");
            this.friend_email = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.location.BeaconOutOfReachRequestTO object is missing field 'friend_email'");
        }
        if (json.containsKey("name")) {
            Object val = json.get("name");
            this.name = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.location.BeaconOutOfReachRequestTO object is missing field 'name'");
        }
        if (json.containsKey("uuid")) {
            Object val = json.get("uuid");
            this.uuid = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.location.BeaconOutOfReachRequestTO object is missing field 'uuid'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("friend_email", this.friend_email);
        obj.put("name", this.name);
        obj.put("uuid", this.uuid);
        return obj;
    }

}

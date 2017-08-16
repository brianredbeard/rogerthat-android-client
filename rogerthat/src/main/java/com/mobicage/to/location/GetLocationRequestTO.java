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

public class GetLocationRequestTO implements com.mobicage.rpc.IJSONable {

    public String friend;
    public boolean high_prio;
    public long target;

    public GetLocationRequestTO() {
    }

    public GetLocationRequestTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("friend")) {
            Object val = json.get("friend");
            this.friend = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.location.GetLocationRequestTO object is missing field 'friend'");
        }
        if (json.containsKey("high_prio")) {
            Object val = json.get("high_prio");
            this.high_prio = ((Boolean) val).booleanValue();
        } else {
            throw new IncompleteMessageException("com.mobicage.to.location.GetLocationRequestTO object is missing field 'high_prio'");
        }
        if (json.containsKey("target")) {
            Object val = json.get("target");
            if (val instanceof Integer) {
                this.target = ((Integer) val).longValue();
            } else {
                this.target = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.location.GetLocationRequestTO object is missing field 'target'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("friend", this.friend);
        obj.put("high_prio", this.high_prio);
        obj.put("target", this.target);
        return obj;
    }

}

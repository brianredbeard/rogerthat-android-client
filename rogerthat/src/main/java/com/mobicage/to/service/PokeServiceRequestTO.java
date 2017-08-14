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

package com.mobicage.to.service;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class PokeServiceRequestTO implements com.mobicage.rpc.IJSONable {

    public String context;
    public String email;
    public String hashed_tag;
    public long timestamp;

    public PokeServiceRequestTO() {
    }

    public PokeServiceRequestTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("context")) {
            Object val = json.get("context");
            this.context = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.service.PokeServiceRequestTO object is missing field 'context'");
        }
        if (json.containsKey("email")) {
            Object val = json.get("email");
            this.email = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.service.PokeServiceRequestTO object is missing field 'email'");
        }
        if (json.containsKey("hashed_tag")) {
            Object val = json.get("hashed_tag");
            this.hashed_tag = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.service.PokeServiceRequestTO object is missing field 'hashed_tag'");
        }
        if (json.containsKey("timestamp")) {
            Object val = json.get("timestamp");
            if (val instanceof Integer) {
                this.timestamp = ((Integer) val).longValue();
            } else {
                this.timestamp = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.service.PokeServiceRequestTO object is missing field 'timestamp'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("context", this.context);
        obj.put("email", this.email);
        obj.put("hashed_tag", this.hashed_tag);
        obj.put("timestamp", this.timestamp);
        return obj;
    }

}
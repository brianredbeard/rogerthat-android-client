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

public class SendApiCallRequestTO implements com.mobicage.rpc.IJSONable {

    public String hashed_tag;
    public long id;
    public String method;
    public String params;
    public String service;

    public SendApiCallRequestTO() {
    }

    public SendApiCallRequestTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("hashed_tag")) {
            Object val = json.get("hashed_tag");
            this.hashed_tag = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.service.SendApiCallRequestTO object is missing field 'hashed_tag'");
        }
        if (json.containsKey("id")) {
            Object val = json.get("id");
            if (val instanceof Integer) {
                this.id = ((Integer) val).longValue();
            } else {
                this.id = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.service.SendApiCallRequestTO object is missing field 'id'");
        }
        if (json.containsKey("method")) {
            Object val = json.get("method");
            this.method = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.service.SendApiCallRequestTO object is missing field 'method'");
        }
        if (json.containsKey("params")) {
            Object val = json.get("params");
            this.params = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.service.SendApiCallRequestTO object is missing field 'params'");
        }
        if (json.containsKey("service")) {
            Object val = json.get("service");
            this.service = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.service.SendApiCallRequestTO object is missing field 'service'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("hashed_tag", this.hashed_tag);
        obj.put("id", this.id);
        obj.put("method", this.method);
        obj.put("params", this.params);
        obj.put("service", this.service);
        return obj;
    }

}
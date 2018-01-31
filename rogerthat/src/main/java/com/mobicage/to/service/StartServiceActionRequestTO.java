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

package com.mobicage.to.service;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class StartServiceActionRequestTO implements com.mobicage.rpc.IJSONable {

    public String action;
    public String context;
    public String email;
    public String message_flow_run_id;
    public String static_flow_hash;
    public long timestamp;

    public StartServiceActionRequestTO() {
    }

    public StartServiceActionRequestTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("action")) {
            Object val = json.get("action");
            this.action = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.service.StartServiceActionRequestTO object is missing field 'action'");
        }
        if (json.containsKey("context")) {
            Object val = json.get("context");
            this.context = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.service.StartServiceActionRequestTO object is missing field 'context'");
        }
        if (json.containsKey("email")) {
            Object val = json.get("email");
            this.email = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.service.StartServiceActionRequestTO object is missing field 'email'");
        }
        if (json.containsKey("message_flow_run_id")) {
            Object val = json.get("message_flow_run_id");
            this.message_flow_run_id = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.service.StartServiceActionRequestTO object is missing field 'message_flow_run_id'");
        }
        if (json.containsKey("static_flow_hash")) {
            Object val = json.get("static_flow_hash");
            this.static_flow_hash = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.service.StartServiceActionRequestTO object is missing field 'static_flow_hash'");
        }
        if (json.containsKey("timestamp")) {
            Object val = json.get("timestamp");
            if (val instanceof Integer) {
                this.timestamp = ((Integer) val).longValue();
            } else {
                this.timestamp = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.service.StartServiceActionRequestTO object is missing field 'timestamp'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("action", this.action);
        obj.put("context", this.context);
        obj.put("email", this.email);
        obj.put("message_flow_run_id", this.message_flow_run_id);
        obj.put("static_flow_hash", this.static_flow_hash);
        obj.put("timestamp", this.timestamp);
        return obj;
    }

}

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

package com.mobicage.to.messaging.jsmfr;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class FlowStartedRequestTO implements com.mobicage.rpc.IJSONable {

    public String message_flow_run_id;
    public String service;
    public String static_flow_hash;
    public String thread_key;

    public FlowStartedRequestTO() {
    }

    public FlowStartedRequestTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("message_flow_run_id")) {
            Object val = json.get("message_flow_run_id");
            this.message_flow_run_id = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.jsmfr.FlowStartedRequestTO object is missing field 'message_flow_run_id'");
        }
        if (json.containsKey("service")) {
            Object val = json.get("service");
            this.service = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.jsmfr.FlowStartedRequestTO object is missing field 'service'");
        }
        if (json.containsKey("static_flow_hash")) {
            Object val = json.get("static_flow_hash");
            this.static_flow_hash = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.jsmfr.FlowStartedRequestTO object is missing field 'static_flow_hash'");
        }
        if (json.containsKey("thread_key")) {
            Object val = json.get("thread_key");
            this.thread_key = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.jsmfr.FlowStartedRequestTO object is missing field 'thread_key'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("message_flow_run_id", this.message_flow_run_id);
        obj.put("service", this.service);
        obj.put("static_flow_hash", this.static_flow_hash);
        obj.put("thread_key", this.thread_key);
        return obj;
    }

}
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

public class JsMessageFlowMemberRunTO implements com.mobicage.rpc.IJSONable {

    public String flow_params;
    public String hashed_tag;
    public String message_flow_run_id;
    public String parent_message_key;
    public String sender;
    public String service_action;

    public JsMessageFlowMemberRunTO() {
    }

    public JsMessageFlowMemberRunTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("flow_params")) {
            Object val = json.get("flow_params");
            this.flow_params = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.jsmfr.JsMessageFlowMemberRunTO object is missing field 'flow_params'");
        }
        if (json.containsKey("hashed_tag")) {
            Object val = json.get("hashed_tag");
            this.hashed_tag = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.jsmfr.JsMessageFlowMemberRunTO object is missing field 'hashed_tag'");
        }
        if (json.containsKey("message_flow_run_id")) {
            Object val = json.get("message_flow_run_id");
            this.message_flow_run_id = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.jsmfr.JsMessageFlowMemberRunTO object is missing field 'message_flow_run_id'");
        }
        if (json.containsKey("parent_message_key")) {
            Object val = json.get("parent_message_key");
            this.parent_message_key = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.jsmfr.JsMessageFlowMemberRunTO object is missing field 'parent_message_key'");
        }
        if (json.containsKey("sender")) {
            Object val = json.get("sender");
            this.sender = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.jsmfr.JsMessageFlowMemberRunTO object is missing field 'sender'");
        }
        if (json.containsKey("service_action")) {
            Object val = json.get("service_action");
            this.service_action = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.jsmfr.JsMessageFlowMemberRunTO object is missing field 'service_action'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("flow_params", this.flow_params);
        obj.put("hashed_tag", this.hashed_tag);
        obj.put("message_flow_run_id", this.message_flow_run_id);
        obj.put("parent_message_key", this.parent_message_key);
        obj.put("sender", this.sender);
        obj.put("service_action", this.service_action);
        return obj;
    }

}

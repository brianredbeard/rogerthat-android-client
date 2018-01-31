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

package com.mobicage.to.messaging.jsmfr;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class MessageFlowFinishedRequestTO implements com.mobicage.rpc.IJSONable {

    public String end_id;
    public String message_flow_run_id;
    public String parent_message_key;

    public MessageFlowFinishedRequestTO() {
    }

    public MessageFlowFinishedRequestTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("end_id")) {
            Object val = json.get("end_id");
            this.end_id = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.jsmfr.MessageFlowFinishedRequestTO object is missing field 'end_id'");
        }
        if (json.containsKey("message_flow_run_id")) {
            Object val = json.get("message_flow_run_id");
            this.message_flow_run_id = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.jsmfr.MessageFlowFinishedRequestTO object is missing field 'message_flow_run_id'");
        }
        if (json.containsKey("parent_message_key")) {
            Object val = json.get("parent_message_key");
            this.parent_message_key = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.jsmfr.MessageFlowFinishedRequestTO object is missing field 'parent_message_key'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("end_id", this.end_id);
        obj.put("message_flow_run_id", this.message_flow_run_id);
        obj.put("parent_message_key", this.parent_message_key);
        return obj;
    }

}

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

package com.mobicage.to.messaging;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class StartFlowRequestTO implements com.mobicage.rpc.IJSONable {

    public String[] attachments_to_dwnl;
    public String[] brandings_to_dwnl;
    public String flow_params;
    public String message_flow_run_id;
    public String parent_message_key;
    public String service;
    public String static_flow;
    public String static_flow_hash;

    public StartFlowRequestTO() {
    }

    public StartFlowRequestTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("attachments_to_dwnl")) {
            org.json.simple.JSONArray val_arr = (org.json.simple.JSONArray) json.get("attachments_to_dwnl");
            if (val_arr == null) {
                this.attachments_to_dwnl = null;
            } else {
                this.attachments_to_dwnl = new String[val_arr.size()];
                for (int i=0; i < val_arr.size(); i++) {
                    this.attachments_to_dwnl[i] = (String) val_arr.get(i);
                }
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.StartFlowRequestTO object is missing field 'attachments_to_dwnl'");
        }
        if (json.containsKey("brandings_to_dwnl")) {
            org.json.simple.JSONArray val_arr = (org.json.simple.JSONArray) json.get("brandings_to_dwnl");
            if (val_arr == null) {
                this.brandings_to_dwnl = null;
            } else {
                this.brandings_to_dwnl = new String[val_arr.size()];
                for (int i=0; i < val_arr.size(); i++) {
                    this.brandings_to_dwnl[i] = (String) val_arr.get(i);
                }
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.StartFlowRequestTO object is missing field 'brandings_to_dwnl'");
        }
        if (json.containsKey("flow_params")) {
            Object val = json.get("flow_params");
            this.flow_params = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.StartFlowRequestTO object is missing field 'flow_params'");
        }
        if (json.containsKey("message_flow_run_id")) {
            Object val = json.get("message_flow_run_id");
            this.message_flow_run_id = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.StartFlowRequestTO object is missing field 'message_flow_run_id'");
        }
        if (json.containsKey("parent_message_key")) {
            Object val = json.get("parent_message_key");
            this.parent_message_key = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.StartFlowRequestTO object is missing field 'parent_message_key'");
        }
        if (json.containsKey("service")) {
            Object val = json.get("service");
            this.service = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.StartFlowRequestTO object is missing field 'service'");
        }
        if (json.containsKey("static_flow")) {
            Object val = json.get("static_flow");
            this.static_flow = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.StartFlowRequestTO object is missing field 'static_flow'");
        }
        if (json.containsKey("static_flow_hash")) {
            Object val = json.get("static_flow_hash");
            this.static_flow_hash = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.StartFlowRequestTO object is missing field 'static_flow_hash'");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        if (this.attachments_to_dwnl == null) {
            obj.put("attachments_to_dwnl", null);
        } else {
            org.json.simple.JSONArray arr = new org.json.simple.JSONArray();
            for (int i=0; i < this.attachments_to_dwnl.length; i++) {
                arr.add(this.attachments_to_dwnl[i]);
            }
            obj.put("attachments_to_dwnl", arr);
        }
        if (this.brandings_to_dwnl == null) {
            obj.put("brandings_to_dwnl", null);
        } else {
            org.json.simple.JSONArray arr = new org.json.simple.JSONArray();
            for (int i=0; i < this.brandings_to_dwnl.length; i++) {
                arr.add(this.brandings_to_dwnl[i]);
            }
            obj.put("brandings_to_dwnl", arr);
        }
        obj.put("flow_params", this.flow_params);
        obj.put("message_flow_run_id", this.message_flow_run_id);
        obj.put("parent_message_key", this.parent_message_key);
        obj.put("service", this.service);
        obj.put("static_flow", this.static_flow);
        obj.put("static_flow_hash", this.static_flow_hash);
        return obj;
    }

}

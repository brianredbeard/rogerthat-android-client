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

package com.mobicage.to.messaging.jsmfr;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class MessageFlowMemberResultRequestTO implements com.mobicage.rpc.IJSONable {

    public com.mobicage.to.messaging.jsmfr.JsMessageFlowMemberRunTO run;
    public boolean email_admins;
    public String[] emails;
    public String end_id;
    public String flush_id;
    public String message_flow_name;
    public boolean results_email;

    public MessageFlowMemberResultRequestTO() {
    }

    @SuppressWarnings("unchecked")
    public MessageFlowMemberResultRequestTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("run")) {
            Object val = json.get("run");
            this.run = val == null ? null : new com.mobicage.to.messaging.jsmfr.JsMessageFlowMemberRunTO((Map<String, Object>) val);
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.jsmfr.MessageFlowMemberResultRequestTO object is missing field 'run'");
        }
        if (json.containsKey("email_admins")) {
            Object val = json.get("email_admins");
            this.email_admins = ((Boolean) val).booleanValue();
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.jsmfr.MessageFlowMemberResultRequestTO object is missing field 'email_admins'");
        }
        if (json.containsKey("emails")) {
            org.json.simple.JSONArray val_arr = (org.json.simple.JSONArray) json.get("emails");
            if (val_arr == null) {
                this.emails = null;
            } else {
                this.emails = new String[val_arr.size()];
                for (int i=0; i < val_arr.size(); i++) {
                    this.emails[i] = (String) val_arr.get(i);
                }
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.jsmfr.MessageFlowMemberResultRequestTO object is missing field 'emails'");
        }
        if (json.containsKey("end_id")) {
            Object val = json.get("end_id");
            this.end_id = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.jsmfr.MessageFlowMemberResultRequestTO object is missing field 'end_id'");
        }
        if (json.containsKey("flush_id")) {
            Object val = json.get("flush_id");
            this.flush_id = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.jsmfr.MessageFlowMemberResultRequestTO object is missing field 'flush_id'");
        }
        if (json.containsKey("message_flow_name")) {
            Object val = json.get("message_flow_name");
            this.message_flow_name = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.jsmfr.MessageFlowMemberResultRequestTO object is missing field 'message_flow_name'");
        }
        if (json.containsKey("results_email")) {
            Object val = json.get("results_email");
            this.results_email = ((Boolean) val).booleanValue();
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.jsmfr.MessageFlowMemberResultRequestTO object is missing field 'results_email'");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("run", this.run == null ? null : this.run.toJSONMap());
        obj.put("email_admins", this.email_admins);
        if (this.emails == null) {
            obj.put("emails", null);
        } else {
            org.json.simple.JSONArray arr = new org.json.simple.JSONArray();
            for (int i=0; i < this.emails.length; i++) {
                arr.add(this.emails[i]);
            }
            obj.put("emails", arr);
        }
        obj.put("end_id", this.end_id);
        obj.put("flush_id", this.flush_id);
        obj.put("message_flow_name", this.message_flow_name);
        obj.put("results_email", this.results_email);
        return obj;
    }

}
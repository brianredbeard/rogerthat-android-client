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

public class MarkMessagesAsReadRequestTO implements com.mobicage.rpc.IJSONable {

    public String[] message_keys;
    public String parent_message_key;

    public MarkMessagesAsReadRequestTO() {
    }

    public MarkMessagesAsReadRequestTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("message_keys")) {
            org.json.simple.JSONArray val_arr = (org.json.simple.JSONArray) json.get("message_keys");
            if (val_arr == null) {
                this.message_keys = null;
            } else {
                this.message_keys = new String[val_arr.size()];
                for (int i=0; i < val_arr.size(); i++) {
                    this.message_keys[i] = (String) val_arr.get(i);
                }
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.MarkMessagesAsReadRequestTO object is missing field 'message_keys'");
        }
        if (json.containsKey("parent_message_key")) {
            Object val = json.get("parent_message_key");
            this.parent_message_key = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.MarkMessagesAsReadRequestTO object is missing field 'parent_message_key'");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        if (this.message_keys == null) {
            obj.put("message_keys", null);
        } else {
            org.json.simple.JSONArray arr = new org.json.simple.JSONArray();
            for (int i=0; i < this.message_keys.length; i++) {
                arr.add(this.message_keys[i]);
            }
            obj.put("message_keys", arr);
        }
        obj.put("parent_message_key", this.parent_message_key);
        return obj;
    }

}

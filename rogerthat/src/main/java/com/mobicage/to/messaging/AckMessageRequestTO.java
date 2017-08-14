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

public class AckMessageRequestTO implements com.mobicage.rpc.IJSONable {

    public String button_id;
    public String custom_reply;
    public String message_key;
    public String parent_message_key;
    public long timestamp;

    public AckMessageRequestTO() {
    }

    public AckMessageRequestTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("button_id")) {
            Object val = json.get("button_id");
            this.button_id = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.AckMessageRequestTO object is missing field 'button_id'");
        }
        if (json.containsKey("custom_reply")) {
            Object val = json.get("custom_reply");
            this.custom_reply = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.AckMessageRequestTO object is missing field 'custom_reply'");
        }
        if (json.containsKey("message_key")) {
            Object val = json.get("message_key");
            this.message_key = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.AckMessageRequestTO object is missing field 'message_key'");
        }
        if (json.containsKey("parent_message_key")) {
            Object val = json.get("parent_message_key");
            this.parent_message_key = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.AckMessageRequestTO object is missing field 'parent_message_key'");
        }
        if (json.containsKey("timestamp")) {
            Object val = json.get("timestamp");
            if (val instanceof Integer) {
                this.timestamp = ((Integer) val).longValue();
            } else {
                this.timestamp = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.AckMessageRequestTO object is missing field 'timestamp'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("button_id", this.button_id);
        obj.put("custom_reply", this.custom_reply);
        obj.put("message_key", this.message_key);
        obj.put("parent_message_key", this.parent_message_key);
        obj.put("timestamp", this.timestamp);
        return obj;
    }

}
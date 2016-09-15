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

package com.mobicage.to.messaging;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class SendMessageRequestTO implements com.mobicage.rpc.IJSONable {

    public com.mobicage.to.messaging.AttachmentTO[] attachments;
    public com.mobicage.to.messaging.ButtonTO[] buttons;
    public long flags;
    public String key;
    public String[] members;
    public String message;
    public String parent_key;
    public long priority;
    public String sender_reply;
    public long timeout;

    public SendMessageRequestTO() {
    }

    @SuppressWarnings("unchecked")
    public SendMessageRequestTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("attachments")) {
            org.json.simple.JSONArray val_arr = (org.json.simple.JSONArray) json.get("attachments");
            if (val_arr == null) {
                this.attachments = null;
            } else {
                this.attachments = new com.mobicage.to.messaging.AttachmentTO[val_arr.size()];
                for (int i=0; i < val_arr.size(); i++) {
                    Object item = val_arr.get(i);
                    if (item != null) {
                        this.attachments[i] = new com.mobicage.to.messaging.AttachmentTO((Map<String, Object>) item);
                    }
                }
            }
        } else {
            this.attachments = new com.mobicage.to.messaging.AttachmentTO[0];
        }
        if (json.containsKey("buttons")) {
            org.json.simple.JSONArray val_arr = (org.json.simple.JSONArray) json.get("buttons");
            if (val_arr == null) {
                this.buttons = null;
            } else {
                this.buttons = new com.mobicage.to.messaging.ButtonTO[val_arr.size()];
                for (int i=0; i < val_arr.size(); i++) {
                    Object item = val_arr.get(i);
                    if (item != null) {
                        this.buttons[i] = new com.mobicage.to.messaging.ButtonTO((Map<String, Object>) item);
                    }
                }
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.SendMessageRequestTO object is missing field 'buttons'");
        }
        if (json.containsKey("flags")) {
            Object val = json.get("flags");
            this.flags = ((Long) val).longValue();
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.SendMessageRequestTO object is missing field 'flags'");
        }
        if (json.containsKey("key")) {
            Object val = json.get("key");
            this.key = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.SendMessageRequestTO object is missing field 'key'");
        }
        if (json.containsKey("members")) {
            org.json.simple.JSONArray val_arr = (org.json.simple.JSONArray) json.get("members");
            if (val_arr == null) {
                this.members = null;
            } else {
                this.members = new String[val_arr.size()];
                for (int i=0; i < val_arr.size(); i++) {
                    this.members[i] = (String) val_arr.get(i);
                }
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.SendMessageRequestTO object is missing field 'members'");
        }
        if (json.containsKey("message")) {
            Object val = json.get("message");
            this.message = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.SendMessageRequestTO object is missing field 'message'");
        }
        if (json.containsKey("parent_key")) {
            Object val = json.get("parent_key");
            this.parent_key = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.SendMessageRequestTO object is missing field 'parent_key'");
        }
        if (json.containsKey("priority")) {
            Object val = json.get("priority");
            this.priority = ((Long) val).longValue();
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.SendMessageRequestTO object is missing field 'priority'");
        }
        if (json.containsKey("sender_reply")) {
            Object val = json.get("sender_reply");
            this.sender_reply = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.SendMessageRequestTO object is missing field 'sender_reply'");
        }
        if (json.containsKey("timeout")) {
            Object val = json.get("timeout");
            this.timeout = ((Long) val).longValue();
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.SendMessageRequestTO object is missing field 'timeout'");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        if (this.attachments == null) {
            obj.put("attachments", null);
        } else {
            org.json.simple.JSONArray arr = new org.json.simple.JSONArray();
            for (int i=0; i < this.attachments.length; i++) {
                arr.add(this.attachments[i].toJSONMap());
            }
            obj.put("attachments", arr);
        }
        if (this.buttons == null) {
            obj.put("buttons", null);
        } else {
            org.json.simple.JSONArray arr = new org.json.simple.JSONArray();
            for (int i=0; i < this.buttons.length; i++) {
                arr.add(this.buttons[i].toJSONMap());
            }
            obj.put("buttons", arr);
        }
        obj.put("flags", this.flags);
        obj.put("key", this.key);
        if (this.members == null) {
            obj.put("members", null);
        } else {
            org.json.simple.JSONArray arr = new org.json.simple.JSONArray();
            for (int i=0; i < this.members.length; i++) {
                arr.add(this.members[i]);
            }
            obj.put("members", arr);
        }
        obj.put("message", this.message);
        obj.put("parent_key", this.parent_key);
        obj.put("priority", this.priority);
        obj.put("sender_reply", this.sender_reply);
        obj.put("timeout", this.timeout);
        return obj;
    }

}

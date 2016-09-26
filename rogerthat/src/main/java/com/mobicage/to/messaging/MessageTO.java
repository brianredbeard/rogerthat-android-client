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

public class MessageTO implements com.mobicage.rpc.IJSONable {

    public com.mobicage.to.messaging.AttachmentTO[] attachments;
    public com.mobicage.to.messaging.ButtonTO[] buttons;
    public com.mobicage.to.messaging.MemberStatusTO[] members;
    public long alert_flags;
    public String branding;
    public String broadcast_type;
    public String context;
    public long default_priority;
    public boolean default_sticky;
    public long dismiss_button_ui_flags;
    public long flags;
    public String key;
    public String message;
    public long message_type;
    public String parent_key;
    public long priority;
    public String sender;
    public long threadTimestamp;
    public String thread_avatar_hash;
    public String thread_background_color;
    public long thread_size;
    public String thread_text_color;
    public long timeout;
    public long timestamp;

    public MessageTO() {
    }

    @SuppressWarnings("unchecked")
    public MessageTO(Map<String, Object> json) throws IncompleteMessageException {
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
            throw new IncompleteMessageException("com.mobicage.to.messaging.MessageTO object is missing field 'buttons'");
        }
        if (json.containsKey("members")) {
            org.json.simple.JSONArray val_arr = (org.json.simple.JSONArray) json.get("members");
            if (val_arr == null) {
                this.members = null;
            } else {
                this.members = new com.mobicage.to.messaging.MemberStatusTO[val_arr.size()];
                for (int i=0; i < val_arr.size(); i++) {
                    Object item = val_arr.get(i);
                    if (item != null) {
                        this.members[i] = new com.mobicage.to.messaging.MemberStatusTO((Map<String, Object>) item);
                    }
                }
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.MessageTO object is missing field 'members'");
        }
        if (json.containsKey("alert_flags")) {
            Object val = json.get("alert_flags");
            if (val instanceof Integer) {
                this.alert_flags = ((Integer) val).longValue();
            } else {
                this.alert_flags = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.MessageTO object is missing field 'alert_flags'");
        }
        if (json.containsKey("branding")) {
            Object val = json.get("branding");
            this.branding = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.MessageTO object is missing field 'branding'");
        }
        if (json.containsKey("broadcast_type")) {
            Object val = json.get("broadcast_type");
            this.broadcast_type = (String) val;
        } else {
            this.broadcast_type = null;
        }
        if (json.containsKey("context")) {
            Object val = json.get("context");
            this.context = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.MessageTO object is missing field 'context'");
        }
        if (json.containsKey("default_priority")) {
            Object val = json.get("default_priority");
            if (val instanceof Integer) {
                this.default_priority = ((Integer) val).longValue();
            } else {
                this.default_priority = ((Long) val).longValue();
            }
        } else {
            this.default_priority = 1;
        }
        if (json.containsKey("default_sticky")) {
            Object val = json.get("default_sticky");
            this.default_sticky = ((Boolean) val).booleanValue();
        } else {
            this.default_sticky = false;
        }
        if (json.containsKey("dismiss_button_ui_flags")) {
            Object val = json.get("dismiss_button_ui_flags");
            if (val instanceof Integer) {
                this.dismiss_button_ui_flags = ((Integer) val).longValue();
            } else {
                this.dismiss_button_ui_flags = ((Long) val).longValue();
            }
        } else {
            this.dismiss_button_ui_flags = 0;
        }
        if (json.containsKey("flags")) {
            Object val = json.get("flags");
            if (val instanceof Integer) {
                this.flags = ((Integer) val).longValue();
            } else {
                this.flags = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.MessageTO object is missing field 'flags'");
        }
        if (json.containsKey("key")) {
            Object val = json.get("key");
            this.key = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.MessageTO object is missing field 'key'");
        }
        if (json.containsKey("message")) {
            Object val = json.get("message");
            this.message = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.MessageTO object is missing field 'message'");
        }
        if (json.containsKey("message_type")) {
            Object val = json.get("message_type");
            if (val instanceof Integer) {
                this.message_type = ((Integer) val).longValue();
            } else {
                this.message_type = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.MessageTO object is missing field 'message_type'");
        }
        if (json.containsKey("parent_key")) {
            Object val = json.get("parent_key");
            this.parent_key = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.MessageTO object is missing field 'parent_key'");
        }
        if (json.containsKey("priority")) {
            Object val = json.get("priority");
            if (val instanceof Integer) {
                this.priority = ((Integer) val).longValue();
            } else {
                this.priority = ((Long) val).longValue();
            }
        } else {
            this.priority = 1;
        }
        if (json.containsKey("sender")) {
            Object val = json.get("sender");
            this.sender = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.MessageTO object is missing field 'sender'");
        }
        if (json.containsKey("threadTimestamp")) {
            Object val = json.get("threadTimestamp");
            if (val instanceof Integer) {
                this.threadTimestamp = ((Integer) val).longValue();
            } else {
                this.threadTimestamp = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.MessageTO object is missing field 'threadTimestamp'");
        }
        if (json.containsKey("thread_avatar_hash")) {
            Object val = json.get("thread_avatar_hash");
            this.thread_avatar_hash = (String) val;
        } else {
            this.thread_avatar_hash = null;
        }
        if (json.containsKey("thread_background_color")) {
            Object val = json.get("thread_background_color");
            this.thread_background_color = (String) val;
        } else {
            this.thread_background_color = null;
        }
        if (json.containsKey("thread_size")) {
            Object val = json.get("thread_size");
            if (val instanceof Integer) {
                this.thread_size = ((Integer) val).longValue();
            } else {
                this.thread_size = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.MessageTO object is missing field 'thread_size'");
        }
        if (json.containsKey("thread_text_color")) {
            Object val = json.get("thread_text_color");
            this.thread_text_color = (String) val;
        } else {
            this.thread_text_color = null;
        }
        if (json.containsKey("timeout")) {
            Object val = json.get("timeout");
            if (val instanceof Integer) {
                this.timeout = ((Integer) val).longValue();
            } else {
                this.timeout = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.MessageTO object is missing field 'timeout'");
        }
        if (json.containsKey("timestamp")) {
            Object val = json.get("timestamp");
            if (val instanceof Integer) {
                this.timestamp = ((Integer) val).longValue();
            } else {
                this.timestamp = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.MessageTO object is missing field 'timestamp'");
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
        if (this.members == null) {
            obj.put("members", null);
        } else {
            org.json.simple.JSONArray arr = new org.json.simple.JSONArray();
            for (int i=0; i < this.members.length; i++) {
                arr.add(this.members[i].toJSONMap());
            }
            obj.put("members", arr);
        }
        obj.put("alert_flags", this.alert_flags);
        obj.put("branding", this.branding);
        obj.put("broadcast_type", this.broadcast_type);
        obj.put("context", this.context);
        obj.put("default_priority", this.default_priority);
        obj.put("default_sticky", this.default_sticky);
        obj.put("dismiss_button_ui_flags", this.dismiss_button_ui_flags);
        obj.put("flags", this.flags);
        obj.put("key", this.key);
        obj.put("message", this.message);
        obj.put("message_type", this.message_type);
        obj.put("parent_key", this.parent_key);
        obj.put("priority", this.priority);
        obj.put("sender", this.sender);
        obj.put("threadTimestamp", this.threadTimestamp);
        obj.put("thread_avatar_hash", this.thread_avatar_hash);
        obj.put("thread_background_color", this.thread_background_color);
        obj.put("thread_size", this.thread_size);
        obj.put("thread_text_color", this.thread_text_color);
        obj.put("timeout", this.timeout);
        obj.put("timestamp", this.timestamp);
        return obj;
    }

}

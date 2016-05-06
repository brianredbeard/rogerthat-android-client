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

public class UpdateMessageRequestTO implements com.mobicage.rpc.IJSONable {

    public long existence;
    public long flags;
    public boolean has_existence;
    public boolean has_flags;
    public String last_child_message;
    public String message;
    public String message_key;
    public String parent_message_key;
    public String thread_avatar_hash;
    public String thread_background_color;
    public String thread_text_color;

    public UpdateMessageRequestTO() {
    }

    public UpdateMessageRequestTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("existence")) {
            Object val = json.get("existence");
            this.existence = ((Long) val).longValue();
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.UpdateMessageRequestTO object is missing field 'existence'");
        }
        if (json.containsKey("flags")) {
            Object val = json.get("flags");
            this.flags = ((Long) val).longValue();
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.UpdateMessageRequestTO object is missing field 'flags'");
        }
        if (json.containsKey("has_existence")) {
            Object val = json.get("has_existence");
            this.has_existence = ((Boolean) val).booleanValue();
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.UpdateMessageRequestTO object is missing field 'has_existence'");
        }
        if (json.containsKey("has_flags")) {
            Object val = json.get("has_flags");
            this.has_flags = ((Boolean) val).booleanValue();
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.UpdateMessageRequestTO object is missing field 'has_flags'");
        }
        if (json.containsKey("last_child_message")) {
            Object val = json.get("last_child_message");
            this.last_child_message = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.UpdateMessageRequestTO object is missing field 'last_child_message'");
        }
        if (json.containsKey("message")) {
            Object val = json.get("message");
            this.message = (String) val;
        } else {
            this.message = null;
        }
        if (json.containsKey("message_key")) {
            Object val = json.get("message_key");
            this.message_key = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.UpdateMessageRequestTO object is missing field 'message_key'");
        }
        if (json.containsKey("parent_message_key")) {
            Object val = json.get("parent_message_key");
            this.parent_message_key = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.UpdateMessageRequestTO object is missing field 'parent_message_key'");
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
        if (json.containsKey("thread_text_color")) {
            Object val = json.get("thread_text_color");
            this.thread_text_color = (String) val;
        } else {
            this.thread_text_color = null;
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("existence", this.existence);
        obj.put("flags", this.flags);
        obj.put("has_existence", this.has_existence);
        obj.put("has_flags", this.has_flags);
        obj.put("last_child_message", this.last_child_message);
        obj.put("message", this.message);
        obj.put("message_key", this.message_key);
        obj.put("parent_message_key", this.parent_message_key);
        obj.put("thread_avatar_hash", this.thread_avatar_hash);
        obj.put("thread_background_color", this.thread_background_color);
        obj.put("thread_text_color", this.thread_text_color);
        return obj;
    }

}
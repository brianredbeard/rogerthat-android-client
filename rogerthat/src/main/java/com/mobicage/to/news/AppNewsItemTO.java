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

package com.mobicage.to.news;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class AppNewsItemTO implements com.mobicage.rpc.IJSONable {

    public com.mobicage.to.news.NewsActionButtonTO[] buttons;
    public com.mobicage.to.news.NewsSenderTO sender;
    public String broadcast_type;
    public long flags;
    public long id;
    public String image_url;
    public String message;
    public String qr_code_caption;
    public String qr_code_content;
    public long reach;
    public long sort_priority;
    public long sort_timestamp;
    public long timestamp;
    public String title;
    public long type;
    public String[] users_that_rogered;
    public long version;

    public AppNewsItemTO() {
    }

    @SuppressWarnings("unchecked")
    public AppNewsItemTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("buttons")) {
            org.json.simple.JSONArray val_arr = (org.json.simple.JSONArray) json.get("buttons");
            if (val_arr == null) {
                this.buttons = null;
            } else {
                this.buttons = new com.mobicage.to.news.NewsActionButtonTO[val_arr.size()];
                for (int i=0; i < val_arr.size(); i++) {
                    Object item = val_arr.get(i);
                    if (item != null) {
                        this.buttons[i] = new com.mobicage.to.news.NewsActionButtonTO((Map<String, Object>) item);
                    }
                }
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.news.AppNewsItemTO object is missing field 'buttons'");
        }
        if (json.containsKey("sender")) {
            Object val = json.get("sender");
            this.sender = val == null ? null : new com.mobicage.to.news.NewsSenderTO((Map<String, Object>) val);
        } else {
            throw new IncompleteMessageException("com.mobicage.to.news.AppNewsItemTO object is missing field 'sender'");
        }
        if (json.containsKey("broadcast_type")) {
            Object val = json.get("broadcast_type");
            this.broadcast_type = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.news.AppNewsItemTO object is missing field 'broadcast_type'");
        }
        if (json.containsKey("flags")) {
            Object val = json.get("flags");
            if (val instanceof Integer) {
                this.flags = ((Integer) val).longValue();
            } else {
                this.flags = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.news.AppNewsItemTO object is missing field 'flags'");
        }
        if (json.containsKey("id")) {
            Object val = json.get("id");
            if (val instanceof Integer) {
                this.id = ((Integer) val).longValue();
            } else {
                this.id = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.news.AppNewsItemTO object is missing field 'id'");
        }
        if (json.containsKey("image_url")) {
            Object val = json.get("image_url");
            this.image_url = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.news.AppNewsItemTO object is missing field 'image_url'");
        }
        if (json.containsKey("message")) {
            Object val = json.get("message");
            this.message = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.news.AppNewsItemTO object is missing field 'message'");
        }
        if (json.containsKey("qr_code_caption")) {
            Object val = json.get("qr_code_caption");
            this.qr_code_caption = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.news.AppNewsItemTO object is missing field 'qr_code_caption'");
        }
        if (json.containsKey("qr_code_content")) {
            Object val = json.get("qr_code_content");
            this.qr_code_content = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.news.AppNewsItemTO object is missing field 'qr_code_content'");
        }
        if (json.containsKey("reach")) {
            Object val = json.get("reach");
            if (val instanceof Integer) {
                this.reach = ((Integer) val).longValue();
            } else {
                this.reach = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.news.AppNewsItemTO object is missing field 'reach'");
        }
        if (json.containsKey("sort_priority")) {
            Object val = json.get("sort_priority");
            if (val instanceof Integer) {
                this.sort_priority = ((Integer) val).longValue();
            } else {
                this.sort_priority = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.news.AppNewsItemTO object is missing field 'sort_priority'");
        }
        if (json.containsKey("sort_timestamp")) {
            Object val = json.get("sort_timestamp");
            if (val instanceof Integer) {
                this.sort_timestamp = ((Integer) val).longValue();
            } else {
                this.sort_timestamp = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.news.AppNewsItemTO object is missing field 'sort_timestamp'");
        }
        if (json.containsKey("timestamp")) {
            Object val = json.get("timestamp");
            if (val instanceof Integer) {
                this.timestamp = ((Integer) val).longValue();
            } else {
                this.timestamp = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.news.AppNewsItemTO object is missing field 'timestamp'");
        }
        if (json.containsKey("title")) {
            Object val = json.get("title");
            this.title = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.news.AppNewsItemTO object is missing field 'title'");
        }
        if (json.containsKey("type")) {
            Object val = json.get("type");
            if (val instanceof Integer) {
                this.type = ((Integer) val).longValue();
            } else {
                this.type = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.news.AppNewsItemTO object is missing field 'type'");
        }
        if (json.containsKey("users_that_rogered")) {
            org.json.simple.JSONArray val_arr = (org.json.simple.JSONArray) json.get("users_that_rogered");
            if (val_arr == null) {
                this.users_that_rogered = null;
            } else {
                this.users_that_rogered = new String[val_arr.size()];
                for (int i=0; i < val_arr.size(); i++) {
                    this.users_that_rogered[i] = (String) val_arr.get(i);
                }
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.news.AppNewsItemTO object is missing field 'users_that_rogered'");
        }
        if (json.containsKey("version")) {
            Object val = json.get("version");
            if (val instanceof Integer) {
                this.version = ((Integer) val).longValue();
            } else {
                this.version = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.news.AppNewsItemTO object is missing field 'version'");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        if (this.buttons == null) {
            obj.put("buttons", null);
        } else {
            org.json.simple.JSONArray arr = new org.json.simple.JSONArray();
            for (int i=0; i < this.buttons.length; i++) {
                arr.add(this.buttons[i].toJSONMap());
            }
            obj.put("buttons", arr);
        }
        obj.put("sender", this.sender == null ? null : this.sender.toJSONMap());
        obj.put("broadcast_type", this.broadcast_type);
        obj.put("flags", this.flags);
        obj.put("id", this.id);
        obj.put("image_url", this.image_url);
        obj.put("message", this.message);
        obj.put("qr_code_caption", this.qr_code_caption);
        obj.put("qr_code_content", this.qr_code_content);
        obj.put("reach", this.reach);
        obj.put("sort_priority", this.sort_priority);
        obj.put("sort_timestamp", this.sort_timestamp);
        obj.put("timestamp", this.timestamp);
        obj.put("title", this.title);
        obj.put("type", this.type);
        if (this.users_that_rogered == null) {
            obj.put("users_that_rogered", null);
        } else {
            org.json.simple.JSONArray arr = new org.json.simple.JSONArray();
            for (int i=0; i < this.users_that_rogered.length; i++) {
                arr.add(this.users_that_rogered[i]);
            }
            obj.put("users_that_rogered", arr);
        }
        obj.put("version", this.version);
        return obj;
    }

}

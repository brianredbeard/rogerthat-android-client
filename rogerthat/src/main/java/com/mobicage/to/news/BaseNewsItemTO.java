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

package com.mobicage.to.news;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class BaseNewsItemTO implements com.mobicage.rpc.IJSONable {

    public com.mobicage.to.news.NewsActionButtonTO[] buttons;
    public com.mobicage.to.news.NewsSenderTO sender;
    public long id;
    public String image_url;
    public String label;
    public String message;
    public String qr_code_caption;
    public String qr_code_content;
    public long reach;
    public long timestamp;
    public String title;
    public String[] users_that_rogered;
    public long version;

    public BaseNewsItemTO() {
    }

    @SuppressWarnings("unchecked")
    public BaseNewsItemTO(Map<String, Object> json) throws IncompleteMessageException {
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
            throw new IncompleteMessageException("com.mobicage.to.news.BaseNewsItemTO object is missing field 'buttons'");
        }
        if (json.containsKey("sender")) {
            Object val = json.get("sender");
            this.sender = val == null ? null : new com.mobicage.to.news.NewsSenderTO((Map<String, Object>) val);
        } else {
            throw new IncompleteMessageException("com.mobicage.to.news.BaseNewsItemTO object is missing field 'sender'");
        }
        if (json.containsKey("id")) {
            Object val = json.get("id");
            this.id = ((Long) val).longValue();
        } else {
            throw new IncompleteMessageException("com.mobicage.to.news.BaseNewsItemTO object is missing field 'id'");
        }
        if (json.containsKey("image_url")) {
            Object val = json.get("image_url");
            this.image_url = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.news.BaseNewsItemTO object is missing field 'image_url'");
        }
        if (json.containsKey("label")) {
            Object val = json.get("label");
            this.label = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.news.BaseNewsItemTO object is missing field 'label'");
        }
        if (json.containsKey("message")) {
            Object val = json.get("message");
            this.message = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.news.BaseNewsItemTO object is missing field 'message'");
        }
        if (json.containsKey("qr_code_caption")) {
            Object val = json.get("qr_code_caption");
            this.qr_code_caption = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.news.BaseNewsItemTO object is missing field 'qr_code_caption'");
        }
        if (json.containsKey("qr_code_content")) {
            Object val = json.get("qr_code_content");
            this.qr_code_content = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.news.BaseNewsItemTO object is missing field 'qr_code_content'");
        }
        if (json.containsKey("reach")) {
            Object val = json.get("reach");
            this.reach = ((Long) val).longValue();
        } else {
            throw new IncompleteMessageException("com.mobicage.to.news.BaseNewsItemTO object is missing field 'reach'");
        }
        if (json.containsKey("timestamp")) {
            Object val = json.get("timestamp");
            this.timestamp = ((Long) val).longValue();
        } else {
            throw new IncompleteMessageException("com.mobicage.to.news.BaseNewsItemTO object is missing field 'timestamp'");
        }
        if (json.containsKey("title")) {
            Object val = json.get("title");
            this.title = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.news.BaseNewsItemTO object is missing field 'title'");
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
            throw new IncompleteMessageException("com.mobicage.to.news.BaseNewsItemTO object is missing field 'users_that_rogered'");
        }
        if (json.containsKey("version")) {
            Object val = json.get("version");
            this.version = ((Long) val).longValue();
        } else {
            throw new IncompleteMessageException("com.mobicage.to.news.BaseNewsItemTO object is missing field 'version'");
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
        obj.put("id", this.id);
        obj.put("image_url", this.image_url);
        obj.put("label", this.label);
        obj.put("message", this.message);
        obj.put("qr_code_caption", this.qr_code_caption);
        obj.put("qr_code_content", this.qr_code_content);
        obj.put("reach", this.reach);
        obj.put("timestamp", this.timestamp);
        obj.put("title", this.title);
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

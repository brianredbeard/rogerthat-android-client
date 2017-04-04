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

public class AppNewsInfoTO implements com.mobicage.rpc.IJSONable {

    public String broadcast_type;
    public long id;
    public String sender_email;
    public long sort_priority;
    public long sort_timestamp;
    public long version;

    public AppNewsInfoTO() {
    }

    public AppNewsInfoTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("broadcast_type")) {
            Object val = json.get("broadcast_type");
            this.broadcast_type = (String) val;
        } else {
            this.broadcast_type = null;
        }
        if (json.containsKey("id")) {
            Object val = json.get("id");
            if (val instanceof Integer) {
                this.id = ((Integer) val).longValue();
            } else {
                this.id = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.news.AppNewsInfoTO object is missing field 'id'");
        }
        if (json.containsKey("sender_email")) {
            Object val = json.get("sender_email");
            this.sender_email = (String) val;
        } else {
            this.sender_email = null;
        }
        if (json.containsKey("sort_priority")) {
            Object val = json.get("sort_priority");
            if (val instanceof Integer) {
                this.sort_priority = ((Integer) val).longValue();
            } else {
                this.sort_priority = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.news.AppNewsInfoTO object is missing field 'sort_priority'");
        }
        if (json.containsKey("sort_timestamp")) {
            Object val = json.get("sort_timestamp");
            if (val instanceof Integer) {
                this.sort_timestamp = ((Integer) val).longValue();
            } else {
                this.sort_timestamp = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.news.AppNewsInfoTO object is missing field 'sort_timestamp'");
        }
        if (json.containsKey("version")) {
            Object val = json.get("version");
            if (val instanceof Integer) {
                this.version = ((Integer) val).longValue();
            } else {
                this.version = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.news.AppNewsInfoTO object is missing field 'version'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("broadcast_type", this.broadcast_type);
        obj.put("id", this.id);
        obj.put("sender_email", this.sender_email);
        obj.put("sort_priority", this.sort_priority);
        obj.put("sort_timestamp", this.sort_timestamp);
        obj.put("version", this.version);
        return obj;
    }

}

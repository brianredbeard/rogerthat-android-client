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

package com.mobicage.to.service;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class PressMenuIconRequestTO implements com.mobicage.rpc.IJSONable {

    public String context;
    public long[] coords;
    public long generation;
    public String hashed_tag;
    public String message_flow_run_id;
    public String service;
    public String static_flow_hash;
    public long timestamp;

    public PressMenuIconRequestTO() {
    }

    public PressMenuIconRequestTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("context")) {
            Object val = json.get("context");
            this.context = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.service.PressMenuIconRequestTO object is missing field 'context'");
        }
        if (json.containsKey("coords")) {
            org.json.simple.JSONArray val_arr = (org.json.simple.JSONArray) json.get("coords");
            if (val_arr == null) {
                this.coords = null;
            } else {
                this.coords = new long[val_arr.size()];
                for (int i=0; i < val_arr.size(); i++) {
                    this.coords[i] = ((Long) val_arr.get(i)).longValue();
                }
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.service.PressMenuIconRequestTO object is missing field 'coords'");
        }
        if (json.containsKey("generation")) {
            Object val = json.get("generation");
            if (val instanceof Integer) {
                this.generation = ((Integer) val).longValue();
            } else {
                this.generation = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.service.PressMenuIconRequestTO object is missing field 'generation'");
        }
        if (json.containsKey("hashed_tag")) {
            Object val = json.get("hashed_tag");
            this.hashed_tag = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.service.PressMenuIconRequestTO object is missing field 'hashed_tag'");
        }
        if (json.containsKey("message_flow_run_id")) {
            Object val = json.get("message_flow_run_id");
            this.message_flow_run_id = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.service.PressMenuIconRequestTO object is missing field 'message_flow_run_id'");
        }
        if (json.containsKey("service")) {
            Object val = json.get("service");
            this.service = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.service.PressMenuIconRequestTO object is missing field 'service'");
        }
        if (json.containsKey("static_flow_hash")) {
            Object val = json.get("static_flow_hash");
            this.static_flow_hash = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.service.PressMenuIconRequestTO object is missing field 'static_flow_hash'");
        }
        if (json.containsKey("timestamp")) {
            Object val = json.get("timestamp");
            if (val instanceof Integer) {
                this.timestamp = ((Integer) val).longValue();
            } else {
                this.timestamp = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.service.PressMenuIconRequestTO object is missing field 'timestamp'");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("context", this.context);
        if (this.coords == null) {
            obj.put("coords", null);
        } else {
            org.json.simple.JSONArray arr = new org.json.simple.JSONArray();
            for (int i=0; i < this.coords.length; i++) {
                arr.add(this.coords[i]);
            }
            obj.put("coords", arr);
        }
        obj.put("generation", this.generation);
        obj.put("hashed_tag", this.hashed_tag);
        obj.put("message_flow_run_id", this.message_flow_run_id);
        obj.put("service", this.service);
        obj.put("static_flow_hash", this.static_flow_hash);
        obj.put("timestamp", this.timestamp);
        return obj;
    }

}

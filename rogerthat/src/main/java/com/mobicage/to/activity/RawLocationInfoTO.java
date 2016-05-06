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

package com.mobicage.to.activity;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class RawLocationInfoTO implements com.mobicage.rpc.IJSONable {

    public com.mobicage.to.activity.CellTowerTO[] towers;
    public long cid;
    public long lac;
    public long mobileDataType;
    public long net;
    public long signalStrength;

    public RawLocationInfoTO() {
    }

    @SuppressWarnings("unchecked")
    public RawLocationInfoTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("towers")) {
            org.json.simple.JSONArray val_arr = (org.json.simple.JSONArray) json.get("towers");
            if (val_arr == null) {
                this.towers = null;
            } else {
                this.towers = new com.mobicage.to.activity.CellTowerTO[val_arr.size()];
                for (int i=0; i < val_arr.size(); i++) {
                    Object item = val_arr.get(i);
                    if (item != null) {
                        this.towers[i] = new com.mobicage.to.activity.CellTowerTO((Map<String, Object>) item);
                    }
                }
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.activity.RawLocationInfoTO object is missing field 'towers'");
        }
        if (json.containsKey("cid")) {
            Object val = json.get("cid");
            this.cid = ((Long) val).longValue();
        } else {
            throw new IncompleteMessageException("com.mobicage.to.activity.RawLocationInfoTO object is missing field 'cid'");
        }
        if (json.containsKey("lac")) {
            Object val = json.get("lac");
            this.lac = ((Long) val).longValue();
        } else {
            throw new IncompleteMessageException("com.mobicage.to.activity.RawLocationInfoTO object is missing field 'lac'");
        }
        if (json.containsKey("mobileDataType")) {
            Object val = json.get("mobileDataType");
            this.mobileDataType = ((Long) val).longValue();
        } else {
            throw new IncompleteMessageException("com.mobicage.to.activity.RawLocationInfoTO object is missing field 'mobileDataType'");
        }
        if (json.containsKey("net")) {
            Object val = json.get("net");
            this.net = ((Long) val).longValue();
        } else {
            throw new IncompleteMessageException("com.mobicage.to.activity.RawLocationInfoTO object is missing field 'net'");
        }
        if (json.containsKey("signalStrength")) {
            Object val = json.get("signalStrength");
            this.signalStrength = ((Long) val).longValue();
        } else {
            throw new IncompleteMessageException("com.mobicage.to.activity.RawLocationInfoTO object is missing field 'signalStrength'");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        if (this.towers == null) {
            obj.put("towers", null);
        } else {
            org.json.simple.JSONArray arr = new org.json.simple.JSONArray();
            for (int i=0; i < this.towers.length; i++) {
                arr.add(this.towers[i].toJSONMap());
            }
            obj.put("towers", arr);
        }
        obj.put("cid", this.cid);
        obj.put("lac", this.lac);
        obj.put("mobileDataType", this.mobileDataType);
        obj.put("net", this.net);
        obj.put("signalStrength", this.signalStrength);
        return obj;
    }

}
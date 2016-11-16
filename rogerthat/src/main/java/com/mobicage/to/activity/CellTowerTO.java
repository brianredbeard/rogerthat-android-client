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

public class CellTowerTO implements com.mobicage.rpc.IJSONable {

    public long cid;
    public long strength;

    public CellTowerTO() {
    }

    public CellTowerTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("cid")) {
            Object val = json.get("cid");
            if (val instanceof Integer) {
                this.cid = ((Integer) val).longValue();
            } else {
                this.cid = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.activity.CellTowerTO object is missing field 'cid'");
        }
        if (json.containsKey("strength")) {
            Object val = json.get("strength");
            if (val instanceof Integer) {
                this.strength = ((Integer) val).longValue();
            } else {
                this.strength = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.activity.CellTowerTO object is missing field 'strength'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("cid", this.cid);
        obj.put("strength", this.strength);
        return obj;
    }

}

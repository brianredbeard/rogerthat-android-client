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

package com.mobicage.to.beacon;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class BeaconRegionTO implements com.mobicage.rpc.IJSONable {

    public boolean has_major;
    public boolean has_minor;
    public long major;
    public long minor;
    public String uuid;

    public BeaconRegionTO() {
    }

    public BeaconRegionTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("has_major")) {
            Object val = json.get("has_major");
            this.has_major = ((Boolean) val).booleanValue();
        } else {
            throw new IncompleteMessageException("com.mobicage.to.beacon.BeaconRegionTO object is missing field 'has_major'");
        }
        if (json.containsKey("has_minor")) {
            Object val = json.get("has_minor");
            this.has_minor = ((Boolean) val).booleanValue();
        } else {
            throw new IncompleteMessageException("com.mobicage.to.beacon.BeaconRegionTO object is missing field 'has_minor'");
        }
        if (json.containsKey("major")) {
            Object val = json.get("major");
            this.major = ((Long) val).longValue();
        } else {
            throw new IncompleteMessageException("com.mobicage.to.beacon.BeaconRegionTO object is missing field 'major'");
        }
        if (json.containsKey("minor")) {
            Object val = json.get("minor");
            this.minor = ((Long) val).longValue();
        } else {
            throw new IncompleteMessageException("com.mobicage.to.beacon.BeaconRegionTO object is missing field 'minor'");
        }
        if (json.containsKey("uuid")) {
            Object val = json.get("uuid");
            this.uuid = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.beacon.BeaconRegionTO object is missing field 'uuid'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("has_major", this.has_major);
        obj.put("has_minor", this.has_minor);
        obj.put("major", this.major);
        obj.put("minor", this.minor);
        obj.put("uuid", this.uuid);
        return obj;
    }

}

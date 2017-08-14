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

package com.mobicage.to.system;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class LogErrorRequestTO implements com.mobicage.rpc.IJSONable {

    public String description;
    public String errorMessage;
    public String mobicageVersion;
    public long platform;
    public String platformVersion;
    public long timestamp;

    public LogErrorRequestTO() {
    }

    public LogErrorRequestTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("description")) {
            Object val = json.get("description");
            this.description = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.system.LogErrorRequestTO object is missing field 'description'");
        }
        if (json.containsKey("errorMessage")) {
            Object val = json.get("errorMessage");
            this.errorMessage = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.system.LogErrorRequestTO object is missing field 'errorMessage'");
        }
        if (json.containsKey("mobicageVersion")) {
            Object val = json.get("mobicageVersion");
            this.mobicageVersion = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.system.LogErrorRequestTO object is missing field 'mobicageVersion'");
        }
        if (json.containsKey("platform")) {
            Object val = json.get("platform");
            if (val instanceof Integer) {
                this.platform = ((Integer) val).longValue();
            } else {
                this.platform = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.system.LogErrorRequestTO object is missing field 'platform'");
        }
        if (json.containsKey("platformVersion")) {
            Object val = json.get("platformVersion");
            this.platformVersion = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.system.LogErrorRequestTO object is missing field 'platformVersion'");
        }
        if (json.containsKey("timestamp")) {
            Object val = json.get("timestamp");
            if (val instanceof Integer) {
                this.timestamp = ((Integer) val).longValue();
            } else {
                this.timestamp = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.system.LogErrorRequestTO object is missing field 'timestamp'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("description", this.description);
        obj.put("errorMessage", this.errorMessage);
        obj.put("mobicageVersion", this.mobicageVersion);
        obj.put("platform", this.platform);
        obj.put("platformVersion", this.platformVersion);
        obj.put("timestamp", this.timestamp);
        return obj;
    }

}
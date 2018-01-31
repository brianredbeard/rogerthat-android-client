/*
 * Copyright 2018 GIG Technology NV
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
 * @@license_version:1.4@@
 */

package com.mobicage.to.system;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class SaveSettingsRequest implements com.mobicage.rpc.IJSONable {

    public boolean callLogging;
    public boolean tracking;

    public SaveSettingsRequest() {
    }

    public SaveSettingsRequest(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("callLogging")) {
            Object val = json.get("callLogging");
            this.callLogging = ((Boolean) val).booleanValue();
        } else {
            throw new IncompleteMessageException("com.mobicage.to.system.SaveSettingsRequest object is missing field 'callLogging'");
        }
        if (json.containsKey("tracking")) {
            Object val = json.get("tracking");
            this.tracking = ((Boolean) val).booleanValue();
        } else {
            throw new IncompleteMessageException("com.mobicage.to.system.SaveSettingsRequest object is missing field 'tracking'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("callLogging", this.callLogging);
        obj.put("tracking", this.tracking);
        return obj;
    }

}

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

public class UpdateSettingsRequestTO implements com.mobicage.rpc.IJSONable {

    public com.mobicage.to.system.SettingsTO settings;

    public UpdateSettingsRequestTO() {
    }

    @SuppressWarnings("unchecked")
    public UpdateSettingsRequestTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("settings")) {
            Object val = json.get("settings");
            this.settings = val == null ? null : new com.mobicage.to.system.SettingsTO((Map<String, Object>) val);
        } else {
            throw new IncompleteMessageException("com.mobicage.to.system.UpdateSettingsRequestTO object is missing field 'settings'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("settings", this.settings == null ? null : this.settings.toJSONMap());
        return obj;
    }

}

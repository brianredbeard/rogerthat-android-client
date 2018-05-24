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

public class ConsentSettingsTO implements com.mobicage.rpc.IJSONable {

    public boolean ask_push_notifications;
    public boolean ask_tos;

    public ConsentSettingsTO() {
    }

    public ConsentSettingsTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("ask_push_notifications")) {
            Object val = json.get("ask_push_notifications");
            this.ask_push_notifications = ((Boolean) val).booleanValue();
        } else {
            this.ask_push_notifications = false;
        }
        if (json.containsKey("ask_tos")) {
            Object val = json.get("ask_tos");
            this.ask_tos = ((Boolean) val).booleanValue();
        } else {
            this.ask_tos = false;
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("ask_push_notifications", this.ask_push_notifications);
        obj.put("ask_tos", this.ask_tos);
        return obj;
    }

}
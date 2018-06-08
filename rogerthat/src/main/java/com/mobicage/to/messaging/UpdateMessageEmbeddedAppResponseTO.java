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

package com.mobicage.to.messaging;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class UpdateMessageEmbeddedAppResponseTO implements com.mobicage.rpc.IJSONable {

    public com.mobicage.models.properties.messaging.MessageEmbeddedApp embedded_app;
    public String message_key;
    public String parent_message_key;

    public UpdateMessageEmbeddedAppResponseTO() {
    }

    @SuppressWarnings("unchecked")
    public UpdateMessageEmbeddedAppResponseTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("embedded_app")) {
            Object val = json.get("embedded_app");
            this.embedded_app = val == null ? null : new com.mobicage.models.properties.messaging.MessageEmbeddedApp((Map<String, Object>) val);
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.UpdateMessageEmbeddedAppResponseTO object is missing field 'embedded_app'");
        }
        if (json.containsKey("message_key")) {
            Object val = json.get("message_key");
            this.message_key = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.UpdateMessageEmbeddedAppResponseTO object is missing field 'message_key'");
        }
        if (json.containsKey("parent_message_key")) {
            Object val = json.get("parent_message_key");
            this.parent_message_key = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.UpdateMessageEmbeddedAppResponseTO object is missing field 'parent_message_key'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("embedded_app", this.embedded_app == null ? null : this.embedded_app.toJSONMap());
        obj.put("message_key", this.message_key);
        obj.put("parent_message_key", this.parent_message_key);
        return obj;
    }

}

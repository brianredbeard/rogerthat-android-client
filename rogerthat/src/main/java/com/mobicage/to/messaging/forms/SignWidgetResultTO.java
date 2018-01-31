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

package com.mobicage.to.messaging.forms;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class SignWidgetResultTO implements com.mobicage.rpc.IJSONable {

    public com.mobicage.models.properties.profiles.PublicKeyTO public_key;
    public String payload_signature;
    public String total_signature;

    public SignWidgetResultTO() {
    }

    @SuppressWarnings("unchecked")
    public SignWidgetResultTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("public_key")) {
            Object val = json.get("public_key");
            this.public_key = val == null ? null : new com.mobicage.models.properties.profiles.PublicKeyTO((Map<String, Object>) val);
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.forms.SignWidgetResultTO object is missing field 'public_key'");
        }
        if (json.containsKey("payload_signature")) {
            Object val = json.get("payload_signature");
            this.payload_signature = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.forms.SignWidgetResultTO object is missing field 'payload_signature'");
        }
        if (json.containsKey("total_signature")) {
            Object val = json.get("total_signature");
            this.total_signature = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.forms.SignWidgetResultTO object is missing field 'total_signature'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("public_key", this.public_key == null ? null : this.public_key.toJSONMap());
        obj.put("payload_signature", this.payload_signature);
        obj.put("total_signature", this.total_signature);
        return obj;
    }

}

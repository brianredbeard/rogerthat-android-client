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

package com.mobicage.to.messaging;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class NewMessageRequestTO implements com.mobicage.rpc.IJSONable {

    public com.mobicage.to.messaging.MessageTO message;

    public NewMessageRequestTO() {
    }

    @SuppressWarnings("unchecked")
    public NewMessageRequestTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("message")) {
            Object val = json.get("message");
            this.message = val == null ? null : new com.mobicage.to.messaging.MessageTO((Map<String, Object>) val);
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.NewMessageRequestTO object is missing field 'message'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("message", this.message == null ? null : this.message.toJSONMap());
        return obj;
    }

}
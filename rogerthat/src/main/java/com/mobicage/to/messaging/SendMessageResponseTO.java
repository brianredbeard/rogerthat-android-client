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

package com.mobicage.to.messaging;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class SendMessageResponseTO implements com.mobicage.rpc.IJSONable {

    public String key;
    public long timestamp;

    public SendMessageResponseTO() {
    }

    public SendMessageResponseTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("key")) {
            Object val = json.get("key");
            this.key = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.SendMessageResponseTO object is missing field 'key'");
        }
        if (json.containsKey("timestamp")) {
            Object val = json.get("timestamp");
            this.timestamp = ((Long) val).longValue();
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.SendMessageResponseTO object is missing field 'timestamp'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("key", this.key);
        obj.put("timestamp", this.timestamp);
        return obj;
    }

}

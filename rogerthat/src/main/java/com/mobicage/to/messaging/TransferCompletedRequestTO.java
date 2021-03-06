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

public class TransferCompletedRequestTO implements com.mobicage.rpc.IJSONable {

    public String message_key;
    public String parent_message_key;
    public String result_url;

    public TransferCompletedRequestTO() {
    }

    public TransferCompletedRequestTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("message_key")) {
            Object val = json.get("message_key");
            this.message_key = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.TransferCompletedRequestTO object is missing field 'message_key'");
        }
        if (json.containsKey("parent_message_key")) {
            Object val = json.get("parent_message_key");
            this.parent_message_key = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.TransferCompletedRequestTO object is missing field 'parent_message_key'");
        }
        if (json.containsKey("result_url")) {
            Object val = json.get("result_url");
            this.result_url = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.TransferCompletedRequestTO object is missing field 'result_url'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("message_key", this.message_key);
        obj.put("parent_message_key", this.parent_message_key);
        obj.put("result_url", this.result_url);
        return obj;
    }

}

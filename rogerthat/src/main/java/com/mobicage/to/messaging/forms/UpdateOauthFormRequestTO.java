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

package com.mobicage.to.messaging.forms;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class UpdateOauthFormRequestTO implements com.mobicage.rpc.IJSONable {

    public com.mobicage.to.messaging.forms.UnicodeWidgetResultTO result;
    public long acked_timestamp;
    public String button_id;
    public String message_key;
    public String parent_message_key;
    public long received_timestamp;
    public long status;

    public UpdateOauthFormRequestTO() {
    }

    @SuppressWarnings("unchecked")
    public UpdateOauthFormRequestTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("result")) {
            Object val = json.get("result");
            this.result = val == null ? null : new com.mobicage.to.messaging.forms.UnicodeWidgetResultTO((Map<String, Object>) val);
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.forms.UpdateOauthFormRequestTO object is missing field 'result'");
        }
        if (json.containsKey("acked_timestamp")) {
            Object val = json.get("acked_timestamp");
            if (val instanceof Integer) {
                this.acked_timestamp = ((Integer) val).longValue();
            } else {
                this.acked_timestamp = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.forms.UpdateOauthFormRequestTO object is missing field 'acked_timestamp'");
        }
        if (json.containsKey("button_id")) {
            Object val = json.get("button_id");
            this.button_id = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.forms.UpdateOauthFormRequestTO object is missing field 'button_id'");
        }
        if (json.containsKey("message_key")) {
            Object val = json.get("message_key");
            this.message_key = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.forms.UpdateOauthFormRequestTO object is missing field 'message_key'");
        }
        if (json.containsKey("parent_message_key")) {
            Object val = json.get("parent_message_key");
            this.parent_message_key = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.forms.UpdateOauthFormRequestTO object is missing field 'parent_message_key'");
        }
        if (json.containsKey("received_timestamp")) {
            Object val = json.get("received_timestamp");
            if (val instanceof Integer) {
                this.received_timestamp = ((Integer) val).longValue();
            } else {
                this.received_timestamp = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.forms.UpdateOauthFormRequestTO object is missing field 'received_timestamp'");
        }
        if (json.containsKey("status")) {
            Object val = json.get("status");
            if (val instanceof Integer) {
                this.status = ((Integer) val).longValue();
            } else {
                this.status = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.forms.UpdateOauthFormRequestTO object is missing field 'status'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("result", this.result == null ? null : this.result.toJSONMap());
        obj.put("acked_timestamp", this.acked_timestamp);
        obj.put("button_id", this.button_id);
        obj.put("message_key", this.message_key);
        obj.put("parent_message_key", this.parent_message_key);
        obj.put("received_timestamp", this.received_timestamp);
        obj.put("status", this.status);
        return obj;
    }

}

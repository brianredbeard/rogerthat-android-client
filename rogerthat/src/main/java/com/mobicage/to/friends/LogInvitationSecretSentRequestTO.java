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

package com.mobicage.to.friends;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class LogInvitationSecretSentRequestTO implements com.mobicage.rpc.IJSONable {

    public String phone_number;
    public String secret;
    public long timestamp;

    public LogInvitationSecretSentRequestTO() {
    }

    public LogInvitationSecretSentRequestTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("phone_number")) {
            Object val = json.get("phone_number");
            this.phone_number = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.friends.LogInvitationSecretSentRequestTO object is missing field 'phone_number'");
        }
        if (json.containsKey("secret")) {
            Object val = json.get("secret");
            this.secret = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.friends.LogInvitationSecretSentRequestTO object is missing field 'secret'");
        }
        if (json.containsKey("timestamp")) {
            Object val = json.get("timestamp");
            if (val instanceof Integer) {
                this.timestamp = ((Integer) val).longValue();
            } else {
                this.timestamp = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.friends.LogInvitationSecretSentRequestTO object is missing field 'timestamp'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("phone_number", this.phone_number);
        obj.put("secret", this.secret);
        obj.put("timestamp", this.timestamp);
        return obj;
    }

}

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

public class UserScannedRequestTO implements com.mobicage.rpc.IJSONable {

    public String app_id;
    public String email;
    public String service_email;

    public UserScannedRequestTO() {
    }

    public UserScannedRequestTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("app_id")) {
            Object val = json.get("app_id");
            this.app_id = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.friends.UserScannedRequestTO object is missing field 'app_id'");
        }
        if (json.containsKey("email")) {
            Object val = json.get("email");
            this.email = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.friends.UserScannedRequestTO object is missing field 'email'");
        }
        if (json.containsKey("service_email")) {
            Object val = json.get("service_email");
            this.service_email = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.friends.UserScannedRequestTO object is missing field 'service_email'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("app_id", this.app_id);
        obj.put("email", this.email);
        obj.put("service_email", this.service_email);
        return obj;
    }

}
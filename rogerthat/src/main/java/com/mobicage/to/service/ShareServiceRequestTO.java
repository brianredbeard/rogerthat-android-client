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

package com.mobicage.to.service;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class ShareServiceRequestTO implements com.mobicage.rpc.IJSONable {

    public String recipient;
    public String service_email;

    public ShareServiceRequestTO() {
    }

    public ShareServiceRequestTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("recipient")) {
            Object val = json.get("recipient");
            this.recipient = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.service.ShareServiceRequestTO object is missing field 'recipient'");
        }
        if (json.containsKey("service_email")) {
            Object val = json.get("service_email");
            this.service_email = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.service.ShareServiceRequestTO object is missing field 'service_email'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("recipient", this.recipient);
        obj.put("service_email", this.service_email);
        return obj;
    }

}

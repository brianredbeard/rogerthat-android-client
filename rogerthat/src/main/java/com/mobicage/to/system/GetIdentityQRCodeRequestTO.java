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

package com.mobicage.to.system;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class GetIdentityQRCodeRequestTO implements com.mobicage.rpc.IJSONable {

    public String email;
    public String size;

    public GetIdentityQRCodeRequestTO() {
    }

    public GetIdentityQRCodeRequestTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("email")) {
            Object val = json.get("email");
            this.email = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.system.GetIdentityQRCodeRequestTO object is missing field 'email'");
        }
        if (json.containsKey("size")) {
            Object val = json.get("size");
            this.size = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.system.GetIdentityQRCodeRequestTO object is missing field 'size'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("email", this.email);
        obj.put("size", this.size);
        return obj;
    }

}

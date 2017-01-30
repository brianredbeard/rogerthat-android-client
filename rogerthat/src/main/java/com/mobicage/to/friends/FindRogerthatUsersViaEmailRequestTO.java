/*
 * Copyright 2017 Mobicage NV
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
 * @@license_version:1.2@@
 */

package com.mobicage.to.friends;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class FindRogerthatUsersViaEmailRequestTO implements com.mobicage.rpc.IJSONable {

    public String[] email_addresses;

    public FindRogerthatUsersViaEmailRequestTO() {
    }

    public FindRogerthatUsersViaEmailRequestTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("email_addresses")) {
            org.json.simple.JSONArray val_arr = (org.json.simple.JSONArray) json.get("email_addresses");
            if (val_arr == null) {
                this.email_addresses = null;
            } else {
                this.email_addresses = new String[val_arr.size()];
                for (int i=0; i < val_arr.size(); i++) {
                    this.email_addresses[i] = (String) val_arr.get(i);
                }
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.friends.FindRogerthatUsersViaEmailRequestTO object is missing field 'email_addresses'");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        if (this.email_addresses == null) {
            obj.put("email_addresses", null);
        } else {
            org.json.simple.JSONArray arr = new org.json.simple.JSONArray();
            for (int i=0; i < this.email_addresses.length; i++) {
                arr.add(this.email_addresses[i]);
            }
            obj.put("email_addresses", arr);
        }
        return obj;
    }

}

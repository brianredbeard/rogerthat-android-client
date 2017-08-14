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

package com.mobicage.to.friends;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class FindRogerthatUsersViaEmailResponseTO implements com.mobicage.rpc.IJSONable {

    public String[] matched_addresses;

    public FindRogerthatUsersViaEmailResponseTO() {
    }

    public FindRogerthatUsersViaEmailResponseTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("matched_addresses")) {
            org.json.simple.JSONArray val_arr = (org.json.simple.JSONArray) json.get("matched_addresses");
            if (val_arr == null) {
                this.matched_addresses = null;
            } else {
                this.matched_addresses = new String[val_arr.size()];
                for (int i=0; i < val_arr.size(); i++) {
                    this.matched_addresses[i] = (String) val_arr.get(i);
                }
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.friends.FindRogerthatUsersViaEmailResponseTO object is missing field 'matched_addresses'");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        if (this.matched_addresses == null) {
            obj.put("matched_addresses", null);
        } else {
            org.json.simple.JSONArray arr = new org.json.simple.JSONArray();
            for (int i=0; i < this.matched_addresses.length; i++) {
                arr.add(this.matched_addresses[i]);
            }
            obj.put("matched_addresses", arr);
        }
        return obj;
    }

}
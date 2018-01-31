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

package com.mobicage.models.properties.forms;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class MyDigiPassAddress implements com.mobicage.rpc.IJSONable {

    public String address_1;
    public String address_2;
    public String city;
    public String country;
    public String state;
    public String zip;

    public MyDigiPassAddress() {
    }

    public MyDigiPassAddress(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("address_1")) {
            Object val = json.get("address_1");
            this.address_1 = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.models.properties.forms.MyDigiPassAddress object is missing field 'address_1'");
        }
        if (json.containsKey("address_2")) {
            Object val = json.get("address_2");
            this.address_2 = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.models.properties.forms.MyDigiPassAddress object is missing field 'address_2'");
        }
        if (json.containsKey("city")) {
            Object val = json.get("city");
            this.city = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.models.properties.forms.MyDigiPassAddress object is missing field 'city'");
        }
        if (json.containsKey("country")) {
            Object val = json.get("country");
            this.country = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.models.properties.forms.MyDigiPassAddress object is missing field 'country'");
        }
        if (json.containsKey("state")) {
            Object val = json.get("state");
            this.state = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.models.properties.forms.MyDigiPassAddress object is missing field 'state'");
        }
        if (json.containsKey("zip")) {
            Object val = json.get("zip");
            this.zip = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.models.properties.forms.MyDigiPassAddress object is missing field 'zip'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("address_1", this.address_1);
        obj.put("address_2", this.address_2);
        obj.put("city", this.city);
        obj.put("country", this.country);
        obj.put("state", this.state);
        obj.put("zip", this.zip);
        return obj;
    }

}

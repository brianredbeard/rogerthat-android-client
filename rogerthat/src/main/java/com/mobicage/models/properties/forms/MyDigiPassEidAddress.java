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

package com.mobicage.models.properties.forms;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class MyDigiPassEidAddress implements com.mobicage.rpc.IJSONable {

    public String municipality;
    public String street_and_number;
    public String zip_code;

    public MyDigiPassEidAddress() {
    }

    public MyDigiPassEidAddress(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("municipality")) {
            Object val = json.get("municipality");
            this.municipality = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.models.properties.forms.MyDigiPassEidAddress object is missing field 'municipality'");
        }
        if (json.containsKey("street_and_number")) {
            Object val = json.get("street_and_number");
            this.street_and_number = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.models.properties.forms.MyDigiPassEidAddress object is missing field 'street_and_number'");
        }
        if (json.containsKey("zip_code")) {
            Object val = json.get("zip_code");
            this.zip_code = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.models.properties.forms.MyDigiPassEidAddress object is missing field 'zip_code'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("municipality", this.municipality);
        obj.put("street_and_number", this.street_and_number);
        obj.put("zip_code", this.zip_code);
        return obj;
    }

}

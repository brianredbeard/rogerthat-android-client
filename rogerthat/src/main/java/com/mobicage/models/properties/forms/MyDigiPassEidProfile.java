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

package com.mobicage.models.properties.forms;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class MyDigiPassEidProfile implements com.mobicage.rpc.IJSONable {

    public String card_number;
    public String chip_number;
    public String created_at;
    public String date_of_birth;
    public String first_name;
    public String first_name_3;
    public String gender;
    public String issuing_municipality;
    public String last_name;
    public String location_of_birth;
    public String nationality;
    public String noble_condition;
    public String validity_begins_at;
    public String validity_ends_at;

    public MyDigiPassEidProfile() {
    }

    public MyDigiPassEidProfile(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("card_number")) {
            Object val = json.get("card_number");
            this.card_number = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.models.properties.forms.MyDigiPassEidProfile object is missing field 'card_number'");
        }
        if (json.containsKey("chip_number")) {
            Object val = json.get("chip_number");
            this.chip_number = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.models.properties.forms.MyDigiPassEidProfile object is missing field 'chip_number'");
        }
        if (json.containsKey("created_at")) {
            Object val = json.get("created_at");
            this.created_at = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.models.properties.forms.MyDigiPassEidProfile object is missing field 'created_at'");
        }
        if (json.containsKey("date_of_birth")) {
            Object val = json.get("date_of_birth");
            this.date_of_birth = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.models.properties.forms.MyDigiPassEidProfile object is missing field 'date_of_birth'");
        }
        if (json.containsKey("first_name")) {
            Object val = json.get("first_name");
            this.first_name = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.models.properties.forms.MyDigiPassEidProfile object is missing field 'first_name'");
        }
        if (json.containsKey("first_name_3")) {
            Object val = json.get("first_name_3");
            this.first_name_3 = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.models.properties.forms.MyDigiPassEidProfile object is missing field 'first_name_3'");
        }
        if (json.containsKey("gender")) {
            Object val = json.get("gender");
            this.gender = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.models.properties.forms.MyDigiPassEidProfile object is missing field 'gender'");
        }
        if (json.containsKey("issuing_municipality")) {
            Object val = json.get("issuing_municipality");
            this.issuing_municipality = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.models.properties.forms.MyDigiPassEidProfile object is missing field 'issuing_municipality'");
        }
        if (json.containsKey("last_name")) {
            Object val = json.get("last_name");
            this.last_name = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.models.properties.forms.MyDigiPassEidProfile object is missing field 'last_name'");
        }
        if (json.containsKey("location_of_birth")) {
            Object val = json.get("location_of_birth");
            this.location_of_birth = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.models.properties.forms.MyDigiPassEidProfile object is missing field 'location_of_birth'");
        }
        if (json.containsKey("nationality")) {
            Object val = json.get("nationality");
            this.nationality = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.models.properties.forms.MyDigiPassEidProfile object is missing field 'nationality'");
        }
        if (json.containsKey("noble_condition")) {
            Object val = json.get("noble_condition");
            this.noble_condition = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.models.properties.forms.MyDigiPassEidProfile object is missing field 'noble_condition'");
        }
        if (json.containsKey("validity_begins_at")) {
            Object val = json.get("validity_begins_at");
            this.validity_begins_at = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.models.properties.forms.MyDigiPassEidProfile object is missing field 'validity_begins_at'");
        }
        if (json.containsKey("validity_ends_at")) {
            Object val = json.get("validity_ends_at");
            this.validity_ends_at = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.models.properties.forms.MyDigiPassEidProfile object is missing field 'validity_ends_at'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("card_number", this.card_number);
        obj.put("chip_number", this.chip_number);
        obj.put("created_at", this.created_at);
        obj.put("date_of_birth", this.date_of_birth);
        obj.put("first_name", this.first_name);
        obj.put("first_name_3", this.first_name_3);
        obj.put("gender", this.gender);
        obj.put("issuing_municipality", this.issuing_municipality);
        obj.put("last_name", this.last_name);
        obj.put("location_of_birth", this.location_of_birth);
        obj.put("nationality", this.nationality);
        obj.put("noble_condition", this.noble_condition);
        obj.put("validity_begins_at", this.validity_begins_at);
        obj.put("validity_ends_at", this.validity_ends_at);
        return obj;
    }

}

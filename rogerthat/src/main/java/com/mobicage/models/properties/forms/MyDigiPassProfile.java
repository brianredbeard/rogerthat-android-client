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

package com.mobicage.models.properties.forms;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class MyDigiPassProfile implements com.mobicage.rpc.IJSONable {

    public String born_on;
    public String first_name;
    public String last_name;
    public String preferred_locale;
    public String updated_at;
    public String uuid;

    public MyDigiPassProfile() {
    }

    public MyDigiPassProfile(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("born_on")) {
            Object val = json.get("born_on");
            this.born_on = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.models.properties.forms.MyDigiPassProfile object is missing field 'born_on'");
        }
        if (json.containsKey("first_name")) {
            Object val = json.get("first_name");
            this.first_name = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.models.properties.forms.MyDigiPassProfile object is missing field 'first_name'");
        }
        if (json.containsKey("last_name")) {
            Object val = json.get("last_name");
            this.last_name = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.models.properties.forms.MyDigiPassProfile object is missing field 'last_name'");
        }
        if (json.containsKey("preferred_locale")) {
            Object val = json.get("preferred_locale");
            this.preferred_locale = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.models.properties.forms.MyDigiPassProfile object is missing field 'preferred_locale'");
        }
        if (json.containsKey("updated_at")) {
            Object val = json.get("updated_at");
            this.updated_at = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.models.properties.forms.MyDigiPassProfile object is missing field 'updated_at'");
        }
        if (json.containsKey("uuid")) {
            Object val = json.get("uuid");
            this.uuid = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.models.properties.forms.MyDigiPassProfile object is missing field 'uuid'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("born_on", this.born_on);
        obj.put("first_name", this.first_name);
        obj.put("last_name", this.last_name);
        obj.put("preferred_locale", this.preferred_locale);
        obj.put("updated_at", this.updated_at);
        obj.put("uuid", this.uuid);
        return obj;
    }

}

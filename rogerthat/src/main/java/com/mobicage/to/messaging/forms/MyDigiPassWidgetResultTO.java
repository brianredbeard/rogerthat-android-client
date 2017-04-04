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

package com.mobicage.to.messaging.forms;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class MyDigiPassWidgetResultTO implements com.mobicage.rpc.IJSONable {

    public com.mobicage.models.properties.forms.MyDigiPassAddress address;
    public com.mobicage.models.properties.forms.MyDigiPassEidAddress eid_address;
    public com.mobicage.models.properties.forms.MyDigiPassEidProfile eid_profile;
    public com.mobicage.models.properties.forms.MyDigiPassProfile profile;
    public String eid_photo;
    public String email;
    public String phone;

    public MyDigiPassWidgetResultTO() {
    }

    @SuppressWarnings("unchecked")
    public MyDigiPassWidgetResultTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("address")) {
            Object val = json.get("address");
            this.address = val == null ? null : new com.mobicage.models.properties.forms.MyDigiPassAddress((Map<String, Object>) val);
        } else {
            this.address = null;
        }
        if (json.containsKey("eid_address")) {
            Object val = json.get("eid_address");
            this.eid_address = val == null ? null : new com.mobicage.models.properties.forms.MyDigiPassEidAddress((Map<String, Object>) val);
        } else {
            this.eid_address = null;
        }
        if (json.containsKey("eid_profile")) {
            Object val = json.get("eid_profile");
            this.eid_profile = val == null ? null : new com.mobicage.models.properties.forms.MyDigiPassEidProfile((Map<String, Object>) val);
        } else {
            this.eid_profile = null;
        }
        if (json.containsKey("profile")) {
            Object val = json.get("profile");
            this.profile = val == null ? null : new com.mobicage.models.properties.forms.MyDigiPassProfile((Map<String, Object>) val);
        } else {
            this.profile = null;
        }
        if (json.containsKey("eid_photo")) {
            Object val = json.get("eid_photo");
            this.eid_photo = (String) val;
        } else {
            this.eid_photo = null;
        }
        if (json.containsKey("email")) {
            Object val = json.get("email");
            this.email = (String) val;
        } else {
            this.email = null;
        }
        if (json.containsKey("phone")) {
            Object val = json.get("phone");
            this.phone = (String) val;
        } else {
            this.phone = null;
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("address", this.address == null ? null : this.address.toJSONMap());
        obj.put("eid_address", this.eid_address == null ? null : this.eid_address.toJSONMap());
        obj.put("eid_profile", this.eid_profile == null ? null : this.eid_profile.toJSONMap());
        obj.put("profile", this.profile == null ? null : this.profile.toJSONMap());
        obj.put("eid_photo", this.eid_photo);
        obj.put("email", this.email);
        obj.put("phone", this.phone);
        return obj;
    }

}

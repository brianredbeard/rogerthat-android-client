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

package com.mobicage.to.service;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class UpdateUserDataRequestTO implements com.mobicage.rpc.IJSONable {

    public String app_data;
    public String service;
    public String user_data;

    public UpdateUserDataRequestTO() {
    }

    public UpdateUserDataRequestTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("app_data")) {
            Object val = json.get("app_data");
            this.app_data = (String) val;
        } else {
            this.app_data = null;
        }
        if (json.containsKey("service")) {
            Object val = json.get("service");
            this.service = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.service.UpdateUserDataRequestTO object is missing field 'service'");
        }
        if (json.containsKey("user_data")) {
            Object val = json.get("user_data");
            this.user_data = (String) val;
        } else {
            this.user_data = null;
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("app_data", this.app_data);
        obj.put("service", this.service);
        obj.put("user_data", this.user_data);
        return obj;
    }

}

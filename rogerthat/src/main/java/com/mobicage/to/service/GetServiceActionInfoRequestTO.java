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

package com.mobicage.to.service;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class GetServiceActionInfoRequestTO implements com.mobicage.rpc.IJSONable {

    public String action;
    public boolean allow_cross_app;
    public String code;

    public GetServiceActionInfoRequestTO() {
    }

    public GetServiceActionInfoRequestTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("action")) {
            Object val = json.get("action");
            this.action = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.service.GetServiceActionInfoRequestTO object is missing field 'action'");
        }
        if (json.containsKey("allow_cross_app")) {
            Object val = json.get("allow_cross_app");
            this.allow_cross_app = ((Boolean) val).booleanValue();
        } else {
            this.allow_cross_app = false;
        }
        if (json.containsKey("code")) {
            Object val = json.get("code");
            this.code = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.service.GetServiceActionInfoRequestTO object is missing field 'code'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("action", this.action);
        obj.put("allow_cross_app", this.allow_cross_app);
        obj.put("code", this.code);
        return obj;
    }

}
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

package com.mobicage.to.location;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class GetLocationResponseTO implements com.mobicage.rpc.IJSONable {

    public com.mobicage.to.location.GetLocationErrorTO error;

    public GetLocationResponseTO() {
    }

    @SuppressWarnings("unchecked")
    public GetLocationResponseTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("error")) {
            Object val = json.get("error");
            this.error = val == null ? null : new com.mobicage.to.location.GetLocationErrorTO((Map<String, Object>) val);
        } else {
            throw new IncompleteMessageException("com.mobicage.to.location.GetLocationResponseTO object is missing field 'error'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("error", this.error == null ? null : this.error.toJSONMap());
        return obj;
    }

}
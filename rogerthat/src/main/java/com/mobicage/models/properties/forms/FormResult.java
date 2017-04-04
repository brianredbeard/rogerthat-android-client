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

public class FormResult implements com.mobicage.rpc.IJSONable {

    public com.mobicage.models.properties.forms.WidgetResult result;
    public String type;

    public FormResult() {
    }

    @SuppressWarnings("unchecked")
    public FormResult(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("result")) {
            Object val = json.get("result");
            this.result = val == null ? null : new com.mobicage.models.properties.forms.WidgetResult((Map<String, Object>) val);
        } else {
            throw new IncompleteMessageException("com.mobicage.models.properties.forms.FormResult object is missing field 'result'");
        }
        if (json.containsKey("type")) {
            Object val = json.get("type");
            this.type = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.models.properties.forms.FormResult object is missing field 'type'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("result", this.result == null ? null : this.result.toJSONMap());
        obj.put("type", this.type);
        return obj;
    }

}

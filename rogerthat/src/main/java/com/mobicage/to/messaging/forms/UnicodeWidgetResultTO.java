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

public class UnicodeWidgetResultTO implements com.mobicage.rpc.IJSONable {

    public String value;

    public UnicodeWidgetResultTO() {
    }

    public UnicodeWidgetResultTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("value")) {
            Object val = json.get("value");
            this.value = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.forms.UnicodeWidgetResultTO object is missing field 'value'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("value", this.value);
        return obj;
    }

}
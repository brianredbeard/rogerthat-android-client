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

package com.mobicage.to.messaging.forms;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class FloatWidgetResultTO implements com.mobicage.rpc.IJSONable {

    public float value;

    public FloatWidgetResultTO() {
    }

    public FloatWidgetResultTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("value")) {
            Object val = json.get("value");
            if (val instanceof Float) {
                this.value = ((Float) val).floatValue();
            } else if (val instanceof Double) {
                this.value = new Float((Double) val).floatValue();
            } else {
                this.value = new Float((Long) val).floatValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.forms.FloatWidgetResultTO object is missing field 'value'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("value", this.value);
        return obj;
    }

}
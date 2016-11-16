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

public class TextLineTO implements com.mobicage.rpc.IJSONable {

    public String keyboard_type;
    public long max_chars;
    public String place_holder;
    public String value;

    public TextLineTO() {
    }

    public TextLineTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("keyboard_type")) {
            Object val = json.get("keyboard_type");
            this.keyboard_type = (String) val;
        } else {
            this.keyboard_type = "default";
        }
        if (json.containsKey("max_chars")) {
            Object val = json.get("max_chars");
            if (val instanceof Integer) {
                this.max_chars = ((Integer) val).longValue();
            } else {
                this.max_chars = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.forms.TextLineTO object is missing field 'max_chars'");
        }
        if (json.containsKey("place_holder")) {
            Object val = json.get("place_holder");
            this.place_holder = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.forms.TextLineTO object is missing field 'place_holder'");
        }
        if (json.containsKey("value")) {
            Object val = json.get("value");
            this.value = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.forms.TextLineTO object is missing field 'value'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("keyboard_type", this.keyboard_type);
        obj.put("max_chars", this.max_chars);
        obj.put("place_holder", this.place_holder);
        obj.put("value", this.value);
        return obj;
    }

}

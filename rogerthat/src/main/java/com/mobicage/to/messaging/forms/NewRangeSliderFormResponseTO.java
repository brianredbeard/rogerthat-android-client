/*
 * Copyright 2017 Mobicage NV
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
 * @@license_version:1.2@@
 */

package com.mobicage.to.messaging.forms;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class NewRangeSliderFormResponseTO implements com.mobicage.rpc.IJSONable {

    public long received_timestamp;

    public NewRangeSliderFormResponseTO() {
    }

    public NewRangeSliderFormResponseTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("received_timestamp")) {
            Object val = json.get("received_timestamp");
            if (val instanceof Integer) {
                this.received_timestamp = ((Integer) val).longValue();
            } else {
                this.received_timestamp = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.forms.NewRangeSliderFormResponseTO object is missing field 'received_timestamp'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("received_timestamp", this.received_timestamp);
        return obj;
    }

}

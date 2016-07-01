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

public class SubmitSingleSelectFormResponseTO implements com.mobicage.rpc.IJSONable {

    public long result;

    public SubmitSingleSelectFormResponseTO() {
    }

    public SubmitSingleSelectFormResponseTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("result")) {
            Object val = json.get("result");
            this.result = ((Long) val).longValue();
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.forms.SubmitSingleSelectFormResponseTO object is missing field 'result'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("result", this.result);
        return obj;
    }

}

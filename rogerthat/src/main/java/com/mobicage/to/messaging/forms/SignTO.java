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

public class SignTO implements com.mobicage.rpc.IJSONable {

    public String caption;
    public String payload;

    public SignTO() {
    }

    public SignTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("caption")) {
            Object val = json.get("caption");
            this.caption = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.forms.SignTO object is missing field 'caption'");
        }
        if (json.containsKey("payload")) {
            Object val = json.get("payload");
            this.payload = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.forms.SignTO object is missing field 'payload'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("caption", this.caption);
        obj.put("payload", this.payload);
        return obj;
    }

}

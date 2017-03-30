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

package com.mobicage.to.app;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class UpdateLookAndFeelRequestTO implements com.mobicage.rpc.IJSONable {

    public com.mobicage.to.app.LookAndFeelTO look_and_feel;

    public UpdateLookAndFeelRequestTO() {
    }

    @SuppressWarnings("unchecked")
    public UpdateLookAndFeelRequestTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("look_and_feel")) {
            Object val = json.get("look_and_feel");
            this.look_and_feel = val == null ? null : new com.mobicage.to.app.LookAndFeelTO((Map<String, Object>) val);
        } else {
            throw new IncompleteMessageException("com.mobicage.to.app.UpdateLookAndFeelRequestTO object is missing field 'look_and_feel'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("look_and_feel", this.look_and_feel == null ? null : this.look_and_feel.toJSONMap());
        return obj;
    }

}
/*
 * Copyright 2018 GIG Technology NV
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
 * @@license_version:1.4@@
 */

package com.mobicage.to.messaging.forms;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class FriendSelectTO implements com.mobicage.rpc.IJSONable {

    public boolean multi_select;
    public boolean selection_required;

    public FriendSelectTO() {
    }

    public FriendSelectTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("multi_select")) {
            Object val = json.get("multi_select");
            this.multi_select = ((Boolean) val).booleanValue();
        } else {
            this.multi_select = false;
        }
        if (json.containsKey("selection_required")) {
            Object val = json.get("selection_required");
            this.selection_required = ((Boolean) val).booleanValue();
        } else {
            this.selection_required = true;
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("multi_select", this.multi_select);
        obj.put("selection_required", this.selection_required);
        return obj;
    }

}

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

package com.mobicage.to.messaging;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class ButtonTO implements com.mobicage.rpc.IJSONable {

    public String action;
    public String caption;
    public String id;
    public long ui_flags;

    public ButtonTO() {
    }

    public ButtonTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("action")) {
            Object val = json.get("action");
            this.action = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.ButtonTO object is missing field 'action'");
        }
        if (json.containsKey("caption")) {
            Object val = json.get("caption");
            this.caption = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.ButtonTO object is missing field 'caption'");
        }
        if (json.containsKey("id")) {
            Object val = json.get("id");
            this.id = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.ButtonTO object is missing field 'id'");
        }
        if (json.containsKey("ui_flags")) {
            Object val = json.get("ui_flags");
            this.ui_flags = ((Long) val).longValue();
        } else {
            this.ui_flags = 0;
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("action", this.action);
        obj.put("caption", this.caption);
        obj.put("id", this.id);
        obj.put("ui_flags", this.ui_flags);
        return obj;
    }

}

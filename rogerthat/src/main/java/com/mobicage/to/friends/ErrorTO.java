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

package com.mobicage.to.friends;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class ErrorTO implements com.mobicage.rpc.IJSONable {

    public String action;
    public String caption;
    public String message;
    public String title;

    public ErrorTO() {
    }

    public ErrorTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("action")) {
            Object val = json.get("action");
            this.action = (String) val;
        } else {
            this.action = null;
        }
        if (json.containsKey("caption")) {
            Object val = json.get("caption");
            this.caption = (String) val;
        } else {
            this.caption = null;
        }
        if (json.containsKey("message")) {
            Object val = json.get("message");
            this.message = (String) val;
        } else {
            this.message = null;
        }
        if (json.containsKey("title")) {
            Object val = json.get("title");
            this.title = (String) val;
        } else {
            this.title = null;
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("action", this.action);
        obj.put("caption", this.caption);
        obj.put("message", this.message);
        obj.put("title", this.title);
        return obj;
    }

}

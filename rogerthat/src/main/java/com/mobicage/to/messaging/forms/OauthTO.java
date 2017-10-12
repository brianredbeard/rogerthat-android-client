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

public class OauthTO implements com.mobicage.rpc.IJSONable {

    public String caption;
    public String success_message;
    public String url;

    public OauthTO() {
    }

    public OauthTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("caption")) {
            Object val = json.get("caption");
            this.caption = (String) val;
        } else {
            this.caption = null;
        }
        if (json.containsKey("success_message")) {
            Object val = json.get("success_message");
            this.success_message = (String) val;
        } else {
            this.success_message = null;
        }
        if (json.containsKey("url")) {
            Object val = json.get("url");
            this.url = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.forms.OauthTO object is missing field 'url'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("caption", this.caption);
        obj.put("success_message", this.success_message);
        obj.put("url", this.url);
        return obj;
    }

}

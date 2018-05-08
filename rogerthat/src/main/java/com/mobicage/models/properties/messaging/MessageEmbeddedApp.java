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

package com.mobicage.models.properties.messaging;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class MessageEmbeddedApp implements com.mobicage.rpc.IJSONable {

    public String context;
    public String description;
    public String id;
    public String image_url;
    public String result;
    public String title;

    public MessageEmbeddedApp() {
    }

    public MessageEmbeddedApp(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("context")) {
            Object val = json.get("context");
            this.context = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.models.properties.messaging.MessageEmbeddedApp object is missing field 'context'");
        }
        if (json.containsKey("description")) {
            Object val = json.get("description");
            this.description = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.models.properties.messaging.MessageEmbeddedApp object is missing field 'description'");
        }
        if (json.containsKey("id")) {
            Object val = json.get("id");
            this.id = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.models.properties.messaging.MessageEmbeddedApp object is missing field 'id'");
        }
        if (json.containsKey("image_url")) {
            Object val = json.get("image_url");
            this.image_url = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.models.properties.messaging.MessageEmbeddedApp object is missing field 'image_url'");
        }
        if (json.containsKey("result")) {
            Object val = json.get("result");
            this.result = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.models.properties.messaging.MessageEmbeddedApp object is missing field 'result'");
        }
        if (json.containsKey("title")) {
            Object val = json.get("title");
            this.title = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.models.properties.messaging.MessageEmbeddedApp object is missing field 'title'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("context", this.context);
        obj.put("description", this.description);
        obj.put("id", this.id);
        obj.put("image_url", this.image_url);
        obj.put("result", this.result);
        obj.put("title", this.title);
        return obj;
    }

}

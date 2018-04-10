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

package com.mobicage.to.app;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class GetEmbeddedAppResponseTO implements com.mobicage.rpc.IJSONable {

    public String name;
    public String serving_url;
    public long version;

    public GetEmbeddedAppResponseTO() {
    }

    public GetEmbeddedAppResponseTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("name")) {
            Object val = json.get("name");
            this.name = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.app.GetEmbeddedAppResponseTO object is missing field 'name'");
        }
        if (json.containsKey("serving_url")) {
            Object val = json.get("serving_url");
            this.serving_url = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.app.GetEmbeddedAppResponseTO object is missing field 'serving_url'");
        }
        if (json.containsKey("version")) {
            Object val = json.get("version");
            if (val instanceof Integer) {
                this.version = ((Integer) val).longValue();
            } else {
                this.version = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.app.GetEmbeddedAppResponseTO object is missing field 'version'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("name", this.name);
        obj.put("serving_url", this.serving_url);
        obj.put("version", this.version);
        return obj;
    }

}

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

package com.mobicage.models.properties.profiles;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class PublicKeyTO implements com.mobicage.rpc.IJSONable {

    public String algorithm;
    public String index;
    public String name;
    public String public_key;

    public PublicKeyTO() {
    }

    public PublicKeyTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("algorithm")) {
            Object val = json.get("algorithm");
            this.algorithm = (String) val;
        } else {
            this.algorithm = null;
        }
        if (json.containsKey("index")) {
            Object val = json.get("index");
            this.index = (String) val;
        } else {
            this.index = null;
        }
        if (json.containsKey("name")) {
            Object val = json.get("name");
            this.name = (String) val;
        } else {
            this.name = null;
        }
        if (json.containsKey("public_key")) {
            Object val = json.get("public_key");
            this.public_key = (String) val;
        } else {
            this.public_key = null;
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("algorithm", this.algorithm);
        obj.put("index", this.index);
        obj.put("name", this.name);
        obj.put("public_key", this.public_key);
        return obj;
    }

}

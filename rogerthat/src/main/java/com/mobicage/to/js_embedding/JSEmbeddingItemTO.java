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

package com.mobicage.to.js_embedding;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class JSEmbeddingItemTO implements com.mobicage.rpc.IJSONable {

    public String hash;
    public String name;

    public JSEmbeddingItemTO() {
    }

    public JSEmbeddingItemTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("hash")) {
            Object val = json.get("hash");
            this.hash = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.js_embedding.JSEmbeddingItemTO object is missing field 'hash'");
        }
        if (json.containsKey("name")) {
            Object val = json.get("name");
            this.name = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.js_embedding.JSEmbeddingItemTO object is missing field 'name'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("hash", this.hash);
        obj.put("name", this.name);
        return obj;
    }

}

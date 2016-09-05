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

package com.mobicage.to.js_embedding;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class UpdateJSEmbeddingRequestTO implements com.mobicage.rpc.IJSONable {

    public com.mobicage.to.js_embedding.JSEmbeddingItemTO[] items;

    public UpdateJSEmbeddingRequestTO() {
    }

    @SuppressWarnings("unchecked")
    public UpdateJSEmbeddingRequestTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("items")) {
            org.json.simple.JSONArray val_arr = (org.json.simple.JSONArray) json.get("items");
            if (val_arr == null) {
                this.items = null;
            } else {
                this.items = new com.mobicage.to.js_embedding.JSEmbeddingItemTO[val_arr.size()];
                for (int i=0; i < val_arr.size(); i++) {
                    Object item = val_arr.get(i);
                    if (item != null) {
                        this.items[i] = new com.mobicage.to.js_embedding.JSEmbeddingItemTO((Map<String, Object>) item);
                    }
                }
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.js_embedding.UpdateJSEmbeddingRequestTO object is missing field 'items'");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        if (this.items == null) {
            obj.put("items", null);
        } else {
            org.json.simple.JSONArray arr = new org.json.simple.JSONArray();
            for (int i=0; i < this.items.length; i++) {
                arr.add(this.items[i].toJSONMap());
            }
            obj.put("items", arr);
        }
        return obj;
    }

}

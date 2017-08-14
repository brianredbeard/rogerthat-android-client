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

package com.mobicage.to.system;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class UpdateEmbeddedAppTranslationsRequestTO implements com.mobicage.rpc.IJSONable {

    public com.mobicage.to.system.EmbeddedAppTranslationsTO[] translations;

    public UpdateEmbeddedAppTranslationsRequestTO() {
    }

    @SuppressWarnings("unchecked")
    public UpdateEmbeddedAppTranslationsRequestTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("translations")) {
            org.json.simple.JSONArray val_arr = (org.json.simple.JSONArray) json.get("translations");
            if (val_arr == null) {
                this.translations = null;
            } else {
                this.translations = new com.mobicage.to.system.EmbeddedAppTranslationsTO[val_arr.size()];
                for (int i=0; i < val_arr.size(); i++) {
                    Object item = val_arr.get(i);
                    if (item != null) {
                        this.translations[i] = new com.mobicage.to.system.EmbeddedAppTranslationsTO((Map<String, Object>) item);
                    }
                }
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.system.UpdateEmbeddedAppTranslationsRequestTO object is missing field 'translations'");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        if (this.translations == null) {
            obj.put("translations", null);
        } else {
            org.json.simple.JSONArray arr = new org.json.simple.JSONArray();
            for (int i=0; i < this.translations.length; i++) {
                arr.add(this.translations[i].toJSONMap());
            }
            obj.put("translations", arr);
        }
        return obj;
    }

}
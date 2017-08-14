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

public class RatingWidgetResultTO implements com.mobicage.rpc.IJSONable {

    public com.mobicage.models.properties.forms.RatingTopic[] topics;

    public RatingWidgetResultTO() {
    }

    @SuppressWarnings("unchecked")
    public RatingWidgetResultTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("topics")) {
            org.json.simple.JSONArray val_arr = (org.json.simple.JSONArray) json.get("topics");
            if (val_arr == null) {
                this.topics = null;
            } else {
                this.topics = new com.mobicage.models.properties.forms.RatingTopic[val_arr.size()];
                for (int i=0; i < val_arr.size(); i++) {
                    Object item = val_arr.get(i);
                    if (item != null) {
                        this.topics[i] = new com.mobicage.models.properties.forms.RatingTopic((Map<String, Object>) item);
                    }
                }
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.forms.RatingWidgetResultTO object is missing field 'topics'");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        if (this.topics == null) {
            obj.put("topics", null);
        } else {
            org.json.simple.JSONArray arr = new org.json.simple.JSONArray();
            for (int i=0; i < this.topics.length; i++) {
                arr.add(this.topics[i].toJSONMap());
            }
            obj.put("topics", arr);
        }
        return obj;
    }

}
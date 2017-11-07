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

package com.mobicage.models.properties.forms;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class RatingTopic implements com.mobicage.rpc.IJSONable {

    public String name;
    public String question;
    public float score;
    public String title;
    public long total_count;

    public RatingTopic() {
    }

    public RatingTopic(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("name")) {
            Object val = json.get("name");
            this.name = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.models.properties.forms.RatingTopic object is missing field 'name'");
        }
        if (json.containsKey("question")) {
            Object val = json.get("question");
            this.question = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.models.properties.forms.RatingTopic object is missing field 'question'");
        }
        if (json.containsKey("score")) {
            Object val = json.get("score");
            if (val instanceof Float) {
                this.score = ((Float) val).floatValue();
            } else if (val instanceof Double) {
                this.score = new Float((Double) val).floatValue();
            } else {
                this.score = new Float((Long) val).floatValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.models.properties.forms.RatingTopic object is missing field 'score'");
        }
        if (json.containsKey("title")) {
            Object val = json.get("title");
            this.title = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.models.properties.forms.RatingTopic object is missing field 'title'");
        }
        if (json.containsKey("total_count")) {
            Object val = json.get("total_count");
            if (val instanceof Integer) {
                this.total_count = ((Integer) val).longValue();
            } else {
                this.total_count = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.models.properties.forms.RatingTopic object is missing field 'total_count'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("name", this.name);
        obj.put("question", this.question);
        obj.put("score", this.score);
        obj.put("title", this.title);
        obj.put("total_count", this.total_count);
        return obj;
    }

}

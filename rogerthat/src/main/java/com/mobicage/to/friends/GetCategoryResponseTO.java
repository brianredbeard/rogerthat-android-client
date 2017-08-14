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

package com.mobicage.to.friends;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class GetCategoryResponseTO implements com.mobicage.rpc.IJSONable {

    public com.mobicage.to.friends.FriendCategoryTO category;

    public GetCategoryResponseTO() {
    }

    @SuppressWarnings("unchecked")
    public GetCategoryResponseTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("category")) {
            Object val = json.get("category");
            this.category = val == null ? null : new com.mobicage.to.friends.FriendCategoryTO((Map<String, Object>) val);
        } else {
            throw new IncompleteMessageException("com.mobicage.to.friends.GetCategoryResponseTO object is missing field 'category'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("category", this.category == null ? null : this.category.toJSONMap());
        return obj;
    }

}
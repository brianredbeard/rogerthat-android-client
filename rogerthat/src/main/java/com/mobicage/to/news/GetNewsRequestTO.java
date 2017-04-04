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

package com.mobicage.to.news;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class GetNewsRequestTO implements com.mobicage.rpc.IJSONable {

    public String cursor;
    public long updated_since;

    public GetNewsRequestTO() {
    }

    public GetNewsRequestTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("cursor")) {
            Object val = json.get("cursor");
            this.cursor = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.news.GetNewsRequestTO object is missing field 'cursor'");
        }
        if (json.containsKey("updated_since")) {
            Object val = json.get("updated_since");
            if (val instanceof Integer) {
                this.updated_since = ((Integer) val).longValue();
            } else {
                this.updated_since = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.news.GetNewsRequestTO object is missing field 'updated_since'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("cursor", this.cursor);
        obj.put("updated_since", this.updated_since);
        return obj;
    }

}

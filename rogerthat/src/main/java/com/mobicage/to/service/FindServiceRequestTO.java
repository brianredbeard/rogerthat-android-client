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

package com.mobicage.to.service;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class FindServiceRequestTO implements com.mobicage.rpc.IJSONable {

    public com.mobicage.to.activity.GeoPointWithTimestampTO geo_point;
    public long avatar_size;
    public String cursor;
    public String hashed_tag;
    public long organization_type;
    public String search_string;

    public FindServiceRequestTO() {
    }

    @SuppressWarnings("unchecked")
    public FindServiceRequestTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("geo_point")) {
            Object val = json.get("geo_point");
            this.geo_point = val == null ? null : new com.mobicage.to.activity.GeoPointWithTimestampTO((Map<String, Object>) val);
        } else {
            throw new IncompleteMessageException("com.mobicage.to.service.FindServiceRequestTO object is missing field 'geo_point'");
        }
        if (json.containsKey("avatar_size")) {
            Object val = json.get("avatar_size");
            if (val instanceof Integer) {
                this.avatar_size = ((Integer) val).longValue();
            } else {
                this.avatar_size = ((Long) val).longValue();
            }
        } else {
            this.avatar_size = 50;
        }
        if (json.containsKey("cursor")) {
            Object val = json.get("cursor");
            this.cursor = (String) val;
        } else {
            this.cursor = null;
        }
        if (json.containsKey("hashed_tag")) {
            Object val = json.get("hashed_tag");
            this.hashed_tag = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.service.FindServiceRequestTO object is missing field 'hashed_tag'");
        }
        if (json.containsKey("organization_type")) {
            Object val = json.get("organization_type");
            if (val instanceof Integer) {
                this.organization_type = ((Integer) val).longValue();
            } else {
                this.organization_type = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.service.FindServiceRequestTO object is missing field 'organization_type'");
        }
        if (json.containsKey("search_string")) {
            Object val = json.get("search_string");
            this.search_string = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.service.FindServiceRequestTO object is missing field 'search_string'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("geo_point", this.geo_point == null ? null : this.geo_point.toJSONMap());
        obj.put("avatar_size", this.avatar_size);
        obj.put("cursor", this.cursor);
        obj.put("hashed_tag", this.hashed_tag);
        obj.put("organization_type", this.organization_type);
        obj.put("search_string", this.search_string);
        return obj;
    }

}

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

package com.mobicage.to.friends;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class GetGroupAvatarRequestTO implements com.mobicage.rpc.IJSONable {

    public String avatar_hash;
    public long size;

    public GetGroupAvatarRequestTO() {
    }

    public GetGroupAvatarRequestTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("avatar_hash")) {
            Object val = json.get("avatar_hash");
            this.avatar_hash = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.friends.GetGroupAvatarRequestTO object is missing field 'avatar_hash'");
        }
        if (json.containsKey("size")) {
            Object val = json.get("size");
            if (val instanceof Integer) {
                this.size = ((Integer) val).longValue();
            } else {
                this.size = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.friends.GetGroupAvatarRequestTO object is missing field 'size'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("avatar_hash", this.avatar_hash);
        obj.put("size", this.size);
        return obj;
    }

}

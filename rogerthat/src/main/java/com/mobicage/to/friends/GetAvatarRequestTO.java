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

public class GetAvatarRequestTO implements com.mobicage.rpc.IJSONable {

    public long avatarId;
    public long size;

    public GetAvatarRequestTO() {
    }

    public GetAvatarRequestTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("avatarId")) {
            Object val = json.get("avatarId");
            if (val instanceof Integer) {
                this.avatarId = ((Integer) val).longValue();
            } else {
                this.avatarId = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.friends.GetAvatarRequestTO object is missing field 'avatarId'");
        }
        if (json.containsKey("size")) {
            Object val = json.get("size");
            if (val instanceof Integer) {
                this.size = ((Integer) val).longValue();
            } else {
                this.size = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.friends.GetAvatarRequestTO object is missing field 'size'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("avatarId", this.avatarId);
        obj.put("size", this.size);
        return obj;
    }

}

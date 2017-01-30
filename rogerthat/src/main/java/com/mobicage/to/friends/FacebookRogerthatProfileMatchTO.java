/*
 * Copyright 2017 Mobicage NV
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
 * @@license_version:1.2@@
 */

package com.mobicage.to.friends;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class FacebookRogerthatProfileMatchTO implements com.mobicage.rpc.IJSONable {

    public String fbId;
    public String fbName;
    public String fbPicture;
    public String rtId;

    public FacebookRogerthatProfileMatchTO() {
    }

    public FacebookRogerthatProfileMatchTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("fbId")) {
            Object val = json.get("fbId");
            this.fbId = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.friends.FacebookRogerthatProfileMatchTO object is missing field 'fbId'");
        }
        if (json.containsKey("fbName")) {
            Object val = json.get("fbName");
            this.fbName = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.friends.FacebookRogerthatProfileMatchTO object is missing field 'fbName'");
        }
        if (json.containsKey("fbPicture")) {
            Object val = json.get("fbPicture");
            this.fbPicture = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.friends.FacebookRogerthatProfileMatchTO object is missing field 'fbPicture'");
        }
        if (json.containsKey("rtId")) {
            Object val = json.get("rtId");
            this.rtId = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.friends.FacebookRogerthatProfileMatchTO object is missing field 'rtId'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("fbId", this.fbId);
        obj.put("fbName", this.fbName);
        obj.put("fbPicture", this.fbPicture);
        obj.put("rtId", this.rtId);
        return obj;
    }

}

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

public class ServiceMenuItemLinkTO implements com.mobicage.rpc.IJSONable {

    public boolean external;
    public String url;

    public ServiceMenuItemLinkTO() {
    }

    public ServiceMenuItemLinkTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("external")) {
            Object val = json.get("external");
            this.external = ((Boolean) val).booleanValue();
        } else {
            this.external = false;
        }
        if (json.containsKey("url")) {
            Object val = json.get("url");
            this.url = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.friends.ServiceMenuItemLinkTO object is missing field 'url'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("external", this.external);
        obj.put("url", this.url);
        return obj;
    }

}

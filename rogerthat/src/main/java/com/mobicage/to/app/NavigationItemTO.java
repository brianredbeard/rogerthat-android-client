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

package com.mobicage.to.app;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class NavigationItemTO implements com.mobicage.rpc.IJSONable {

    public String action;
    public String action_type;
    public boolean collapse;
    public String icon;
    public String icon_color;
    public String service_email;
    public String text;

    public NavigationItemTO() {
    }

    public NavigationItemTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("action")) {
            Object val = json.get("action");
            this.action = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.app.NavigationItemTO object is missing field 'action'");
        }
        if (json.containsKey("action_type")) {
            Object val = json.get("action_type");
            this.action_type = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.app.NavigationItemTO object is missing field 'action_type'");
        }
        if (json.containsKey("collapse")) {
            Object val = json.get("collapse");
            this.collapse = ((Boolean) val).booleanValue();
        } else {
            throw new IncompleteMessageException("com.mobicage.to.app.NavigationItemTO object is missing field 'collapse'");
        }
        if (json.containsKey("icon")) {
            Object val = json.get("icon");
            this.icon = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.app.NavigationItemTO object is missing field 'icon'");
        }
        if (json.containsKey("icon_color")) {
            Object val = json.get("icon_color");
            this.icon_color = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.app.NavigationItemTO object is missing field 'icon_color'");
        }
        if (json.containsKey("service_email")) {
            Object val = json.get("service_email");
            this.service_email = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.app.NavigationItemTO object is missing field 'service_email'");
        }
        if (json.containsKey("text")) {
            Object val = json.get("text");
            this.text = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.app.NavigationItemTO object is missing field 'text'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("action", this.action);
        obj.put("action_type", this.action_type);
        obj.put("collapse", this.collapse);
        obj.put("icon", this.icon);
        obj.put("icon_color", this.icon_color);
        obj.put("service_email", this.service_email);
        obj.put("text", this.text);
        return obj;
    }

}

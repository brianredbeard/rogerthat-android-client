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

public class LookAndFeelTO implements com.mobicage.rpc.IJSONable {

    public com.mobicage.to.app.ColorSettingsTO colors;
    public com.mobicage.to.app.HomeScreenSettingsTO homescreen;
    public com.mobicage.to.app.ToolbarSettingsTO toolbar;

    public LookAndFeelTO() {
    }

    @SuppressWarnings("unchecked")
    public LookAndFeelTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("colors")) {
            Object val = json.get("colors");
            this.colors = val == null ? null : new com.mobicage.to.app.ColorSettingsTO((Map<String, Object>) val);
        } else {
            throw new IncompleteMessageException("com.mobicage.to.app.LookAndFeelTO object is missing field 'colors'");
        }
        if (json.containsKey("homescreen")) {
            Object val = json.get("homescreen");
            this.homescreen = val == null ? null : new com.mobicage.to.app.HomeScreenSettingsTO((Map<String, Object>) val);
        } else {
            throw new IncompleteMessageException("com.mobicage.to.app.LookAndFeelTO object is missing field 'homescreen'");
        }
        if (json.containsKey("toolbar")) {
            Object val = json.get("toolbar");
            this.toolbar = val == null ? null : new com.mobicage.to.app.ToolbarSettingsTO((Map<String, Object>) val);
        } else {
            throw new IncompleteMessageException("com.mobicage.to.app.LookAndFeelTO object is missing field 'toolbar'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("colors", this.colors == null ? null : this.colors.toJSONMap());
        obj.put("homescreen", this.homescreen == null ? null : this.homescreen.toJSONMap());
        obj.put("toolbar", this.toolbar == null ? null : this.toolbar.toJSONMap());
        return obj;
    }

}
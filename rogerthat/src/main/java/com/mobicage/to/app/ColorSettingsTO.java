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

package com.mobicage.to.app;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class ColorSettingsTO implements com.mobicage.rpc.IJSONable {

    public String primary_color;
    public String primary_color_dark;
    public String primary_icon_color;
    public String secondary_color;
    public String tint_color;

    public ColorSettingsTO() {
    }

    public ColorSettingsTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("primary_color")) {
            Object val = json.get("primary_color");
            this.primary_color = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.app.ColorSettingsTO object is missing field 'primary_color'");
        }
        if (json.containsKey("primary_color_dark")) {
            Object val = json.get("primary_color_dark");
            this.primary_color_dark = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.app.ColorSettingsTO object is missing field 'primary_color_dark'");
        }
        if (json.containsKey("primary_icon_color")) {
            Object val = json.get("primary_icon_color");
            this.primary_icon_color = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.app.ColorSettingsTO object is missing field 'primary_icon_color'");
        }
        if (json.containsKey("secondary_color")) {
            Object val = json.get("secondary_color");
            this.secondary_color = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.app.ColorSettingsTO object is missing field 'secondary_color'");
        }
        if (json.containsKey("tint_color")) {
            Object val = json.get("tint_color");
            this.tint_color = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.app.ColorSettingsTO object is missing field 'tint_color'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("primary_color", this.primary_color);
        obj.put("primary_color_dark", this.primary_color_dark);
        obj.put("primary_icon_color", this.primary_icon_color);
        obj.put("secondary_color", this.secondary_color);
        obj.put("tint_color", this.tint_color);
        return obj;
    }

}

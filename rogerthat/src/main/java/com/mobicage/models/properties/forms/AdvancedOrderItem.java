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

package com.mobicage.models.properties.forms;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class AdvancedOrderItem implements com.mobicage.rpc.IJSONable {

    public String description;
    public boolean has_price;
    public String id;
    public String image_url;
    public String name;
    public long step;
    public String step_unit;
    public long step_unit_conversion;
    public String unit;
    public long unit_price;
    public long value;

    public AdvancedOrderItem() {
    }

    public AdvancedOrderItem(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("description")) {
            Object val = json.get("description");
            this.description = (String) val;
        } else {
            this.description = null;
        }
        if (json.containsKey("has_price")) {
            Object val = json.get("has_price");
            this.has_price = ((Boolean) val).booleanValue();
        } else {
            this.has_price = true;
        }
        if (json.containsKey("id")) {
            Object val = json.get("id");
            this.id = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.models.properties.forms.AdvancedOrderItem object is missing field 'id'");
        }
        if (json.containsKey("image_url")) {
            Object val = json.get("image_url");
            this.image_url = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.models.properties.forms.AdvancedOrderItem object is missing field 'image_url'");
        }
        if (json.containsKey("name")) {
            Object val = json.get("name");
            this.name = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.models.properties.forms.AdvancedOrderItem object is missing field 'name'");
        }
        if (json.containsKey("step")) {
            Object val = json.get("step");
            if (val instanceof Integer) {
                this.step = ((Integer) val).longValue();
            } else {
                this.step = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.models.properties.forms.AdvancedOrderItem object is missing field 'step'");
        }
        if (json.containsKey("step_unit")) {
            Object val = json.get("step_unit");
            this.step_unit = (String) val;
        } else {
            this.step_unit = null;
        }
        if (json.containsKey("step_unit_conversion")) {
            Object val = json.get("step_unit_conversion");
            if (val instanceof Integer) {
                this.step_unit_conversion = ((Integer) val).longValue();
            } else {
                this.step_unit_conversion = ((Long) val).longValue();
            }
        } else {
            this.step_unit_conversion = 0;
        }
        if (json.containsKey("unit")) {
            Object val = json.get("unit");
            this.unit = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.models.properties.forms.AdvancedOrderItem object is missing field 'unit'");
        }
        if (json.containsKey("unit_price")) {
            Object val = json.get("unit_price");
            if (val instanceof Integer) {
                this.unit_price = ((Integer) val).longValue();
            } else {
                this.unit_price = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.models.properties.forms.AdvancedOrderItem object is missing field 'unit_price'");
        }
        if (json.containsKey("value")) {
            Object val = json.get("value");
            if (val instanceof Integer) {
                this.value = ((Integer) val).longValue();
            } else {
                this.value = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.models.properties.forms.AdvancedOrderItem object is missing field 'value'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("description", this.description);
        obj.put("has_price", this.has_price);
        obj.put("id", this.id);
        obj.put("image_url", this.image_url);
        obj.put("name", this.name);
        obj.put("step", this.step);
        obj.put("step_unit", this.step_unit);
        obj.put("step_unit_conversion", this.step_unit_conversion);
        obj.put("unit", this.unit);
        obj.put("unit_price", this.unit_price);
        obj.put("value", this.value);
        return obj;
    }

}

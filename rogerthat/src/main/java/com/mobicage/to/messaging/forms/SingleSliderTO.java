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

package com.mobicage.to.messaging.forms;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class SingleSliderTO implements com.mobicage.rpc.IJSONable {

    public float max;
    public float min;
    public long precision;
    public float step;
    public String unit;
    public float value;

    public SingleSliderTO() {
    }

    public SingleSliderTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("max")) {
            Object val = json.get("max");
            if (val instanceof Float) {
                this.max = ((Float) val).floatValue();
            } else if (val instanceof Double) {
                this.max = new Float((Double) val).floatValue();
            } else {
                this.max = new Float((Long) val).floatValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.forms.SingleSliderTO object is missing field 'max'");
        }
        if (json.containsKey("min")) {
            Object val = json.get("min");
            if (val instanceof Float) {
                this.min = ((Float) val).floatValue();
            } else if (val instanceof Double) {
                this.min = new Float((Double) val).floatValue();
            } else {
                this.min = new Float((Long) val).floatValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.forms.SingleSliderTO object is missing field 'min'");
        }
        if (json.containsKey("precision")) {
            Object val = json.get("precision");
            if (val instanceof Integer) {
                this.precision = ((Integer) val).longValue();
            } else {
                this.precision = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.forms.SingleSliderTO object is missing field 'precision'");
        }
        if (json.containsKey("step")) {
            Object val = json.get("step");
            if (val instanceof Float) {
                this.step = ((Float) val).floatValue();
            } else if (val instanceof Double) {
                this.step = new Float((Double) val).floatValue();
            } else {
                this.step = new Float((Long) val).floatValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.forms.SingleSliderTO object is missing field 'step'");
        }
        if (json.containsKey("unit")) {
            Object val = json.get("unit");
            this.unit = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.forms.SingleSliderTO object is missing field 'unit'");
        }
        if (json.containsKey("value")) {
            Object val = json.get("value");
            if (val instanceof Float) {
                this.value = ((Float) val).floatValue();
            } else if (val instanceof Double) {
                this.value = new Float((Double) val).floatValue();
            } else {
                this.value = new Float((Long) val).floatValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.forms.SingleSliderTO object is missing field 'value'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("max", this.max);
        obj.put("min", this.min);
        obj.put("precision", this.precision);
        obj.put("step", this.step);
        obj.put("unit", this.unit);
        obj.put("value", this.value);
        return obj;
    }

}

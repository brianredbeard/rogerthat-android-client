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

package com.mobicage.to.messaging.forms;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class RangeSliderTO implements com.mobicage.rpc.IJSONable {

    public float high_value;
    public float low_value;
    public float max;
    public float min;
    public long precision;
    public float step;
    public String unit;

    public RangeSliderTO() {
    }

    public RangeSliderTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("high_value")) {
            Object val = json.get("high_value");
            if (val instanceof Float) {
                this.high_value = ((Float) val).floatValue();
            } else if (val instanceof Double) {
                this.high_value = new Float((Double) val).floatValue();
            } else {
                this.high_value = new Float((Long) val).floatValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.forms.RangeSliderTO object is missing field 'high_value'");
        }
        if (json.containsKey("low_value")) {
            Object val = json.get("low_value");
            if (val instanceof Float) {
                this.low_value = ((Float) val).floatValue();
            } else if (val instanceof Double) {
                this.low_value = new Float((Double) val).floatValue();
            } else {
                this.low_value = new Float((Long) val).floatValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.forms.RangeSliderTO object is missing field 'low_value'");
        }
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
            throw new IncompleteMessageException("com.mobicage.to.messaging.forms.RangeSliderTO object is missing field 'max'");
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
            throw new IncompleteMessageException("com.mobicage.to.messaging.forms.RangeSliderTO object is missing field 'min'");
        }
        if (json.containsKey("precision")) {
            Object val = json.get("precision");
            if (val instanceof Integer) {
                this.precision = ((Integer) val).longValue();
            } else {
                this.precision = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.forms.RangeSliderTO object is missing field 'precision'");
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
            throw new IncompleteMessageException("com.mobicage.to.messaging.forms.RangeSliderTO object is missing field 'step'");
        }
        if (json.containsKey("unit")) {
            Object val = json.get("unit");
            this.unit = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.forms.RangeSliderTO object is missing field 'unit'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("high_value", this.high_value);
        obj.put("low_value", this.low_value);
        obj.put("max", this.max);
        obj.put("min", this.min);
        obj.put("precision", this.precision);
        obj.put("step", this.step);
        obj.put("unit", this.unit);
        return obj;
    }

}

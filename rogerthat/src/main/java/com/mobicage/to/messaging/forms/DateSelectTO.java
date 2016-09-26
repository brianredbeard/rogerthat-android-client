/*
 * Copyright 2016 Mobicage NV
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
 * @@license_version:1.1@@
 */

package com.mobicage.to.messaging.forms;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class DateSelectTO implements com.mobicage.rpc.IJSONable {

    public long date;
    public boolean has_date;
    public boolean has_max_date;
    public boolean has_min_date;
    public long max_date;
    public long min_date;
    public long minute_interval;
    public String mode;
    public String unit;

    public DateSelectTO() {
    }

    public DateSelectTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("date")) {
            Object val = json.get("date");
            if (val instanceof Integer) {
                this.date = ((Integer) val).longValue();
            } else {
                this.date = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.forms.DateSelectTO object is missing field 'date'");
        }
        if (json.containsKey("has_date")) {
            Object val = json.get("has_date");
            this.has_date = ((Boolean) val).booleanValue();
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.forms.DateSelectTO object is missing field 'has_date'");
        }
        if (json.containsKey("has_max_date")) {
            Object val = json.get("has_max_date");
            this.has_max_date = ((Boolean) val).booleanValue();
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.forms.DateSelectTO object is missing field 'has_max_date'");
        }
        if (json.containsKey("has_min_date")) {
            Object val = json.get("has_min_date");
            this.has_min_date = ((Boolean) val).booleanValue();
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.forms.DateSelectTO object is missing field 'has_min_date'");
        }
        if (json.containsKey("max_date")) {
            Object val = json.get("max_date");
            if (val instanceof Integer) {
                this.max_date = ((Integer) val).longValue();
            } else {
                this.max_date = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.forms.DateSelectTO object is missing field 'max_date'");
        }
        if (json.containsKey("min_date")) {
            Object val = json.get("min_date");
            if (val instanceof Integer) {
                this.min_date = ((Integer) val).longValue();
            } else {
                this.min_date = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.forms.DateSelectTO object is missing field 'min_date'");
        }
        if (json.containsKey("minute_interval")) {
            Object val = json.get("minute_interval");
            if (val instanceof Integer) {
                this.minute_interval = ((Integer) val).longValue();
            } else {
                this.minute_interval = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.forms.DateSelectTO object is missing field 'minute_interval'");
        }
        if (json.containsKey("mode")) {
            Object val = json.get("mode");
            this.mode = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.forms.DateSelectTO object is missing field 'mode'");
        }
        if (json.containsKey("unit")) {
            Object val = json.get("unit");
            this.unit = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.forms.DateSelectTO object is missing field 'unit'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("date", this.date);
        obj.put("has_date", this.has_date);
        obj.put("has_max_date", this.has_max_date);
        obj.put("has_min_date", this.has_min_date);
        obj.put("max_date", this.max_date);
        obj.put("min_date", this.min_date);
        obj.put("minute_interval", this.minute_interval);
        obj.put("mode", this.mode);
        obj.put("unit", this.unit);
        return obj;
    }

}

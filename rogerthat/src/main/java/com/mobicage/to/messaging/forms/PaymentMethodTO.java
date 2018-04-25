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

public class PaymentMethodTO implements com.mobicage.rpc.IJSONable {

    public long amount;
    public boolean calculate_amount;
    public String currency;
    public long precision;
    public String provider_id;
    public String target;

    public PaymentMethodTO() {
    }

    public PaymentMethodTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("amount")) {
            Object val = json.get("amount");
            if (val instanceof Integer) {
                this.amount = ((Integer) val).longValue();
            } else {
                this.amount = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.forms.PaymentMethodTO object is missing field 'amount'");
        }
        if (json.containsKey("calculate_amount")) {
            Object val = json.get("calculate_amount");
            this.calculate_amount = ((Boolean) val).booleanValue();
        } else {
            this.calculate_amount = false;
        }
        if (json.containsKey("currency")) {
            Object val = json.get("currency");
            this.currency = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.forms.PaymentMethodTO object is missing field 'currency'");
        }
        if (json.containsKey("precision")) {
            Object val = json.get("precision");
            if (val instanceof Integer) {
                this.precision = ((Integer) val).longValue();
            } else {
                this.precision = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.forms.PaymentMethodTO object is missing field 'precision'");
        }
        if (json.containsKey("provider_id")) {
            Object val = json.get("provider_id");
            this.provider_id = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.forms.PaymentMethodTO object is missing field 'provider_id'");
        }
        if (json.containsKey("target")) {
            Object val = json.get("target");
            this.target = (String) val;
        } else {
            this.target = null;
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("amount", this.amount);
        obj.put("calculate_amount", this.calculate_amount);
        obj.put("currency", this.currency);
        obj.put("precision", this.precision);
        obj.put("provider_id", this.provider_id);
        obj.put("target", this.target);
        return obj;
    }

}

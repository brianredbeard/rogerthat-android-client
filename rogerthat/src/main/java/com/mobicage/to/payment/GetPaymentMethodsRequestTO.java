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

package com.mobicage.to.payment;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class GetPaymentMethodsRequestTO implements com.mobicage.rpc.IJSONable {

    public com.mobicage.models.properties.forms.BasePaymentMethod base_method;
    public com.mobicage.to.messaging.forms.PaymentMethodTO[] methods;
    public String target;
    public boolean test_mode;

    public GetPaymentMethodsRequestTO() {
    }

    @SuppressWarnings("unchecked")
    public GetPaymentMethodsRequestTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("base_method")) {
            Object val = json.get("base_method");
            this.base_method = val == null ? null : new com.mobicage.models.properties.forms.BasePaymentMethod((Map<String, Object>) val);
        } else {
            throw new IncompleteMessageException("com.mobicage.to.payment.GetPaymentMethodsRequestTO object is missing field 'base_method'");
        }
        if (json.containsKey("methods")) {
            org.json.simple.JSONArray val_arr = (org.json.simple.JSONArray) json.get("methods");
            if (val_arr == null) {
                this.methods = null;
            } else {
                this.methods = new com.mobicage.to.messaging.forms.PaymentMethodTO[val_arr.size()];
                for (int i=0; i < val_arr.size(); i++) {
                    Object item = val_arr.get(i);
                    if (item != null) {
                        this.methods[i] = new com.mobicage.to.messaging.forms.PaymentMethodTO((Map<String, Object>) item);
                    }
                }
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.payment.GetPaymentMethodsRequestTO object is missing field 'methods'");
        }
        if (json.containsKey("target")) {
            Object val = json.get("target");
            this.target = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.payment.GetPaymentMethodsRequestTO object is missing field 'target'");
        }
        if (json.containsKey("test_mode")) {
            Object val = json.get("test_mode");
            this.test_mode = ((Boolean) val).booleanValue();
        } else {
            throw new IncompleteMessageException("com.mobicage.to.payment.GetPaymentMethodsRequestTO object is missing field 'test_mode'");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("base_method", this.base_method == null ? null : this.base_method.toJSONMap());
        if (this.methods == null) {
            obj.put("methods", null);
        } else {
            org.json.simple.JSONArray arr = new org.json.simple.JSONArray();
            for (int i=0; i < this.methods.length; i++) {
                arr.add(this.methods[i].toJSONMap());
            }
            obj.put("methods", arr);
        }
        obj.put("target", this.target);
        obj.put("test_mode", this.test_mode);
        return obj;
    }

}

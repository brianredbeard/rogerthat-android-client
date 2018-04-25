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

public class PaymentProviderMethodsTO implements com.mobicage.rpc.IJSONable {

    public com.mobicage.to.payment.PayMethodTO[] methods;
    public com.mobicage.to.payment.AppPaymentProviderTO provider;

    public PaymentProviderMethodsTO() {
    }

    @SuppressWarnings("unchecked")
    public PaymentProviderMethodsTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("methods")) {
            org.json.simple.JSONArray val_arr = (org.json.simple.JSONArray) json.get("methods");
            if (val_arr == null) {
                this.methods = null;
            } else {
                this.methods = new com.mobicage.to.payment.PayMethodTO[val_arr.size()];
                for (int i=0; i < val_arr.size(); i++) {
                    Object item = val_arr.get(i);
                    if (item != null) {
                        this.methods[i] = new com.mobicage.to.payment.PayMethodTO((Map<String, Object>) item);
                    }
                }
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.payment.PaymentProviderMethodsTO object is missing field 'methods'");
        }
        if (json.containsKey("provider")) {
            Object val = json.get("provider");
            this.provider = val == null ? null : new com.mobicage.to.payment.AppPaymentProviderTO((Map<String, Object>) val);
        } else {
            throw new IncompleteMessageException("com.mobicage.to.payment.PaymentProviderMethodsTO object is missing field 'provider'");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        if (this.methods == null) {
            obj.put("methods", null);
        } else {
            org.json.simple.JSONArray arr = new org.json.simple.JSONArray();
            for (int i=0; i < this.methods.length; i++) {
                arr.add(this.methods[i].toJSONMap());
            }
            obj.put("methods", arr);
        }
        obj.put("provider", this.provider == null ? null : this.provider.toJSONMap());
        return obj;
    }

}

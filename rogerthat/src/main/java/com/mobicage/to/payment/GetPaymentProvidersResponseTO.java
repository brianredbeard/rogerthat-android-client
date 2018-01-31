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

public class GetPaymentProvidersResponseTO implements com.mobicage.rpc.IJSONable {

    public com.mobicage.to.payment.AppPaymentProviderTO[] payment_providers;

    public GetPaymentProvidersResponseTO() {
    }

    @SuppressWarnings("unchecked")
    public GetPaymentProvidersResponseTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("payment_providers")) {
            org.json.simple.JSONArray val_arr = (org.json.simple.JSONArray) json.get("payment_providers");
            if (val_arr == null) {
                this.payment_providers = null;
            } else {
                this.payment_providers = new com.mobicage.to.payment.AppPaymentProviderTO[val_arr.size()];
                for (int i=0; i < val_arr.size(); i++) {
                    Object item = val_arr.get(i);
                    if (item != null) {
                        this.payment_providers[i] = new com.mobicage.to.payment.AppPaymentProviderTO((Map<String, Object>) item);
                    }
                }
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.payment.GetPaymentProvidersResponseTO object is missing field 'payment_providers'");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        if (this.payment_providers == null) {
            obj.put("payment_providers", null);
        } else {
            org.json.simple.JSONArray arr = new org.json.simple.JSONArray();
            for (int i=0; i < this.payment_providers.length; i++) {
                arr.add(this.payment_providers[i].toJSONMap());
            }
            obj.put("payment_providers", arr);
        }
        return obj;
    }

}

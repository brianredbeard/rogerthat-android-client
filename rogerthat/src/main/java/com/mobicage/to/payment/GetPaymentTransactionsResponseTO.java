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

package com.mobicage.to.payment;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class GetPaymentTransactionsResponseTO implements com.mobicage.rpc.IJSONable {

    public com.mobicage.to.payment.PaymentProviderTransactionTO[] transactions;
    public String cursor;

    public GetPaymentTransactionsResponseTO() {
    }

    @SuppressWarnings("unchecked")
    public GetPaymentTransactionsResponseTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("transactions")) {
            org.json.simple.JSONArray val_arr = (org.json.simple.JSONArray) json.get("transactions");
            if (val_arr == null) {
                this.transactions = null;
            } else {
                this.transactions = new com.mobicage.to.payment.PaymentProviderTransactionTO[val_arr.size()];
                for (int i=0; i < val_arr.size(); i++) {
                    Object item = val_arr.get(i);
                    if (item != null) {
                        this.transactions[i] = new com.mobicage.to.payment.PaymentProviderTransactionTO((Map<String, Object>) item);
                    }
                }
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.payment.GetPaymentTransactionsResponseTO object is missing field 'transactions'");
        }
        if (json.containsKey("cursor")) {
            Object val = json.get("cursor");
            this.cursor = (String) val;
        } else {
            this.cursor = null;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        if (this.transactions == null) {
            obj.put("transactions", null);
        } else {
            org.json.simple.JSONArray arr = new org.json.simple.JSONArray();
            for (int i=0; i < this.transactions.length; i++) {
                arr.add(this.transactions[i].toJSONMap());
            }
            obj.put("transactions", arr);
        }
        obj.put("cursor", this.cursor);
        return obj;
    }

}
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

public class CryptoTransactionTO implements com.mobicage.rpc.IJSONable {

    public com.mobicage.to.payment.CryptoTransactionDataTO[] data;
    public String from_address;
    public String minerfees;
    public String to_address;

    public CryptoTransactionTO() {
    }

    @SuppressWarnings("unchecked")
    public CryptoTransactionTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("data")) {
            org.json.simple.JSONArray val_arr = (org.json.simple.JSONArray) json.get("data");
            if (val_arr == null) {
                this.data = null;
            } else {
                this.data = new com.mobicage.to.payment.CryptoTransactionDataTO[val_arr.size()];
                for (int i=0; i < val_arr.size(); i++) {
                    Object item = val_arr.get(i);
                    if (item != null) {
                        this.data[i] = new com.mobicage.to.payment.CryptoTransactionDataTO((Map<String, Object>) item);
                    }
                }
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.payment.CryptoTransactionTO object is missing field 'data'");
        }
        if (json.containsKey("from_address")) {
            Object val = json.get("from_address");
            this.from_address = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.payment.CryptoTransactionTO object is missing field 'from_address'");
        }
        if (json.containsKey("minerfees")) {
            Object val = json.get("minerfees");
            this.minerfees = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.payment.CryptoTransactionTO object is missing field 'minerfees'");
        }
        if (json.containsKey("to_address")) {
            Object val = json.get("to_address");
            this.to_address = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.payment.CryptoTransactionTO object is missing field 'to_address'");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        if (this.data == null) {
            obj.put("data", null);
        } else {
            org.json.simple.JSONArray arr = new org.json.simple.JSONArray();
            for (int i=0; i < this.data.length; i++) {
                arr.add(this.data[i].toJSONMap());
            }
            obj.put("data", arr);
        }
        obj.put("from_address", this.from_address);
        obj.put("minerfees", this.minerfees);
        obj.put("to_address", this.to_address);
        return obj;
    }

}
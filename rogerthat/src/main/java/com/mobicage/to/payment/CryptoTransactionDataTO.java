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

public class CryptoTransactionDataTO implements com.mobicage.rpc.IJSONable {

    public com.mobicage.to.payment.CryptoTransactionInputTO input;
    public com.mobicage.to.payment.CryptoTransactionOutputTO[] outputs;
    public String algorithm;
    public String public_key;
    public long public_key_index;
    public String signature;
    public String signature_hash;
    public long timelock;

    public CryptoTransactionDataTO() {
    }

    @SuppressWarnings("unchecked")
    public CryptoTransactionDataTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("input")) {
            Object val = json.get("input");
            this.input = val == null ? null : new com.mobicage.to.payment.CryptoTransactionInputTO((Map<String, Object>) val);
        } else {
            throw new IncompleteMessageException("com.mobicage.to.payment.CryptoTransactionDataTO object is missing field 'input'");
        }
        if (json.containsKey("outputs")) {
            org.json.simple.JSONArray val_arr = (org.json.simple.JSONArray) json.get("outputs");
            if (val_arr == null) {
                this.outputs = null;
            } else {
                this.outputs = new com.mobicage.to.payment.CryptoTransactionOutputTO[val_arr.size()];
                for (int i=0; i < val_arr.size(); i++) {
                    Object item = val_arr.get(i);
                    if (item != null) {
                        this.outputs[i] = new com.mobicage.to.payment.CryptoTransactionOutputTO((Map<String, Object>) item);
                    }
                }
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.payment.CryptoTransactionDataTO object is missing field 'outputs'");
        }
        if (json.containsKey("algorithm")) {
            Object val = json.get("algorithm");
            this.algorithm = (String) val;
        } else {
            this.algorithm = null;
        }
        if (json.containsKey("public_key")) {
            Object val = json.get("public_key");
            this.public_key = (String) val;
        } else {
            this.public_key = null;
        }
        if (json.containsKey("public_key_index")) {
            Object val = json.get("public_key_index");
            if (val instanceof Integer) {
                this.public_key_index = ((Integer) val).longValue();
            } else {
                this.public_key_index = ((Long) val).longValue();
            }
        } else {
            this.public_key_index = 0;
        }
        if (json.containsKey("signature")) {
            Object val = json.get("signature");
            this.signature = (String) val;
        } else {
            this.signature = null;
        }
        if (json.containsKey("signature_hash")) {
            Object val = json.get("signature_hash");
            this.signature_hash = (String) val;
        } else {
            this.signature_hash = null;
        }
        if (json.containsKey("timelock")) {
            Object val = json.get("timelock");
            if (val instanceof Integer) {
                this.timelock = ((Integer) val).longValue();
            } else {
                this.timelock = ((Long) val).longValue();
            }
        } else {
            this.timelock = 0;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("input", this.input == null ? null : this.input.toJSONMap());
        if (this.outputs == null) {
            obj.put("outputs", null);
        } else {
            org.json.simple.JSONArray arr = new org.json.simple.JSONArray();
            for (int i=0; i < this.outputs.length; i++) {
                arr.add(this.outputs[i].toJSONMap());
            }
            obj.put("outputs", arr);
        }
        obj.put("algorithm", this.algorithm);
        obj.put("public_key", this.public_key);
        obj.put("public_key_index", this.public_key_index);
        obj.put("signature", this.signature);
        obj.put("signature_hash", this.signature_hash);
        obj.put("timelock", this.timelock);
        return obj;
    }

}

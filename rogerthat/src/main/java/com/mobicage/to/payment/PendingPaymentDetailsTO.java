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

public class PendingPaymentDetailsTO implements com.mobicage.rpc.IJSONable {

    public com.mobicage.to.payment.PaymentProviderAssetTO[] assets;
    public com.mobicage.to.payment.AppPaymentProviderTO provider;
    public com.mobicage.to.service.UserDetailsTO receiver;
    public com.mobicage.to.payment.PaymentProviderAssetTO receiver_asset;
    public long amount;
    public String currency;
    public String memo;
    public long precision;
    public String status;
    public long timestamp;
    public String transaction_id;

    public PendingPaymentDetailsTO() {
    }

    @SuppressWarnings("unchecked")
    public PendingPaymentDetailsTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("assets")) {
            org.json.simple.JSONArray val_arr = (org.json.simple.JSONArray) json.get("assets");
            if (val_arr == null) {
                this.assets = null;
            } else {
                this.assets = new com.mobicage.to.payment.PaymentProviderAssetTO[val_arr.size()];
                for (int i=0; i < val_arr.size(); i++) {
                    Object item = val_arr.get(i);
                    if (item != null) {
                        this.assets[i] = new com.mobicage.to.payment.PaymentProviderAssetTO((Map<String, Object>) item);
                    }
                }
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.payment.PendingPaymentDetailsTO object is missing field 'assets'");
        }
        if (json.containsKey("provider")) {
            Object val = json.get("provider");
            this.provider = val == null ? null : new com.mobicage.to.payment.AppPaymentProviderTO((Map<String, Object>) val);
        } else {
            throw new IncompleteMessageException("com.mobicage.to.payment.PendingPaymentDetailsTO object is missing field 'provider'");
        }
        if (json.containsKey("receiver")) {
            Object val = json.get("receiver");
            this.receiver = val == null ? null : new com.mobicage.to.service.UserDetailsTO((Map<String, Object>) val);
        } else {
            throw new IncompleteMessageException("com.mobicage.to.payment.PendingPaymentDetailsTO object is missing field 'receiver'");
        }
        if (json.containsKey("receiver_asset")) {
            Object val = json.get("receiver_asset");
            this.receiver_asset = val == null ? null : new com.mobicage.to.payment.PaymentProviderAssetTO((Map<String, Object>) val);
        } else {
            throw new IncompleteMessageException("com.mobicage.to.payment.PendingPaymentDetailsTO object is missing field 'receiver_asset'");
        }
        if (json.containsKey("amount")) {
            Object val = json.get("amount");
            if (val instanceof Integer) {
                this.amount = ((Integer) val).longValue();
            } else {
                this.amount = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.payment.PendingPaymentDetailsTO object is missing field 'amount'");
        }
        if (json.containsKey("currency")) {
            Object val = json.get("currency");
            this.currency = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.payment.PendingPaymentDetailsTO object is missing field 'currency'");
        }
        if (json.containsKey("memo")) {
            Object val = json.get("memo");
            this.memo = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.payment.PendingPaymentDetailsTO object is missing field 'memo'");
        }
        if (json.containsKey("precision")) {
            Object val = json.get("precision");
            if (val instanceof Integer) {
                this.precision = ((Integer) val).longValue();
            } else {
                this.precision = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.payment.PendingPaymentDetailsTO object is missing field 'precision'");
        }
        if (json.containsKey("status")) {
            Object val = json.get("status");
            this.status = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.payment.PendingPaymentDetailsTO object is missing field 'status'");
        }
        if (json.containsKey("timestamp")) {
            Object val = json.get("timestamp");
            if (val instanceof Integer) {
                this.timestamp = ((Integer) val).longValue();
            } else {
                this.timestamp = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.payment.PendingPaymentDetailsTO object is missing field 'timestamp'");
        }
        if (json.containsKey("transaction_id")) {
            Object val = json.get("transaction_id");
            this.transaction_id = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.payment.PendingPaymentDetailsTO object is missing field 'transaction_id'");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        if (this.assets == null) {
            obj.put("assets", null);
        } else {
            org.json.simple.JSONArray arr = new org.json.simple.JSONArray();
            for (int i=0; i < this.assets.length; i++) {
                arr.add(this.assets[i].toJSONMap());
            }
            obj.put("assets", arr);
        }
        obj.put("provider", this.provider == null ? null : this.provider.toJSONMap());
        obj.put("receiver", this.receiver == null ? null : this.receiver.toJSONMap());
        obj.put("receiver_asset", this.receiver_asset == null ? null : this.receiver_asset.toJSONMap());
        obj.put("amount", this.amount);
        obj.put("currency", this.currency);
        obj.put("memo", this.memo);
        obj.put("precision", this.precision);
        obj.put("status", this.status);
        obj.put("timestamp", this.timestamp);
        obj.put("transaction_id", this.transaction_id);
        return obj;
    }

}

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

public class PaymentProviderAssetTO implements com.mobicage.rpc.IJSONable {

    public com.mobicage.to.payment.PaymentAssetBalanceTO available_balance;
    public com.mobicage.to.payment.PaymentAssetRequiredActionTO required_action;
    public com.mobicage.to.payment.PaymentAssetBalanceTO total_balance;
    public String currency;
    public boolean enabled;
    public boolean has_balance;
    public boolean has_transactions;
    public String id;
    public String name;
    public String provider_id;
    public String type;
    public boolean verified;

    public PaymentProviderAssetTO() {
    }

    @SuppressWarnings("unchecked")
    public PaymentProviderAssetTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("available_balance")) {
            Object val = json.get("available_balance");
            this.available_balance = val == null ? null : new com.mobicage.to.payment.PaymentAssetBalanceTO((Map<String, Object>) val);
        } else {
            throw new IncompleteMessageException("com.mobicage.to.payment.PaymentProviderAssetTO object is missing field 'available_balance'");
        }
        if (json.containsKey("required_action")) {
            Object val = json.get("required_action");
            this.required_action = val == null ? null : new com.mobicage.to.payment.PaymentAssetRequiredActionTO((Map<String, Object>) val);
        } else {
            throw new IncompleteMessageException("com.mobicage.to.payment.PaymentProviderAssetTO object is missing field 'required_action'");
        }
        if (json.containsKey("total_balance")) {
            Object val = json.get("total_balance");
            this.total_balance = val == null ? null : new com.mobicage.to.payment.PaymentAssetBalanceTO((Map<String, Object>) val);
        } else {
            throw new IncompleteMessageException("com.mobicage.to.payment.PaymentProviderAssetTO object is missing field 'total_balance'");
        }
        if (json.containsKey("currency")) {
            Object val = json.get("currency");
            this.currency = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.payment.PaymentProviderAssetTO object is missing field 'currency'");
        }
        if (json.containsKey("enabled")) {
            Object val = json.get("enabled");
            this.enabled = ((Boolean) val).booleanValue();
        } else {
            throw new IncompleteMessageException("com.mobicage.to.payment.PaymentProviderAssetTO object is missing field 'enabled'");
        }
        if (json.containsKey("has_balance")) {
            Object val = json.get("has_balance");
            this.has_balance = ((Boolean) val).booleanValue();
        } else {
            throw new IncompleteMessageException("com.mobicage.to.payment.PaymentProviderAssetTO object is missing field 'has_balance'");
        }
        if (json.containsKey("has_transactions")) {
            Object val = json.get("has_transactions");
            this.has_transactions = ((Boolean) val).booleanValue();
        } else {
            throw new IncompleteMessageException("com.mobicage.to.payment.PaymentProviderAssetTO object is missing field 'has_transactions'");
        }
        if (json.containsKey("id")) {
            Object val = json.get("id");
            this.id = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.payment.PaymentProviderAssetTO object is missing field 'id'");
        }
        if (json.containsKey("name")) {
            Object val = json.get("name");
            this.name = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.payment.PaymentProviderAssetTO object is missing field 'name'");
        }
        if (json.containsKey("provider_id")) {
            Object val = json.get("provider_id");
            this.provider_id = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.payment.PaymentProviderAssetTO object is missing field 'provider_id'");
        }
        if (json.containsKey("type")) {
            Object val = json.get("type");
            this.type = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.payment.PaymentProviderAssetTO object is missing field 'type'");
        }
        if (json.containsKey("verified")) {
            Object val = json.get("verified");
            this.verified = ((Boolean) val).booleanValue();
        } else {
            throw new IncompleteMessageException("com.mobicage.to.payment.PaymentProviderAssetTO object is missing field 'verified'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("available_balance", this.available_balance == null ? null : this.available_balance.toJSONMap());
        obj.put("required_action", this.required_action == null ? null : this.required_action.toJSONMap());
        obj.put("total_balance", this.total_balance == null ? null : this.total_balance.toJSONMap());
        obj.put("currency", this.currency);
        obj.put("enabled", this.enabled);
        obj.put("has_balance", this.has_balance);
        obj.put("has_transactions", this.has_transactions);
        obj.put("id", this.id);
        obj.put("name", this.name);
        obj.put("provider_id", this.provider_id);
        obj.put("type", this.type);
        obj.put("verified", this.verified);
        return obj;
    }

}

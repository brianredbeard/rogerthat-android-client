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

public class PaymentProviderTransactionTO implements com.mobicage.rpc.IJSONable {

    public long amount;
    public String currency;
    public String from_asset_id;
    public String id;
    public String memo;
    public String name;
    public long precision;
    public long timestamp;
    public String to_asset_id;
    public String type;

    public PaymentProviderTransactionTO() {
    }

    public PaymentProviderTransactionTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("amount")) {
            Object val = json.get("amount");
            if (val instanceof Integer) {
                this.amount = ((Integer) val).longValue();
            } else {
                this.amount = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.payment.PaymentProviderTransactionTO object is missing field 'amount'");
        }
        if (json.containsKey("currency")) {
            Object val = json.get("currency");
            this.currency = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.payment.PaymentProviderTransactionTO object is missing field 'currency'");
        }
        if (json.containsKey("from_asset_id")) {
            Object val = json.get("from_asset_id");
            this.from_asset_id = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.payment.PaymentProviderTransactionTO object is missing field 'from_asset_id'");
        }
        if (json.containsKey("id")) {
            Object val = json.get("id");
            this.id = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.payment.PaymentProviderTransactionTO object is missing field 'id'");
        }
        if (json.containsKey("memo")) {
            Object val = json.get("memo");
            this.memo = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.payment.PaymentProviderTransactionTO object is missing field 'memo'");
        }
        if (json.containsKey("name")) {
            Object val = json.get("name");
            this.name = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.payment.PaymentProviderTransactionTO object is missing field 'name'");
        }
        if (json.containsKey("precision")) {
            Object val = json.get("precision");
            if (val instanceof Integer) {
                this.precision = ((Integer) val).longValue();
            } else {
                this.precision = ((Long) val).longValue();
            }
        } else {
            this.precision = 2;
        }
        if (json.containsKey("timestamp")) {
            Object val = json.get("timestamp");
            if (val instanceof Integer) {
                this.timestamp = ((Integer) val).longValue();
            } else {
                this.timestamp = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.payment.PaymentProviderTransactionTO object is missing field 'timestamp'");
        }
        if (json.containsKey("to_asset_id")) {
            Object val = json.get("to_asset_id");
            this.to_asset_id = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.payment.PaymentProviderTransactionTO object is missing field 'to_asset_id'");
        }
        if (json.containsKey("type")) {
            Object val = json.get("type");
            this.type = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.payment.PaymentProviderTransactionTO object is missing field 'type'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("amount", this.amount);
        obj.put("currency", this.currency);
        obj.put("from_asset_id", this.from_asset_id);
        obj.put("id", this.id);
        obj.put("memo", this.memo);
        obj.put("name", this.name);
        obj.put("precision", this.precision);
        obj.put("timestamp", this.timestamp);
        obj.put("to_asset_id", this.to_asset_id);
        obj.put("type", this.type);
        return obj;
    }

}

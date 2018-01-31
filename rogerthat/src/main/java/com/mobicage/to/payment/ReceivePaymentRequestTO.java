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

public class ReceivePaymentRequestTO implements com.mobicage.rpc.IJSONable {

    public long amount;
    public String asset_id;
    public String memo;
    public String provider_id;

    public ReceivePaymentRequestTO() {
    }

    public ReceivePaymentRequestTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("amount")) {
            Object val = json.get("amount");
            if (val instanceof Integer) {
                this.amount = ((Integer) val).longValue();
            } else {
                this.amount = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.payment.ReceivePaymentRequestTO object is missing field 'amount'");
        }
        if (json.containsKey("asset_id")) {
            Object val = json.get("asset_id");
            this.asset_id = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.payment.ReceivePaymentRequestTO object is missing field 'asset_id'");
        }
        if (json.containsKey("memo")) {
            Object val = json.get("memo");
            this.memo = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.payment.ReceivePaymentRequestTO object is missing field 'memo'");
        }
        if (json.containsKey("provider_id")) {
            Object val = json.get("provider_id");
            this.provider_id = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.payment.ReceivePaymentRequestTO object is missing field 'provider_id'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("amount", this.amount);
        obj.put("asset_id", this.asset_id);
        obj.put("memo", this.memo);
        obj.put("provider_id", this.provider_id);
        return obj;
    }

}

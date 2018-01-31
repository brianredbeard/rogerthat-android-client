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

public class PaymentAssetBalanceTO implements com.mobicage.rpc.IJSONable {

    public long amount;
    public String description;

    public PaymentAssetBalanceTO() {
    }

    public PaymentAssetBalanceTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("amount")) {
            Object val = json.get("amount");
            if (val instanceof Integer) {
                this.amount = ((Integer) val).longValue();
            } else {
                this.amount = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.payment.PaymentAssetBalanceTO object is missing field 'amount'");
        }
        if (json.containsKey("description")) {
            Object val = json.get("description");
            this.description = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.payment.PaymentAssetBalanceTO object is missing field 'description'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("amount", this.amount);
        obj.put("description", this.description);
        return obj;
    }

}

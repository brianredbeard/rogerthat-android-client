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

public class ConfirmPaymentRequestTO implements com.mobicage.rpc.IJSONable {

    public com.mobicage.to.payment.CryptoTransactionTO crypto_transaction;
    public String transaction_id;

    public ConfirmPaymentRequestTO() {
    }

    @SuppressWarnings("unchecked")
    public ConfirmPaymentRequestTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("crypto_transaction")) {
            Object val = json.get("crypto_transaction");
            this.crypto_transaction = val == null ? null : new com.mobicage.to.payment.CryptoTransactionTO((Map<String, Object>) val);
        } else {
            throw new IncompleteMessageException("com.mobicage.to.payment.ConfirmPaymentRequestTO object is missing field 'crypto_transaction'");
        }
        if (json.containsKey("transaction_id")) {
            Object val = json.get("transaction_id");
            this.transaction_id = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.payment.ConfirmPaymentRequestTO object is missing field 'transaction_id'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("crypto_transaction", this.crypto_transaction == null ? null : this.crypto_transaction.toJSONMap());
        obj.put("transaction_id", this.transaction_id);
        return obj;
    }

}
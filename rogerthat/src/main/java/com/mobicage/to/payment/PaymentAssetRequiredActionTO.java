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

public class PaymentAssetRequiredActionTO implements com.mobicage.rpc.IJSONable {

    public String action;
    public String data;
    public String description;

    public PaymentAssetRequiredActionTO() {
    }

    public PaymentAssetRequiredActionTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("action")) {
            Object val = json.get("action");
            this.action = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.payment.PaymentAssetRequiredActionTO object is missing field 'action'");
        }
        if (json.containsKey("data")) {
            Object val = json.get("data");
            this.data = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.payment.PaymentAssetRequiredActionTO object is missing field 'data'");
        }
        if (json.containsKey("description")) {
            Object val = json.get("description");
            this.description = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.payment.PaymentAssetRequiredActionTO object is missing field 'description'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("action", this.action);
        obj.put("data", this.data);
        obj.put("description", this.description);
        return obj;
    }

}
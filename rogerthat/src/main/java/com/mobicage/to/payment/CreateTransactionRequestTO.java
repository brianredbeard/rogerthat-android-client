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

public class CreateTransactionRequestTO implements com.mobicage.rpc.IJSONable {

    public String params;
    public String provider_id;

    public CreateTransactionRequestTO() {
    }

    public CreateTransactionRequestTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("params")) {
            Object val = json.get("params");
            this.params = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.payment.CreateTransactionRequestTO object is missing field 'params'");
        }
        if (json.containsKey("provider_id")) {
            Object val = json.get("provider_id");
            this.provider_id = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.payment.CreateTransactionRequestTO object is missing field 'provider_id'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("params", this.params);
        obj.put("provider_id", this.provider_id);
        return obj;
    }

}

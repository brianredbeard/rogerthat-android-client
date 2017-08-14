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

public class CreateAssetRequestTO implements com.mobicage.rpc.IJSONable {

    public String address;
    public String currency;
    public String iban;
    public String id;
    public String provider_id;
    public String type;

    public CreateAssetRequestTO() {
    }

    public CreateAssetRequestTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("address")) {
            Object val = json.get("address");
            this.address = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.payment.CreateAssetRequestTO object is missing field 'address'");
        }
        if (json.containsKey("currency")) {
            Object val = json.get("currency");
            this.currency = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.payment.CreateAssetRequestTO object is missing field 'currency'");
        }
        if (json.containsKey("iban")) {
            Object val = json.get("iban");
            this.iban = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.payment.CreateAssetRequestTO object is missing field 'iban'");
        }
        if (json.containsKey("id")) {
            Object val = json.get("id");
            this.id = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.payment.CreateAssetRequestTO object is missing field 'id'");
        }
        if (json.containsKey("provider_id")) {
            Object val = json.get("provider_id");
            this.provider_id = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.payment.CreateAssetRequestTO object is missing field 'provider_id'");
        }
        if (json.containsKey("type")) {
            Object val = json.get("type");
            this.type = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.payment.CreateAssetRequestTO object is missing field 'type'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("address", this.address);
        obj.put("currency", this.currency);
        obj.put("iban", this.iban);
        obj.put("id", this.id);
        obj.put("provider_id", this.provider_id);
        obj.put("type", this.type);
        return obj;
    }

}
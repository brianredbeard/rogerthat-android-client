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

public class UpdatePaymentAssetsRequestTO implements com.mobicage.rpc.IJSONable {

    public com.mobicage.to.payment.PaymentProviderAssetTO[] assets;
    public String[] provider_ids;

    public UpdatePaymentAssetsRequestTO() {
    }

    @SuppressWarnings("unchecked")
    public UpdatePaymentAssetsRequestTO(Map<String, Object> json) throws IncompleteMessageException {
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
            throw new IncompleteMessageException("com.mobicage.to.payment.UpdatePaymentAssetsRequestTO object is missing field 'assets'");
        }
        if (json.containsKey("provider_ids")) {
            org.json.simple.JSONArray val_arr = (org.json.simple.JSONArray) json.get("provider_ids");
            if (val_arr == null) {
                this.provider_ids = null;
            } else {
                this.provider_ids = new String[val_arr.size()];
                for (int i=0; i < val_arr.size(); i++) {
                    this.provider_ids[i] = (String) val_arr.get(i);
                }
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.payment.UpdatePaymentAssetsRequestTO object is missing field 'provider_ids'");
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
        if (this.provider_ids == null) {
            obj.put("provider_ids", null);
        } else {
            org.json.simple.JSONArray arr = new org.json.simple.JSONArray();
            for (int i=0; i < this.provider_ids.length; i++) {
                arr.add(this.provider_ids[i]);
            }
            obj.put("provider_ids", arr);
        }
        return obj;
    }

}

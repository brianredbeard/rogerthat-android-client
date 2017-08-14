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

public class VerifyPaymentAssetResponseTO implements com.mobicage.rpc.IJSONable {

    public com.mobicage.to.payment.ErrorPaymentTO error;
    public boolean success;

    public VerifyPaymentAssetResponseTO() {
    }

    @SuppressWarnings("unchecked")
    public VerifyPaymentAssetResponseTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("error")) {
            Object val = json.get("error");
            this.error = val == null ? null : new com.mobicage.to.payment.ErrorPaymentTO((Map<String, Object>) val);
        } else {
            throw new IncompleteMessageException("com.mobicage.to.payment.VerifyPaymentAssetResponseTO object is missing field 'error'");
        }
        if (json.containsKey("success")) {
            Object val = json.get("success");
            this.success = ((Boolean) val).booleanValue();
        } else {
            throw new IncompleteMessageException("com.mobicage.to.payment.VerifyPaymentAssetResponseTO object is missing field 'success'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("error", this.error == null ? null : this.error.toJSONMap());
        obj.put("success", this.success);
        return obj;
    }

}
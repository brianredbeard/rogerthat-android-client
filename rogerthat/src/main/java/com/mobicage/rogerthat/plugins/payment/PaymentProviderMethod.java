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

package com.mobicage.rogerthat.plugins.payment;

import com.mobicage.rpc.IncompleteMessageException;
import com.mobicage.to.payment.AppPaymentProviderTO;
import com.mobicage.to.payment.PayMethodTO;

import java.util.LinkedHashMap;
import java.util.Map;

public class PaymentProviderMethod implements com.mobicage.rpc.IJSONable {
    public PayMethodTO method;
    public AppPaymentProviderTO provider;

    public PaymentProviderMethod(PayMethodTO method, AppPaymentProviderTO paymentProviderTO) {
        this.method = method;
        this.provider = paymentProviderTO;
    }

    public PaymentProviderMethod(Map<String, Object> json) throws IncompleteMessageException {
        this.method = new PayMethodTO((Map<String, Object>) json.get("method"));
        this.provider = new AppPaymentProviderTO((Map<String, Object>) json.get("provider"));
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<>();
        obj.put("method", this.method.toJSONMap());
        obj.put("provider", this.provider.toJSONMap());
        return obj;
    }
}
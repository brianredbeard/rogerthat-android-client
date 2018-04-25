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

import com.mobicage.rpc.IJSONable;
import com.mobicage.to.payment.AppPaymentProviderTO;
import com.mobicage.to.payment.PayMethodTO;

import java.util.LinkedHashMap;
import java.util.Map;

public class PayWidgetContextData implements IJSONable {

    public boolean test_mode;
    public String message_key;
    public String target;
    public String memo;
    public PayMethodTO method;
    public AppPaymentProviderTO provider;

    public PayWidgetContextData(boolean test_mode, String message_key, String memo, PayMethodTO method, AppPaymentProviderTO provider, String target) {
        this.test_mode = test_mode;
        this.message_key = message_key;
        this.memo = memo;
        this.method = method;
        this.provider = provider;
        this.target = target;
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<>();
        obj.put("test_mode", this.test_mode);
        obj.put("message_key", this.message_key);
        obj.put("target", this.target);
        obj.put("memo", this.memo);
        obj.put("method", this.method.toJSONMap());
        obj.put("provider", this.provider.toJSONMap());
        return obj;
    }
}

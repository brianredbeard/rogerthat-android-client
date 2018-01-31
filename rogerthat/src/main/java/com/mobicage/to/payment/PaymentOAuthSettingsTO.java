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

public class PaymentOAuthSettingsTO implements com.mobicage.rpc.IJSONable {

    public String authorize_path;
    public String base_url;
    public String client_id;
    public String secret;
    public String token_path;

    public PaymentOAuthSettingsTO() {
    }

    public PaymentOAuthSettingsTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("authorize_path")) {
            Object val = json.get("authorize_path");
            this.authorize_path = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.payment.PaymentOAuthSettingsTO object is missing field 'authorize_path'");
        }
        if (json.containsKey("base_url")) {
            Object val = json.get("base_url");
            this.base_url = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.payment.PaymentOAuthSettingsTO object is missing field 'base_url'");
        }
        if (json.containsKey("client_id")) {
            Object val = json.get("client_id");
            this.client_id = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.payment.PaymentOAuthSettingsTO object is missing field 'client_id'");
        }
        if (json.containsKey("secret")) {
            Object val = json.get("secret");
            this.secret = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.payment.PaymentOAuthSettingsTO object is missing field 'secret'");
        }
        if (json.containsKey("token_path")) {
            Object val = json.get("token_path");
            this.token_path = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.payment.PaymentOAuthSettingsTO object is missing field 'token_path'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("authorize_path", this.authorize_path);
        obj.put("base_url", this.base_url);
        obj.put("client_id", this.client_id);
        obj.put("secret", this.secret);
        obj.put("token_path", this.token_path);
        return obj;
    }

}

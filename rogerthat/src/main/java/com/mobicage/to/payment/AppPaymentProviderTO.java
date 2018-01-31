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

public class AppPaymentProviderTO implements com.mobicage.rpc.IJSONable {

    public String[] asset_types;
    public String background_color;
    public String black_white_logo;
    public String button_color;
    public String[] currencies;
    public String description;
    public boolean enabled;
    public String id;
    public String logo_url;
    public String name;
    public String oauth_authorize_url;
    public String text_color;
    public long version;

    public AppPaymentProviderTO() {
    }

    public AppPaymentProviderTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("asset_types")) {
            org.json.simple.JSONArray val_arr = (org.json.simple.JSONArray) json.get("asset_types");
            if (val_arr == null) {
                this.asset_types = null;
            } else {
                this.asset_types = new String[val_arr.size()];
                for (int i=0; i < val_arr.size(); i++) {
                    this.asset_types[i] = (String) val_arr.get(i);
                }
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.payment.AppPaymentProviderTO object is missing field 'asset_types'");
        }
        if (json.containsKey("background_color")) {
            Object val = json.get("background_color");
            this.background_color = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.payment.AppPaymentProviderTO object is missing field 'background_color'");
        }
        if (json.containsKey("black_white_logo")) {
            Object val = json.get("black_white_logo");
            this.black_white_logo = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.payment.AppPaymentProviderTO object is missing field 'black_white_logo'");
        }
        if (json.containsKey("button_color")) {
            Object val = json.get("button_color");
            this.button_color = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.payment.AppPaymentProviderTO object is missing field 'button_color'");
        }
        if (json.containsKey("currencies")) {
            org.json.simple.JSONArray val_arr = (org.json.simple.JSONArray) json.get("currencies");
            if (val_arr == null) {
                this.currencies = null;
            } else {
                this.currencies = new String[val_arr.size()];
                for (int i=0; i < val_arr.size(); i++) {
                    this.currencies[i] = (String) val_arr.get(i);
                }
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.payment.AppPaymentProviderTO object is missing field 'currencies'");
        }
        if (json.containsKey("description")) {
            Object val = json.get("description");
            this.description = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.payment.AppPaymentProviderTO object is missing field 'description'");
        }
        if (json.containsKey("enabled")) {
            Object val = json.get("enabled");
            this.enabled = ((Boolean) val).booleanValue();
        } else {
            throw new IncompleteMessageException("com.mobicage.to.payment.AppPaymentProviderTO object is missing field 'enabled'");
        }
        if (json.containsKey("id")) {
            Object val = json.get("id");
            this.id = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.payment.AppPaymentProviderTO object is missing field 'id'");
        }
        if (json.containsKey("logo_url")) {
            Object val = json.get("logo_url");
            this.logo_url = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.payment.AppPaymentProviderTO object is missing field 'logo_url'");
        }
        if (json.containsKey("name")) {
            Object val = json.get("name");
            this.name = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.payment.AppPaymentProviderTO object is missing field 'name'");
        }
        if (json.containsKey("oauth_authorize_url")) {
            Object val = json.get("oauth_authorize_url");
            this.oauth_authorize_url = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.payment.AppPaymentProviderTO object is missing field 'oauth_authorize_url'");
        }
        if (json.containsKey("text_color")) {
            Object val = json.get("text_color");
            this.text_color = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.payment.AppPaymentProviderTO object is missing field 'text_color'");
        }
        if (json.containsKey("version")) {
            Object val = json.get("version");
            if (val instanceof Integer) {
                this.version = ((Integer) val).longValue();
            } else {
                this.version = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.payment.AppPaymentProviderTO object is missing field 'version'");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        if (this.asset_types == null) {
            obj.put("asset_types", null);
        } else {
            org.json.simple.JSONArray arr = new org.json.simple.JSONArray();
            for (int i=0; i < this.asset_types.length; i++) {
                arr.add(this.asset_types[i]);
            }
            obj.put("asset_types", arr);
        }
        obj.put("background_color", this.background_color);
        obj.put("black_white_logo", this.black_white_logo);
        obj.put("button_color", this.button_color);
        if (this.currencies == null) {
            obj.put("currencies", null);
        } else {
            org.json.simple.JSONArray arr = new org.json.simple.JSONArray();
            for (int i=0; i < this.currencies.length; i++) {
                arr.add(this.currencies[i]);
            }
            obj.put("currencies", arr);
        }
        obj.put("description", this.description);
        obj.put("enabled", this.enabled);
        obj.put("id", this.id);
        obj.put("logo_url", this.logo_url);
        obj.put("name", this.name);
        obj.put("oauth_authorize_url", this.oauth_authorize_url);
        obj.put("text_color", this.text_color);
        obj.put("version", this.version);
        return obj;
    }

}

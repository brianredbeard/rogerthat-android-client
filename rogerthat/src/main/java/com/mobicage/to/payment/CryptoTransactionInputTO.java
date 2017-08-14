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

public class CryptoTransactionInputTO implements com.mobicage.rpc.IJSONable {

    public String parent_id;
    public long timelock;

    public CryptoTransactionInputTO() {
    }

    public CryptoTransactionInputTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("parent_id")) {
            Object val = json.get("parent_id");
            this.parent_id = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.payment.CryptoTransactionInputTO object is missing field 'parent_id'");
        }
        if (json.containsKey("timelock")) {
            Object val = json.get("timelock");
            if (val instanceof Integer) {
                this.timelock = ((Integer) val).longValue();
            } else {
                this.timelock = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.payment.CryptoTransactionInputTO object is missing field 'timelock'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("parent_id", this.parent_id);
        obj.put("timelock", this.timelock);
        return obj;
    }

}
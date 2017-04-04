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

package com.mobicage.to.messaging.forms;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class NewPhotoUploadFormRequestTO implements com.mobicage.rpc.IJSONable {

    public com.mobicage.to.messaging.forms.PhotoUploadFormMessageTO form_message;

    public NewPhotoUploadFormRequestTO() {
    }

    @SuppressWarnings("unchecked")
    public NewPhotoUploadFormRequestTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("form_message")) {
            Object val = json.get("form_message");
            this.form_message = val == null ? null : new com.mobicage.to.messaging.forms.PhotoUploadFormMessageTO((Map<String, Object>) val);
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.forms.NewPhotoUploadFormRequestTO object is missing field 'form_message'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("form_message", this.form_message == null ? null : this.form_message.toJSONMap());
        return obj;
    }

}

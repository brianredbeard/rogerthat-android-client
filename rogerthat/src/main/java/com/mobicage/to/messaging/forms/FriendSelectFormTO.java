/*
 * Copyright 2016 Mobicage NV
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
 * @@license_version:1.1@@
 */

package com.mobicage.to.messaging.forms;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class FriendSelectFormTO implements com.mobicage.rpc.IJSONable {

    public com.mobicage.to.messaging.forms.FriendSelectTO widget;
    public String javascript_validation;
    public String negative_button;
    public long negative_button_ui_flags;
    public String negative_confirmation;
    public String positive_button;
    public long positive_button_ui_flags;
    public String positive_confirmation;
    public String type;

    public FriendSelectFormTO() {
    }

    @SuppressWarnings("unchecked")
    public FriendSelectFormTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("widget")) {
            Object val = json.get("widget");
            this.widget = val == null ? null : new com.mobicage.to.messaging.forms.FriendSelectTO((Map<String, Object>) val);
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.forms.FriendSelectFormTO object is missing field 'widget'");
        }
        if (json.containsKey("javascript_validation")) {
            Object val = json.get("javascript_validation");
            this.javascript_validation = (String) val;
        } else {
            this.javascript_validation = null;
        }
        if (json.containsKey("negative_button")) {
            Object val = json.get("negative_button");
            this.negative_button = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.forms.FriendSelectFormTO object is missing field 'negative_button'");
        }
        if (json.containsKey("negative_button_ui_flags")) {
            Object val = json.get("negative_button_ui_flags");
            if (val instanceof Integer) {
                this.negative_button_ui_flags = ((Integer) val).longValue();
            } else {
                this.negative_button_ui_flags = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.forms.FriendSelectFormTO object is missing field 'negative_button_ui_flags'");
        }
        if (json.containsKey("negative_confirmation")) {
            Object val = json.get("negative_confirmation");
            this.negative_confirmation = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.forms.FriendSelectFormTO object is missing field 'negative_confirmation'");
        }
        if (json.containsKey("positive_button")) {
            Object val = json.get("positive_button");
            this.positive_button = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.forms.FriendSelectFormTO object is missing field 'positive_button'");
        }
        if (json.containsKey("positive_button_ui_flags")) {
            Object val = json.get("positive_button_ui_flags");
            if (val instanceof Integer) {
                this.positive_button_ui_flags = ((Integer) val).longValue();
            } else {
                this.positive_button_ui_flags = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.forms.FriendSelectFormTO object is missing field 'positive_button_ui_flags'");
        }
        if (json.containsKey("positive_confirmation")) {
            Object val = json.get("positive_confirmation");
            this.positive_confirmation = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.forms.FriendSelectFormTO object is missing field 'positive_confirmation'");
        }
        if (json.containsKey("type")) {
            Object val = json.get("type");
            this.type = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.forms.FriendSelectFormTO object is missing field 'type'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("widget", this.widget == null ? null : this.widget.toJSONMap());
        obj.put("javascript_validation", this.javascript_validation);
        obj.put("negative_button", this.negative_button);
        obj.put("negative_button_ui_flags", this.negative_button_ui_flags);
        obj.put("negative_confirmation", this.negative_confirmation);
        obj.put("positive_button", this.positive_button);
        obj.put("positive_button_ui_flags", this.positive_button_ui_flags);
        obj.put("positive_confirmation", this.positive_confirmation);
        obj.put("type", this.type);
        return obj;
    }

}

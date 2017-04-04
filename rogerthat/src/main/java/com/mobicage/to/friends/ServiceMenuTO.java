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

package com.mobicage.to.friends;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class ServiceMenuTO implements com.mobicage.rpc.IJSONable {

    public com.mobicage.to.friends.ServiceMenuItemTO[] items;
    public String aboutLabel;
    public String branding;
    public String callConfirmation;
    public String callLabel;
    public String messagesLabel;
    public String phoneNumber;
    public boolean share;
    public String shareCaption;
    public String shareDescription;
    public String shareImageUrl;
    public String shareLabel;
    public String shareLinkUrl;
    public String[] staticFlowBrandings;

    public ServiceMenuTO() {
    }

    @SuppressWarnings("unchecked")
    public ServiceMenuTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("items")) {
            org.json.simple.JSONArray val_arr = (org.json.simple.JSONArray) json.get("items");
            if (val_arr == null) {
                this.items = null;
            } else {
                this.items = new com.mobicage.to.friends.ServiceMenuItemTO[val_arr.size()];
                for (int i=0; i < val_arr.size(); i++) {
                    Object item = val_arr.get(i);
                    if (item != null) {
                        this.items[i] = new com.mobicage.to.friends.ServiceMenuItemTO((Map<String, Object>) item);
                    }
                }
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.friends.ServiceMenuTO object is missing field 'items'");
        }
        if (json.containsKey("aboutLabel")) {
            Object val = json.get("aboutLabel");
            this.aboutLabel = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.friends.ServiceMenuTO object is missing field 'aboutLabel'");
        }
        if (json.containsKey("branding")) {
            Object val = json.get("branding");
            this.branding = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.friends.ServiceMenuTO object is missing field 'branding'");
        }
        if (json.containsKey("callConfirmation")) {
            Object val = json.get("callConfirmation");
            this.callConfirmation = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.friends.ServiceMenuTO object is missing field 'callConfirmation'");
        }
        if (json.containsKey("callLabel")) {
            Object val = json.get("callLabel");
            this.callLabel = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.friends.ServiceMenuTO object is missing field 'callLabel'");
        }
        if (json.containsKey("messagesLabel")) {
            Object val = json.get("messagesLabel");
            this.messagesLabel = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.friends.ServiceMenuTO object is missing field 'messagesLabel'");
        }
        if (json.containsKey("phoneNumber")) {
            Object val = json.get("phoneNumber");
            this.phoneNumber = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.friends.ServiceMenuTO object is missing field 'phoneNumber'");
        }
        if (json.containsKey("share")) {
            Object val = json.get("share");
            this.share = ((Boolean) val).booleanValue();
        } else {
            throw new IncompleteMessageException("com.mobicage.to.friends.ServiceMenuTO object is missing field 'share'");
        }
        if (json.containsKey("shareCaption")) {
            Object val = json.get("shareCaption");
            this.shareCaption = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.friends.ServiceMenuTO object is missing field 'shareCaption'");
        }
        if (json.containsKey("shareDescription")) {
            Object val = json.get("shareDescription");
            this.shareDescription = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.friends.ServiceMenuTO object is missing field 'shareDescription'");
        }
        if (json.containsKey("shareImageUrl")) {
            Object val = json.get("shareImageUrl");
            this.shareImageUrl = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.friends.ServiceMenuTO object is missing field 'shareImageUrl'");
        }
        if (json.containsKey("shareLabel")) {
            Object val = json.get("shareLabel");
            this.shareLabel = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.friends.ServiceMenuTO object is missing field 'shareLabel'");
        }
        if (json.containsKey("shareLinkUrl")) {
            Object val = json.get("shareLinkUrl");
            this.shareLinkUrl = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.friends.ServiceMenuTO object is missing field 'shareLinkUrl'");
        }
        if (json.containsKey("staticFlowBrandings")) {
            org.json.simple.JSONArray val_arr = (org.json.simple.JSONArray) json.get("staticFlowBrandings");
            if (val_arr == null) {
                this.staticFlowBrandings = null;
            } else {
                this.staticFlowBrandings = new String[val_arr.size()];
                for (int i=0; i < val_arr.size(); i++) {
                    this.staticFlowBrandings[i] = (String) val_arr.get(i);
                }
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.friends.ServiceMenuTO object is missing field 'staticFlowBrandings'");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        if (this.items == null) {
            obj.put("items", null);
        } else {
            org.json.simple.JSONArray arr = new org.json.simple.JSONArray();
            for (int i=0; i < this.items.length; i++) {
                arr.add(this.items[i].toJSONMap());
            }
            obj.put("items", arr);
        }
        obj.put("aboutLabel", this.aboutLabel);
        obj.put("branding", this.branding);
        obj.put("callConfirmation", this.callConfirmation);
        obj.put("callLabel", this.callLabel);
        obj.put("messagesLabel", this.messagesLabel);
        obj.put("phoneNumber", this.phoneNumber);
        obj.put("share", this.share);
        obj.put("shareCaption", this.shareCaption);
        obj.put("shareDescription", this.shareDescription);
        obj.put("shareImageUrl", this.shareImageUrl);
        obj.put("shareLabel", this.shareLabel);
        obj.put("shareLinkUrl", this.shareLinkUrl);
        if (this.staticFlowBrandings == null) {
            obj.put("staticFlowBrandings", null);
        } else {
            org.json.simple.JSONArray arr = new org.json.simple.JSONArray();
            for (int i=0; i < this.staticFlowBrandings.length; i++) {
                arr.add(this.staticFlowBrandings[i]);
            }
            obj.put("staticFlowBrandings", arr);
        }
        return obj;
    }

}

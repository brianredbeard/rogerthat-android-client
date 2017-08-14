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

public class FriendTO implements com.mobicage.rpc.IJSONable {

    public com.mobicage.to.friends.ServiceMenuTO actionMenu;
    public String appData;
    public String avatarHash;
    public long avatarId;
    public String broadcastFlowHash;
    public long callbacks;
    public String category_id;
    public String contentBrandingHash;
    public String description;
    public String descriptionBranding;
    public String email;
    public long existence;
    public long flags;
    public long generation;
    public boolean hasUserData;
    public String name;
    public long organizationType;
    public String pokeDescription;
    public String profileData;
    public String qualifiedIdentifier;
    public boolean shareLocation;
    public boolean sharesContacts;
    public boolean sharesLocation;
    public long type;
    public String userData;
    public long[] versions;

    public FriendTO() {
    }

    @SuppressWarnings("unchecked")
    public FriendTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("actionMenu")) {
            Object val = json.get("actionMenu");
            this.actionMenu = val == null ? null : new com.mobicage.to.friends.ServiceMenuTO((Map<String, Object>) val);
        } else {
            throw new IncompleteMessageException("com.mobicage.to.friends.FriendTO object is missing field 'actionMenu'");
        }
        if (json.containsKey("appData")) {
            Object val = json.get("appData");
            this.appData = (String) val;
        } else {
            this.appData = null;
        }
        if (json.containsKey("avatarHash")) {
            Object val = json.get("avatarHash");
            this.avatarHash = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.friends.FriendTO object is missing field 'avatarHash'");
        }
        if (json.containsKey("avatarId")) {
            Object val = json.get("avatarId");
            if (val instanceof Integer) {
                this.avatarId = ((Integer) val).longValue();
            } else {
                this.avatarId = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.friends.FriendTO object is missing field 'avatarId'");
        }
        if (json.containsKey("broadcastFlowHash")) {
            Object val = json.get("broadcastFlowHash");
            this.broadcastFlowHash = (String) val;
        } else {
            this.broadcastFlowHash = null;
        }
        if (json.containsKey("callbacks")) {
            Object val = json.get("callbacks");
            if (val instanceof Integer) {
                this.callbacks = ((Integer) val).longValue();
            } else {
                this.callbacks = ((Long) val).longValue();
            }
        } else {
            this.callbacks = 0;
        }
        if (json.containsKey("category_id")) {
            Object val = json.get("category_id");
            this.category_id = (String) val;
        } else {
            this.category_id = null;
        }
        if (json.containsKey("contentBrandingHash")) {
            Object val = json.get("contentBrandingHash");
            this.contentBrandingHash = (String) val;
        } else {
            this.contentBrandingHash = null;
        }
        if (json.containsKey("description")) {
            Object val = json.get("description");
            this.description = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.friends.FriendTO object is missing field 'description'");
        }
        if (json.containsKey("descriptionBranding")) {
            Object val = json.get("descriptionBranding");
            this.descriptionBranding = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.friends.FriendTO object is missing field 'descriptionBranding'");
        }
        if (json.containsKey("email")) {
            Object val = json.get("email");
            this.email = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.friends.FriendTO object is missing field 'email'");
        }
        if (json.containsKey("existence")) {
            Object val = json.get("existence");
            if (val instanceof Integer) {
                this.existence = ((Integer) val).longValue();
            } else {
                this.existence = ((Long) val).longValue();
            }
        } else {
            this.existence = 0;
        }
        if (json.containsKey("flags")) {
            Object val = json.get("flags");
            if (val instanceof Integer) {
                this.flags = ((Integer) val).longValue();
            } else {
                this.flags = ((Long) val).longValue();
            }
        } else {
            this.flags = 0;
        }
        if (json.containsKey("generation")) {
            Object val = json.get("generation");
            if (val instanceof Integer) {
                this.generation = ((Integer) val).longValue();
            } else {
                this.generation = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.friends.FriendTO object is missing field 'generation'");
        }
        if (json.containsKey("hasUserData")) {
            Object val = json.get("hasUserData");
            this.hasUserData = ((Boolean) val).booleanValue();
        } else {
            this.hasUserData = false;
        }
        if (json.containsKey("name")) {
            Object val = json.get("name");
            this.name = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.friends.FriendTO object is missing field 'name'");
        }
        if (json.containsKey("organizationType")) {
            Object val = json.get("organizationType");
            if (val instanceof Integer) {
                this.organizationType = ((Integer) val).longValue();
            } else {
                this.organizationType = ((Long) val).longValue();
            }
        } else {
            this.organizationType = 0;
        }
        if (json.containsKey("pokeDescription")) {
            Object val = json.get("pokeDescription");
            this.pokeDescription = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.friends.FriendTO object is missing field 'pokeDescription'");
        }
        if (json.containsKey("profileData")) {
            Object val = json.get("profileData");
            this.profileData = (String) val;
        } else {
            this.profileData = null;
        }
        if (json.containsKey("qualifiedIdentifier")) {
            Object val = json.get("qualifiedIdentifier");
            this.qualifiedIdentifier = (String) val;
        } else {
            this.qualifiedIdentifier = null;
        }
        if (json.containsKey("shareLocation")) {
            Object val = json.get("shareLocation");
            this.shareLocation = ((Boolean) val).booleanValue();
        } else {
            throw new IncompleteMessageException("com.mobicage.to.friends.FriendTO object is missing field 'shareLocation'");
        }
        if (json.containsKey("sharesContacts")) {
            Object val = json.get("sharesContacts");
            this.sharesContacts = ((Boolean) val).booleanValue();
        } else {
            throw new IncompleteMessageException("com.mobicage.to.friends.FriendTO object is missing field 'sharesContacts'");
        }
        if (json.containsKey("sharesLocation")) {
            Object val = json.get("sharesLocation");
            this.sharesLocation = ((Boolean) val).booleanValue();
        } else {
            throw new IncompleteMessageException("com.mobicage.to.friends.FriendTO object is missing field 'sharesLocation'");
        }
        if (json.containsKey("type")) {
            Object val = json.get("type");
            if (val instanceof Integer) {
                this.type = ((Integer) val).longValue();
            } else {
                this.type = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.friends.FriendTO object is missing field 'type'");
        }
        if (json.containsKey("userData")) {
            Object val = json.get("userData");
            this.userData = (String) val;
        } else {
            this.userData = null;
        }
        if (json.containsKey("versions")) {
            org.json.simple.JSONArray val_arr = (org.json.simple.JSONArray) json.get("versions");
            if (val_arr == null) {
                this.versions = null;
            } else {
                this.versions = new long[val_arr.size()];
                for (int i=0; i < val_arr.size(); i++) {
                    this.versions[i] = ((Long) val_arr.get(i)).longValue();
                }
            }
        } else {
            this.versions = new long[0];
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("actionMenu", this.actionMenu == null ? null : this.actionMenu.toJSONMap());
        obj.put("appData", this.appData);
        obj.put("avatarHash", this.avatarHash);
        obj.put("avatarId", this.avatarId);
        obj.put("broadcastFlowHash", this.broadcastFlowHash);
        obj.put("callbacks", this.callbacks);
        obj.put("category_id", this.category_id);
        obj.put("contentBrandingHash", this.contentBrandingHash);
        obj.put("description", this.description);
        obj.put("descriptionBranding", this.descriptionBranding);
        obj.put("email", this.email);
        obj.put("existence", this.existence);
        obj.put("flags", this.flags);
        obj.put("generation", this.generation);
        obj.put("hasUserData", this.hasUserData);
        obj.put("name", this.name);
        obj.put("organizationType", this.organizationType);
        obj.put("pokeDescription", this.pokeDescription);
        obj.put("profileData", this.profileData);
        obj.put("qualifiedIdentifier", this.qualifiedIdentifier);
        obj.put("shareLocation", this.shareLocation);
        obj.put("sharesContacts", this.sharesContacts);
        obj.put("sharesLocation", this.sharesLocation);
        obj.put("type", this.type);
        obj.put("userData", this.userData);
        if (this.versions == null) {
            obj.put("versions", null);
        } else {
            org.json.simple.JSONArray arr = new org.json.simple.JSONArray();
            for (int i=0; i < this.versions.length; i++) {
                arr.add(this.versions[i]);
            }
            obj.put("versions", arr);
        }
        return obj;
    }

}
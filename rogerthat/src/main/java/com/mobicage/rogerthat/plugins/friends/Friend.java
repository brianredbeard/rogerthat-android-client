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
package com.mobicage.rogerthat.plugins.friends;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.mobicage.rogerthat.util.TextUtils;
import com.mobicage.rogerthat.util.ui.ImageHelper;
import com.mobicage.to.friends.FriendTO;

import org.json.simple.JSONValue;

import java.util.Map;

public class Friend extends FriendTO {

    public final static int ACTIVE = 0;
    public final static int DELETION_PENDING = 1;
    public final static int DELETED = 2;
    public final static int NOT_FOUND = 3;
    public final static int INVITE_PENDING = 4;

    public byte[] avatar;
    public int existenceStatus;
    public FriendCategory category = null;
    public String actions = null;

    private Map<String, String> mCachedProfileDataDict = null;

    public String getDisplayName() {
        return TextUtils.isEmptyOrWhitespace(name) ? getDisplayEmail() : name;
    }

    public String getDisplayEmail() {
        return TextUtils.isEmptyOrWhitespace(qualifiedIdentifier) ? email : qualifiedIdentifier;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null || !(other instanceof Friend))
            return false;
        return email.equals(((Friend) other).email);
    }

    @Override
    public int hashCode() {
        return email == null ? 0 : email.hashCode();
    }

    public Bitmap getAvatarBitmap() {
        return ImageHelper.getRoundedCornerAvatar(BitmapFactory.decodeByteArray(avatar, 0, avatar.length));
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> getProfileDataDict() {
        if (mCachedProfileDataDict == null) {
            mCachedProfileDataDict = profileData == null ? null : (Map<String, String>) JSONValue.parse(profileData);
        }
        return mCachedProfileDataDict;
    }

}

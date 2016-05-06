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
package com.mobicage.rogerthat;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.json.simple.JSONValue;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.plugins.friends.EmailHashCalculator;
import com.mobicage.rogerthat.plugins.friends.FriendsPlugin;
import com.mobicage.rogerthat.util.TextUtils;
import com.mobicage.rogerthat.util.ui.ImageHelper;

public class MyIdentity {
    private final String mEmail;
    private final byte[] mEmailHash;
    private final String mName;
    private final byte[] mAvatarBytes;
    private final Bitmap mAvatarBitmap;
    private final Bitmap mQRBitmap;
    private final String mShortLink;
    private final String mQualifiedIdentifier;
    private final Long mAvatarId;
    private final Long mBirthdate;
    private final Long mGender;
    private final String mProfileData;
    private Map<String, String> mCachedProfileDataDict = null;
    public static final long GENDER_UNDEFINED = 0;
    public static final long GENDER_MALE = 1;
    public static final long GENDER_FEMALE = 2;
    public static final long GENDER_CUSTOM_FACEBOOK = 3;

    public MyIdentity(String email, String name, byte[] avatarBytes, byte[] qrBytes, String shortLink,
        String qualifiedIdentifier, Long avatarId, Long birthdate, Long gender, String profileData, Context context) {
        mEmail = email;
        mEmailHash = EmailHashCalculator.calculateEmailHash(email, FriendsPlugin.FRIEND_TYPE_USER);
        mName = name;
        mAvatarBytes = avatarBytes;
        if (avatarBytes == null) {
            mAvatarBitmap = ImageHelper.getRoundedCornerAvatar(BitmapFactory.decodeResource(context.getResources(),
                R.drawable.unknown_avatar));
        } else {
            mAvatarBitmap = ImageHelper.getRoundedCornerAvatar(BitmapFactory.decodeByteArray(mAvatarBytes, 0,
                mAvatarBytes.length));
        }
        if (qrBytes == null)
            mQRBitmap = null;
        else
            mQRBitmap = BitmapFactory.decodeByteArray(qrBytes, 0, qrBytes.length);
        mShortLink = shortLink;
        mQualifiedIdentifier = qualifiedIdentifier;
        mAvatarId = avatarId;
        mBirthdate = birthdate;
        mGender = gender;
        mProfileData = profileData;
    }

    public String getEmail() {
        return mEmail;
    }

    public byte[] getEmailHash() {
        return mEmailHash;
    }

    public String getName() {
        return mName;
    }

    public byte[] getAvatar() {
        return mAvatarBytes;
    }

    public Bitmap getAvatarBitmap() {
        return mAvatarBitmap;
    }

    public Bitmap getQRBitmap() {
        return mQRBitmap;
    }

    public String getShortLink() {
        return mShortLink;
    }

    public Long getAvatarId() {
        return mAvatarId;
    }

    public String getQualifiedIdentifier() {
        return mQualifiedIdentifier;
    }

    public String getDisplayEmail() {
        return TextUtils.isEmptyOrWhitespace(mQualifiedIdentifier) ? getEmail() : mQualifiedIdentifier;
    }

    public String getDisplayName() {
        return TextUtils.isEmptyOrWhitespace(mName) ? getDisplayEmail() : mName;
    }

    public Long getBirthdate() {
        return mBirthdate;
    }

    public String getDisplayBirthdate() {
        if (mBirthdate == null) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(mBirthdate * 1000);
        return getDisplayBirthdate(calendar);
    }

    public String getDisplayBirthdate(Calendar birthdate) {
        Date date = birthdate.getTime();
        return DateFormat.getDateInstance().format(date);
    }

    public long getGender() {
        if (mGender == null) {
            return GENDER_UNDEFINED;
        } else if (mGender == GENDER_FEMALE) {
            return GENDER_FEMALE;
        } else {
            return GENDER_MALE;
        }
    }

    public boolean hasBirthdate() {
        return mBirthdate != null;
    }

    public boolean hasGender() {
        return mGender != null;
    }

    public String getProfileData() {
        return mProfileData;
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> getProfileDataDict() {
        if (mCachedProfileDataDict == null) {
            mCachedProfileDataDict = mProfileData == null ? null : (Map<String, String>) JSONValue.parse(mProfileData);
        }
        return mCachedProfileDataDict;
    }
}
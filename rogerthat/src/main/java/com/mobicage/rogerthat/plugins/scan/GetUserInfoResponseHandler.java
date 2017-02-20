/*
 * Copyright 2017 Mobicage NV
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
 * @@license_version:1.2@@
 */

package com.mobicage.rogerthat.plugins.scan;

import android.content.Intent;

import com.mobicage.rogerthat.plugins.friends.Friend;
import com.mobicage.rogerthat.plugins.friends.FriendStore;
import com.mobicage.rogerthat.plugins.friends.FriendsPlugin;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.pickle.PickleException;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.ui.UIUtils;
import com.mobicage.rpc.IResponse;
import com.mobicage.rpc.ResponseHandler;
import com.mobicage.rpc.config.AppConstants;
import com.mobicage.to.friends.FriendTO;
import com.mobicage.to.friends.GetUserInfoResponseTO;
import com.mobicage.to.friends.UserScannedRequestTO;
import com.mobicage.to.friends.UserScannedResponseTO;

import org.jivesoftware.smack.util.Base64;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GetUserInfoResponseHandler extends ResponseHandler<GetUserInfoResponseTO> {

    protected final int CLASS_VERSION = 3;

    private String mEmailHash;
    private boolean mStoreInDB = false;
    private Map<String, String> mIntentStringExtras = null;
    private boolean mSendUserScanned = false;
    private String mServiceEmail;

    public void setCode(final String emailHash) {
        mEmailHash = emailHash;
    }

    public void setStoreInDB(final boolean storeInDB) {
        mStoreInDB = storeInDB;
    }

    public void putStringExtra(final String key, final String value) {
        if (mIntentStringExtras == null) {
            mIntentStringExtras = new HashMap<String, String>();
        }
        mIntentStringExtras.put(key, value);
    }

    public void setSendUserScanned(final boolean sendUserScanned) {
        mSendUserScanned = sendUserScanned;
    }

    public void setServiceEmail(final String serviceEmail) {
        mServiceEmail = serviceEmail;
    }

    @Override
    public int getPickleClassVersion() {
        T.dontCare();
        return CLASS_VERSION;
    }

    @Override
    public void writePickle(DataOutput out) throws IOException {
        T.dontCare();
        super.writePickle(out);
        out.writeUTF(mEmailHash);
        out.writeBoolean(mStoreInDB);
        out.writeBoolean(mIntentStringExtras != null);
        if (mIntentStringExtras != null) {
            out.writeUTF(JSONValue.toJSONString(mIntentStringExtras));
        }
        out.writeBoolean(mSendUserScanned);
        if (mSendUserScanned) {
            out.writeUTF(mServiceEmail);
        }
    }

    @Override
    public void readFromPickle(int version, DataInput in) throws IOException, PickleException {
        T.dontCare();
        super.readFromPickle(version, in);
        mEmailHash = in.readUTF();
        if (version >= 2) {
            mStoreInDB = in.readBoolean();
        }
        if (version >= 3) {
            if (in.readBoolean()) {
                mIntentStringExtras = new HashMap<String, String>();
                final JSONObject object = (JSONObject) JSONValue.parse(in.readUTF());
                for (Object key : object.keySet()) {
                    mIntentStringExtras.put((String) key, (String) object.get(key));
                }
            }
            mSendUserScanned = in.readBoolean();
            if (mSendUserScanned) {
                mServiceEmail = in.readUTF();
            }
        }
    }

    @Override
    public void handle(final IResponse<GetUserInfoResponseTO> response) {
        T.BIZZ();
        try {

            final GetUserInfoResponseTO theResponse = response.getResponse();

            final Intent intent = new Intent(FriendsPlugin.FRIEND_INFO_RECEIVED_INTENT);
            intent.putExtra(ProcessScanActivity.EMAILHASH, mEmailHash);
            intent.putExtra(ProcessScanActivity.AVATAR, theResponse.avatar);
            intent.putExtra(ProcessScanActivity.NAME, theResponse.name);
            intent.putExtra(ProcessScanActivity.APP_ID, theResponse.app_id);
            intent.putExtra(ProcessScanActivity.EMAIL, theResponse.email);
            intent.putExtra(ProcessScanActivity.TYPE, theResponse.type);
            intent.putExtra(ProcessScanActivity.DESCRIPTION, theResponse.description);
            intent.putExtra(ProcessScanActivity.DESCRIPTION_BRANDING, theResponse.descriptionBranding);
            intent.putExtra(ProcessScanActivity.QUALIFIED_IDENTIFIER, theResponse.qualifiedIdentifier);

            if (mIntentStringExtras != null) {
                for (Map.Entry<String, String> entry : mIntentStringExtras.entrySet()) {
                    intent.putExtra(entry.getKey(), entry.getValue());
                }
            }

            if (theResponse.error == null) {
                intent.putExtra(ProcessScanActivity.SUCCESS, true);
                // updateFriendInfo
                if (mStoreInDB && mEmailHash.contains("@") && AppConstants.APP_ID.equals(theResponse.app_id)) {
                    final FriendStore friendStore = mMainService.getPlugin(FriendsPlugin.class).getStore();
                    if (friendStore.getExistence(mEmailHash) == Friend.NOT_FOUND) {
                        FriendTO f = new FriendTO();
                        f.organizationType = 0;
                        f.callbacks = 0;
                        f.flags = 0;
                        f.versions = new long[0];
                        f.existence = Friend.DELETED;
                        f.avatarId = theResponse.avatar_id;
                        f.email = mEmailHash;
                        f.name = theResponse.name;
                        f.type = theResponse.type;
                        f.description = theResponse.description;
                        f.descriptionBranding = theResponse.descriptionBranding;
                        f.qualifiedIdentifier = theResponse.qualifiedIdentifier;
                        f.profileData = theResponse.profileData;
                        friendStore.storeFriend(f, Base64.decode(theResponse.avatar), true);
                    } else {
                        friendStore.updateFriendInfo(mEmailHash, theResponse);
                    }
                }
                if (mSendUserScanned) {
                    try {
                        UserScannedRequestTO us = new UserScannedRequestTO();
                        us.app_id = theResponse.app_id;
                        us.email = theResponse.email;
                        us.service_email = mServiceEmail;
                        com.mobicage.api.friends.Rpc.userScanned(new ResponseHandler<UserScannedResponseTO>(), us);
                    } catch (Exception e) {
                        L.d("userScanned api call failed", e);
                    }
                }
            } else {
                intent.putExtra(ProcessScanActivity.SUCCESS, false);
                intent.putExtra(UIUtils.ERROR_MESSAGE, theResponse.error.message);
                intent.putExtra(UIUtils.ERROR_TITLE, theResponse.error.title);
                intent.putExtra(UIUtils.ERROR_CAPTION, theResponse.error.caption);
                intent.putExtra(UIUtils.ERROR_ACTION, theResponse.error.action);
            }

            mMainService.sendBroadcast(intent);

        } catch (Exception e) {
            L.d("GetUserInfo api call failed", e);
            final Intent intent = new Intent(FriendsPlugin.FRIEND_INFO_RECEIVED_INTENT);
            intent.putExtra(ProcessScanActivity.EMAILHASH, mEmailHash);
            intent.putExtra(ProcessScanActivity.SUCCESS, false);
            mMainService.sendBroadcast(intent);
        }
    }
}

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
package com.mobicage.rogerthat.plugins.friends;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.jivesoftware.smack.util.Base64;

import android.content.Intent;

import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.pickle.PickleException;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rpc.IResponse;
import com.mobicage.rpc.ResponseHandler;
import com.mobicage.to.friends.GetGroupAvatarResponseTO;

public class GetGroupAvatarResponseHandler extends ResponseHandler<GetGroupAvatarResponseTO> {

    private String mAvatarHash;

    public GetGroupAvatarResponseHandler() {
    }

    public GetGroupAvatarResponseHandler(String avatarHash) {
        mAvatarHash = avatarHash;
    }

    @Override
    public void writePickle(DataOutput out) throws IOException {
        T.dontCare();
        super.writePickle(out);
        out.writeUTF(mAvatarHash);
    }

    @Override
    public void readFromPickle(int version, DataInput in) throws IOException, PickleException {
        T.dontCare();
        super.readFromPickle(version, in);
        mAvatarHash = in.readUTF();
    }

    @Override
    public void handle(final IResponse<GetGroupAvatarResponseTO> response) {
        T.BIZZ();
        final GetGroupAvatarResponseTO resp;
        try {
            resp = response.getResponse();
        } catch (Exception e) {
            L.d("Get group avatar api call failed", e);
            return;
        }
        FriendsPlugin friendsPlugin = mMainService.getPlugin(FriendsPlugin.class);
        friendsPlugin.getStore().insertGroupAvatar(mAvatarHash, Base64.decode(resp.avatar));

        Intent intent = new Intent(FriendsPlugin.GROUPS_UPDATED);
        mMainService.sendBroadcast(intent);
    }
}

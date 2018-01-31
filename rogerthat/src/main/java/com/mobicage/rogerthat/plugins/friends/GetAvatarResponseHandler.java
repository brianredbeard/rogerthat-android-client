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

package com.mobicage.rogerthat.plugins.friends;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.jivesoftware.smack.util.Base64;

import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.pickle.PickleException;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rpc.IResponse;
import com.mobicage.rpc.ResponseHandler;

public class GetAvatarResponseHandler extends ResponseHandler<com.mobicage.to.friends.GetAvatarResponseTO> {

    private String mFriendEmail;

    public void setFriendEmail(String friendEmail) {
        mFriendEmail = friendEmail;
    }

    @Override
    public void writePickle(DataOutput out) throws IOException {
        T.dontCare();
        super.writePickle(out);
        out.writeUTF(mFriendEmail);
    }

    @Override
    public void readFromPickle(int version, DataInput in) throws IOException, PickleException {
        T.dontCare();
        super.readFromPickle(version, in);
        mFriendEmail = in.readUTF();
    }

    @Override
    public void handle(final IResponse<com.mobicage.to.friends.GetAvatarResponseTO> response) {
        T.BIZZ();
        try {
            final byte[] avatarBytes = Base64.decode(response.getResponse().avatar);
            if (avatarBytes != null) {
                final FriendsPlugin plugin = mMainService.getPlugin(FriendsPlugin.class);
                plugin.updateFriendAvatar(mFriendEmail, avatarBytes);
            }
        } catch (Exception e) {
            L.bug(e);
        }
    }
}

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
package com.mobicage.rogerthat.plugins.messaging;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.pickle.PickleException;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rpc.IResponse;
import com.mobicage.rpc.ResponseHandler;
import com.mobicage.to.messaging.GetConversationAvatarResponseTO;

public class GetConversationAvatarResponseHandler extends ResponseHandler<GetConversationAvatarResponseTO> {

    private volatile String mAvatarHash;

    public void setAvatarHash(String avatarHash) {
        this.mAvatarHash = avatarHash;
    }

    @Override
    public void handle(final IResponse<GetConversationAvatarResponseTO> response) {
        T.BIZZ();
        try {
            GetConversationAvatarResponseTO result = response.getResponse();
            final MessagingPlugin plugin = mMainService.getPlugin(MessagingPlugin.class);
            plugin.getStore().storeThreadAvatar(mAvatarHash, result.avatar);
        } catch (Exception e) {
            L.d("Get conversation avatar failed", e);
        }
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

}

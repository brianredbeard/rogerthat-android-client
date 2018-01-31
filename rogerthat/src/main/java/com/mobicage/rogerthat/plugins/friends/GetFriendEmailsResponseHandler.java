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
import java.util.Arrays;

import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.pickle.PickleException;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rpc.IResponse;
import com.mobicage.rpc.ResponseHandler;
import com.mobicage.to.friends.GetFriendEmailsResponseTO;

public class GetFriendEmailsResponseHandler extends ResponseHandler<GetFriendEmailsResponseTO> {

    // Do a forced update of local friend list even if the generation is
    // identical to the one on server
    private volatile boolean mForce = false;
    private volatile boolean mRecalculateMessagesShowInList = false;

    public void setForce(boolean pForce) {
        T.UI();
        mForce = pForce;
    }

    public void setRecalculateMessagesShowInList(boolean pRecalculateMessagesShowInList) {
        T.UI();
        mRecalculateMessagesShowInList = pRecalculateMessagesShowInList;
    }

    @Override
    public void writePickle(DataOutput out) throws IOException {
        T.dontCare();
        super.writePickle(out);
        out.writeBoolean(mForce);
        out.writeBoolean(mRecalculateMessagesShowInList);
    }

    @Override
    public void readFromPickle(int version, DataInput in) throws IOException, PickleException {
        T.dontCare();
        super.readFromPickle(version, in);
        mForce = in.readBoolean();
        mRecalculateMessagesShowInList = in.readBoolean();
    }

    @Override
    public void handle(final IResponse<GetFriendEmailsResponseTO> response) {
        T.BIZZ();
        final GetFriendEmailsResponseTO resp;
        try {
            resp = response.getResponse();
        } catch (Exception e) {
            L.d("Get friend emails failed", e);
            return;
        }

        mMainService.getPlugin(FriendsPlugin.class).updateFriendSet(Arrays.asList(resp.emails),
            resp.friend_set_version, mForce, mRecalculateMessagesShowInList, null);
    }
}

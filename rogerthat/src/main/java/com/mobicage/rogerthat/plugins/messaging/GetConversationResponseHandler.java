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
package com.mobicage.rogerthat.plugins.messaging;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.pickle.PickleException;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rpc.IResponse;
import com.mobicage.rpc.ResponseHandler;
import com.mobicage.to.messaging.GetConversationResponseTO;

public class GetConversationResponseHandler extends ResponseHandler<GetConversationResponseTO> {

    private volatile String mThreadKey;

    public void setThreadKey(String threadKey) {
        this.mThreadKey = threadKey;
    }

    @Override
    public void handle(final IResponse<GetConversationResponseTO> response) {
        T.BIZZ();
        try {
            GetConversationResponseTO result = response.getResponse();
            if (!result.conversation_sent) {
                final MessagingPlugin plugin = mMainService.getPlugin(MessagingPlugin.class);
                plugin.getStore().deleteRequestedConversation(mThreadKey);
            }
        } catch (Exception e) {
            L.d("Get conversation failed", e);
            final MessagingPlugin plugin = mMainService.getPlugin(MessagingPlugin.class);
            plugin.getStore().deleteRequestedConversation(mThreadKey);
        }
    }

    @Override
    public void writePickle(DataOutput out) throws IOException {
        T.dontCare();
        super.writePickle(out);
        out.writeUTF(mThreadKey);
    }

    @Override
    public void readFromPickle(int version, DataInput in) throws IOException, PickleException {
        T.dontCare();
        super.readFromPickle(version, in);
        mThreadKey = in.readUTF();
    }

}

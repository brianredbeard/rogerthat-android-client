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
package com.mobicage.rogerthat.plugins.messaging;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.pickle.PickleException;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rpc.IResponse;
import com.mobicage.rpc.ResponseHandler;
import com.mobicage.to.messaging.LockMessageResponseTO;

public class LockMessageResponseHandler extends ResponseHandler<LockMessageResponseTO> {

    private volatile String mMessageKey;

    @Override
    public void handle(final IResponse<LockMessageResponseTO> response) {
        T.BIZZ();
        try {
            LockMessageResponseTO resp = response.getResponse();
            MessagingPlugin plugin = mMainService.getPlugin(MessagingPlugin.class);
            plugin.messageLocked(mMessageKey, resp.members, MessagingPlugin.DIRTY_BEHAVIOR_NORMAL, true, null);
        } catch (Exception e) {
            L.d("lock message failed", e);
        }
    }

    @Override
    public void writePickle(DataOutput out) throws IOException {
        T.dontCare();
        super.writePickle(out);
        out.writeUTF(mMessageKey);
    }

    @Override
    public void readFromPickle(int version, DataInput in) throws IOException, PickleException {
        T.dontCare();
        super.readFromPickle(version, in);
        mMessageKey = in.readUTF();
    }

    public void setMessageKey(String messageKey) {
        mMessageKey = messageKey;
    }

}

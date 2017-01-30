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

package com.mobicage.rogerthat.plugins.friends;

import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.pickle.PickleException;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rpc.IResponse;
import com.mobicage.rpc.ResponseHandler;
import com.mobicage.to.service.GetStaticFlowResponseTO;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class GetStaticFlowResponseHandler extends ResponseHandler<com.mobicage.to.service.GetStaticFlowResponseTO> {

    private String mStaticFlowHash;
    private String mEmail;

    public void setStaticFlowHash(String staticFlowHash) {
        this.mStaticFlowHash = staticFlowHash;
    }

    public void setEmail(String email) {
        this.mEmail = email;
    }

    @Override
    public void writePickle(DataOutput out) throws IOException {
        T.dontCare();
        super.writePickle(out);
        out.writeUTF(mStaticFlowHash);
        out.writeUTF(mEmail);
    }

    @Override
    public void readFromPickle(int version, DataInput in) throws IOException, PickleException {
        T.dontCare();
        super.readFromPickle(version, in);
        mStaticFlowHash = in.readUTF();
        mEmail = in.readUTF();
    }

    @Override
    public void handle(final IResponse<com.mobicage.to.service.GetStaticFlowResponseTO> response) {
        T.BIZZ();
        try {
            GetStaticFlowResponseTO resp = response.getResponse();
            if (resp != null && resp.staticFlow != null) {
                final FriendsPlugin plugin = mMainService.getPlugin(FriendsPlugin.class);
                plugin.getStore().storeStaticFlow(mEmail, resp.staticFlow, mStaticFlowHash);
            }
        } catch (Exception e) {
            L.bug(e);
        }
    }

}

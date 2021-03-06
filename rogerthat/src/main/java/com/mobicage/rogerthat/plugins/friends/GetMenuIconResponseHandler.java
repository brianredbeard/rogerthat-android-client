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
import com.mobicage.to.service.GetMenuIconResponseTO;

public class GetMenuIconResponseHandler extends ResponseHandler<com.mobicage.to.service.GetMenuIconResponseTO> {

    private String mIconHash;
    private String mEmail;

    public void setIconHash(String iconHash) {
        this.mIconHash = iconHash;
    }

    public void setEmail(String email) {
        this.mEmail = email;
    }

    @Override
    public void writePickle(DataOutput out) throws IOException {
        T.dontCare();
        super.writePickle(out);
        out.writeUTF(mIconHash);
        out.writeUTF(mEmail);
    }

    @Override
    public void readFromPickle(int version, DataInput in) throws IOException, PickleException {
        T.dontCare();
        super.readFromPickle(version, in);
        mIconHash = in.readUTF();
        mEmail = in.readUTF();
    }

    @Override
    public void handle(final IResponse<com.mobicage.to.service.GetMenuIconResponseTO> response) {
        T.BIZZ();
        try {
            GetMenuIconResponseTO resp = response.getResponse();
            if (resp != null)
                if (mIconHash.equals(resp.iconHash)) {
                    final byte[] iconBytes = Base64.decode(resp.icon);
                    final FriendsPlugin plugin = mMainService.getPlugin(FriendsPlugin.class);
                    plugin.storeMenuIcon(mIconHash, iconBytes, mEmail);
                } else {
                    L.bug("Iconhash mismatch");
                }
        } catch (Exception e) {
            L.bug(e);
        }

    }

}

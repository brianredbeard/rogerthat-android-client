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

package com.mobicage.rogerthat.plugins.news;

import android.content.Intent;

import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.pickle.PickleException;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rpc.IResponse;
import com.mobicage.rpc.ResponseHandler;
import com.mobicage.to.news.GetNewsResponseTO;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class GetNewsResponseHandler extends ResponseHandler<GetNewsResponseTO> {

    private String mUUID = null;

    public void setUUID(String uuid) {
        this.mUUID = uuid;
    }

    @Override
    public void writePickle(DataOutput out) throws IOException {
        T.dontCare();
        super.writePickle(out);
        out.writeUTF(this.mUUID);
    }

    @Override
    public void readFromPickle(int version, DataInput in) throws IOException, PickleException {
        T.dontCare();
        super.readFromPickle(version, in);
        this.mUUID = in.readUTF();
    }

    @Override
    public void handle(IResponse<GetNewsResponseTO> response) {
        T.BIZZ();
        try {
            GetNewsResponseTO resp = response.getResponse();
            Intent intent = new Intent(NewsPlugin.GET_NEWS_RECEIVED_INTENT);
            intent.putExtra("ids", resp.ids);
            intent.putExtra("versions", resp.versions);
            mMainService.sendBroadcast(intent);
        } catch (Exception e) {
            L.e("Server responded with an error.", e);
        }
    }
}
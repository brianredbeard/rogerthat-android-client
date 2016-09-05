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

package com.mobicage.rogerthat.plugins.messaging;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.jivesoftware.smack.util.Base64;

import com.mobicage.api.messaging.Rpc;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.pickle.PickleException;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rpc.IResponse;
import com.mobicage.rpc.ResponseHandler;
import com.mobicage.to.messaging.UploadChunkRequestTO;
import com.mobicage.to.messaging.UploadChunkResponseTO;

public class UploadChunkResponseHandler extends ResponseHandler<com.mobicage.to.messaging.UploadChunkResponseTO> {

    private UploadChunkRequestTO mChunkRequest = null;

    public void setChunkRequest(UploadChunkRequestTO chunkRequest) {
        this.mChunkRequest = chunkRequest;
    }

    @Override
    public void writePickle(DataOutput out) throws IOException {
        T.dontCare();
        super.writePickle(out);
        byte[] chunk = Base64.decode(this.mChunkRequest.chunk);
        out.writeInt(chunk.length);
        out.write(chunk);
        out.writeUTF(this.mChunkRequest.message_key);
        out.writeUTF(this.mChunkRequest.parent_message_key == null ? "" : this.mChunkRequest.parent_message_key);
        out.writeUTF(this.mChunkRequest.photo_hash == null ? "" : this.mChunkRequest.photo_hash);
        out.writeUTF(this.mChunkRequest.service_identity_user == null ? "" : this.mChunkRequest.service_identity_user);
        out.writeLong(this.mChunkRequest.number);
        out.writeLong(this.mChunkRequest.total_chunks);
    }

    @Override
    public void readFromPickle(int version, DataInput in) throws IOException, PickleException {
        T.dontCare();
        super.readFromPickle(version, in);
        this.mChunkRequest = new UploadChunkRequestTO();
        byte[] chunk = new byte[in.readInt()];
        in.readFully(chunk);
        this.mChunkRequest.chunk = Base64.encodeBytes(chunk);
        this.mChunkRequest.message_key = in.readUTF();
        String tmp = in.readUTF();
        this.mChunkRequest.parent_message_key = tmp.equals("") ? null : tmp;
        tmp = in.readUTF();
        this.mChunkRequest.photo_hash = tmp.equals("") ? null : tmp;
        this.mChunkRequest.service_identity_user = in.readUTF();
        this.mChunkRequest.number = in.readLong();
        this.mChunkRequest.total_chunks = in.readLong();
    }

    @Override
    public void handle(IResponse<UploadChunkResponseTO> response) {
        T.BIZZ();
        try {
            response.getResponse();
        } catch (Exception e) {
            L.e("Server responded with an error. Must resend the chunk.", e);
            try {
                UploadChunkResponseHandler responsehandler = new UploadChunkResponseHandler();
                responsehandler.setChunkRequest(mChunkRequest);
                Rpc.uploadChunk(responsehandler, mChunkRequest);
            } catch (Exception e1) {
                L.bug("Resending the uploadChunk failed.", e1);
            }
            return;
        }
    }
}

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

import com.mobicage.rogerthat.util.IOUtils;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.pickle.PickleException;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rpc.IResponse;
import com.mobicage.rpc.ResponseHandler;
import com.mobicage.to.messaging.SendMessageResponseTO;

public class SendMessageResponseHandler extends ResponseHandler<SendMessageResponseTO> {

    private final int CLASS_VERSION = 2;

    private volatile String mParentKey;
    private volatile String mKey;
    private volatile boolean mAttachmentsUploaded;

    @Override
    public void handle(final IResponse<SendMessageResponseTO> response) {
        T.BIZZ();
        try {
            SendMessageResponseTO resp = response.getResponse();
            MessagingPlugin messagingPlugin = mMainService.getPlugin(MessagingPlugin.class);

            if (mAttachmentsUploaded) {
                String parentKeyBackup = mParentKey;

                if (mParentKey == null) {
                    mParentKey = mKey;
                }
                IOUtils.copyDirectory(messagingPlugin.attachmentsDir(mParentKey, mKey),
                    messagingPlugin.attachmentsDir(mParentKey, resp.key));

                if (parentKeyBackup == null) {
                    IOUtils.deleteRecursive(messagingPlugin.attachmentTreadDir(mKey));
                }
                IOUtils.deleteRecursive(messagingPlugin.attachmentsDir(mParentKey, mKey));
            }
        } catch (Exception e) {
            L.d("Send message failed", e);
            final MessagingPlugin plugin = mMainService.getPlugin(MessagingPlugin.class);
            plugin.setMessageFailed(mKey);
        }
    }

    @Override
    public int getPickleClassVersion() {
        T.dontCare();
        return CLASS_VERSION;
    }

    @Override
    public void writePickle(DataOutput out) throws IOException {
        T.dontCare();
        super.writePickle(out);
        out.writeUTF(mKey);
        out.writeUTF(mParentKey == null ? "" : mParentKey);
        out.writeBoolean(mAttachmentsUploaded);
    }

    @Override
    public void readFromPickle(int version, DataInput in) throws IOException, PickleException {
        T.dontCare();
        super.readFromPickle(version, in);
        mKey = in.readUTF();
        if (version >= 2) {
            mParentKey = in.readUTF();
            mParentKey = "".equals(mParentKey) ? null : mParentKey;
            mAttachmentsUploaded = in.readBoolean();
        } else {
            mParentKey = null;
            mAttachmentsUploaded = false;
        }
    }

    public void setParentKey(String parentKey) {
        T.UI();
        mParentKey = parentKey;
    }

    public void setKey(String key) {
        T.UI();
        mKey = key;
    }

    public void setAttachmentsUploaded(boolean attachmentsUploaded) {
        T.UI();
        mAttachmentsUploaded = attachmentsUploaded;
    }
}

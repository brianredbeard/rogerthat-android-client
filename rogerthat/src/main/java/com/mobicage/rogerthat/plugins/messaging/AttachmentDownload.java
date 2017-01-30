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

import java.util.Map;

import com.mobicage.rpc.IncompleteMessageException;
import com.mobicage.to.messaging.AttachmentTO;

public class AttachmentDownload extends AttachmentTO {

    public String threadKey;
    public String messageKey;

    public AttachmentDownload(AttachmentTO attachmentTO, String threadKey, String messageKey)
        throws IncompleteMessageException {
        super(attachmentTO.toJSONMap());
        this.threadKey = threadKey;
        this.messageKey = messageKey;
    }

    public AttachmentDownload(Map<String, Object> json) throws IncompleteMessageException {
        super(json);
        this.threadKey = (String) json.get("thread_key");
        this.messageKey = (String) json.get("message_key");
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> map = super.toJSONMap();
        map.put("thread_key", this.threadKey);
        map.put("message_key", this.messageKey);
        return map;
    }
}

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

package com.mobicage.to.messaging;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class UploadChunkRequestTO implements com.mobicage.rpc.IJSONable {

    public String chunk;
    public String content_type;
    public String message_key;
    public long number;
    public String parent_message_key;
    public String photo_hash;
    public String service_identity_user;
    public long total_chunks;

    public UploadChunkRequestTO() {
    }

    public UploadChunkRequestTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("chunk")) {
            Object val = json.get("chunk");
            this.chunk = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.UploadChunkRequestTO object is missing field 'chunk'");
        }
        if (json.containsKey("content_type")) {
            Object val = json.get("content_type");
            this.content_type = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.UploadChunkRequestTO object is missing field 'content_type'");
        }
        if (json.containsKey("message_key")) {
            Object val = json.get("message_key");
            this.message_key = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.UploadChunkRequestTO object is missing field 'message_key'");
        }
        if (json.containsKey("number")) {
            Object val = json.get("number");
            this.number = ((Long) val).longValue();
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.UploadChunkRequestTO object is missing field 'number'");
        }
        if (json.containsKey("parent_message_key")) {
            Object val = json.get("parent_message_key");
            this.parent_message_key = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.UploadChunkRequestTO object is missing field 'parent_message_key'");
        }
        if (json.containsKey("photo_hash")) {
            Object val = json.get("photo_hash");
            this.photo_hash = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.UploadChunkRequestTO object is missing field 'photo_hash'");
        }
        if (json.containsKey("service_identity_user")) {
            Object val = json.get("service_identity_user");
            this.service_identity_user = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.UploadChunkRequestTO object is missing field 'service_identity_user'");
        }
        if (json.containsKey("total_chunks")) {
            Object val = json.get("total_chunks");
            this.total_chunks = ((Long) val).longValue();
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.UploadChunkRequestTO object is missing field 'total_chunks'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("chunk", this.chunk);
        obj.put("content_type", this.content_type);
        obj.put("message_key", this.message_key);
        obj.put("number", this.number);
        obj.put("parent_message_key", this.parent_message_key);
        obj.put("photo_hash", this.photo_hash);
        obj.put("service_identity_user", this.service_identity_user);
        obj.put("total_chunks", this.total_chunks);
        return obj;
    }

}

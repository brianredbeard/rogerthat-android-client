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

package com.mobicage.to.messaging;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class AttachmentTO implements com.mobicage.rpc.IJSONable {

    public String content_type;
    public String download_url;
    public String name;
    public long size;

    public AttachmentTO() {
    }

    public AttachmentTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("content_type")) {
            Object val = json.get("content_type");
            this.content_type = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.AttachmentTO object is missing field 'content_type'");
        }
        if (json.containsKey("download_url")) {
            Object val = json.get("download_url");
            this.download_url = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.AttachmentTO object is missing field 'download_url'");
        }
        if (json.containsKey("name")) {
            Object val = json.get("name");
            this.name = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.AttachmentTO object is missing field 'name'");
        }
        if (json.containsKey("size")) {
            Object val = json.get("size");
            if (val instanceof Integer) {
                this.size = ((Integer) val).longValue();
            } else {
                this.size = ((Long) val).longValue();
            }
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.AttachmentTO object is missing field 'size'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("content_type", this.content_type);
        obj.put("download_url", this.download_url);
        obj.put("name", this.name);
        obj.put("size", this.size);
        return obj;
    }

}
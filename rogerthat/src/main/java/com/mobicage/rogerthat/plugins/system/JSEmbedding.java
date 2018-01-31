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

package com.mobicage.rogerthat.plugins.system;

import java.util.HashMap;
import java.util.Map;

import com.mobicage.rpc.IJSONable;

public class JSEmbedding implements IJSONable {

    public static final int VERSION = 1;
    public static final int STATUS_UNAVAILABLE = 0;
    public static final int STATUS_AVAILABLE = 1;

    private String mName;
    private String mEmbeddingHash;
    private long mStatus;

    public JSEmbedding(String name, String embeddingHash, long status) {
        mName = name;
        mEmbeddingHash = embeddingHash;
        mStatus = status;
    }

    public String getName() {
        return mName;
    }

    public String getEmeddingHash() {
        return mEmbeddingHash;
    }

    public long getStatus() {
        return mStatus;
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> json = new HashMap<String, Object>();
        json.put("version", VERSION);
        json.put("name", mName);
        json.put("embeddingHash", mEmbeddingHash);
        json.put("status", mStatus);
        return json;
    }

}

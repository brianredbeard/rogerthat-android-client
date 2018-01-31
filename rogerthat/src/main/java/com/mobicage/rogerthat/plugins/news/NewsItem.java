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
package com.mobicage.rogerthat.plugins.news;

import com.mobicage.rpc.config.CloudConstants;
import com.mobicage.to.news.AppNewsItemTO;

import java.util.LinkedHashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public class NewsItem extends AppNewsItemTO {

    public static final long TYPE_NORMAL = 1;
    public static final long TYPE_QR_CODE = 2;

    public static final long FLAG_ACTION_ROGERTHAT = 1;
    public static final long FLAG_ACTION_FOLLOW = 2;
    public static final long FLAG_SILENT = 4;

    public boolean read;
    public boolean pinned;
    public boolean rogered;
    public boolean disabled;
    public boolean isPartial;
    public long sortKey;

    public String getAvatarUrl() {
        if (isPartial)
            return null;
        return CloudConstants.CACHED_AVATAR_URL_PREFIX + sender.avatar_id;
    }


    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = super.toJSONMap();
        obj.put("read", this.read);
        obj.put("pinned", this.pinned);
        obj.put("rogered", this.rogered);
        obj.put("disabled", this.disabled);
        obj.put("is_partial", this.isPartial);
        obj.put("sort_key", this.sortKey);
        return obj;
    }

}

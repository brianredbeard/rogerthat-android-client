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

import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rpc.IncompleteMessageException;
import com.mobicage.to.news.BaseNewsItemTO;

import java.util.Map;

@SuppressWarnings("unchecked")
public class NewsItem extends BaseNewsItemTO {

    public static long TYPE_NORMAL = 1;
    public static long TYPE_QR_CODE = 2;

    public boolean read;
    public boolean pinned;
    public boolean rogered;
    public boolean deleted;

    public NewsItem() {
    }

    public NewsItem(Map<String, Object> json) throws IncompleteMessageException {
        super(json);
        this.read = (boolean) json.get("read");
        this.pinned = (boolean) json.get("pinned");
        this.rogered = (boolean) json.get("rogered");
        this.deleted = (boolean) json.get("deleted");
    }

    public static NewsItem fromFormMessage(Map<String, Object> form) {
        L.bug("NewsItem.fromFormMessage is not implemented");
        return null;
    }


    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> map = super.toJSONMap();
        map.put("read", read);
        map.put("pinned", pinned);
        map.put("rogered", rogered);
        map.put("deleted", deleted);
        return map;
    }

}

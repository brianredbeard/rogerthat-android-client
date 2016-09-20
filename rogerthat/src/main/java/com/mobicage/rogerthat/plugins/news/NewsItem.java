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

import java.util.Map;

import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rpc.IncompleteMessageException;
import com.mobicage.to.messaging.ButtonTO;
import com.mobicage.to.messaging.MemberStatusTO;
import com.mobicage.to.messaging.MessageTO;
import com.mobicage.to.news.BaseNewsItemTO;

@SuppressWarnings("unchecked")
public class NewsItem extends BaseNewsItemTO {


    public boolean dirty;
    public boolean pinned;

    public NewsItem() {
    }

    public NewsItem(Map<String, Object> json) throws IncompleteMessageException {
        super(json);
        this.dirty =  (boolean) json.get("dirty");
        this.pinned = (boolean) json.get("pinned");
    }

    public static NewsItem fromFormMessage(Map<String, Object> form) {
        L.bug("NewsItem.fromFormMessage is not implemented");
        return null;
    }


    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> map = super.toJSONMap();
        map.put("dirty", dirty);
        map.put("pinned", pinned);
        return map;
    }

}

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
public class NewsItemDetails {


    public long id;
    public long version;
    public boolean dirty;
    public boolean pinned;
}

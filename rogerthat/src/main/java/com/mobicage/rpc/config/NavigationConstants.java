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

package com.mobicage.rpc.config;

import android.graphics.Color;

import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.NavigationItem;

public class NavigationConstants {

    public static NavigationItem[] getNavigationItems() {
        return new NavigationItem[]{
                new NavigationItem(FontAwesome.Icon.faw_newspaper_o, null, "news", R.string.news, null, Color.parseColor("#84B53F")),
                new NavigationItem(FontAwesome.Icon.faw_envelope, null, "messages", R.string.tab_messaging, null, Color.parseColor("#84B53F")),
                new NavigationItem(FontAwesome.Icon.faw_cogs, null, "services", R.string.tab_services, null, Color.parseColor("#84B53F")),
                new NavigationItem(FontAwesome.Icon.faw_users, null, "friends", R.string.tab_friends, null, Color.parseColor("#84B53F")),
                new NavigationItem(FontAwesome.Icon.faw_qrcode, null, "scan", R.string.scan, null, Color.parseColor("#84B53F")),
                new NavigationItem(FontAwesome.Icon.faw_user, null, "profile", R.string.profile, null, Color.parseColor("#84B53F")),
                new NavigationItem(FontAwesome.Icon.faw_wrench, null, "settings", R.string.settings, null, Color.parseColor("#84B53F")),
                new NavigationItem(FontAwesome.Icon.faw_history, null, "stream", R.string.tab_stream, null, Color.parseColor("#84B53F")),
        };
    }

    public static NavigationItem[] getNavigationFooterItems() {
        return new NavigationItem[]{
                new NavigationItem(FontAwesome.Icon.faw_newspaper_o, null, "news", R.string.news),
                new NavigationItem(FontAwesome.Icon.faw_shopping_cart, "action", "320aed9cbe35387739dd9ea0c064a0539dac5a183fc3b1f982a63afe9ff0bd75", R.string.order),
                new NavigationItem(FontAwesome.Icon.faw_cutlery, "action", "d2620bdc4c92a0016d714478752aabaf71aa465621c67ed2a217b785726e5435", R.string.reserve),
                new NavigationItem(FontAwesome.Icon.faw_credit_card, null, "qrcode", R.string.app_name),
        };
    }
}

/*
 * Copyright 2016 Mobicage NV
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @@license_version:1.1@@
 */

package com.mobicage.rpc.config;

import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.ServiceBoundActivity;

public class NavigationConstants {

    public static ServiceBoundActivity.NavigationItem[] getNavigationItems() {
        return new ServiceBoundActivity.NavigationItem[]{
                new ServiceBoundActivity.NavigationItem(R.drawable.menu_0, null, "messages", R.string.tab_messaging, false),
                new ServiceBoundActivity.NavigationItem(R.drawable.menu_1, null, "services", R.string.tab_services, false),
                new ServiceBoundActivity.NavigationItem(R.drawable.menu_2, null, "friends", R.string.tab_friends, false),
                new ServiceBoundActivity.NavigationItem(R.drawable.menu_3, null, "scan", R.string.scan, false),
                new ServiceBoundActivity.NavigationItem(R.drawable.menu_4, null, "profile", R.string.profile, false),
                new ServiceBoundActivity.NavigationItem(R.drawable.menu_5, null, "settings", R.string.settings, false),
                new ServiceBoundActivity.NavigationItem(R.drawable.menu_6, null, "stream", R.string.tab_stream, false),
        };
    }

    public static ServiceBoundActivity.NavigationItem[] getNavigationFooterItems() {
        return new ServiceBoundActivity.NavigationItem[]{
                new ServiceBoundActivity.NavigationItem(FontAwesome.Icon.faw_newspaper_o, null, "news", R.string.tab_messaging, false),
                new ServiceBoundActivity.NavigationItem(FontAwesome.Icon.faw_shopping_cart, "action", "__sln__.order", R.string.order, false),
                new ServiceBoundActivity.NavigationItem(FontAwesome.Icon.faw_calendar, "click", "agenda", R.string.agenda, false),
                new ServiceBoundActivity.NavigationItem(FontAwesome.Icon.faw_credit_card, null, "qrcode", R.string.app_name, true),
        };
    }
}

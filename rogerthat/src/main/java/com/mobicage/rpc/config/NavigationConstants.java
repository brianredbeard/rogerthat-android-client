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

package com.mobicage.rpc.config;

import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mobicage.rogerthat.ServiceBoundActivity;

public class NavigationConstants {
    public static final String[] NAVIGATION_CLICKS = new String[]{"messages", "services", "friends", "scan", "profile", "settings", "stream"};
    public static final String[] NAVIGATION_TAGS = new String[]{null, null, null, null, null, null, null};

    public static final String[] NAVIGATION_FOOTER_TAGS = new String[]{"news", "action|__sln__.order", "click|agenda", "qrcode"};
    // faw_newspaper_o
    // faw_shopping_cart
    // faw_calendar
    // faw_credit_card


    public static ServiceBoundActivity.NavigationItem[] getNavigationFooterItems() {

        return new ServiceBoundActivity.NavigationItem[]{
                new ServiceBoundActivity.NavigationItem(FontAwesome.Icon.faw_newspaper_o, null, "news", com.mobicage.rogerth.at.R.string.news),
                new ServiceBoundActivity.NavigationItem(FontAwesome.Icon.faw_shopping_cart, "action", "__sln__.order", com.mobicage.rogerth.at.R.string.order),
                new ServiceBoundActivity.NavigationItem(FontAwesome.Icon.faw_calendar, "click", "agenda", com.mobicage.rogerth.at.R.string.order),
                new ServiceBoundActivity.NavigationItem(FontAwesome.Icon.faw_credit_card, null, "qrcode", com.mobicage.rogerth.at.R.string.order)
        };
    }
}

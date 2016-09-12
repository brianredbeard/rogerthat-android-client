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

import com.mobicage.rogerth.at.R;

public class AppConstants {
    static final int APP_TYPE_ROGERTHAT = 0;
    static final int APP_TYPE_CITYAPP = 1;
    static final int APP_TYPE_ENTERPRISE = 2;
    static final int APP_TYPE_CONTENT_BRANDING = 3;
    static final int APP_TYPE_YSAAA = 4;
    static int getAppType() {
        return APP_TYPE_ROGERTHAT;
    };

    public static final int REGISTRATION_TYPE_DEFAULT = 1;
    public static final int REGISTRATION_TYPE_OAUTH = 2;
    public static final String REGISTRATION_TYPE_OAUTH_DOMAIN = "dummy";
    public static int getRegistrationType() {
        return REGISTRATION_TYPE_DEFAULT;
    };

    // Customized by App flavor
    public static final String APP_ID = "rogerthat";
    public static final int HOME_ACTIVITY_LAYOUT = R.layout.messaging;
    public static final int HOMESCREEN_QRCODE_HEADER = R.string.loyalty_card_description;
    public static final boolean SHOW_HOMESCREEN_FOOTER = false;
    public static final String FACEBOOK_APP_ID = "188033791211994";
    public static final boolean FACEBOOK_REGISTRATION = true;
    public static final String APP_EMAIL = "service-46e13548-a278-4780-a70a-450452a6542d@rogerth.at";
    public static final String APP_SERVICE_GUID = null;
    public static final boolean FRIENDS_ENABLED = true;
    public static final boolean SERVICES_ENABLED = true;
    public static final FriendsCaption FRIENDS_CAPTION = FriendsCaption.FRIENDS;
    public static final boolean SHOW_FRIENDS_IN_MORE = false;
    public static final boolean SHOW_PROFILE_IN_MORE = true;
    public static final boolean SHOW_SCAN_IN_MORE = false;
    public static final boolean FULL_WIDTH_HEADERS = false;

    public static final String[] NAVIGATION_CLICKS = new String[] {"messages", "services", "friends", "scan", "profile", "settings"};
    public static final String[] NAVIGATION_TAGS = new String[] {null, null, null, null, null, null};

    public static final boolean REGISTRATION_ASKS_LOCATION_PERMISSION = true;
    public static final int[] SEARCH_SERVICES_IF_NONE_CONNECTED = new int[] { -1 };

    public static final String[] PROFILE_DATA_FIELDS = new String[] {"todo ruben field", "todo ruben teset"};
    public static final boolean PROFILE_SHOW_GENDER_AND_BIRTHDATE = true;

    public static final String ABOUT_WEBSITE = "www.rogerthat.net";
    public static final String ABOUT_WEBSITE_URL = "http://www.rogerthat.net";
    public static final String ABOUT_EMAIL = "info@mobicage.com";
    public static final String ABOUT_TWITTER = "@rogerthat";
    public static final String ABOUT_TWITTER_URL = "https://twitter.com/rogerthat";
    public static final String ABOUT_FACEBOOK = "/rogerthatplatform";
    public static final String ABOUT_FACEBOOK_URL = "https://www.facebook.com/rogerthatplatform";

    public static final boolean SPEECH_TO_TEXT = false;
    public static final boolean SECURE_APP = false;
    public static final int SECURE_PIN_INTERVAL = 15 * 60;
    public static final int SECURE_PIN_RETRY_INTERVAL = 5 * 60;
}

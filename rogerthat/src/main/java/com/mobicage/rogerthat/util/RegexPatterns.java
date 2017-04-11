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
package com.mobicage.rogerthat.util;

import java.util.regex.Pattern;

public class RegexPatterns {

    public static final Pattern EMAIL = Pattern
        .compile(
            "[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?",
            Pattern.CASE_INSENSITIVE);

    public static final Pattern PIN = Pattern.compile("^\\d\\d\\d\\d$");

    public static final Pattern FRIEND_INVITE_URL = Pattern.compile("^rogerthat://q/i.*$");

    public static final Pattern FRIEND_INVITE_WITH_SECRET_URL = Pattern.compile("^rogerthat://q/i.*\\?.*s=.+$");

    public static final Pattern SERVICE_INTERACT_URL = Pattern.compile("^rogerthat://q/s/(.*)/(\\d+)(\\?.+)?$");

    public static final Pattern OPEN_HOME_URL = Pattern.compile("^rogerthat://$");

    public static final Pattern IDENTITY_SHORT_URL = Pattern
        .compile("^https://rogerth.at/M/([0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ$*-./:]+)(\\?.*)?$");

    public static final Pattern BRANDING_BACKGROUND_COLOR = Pattern
        .compile(
            "<\\s*meta\\s+property\\s*=\\s*\"rt:style:background-color\"\\s+content\\s*=\\s*\"(#[a-f0-9]{3}([a-f0-9]{3})?)\"\\s*/>",
            Pattern.CASE_INSENSITIVE);

    public static final Pattern BRANDING_MENU_ITEM_COLOR = Pattern
        .compile(
            "<\\s*meta\\s+property\\s*=\\s*\"rt:style:menu-item-color\"\\s+content\\s*=\\s*\"(#[a-f0-9]{3}([a-f0-9]{3})?)\"\\s*/>",
            Pattern.CASE_INSENSITIVE);

    public static final Pattern BRANDING_DIMENSIONS = Pattern.compile(
        "<\\s*meta\\s+property\\s*=\\s*\"rt:dimensions\"\\s+content\\s*=\\s*\"\\[((\\d+,){3}\\d+)\\]\"\\s*/>",
        Pattern.CASE_INSENSITIVE);

    public static final Pattern BRANDING_COLOR_SCHEME = Pattern.compile(
            "<\\s*meta\\s+property\\s*=\\s*\"rt:style:color-scheme\"\\s+content\\s*=\\s*\"(dark|light)\"\\s*/>",
            Pattern.CASE_INSENSITIVE);

    public static final Pattern BRANDING_ORIENTATION = Pattern.compile
            ("<\\s*meta\\s+property\\s*=\\s*\"rt:style:orientation\"\\s+content\\s*=\\s*\"" +
                    "(portrait|landscape|dynamic)\"\\s*/>", Pattern.CASE_INSENSITIVE);

    public static final Pattern BRANDING_SHOW_HEADER = Pattern.compile(
            "<\\s*meta\\s+property\\s*=\\s*\"rt:style:show-header\"\\s+content\\s*=\\s*\"(true|false)\"\\s*/>",
            Pattern.CASE_INSENSITIVE);

    public static final Pattern BRANDING_SHOW_NAME = Pattern.compile(
            "<\\s*meta\\s+property\\s*=\\s*\"rt:style:show-name\"\\s+content\\s*=\\s*\"(true|false)\"\\s*/>",
            Pattern.CASE_INSENSITIVE);

    public static final Pattern BRANDING_CONTENT_TYPE = Pattern.compile(
        "<\\s*meta\\s+property\\s*=\\s*\"rt:style:content-type\"\\s+content\\s*=\\s*\"([a-zA-Z0-9_/-]*)\"\\s*/>",
        Pattern.CASE_INSENSITIVE);

    public static final Pattern BRANDING_WAKELOCK_ENABLED = Pattern.compile(
        "<\\s*meta\\s+property\\s*=\\s*\"rt:wakelock\"\\s+content\\s*=\\s*\"(true|false)\"\\s*/>",
        Pattern.CASE_INSENSITIVE);

    public static final Pattern BRANDING_EXTERNAL_URLS = Pattern
        .compile("<\\s*meta\\s+property\\s*=\\s*\"rt:external-url\"\\s+content\\s*=\\s*\"(.*)\"\\s*/>",
            Pattern.CASE_INSENSITIVE);

    public static final Pattern IS_DASHBOARD_ACCOUNT = Pattern.compile("^dashboard.*@rogerth\\.at$",
        Pattern.CASE_INSENSITIVE);

    public static final Pattern BRANDING_DISPLAY_TYPE = Pattern.compile(
            "<\\s*meta\\s+property\\s*=\\s*\"rt:style:display-type\"\\s+content\\s*=\\s*\"(native|webview)\"\\s*/>",
            Pattern.CASE_INSENSITIVE);

}

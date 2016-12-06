#!/usr/bin/env python
# -*- coding: utf-8 -*-
# Copyright 2016 Mobicage NV
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# @@license_version:1.1@@

import hashlib
import logging
import os
import pprint
import re
import shutil
import sys
import tempfile
import yaml
from PIL import Image, ImageDraw
from xml.dom import minidom

logging.basicConfig(
        level=logging.INFO,
        format="%(levelname)s [%(asctime)s] [%(funcName)s:%(lineno)d] %(message)s",
        datefmt="%H:%M:%S", stream=sys.stdout)

CURRENT_DIR = os.path.dirname(os.path.realpath(__file__))
APPS_REPO_DIR = os.path.join(CURRENT_DIR, "..", "..", "apps", 'res')
sys.path.append(os.path.join(CURRENT_DIR, "..", "..", "rogerthat-build", 'src'))
import app_utils

ANDROID_SRC_DIR = os.path.join(CURRENT_DIR, '..', 'rogerthat', 'src')
SRC_JAVA_DIR = os.path.join(ANDROID_SRC_DIR, 'main', 'java')
TEST_SRC_JAVA_DIR = os.path.join(ANDROID_SRC_DIR, 'androidTest', 'java')
SRC_RES_DIR = os.path.join(ANDROID_SRC_DIR, 'main', 'res')

MAIN_APP_ID = "rogerthat"

APP_TYPE_ROGERTHAT = "rogerthat"
APP_TYPE_CITYPAPP = "cityapp"
APP_TYPE_ENTERPRISE = "enterprise"
APP_TYPE_CONTENT_BRANDING = "content_branding"
APP_TYPE_YSAAA = "ysaaa"

LAUNCHER_ICON_SIZES = {
    'drawable-ldpi-v5': 36,
    'drawable': 48,
    'drawable-mdpi-v5': 48,
    'drawable-hdpi-v5': 72,
    'drawable-xhdpi-v5': 96,
    'drawable-xxhdpi-v5': 180,
    'drawable-xxxhdpi-v5': 196,
}

ICON_SIZES = {
    'drawable-ldpi-v5': 36,
    'drawable': 48,
    'drawable-mdpi-v5': 48,
    'drawable-hdpi-v5': 72,
    'drawable-xhdpi-v5': 96,
    'drawable-xxhdpi-v5': 180,
}

SCREEN_SIZES = {
    'drawable-ldpi-v5': 240,
    'drawable': 320,
    'drawable-mdpi-v5': 320,
    'drawable-hdpi-v5': 480,
    'drawable-xhdpi-v5': 720,
    'drawable-xxhdpi-v5': 1080,
}

NOTIFICATION_ICON_SIZES = {
    'drawable-ldpi-v5': 18,
    'drawable': 24,
    'drawable-mdpi-v5': 36,
    'drawable-hdpi-v5': 48,
    'drawable-xhdpi-v5': 72,
    'drawable-xxhdpi-v5': 96,
}

HOME_SCREEN_STYLE_NEWS = "news"
HOME_SCREEN_STYLE_MESSAGING = "messaging"
HOME_SCREEN_STYLE_2X3 = "2x3"
HOME_SCREEN_STYLE_3X3 = "3x3"

FRIENDS_CAPTION_FRIENDS = "friends"
FRIENDS_CAPTION_COLLEAGUES = "colleagues"
FRIENDS_CAPTION_CONTACTS = "contacts"

FRIENDS_CAPTION_ENUMS = {FRIENDS_CAPTION_FRIENDS: 'FriendsCaption.FRIENDS',
                         FRIENDS_CAPTION_COLLEAGUES: 'FriendsCaption.COLLEAGUES',
                         FRIENDS_CAPTION_CONTACTS: 'FriendsCaption.CONTACTS'}

COLOURED_BUTTONS = {
    'facebook': ('#39527F', '#39527F'),
    'gray': ('#989898', '#989898'),
    'primary': {
        'sizes': {
            'drawable-ldpi-v5': SCREEN_SIZES['drawable-ldpi-v5'] * .9,
            'drawable': SCREEN_SIZES['drawable'] * .9,
            'drawable-mdpi-v5': SCREEN_SIZES['drawable-mdpi-v5'] * .9,
            'drawable-hdpi-v5': SCREEN_SIZES['drawable-hdpi-v5'] * .9,
            'drawable-xhdpi-v5': SCREEN_SIZES['drawable-xhdpi-v5'] * .9,
            'drawable-xxhdpi-v5': SCREEN_SIZES['drawable-xxhdpi-v5'] * .9,
        }
    },
    'small_square': {
        'sizes': {
            'drawable-ldpi-v5': SCREEN_SIZES['drawable-ldpi-v5'] * .1,
            'drawable': SCREEN_SIZES['drawable'] * .1,
            'drawable-mdpi-v5': SCREEN_SIZES['drawable-mdpi-v5'] * .1,
            'drawable-hdpi-v5': SCREEN_SIZES['drawable-hdpi-v5'] * .1,
            'drawable-xhdpi-v5': SCREEN_SIZES['drawable-xhdpi-v5'] * .1,
            'drawable-xxhdpi-v5': SCREEN_SIZES['drawable-xxhdpi-v5'] * .1,
        }
    }
}

LICENSE = app_utils.get_license_header()


def sha256_hash(val):
    digester = hashlib.sha256()
    digester.update(val)
    return digester.hexdigest()


def generate_resource_images(source_file_name, size, height_width_ratio):
    # size: percentage of screen width
    # height_width_ratio: 1/2 means that the image should be twice as wide compared to its height.
    resource_name = os.path.split(source_file_name)[1]

    im1 = Image.open(source_file_name)
    im1_width, im1_height = im1.size
    im1_heigth_width_ratio = float(im1_height) / im1_width

    im1_heigth_width_ratio_str = "%.2f" % im1_heigth_width_ratio
    height_width_ratio_str = "%.2f" % height_width_ratio
    if im1_heigth_width_ratio_str != height_width_ratio_str:
        raise Exception(
                "Cannot generate resource images for %s ratio does not match (IMG:%s, GIVEN:%s)" % (
                    source_file_name, im1_heigth_width_ratio_str, height_width_ratio_str))

    for drawable_folder_name, screen_width in SCREEN_SIZES.iteritems():
        width = int(screen_width * size)
        height = int(width * height_width_ratio)
        app_utils.resize_image(source_file_name,
                               os.path.join(SRC_RES_DIR, drawable_folder_name, resource_name),
                               width, height)


def bool_str(b):
    return "true" if b else "false"


def quoted_str_or_null(s):
    return ('"%s"' % s) if s else "null"


def rename_package():
    rogerthat_build_gradle = os.path.join(ANDROID_SRC_DIR, '..', 'build.gradle')
    facebook_app_id = doc["APP_CONSTANTS"].get("FACEBOOK_APP_ID")
    with open(rogerthat_build_gradle, 'r+') as f:
        s = f.read()
        package_sufix = APP_ID.replace('-', '.')
        if APP_ID != 'rogerthat':
            s = s.replace('com.mobicage.rogerth.at', 'com.mobicage.rogerthat')
        s = s.replace("applicationIdSuffix '.debug'", "applicationIdSuffix '.%s.debug'" % package_sufix)
        s = s.replace("applicationIdSuffix ''", "applicationIdSuffix '.%s'" % package_sufix)
        s = s.replace('"app_id", "rogerthat"', '"app_id", "%s"' % APP_ID)
        if facebook_app_id:
            s = s.replace('188033791211994', str(facebook_app_id))
        f.seek(0)
        f.write(s)
        f.truncate()
    with open(os.path.join(ANDROID_SRC_DIR, 'main', 'AndroidManifest.xml'), 'r+') as f:
        s = f.read()

        if doc['CLOUD_CONSTANTS'].get("USE_XMPP_KICK_CHANNEL", False):
            # remove GCM permissions
            splitted = s.split('<!-- BEGIN GCM -->')
            if len(splitted) != 1:
                s = splitted[0].strip() + splitted[1].split('<!-- END GCM -->')[1]

        device_type = doc["BUILD_CONSTANTS"].get("DEVICE_TYPE", "universal")
        splitted = s.split('<!-- BEGIN supports-screens -->')
        if len(splitted) == 2:
            if device_type == "phone":
                supports_screens = ""
            elif device_type == "tablet":
                supports_screens = '''

    <!-- BEGIN supports-screens -->
    <supports-screens android:smallScreens="false"
                      android:normalScreens="false"
                      android:largeScreens="true"
                      android:xlargeScreens="true"
                      android:requiresSmallestWidthDp="600" />
    <!-- END supports-screens -->'''
            else:
                supports_screens = ""

            s = splitted[0].strip() + supports_screens + splitted[1].split('<!-- END supports-screens -->')[1]
        else:
            raise Exception("Could not apply DEVICE_TYPE '%s'" % device_type)

        if not facebook_app_id:
            # remove FacebookProvider
            splitted = s.split('<!-- BEGIN FB -->')
            if len(splitted) != 1:
                s = splitted[0].strip() + splitted[1].split('<!-- END FB -->')[1]

        f.seek(0)
        f.write(s)
        f.truncate()


def create_notification_icon(android_icon_filename, android_notification_icon_filename):
    img = Image.open(android_icon_filename)
    img = img.convert("RGBA")
    datas = img.getdata()

    new_data = list()
    for item in datas:
        if item[3] == 0:  # Transparent remains transparent
            new_data.append((255, 255, 255, 0))
            continue
        gray_factor = item[0] * 0.2126 + item[1] * 0.7152 + item[2] * 0.0722
        if gray_factor > 240:  # Almost white
            new_data.append((255, 255, 255, 0))  # Make transparent
        else:
            new_data.append((255, 255, 255, int(255 - gray_factor)))  # Make white

    img.putdata(new_data)
    img.save(android_notification_icon_filename, "PNG")


def get_translation_strings():
    strings_map = dict()
    with open(os.path.join(SRC_RES_DIR, "values", "allstr.xml"), 'r+') as f:
        s = f.read()
        for i in re.findall('<string name="(.*)</string', s):
            v = i.split('">')
            strings_map[v[1]] = v[0]
    return strings_map


def get_action(item):
    if item.get("click"):
        action_type = "null"
        action = quoted_str_or_null(item["click"])
    elif item.get("tag"):
        action_type = quoted_str_or_null("click")
        action = quoted_str_or_null(sha256_hash(item["tag"]))
    elif item.get("action"):
        action_type = quoted_str_or_null(item["action_type"])
        if item["action_type"] in ("action", "click"):
            action = quoted_str_or_null(sha256_hash(item["action"]))
        else:
            action = quoted_str_or_null(item["action"])
    else:
        logging.error(item)
        raise Exception('Unknown homescreen item')

    return action, action_type


def generate_navigation_menu(doc, strings_map):
    app_type = doc.get('APP_CONSTANTS')['APP_TYPE']
    homescreen_items = doc['HOMESCREEN'].get('items', [])
    if app_type == 'cityapp' and not homescreen_items:
        raise Exception('No homescreen items are specified in build.yaml')

    if app_type == 'cityapp' and 'TOOLBAR' not in doc:
        raise Exception('The build.yaml for this app should be migrated first')

    output = u'''%(LICENSE)s

package com.mobicage.rpc.config;

import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.ServiceBoundActivity;

public class NavigationConstants {

    public static ServiceBoundActivity.NavigationItem[] getNavigationItems() {
        return new ServiceBoundActivity.NavigationItem[]{''' % dict(LICENSE=LICENSE)

    for i, item in enumerate(homescreen_items):
        icon_file_name = "menu_%s.png" % (i)
        source_file = os.path.join(APP_DIR, "build", icon_file_name)
        app_utils.download_icon(item["icon"], "#FFFFFF", 512, source_file)
        foreground_image = Image.open(source_file)

        background_image = Image.new('RGBA', (1024, 1024))
        draw = ImageDraw.Draw(background_image)
        draw.ellipse((124, 124, 900, 900), fill="#%s" % doc["HOMESCREEN"]["color"],
                     outline="#%s" % doc["HOMESCREEN"]["color"])

        background_image.paste(foreground_image, (262, 262), mask=foreground_image)
        background_image.save(source_file)
        generate_resource_images(source_file, 0.2, 1)

        action, action_type = get_action(item)
        output += '''
            new ServiceBoundActivity.NavigationItem(R.drawable.menu_%(i)s, %(action_type)s, %(action)s, R.string.%(string_id)s, %(collapse)s),''' % dict(
            i=i,
            action_type=action_type,
            action=action,
            string_id=strings_map[item['text']],
            collapse=bool_str(item.get('collapse', False)))

    output += '''
        };
    }

    public static ServiceBoundActivity.NavigationItem[] getNavigationFooterItems() {
        return new ServiceBoundActivity.NavigationItem[]{'''

    if doc.get('TOOLBAR') and doc['TOOLBAR'].get('items'):
        for i, item in enumerate(doc['TOOLBAR']['items']):
            icon_name = item["icon"].replace("-", "_").replace("fa_", "faw_")
            action, action_type = get_action(item)
            output += '''
                new ServiceBoundActivity.NavigationItem(FontAwesome.Icon.%(icon_name)s, %(action_type)s, %(action)s, R.string.%(string_id)s, %(collapse)s),''' % dict(
                    icon_name=icon_name,
                    action_type=action_type,
                    action=action,
                    string_id=strings_map[item['text']] if item.get('text') else u"app_name",
                    collapse=bool_str(item.get('collapse', False)))

    output += '''
        };
    }
}
'''

    path = os.path.join(SRC_JAVA_DIR, "com", "mobicage", "rpc", "config")
    if not os.path.exists(path):
        os.makedirs(path)

    with open(os.path.join(path, "NavigationConstants.java"), 'w+') as f:
        f.write(output.encode('utf-8'))


# This function is not executed in case the app is Rogerthat
def convert_config():
    path = os.path.join(SRC_JAVA_DIR, "com", "mobicage", "rogerthat")
    if not os.path.exists(path):
        os.makedirs(path)

    main_screen_contains_friends = False
    main_screen_contains_profile = False
    main_screen_contains_scan = False
    main_screen_contains_services = False
    main_screen_contains_news = False

    SERVICE_TYPES = ["services", "community_services", "merchants", "associations", "emergency_services"]

    add_translations(doc)

    ##### HOMESCREEN #############################################
    if doc["HOMESCREEN"].get("style") == HOME_SCREEN_STYLE_MESSAGING or \
        doc["HOMESCREEN"].get("style") == HOME_SCREEN_STYLE_NEWS:
        print "Not generating homescreen images"
        items = doc['HOMESCREEN'].get('items', [])
        toolbar = doc.get('TOOLBAR')
        if toolbar:
            items += toolbar.get('items', [])
        main_screen_contains_news = any((i for i in items if i['click'] == 'news'))
    else:
        color = doc["HOMESCREEN"]["color"]

        output = u'''%(LICENSE)s

    package com.mobicage.rogerthat;

    import android.view.View;

    import com.mobicage.rogerth.at.R;
    import com.mobicage.rogerthat.util.system.SafeViewOnClickListener;
    import com.mobicage.rpc.config.AppConstants;

    public class HomeActivity extends AbstractHomeActivity {

        @Override
        ItemDef[] getItemDefs() {
            return new ItemDef[] {
    ''' % dict(LICENSE=LICENSE)

        for item in doc["HOMESCREEN"].get("items", []):
            icon_file_name = "menu_%sx%s.png" % (item["position"][0], item["position"][1])

            source_file = os.path.join(APP_DIR, "build", icon_file_name)
            app_utils.download_icon(item["icon"], item.get("color", color), 512, source_file)

            if doc["APP_CONSTANTS"]["APP_TYPE"] == APP_TYPE_CITYPAPP:
                image_width = 0.15
            elif doc["APP_CONSTANTS"]["APP_TYPE"] == APP_TYPE_ENTERPRISE:
                image_width = 0.20
            else:
                raise Exception("Could not generate homescreen item for app_type %s" % doc["APP_CONSTANTS"]["APP_TYPE"])
            generate_resource_images(source_file, image_width, 1)

            output += '''
            new ItemDef(R.id.icon_%(x)sx%(y)s, R.id.label_%(x)sx%(y)s, R.string.%(string_id)s,
                new SafeViewOnClickListener() {
                    @Override
                    public void safeOnClick(View v) {
''' % dict(x=item["position"][0],
           y=item["position"][1],
           string_id=strings_map.get(item["text"]))

            output += '                        '
            if 'coords' in item:
                output += 'simulateMenuItemPress(AppConstants.APP_EMAIL, new long[] { %s });' % (', '.join(map(str, item["coords"])))
            else:
                action, action_type = get_action(item)
                output += 'goToActivity(new ServiceBoundActivity.NavigationItem(R.drawable.menu_0, %(action_type)s, %(action)s, R.string.%(string_id)s, %(collapse)s));' % dict(
                    action_type=action_type,
                    action=action,
                    string_id=strings_map[item['text']],
                    collapse=bool_str(item.get('collapse', False)))

                if item["click"] == "friends":
                    main_screen_contains_friends = True
                elif item["click"] == "profile":
                    main_screen_contains_profile = True
                elif item["click"] == "scan":
                    main_screen_contains_scan = True
                elif item["click"] == "news":
                    main_screen_contains_news = True
                elif item["click"] in SERVICE_TYPES:
                    main_screen_contains_services = True
            output += '''\n                    }
                }
            ),
'''

        output += '''};
    }
}
'''

        with open(os.path.join(path, "HomeActivity.java"), 'w+') as f:
            f.write(output.encode('utf-8'))

    ##### RESIZE IMAGES ################################

    full_width_headers = doc['APP_CONSTANTS'].get('FULL_WIDTH_HEADERS', False)
    headers_ratio = (330.0 / 960.0) if full_width_headers else (240.0 / 840.0)

    path = os.path.join(SRC_RES_DIR, "drawable", "registration_logo.png")
    if os.path.exists(path):
        source_file = os.path.join(APP_DIR, "build", "registration_logo.png")
        shutil.copy2(path, source_file)
        generate_resource_images(source_file, 0.75, headers_ratio)

    path = os.path.join(SRC_RES_DIR, "drawable", "homescreen_watermark.png")
    if os.path.exists(path):
        source_file = os.path.join(APP_DIR, "build", "homescreen_watermark.png")
        shutil.copy2(path, source_file)
        generate_resource_images(source_file, 0.75, 1)

    path = os.path.join(SRC_RES_DIR, "drawable", "homescreen_header.png")
    if os.path.exists(path):
        source_file = os.path.join(APP_DIR, "build", "homescreen_header.png")
        shutil.copy2(path, source_file)
        generate_resource_images(source_file, 0.75, headers_ratio)

    path = os.path.join(SRC_RES_DIR, "drawable", "about_header.png")
    if os.path.exists(path):
        source_file = os.path.join(APP_DIR, "build", "about_header.png")
        shutil.copy2(path, source_file)
        generate_resource_images(source_file, 0.75, headers_ratio)

    path = os.path.join(SRC_RES_DIR, "drawable", "about_footer.png")
    if os.path.exists(path):
        source_file = os.path.join(APP_DIR, "build", "about_footer.png")
        shutil.copy2(path, source_file)
        generate_resource_images(source_file, 0.75, 161.0 / 840.0)

    source_file = os.path.join(APP_DIR, "build", "homescreen_footer.png")
    path = os.path.join(SRC_RES_DIR, "drawable", "homescreen_footer.png")
    if os.path.exists(path):
        shutil.copy2(path, source_file)
        generate_resource_images(source_file, 0.75, 180.0 / 960.0)

    ##### STRINGS ###########################################

    output = '<?xml version="1.0" encoding="utf-8"?>\n<resources>\n'
    for key in doc["STRINGS"]:
        output += '    <string name="%s">' % key["name"]
        output += key["value"].replace("\n", "\\n") \
            .replace("'", "\\'") \
            .replace('"', '\\"') \
            .replace("&", "&amp;") \
            .replace("<", "&lt;") \
            .replace(">", "&gt;")
        output += "</string>\n"
    output += "</resources>\n"

    path = os.path.join(SRC_RES_DIR, "values")
    if not os.path.exists(path):
        os.makedirs(path)

    with open(os.path.join(path, "appstr.xml"), 'w+') as f:
        f.write(output)

    ##### APP CONSTANTS #####################################

    facebook_app_id = doc["APP_CONSTANTS"]["FACEBOOK_APP_ID"]
    if facebook_app_id:
        output = u"""<?xml version="1.0" encoding="utf-8"?>\n<resources>\n"""
        output += '    <string name="facebook_app_id">'
        output += "%s" % facebook_app_id
        output += "</string>\n"
        output += "</resources>\n"

        path = os.path.join(SRC_RES_DIR, "values")
        if not os.path.exists(path):
            os.makedirs(path)

        with open(os.path.join(path, "fb.xml"), 'w+') as f:
            f.write(output.encode('utf-8'))

    if doc["APP_CONSTANTS"]["APP_TYPE"] == APP_TYPE_ROGERTHAT:
        app_type = "APP_TYPE_ROGERTHAT"
        home_activity = "R.layout.messaging"
        show_profile_in_more = "true"
        show_scan_in_more = "false"
        services_enabled = "true"

    elif doc["APP_CONSTANTS"]["APP_TYPE"] == APP_TYPE_CITYPAPP:
        app_type = "APP_TYPE_CITYAPP"
        home_activity = "R.layout.homescreen_3x3_with_qr_code"
        show_profile_in_more = bool_str(not main_screen_contains_profile)
        show_scan_in_more = bool_str(not main_screen_contains_scan)
        services_enabled = bool_str(main_screen_contains_services)

    elif doc["APP_CONSTANTS"]["APP_TYPE"] == APP_TYPE_ENTERPRISE:
        app_type = "APP_TYPE_ENTERPRISE"
        home_activity = "R.layout.homescreen_2x3"
        show_profile_in_more = bool_str(not main_screen_contains_profile)
        show_scan_in_more = bool_str(not main_screen_contains_scan)
        services_enabled = bool_str(main_screen_contains_services)

    elif doc["APP_CONSTANTS"]["APP_TYPE"] == APP_TYPE_CONTENT_BRANDING:
        app_type = "APP_TYPE_CONTENT_BRANDING"
        home_activity = "R.layout.messaging"
        show_profile_in_more = "true"
        show_scan_in_more = "true"
        services_enabled = "true"

        if not doc['CLOUD_CONSTANTS'].get("USE_XMPP_KICK_CHANNEL", False):
            raise Exception("XMPP_KICK_CHANNEL must be enabled for content_branding")

    elif doc["APP_CONSTANTS"]["APP_TYPE"] == APP_TYPE_YSAAA:
        app_type = "APP_TYPE_YSAAA"
        home_activity = "R.layout.messaging"
        show_profile_in_more = "false"
        show_scan_in_more = "false"
        services_enabled = "true"

    else:
        raise Exception("There is no app_type defined")

    home_screen_style = doc['HOMESCREEN']['style']

    if home_screen_style == HOME_SCREEN_STYLE_MESSAGING:
        home_activity = "R.layout.messaging"
    elif home_screen_style == HOME_SCREEN_STYLE_NEWS:
        home_activity = "R.layout.news"
    elif home_screen_style == HOME_SCREEN_STYLE_2X3:
        home_activity = "R.layout.homescreen_2x3"
    elif home_screen_style == HOME_SCREEN_STYLE_3X3:
        if doc['HOMESCREEN'].get('show_qr_code', doc["APP_CONSTANTS"]["APP_TYPE"] == APP_TYPE_CITYPAPP):
            home_activity = "R.layout.homescreen_3x3_with_qr_code"
        else:
            home_activity = "R.layout.homescreen_3x3"

    homescreen_qrcode_header_text = doc["HOMESCREEN"].get("qrcode_header", "loyalty_card_description")

    friends_enabled = bool_str(doc["APP_CONSTANTS"].get("FRIENDS_ENABLED", True))
    friends_caption = doc["APP_CONSTANTS"].get("FRIENDS_CAPTION", None)
    if friends_caption is None:
        if doc["APP_CONSTANTS"]["APP_TYPE"] == APP_TYPE_ENTERPRISE:
            friends_caption = FRIENDS_CAPTION_COLLEAGUES
        else:
            friends_caption = FRIENDS_CAPTION_FRIENDS
    friends_caption_enum = FRIENDS_CAPTION_ENUMS[friends_caption]

    show_friends_in_more = bool_str(not main_screen_contains_friends) if friends_enabled == "true" else "false"
    show_homescreen_footer = bool_str(doc["HOMESCREEN"].get("footer", False))
    app_email = quoted_str_or_null(doc["APP_CONSTANTS"]["APP_EMAIL"])
    app_service_guid = quoted_str_or_null(doc["APP_CONSTANTS"].get("APP_SERVICE_GUID"))
    fb_app_id = quoted_str_or_null(doc["APP_CONSTANTS"]["FACEBOOK_APP_ID"])
    fb_registration = bool_str(doc["APP_CONSTANTS"]["FACEBOOK_REGISTRATION"])
    full_width_headers = bool_str(full_width_headers)

    profile_settings = doc.get('PROFILE', dict())
    profile_data_fields = ','.join(['"%s"' % s for s in profile_settings.get('DATA_FIELDS', [])])
    profile_show_gender_and_birthdate = bool_str(profile_settings.get('SHOW_GENDER_AND_BIRTHDATE', "true"))


    if doc["APP_CONSTANTS"]["APP_TYPE"] == APP_TYPE_CITYPAPP:
        default_about_website = "www.onzestadapp.be"
        default_about_website_url = "http://www.onzestadapp.be"
        default_about_email = "info@onzestadapp.be"
        default_about_twitter = "@onzestadapp"
        default_about_twitter_url = "https://twitter.com/onzestadapp"
        default_about_facebook = "/onzestadapp"
        default_about_facebook_url = "https://www.facebook.com/onzestadapp"
    else:
        default_about_website = "www.rogerthat.net"
        default_about_website_url = "http://www.rogerthat.net"
        default_about_email = "info@mobicage.com"
        default_about_twitter = "@rogerthat"
        default_about_twitter_url = "https://twitter.com/rogerthat"
        default_about_facebook = "/rogerthatplatform"
        default_about_facebook_url = "https://www.facebook.com/rogerthatplatform"

    about_website = doc.get("ABOUT_ACTIVITY", {}).get('website', default_about_website)
    about_website_url = doc.get("ABOUT_ACTIVITY", {}).get('website_url', default_about_website_url)
    about_email = doc.get("ABOUT_ACTIVITY", {}).get('email', default_about_email)
    about_twitter = doc.get("ABOUT_ACTIVITY", {}).get('twitter', default_about_twitter)
    about_twitter_url = doc.get("ABOUT_ACTIVITY", {}).get('twitter_url', default_about_twitter_url)
    about_facebook = doc.get("ABOUT_ACTIVITY", {}).get('facebook', default_about_facebook)
    about_facebook_url = doc.get("ABOUT_ACTIVITY", {}).get('facebook_url', default_about_facebook_url)

    speech_to_text = bool_str(doc["APP_CONSTANTS"].get("SPEECH_TO_TEXT", False))
    secure_app = bool_str(doc["APP_CONSTANTS"].get("SECURE_APP", False))
    secure_pin_interval = str(doc["APP_CONSTANTS"].get("SECURE_PIN_INTERVAL", 900))
    secure_pin_retry_interval = str(doc["APP_CONSTANTS"].get("SECURE_PIN_RETRY_INTERVAL", 300))

    if doc["APP_CONSTANTS"].get("SECURE_APP", False):
        rogerthat_build_gradle = os.path.join(ANDROID_SRC_DIR, '..', 'build.gradle')
        with open(rogerthat_build_gradle, 'r+') as f:
            s = f.read()
            s = re.sub('minSdkVersion \d+', 'minSdkVersion 23', s)

            f.seek(0)
            f.write(s)
            f.truncate()

    registration_type = long(doc['APP_CONSTANTS'].get('REGISTRATION_TYPE', 1))
    if registration_type == 1:
        registration_type = 'REGISTRATION_TYPE_DEFAULT'
        registration_type_oauth_domain = 'dummy'
    elif registration_type == 2:
        registration_type = 'REGISTRATION_TYPE_OAUTH'
        registration_type_oauth_domain = doc['APP_CONSTANTS']['REGISTRATION_TYPE_OAUTH_DOMAIN']
    else:
        raise Exception('Invalid registration type %d' % registration_type)

    registration_asks_location_permission = bool_str(doc['APP_CONSTANTS'].get('REGISTRATION_ASKS_LOCATION_PERMISSION', True))

    output = u'''%(LICENSE)s

package com.mobicage.rpc.config;

import com.mobicage.rogerth.at.R;

public class AppConstants {
    static final int APP_TYPE_ROGERTHAT = 0;
    static final int APP_TYPE_CITYAPP = 1;
    static final int APP_TYPE_ENTERPRISE = 2;
    static final int APP_TYPE_CONTENT_BRANDING = 3;
    static final int APP_TYPE_YSAAA = 4;
    static int getAppType() {
        return %(app_type)s;
    };

    public static final int REGISTRATION_TYPE_DEFAULT = 1;
    public static final int REGISTRATION_TYPE_OAUTH = 2;
    public static final String REGISTRATION_TYPE_OAUTH_DOMAIN = "%(registration_type_oauth_domain)s";
    public static int getRegistrationType() {
        return %(registration_type)s;
    };

    // Customized by App flavor
    public static final String APP_ID = "%(app_id)s";
    public static final int HOME_ACTIVITY_LAYOUT = %(home_activity)s;
    public static final int HOMESCREEN_QRCODE_HEADER = R.string.%(homescreen_qrcode_header_text)s;
    public static final boolean SHOW_HOMESCREEN_FOOTER = %(show_homescreen_footer)s;
    public static final String FACEBOOK_APP_ID = %(fb_app_id)s;
    public static final boolean FACEBOOK_REGISTRATION = %(fb_registration)s;
    public static final String APP_EMAIL = %(app_email)s;
    public static final String APP_SERVICE_GUID = %(app_service_guid)s;
    public static final boolean FRIENDS_ENABLED = %(friends_enabled)s;
    public static final boolean SERVICES_ENABLED = %(services_enabled)s;
    public static final boolean NEWS_ENABLED = %(news_enabled)s;
    public static final FriendsCaption FRIENDS_CAPTION = %(friends_caption_enum)s;
    public static final boolean SHOW_FRIENDS_IN_MORE = %(show_friends_in_more)s;
    public static final boolean SHOW_PROFILE_IN_MORE = %(show_profile_in_more)s;
    public static final boolean SHOW_SCAN_IN_MORE = %(show_scan_in_more)s;
    public static final boolean FULL_WIDTH_HEADERS = %(full_width_headers)s;

    public static final boolean REGISTRATION_ASKS_LOCATION_PERMISSION = %(registration_asks_location_permission)s;

    public static final String[] PROFILE_DATA_FIELDS = new String[] { %(profile_data_fields)s };
    public static final boolean PROFILE_SHOW_GENDER_AND_BIRTHDATE = %(profile_show_gender_and_birthdate)s;

    public static final String ABOUT_WEBSITE = "%(about_website)s";
    public static final String ABOUT_WEBSITE_URL = "%(about_website_url)s";
    public static final String ABOUT_EMAIL = "%(about_email)s";
    public static final String ABOUT_TWITTER = "%(about_twitter)s";
    public static final String ABOUT_TWITTER_URL = "%(about_twitter_url)s";
    public static final String ABOUT_FACEBOOK = "%(about_facebook)s";
    public static final String ABOUT_FACEBOOK_URL = "%(about_facebook_url)s";

    public static final boolean SPEECH_TO_TEXT = %(speech_to_text)s;
    public static final boolean SECURE_APP = %(secure_app)s;
    public static final int SECURE_PIN_INTERVAL = %(secure_pin_interval)s;
    public static final int SECURE_PIN_RETRY_INTERVAL = %(secure_pin_retry_interval)s;
}
''' % dict(LICENSE=LICENSE,
           app_type=app_type,
           app_id=APP_ID,
           home_activity=home_activity,
           homescreen_qrcode_header_text=homescreen_qrcode_header_text,
           show_homescreen_footer=show_homescreen_footer,
           fb_app_id=fb_app_id,
           fb_registration=fb_registration,
           app_email=app_email,
           friends_enabled=friends_enabled,
           services_enabled=services_enabled,
           news_enabled=bool_str(main_screen_contains_news),
           friends_caption_enum=friends_caption_enum,
           show_friends_in_more=show_friends_in_more,
           show_profile_in_more=show_profile_in_more,
           show_scan_in_more=show_scan_in_more,
           full_width_headers=full_width_headers,
           profile_data_fields=profile_data_fields,
           profile_show_gender_and_birthdate=profile_show_gender_and_birthdate,
           about_website=about_website,
           about_website_url=about_website_url,
           about_email=about_email,
           about_twitter=about_twitter,
           about_twitter_url=about_twitter_url,
           about_facebook=about_facebook,
           about_facebook_url=about_facebook_url,
           speech_to_text=speech_to_text,
           secure_app=secure_app,
           secure_pin_interval=secure_pin_interval,
           secure_pin_retry_interval=secure_pin_retry_interval,
           app_service_guid=app_service_guid,
           registration_type=registration_type,
           registration_type_oauth_domain=registration_type_oauth_domain,
           registration_asks_location_permission=registration_asks_location_permission)

    path = os.path.join(SRC_JAVA_DIR, "com", "mobicage", "rpc", "config")
    if not os.path.exists(path):
        os.makedirs(path)

    with open(os.path.join(path, "AppConstants.java"), 'w+') as f:
        f.write(output.encode('utf-8'))

    ##### COLORS ############################################

    path = os.path.join(SRC_RES_DIR, "values")
    if not os.path.exists(path):
        os.makedirs(path)

    colors = dict(mc_homescreen_background='homescreen_background',
                  mc_homescreen_text='homescreen_text',
                  mc_primary_color='primary_color',
                  mc_secondary_color='secondary_color',
                  mc_primary_icon='primary_color')
    with open(os.path.join(path, "colors.xml"), 'r+') as f:
        s = f.read()
        for mc_color_name, color_name in colors.iteritems():
            s = re.sub('<color name="%s">(.*)</color>' % mc_color_name,
                       '<color name="%s">#%s</color>' % (mc_color_name, doc["COLORS"][color_name]),
                       s)

        s = re.sub('<color name="mc_primary_color_dark">(.*)</color>',
                   '<color name="mc_primary_color_dark">%s</color>' % app_utils.colorscale(doc["COLORS"]["primary_color"],
                                                                                           0.6),
                   s)

        f.seek(0)
        f.write(s)
        f.truncate()

    ##### TRUSTSORE ########################################

    if doc["CLOUD_CONSTANTS"]["USE_TRUSTSTORE"]:
        app_utils.create_trusstore(APP_ID, os.path.join(ANDROID_SRC_DIR, "main", "assets", "truststore.bks"))


    ##### HOMESCREEN QR AREA BACKGROUND ####################

    if show_homescreen_footer:
        app_utils.create_background(os.path.join(APP_DIR, "build", "homescreen_footer.png"),
                                    os.path.join(SRC_RES_DIR, "drawable", "homescreen_qr_area_background.png"))


def encode_translation(s):
    return s.replace("\n", "\\n") \
        .replace("'", "\\'") \
        .replace('\r', '') \
        .replace('"', '\\"') \
        .replace("&", "&amp;") \
        .replace("<", "&lt;") \
        .replace(">", "&gt;").encode('utf-8')


def add_translations(doc):
    translations = doc.get('TRANSLATIONS')
    if not translations:
        return

    for language, entries in translations.iteritems():
        values_dir = 'values'
        if language != 'en':
            values_dir += '-' + language
        xml_path = os.path.join(SRC_RES_DIR, values_dir, 'allstr.xml')
        added_lines = list()
        for entry in entries:
            added_lines.append('    <string name="%s">%s</string>' % (entry['name'],
                                                                      encode_translation(entry['value'])))
        if added_lines:
            added_lines.insert(0, '    <!-- Extra translations added via build.yaml -->')
            with open(xml_path, 'rb+') as all_str_f:
                s = all_str_f.read()
                all_str_f.seek(0)
                all_str_f.write(s.replace('</resources>', '%s\n</resources>' % '\n'.join(added_lines)))


def generate_custom_cloud_constants(doc):
    ##### CLOUD CONSTANTS ###################################
    params = dict(LICENSE=LICENSE)
    params.update(doc['CLOUD_CONSTANTS'])
    params['USE_TRUSTSTORE'] = bool_str(params["USE_TRUSTSTORE"])
    params['USE_XMPP_KICK_CHANNEL'] = bool_str(params.get("USE_XMPP_KICK_CHANNEL", False))
    params['GCM_SENDER_ID'] = quoted_str_or_null(params.get('GCM_SENDER_ID', None))
    params['REGISTRATION_MAIN_SIGNATURE'] = quoted_str_or_null(doc['APP_CONSTANTS']['REGISTRATION_MAIN_SIGNATURE'])
    params['REGISTRATION_EMAIL_SIGNATURE'] = quoted_str_or_null(doc['APP_CONSTANTS']['REGISTRATION_EMAIL_SIGNATURE'])
    params['REGISTRATION_PIN_SIGNATURE'] = quoted_str_or_null(doc['APP_CONSTANTS']['REGISTRATION_PIN_SIGNATURE'])
    params['EMAIL_HASH_ENCRYPTION_KEY'] = quoted_str_or_null(doc['APP_CONSTANTS']['EMAIL_HASH_ENCRYPTION_KEY'])
    output = u'''%(LICENSE)s

package com.mobicage.rpc.config;

// Warning: DO NOT MODIFY & COMMIT THIS FILE !!!

// Package-private class
class CustomCloudConstants {

    // Port can be added e.g. HTTP_BASE_URL = "http://10.100.5.1:8080";

    final static String HTTPS_BASE_URL = "%(HTTPS_BASE_URL)s";
    final static int HTTPS_PORT = %(HTTPS_PORT)s;
    final static String HTTP_BASE_URL = "%(HTTP_BASE_URL)s";

    final static boolean USE_XMPP_KICK_CHANNEL = %(USE_XMPP_KICK_CHANNEL)s; // when false, kicks will arrive via GCM
    final static String GCM_SENDER_ID = %(GCM_SENDER_ID)s;

    final static String XMPP_DOMAIN = "%(XMPP_DOMAIN)s";
    final static boolean USE_TRUSTSTORE = %(USE_TRUSTSTORE)s;
    final static boolean XMPP_MUST_VALIDATE_SSL_CERTIFICATE = true;
    final static boolean XMPP_DEBUG = false;
    final static boolean DEBUG_LOGGING = false;

    final static boolean NEWS_CHANNEL_SSL = true;
    final static boolean NEWS_CHANNEL_MUST_VALIDATE_SSL_CERTIFICATE = true;

    final static String REGISTRATION_MAIN_SIGNATURE = %(REGISTRATION_MAIN_SIGNATURE)s;
    final static String REGISTRATION_EMAIL_SIGNATURE = %(REGISTRATION_EMAIL_SIGNATURE)s;
    final static String REGISTRATION_PIN_SIGNATURE = %(REGISTRATION_PIN_SIGNATURE)s;
    final static String EMAIL_HASH_ENCRYPTION_KEY = %(EMAIL_HASH_ENCRYPTION_KEY)s;

}
''' % params
    path = os.path.join(SRC_JAVA_DIR, "com", "mobicage", "rpc", "config")
    if not os.path.exists(path):
        os.makedirs(path)
    with open(os.path.join(path, "CustomCloudConstants.java"), 'w+') as f:
        f.write(output.encode('utf-8'))


def resize_more_icon(f, name):
    for drawable_folder, size in ICON_SIZES.iteritems():
        dest = os.path.join(SRC_RES_DIR, drawable_folder, 'more_%s.png' % name)
        app_utils.resize_image(f, dest, int(size * 0.7), int(size * 0.7))
        app_utils.increase_canvas(dest, dest, size, size)


def validate_android_manifest():
    manifest_file = os.path.join(ANDROID_SRC_DIR, 'main', 'AndroidManifest.xml')
    xml = minidom.parse(manifest_file)

    missing_activities = list()
    for activity in xml.getElementsByTagName('activity'):
        activity_name = activity.getAttribute('android:name')
        if activity_name.startswith('com.mobicage'):
            file_path = os.path.realpath(os.path.join(SRC_JAVA_DIR, '%s.java' % activity_name.replace('.', '/')))
            if not os.path.exists(file_path):
                missing_activities.append(file_path)

    if missing_activities:
        raise Exception("There are activities defined in AndroidManifest.xml which don't exist on disk:\n- %s" % \
                        "\n- ".join(missing_activities))


##### START ########################################

if __name__ == "__main__":
    if len(sys.argv) != 2:
        raise Exception("app_id is a required argument")

    validate_android_manifest()

    APP_ID = sys.argv[1]
    logging.info('APP ID: %s', APP_ID)

    APP_DIR = os.path.join(APPS_REPO_DIR, APP_ID)

    if not os.path.exists(APP_DIR):
        raise Exception("app_id not in valid app ids")

    #### IMAGES ###################################

    src_dir_images_0 = os.path.join(APP_DIR, 'images', 'common')
    to_dir_images_0 = os.path.join(SRC_RES_DIR, 'drawable')
    if os.path.exists(src_dir_images_0):
        app_utils.copytree(src_dir_images_0, to_dir_images_0, ignore=lambda p, f: ['localized'])
        localized_path = os.path.join(src_dir_images_0, 'localized')
        if os.path.exists(localized_path):
            for dir_name in os.listdir(localized_path):
                d = os.path.join(localized_path, dir_name)
                for file_name in os.listdir(d):
                    if dir_name == 'en':
                        shutil.copyfile(os.path.join(d, file_name), os.path.join(to_dir_images_0, file_name))
                    else:
                        target_dir = os.path.realpath(os.path.join(SRC_RES_DIR, 'drawable-' + dir_name))
                        if not os.path.exists(target_dir):
                            os.mkdir(target_dir)
                        shutil.copyfile(os.path.join(d, file_name), os.path.join(target_dir, file_name))

    src_dir_images_1 = os.path.join(APP_DIR, 'images', 'android')
    to_dir_images_1 = os.path.join(SRC_RES_DIR, 'drawable')
    if os.path.exists(src_dir_images_1):
        app_utils.copytree(src_dir_images_1, to_dir_images_1)

    itunes_artwork = os.path.join(APP_DIR, "images", "iTunesArtwork.png")
    android_icon = os.path.join(APP_DIR, "images", "android_icon.png")
    for drawable_folder, size in LAUNCHER_ICON_SIZES.iteritems():
        app_utils.resize_image(android_icon,
                               os.path.join(SRC_RES_DIR, drawable_folder, 'ic_launcher.png'),
                               size, size)
    for drawable_folder, size in ICON_SIZES.iteritems():
        app_utils.resize_image(itunes_artwork,
                               os.path.join(SRC_RES_DIR, drawable_folder, 'ic_dashboard.png'),
                               size, size)

    ##### NOTIFICATION ICONS ################################
    tmp_file = tempfile.NamedTemporaryFile(delete=False)
    tmp_file_name = tmp_file.name
    tmp_file.close()

    create_notification_icon(android_icon, tmp_file_name)
    for screen_type, icon_size in NOTIFICATION_ICON_SIZES.iteritems():
        im = Image.open(tmp_file_name)
        im.thumbnail((icon_size, icon_size), Image.ANTIALIAS)
        im.save(os.path.join(SRC_RES_DIR, screen_type, 'notification_icon.png'), "PNG")

    os.remove(tmp_file_name)

    with open(os.path.join(APP_DIR, "build.yaml"), 'r') as f:
        doc = yaml.load(f)

    logging.info('BUILD CFG:')
    logging.info(pprint.pformat(doc))

    strings_map = get_translation_strings()
    if doc.get('TRANSLATIONS'):
        for entry in doc['TRANSLATIONS']['en']:
            strings_map[entry['value']] = entry['name']
    generate_navigation_menu(doc, strings_map)

    if APP_ID != MAIN_APP_ID:
        convert_config()
        rename_package()
    else:
        logging.info('app_id was rogerthat, only limited prepare is needed')
    generate_custom_cloud_constants(doc)

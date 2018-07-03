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

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;

import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.NavigationItem;
import com.mobicage.rogerthat.util.IOUtils;
import com.mobicage.rogerthat.util.TextUtils;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SystemUtils;
import com.mobicage.rogerthat.util.ui.UIUtils;
import com.mobicage.to.app.LookAndFeelTO;
import com.mobicage.to.app.NavigationItemTO;

import org.json.simple.JSONValue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

public class LookAndFeelConstants {

    private static boolean sNeedsSetup = true;

    private static int sHomeActivityLayout;
    private static NavigationItem[] sDrawerNavigationItems;
    private static NavigationItem[] sFooterNavigationItems;
    private static int sPrimaryColor;
    private static int sPrimaryColorDark;
    private static int sPrimaryIconColor;

    private static File lookAndFeelDirectory(Context context) throws IOException {
        File file = IOUtils.getFilesDirectory(context);
        file = new File(file, "lookandfeel");
        IOUtils.createDirIfNotExists(context, file);
        return file;
    }

    private static File lookAndFeelSettingsFile(Context context) throws IOException {
        return new File(lookAndFeelDirectory(context), "settings.json");
    }

    private static NavigationItem createNavigationItem(NavigationItemTO itemTO, String iconColorString) {
        FontAwesome.Icon icon = UIUtils.getIcon(itemTO.icon);
        if (icon == null) {
            icon = FontAwesome.Icon.faw_question;
        }
        int iconColor = sPrimaryColor;
        if (itemTO.icon_color != null) {
            iconColor = Color.parseColor(itemTO.icon_color);
        } else if (iconColorString != null) {
            iconColor = Color.parseColor(iconColorString);
        }

        NavigationItem item =  new NavigationItem(icon, itemTO.action_type,
                itemTO.action, itemTO.text, itemTO.service_email, iconColor);

        // for backward compatibility, if params is not set
        // e.g. when loading settings from a file
        // collapse params should be set
        if (TextUtils.isEmptyOrWhitespace(itemTO.params)) {
            item.setParam("collapse", itemTO.collapse);
        } else {
            item.setParams(itemTO.params);
        }

        return item;
    }

    private static NavigationItem[] createNavigationItems(NavigationItemTO[] itemTOs, String iconColorString) {
        NavigationItem[] arr = new NavigationItem[itemTOs.length];
        for (int i=0; i < itemTOs.length; i++) {
            arr[i] = createNavigationItem(itemTOs[i], iconColorString);
        }
        return arr;
    }

    private static void setup(Context context, boolean force) {
        if (!sNeedsSetup && !force) {
            return;
        }

        try {
            File f = lookAndFeelSettingsFile(context);
            if (!f.exists()) {
                setup(context, null);
                return;
            }
            FileInputStream fis = new FileInputStream(f);
            try {
                String json = SystemUtils.convertStreamToString(fis);
                LookAndFeelTO request = new LookAndFeelTO((Map<String, Object>) JSONValue.parse(json));
                setup(context, request);
            } finally {
                fis.close();
            }
        } catch (Exception e) {
            L.e("Failed to buildNavigationItems", e);
            setup(context, null);
        }
    }

    private static void setup(Context context, LookAndFeelTO request) {
        if (request == null) {
            sHomeActivityLayout = AppConstants.HOME_ACTIVITY_LAYOUT;

            sPrimaryColor = ContextCompat.getColor(context, R.color.mc_primary_color);
            sPrimaryColorDark = ContextCompat.getColor(context, R.color.mc_primary_color_dark);
            sPrimaryIconColor = ContextCompat.getColor(context, R.color.mc_primary_icon);

            sDrawerNavigationItems = NavigationConstants.getNavigationItems();
            sFooterNavigationItems = NavigationConstants.getNavigationFooterItems();

        } else {
            if ("news".equals(request.homescreen.style)) {
                sHomeActivityLayout = R.layout.news;
            } else if ("messages".equals(request.homescreen.style)) {
                sHomeActivityLayout = R.layout.messaging;
            } else if ("home_branding".equals(request.homescreen.style)) {
                sHomeActivityLayout = R.layout.home_branding;
            } else {
                sHomeActivityLayout = AppConstants.HOME_ACTIVITY_LAYOUT;
            }
            sPrimaryColor = ContextCompat.getColor(context, R.color.mc_primary_color);
            sPrimaryColorDark = ContextCompat.getColor(context, R.color.mc_primary_color_dark);
            sPrimaryIconColor = ContextCompat.getColor(context, R.color.mc_primary_icon);

            sDrawerNavigationItems = createNavigationItems(request.homescreen.items, request.homescreen.color);
            sFooterNavigationItems = createNavigationItems(request.toolbar.items, request.homescreen.color);
        }
    }

    public static void saveDynamicLookAndFeel(Context context, LookAndFeelTO request) {
        try {
            setup(context, request);
            File file = lookAndFeelSettingsFile(context);
            if (request == null) {
                if (file.exists()) {
                    Boolean fileDeleted = file.delete();
                    L.d("settings.json deleted:" + fileDeleted);
                }
            } else {
                SystemUtils.writeStringToFile(file, JSONValue.toJSONString(request.toJSONMap()));
            }
        } catch (Exception e) {
            L.bug("Failed to saveDynamicLookAndFeel", e);
            setup(context, null);
        }
    }

    public static boolean removeLookAndFeel(Context context) {
        try {
            File file = lookAndFeelSettingsFile(context);
            if (file.exists()) {
                return file.delete();
            }
        } catch (IOException exception) {
            L.e(exception);
        }
        return true;
    }

    public static int getHomeActivityLayout(Context context) {
        setup(context, false);
        return sHomeActivityLayout;
    }

    public static String getAssetKindOfHeaderImage() {
        return "lookandfeel_header_image_url";
    }

    public static NavigationItem[] getNavigationItems(Context context) {
        setup(context, false);
        return sDrawerNavigationItems;
    }

    public static NavigationItem[] getNavigationFooterItems(Context context) {
        setup(context, false);
        return sFooterNavigationItems;
    }

    public static int getPrimaryColor(Context context) {
        setup(context, false);
        return sPrimaryColor;
    }

    public static int getPrimaryColorDark(Context context) {
        setup(context, false);
        return sPrimaryColorDark;
    }

    public static int getPrimaryIconColor(Context context) {
        setup(context, false);
        return sPrimaryIconColor;
    }
}

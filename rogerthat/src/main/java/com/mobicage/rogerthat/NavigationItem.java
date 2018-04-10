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

package com.mobicage.rogerthat;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;

import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;
import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.plugins.news.NewsPlugin;
import com.mobicage.rogerthat.util.TextUtils;
import com.mobicage.rogerthat.util.ui.ImageHelper;

import org.json.simple.JSONValue;

public class NavigationItem {
    public int iconId;
    public FontAwesome.Icon faIcon;
    public String actionType;
    public String action;
    public int labelTextId;
    public String labelText;
    public String serviceEmail;
    public int iconColor;
    public Map<String, Object> params;

    public NavigationItem(FontAwesome.Icon icon, String actionType, String action, int labelTextId) {
        this(0, icon, actionType, action, null, labelTextId, null, 0, null);
    }

    public NavigationItem(FontAwesome.Icon icon, String actionType, String action, String labelText) {
        this(0, icon, actionType, action, labelText, 0, null, 0, null);
    }

    public NavigationItem(FontAwesome.Icon icon, String actionType, String action, int labelTextId,
                          String serviceEmail, int iconColor) {
        this(0, icon, actionType, action, null, labelTextId, serviceEmail, iconColor, null);
    }

    public NavigationItem(FontAwesome.Icon icon, String actionType, String action, String labelText,
                          String serviceEmail, int iconColor) {
        this(0, icon, actionType, action, labelText, 0, serviceEmail, iconColor, null);
    }

    public NavigationItem(FontAwesome.Icon icon, String actionType, String action, String labelText,
                          String serviceEmail, int iconColor, Map<String, Object> params) {
        this(0, icon, actionType, action, labelText, 0, serviceEmail, iconColor, params);
    }

    public NavigationItem(int iconId, String actionType, String action, int labelTextId,
                          String serviceEmail, int iconColor) {
        this(iconId, null, actionType, action, null, labelTextId, serviceEmail, iconColor, null);
    }

    public NavigationItem(int iconId, String actionType, String action, String labelText,
                          String serviceEmail, int iconColor) {
        this(iconId, null, actionType, action, labelText, 0, serviceEmail, iconColor, null);
    }

    public NavigationItem(int iconId, FontAwesome.Icon faIcon, String actionType, String action, String labelText,
                          int textLabelId, String serviceEmail, int iconColor, Map<String, Object> params) {
        super();
        this.iconId = iconId;
        this.faIcon = faIcon;
        this.actionType = actionType;
        this.action = action;
        this.labelTextId = textLabelId;
        this.labelText = labelText;
        this.serviceEmail = serviceEmail;
        this.iconColor = iconColor;
        this.params = params;
    }

    public Drawable getIcon(Context context) {
        if (this.faIcon == null) {
            Bitmap bm = ImageHelper.getRoundedCornerAvatar(BitmapFactory.decodeResource(context.getResources(), this.iconId));
            return new BitmapDrawable(context.getResources(), bm);
        }
        return new IconicsDrawable(context, this.faIcon).color(ContextCompat.getColor(context, R.color.mc_white)).paddingDp(8);
    }

    public Drawable getFooterIcon(Context context) {
        if (this.faIcon == null) {
            return context.getResources().getDrawable(this.iconId);
        }
        return new IconicsDrawable(context, this.faIcon).color(ContextCompat.getColor(context, R.color.mc_white)).sizeDp(20);
    }

    public String actionWithType() {
        if (this.action.equals("news")){
            return NewsPlugin.getFeedKey(this.feedName());
        } else if (this.actionType == null) {
            return this.action;
        } else {
            return this.actionType + "|" + this.action;
        }
    }

    public String getLabel(Context ctx) {
        return this.labelText == null ? ctx.getString(this.labelTextId) : this.labelText;
    }

    public void setParam(String name, Object value) {
        if (this.params == null) {
            this.params = new HashMap<>();
        }
        this.params.put(name, value);
    }

    public Object getParam(String name) {
        return getParam(name, null);
    }

    public Object getParam(String name, Object defaultValue) {
        if (this.params == null || !this.params.containsKey(name)) {
            return defaultValue;
        }
        return this.params.get(name);
    }

    public NavigationItem setParams(Object params) {
        if (params instanceof  Map) {
            this.params = (Map<String, Object>) params;
        } else if (params instanceof String) {
            String jsonParams = (String) params;
            if (!TextUtils.isEmptyOrWhitespace(jsonParams)) {
                this.params = (Map<String, Object>) JSONValue.parse(jsonParams);
            }
        }
        return this;
    }

    public boolean isCollapsible() {
        return (boolean) getParam("collapse", false);
    }

    public String feedName() {
        return (String) getParam("feed_name");
    }
}

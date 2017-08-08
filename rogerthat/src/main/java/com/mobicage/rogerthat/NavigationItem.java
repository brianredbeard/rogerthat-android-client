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

package com.mobicage.rogerthat;

import android.content.Context;
import android.support.v4.content.ContextCompat;

import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;
import com.mobicage.rogerth.at.R;

public class NavigationItem {
    public FontAwesome.Icon icon;
    public String actionType;
    public String action;
    public int labelTextId;
    public String labelText;
    public boolean collapse;
    public String serviceEmail;
    public int iconColor;

    public NavigationItem(FontAwesome.Icon icon, String actionType, String action, int labelTextId, boolean collapse) {
        super();
        this.icon = icon;
        this.actionType = actionType;
        this.action = action;
        this.labelTextId = labelTextId;
        this.labelText = null;
        this.collapse = collapse;
        this.serviceEmail = null;
        this.iconColor = 0;
    }

    public NavigationItem(FontAwesome.Icon icon, String actionType, String action, String labelText, boolean collapse) {
        super();
        this.icon = icon;
        this.actionType = actionType;
        this.action = action;
        this.labelTextId = 0;
        this.labelText = labelText;
        this.collapse = collapse;
        this.serviceEmail = null;
        this.iconColor = 0;
    }

    public NavigationItem(FontAwesome.Icon icon, String actionType, String action, int labelTextId, boolean collapse,
                          String serviceEmail, int iconColor) {
        super();
        this.icon = icon;
        this.actionType = actionType;
        this.action = action;
        this.labelTextId = labelTextId;
        this.labelText = null;
        this.collapse = collapse;
        this.serviceEmail = serviceEmail;
        this.iconColor = iconColor;
    }

    public NavigationItem(FontAwesome.Icon icon, String actionType, String action, String labelText, boolean collapse,
                          String serviceEmail, int iconColor) {
        super();
        this.icon = icon;
        this.actionType = actionType;
        this.action = action;
        this.labelTextId = 0;
        this.labelText = labelText;
        this.collapse = collapse;
        this.serviceEmail = serviceEmail;
        this.iconColor = iconColor;
    }

    public IconicsDrawable getIcon(Context context) {
        return new IconicsDrawable(context, icon).color(ContextCompat.getColor(context, R.color.mc_white)).paddingDp(8);
    }

    public String actionWithType() {
        if (this.actionType == null) {
            return this.action;
        } else {
            return this.actionType + "|" + this.action;
        }
    }

    public String getLabel(Context ctx) {
        return this.labelText == null ? ctx.getString(this.labelTextId) : this.labelText;
    }
}

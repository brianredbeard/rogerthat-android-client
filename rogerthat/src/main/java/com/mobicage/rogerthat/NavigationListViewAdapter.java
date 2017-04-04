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

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.util.ui.UIUtils;
import com.mobicage.rpc.config.LookAndFeelConstants;

class NavigationListViewAdapter extends BaseAdapter {

    private NavigationItem[] mNavigationItems;
    private final ServiceBoundActivity mActivity;

    NavigationListViewAdapter(ServiceBoundActivity activity) {
        mActivity = activity;
    }

    public void setmNavigationItems(NavigationItem[] navigationItems) {
        mNavigationItems = navigationItems;
    }

    @Override
    public int getCount() {
        return mNavigationItems.length;
    }

    @Override
    public Object getItem(int position) {
        return mNavigationItems[position];
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        view = LayoutInflater.from(mActivity).inflate(R.layout.navigation_item, parent, false);
        final NavigationItem navItem = mNavigationItems[position];
        final ImageView iconView = (ImageView) view.findViewById(R.id.icon);
        final TextView titleView = (TextView) view.findViewById(R.id.title);
        final TextView badgeView = (TextView) view.findViewById(R.id.badge);
        final String activityName = mActivity.getActivityName();

        if (navItem.iconColor == 0) {
            navItem.iconColor = LookAndFeelConstants.getPrimaryColor(mActivity);
        }
        UIUtils.setBackgroundColor(iconView, navItem.iconColor);
        iconView.setImageDrawable(navItem.getIcon(mActivity));

        if (navItem.labelText == null) {
            titleView.setText(navItem.labelTextId);
        } else {
            titleView.setText(navItem.labelText);
        }
        badgeView.setTextColor(LookAndFeelConstants.getPrimaryColor(mActivity));
        long badgeCount = mActivity.getBadgeCount(navItem.action);
        if (badgeCount > 0) {
            badgeView.setText(badgeCount > 9 ? "9+" : String.valueOf(badgeCount));
            badgeView.setVisibility(View.VISIBLE);
        }
        boolean isSelected = mActivity.getActivityName() != null && (navItem.actionType == null
                && navItem.action.equals(activityName)
                || navItem.actionType != null && activityName.equals(navItem.actionType + "|" + navItem.action));
        if (isSelected) {
            view.setBackgroundResource(R.color.mc_selected_list_item);
        }
        return view;
    }
}

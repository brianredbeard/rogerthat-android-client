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

package com.mobicage.rogerthat;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import com.mobicage.rogerth.at.R;
import com.mobicage.rpc.config.AppConstants;

public class FriendCategoryActivity extends ServiceFriendsActivity {

    public static final String FRIEND_CATEGORY_ID = "category_id";
    public static final String FRIEND_CATEGORY_NAME = "category_name";
    private String mCategoryId = null;
    private String mCategoryName = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        mCategoryId = intent.getStringExtra(FRIEND_CATEGORY_ID);
        mCategoryName = intent.getStringExtra(FRIEND_CATEGORY_NAME);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onServiceBound() {
        super.onServiceBound();
        setNavigationBarTitle(mCategoryName);
    }

    @Override
    protected void createCursor() {
        setCursor(mFriendsPlugin.getStore().getFriendsByCategoryListCursor(mCategoryId));
    }

    @Override
    protected CharSequence getHeaderCellMainText() {
        return null;
    }

    @Override
    protected CharSequence getHeaderCellSubText() {
        return null;
    }

    @Override
    protected View getHeaderView() {
        if (!AppConstants.SHOW_NAV_HEADER) {
            final View view = getLayoutInflater().inflate(R.layout.title_bar, null);
            TextView textView = (TextView) view.findViewById(R.id.title);
            textView.setText(mCategoryName);
            return view;
        } else {
            return null;
        }
    }

    @Override
    protected void onHeaderTapped() {
        // Do nothing
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Don't show a menu
        return false;
    }

    @Override
    protected boolean showFABMenu() {
        return false;
    }
}
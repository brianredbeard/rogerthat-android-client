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

import android.graphics.Color;
import android.view.Menu;
import android.view.View;

import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;
import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.util.system.T;

public class UserDetailActivity extends FriendDetailActivity {

    @Override
    protected int getServiceAreaVisibility() {
        return View.GONE;
    }

    @Override
    protected int getFriendAreaVisibility() {
        return View.VISIBLE;
    }

    @Override
    protected int getHeaderVisibility() {
        return View.VISIBLE;
    }

    @Override
    protected int getPassportVisibility() {
        return View.VISIBLE;
    }

    @Override
    protected int getMenu() {
        return R.menu.friend_detail_menu;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        T.UI();
        boolean result = super.onCreateOptionsMenu(menu);
        addIconToMenuItem(menu, R.id.remove_friend, FontAwesome.Icon.faw_trash);
        return result;
    }

    @Override
    protected int getUnfriendMessage() {
        return R.string.confirm_remove_friend;
    }

    @Override
    protected int getRemoveFailedMessage() {
        return R.string.friend_remove_failed;
    }

    @Override
    protected int getPokeVisibility() {
        return View.GONE;
    }

}

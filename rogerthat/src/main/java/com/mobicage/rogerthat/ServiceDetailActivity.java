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

import java.util.Map;

import org.jivesoftware.smack.util.Base64;
import org.json.simple.JSONValue;

import android.content.Intent;
import android.graphics.Color;
import android.view.Menu;
import android.view.View;

import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;
import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.plugins.friends.Friend;
import com.mobicage.rogerthat.plugins.friends.FriendsPlugin;
import com.mobicage.rogerthat.util.TextUtils;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SystemUtils;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rpc.IncompleteMessageException;
import com.mobicage.to.service.FindServiceItemTO;

public class ServiceDetailActivity extends FriendDetailActivity {

    public static final String FIND_SERVICE_RESULT = "FIND_SERVICE_RESULT";
    public static final String EXISTENCE = "EXISTENCE";

    private int mExistence = Friend.ACTIVE;
    private Friend mFriend;

    @Override
    protected int getHeaderVisibility() {
        return View.GONE;
    }

    @Override
    protected int getServiceAreaVisibility() {
        return View.VISIBLE;
    }

    @Override
    protected int getFriendAreaVisibility() {
        return View.GONE;
    }

    @Override
    protected int getPassportVisibility() {
        return View.GONE;
    }

    @Override
    protected int getMenu() {
        if (mExistence == Friend.NOT_FOUND)
            return -1;

        if (SystemUtils.isFlagEnabled(mFriend.flags, FriendsPlugin.FRIEND_NOT_REMOVABLE))
            return -1;

        return R.menu.service_detail_menu;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        T.UI();
        boolean result = super.onCreateOptionsMenu(menu);
        menu.getItem(0).setIcon(new IconicsDrawable(this).icon(FontAwesome.Icon.faw_trash).color(Color.DKGRAY).sizeDp(18));
        return result;
    }

    @Override
    protected int getUnfriendMessage() {
        return R.string.confirm_remove_service;
    }

    @Override
    protected int getRemoveFailedMessage() {
        return R.string.service_remove_failed;
    }

    @Override
    protected int getPokeVisibility() {
        if (mExistence == Friend.NOT_FOUND || mExistence == Friend.INVITE_PENDING) {
            return View.VISIBLE;
        }

        return View.GONE;
    }

    @Override
    protected Friend loadFriend(Intent intent) {
        mExistence = intent.getIntExtra(EXISTENCE, Friend.ACTIVE);

        if (intent.hasExtra(FIND_SERVICE_RESULT)) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) JSONValue.parse(intent.getStringExtra(FIND_SERVICE_RESULT));
            try {
                FindServiceItemTO item = new FindServiceItemTO(map);

                Friend service = new Friend();
                service.avatar = Base64.decode(item.avatar);
                service.avatarId = 0;
                service.description = item.description;
                service.descriptionBranding = TextUtils.isEmptyOrWhitespace(item.description_branding) ? null
                        : item.description_branding;
                service.email = item.email;
                service.existenceStatus = mExistence;
                service.name = item.name;
                service.type = FriendsPlugin.FRIEND_TYPE_SERVICE;
                service.qualifiedIdentifier = item.qualified_identifier;
                return service;

            } catch (IncompleteMessageException e) {
                L.bug(e);
                return null;
            }
        }
        mFriend = super.loadFriend(intent);
        return mFriend;
    }

}

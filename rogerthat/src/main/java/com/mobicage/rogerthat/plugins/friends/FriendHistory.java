/*
 * Copyright 2017 Mobicage NV
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
 * @@license_version:1.2@@
 */

package com.mobicage.rogerthat.plugins.friends;

import com.mobicage.rogerthat.MainService;
import com.mobicage.rogerthat.plugins.history.HistoryItem;
import com.mobicage.rogerthat.plugins.history.HistoryPlugin;
import com.mobicage.rogerthat.util.system.T;

public class FriendHistory {

    private final MainService mService;
    private final FriendsPlugin mFriendsPlugin;
    private final HistoryPlugin mHistoryPlugin;

    FriendHistory(FriendsPlugin plugin, MainService service) {
        T.UI();
        mFriendsPlugin = plugin;
        mService = service;
        mHistoryPlugin = mService.getPlugin(HistoryPlugin.class);
    }

    void putAddFriendInHistory(final String friendEmail) {
        final HistoryItem item = new HistoryItem();
        item.timestampMillis = System.currentTimeMillis();
        item.reference = friendEmail;
        item.friendReference = friendEmail;
        item.type = HistoryItem.FRIEND_ADDED;
        item.parameters.put(HistoryItem.PARAM_FRIEND_NAME, mFriendsPlugin.getName(friendEmail));
        mHistoryPlugin.addHistoryItem(item);
    }

    void putRemoveFriendInHistory(final String friendEmail) {
        final HistoryItem item = new HistoryItem();
        item.timestampMillis = System.currentTimeMillis();
        item.reference = friendEmail;
        item.friendReference = friendEmail;
        item.type = HistoryItem.FRIEND_REMOVED;
        item.parameters.put(HistoryItem.PARAM_FRIEND_NAME, mFriendsPlugin.getName(friendEmail));
        mHistoryPlugin.addHistoryItem(item);
    }

    void putUpdateFriendInHistory(final String friendEmail) {
        final HistoryItem item = new HistoryItem();
        item.timestampMillis = System.currentTimeMillis();
        item.reference = friendEmail;
        item.friendReference = friendEmail;
        item.type = HistoryItem.FRIEND_UPDATED;
        item.parameters.put(HistoryItem.PARAM_FRIEND_NAME, mFriendsPlugin.getName(friendEmail));
        // XXX: put update information in item.parameters
        // e.g. location sharing started/stopped me_to_friend/friend_to_me
        // avatar changed, name changed, ...
        mHistoryPlugin.addHistoryItem(item);
    }

    void putFriendBecameFriendWithInHistory(final String friendEmail, final String friendsFriendName,
        final String friendsFriendEmailHash) {
        final HistoryItem item = new HistoryItem();
        item.timestampMillis = System.currentTimeMillis();
        item.reference = friendsFriendEmailHash;
        item.friendReference = friendEmail;
        item.parameters.put(HistoryItem.PARAM_FRIEND_NAME, mFriendsPlugin.getName(friendEmail));
        item.parameters.put(HistoryItem.PARAM_FRIENDS_FRIEND_NAME, friendsFriendName);
        item.type = HistoryItem.FRIEND_BECAME_FRIEND;
        mHistoryPlugin.addHistoryItem(item);
    }

    public void putSendLocationInHistory(final String friendEmail) {
        final HistoryItem item = new HistoryItem();
        item.timestampMillis = System.currentTimeMillis();
        item.reference = friendEmail;
        item.friendReference = friendEmail;
        item.type = HistoryItem.LOCATION_SHARING_MY_LOCATION_SENT;
        item.parameters.put(HistoryItem.PARAM_FRIEND_NAME, mFriendsPlugin.getName(friendEmail));
        mHistoryPlugin.addHistoryItem(item);
    }

    public void putServicePokedInHistory(final String serviceEmail, final String pokeAction) {
        final HistoryItem item = new HistoryItem();
        item.timestampMillis = System.currentTimeMillis();
        item.reference = serviceEmail;
        item.friendReference = serviceEmail;
        item.type = HistoryItem.SERVICE_POKED;
        item.parameters.put(HistoryItem.PARAM_FRIEND_NAME, mFriendsPlugin.getName(serviceEmail));
        item.parameters.put(HistoryItem.PARAM_POKE_ACTION, pokeAction == null ? "" : pokeAction);
        mHistoryPlugin.addHistoryItem(item);
    }

}

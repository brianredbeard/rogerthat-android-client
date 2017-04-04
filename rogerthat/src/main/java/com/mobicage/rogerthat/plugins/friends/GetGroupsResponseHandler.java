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
package com.mobicage.rogerthat.plugins.friends;

import java.util.HashSet;
import java.util.Set;

import android.content.Intent;

import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rpc.IResponse;
import com.mobicage.rpc.ResponseHandler;
import com.mobicage.to.friends.GetGroupsResponseTO;
import com.mobicage.to.friends.GroupTO;

public class GetGroupsResponseHandler extends ResponseHandler<GetGroupsResponseTO> {

    @Override
    public void handle(final IResponse<GetGroupsResponseTO> response) {
        T.BIZZ();
        final GetGroupsResponseTO resp;
        try {
            resp = response.getResponse();
        } catch (Exception e) {
            L.d("Get groups failed", e);
            return;
        }
        FriendsPlugin friendsPlugin = mMainService.getPlugin(FriendsPlugin.class);
        friendsPlugin.getStore().clearGroups();
        Set<String> avatarHashes = new HashSet<String>();
        for (GroupTO g : resp.groups) {
            friendsPlugin.getStore().insertGroup(g.guid, g.name, null, g.avatar_hash);
            if (g.avatar_hash != null) {
                avatarHashes.add(g.avatar_hash);
            }
            for (String member : g.members) {
                friendsPlugin.getStore().insertGroupMember(g.guid, member);
            }
        }

        for (String avatarHash : avatarHashes) {
            friendsPlugin.requestGroupAvatar(avatarHash);
        }

        Intent intent = new Intent(FriendsPlugin.GROUPS_UPDATED);
        mMainService.sendBroadcast(intent);
    }
}

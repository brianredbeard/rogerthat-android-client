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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.jivesoftware.smack.util.Base64;

import android.content.Intent;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.plugins.history.HistoryItem;
import com.mobicage.rogerthat.plugins.messaging.MessagingPlugin;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.pickle.PickleException;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rpc.IResponse;
import com.mobicage.rpc.ResponseHandler;
import com.mobicage.to.friends.FriendTO;
import com.mobicage.to.friends.GetFriendResponseTO;

public class GetFriendResponseHandler extends ResponseHandler<GetFriendResponseTO> {

    protected final int CLASS_VERSION = 2;

    // Do a forced update of local friend list even if the generation is
    // identical to the one on server
    private volatile boolean mForce = false;
    private volatile boolean mRecalculateMessagesShowInList = false;
    private volatile boolean mIsLast = false;

    public void setForce(boolean pForce) {
        T.UI();
        mForce = pForce;
    }

    public void setRecalculateMessagesShowInList(boolean pRecalculateMessagesShowInList) {
        T.UI();
        mRecalculateMessagesShowInList = pRecalculateMessagesShowInList;
    }

    public void setIsLast(boolean pIsLast) {
        T.UI();
        mIsLast = pIsLast;
    }

    @Override
    public int getPickleClassVersion() {
        T.dontCare();
        return CLASS_VERSION;
    }

    @Override
    public void writePickle(DataOutput out) throws IOException {
        T.dontCare();
        super.writePickle(out);
        out.writeBoolean(mForce);
        out.writeBoolean(mRecalculateMessagesShowInList);
        out.writeBoolean(mIsLast);
    }

    @Override
    public void readFromPickle(int version, DataInput in) throws IOException, PickleException {
        T.dontCare();
        super.readFromPickle(version, in);
        mForce = in.readBoolean();
        mRecalculateMessagesShowInList = in.readBoolean();
        mIsLast = in.readBoolean();
        if (version < 2) {
            in.readLong(); // generation
        }
    }

    @Override
    public void handle(final IResponse<GetFriendResponseTO> response) {
        T.BIZZ();
        final FriendsPlugin friendsPlugin = mMainService.getPlugin(FriendsPlugin.class);
        final MessagingPlugin messagingPlugin = mMainService.getPlugin(MessagingPlugin.class);

        final GetFriendResponseTO resp;
        try {
            resp = response.getResponse();
        } catch (Exception e) {
            try {
                L.d("Get friend failed", e);
            } finally {
                finalize(friendsPlugin, messagingPlugin, null);
            }
            return;
        }

        try {
            if (resp.friend != null && (mForce || friendsPlugin.getStore().shouldUpdateFriend(resp.friend).updated)) {
                friendsPlugin.storeNewFriend(resp.friend, Base64.decode(resp.avatar), mForce);
            }
        } finally {
            finalize(friendsPlugin, messagingPlugin, resp.friend);
        }
    }

    private void finalize(final FriendsPlugin friendsPlugin, final MessagingPlugin messagingPlugin,
        final FriendTO friend) {
        T.BIZZ();
        try {
            if (mIsLast) {
                friendsPlugin.getStore().scrub();
            }
        } finally {
            try {
                if (mIsLast && mRecalculateMessagesShowInList) {
                    messagingPlugin.getStore().recalculateShowInList();
                }
            } finally {
                if (mForce) {
                    if (mIsLast) {
                        Intent intent = new Intent(FriendsPlugin.FRIENDS_LIST_REFRESHED);
                        mMainService.sendBroadcast(intent);

                        mMainService.postOnBIZZHandler(new SafeRunnable() {
                            @Override
                            protected void safeRun() throws Exception {
                                mMainService.putInHistoryLog(
                                    mMainService.getString(R.string.friend_bulk_update_received), HistoryItem.INFO);
                            }
                        });
                    }
                } else if (friend != null) {
                    Intent intent = new Intent(FriendsPlugin.FRIEND_ADDED_INTENT);
                    intent.putExtra("email", friend.email);
                    mMainService.sendBroadcast(intent);

                    mMainService.postOnBIZZHandler(new SafeRunnable() {
                        @Override
                        protected void safeRun() throws Exception {
                            friendsPlugin.getHistory().putAddFriendInHistory(friend.email);
                        }
                    });
                }
            }
        }
    }
}

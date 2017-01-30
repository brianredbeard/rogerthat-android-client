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

package com.mobicage.rogerthat;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;
import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.plugins.friends.Friend;
import com.mobicage.rogerthat.plugins.friends.FriendsPlugin;
import com.mobicage.rogerthat.plugins.messaging.MessagingFilterActivity;
import com.mobicage.rogerthat.plugins.messaging.MessagingPlugin;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeDialogInterfaceOnClickListener;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.ui.UIUtils;
import com.mobicage.rpc.config.AppConstants;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class UserFriendsActivity extends FriendsActivity {

    private final static String HINT_FRIEND_LOCATION_REQUESTED = "com.mobicage.rogerthat.plugins.friends.UserFriendsActivity.HINT_FRIEND_LOCATION_REQUESTED";

    private Button mMapButton;
    private int mFirstVisibleItem = 0;
    private int mVisibleItemCount = 0;
    private View mLastExpandedActionView = null;

    private Cursor mCursorFriends = null;

    @Override
    protected int getLayout() {
        return R.layout.userfriends;
    }

    @Override
    protected void createCursor() {
        L.e("Should not be used");
    }

    @Override
    protected void changeCursor() {
        if (mServiceIsBound) {
            FriendListAdapter fla = ((FriendListAdapter) getListAdapter());
            createFriendsCursor();
            if (mCursorFriends != null) {
                fla.changeCursor(mCursorFriends);
            }
            fla.notifyDataSetChanged();
        }

        if (mMapButton != null) {
            updateMapButtonCaption();
        }
    }

    @Override
    protected String[] getAllReceivingIntents() {
        Set<String> intents = new HashSet<String>(Arrays.asList(super.getAllReceivingIntents()));
        intents.add(FriendsPlugin.GROUPS_UPDATED);
        intents.add(FriendsPlugin.GROUP_ADDED);
        intents.add(FriendsPlugin.GROUP_MODIFIED);
        intents.add(FriendsPlugin.GROUP_REMOVED);
        return intents.toArray(new String[intents.size()]);
    }

    private void createFriendsCursor() {
        if (mCursorFriends != null)
            stopManagingCursor(mCursorFriends);
        mCursorFriends = mFriendsPlugin.getStore().getUserFriendListCursor();
    }

    @Override
    protected void loadCursorAndSetAdaptar() {
        createFriendsCursor();
        startManagingCursor(mCursorFriends);

        FriendListAdapter fla = new FriendListAdapter(this, mCursorFriends, mFriendsPlugin.getStore(), null,
                mFriendsPlugin, false, null);
        setListAdapter(fla);
    }

    private void updateMapButtonCaption() {
        final long count = mFriendsPlugin.getStore().getNumberOfLocationSharingFriends();
        if (count > 0) {
            mMapButton.setText(getString(R.string.friends_map) + " (" + count + ")");
            mMapButton.setEnabled(true);
        } else {
            mMapButton.setText(getString(R.string.friends_map));
            mMapButton.setEnabled(false);
        }
    }

    @Override
    protected void setListView(ListView listView) {
        super.setListView(listView);
        listView.setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                mFirstVisibleItem = firstVisibleItem;
                mVisibleItemCount = visibleItemCount;
            }
        });
    }

    @Override
    protected void onServiceBound() {
        super.onServiceBound();
        setActivityName("friends");
        final int text;
        switch (AppConstants.FRIENDS_CAPTION) {
        case COLLEAGUES:
            text = R.string.colleagues;
            break;
        case CONTACTS:
            text = R.string.contacts;
            break;
        case FRIENDS:
        default:
            text = R.string.tab_friends;
            break;
        }
        setTitle(text);
     }

    @Override
    protected void onPause() {
        super.onPause();
        if (mCursorFriends != null) {
            stopManagingCursor(mCursorFriends);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getWasPaused() && mCursorFriends != null) {
            startManagingCursor(mCursorFriends);
        }
    }

    @Override
    protected void onListItemClick(ListView listView, final View listItem, int position, long id) {
        T.UI();
        Object tag = listItem.getTag();
        if (tag == null) {
            return;
        } else if (tag instanceof Friend) {
            if (mLastExpandedActionView != null) {
                mLastExpandedActionView.findViewById(R.id.actions).setVisibility(View.GONE);
            }

            if (mLastExpandedActionView == listItem) {
                mLastExpandedActionView = null;
            } else {
                LinearLayout actions = (LinearLayout) listItem.findViewById(R.id.actions);

                if (position >= mFirstVisibleItem + mVisibleItemCount - 2) {
                    // item is the last visible one, scroll up
                    int x = position - mVisibleItemCount + 3;
                    if (x >= 0)
                        listView.setSelection(x);
                }
                actions.setVisibility(View.VISIBLE);
                mLastExpandedActionView = listItem;

                final Friend friend = (Friend) tag;
                handleLocation(listItem, friend);
                handleHistory(listItem, friend);
                handleSend(listItem, friend);
                handleDetails(listItem);
            }
        }
    }

    private void handleDetails(final View listItem) {
        ImageView details = (ImageView) listItem.findViewById(R.id.details);
        details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Object tag = listItem.getTag();
                if (tag instanceof Friend) {
                    final Intent friendDetails = new Intent(UserFriendsActivity.this, UserDetailActivity.class);
                    friendDetails.putExtra(FriendDetailActivity.EMAIL, ((Friend) tag).email);
                    startActivity(friendDetails);
                }
            }
        });
    }

    private void handleSend(final View listItem, final Friend friend) {
        ImageView newMessage = (ImageView) listItem.findViewById(R.id.send);
        newMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent sendMessage = new Intent(UserFriendsActivity.this, SendMessageMessageActivity.class);
                sendMessage.putExtra(SendMessageMessageActivity.RECIPIENTS, new String[] { friend.email });
                startActivity(sendMessage);
            }
        });
    }

    private void handleHistory(final View listItem, final Friend friend) {
        ImageView history = (ImageView) listItem.findViewById(R.id.history);
        history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent viewMessages = new Intent(UserFriendsActivity.this, MessagingFilterActivity.class);
                viewMessages.putExtra(MessagingPlugin.MEMBER_FILTER, friend.email);
                startActivity(viewMessages);
            }
        });
    }

    private void handleLocation(final View listItem, final Friend friend) {
        ImageView location = (ImageView) listItem.findViewById(R.id.location);
        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (friend.sharesLocation) {
                    showTransmitting(null);
                    mFriendsPlugin.scheduleSingleFriendLocationRetrieval(friend.email);
                    mService.postDelayedOnUIHandler(new SafeRunnable() {
                        @Override
                        protected void safeRun() throws Exception {
                            T.UI();
                            completeTransmit(new SafeRunnable() {
                                @Override
                                protected void safeRun() throws Exception {
                                    T.UI();
                                    UIUtils.showHint(UserFriendsActivity.this, mService,
                                        HINT_FRIEND_LOCATION_REQUESTED, R.string.friend_location_requested_body,
                                        friend.name);
                                }
                            });
                        }
                    }, 1000);
                } else {
                    SafeDialogInterfaceOnClickListener onPositiveClickListener = new SafeDialogInterfaceOnClickListener() {
                        @Override
                        public void safeOnClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            mFriendsPlugin.requestFriendShareLocation(friend.email, null);
                            UIUtils.showLongToast(UserFriendsActivity.this,
                                getString(R.string.friend_request_share_location_invitation_sent, friend.name));
                        }
                    };

                    SafeDialogInterfaceOnClickListener onNegativeClickListener = new SafeDialogInterfaceOnClickListener() {
                        @Override
                        public void safeOnClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    };

                    Dialog dialog = new AlertDialog.Builder(UserFriendsActivity.this)
                        .setMessage(getString(R.string.dialog_request_location_sharing, friend.name))
                        .setCancelable(true).setPositiveButton(R.string.yes, onPositiveClickListener)
                        .setNegativeButton(R.string.no, onNegativeClickListener).create();
                    dialog.setCanceledOnTouchOutside(true);
                    dialog.show();
                }
            }
        });
    }

    @Override
    protected String getHelpMessage() {
        return null;
    }

    @Override
    protected Class<? extends FriendDetailActivity> getDetailClass() {
        return UserDetailActivity.class;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        T.UI();
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.friends_menu, menu);

        MenuItem findItem = menu.findItem(R.id.find_friends);
        if (findItem != null) {
            switch (AppConstants.FRIENDS_CAPTION) {
                case COLLEAGUES:
                    findItem.setTitle(R.string.find_colleagues);
                    break;
                case CONTACTS:
                    findItem.setTitle(R.string.find_contacts);
                    break;
                case FRIENDS:
                default:
                    findItem.setTitle(R.string.invite_friends_short);
                    break;
            }
        }

        addIconToMenuItem(menu, R.id.find_friends, FontAwesome.Icon.faw_search);
        addIconToMenuItem(menu, R.id.friend_map, FontAwesome.Icon.faw_street_view);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        T.UI();

        switch (item.getItemId()) {
            case R.id.find_friends:
                startActivity(new Intent(this, AddFriendsActivity.class));
                return true;
            case R.id.friend_map:
                startActivity(new Intent(UserFriendsActivity.this, FriendsLocationActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected CharSequence getHeaderCellMainText() {
        return null;
    }

    @Override
    protected CharSequence getHeaderCellSubText() {
        return null;
    }
}

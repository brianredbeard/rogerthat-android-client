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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.plugins.friends.Friend;
import com.mobicage.rogerthat.plugins.friends.FriendsPlugin;
import com.mobicage.rogerthat.plugins.friends.Group;
import com.mobicage.rogerthat.plugins.messaging.MessagingActivity;
import com.mobicage.rogerthat.plugins.messaging.MessagingPlugin;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeDialogInterfaceOnClickListener;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.system.SafeViewOnClickListener;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.ui.SeparatedListAdapter;
import com.mobicage.rogerthat.util.ui.UIUtils;
import com.mobicage.rpc.config.AppConstants;

public class UserFriendsActivity extends FriendsActivity {

    private final static String HINT_FRIEND_LOCATION_REQUESTED = "com.mobicage.rogerthat.plugins.friends.UserFriendsActivity.HINT_FRIEND_LOCATION_REQUESTED";

    private final static int HEADERCELL_GROUPS = 0;
    private final static int HEADERCELL_FRIENDS = 1;

    private Button mMapButton;
    private int mFirstVisibleItem = 0;
    private int mVisibleItemCount = 0;
    private View mLastExpandedActionView = null;

    private Cursor mCursorFriends = null;
    private Cursor mCursorGroups = null;

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
            SeparatedListAdapter separatedListAdapter = ((SeparatedListAdapter) getListAdapter());
            int sectionIndex = 0;
            for (Adapter adapter : separatedListAdapter.sections.values()) {
                Cursor c = null;
                if (sectionIndex == 0) {
                    createGroupCursor();
                    c = mCursorGroups;
                } else if (sectionIndex == 1) {
                    createFriendsCursor();
                    c = mCursorFriends;
                } else {
                    L.w("Don't know how to change if section " + sectionIndex);
                }
                if (c != null) {
                    ((CursorAdapter) adapter).changeCursor(c);
                }
                sectionIndex += 1;
            }
            separatedListAdapter.notifyDataSetChanged();
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

    private void createGroupCursor() {
        if (mCursorGroups != null)
            stopManagingCursor(mCursorGroups);
        mCursorGroups = mFriendsPlugin.getStore().getGroupListCursor();
    }

    @Override
    protected void loadCursorAndSetAdaptar() {
        createFriendsCursor();
        startManagingCursor(mCursorFriends);

        View headerViewFriends = getLayoutInflater().inflate(R.layout.main_list_header, null);

        final int longText;
        final int shortText;
        switch (AppConstants.FRIENDS_CAPTION) {
        case COLLEAGUES:
            longText = R.string.invite_colleagues_long;
            shortText = R.string.find_colleagues;
            break;
        case CONTACTS:
            longText = R.string.invite_contacts_long;
            shortText = R.string.find_contacts;
            break;
        case FRIENDS:
        default:
            longText = R.string.invite_friends_long;
            shortText = R.string.invite_friends_short;
            break;
        }

        ((TextView) headerViewFriends.findViewById(R.id.mainheader)).setText(shortText);
        ((TextView) headerViewFriends.findViewById(R.id.subheader)).setText(longText);
        headerViewFriends.setTag(HEADERCELL_FRIENDS);

        FriendListAdapter fla = new FriendListAdapter(this, mCursorFriends, mFriendsPlugin.getStore(), null,
            mFriendsPlugin, true, headerViewFriends);

        createGroupCursor();
        startManagingCursor(mCursorGroups);

        View headerViewGroups = getLayoutInflater().inflate(R.layout.main_list_header, null);
        ((TextView) headerViewGroups.findViewById(R.id.mainheader)).setText(R.string.create_groups_short);
        ((TextView) headerViewGroups.findViewById(R.id.subheader)).setText(R.string.create_groups_long);
        headerViewGroups.setTag(HEADERCELL_GROUPS);

        GroupListAdapter gla = new GroupListAdapter(this, mCursorGroups, mFriendsPlugin.getStore(), null,
            mFriendsPlugin, true, headerViewGroups);

        SeparatedListAdapter adapter = new SeparatedListAdapter(this);

        adapter.addSection(mService.getString(R.string.groups), gla);
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
        adapter.addSection(mService.getString(text), fla);

        setListAdapter(adapter);
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
        setNavigationBarVisible(AppConstants.SHOW_NAV_HEADER);
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
        setNavigationBarTitle(text);
        findViewById(R.id.navigation_bar_home_button).setOnClickListener(new SafeViewOnClickListener() {
            @Override
            public void safeOnClick(View v) {
                Intent i = new Intent(UserFriendsActivity.this, HomeActivity.class);
                i.setFlags(MainActivity.FLAG_CLEAR_STACK);
                startActivity(i);
                finish();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mCursorFriends != null) {
            stopManagingCursor(mCursorFriends);
        }
        if (mCursorGroups != null) {
            stopManagingCursor(mCursorGroups);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getWasPaused() && mCursorFriends != null) {
            startManagingCursor(mCursorFriends);
        }
        if (getWasPaused() && mCursorGroups != null) {
            startManagingCursor(mCursorGroups);
        }
    }

    @Override
    protected void onListItemClick(ListView listView, final View listItem, int position, long id) {
        T.UI();
        Object tag = listItem.getTag();
        if (tag == null) {
            return;
        } else if (tag instanceof Integer && (Integer) tag == HEADERCELL_FRIENDS) {
            startActivity(new Intent(this, AddFriendsActivity.class));

        } else if (tag instanceof Integer && (Integer) tag == HEADERCELL_GROUPS) {
            final EditText edit = (EditText) getLayoutInflater().inflate(R.layout.save_canned_message_edit, null);
            new AlertDialog.Builder(this).setTitle(R.string.create_group).setView(edit)
                .setPositiveButton(R.string.ok, new SafeDialogInterfaceOnClickListener() {
                    @Override
                    public void safeOnClick(DialogInterface dialog, int which) {
                        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        mgr.hideSoftInputFromWindow(edit.getWindowToken(), 0);

                        String name = edit.getText().toString();
                        String guid = UUID.randomUUID().toString();
                        mFriendsPlugin.getStore().insertGroup(guid, name, null, null);

                        final Intent groupDetails = new Intent(UserFriendsActivity.this, GroupDetailActivity.class);
                        groupDetails.putExtra(GroupDetailActivity.GUID, guid);
                        groupDetails.putExtra(GroupDetailActivity.NEW_GROUP, true);
                        startActivity(groupDetails);
                    }
                }).setNegativeButton(R.string.cancel, new SafeDialogInterfaceOnClickListener() {
                    @Override
                    public void safeOnClick(DialogInterface dialog, int which) {
                        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        mgr.hideSoftInputFromWindow(edit.getWindowToken(), 0);
                    }
                }).create().show();

            edit.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

        } else if (tag instanceof Friend || tag instanceof Group) {
            if (mLastExpandedActionView != null)
                mLastExpandedActionView.setVisibility(View.GONE);

            LinearLayout actions = (LinearLayout) listItem.findViewById(R.id.actions);
            if (mLastExpandedActionView == actions) {
                mLastExpandedActionView = null;
            } else {
                if (position >= mFirstVisibleItem + mVisibleItemCount - 2) {
                    // item is the last visible one, scroll up
                    int x = position - mVisibleItemCount + 3;
                    if (x >= 0)
                        listView.setSelection(x);
                }

                actions.setVisibility(View.VISIBLE);
                mLastExpandedActionView = actions;

                if (tag instanceof Friend) {
                    final Friend friend = (Friend) tag;
                    handleLocation(listItem, friend);
                    handleHistory(listItem, friend);
                    handleSend(listItem, friend);

                } else {
                    final Group group = (Group) tag;
                    handleSend(listItem, group);
                }
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

                } else if (tag instanceof Group) {
                    final Intent groupDetails = new Intent(UserFriendsActivity.this, GroupDetailActivity.class);
                    groupDetails.putExtra(GroupDetailActivity.GUID, ((Group) tag).guid);
                    groupDetails.putExtra(GroupDetailActivity.NEW_GROUP, false);
                    startActivity(groupDetails);
                }
            }
        });
    }

    private void handleSend(final View listItem, final Group group) {
        ImageView newMessage = (ImageView) listItem.findViewById(R.id.send);
        newMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent sendMessage = new Intent(UserFriendsActivity.this, SendMessageWizardActivity.class);
                sendMessage.putExtra(SendMessageWizardActivity.GROUP_RECIPIENTS, new String[] { group.guid });
                startActivity(sendMessage);
            }
        });
    }

    private void handleSend(final View listItem, final Friend friend) {
        ImageView newMessage = (ImageView) listItem.findViewById(R.id.send);
        newMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent sendMessage = new Intent(UserFriendsActivity.this, SendMessageWizardActivity.class);
                sendMessage.putExtra(SendMessageWizardActivity.RECIPIENTS, new String[] { friend.email });
                startActivity(sendMessage);
            }
        });
    }

    private void handleHistory(final View listItem, final Friend friend) {
        ImageView history = (ImageView) listItem.findViewById(R.id.history);
        history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent viewMessages = new Intent(UserFriendsActivity.this, MessagingActivity.class);
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
        return getString(R.string.userfriends_getting_started_msg, getString(R.string.app_name));
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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        T.UI();

        switch (item.getItemId()) {
        case R.id.help:
            showHelp();
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
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

package com.mobicage.rogerthat.plugins.friends;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONValue;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.FriendDetailOrInviteActivity;
import com.mobicage.rogerthat.MyIdentity;
import com.mobicage.rogerthat.ServiceBoundActivity;
import com.mobicage.rogerthat.plugins.scan.ProfileActivity;
import com.mobicage.rogerthat.util.TextUtils;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeAsyncTask;
import com.mobicage.rogerthat.util.system.SafeBroadcastReceiver;
import com.mobicage.rogerthat.util.system.SafeViewOnClickListener;
import com.mobicage.rogerthat.util.ui.ImageHelper;
import com.mobicage.rogerthat.util.ui.UIUtils;
import com.mobicage.rpc.IncompleteMessageException;
import com.mobicage.to.friends.FindFriendItemTO;
import com.mobicage.to.friends.FindFriendResponseTO;

public class FriendSearchActivity extends ServiceBoundActivity {

    public static final String SEARCH_RESULT = "SEARCH_RESULT";
    public static final String SEARCH_STRING = "SEARCH_STRING";

    private static final String[] UPDATE_VIEW_INTENTS = new String[] { FriendsPlugin.FRIENDS_LIST_REFRESHED,
        FriendsPlugin.FRIEND_ADDED_INTENT, FriendsPlugin.FRIEND_MARKED_FOR_REMOVAL_INTENT,
        FriendsPlugin.FRIEND_REMOVED_INTENT };

    private FriendsPlugin mFriendsPlugin;
    private BroadcastReceiver mBroadcastReceiver;
    private ProgressDialog mProgressDialog;
    private String mSearchString = null;
    private FindFriendResponseTO mResponseTO;
    private SearchInfo mSearchInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.friend_search);

        mSearchInfo = new SearchInfo();
        mSearchInfo.results = (ListView) findViewById(R.id.search_result_lists);

        final Context ctx = this;
        final EditText editText = (EditText) findViewById(R.id.search_text);
        editText.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                    || (event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                    if (!TextUtils.isEmptyOrWhitespace(mSearchString)) {
                        UIUtils.hideKeyboard(FriendSearchActivity.this, v);
                        launchFindFriendsCall();
                        return true;
                    }
                }
                return false;
            }
        });
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                mSearchString = s.toString();
                clearSearches();
            }
        });

        findViewById(R.id.search_button).setOnClickListener(new SafeViewOnClickListener() {
            @Override
            public void safeOnClick(View v) {
                if (TextUtils.isEmptyOrWhitespace(mSearchString)) {
                    mSearchString = "";
                }
                UIUtils.hideKeyboard(ctx, editText);
                clearSearches();
                launchFindFriendsCall();
            }
        });
    }

    @Override
    protected void onServiceBound() {
        mFriendsPlugin = mService.getPlugin(FriendsPlugin.class);

        mBroadcastReceiver = getBroadCastReceiver();

        IntentFilter filter = new IntentFilter();
        filter.addAction(FriendsPlugin.FRIEND_SEARCH_FAILED_INTENT);
        filter.addAction(FriendsPlugin.FRIEND_SEARCH_RESULT_INTENT);
        for (String action : UPDATE_VIEW_INTENTS)
            filter.addAction(action);
        registerReceiver(mBroadcastReceiver, filter);

        mSearchString = "";
        launchFindFriendsCall();
    }

    @Override
    protected void onServiceUnbound() {
        unregisterReceiver(mBroadcastReceiver);
    }

    private AdapterView.OnItemClickListener mListItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            @SuppressWarnings("unchecked")
            Map<String, Object> tag = (Map<String, Object>) view.getTag();
            if (tag != null) {
                FindFriendItemTO item = (FindFriendItemTO) tag.get("item");
                MyIdentity myIdentity = mService.getIdentityStore().getIdentity();
                if (myIdentity.getEmail().equals(item.email)) {
                    Intent intent = new Intent(FriendSearchActivity.this, ProfileActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(FriendSearchActivity.this, FriendDetailOrInviteActivity.class);
                    intent.putExtra(FriendDetailOrInviteActivity.EMAIL, item.email);
                    startActivity(intent);
                }
            }
        }
    };

    final AbsListView.OnScrollListener mListViewScrollListener = new AbsListView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            int lastInScreen = firstVisibleItem + visibleItemCount;
            if (lastInScreen == totalItemCount && !mSearchInfo.loading && mSearchInfo.cursor != null) {
                launchFindFriendsCall(mSearchInfo.cursor);
                mSearchInfo.loading = true;
            }
        }
    };

    private BroadcastReceiver getBroadCastReceiver() {
        return new SafeBroadcastReceiver() {

            @Override
            public String[] onSafeReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if (mSearchString != null && FriendsPlugin.FRIEND_SEARCH_RESULT_INTENT.equals(action)) {
                    if (mSearchString.equals(intent.getStringExtra(SEARCH_STRING))) {
                        mProgressDialog.dismiss();
                        String jsonResult = intent.getStringExtra(SEARCH_RESULT);
                        @SuppressWarnings("unchecked")
                        Map<String, Object> jsonMap = (Map<String, Object>) JSONValue.parse(jsonResult);
                        try {
                            mResponseTO = new FindFriendResponseTO(jsonMap);
                        } catch (IncompleteMessageException e) {
                            L.bug(e);
                            showSearchFailedDialog();
                            return new String[] { action };
                        }

                        if (!TextUtils.isEmptyOrWhitespace(mResponseTO.error_string)) {
                            UIUtils.showAlertDialog(FriendSearchActivity.this, null, mResponseTO.error_string);
                            return new String[] { action };
                        }
                        mSearchInfo.cursor = mResponseTO.cursor;
                        mSearchInfo.loading = false;
                        if (mSearchInfo.adapter == null) {
                            FriendSearchAdapter adapter = new FriendSearchAdapter(mResponseTO.items, mSearchInfo);
                            mSearchInfo.adapter = adapter;
                            mSearchInfo.results.setAdapter(adapter);
                            mSearchInfo.results.setOnItemClickListener(mListItemClickListener);
                            mSearchInfo.results.setOnScrollListener(mListViewScrollListener);
                        } else {
                            mSearchInfo.adapter.addAll(mResponseTO.items);
                        }

                        return new String[] { action };
                    }
                } else if (FriendsPlugin.FRIEND_SEARCH_FAILED_INTENT.equals(action)) {
                    if (mSearchString.equals(intent.getStringExtra(SEARCH_STRING))) {
                        mProgressDialog.dismiss();
                        showSearchFailedDialog();
                        return new String[] { action };
                    }
                } else {
                    L.d(FriendSearchActivity.class.getName() + " received " + action + " intent");
                    mSearchInfo.adapter.notifyDataSetChanged();

                    // not interested in older intents which also result in reloading the list
                    return UPDATE_VIEW_INTENTS;
                }
                return null;
            }

        };
    }

    private void launchFindFriendsCall() {
        launchFindFriendsCall(null);
    }

    private void launchFindFriendsCall(String cursor) {
        if (mFriendsPlugin.searchFriend(mSearchString, cursor)) {
            if (cursor == null)
                mProgressDialog = ProgressDialog.show(this, null, getString(R.string.searching), true, true);
        } else {
            showSearchFailedDialog();
        }
    }

    private void showSearchFailedDialog() {
        UIUtils.showAlertDialog(this, null, R.string.error_search);
    }

    private void clearSearches() {
        mSearchInfo.cursor = null;
        if (mSearchInfo.adapter != null) {
            mSearchInfo.adapter.mItems.clear();
        }
    }

    private class FriendSearchAdapter extends BaseAdapter {

        private final List<FindFriendItemTO> mItems = new ArrayList<FindFriendItemTO>();
        private final SearchInfo mSearchInfo;

        public FriendSearchAdapter(FindFriendItemTO[] items, SearchInfo info) {
            mSearchInfo = info;
            addAll(items);
        }

        public void addAll(FindFriendItemTO[] items) {
            mItems.addAll(Arrays.asList(items));
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mItems.size() + (mSearchInfo.cursor == null ? 0 : 1);
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (position == mItems.size() && mSearchInfo.cursor != null) {
                return getLayoutInflater().inflate(R.layout.list_loading_more_indicator, null);
            }

            final FindFriendItemTO item = mItems.get(position);

            View v = convertView;
            if (v == null || v.getTag() == null) {
                v = getLayoutInflater().inflate(R.layout.search_friend, null);
            }

            Map<String, Object> tag = new HashMap<String, Object>();
            tag.put("item", item);
            v.setTag(tag);

            // Set avatar
            final ImageView avatarView = (ImageView) v.findViewById(R.id.friend_avatar);
            LayoutParams lp = avatarView.getLayoutParams();
            lp.width = lp.height = UIUtils.convertDipToPixels(FriendSearchActivity.this, 50);

            new SafeAsyncTask<Object, Object, Object>() {
                @Override
                protected Object safeDoInBackground(Object... params) {
                    try {
                        return BitmapFactory.decodeStream((InputStream) new URL((String) params[0]).getContent());
                    } catch (MalformedURLException e) {
                        L.bug("Could not download friend avatar: " + item.avatar_url, e);
                    } catch (IOException e) {
                        L.bug("Could not download friend avatar: " + item.avatar_url, e);
                    } catch (Exception e) {
                        L.bug("Could not download friend avatar: " + item.avatar_url, e);
                    }
                    return null;
                };

                @Override
                protected void safeOnPostExecute(Object result) {
                    Bitmap bitmap = (Bitmap) result;
                    if (bitmap != null) {
                        Bitmap avatar = ImageHelper.getRoundedCornerAvatar(bitmap);
                        avatarView.setImageBitmap(avatar);
                    }
                }

                @Override
                protected void safeOnCancelled(Object result) {
                }

                @Override
                protected void safeOnProgressUpdate(Object... values) {
                }

                @Override
                protected void safeOnPreExecute() {
                };
            }.execute(item.avatar_url);

            // Set name
            ((TextView) v.findViewById(R.id.friend_name)).setText(item.name);

            // Set status icon
            v.findViewById(R.id.friend_existence_layout).setVisibility(View.VISIBLE);
            ProgressBar spinnerView = (ProgressBar) v.findViewById(R.id.friend_spinner);
            ImageView statusView = (ImageView) v.findViewById(R.id.friend_existence);
            spinnerView.setVisibility(View.GONE);
            statusView.setVisibility(View.VISIBLE);
            statusView.setImageResource(R.drawable.ic_btn_arrow_right_unselected);
            return v;

        }
    }

    private class SearchInfo {
        FriendSearchAdapter adapter;
        String cursor;
        ListView results;
        boolean loading = false;
    }
}

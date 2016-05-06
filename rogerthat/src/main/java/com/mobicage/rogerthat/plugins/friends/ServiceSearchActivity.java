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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jivesoftware.smack.util.Base64;
import org.json.simple.JSONValue;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.ServiceBoundActivity;
import com.mobicage.rogerthat.ServiceDetailActivity;
import com.mobicage.rogerthat.util.TextUtils;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeBroadcastReceiver;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.system.SafeViewOnClickListener;
import com.mobicage.rogerthat.util.ui.ImageHelper;
import com.mobicage.rogerthat.util.ui.SafeViewFlipper;
import com.mobicage.rogerthat.util.ui.UIUtils;
import com.mobicage.rogerthat.util.ui.ViewFlipperSlider;
import com.mobicage.rpc.IncompleteMessageException;
import com.mobicage.to.service.FindServiceCategoryTO;
import com.mobicage.to.service.FindServiceItemTO;
import com.mobicage.to.service.FindServiceResponseTO;

public class ServiceSearchActivity extends ServiceBoundActivity {

    public final static String ORGANIZATION_TYPE = "organization_type";

    public static final String SEARCH_RESULT = "SEARCH_RESULT";
    public static final String SEARCH_STRING = "SEARCH_STRING";

    private static final int PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 1;

    private static final String[] UPDATE_VIEW_INTENTS = new String[] { FriendsPlugin.FRIENDS_LIST_REFRESHED,
        FriendsPlugin.FRIEND_ADDED_INTENT, FriendsPlugin.FRIEND_MARKED_FOR_REMOVAL_INTENT,
        FriendsPlugin.FRIEND_REMOVED_INTENT };

    private FriendsPlugin mFriendsPlugin;
    private BroadcastReceiver mBroadcastReceiver;
    private LinearLayout mSearchCategoryLabels;
    private SafeViewFlipper mSearchCategoryViewFlipper;
    private ProgressDialog mProgressDialog;
    private String mSearchString = null;
    private FindServiceResponseTO mResponseTO;
    private int mOrganizationType;
    private Map<String, SearchInfo> mSearchInfoByCategory;
    private Map<AbsListView, SearchInfo> mSearchInfoByListView;

    private GestureDetector mGestureScanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.service_search);

        mSearchCategoryLabels = (LinearLayout) findViewById(R.id.search_category);
        mSearchCategoryViewFlipper = (SafeViewFlipper) findViewById(R.id.search_result_lists);

        mSearchInfoByCategory = new HashMap<String, ServiceSearchActivity.SearchInfo>();
        mSearchInfoByListView = new HashMap<AbsListView, ServiceSearchActivity.SearchInfo>();

        mGestureScanner = new GestureDetector(new ViewFlipperSlider(mOnSwipeLeft, mOnSwipeRight));

        final Context ctx = this;
        final EditText editText = (EditText) findViewById(R.id.search_text);
        editText.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                    || (event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                    if (!TextUtils.isEmptyOrWhitespace(mSearchString)) {
                        UIUtils.hideKeyboard(ServiceSearchActivity.this, v);
                        launchFindServiceCall();
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
                launchFindServiceCall();
            }
        });
    }

    @Override
    protected void onServiceBound() {
        mFriendsPlugin = mService.getPlugin(FriendsPlugin.class);
        mOrganizationType = getIntent().getIntExtra(ORGANIZATION_TYPE,
                FriendStore.SERVICE_ORGANIZATION_TYPE_UNSPECIFIED);

        mBroadcastReceiver = getBroadCastReceiver();

        IntentFilter filter = new IntentFilter();
        filter.addAction(FriendsPlugin.SERVICE_SEARCH_FAILED_INTENT);
        filter.addAction(FriendsPlugin.SERVICE_SEARCH_RESULT_INTENT);
        for (String action : UPDATE_VIEW_INTENTS)
            filter.addAction(action);
        registerReceiver(mBroadcastReceiver, filter);

        mSearchString = "";
        if (mService.isPermitted(Manifest.permission.ACCESS_FINE_LOCATION)) {
            launchFindServiceCall();
        } else {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_ACCESS_FINE_LOCATION) {
            launchFindServiceCall();
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onServiceUnbound() {
        unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent e) {
        if (mGestureScanner != null && mGestureScanner.onTouchEvent(e)) {
            return true;
        }
        return super.dispatchTouchEvent(e);
    }

    private AdapterView.OnItemClickListener mListItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            @SuppressWarnings("unchecked")
            Map<String, Object> tag = (Map<String, Object>) view.getTag();
            if (tag != null) {
                int existence = (Integer) tag.get("existence");
                FindServiceItemTO item = (FindServiceItemTO) tag.get("item");
                if (existence == Friend.ACTIVE) {
                    Intent intent = new Intent(ServiceSearchActivity.this, ServiceActionMenuActivity.class);
                    intent.putExtra(ServiceActionMenuActivity.SERVICE_EMAIL, item.email);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(ServiceSearchActivity.this, ServiceDetailActivity.class);
                    if (existence == Friend.DELETED || existence == Friend.DELETION_PENDING) {
                        intent.putExtra(ServiceDetailActivity.EXISTENCE, Friend.NOT_FOUND);
                    } else {
                        intent.putExtra(ServiceDetailActivity.EXISTENCE, existence);
                    }
                    intent
                        .putExtra(ServiceDetailActivity.FIND_SERVICE_RESULT, JSONValue.toJSONString(item.toJSONMap()));
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
            SearchInfo si = mSearchInfoByListView.get(view);
            if (si == null) {
                L.bug("Could not get SearchInfo by list view!");
                return;
            }

            int lastInScreen = firstVisibleItem + visibleItemCount;
            if (lastInScreen == totalItemCount && !si.loading && si.cursor != null) {
                launchFindServiceCall(si.cursor);
                si.loading = true;
            }
        }
    };

    private BroadcastReceiver getBroadCastReceiver() {
        return new SafeBroadcastReceiver() {

            @Override
            public String[] onSafeReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if (mSearchString != null && FriendsPlugin.SERVICE_SEARCH_RESULT_INTENT.equals(action)) {
                    if (mSearchString.equals(intent.getStringExtra(SEARCH_STRING))) {
                        mProgressDialog.dismiss();
                        String jsonResult = intent.getStringExtra(SEARCH_RESULT);
                        @SuppressWarnings("unchecked")
                        Map<String, Object> jsonMap = (Map<String, Object>) JSONValue.parse(jsonResult);
                        try {
                            mResponseTO = new FindServiceResponseTO(jsonMap);
                        } catch (IncompleteMessageException e) {
                            L.bug(e);
                            showSearchFailedDialog();
                            return new String[] { action };
                        }

                        if (!TextUtils.isEmptyOrWhitespace(mResponseTO.error_string)) {
                            UIUtils.showAlertDialog(ServiceSearchActivity.this, null, mResponseTO.error_string);
                            return new String[] { action };
                        }

                        for (FindServiceCategoryTO category : mResponseTO.matches) {
                            if (mSearchInfoByCategory.containsKey(category.category)) {
                                SearchInfo si = mSearchInfoByCategory.get(category.category);
                                si.cursor = category.cursor;
                                si.loading = false;
                                si.adapter.addAll(category.items);
                            } else {
                                // Add Label
                                LinearLayout label = (LinearLayout) getLayoutInflater().inflate(
                                    R.layout.search_category, null);
                                final boolean selected = mSearchCategoryLabels.getChildCount() == 0;
                                final TextView labelTextView = setCatorySelected(label, selected);
                                labelTextView.setText(category.category);
                                label.setOnClickListener(new OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        displayTab(mSearchCategoryLabels.indexOfChild(v));
                                    }
                                });
                                mSearchCategoryLabels.addView(label);

                                // Add ListView
                                ListView results = (ListView) getLayoutInflater().inflate(
                                    R.layout.search_category_results, null);
                                mSearchCategoryViewFlipper.addView(results);
                                SearchInfo si = new SearchInfo();
                                ServiceSearchAdapter adapter = new ServiceSearchAdapter(category.items, si);
                                si.cursor = category.cursor;
                                si.adapter = adapter;
                                si.label = label;
                                si.results = results;
                                mSearchInfoByCategory.put(category.category, si);
                                mSearchInfoByListView.put(results, si);

                                results.setAdapter(adapter);
                                results.setOnItemClickListener(mListItemClickListener);
                                results.setOnScrollListener(mListViewScrollListener);
                            }
                        }

                        return new String[] { action };
                    }
                } else if (FriendsPlugin.SERVICE_SEARCH_FAILED_INTENT.equals(action)) {
                    if (mSearchString.equals(intent.getStringExtra(SEARCH_STRING))) {
                        mProgressDialog.dismiss();
                        showSearchFailedDialog();
                        return new String[] { action };
                    }
                } else {
                    L.d(ServiceSearchActivity.class.getName() + " received " + action + " intent");
                    for (SearchInfo si : mSearchInfoByCategory.values()) {
                        si.adapter.notifyDataSetChanged();
                    }

                    // not interested in older intents which also result in reloading the list
                    return UPDATE_VIEW_INTENTS;
                }
                return null;
            }

        };
    }

    private TextView setCatorySelected(LinearLayout label, final boolean selected) {
        final View labelIndicatorView = label.findViewById(R.id.indicator);
        final TextView labelTextView = (TextView) label.findViewById(R.id.category);
        if (selected) {
            labelIndicatorView.setVisibility(View.VISIBLE);
            labelTextView.setTypeface(Typeface.create(labelTextView.getTypeface(), Typeface.BOLD));
            labelTextView.setTextColor(getResources().getColor(R.color.mc_blue2));
        } else {
            labelIndicatorView.setVisibility(View.INVISIBLE);
            labelTextView.setTypeface(Typeface.create(labelTextView.getTypeface(), Typeface.NORMAL));
            labelTextView.setTextColor(getResources().getColor(android.R.color.secondary_text_light));
        }
        return labelTextView;
    }

    private void displayTab(int tab) {
        final int childCount = mSearchCategoryViewFlipper.getChildCount();
        if (childCount == 0) {
            return;
        }
        while (tab < 0)
            tab += childCount;
        tab = tab % childCount;
        mSearchCategoryViewFlipper.setDisplayedChild(tab);
        for (int i = 0; i < childCount; i++) {
            SearchInfo si = mSearchInfoByListView.get(mSearchCategoryViewFlipper.getChildAt(i));
            if (si == null) {
                L.bug("Could not find list view!");
                return;
            }
            final boolean selected = tab == i;
            final LinearLayout v = si.label;
            setCatorySelected(v, selected);
            if (selected) {
                final HorizontalScrollView sv = (HorizontalScrollView) findViewById(R.id.search_category_scroll_container);
                sv.post(new SafeRunnable() {
                    @Override
                    public void safeRun() {
                        int vLeft = v.getLeft();
                        int vRight = v.getRight();
                        int width = v.getWidth();
                        sv.smoothScrollTo(((vLeft + vRight) / 2) - (width), 0);
                    }
                });
            }
        }
    }

    private void launchFindServiceCall() {
        launchFindServiceCall(null);
    }

    private void launchFindServiceCall(String cursor) {
        if (mFriendsPlugin.searchService(mSearchString, mOrganizationType, cursor)) {
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
        mSearchCategoryLabels.removeAllViews();
        mSearchCategoryViewFlipper.removeAllViews();
        mSearchInfoByCategory.clear();
        mSearchInfoByListView.clear();
    }

    private SafeRunnable mOnSwipeLeft = new SafeRunnable() {
        @Override
        protected void safeRun() throws Exception {
            int i = mSearchCategoryViewFlipper.getDisplayedChild();
            displayTab(i + 1);
        }
    };

    private SafeRunnable mOnSwipeRight = new SafeRunnable() {
        @Override
        protected void safeRun() throws Exception {
            int i = mSearchCategoryViewFlipper.getDisplayedChild();
            displayTab(i - 1);
        }
    };

    private class ServiceSearchAdapter extends BaseAdapter {

        private final List<FindServiceItemTO> mItems = new ArrayList<FindServiceItemTO>();
        private final SearchInfo mSearchInfo;

        public ServiceSearchAdapter(FindServiceItemTO[] items, SearchInfo info) {
            mSearchInfo = info;
            addAll(items);
        }

        public void addAll(FindServiceItemTO[] items) {
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

            final FindServiceItemTO item = mItems.get(position);
            final int existence = mFriendsPlugin.getStore().getExistence(item.email);

            View v = convertView;
            if (v == null || v.getTag() == null) {
                v = getLayoutInflater().inflate(R.layout.search_friend, null);
            }

            Map<String, Object> tag = new HashMap<String, Object>();
            tag.put("item", item);
            tag.put("existence", existence);
            v.setTag(tag);

            // Set avatar
            byte[] img = Base64.decode(item.avatar);
            Bitmap avatar = ImageHelper.getRoundedCornerAvatar(BitmapFactory.decodeByteArray(img, 0, img.length));
            ImageView avatarView = (ImageView) v.findViewById(R.id.friend_avatar);
            avatarView.setImageBitmap(avatar);
            LayoutParams lp = avatarView.getLayoutParams();
            lp.width = lp.height = UIUtils.convertDipToPixels(ServiceSearchActivity.this, 50);

            // Set name
            ((TextView) v.findViewById(R.id.friend_name)).setText(item.name);
            final TextView detailTextView = (TextView) v.findViewById(R.id.friend_subtitle);
            detailTextView.setText(item.detail_text);

            // Set status icon
            v.findViewById(R.id.friend_existence_layout).setVisibility(View.VISIBLE);
            ProgressBar spinnerView = (ProgressBar) v.findViewById(R.id.friend_spinner);
            ImageView statusView = (ImageView) v.findViewById(R.id.friend_existence);

            switch (existence) {
            case Friend.ACTIVE:
                spinnerView.setVisibility(View.GONE);
                statusView.setVisibility(View.VISIBLE);
                statusView.setImageResource(R.drawable.ic_bullet_key_permission);
                break;
            case Friend.DELETED:
            case Friend.DELETION_PENDING:
            case Friend.NOT_FOUND:
                spinnerView.setVisibility(View.GONE);
                statusView.setVisibility(View.VISIBLE);
                statusView.setImageResource(R.drawable.ic_btn_arrow_right_unselected);
                break;
            case Friend.INVITE_PENDING:
                spinnerView.setVisibility(View.VISIBLE);
                statusView.setVisibility(View.GONE);
                break;
            default:
                spinnerView.setVisibility(View.GONE);
                statusView.setVisibility(View.GONE);
                break;
            }
            return v;

        }
    }

    private class SearchInfo {
        ServiceSearchAdapter adapter;
        String cursor;
        LinearLayout label;
        ListView results;
        boolean loading = false;
    }
}
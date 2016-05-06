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

package com.mobicage.rogerthat.plugins.messaging.widgets;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.mobicage.api.messaging.Rpc;
import com.mobicage.models.properties.forms.AdvancedOrderCategory;
import com.mobicage.models.properties.forms.AdvancedOrderItem;
import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.plugins.messaging.AdvancedOrderWidgetResult;
import com.mobicage.rogerthat.plugins.messaging.Message;
import com.mobicage.rogerthat.plugins.messaging.MessagingPlugin;
import com.mobicage.rogerthat.util.CachedDownloader;
import com.mobicage.rogerthat.util.TextUtils;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeAsyncTask;
import com.mobicage.rogerthat.util.system.SafeBroadcastReceiver;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.ui.UIUtils;
import com.mobicage.rogerthat.widget.Resizable16by9ImageView;
import com.mobicage.rpc.IncompleteMessageException;
import com.mobicage.rpc.ResponseHandler;
import com.mobicage.to.messaging.forms.AdvancedOrderTO;
import com.mobicage.to.messaging.forms.SubmitAdvancedOrderFormRequestTO;
import com.mobicage.to.messaging.forms.SubmitAdvancedOrderFormResponseTO;

public class AdvancedOrderWidget extends Widget {

    private static class AdvancedOrderCategoryRow {
        public String id;
        public String name;

        public AdvancedOrderCategoryRow(String id, String name) {
            super();
            this.id = id;
            this.name = name;
        }
    }

    private static class AdvancedOrderCategoryItemRow {
        public String categoryId;
        public String description;
        public String id;
        public String imageUrl;
        public String name;
        public long step;
        public String stepUnit;
        public long stepUnitConversion;
        public String unit;
        public long unitPrice;
        public long value;
        public boolean hasPrice;
        public boolean expanded;

        public AdvancedOrderCategoryItemRow(String categoryId, String description, String id, String imageUrl,
            String name, long step, String stepUnit, long stepUnitConversion, String unit, long unitPrice, long value,
            boolean hasPrice, boolean expanded) {
            super();
            this.categoryId = categoryId;
            this.description = description;
            this.id = id;
            this.imageUrl = imageUrl;
            this.name = name;
            this.step = step;
            this.stepUnit = stepUnit;
            this.stepUnitConversion = stepUnitConversion;
            this.unit = unit;
            this.unitPrice = unitPrice;
            this.value = value;
            this.hasPrice = hasPrice;
            this.expanded = expanded;
        }
    }

    private class AdvancedOrderAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            T.UI();
            if (mData == null)
                return 0;

            return mData.size();
        }

        @Override
        public Object getItem(int position) {
            T.UI();
            return null;
        }

        @Override
        public long getItemId(int position) {
            T.UI();
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            T.UI();
            final View v;
            final Object rowObject = mData.get(position);
            if (rowObject instanceof AdvancedOrderCategoryRow) {
                v = LayoutInflater.from(mActivity).inflate(R.layout.widget_advanced_order_list_category, parent, false);
            } else {
                v = LayoutInflater.from(mActivity).inflate(R.layout.widget_advanced_order_list_item, parent, false);
            }

            TextView nameLbl = (TextView) v.findViewById(R.id.name);
            final TextView iconLbl = (TextView) v.findViewById(R.id.icon);
            iconLbl.setTypeface(mFontAwesomeTypeFace);

            if (rowObject instanceof AdvancedOrderCategoryRow) {
                AdvancedOrderCategoryRow row = (AdvancedOrderCategoryRow) rowObject;
                nameLbl.setText(row.name);
                nameLbl.setTypeface(Typeface.DEFAULT_BOLD);
                iconLbl.setTextColor(getResources().getColor(android.R.color.black));
                if (mResultDictionary.containsKey(row.id)) {
                    for (String itemId : mResultDictionary.get(row.id).keySet()) {
                        AdvancedOrderItem item = mResultDictionary.get(row.id).get(itemId);
                        if (item.value > 0) {
                            iconLbl.setTextColor(getResources().getColor(R.color.mc_divider_green));
                            break;
                        }
                    }
                }
                if (row.id.equalsIgnoreCase(mActiveCategoryId)) {
                    iconLbl.setText(R.string.fa_angle_up);
                } else {
                    iconLbl.setText(R.string.fa_angle_down);
                }

            } else {
                final TextView priceLbl = (TextView) v.findViewById(R.id.price);
                final TextView countLbl = (TextView) v.findViewById(R.id.count);
                final AdvancedOrderCategoryItemRow row = (AdvancedOrderCategoryItemRow) rowObject;
                nameLbl.setText(row.name);
                nameLbl.setTypeface(Typeface.DEFAULT);
                if (row.hasPrice) {
                    priceLbl.setText(getPriceStringForRow(row));
                    priceLbl.setVisibility(View.VISIBLE);
                } else {
                    priceLbl.setVisibility(View.GONE);
                }

                priceLbl.setTextColor(getResources().getColor(R.color.mc_divider_gray));
                if (row.value > 0) {
                    iconLbl.setVisibility(View.GONE);
                    countLbl.setVisibility(View.VISIBLE);
                    countLbl.setText(getValueStringForRow(row));
                    correctCountWidth(v, row);
                } else {
                    countLbl.setVisibility(View.GONE);
                    iconLbl.setVisibility(View.VISIBLE);
                    iconLbl.setTextColor(getResources().getColor(R.color.mc_divider_gray));
                    iconLbl.setText(R.string.fa_plus_circle);
                }

                correctNameAndPriceWidth(v, row);
            }
            return v;
        }

        private void onItemClick(AdapterView<?> parent, final View view, final int position, long id) {
            final Object rowObject = mData.get(position);
            if (rowObject instanceof AdvancedOrderCategoryRow) {
                final AdvancedOrderCategoryRow row = (AdvancedOrderCategoryRow) rowObject;
                if (row.id.equalsIgnoreCase(mActiveCategoryId)) {
                    mActiveCategoryId = null;
                } else {
                    mActiveCategoryId = row.id;
                }
                renderListData();
                mListAdapter.notifyDataSetChanged();
                UIUtils.setListViewHeightBasedOnItems(mListView);

                if (mActiveCategoryId != null) {

                    mActivity.getMainService().postOnUIHandler(new SafeRunnable() {
                        @Override
                        protected void safeRun() throws Exception {
                            int position = 0;
                            for (AdvancedOrderCategory category : mAdvancedOrder.categories) {
                                if (category.id.equals(row.id)) {
                                    scrollToCategory(mListView.getChildAt(position));
                                    break;
                                }
                                position += 1;
                            }
                        }
                    });
                }
            } else {
                final AdvancedOrderCategoryItemRow row = (AdvancedOrderCategoryItemRow) rowObject;
                if (row.value == 0) {
                    long value = excecutePlusValuePress(row);
                    AdvancedOrderCategoryItemRow tmpRow = (AdvancedOrderCategoryItemRow) mData.get(position);
                    tmpRow.value = value;
                    mData.set(position, tmpRow);
                    mListAdapter.notifyDataSetChanged();
                    row.value = value;
                }
                showAdvancedOrderItemDetail(position, row);
            }
        }
    }

    private int getRelativeTop(ScrollView rootView, View myView) {
        if (myView.getParent() == rootView) {
            return myView.getTop();
        } else {
            return myView.getTop() + getRelativeTop(rootView, (View) myView.getParent());
        }
    }

    private void scrollToCategory(View view) {
        ScrollView v = (ScrollView) mActivity.findViewById(R.id.message_scroll_view);
        int relativeTop = getRelativeTop(v, view);
        v.smoothScrollTo(0, relativeTop);
    }

    private class AdvancedOrderBasketAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            T.UI();
            if (mDataBasket == null)
                return 0;

            return mDataBasket.size();
        }

        @Override
        public Object getItem(int position) {
            T.UI();
            return null;
        }

        @Override
        public long getItemId(int position) {
            T.UI();
            return 0;
        }

        @SuppressLint("ViewHolder")
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            T.UI();
            final View v = LayoutInflater.from(mActivity).inflate(R.layout.widget_advanced_order_basket_item, parent,
                false);

            final TextView nameLbl = (TextView) v.findViewById(R.id.name);
            final TextView priceLbl = (TextView) v.findViewById(R.id.price);
            final TextView countLbl = (TextView) v.findViewById(R.id.count);
            final LinearLayout details = (LinearLayout) v.findViewById(R.id.details);
            final TextView minValueLbl = (TextView) v.findViewById(R.id.value_min);
            final TextView valueLbl = (TextView) v.findViewById(R.id.value);
            final TextView plusValueLbl = (TextView) v.findViewById(R.id.value_plus);

            final AdvancedOrderCategoryItemRow row = mDataBasket.get(position);
            nameLbl.setText(row.name);
            nameLbl.setTypeface(Typeface.DEFAULT);
            priceLbl.setText(getPriceStringForRow(row));
            priceLbl.setTextColor(getResources().getColor(R.color.mc_divider_gray));

            if (row.expanded) {
                countLbl.setVisibility(View.GONE);
                details.setVisibility(View.VISIBLE);
                minValueLbl.setText(R.string.fa_minus_circle);
                minValueLbl.setTypeface(mFontAwesomeTypeFace);
                minValueLbl.setTextColor(getResources().getColor(R.color.mc_red_divider));

                minValueLbl.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        long value = excecuteMinValuePress(row);
                        row.value = value;
                        mDataBasket.set(position, row);
                        mBasketListAdapter.notifyDataSetChanged();

                        renderListData();
                        mListAdapter.notifyDataSetChanged();
                    }
                });

                valueLbl.setText(getValueStringForRow(row));

                plusValueLbl.setText(R.string.fa_plus_circle);
                plusValueLbl.setTypeface(mFontAwesomeTypeFace);
                plusValueLbl.setTextColor(getResources().getColor(R.color.mc_divider_green));

                plusValueLbl.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        long value = excecutePlusValuePress(row);
                        row.value = value;
                        mDataBasket.set(position, row);
                        mBasketListAdapter.notifyDataSetChanged();

                        renderListData();
                        mListAdapter.notifyDataSetChanged();
                    }
                });
            } else {
                details.setVisibility(View.GONE);
                countLbl.setVisibility(View.VISIBLE);
                countLbl.setText(getValueStringForRow(row));
                correctCountWidth(v, row);
            }

            correctNameAndPriceWidth(v, row);

            return v;
        }

        private void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
            AdvancedOrderCategoryItemRow row = mDataBasket.get(position);
            row.expanded = !row.expanded;
            mDataBasket.set(position, row);
            mBasketListAdapter.notifyDataSetChanged();

            setBasketListViewHeight();
        }
    }

    private void correctNameAndPriceWidth(final View v, final AdvancedOrderCategoryItemRow row) {
        mActivity.getMainService().postOnUIHandler(new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                TextView nameLbl = (TextView) v.findViewById(R.id.name);
                if (!row.name.equals(nameLbl.getText())) {
                    return;
                }
                LinearLayout txtContainer = (LinearLayout) v.findViewById(R.id.text_container);
                txtContainer.measure(0, 0);
                int currentWidth = txtContainer.getMeasuredWidth();
                int maxWidth = txtContainer.getWidth() - UIUtils.convertDipToPixels(mActivity, 35);

                nameLbl.measure(0, 0);
                int nameWidth = nameLbl.getMeasuredWidth();
                int priceWidth = 0;
                if (row.hasPrice) {
                    TextView priceLbl = (TextView) v.findViewById(R.id.price);
                    priceLbl.measure(0, 0);
                    priceWidth = priceLbl.getMeasuredWidth();
                    ViewGroup.LayoutParams lpPrice = priceLbl.getLayoutParams();
                    lpPrice.width = priceWidth;
                    priceLbl.setLayoutParams(lpPrice);
                    priceLbl.requestLayout();
                }

                ViewGroup.LayoutParams lpName = nameLbl.getLayoutParams();
                if (maxWidth > 0 && currentWidth < maxWidth) {
                    if (maxWidth - priceWidth > nameWidth) {
                        lpName.width = nameWidth;
                    } else {
                        lpName.width = maxWidth - priceWidth;
                    }
                } else {
                    lpName.width = maxWidth - priceWidth;
                }

                nameLbl.setLayoutParams(lpName);
                nameLbl.requestLayout();
            }
        });
    }

    private void correctCountWidth(final View v, final AdvancedOrderCategoryItemRow row) {
        mActivity.getMainService().postOnUIHandler(new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                TextView nameLbl = (TextView) v.findViewById(R.id.name);
                if (!row.name.equals(nameLbl.getText())) {
                    return;
                }
                TextView countLbl = (TextView) v.findViewById(R.id.count);
                countLbl.measure(0, 0);
                int countWidth = countLbl.getMeasuredWidth();
                if (countWidth > 140) {
                    ViewGroup.LayoutParams lpCount = countLbl.getLayoutParams();
                    lpCount.width = 140;
                    countLbl.setLayoutParams(lpCount);
                    countLbl.requestLayout();
                }
            }
        });
    }

    private Typeface mFontAwesomeTypeFace;
    private LayoutInflater mLayoutInFlater;
    private String mActiveCategoryId = null;
    private List<Object> mData;
    private List<AdvancedOrderCategoryItemRow> mDataBasket;
    private CachedDownloader mCachedDownloader;
    private BroadcastReceiver mBroadcastReceiver;

    private TextView mBasketBtn;
    private ListView mListView;
    private AdvancedOrderAdapter mListAdapter = new AdvancedOrderAdapter();
    private Dialog mDetailDialog;
    private int mCurrentItemDetail = -1;
    private Dialog mBasketDialog;
    private AdvancedOrderBasketAdapter mBasketListAdapter = new AdvancedOrderBasketAdapter();

    private AdvancedOrderTO mAdvancedOrder = null;
    private Map<String, Map<String, AdvancedOrderItem>> mAdvancedOrderDictionary = null;
    private Map<String, Map<String, AdvancedOrderItem>> mResultDictionary = null;

    private AdvancedOrderWidgetResult mResult = null;

    public AdvancedOrderWidget(Context context) {
        super(context);
    }

    public AdvancedOrderWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onServiceUnbound() {
        if (mBroadcastReceiver != null)
            mActivity.unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    public void initializeWidget() {
        T.UI();

        if (!isEnabled()) {
            LinearLayout advancedOrderContainer = (LinearLayout) findViewById(R.id.advanced_order_container);
            advancedOrderContainer.setVisibility(View.GONE);
            TextView advancedOrderLocked = (TextView) findViewById(R.id.advanced_order_locked);
            advancedOrderLocked.setVisibility(View.VISIBLE);
            advancedOrderLocked.setText(AdvancedOrderWidget.valueString(mActivity, mWidgetMap));
            return;
        }

        mCachedDownloader = CachedDownloader.getInstance(mActivity.getMainService());
        mLayoutInFlater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mAdvancedOrderDictionary = new HashMap<String, Map<String, AdvancedOrderItem>>();
        try {
            mAdvancedOrder = new AdvancedOrderTO(mWidgetMap);
            for (AdvancedOrderCategory category : mAdvancedOrder.categories) {
                Map<String, AdvancedOrderItem> items = new HashMap<String, AdvancedOrderItem>();
                for (AdvancedOrderItem item : category.items) {
                    items.put(item.id, item);
                }
                mAdvancedOrderDictionary.put(category.id, items);
            }
        } catch (IncompleteMessageException e) {
            L.bug(e);
            return;
        }

        mResultDictionary = new HashMap<String, Map<String, AdvancedOrderItem>>();
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) mWidgetMap.get("value");
        if (result != null) {
            try {
                AdvancedOrderWidgetResult r = new AdvancedOrderWidgetResult(result);
                for (AdvancedOrderCategory category : r.categories) {
                    Map<String, AdvancedOrderItem> items = new HashMap<String, AdvancedOrderItem>();
                    for (AdvancedOrderItem item : category.items) {
                        items.put(item.id, item);
                    }
                    mResultDictionary.put(category.id, items);
                }
            } catch (IncompleteMessageException e) {
                L.bug(e); // Should never happen
            }
        } else {
            for (AdvancedOrderCategory category : mAdvancedOrder.categories) {
                Map<String, AdvancedOrderItem> items = new HashMap<String, AdvancedOrderItem>();
                for (AdvancedOrderItem item : category.items) {
                    if (item.value > 0) {
                        items.put(item.id, item);
                    }
                    if (items.size() > 0) {
                        mResultDictionary.put(category.id, items);
                    }
                }
            }
        }
        renderListData();

        mFontAwesomeTypeFace = Typeface.createFromAsset(mActivity.getAssets(), "FontAwesome.ttf");
        mBasketBtn = (TextView) findViewById(R.id.basket);
        mBasketBtn.setText(R.string.fa_shopping_cart);
        mBasketBtn.setTypeface(mFontAwesomeTypeFace);

        if (numberOfItemsInBasket() > 0) {
            mBasketBtn.setEnabled(true);
            mBasketBtn.setTextColor(mTextColor);
        } else {
            mBasketBtn.setEnabled(false);
            mBasketBtn.setTextColor(getResources().getColor(R.color.mc_divider_gray));
        }

        mBasketBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                renderBasketListData();
                showAdvancedOrderBasket();
            }
        });

        mListView = (ListView) findViewById(R.id.list_view);
        mListView.setAdapter(mListAdapter);
        mListView.setScrollContainer(false);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mListAdapter.onItemClick(parent, view, position, id);
            }
        });

        mActivity.getMainService().postOnUIHandler(new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                UIUtils.setListViewHeightBasedOnItems(mListView);
            }
        });

        mBroadcastReceiver = getBroadcastReceiver();
        final IntentFilter filter = getIntentFilter();
        mActivity.registerReceiver(mBroadcastReceiver, filter);
    }

    protected SafeBroadcastReceiver getBroadcastReceiver() {

        return new SafeBroadcastReceiver() {
            @Override
            public String[] onSafeReceive(Context context, Intent intent) {
                T.UI();
                String action = intent.getAction();
                if (CachedDownloader.CACHED_DOWNLOAD_AVAILABLE_INTENT.equals(action)) {
                    if (mDetailDialog != null && mDetailDialog.isShowing() && mCurrentItemDetail != -1) {
                        AdvancedOrderCategoryItemRow tmpRow = (AdvancedOrderCategoryItemRow) mData
                            .get(mCurrentItemDetail);

                        if (!TextUtils.isEmptyOrWhitespace(tmpRow.imageUrl)) {
                            String url = intent.getStringExtra("url");
                            final Resizable16by9ImageView imageView = (Resizable16by9ImageView) mDetailDialog
                                .findViewById(R.id.image);

                            File cachedFile = mCachedDownloader.getCachedFilePath(url);
                            if (cachedFile != null) {
                                Bitmap bm = BitmapFactory.decodeFile(cachedFile.getAbsolutePath());
                                imageView.setImageBitmap(bm);
                                imageView.setVisibility(View.VISIBLE);
                            }
                        }
                    }

                    return new String[] { action };
                }
                return null;
            }
        };
    }

    protected IntentFilter getIntentFilter() {
        final IntentFilter filter = new IntentFilter(CachedDownloader.CACHED_DOWNLOAD_AVAILABLE_INTENT);
        return filter;
    }

    private void renderListData() {
        mData = new ArrayList<Object>();
        for (AdvancedOrderCategory category : mAdvancedOrder.categories) {
            mData.add(new AdvancedOrderCategoryRow(category.id, category.name));
            if (category.id.equalsIgnoreCase(mActiveCategoryId)) {
                for (AdvancedOrderItem item : category.items) {
                    if (mResultDictionary.containsKey(category.id)) {
                        if (mResultDictionary.get(category.id).containsKey(item.id)) {
                            item.value = mResultDictionary.get(category.id).get(item.id).value;
                        }
                    }

                    mData.add(new AdvancedOrderCategoryItemRow(category.id, item.description, item.id, item.image_url,
                        item.name, item.step, item.step_unit, item.step_unit_conversion, item.unit, item.unit_price,
                        item.value, item.has_price, false));
                }
            }
        }
    }

    private void renderBasketListData() {
        mDataBasket = new ArrayList<AdvancedOrderCategoryItemRow>();
        for (AdvancedOrderCategory category : mAdvancedOrder.categories) {
            for (AdvancedOrderItem item : category.items) {
                if (mResultDictionary.containsKey(category.id)) {
                    if (mResultDictionary.get(category.id).containsKey(item.id)) {
                        item.value = mResultDictionary.get(category.id).get(item.id).value;
                        if (item.value > 0) {
                            mDataBasket.add(new AdvancedOrderCategoryItemRow(category.id, item.description, item.id,
                                item.image_url, item.name, item.step, item.step_unit, item.step_unit_conversion,
                                item.unit, item.unit_price, item.value, item.has_price, false));
                        }
                    }
                }
            }
        }
    }

    private int numberOfItemsInBasket() {
        int count = 0;

        if (mResultDictionary.size() > 0) {
            for (String categoryKey : mResultDictionary.keySet()) {
                for (String itemKey : mResultDictionary.get(categoryKey).keySet()) {
                    AdvancedOrderItem item = mResultDictionary.get(categoryKey).get(itemKey);
                    if (item.value > 0) {
                        count = count + 1;
                    }
                }
            }
        }

        return count;
    }

    private void setDefaultValueInResultDict(String categoryId, String itemId) {
        if (!mResultDictionary.containsKey(categoryId)) {
            mResultDictionary.put(categoryId, new HashMap<String, AdvancedOrderItem>());
        }

        if (!mResultDictionary.get(categoryId).containsKey(itemId)) {
            AdvancedOrderItem item = mAdvancedOrderDictionary.get(categoryId).get(itemId);
            mResultDictionary.get(categoryId).put(itemId, item);
        }
    }

    private long excecuteMinValuePress(final AdvancedOrderCategoryItemRow row) {
        setDefaultValueInResultDict(row.categoryId, row.id);
        AdvancedOrderItem item = mResultDictionary.get(row.categoryId).get(row.id);
        item.value = item.value - item.step;
        if (item.value < 0) {
            item.value = 0;
        }

        if (numberOfItemsInBasket() > 0) {
            mBasketBtn.setEnabled(true);
            mBasketBtn.setTextColor(mTextColor);
        } else {
            mBasketBtn.setEnabled(false);
            mBasketBtn.setTextColor(getResources().getColor(R.color.mc_divider_gray));
        }

        return item.value;
    }

    private long excecutePlusValuePress(final AdvancedOrderCategoryItemRow row) {
        setDefaultValueInResultDict(row.categoryId, row.id);
        AdvancedOrderItem item = mResultDictionary.get(row.categoryId).get(row.id);
        item.value = item.value + item.step;

        if (numberOfItemsInBasket() > 0) {
            mBasketBtn.setEnabled(true);
            mBasketBtn.setTextColor(mTextColor);
        } else {
            mBasketBtn.setEnabled(false);
            mBasketBtn.setTextColor(getResources().getColor(R.color.mc_divider_gray));
        }

        return item.value;
    }

    @SuppressLint("DefaultLocale")
    private String getPriceStringForRow(AdvancedOrderCategoryItemRow row) {
        return String.format("%s%.2f / %s", mAdvancedOrder.currency, row.unitPrice / 100.0, row.unit);
    }

    private String getValueStringForRow(AdvancedOrderCategoryItemRow row) {
        if (TextUtils.isEmptyOrWhitespace(row.stepUnit)) {
            return String.format("%s %s", row.value, row.unit);
        } else {
            return String.format("%s %s", row.value, row.stepUnit);
        }
    }

    @Override
    public void putValue() {
        mResult = new AdvancedOrderWidgetResult();
        mResult.currency = mAdvancedOrder.currency;
        List<AdvancedOrderCategory> categories = new ArrayList<AdvancedOrderCategory>();
        for (AdvancedOrderCategory category : mAdvancedOrder.categories) {
            List<AdvancedOrderItem> items = new ArrayList<AdvancedOrderItem>();
            for (AdvancedOrderItem item : category.items) {
                if (item.value > 0) {
                    items.add(item);
                }
            }
            if (items.size() > 0) {
                AdvancedOrderCategory tmpCategory = new AdvancedOrderCategory();
                tmpCategory.id = category.id;
                tmpCategory.name = category.name;
                tmpCategory.items = new AdvancedOrderItem[items.size()];
                for (int i = 0; i < items.size(); i++) {
                    tmpCategory.items[i] = items.get(i);
                }
                categories.add(tmpCategory);
            }
        }
        mResult.categories = new AdvancedOrderCategory[categories.size()];
        for (int i = 0; i < categories.size(); i++) {
            mResult.categories[i] = categories.get(i);
        }
        mWidgetMap.put("value", mResult.toJSONMap());
    }

    @Override
    public AdvancedOrderWidgetResult getFormResult() {
        return mResult;
    }

    @Override
    public void submit(String buttonId, long timestamp) throws Exception {
        SubmitAdvancedOrderFormRequestTO request = new SubmitAdvancedOrderFormRequestTO();
        request.button_id = buttonId;
        request.message_key = mMessage.key;
        request.parent_message_key = mMessage.parent_key;
        request.timestamp = timestamp;
        if (Message.POSITIVE.equals(buttonId)) {
            request.result = getFormResult();
            L.d("Submit Advanced Order " + mWidgetMap);
        }
        if ((mMessage.flags & MessagingPlugin.FLAG_SENT_BY_JSMFR) == MessagingPlugin.FLAG_SENT_BY_JSMFR)
            mPlugin.answerJsMfrMessage(mMessage, request.toJSONMap(),
                "com.mobicage.api.messaging.submitAdvancedOrderForm", mActivity, mParentView);
        else
            Rpc.submitAdvancedOrderForm(new ResponseHandler<SubmitAdvancedOrderFormResponseTO>(), request);
    }

    public static String valueString(Context context, Map<String, Object> widget) {
        @SuppressWarnings("unchecked")
        final Map<String, Object> jsonResult = (Map<String, Object>) widget.get("value");
        if (jsonResult == null) {
            return "";
        }

        final AdvancedOrderWidgetResult result;
        try {
            result = new AdvancedOrderWidgetResult(jsonResult);
        } catch (IncompleteMessageException e) {
            L.bug(e);
            return "";
        }

        final List<String> parts = new ArrayList<String>();
        for (int i = 0; i < result.categories.length; i++) {
            AdvancedOrderCategory category = result.categories[i];
            if (i != 0) {
                parts.add("");
            }
            parts.add(String.format("%s: ", category.name));
            for (AdvancedOrderItem item : category.items) {
                String unit = item.unit;
                if (!TextUtils.isEmptyOrWhitespace(item.step_unit)) {
                    unit = item.step_unit;
                }
                parts.add(String.format("\t* %s, %s %s", item.name, item.value, unit));
            }
        }
        return android.text.TextUtils.join("\n", parts);
    }

    private void showAdvancedOrderItemDetail(final int position, final AdvancedOrderCategoryItemRow row) {
        if (mDetailDialog != null && mDetailDialog.isShowing())
            return;
        mCurrentItemDetail = position;
        final View v = mLayoutInFlater.inflate(R.layout.widget_advanced_order_item_detail, this, false);

        mDetailDialog = new Dialog(mActivity);
        mDetailDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDetailDialog.setContentView(v);
        mDetailDialog.setCanceledOnTouchOutside(true);
        mDetailDialog.setCancelable(true);

        final TextView nameLbl = (TextView) v.findViewById(R.id.name);
        final TextView priceLbl = (TextView) v.findViewById(R.id.price);
        final TextView descriptionLbl = (TextView) v.findViewById(R.id.description);
        final Resizable16by9ImageView imageView = (Resizable16by9ImageView) v.findViewById(R.id.image);
        final TextView minValueBtn = (TextView) v.findViewById(R.id.value_min);
        final TextView valueLbl = (TextView) v.findViewById(R.id.value);
        final TextView plusValueBtn = (TextView) v.findViewById(R.id.value_plus);

        final Button dismissBtn = (Button) v.findViewById(R.id.dismiss);

        nameLbl.setText(row.name);
        nameLbl.setTextColor(getResources().getColor(android.R.color.black));

        if (row.hasPrice) {
            priceLbl.setVisibility(View.VISIBLE);
            priceLbl.setText(getPriceStringForRow(row));
            priceLbl.setTextColor(getResources().getColor(R.color.mc_divider_gray));
        } else {
            priceLbl.setVisibility(View.GONE);
        }

        if (TextUtils.isEmptyOrWhitespace(row.description)) {
            descriptionLbl.setVisibility(View.GONE);
        } else {
            descriptionLbl.setVisibility(View.VISIBLE);
            descriptionLbl.setText(row.description);
            descriptionLbl.setTextColor(getResources().getColor(android.R.color.black));
        }

        minValueBtn.setText(R.string.fa_minus_circle);
        minValueBtn.setTypeface(mFontAwesomeTypeFace);
        minValueBtn.setTextColor(getResources().getColor(R.color.mc_red_divider));

        minValueBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                long value = excecuteMinValuePress(row);
                AdvancedOrderCategoryItemRow tmpRow = (AdvancedOrderCategoryItemRow) mData.get(position);
                tmpRow.value = value;
                mData.set(position, tmpRow);
                valueLbl.setText(getValueStringForRow(tmpRow));
                mListAdapter.notifyDataSetChanged();
            }
        });

        valueLbl.setText(getValueStringForRow(row));

        plusValueBtn.setText(R.string.fa_plus_circle);
        plusValueBtn.setTypeface(mFontAwesomeTypeFace);
        plusValueBtn.setTextColor(getResources().getColor(R.color.mc_divider_green));

        plusValueBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                long value = excecutePlusValuePress(row);
                AdvancedOrderCategoryItemRow tmpRow = (AdvancedOrderCategoryItemRow) mData.get(position);
                tmpRow.value = value;
                mData.set(position, tmpRow);
                valueLbl.setText(getValueStringForRow(tmpRow));
                mListAdapter.notifyDataSetChanged();
            }
        });

        dismissBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mDetailDialog.dismiss();
            }
        });

        if (!TextUtils.isEmptyOrWhitespace(row.imageUrl)) {
            if (mCachedDownloader.isStorageAvailable()) {
                File cachedFile = mCachedDownloader.getCachedFilePath(row.imageUrl);
                if (cachedFile != null) {
                    Bitmap bm = BitmapFactory.decodeFile(cachedFile.getAbsolutePath());
                    imageView.setImageBitmap(bm);
                    imageView.setVisibility(View.VISIBLE);
                } else {
                    // item started downloading intent when ready
                }
            } else {
                new DownloadImageTask(imageView).execute(row.imageUrl);
            }
        }

        mDetailDialog.show();
    }

    private class DownloadImageTask extends SafeAsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        @Override
        protected Bitmap safeDoInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                L.d("AdvancedOrder DownloadImageTask error", e);
            }
            return mIcon11;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (result != null) {
                bmImage.setImageBitmap(result);
                bmImage.setVisibility(View.VISIBLE);
            }
        }
    }

    private void showAdvancedOrderBasket() {
        if (mBasketDialog != null && mBasketDialog.isShowing())
            return;
        final View v = mLayoutInFlater.inflate(R.layout.widget_advanced_order_basket, this, false);
        mBasketDialog = new Dialog(mActivity);
        mBasketDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mBasketDialog.setContentView(v);
        mBasketDialog.setCanceledOnTouchOutside(true);
        mBasketDialog.setCancelable(true);

        final ListView listView = (ListView) v.findViewById(R.id.list_view);
        final Button submitBtn = (Button) v.findViewById(R.id.submit);
        final Button dismissBtn = (Button) v.findViewById(R.id.dismiss);

        listView.setAdapter(mBasketListAdapter);
        listView.setScrollContainer(false);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mBasketListAdapter.onItemClick(parent, view, position, id);
            }
        });

        submitBtn.setText((String) mMessage.form.get("positive_button"));

        submitBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mBasketDialog.dismiss();
                mActivity.excecutePositiveButtonClick();
            }
        });

        dismissBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mBasketDialog.dismiss();
            }
        });

        mActivity.getMainService().postOnUIHandler(new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                setBasketListViewHeight();
            }
        });

        mBasketDialog.show();
    }

    private void setBasketListViewHeight() {
        final Point displaySize = UIUtils.getDisplaySize(mActivity);
        final TextView nameLbl = (TextView) mBasketDialog.findViewById(R.id.name);
        final ListView listView = (ListView) mBasketDialog.findViewById(R.id.list_view);
        final LinearLayout buttonContainer = (LinearLayout) mBasketDialog.findViewById(R.id.button_container);
        int maxPopupHeight = (int) Math.floor(displaySize.y * 0.75);
        nameLbl.measure(0, 0);
        int nameLblHeight = nameLbl.getMeasuredHeight();
        buttonContainer.measure(0, 0);
        int buttonContainerHeight = buttonContainer.getMeasuredHeight();
        UIUtils.setListViewHeightBasedOnItems(listView, maxPopupHeight - nameLblHeight - buttonContainerHeight);
    }
}
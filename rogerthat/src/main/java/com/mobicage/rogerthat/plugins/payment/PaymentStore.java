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

package com.mobicage.rogerthat.plugins.payment;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.text.TextUtils;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.MainService;
import com.mobicage.rogerthat.util.db.DatabaseManager;
import com.mobicage.rogerthat.util.db.TransactionHelper;
import com.mobicage.rogerthat.util.db.TransactionWithoutResult;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rpc.IncompleteMessageException;
import com.mobicage.to.payment.AppPaymentProviderTO;
import com.mobicage.to.payment.PaymentAssetBalanceTO;
import com.mobicage.to.payment.PaymentAssetRequiredActionTO;
import com.mobicage.to.payment.PaymentProviderAssetTO;

import org.json.simple.JSONValue;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.mobicage.rogerthat.util.db.DBUtils.bindString;


public class PaymentStore implements Closeable {

    private final MainService mMainService;
    private final SQLiteDatabase mDb;

    private final SQLiteStatement mInsertPaymentProvider;
    private final SQLiteStatement mInsertPaymentAsset;

    private final SQLiteStatement mDeletePaymentProvider;
    private final SQLiteStatement mDeletePaymentAsset;

    private final SQLiteStatement mClearPaymentProviders;
    private final SQLiteStatement mClearPaymentAssets;

    public PaymentStore(final MainService mainService, final DatabaseManager dbManager) {
        T.UI();
        mMainService = mainService;
        mDb = dbManager.getDatabase();

        mInsertPaymentProvider = mDb.compileStatement(mMainService.getString(R.string.sql_payment_provider_insert));
        mInsertPaymentAsset = mDb.compileStatement(mMainService.getString(R.string.sql_payment_asset_insert));

        mDeletePaymentProvider = mDb.compileStatement(mMainService.getString(R.string.sql_payment_provider_delete));
        mDeletePaymentAsset = mDb.compileStatement(mMainService.getString(R.string.sql_payment_asset_delete));

        mClearPaymentProviders = mDb.compileStatement(mMainService.getString(R.string.sql_payment_provider_clear));
        mClearPaymentAssets = mDb.compileStatement(mMainService.getString(R.string.sql_payment_asset_clear));
    }

    @Override
    public void close() {
        T.UI();
        mInsertPaymentProvider.close();
        mInsertPaymentAsset.close();

        mDeletePaymentProvider.close();
        mDeletePaymentAsset.close();

        mClearPaymentProviders.close();
        mClearPaymentAssets.close();
    }

    public SQLiteDatabase getDatabase() {
        return mDb;
    }

    public void deletePaymentProviders(final String[] providerIds) {
        T.dontCare();
        TransactionHelper.runInTransaction(getDatabase(), "deletePaymentProviders", new TransactionWithoutResult() {
            @Override
            protected void run() {
                if (providerIds.length == 0) {
                    mClearPaymentProviders.execute();
                }

                for (String providerId : providerIds) {
                    bindString(mDeletePaymentProvider, 1, providerId);
                    mDeletePaymentProvider.execute();
                }
            }
        });
    }

    public void savePaymentProvider(final AppPaymentProviderTO paymentProvider) {
        T.dontCare();
        bindString(mInsertPaymentProvider, 1, paymentProvider.id);
        bindString(mInsertPaymentProvider, 2, paymentProvider.name);
        bindString(mInsertPaymentProvider, 3, paymentProvider.logo_url);
        mInsertPaymentProvider.bindLong(4, paymentProvider.version);
        bindString(mInsertPaymentProvider, 5, paymentProvider.description);
        bindString(mInsertPaymentProvider, 6, paymentProvider.oauth_authorize_url);
        bindString(mInsertPaymentProvider, 7, paymentProvider.black_white_logo);
        bindString(mInsertPaymentProvider, 8, paymentProvider.background_color);
        bindString(mInsertPaymentProvider, 9, paymentProvider.text_color);
        bindString(mInsertPaymentProvider, 10, paymentProvider.button_color);
        bindString(mInsertPaymentProvider, 11, TextUtils.join(",", paymentProvider.currencies));
        bindString(mInsertPaymentProvider, 12, TextUtils.join(",", paymentProvider.asset_types));
        mInsertPaymentProvider.execute();
    }

    private AppPaymentProviderTO readPaymentProvider(Cursor c) {
        T.dontCare();
        AppPaymentProviderTO to = new AppPaymentProviderTO();
        to.id = c.getString(0);
        to.name = c.getString(1);
        to.logo_url = c.getString(2);
        to.version = c.getLong(3);
        to.description = c.getString(4);
        to.oauth_authorize_url = c.getString(5);
        to.black_white_logo = c.getString(6);
        to.background_color = c.getString(7);
        to.text_color = c.getString(8);
        to.button_color = c.getString(9);
        to.currencies = c.getString(10).split(",");
        to.asset_types = c.getString(11).split(",");
        to.enabled = true;
        return to;
    }

    public List<String> getPaymentProvidersIds() {
        T.dontCare();
        final Cursor c = mDb.rawQuery(mMainService.getString(R.string.sql_payment_provider_select), new String[]{});
        List<String> items = new ArrayList<>();
        try {
            if (!c.moveToFirst()) {
                return items;
            }
            do {
                items.add(c.getString(0));
            } while (c.moveToNext());
        } finally {
            c.close();
        }
        return items;
    }

    public List<AppPaymentProviderTO> getPaymentProviders() {
        T.dontCare();
        final Cursor c = mDb.rawQuery(mMainService.getString(R.string.sql_payment_provider_select), new String[]{});
        List<AppPaymentProviderTO> items = new ArrayList<>();
        try {
            if (!c.moveToFirst()) {
                return items;
            }
            do {
                items.add(readPaymentProvider(c));
            } while (c.moveToNext());
        } finally {
            c.close();
        }
        return items;
    }

    public AppPaymentProviderTO getPaymentProvider(final String providerId) {
        T.dontCare();
        final Cursor c = mDb.rawQuery(mMainService.getString(R.string.sql_payment_provider_select_by_id), new String[]{providerId});
        try {
            if (!c.moveToFirst()) {
                return null;
            }
            return readPaymentProvider(c);
        } finally {
            c.close();
        }
    }

    public void deletePaymentAssets(final String[] providerIds) {
        T.dontCare();
        TransactionHelper.runInTransaction(getDatabase(), "deletePaymentAssets", new TransactionWithoutResult() {
            @Override
            protected void run() {
                if (providerIds.length == 0) {
                    mClearPaymentAssets.execute();
                }

                for (String providerId : providerIds) {
                    bindString(mDeletePaymentAsset, 1, providerId);
                    mDeletePaymentAsset.execute();
                }
            }
        });
    }

    public void savePaymentAsset(final PaymentProviderAssetTO asset) {
        T.dontCare();
        String requiredAction = asset.required_action == null ? null : JSONValue.toJSONString(asset.required_action.toJSONMap());
        String availableBalance = asset.available_balance == null ? null : JSONValue.toJSONString(asset.available_balance.toJSONMap());
        String totalBalance = asset.total_balance == null ? null : JSONValue.toJSONString(asset.total_balance.toJSONMap());
        bindString(mInsertPaymentAsset, 1, asset.provider_id);
        bindString(mInsertPaymentAsset, 2, asset.id);
        bindString(mInsertPaymentAsset, 3, asset.type);
        bindString(mInsertPaymentAsset, 4, asset.name);
        bindString(mInsertPaymentAsset, 5, asset.currency);
        bindString(mInsertPaymentAsset, 6, availableBalance);
        bindString(mInsertPaymentAsset, 7, totalBalance);
        mInsertPaymentAsset.bindLong(8, asset.verified ? 1 : 0);
        mInsertPaymentAsset.bindLong(9, asset.enabled ? 1 : 0);
        mInsertPaymentAsset.bindLong(10, asset.has_balance ? 1 : 0);
        mInsertPaymentAsset.bindLong(11, asset.has_transactions ? 1 : 0);
        bindString(mInsertPaymentAsset, 12, requiredAction);
        mInsertPaymentAsset.execute();
    }

    private PaymentProviderAssetTO readPaymentAsset(Cursor c) {
        T.dontCare();
        PaymentProviderAssetTO to = new PaymentProviderAssetTO();
        to.provider_id = c.getString(0);
        to.id = c.getString(1);
        to.type = c.getString(2);
        to.name = c.getString(3);
        to.currency = c.getString(4);
        String availableBalance = c.getString(5);
        if (availableBalance == null || "".equals(availableBalance)) {
            to.available_balance = null;
        } else {
            try {
                Map<String, Object> json = (Map<String, Object>) JSONValue.parse(availableBalance);
                to.available_balance = new PaymentAssetBalanceTO(json);
            } catch (IncompleteMessageException e) {
                L.bug(e);
                to.available_balance = null;
            }
        }
        String totalBalance = c.getString(6);
        if (totalBalance == null || "".equals(totalBalance)) {
            to.total_balance = null;
        } else {
            try {
                Map<String, Object> json = (Map<String, Object>) JSONValue.parse(totalBalance);
                to.total_balance = new PaymentAssetBalanceTO(json);
            } catch (IncompleteMessageException e) {
                L.bug(e);
                to.total_balance = null;
            }
        }
        to.verified = c.getLong(7) > 0;
        to.enabled = c.getLong(8) > 0;
        to.has_balance = c.getLong(9) > 0;
        to.has_transactions = c.getLong(10) > 0;
        String actionString = c.getString(11);
        if (actionString == null || "".equals(actionString)) {
            to.required_action = null;
        } else {
            try {
                Map<String, Object> json = (Map<String, Object>) JSONValue.parse(actionString);
                to.required_action = new PaymentAssetRequiredActionTO(json);
            } catch (IncompleteMessageException e) {
                L.bug(e);
                to.required_action = null;
            }
        }
        return to;
    }

    public List<PaymentProviderAssetTO> getPaymentAssets(final String providerId) {
        T.dontCare();
        final Cursor c;
        if (providerId == null) {
            c = mDb.rawQuery(mMainService.getString(R.string.sql_payment_asset_select), new String[]{});
        } else {
            c = mDb.rawQuery(mMainService.getString(R.string.sql_payment_asset_select_by_provider_id), new String[]{providerId});
        }
        List<PaymentProviderAssetTO> items = new ArrayList<>();
        try {
            if (!c.moveToFirst()) {
                return items;
            }
            do {
                items.add(readPaymentAsset(c));
            } while (c.moveToNext());
        } finally {
            c.close();
        }
        return items;
    }

    public PaymentProviderAssetTO getPaymentAsset(final String providerId, final String assetId) {
        T.dontCare();
        final Cursor c = mDb.rawQuery(mMainService.getString(R.string.sql_payment_asset_select_by_provider_and_asset_id), new String[]{providerId, assetId});
        try {
            if (!c.moveToFirst()) {
                return null;
            }
            return readPaymentAsset(c);
        } finally {
            c.close();
        }
    }
}

/*
 * Copyright 2018 GIG Technology NV
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
 * @@license_version:1.4@@
 */

package com.mobicage.rogerthat.plugins.payment;

import android.content.Intent;

import com.mobicage.api.payment.Rpc;
import com.mobicage.rogerthat.MainService;
import com.mobicage.rogerthat.config.ConfigurationProvider;
import com.mobicage.rogerthat.plugins.MobicagePlugin;
import com.mobicage.rogerthat.util.db.DatabaseManager;
import com.mobicage.rogerthat.util.db.TransactionHelper;
import com.mobicage.rogerthat.util.db.TransactionWithoutResult;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rpc.CallReceiver;
import com.mobicage.rpc.IncompleteMessageException;
import com.mobicage.to.payment.AppPaymentProviderTO;
import com.mobicage.to.payment.CancelPaymentRequestTO;
import com.mobicage.to.payment.ConfirmPaymentRequestTO;
import com.mobicage.to.payment.CreateAssetRequestTO;
import com.mobicage.to.payment.CreateTransactionRequestTO;
import com.mobicage.to.payment.CryptoTransactionTO;
import com.mobicage.to.payment.GetPaymentProfileRequestTO;
import com.mobicage.to.payment.GetPaymentProvidersRequestTO;
import com.mobicage.to.payment.GetPaymentTransactionsRequestTO;
import com.mobicage.to.payment.GetPendingPaymentDetailsRequestTO;
import com.mobicage.to.payment.GetPendingPaymentSignatureDataRequestTO;
import com.mobicage.to.payment.GetTargetInfoRequestTO;
import com.mobicage.to.payment.PaymentProviderAssetTO;
import com.mobicage.to.payment.ReceivePaymentRequestTO;
import com.mobicage.to.payment.UpdatePaymentAssetRequestTO;
import com.mobicage.to.payment.UpdatePaymentAssetResponseTO;
import com.mobicage.to.payment.UpdatePaymentAssetsRequestTO;
import com.mobicage.to.payment.UpdatePaymentAssetsResponseTO;
import com.mobicage.to.payment.UpdatePaymentProviderRequestTO;
import com.mobicage.to.payment.UpdatePaymentProviderResponseTO;
import com.mobicage.to.payment.UpdatePaymentProvidersRequestTO;
import com.mobicage.to.payment.UpdatePaymentProvidersResponseTO;
import com.mobicage.to.payment.UpdatePaymentStatusRequestTO;
import com.mobicage.to.payment.UpdatePaymentStatusResponseTO;
import com.mobicage.to.payment.VerifyPaymentAssetRequestTO;
import com.mobicage.to.system.SettingsTO;

import org.json.simple.JSONValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PaymentPlugin implements MobicagePlugin {

    private static final String CONFIGKEY = "com.mobicage.rogerthat.plugins.payment";

    public static final String GET_PAYMENT_PROVIDERS_RESULT_INTENT = "com.mobicage.rogerthat.plugins.payment.GET_PAYMENT_PROVIDERS_RESULT_INTENT";
    public static final String GET_PAYMENT_PROVIDERS_FAILED_INTENT = "com.mobicage.rogerthat.plugins.payment.GET_PAYMENT_PROVIDERS_FAILED_INTENT";

    public static final String GET_PAYMENT_PROFILE_RESULT_INTENT = "com.mobicage.rogerthat.plugins.payment.GET_PAYMENT_PROFILE_RESULT_INTENT";
    public static final String GET_PAYMENT_PROFILE_FAILED_INTENT = "com.mobicage.rogerthat.plugins.payment.GET_PAYMENT_PROFILE_FAILED_INTENT";

    public static final String GET_PAYMENT_ASSETS_RESULT_INTENT = "com.mobicage.rogerthat.plugins.payment.GET_PAYMENT_ASSETS_RESULT_INTENT";
    public static final String GET_PAYMENT_ASSETS_FAILED_INTENT = "com.mobicage.rogerthat.plugins.payment.GET_PAYMENT_ASSETS_FAILED_INTENT";

    public static final String GET_PAYMENT_TRANSACTIONS_RESULT_INTENT = "com.mobicage.rogerthat.plugins.payment.GET_PAYMENT_TRANSACTIONS_RESULT_INTENT";
    public static final String GET_PAYMENT_TRANSACTIONS_FAILED_INTENT = "com.mobicage.rogerthat.plugins.payment.GET_PAYMENT_TRANSACTIONS_FAILED_INTENT";

    public static final String VERIFY_PAYMENT_ASSET_RESULT_INTENT = "com.mobicage.rogerthat.plugins.payment.VERIFY_PAYMENT_ASSET_RESULT_INTENT";
    public static final String VERIFY_PAYMENT_ASSET_FAILED_INTENT = "com.mobicage.rogerthat.plugins.payment.VERIFY_PAYMENT_ASSET_FAILED_INTENT";

    public static final String RECEIVE_PAYMENT_RESULT_INTENT = "com.mobicage.rogerthat.plugins.payment.RECEIVE_PAYMENT_RESULT_INTENT";
    public static final String RECEIVE_PAYMENT_FAILED_INTENT = "com.mobicage.rogerthat.plugins.payment.RECEIVE_PAYMENT_FAILED_INTENT";

    public static final String CANCEL_PAYMENT_RESULT_INTENT = "com.mobicage.rogerthat.plugins.payment.CANCEL_PAYMENT_RESULT_INTENT";
    public static final String CANCEL_PAYMENT_FAILED_INTENT = "com.mobicage.rogerthat.plugins.payment.CANCEL_PAYMENT_FAILED_INTENT";

    public static final String GET_PENDING_PAYMENT_DETAILS_RESULT_INTENT = "com.mobicage.rogerthat.plugins.payment.GET_PENDING_PAYMENT_DETAILS_RESULT_INTENT";
    public static final String GET_PENDING_PAYMENT_DETAILS_FAILED_INTENT = "com.mobicage.rogerthat.plugins.payment.GET_PENDING_PAYMENT_DETAILS_FAILED_INTENT";

    public static final String GET_PENDING_PAYMENT_SIGNATURE_DATA_RESULT_INTENT = "com.mobicage.rogerthat.plugins.payment.GET_PENDING_PAYMENT_SIGNATURE_DATA_RESULT_INTENT";
    public static final String GET_PENDING_PAYMENT_SIGNATURE_DATA_FAILED_INTENT = "com.mobicage.rogerthat.plugins.payment.GET_PENDING_PAYMENT_SIGNATURE_DATA_FAILED_INTENT";

    public static final String CONFIRM_PAYMENT_RESULT_INTENT = "com.mobicage.rogerthat.plugins.payment.CONFIRM_PAYMENT_RESULT_INTENT";
    public static final String CONFIRM_PAYMENT_FAILED_INTENT = "com.mobicage.rogerthat.plugins.payment.CONFIRM_PAYMENT_FAILED_INTENT";

    public static final String CREATE_PAYMENT_ASSET_RESULT_INTENT = "com.mobicage.rogerthat.plugins.payment.CREATE_PAYMENT_ASSET_RESULT_INTENT";
    public static final String CREATE_PAYMENT_ASSET_FAILED_INTENT = "com.mobicage.rogerthat.plugins.payment.CREATE_PAYMENT_ASSET_FAILED_INTENT";

    public static final String GET_TARGET_INFO_RESULT_INTENT = "com.mobicage.rogerthat.plugins.payment.GET_TARGET_INFO_RESULT_INTENT";
    public static final String GET_TARGET_INFO_FAILED_INTENT = "com.mobicage.rogerthat.plugins.payment.GET_TARGET_INFO_FAILED_INTENT";

    public static final String CREATE_TRANSACTION_RESULT_INTENT = "com.mobicage.rogerthat.plugins.payment.CREATE_TRANSACTION_RESULT_INTENT";
    public static final String CREATE_TRANSACTION_FAILED_INTENT = "com.mobicage.rogerthat.plugins.payment.CREATE_TRANSACTION_FAILED_INTENT";

    public static final String PAYMENT_PROVIDER_UPDATED_INTENT = "com.mobicage.rogerthat.plugins.payment.PAYMENT_PROVIDER_UPDATED_INTENT";
    public static final String PAYMENT_PROVIDER_REMOVED_INTENT = "com.mobicage.rogerthat.plugins.payment.PAYMENT_PROVIDER_REMOVED_INTENT";
    public static final String PAYMENT_ASSETS_UPDATED_INTENT = "com.mobicage.rogerthat.plugins.payment.PAYMENT_ASSETS_UPDATED_INTENT";
    public static final String PAYMENT_ASSET_UPDATED_INTENT = "com.mobicage.rogerthat.plugins.payment.PAYMENT_ASSET_UPDATED_INTENT";
    public static final String UPDATE_RECEIVE_PAYMENT_STATUS_UPDATED_INTENT = "com.mobicage.rogerthat.plugins.payment.UPDATE_RECEIVE_PAYMENT_STATUS_UPDATED_INTENT";

    private final ConfigurationProvider mConfigProvider;
    private final MainService mMainService;

    private final PaymentStore mStore;

    public PaymentPlugin(final MainService mainService, ConfigurationProvider pConfigProvider, final DatabaseManager dbManager) {
        T.UI();
        mMainService = mainService;
        mConfigProvider = pConfigProvider;
        mStore = new PaymentStore(mainService, dbManager);
    }

    @Override
    public void destroy() {
        T.UI();
        mConfigProvider.unregisterListener(CONFIGKEY, this);
    }

    @Override
    public void processSettings(SettingsTO settings) {
        // not used
    }

    @Override
    public void reconfigure() {
        T.UI();
    }

    public PaymentStore getStore() {
        T.dontCare();
        return mStore;
    }

    @Override
    public void initialize() {
        T.UI();
        reconfigure();
        initCallReceiver();
        mConfigProvider.registerListener(CONFIGKEY, this);
    }

    private void initCallReceiver() {
        CallReceiver.comMobicageCapiPaymentIClientRpc = new com.mobicage.capi.payment.IClientRpc() {

            @Override
            public UpdatePaymentAssetResponseTO updatePaymentAsset(UpdatePaymentAssetRequestTO request) throws java.lang.Exception {
                PaymentPlugin.this.updatePaymentAsset(request);
                return new UpdatePaymentAssetResponseTO();
            }

            @Override
            public UpdatePaymentAssetsResponseTO updatePaymentAssets(UpdatePaymentAssetsRequestTO request) throws java.lang.Exception {
                PaymentPlugin.this.updatePaymentAssets(request);
                return new UpdatePaymentAssetsResponseTO();
            }

            @Override
            public UpdatePaymentProviderResponseTO updatePaymentProvider(UpdatePaymentProviderRequestTO request) throws Exception {
                PaymentPlugin.this.updatePaymentProvider(new AppPaymentProviderTO(request.toJSONMap()));
                return new UpdatePaymentProviderResponseTO();
            }

            @Override
            public UpdatePaymentProvidersResponseTO updatePaymentProviders(UpdatePaymentProvidersRequestTO request) throws Exception {
                PaymentPlugin.this.updatePaymentProviders(request);
                return new UpdatePaymentProvidersResponseTO();
            }

                @Override
            public UpdatePaymentStatusResponseTO updatePaymentStatus(UpdatePaymentStatusRequestTO request) throws Exception {
                PaymentPlugin.this.updateReceivePaymentStatus(request);
                return new UpdatePaymentStatusResponseTO();
            }
        };
    }

    public boolean getPaymentProviders(String callbackKey) {
        GetPaymentProvidersRequestTO request = new GetPaymentProvidersRequestTO();
        try {
            GetPaymentProvidersResponseHandler rh = new GetPaymentProvidersResponseHandler();
            rh.setCallbackKey(callbackKey);
            Rpc.getPaymentProviders(rh, request);
        }  catch (Exception e) {
            L.bug("Error while get payment providers rpc request", e);
            return false;
        }
        return true;
    }

    public boolean getPaymentProfile(String callbackKey, String providerId) {
        GetPaymentProfileRequestTO request = new GetPaymentProfileRequestTO();
        request.provider_id = providerId;
        try {
            GetPaymentProfileResponseHandler rh = new GetPaymentProfileResponseHandler();
            rh.setCallbackKey(callbackKey);
            Rpc.getPaymentProfile(rh, request);
        }  catch (Exception e) {
            L.bug("Error while get payment profile rpc request", e);
            return false;
        }
        return true;
    }

    public boolean getPaymentTransactions(String callbackKey, String providerId, String assetId, String cursor, String type) {
        GetPaymentTransactionsRequestTO request = new GetPaymentTransactionsRequestTO();
        request.provider_id = providerId;
        request.asset_id = assetId;
        request.cursor = cursor;
        request.type = type;
        try {
            GetPaymentTransactionsResponseHandler rh = new GetPaymentTransactionsResponseHandler();
            rh.setCallbackKey(callbackKey);
            Rpc.getPaymentTransactions(rh, request);
        }  catch (Exception e) {
            L.bug("Error while get payment transactions rpc request", e);
            return false;
        }
        return true;
    }

    public boolean verifyPaymentAsset(String callbackKey, String providerId, String assetId, String code) {
        VerifyPaymentAssetRequestTO request = new VerifyPaymentAssetRequestTO();
        request.provider_id = providerId;
        request.asset_id = assetId;
        request.code = code;

        try {
            VerifyPaymentAssetResponseHandler rh = new VerifyPaymentAssetResponseHandler();
            rh.setCallbackKey(callbackKey);
            Rpc.verifyPaymentAsset(rh, request);
        }  catch (Exception e) {
            L.bug("Error while verify payment asset rpc request", e);
            return false;
        }
        return true;
    }

    public boolean receivePayment(String callbackKey, String providerId, String assetId,
                                  long amount, String memo, long precision) {
        ReceivePaymentRequestTO request = new ReceivePaymentRequestTO();
        request.provider_id = providerId;
        request.asset_id = assetId;
        request.amount = amount;
        request.memo = memo;
        request.precision = precision;

        try {
            ReceivePaymentResponseHandler rh = new ReceivePaymentResponseHandler();
            rh.setCallbackKey(callbackKey);
            Rpc.receivePayment(rh, request);
        }  catch (Exception e) {
            L.bug("Error while receive payment rpc request", e);
            return false;
        }
        return true;
    }

    public boolean cancelPayment(String callbackKey, String transactionId) {
        CancelPaymentRequestTO request = new CancelPaymentRequestTO();
        request.transaction_id = transactionId;

        try {
            CancelPaymentResponseHandler rh = new CancelPaymentResponseHandler();
            rh.setCallbackKey(callbackKey);
            Rpc.cancelPayment(rh, request);
        }  catch (Exception e) {
            L.bug("Error while cancel pay payment rpc request", e);
            return false;
        }
        return true;
    }

    public boolean getPendingPaymentDetails(String callbackKey, String transactionId) {
        GetPendingPaymentDetailsRequestTO request = new GetPendingPaymentDetailsRequestTO();
        request.transaction_id = transactionId;

        try {
            GetPendingPaymentDetailsResponseHandler rh = new GetPendingPaymentDetailsResponseHandler();
            rh.setCallbackKey(callbackKey);
            Rpc.getPendingPaymentDetails(rh, request);
        }  catch (Exception e) {
            L.bug("Error while getting pending payment details rpc request", e);
            return false;
        }
        return true;
    }

    public boolean getPendingPaymentSignatureData(String callbackKey, String transactionId, String asset_id) {
        GetPendingPaymentSignatureDataRequestTO request = new GetPendingPaymentSignatureDataRequestTO();
        request.transaction_id = transactionId;
        request.asset_id = asset_id;

        try {
            GetPendingPaymentSignatureDataResponseHandler rh = new GetPendingPaymentSignatureDataResponseHandler();
            rh.setCallbackKey(callbackKey);
            Rpc.getPendingPaymentSignatureData(rh, request);
        }  catch (Exception e) {
            L.bug("Error while getting pending payment signature data rpc request", e);
            return false;
        }
        return true;
    }

    public boolean confirmPayPayment(String callbackKey, String transactionId, CryptoTransactionTO cryptoTransaction) {
        ConfirmPaymentRequestTO request = new ConfirmPaymentRequestTO();
        request.transaction_id = transactionId;
        request.crypto_transaction = cryptoTransaction;

        try {
            ConfirmPaymentResponseHandler rh = new ConfirmPaymentResponseHandler();
            rh.setCallbackKey(callbackKey);
            Rpc.confirmPayment(rh, request);
        }  catch (Exception e) {
            L.bug("Error while confirm pay payment rpc request", e);
            return false;
        }
        return true;
    }

    public boolean createAsset(String callbackKey, CreateAssetRequestTO request) {
        try {
            CreatePaymentAssetResponseHandler handler = new CreatePaymentAssetResponseHandler();
            handler.setCallbackKey(callbackKey);
            Rpc.createAsset(handler, request);
        } catch (Exception e) {
            L.bug("Error while executing createAsset rpc", e);
            return false;
        }
        return true;
    }

    public void updatePaymentProviders(final UpdatePaymentProvidersRequestTO request) {
        final List<String> paymentProviderIds;
        if (request.provider_ids.length == 0) {
            paymentProviderIds = mStore.getPaymentProvidersIds();
        } else {
            paymentProviderIds = Arrays.asList(request.provider_ids);
        }
        final List<String> deletedProviderIds = new ArrayList<>(paymentProviderIds);

        TransactionHelper.runInTransaction(mStore.getDatabase(), "updatePaymentProviders", new TransactionWithoutResult() {
            @Override
            protected void run() {
                mStore.deletePaymentProviders(request.provider_ids);

                for (AppPaymentProviderTO paymentProvider : request.payment_providers) {
                    deletedProviderIds.remove(paymentProvider.id);
                    if (!paymentProviderIds.contains(paymentProvider.id)) {
                        paymentProviderIds.add(paymentProvider.id);
                    }
                    mStore.savePaymentProvider(paymentProvider);
                }
            }
        });

        for (String providerId : paymentProviderIds) {
            if (deletedProviderIds.contains(providerId)) {
                Intent intent = new Intent(PAYMENT_PROVIDER_REMOVED_INTENT);
                intent.putExtra("provider_id", providerId);
                mMainService.sendBroadcast(intent);
            } else {
                Intent intent = new Intent(PAYMENT_PROVIDER_UPDATED_INTENT);
                intent.putExtra("provider_id", providerId);
                mMainService.sendBroadcast(intent);
            }
        }
    }

    public void updatePaymentProvider(final AppPaymentProviderTO paymentProvider) {
        mStore.savePaymentProvider(paymentProvider);
        Intent intent = new Intent(PAYMENT_PROVIDER_UPDATED_INTENT);
        intent.putExtra("provider_id", paymentProvider.id);
        mMainService.sendBroadcast(intent);
    }

    public void updatePaymentAssets(final UpdatePaymentAssetsRequestTO request) {
        final List<String> paymentProviderIds;
        if (request.provider_ids.length == 0) {
            paymentProviderIds = mStore.getPaymentProvidersIds();
        } else {
            paymentProviderIds = Arrays.asList(request.provider_ids);
        }

        TransactionHelper.runInTransaction(mStore.getDatabase(), "updatePaymentAssets", new TransactionWithoutResult() {
            @Override
            protected void run() {
                mStore.deletePaymentAssets(request.provider_ids);

                for (PaymentProviderAssetTO asset : request.assets) {
                    if (!paymentProviderIds.contains(asset.provider_id)) {
                        paymentProviderIds.add(asset.provider_id);
                    }
                    mStore.savePaymentAsset(asset);
                }
            }
        });

        for (String providerId : paymentProviderIds) {
            Intent intent = new Intent(PAYMENT_ASSETS_UPDATED_INTENT);
            intent.putExtra("provider_id", providerId);
            mMainService.sendBroadcast(intent);
        }
    }

    public void updatePaymentAsset(final UpdatePaymentAssetRequestTO request) {
        try {
            mStore.savePaymentAsset(new PaymentProviderAssetTO(request.toJSONMap()));
        } catch (IncompleteMessageException e) {
            L.bug("Please re-generate UpdatePaymentAssetRequestTO or PaymentProviderAssetTO");
        }

        Intent intent = new Intent(PAYMENT_ASSET_UPDATED_INTENT);
        intent.putExtra("provider_id", request.provider_id);
        intent.putExtra("asset_id", request.id);
        mMainService.sendBroadcast(intent);
    }

    public void updateReceivePaymentStatus(final UpdatePaymentStatusRequestTO request) {
        Intent intent = new Intent(UPDATE_RECEIVE_PAYMENT_STATUS_UPDATED_INTENT);

        intent.putExtra("result", JSONValue.toJSONString(request.toJSONMap()));
        mMainService.sendBroadcast(intent);
    }

    public boolean createTransaction(String callbackKey, CreateTransactionRequestTO request) {
        try {
            CreateTransactionResponseHandler handler = new CreateTransactionResponseHandler();
            handler.setCallbackKey(callbackKey);
            Rpc.createTransaction(handler, request);
        } catch (Exception e) {
            L.bug("Error while executing createTransaction rpc", e);
            return false;
        }
        return true;
    }

    public boolean getTargetInfo(String callbackKey, GetTargetInfoRequestTO request) {
        try {
            GetTargetInfoResponseHandler handler = new GetTargetInfoResponseHandler();
            handler.setCallbackKey(callbackKey);
            Rpc.getTargetInfo(handler, request);
        } catch (Exception e) {
            L.bug("Error while executing getTargetInfo rpc", e);
            return false;
        }
        return true;
    }
}

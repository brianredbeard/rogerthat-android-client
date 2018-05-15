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

package com.mobicage.rogerthat.plugins.messaging.widgets;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;
import com.mobicage.api.messaging.Rpc;
import com.mobicage.models.properties.forms.BasePaymentMethod;
import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.MainService;
import com.mobicage.rogerthat.cordova.CordovaActionScreenActivity;
import com.mobicage.rogerthat.plugins.friends.ActionScreenActivity;
import com.mobicage.rogerthat.plugins.messaging.BrandingMgr;
import com.mobicage.rogerthat.plugins.messaging.Message;
import com.mobicage.rogerthat.plugins.messaging.MessagingPlugin;
import com.mobicage.rogerthat.plugins.payment.ChoosePaymentMethodActivity;
import com.mobicage.rogerthat.plugins.payment.OpenEmbeddedAppContext;
import com.mobicage.rogerthat.plugins.payment.OpenEmbeddedAppContextType;
import com.mobicage.rogerthat.plugins.payment.PaymentPlugin;
import com.mobicage.rogerthat.plugins.payment.PaymentProviderMethod;
import com.mobicage.rogerthat.plugins.system.SystemPlugin;
import com.mobicage.rogerthat.util.JsonUtils;
import com.mobicage.rogerthat.util.TextUtils;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeBroadcastReceiver;
import com.mobicage.rogerthat.util.system.SafeDialogClick;
import com.mobicage.rogerthat.util.system.SafeViewOnClickListener;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.ui.UIUtils;
import com.mobicage.rpc.IncompleteMessageException;
import com.mobicage.rpc.ResponseHandler;
import com.mobicage.rpc.config.LookAndFeelConstants;
import com.mobicage.to.messaging.forms.PayWidgetResultTO;
import com.mobicage.to.messaging.forms.PaymentMethodTO;
import com.mobicage.to.messaging.forms.SubmitPayFormRequestTO;
import com.mobicage.to.messaging.forms.SubmitPayFormResponseTO;
import com.mobicage.to.payment.GetPaymentMethodsRequestTO;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PayWidget extends Widget {

    private static final int START_CORDOVA_APP_REQUEST_CODE = 123;
    private static final int SHOW_PAYMENT_METHODS_CODE = 124;

    private Button mPayBtn;
    private PayWidgetResultTO mResult;
    private TextView mResultTextView;
    private LinearLayout mResultData;
    private SystemPlugin mSystemPlugin;
    private BroadcastReceiver mBroadcastReceiver;
    private ProgressDialog mEmbeddedAppDownloadSpinner;
    private PaymentProviderMethod mChosenPaymentProviderMethod;

    public PayWidget(Context context) {
        super(context);
    }

    public PayWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onServiceUnbound() {
        if (mBroadcastReceiver != null)
            mActivity.unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    public void initializeWidget() {
        mResultData = (LinearLayout) findViewById(R.id.result_data);
        mResultTextView = (TextView) findViewById(R.id.result_textview);
        mPayBtn = (Button) findViewById(R.id.pay_btn);
        mPayBtn.setOnClickListener(new SafeViewOnClickListener() {
            @Override
            public void safeOnClick(View v) {
                pay();
            }
        });

        if (Boolean.TRUE.equals(mWidgetMap.get("test_mode"))) {
            findViewById(R.id.test_mode).setVisibility(View.VISIBLE);
        }

        IconicsDrawable d = new IconicsDrawable(mActivity, FontAwesome.Icon.faw_credit_card).color(LookAndFeelConstants.getPrimaryIconColor(mActivity)).sizeDp(24);
        mPayBtn.setCompoundDrawablesWithIntrinsicBounds(d, null, null, null);

        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) mWidgetMap.get("value");
        if (result != null) {
            try {
                mResult = new PayWidgetResultTO(result);
                showResult();
            } catch (IncompleteMessageException e) {
                L.bug(e); // Should never happen
            }
        }

        mBroadcastReceiver = getBroadcastReceiver();
        final IntentFilter filter = getIntentFilter();
        mActivity.registerReceiver(mBroadcastReceiver, filter);
        mSystemPlugin = mActivity.getMainService().getPlugin(SystemPlugin.class);
    }

    protected SafeBroadcastReceiver getBroadcastReceiver() {

        return new SafeBroadcastReceiver() {
            @Override
            public String[] onSafeReceive(Context context, Intent intent) {
                T.UI();
                String action = intent.getAction();
                if (BrandingMgr.EMBEDDED_APP_AVAILABLE_INTENT.equals(action) && mEmbeddedAppDownloadSpinner != null) {
                    String id = intent.getStringExtra("id");
                    if (id.equals(mChosenPaymentProviderMethod.provider.embedded_app_id)) {
                        mEmbeddedAppDownloadSpinner.dismiss();
                        openEmbeddedApp(mChosenPaymentProviderMethod);
                        return new String[] { action };
                    }
                }
                return null;
            }
        };
    }

    protected IntentFilter getIntentFilter() {
        return new IntentFilter(BrandingMgr.EMBEDDED_APP_AVAILABLE_INTENT);
    }

    public static String valueString(Context context, Map<String, Object> widget) {
        final Map<String, Object> jsonResult = (Map<String, Object>) widget.get("value");
        if (jsonResult == null) {
            return "";
        }

        final PayWidgetResultTO result;
        try {
            result = new PayWidgetResultTO(jsonResult);
        } catch (IncompleteMessageException e) {
            L.bug(e);
            return "";
        }

        final List<String> parts = new ArrayList<>();
        parts.add(String.format("%s: %s", context.getString(R.string.via), result.provider_id));
        parts.add(String.format("%s: %s", context.getString(R.string.ref), result.transaction_id));
        parts.add(String.format("%s: %s", context.getString(R.string.status), result.status));

        return android.text.TextUtils.join("\n", parts);
    }

    @Override
    public void putValue() {
        mWidgetMap.put("value", mResult == null ? null : mResult.toJSONMap());
    }

    @Override
    public PayWidgetResultTO getWidgetResult() {
        return mResult;
    }

    @Override
    public boolean proceedWithSubmit(final String buttonId) {
        if (Message.POSITIVE.equals(buttonId)) {
            if (mResult == null) {
                String title = mActivity.getString(R.string.not_paid);
                String message = mActivity.getString(R.string.pay_first);
                String positiveCaption = mActivity.getString(R.string.pay);
                String negativeCaption = mActivity.getString(R.string.cancel);
                SafeDialogClick onPositiveClick = new SafeDialogClick() {
                    @Override
                    public void safeOnClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        pay();
                    }
                };
                UIUtils.showDialog(mActivity, title, message, positiveCaption, onPositiveClick, negativeCaption, null);
                return false;
            }
        }
        return true;
    }

    @Override
    public void submit(String buttonId, long timestamp) throws Exception {
        final SubmitPayFormRequestTO request = new SubmitPayFormRequestTO();
        request.button_id = buttonId;
        request.message_key = mMessage.key;
        request.parent_message_key = mMessage.parent_key;
        request.timestamp = timestamp;
        if (Message.POSITIVE.equals(buttonId)) {
            request.result = getWidgetResult();
        }
        if ((mMessage.flags & MessagingPlugin.FLAG_SENT_BY_JSMFR) == MessagingPlugin.FLAG_SENT_BY_JSMFR) {
            mPlugin.answerJsMfrMessage(mMessage, request.toJSONMap(),
                    "com.mobicage.api.messaging.submitPayForm", mActivity, mParentView);
        } else {
            Rpc.submitPayForm(new ResponseHandler<SubmitPayFormResponseTO>(), request);
        }
    }

    private void pay() {
        MainService mainService = mActivity.getMainService();
        if (!mainService.getNetworkConnectivityManager().isConnected()) {
            UIUtils.showDialog(mActivity,
                    mainService.getString(R.string.no_internet_connection),
                    mainService.getString(R.string.no_internet_connection_try_again));
            return;
        }
        try {

            JSONArray methodsArr = (JSONArray) mWidgetMap.get("methods");
            PaymentMethodTO[] methods = new PaymentMethodTO[methodsArr.size()];
            for (int i = 0; i < methodsArr.size(); i++) {
                methods[i] = new PaymentMethodTO((Map<String, Object>) methodsArr.get(i));
            }
            GetPaymentMethodsRequestTO request = new GetPaymentMethodsRequestTO();
            request.base_method = new BasePaymentMethod((Map<String, Object>) mWidgetMap.get("base_method"));
            request.target = (String) mWidgetMap.get("target");
            request.methods = methods;
            request.test_mode = (boolean) mWidgetMap.get("test_mode");
            mainService.getPlugin(PaymentPlugin.class).getPaymentMethods(request);
            Intent intent = new Intent(this.mActivity, ChoosePaymentMethodActivity.class);
            mActivity.startActivityForResult(intent, SHOW_PAYMENT_METHODS_CODE);
        } catch (IncompleteMessageException e) {
            e.printStackTrace();
        }
    }

    private void openEmbeddedApp(PaymentProviderMethod paymentProviderMethod) {
        mChosenPaymentProviderMethod = null;
        String embeddedAppId = paymentProviderMethod.provider.embedded_app_id;
        // Should always be set, but just in case.
        if (embeddedAppId == null) {
            UIUtils.showLongToast(mActivity, mActivity.getString(R.string.payment_not_enabled));
            return;
        }
        long version = mSystemPlugin.getStore().getEmbeddedAppVersion(embeddedAppId);
        if (version < 0 || !mSystemPlugin.getBrandingMgr().embeddedAppExists(embeddedAppId)) {
            mEmbeddedAppDownloadSpinner = UIUtils.showProgressDialog(mActivity, null, mActivity.getString(R.string.loading), true, false);
            mChosenPaymentProviderMethod = paymentProviderMethod;
            mSystemPlugin.getEmbeddedApp(embeddedAppId);
            return;
        }

        HashMap<String, Object> data = new HashMap<>();
        data.put("test_mode", mWidgetMap.get("test_mode"));
        data.put("message_key", mMessage.key);
        data.put("memo", mWidgetMap.get("memo"));
        data.put("target", mWidgetMap.get("target"));
        data.put("method", paymentProviderMethod.method.toJSONMap());
        data.put("provider", paymentProviderMethod.provider.toJSONMap());
        OpenEmbeddedAppContext context = new OpenEmbeddedAppContext(OpenEmbeddedAppContextType.PAY_WIDGET, data);
        Bundle extras = new Bundle();
        extras.putString(ActionScreenActivity.CONTEXT, JSONValue.toJSONString(context.toJSONMap()));
        extras.putString(CordovaActionScreenActivity.EMBEDDED_APP_ID, embeddedAppId);
        extras.putString(CordovaActionScreenActivity.TITLE, paymentProviderMethod.provider.name);

        final Intent i = new Intent(mActivity, CordovaActionScreenActivity.class);
        i.putExtras(extras);
        mActivity.startActivityForResult(i, START_CORDOVA_APP_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == START_CORDOVA_APP_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                final String result =  data.getStringExtra(ActionScreenActivity.EXIT_APP_RESULT);
                try {
                    JSONObject args = new JSONObject(result);
                    if (args.optBoolean("success")) {
                        mResult = new PayWidgetResultTO();
                        mResult.provider_id = JsonUtils.optString(args, "provider_id", null);
                        mResult.transaction_id = JsonUtils.optString(args, "transaction_id", null);
                        mResult.status = JsonUtils.optString(args, "status", null);
                        showResult();

                        String message = JsonUtils.optString(args, "message", null);
                        if (!TextUtils.isEmptyOrWhitespace(message)) {
                            SafeDialogClick onPositiveClick = new SafeDialogClick() {
                                @Override
                                public void safeOnClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                    if ((boolean) mWidgetMap.get("auto_submit")) {
                                        mActivity.excecutePositiveButtonClick();
                                    }
                                }
                            };
                            UIUtils.showDialog(mActivity, null, message, mActivity.getString(R.string.rogerthat), onPositiveClick,
                                    null, null);
                        } else if ((boolean) mWidgetMap.get("auto_submit")){
                            mActivity.excecutePositiveButtonClick();
                        }

                    } else {
                        String code = JsonUtils.optString(args, "code", null);
                        String message = JsonUtils.optString(args, "message", null);
                        if (TextUtils.isEmptyOrWhitespace(code) || TextUtils.isEmptyOrWhitespace(message)) {
                            L.e("Failed to make payment: unknown reason!");
                            UIUtils.showDialog(mActivity, null, R.string.error_please_try_again);
                        } else {
                            L.i("Failed to make payment: " + code);
                            UIUtils.showDialog(mActivity, null, message);
                        }
                    }
                } catch (JSONException e) {
                    L.e("Failed to make payment", e);
                    UIUtils.showDialog(mActivity, null, R.string.error_please_try_again);
                }
            }
        } else if (requestCode == SHOW_PAYMENT_METHODS_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                final String result = data.getStringExtra(ChoosePaymentMethodActivity.RESULT_DATA_KEY);
                Map<String, Object> obj = (Map<String, Object>) JSONValue.parse(result);
                try {
                    openEmbeddedApp(new PaymentProviderMethod(obj));
                } catch (IncompleteMessageException e) {
                    L.bug(e);
                }
            }
        }
    }

    private void showResult() {
        mPayBtn.setVisibility(View.GONE);
        if ("succeeded".equals(mResult.status)) {
            mResultTextView.setVisibility(View.VISIBLE);
        }
        mResultData.removeAllViews();
        mResultData.setVisibility(View.VISIBLE);

        addRow(mActivity.getString(R.string.via), mResult.provider_id);
        addRow(mActivity.getString(R.string.ref), mResult.transaction_id);
    }

    private void addRow(String key, String value) {
        final LinearLayout ll = (LinearLayout) View.inflate(mActivity, R.layout.pay_data_detail, null);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 10, 0, 0);

        final TextView tvKey = (TextView) ll.findViewById(R.id.pay_data_detail_key);
        final TextView tvVal = (TextView) ll.findViewById(R.id.pay_data_detail_value);

        tvKey.setText(key);
        tvKey.setTextColor(LookAndFeelConstants.getPrimaryColor(mActivity));
        tvVal.setText(value);
        mResultData.addView(ll, layoutParams);
    }
}

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
import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.cordova.CordovaActionScreenActivity;
import com.mobicage.rogerthat.cordova.CordovaSettings;
import com.mobicage.rogerthat.plugins.friends.ActionScreenActivity;
import com.mobicage.rogerthat.plugins.messaging.BrandingMgr;
import com.mobicage.rogerthat.plugins.messaging.Message;
import com.mobicage.rogerthat.plugins.messaging.MessagingPlugin;
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
import com.mobicage.to.messaging.forms.SubmitPayFormRequestTO;
import com.mobicage.to.messaging.forms.SubmitPayFormResponseTO;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.JSONArray;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PayWidget extends Widget {

    private final String APPLICATION_TAG = "rogerthat-payment";
    private static final int START_CORDOVA_APP_REQUEST_CODE = 123;

    private Button mPayBtn;
    private PayWidgetResultTO mResult;
    private TextView mResultTextView;
    private LinearLayout mResultData;
    private SystemPlugin mSystemPlugin;
    private BroadcastReceiver mBroadcastReceiver;
    private ProgressDialog mEmbeddedAppDownloadSpinner;

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
    }

    protected SafeBroadcastReceiver getBroadcastReceiver() {

        return new SafeBroadcastReceiver() {
            @Override
            public String[] onSafeReceive(Context context, Intent intent) {
                T.UI();
                String action = intent.getAction();
                if (BrandingMgr.EMBEDDED_APP_AVAILABLE_INTENT.equals(action) && mEmbeddedAppDownloadSpinner != null) {
                    String id = intent.getStringExtra("id");
                    String embeddedAppIdWidget = (String) mWidgetMap.get("embedded_app_id");

                    if (id.equals(embeddedAppIdWidget)) {
                        mEmbeddedAppDownloadSpinner.dismiss();
                        pay();
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
        String embeddedApp = null;
        String embeddedAppId = null;

        String embeddedAppIdWidget = (String) mWidgetMap.get("embedded_app_id");
        if (embeddedAppIdWidget != null) {
            embeddedAppId = embeddedAppIdWidget;
            if (mSystemPlugin == null) {
                mSystemPlugin = mActivity.getMainService().getPlugin(SystemPlugin.class);
            }
            long version = mSystemPlugin.getStore().getEmbeddedAppVersion(embeddedAppId);
            if (version < 0 || !mSystemPlugin.getBrandingMgr().embeddedAppExists(embeddedAppId)) {
                mEmbeddedAppDownloadSpinner = UIUtils.showProgressDialog(mActivity, null, mActivity.getString(R.string.loading), true, false);
                mSystemPlugin.getEmbeddedApp(embeddedAppId);
                return;
            }

        } else {
            embeddedApp = APPLICATION_TAG;
            if (!CordovaSettings.APPS.contains(APPLICATION_TAG)) {
                UIUtils.showLongToast(mActivity, mActivity.getString(R.string.payment_not_enabled));
                return;
            }
        }

        org.json.simple.JSONObject context = new org.json.simple.JSONObject();
        // t should match a PaymentQRCodeType in rogerthat-payment branding
        // see https://github.com/rogerthat-platform/rogerthat-payment/blob/master/src/interfaces/actions.interfaces.ts
        context.put("t", 2);
        // result_type is the way we return our data
        // plugin needs to be used when using startActivityForResult
        context.put("result_type", "plugin");
        context.put("message_key", mMessage.key);
        JSONArray methods = (JSONArray) mWidgetMap.get("methods");
        context.put("methods", methods);
        context.put("memo", mWidgetMap.get("memo"));
        context.put("target", mWidgetMap.get("target"));
        context.put("test_mode", mWidgetMap.get("test_mode"));

        Bundle extras = new Bundle();
        extras.putString(ActionScreenActivity.CONTEXT, context.toString());
        extras.putString(CordovaActionScreenActivity.EMBEDDED_APP, embeddedApp);
        extras.putString(CordovaActionScreenActivity.EMBEDDED_APP_ID, embeddedAppId);
        extras.putString(CordovaActionScreenActivity.TITLE, "");

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

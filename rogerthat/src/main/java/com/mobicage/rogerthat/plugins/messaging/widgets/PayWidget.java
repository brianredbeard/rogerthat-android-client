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
import android.content.Context;
import android.content.Intent;
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
import com.mobicage.rogerthat.MainActivity;
import com.mobicage.rogerthat.NavigationItem;
import com.mobicage.rogerthat.cordova.CordovaActionScreenActivity;
import com.mobicage.rogerthat.cordova.CordovaSettings;
import com.mobicage.rogerthat.plugins.friends.ActionScreenActivity;
import com.mobicage.rogerthat.plugins.messaging.Message;
import com.mobicage.rogerthat.plugins.messaging.MessagingPlugin;
import com.mobicage.rogerthat.util.ActivityUtils;
import com.mobicage.rogerthat.util.TextUtils;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeViewOnClickListener;
import com.mobicage.rogerthat.util.ui.UIUtils;
import com.mobicage.rpc.IncompleteMessageException;
import com.mobicage.rpc.ResponseHandler;
import com.mobicage.rpc.config.LookAndFeelConstants;
import com.mobicage.to.messaging.forms.PayWidgetResultTO;
import com.mobicage.to.messaging.forms.SubmitPayFormRequestTO;
import com.mobicage.to.messaging.forms.SubmitPayFormResponseTO;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.JSONValue;

import java.util.Map;

public class PayWidget extends Widget {

    private final String APPLICATION_TAG = "rogerthat-payment";
    private static final int START_CORDOVA_APP_REQUEST_CODE = 1;

    private Button mPayBtn;
    private PayWidgetResultTO mResult;
    private LinearLayout mResultData;

    public PayWidget(Context context) {
        super(context);
    }

    public PayWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void initializeWidget() {
        mPayBtn = (Button) findViewById(R.id.pay_btn);
        mPayBtn.setOnClickListener(new SafeViewOnClickListener() {
            @Override
            public void safeOnClick(View v) {
                pay();
            }
        });

        IconicsDrawable d = new IconicsDrawable(mActivity, FontAwesome.Icon.faw_credit_card).color(LookAndFeelConstants.getPrimaryIconColor(mActivity)).sizeDp(24);
        mPayBtn.setCompoundDrawablesWithIntrinsicBounds(d, null, null, null);

        mResultData = (LinearLayout) findViewById(R.id.result_data);

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
    }

    public static String valueString(Context context, Map<String, Object> widget) {
        return (String) widget.get("value");
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

        JSONObject context = new JSONObject();
        try {
            // t should match a PaymentQRCodeType in rogerthat-payment branding
            // see https://github.com/rogerthat-platform/rogerthat-payment/blob/master/src/interfaces/actions.interfaces.ts
            context.put("t", 2);
            // result_type is the way we return our data
            // plugin needs to be used when using startActivityForResult
            context.put("result_type", "plugin");

            context.put("methods", mWidgetMap.get("methods"));
            context.put("memo", mWidgetMap.get("memo"));
            context.put("target", mWidgetMap.get("target"));

        } catch (JSONException e) {
            L.bug("Failed to start payment branding with context", e);
            UIUtils.showErrorPleaseRetryDialog(mActivity);
            return;
        }

        if (CordovaSettings.APPS.contains(APPLICATION_TAG)) {
            Bundle extras = new Bundle();
            extras.putString(ActionScreenActivity.CONTEXT, JSONValue.toJSONString(context));
            extras.putString(CordovaActionScreenActivity.EMBEDDED_APP, APPLICATION_TAG);
            extras.putString(CordovaActionScreenActivity.TITLE, "");

            final Intent i = new Intent(mActivity, CordovaActionScreenActivity.class);
            i.putExtras(extras);
            i.addFlags(MainActivity.FLAG_CLEAR_STACK);
            mActivity.startActivityForResult(i, START_CORDOVA_APP_REQUEST_CODE);

        } else {
            UIUtils.showLongToast(mActivity, mActivity.getString(R.string.payment_not_enabled));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == START_CORDOVA_APP_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                if (data.getBooleanExtra("success", false)) {
                    mResult = new PayWidgetResultTO();
                    mResult.transaction_id = data.getStringExtra("transaction_id");
                    mResult.provider_id = data.getStringExtra("provider_id");
                    mResult.status = data.getStringExtra("status");
                    showResult();

                    mActivity.excecutePositiveButtonClick();

                } else {
                    String code = data.getStringExtra("code");
                    String message = data.getStringExtra("message");
                    if (TextUtils.isEmptyOrWhitespace(code) || TextUtils.isEmptyOrWhitespace(message)) {
                        L.e("Failed to make payment: unknown reason!");
                        UIUtils.showLongToast(mActivity, mActivity.getString(R.string.error_please_try_again));
                    } else {
                        L.i("Failed to make payment: " + code);
                        UIUtils.showLongToast(mActivity, message);
                    }
                }
            }
        }
    }

    private void showResult() {
        mPayBtn.setVisibility(View.GONE);
        mResultData.removeAllViews();
        mResultData.setVisibility(View.VISIBLE);

        addRow(mActivity.getString(R.string.transaction_id), mResult.transaction_id);
        addRow(mActivity.getString(R.string.provider_id), mResult.provider_id);
        addRow(mActivity.getString(R.string.status), mResult.status);
    }

    private void addRow(String key, String value) {
        final LinearLayout ll = (LinearLayout) View.inflate(mActivity, R.layout.profile_data_detail, null);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 10, 0, 0);

        final TextView tvKey = (TextView) ll.findViewById(R.id.profile_data_detail_key);
        final TextView tvVal = (TextView) ll.findViewById(R.id.profile_data_detail_value);

        tvKey.setText(key);
        tvKey.setTextColor(LookAndFeelConstants.getPrimaryColor(mActivity));
        tvVal.setText(value);
        mResultData.addView(ll, layoutParams);
    }
}

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

package com.mobicage.rogerthat.plugins.messaging.widgets;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;
import com.mobicage.api.messaging.Rpc;
import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.OauthActivity;
import com.mobicage.rogerthat.plugins.messaging.Message;
import com.mobicage.rogerthat.plugins.messaging.MessagingPlugin;
import com.mobicage.rogerthat.util.OauthUtils;
import com.mobicage.rogerthat.util.TextUtils;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeDialogClick;
import com.mobicage.rogerthat.util.system.SafeViewOnClickListener;
import com.mobicage.rogerthat.util.ui.UIUtils;
import com.mobicage.rpc.IncompleteMessageException;
import com.mobicage.rpc.ResponseHandler;
import com.mobicage.rpc.config.LookAndFeelConstants;
import com.mobicage.to.messaging.forms.SubmitOauthFormRequestTO;
import com.mobicage.to.messaging.forms.SubmitOauthFormResponseTO;
import com.mobicage.to.messaging.forms.UnicodeWidgetResultTO;

import java.util.Map;

@SuppressWarnings("unchecked")
public class OauthWidget extends Widget {

    private static final int START_OAUTH_REQUEST_CODE = 1;

    private Button mOauthBtn;
    private TextView mOauthResultView;
    private UnicodeWidgetResultTO mResult;
    private String mUrl;
    private String mSuccessMessage;

    public OauthWidget(Context context) {
        super(context);
    }

    public OauthWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void initializeWidget() {
        mOauthResultView = (TextView) findViewById(R.id.oauth_result);
        mOauthBtn = (Button) findViewById(R.id.oauth_btn);
        mOauthBtn.setOnClickListener(new SafeViewOnClickListener() {
            @Override
            public void safeOnClick(View v) {
                authorize();
            }
        });

        mUrl = (String) mWidgetMap.get("url");
        String caption = (String) mWidgetMap.get("caption");
        if (TextUtils.isEmptyOrWhitespace(caption)) {
            caption = mActivity.getString(R.string.authorize);
        }
        mSuccessMessage = (String) mWidgetMap.get("success_message");
        if (TextUtils.isEmptyOrWhitespace(mSuccessMessage)) {
            mSuccessMessage = mActivity.getString(R.string.authorize_success);
        }
        IconicsDrawable d = new IconicsDrawable(mActivity, FontAwesome.Icon.faw_lock).color(LookAndFeelConstants.getPrimaryIconColor(mActivity)).sizeDp(24);
        mOauthBtn.setCompoundDrawablesWithIntrinsicBounds(d, null, null, null);
        mOauthBtn.setText(caption);

        Map<String, Object> result = (Map<String, Object>) mWidgetMap.get("value");
        if (result != null) {
            try {
                mResult = new UnicodeWidgetResultTO(result);
            } catch (IncompleteMessageException e) {
                L.bug(e);
            }
        }
        if (mResult != null) {
            mOauthBtn.setVisibility(View.GONE);
            mOauthResultView.setVisibility(View.VISIBLE);
            mOauthResultView.setText(mSuccessMessage);
        }
    }

    public static String valueString(Context context, Map<String, Object> widget) {
        String successMessage = (String) widget.get("success_message");
        if (TextUtils.isEmptyOrWhitespace(successMessage)) {
            successMessage = context.getString(R.string.authorize_success);
        }
        return widget.get("value") != null ? successMessage : null;
    }

    @Override
    public void putValue() {
        mWidgetMap.put("value", mResult == null ? null : mResult.toJSONMap());
    }

    @Override
    public UnicodeWidgetResultTO getWidgetResult() {
        return mResult;
    }

    @Override
    public boolean proceedWithSubmit(final String buttonId) {
        if (Message.POSITIVE.equals(buttonId)) {
            if (mResult == null) {
                String title = mActivity.getString(R.string.not_authenticated);
                String message = mActivity.getString(R.string.authenticate_first);
                String positiveCaption = (String) mOauthBtn.getText();
                String negativeCaption = mActivity.getString(R.string.cancel);
                SafeDialogClick onPositiveClick = new SafeDialogClick() {
                    @Override
                    public void safeOnClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        authorize();
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
        final SubmitOauthFormRequestTO request = new SubmitOauthFormRequestTO();
        request.button_id = buttonId;
        request.message_key = mMessage.key;
        request.parent_message_key = mMessage.parent_key;
        request.timestamp = timestamp;
        if (Message.POSITIVE.equals(buttonId)) {
            request.result = getWidgetResult();
        }
        if ((mMessage.flags & MessagingPlugin.FLAG_SENT_BY_JSMFR) == MessagingPlugin.FLAG_SENT_BY_JSMFR) {
            mPlugin.answerJsMfrMessage(mMessage, request.toJSONMap(),
                    "com.mobicage.api.messaging.submitOauthForm", mActivity, mParentView);
        } else {
            Rpc.submitOauthForm(new ResponseHandler<SubmitOauthFormResponseTO>(), request);
        }
    }

    private void authorize() {
        Uri.Builder builder = Uri.parse(mUrl).buildUpon();
        builder.appendQueryParameter("app_redirect_uri", OauthUtils.getCallbackUrl());

        Intent intent = new Intent(mActivity, OauthActivity.class);
        intent.putExtra(OauthActivity.OAUTH_URL, builder.build().toString());
        intent.putExtra(OauthActivity.BUILD_URL, false);
        intent.putExtra(OauthActivity.ALLOW_BACKPRESS, true);
        mActivity.startActivityForResult(intent, START_OAUTH_REQUEST_CODE);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == START_OAUTH_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                if (TextUtils.isEmptyOrWhitespace(data.getStringExtra(OauthActivity.RESULT_ERROR_MESSAGE))) {
                    mResult = new UnicodeWidgetResultTO();
                    mResult.value = data.getStringExtra(OauthActivity.RESULT_QUERY);

                    mOauthBtn.setVisibility(View.GONE);
                    mOauthResultView.setVisibility(View.VISIBLE);
                    mOauthResultView.setText(mSuccessMessage);
                    mActivity.excecutePositiveButtonClick();
                } else {
                    String message = data.getStringExtra(OauthActivity.RESULT_ERROR_MESSAGE);
                    UIUtils.showDialog(mActivity, null, message);
                }
            }
        }
    }
}

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

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;
import com.mobicage.api.messaging.Rpc;
import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.plugins.messaging.Message;
import com.mobicage.rogerthat.plugins.messaging.MessagingPlugin;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeViewOnClickListener;
import com.mobicage.rpc.IncompleteMessageException;
import com.mobicage.rpc.ResponseHandler;
import com.mobicage.rpc.config.LookAndFeelConstants;
import com.mobicage.to.messaging.forms.PayWidgetResultTO;
import com.mobicage.to.messaging.forms.SubmitPayFormRequestTO;
import com.mobicage.to.messaging.forms.SubmitPayFormResponseTO;

import java.util.Map;

@SuppressWarnings("unchecked")
public class PayWidget extends Widget {

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
        showResult();
    }

    private void showResult() {
        mPayBtn.setVisibility(View.GONE);
        mResultData.removeAllViews();
        mResultData.setVisibility(View.VISIBLE);

        addRow(mActivity.getString(R.string.transaction_id), "trans id");
        addRow(mActivity.getString(R.string.provider_id), "prov id");
        addRow(mActivity.getString(R.string.status), "pending");
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

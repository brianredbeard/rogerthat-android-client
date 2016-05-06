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

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Checkable;

import com.mobicage.api.messaging.Rpc;
import com.mobicage.rogerthat.plugins.messaging.Message;
import com.mobicage.rogerthat.plugins.messaging.MessagingPlugin;
import com.mobicage.rogerthat.util.system.SafeViewOnClickListener;
import com.mobicage.rpc.ResponseHandler;
import com.mobicage.to.messaging.forms.SubmitMultiSelectFormRequestTO;
import com.mobicage.to.messaging.forms.SubmitMultiSelectFormResponseTO;
import com.mobicage.to.messaging.forms.UnicodeListWidgetResultTO;

@SuppressWarnings("unchecked")
public class MultiSelectWidget extends AbstractSelectWidget {

    public MultiSelectWidget(Context context) {
        super(context);
    }

    public MultiSelectWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected int getTextViewResourceId() {
        return android.R.layout.simple_list_item_multiple_choice;
    }

    @Override
    protected List<String> getDefaultValues() {
        return (List<String>) mWidgetMap.get("values");
    }

    @Override
    protected SafeViewOnClickListener getItemOnClickListener(final Checkable tv) {
        return new SafeViewOnClickListener() {
            @Override
            public void safeOnClick(View v) {
                if (tv == v)
                    tv.setChecked(!tv.isChecked());
            }
        };
    }

    @Override
    public void putValue() {
        mWidgetMap.put("values", getSelectedValues());
    }

    @Override
    public UnicodeListWidgetResultTO getFormResult() {
        UnicodeListWidgetResultTO result = new UnicodeListWidgetResultTO();
        result.values = ((List<String>) mWidgetMap.get("values")).toArray(new String[] {});
        return result;
    }

    @Override
    public void submit(final String buttonId, long timestamp) throws Exception {
        SubmitMultiSelectFormRequestTO request = new SubmitMultiSelectFormRequestTO();
        request.button_id = buttonId;
        request.message_key = mMessage.key;
        request.parent_message_key = mMessage.parent_key;
        request.timestamp = timestamp;
        if (Message.POSITIVE.equals(buttonId)) {
            request.result = getFormResult();
        }
        if ((mMessage.flags & MessagingPlugin.FLAG_SENT_BY_JSMFR) == MessagingPlugin.FLAG_SENT_BY_JSMFR)
            mPlugin.answerJsMfrMessage(mMessage, request.toJSONMap(),
                "com.mobicage.api.messaging.submitMultiSelectForm", mActivity, mParentView);
        else
            Rpc.submitMultiSelectForm(new ResponseHandler<SubmitMultiSelectFormResponseTO>(), request);
    }

    public static String valueString(Context context, Map<String, Object> widget) {
        List<String> selectedValues = (List<String>) widget.get("values");

        if (selectedValues != null && selectedValues.size() != 0) {
            StringBuilder sb = new StringBuilder();

            for (Map<String, Object> choice : (List<Map<String, Object>>) widget.get("choices")) {
                if (selectedValues.contains(choice.get("value"))) {
                    if (sb.length() != 0)
                        sb.append("\n");
                    sb.append((String) choice.get("label"));
                }
            }

            return sb.toString();
        }

        return null;
    }
}
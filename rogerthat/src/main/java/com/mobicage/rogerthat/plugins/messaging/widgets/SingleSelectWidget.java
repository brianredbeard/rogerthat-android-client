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

import java.util.ArrayList;
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
import com.mobicage.to.messaging.forms.SubmitSingleSelectFormRequestTO;
import com.mobicage.to.messaging.forms.SubmitSingleSelectFormResponseTO;
import com.mobicage.to.messaging.forms.UnicodeWidgetResultTO;

public class SingleSelectWidget extends AbstractSelectWidget {

    public SingleSelectWidget(Context context) {
        super(context);
    }

    public SingleSelectWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected SafeViewOnClickListener getItemOnClickListener(final Checkable tv) {
        return new SafeViewOnClickListener() {
            @Override
            public void safeOnClick(View v) {
                for (int i = 0; i < getChildCount(); i++) {
                    Checkable ctv = (Checkable) getChildAt(i);
                    ctv.setChecked((ctv == tv));
                }
            }
        };
    }

    @Override
    protected int getTextViewResourceId() {
        return android.R.layout.simple_list_item_single_choice;
    }

    @Override
    protected List<String> getDefaultValues() {
        List<String> defaults = new ArrayList<String>();
        String value = (String) mWidgetMap.get("value");
        if (value != null)
            defaults.add(value);
        return defaults;
    }

    @Override
    public void putValue() {
        List<String> selectedValues = getSelectedValues();
        mWidgetMap.put("value", (selectedValues.size() > 0) ? selectedValues.get(0) : null);
    }

    @Override
    public UnicodeWidgetResultTO getWidgetResult() {
        UnicodeWidgetResultTO r = new UnicodeWidgetResultTO();
        r.value = (String) mWidgetMap.get("value");
        return r;
    }

    @Override
    public void submit(final String buttonId, long timestamp) throws Exception {
        SubmitSingleSelectFormRequestTO request = new SubmitSingleSelectFormRequestTO();
        request.button_id = buttonId;
        request.message_key = mMessage.key;
        request.parent_message_key = mMessage.parent_key;
        request.timestamp = timestamp;
        if (Message.POSITIVE.equals(buttonId)) {
            request.result = getWidgetResult();
        }
        if ((mMessage.flags & MessagingPlugin.FLAG_SENT_BY_JSMFR) == MessagingPlugin.FLAG_SENT_BY_JSMFR)
            mPlugin.answerJsMfrMessage(mMessage, request.toJSONMap(),
                "com.mobicage.api.messaging.submitSingleSelectForm", mActivity, mParentView);
        else
            Rpc.submitSingleSelectForm(new ResponseHandler<SubmitSingleSelectFormResponseTO>(), request);
    }

    @SuppressWarnings("unchecked")
    public static String valueString(Context context, Map<String, Object> widget) {
        String selectedValue = (String) widget.get("value");
        if (selectedValue != null)
            for (Map<String, Object> choice : (List<Map<String, Object>>) widget.get("choices"))
                if (selectedValue.equals(choice.get("value")))
                    return (String) choice.get("label");

        return null;
    }

}

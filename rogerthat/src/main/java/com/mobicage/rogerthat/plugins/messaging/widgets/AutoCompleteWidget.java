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

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.mobicage.api.messaging.Rpc;
import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.plugins.messaging.Message;
import com.mobicage.rogerthat.plugins.messaging.MessagingPlugin;
import com.mobicage.rpc.ResponseHandler;
import com.mobicage.to.messaging.forms.SubmitAutoCompleteFormRequestTO;
import com.mobicage.to.messaging.forms.SubmitAutoCompleteFormResponseTO;

public class AutoCompleteWidget extends TextLineWidget {

    public AutoCompleteWidget(Context context) {
        super(context);
    }

    public AutoCompleteWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void initializeWidget() {
        super.initializeWidget();

        AutoCompleteTextView ac = (AutoCompleteTextView) findViewById(R.id.edit_text);
        ac.setAdapter(new ArrayAdapter<String>(getContext(), R.layout.list_item, (List<String>) mWidgetMap
            .get("choices")));
    }

    @Override
    public void submit(final String buttonId, long timestamp) throws Exception {
        SubmitAutoCompleteFormRequestTO request = new SubmitAutoCompleteFormRequestTO();
        request.button_id = buttonId;
        request.message_key = mMessage.key;
        request.parent_message_key = mMessage.parent_key;
        request.timestamp = timestamp;
        if (Message.POSITIVE.equals(buttonId)) {
            request.result = getFormResult();
        }
        if ((mMessage.flags & MessagingPlugin.FLAG_SENT_BY_JSMFR) == MessagingPlugin.FLAG_SENT_BY_JSMFR)
            mPlugin.answerJsMfrMessage(mMessage, request.toJSONMap(),
                "com.mobicage.api.messaging.submitAutoCompleteForm", mActivity, mParentView);
        else
            Rpc.submitAutoCompleteForm(new ResponseHandler<SubmitAutoCompleteFormResponseTO>(), request);
    }

}
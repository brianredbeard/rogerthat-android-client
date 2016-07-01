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

import android.content.Context;
import android.util.AttributeSet;

import com.mobicage.api.messaging.Rpc;
import com.mobicage.rogerthat.plugins.messaging.Message;
import com.mobicage.rogerthat.plugins.messaging.MessagingPlugin;
import com.mobicage.rpc.ResponseHandler;
import com.mobicage.to.messaging.forms.SubmitTextBlockFormRequestTO;
import com.mobicage.to.messaging.forms.SubmitTextBlockFormResponseTO;
import com.mobicage.to.messaging.forms.UnicodeWidgetResultTO;

public class TextBlockWidget extends TextLineWidget {

    public TextBlockWidget(Context context) {
        super(context);
    }

    public TextBlockWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public UnicodeWidgetResultTO getFormResult() {
        UnicodeWidgetResultTO r = new UnicodeWidgetResultTO();
        r.value = (String) mWidgetMap.get("value");
        return r;
    }

    @Override
    public void submit(final String buttonId, long timestamp) throws Exception {
        SubmitTextBlockFormRequestTO request = new SubmitTextBlockFormRequestTO();
        request.button_id = buttonId;
        request.message_key = mMessage.key;
        request.parent_message_key = mMessage.parent_key;
        request.timestamp = timestamp;
        if (Message.POSITIVE.equals(buttonId)) {
            request.result = getFormResult();
        }
        if ((mMessage.flags & MessagingPlugin.FLAG_SENT_BY_JSMFR) == MessagingPlugin.FLAG_SENT_BY_JSMFR)
            mPlugin.answerJsMfrMessage(mMessage, request.toJSONMap(), "com.mobicage.api.messaging.submitTextBlockForm",
                mActivity, mParentView);
        else
            Rpc.submitTextBlockForm(new ResponseHandler<SubmitTextBlockFormResponseTO>(), request);
    }

}

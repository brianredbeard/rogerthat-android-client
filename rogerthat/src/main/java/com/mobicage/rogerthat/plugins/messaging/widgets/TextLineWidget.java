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

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.speech.RecognizerIntent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatAutoCompleteTextView;
import android.support.v7.widget.AppCompatEditText;
import android.text.InputFilter;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mobicage.api.messaging.Rpc;
import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.plugins.messaging.Message;
import com.mobicage.rogerthat.plugins.messaging.MessagingPlugin;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rpc.ResponseHandler;
import com.mobicage.rpc.config.AppConstants;
import com.mobicage.to.messaging.forms.SubmitTextLineFormRequestTO;
import com.mobicage.to.messaging.forms.SubmitTextLineFormResponseTO;
import com.mobicage.to.messaging.forms.UnicodeWidgetResultTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TextLineWidget extends Widget {

    public static final int REQUEST_CODE_VOICE = 123;

    private EditText mEditText;

    public TextLineWidget(Context context) {
        super(context);
    }

    public TextLineWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private static boolean isSpeechRecognitionActivityPresented(Activity callerActivity) {
        try {
            PackageManager pm = callerActivity.getPackageManager();
            List<ResolveInfo> activities = pm.queryIntentActivities(
                new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);

            if (activities.size() != 0) {
                return true;
            }
        } catch (Exception e) {
            L.bug(e);
        }
        return false;
    }

    public int getDefaultInputTypes() {
        return InputType.TYPE_NULL;
    }

    @Override
    public void initializeWidget() {
        mEditText = (EditText) findViewById(R.id.edit_text);
        mEditText.setText((String) mWidgetMap.get("value"));
        mEditText.setHint((String) mWidgetMap.get("place_holder"));
        mEditText.setFilters(new InputFilter[] { new InputFilter.LengthFilter(((Long) mWidgetMap.get("max_chars"))
            .intValue()) });
        mEditText.setInputType(getDefaultInputTypes() | KeyboardType.getInputType((String) mWidgetMap.get("keyboard_type")));
        mEditText.setTextColor(mTextColor);
        if (mEditText instanceof AppCompatEditText) {
            ((AppCompatEditText) mEditText).setSupportBackgroundTintList(ContextCompat.getColorStateList(mActivity, mColorId));
        } else if (mEditText instanceof AppCompatAutoCompleteTextView) {
            ((AppCompatAutoCompleteTextView) mEditText).setSupportBackgroundTintList(ContextCompat.getColorStateList(mActivity, mColorId));
        }


        ImageButton btnSpeak = (ImageButton) findViewById(R.id.btn_speak);
        if (AppConstants.SPEECH_TO_TEXT && isSpeechRecognitionActivityPresented(mActivity)) {
            btnSpeak.setVisibility(View.VISIBLE);
            btnSpeak.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    try {
                        Intent voiceIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                        voiceIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                        voiceIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1500);
                        voiceIntent.putExtra(
                            RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1500);
                        voiceIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 15000);
                        voiceIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
                        voiceIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                        mActivity.startActivityForResult(voiceIntent, REQUEST_CODE_VOICE);
                    } catch (ActivityNotFoundException e) {
                        L.bug(e);
                    }
                }
            });
        } else {
            btnSpeak.setVisibility(View.GONE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_VOICE) {
            if (resultCode == Activity.RESULT_OK) {
                TextView txtLbl = (TextView) findViewById(R.id.edit_text);
                ArrayList<String> text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                String txt = txtLbl.getText().toString();
                if (!"".equals(txt)) {
                    txt = txt + " ";
                }
                for (String str : text) {
                    txt = txt + str;
                }
                txtLbl.setText(txt);
            }
        }
    }

    @Override
    public void putValue() {
        mWidgetMap.put("value", mEditText.getText().toString());
    }

    @Override
    public UnicodeWidgetResultTO getWidgetResult() {
        UnicodeWidgetResultTO r = new UnicodeWidgetResultTO();
        r.value = (String) mWidgetMap.get("value");
        return r;
    }

    @Override
    public void submit(final String buttonId, long timestamp) throws Exception {
        SubmitTextLineFormRequestTO request = new SubmitTextLineFormRequestTO();
        request.button_id = buttonId;
        request.message_key = mMessage.key;
        request.parent_message_key = mMessage.parent_key;
        request.timestamp = timestamp;
        if (Message.POSITIVE.equals(buttonId)) {
            request.result = getWidgetResult();
        }
        if ((mMessage.flags & MessagingPlugin.FLAG_SENT_BY_JSMFR) == MessagingPlugin.FLAG_SENT_BY_JSMFR)
            mPlugin.answerJsMfrMessage(mMessage, request.toJSONMap(), "com.mobicage.api.messaging.submitTextLineForm",
                mActivity, mParentView);
        else
            Rpc.submitTextLineForm(new ResponseHandler<SubmitTextLineFormResponseTO>(), request);
    }

    public static String valueString(Context context, Map<String, Object> widget) {
        return (String) widget.get("value");
    }
}

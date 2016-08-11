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

import java.util.Map;
import java.util.UnknownFormatConversionException;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.SeekBar;
import android.widget.TextView;

import com.mobicage.api.messaging.Rpc;
import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.plugins.messaging.Message;
import com.mobicage.rogerthat.plugins.messaging.MessagingPlugin;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rpc.ResponseHandler;
import com.mobicage.to.messaging.forms.FloatWidgetResultTO;
import com.mobicage.to.messaging.forms.SubmitSingleSliderFormRequestTO;
import com.mobicage.to.messaging.forms.SubmitSingleSliderFormResponseTO;

public class SingleSliderWidget extends Widget implements SeekBar.OnSeekBarChangeListener {

    private SeekBar mSeekBar;
    private TextView mTextView;
    protected double mMin;
    protected double mMax;
    protected double mStep;
    protected long mPrecision;
    protected long mFactor;
    protected String mFormat;

    public SingleSliderWidget(Context context) {
        super(context);
    }

    public SingleSliderWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void initializeWidget() {
        mMax = (Double) mWidgetMap.get("max");
        mMin = (Double) mWidgetMap.get("min");
        mStep = (Double) mWidgetMap.get("step");
        if (mStep == 0)
            mStep = 1;
        mPrecision = (Long) mWidgetMap.get("precision");
        mFactor = (long) Math.pow(10, mPrecision);
        String unit = (String) mWidgetMap.get("unit");
        if (unit == null)
            unit = Message.UNIT_VALUE;
        mFormat = unit.replace(Message.UNIT_VALUE, "%." + mPrecision + "f");

        double value = (Double) mWidgetMap.get("value");

        mTextView = (TextView) findViewById(R.id.slider_text);
        mTextView.setTextColor(mTextColor);
        try {
            mTextView.setText(String.format(mFormat, value));
        } catch (UnknownFormatConversionException e) {
            L.e(e);
            mFormat = "%." + mPrecision + "f";
            mTextView.setText(String.format(mFormat, value));
        }

        mSeekBar = (SeekBar) findViewById(R.id.seek_bar);
        mSeekBar.setOnSeekBarChangeListener(this);
        mSeekBar.setMax(toProgress(mMax));
        mSeekBar.setProgress(toProgress(value));
        onProgressChanged(mSeekBar, mSeekBar.getProgress(), false);
    }

    private int toProgress(double value) {
        int progress = (int) ((value - mMin) * mFactor);
        return progress;
    }

    private double fromProgress(int progress) {
        double value = ((double) progress / mFactor + mMin);
        value = Math.round(value / mStep) * mStep;
        return value;
    }

    @Override
    public void putValue() {
        mWidgetMap.put("value", fromProgress(mSeekBar.getProgress()));
    }

    @Override
    public FloatWidgetResultTO getWidgetResult() {
        FloatWidgetResultTO r = new FloatWidgetResultTO();
        Number value = (Number) mWidgetMap.get("value");
        r.value = value.floatValue();
        return r;
    }

    @Override
    public void submit(final String buttonId, long timestamp) throws Exception {
        SubmitSingleSliderFormRequestTO request = new SubmitSingleSliderFormRequestTO();
        request.button_id = buttonId;
        request.message_key = mMessage.key;
        request.parent_message_key = mMessage.parent_key;
        request.timestamp = timestamp;
        if (Message.POSITIVE.equals(buttonId)) {
            request.result = getWidgetResult();
        }
        if ((mMessage.flags & MessagingPlugin.FLAG_SENT_BY_JSMFR) == MessagingPlugin.FLAG_SENT_BY_JSMFR)
            mPlugin.answerJsMfrMessage(mMessage, request.toJSONMap(),
                "com.mobicage.api.messaging.submitSingleSliderForm", mActivity, mParentView);
        else
            Rpc.submitSingleSliderForm(new ResponseHandler<SubmitSingleSliderFormResponseTO>(), request);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        double value = fromProgress(progress);
        mTextView.setText(String.format(mFormat, value));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        L.d("onStartTrackingTouch");
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        L.d("onStopTrackingTouch");
    }

    public static String valueString(Context context, Map<String, Object> widget) {
        Long precision = (Long) widget.get("precision");
        double value = (Double) widget.get("value");
        String unit = (String) widget.get("unit");
        if (unit == null)
            unit = Message.UNIT_VALUE;
        String format = unit.replace(Message.UNIT_VALUE, "%." + precision + "f");

        try {
            return String.format(format, value);
        } catch (UnknownFormatConversionException e) {
            L.e(e);
            return String.format("%." + precision + "f", value);
        }
    }

}

/*
 * Copyright 2017 Mobicage NV
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
 * @@license_version:1.2@@
 */

package com.mobicage.rogerthat.plugins.messaging.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.mobicage.api.messaging.Rpc;
import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.plugins.messaging.BrandingMgr;
import com.mobicage.rogerthat.plugins.messaging.Message;
import com.mobicage.rogerthat.plugins.messaging.MessagingPlugin;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rpc.ResponseHandler;
import com.mobicage.rpc.config.LookAndFeelConstants;
import com.mobicage.to.messaging.forms.FloatListWidgetResultTO;
import com.mobicage.to.messaging.forms.SubmitRangeSliderFormRequestTO;
import com.mobicage.to.messaging.forms.SubmitRangeSliderFormResponseTO;

import org.florescu.android.rangeseekbar.RangeSeekBar;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.UnknownFormatConversionException;

public class RangeSliderWidget extends Widget implements RangeSeekBar.OnRangeSeekBarChangeListener {

    private RangeSeekBar mRangeSeekBar;
    private TextView mTextView;
    protected double mStep;
    protected long mPrecision;
    protected String mFormat;

    public RangeSliderWidget(Context context) {
        super(context);
    }

    public RangeSliderWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void initializeWidget() {
        double min = (Double) mWidgetMap.get("min");
        double max = (Double) mWidgetMap.get("max");
        double low = (Double) mWidgetMap.get("low_value");
        double high = (Double) mWidgetMap.get("high_value");

        mStep = (Double) mWidgetMap.get("step");
        if (mStep == 0)
            mStep = 1;
        mPrecision = (Long) mWidgetMap.get("precision");
        String unit = (String) mWidgetMap.get("unit");
        mTextView = (TextView) findViewById(R.id.slider_text);
        mTextView.setTextColor(mTextColor);
        if (unit == null)
            unit = Message.UNIT_LOW_VALUE + " - " + Message.UNIT_HIGH_VALUE;

        if (mColorScheme == BrandingMgr.ColorScheme.DARK) {
            findViewById(R.id.range_slider).setVisibility(View.GONE);
            mRangeSeekBar = (RangeSeekBar) findViewById(R.id.range_slider_white);
            mRangeSeekBar.setVisibility(View.VISIBLE);
        } else {
            mRangeSeekBar = (RangeSeekBar) findViewById(R.id.range_slider);
        }

        mFormat = unit.replace(Message.UNIT_LOW_VALUE, "%1$." + mPrecision + "f")
                .replace(Message.UNIT_HIGH_VALUE, "%2$." + mPrecision + "f");

        mRangeSeekBar.setTextAboveThumbsColor(mTextColor);
        mRangeSeekBar.setNotifyWhileDragging(true);
        mRangeSeekBar.setRangeValues(min, max);
        mRangeSeekBar.setSelectedMaxValue(high);
        mRangeSeekBar.setSelectedMinValue(low);
        mRangeSeekBar.setOnRangeSeekBarChangeListener(this);
        this.onRangeSeekBarValuesChanged(mRangeSeekBar, min, max);
        // Use reflection to set the line to the correct color
        // Changing the color of the thumbs via code seems to be impossible so they're gray for now.
        changeColors("defaultColor");
        changeColors("activeColor");
    }

    private void changeColors(String varName) {
        try {
            final Field field = mRangeSeekBar.getClass().getDeclaredField(varName);
            field.setAccessible(true);
            field.set(mRangeSeekBar, LookAndFeelConstants.getPrimaryColor(mActivity));
        } catch (IllegalAccessException e) {
            L.bug(e);
        } catch (NoSuchFieldException e) {
            L.bug(e);
        }
    }

    private double rounded(Double value) {
        return Math.round(value / mStep) * mStep;
    }

    @Override
    public void putValue() {
        mWidgetMap.put("low_value", rounded((Double) mRangeSeekBar.getSelectedMinValue()));
        mWidgetMap.put("high_value", rounded((Double) mRangeSeekBar.getSelectedMaxValue()));
    }

    @Override
    public FloatListWidgetResultTO getWidgetResult() {
        FloatListWidgetResultTO r = new FloatListWidgetResultTO();
        r.values = new float[2];
        r.values[0] = ((Number) mWidgetMap.get("low_value")).floatValue();
        r.values[1] = ((Number) mWidgetMap.get("high_value")).floatValue();
        return r;
    }

    @Override
    public void submit(final String buttonId, long timestamp) throws Exception {
        SubmitRangeSliderFormRequestTO request = new SubmitRangeSliderFormRequestTO();
        request.button_id = buttonId;
        request.message_key = mMessage.key;
        request.parent_message_key = mMessage.parent_key;
        request.timestamp = timestamp;
        if (Message.POSITIVE.equals(buttonId)) {
            request.result = getWidgetResult();
        }
        if ((mMessage.flags & MessagingPlugin.FLAG_SENT_BY_JSMFR) == MessagingPlugin.FLAG_SENT_BY_JSMFR)
            mPlugin.answerJsMfrMessage(mMessage, request.toJSONMap(),
                    "com.mobicage.api.messaging.submitRangeSliderForm", mActivity, mParentView);
        else
            Rpc.submitRangeSliderForm(new ResponseHandler<SubmitRangeSliderFormResponseTO>(), request);
    }

    @Override
    public void onRangeSeekBarValuesChanged(RangeSeekBar bar, Number minValue, Number maxValue) {
        Double min = rounded((Double) minValue);
        Double max = rounded((Double) maxValue);
        try {
            mTextView.setText(String.format(mFormat, min, max));
        } catch (UnknownFormatConversionException e) {
            L.e(e);
            mFormat = "%1$." + mPrecision + "f - %2$." + mPrecision + "f";
            mTextView.setText(String.format(mFormat, min, max));
        }
    }

    public static String valueString(Context context, Map<String, Object> widget) {
        Long precision = (Long) widget.get("precision");
        double lowValue = (Double) widget.get("low_value");
        double highValue = (Double) widget.get("high_value");
        String unit = (String) widget.get("unit");
        if (unit == null)
            unit = "<low_value/> - <high_value/>";
        String format = unit.replace(Message.UNIT_LOW_VALUE, "%1$." + precision + "f")
                .replace(Message.UNIT_HIGH_VALUE, "%2$." + precision + "f");

        try {
            return String.format(format, lowValue, highValue);
        } catch (UnknownFormatConversionException e) {
            L.e(e);
            return String.format("%1$." + precision + "f - %2$." + precision + "f", lowValue, highValue);
        }
    }
}

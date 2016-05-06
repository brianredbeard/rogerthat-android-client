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

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UnknownFormatConversionException;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.View;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.mobicage.api.messaging.Rpc;
import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.plugins.messaging.Message;
import com.mobicage.rogerthat.plugins.messaging.MessagingPlugin;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SystemUtils;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.ui.UIUtils;
import com.mobicage.rpc.ResponseHandler;
import com.mobicage.to.messaging.forms.LongWidgetResultTO;
import com.mobicage.to.messaging.forms.SubmitDateSelectFormRequestTO;
import com.mobicage.to.messaging.forms.SubmitDateSelectFormResponseTO;

public class DateSelectWidget extends Widget {

    public static String MODE_DATE = "date";
    public static String MODE_TIME = "time";
    public static String MODE_DATE_TIME = "date_time";

    private Long mDateInMillis = null;
    private Long mMaxDateInMillis = null;
    private Long mMinDateInMillis = null;
    private int mMinuteInterval;
    private String mMode;
    private String mFormat;
    private TextView mTextView;
    private LinearLayout mContainer;
    private DatePicker mDatePicker;
    private TimePicker mTimePicker;
    private Calendar mCal;
    private boolean mIgnoreDateOrTimeChanges = false;
    private Toast mCurrentToast;

    public DateSelectWidget(Context context) {
        super(context);
    }

    public DateSelectWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void initializeWidget() {
        T.UI();
        mDateInMillis = (Boolean) mWidgetMap.get("has_date") ? (Long) mWidgetMap.get("date") * 1000 : null;
        mMinDateInMillis = (Boolean) mWidgetMap.get("has_min_date") ? (Long) mWidgetMap.get("min_date") * 1000 : null;
        mMaxDateInMillis = (Boolean) mWidgetMap.get("has_max_date") ? (Long) mWidgetMap.get("max_date") * 1000 : null;

        mMode = (String) mWidgetMap.get("mode");
        L.d("date_select mode: " + mMode);

        mMinuteInterval = MODE_DATE.equals(mMode) ? 24 * 60 : ((Long) mWidgetMap.get("minute_interval")).intValue();

        String unit = (String) mWidgetMap.get("unit");
        mFormat = (unit != null) ? unit.replace(Message.UNIT_VALUE, "%s") : "%s";

        mTextView = (TextView) findViewById(R.id.label);
        mTextView.setTextColor(mTextColor);

        mCal = Calendar.getInstance();
        mCal.setTimeZone(TimeZone.getTimeZone("UTC"));

        if (isEnabled()) {
            initDateSlider();
        } else if (mDateInMillis != null) {
            mCal.setTimeInMillis(mDateInMillis);
            updateLabel();
        }
    }

    private void initDateSlider() {
        T.UI();
        int resource;
        if (MODE_DATE.equals(mMode)) {
            resource = R.layout.ds_date_picker;
        } else if (MODE_TIME.equals(mMode)) {
            resource = R.layout.ds_time_picker;
        } else {
            if (!MODE_DATE_TIME.equals(mMode)) {
                L.e("I dont know date_select mode '" + mMode + "'. Falling back to date_time");
            }
            resource = R.layout.ds_date_time_picker;
        }

        if (mDateInMillis != null) {
            mCal.setTimeInMillis(mDateInMillis);
        } else {
            long currentMillis = mCal.getTimeInMillis() + TimeZone.getDefault().getRawOffset();
            currentMillis -= currentMillis % (mMinuteInterval * 60000); // floor to minute interval
            mCal.setTimeInMillis(currentMillis);

            if (MODE_TIME.equals(mMode)) {
                // Clear date/year/month
                mCal.set(Calendar.YEAR, 1970);
                mCal.set(Calendar.DAY_OF_YEAR, 1);
            }

            if (mMinDateInMillis != null && mCal.getTimeInMillis() < mMinDateInMillis)
                mCal.setTimeInMillis(mMinDateInMillis);
            else if (mMaxDateInMillis != null && mCal.getTimeInMillis() > mMaxDateInMillis)
                mCal.setTimeInMillis(mMaxDateInMillis);
        }

        mContainer = (LinearLayout) inflate(getContext(), resource, null);
        if (SystemUtils.getAndroidVersion() >= 8) {
            // onConfigurationChanged is Added in API level 8
            boolean landscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
            mContainer.setOrientation(landscape ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL);
        }
        mDatePicker = (DatePicker) mContainer.findViewById(R.id.date_picker);
        mTimePicker = (TimePicker) mContainer.findViewById(R.id.time_picker);

        if (mDatePicker != null) {
            mDatePicker.setDescendantFocusability(DatePicker.FOCUS_BLOCK_DESCENDANTS);
            mDatePicker.init(mCal.get(Calendar.YEAR), mCal.get(Calendar.MONTH), mCal.get(Calendar.DAY_OF_MONTH),
                new DatePicker.OnDateChangedListener() {
                    @Override
                    public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        if (mIgnoreDateOrTimeChanges)
                            return;

                        mCal.set(Calendar.YEAR, year);
                        mCal.set(Calendar.MONTH, monthOfYear);
                        mCal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        checkBoundaries();
                        updateLabel();
                    }
                });
        }

        if (mTimePicker != null) {
            mTimePicker.setDescendantFocusability(TimePicker.FOCUS_BLOCK_DESCENDANTS);
            mTimePicker.setIs24HourView(android.text.format.DateFormat.is24HourFormat(getContext()));
            mTimePicker.setCurrentHour(mCal.get(Calendar.HOUR_OF_DAY));
            mTimePicker.setCurrentMinute(mCal.get(Calendar.MINUTE));
            mTimePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
                @Override
                public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                    if (mIgnoreDateOrTimeChanges)
                        return;

                    int oldMinute = mCal.get(Calendar.MINUTE);
                    int oldHour = mCal.get(Calendar.HOUR_OF_DAY);
                    if (oldMinute == minute && oldHour == hourOfDay)
                        return;

                    if (oldMinute != minute && mMinuteInterval > 1) {
                        boolean hourOfDayModified = false;
                        minute = (oldMinute > minute || oldMinute == 0 && minute == 59) ? oldMinute - mMinuteInterval
                            : oldMinute + mMinuteInterval;
                        if (minute >= 60) {
                            minute -= 60;
                            hourOfDay = (hourOfDay + 1) % 24;
                            hourOfDayModified = true;
                        } else if (minute < 0) {
                            minute += 60;
                            if (oldHour == hourOfDay) {
                                // Only decrease if TimePicker did not do it itself (different behavior between APIs)
                                hourOfDay = (hourOfDay - 1) % 24;
                                hourOfDayModified = true;
                            }
                        }
                        mCal.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        mCal.set(Calendar.MINUTE, minute);
                        mIgnoreDateOrTimeChanges = true;
                        try {
                            mTimePicker.setCurrentMinute(minute);
                            if (hourOfDayModified)
                                mTimePicker.setCurrentHour(hourOfDay);
                        } finally {
                            mIgnoreDateOrTimeChanges = false;
                        }
                    } else {
                        mCal.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        mCal.set(Calendar.MINUTE, minute);
                    }
                    checkBoundaries();
                    updateLabel();
                }
            });
        }

        updateLabel();
        addView(mContainer);
    }

    private void checkBoundaries() {
        T.UI();
        mIgnoreDateOrTimeChanges = true;
        try {
            long timeInMillis = mCal.getTimeInMillis();

            boolean wasOutsideBoundaries = false;
            if (mMinDateInMillis != null && timeInMillis < mMinDateInMillis) {
                mCal.setTimeInMillis(mMinDateInMillis);
                wasOutsideBoundaries = true;
                if (mCurrentToast != null) {
                    mCurrentToast.setText(R.string.min_date_reached);
                    mCurrentToast.show();
                } else {
                    mCurrentToast = UIUtils.showLongToast(getContext(), R.string.min_date_reached);
                }
            } else if (mMaxDateInMillis != null && timeInMillis > mMaxDateInMillis) {
                mCal.setTimeInMillis(mMaxDateInMillis);
                wasOutsideBoundaries = true;
                if (mCurrentToast != null) {
                    mCurrentToast.setText(R.string.max_date_reached);
                    mCurrentToast.show();
                } else {
                    mCurrentToast = UIUtils.showLongToast(getContext(), R.string.max_date_reached);
                }
            }

            if (wasOutsideBoundaries) {
                if (mDatePicker != null) {
                    mDatePicker.updateDate(mCal.get(Calendar.YEAR), mCal.get(Calendar.MONTH),
                        mCal.get(Calendar.DAY_OF_MONTH));
                }
                if (mTimePicker != null) {
                    mTimePicker.setCurrentHour(mCal.get(Calendar.HOUR_OF_DAY));
                    mTimePicker.setCurrentMinute(mCal.get(Calendar.MINUTE));
                }
            }
        } finally {
            mIgnoreDateOrTimeChanges = false;
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        T.UI();
        super.setEnabled(enabled);
        if (mContainer != null) {
            mContainer.setVisibility(enabled ? View.VISIBLE : View.GONE);
        }
    }

    private void updateLabel() {
        T.UI();
        mTextView.setText(valueString(getContext(), mMode, mFormat, mCal.getTimeInMillis()));
    }

    @Override
    public void putValue() {
        T.UI();
        mWidgetMap.put("date", mCal.getTimeInMillis() / 1000);
        mWidgetMap.put("has_date", true);
    }

    @Override
    public LongWidgetResultTO getFormResult() {
        final LongWidgetResultTO result = new LongWidgetResultTO();
        result.value = (Long) mWidgetMap.get("date");
        return result;
    }

    @Override
    public void submit(final String buttonId, long timestamp) throws Exception {
        T.UI();
        SubmitDateSelectFormRequestTO request = new SubmitDateSelectFormRequestTO();
        request.button_id = buttonId;
        request.message_key = mMessage.key;
        request.parent_message_key = mMessage.parent_key;
        request.timestamp = timestamp;
        if (Message.POSITIVE.equals(buttonId)) {
            request.result = getFormResult();
        }
        if ((mMessage.flags & MessagingPlugin.FLAG_SENT_BY_JSMFR) == MessagingPlugin.FLAG_SENT_BY_JSMFR)
            mPlugin.answerJsMfrMessage(mMessage, request.toJSONMap(),
                "com.mobicage.api.messaging.submitDateSelectForm", mActivity, mParentView);
        else
            Rpc.submitDateSelectForm(new ResponseHandler<SubmitDateSelectFormResponseTO>(), request);
    }

    private static String valueString(Context context, String mode, String format, Long dateInMillis) {
        T.UI();
        final List<DateFormat> dfs = new ArrayList<DateFormat>();

        if (MODE_DATE.equals(mode) || MODE_DATE_TIME.equals(mode)) {
            dfs.add(android.text.format.DateFormat.getMediumDateFormat(context));
        }
        if (MODE_TIME.equals(mode) || MODE_DATE_TIME.equals(mode)) {
            dfs.add(android.text.format.DateFormat.getTimeFormat(context));
        }

        String s = "";
        for (DateFormat df : dfs) {
            df.setTimeZone(TimeZone.getTimeZone("UTC"));

            if (s.length() != 0)
                s += " ";

            s += df.format(new Date(dateInMillis));
        }

        try {
            return String.format(format, s);
        } catch (UnknownFormatConversionException e) {
            L.bug("Error in String.format('" + format + "', '" + s + "')", e);
            return s;
        }
    }

    public static String valueString(Context context, Map<String, Object> widget) {
        T.UI();
        if (!(Boolean) widget.get("has_date"))
            return null;

        final Long date = (Long) widget.get("date") * 1000;
        final String mode = (String) widget.get("mode");
        final String unit = (String) widget.get("unit");
        final String format = (unit != null) ? unit.replace(Message.UNIT_VALUE, "%s") : "%s";

        return valueString(context, mode, format, date);
    }

    @Override
    @TargetApi(8)
    protected void onConfigurationChanged(Configuration newConfig) {
        T.UI();
        super.onConfigurationChanged(newConfig);
        if (mContainer != null) {
            boolean landscape = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE;
            mContainer.setOrientation(landscape ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL);
        }
    }

}
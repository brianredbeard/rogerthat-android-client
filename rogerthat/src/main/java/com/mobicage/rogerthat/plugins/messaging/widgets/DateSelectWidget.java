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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.mobicage.api.messaging.Rpc;
import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.plugins.messaging.Message;
import com.mobicage.rogerthat.plugins.messaging.MessagingPlugin;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeDialogInterfaceOnClickListener;
import com.mobicage.rogerthat.util.system.SafeViewOnClickListener;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.ui.UIUtils;
import com.mobicage.rpc.ResponseHandler;
import com.mobicage.to.messaging.forms.LongWidgetResultTO;
import com.mobicage.to.messaging.forms.SubmitDateSelectFormRequestTO;
import com.mobicage.to.messaging.forms.SubmitDateSelectFormResponseTO;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UnknownFormatConversionException;

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
    private AlertDialog mDatePickerDialog;
    private DatePicker mDatePicker;
    private AlertDialog mTimePickerDialog;
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
        ImageButton pickDate = (ImageButton) findViewById(R.id.pick_date);
        ImageButton pickTime = (ImageButton) findViewById(R.id.pick_time);
        if (MODE_DATE.equals(mMode)) {
            pickTime.setVisibility(View.GONE);
        } else if (MODE_TIME.equals(mMode)) {
            pickDate.setVisibility(View.GONE);
        } else {
            if (!MODE_DATE_TIME.equals(mMode)) {
                L.e("I don't know date_select mode '" + mMode + "'. Falling back to date_time");
            }
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

        if (pickDate.getVisibility() == View.VISIBLE) {
            final View dialog = mActivity.getLayoutInflater().inflate(R.layout.ds_date_picker, null);
            mDatePicker = (DatePicker) dialog.findViewById(R.id.date_picker);
            mDatePicker.setDescendantFocusability(DatePicker.FOCUS_BLOCK_DESCENDANTS);
            mDatePicker.init(mCal.get(Calendar.YEAR), mCal.get(Calendar.MONTH), mCal.get(Calendar.DAY_OF_MONTH),
                    new DatePicker.OnDateChangedListener() {
                        @Override
                        public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                            datePickerChanged(year, monthOfYear, dayOfMonth);
                        }
                    }
            );
            mDatePickerDialog = new AlertDialog.Builder(mActivity)
                    .setView(dialog)
                    .setPositiveButton(mActivity.getString(R.string.ok), new SafeDialogInterfaceOnClickListener() {
                        @Override
                        public void safeOnClick(DialogInterface di, int which) {
                            // Workaround for android 5.0 not triggering the changed listener
                            datePickerChanged(mDatePicker.getYear(), mDatePicker.getMonth(), mDatePicker.getDayOfMonth());

                        }
                    }).setNegativeButton(mActivity.getString(R.string.cancel), new SafeDialogInterfaceOnClickListener() {
                        @Override
                        public void safeOnClick(DialogInterface dialog, int which) {
                        }
                    }).create();
            mDatePickerDialog.setCanceledOnTouchOutside(true);

            pickDate.setOnClickListener(new SafeViewOnClickListener() {
                @Override
                public void safeOnClick(View v) {
                    mDatePickerDialog.show();
                }
            });
        }

        if (pickTime.getVisibility() == View.VISIBLE) {
            final View dialog = mActivity.getLayoutInflater().inflate(R.layout.ds_time_picker, null);
            mTimePicker = (TimePicker) dialog.findViewById(R.id.time_picker);
            mTimePicker.setDescendantFocusability(TimePicker.FOCUS_BLOCK_DESCENDANTS);
            mTimePicker.setIs24HourView(android.text.format.DateFormat.is24HourFormat(getContext()));
            mTimePicker.setCurrentHour(mCal.get(Calendar.HOUR_OF_DAY));
            mTimePicker.setCurrentMinute(mCal.get(Calendar.MINUTE));
            mTimePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
                @Override
                public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                    timePickerChanged(hourOfDay, minute);
                }
            });

            mTimePickerDialog = new AlertDialog.Builder(mActivity)
                    .setView(dialog)
                    .setPositiveButton(mActivity.getString(R.string.ok), new SafeDialogInterfaceOnClickListener() {
                        @Override
                        public void safeOnClick(DialogInterface di, int which) {
                            // Workaround for android 5.0 not triggering the changed listener
                            timePickerChanged(mTimePicker.getCurrentHour(), mTimePicker.getCurrentMinute());
                        }
                    }).setNegativeButton(mActivity.getString(R.string.cancel), new SafeDialogInterfaceOnClickListener() {
                        @Override
                        public void safeOnClick(DialogInterface dialog, int which) {
                        }
                    }).create();
            mTimePickerDialog.setCanceledOnTouchOutside(true);

            pickTime.setOnClickListener(new SafeViewOnClickListener() {
                @Override
                public void safeOnClick(View v) {
                    mTimePickerDialog.show();
                }
            });
        }

        updateLabel();
    }

    private void timePickerChanged(int hourOfDay, int minute) {
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

    private void datePickerChanged(int year, int month, int dayOfMonth) {
        if (mIgnoreDateOrTimeChanges)
            return;

        mCal.set(Calendar.YEAR, year);
        mCal.set(Calendar.MONTH, month);
        mCal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        checkBoundaries();
        updateLabel();
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
        if (!enabled) {
            ImageButton pickDate = (ImageButton) findViewById(R.id.pick_date);
            ImageButton pickTime = (ImageButton) findViewById(R.id.pick_time);
            pickDate.setVisibility(View.GONE);
            pickTime.setVisibility(View.GONE);
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
    public LongWidgetResultTO getWidgetResult() {
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
            request.result = getWidgetResult();
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
}

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

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.plugins.messaging.BrandingMgr;
import com.mobicage.rogerthat.plugins.messaging.Message;
import com.mobicage.rogerthat.plugins.messaging.MessagingPlugin;
import com.mobicage.rogerthat.plugins.messaging.ServiceMessageDetailActivity;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rpc.IJSONable;

public abstract class Widget extends LinearLayout {

    public static final String TYPE_TEXT_LINE = "text_line";
    public static final String TYPE_TEXT_BLOCK = "text_block";
    public static final String TYPE_AUTO_COMPLETE = "auto_complete";
    public static final String TYPE_SINGLE_SELECT = "single_select";
    public static final String TYPE_MULTI_SELECT = "multi_select";
    public static final String TYPE_DATE_SELECT = "date_select";
    public static final String TYPE_SINGLE_SLIDER = "single_slider";
    public static final String TYPE_RANGE_SLIDER = "range_slider";
    public static final String TYPE_PHOTO_UPLOAD = "photo_upload";
    public static final String TYPE_GPS_LOCATION = "gps_location";
    public static final String TYPE_MYDIGIPASS = "mydigipass";
    public static final String TYPE_ADVANCED_ORDER = "advanced_order";
    // Do not forget to update the valueString function when adding a form type

    public final static Map<String, Integer> RESOURCES;

    static {
        RESOURCES = new HashMap<String, Integer>();
        RESOURCES.put(TYPE_TEXT_LINE, R.layout.widget_text_line);
        RESOURCES.put(TYPE_TEXT_BLOCK, R.layout.widget_text_block);
        RESOURCES.put(TYPE_AUTO_COMPLETE, R.layout.widget_auto_complete);
        RESOURCES.put(TYPE_SINGLE_SELECT, R.layout.widget_single_select);
        RESOURCES.put(TYPE_MULTI_SELECT, R.layout.widget_multi_select);
        RESOURCES.put(TYPE_DATE_SELECT, R.layout.widget_date_select);
        RESOURCES.put(TYPE_SINGLE_SLIDER, R.layout.widget_single_slider);
        RESOURCES.put(TYPE_RANGE_SLIDER, R.layout.widget_range_slider);
        RESOURCES.put(TYPE_PHOTO_UPLOAD, R.layout.widget_photo_upload);
        RESOURCES.put(TYPE_GPS_LOCATION, R.layout.widget_gps_location);
        RESOURCES.put(TYPE_MYDIGIPASS, R.layout.widget_mydigipass);
        RESOURCES.put(TYPE_ADVANCED_ORDER, R.layout.widget_advanced_order);
    }

    protected Message mMessage;
    protected Map<String, Object> mWidgetMap;
    protected int mTextColor;
    protected MessagingPlugin mPlugin;
    protected ServiceMessageDetailActivity mActivity;
    protected ViewGroup mParentView;

    public Widget(Context context, AttributeSet attrs) {
        super(context, attrs);
        setColorScheme(BrandingMgr.ColorScheme.LIGHT);
    }

    public Widget(Context context) {
        super(context);
        setColorScheme(BrandingMgr.ColorScheme.LIGHT);
    }

    public Map<String, Object> getWidgetMap() {
        putValue();
        return mWidgetMap;
    }

    public void setColorScheme(BrandingMgr.ColorScheme colorScheme) {
        int colorId = android.R.color.primary_text_light;
        if (colorScheme == BrandingMgr.ColorScheme.DARK)
            colorId = android.R.color.primary_text_dark;

        mTextColor = getResources().getColor(colorId);
    }

    @SuppressWarnings("unchecked")
    public void loadMessage(Message message, final ServiceMessageDetailActivity activity, final ViewGroup parentView) {
        mPlugin = activity.getMainService().getPlugin(MessagingPlugin.class);
        mActivity = activity;
        mParentView = parentView;
        mMessage = message;
        mWidgetMap = (Map<String, Object>) message.form.get("widget");
        initializeWidget();
    }

    public abstract void initializeWidget();

    public abstract void putValue();

    public abstract IJSONable getFormResult();

    public boolean proceedWithSubmit(final String buttonId) {
        return true;
    }

    public abstract void submit(final String buttonId, final long timestamp) throws Exception;

    public static String valueString(Context context, String formType, Map<String, Object> widget) {
        if (TYPE_TEXT_LINE.equals(formType))
            return TextLineWidget.valueString(context, widget);

        if (TYPE_TEXT_BLOCK.equals(formType))
            return TextBlockWidget.valueString(context, widget);

        if (TYPE_AUTO_COMPLETE.equals(formType))
            return AutoCompleteWidget.valueString(context, widget);

        if (TYPE_SINGLE_SELECT.equals(formType))
            return SingleSelectWidget.valueString(context, widget);

        if (TYPE_MULTI_SELECT.equals(formType))
            return MultiSelectWidget.valueString(context, widget);

        if (TYPE_DATE_SELECT.equals(formType))
            return DateSelectWidget.valueString(context, widget);

        if (TYPE_SINGLE_SLIDER.equals(formType))
            return SingleSliderWidget.valueString(context, widget);

        if (TYPE_RANGE_SLIDER.equals(formType))
            return RangeSliderWidget.valueString(context, widget);

        if (TYPE_PHOTO_UPLOAD.equals(formType))
            return PhotoUploadWidget.valueString(context, widget);

        if (TYPE_GPS_LOCATION.equals(formType))
            return GPSLocationWidget.valueString(context, widget);

        if (TYPE_GPS_LOCATION.equals(formType))
            return GPSLocationWidget.valueString(context, widget);

        if (TYPE_MYDIGIPASS.equals(formType))
            return MyDigiPassWidget.valueString(context, widget);

        if (TYPE_ADVANCED_ORDER.equals(formType))
            return AdvancedOrderWidget.valueString(context, widget);

        L.bug("Unexpected form type: " + formType);
        return null;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    }

    public void onServiceUnbound() {
    }
}

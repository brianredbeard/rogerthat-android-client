/*
 * Copyright 2018 GIG Technology NV
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
 * @@license_version:1.4@@
 */

package com.mobicage.rogerthat.plugins.messaging.widgets;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Checkable;
import android.widget.CheckedTextView;
import android.widget.TextView;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.plugins.messaging.BrandingMgr;
import com.mobicage.rogerthat.util.system.SafeViewOnClickListener;
import com.mobicage.rogerthat.util.ui.UIUtils;
import com.mobicage.rpc.config.LookAndFeelConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public abstract class AbstractSelectWidget extends Widget {

    public AbstractSelectWidget(Context context) {
        super(context);
    }

    public AbstractSelectWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void initializeWidget() {
        List<String> defaults = getDefaultValues();

        int primaryColor = LookAndFeelConstants.getPrimaryColor(mActivity);

        for (Map<String, String> choice : (List<Map<String, String>>) mWidgetMap.get("choices")) {
            View v = LayoutInflater.from(getContext()).inflate(getTextViewResourceId(), null);
            final CheckedTextView ctv = (CheckedTextView) v;

            if (mColorScheme != BrandingMgr.ColorScheme.DARK) {
                UIUtils.setColors(primaryColor, ctv);
            }
            ctv.setChecked(defaults.contains(choice.get("value")));
            ctv.setText(choice.get("label"));
            ctv.setTextColor(mTextColor);
            ctv.setOnClickListener(getItemOnClickListener(ctv));
            addView(ctv);
        }
    }

    protected List<String> getSelectedValues() {
        List<Map<String, String>> choices = (List<Map<String, String>>) mWidgetMap.get("choices");
        List<String> values = new ArrayList<String>(choices.size());
        for (int i = 0; i < getChildCount(); i++) {
            Checkable ctv = (Checkable) getChildAt(i);
            if (ctv.isChecked())
                values.add(choices.get(i).get("value"));
        }
        return values;
    }

    protected abstract SafeViewOnClickListener getItemOnClickListener(final Checkable tv);

    protected abstract int getTextViewResourceId();

    protected abstract List<String> getDefaultValues();

}

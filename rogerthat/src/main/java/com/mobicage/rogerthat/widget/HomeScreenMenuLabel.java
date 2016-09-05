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

package com.mobicage.rogerthat.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.widget.TextView;

import com.mobicage.rogerth.at.R;

public class HomeScreenMenuLabel extends TextView {

    public HomeScreenMenuLabel(Context context) {
        super(context);
    }

    public HomeScreenMenuLabel(Context context, AttributeSet attrs) {
        super(context, attrs);
        calculateFontSize(attrs);
    }

    public HomeScreenMenuLabel(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        calculateFontSize(attrs);
    }

    public void calculateFontSize(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.HomeScreenMenuLabel);
        DisplayMetrics outMetrics = getResources().getDisplayMetrics();
        float density = outMetrics.density;
        float dpWidth = outMetrics.widthPixels / density;
        float dpTextSizeOnNexus10 = a.getInteger(R.styleable.HomeScreenMenuLabel_nexus10TextSize, 0);
        float dpScreenWidthOnNexus10 = 800;
        float dpFontSize = dpTextSizeOnNexus10 * dpWidth / dpScreenWidthOnNexus10;
        setTextSize(TypedValue.COMPLEX_UNIT_DIP, dpFontSize);
        a.recycle();
    }
}

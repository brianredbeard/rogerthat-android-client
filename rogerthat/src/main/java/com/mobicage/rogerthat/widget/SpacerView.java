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

package com.mobicage.rogerthat.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

import com.mobicage.rogerth.at.R;

public class SpacerView extends View {

    private int pxSpacerViewHeight = 0;

    public SpacerView(Context context) {
        super(context);
    }

    public SpacerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public SpacerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.SpacerView);
        DisplayMetrics outMetrics = getResources().getDisplayMetrics();
        float density = outMetrics.density;
        float dpWidth = outMetrics.widthPixels / density;
        int dpSpacerViewHeightOnNexus10 = a.getInteger(R.styleable.SpacerView_nexus10Height, 0);
        float dpScreenWidthOnNexus10 = 800;
        float dpSpacerViewHeight = dpSpacerViewHeightOnNexus10 * dpWidth / dpScreenWidthOnNexus10;
        pxSpacerViewHeight = Math.round(dpSpacerViewHeight * density);
        a.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        DisplayMetrics outMetrics = getResources().getDisplayMetrics();
        setMeasuredDimension(outMetrics.widthPixels, pxSpacerViewHeight);
    }
}

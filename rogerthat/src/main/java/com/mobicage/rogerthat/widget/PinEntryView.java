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
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.util.logging.L;

public class PinEntryView extends LinearLayout {

    private int[] pinArray = new int[4];
    private int charIndex = -1;

    private ImageView[] imgViews = new ImageView[4];

    private PinEntryListener mPinEntryListener;

    public PinEntryView(Context context) {
        super(context);
        init();
    }

    public PinEntryView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void setPinEntryListener(PinEntryListener view) {
        mPinEntryListener = view;
    }

    public void clearPinEntry() {
        while (charIndex > -1) {
            imgViews[charIndex].setSelected(false);
            charIndex--;
        }
    }

    public void sendKey(PinButtons key) throws Exception {
        switch (key) {
            case BUTTON_0:
            case BUTTON_1:
            case BUTTON_2:
            case BUTTON_3:
            case BUTTON_4:
            case BUTTON_5:
            case BUTTON_6:
            case BUTTON_7:
            case BUTTON_8:
            case BUTTON_9:
                if (charIndex >= -1 && charIndex <= 2) {
                    charIndex++;
                    imgViews[charIndex].setSelected(true);
                    pinArray[charIndex] = key.ordinal();
                }
                break;
            case BUTTON_CANCEL:
                mPinEntryListener.onPinCancelled();
                break;
            case BUTTON_DELETE:
                if (charIndex > -1) {
                    imgViews[charIndex].setSelected(false);
                    charIndex--;
                }
                break;
            case BUTTON_OK:
                if (charIndex == 3) {
                    processKeyEntryComplete();
                } else {
                    mPinEntryListener.onPinIncomplete();
                }
                break;
        }
    }

    private void init() {
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        View view = inflate(getContext(), R.layout.enter_pin_entry, null);
        imgViews[0] = (ImageView) view.findViewById(R.id.pe0);
        imgViews[1] = (ImageView) view.findViewById(R.id.pe1);
        imgViews[2] = (ImageView) view.findViewById(R.id.pe2);
        imgViews[3] = (ImageView) view.findViewById(R.id.pe3);
        addView(view, params);
    }

    private void processKeyEntryComplete() {
        final String pin = getString(pinArray);
        mPinEntryListener.onPinEntered(pin);
    }

    private String getString(int[] array) {
        String text = "";
        for (int i = 0; i < array.length; i++) {
            text = text + array[i];
        }
        return text;
    }
}

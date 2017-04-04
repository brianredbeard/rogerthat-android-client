/*
 * Copyright 2017 GIG Technology NV
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
 * @@license_version:1.3@@
 */

package com.mobicage.rogerthat.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.mobicage.rogerth.at.R;

public class PinKeyboardView extends LinearLayout {

    Button pin0;
    Button pin1;
    Button pin2;
    Button pin3;
    Button pin4;
    Button pin5;
    Button pin6;
    Button pin7;
    Button pin8;
    Button pin9;
    Button pinCancel;
    Button pinDelete;
    Button pinOk;

    private PinEntryView pinEntryView;

    public PinKeyboardView(Context context) {
        super(context);
        init();
    }

    public PinKeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }


    private void init() {
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        View view = inflate(getContext(), R.layout.enter_pin_keyboard, null);
        setupButtons(view);
        setupListeners();
        addView(view, params);
    }

    private void setupButtons(View view) {
        pin0 = (Button) view.findViewById(R.id.pin0);
        pin1 = (Button) view.findViewById(R.id.pin1);
        pin2 = (Button) view.findViewById(R.id.pin2);
        pin3 = (Button) view.findViewById(R.id.pin3);
        pin4 = (Button) view.findViewById(R.id.pin4);
        pin5 = (Button) view.findViewById(R.id.pin5);
        pin6 = (Button) view.findViewById(R.id.pin6);
        pin7 = (Button) view.findViewById(R.id.pin7);
        pin8 = (Button) view.findViewById(R.id.pin8);
        pin9 = (Button) view.findViewById(R.id.pin9);
        pinCancel = (Button) view.findViewById(R.id.pinCancel);
        pinDelete = (Button) view.findViewById(R.id.pinDelete);
        pinOk = (Button) view.findViewById(R.id.pinOk);
    }

    private void setupListeners() {
        pin0.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                setListener(PinButtons.BUTTON_0);
            }
        });
        pin1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                setListener(PinButtons.BUTTON_1);
            }
        });
        pin2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                setListener(PinButtons.BUTTON_2);
            }
        });
        pin3.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                setListener(PinButtons.BUTTON_3);
            }
        });
        pin4.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                setListener(PinButtons.BUTTON_4);
            }
        });
        pin5.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                setListener(PinButtons.BUTTON_5);
            }
        });
        pin6.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                setListener(PinButtons.BUTTON_6);
            }
        });
        pin7.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                setListener(PinButtons.BUTTON_7);
            }
        });
        pin8.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                setListener(PinButtons.BUTTON_8);
            }
        });
        pin9.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                setListener(PinButtons.BUTTON_9);
            }
        });
        pinCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                setListener(PinButtons.BUTTON_CANCEL);
            }
        });
        pinDelete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                setListener(PinButtons.BUTTON_DELETE);
            }
        });
        pinOk.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                setListener(PinButtons.BUTTON_OK);
            }
        });
    }

    private void setListener(PinButtons which) {
        try {
            if (pinEntryView != null) {
                pinEntryView.sendKey(which);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setPinEntryView(PinEntryView view) {
        this.pinEntryView = view;
    }
}

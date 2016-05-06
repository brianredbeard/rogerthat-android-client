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

package com.mobicage.rogerthat.util.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

public class BackButtonOverriddenEditText extends EditText {

    private OnBackButtonPressedHandler mOnBackButtonPressedHandler;

    public interface OnBackButtonPressedHandler {
        public boolean onBackButtonpressed();
    }

    public BackButtonOverriddenEditText(Context context) {
        super(context);
    }

    public BackButtonOverriddenEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BackButtonOverriddenEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setOnBackButtonPressed(OnBackButtonPressedHandler handler) {
        mOnBackButtonPressedHandler = handler;
    }

    @Override
    public boolean dispatchKeyEventPreIme(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
                if (mOnBackButtonPressedHandler != null) {
                    return mOnBackButtonPressedHandler.onBackButtonpressed();
                }
            } else if (event.getAction() == KeyEvent.ACTION_UP) {
                return true;
            }
        }
        return super.dispatchKeyEventPreIme(event);
    }
}
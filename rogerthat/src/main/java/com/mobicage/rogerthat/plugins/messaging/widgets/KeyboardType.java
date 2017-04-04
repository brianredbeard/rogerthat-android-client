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

package com.mobicage.rogerthat.plugins.messaging.widgets;

import android.text.InputType;

import com.mobicage.rogerthat.util.TextUtils;

public class KeyboardType {

    public static final String DEFAULT = "default";
    public static final String AUTO_CAPITALIZED = "auto_capitalized";
    public static final String EMAIL = "email";
    public static final String URL = "url";
    public static final String PHONE = "phone";
    public static final String NUMBER = "number";
    public static final String DECIMAL = "decimal";
    public static final String PASSWORD = "password";
    public static final String NUMBER_PASSWORD = "number_password";

    public static int getInputType(String keyboardType) {
        if (TextUtils.isEmptyOrWhitespace(keyboardType)) {
            return InputType.TYPE_CLASS_TEXT;
        }
        switch (keyboardType) {
            case AUTO_CAPITALIZED:
                return InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS;
            case EMAIL:
                return InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;
            case URL:
                return InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI;
            case PHONE:
                return InputType.TYPE_CLASS_PHONE;
            case NUMBER:
                return InputType.TYPE_CLASS_NUMBER;
            case DECIMAL:
                return InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL;
            case PASSWORD:
                return InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD;
            case NUMBER_PASSWORD:
                return InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD;
            case DEFAULT:
            default:
                return InputType.TYPE_CLASS_TEXT;
        }
    }
}

package com.mobicage.rogerthat.plugins.messaging.widgets;

import android.text.InputType;

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

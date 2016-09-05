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

package com.mobicage.rogerthat.util;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.mobicage.rogerth.at.R;

import java.math.BigInteger;

public class TextUtils {
    private TextUtils() {
        /* cannot be instantiated */
    }

    public static boolean isEmptyOrWhitespace(String s) {
        return s == null || s.trim().length() == 0;
    }

    // Copied from
    // http://stackoverflow.com/questions/332079/in-java-how-do-i-convert-a-byte-array-to-a-string-of-hex-digits-while-keeping-le
    public static String toHex(byte[] bytes) {
        BigInteger bi = new BigInteger(1, bytes);
        return String.format("%0" + (bytes.length << 1) + "X", bi);
    }

    // End copied

    public static String trimString(String s, int len, boolean addDots) {
        if (s.length() > len)
            s = s.substring(0, len) + (addDots ? "..." : "");
        return s;
    }

    //Font
    public static void overrideFonts(final Context context, final View v) {
        try {
            if (v instanceof ViewGroup) {
                ViewGroup vg = (ViewGroup) v;
                for (int i = 0; i < vg.getChildCount(); i++) {
                    View child = vg.getChildAt(i);
                    overrideFonts(context, child);
                }
            } else if (v instanceof EditText) {
                ((EditText) v).setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/lato_light.ttf"));
                ((EditText) v).setTextColor(ContextCompat.getColor(context, R.color.mc_words_color));
            } else if (v instanceof Button) {
                ((Button) v).setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/lato_bold.ttf"));
                ((Button) v).setTextColor(ContextCompat.getColor(context, android.R.color.white));
            } else if (v instanceof TextView) {
                ((TextView) v).setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/lato_light.ttf"));
                ((TextView) v).setTextColor(ContextCompat.getColor(context, R.color.mc_words_color));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


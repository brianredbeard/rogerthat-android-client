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

import java.math.BigInteger;
import java.util.HashMap;

public class TextUtils {
    public static HashMap<String, Typeface> fonts = new HashMap<>();
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

    public static synchronized Typeface getFont(Context context) {
        String fontType = "bold";
        return getFont(context, fontType);
    }

    public static synchronized Typeface getFont(Context context, String fontType) {
        if (fontType == null) {
            fontType = "bold";
        }
        String fileName = "fonts/lato_" + fontType + ".ttf";
        if (!fonts.containsKey(fileName)) {
            Typeface typeface = Typeface.createFromAsset(context.getAssets(), fileName);
            fonts.put(fileName, typeface);
        }
        return fonts.get(fileName);
    }
}

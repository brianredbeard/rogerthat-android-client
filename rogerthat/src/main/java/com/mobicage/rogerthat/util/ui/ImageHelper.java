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

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;

import com.mobicage.rogerthat.util.system.SystemUtils;
import com.soundcloud.android.crop.CropUtil;

// Copied from http://stackoverflow.com/questions/2459916/how-to-make-an-imageview-to-have-rounded-corners
// Based on ideas from http://ruibm.com/?p=184

public class ImageHelper {

    public static Bitmap getRoundedCornerAvatar(final Bitmap bitmap) {
        int roundingPixels = bitmap.getWidth() / 10;
        return getRoundedCornerBitmap(bitmap, roundingPixels);
    }

    public static Bitmap getRoundedCornerBitmap(final Bitmap bitmap, final int roundingPixels) {

        if (SystemUtils.getAndroidVersion() <= 3) {
            // burden on performance
            return bitmap;
        }

        if (bitmap == null)
            return null; // saw this once when phone complains about "not enough storage"

        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = roundingPixels;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    public static Bitmap getBitmapFromFile(final String imageFilePath) {
        return getBitmapFromFile(imageFilePath, null);
    }

    public static Bitmap getBitmapFromFile(final String imageFilePath, final BitmapFactory.Options options) {
        Bitmap bm = BitmapFactory.decodeFile(imageFilePath, options);
        int orientation = CropUtil.getExifRotation(imageFilePath);
        return rotateBitmap(bm, orientation);
    }

    public static Bitmap rotateBitmap(final Bitmap bm, final int orientation) {
        if (orientation != 0) {
            Matrix mat = new Matrix();
            mat.postRotate(orientation);
            return Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), mat, true);
        } else {
            return bm;
        }
    }

}
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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.ImageView;

import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeAsyncTask;
import com.mobicage.rogerthat.util.ui.ImageHelper;

import java.io.InputStream;


public class DownloadImageTask extends SafeAsyncTask<String, Void, Bitmap> {
    boolean rounded;
    int topRadius;
    ImageView bmImage;

    public DownloadImageTask(ImageView bmImage) {
        this.bmImage = bmImage;
        this.rounded = false;
        this.topRadius = 0;
    }

    public DownloadImageTask(ImageView bmImage, boolean rounded) {
        this.bmImage = bmImage;
        this.rounded = rounded;
        this.topRadius = 0;
    }

    public DownloadImageTask(ImageView bmImage, boolean rounded, int topRadius) {
        this.bmImage = bmImage;
        this.rounded = rounded;
        this.topRadius = topRadius;
    }

    @Override
    protected Bitmap safeDoInBackground(String... urls) {
        String urldisplay = urls[0];
        Bitmap mIcon11 = null;
        try {
            InputStream in = new java.net.URL(urldisplay).openStream();
            mIcon11 = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            L.d("DownloadImageTask error", e);
        }
        return mIcon11;
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        if (result != null) {
            if (rounded && topRadius > 0) {
                bmImage.setImageBitmap(ImageHelper.getRoundTopCornerBitmap(result, topRadius));
            } else if (rounded) {
                bmImage.setImageBitmap(ImageHelper.getRoundedCornerAvatar(result));
            } else {
                bmImage.setImageBitmap(result);
            }
            bmImage.setVisibility(View.VISIBLE);
        }
    }
}
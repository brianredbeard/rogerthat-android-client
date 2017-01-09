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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.ImageView;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeAsyncTask;
import com.mobicage.rogerthat.util.ui.ImageHelper;

import java.io.File;
import java.io.InputStream;


public class DownloadImageTask extends SafeAsyncTask<String, Void, Bitmap> {
    private boolean rounded;
    private int topRadius;
    private ImageView bmImage;
    private Context context;
    private CachedDownloader cachedDownloader;

    public DownloadImageTask(CachedDownloader cachedDownloader, ImageView bmImage, boolean rounded, Context context, int topRadius) {
        this.cachedDownloader = cachedDownloader;
        this.bmImage = bmImage;
        this.rounded = rounded;
        this.context = context;
        this.topRadius = topRadius;
    }

    @Override
    protected Bitmap safeDoInBackground(String... urls) {
        String urldisplay = urls[0];
        try {
            final Bitmap bitmap;
            if (cachedDownloader.isStorageAvailable()) {
                File cachedFile = cachedDownloader.getCachedFilePath(urldisplay);
                if (cachedFile != null) {
                    bitmap = BitmapFactory.decodeFile(cachedFile.getAbsolutePath());
                } else {
                    // item started downloading intent when ready
                    bitmap = null;
                }
            } else {
                bitmap = BitmapFactory.decodeStream(new java.net.URL(urldisplay).openStream());
            }
            if (bitmap != null) {
                if (rounded && topRadius > 0)
                    return ImageHelper.getRoundTopCornerBitmap(this.context, bitmap, topRadius);
                else if (rounded)
                    return ImageHelper.getRoundedCornerAvatar(bitmap);
            }
            return bitmap;
        } catch (Exception e) {
            L.d("DownloadImageTask error", e);
            return null;
        }
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        if (result != null) {
            bmImage.setImageBitmap(result);
            bmImage.setVisibility(View.VISIBLE);
        } else {
            if (rounded) {
                if (topRadius > 0) {
                    bmImage.setImageResource(R.drawable.news_image_placeholder_rounded);
                } else {
                    bmImage.setImageResource(R.drawable.news_avatar_placeholder);
                }
            } else {
                bmImage.setImageResource(R.drawable.news_image_placeholder);
            }
        }
    }
}

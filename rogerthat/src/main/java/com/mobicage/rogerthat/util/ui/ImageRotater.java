/*
 * Copyright 2017 Mobicage NV
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
 * @@license_version:1.2@@
 */
package com.mobicage.rogerthat.util.ui;

import android.os.Handler;
import android.widget.ImageView;

import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.system.T;

public class ImageRotater {

    private final ImageView mView;
    private final Handler mUIHandler;
    private boolean mRunning;
    private final int[] mResourceIds;
    private final int mTimeout;

    public ImageRotater(ImageView view, int[] resourceIds, int timeout) {
        T.UI();
        mView = view;
        mUIHandler = new Handler();
        mRunning = false;
        mResourceIds = resourceIds;
        mTimeout = timeout;
    }

    public void start() {
        T.UI();
        mRunning = true;
        loop();
    }

    public void stop() {
        T.UI();
        mRunning = false;
    }

    private void loop() {
        loop(0);
    }

    private void loop(final int position) {
        if (!mRunning)
            return;
        int resourceId = mResourceIds[position % mResourceIds.length];
        mView.setImageResource(resourceId);
        mUIHandler.postDelayed(new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                loop(position + 1);
            }
        }, mTimeout);
    }
}

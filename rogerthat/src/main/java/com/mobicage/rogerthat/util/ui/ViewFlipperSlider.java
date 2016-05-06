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

import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;

import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeRunnable;

public class ViewFlipperSlider implements OnGestureListener {
    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    private final SafeRunnable mOnSwipeLeft;
    private final SafeRunnable mOnSwipeRight;

    public ViewFlipperSlider(SafeRunnable onSwipeLeft, SafeRunnable onSwipeRight) {
        mOnSwipeLeft = onSwipeLeft;
        mOnSwipeRight = onSwipeRight;
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        L.d("-----------------------------------");

        if (e1 == null) {
            L.d("e1 is null");
            return false;
        }
        if (e2 == null) {
            L.d("e2 is null");
            return false;
        }

        // L.d("Swipe of path: " + Math.abs(e1.getY() - e2.getY()));

        if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH) {
            L.d("Swipe is off path, exiting.");
            return false;
        }

        // right to left swipe
        // L.d("Swipe distance: " + Math.abs(e1.getX() - e2.getX()));
        // L.d("Swipe velocity: " + Math.abs(velocityX));
        if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
            L.d("Going left");
            mOnSwipeLeft.run();
            return true;
        } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
            L.d("Going right");
            mOnSwipeRight.run();
            return true;
        }

        return false;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }
}
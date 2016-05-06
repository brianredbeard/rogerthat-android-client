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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeRunnable;

public class Slider implements OnGestureListener, OnDoubleTapListener {

    public interface Swiper {
        Intent onSwipe();
    }

    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;

    private Context mContext;
    private Activity mActivity;

    public interface OnDoubleTapListener {
        boolean onDoubleTap(MotionEvent event);
    }

    public interface OnLongPressListener {
        boolean onLongPress(MotionEvent event);
    }

    private OnDoubleTapListener mOnDoubleTapListener;
    private OnLongPressListener mOnLongPressListener;
    private Swiper mLeft;
    private Swiper mRight;

    public void setOnDoubleTapListener(OnDoubleTapListener onDoubleTapListener) {
        this.mOnDoubleTapListener = onDoubleTapListener;
    }

    public void setOnLongPressListener(OnLongPressListener onLongPressListener) {
        this.mOnLongPressListener = onLongPressListener;
    }

    public Slider(Context context, Activity activity, Swiper left, Swiper right) {
        mContext = context;
        mActivity = activity;
        mLeft = left;
        mRight = right;
    }

    public Activity getActivity() {
        return mActivity;
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
        Intent intent = null;
        if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
            L.d("Going left");
            intent = mLeft != null ? mLeft.onSwipe() : null;
            if (intent != null) {
                preTransition(true);
                mContext.startActivity(intent);
                postTransition(true);
                finish();
                return true;
            }
        } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
            L.d("Going right");
            intent = mRight != null ? mRight.onSwipe() : null;
            if (intent != null) {
                preTransition(false);
                mContext.startActivity(intent);
                postTransition(false);
                finish();
                return true;
            }
        }

        return false;
    }

    private void finish() {
        new Handler().postDelayed(new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                mActivity.finish();
            }
        }, 500);
    }

    // It is necessary to return true from onDown for the onFling event to register
    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }

    @Override
    public void onLongPress(MotionEvent arg0) {
        if (mOnLongPressListener != null)
            mOnLongPressListener.onLongPress(arg0);
    }

    @Override
    public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2, float arg3) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent arg0) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent arg0) {
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent event) {
        if (mOnDoubleTapListener != null)
            return mOnDoubleTapListener.onDoubleTap(event);
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent arg0) {
        return false;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent arg0) {
        return false;
    }

    protected void preTransition(boolean toLeft) {
    }

    protected void postTransition(boolean toLeft) {
        if (toLeft) {
            getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        } else {
            getActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        }
    }

}
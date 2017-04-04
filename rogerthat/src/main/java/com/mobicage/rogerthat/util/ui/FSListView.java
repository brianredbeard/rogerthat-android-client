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
package com.mobicage.rogerthat.util.ui;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.AbsListView;
import android.widget.ListView;

import com.mobicage.rogerthat.util.logging.L;

import java.lang.reflect.Field;

public class FSListView extends ListView {

    public FSListView(Context context) {
        super(context);

    }

    public FSListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FSListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        L.d(String.format("onSizeChanged(%s, %s, %s, %s);", w, h, oldw, oldh));

        // // XXX Should not use reflection!
        // // Fix width of overlay of fastscroller
        @SuppressWarnings("rawtypes")
        Class listViewClass = AbsListView.class;
        try {
            // 1. Get reference to the fastScroller
            Field fastScrollerField = listViewClass.getDeclaredField("mFastScroller");
            fastScrollerField.setAccessible(true);
            Object fastScroller = fastScrollerField.get(this);
            // 2. Get reference to the mOverlayDrawable and replace it with transparent one
            @SuppressWarnings("rawtypes")
            Class fastScrollerClass = fastScroller.getClass();
            // Field overlayPosSizeField = fastScrollerClass.getDeclaredField("mOverlaySize");
            // overlayPosSizeField.setAccessible(true);
            // Integer overlayPosSize = (Integer) overlayPosSizeField.get(fastScroller);
            // overlayPosSizeField.set(fastScroller, overlayPosSize * 2);
            Field overlayDrawableField = fastScrollerClass.getDeclaredField("mOverlayDrawable");
            overlayDrawableField.setAccessible(true);
            Drawable overlayDrawable = (Drawable) overlayDrawableField.get(fastScroller);
            // overlayDrawableField.set(fastScroller, getResources().getDrawable(R.drawable.fast_scroll_stub));
            Rect currentBounds = overlayDrawable.getBounds();
            int width = (currentBounds.right - currentBounds.left) / 2;
            overlayDrawable.setBounds(new Rect(currentBounds.left - width, currentBounds.top, currentBounds.right
                + width, currentBounds.bottom));
            // 3. Use better color for text
            // // Field paintField = fastScrollerClass.getDeclaredField("mPaint");
            // paintField.setAccessible(true);
            // Paint paint = (Paint) paintField.get(fastScroller);
            // paint.setColor(ContextCompat.getColor(R.color.mc_green));
        } catch (IllegalArgumentException e) {
            L.bug(e);
        } catch (IllegalAccessException e) {
            L.bug(e);
        } catch (SecurityException e) {
            L.bug(e);
        } catch (NoSuchFieldException e) {
            return;
        }
    }
}

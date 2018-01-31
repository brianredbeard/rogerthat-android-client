/*
 * Copyright 2018 GIG Technology NV
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
 * @@license_version:1.4@@
 */

package com.mobicage.rogerthat.util.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.MainService;
import com.mobicage.rogerthat.plugins.messaging.BrandingMgr;
import com.mobicage.rogerthat.util.TextUtils;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeRunnable;

import in.uncod.android.bypass.Bypass;

import static com.mobicage.rogerthat.util.ui.ImageHelper.getRoundedCornerAvatar;

public class ServiceHeader {

    public static void setupNative(final MainService mainService, final BrandingMgr.BrandingResult brandingResult,
                                   final LinearLayout headerContainer) {
        final FrameLayout imagesContainer = (FrameLayout) headerContainer.findViewById(R.id.images_container);
        final ImageView logoImage = (ImageView) headerContainer.findViewById(R.id.logo_image);
        final ImageView avatarImage = (ImageView) headerContainer.findViewById(R.id.avatar_image);
        final TextView senderNameView = (TextView) headerContainer.findViewById(R.id.sender_name);
        final TextView messageView = (TextView) headerContainer.findViewById(R.id.message);
        final Bitmap logo = BitmapFactory.decodeFile(brandingResult.logo.getAbsolutePath());
        headerContainer.setVisibility(View.VISIBLE);
        if (TextUtils.isEmptyOrWhitespace(brandingResult.senderName)) {
            senderNameView.setVisibility(View.GONE);
        } else {
            senderNameView.setText(brandingResult.senderName);
            senderNameView.setVisibility(View.VISIBLE);
        }
        if (TextUtils.isEmptyOrWhitespace(brandingResult.message)) {
            messageView.setVisibility(View.GONE);
        } else {
            messageView.setVisibility(View.VISIBLE);
            messageView.setText(TextUtils.toMarkDown(mainService, brandingResult.message));
            messageView.setMovementMethod(LinkMovementMethod.getInstance());
        }
        logoImage.setVisibility(View.VISIBLE);
        logoImage.setImageBitmap(logo);
        if (brandingResult.avatar != null) {
            if (logoImage.getHeight() != 0) {
                // This will be called when the branding is refreshed due to an intent, but the logo didn't change.
                // Therefore the height is already set and the layoutChangeListener shouldn't be attached again.
                setAvatarMargin(mainService, brandingResult, logoImage, imagesContainer, avatarImage);
                return;
            }
            // When the view is drawn for the first time, calculate the correct height of the container so that the
            // avatar can be shown on the correct place.
            logoImage.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop,
                                           int oldRight, int oldBottom) {
                    // It is possible that the layout is not complete in which case we will get all zero values for
                    // the positions, so ignore the event
                    if (left == 0 && top == 0 && right == 0 && bottom == 0) {
                        return;
                    }
                    setAvatarMargin(mainService, brandingResult, logoImage, imagesContainer, avatarImage);
                }
            });
        }
    }

    private static void setAvatarMargin(MainService mainService, final BrandingMgr.BrandingResult brandingResult,
                                        final ImageView logoImage, final FrameLayout imagesContainer,
                                        final ImageView avatarImage) {
        mainService.postOnUIHandler(new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                final Bitmap avatar = BitmapFactory.decodeFile(brandingResult.avatar.getAbsolutePath());
                avatarImage.setImageBitmap(getRoundedCornerAvatar(avatar));
                avatarImage.setVisibility(View.VISIBLE);

                int avatarHeight = logoImage.getHeight();
                int halfAvatarHeight = Math.round(avatarHeight / 2);
                
                LinearLayout.LayoutParams lpImagesContainer = (LinearLayout.LayoutParams) imagesContainer.getLayoutParams();
                lpImagesContainer.height = avatarHeight + halfAvatarHeight;
                imagesContainer.setLayoutParams(lpImagesContainer);

                ViewGroup.MarginLayoutParams avatarMarginLayoutParams = (ViewGroup.MarginLayoutParams) avatarImage.getLayoutParams();
                //noinspection SuspiciousNameCombination
                avatarMarginLayoutParams.width = avatarHeight;
                avatarMarginLayoutParams.height = avatarHeight;
                avatarMarginLayoutParams.topMargin = avatarHeight - halfAvatarHeight;
                avatarImage.setLayoutParams(avatarMarginLayoutParams);
            }
        });
    }
}

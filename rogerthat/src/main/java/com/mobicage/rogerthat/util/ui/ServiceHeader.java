package com.mobicage.rogerthat.util.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.plugins.messaging.BrandingMgr;
import com.mobicage.rogerthat.util.TextUtils;

import static com.mobicage.rogerthat.util.ui.ImageHelper.getRoundedCornerAvatar;

public class ServiceHeader {

    public static void setupNative(final BrandingMgr.BrandingResult brandingResult, final LinearLayout headerContainer) {
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
            messageView.setText(brandingResult.message);
        }
        logoImage.setVisibility(View.VISIBLE);
        logoImage.setImageBitmap(logo);
        if (brandingResult.avatar != null) {
            final Bitmap avatar = BitmapFactory.decodeFile(brandingResult.avatar.getAbsolutePath());
            avatarImage.setImageBitmap(getRoundedCornerAvatar(avatar));
            avatarImage.setVisibility(View.VISIBLE);
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
                    int avatarHeight = logoImage.getHeight();
                    int halfAvatarHeight = Math.round(avatarHeight / 2);
                    imagesContainer.getLayoutParams().height = avatarHeight + halfAvatarHeight;
                    ViewGroup.MarginLayoutParams avatarMarginLayoutParams = (ViewGroup.MarginLayoutParams) avatarImage.getLayoutParams();
                    //noinspection SuspiciousNameCombination
                    avatarMarginLayoutParams.width = avatarHeight;
                    avatarMarginLayoutParams.height = avatarHeight;
                    avatarMarginLayoutParams.topMargin = avatarHeight - halfAvatarHeight;
                }
            });
        }
    }
}
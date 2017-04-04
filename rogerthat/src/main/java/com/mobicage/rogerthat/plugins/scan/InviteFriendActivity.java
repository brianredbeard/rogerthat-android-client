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

package com.mobicage.rogerthat.plugins.scan;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.ServiceBoundActivity;
import com.mobicage.rogerthat.plugins.friends.Friend;
import com.mobicage.rogerthat.plugins.friends.FriendsPlugin;
import com.mobicage.rogerthat.plugins.messaging.BrandingFailureException;
import com.mobicage.rogerthat.plugins.messaging.BrandingMgr;
import com.mobicage.rogerthat.plugins.messaging.BrandingMgr.BrandingResult;
import com.mobicage.rogerthat.util.TextUtils;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeBroadcastReceiver;
import com.mobicage.rogerthat.util.system.SafeViewOnClickListener;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.ui.ImageHelper;
import com.mobicage.rpc.config.AppConstants;

import org.jivesoftware.smack.util.Base64;

public class InviteFriendActivity extends ServiceBoundActivity {

    private String mEmailHash;
    private Friend mFriend;
    private SafeBroadcastReceiver mBroadcastReceiver;

    @Override
    protected void onServiceBound() {
        T.UI();
        setContentView(R.layout.friend_invite);
        final int btnText;
        switch (AppConstants.FRIENDS_CAPTION) {
        case COLLEAGUES:
            btnText = R.string.invite_colleague;
            break;
        case CONTACTS:
            btnText = R.string.invite_contact;
            break;
        case FRIENDS:
        default:
            btnText = R.string.invite_friend;
            break;
        }
        ((Button) findViewById(R.id.invite_friend_button)).setText(btnText);

        final IntentFilter filter = new IntentFilter(BrandingMgr.SERVICE_BRANDING_AVAILABLE_INTENT);
        filter.addAction(BrandingMgr.GENERIC_BRANDING_AVAILABLE_INTENT);
        mBroadcastReceiver = new SafeBroadcastReceiver() {
            @Override
            public String[] onSafeReceive(Context context, Intent intent) {
                T.UI();
                if (BrandingMgr.SERVICE_BRANDING_AVAILABLE_INTENT.equals(intent.getAction())
                    || BrandingMgr.GENERIC_BRANDING_AVAILABLE_INTENT.equals(intent.getAction())) {
                    if (mEmailHash != null & mEmailHash.equals(intent.getStringExtra(BrandingMgr.SERVICE_EMAIL))) {
                        showBrandedDescription(mFriend);
                        return new String[] { intent.getAction() };
                    }
                }
                return null;
            }
        };

        registerReceiver(mBroadcastReceiver, filter);

        try {
            final Intent intent = getIntent();
            mEmailHash = intent.getStringExtra(ProcessScanActivity.EMAILHASH);
            mFriend = new Friend();
            mFriend.avatar = Base64.decode(intent.getStringExtra(ProcessScanActivity.AVATAR));
            mFriend.name = intent.getStringExtra(ProcessScanActivity.NAME);
            mFriend.email = intent.getStringExtra(ProcessScanActivity.EMAIL);
            mFriend.type = intent.getLongExtra(ProcessScanActivity.TYPE, FriendsPlugin.FRIEND_TYPE_USER);
            mFriend.description = intent.getStringExtra(ProcessScanActivity.DESCRIPTION);
            mFriend.descriptionBranding = intent.getStringExtra(ProcessScanActivity.DESCRIPTION_BRANDING);
            mFriend.qualifiedIdentifier = intent.getStringExtra(ProcessScanActivity.QUALIFIED_IDENTIFIER);

            if (mFriend.description != null && !TextUtils.isEmptyOrWhitespace(mFriend.descriptionBranding)) {
                FriendsPlugin plugin = mService.getPlugin(FriendsPlugin.class);
                if (plugin.getBrandingMgr().isBrandingAvailable(mFriend.descriptionBranding)) {
                    showBrandedDescription(mFriend);
                } else {
                    Friend copy = new Friend();
                    copy.description = mFriend.description;
                    copy.descriptionBranding = mFriend.descriptionBranding;
                    copy.email = mEmailHash;
                    plugin.getBrandingMgr().queue(copy);
                }
            }

            setTitle(R.string.successful_scan);

            final Bitmap bitmap = ImageHelper.getRoundedCornerAvatar(BitmapFactory.decodeByteArray(mFriend.avatar, 0,
                mFriend.avatar.length));
            ((ImageView) findViewById(R.id.friend_avatar)).setImageBitmap(bitmap);

            ((TextView) findViewById(R.id.friend_name)).setText(mFriend.name);

            if (mFriend.getDisplayEmail() == null) {
                findViewById(R.id.email).setVisibility(View.GONE);
            } else {
                ((TextView) findViewById(R.id.email)).setText(mFriend.getDisplayEmail());
            }

            final Button inviteButton = ((Button) findViewById(R.id.invite_friend_button));
            if (mFriend.type == FriendsPlugin.FRIEND_TYPE_SERVICE) {
                inviteButton.setText(R.string.follow);
            } else {
                final int text;
                switch (AppConstants.FRIENDS_CAPTION) {
                case COLLEAGUES:
                    text = R.string.invite_colleague;
                    break;
                case CONTACTS:
                    text = R.string.invite_contact;
                    break;
                case FRIENDS:
                default:
                    text = R.string.invite_friend;
                    break;
                }
                inviteButton.setText(text);
            }
            inviteButton.setOnClickListener(new SafeViewOnClickListener() {
                @Override
                public void safeOnClick(View v) {
                    final FriendsPlugin plugin = mService.getPlugin(FriendsPlugin.class);
                    plugin.inviteFriend(mEmailHash, null, mFriend.name, true);
                    finish();
                }
            });

            TextView descriptionTextView = (TextView)findViewById(R.id.description);
            if (mFriend.description == null) {
                descriptionTextView.setVisibility(View.GONE);
            } else {
                descriptionTextView.setText(mFriend.description);
            }

        } catch (Exception e) {
            L.bug(e);
            finish();
        }
    }

    private void showBrandedDescription(Friend friend) {
        if (friend.description == null || friend.descriptionBranding == null)
            return;

        BrandingResult br = null;
        try {
            FriendsPlugin plugin = mService.getPlugin(FriendsPlugin.class);
            br = plugin.getBrandingMgr().prepareBranding(friend);
        } catch (BrandingFailureException e) {
            L.bug("Could not display service detail with branding.", e);
            return;
        }

        WebView webView = (WebView) findViewById(R.id.webview);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(false);
        settings.setBlockNetworkImage(false);

        webView.loadUrl("file://" + br.file.getAbsolutePath());
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        webView.setVisibility(View.VISIBLE);

        findViewById(R.id.description).setVisibility(View.GONE);
    }

    @Override
    protected void onServiceUnbound() {
        unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    protected void onDestroy() {
        if (mFriend != null && mFriend.descriptionBranding != null) {
            FriendsPlugin plugin = mService.getPlugin(FriendsPlugin.class);
            plugin.getBrandingMgr().cleanupBranding(mFriend.descriptionBranding);
        }
        super.onDestroy();
    }

}

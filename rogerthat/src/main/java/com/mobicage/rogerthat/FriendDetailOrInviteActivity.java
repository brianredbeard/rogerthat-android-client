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
package com.mobicage.rogerthat;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.plugins.friends.Friend;
import com.mobicage.rogerthat.plugins.friends.FriendsPlugin;
import com.mobicage.rogerthat.plugins.history.HistoryItem;
import com.mobicage.rogerthat.plugins.scan.GetUserInfoResponseHandler;
import com.mobicage.rogerthat.plugins.scan.ProcessScanActivity;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeBroadcastReceiver;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.ui.ImageHelper;
import com.mobicage.rogerthat.util.ui.UIUtils;
import com.mobicage.rpc.config.AppConstants;
import com.mobicage.rpc.config.LookAndFeelConstants;
import com.mobicage.to.friends.GetUserInfoRequestTO;

import java.util.Map;

public class FriendDetailOrInviteActivity extends ServiceBoundActivity {

    public final static String EMAIL = "email";

    private static final String[] ALL_RECEIVING_INTENTS = new String[] { FriendsPlugin.FRIEND_INFO_RECEIVED_INTENT };

    // assigned in onServiceBound
    private String mFriendEmail;
    private Friend mFriend;
    protected FriendsPlugin mFriendsPlugin;
    private BroadcastReceiver mBroadcastReceiver;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        T.UI();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friend_detail_or_invite);
    }

    @Override
    protected void onDestroy() {
        T.UI();
        super.onDestroy();
    }

    private void initBroadcastReceiver(final IntentFilter filter) {
        T.UI();

        mBroadcastReceiver = new SafeBroadcastReceiver() {

            @Override
            public String[] onSafeReceive(final Context context, final Intent intent) {
                T.UI();
                if (FriendsPlugin.FRIEND_INFO_RECEIVED_INTENT.equals(intent.getAction())) {
                    final String emailHash = intent.getStringExtra(ProcessScanActivity.EMAILHASH);
                    if (emailHash != null && emailHash.equals(mFriendEmail)) {
                        abortProcessing();

                        if (intent.getBooleanExtra(ProcessScanActivity.SUCCESS, true)) {
                            mFriend = mFriendsPlugin.getStore().getFriend(mFriendEmail);
                            updateView();
                        } else {
                            UIUtils.showErrorDialog(FriendDetailOrInviteActivity.this, intent);
                        }

                        return ALL_RECEIVING_INTENTS;
                    }
                }
                return null;
            }
        };
        registerReceiver(mBroadcastReceiver, filter);
    }

    private void startSpinner() {
        mProgressDialog = UIUtils.showProgressDialog(this, getString(R.string.loading),
            getString(R.string.retrieving_information), true, true, new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    T.UI();
                    finish();
                }
            });
    }

    private void abortProcessing() {
        T.UI();
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    private void updateView() {
        final ImageView image = (ImageView) findViewById(R.id.friend_avatar);

        if (mFriend == null) {
            image.setVisibility(View.INVISIBLE);
            return;
        }
        image.setVisibility(View.VISIBLE);
        final TextView nameView = (TextView) findViewById(R.id.friend_name);

        if (mFriend.avatar == null) {
            image.setImageBitmap(mFriendsPlugin.getMissingFriendAvatarBitmap());
        } else {
            final Bitmap avatarBitmap = BitmapFactory.decodeByteArray(mFriend.avatar, 0, mFriend.avatar.length);
            image.setImageBitmap(ImageHelper.getRoundedCornerAvatar(avatarBitmap));
        }

        setTitle(mFriend.getDisplayName());
        nameView.setText(mFriend.getDisplayName());
        nameView.setTextColor(ContextCompat.getColor(this, android.R.color.primary_text_light));

        final LinearLayout profileDataContainer = (LinearLayout) findViewById(R.id.profile_data);
        if (AppConstants.PROFILE_DATA_FIELDS.length > 0) {
            profileDataContainer.removeAllViews();
            profileDataContainer.setVisibility(View.VISIBLE);
            Map<String, String> profileData = mFriend.getProfileDataDict();
            for (String k : AppConstants.PROFILE_DATA_FIELDS) {
                final LinearLayout ll = (LinearLayout) View.inflate(this, R.layout.profile_data_detail, null);
                final TextView tvKey = (TextView) ll.findViewById(R.id.profile_data_detail_key);
                final TextView tvVal = (TextView) ll.findViewById(R.id.profile_data_detail_value);

                String v = profileData == null ? null : profileData.get(k);
                if (v == null) {
                    v = getString(R.string.unknown);
                }
                tvKey.setText(k);
                tvKey.setTextColor(LookAndFeelConstants.getPrimaryColor(this));
                tvVal.setText(v);

                profileDataContainer.addView(ll);
            }
        } else {
            profileDataContainer.setVisibility(View.GONE);
        }
    }

    private void requestFriendInfoByEmailHash(String emailHash) {

        final GetUserInfoRequestTO request = new GetUserInfoRequestTO();
        request.code = emailHash;
        request.allow_cross_app = false;

        final GetUserInfoResponseHandler handler = new GetUserInfoResponseHandler();
        handler.setCode(emailHash);
        handler.setStoreInDB(true);

        try {
            com.mobicage.api.friends.Rpc.getUserInfo(handler, request);
        } catch (Exception e) {
            finish();
            mService.putInHistoryLog(getString(R.string.getuserinfo_failure), HistoryItem.ERROR);
        }

    }

    @Override
    protected void onServiceBound() {
        T.UI();
        mFriendsPlugin = mService.getPlugin(FriendsPlugin.class);

        IntentFilter filter = getIntentFilter();
        if (filter != null)
            initBroadcastReceiver(filter);

        mFriendEmail = getIntent().getStringExtra(EMAIL);
        L.d("FriendDetailOrInviteActivity: " + mFriendEmail);

        mFriend = mFriendsPlugin.getStore().getFriend(mFriendEmail);
        if (mFriend == null) {
            startSpinner();
        }
        updateView();
        requestFriendInfoByEmailHash(mFriendEmail);
    }

    protected IntentFilter getIntentFilter() {
        IntentFilter filter = new IntentFilter();
        for (String action : ALL_RECEIVING_INTENTS)
            filter.addAction(action);
        return filter;
    }

    @Override
    protected void onServiceUnbound() {
        T.UI();

        L.d("FriendDetailOrInviteActivity.onServiceUnbound()");
        if (mBroadcastReceiver != null)
            unregisterReceiver(mBroadcastReceiver);
    }
}

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
package com.mobicage.rogerthat;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.plugins.friends.Friend;
import com.mobicage.rogerthat.plugins.friends.FriendsPlugin;
import com.mobicage.rogerthat.plugins.messaging.BrandingFailureException;
import com.mobicage.rogerthat.plugins.messaging.BrandingMgr;
import com.mobicage.rogerthat.plugins.messaging.BrandingMgr.BrandingResult;
import com.mobicage.rogerthat.plugins.messaging.BrandingMgr.ColorScheme;
import com.mobicage.rogerthat.plugins.messaging.MessagingFilterActivity;
import com.mobicage.rogerthat.plugins.messaging.MessagingPlugin;
import com.mobicage.rogerthat.plugins.messaging.mfr.EmptyStaticFlowException;
import com.mobicage.rogerthat.plugins.messaging.mfr.JsMfr;
import com.mobicage.rogerthat.plugins.messaging.mfr.MessageFlowRun;
import com.mobicage.rogerthat.util.RegexPatterns;
import com.mobicage.rogerthat.util.TextUtils;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeBroadcastReceiver;
import com.mobicage.rogerthat.util.system.SafeDialogInterfaceOnClickListener;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.system.SafeViewOnClickListener;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.ui.ImageHelper;
import com.mobicage.rogerthat.util.ui.UIUtils;
import com.mobicage.rpc.config.CloudConstants;
import com.mobicage.to.friends.FriendTO;
import com.mobicage.to.service.StartServiceActionRequestTO;
import com.mobicage.to.system.GetIdentityQRCodeRequestTO;

import org.jivesoftware.smack.util.Base64;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;

public abstract class FriendDetailActivity extends ServiceBoundActivity {

    public final static String EMAIL = "email";

    private static final String[] ALL_RECEIVING_INTENTS = new String[] { FriendsPlugin.FRIEND_UPDATE_INTENT,
        FriendsPlugin.FRIEND_AVATAR_CHANGED_INTENT, FriendsPlugin.FRIEND_REMOVED_INTENT,
        FriendsPlugin.FRIENDS_LIST_REFRESHED, FriendsPlugin.FRIEND_QR_CODE_RECEIVED_INTENT,
        BrandingMgr.SERVICE_BRANDING_AVAILABLE_INTENT, BrandingMgr.GENERIC_BRANDING_AVAILABLE_INTENT };

    private static final String[] UPDATE_VIEW_INTENTS = new String[] { FriendsPlugin.FRIEND_UPDATE_INTENT,
        FriendsPlugin.FRIEND_AVATAR_CHANGED_INTENT, FriendsPlugin.FRIEND_REMOVED_INTENT,
        FriendsPlugin.FRIENDS_LIST_REFRESHED };

    // assigned in onServiceBound
    private Friend mFriend;
    private String mFriendName;
    protected FriendsPlugin mFriendsPlugin;
    private BroadcastReceiver mBroadcastReceiver;

    // assigned as location info flows in
    private AlertDialog mRequestFriendToShareLocationDialog;

    // assigned in onCreate
    private View mTopArea;
    private View mFriendArea;
    private ViewGroup mServiceArea;
    private View mPokeArea;
    private View mHeader;

    protected String mContextMatch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        T.UI();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friend_detail);
        setNavigationBarBurgerVisible(false, true);

        mTopArea = findViewById(R.id.friend_detail_layout);
        mServiceArea = (ViewGroup) findViewById(R.id.service_area);
        mPokeArea = findViewById(R.id.poke_area);
        mFriendArea = findViewById(R.id.friend_area);
        mHeader = findViewById(R.id.friend_detail_header);
    }

    @Override
    protected void onDestroy() {
        T.UI();
        dismissDialogs();
        if (mFriend != null && mFriend.descriptionBranding != null) {
            mFriendsPlugin.getBrandingMgr().cleanupBranding(mFriend.descriptionBranding);
        }
        super.onDestroy();
    }

    private void initBroadcastReceiver(final IntentFilter filter) {

        T.UI();

        mBroadcastReceiver = new SafeBroadcastReceiver() {

            @Override
            public String[] onSafeReceive(final Context context, final Intent intent) {
                T.UI();
                if (mFriend == null) {
                    L.bug("FriendDetailActivity received intent but mFriend == null");
                    return ALL_RECEIVING_INTENTS;
                }

                String action = intent.getAction();
                if (FriendsPlugin.FRIENDS_LIST_REFRESHED.equals(action)) {

                    Friend newFriend = showFriend(intent);
                    if (newFriend == null) {
                        // friend does not exist anymore, not interested in any older intent
                        return ALL_RECEIVING_INTENTS;
                    }
                    mFriend = newFriend;
                    return UPDATE_VIEW_INTENTS;

                } else if (BrandingMgr.SERVICE_BRANDING_AVAILABLE_INTENT.equals(action)
                    || BrandingMgr.GENERIC_BRANDING_AVAILABLE_INTENT.equals(action)) {

                    if (mFriend != null && mFriend.descriptionBranding != null
                        && mFriend.descriptionBranding.equals(intent.getStringExtra(BrandingMgr.BRANDING_KEY))) {

                        showBrandedDescription(mFriend);
                        return new String[] { BrandingMgr.SERVICE_BRANDING_AVAILABLE_INTENT,
                            BrandingMgr.GENERIC_BRANDING_AVAILABLE_INTENT };
                    }

                } else if (mFriend.email.equals(intent.getStringExtra(EMAIL))) {

                    // Process friend intents

                    if (FriendsPlugin.FRIEND_UPDATE_INTENT.equals(action)
                        || FriendsPlugin.FRIEND_AVATAR_CHANGED_INTENT.equals(action)) {

                        Friend newFriend = showFriend(intent);
                        if (newFriend == null) {
                            // friend does not exist anymore, not interested in any older intent
                            return ALL_RECEIVING_INTENTS;
                        }
                        mFriend = newFriend;
                        return UPDATE_VIEW_INTENTS;

                    } else if (FriendsPlugin.FRIEND_REMOVED_INTENT.equals(action)) {

                        L.d("Received intent - FRIEND_REMOVED_INTENT");
                        if (mFriend.existenceStatus != Friend.NOT_FOUND && !isFinishing()) {
                            finish();
                            return ALL_RECEIVING_INTENTS;
                        }

                    } else if (FriendsPlugin.FRIEND_QR_CODE_RECEIVED_INTENT.equals(action)) {

                        byte[] image = Base64.decode(intent.getStringExtra("qrcode"));
                        Bitmap bm = BitmapFactory.decodeByteArray(image, 0, image.length);
                        ImageView view = (ImageView) findViewById(R.id.qrcode);
                        view.setImageBitmap(bm);
                        return new String[] { action };

                    }
                }
                return null;
            }
        };
        registerReceiver(mBroadcastReceiver, filter);
    }

    @Override
    protected void onServiceBound() {
        T.UI();

        mFriendsPlugin = mService.getPlugin(FriendsPlugin.class);

        mFriend = showFriend(getIntent());
        if (mFriend == null) {
            if (!isFinishing()) {
                L.d("Finishing FriendDetailActivity - friend not found");
                finish();
            }
            return;
        }

        setTitle(mFriend.getDisplayName());

        if (getPassportVisibility() != View.GONE) {
            GetFriendIdentityQRCodeResponseHandler rh = new GetFriendIdentityQRCodeResponseHandler();
            rh.setFriendEmail(mFriend.email);
            GetIdentityQRCodeRequestTO request = new GetIdentityQRCodeRequestTO();
            request.email = mFriend.email;

            try {
                com.mobicage.api.system.Rpc.getIdentityQRCode(rh, request);
            } catch (Exception e) {
                L.bug(e);
            }
        }

        ImageView location = (ImageView) findViewById(R.id.location);
        location.setOnClickListener(new SafeViewOnClickListener() {
            @Override
            public void safeOnClick(View v) {
                T.UI();
                if (mFriend.sharesLocation) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(FriendDetailActivity.this);
                    builder.setTitle(R.string.friend_location_requested_title);
                    builder.setMessage(getString(R.string.friend_location_requested_body, mFriend.name));
                    builder.setPositiveButton(R.string.rogerthat, new SafeDialogInterfaceOnClickListener() {
                        @Override
                        public void safeOnClick(DialogInterface dialog, int which) {
                            HomeActivity.startWithLaunchInfo(FriendDetailActivity.this,
                                HomeActivity.INTENT_VALUE_SHOW_MESSAGES);
                        }
                    });
                    builder.create().show();

                    mFriendsPlugin.scheduleSingleFriendLocationRetrieval(mFriend.email);
                } else {

                    SafeDialogInterfaceOnClickListener onPositiveClickListener = new SafeDialogInterfaceOnClickListener() {
                        @Override
                        public void safeOnClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            mFriendsPlugin.requestFriendShareLocation(mFriend.email, null);
                            UIUtils.showLongToast(FriendDetailActivity.this,
                                getString(R.string.friend_request_share_location_invitation_sent, mFriendName));
                        }
                    };

                    SafeDialogInterfaceOnClickListener onNegativeClickListener = new SafeDialogInterfaceOnClickListener() {
                        @Override
                        public void safeOnClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    };

                    mRequestFriendToShareLocationDialog = new AlertDialog.Builder(FriendDetailActivity.this)
                        .setMessage(getString(R.string.dialog_request_location_sharing, mFriendName))
                        .setCancelable(true).setPositiveButton(R.string.yes, onPositiveClickListener)
                        .setNegativeButton(R.string.no, onNegativeClickListener).create();
                    mRequestFriendToShareLocationDialog.setCanceledOnTouchOutside(true);
                    mRequestFriendToShareLocationDialog.show();
                }
            }
        });

        ImageView newMessage = (ImageView) findViewById(R.id.send);
        newMessage.setOnClickListener(new SafeViewOnClickListener() {
            @Override
            public void safeOnClick(View v) {
                final Intent sendMessage = new Intent(FriendDetailActivity.this, SendMessageMessageActivity.class);
                sendMessage.putExtra(SendMessageMessageActivity.RECIPIENTS, new String[] { mFriend.email });
                startActivity(sendMessage);
            }
        });

        ImageView history = (ImageView) findViewById(R.id.history);
        history.setOnClickListener(new SafeViewOnClickListener() {
            @Override
            public void safeOnClick(View v) {
                final Intent viewMessages = new Intent(FriendDetailActivity.this, MessagingFilterActivity.class);
                viewMessages.putExtra(MessagingPlugin.MEMBER_FILTER, mFriend.email);
                startActivity(viewMessages);
            }
        });

        CheckBox iShareLocationCheckBox = (CheckBox) findViewById(R.id.share_location);
        // Increase space between checkbox and text
        iShareLocationCheckBox.setPadding(
                iShareLocationCheckBox.getPaddingLeft() + UIUtils.convertDipToPixels(this, 10),
                iShareLocationCheckBox.getPaddingTop(), iShareLocationCheckBox.getPaddingRight(),
                iShareLocationCheckBox.getPaddingBottom());

        IntentFilter filter = getIntentFilter();
        if (filter != null)
            initBroadcastReceiver(filter);
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

        L.d("FriendDetailActivity.onServiceUnbound()");
        if (mBroadcastReceiver != null)
            unregisterReceiver(mBroadcastReceiver);
    }

    protected abstract int getMenu();

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        T.UI();

        super.onCreateOptionsMenu(menu);
        int menuResource = getMenu();
        if (menuResource != -1)
            getMenuInflater().inflate(menuResource, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (mFriend != null) {
            MenuItem removeFriend = menu.findItem(R.id.remove_friend);
            if (removeFriend != null) {
                Matcher matcher = RegexPatterns.IS_DASHBOARD_ACCOUNT.matcher(mFriend.email);
                removeFriend.setEnabled(!matcher.matches());
            }
        }
        return true;
    }

    protected abstract int getUnfriendMessage();

    protected abstract int getRemoveFailedMessage();

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        T.UI();

        switch (item.getItemId()) {
        case R.id.remove_friend:
            if (mFriend == null)
                return true;

            new AlertDialog.Builder(this).setMessage(getString(getUnfriendMessage(), mFriend.getDisplayName()))
                .setPositiveButton(R.string.yes, new SafeDialogInterfaceOnClickListener() {
                    @Override
                    public void safeOnClick(DialogInterface dialog, int which) {
                        T.UI();
                        L.d("clicked menu option to remove friend");
                        if (!mFriendsPlugin.scheduleFriendRemoval(mFriend.email)) {
                            UIUtils.showLongToast(FriendDetailActivity.this, getString(getRemoveFailedMessage()));
                            L.d("FriendDetailActivity - removeFriend failed");
                        } else {
                            L.d("FriendDetailActivity - removeFriend succeeded");
                            mService.postOnUIHandler(new SafeRunnable() {
                                @Override
                                protected void safeRun() throws Exception {
                                    T.UI();
                                    FriendDetailActivity.this.finish();
                                }
                            });
                        }
                    }
                }).setNegativeButton(R.string.no, new SafeDialogInterfaceOnClickListener() {
                    @Override
                    public void safeOnClick(DialogInterface dialog, int which) {
                        T.UI();
                        L.d("FriendDetailActivity - removefriend - Clicked NO - do not remove friend");
                        dialog.dismiss();
                    }
                }).create().show();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected abstract int getHeaderVisibility();

    protected abstract int getFriendAreaVisibility();

    protected abstract int getServiceAreaVisibility();

    protected abstract int getPokeVisibility();

    protected abstract int getPassportVisibility();

    protected String getPokeAction() {
        return null;
    }

    protected String getStaticFlow() {
        return null;
    }

    protected String getStaticFlowHash() {
        return null;
    }

    protected void poke() {
        showTransmitting(new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                finish();
                if (CloudConstants.isYSAAA()) {
                    HomeActivity
                        .startWithLaunchInfo(FriendDetailActivity.this, HomeActivity.INTENT_VALUE_SHOW_MESSAGES);
                }
            }
        });

        mContextMatch = "QRSCAN_" + UUID.randomUUID().toString();

        final String staticFlow = getStaticFlow();
        final String staticFlowHash = getStaticFlowHash();

        if (staticFlow != null && staticFlowHash != null) {
            mFriendsPlugin.getStore().storeStaticFlow(staticFlow, staticFlowHash);

            MessageFlowRun mfr = new MessageFlowRun();
            mfr.staticFlowHash = staticFlowHash;

            StartServiceActionRequestTO request = new StartServiceActionRequestTO();
            request.email = mFriend.email;
            request.action = getPokeAction();
            request.context = mContextMatch;
            request.static_flow_hash = mfr.staticFlowHash;
            request.timestamp = System.currentTimeMillis() / 1000;

            Map<String, Object> userInput = new HashMap<String, Object>();
            userInput.put("request", request.toJSONMap());
            userInput.put("func", "com.mobicage.api.services.startAction");
            try {
                JsMfr.executeMfr(mfr, userInput, mService, false);
            } catch (EmptyStaticFlowException ex) {
                L.bug(ex);
            }
        } else {
            boolean success = mFriendsPlugin.startAction(mFriend.email, getPokeAction(), mContextMatch);
            if (!success) {
                completeTransmit(new SafeRunnable() {
                    @Override
                    protected void safeRun() throws Exception {
                        new AlertDialog.Builder(FriendDetailActivity.this)
                            .setMessage(R.string.scanner_communication_failure)
                            .setPositiveButton(R.string.rogerthat, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    finish();
                                }
                            }).create().show();
                    }
                });
            }
        }
    }

    protected Friend loadFriend(Intent intent) {
        T.UI();
        String email;
        if (FriendsPlugin.FRIENDS_LIST_REFRESHED.equals(intent.getAction())) {
            email = mFriend.email;
        } else {
            email = intent.getStringExtra(EMAIL);
        }
        final Friend friend = mFriendsPlugin.getStore().getExistingFriend(email);
        if (friend == null) {
            if (FriendsPlugin.FRIENDS_LIST_REFRESHED.equals(intent.getAction())) {
                L.d("Friend does not exist anymore after FRIENDS_LIST_REFRESHED intent");
                if (!isFinishing()) {
                    finish();
                }
            } else {
                L.bug("FriendDetailActivity - cannot load friend " + email + " after receiving intent with action "
                    + intent.getAction());
            }
        } else {
            friend.pokeDescription = null;
        }
        return friend;
    }

    private void showBrandedDescription(FriendTO friend) {
        if (friend.description == null || TextUtils.isEmptyOrWhitespace(friend.descriptionBranding))
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

        if (br.color == null) {
            mTopArea.setBackgroundResource(R.color.mc_background_color);
        } else {
            mTopArea.setBackgroundColor(br.color);
        }

        int r = br.scheme == ColorScheme.LIGHT ? android.R.color.primary_text_light : android.R.color.primary_text_dark;
        ((TextView) findViewById(R.id.friend_name)).setTextColor(ContextCompat.getColor(this, r));
        ((TextView) findViewById(R.id.email)).setTextColor(ContextCompat.getColor(this, r));

        findViewById(R.id.description).setVisibility(View.GONE);
    }

    private Friend showFriend(Intent intent) {
        T.UI();

        final Friend friend = loadFriend(intent);
        if (friend == null) {
            return null;
        }

        mServiceArea.setVisibility(getServiceAreaVisibility());
        mPokeArea.setVisibility(getPokeVisibility());
        mFriendArea.setVisibility(getFriendAreaVisibility());
        mHeader.setVisibility(getHeaderVisibility());

        final ImageView image = (ImageView) findViewById(R.id.friend_avatar);
        if (friend.avatar == null) {
            image.setImageBitmap(mFriendsPlugin.getMissingFriendAvatarBitmap());
        } else {
            final Bitmap avatarBitmap = BitmapFactory.decodeByteArray(friend.avatar, 0, friend.avatar.length);
            image.setImageBitmap(ImageHelper.getRoundedCornerAvatar(avatarBitmap));
        }

        final TextView nameView = (TextView) findViewById(R.id.friend_name);
        mFriendName = friend.getDisplayName();
        nameView.setText(mFriendName);
        nameView.setTextColor(ContextCompat.getColor(this, android.R.color.primary_text_light));

        final TextView emailView = (TextView) findViewById(R.id.email);
        emailView.setText(friend.getDisplayEmail());
        emailView.setTextColor(nameView.getTextColors());

        if (friend.existenceStatus == Friend.NOT_FOUND || friend.existenceStatus == Friend.INVITE_PENDING) {
            final Button pokeBtn = (Button) findViewById(R.id.poke_button);
            pokeBtn.setText(R.string.connect_service_now);
            pokeBtn.setOnClickListener(new SafeViewOnClickListener() {
                @Override
                public void safeOnClick(View v) {
                    if (mFriendsPlugin.inviteService(mFriend)) {
                        finish();
                    }
                }
            });
        } else if (TextUtils.isEmptyOrWhitespace(friend.pokeDescription)) {
            findViewById(R.id.poke_area).setVisibility(View.GONE);
        } else {
            findViewById(R.id.poke_area).setVisibility(View.VISIBLE);
            final Button pokeBtn = (Button) findViewById(R.id.poke_button);
            pokeBtn.setText(friend.pokeDescription);
            pokeBtn.setOnClickListener(new SafeViewOnClickListener() {
                @Override
                public void safeOnClick(View v) {
                    T.UI();
                    poke();
                }
            });
        }

        final TextView descriptionView = (TextView) mServiceArea.findViewById(R.id.description);
        descriptionView.setText(friend.description);

        if (friend.descriptionBranding != null) {
            boolean available = false;
            try {
                available = mFriendsPlugin.getBrandingMgr().isBrandingAvailable(friend.descriptionBranding);
            } catch (BrandingFailureException e) {
                // ignore
            }
            if (available) {
                showBrandedDescription(friend);
            } else {
                mFriendsPlugin.getBrandingMgr().queue(friend);
            }
        } else {
            descriptionView.setVisibility(View.VISIBLE);
            mServiceArea.findViewById(R.id.webview).setVisibility(View.GONE);
            mTopArea.setBackgroundResource(R.color.mc_background_color);
        }

        final CheckBox iShareLocationCheckBox = (CheckBox) findViewById(R.id.share_location);
        iShareLocationCheckBox.setOnCheckedChangeListener(null);
        iShareLocationCheckBox.setChecked(friend.shareLocation);
        iShareLocationCheckBox.setText(getString(R.string.friend_share_location));

        iShareLocationCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                T.UI();
                mFriendsPlugin.updateFriendShareLocation(friend.email, isChecked);
            }
        });

        dismissDialogs();

        return friend;
    }

    private void dismissDialogs() {
        if (mRequestFriendToShareLocationDialog != null && mRequestFriendToShareLocationDialog.isShowing())
            mRequestFriendToShareLocationDialog.dismiss();
    }

}

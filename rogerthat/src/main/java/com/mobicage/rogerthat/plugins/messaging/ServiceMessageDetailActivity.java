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
package com.mobicage.rogerthat.plugins.messaging;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;
import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.IdentityStore;
import com.mobicage.rogerthat.MainActivity;
import com.mobicage.rogerthat.ServiceBoundActivity;
import com.mobicage.rogerthat.plugins.friends.Friend;
import com.mobicage.rogerthat.plugins.friends.FriendsPlugin;
import com.mobicage.rogerthat.plugins.friends.MenuItemPresser;
import com.mobicage.rogerthat.plugins.friends.MenuItemPressingActivity;
import com.mobicage.rogerthat.plugins.friends.ServiceActionMenuActivity;
import com.mobicage.rogerthat.plugins.friends.ServiceMenuItemDetails;
import com.mobicage.rogerthat.plugins.messaging.BrandingMgr.BrandingResult;
import com.mobicage.rogerthat.plugins.messaging.BrandingMgr.ColorScheme;
import com.mobicage.rogerthat.plugins.messaging.widgets.Widget;
import com.mobicage.rogerthat.util.IOUtils;
import com.mobicage.rogerthat.util.TextUtils;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeBroadcastReceiver;
import com.mobicage.rogerthat.util.system.SafeDialogInterfaceOnClickListener;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.system.SafeViewOnClickListener;
import com.mobicage.rogerthat.util.system.SystemUtils;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.time.TimeUtils;
import com.mobicage.rogerthat.util.ui.UIUtils;
import com.mobicage.rpc.IJSONable;
import com.mobicage.rpc.config.AppConstants;
import com.mobicage.to.messaging.AttachmentTO;
import com.mobicage.to.messaging.ButtonTO;
import com.mobicage.to.messaging.MemberStatusTO;

import org.json.JSONObject;
import org.json.simple.JSONValue;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.Map;
import java.util.SortedSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;

import thirdparty.nishantnair.FlowLayout;

public class ServiceMessageDetailActivity extends ServiceBoundActivity implements MenuItemPressingActivity {

    private final static String HINT_BROADCAST = "com.mobicage.rogerthat.plugins.messaging.ServiceMessageDetailActivity.HINT_BROADCAST";

    public final static String BUTTON_INFO = "buttonInfo";

    public final static String STARTED_FROM_SERVICE_MENU = "STARTED_FROM_SERVICE_MENU";
    public static final String JUMP_TO_SERVICE_HOME_SCREEN = "JUMP_TO_SERVICE_HOME_SCREEN";
    public static final String TITLE = "TITLE";

    private final static int[] DETAIL_SECTIONS = new int[] { R.id.previous_messages_in_thread_title,
        R.id.previous_messages_in_thread, R.id.message_section_title, R.id.member_details_title, R.id.members,
        R.id.next_messages_in_thread_title, R.id.next_messages_in_thread };

    /**
     * Intent action which trigger an updateView(true)
     */
    private static final String[] UPDATE_VIEW_INTENT_ACTIONS = new String[] {
        MessagingPlugin.MESSAGE_MEMBER_STATUS_UPDATE_RECEIVED_INTENT, MessagingPlugin.MESSAGE_LOCKED_INTENT,
        MessagingPlugin.MESSAGE_PROCESSED_INTENT, FriendsPlugin.FRIEND_UPDATE_INTENT,
        IdentityStore.IDENTITY_CHANGED_INTENT, FriendsPlugin.FRIENDS_LIST_REFRESHED,
        FriendsPlugin.FRIEND_AVATAR_CHANGED_INTENT };

    public static final int PERMISSION_REQUEST_UI_FLAG_LOCATION = 1;
    public static final int PERMISSION_REQUEST_GPS_LCOATION_WIDGET = 2;
    public static final int PERMISSION_REQUEST_PHOTO_UPLOAD_WIDGET = 3;


    // UI Thread owns these
    private FriendsPlugin mFriendsPlugin;
    private MessagingPlugin mMessagingPlugin;
    private MessageStore mStore;
    private MenuItemPresser mMenuItemPresser;
    private Message mCurrentMessage;
    private int mDisplayWidth;
    private BroadcastReceiver mBroadcastReceiver;
    private boolean mMustQuit = false;
    private ProgressDialog mDialog = null;
    private Timer mExpectNextTimer = null;
    private boolean mSomebodyAnswered = false;
    private boolean mTransfering = false;

    private ImageView mStatusImage;

    public void setTransfering(boolean b) {
        mTransfering = b;
    }

    public void transferComplete() {
        T.UI();
        L.d("Transfer completed");
        mTransfering = false;
        dismissTransferingDialog();
        MemberStatusTO myMemberStatus = mCurrentMessage.getMemberStatus(mService.getIdentityStore().getIdentity()
            .getEmail());
        if ((myMemberStatus.status & MessagingPlugin.STATUS_ACKED) == MessagingPlugin.STATUS_ACKED) {
            ButtonTO button = mCurrentMessage.getButton(myMemberStatus.button_id);
            animateAfterAck(getExpectNext(button));
            jumpToServiceHomeScreen(button, null);
            updateView(false);
        }
    }

    public void showTransferingDialog() {
        T.UI();
        mDialog = new ProgressDialog(this);
        mDialog.setMessage(getString(R.string.transmitting));
        mDialog.setCancelable(true);
        mDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.send_to_background),
            new DialogInterface.OnClickListener() { // TODO: SafeRunnable
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    T.UI();
                    mTransfering = false;
                    dismissTransferingDialog();
                    jumpToServiceHomeScreen(null, null);
                    updateView(false);
                }
            });
        mDialog.show();
    }

    public void dismissTransferingDialog() {
        T.UI();
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                int scrcoords[] = new int[2];
                v.getLocationOnScreen(scrcoords);
                float x = event.getRawX() + v.getLeft() - scrcoords[0];
                float y = event.getRawY() + v.getTop() - scrcoords[1];

                if (x < v.getLeft() || x >= v.getRight() || y < v.getTop() || y > v.getBottom()) {
                    // Tapped outside the editText
                    UIUtils.hideKeyboard(this, v);
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    protected void onServiceBound() {
        T.UI();
        mFriendsPlugin = mService.getPlugin(FriendsPlugin.class);
        mMessagingPlugin = mService.getPlugin(MessagingPlugin.class);
        mStore = mMessagingPlugin.getStore();
        mDisplayWidth = UIUtils.getDisplayWidth(this);

        final View activityView = LayoutInflater.from(this).inflate(R.layout.message_detail, null);

        ((ImageButton) activityView.findViewById(R.id.expand)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expandDetails();
            }
        });

        mStatusImage = (ImageView) activityView.findViewById(R.id.status_image);

        setContentViewWithoutNavigationBar(activityView);

        final Intent intent = getIntent();
        String messageKey = intent.getStringExtra("message");
        String title = intent.getStringExtra(TITLE);
        if (title != null) {
            setTitle(title);
        }
        mCurrentMessage = mStore.getFullMessageByKey(messageKey);
        invalidateOptionsMenu();

        if (intent.hasExtra("submitToJSMFR")) {
            messageSubmitToJsMfr(intent, intent.getAction());
        }

        updateView(false);

        if (MainActivity.ACTION_NOTIFICATION_PHOTO_UPLOAD_DONE.equals(intent.getAction()))
            animateAfterAck(2);
        mBroadcastReceiver = getBroadcastReceiver();
        final IntentFilter filter = getIntentFilter();
        registerReceiver(mBroadcastReceiver, filter);

        ((TextView) findViewById(R.id.show_magic_message_header)).setText(getString(R.string.show_message_header,
            getString(R.string.app_name)));
    }

    @Override
    protected void onServiceUnbound() {
        T.UI();
        if (mBroadcastReceiver != null)
            unregisterReceiver(mBroadcastReceiver);

        if (mMenuItemPresser != null) {
            mMenuItemPresser.stop();
            mMenuItemPresser = null;
        }

        if (mCurrentMessage.form != null) {
            LinearLayout widgetLayout = (LinearLayout) findViewById(R.id.widget_layout);
            Widget widget = (Widget) widgetLayout.getChildAt(0);
            widget.onServiceUnbound();
        }
    }

    @Override
    protected void onDestroy() {
        if (mExpectNextTimer != null)
            mExpectNextTimer.cancel();
        super.onDestroy();
    }

    protected void expandDetails() {
        // Memberdetails
        for (int id : new int[] { R.id.member_details_title, R.id.members }) {
            findViewById(id).setVisibility(View.VISIBLE);
        }

        findViewById(R.id.member_summary).setVisibility(View.GONE);

        final ScrollView msgScrollView = (ScrollView) findViewById(R.id.message_scroll_view);
        msgScrollView.post(new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                msgScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    protected void collapseDetails(int[] sections) {
        for (int id : sections) {
            findViewById(id).setVisibility(View.GONE);
        }
    }

    protected void updateMessageDetail(final boolean isUpdate) {
        T.UI();
        // Set sender avatar
        ImageView avatarImage = (ImageView) findViewById(R.id.avatar);
        String sender = mCurrentMessage.sender;
        setAvatar(avatarImage, sender);
        // Set sender name
        TextView senderView = (TextView) findViewById(R.id.sender);
        final String senderName = mFriendsPlugin.getName(sender);
        senderView.setText(senderName == null ? sender : senderName);
        // Set timestamp
        TextView timestampView = (TextView) findViewById(R.id.timestamp);
        timestampView.setText(TimeUtils.getDayTimeStr(this, mCurrentMessage.timestamp * 1000));

        // Set clickable region on top to go to friends detail
        final RelativeLayout messageHeader = (RelativeLayout) findViewById(R.id.message_header);
        messageHeader.setOnClickListener(getFriendDetailOnClickListener(mCurrentMessage.sender));
        messageHeader.setVisibility(View.VISIBLE);

        // Set message
        TextView messageView = (TextView) findViewById(R.id.message);
        WebView web = (WebView) findViewById(R.id.webview);
        FrameLayout flay = (FrameLayout) findViewById(R.id.message_details);
        Resources resources = getResources();
        flay.setBackgroundColor(resources.getColor(R.color.mc_background_color));
        boolean showBranded = false;

        int darkSchemeTextColor = resources.getColor(android.R.color.primary_text_dark);
        int lightSchemeTextColor = resources.getColor(android.R.color.primary_text_light);

        senderView.setTextColor(lightSchemeTextColor);
        timestampView.setTextColor(lightSchemeTextColor);

        BrandingResult br = null;
        if (!TextUtils.isEmptyOrWhitespace(mCurrentMessage.branding)) {
            boolean brandingAvailable = false;
            try {
                brandingAvailable = mMessagingPlugin.getBrandingMgr().isBrandingAvailable(mCurrentMessage.branding);
            } catch (BrandingFailureException e1) {
                L.d(e1);
            }
            try {
                if (brandingAvailable) {
                    br = mMessagingPlugin.getBrandingMgr().prepareBranding(mCurrentMessage);
                    WebSettings settings = web.getSettings();
                    settings.setJavaScriptEnabled(false);
                    settings.setBlockNetworkImage(false);
                    web.loadUrl("file://" + br.file.getAbsolutePath());
                    web.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
                    if (br.color != null) {
                        flay.setBackgroundColor(br.color);
                    }
                    if (!br.showHeader) {
                        messageHeader.setVisibility(View.GONE);
                        MarginLayoutParams mlp = (MarginLayoutParams) web.getLayoutParams();
                        mlp.setMargins(0, 0, 0, mlp.bottomMargin);
                    } else if (br.scheme == ColorScheme.DARK) {
                        senderView.setTextColor(darkSchemeTextColor);
                        timestampView.setTextColor(darkSchemeTextColor);
                    }

                    showBranded = true;
                } else {
                    mMessagingPlugin.getBrandingMgr().queueGenericBranding(mCurrentMessage.branding);
                }
            } catch (BrandingFailureException e) {
                L.bug("Could not display message with branding: branding is available, but prepareBranding failed", e);
            }
        }

        if (showBranded) {
            web.setVisibility(View.VISIBLE);
            messageView.setVisibility(View.GONE);
        } else {
            web.setVisibility(View.GONE);
            messageView.setVisibility(View.VISIBLE);
            messageView.setText(mCurrentMessage.message);
        }

        // Add list of members who did not ack yet
        FlowLayout memberSummary = (FlowLayout) findViewById(R.id.member_summary);
        memberSummary.removeAllViews();
        SortedSet<MemberStatusTO> memberSummarySet = new TreeSet<MemberStatusTO>(getMemberstatusComparator());
        for (MemberStatusTO ms : mCurrentMessage.members) {
            if ((ms.status & MessagingPlugin.STATUS_ACKED) != MessagingPlugin.STATUS_ACKED
                && !ms.member.equals(mCurrentMessage.sender)) {
                memberSummarySet.add(ms);
            }
        }
        FlowLayout.LayoutParams flowLP = new FlowLayout.LayoutParams(2, 0);
        for (MemberStatusTO ms : memberSummarySet) {
            FrameLayout fl = new FrameLayout(this);
            fl.setLayoutParams(flowLP);
            memberSummary.addView(fl);
            fl.addView(createParticipantView(ms));
        }
        memberSummary.setVisibility(memberSummarySet.size() < 2 ? View.GONE : View.VISIBLE);

        // Add members statuses
        final LinearLayout members = (LinearLayout) findViewById(R.id.members);
        members.removeAllViews();
        final String myEmail = mService.getIdentityStore().getIdentity().getEmail();
        boolean isMember = false;
        mSomebodyAnswered = false;
        for (MemberStatusTO ms : mCurrentMessage.members) {
            boolean showMember = true;
            View view = getLayoutInflater().inflate(R.layout.message_member_detail, null);
            // Set receiver avatar
            RelativeLayout rl = (RelativeLayout) view.findViewById(R.id.avatar);
            rl.addView(createParticipantView(ms));
            // Set receiver name
            TextView receiverView = (TextView) view.findViewById(R.id.receiver);
            final String memberName = mFriendsPlugin.getName(ms.member);
            receiverView.setText(memberName == null ? sender : memberName);
            // Set received timestamp
            TextView receivedView = (TextView) view.findViewById(R.id.received_timestamp);
            if ((ms.status & MessagingPlugin.STATUS_RECEIVED) == MessagingPlugin.STATUS_RECEIVED) {
                final String humanTime = TimeUtils.getDayTimeStr(this, ms.received_timestamp * 1000);
                if (ms.member.equals(mCurrentMessage.sender))
                    receivedView.setText(getString(R.string.sent_at, humanTime));
                else
                    receivedView.setText(getString(R.string.received_at, humanTime));
            } else {
                receivedView.setText(R.string.not_yet_received);
            }
            // Set replied timestamp
            TextView repliedView = (TextView) view.findViewById(R.id.acked_timestamp);
            if ((ms.status & MessagingPlugin.STATUS_ACKED) == MessagingPlugin.STATUS_ACKED) {
                mSomebodyAnswered = true;
                String acked_timestamp = TimeUtils.getDayTimeStr(this, ms.acked_timestamp * 1000);
                if (ms.button_id != null) {

                    ButtonTO button = null;
                    for (ButtonTO b : mCurrentMessage.buttons) {
                        if (b.id.equals(ms.button_id)) {
                            button = b;
                            break;
                        }
                    }
                    if (button == null) {
                        repliedView.setText(getString(R.string.dismissed_at, acked_timestamp));
                        // Do not show sender as member if he hasn't clicked a
                        // button
                        showMember = !ms.member.equals(mCurrentMessage.sender);
                    } else {
                        repliedView.setText(getString(R.string.replied_at, button.caption, acked_timestamp));
                    }
                } else {
                    if (ms.custom_reply == null) {
                        // Do not show sender as member if he hasn't clicked a
                        // button
                        showMember = !ms.member.equals(mCurrentMessage.sender);
                        repliedView.setText(getString(R.string.dismissed_at, acked_timestamp));
                    } else
                        repliedView.setText(getString(R.string.replied_at, ms.custom_reply, acked_timestamp));
                }
            } else {
                repliedView.setText(R.string.not_yet_replied);
                showMember = !ms.member.equals(mCurrentMessage.sender);
            }
            if (br != null && br.scheme == ColorScheme.DARK) {
                receiverView.setTextColor(darkSchemeTextColor);
                receivedView.setTextColor(darkSchemeTextColor);
                repliedView.setTextColor(darkSchemeTextColor);
            } else {
                receiverView.setTextColor(lightSchemeTextColor);
                receivedView.setTextColor(lightSchemeTextColor);
                repliedView.setTextColor(lightSchemeTextColor);
            }

            if (showMember)
                members.addView(view);
            isMember |= ms.member.equals(myEmail);
        }

        boolean isLocked = (mCurrentMessage.flags & MessagingPlugin.FLAG_LOCKED) == MessagingPlugin.FLAG_LOCKED;
        boolean canEdit = isMember && !isLocked;

        // Add attachments
        LinearLayout attachmentLayout = (LinearLayout) findViewById(R.id.attachment_layout);
        attachmentLayout.removeAllViews();
        if (mCurrentMessage.attachments.length > 0) {
            attachmentLayout.setVisibility(View.VISIBLE);

            for (final AttachmentTO attachment : mCurrentMessage.attachments) {
                View v = getLayoutInflater().inflate(R.layout.attachment_item, null);

                ImageView attachment_image = (ImageView) v.findViewById(R.id.attachment_image);
                if (AttachmentViewerActivity.CONTENT_TYPE_JPEG.equalsIgnoreCase(attachment.content_type)
                    || AttachmentViewerActivity.CONTENT_TYPE_PNG.equalsIgnoreCase(attachment.content_type)) {
                    attachment_image.setImageResource(R.drawable.attachment_img);
                } else if (AttachmentViewerActivity.CONTENT_TYPE_PDF.equalsIgnoreCase(attachment.content_type)) {
                    attachment_image.setImageResource(R.drawable.attachment_pdf);
                } else if (AttachmentViewerActivity.CONTENT_TYPE_VIDEO_MP4.equalsIgnoreCase(attachment.content_type)) {
                    attachment_image.setImageResource(R.drawable.attachment_video);
                } else {
                    attachment_image.setImageResource(R.drawable.attachment_unknown);
                    L.d("attachment.content_type not known: " + attachment.content_type);
                }

                TextView attachment_text = (TextView) v.findViewById(R.id.attachment_text);
                attachment_text.setText(attachment.name);

                v.setOnClickListener(new SafeViewOnClickListener() {

                    @Override
                    public void safeOnClick(View v) {
                        String downloadUrlHash = mMessagingPlugin.attachmentDownloadUrlHash(attachment.download_url);

                        File attachmentsDir;
                        try {
                            attachmentsDir = mMessagingPlugin.attachmentsDir(mCurrentMessage.getThreadKey(), null);
                        } catch (IOException e) {
                            L.d("Unable to create attachment directory", e);
                            UIUtils.showAlertDialog(ServiceMessageDetailActivity.this, "",
                                R.string.unable_to_read_write_sd_card);
                            return;
                        }

                        boolean attachmentAvailable = mMessagingPlugin
                            .attachmentExists(attachmentsDir, downloadUrlHash);

                        if (!attachmentAvailable) {
                            try {
                                attachmentsDir = mMessagingPlugin.attachmentsDir(mCurrentMessage.getThreadKey(),
                                    mCurrentMessage.key);
                            } catch (IOException e) {
                                L.d("Unable to create attachment directory", e);
                                UIUtils.showAlertDialog(ServiceMessageDetailActivity.this, "",
                                    R.string.unable_to_read_write_sd_card);
                                return;
                            }

                            attachmentAvailable = mMessagingPlugin.attachmentExists(attachmentsDir, downloadUrlHash);
                        }

                        if (!mService.getNetworkConnectivityManager().isConnected() && !attachmentAvailable) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(ServiceMessageDetailActivity.this);
                            builder.setMessage(R.string.no_internet_connection_try_again);
                            builder.setPositiveButton(R.string.rogerthat, null);
                            AlertDialog dialog = builder.create();
                            dialog.show();
                            return;
                        }
                        if (IOUtils.shouldCheckExternalStorageAvailable()) {
                            String state = Environment.getExternalStorageState();
                            if (Environment.MEDIA_MOUNTED.equals(state)) {
                                // Its all oke
                            } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
                                if (!attachmentAvailable) {
                                    L.d("Unable to write to sd-card");
                                    UIUtils.showAlertDialog(ServiceMessageDetailActivity.this, "",
                                        R.string.unable_to_read_write_sd_card);
                                    return;
                                }
                            } else {
                                L.d("Unable to read or write to sd-card");
                                UIUtils.showAlertDialog(ServiceMessageDetailActivity.this, "",
                                    R.string.unable_to_read_write_sd_card);
                                return;
                            }
                        }

                        L.d("attachment.content_type: " + attachment.content_type);
                        L.d("attachment.download_url: " + attachment.download_url);
                        L.d("attachment.name: " + attachment.name);
                        L.d("attachment.size: " + attachment.size);

                        if (AttachmentViewerActivity.supportsContentType(attachment.content_type)) {
                            Intent i = new Intent(ServiceMessageDetailActivity.this, AttachmentViewerActivity.class);
                            i.putExtra("thread_key", mCurrentMessage.getThreadKey());
                            i.putExtra("message", mCurrentMessage.key);
                            i.putExtra("content_type", attachment.content_type);
                            i.putExtra("download_url", attachment.download_url);
                            i.putExtra("name", attachment.name);
                            i.putExtra("download_url_hash", downloadUrlHash);

                            startActivity(i);
                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(ServiceMessageDetailActivity.this);
                            builder.setMessage(getString(R.string.attachment_can_not_be_displayed_in_your_version,
                                getString(R.string.app_name)));
                            builder.setPositiveButton(R.string.rogerthat, null);
                            AlertDialog dialog = builder.create();
                            dialog.show();
                        }
                    }

                });

                attachmentLayout.addView(v);
            }

        } else {
            attachmentLayout.setVisibility(View.GONE);
        }

        LinearLayout widgetLayout = (LinearLayout) findViewById(R.id.widget_layout);
        if (mCurrentMessage.form == null) {
            widgetLayout.setVisibility(View.GONE);
        } else {
            widgetLayout.setVisibility(View.VISIBLE);
            widgetLayout.setEnabled(canEdit);
            displayWidget(widgetLayout, br);
        }

        // Add buttons
        TableLayout tableLayout = (TableLayout) findViewById(R.id.buttons);
        tableLayout.removeAllViews();

        for (final ButtonTO button : mCurrentMessage.buttons) {
            addButton(senderName, myEmail, mSomebodyAnswered, canEdit, tableLayout, button);
        }
        if (mCurrentMessage.form == null
            && (mCurrentMessage.flags & MessagingPlugin.FLAG_ALLOW_DISMISS) == MessagingPlugin.FLAG_ALLOW_DISMISS) {
            ButtonTO button = new ButtonTO();
            button.caption = "Roger that!";
            addButton(senderName, myEmail, mSomebodyAnswered, canEdit, tableLayout, button);
        }

        if (mCurrentMessage.broadcast_type != null) {
            L.d("todo ruben Show broadcast spam control");

            final ServiceMenuItemDetails smi = mFriendsPlugin.getStore().getBroadcastServiceMenuItem(mCurrentMessage
                    .sender);
            if (smi == null) {
                L.bug("BroadcastData was null for: " + mCurrentMessage.sender);
                collapseDetails(DETAIL_SECTIONS);
                return;
            }

//            broadcastSpamControlSettingsContainer.setOnClickListener(new SafeViewOnClickListener() {
//
//                @Override
//                public void safeOnClick(View v) {
//                    L.d("goto broadcast settings");
//                    if (mMenuItemPresser == null) {
//                        //noinspection unchecked,unchecked
//                        mMenuItemPresser = new MenuItemPresser(ServiceMessageDetailActivity.this, mCurrentMessage
//                                .sender);
//                    }
//                    mMenuItemPresser.itemPressed(smi, smi.menuGeneration, new MenuItemPresser.ResultHandler() {
//                        @Override
//                        public void onSuccess() {
//                            overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_up);
//                            finish();
//                        }
//                    });
//
//                }
//
//            });

            UIUtils.showHint(this, mService, HINT_BROADCAST, R.string.hint_broadcast, mCurrentMessage.broadcast_type,
                mFriendsPlugin.getName(mCurrentMessage.sender));
        }

        if (!isUpdate)
            collapseDetails(DETAIL_SECTIONS);
    }

    private void displayWidget(LinearLayout widgetLayout, BrandingResult br) {
        final String type = (String) mCurrentMessage.form.get("type");

        widgetLayout.removeAllViews();

        final Widget widget = (Widget) getLayoutInflater().inflate(Widget.RESOURCES.get(type), null, false);
        if (br != null) {
            widget.setColorScheme(br.scheme);
        }
        widget.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
        widget.setEnabled(widgetLayout.isEnabled());
        widget.loadMessage(mCurrentMessage, this, widgetLayout);

        if (!widgetLayout.isEnabled()) {
            disableView(widget);
        }
        widgetLayout.addView(widget);
    }

    private void disableView(View v) {
        if (v instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) v;
            for (int i = 0; i < vg.getChildCount(); i++) {
                View child = vg.getChildAt(i);
                disableView(child);
            }
        } else {
            v.setEnabled(false);
            v.setFocusable(false);
        }
    }

    private Comparator<MemberStatusTO> getMemberstatusComparator() {
        return new Comparator<MemberStatusTO>() {
            @Override
            public int compare(MemberStatusTO ms1, MemberStatusTO ms2) {
                if (ms1.status == ms2.status)
                    return ms1.member.compareTo(ms2.member);
                if ((ms1.status & MessagingPlugin.STATUS_ACKED) == MessagingPlugin.STATUS_ACKED)
                    return -1;
                if ((ms1.status & MessagingPlugin.STATUS_RECEIVED) == MessagingPlugin.STATUS_RECEIVED) {
                    if ((ms2.status & MessagingPlugin.STATUS_ACKED) == MessagingPlugin.STATUS_ACKED) {
                        return 1;
                    }
                    return -1;
                }
                return 1;
            }
        };
    }

    private long getExpectNext(long uiFlags) {
        if ((uiFlags & MessagingPlugin.UI_FLAG_EXPECT_NEXT_WAIT_5) == MessagingPlugin.UI_FLAG_EXPECT_NEXT_WAIT_5)
            return 10;
        return 0;
    }

    private long getExpectNext(final ButtonTO button) {
        if (button.id == null) {
            return getExpectNext(mCurrentMessage.dismiss_button_ui_flags);
        } else {
            return getExpectNext(button.ui_flags);
        }
    }

    private void jumpToServiceHomeScreen(final ButtonTO button, final Bundle extras) {
        // detect if we come from a branding or message history
        if (AppConstants.SERVICES_ENABLED
                && getIntent().getBooleanExtra(ServiceMessageDetailActivity.JUMP_TO_SERVICE_HOME_SCREEN, true)
                && getIntent().getStringExtra(MessagingPlugin.MEMBER_FILTER) == null
                && (button == null || getExpectNext(button) == 0)
                && mFriendsPlugin.getStore().getFriendType(mCurrentMessage.sender) == FriendsPlugin.FRIEND_TYPE_SERVICE
                && mFriendsPlugin.getStore().getExistence(mCurrentMessage.sender) == Friend.ACTIVE) {

            L.d("Jumping to service home screen");

            // Message flow ended.
            Intent intent = new Intent(ServiceMessageDetailActivity.this, ServiceActionMenuActivity.class);
            intent.putExtra(ServiceActionMenuActivity.SERVICE_EMAIL, mCurrentMessage.sender);
            if (extras != null)
                intent.putExtras(extras);

            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);

            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            quit();
        }
    }

    public void excecutePositiveButtonClick() {
        for (final ButtonTO button : mCurrentMessage.buttons) {
            if (Message.POSITIVE.equals(button.id)) {
                final LinearLayout container = (LinearLayout) getLayoutInflater().inflate(
                    R.layout.message_button_detail, null);
                container.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);

                executeButtonClick(button, container, true);
                break;
            }
        }
    }

    private boolean askPermissionIfNeeded(final ButtonTO button, final SafeRunnable onGranted,
        final SafeRunnable onDenied) {
        if (SystemUtils.isFlagEnabled(button.ui_flags, MessagingPlugin.UI_FLAG_AUTHORIZE_LOCATION)) {
            return askPermissionIfNeeded(Manifest.permission.ACCESS_FINE_LOCATION, PERMISSION_REQUEST_UI_FLAG_LOCATION,
                    onGranted, onDenied);
        }
        return false;
    }

    private void executeButtonClick(final ButtonTO button, final LinearLayout container,
                                    final boolean shouldAskConfirmation) {
        executeButtonClick(button, container, shouldAskConfirmation, false);
    }

    private void executeButtonClick(final ButtonTO button, final LinearLayout container,
        final boolean shouldAskConfirmation, final boolean alreadyAskedPermission) {

        boolean askedPermission = !alreadyAskedPermission && askPermissionIfNeeded(button, new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                executeButtonClick(button, container, shouldAskConfirmation, true);
            }
        }, null);
        if (askedPermission) {
            return;
        }

        Map<String, String> actionInfo = mMessagingPlugin.getButtonActionInfo(button);
        final String buttonAction = actionInfo.get("androidAction");
        final String buttonUrl = actionInfo.get("androidUrl");

        if (shouldAskConfirmation && Message.MC_CONFIRM_PREFIX.equals(buttonAction)) {
            askConfirmation(button, buttonUrl, container);
        } else if (Message.MC_SMI_PREFIX.equals(buttonAction)) {
            if (mMenuItemPresser == null) {
                mMenuItemPresser = new MenuItemPresser(this, mCurrentMessage.sender);
            }

            mMenuItemPresser.itemPressed(buttonUrl, new MenuItemPresser.ResultHandler() {
                @Override
                public void onSuccess() {
                    // ack the message and finish without showing progress bar
                    buttonPressed(button, container, 0);
                    overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_up);
                    finish();
                }

                @Override
                public void onError() {
                    L.e("SMI with hash " + buttonUrl + " not found!"); // XXX: log error to message.sender
                    onTimeout();
                }

                @Override
                public void onTimeout() {
                    // Continue with the button press, just as if there was no smi://
                    buttonPressed(button, buttonAction, buttonUrl, container);
                }
            });
        } else {
            buttonPressed(button, buttonAction, buttonUrl, container);
        }
    }

    private void buttonPressed(ButtonTO button, String buttonAction, String buttonUrl, LinearLayout container) {
        if (!buttonPressed(button, container))
            return;

        if (!mTransfering)
            jumpToServiceHomeScreen(button, null);

        if (buttonAction != null && !Message.MC_CONFIRM_PREFIX.equals(buttonAction)) {
            final Intent intent = new Intent(buttonAction, Uri.parse(buttonUrl));
            startActivity(intent);
        }
    }

    private void askConfirmation(final ButtonTO button, final String text, final LinearLayout container) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ServiceMessageDetailActivity.this);
        builder.setTitle(R.string.message_confirm);
        builder.setMessage(text);
        builder.setPositiveButton(R.string.yes, new SafeDialogInterfaceOnClickListener() {
            @Override
            public void safeOnClick(DialogInterface dialog, int which) {
                T.UI();
                dialog.dismiss();
                executeButtonClick(button, container, false);
            }
        });
        builder.setNegativeButton(R.string.no, new SafeDialogInterfaceOnClickListener() {
            @Override
            public void safeOnClick(DialogInterface dialog, int which) {
                T.UI();
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private boolean buttonPressed(final ButtonTO button, final LinearLayout container) {
        return buttonPressed(button, container, getExpectNext(button));
    }

    private boolean buttonPressed(final ButtonTO button, final LinearLayout container, final long expectNext) {
        boolean submitted = true;
        if (mCurrentMessage.form == null) {
            ackMessage(button, expectNext, container);
        } else {
            submitted = submitForm(button);
        }
        if (!mTransfering && submitted) {
            animateAfterAck(expectNext);
        }
        return submitted;
    }

    private void ackMessage(final ButtonTO button, final long expectNext, final LinearLayout container) {
        SafeRunnable updateDoneHandler = null;
        if (expectNext == 0) {
            updateDoneHandler = new SafeRunnable() {
                @Override
                protected void safeRun() throws Exception {
                    T.dontCare();
                    if (T.getThreadType() == T.UI) {
                        dismissTransferingDialog();
                    } else if (mService != null) {
                        mService.postOnUIHandler(this);
                    }
                }
            };
        }
        mMessagingPlugin.ackMessage(mCurrentMessage, button.id, null, updateDoneHandler,
            ServiceMessageDetailActivity.this, container);
    }

    private boolean submitForm(final ButtonTO button) {
        LinearLayout widgetLayout = (LinearLayout) findViewById(R.id.widget_layout);
        Widget widget = (Widget) widgetLayout.getChildAt(0);

        if (Message.POSITIVE.equals(button.id)) {
            widget.putValue();
        }

        if (!widget.proceedWithSubmit(button.id)) {
            return false;
        }

        if (Message.POSITIVE.equals(button.id)) {
            try {
                final IJSONable formResult = widget.getWidgetResult();
                if (formResult != null) {
                    String validationError = mMessagingPlugin.validateFormResult(mCurrentMessage, formResult);
                    if (validationError != null) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(ServiceMessageDetailActivity.this);
                        builder.setTitle(R.string.validation_failed);
                        builder.setMessage(validationError);
                        builder.setPositiveButton(R.string.ok, new SafeDialogInterfaceOnClickListener() {
                            @Override
                            public void safeOnClick(DialogInterface dialog, int which) {
                                T.UI();
                                dialog.dismiss();
                            }
                        });
                        builder.create().show();

                        return false;
                    }
                }
            } catch (Exception e) {
                L.bug(e);
            }
        }

        try {
            widget.submit(button.id, mService.currentTimeMillis() / 1000);
        } catch (Exception e) {
            L.bug(e);
            dismissTransferingDialog();
            return false;
        }

        mMessagingPlugin.formSubmitted(mCurrentMessage, button.id);
        return true;
    }

    private void addButton(final String senderName, String myEmail, boolean somebodyAnswered, boolean canEdit,
        TableLayout tableLayout, final ButtonTO button) {
        TableRow row = new TableRow(this);
        tableLayout.addView(row);
        row.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT,
            TableLayout.LayoutParams.WRAP_CONTENT, 1));

        // XXX: inconsistent margin between 2 rows

        final Button buttonView = new Button(this);
        buttonView.setMinWidth(UIUtils.convertDipToPixels(this, 100));
        buttonView.setText(button.caption);
        buttonView.setTextColor(getResources().getColor(android.R.color.primary_text_dark));
        if (somebodyAnswered)
            buttonView.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        else {
            buttonView.setWidth(mDisplayWidth - UIUtils.convertDipToPixels(this, 12));
        }
        buttonView.setTextSize(19);
        row.addView(buttonView);

        final LinearLayout container = (LinearLayout) getLayoutInflater().inflate(R.layout.message_button_detail, null);
        container.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);

        boolean buttonSelectedByMe = false;
        for (MemberStatusTO status : mCurrentMessage.members) {
            if ((status.status & MessagingPlugin.STATUS_ACKED) == MessagingPlugin.STATUS_ACKED
                && ((button.id == null && status.button_id == null) || (button.id != null && button.id
                    .equals(status.button_id)))) {

                getLayoutInflater().inflate(R.layout.avatar, container);
                ImageView avatar = (ImageView) container.getChildAt(container.getChildCount() - 1);

                setAvatar(avatar, status.member);
                if (status.member.equals(myEmail))
                    buttonSelectedByMe = true;

                int dp42 = UIUtils.convertDipToPixels(this, 42);
                int dp2 = UIUtils.convertDipToPixels(this, 1);
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) avatar.getLayoutParams();
                lp.setMargins(0, dp2, 0, 0);
                lp.width = dp42;
                lp.height = dp42;
            }
        }

        row.addView(container);

        boolean hasAction = button.action != null && !"".equals(button.action);
        final boolean buttonIsEnabled = canEdit && (mCurrentMessage.form != null || !buttonSelectedByMe || hasAction);
        buttonView.setEnabled(buttonIsEnabled);

        int color;
        if (button.id == null || mCurrentMessage.form != null && Message.POSITIVE.equals(button.id)) {
            color = buttonIsEnabled ? Message.GREEN_BUTTON_COLOR : Message.GREENGRAY_BUTTON_COLOR;
        } else if (mCurrentMessage.form != null && Message.NEGATIVE.equals(button.id)) {
            color = buttonIsEnabled ? Message.RED_BUTTON_COLOR : Message.REDGRAY_BUTTON_COLOR;
        } else {
            color = buttonIsEnabled ? Message.BLUE_BUTTON_COLOR : Message.BLUEGRAY_BUTTON_COLOR;
        }
        buttonView.getBackground().setColorFilter(color, PorterDuff.Mode.MULTIPLY);

        buttonView.setOnClickListener(new SafeViewOnClickListener() {

            @Override
            public void safeOnClick(View v) {
                T.UI();
                executeButtonClick(button, container, true);
            }
        });

        final HorizontalScrollView scroller = (HorizontalScrollView) findViewById(R.id.button_scroller);
        scroller.post(new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                scroller.fullScroll(ScrollView.FOCUS_RIGHT);
            }
        });
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            switch (item.getItemId()) {
                case R.id.show_notification_settings:
                    item.setVisible(mCurrentMessage.broadcast_type != null);
                    break;
                case R.id.show_details:
                default:
                    item.setVisible(false);
                    break;
            }
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        T.UI();
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.message_menu, menu);
        menu.getItem(0).setIcon(new IconicsDrawable(this).icon(FontAwesome.Icon.faw_bell).color(Color.DKGRAY).sizeDp(18));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        T.UI();
        switch (item.getItemId()) {
            case R.id.show_notification_settings:
                final ServiceMenuItemDetails smi = mFriendsPlugin.getStore().getBroadcastServiceMenuItem(mCurrentMessage.sender);
                if (smi == null) {
                    L.bug("BroadcastData was null for: " + mCurrentMessage.sender);
                } else {
                    L.d("goto broadcast settings");
                    if (mMenuItemPresser == null) {
                        //noinspection unchecked,unchecked
                        mMenuItemPresser = new MenuItemPresser(ServiceMessageDetailActivity.this, mCurrentMessage.sender);
                    }
                    mMenuItemPresser.itemPressed(smi, smi.menuGeneration, new MenuItemPresser.ResultHandler() {
                        @Override
                        public void onSuccess() {
                            overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_up);
                            finish();
                        }
                    });
                }

                return true;
            case R.id.show_details:
                expandDetails();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setAvatar(ImageView imageView, final String email) {
        if (FriendsPlugin.SYSTEM_FRIEND.equals(email)) {
            imageView.setImageResource(R.drawable.ic_dashboard);
        } else {
            imageView.setImageBitmap(mFriendsPlugin.getAvatarBitmap(email, true));
            final SafeViewOnClickListener listener = getFriendDetailOnClickListener(email);
            if (listener != null)
                imageView.setOnClickListener(listener);
        }
    }

    private SafeViewOnClickListener getFriendDetailOnClickListener(final String email) {
        if (FriendsPlugin.SYSTEM_FRIEND.equals(email))
            return null;
        final int contactType = mFriendsPlugin.getContactType(email);
        return new SafeViewOnClickListener() {
            @Override
            public void safeOnClick(View v) {
                T.UI();
                if ((contactType & FriendsPlugin.FRIEND) == FriendsPlugin.FRIEND) {
                    mFriendsPlugin.launchDetailActivity(ServiceMessageDetailActivity.this, email);
                } else {
                    if ((contactType & FriendsPlugin.NON_FRIEND) == FriendsPlugin.NON_FRIEND) {
                        new AlertDialog.Builder(ServiceMessageDetailActivity.this)
                            .setMessage(getString(R.string.invite_as_friend, new Object[] { email }))
                            .setPositiveButton(R.string.yes, new SafeDialogInterfaceOnClickListener() {
                                @Override
                                public void safeOnClick(DialogInterface dialog, int which) {
                                    mFriendsPlugin.inviteFriend(email, null, null, true);
                                }
                            }).setNegativeButton(R.string.no, null).create().show();
                    }
                }
            }
        };
    }

    private void setStatusIcon(ImageView imageView, final MemberStatusTO ms) {
        int resource;
        if ((ms.status & MessagingPlugin.STATUS_ACKED) == MessagingPlugin.STATUS_ACKED)
            resource = R.drawable.status_blue;
        else if ((ms.status & MessagingPlugin.STATUS_RECEIVED) == MessagingPlugin.STATUS_RECEIVED)
            resource = R.drawable.status_green;
        else
            resource = R.drawable.status_yellow;
        imageView.setImageResource(resource);
    }

    private RelativeLayout createParticipantView(MemberStatusTO ms) {
        RelativeLayout rl = new RelativeLayout(this);
        int rlW = UIUtils.convertDipToPixels(this, 55);
        rl.setLayoutParams(new RelativeLayout.LayoutParams(rlW, rlW));

        getLayoutInflater().inflate(R.layout.avatar, rl);
        ImageView avatar = (ImageView) rl.getChildAt(rl.getChildCount() - 1);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(avatar.getLayoutParams());
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        avatar.setLayoutParams(params);
        setAvatar(avatar, ms.member);

        ImageView statusView = new ImageView(this);
        int w = UIUtils.convertDipToPixels(this, 12);
        RelativeLayout.LayoutParams iconParams = new RelativeLayout.LayoutParams(w, w);
        iconParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        iconParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        statusView.setLayoutParams(iconParams);
        statusView.setAdjustViewBounds(true);
        statusView.setScaleType(ScaleType.CENTER_CROP);
        setStatusIcon(statusView, ms);
        rl.addView(statusView);
        return rl;
    }

    protected void updateView(boolean isUpdate) {
        T.UI();
        if (mMustQuit) {
            if (!isFinishing())
                finish();
            return;
        }

        if (isUpdate) {
            mCurrentMessage = mStore.getFullMessageByKey(mCurrentMessage.key);
            invalidateOptionsMenu();
        }

        boolean isLocked = (mCurrentMessage.flags & MessagingPlugin.FLAG_LOCKED) == MessagingPlugin.FLAG_LOCKED;
        boolean isRinging = mCurrentMessage.alert_flags >= AlertManager.ALERT_FLAG_RING_5
            && !mMessagingPlugin.isMessageAckedByMe(mCurrentMessage);
        if (isLocked) {
            mStatusImage.setImageResource(R.drawable.status_locked);
            mStatusImage.setVisibility(View.VISIBLE);
        } else if (isRinging) {
            mStatusImage.setImageResource(R.drawable.status_ringing);
            mStatusImage.setVisibility(View.VISIBLE);
        } else {
            mStatusImage.setVisibility(View.GONE);
        }

        updateMessageDetail(isUpdate);

        if (mCurrentMessage.dirty) {
            mMessagingPlugin.cleanDirtyFlag(mCurrentMessage.key);
            if ((mCurrentMessage.flags & MessagingPlugin.FLAG_SENT_BY_JSMFR) == 0) {
                mMessagingPlugin.markMessagesAsRead(mCurrentMessage.getThreadKey(),
                    new String[] { mCurrentMessage.key });
            }
            mCurrentMessage.dirty = false;
        }
    }

    protected void quit() {
        mMustQuit = true;
    }

    protected SafeBroadcastReceiver getBroadcastReceiver() {

        return new SafeBroadcastReceiver() {
            @Override
            public String[] onSafeReceive(Context context, Intent intent) {
                T.UI();
                String action = intent.getAction();
                if (MessagingPlugin.MESSAGE_MEMBER_STATUS_UPDATE_RECEIVED_INTENT.equals(action)
                    || MessagingPlugin.MESSAGE_LOCKED_INTENT.equals(action)
                    || MessagingPlugin.MESSAGE_PROCESSED_INTENT.equals(action)) {
                    if (mCurrentMessage != null && intent.hasExtra("message")
                        && intent.getStringExtra("message").equals(mCurrentMessage.key)) {
                        updateView(true);

                        return UPDATE_VIEW_INTENT_ACTIONS;
                    }
                }
                if (FriendsPlugin.FRIEND_UPDATE_INTENT.equals(action)
                    || IdentityStore.IDENTITY_CHANGED_INTENT.equals(action)
                    || FriendsPlugin.FRIENDS_LIST_REFRESHED.equals(action)
                    || FriendsPlugin.FRIEND_AVATAR_CHANGED_INTENT.equals(action)) {
                    updateView(true);

                    return UPDATE_VIEW_INTENT_ACTIONS;
                }
                if (MessagingPlugin.NEW_MESSAGE_RECEIVED_INTENT.equals(action)) {
                    String transferFailedContext = "DASHBOARD_" + mCurrentMessage.key;
                    if (mTransfering && transferFailedContext.equals(intent.getStringExtra("context"))) {
                        final Intent i = new Intent(context, ServiceMessageDetailActivity.class);
                        i.putExtra("message", intent.getStringExtra("message"));
                        i.putExtra(JUMP_TO_SERVICE_HOME_SCREEN, getIntent().getBooleanExtra(JUMP_TO_SERVICE_HOME_SCREEN, true));
                        i.putExtra(MessagingPlugin.MEMBER_FILTER, getIntent().getStringExtra(MessagingPlugin.MEMBER_FILTER));
                        startActivity(i);
                        overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_up);
                        dismissTransferingDialog();
                        finish();
                        return new String[] { action };
                    }

                    if (mExpectNextTimer == null) // Not interested in NEW_MESSAGE_RECEIVED_INTENTS at this moment
                        return new String[]{action};

                    // We are expecting a reply on this thread!
                    String pKey = mCurrentMessage.getThreadKey();
                    if (!pKey.equals(intent.getStringExtra("parent"))) {
                        L.d("New message is from another thread");
                        return null; // New message is from another thread
                    }

                    // We received the reply!
                    mExpectNextTimer.cancel();
                    mExpectNextTimer = null;

                    final Intent i = new Intent(context, ServiceMessageDetailActivity.class);
                    i.putExtra("message", intent.getStringExtra("message"));
                    i.putExtra(JUMP_TO_SERVICE_HOME_SCREEN, getIntent().getBooleanExtra(JUMP_TO_SERVICE_HOME_SCREEN, true));
                    i.putExtra(MessagingPlugin.MEMBER_FILTER, getIntent().getStringExtra(MessagingPlugin.MEMBER_FILTER));
                    startActivity(i);

                    overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_up);

                    dismissTransferingDialog();
                    finish();
                    return new String[] { action };
                }
                if (MessagingPlugin.MESSAGE_JSMFR_ERROR.equals(action)) {
                    if (mExpectNextTimer == null) // Not interested in MESSAGE_JSMFR_ERROR at this moment
                        return new String[] { action };

                    String threadKey = mCurrentMessage.getThreadKey();
                    if (!threadKey.equals(intent.getStringExtra("parent_message_key"))
                        && !threadKey.equals(intent.getStringExtra("message_key"))) {
                        return null; // Intent is for another thread
                    }

                    mExpectNextTimer.cancel();
                    mExpectNextTimer = null;

                    dismissTransferingDialog();

                    Bundle extras = new Bundle();
                    extras.putBoolean(ServiceActionMenuActivity.SHOW_ERROR_POPUP, true);
                    jumpToServiceHomeScreen(null, extras);
                    updateView(false);
                    return new String[] { action };
                }
                if (MessagingPlugin.MESSAGE_FLOW_ENDED_INTENT.equals(action)) {
                    if (mExpectNextTimer == null) // Not interested in MESSAGE_JSMFR_ERROR at this moment
                        return new String[] { action };

                    String threadKey = mCurrentMessage.getThreadKey();
                    if (!threadKey.equals(intent.getStringExtra("parent_message_key"))) {
                        return null; // Intent is for another thread
                    }

                    if (intent.getBooleanExtra("wait_for_followup", false)) {
                        return null; // We must keep on waiting
                    }

                    mExpectNextTimer.cancel();
                    mExpectNextTimer = null;

                    dismissTransferingDialog();

                    jumpToServiceHomeScreen(null, null);
                    return new String[] { action };
                }
                if (BrandingMgr.GENERIC_BRANDING_AVAILABLE_INTENT.equals(intent.getAction())) {
                    if (intent.getStringExtra(BrandingMgr.BRANDING_KEY).equals(mCurrentMessage.branding)) {
                        updateView(false);
                        return new String[] { action };
                    }
                }
                if (MessagingPlugin.MESSAGE_SUBMIT_PHOTO_UPLOAD.equals(action)) {
                    L.d("MessagingPlugin.MESSAGE_SUBMIT_PHOTO_UPLOAD.equals(action)");
                    if (intent.hasExtra("submitToJSMFR"))
                        messageSubmitToJsMfr(intent, action);
                    String messageKey = intent.getStringExtra("message_key");
                    if (mCurrentMessage.key.equals(messageKey)) {
                        transferComplete();
                    }

                    return null;
                }
                return null;
            }
        };
    }

    protected IntentFilter getIntentFilter() {
        final IntentFilter filter = new IntentFilter(MessagingPlugin.MESSAGE_MEMBER_STATUS_UPDATE_RECEIVED_INTENT);
        filter.addAction(MessagingPlugin.MESSAGE_LOCKED_INTENT);
        filter.addAction(MessagingPlugin.MESSAGE_PROCESSED_INTENT);
        filter.addAction(MessagingPlugin.NEW_MESSAGE_RECEIVED_INTENT);
        filter.addAction(MessagingPlugin.MESSAGE_JSMFR_ERROR);
        filter.addAction(MessagingPlugin.MESSAGE_FLOW_ENDED_INTENT);
        filter.addAction(FriendsPlugin.FRIEND_AVATAR_CHANGED_INTENT);
        filter.addAction(FriendsPlugin.FRIEND_UPDATE_INTENT);
        filter.addAction(IdentityStore.IDENTITY_CHANGED_INTENT);
        filter.addAction(FriendsPlugin.FRIENDS_LIST_REFRESHED);
        filter.addAction(BrandingMgr.GENERIC_BRANDING_AVAILABLE_INTENT);
        filter.addAction(MessagingPlugin.MESSAGE_SUBMIT_PHOTO_UPLOAD);
        return filter;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        mDisplayWidth = UIUtils.getDisplayWidth(this);

        if (!mSomebodyAnswered) {
            TableLayout tableLayout = (TableLayout) findViewById(R.id.buttons);
            for (int i = 0; i < tableLayout.getChildCount(); i++) {
                TableRow row = (TableRow) tableLayout.getChildAt(i);
                Button button = (Button) row.getChildAt(0);
                button.setWidth(mDisplayWidth - UIUtils.convertDipToPixels(this, 12));
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (mCurrentMessage.form != null) {
            LinearLayout widgetLayout = (LinearLayout) findViewById(R.id.widget_layout);
            Widget widget = (Widget) widgetLayout.getChildAt(0);
            widget.onActivityResult(requestCode, resultCode, data);
        }
    }

    protected void messageSubmitToJsMfr(Intent intent, String function) {
        T.UI();
        MessagingPlugin plugin = mService.getPlugin(MessagingPlugin.class);
        if (MainActivity.ACTION_NOTIFICATION_PHOTO_UPLOAD_DONE.equals(function)) {
            JSONObject transfers = plugin.jsmfrTransferCompletedGetNext();
            while (transfers != null) {
                String threadKey;
                try {
                    threadKey = (String) transfers.get("threadKey");
                    String requestJSON = (String) transfers.get("submitToJSMFR");
                    @SuppressWarnings("unchecked")
                    Map<String, Object> request = (Map<String, Object>) JSONValue.parse(requestJSON);

                    plugin.answerJsMfrMessage(threadKey, request, function, ServiceMessageDetailActivity.this,
                        (LinearLayout) findViewById(R.id.widget_layout));
                    transfers = plugin.jsmfrTransferCompletedGetNext();
                } catch (Exception e) {
                    L.e(e);
                    e.printStackTrace();
                }

            }
        } else {
            String threadKey = intent.getStringExtra("threadKey");
            String requestJSON = intent.getStringExtra("submitToJSMFR");
            @SuppressWarnings("unchecked")
            Map<String, Object> request = (Map<String, Object>) JSONValue.parse(requestJSON);

            plugin.answerJsMfrMessage(threadKey, request, function, ServiceMessageDetailActivity.this,
                (LinearLayout) findViewById(R.id.widget_layout));
        }
    }

    private void animateAfterAck(long expectNext) {
        mDialog = ProgressDialog.show(ServiceMessageDetailActivity.this, "", getString(R.string.transmitting), true,
            expectNext != 0);
        mDialog.show();

        if (expectNext == 0 || (mCurrentMessage.flags & MessagingPlugin.FLAG_SENT_BY_JSMFR) == 0
            && !checkConnectivity()) {
            dismissTransferingDialog();
            quit();
        } else {
            mExpectNextTimer = new Timer("expect_next");
            mExpectNextTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (mService != null) {
                        mService.postOnUIHandler(new SafeRunnable() {
                            @Override
                            public void safeRun() {
                                T.UI();
                                dismissTransferingDialog();
                                finish();
                            }
                        });
                    }
                }
            }, 1000 * expectNext);
        }
    }
}

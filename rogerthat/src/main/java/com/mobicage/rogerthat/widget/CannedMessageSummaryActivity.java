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

package com.mobicage.rogerthat.widget;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.CannedButton;
import com.mobicage.rogerthat.MainService;
import com.mobicage.rogerthat.SendMessageWizard;
import com.mobicage.rogerthat.SendMessageWizardActivity;
import com.mobicage.rogerthat.ServiceBoundActivity;
import com.mobicage.rogerthat.plugins.friends.FriendsPlugin;
import com.mobicage.rogerthat.plugins.messaging.Message;
import com.mobicage.rogerthat.plugins.messaging.MessagingPlugin;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeViewOnClickListener;

public class CannedMessageSummaryActivity extends ServiceBoundActivity {

    public static final String CANNED_MESSAGE_SENT = "CannedMessageSummaryActivity.CANNED_MESSAGE_SENT";
    public static final String CANNED_MESSAGE_CANCELED = "CannedMessageSummaryActivity.CANNED_MESSAGE_CANCELED";
    public static final String CANNED_MESSAGE_NAME = "CannedMessageSummaryActivity.CANNED_MESSAGE_NAME";

    @Override
    protected void onServiceBound() {
        String cannedMessageName = getIntent().getStringExtra(CannedMessageSummaryActivity.CANNED_MESSAGE_NAME);
        final SendMessageWizard wiz = SendMessageWizard.getWizard(this, mService.getConfigurationProvider(),
            cannedMessageName, false);

        FriendsPlugin friendsPlugin = mService.getPlugin(FriendsPlugin.class);

        LayoutInflater layoutInflater = getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.canned_message_summary, null);
        LinearLayout recipients = (LinearLayout) view.findViewById(R.id.recipients);

        // Add recipients to view
        for (String recipient : wiz.getRecipients()) {
            layoutInflater.inflate(R.layout.avatar, recipients);
            ImageView avatar = (ImageView) recipients.getChildAt(recipients.getChildCount() - 1);

            Bitmap bitmap = friendsPlugin.getAvatarBitmap(recipient);
            avatar.setImageBitmap(bitmap == null ? friendsPlugin.getMissingFriendAvatarBitmap() : bitmap);

            HorizontalScrollView scroller = (HorizontalScrollView) view.findViewById(R.id.recipients_scroller);
            scroller.smoothScrollTo(recipients.getWidth(), 0);
        }

        // Add message to view
        TextView messageView = (TextView) view.findViewById(R.id.message);
        messageView.setText(wiz.getMessage());

        // Add buttons to view
        LinearLayout buttons = (LinearLayout) view.findViewById(R.id.buttons);
        for (Long buttonId : wiz.getButtons()) {
            CannedButton button = wiz.getCannedButtons().getById(buttonId);
            if (button == null)
                continue;
            RelativeLayout buttonContainer = (RelativeLayout) layoutInflater.inflate(
                R.layout.message_thread_member_detail_right, null);
            buttons.addView(buttonContainer);
            Button buttonView = (Button) buttonContainer.findViewById(R.id.button);
            buttonView.setText(button.getCaption());
            buttonView.getBackground().setColorFilter(Message.BLUE_BUTTON_COLOR, PorterDuff.Mode.MULTIPLY);
            buttonView.setEnabled(false);
        }

        // Back button onClickListener
        view.findViewById(R.id.previousButton).setOnClickListener(new SafeViewOnClickListener() {
            @Override
            public void safeOnClick(View v) {
                cancel();
            }
        });

        // Send button onClickListener
        view.findViewById(R.id.sendButton).setOnClickListener(new SafeViewOnClickListener() {
            @Override
            public void safeOnClick(View v) {
                sendMessage(wiz);
                mService.sendBroadcast(new Intent(CANNED_MESSAGE_SENT));
                finish();
            }
        });

        setContentView(view);
    }

    @Override
    protected void onServiceUnbound() {
    }

    private void sendMessage(final SendMessageWizard wiz) {
        final MainService mainService = mService;
        final String me = mainService.getIdentityStore().getIdentity().getEmail();
        final MessagingPlugin messagingPlugin = mainService.getPlugin(MessagingPlugin.class);
        final FriendsPlugin friendsPlugin = mainService.getPlugin(FriendsPlugin.class);
        try {
            final String tmpKey = messagingPlugin.generateTmpKey();
            SendMessageWizardActivity
                .sendMessage(wiz, null, 0, tmpKey, me, friendsPlugin, messagingPlugin, mainService);
        } catch (Exception e) {
            L.bug(e);
            new AlertDialog.Builder(this).setMessage(R.string.failed_to_send_message).setCancelable(true).create()
                .show();
        }
    }

    private void cancel() {
        mService.sendBroadcast(new Intent(CANNED_MESSAGE_CANCELED));
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            cancel();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
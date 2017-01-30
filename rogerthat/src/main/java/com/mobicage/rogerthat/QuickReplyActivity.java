/*
 * Copyright 2017 Mobicage NV
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
 * @@license_version:1.2@@
 */

package com.mobicage.rogerthat;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.plugins.friends.FriendsPlugin;
import com.mobicage.rogerthat.plugins.messaging.Message;
import com.mobicage.rogerthat.plugins.messaging.MessagingPlugin;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rpc.ResponseHandler;
import com.mobicage.to.messaging.AttachmentTO;
import com.mobicage.to.messaging.ButtonTO;
import com.mobicage.to.messaging.SendMessageRequestTO;
import com.mobicage.to.messaging.SendMessageResponseTO;

import java.util.UUID;

public class QuickReplyActivity extends ServiceBoundActivity {


    public static final String MESSAGE_KEY = "message_key";
    public static final String MESSAGE = "message";
    public static final String TITLE = "title";
    public static final String SENDER = "sender";
    private String mMessageKey;
    private EditText mReplyEditText;
    private MessagingPlugin mMessagingPlugin;
    private String mSender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentViewWithoutNavigationBar(R.layout.quick_reply_notification);
        Intent intent = getIntent();
        processIntent(intent);
    }

    @Override
    protected void onServiceBound() {
        mMessagingPlugin = mService.getPlugin(MessagingPlugin.class);
        FriendsPlugin mFriendsPlugin = mService.getPlugin(FriendsPlugin.class);
        ImageView avatarView = (ImageView) findViewById(R.id.quick_reply_avatar);
        avatarView.setImageBitmap(mFriendsPlugin.getAvatarBitmap(mSender));
    }

    @Override
    protected void onServiceUnbound() {

    }

    private void processIntent(Intent intent) {
        mMessageKey = intent.getStringExtra(MESSAGE_KEY);
        String message = intent.getStringExtra(MESSAGE);
        String title = intent.getStringExtra(TITLE);
        mSender = intent.getStringExtra(SENDER);
        TextView titleView = (TextView) findViewById(R.id.quick_reply_title);
        TextView messageView = (TextView) findViewById(R.id.quick_reply_message);
        titleView.setText(title);
        messageView.setText(message);

        final ImageButton replyButton = (ImageButton) findViewById(R.id.reply_button);
        replyButton.setEnabled(false);

        mReplyEditText = (EditText) findViewById(R.id.reply_text);
        // Enter key listener
        mReplyEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    sendMessage(mReplyEditText);
                    return true;
                }
                return false;
            }
        });
        mReplyEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = s.toString().trim();
                replyButton.setEnabled(text.length() != 0);
            }

        });
    }

    public void sendMessage(View view) {
        T.UI();
        String message = mReplyEditText.getText().toString().trim();
        if ("".equals(message)) {
            finish();
            return;
        }

        final Message parentMessage = mMessagingPlugin.getStore().getMessageByKey(mMessageKey);
        final SendMessageRequestTO request = new SendMessageRequestTO();
        request.message = message;
        request.timeout = 0;
        request.key = UUID.randomUUID().toString();
        request.priority = Message.PRIORITY_NORMAL;
        request.buttons = new ButtonTO[0];
        request.members = new String[0];  // Server calculates members in case of reply
        request.sender_reply = null;
        request.attachments = new AttachmentTO[0];

        if (parentMessage.parent_key == null) {
            request.flags = parentMessage.flags;
            request.parent_key = parentMessage.key;
        } else {
            request.flags = mMessagingPlugin.getStore().getMessageFlagsUI(parentMessage.parent_key);
            request.parent_key = parentMessage.parent_key;
        }

        try {
            com.mobicage.api.messaging.Rpc.sendMessage(new ResponseHandler<SendMessageResponseTO>(), request);
        } catch (Exception e) {
            L.bug(e);
        }
        mService.postAtFrontOfBIZZHandler(new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                String ownEmail = mService.getIdentityStore().getIdentity().getEmail();
                mMessagingPlugin.storeMessage(ownEmail, request, null);
            }
        });
        finish();
    }
}

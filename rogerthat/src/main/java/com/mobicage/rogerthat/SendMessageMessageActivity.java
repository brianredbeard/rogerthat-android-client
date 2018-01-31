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

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.MenuItem;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.plugins.messaging.Message;
import com.mobicage.rogerthat.plugins.system.SystemPlugin;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.ui.SendMessageView;

public class SendMessageMessageActivity extends ServiceBoundActivity {


    public static final String PARENT_KEY = "parent_key";
    public static final String REPLIED_TO_KEY = "replied_to_key";
    public static final String RECIPIENTS = "recipients";
    public static final String INITIAL_TEXT = "initial_text";
    public static final String FLAGS = "flags";
    public static final String DEFAULT_PRIORITY = "default_priority";
    public static final String DEFAULT_STICKY = "default_sticky";

    // Owned by UI thread
    private String mParentKey = null;
    private long mParentFlags = 0;
    private String mRepliedToKey = null;
    private String mInitialText;

    private String[] mFriendRecipients;
    private long mPriority = Message.PRIORITY_NORMAL;
    private boolean mIsSticky = false;

    private SendMessageView mSendMessageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        T.UI();

        setContentView(R.layout.send_message_message);
        setActivityName("send_message_message");
        setTitle(R.string.title_message);

        Intent intent = getIntent();
        mInitialText = intent.getStringExtra(INITIAL_TEXT);
        mFriendRecipients = intent.getStringArrayExtra(RECIPIENTS);
        mParentKey = intent.hasExtra(PARENT_KEY) ? intent.getStringExtra(PARENT_KEY) : null;
        mParentFlags = intent.getLongExtra(FLAGS, 0);
        mRepliedToKey = intent.hasExtra(REPLIED_TO_KEY) ? intent.getStringExtra(REPLIED_TO_KEY) : null;

        if (intent.hasExtra(DEFAULT_PRIORITY)) {
            mPriority = intent.getLongExtra(DEFAULT_PRIORITY, mPriority);
        }

        if (intent.hasExtra(DEFAULT_STICKY)) {
            mIsSticky = intent.getBooleanExtra(DEFAULT_STICKY, mIsSticky);
        }
    }

    @Override
    protected void onServiceBound() {
        mSendMessageView = (SendMessageView) findViewById(R.id.chat_container);
        mSendMessageView.setActive(this, mService, mFriendRecipients, mInitialText, mParentKey, mParentFlags, mRepliedToKey, mPriority, mIsSticky);
        setThreadBackground();
    }

    @Override
    protected void onServiceUnbound() {

    }

    private void setThreadBackground() {
        Bitmap background = SystemPlugin.getAppAsset(mService, SystemPlugin.ASSET_CHAT_BACKGROUND);
        if (background != null) {
            BitmapDrawable backgroundDrawable = new BitmapDrawable(getResources(), background);
            backgroundDrawable.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
            findViewById(R.id.chat_layout).setBackground(backgroundDrawable);
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (!mServiceIsBound) {
            addOnServiceBoundRunnable(new SafeRunnable() {
                @Override
                protected void safeRun() throws Exception {
                    onActivityResult(requestCode, resultCode, data);
                }
            });
            return;
        }

        if (mSendMessageView != null) {
            mSendMessageView.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mSendMessageView.hideKeyboard();
        }
        return super.onOptionsItemSelected(item);
    }
}

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

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;
import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.config.Configuration;
import com.mobicage.rogerthat.plugins.messaging.AttachmentViewerActivity;
import com.mobicage.rogerthat.plugins.messaging.Message;
import com.mobicage.rogerthat.plugins.messaging.MessageStore;
import com.mobicage.rogerthat.plugins.messaging.MessagingActivity;
import com.mobicage.rogerthat.plugins.messaging.MessagingPlugin;
import com.mobicage.rogerthat.plugins.messaging.SendMessageResponseHandler;
import com.mobicage.rogerthat.util.IOUtils;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.pickle.PickleException;
import com.mobicage.rogerthat.util.pickle.Pickler;
import com.mobicage.rogerthat.util.system.SafeAsyncTask;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.system.SafeViewOnClickListener;
import com.mobicage.rogerthat.util.system.SystemUtils;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.ui.ImageHelper;
import com.mobicage.rogerthat.util.ui.SendMessageView;
import com.mobicage.rogerthat.util.ui.UIUtils;
import com.mobicage.to.messaging.AttachmentTO;
import com.mobicage.to.messaging.ButtonTO;
import com.mobicage.to.messaging.MemberStatusTO;
import com.mobicage.to.messaging.MessageTO;
import com.mobicage.to.messaging.SendMessageRequestTO;

import org.jivesoftware.smack.util.Base64;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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

        setContentViewWithoutNavigationBar(R.layout.send_message_message);
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
    }

    @Override
    protected void onServiceUnbound() {

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

        mSendMessageView.onActivityResult(requestCode, resultCode, data);
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

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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.plugins.friends.Friend;
import com.mobicage.rogerthat.plugins.friends.FriendStore;
import com.mobicage.rogerthat.plugins.friends.FriendsPlugin;
import com.mobicage.rogerthat.plugins.friends.Group;
import com.mobicage.rogerthat.plugins.messaging.AttachmentViewerActivity;
import com.mobicage.rogerthat.plugins.messaging.Message;
import com.mobicage.rogerthat.plugins.messaging.MessageStore;
import com.mobicage.rogerthat.plugins.messaging.MessagingPlugin;
import com.mobicage.rogerthat.plugins.messaging.SendMessageResponseHandler;
import com.mobicage.rogerthat.plugins.messaging.ServiceMessageDetailActivity;
import com.mobicage.rogerthat.util.IOUtils;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeAsyncTask;
import com.mobicage.rogerthat.util.system.SafeBroadcastReceiver;
import com.mobicage.rogerthat.util.system.SafeDialogInterfaceOnClickListener;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.system.SafeViewOnClickListener;
import com.mobicage.rogerthat.util.system.SystemUtils;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.ui.BackButtonOverriddenEditText;
import com.mobicage.rogerthat.util.ui.ImageHelper;
import com.mobicage.rogerthat.util.ui.SeparatedListAdapter;
import com.mobicage.rogerthat.util.ui.UIUtils;
import com.mobicage.rogerthat.util.ui.Wizard;
import com.mobicage.rpc.config.AppConstants;
import com.mobicage.to.messaging.AttachmentTO;
import com.mobicage.to.messaging.ButtonTO;
import com.mobicage.to.messaging.MemberStatusTO;
import com.mobicage.to.messaging.MessageTO;
import com.mobicage.to.messaging.SendMessageRequestTO;

public class SendMessageWizardActivity extends ServiceBoundActivity {

    // parent_key = message key of the parent key of this thread

    // replied_to_key = message key from which the reply was launched
    // this is potentially a child of parent_key
    // we need it to allow this child message to be auto-dismissed when the reply is finished

    public static final String SAVE_CANNED_MESSAGE_MODE = "SendMessageWizardActivity.SAVE_CANNED_MESSAGE_MODE";
    public static final String CANNED_MESSAGE_SAVED = "SendMessageWizardActivity.CANNED_MESSAGE_SAVED";
    public static final String CANNED_MESSAGE_CANCELED = "SendMessageWizardActivity.CANNED_MESSAGE_CANCELED";
    public static final String CANNED_MESSAGE_NAME = "SendMessageWizardActivity.CANNED_MESSAGE_NAME";
    public static final String PARENT_KEY = "parent_key";
    public static final String REPLIED_TO_KEY = "replied_to_key";
    public static final String GROUP_RECIPIENTS = "group_recipients";
    public static final String RECIPIENTS = "recipients";
    public static final String INITIAL_TEXT = "initial_text";
    public static final String FLAGS = "flags";
    public static final String DEFAULT_PRIORITY = "default_priority";
    public static final String DEFAULT_STICKY = "default_sticky";

    private static final int PICK_CONTACT = 1;
    private static final int GET_LOCATION = 2;
    private static final int PICK_IMAGE = 3;
    private static final int PICK_VIDEO = 4;

    private int _5_DP_IN_PX;
    private int _30_DP_IN_PX;
    private int _60_DP_IN_PX;
    private final int IMAGE_BUTTON_SEND_OR_SAVE = 1;
    private final int IMAGE_BUTTON_TEXT = 2;
    private final int IMAGE_BUTTON_BUTTONS = 3;
    private final int IMAGE_BUTTON_PICTURE = 4;
    private final int IMAGE_BUTTON_VIDEO = 5;
    private final int IMAGE_BUTTON_PRIORITY = 6;
    private final int IMAGE_BUTTON_STICKY = 7;
    private final int IMAGE_BUTTON_MORE = 8;
    private final int IMAGE_BUTTON_PADDING = 10;

    private final int PERMISSION_REQUEST_CAMERA = 1;

    private List<Integer> mImageButtons;
    private int mMaxImageButtonsOnScreen;

    private Uri mUriSavedFile;

    private Cursor mCursorFriends = null;
    private Cursor mCursorGroups = null;

    private class CannedButtonAdapter extends BaseAdapter {

        private final CannedButtons mButtons;

        public CannedButtonAdapter(CannedButtons buttons) {
            mButtons = buttons;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final View view;
            if (convertView == null)
                view = SendMessageWizardActivity.this.getLayoutInflater().inflate(R.layout.canned_button_item, parent,
                    false);
            else
                view = convertView;
            CannedButton item = mButtons.get(position);
            setButtonItemView(view, item);
            return view;
        }

        @Override
        public int getCount() {
            return mButtons.size();
        }

        @Override
        public Object getItem(int position) {
            return mButtons.get(position);
        }

        @Override
        public long getItemId(int position) {
            return mButtons.get(position).getId();
        }
    }

    // XXX: correctly implement behavior when friend is added / removed

    private static final Pattern actionPattern = Pattern.compile("^(tel://|geo://|https?://)(.*)$");

    // Owned by UI thread
    private FriendsPlugin mFriendsPlugin;
    private MessagingPlugin mMessagingPlugin;
    private String mTmpKey;

    private LinearLayout mRecipients;

    private ListView mListView;
    private ListView mButtonsListView;
    private SendMessageWizard mWiz;
    private EditText mActionView;
    private EditText mCaptionView;

    private String mParentKey = null;
    private long mParentFlags = 0;
    private String mRepliedToKey = null;
    private int mRestorePosition;
    private Set<String> mGroupRecipients;
    private Set<String> mFriendRecipients;
    private String mInitialText;
    private boolean mSaveOnlyMode = false;

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            switch (item.getItemId()) {
            case R.id.create_group:
            case R.id.load:
                item.setVisible(mWiz.getPosition() == 0);
                break;
            case R.id.save:
                item.setVisible(mWiz.getPosition() != 0);
                break;
            default:
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
        inflater.inflate(R.menu.send_message_wizard_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        T.UI();
        switch (item.getItemId()) {
        case R.id.create_group:
            showCreateGroup();
            break;
        case R.id.load:
            showLoadCannedMessages();
            break;
        case R.id.save:
            saveCannedMessage();
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showCreateGroup() {
        final EditText edit = (EditText) getLayoutInflater().inflate(R.layout.save_canned_message_edit, null);
        new AlertDialog.Builder(this).setTitle(R.string.create_group).setView(edit)
            .setPositiveButton(R.string.ok, new SafeDialogInterfaceOnClickListener() {
                @Override
                public void safeOnClick(DialogInterface dialog, int which) {
                    InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    mgr.hideSoftInputFromWindow(edit.getWindowToken(), 0);

                    String name = edit.getText().toString();
                    String guid = UUID.randomUUID().toString();
                    mFriendsPlugin.getStore().insertGroup(guid, name, null, null);

                    final Intent groupDetails = new Intent(SendMessageWizardActivity.this, GroupDetailActivity.class);
                    groupDetails.putExtra(GroupDetailActivity.GUID, guid);
                    groupDetails.putExtra(GroupDetailActivity.NEW_GROUP, true);
                    startActivity(groupDetails);
                }
            }).setNegativeButton(R.string.cancel, new SafeDialogInterfaceOnClickListener() {
                @Override
                public void safeOnClick(DialogInterface dialog, int which) {
                    InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    mgr.hideSoftInputFromWindow(edit.getWindowToken(), 0);
                }
            }).create().show();

        edit.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    private void saveCannedMessage() {
        final EditText message = (EditText) findViewById(R.id.view_flipper_message);
        mWiz.setMessage(message.getText().toString());

        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(message.getWindowToken(), 0);

        final EditText edit = (EditText) getLayoutInflater().inflate(R.layout.save_canned_message_edit, null);
        new AlertDialog.Builder(this).setTitle(R.string.save_canned_message_dialog_title).setView(edit)
            .setPositiveButton(R.string.ok, new SafeDialogInterfaceOnClickListener() {
                @Override
                public void safeOnClick(DialogInterface dialog, int which) {
                    String name = edit.getText().toString();
                    mWiz.save(name);
                    UIUtils.showLongToast(SendMessageWizardActivity.this, getString(R.string.message_saved));
                    if (mSaveOnlyMode) {
                        clearWizard(mWiz);
                        Intent intent = new Intent(CANNED_MESSAGE_SAVED);
                        intent.putExtra(CANNED_MESSAGE_NAME, name);
                        mService.sendBroadcast(intent);
                        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        mgr.hideSoftInputFromWindow(edit.getWindowToken(), 0);
                        finish();
                    }
                }
            }).setNegativeButton(R.string.cancel, null).create().show();

        edit.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    private void showLoadCannedMessages() {
        final List<String> items = new ArrayList<String>(SendMessageWizard.getCannedMessages(mService
            .getConfigurationProvider()));
        if (items.size() == 0) {
            new AlertDialog.Builder(this).setTitle(R.string.warning)
                .setMessage(getString(R.string.no_saved_messages_found)).setPositiveButton(R.string.ok, null).create()
                .show();
        } else {
            Collections.sort(items);
            new AlertDialog.Builder(this)
                .setTitle(R.string.load_canned_message_dialog_title)
                .setAdapter(new ArrayAdapter<String>(this, R.layout.canned_message_title_item, items),
                    new SafeDialogInterfaceOnClickListener() {

                        @Override
                        public void safeOnClick(DialogInterface dialog, final int which) {
                            new SafeRunnable() {
                                @Override
                                protected void safeRun() throws Exception {
                                    SendMessageWizardActivity.this.initActivity(items.get(which));
                                }
                            }.run();
                        }

                    }).create().show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        T.UI();
        T.setUIThread("SendMessageWizardActivity.onCreate()");
        setContentView(R.layout.send_message_wizard_activity);

        _5_DP_IN_PX = UIUtils.convertDipToPixels(this, 5);
        _30_DP_IN_PX = UIUtils.convertDipToPixels(this, 30);
        _60_DP_IN_PX = UIUtils.convertDipToPixels(this, 60);
        final int displayWidth = UIUtils.getDisplayWidth(this);
        mMaxImageButtonsOnScreen = displayWidth / _60_DP_IN_PX;
        L.i("displayWidth: " + displayWidth);
        L.i("mMaxImageButtonsOnScreen: " + mMaxImageButtonsOnScreen);

        mRestorePosition = -1;
        if (savedInstanceState != null) {
            mParentKey = savedInstanceState.getString(PARENT_KEY);
            mRepliedToKey = savedInstanceState.getString(REPLIED_TO_KEY);
            mParentFlags = savedInstanceState.containsKey(FLAGS) ? savedInstanceState.getLong(FLAGS) : 0;
        }
        Intent intent = getIntent();
        if (intent != null) {
            mInitialText = intent.getStringExtra(INITIAL_TEXT);
            mParentFlags = intent.getLongExtra(FLAGS, 0);

            Bundle extras = intent.getExtras();
            if (extras != null) {
                String[] groupRecipients = extras.getStringArray(GROUP_RECIPIENTS);
                if (groupRecipients != null) {
                    mGroupRecipients = new HashSet<String>(Arrays.asList(groupRecipients));
                }
                String[] recipients = extras.getStringArray(RECIPIENTS);
                if (recipients != null) {
                    mFriendRecipients = new HashSet<String>(Arrays.asList(recipients));
                }
            }
        }
    }

    private final BroadcastReceiver mBroadcastReceiver = new SafeBroadcastReceiver() {
        @Override
        public String[] onSafeReceive(Context context, Intent intent) {
            T.UI();
            if (FriendsPlugin.GROUPS_UPDATED.equals(intent.getAction())
                || FriendsPlugin.GROUP_ADDED.equals(intent.getAction())
                || FriendsPlugin.GROUP_MODIFIED.equals(intent.getAction())
                || FriendsPlugin.GROUP_REMOVED.equals(intent.getAction())) {

                if (FriendsPlugin.GROUP_REMOVED.equals(intent.getAction())) {
                    String guid = intent.getStringExtra(GroupDetailActivity.GUID);
                    boolean selected = mWiz.getGroupRecipients().contains(guid);
                    if (selected) {
                        mWiz.getGroupRecipients().remove(guid);
                        ImageView avatar = (ImageView) mRecipients.findViewWithTag(guid);
                        mRecipients.removeView(avatar);
                        mWiz.save();
                    }
                }
                BaseAdapter adapter = (BaseAdapter) mListView.getAdapter();
                if (adapter != null)
                    adapter.notifyDataSetChanged();
            }

            return new String[] { intent.getAction() };
        }
    };

    @Override
    protected void onServiceBound() {
        try {
            final IntentFilter intentFilter = new IntentFilter(FriendsPlugin.GROUPS_UPDATED);
            intentFilter.addAction(FriendsPlugin.GROUP_ADDED);
            intentFilter.addAction(FriendsPlugin.GROUP_MODIFIED);
            intentFilter.addAction(FriendsPlugin.GROUP_REMOVED);
            registerReceiver(mBroadcastReceiver, intentFilter);

            mFriendsPlugin = mService.getPlugin(FriendsPlugin.class);
            mMessagingPlugin = mService.getPlugin(MessagingPlugin.class);
            mTmpKey = mMessagingPlugin.generateTmpKey();

            mSaveOnlyMode = getIntent().getBooleanExtra(SAVE_CANNED_MESSAGE_MODE, false);
            initActivity(null);
        } catch (Exception e) {
            L.bug(e);
        }
    }

    @Override
    protected void onServiceUnbound() {
        if (mBroadcastReceiver != null)
            unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    public void onConfigurationChanged(android.content.res.Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        L.d("onConfigurationChanged");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mParentKey != null) {
            outState.putString(PARENT_KEY, mParentKey);
            outState.putString(REPLIED_TO_KEY, mRepliedToKey);
            outState.putLong(FLAGS, mParentFlags);
        }

        outState.putInt("position", mWiz == null ? -1 : mWiz.getPosition());
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

        switch (requestCode) {
        case PICK_CONTACT:
            if (resultCode == Activity.RESULT_OK) {
                Uri contactData = data.getData();
                Cursor c = managedQuery(contactData, null, null, null, null);
                if (c.moveToFirst()) {
                    try {
                        String number = c.getString(c
                            .getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        String name = c.getString(c
                            .getColumnIndexOrThrow(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME));
                        mActionView.setText("tel://" + number);
                        if (mCaptionView.getText().equals(""))
                            mCaptionView.setText(getString(R.string.caption_call, new Object[] { name }));
                    } catch (IllegalArgumentException e) {
                        L.bug("Could not get phone number from list.", e);
                    }
                }
            }
            break;
        case GET_LOCATION:
            if (resultCode == Activity.RESULT_OK) {
                mActionView.setText("geo://" + data.getDoubleExtra("latitude", 0) + ","
                    + data.getDoubleExtra("longitude", 0));
            }
            break;
        case PICK_IMAGE:
            if (resultCode == Activity.RESULT_OK) {
                if (mUriSavedFile == null) {
                    final ViewFlipper vf = (ViewFlipper) findViewById(R.id.viewFlipper);
                    setupUploadFile("png", false);
                    mWiz.proceedToPosition(vf.indexOfChild(findViewById(R.id.view_flipper_image)));
                }
                if (data != null && data.getData() != null) {
                    final Uri selectedImage = data.getData();
                    L.d("selectedImage: " + selectedImage.toString());
                    copyImageFile(selectedImage);
                } else {
                    mWiz.setUploadFileExtenstion(AttachmentViewerActivity.CONTENT_TYPE_PNG);
                    setPictureSelected();
                }
            }
            break;
        case PICK_VIDEO:
            if (resultCode == Activity.RESULT_OK) {
                if (mUriSavedFile == null) {
                    final ViewFlipper vf = (ViewFlipper) findViewById(R.id.viewFlipper);
                    setupUploadFile("mp4", false);
                    mWiz.proceedToPosition(vf.indexOfChild(findViewById(R.id.view_flipper_video)));
                }
                final ContentResolver cr = SendMessageWizardActivity.this.getContentResolver();
                mWiz.setUploadFileExtenstion(AttachmentViewerActivity.CONTENT_TYPE_VIDEO_MP4);
                if (data != null && data.getData() != null) {
                    final Uri selectedVideo = data.getData();
                    L.d("selectedVideo: " + selectedVideo.toString());
                    final String fileType = cr.getType(selectedVideo);
                    L.d("fileType: " + fileType);
                    if (fileType != null && !AttachmentViewerActivity.CONTENT_TYPE_VIDEO_MP4.equalsIgnoreCase(fileType)) {
                        L.bug("A video convert is needed for type: " + fileType);
                    }

                    if (!mUriSavedFile.toString().equalsIgnoreCase(selectedVideo.toString())) {
                        copyVideoFile(cr, selectedVideo);
                    } else {
                        setVideoSelected();
                    }

                } else {
                    setVideoSelected();
                }
            }
            break;
        }
    }

    private void copyVideoFile(final ContentResolver cr, final Uri selectedVideo) {
        final ProgressDialog progressDialog = new ProgressDialog(SendMessageWizardActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage(SendMessageWizardActivity.this.getString(R.string.processing));
        progressDialog.setCancelable(false);
        progressDialog.show();

        new SafeAsyncTask<Object, Object, Boolean>() {
            @Override
            protected Boolean safeDoInBackground(Object... params) {
                L.d("Processing video: " + selectedVideo.toString());
                try {
                    File tmpUploadFile = mWiz.getTmpUploadFile();
                    if (tmpUploadFile.getAbsolutePath().equals(selectedVideo.getPath())) {
                        return true;
                    } else {
                        InputStream is = cr.openInputStream(selectedVideo);
                        if (is != null) {
                            try {
                                OutputStream out = new FileOutputStream(tmpUploadFile);
                                try {
                                    IOUtils.copy(is, out, 1024);
                                } finally {
                                    out.close();
                                }
                            } finally {
                                is.close();
                            }
                            return true;
                        }
                    }
                } catch (FileNotFoundException e) {
                    L.d(e);
                } catch (Exception e) {
                    L.bug("Unknown exception occured while processing video: " + selectedVideo.toString(), e);
                }

                return false;
            };

            @Override
            protected void safeOnPostExecute(Boolean result) {
                progressDialog.dismiss();
                if (result) {
                    setVideoSelected();
                } else {
                    UIUtils.showLongToast(SendMessageWizardActivity.this, getString(R.string.error_please_try_again));
                }
            }

            @Override
            protected void safeOnCancelled(Boolean result) {
            }

            @Override
            protected void safeOnProgressUpdate(Object... values) {
            }

            @Override
            protected void safeOnPreExecute() {
            };
        }.execute();
    }

    private void copyImageFile(final Uri selectedImage) {
        final ContentResolver cr = SendMessageWizardActivity.this.getContentResolver();
        final ProgressDialog progressDialog = new ProgressDialog(SendMessageWizardActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage(SendMessageWizardActivity.this.getString(R.string.processing));
        progressDialog.setCancelable(false);
        progressDialog.show();

        new SafeAsyncTask<Object, Object, Boolean>() {
            @Override
            protected Boolean safeDoInBackground(Object... params) {
                L.d("Processing picture: " + selectedImage.toString());

                try {
                    String fileType = cr.getType(selectedImage);
                    L.d("fileType: " + fileType);
                    if (fileType == null || AttachmentViewerActivity.CONTENT_TYPE_PNG.equalsIgnoreCase(fileType)) {
                        mWiz.setUploadFileExtenstion(AttachmentViewerActivity.CONTENT_TYPE_PNG);
                    } else {
                        mWiz.setUploadFileExtenstion(AttachmentViewerActivity.CONTENT_TYPE_JPEG);
                    }

                    File tmpUploadFile = mWiz.getTmpUploadFile();
                    if (tmpUploadFile.getAbsolutePath().equals(selectedImage.getPath())) {
                        return true;
                    } else {
                        InputStream is = cr.openInputStream(selectedImage);
                        if (is != null) {
                            try {
                                OutputStream out = new FileOutputStream(tmpUploadFile);
                                try {
                                    IOUtils.copy(is, out, 1024);
                                } finally {
                                    out.close();
                                }
                            } finally {
                                is.close();
                            }
                            return true;
                        }
                    }

                } catch (FileNotFoundException e) {
                    L.d(e);
                } catch (Exception e) {
                    L.bug("Unknown exception occured while processing picture: " + selectedImage.toString(), e);
                }

                return false;
            };

            @Override
            protected void safeOnPostExecute(Boolean result) {
                progressDialog.dismiss();
                if (result) {
                    setPictureSelected();
                } else {
                    UIUtils.showLongToast(SendMessageWizardActivity.this, getString(R.string.error_please_try_again));
                }
            }

            @Override
            protected void safeOnCancelled(Boolean result) {
            }

            @Override
            protected void safeOnProgressUpdate(Object... values) {
            }

            @Override
            protected void safeOnPreExecute() {
            };
        }.execute();
    }

    private void setPictureSelected() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;
        if (!new File(mUriSavedFile.getPath()).exists()) {
            UIUtils.showLongToast(SendMessageWizardActivity.this, getString(R.string.error_please_try_again));
            return;
        }

        Bitmap bitmap = ImageHelper.getBitmapFromFile(mUriSavedFile.getPath(), options);

        Drawable d = new BitmapDrawable(Resources.getSystem(), bitmap);
        ImageView imagePreview = (ImageView) findViewById(R.id.message_image);
        imagePreview.setImageDrawable(d);
        imagePreview.setVisibility(View.VISIBLE);

        mWiz.setHasImageSelected(true);
        updateAddImageBtnText();
        initImageButtonsNavigation(mWiz, mWiz.getPosition());
    }

    private void setVideoSelected() {
        if (!new File(mUriSavedFile.getPath()).exists()) {
            UIUtils.showLongToast(SendMessageWizardActivity.this, getString(R.string.error_please_try_again));
            return;
        }
        Bitmap bitmap = UIUtils.createVideoThumbnail(SendMessageWizardActivity.this, mUriSavedFile.getPath(),
            UIUtils.convertDipToPixels(SendMessageWizardActivity.this, 200));
        Drawable d = new BitmapDrawable(Resources.getSystem(), bitmap);
        ImageView imagePreview = (ImageView) findViewById(R.id.message_video);
        imagePreview.setImageDrawable(d);
        imagePreview.setVisibility(View.VISIBLE);

        mWiz.setHasVideoSelected(true);
        updateAddVideoBtnText();
        initImageButtonsNavigation(mWiz, mWiz.getPosition());
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mWiz != null
            && (mWiz.getRecipients().size() > 0 || mWiz.getGroupRecipients().size() > 0 || mParentKey != null)) {
            final EditText message = (EditText) findViewById(R.id.view_flipper_message);
            mWiz.setMessage(message.getText().toString());
            mWiz.save();
        }
        View v = findViewById(R.id.viewFlipper);
        if (v != null)
            UIUtils.hideKeyboard(SendMessageWizardActivity.this, v);

        if (mCursorFriends != null) {
            stopManagingCursor(mCursorFriends);
        }
        if (mCursorGroups != null) {
            stopManagingCursor(mCursorGroups);
        }
    }

    @Override
    protected  void onResume() {
        super.onResume();
        if (getWasPaused() && mCursorFriends != null) {
            startManagingCursor(mCursorFriends);
        }
        if (getWasPaused() && mCursorGroups != null) {
            startManagingCursor(mCursorGroups);
        }
    }

    private void initActivity(String message) {
        T.UI();
        Intent intent = getIntent();
        mParentKey = intent.hasExtra(PARENT_KEY) ? intent.getStringExtra(PARENT_KEY) : null;
        mParentFlags = intent.getLongExtra(FLAGS, 0);
        mRepliedToKey = intent.hasExtra(REPLIED_TO_KEY) ? intent.getStringExtra(REPLIED_TO_KEY) : null;

        mWiz = SendMessageWizard.getWizard(this, mService.getConfigurationProvider(), message, mSaveOnlyMode);
        mWiz.setNextButton((Button) findViewById(R.id.nextButton));
        mWiz.setFlipper((ViewFlipper) findViewById(R.id.viewFlipper));
        mWiz.setTitleBar((TextView) findViewById(R.id.title_bar));
        addSelectRecipientsPageHandler(mWiz);
        addEnterMessagePageHandler(mWiz);
        addButtonsPageHandler(mWiz);
        addImagePageHandler(mWiz);
        addVideoPageHandler(mWiz);
        setOnFinishHandler(mWiz);
        if (mRestorePosition == -1) {
            if (mParentKey != null || mFriendRecipients != null || mGroupRecipients != null) {
                mWiz.setPosition(1);
            } else {
                mWiz.setPosition(0);
            }
        } else {
            mWiz.setPosition(mRestorePosition);
        }
        if (intent.hasExtra(DEFAULT_PRIORITY)) {
            mWiz.setPriority(intent.getLongExtra(DEFAULT_PRIORITY, mWiz.getPriority()));
        }

        if (intent.hasExtra(DEFAULT_STICKY)) {
            mWiz.setIsSticky(intent.getBooleanExtra(DEFAULT_STICKY, mWiz.getIsSticky()));
        }
        mWiz.run();

        if (mInitialText != null)
            mWiz.setMessage(mInitialText);

        initMessage(mWiz);
        mRecipients = (LinearLayout) findViewById(R.id.recipients);

        mRecipients.removeAllViews();
        final FriendStore friendStore = mFriendsPlugin.getStore();
        Set<String> groupRecipientsToRemove = null;
        Set<String> recipientsToRemove = null;

        for (String groupGuid : mWiz.getGroupRecipients()) {
            Group group = friendStore.getGroup(groupGuid);
            if (group != null) {
                if (mGroupRecipients != null) {
                    if (mGroupRecipients.remove(groupGuid)) {
                        addGroupRecipient(mWiz, mRecipients, group);
                    } else {
                        if (groupRecipientsToRemove == null)
                            groupRecipientsToRemove = new HashSet<String>(1);
                        groupRecipientsToRemove.add(groupGuid);
                    }

                } else {
                    if (mFriendRecipients == null) {
                        addGroupRecipient(mWiz, mRecipients, group);
                    } else {
                        if (groupRecipientsToRemove == null)
                            groupRecipientsToRemove = new HashSet<String>(1);
                        groupRecipientsToRemove.add(groupGuid);
                    }
                }
            } else {
                if (groupRecipientsToRemove == null)
                    groupRecipientsToRemove = new HashSet<String>(1);
                groupRecipientsToRemove.add(groupGuid);
            }
        }
        if (mGroupRecipients != null) {
            for (String groupGuid : mGroupRecipients) {
                Group group = friendStore.getGroup(groupGuid);
                if (group != null) {
                    mWiz.getGroupRecipients().add(groupGuid);
                    addGroupRecipient(mWiz, mRecipients, group);
                }
            }
        }

        for (String friendEmail : mWiz.getRecipients()) {
            Friend recipient = friendStore.getExistingFriend(friendEmail);
            if (recipient != null) {
                if (mFriendRecipients != null) {
                    if (mFriendRecipients.remove(friendEmail)) {
                        addRecipient(mWiz, mRecipients, recipient);
                    } else {
                        if (recipientsToRemove == null)
                            recipientsToRemove = new HashSet<String>(1);
                        recipientsToRemove.add(friendEmail);
                    }
                } else {
                    if (mGroupRecipients == null) {
                        addRecipient(mWiz, mRecipients, recipient);
                    } else {
                        if (recipientsToRemove == null)
                            recipientsToRemove = new HashSet<String>(1);
                        recipientsToRemove.add(friendEmail);
                    }
                }
            } else {
                if (recipientsToRemove == null)
                    recipientsToRemove = new HashSet<String>(1);
                recipientsToRemove.add(friendEmail);
            }
        }
        if (mFriendRecipients != null) {
            for (String email : mFriendRecipients) {
                Friend recipient = friendStore.getExistingFriend(email);
                if (recipient != null) {
                    mWiz.getRecipients().add(email);
                    addRecipient(mWiz, mRecipients, recipient);
                }
            }
        }
        if (recipientsToRemove != null || mFriendRecipients != null || groupRecipientsToRemove != null
            || mGroupRecipients != null) {
            if (groupRecipientsToRemove != null)
                mWiz.getGroupRecipients().removeAll(groupRecipientsToRemove);
            if (recipientsToRemove != null)
                mWiz.getRecipients().removeAll(recipientsToRemove);
            mWiz.save();
        }

        initListView(mWiz, mRecipients, friendStore);
        initButtonsList(mWiz);
        initImageButtonsNavigation(mWiz, mWiz.getPosition());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mRestorePosition = savedInstanceState.getInt("position");
    }

    private void setImageButtonNavigation(int position) {
        final LinearLayout optionButtons = (LinearLayout) findViewById(R.id.imageButtons);
        final Button nextButton = (Button) findViewById(R.id.nextButton);

        if (position == 0) {
            optionButtons.setVisibility(View.GONE);
            nextButton.setVisibility(View.VISIBLE);
        } else {
            optionButtons.setVisibility(View.VISIBLE);
            nextButton.setVisibility(View.GONE);

            initImageButtonsNavigation(mWiz, position);
        }
    }

    private int getImageResourceForKey(final SendMessageWizard wiz, final int key) {
        if (IMAGE_BUTTON_SEND_OR_SAVE == key) {
            if (mSaveOnlyMode) {
                return R.drawable.fa_floppy_o;
            } else {
                return android.R.drawable.ic_menu_send;
            }
        } else if (IMAGE_BUTTON_TEXT == key) {
            return R.drawable.fa_font;
        } else if (IMAGE_BUTTON_BUTTONS == key) {
            return R.drawable.fa_list;
        } else if (IMAGE_BUTTON_PICTURE == key) {
            return R.drawable.fa_camera;
        } else if (IMAGE_BUTTON_VIDEO == key) {
            return R.drawable.fa_video_camera;
        } else if (IMAGE_BUTTON_PRIORITY == key) {
            if (wiz.getPriority() == Message.PRIORITY_HIGH) {
                return R.drawable.fa_priority_2;
            } else if (wiz.getPriority() == Message.PRIORITY_URGENT) {
                return R.drawable.fa_priority_3;
            } else if (wiz.getPriority() == Message.PRIORITY_URGENT_WITH_ALARM) {
                return R.drawable.fa_priority_4;
            } else {
                return R.drawable.fa_priority_1;
            }
        } else if (IMAGE_BUTTON_STICKY == key) {
            if (wiz.getIsSticky()) {
                return R.drawable.fa_sticky_1;
            } else {
                return R.drawable.fa_sticky_0;
            }
        } else if (IMAGE_BUTTON_MORE == key) {
            return R.drawable.fa_ellipsis_h;
        } else if (IMAGE_BUTTON_PADDING == key) {
            return 0;
        } else {
            L.d("Could not find image resourse for key: " + key);
            return 0;
        }
    }

    private void processOnClickListenerForKey(final SendMessageWizard wiz, final ViewFlipper vf,
        final EditText message, final int key) {
        if (IMAGE_BUTTON_SEND_OR_SAVE == key) {
            wiz.setMessage(message.getText().toString());
            wiz.finish();
        } else if (IMAGE_BUTTON_TEXT == key) {
            wiz.setMessage(message.getText().toString());
            wiz.proceedToPosition(vf.indexOfChild(findViewById(R.id.view_flipper_message)));
        } else if (IMAGE_BUTTON_BUTTONS == key) {
            wiz.setMessage(message.getText().toString());
            wiz.proceedToPosition(vf.indexOfChild(findViewById(R.id.view_flipper_buttons)));
        } else if (IMAGE_BUTTON_PICTURE == key) {
            wiz.setMessage(message.getText().toString());
            wiz.proceedToPosition(vf.indexOfChild(findViewById(R.id.view_flipper_image)));
        } else if (IMAGE_BUTTON_VIDEO == key) {
            wiz.setMessage(message.getText().toString());
            wiz.proceedToPosition(vf.indexOfChild(findViewById(R.id.view_flipper_video)));
        } else if (IMAGE_BUTTON_PRIORITY == key) {
            UIUtils.hideKeyboard(SendMessageWizardActivity.this, vf);
            final Dialog dialog = new Dialog(SendMessageWizardActivity.this);
            dialog.setContentView(R.layout.msg_priority_picker);
            dialog.setTitle(R.string.priority);
            Button savePriorityBtn = (Button) dialog.findViewById(R.id.ok);
            final RadioButton priorityNormalBtn = ((RadioButton) dialog.findViewById(R.id.priority_normal));
            final RadioButton priorityHighBtn = ((RadioButton) dialog.findViewById(R.id.priority_high));
            final RadioButton priorityUrgentBtn = ((RadioButton) dialog.findViewById(R.id.priority_urgent));
            final RadioButton priorityUrgentWithAlarmBtn = ((RadioButton) dialog
                .findViewById(R.id.priority_urgent_with_alarm));

            savePriorityBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (priorityHighBtn.isChecked()) {
                        wiz.setPriority(Message.PRIORITY_HIGH);
                    } else if (priorityUrgentBtn.isChecked()) {
                        wiz.setPriority(Message.PRIORITY_URGENT);
                    } else if (priorityUrgentWithAlarmBtn.isChecked()) {
                        wiz.setPriority(Message.PRIORITY_URGENT_WITH_ALARM);
                    } else {
                        wiz.setPriority(Message.PRIORITY_NORMAL);
                    }

                    dialog.dismiss();
                    initImageButtonsNavigation(wiz, wiz.getPosition());
                }
            });

            priorityNormalBtn.setChecked(false);
            priorityHighBtn.setChecked(false);
            priorityUrgentBtn.setChecked(false);
            priorityUrgentWithAlarmBtn.setChecked(false);

            if (wiz.getPriority() == Message.PRIORITY_HIGH) {
                priorityHighBtn.setChecked(true);
            } else if (wiz.getPriority() == Message.PRIORITY_URGENT) {
                priorityUrgentBtn.setChecked(true);
            } else if (wiz.getPriority() == Message.PRIORITY_URGENT_WITH_ALARM) {
                priorityUrgentWithAlarmBtn.setChecked(true);
            } else {
                priorityNormalBtn.setChecked(true);
            }

            dialog.show();
        } else if (IMAGE_BUTTON_STICKY == key) {
            UIUtils.hideKeyboard(SendMessageWizardActivity.this, vf);

            final Dialog dialog = new Dialog(SendMessageWizardActivity.this);
            dialog.setContentView(R.layout.msg_sticky_picker);
            dialog.setTitle(R.string.sticky);
            Button savePriorityBtn = (Button) dialog.findViewById(R.id.ok);
            final RadioButton stickyDisabled = ((RadioButton) dialog.findViewById(R.id.sticky_disabled));
            final RadioButton stickyEnabled = ((RadioButton) dialog.findViewById(R.id.sticky_enabled));

            savePriorityBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (stickyEnabled.isChecked()) {
                        wiz.setIsSticky(true);
                    } else {
                        wiz.setIsSticky(false);
                    }

                    dialog.dismiss();
                    initImageButtonsNavigation(wiz, wiz.getPosition());
                }
            });

            stickyDisabled.setChecked(false);
            stickyEnabled.setChecked(false);

            if (wiz.getIsSticky()) {
                stickyEnabled.setChecked(true);
            } else {
                stickyDisabled.setChecked(true);
            }

            dialog.show();
        } else if (IMAGE_BUTTON_MORE == key) {
            UIUtils.hideKeyboard(SendMessageWizardActivity.this, vf);

            final Dialog dialog = new Dialog(SendMessageWizardActivity.this);
            dialog.setContentView(R.layout.msg_more_picker);
            dialog.setTitle(R.string.more);
            final ListView pickMsgMore = (ListView) dialog.findViewById(R.id.pick_msg_more);

            pickMsgMore.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
                    dialog.dismiss();
                    PickMoreItem item = (PickMoreItem) view.getTag();
                    processOnClickListenerForKey(wiz, vf, message, item.imageButtonKey);
                }
            });

            List<PickMoreItem> items = new ArrayList<SendMessageWizardActivity.PickMoreItem>();
            for (int i = mMaxImageButtonsOnScreen - 2; i < mImageButtons.size(); i++) {
                int k = mImageButtons.get(i);
                String t = "";
                if (k == IMAGE_BUTTON_TEXT) {
                    t = getString(R.string.title_message);
                } else if (k == IMAGE_BUTTON_BUTTONS) {
                    t = getString(R.string.title_buttons);
                } else if (k == IMAGE_BUTTON_PICTURE) {
                    t = getString(R.string.title_new_message_image);
                } else if (k == IMAGE_BUTTON_VIDEO) {
                    t = getString(R.string.title_new_message_video);
                } else if (k == IMAGE_BUTTON_PRIORITY) {
                    t = getString(R.string.priority);
                } else if (k == IMAGE_BUTTON_STICKY) {
                    t = getString(R.string.sticky);
                } else {
                    L.d("Could not find more text for key: " + key);
                }

                items.add(new PickMoreItem(k, getImageResourceForKey(wiz, k), t));
            }
            pickMsgMore.setAdapter(new ListAdapter(SendMessageWizardActivity.this, items.toArray(new PickMoreItem[items
                .size()])));

            Button closeMoreBtn = (Button) dialog.findViewById(R.id.close);
            closeMoreBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            dialog.show();
        } else {
            L.d("Could not find processOnClickListener for key: " + key);
        }
    }

    private ImageView generateImageView(final SendMessageWizard wiz, final ViewFlipper vf, final EditText message,
        final int imageButton, final int visible, final int position) {
        ImageView iv = new ImageView(SendMessageWizardActivity.this);
        iv.setVisibility(visible);
        final int imageResourse = getImageResourceForKey(wiz, imageButton);
        if (imageResourse != 0)
            iv.setImageResource(imageResourse);
        iv.setOnClickListener(new SafeViewOnClickListener() {
            @Override
            public void safeOnClick(View v) {
                processOnClickListenerForKey(wiz, vf, message, imageButton);
            }
        });

        if (IMAGE_BUTTON_SEND_OR_SAVE == imageButton) {
            if (!mSaveOnlyMode) {
                iv.setColorFilter(UIUtils.imageColorFilter(getResources().getColor(R.color.mc_blue2)));
            }
        } else if (IMAGE_BUTTON_TEXT == imageButton) {
            if (position == 1)
                iv.setColorFilter(UIUtils.imageColorFilter(getResources().getColor(R.color.mc_divider_gray)));
        } else if (IMAGE_BUTTON_BUTTONS == imageButton) {
            if (position == 2)
                iv.setColorFilter(UIUtils.imageColorFilter(getResources().getColor(R.color.mc_divider_gray)));
        } else if (IMAGE_BUTTON_PICTURE == imageButton) {
            final Button addMessageImageButton = (Button) findViewById(R.id.add_message_image);
            addMessageImageButton.setOnClickListener(new SafeViewOnClickListener() {
                @Override
                public void safeOnClick(View v) {
                    if (wiz.getHasImageSelected()) {
                        ImageView imagePreview = (ImageView) findViewById(R.id.message_image);
                        imagePreview.setVisibility(View.GONE);
                        wiz.setHasImageSelected(false);
                        updateAddImageBtnText();
                        initImageButtonsNavigation(wiz, wiz.getPosition());
                    } else {
                        getNewPicture();
                    }
                }
            });
            if (position == 3)
                iv.setColorFilter(UIUtils.imageColorFilter(getResources().getColor(R.color.mc_divider_gray)));
        } else if (IMAGE_BUTTON_VIDEO == imageButton) {
            final Button addMessageVideoButton = (Button) findViewById(R.id.add_message_video);
            addMessageVideoButton.setOnClickListener(new SafeViewOnClickListener() {
                @Override
                public void safeOnClick(View v) {
                    if (wiz.getHasVideoSelected()) {
                        ImageView videoPreview = (ImageView) findViewById(R.id.message_video);
                        videoPreview.setVisibility(View.GONE);
                        wiz.setHasVideoSelected(false);
                        updateAddVideoBtnText();
                        initImageButtonsNavigation(wiz, wiz.getPosition());
                    } else {
                        getNewVideo();
                    }
                }
            });
            if (position == 4)
                iv.setColorFilter(UIUtils.imageColorFilter(getResources().getColor(R.color.mc_divider_gray)));
        } else if (IMAGE_BUTTON_PRIORITY == imageButton) {
        } else if (IMAGE_BUTTON_STICKY == imageButton) {
        } else if (IMAGE_BUTTON_MORE == imageButton) {
        } else {
        }

        iv.setPadding(_5_DP_IN_PX, _5_DP_IN_PX, _5_DP_IN_PX, _5_DP_IN_PX);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(_30_DP_IN_PX, _30_DP_IN_PX, 1.0f);
        iv.setLayoutParams(lp);
        return iv;
    }

    private class PickMoreItem {
        int imageButtonKey;
        int iv;
        String title;

        public PickMoreItem(int imageButtonKey, int iv, String title) {
            this.imageButtonKey = imageButtonKey;
            this.iv = iv;
            this.title = title;
        }
    }

    private class ListAdapter extends ArrayAdapter<PickMoreItem> {

        private PickMoreItem[] values;

        public ListAdapter(Context context, PickMoreItem[] values) {
            super(context, -1, values);
            this.values = values;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final PickMoreItem item = this.values[position];
            final View itemView;
            if (convertView != null && convertView instanceof RelativeLayout)
                itemView = convertView;
            else
                itemView = getLayoutInflater().inflate(R.layout.more_item, null);

            ImageView itemAvatar = (ImageView) itemView.findViewById(R.id.item_avatar);
            itemAvatar.setImageResource(item.iv);

            TextView itemName = (TextView) itemView.findViewById(R.id.item_name);
            itemName.setText(item.title);
            itemName.setTextColor(getResources().getColor(android.R.color.primary_text_light));
            itemView.setTag(item);
            return itemView;
        }

    }

    private void initImageButtonsNavigation(final SendMessageWizard wiz, final int position) {
        final ViewFlipper vf = (ViewFlipper) findViewById(R.id.viewFlipper);
        final EditText message = (EditText) findViewById(R.id.view_flipper_message);
        final LinearLayout optionButtons = (LinearLayout) findViewById(R.id.imageButtons);
        optionButtons.removeAllViews();

        boolean addPicture = true;
        boolean addVideo = true;
        boolean addButtons = true;
        boolean addPriority = false;
        boolean addSticky = false;
        if (SystemUtils.isFlagEnabled(mParentFlags, MessagingPlugin.FLAG_DYNAMIC_CHAT)) {
            if (!SystemUtils.isFlagEnabled(mParentFlags, MessagingPlugin.FLAG_ALLOW_CHAT_PICTURE)) {
                addPicture = false;
            }
            if (!SystemUtils.isFlagEnabled(mParentFlags, MessagingPlugin.FLAG_ALLOW_CHAT_VIDEO)) {
                addVideo = false;
            }
            if (!SystemUtils.isFlagEnabled(mParentFlags, MessagingPlugin.FLAG_ALLOW_CHAT_BUTTONS)) {
                addButtons = false;
            }
            if (SystemUtils.isFlagEnabled(mParentFlags, MessagingPlugin.FLAG_ALLOW_CHAT_PRIORITY)) {
                addPriority = true;
            }
            if (SystemUtils.isFlagEnabled(mParentFlags, MessagingPlugin.FLAG_ALLOW_CHAT_STICKY)) {
                addSticky = true;
            }
        }
        mImageButtons = new ArrayList<Integer>();
        mImageButtons.add(IMAGE_BUTTON_TEXT);

        if (addButtons) {
            mImageButtons.add(IMAGE_BUTTON_BUTTONS);
        }

        if (wiz.getHasImageSelected()) {
            mImageButtons.add(IMAGE_BUTTON_PICTURE);
        } else if (wiz.getHasVideoSelected()) {
            mImageButtons.add(IMAGE_BUTTON_VIDEO);
        } else {
            if (addPicture) {
                mImageButtons.add(IMAGE_BUTTON_PICTURE);
            }
            if (addVideo) {
                mImageButtons.add(IMAGE_BUTTON_VIDEO);
            }
        }

        if (addPriority) {
            mImageButtons.add(IMAGE_BUTTON_PRIORITY);
        }

        if (addSticky) {
            mImageButtons.add(IMAGE_BUTTON_STICKY);
        }

        if (mImageButtons.size() > mMaxImageButtonsOnScreen - 1) {
            for (int i = 0; i < mMaxImageButtonsOnScreen - 2; i++) {
                int key = mImageButtons.get(i);
                ImageView iv = generateImageView(wiz, vf, message, key, View.VISIBLE, position);
                optionButtons.addView(iv);
            }
            ImageView iv = generateImageView(wiz, vf, message, IMAGE_BUTTON_MORE, View.VISIBLE, position);
            optionButtons.addView(iv);
            ImageView iv2 = generateImageView(wiz, vf, message, IMAGE_BUTTON_SEND_OR_SAVE, View.VISIBLE, position);
            optionButtons.addView(iv2);
        } else {
            for (int i = 0; i < mImageButtons.size(); i++) {
                int key = mImageButtons.get(i);
                ImageView iv = generateImageView(wiz, vf, message, key, View.VISIBLE, position);
                optionButtons.addView(iv);
            }
            if (mImageButtons.size() < mMaxImageButtonsOnScreen - 1) {
                for (int i = mMaxImageButtonsOnScreen - 1; i > mImageButtons.size(); i--) {
                    L.i("Adding space");
                    ImageView iv = generateImageView(wiz, vf, message, IMAGE_BUTTON_PADDING, View.INVISIBLE, position);
                    optionButtons.addView(iv);
                }
            }
            ImageView iv = generateImageView(wiz, vf, message, IMAGE_BUTTON_SEND_OR_SAVE, View.VISIBLE, position);
            optionButtons.addView(iv);
        }
    }

    private void initButtonsList(final SendMessageWizard wiz) {
        final LinearLayout buttons = (LinearLayout) findViewById(R.id.buttons);
        buttons.removeAllViews();
        for (Long buttonId : wiz.getButtons()) {
            CannedButton button = wiz.getCannedButtons().getById(buttonId);
            if (button == null)
                continue;
            button.setSelected(true);
            Button b = addButton(buttons, button, wiz);
            if (wiz.getSelectedButton() == button.getId())
                selectButton(b);
        }
        mButtonsListView = (ListView) findViewById(R.id.button_list);
        final CannedButtonAdapter cannedButtonAdapter = new CannedButtonAdapter(wiz.getCannedButtons());
        mButtonsListView.setAdapter(cannedButtonAdapter);
        mButtonsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
                try {
                    final CannedButton cannedButton = (CannedButton) view.getTag();
                    cannedButton.setSelected(!cannedButton.isSelected());
                    setButtonItemView(view, cannedButton);
                    if (cannedButton.isSelected()) {
                        addButton(buttons, cannedButton, wiz);
                        wiz.getButtons().add(cannedButton.getId());
                    } else {
                        removeSelectedButton(wiz, buttons, cannedButton);
                    }
                    wiz.save();
                } catch (Exception e) {
                    L.bug(e);
                }
            }
        });
        mButtonsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            private boolean handled = false;

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    final CannedButton cannedButton = (CannedButton) view.getTag();
                    new AlertDialog.Builder(SendMessageWizardActivity.this)
                            .setMessage(getString(R.string.remove_canned_button, cannedButton.getCaption()))
                            .setPositiveButton(R.string.yes, new SafeDialogInterfaceOnClickListener() {
                                @Override
                                public void safeOnClick(DialogInterface dialog, int which) {
                                    handled = false;
                                    wiz.getCannedButtons().remove(cannedButton);
                                    if (cannedButton.isSelected()) {
                                        removeSelectedButton(wiz, buttons, cannedButton);
                                    }
                                    wiz.save();
                                    cannedButtonAdapter.notifyDataSetChanged();
                                }
                            }).setNegativeButton(R.string.no, new SafeDialogInterfaceOnClickListener() {
                        @Override
                        public void safeOnClick(DialogInterface dialog, int which) {
                            handled = true;
                        }
                    }).create().show();
                    return handled;
                } catch (Exception e) {
                    L.bug(e);
                    return false;
                }
            }
        });
        Button addButton = (Button) findViewById(R.id.add_button);
        addButton.setOnClickListener(new SafeViewOnClickListener() {
            @Override
            public void safeOnClick(View v) {
                final View dialog = getLayoutInflater().inflate(R.layout.new_button_dialog, null);
                mCaptionView = (EditText) dialog.findViewById(R.id.button_caption);
                mActionView = (EditText) dialog.findViewById(R.id.button_action);
                final Button actionHelp = (Button) dialog.findViewById(R.id.action_help_button);
                final RadioButton telRadio = (RadioButton) dialog.findViewById(R.id.action_tel);
                final RadioButton geoRadio = (RadioButton) dialog.findViewById(R.id.action_geo);
                final RadioButton wwwRadio = (RadioButton) dialog.findViewById(R.id.action_www);
                telRadio.setChecked(true);
                telRadio.setOnClickListener(new SafeViewOnClickListener() {
                    @Override
                    public void safeOnClick(View v) {
                        mActionView.setText("tel://");
                        actionHelp.setEnabled(true);
                    }
                });
                geoRadio.setOnClickListener(new SafeViewOnClickListener() {
                    @Override
                    public void safeOnClick(View v) {
                        mActionView.setText("geo://");
                        actionHelp.setEnabled(true);
                    }
                });
                wwwRadio.setOnClickListener(new SafeViewOnClickListener() {
                    @Override
                    public void safeOnClick(View v) {
                        mActionView.setText("http://");
                        actionHelp.setEnabled(false);
                    }
                });
                actionHelp.setOnClickListener(new SafeViewOnClickListener() {
                    @Override
                    public void safeOnClick(View v) {
                        if (telRadio.isChecked()) {
                            Intent intent = new Intent(Intent.ACTION_PICK,
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                            startActivityForResult(intent, PICK_CONTACT);
                        } else if (geoRadio.isChecked()) {
                            Intent intent = new Intent(SendMessageWizardActivity.this, GetLocationActivity.class);
                            startActivityForResult(intent, GET_LOCATION);
                        }
                    }
                });
                AlertDialog alertDialog = new AlertDialog.Builder(SendMessageWizardActivity.this)
                        .setTitle(R.string.create_button_title).setView(dialog)
                        .setPositiveButton(getString(R.string.ok), new SafeDialogInterfaceOnClickListener() {
                            @Override
                            public void safeOnClick(DialogInterface di, int which) {
                                Matcher action = actionPattern.matcher(mActionView.getText());
                                if (!action.matches()) {
                                    UIUtils.showLongToast(SendMessageWizardActivity.this,
                                            getString(R.string.action_not_valid));
                                    return;
                                }
                                String caption = mCaptionView.getText().toString();
                                if ("".equals(caption.trim())) {
                                    UIUtils.showLongToast(SendMessageWizardActivity.this,
                                            getString(R.string.caption_required));
                                    return;
                                }
                                CannedButton cannedButton = new CannedButton(caption, "".equals(action.group(2)) ? null
                                        : action.group());
                                wiz.getCannedButtons().add(cannedButton);
                                cannedButton.setSelected(true);
                                cannedButtonAdapter.notifyDataSetChanged();
                                addButton(buttons, cannedButton, wiz);
                                wiz.getButtons().add(cannedButton.getId());
                                wiz.save();
                            }
                        }).setNegativeButton(getString(R.string.cancel), new SafeDialogInterfaceOnClickListener() {
                            @Override
                            public void safeOnClick(DialogInterface dialog, int which) {
                            }
                        }).create();
                alertDialog.setCanceledOnTouchOutside(true);
                alertDialog.show();

            }
        });
    }

    private void initListView(final SendMessageWizard wiz, final LinearLayout recipients, final FriendStore friendStore) {
        mListView = (ListView) findViewById(R.id.friends);

        mCursorFriends = friendStore.getUserFriendListCursor();
        startManagingCursor(mCursorFriends);

        FriendListAdapter fla = new FriendListAdapter(this, mCursorFriends, friendStore,
            new FriendListAdapter.ViewUpdater() {
                @Override
                public void update(View view) {
                    Friend friend = (Friend) view.getTag();
                    TextView name = (TextView) view.findViewById(R.id.friend_name);
                    boolean selected = wiz.getRecipients().contains(friend.email);
                    setViewColors(view, name, selected);
                }
            }, mFriendsPlugin, false, null);

        mCursorGroups = friendStore.getGroupListCursor();
        startManagingCursor(mCursorGroups);

        GroupListAdapter gla = new GroupListAdapter(this, mCursorGroups, friendStore,
            new GroupListAdapter.ViewUpdater() {
                @Override
                public void update(View view) {
                    Group group = (Group) view.getTag();
                    TextView name = (TextView) view.findViewById(R.id.group_name);
                    boolean selected = wiz.getGroupRecipients().contains(group.guid);
                    setViewColors(view, name, selected);
                }
            }, mFriendsPlugin, false, null);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
                try {
                    boolean selected = false;
                    if (view.getTag() instanceof Friend) {
                        final TextView name = (TextView) view.findViewById(R.id.friend_name);
                        final Friend friend = (Friend) view.getTag();
                        selected = wiz.getRecipients().contains(friend.email);
                        if (selected) {
                            wiz.getRecipients().remove(friend.email);
                            ImageView avatar = (ImageView) recipients.findViewWithTag(friend.email);
                            recipients.removeView(avatar);
                            wiz.save();
                        } else {
                            wiz.getRecipients().add(friend.email);
                            addRecipient(wiz, recipients, friend);
                            wiz.save();
                        }
                        setViewColors(view, name, !selected);
                    } else if (view.getTag() instanceof Group) {
                        final TextView name = (TextView) view.findViewById(R.id.group_name);
                        final Group group = (Group) view.getTag();
                        selected = wiz.getGroupRecipients().contains(group.guid);
                        if (selected) {
                            wiz.getGroupRecipients().remove(group.guid);
                            ImageView avatar = (ImageView) recipients.findViewWithTag(group.guid);
                            recipients.removeView(avatar);
                            wiz.save();
                        } else {
                            wiz.getGroupRecipients().add(group.guid);
                            addGroupRecipient(wiz, recipients, group);
                            wiz.save();
                        }
                        setViewColors(view, name, !selected);
                    }
                } catch (Exception e) {
                    L.bug(e);
                }
            }
        });

        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    if (view.getTag() instanceof Group) {
                        final Group group = (Group) view.getTag();
                        final Intent groupDetails = new Intent(SendMessageWizardActivity.this,
                            GroupDetailActivity.class);
                        groupDetails.putExtra(GroupDetailActivity.GUID, group.guid);
                        groupDetails.putExtra(GroupDetailActivity.NEW_GROUP, false);
                        startActivity(groupDetails);
                        return true;
                    }

                    return false;
                } catch (Exception e) {
                    L.bug(e);
                    return false;
                }
            }
        });

        SeparatedListAdapter adapter = new SeparatedListAdapter(this);

        adapter.addSection(mService.getString(R.string.groups), gla);
        final int text;
        switch (AppConstants.FRIENDS_CAPTION) {
        case COLLEAGUES:
            text = R.string.colleagues;
            break;
        case CONTACTS:
            text = R.string.contacts;
            break;
        case FRIENDS:
        default:
            text = R.string.tab_friends;
            break;
        }
        adapter.addSection(mService.getString(text), fla);

        mListView.setAdapter(adapter);
    }

    private void initMessage(final SendMessageWizard wiz) {
        final BackButtonOverriddenEditText message = (BackButtonOverriddenEditText) findViewById(R.id.view_flipper_message);
        message.setOnBackButtonPressed(new BackButtonOverriddenEditText.OnBackButtonPressedHandler() {
            @Override
            public boolean onBackButtonpressed() {
                if (mWiz != null) {
                    mWiz.goBack();
                    return true;
                }
                return false;
            }
        });
        message.setText(wiz.getMessage());
        message.setSelection(wiz.getMessage().length());
    }

    private void updateAddVideoBtnText() {
        final Button addMessageImageButton = (Button) findViewById(R.id.add_message_video);
        addMessageImageButton.setText(getString(mWiz.getHasVideoSelected() ? R.string.delete : R.string.add));
    }

    private void addVideoPageHandler(final SendMessageWizard wiz) {
        wiz.addPageHandler(new Wizard.PageHandler() {

            @Override
            public void pageDisplayed(Button back, Button next, ViewFlipper switcher) {
                setImageButtonNavigation(mWiz.getPosition());
                UIUtils.hideKeyboard(SendMessageWizardActivity.this, switcher);
                updateAddVideoBtnText();
            }

            @Override
            public String getTitle() {
                return getString(R.string.title_new_message_video);
            }

            @Override
            public boolean beforeNextClicked(Button back, Button next, ViewFlipper switcher) {
                return true;
            }

            @Override
            public boolean beforeBackClicked(Button back, Button next, ViewFlipper switcher) {
                int newPosition = mWiz.getPosition() - 1;

                if (SystemUtils.isFlagEnabled(mParentFlags, MessagingPlugin.FLAG_DYNAMIC_CHAT)) {
                    if (!SystemUtils.isFlagEnabled(mParentFlags, MessagingPlugin.FLAG_ALLOW_CHAT_PICTURE)
                            || mWiz.getHasVideoSelected()) {
                        newPosition -= 1;
                        if (!SystemUtils.isFlagEnabled(mParentFlags, MessagingPlugin.FLAG_ALLOW_CHAT_BUTTONS)) {
                            newPosition -= 1;
                        }
                    }
                } else {
                    if (mWiz.getHasVideoSelected()) {
                        newPosition -= 1;
                    }
                }

                setImageButtonNavigation(newPosition);
                mWiz.proceedToPosition(newPosition);
                return false;
            }
        });
    }

    private void updateAddImageBtnText() {
        final Button addMessageImageButton = (Button) findViewById(R.id.add_message_image);
        addMessageImageButton.setText(getString(mWiz.getHasImageSelected() ? R.string.delete : R.string.add));
    }

    private void addImagePageHandler(final SendMessageWizard wiz) {
        wiz.addPageHandler(new Wizard.PageHandler() {

            @Override
            public void pageDisplayed(Button back, Button next, ViewFlipper switcher) {
                setImageButtonNavigation(mWiz.getPosition());
                UIUtils.hideKeyboard(SendMessageWizardActivity.this, switcher);
                updateAddImageBtnText();
            }

            @Override
            public String getTitle() {
                return getString(R.string.title_new_message_image);
            }

            @Override
            public boolean beforeNextClicked(Button back, Button next, ViewFlipper switcher) {
                return true;
            }

            @Override
            public boolean beforeBackClicked(Button back, Button next, ViewFlipper switcher) {
                int newPosition = mWiz.getPosition() - 1;
                if (SystemUtils.isFlagEnabled(mParentFlags, MessagingPlugin.FLAG_DYNAMIC_CHAT)) {
                    if (!SystemUtils.isFlagEnabled(mParentFlags, MessagingPlugin.FLAG_ALLOW_CHAT_BUTTONS)) {
                        newPosition -= 1;
                    }
                }

                setImageButtonNavigation(newPosition);
                mWiz.proceedToPosition(newPosition);
                return false;
            }
        });
    }

    private void addButtonsPageHandler(final SendMessageWizard wiz) {
        wiz.addPageHandler(new Wizard.PageHandler() {

            @Override
            public void pageDisplayed(Button back, Button next, ViewFlipper switcher) {
                setImageButtonNavigation(mWiz.getPosition());
                UIUtils.hideKeyboard(SendMessageWizardActivity.this, switcher);
            }

            @Override
            public String getTitle() {
                return getString(R.string.title_buttons);
            }

            @Override
            public boolean beforeNextClicked(Button back, Button next, ViewFlipper switcher) {
                return true;
            }

            @Override
            public boolean beforeBackClicked(Button back, Button next, ViewFlipper switcher) {
                setImageButtonNavigation(mWiz.getPosition() - 1);
                return true;
            }
        });
    }

    private void addEnterMessagePageHandler(final SendMessageWizard wiz) {
        final EditText message = (EditText) findViewById(R.id.view_flipper_message);
        wiz.addPageHandler(new Wizard.PageHandler() {
            @Override
            public void pageDisplayed(Button back, Button next, ViewFlipper switcher) {
                setImageButtonNavigation(mWiz.getPosition());
                UIUtils.showKeyboard(SendMessageWizardActivity.this);

                message.requestFocus();
            }

            @Override
            public String getTitle() {
                return getString(R.string.title_message);
            }

            @Override
            public boolean beforeNextClicked(Button back, Button next, ViewFlipper switcher) {
                wiz.setMessage(message.getText().toString());
                UIUtils.hideKeyboard(SendMessageWizardActivity.this, switcher);
                wiz.finish();
                return false;
            }

            @Override
            public boolean beforeBackClicked(Button back, Button next, ViewFlipper switcher) {
                setImageButtonNavigation(mWiz.getPosition() - 1);
                wiz.setMessage(message.getText().toString());
                if (mParentKey == null)
                    return true;
                else {
                    UIUtils.hideKeyboard(SendMessageWizardActivity.this, switcher);
                    finish();
                    return false;
                }
            }
        });
    }

    private void addSelectRecipientsPageHandler(final SendMessageWizard wiz) {
        wiz.addPageHandler(new Wizard.PageHandler() {
            @Override
            public void pageDisplayed(Button back, Button next, ViewFlipper switcher) {
                UIUtils.hideKeyboard(SendMessageWizardActivity.this, switcher);
            }

            @Override
            public String getTitle() {
                return getString(R.string.title_recipients);
            }

            @Override
            public boolean beforeNextClicked(Button back, Button next, ViewFlipper switcher) {
                boolean recipients = wiz.getRecipients().size() > 0 || wiz.getGroupRecipients().size() > 0;
                if (!recipients)
                    UIUtils.showLongToast(SendMessageWizardActivity.this, getString(R.string.add_recipients));
                return recipients;
            }

            @Override
            public boolean beforeBackClicked(Button back, Button next, ViewFlipper switcher) {
                finish();
                if (mSaveOnlyMode)
                    mService.sendBroadcast(new Intent(CANNED_MESSAGE_CANCELED));
                return false;
            }
        });
    }

    private void setOnFinishHandler(final SendMessageWizard wiz) {
        final MainService mainService = mService;
        final boolean saveOnlyMode = mSaveOnlyMode;

        wiz.setOnFinish(new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                T.UI();
                if (saveOnlyMode) {
                    saveCannedMessage();
                    return;
                }

                if ("".equals(wiz.getMessage().trim()) && wiz.getButtons().size() == 0 && !wiz.getHasImageSelected()
                        && !wiz.getHasVideoSelected()) {
                    UIUtils.showLongToast(SendMessageWizardActivity.this,
                            getString(R.string.message_or_buttons_required));
                    return;
                }

                final String me = mainService.getIdentityStore().getIdentity().getEmail();
                sendMessage(wiz, mParentKey, mParentFlags, mTmpKey, me, mFriendsPlugin, mMessagingPlugin, mainService);

                mainService.sendBroadcast(new Intent(MessagingPlugin.NEW_MESSAGE_QUEUED_TO_BACKLOG_INTENT), true, true);
                if (mParentKey != null && !SystemUtils.isFlagEnabled(mParentFlags, MessagingPlugin.FLAG_DYNAMIC_CHAT))
                    mMessagingPlugin.ackThread(mParentKey);

                CannedButtons cannedButtons = wiz.getCannedButtons();
                Long[] buttonIds = wiz.getButtons().toArray(new Long[wiz.getButtons().size()]);
                for (Long buttonId : buttonIds) {
                    final CannedButton cannedButton = cannedButtons.getById(buttonId);
                    if (cannedButton != null) {
                        cannedButton.used();
                        cannedButtons.setDirty(true);
                    }
                }
                if (cannedButtons.isDirty())
                    cannedButtons.sort();
                clearWizard(wiz);
                if (mRepliedToKey != null)
                    dismissMessageOnReply(mRepliedToKey);

                mWiz = null;
                setResult(RESULT_OK);
                finish();
            }

        });
    }

    private void dismissMessageOnReply(final String repliedToKey) {
        T.UI();
        // Called when a reply is sent
        final MessageStore store = mMessagingPlugin.getStore();
        final MessageTO message = store.getPartialMessageByKey(repliedToKey);
        if (SystemUtils.isFlagEnabled(message.flags, MessagingPlugin.FLAG_ALLOW_DISMISS)
            && !SystemUtils.isFlagEnabled(message.flags, MessagingPlugin.FLAG_DYNAMIC_CHAT)
            && store.messageNeedsAnswerUI(repliedToKey)) {
            mMessagingPlugin.ackMessage(message, null, null, null, this, mButtonsListView);
        }
    };

    private void setViewColors(View view, TextView name, boolean selected) {
        Resources resources = getResources();
        name.setTextColor(resources.getColor(android.R.color.primary_text_light));
        if (selected) {
            view.setBackgroundColor(resources.getColor(R.color.mc_highlight_background));
            name.setTypeface(Typeface.DEFAULT_BOLD);
        } else {
            view.setBackgroundColor(resources.getColor(R.color.mc_background));
            name.setTypeface(Typeface.DEFAULT);
        }
    }

    private void addRecipient(final SendMessageWizard wiz, final LinearLayout recipients, final Friend friend) {
        // LinearLayout avatarLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.avatar_layout, null);
        // final ImageView avatar = (ImageView) avatarLayout.getChildAt(0);
        getLayoutInflater().inflate(R.layout.avatar, recipients);
        final ImageView avatar = (ImageView) recipients.getChildAt(recipients.getChildCount() - 1);

        final Bitmap avatarBitmap = mFriendsPlugin.getAvatarBitmap(friend.email);
        if (avatarBitmap == null) {
            avatar.setImageBitmap(mFriendsPlugin.getMissingFriendAvatarBitmap());
        } else {
            avatar.setImageBitmap(avatarBitmap);
        }
        avatar.setTag(friend.email);
        avatar.setOnClickListener(new SafeViewOnClickListener() {
            @Override
            public void safeOnClick(View v) {
                wiz.getRecipients().remove(friend.email);
                recipients.removeView(avatar);
                View friendView = mListView.findViewWithTag(friend);
                if (friendView != null) {
                    setViewColors(friendView, (TextView) friendView.findViewById(R.id.friend_name), false);
                }
                wiz.save();
            }
        });
        HorizontalScrollView scroller = (HorizontalScrollView) findViewById(R.id.recipients_scroller);
        scroller.smoothScrollTo(recipients.getWidth(), 0);
    }

    private void addGroupRecipient(final SendMessageWizard wiz, final LinearLayout recipients, final Group group) {
        getLayoutInflater().inflate(R.layout.avatar, recipients);
        final ImageView avatar = (ImageView) recipients.getChildAt(recipients.getChildCount() - 1);
        avatar.setImageBitmap(mFriendsPlugin.toGroupBitmap(group.avatar));

        avatar.setTag(group.guid);
        avatar.setOnClickListener(new SafeViewOnClickListener() {
            @Override
            public void safeOnClick(View v) {
                wiz.getGroupRecipients().remove(group.guid);
                recipients.removeView(avatar);
                View groupView = mListView.findViewWithTag(group);
                if (groupView != null) {
                    setViewColors(groupView, (TextView) groupView.findViewById(R.id.group_name), false);
                }
                wiz.save();
            }
        });
        HorizontalScrollView scroller = (HorizontalScrollView) findViewById(R.id.recipients_scroller);
        scroller.smoothScrollTo(recipients.getWidth(), 0);
    }

    private void setButtonItemView(final View view, CannedButton item) {
        TextView captionTextView = (TextView) view.findViewById(R.id.caption);
        TextView actionTextView = (TextView) view.findViewById(R.id.action);
        Resources resources = getResources();
        captionTextView.setTextColor(resources.getColor(android.R.color.primary_text_light));
        actionTextView.setTextColor(resources.getColor(android.R.color.secondary_text_light));
        if (item.isSelected()) {
            view.setBackgroundColor(resources.getColor(R.color.mc_highlight_background));
            captionTextView.setTypeface(Typeface.DEFAULT_BOLD);
        } else {
            view.setBackgroundColor(resources.getColor(R.color.mc_background));
            captionTextView.setTypeface(Typeface.DEFAULT);
        }
        captionTextView.setText(item.getCaption());
        String action = item.getAction();
        if (action == null) {
            actionTextView.setVisibility(View.GONE);
            captionTextView.setPadding(0, 13, 0, 13);
        } else {
            actionTextView.setVisibility(View.VISIBLE);
            actionTextView.setText(action);
            captionTextView.setPadding(0, 0, 0, 0);
        }
        view.setTag(item);
    }

    private Button addButton(final LinearLayout buttons, final CannedButton cannedButton, final SendMessageWizard wizard) {
        final Button button = new Button(SendMessageWizardActivity.this);
        button.setText(cannedButton.getCaption());
        button.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        button.setTag(cannedButton);
        button.setOnClickListener(new SafeViewOnClickListener() {
            @Override
            public void safeOnClick(View v) {

                if (wizard.getSelectedButton() == cannedButton.getId())
                    wizard.setSelectedButton(SendMessageWizard.NO_BUTTON_SELECTED);

                cannedButton.setSelected(false);
                View view = mButtonsListView.findViewWithTag(cannedButton);
                setButtonItemView(view, cannedButton);
                buttons.removeView(button);
                wizard.getButtons().remove(cannedButton.getId());
                wizard.save();
            }
        });
        button.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                try {
                    boolean selected = cannedButton.getId() == wizard.getSelectedButton();
                    if (!selected) {
                        selectButton(button);
                        wizard.setSelectedButton(cannedButton.getId());
                        for (int i = 0; i < buttons.getChildCount(); i++) {
                            View view = buttons.getChildAt(i);
                            if (!(view instanceof Button) || view == button)
                                continue;
                            Button b = (Button) view;
                            unSelectButton(b);
                        }
                    } else {
                        unSelectButton(button);
                        wizard.setSelectedButton(SendMessageWizard.NO_BUTTON_SELECTED);
                    }
                    wizard.save();
                    return true;
                } catch (Exception e) {
                    L.bug(e);
                    return false;
                }
            }
        });
        buttons.addView(button);
        HorizontalScrollView scroller = (HorizontalScrollView) findViewById(R.id.buttons_scroller);
        scroller.smoothScrollTo(buttons.getWidth(), 0);
        return button;
    }

    private void selectButton(final Button button) {
        button.setTextColor(getResources().getColor(R.color.mc_highlight_background));
        button.setTypeface(Typeface.DEFAULT, Typeface.BOLD_ITALIC);
    }

    private void unSelectButton(final Button button) {
        button.setTextColor(getResources().getColor(android.R.color.primary_text_light));
        button.setTypeface(Typeface.DEFAULT);
    }

    private void removeSelectedButton(final SendMessageWizard wiz, final LinearLayout buttons,
        final CannedButton cannedButton) {

        if (wiz.getSelectedButton() == cannedButton.getId())
            wiz.setSelectedButton(SendMessageWizard.NO_BUTTON_SELECTED);

        Button button = (Button) buttons.findViewWithTag(cannedButton);
        buttons.removeView(button);
        wiz.getButtons().remove(cannedButton.getId());
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mWiz != null) {
                mWiz.goBack();
                return true;
            } else if (!isFinishing()) {
                finish();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void clearWizard(final SendMessageWizard wiz) {
        wiz.setMessage("");
        wiz.getRecipients().clear();
        wiz.getGroupRecipients().clear();
        wiz.setRecipientStyle(SendMessageWizard.TO);
        wiz.getButtons().clear();
        wiz.setSelectedButton(SendMessageWizard.NO_BUTTON_SELECTED);
        wiz.save();
    }

    public static MessageTO storeMessage(final SendMessageWizard wiz, final MainService mainService, final String me,
        final MessagingPlugin messagingPlugin, final String tmpKey, final SendMessageRequestTO request) {
        T.BIZZ();
        final MessageTO message = new MessageTO();
        message.key = tmpKey;
        message.sender = me;
        message.flags = request.flags;
        message.timeout = request.timeout;
        long currentTimeMillis = mainService.currentTimeMillis();
        message.timestamp = currentTimeMillis / 1000;
        String parent_key = request.parent_key;
        message.parent_key = parent_key;
        message.message = request.message;
        message.buttons = request.buttons;
        String[] members;
        if (parent_key == null)
            members = request.members;
        else {
            MessageStore store = messagingPlugin.getStore();
            Set<String> memberList = store.getMessageMembers(parent_key);
            members = memberList.toArray(new String[memberList.size()]);
        }
        message.members = new MemberStatusTO[members.length];
        for (int i = 0; i < members.length; i++) {
            String member = members[i];
            MemberStatusTO ms = new MemberStatusTO();
            if (me.equals(member)) {
                ms.status = MessagingPlugin.STATUS_ACKED | MessagingPlugin.STATUS_RECEIVED;
                ms.received_timestamp = currentTimeMillis / 1000;
                ms.acked_timestamp = currentTimeMillis / 1000;
                final long button = wiz.getSelectedButton();
                ms.button_id = button == -1 ? null : String.valueOf(button);
            } else {
                ms.status = 0;
                ms.received_timestamp = 0;
                ms.acked_timestamp = 0;
                ms.button_id = null;
            }
            ms.custom_reply = null;
            ms.member = member;
            message.members[i] = ms;
        }
        message.branding = null;
        message.timestamp = currentTimeMillis / 1000;
        message.priority = request.priority;

        message.default_priority = Message.PRIORITY_NORMAL;
        message.default_sticky = false;

        messagingPlugin.newMessage(message, true, true);
        return message;
    }

    public static void sendMessage(final SendMessageRequestTO request, final String parentKey, final String tmpKey,
        final FriendsPlugin friendsPlugin, final MessagingPlugin messagingPlugin, final MainService mainService)
        throws Exception {
        T.dontCare();

        SafeRunnable sendMessageRunnable = new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                final SendMessageResponseHandler responseHandler = new SendMessageResponseHandler();
                responseHandler.setTmpKey(tmpKey);
                responseHandler.setParentKey(parentKey);
                boolean attachmentsUploaded = request.attachments != null && request.attachments.length > 0;
                responseHandler.setAttachmentsUploaded(attachmentsUploaded);
                if (attachmentsUploaded)
                    messagingPlugin.getStore().insertAttachments(request.attachments, tmpKey);
                com.mobicage.api.messaging.Rpc.sendMessage(responseHandler, request);
            }
        };

        if (T.getThreadType() == T.UI) {
            sendMessageRunnable.run();
        } else {
            mainService.postAtFrontOfUIHandler(sendMessageRunnable);
        }
    }

    public static void sendMessage(final SendMessageWizard wiz, final String parentKey, final long parentFlags,
        final String tmpKey, final String me, final FriendsPlugin friendsPlugin, final MessagingPlugin messagingPlugin,
        final MainService mainService) throws Exception {

        T.UI();
        final SendMessageRequestTO request = new SendMessageRequestTO();
        request.flags = MessagingPlugin.FLAG_ALLOW_DISMISS | MessagingPlugin.FLAG_ALLOW_CUSTOM_REPLY
            | MessagingPlugin.FLAG_ALLOW_REPLY;

        if (parentFlags != 0) {
            // The following flags need to be copied from the parent message.
            request.flags |= (parentFlags & MessagingPlugin.FLAG_DYNAMIC_CHAT)
                | (parentFlags & MessagingPlugin.FLAG_NOT_REMOVABLE)
                | (parentFlags & MessagingPlugin.FLAG_ALLOW_CHAT_BUTTONS)
                | (parentFlags & MessagingPlugin.FLAG_ALLOW_CHAT_PICTURE)
                | (parentFlags & MessagingPlugin.FLAG_ALLOW_CHAT_VIDEO)
                | (parentFlags & MessagingPlugin.FLAG_ALLOW_CHAT_PRIORITY)
                | (parentFlags & MessagingPlugin.FLAG_ALLOW_CHAT_STICKY);
        }

        if (SystemUtils.isFlagEnabled(request.flags, MessagingPlugin.FLAG_DYNAMIC_CHAT)) {
            if (wiz.getIsSticky()) {
                request.flags |= MessagingPlugin.FLAG_CHAT_STICKY;
            }
        }

        if (wiz.getRecipientStyle() == SendMessageWizard.TO)
            request.flags |= MessagingPlugin.FLAG_ALLOW_REPLY_ALL | MessagingPlugin.FLAG_SHARED_MEMBERS;

        request.timeout = 0;
        request.parent_key = parentKey;
        request.message = wiz.getMessage();
        request.priority = wiz.getPriority();
        Long[] btnIds = wiz.getButtons().toArray(new Long[wiz.getButtons().size()]);
        List<ButtonTO> buttons = new ArrayList<ButtonTO>();
        for (int i = 0; i < btnIds.length; i++) {
            Long buttonId = btnIds[i];
            CannedButton cannedButton = wiz.getCannedButtons().getById(buttonId);
            if (cannedButton == null)
                continue;
            ButtonTO button = new ButtonTO();
            button.id = buttonId.toString();
            button.caption = cannedButton.getCaption();
            button.action = cannedButton.getAction();
            buttons.add(button);
        }
        request.buttons = buttons.toArray(new ButtonTO[buttons.size()]);
        Set<String> recipients = new HashSet<String>(wiz.getRecipients());
        if (parentKey == null) {
            recipients.add(me);
            Set<String> groups = wiz.getGroupRecipients();
            if (groups.size() > 0) {
                for (String groupGuid : groups) {
                    recipients.addAll(friendsPlugin.getStore().getGroup(groupGuid).members);
                }
            }
            request.members = recipients.toArray(new String[recipients.size()]);
        } else
            request.members = new String[0]; // Server calculates members in case of reply
        long selectedButton = wiz.getSelectedButton();
        if (selectedButton == SendMessageWizard.NO_BUTTON_SELECTED) {
            request.sender_reply = null;
        } else {
            request.sender_reply = String.valueOf(selectedButton);
        }

        SafeRunnable storeMessageRunnable = new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                T.BIZZ();
                storeMessage(wiz, mainService, me, messagingPlugin, tmpKey, request);
            }
        };

        if (wiz.getHasImageSelected() || wiz.getHasVideoSelected()) {
            AttachmentTO att = new AttachmentTO();
            att.download_url = tmpKey;
            att.name = "";
            att.size = wiz.getTmpUploadFile().length();
            att.content_type = wiz.getUploadFileExtenstion();
            if (att.size == 0)
                att.size = -1;
            request.attachments = new AttachmentTO[] { att };
            messagingPlugin.putSendMessageRequest(tmpKey, request);

            String downloadUrlHash = messagingPlugin.attachmentDownloadUrlHash(tmpKey);
            String tmpMessageKey = tmpKey.replace(MessagingPlugin.TMP_KEY_PREFIX, "");
            File attachmentsDir;
            try {
                attachmentsDir = messagingPlugin.attachmentsDir(parentKey == null ? tmpMessageKey : parentKey,
                    tmpMessageKey);
            } catch (IOException e) {
                L.d("Unable to create attachment directory", e);
                UIUtils.showAlertDialog(mainService, "", R.string.unable_to_read_write_sd_card);
                return;
            }

            File attachmentFile = new File(attachmentsDir, downloadUrlHash);
            if (!wiz.getTmpUploadFile().renameTo(attachmentFile)) {
                try {
                    IOUtils.copyFile(wiz.getTmpUploadFile(), attachmentFile);
                    wiz.getTmpUploadFile().delete();
                } catch (IOException e) {
                    L.d("Unable to move file to attachment directory");
                    UIUtils.showAlertDialog(mainService, "", R.string.unable_to_read_write_sd_card);
                    return;
                }
            }

            try {
                // Try to generate a thumbnail
                messagingPlugin.createAttachmentThumbnail(attachmentFile.getAbsolutePath(), wiz.getHasImageSelected(),
                    wiz.getHasVideoSelected());
            } catch (Exception e) {
                L.e("Failed to generate attachment thumbnail", e);
            }

            mainService.postAtFrontOfBIZZHandler(storeMessageRunnable);
            messagingPlugin.startUploadingFile(attachmentFile, parentKey, tmpKey, null, 0, false,
                wiz.getUploadFileExtenstion());

        } else {
            request.attachments = new AttachmentTO[0];
            sendMessage(request, parentKey, tmpKey, friendsPlugin, messagingPlugin, mainService);
            mainService.postAtFrontOfBIZZHandler(storeMessageRunnable);
        }
    }

    private boolean setupUploadFile(String ext, boolean cleanupPrevious) {
        File file;
        try {
            file = getTmpUploadFileLocation(ext);
            mWiz.setTmpUploadFile(file);
        } catch (IOException e) {
            L.d(e);
            UIUtils.showLongToast(this, getString(R.string.unable_to_read_write_sd_card));
            return false;
        }

        if (cleanupPrevious) {
            file.delete();
        }
        mUriSavedFile = Uri.fromFile(file);
        return true;
    }

    private void askCameraPermission(final SafeRunnable continueRunnable) {
        final SafeRunnable runnableCheckStorage = new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                if (askPermissionIfNeeded(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        PERMISSION_REQUEST_CAMERA, continueRunnable, null))
                    return;
                continueRunnable.run();
            }
        };
        if (askPermissionIfNeeded(Manifest.permission.CAMERA, PERMISSION_REQUEST_CAMERA,
                runnableCheckStorage, null))
            return;
        runnableCheckStorage.run();
    }

    private void getNewPicture() {
        getNewPicture(true);
    }

    private void getNewPicture(boolean checkPermission) {
        if (checkPermission) {
            askCameraPermission(new SafeRunnable() {
                @Override
                protected void safeRun() throws Exception {
                    getNewPicture(false);
                }
            });
            return;
        }

        if (!setupUploadFile("png", true)) {
            return;
        }

        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryIntent.putExtra(MediaStore.EXTRA_OUTPUT, mUriSavedFile);
        galleryIntent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());
        galleryIntent.setType("image/*");

        final Intent chooserIntent = Intent.createChooser(galleryIntent, getString(R.string.select_source));

        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA) && mService.isPermitted(Manifest
                .permission.CAMERA)) {
            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mUriSavedFile);
            cameraIntent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{cameraIntent});
        }

        startActivityForResult(chooserIntent, PICK_IMAGE);
    }

    private void getNewVideo() {
        getNewVideo(true);
    }

    private void getNewVideo(boolean checkPermission) {
        if (checkPermission) {
            askCameraPermission(new SafeRunnable() {
                @Override
                protected void safeRun() throws Exception {
                    getNewVideo(false);
                }
            });
            return;
        }

        if (!setupUploadFile("mp4", true)) {
            return;
        }

        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryIntent.putExtra(MediaStore.EXTRA_OUTPUT, mUriSavedFile);
        galleryIntent.setType(AttachmentViewerActivity.CONTENT_TYPE_VIDEO_MP4);

        final Intent chooserIntent = Intent.createChooser(galleryIntent, getString(R.string.select_source));
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA) && mService.isPermitted(Manifest
                .permission.CAMERA)) {
            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_VIDEO_CAPTURE);
            cameraIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{cameraIntent});
        }

        startActivityForResult(chooserIntent, PICK_VIDEO);
    }

    private File getTmpUploadFileLocation(String ext) throws IOException {
        File imagesFolder = getImagesFolder();
        File image = new File(imagesFolder, "tmpUploadFile." + ext);
        File nomedia = new File(imagesFolder, ".nomedia");
        nomedia.createNewFile();
        return image;
    }

    private File getImagesFolder() throws IOException {
        File imagesFolder = new File(IOUtils.getExternalFilesDirectory(this), "images");
        if (!imagesFolder.exists() && !imagesFolder.mkdirs()) {
            throw new IOException(getString(R.string.unable_to_create_images_directory, getString(R.string.app_name)));
        }
        return imagesFolder;
    }

}
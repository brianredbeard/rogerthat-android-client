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

package com.mobicage.rogerthat.util.ui;

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
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.AttributeSet;
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

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.CannedButton;
import com.mobicage.rogerthat.CannedButtons;
import com.mobicage.rogerthat.HomeActivity;
import com.mobicage.rogerthat.MainActivity;
import com.mobicage.rogerthat.MainService;
import com.mobicage.rogerthat.SendMessageButtonActivity;
import com.mobicage.rogerthat.ServiceBoundActivity;
import com.mobicage.rogerthat.config.Configuration;
import com.mobicage.rogerthat.plugins.friends.FriendsPlugin;
import com.mobicage.rogerthat.plugins.messaging.AttachmentViewerActivity;
import com.mobicage.rogerthat.plugins.messaging.FriendsThreadActivity;
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
import java.util.UUID;

public class SendMessageView<T extends ServiceBoundActivity> extends LinearLayout {

    public static final int TO = 1;
    public static final int BCC = 2;

    public static final long NO_BUTTON_SELECTED = -1;
    public static final String CONFIGKEY = "SEND_NEW_MESSAGE_WIZARD";
    public static final String CANNED_BUTTONS = "CANNED_BUTTONS";

    private static final int PICK_IMAGE = 1;
    private static final int PICK_VIDEO = 2;
    private static final int PICK_BUTTON = 3;

    private final int PERMISSION_REQUEST_CAMERA = 1;
    private final int IMAGE_BUTTON_TEXT = 1;
    private final int IMAGE_BUTTON_BUTTONS = 2;
    private final int IMAGE_BUTTON_PICTURE = 3;
    private final int IMAGE_BUTTON_VIDEO = 4;
    private final int IMAGE_BUTTON_PRIORITY = 5;
    private final int IMAGE_BUTTON_STICKY = 6;
    private final int IMAGE_BUTTON_MORE = 7;
    private final int IMAGE_BUTTON_PADDING = 10;

    private int _5_DP_IN_PX;
    private int _30_DP_IN_PX;
    private int _60_DP_IN_PX;

    private long mParentFlags = 0;
    private String mParentKey = null;
    private String mRepliedToKey = null;

    private MessagingPlugin mMessagingPlugin;
    private String mKey;

    private MainService mMainService;

    private T mActivity;
    private FrameLayout mAttachmentContainer;
    private ImageView mAttachmentPreview;
    private EditText mMessage;

    private String[] mFriendRecipients;
    private boolean mHasImageSelected = false;
    private boolean mHasVideoSelected = false;
    private String mUploadFileExtenstion = null;
    private File mTmpUploadFile = null;

    private List<Integer> mImageButtons;
    private int mMaxImageButtonsOnScreen;

    private Uri mUriSavedFile;

    private CannedButtons mCannedButtons = null;
    private Set<Long> mButtons = new LinkedHashSet<Long>();
    private long mSelectedButton = NO_BUTTON_SELECTED;

    private long mPriority = Message.PRIORITY_NORMAL;
    private boolean mIsSticky = false;

    private int mRecipientStyle = TO;

    public SendMessageView(Context context) {
        super(context);
    }

    public SendMessageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SendMessageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setActive(T activity, MainService mainService, String[] friendRecipients,
                          String initialText, String parentKey, long parentFlags, String repliedToKey,
                          long defaultPriority, boolean defaultSticky) {
        mMainService = mainService;
        mMessagingPlugin = mainService.getPlugin(MessagingPlugin.class);
        mActivity = activity;
        mFriendRecipients = friendRecipients;
        mParentKey = parentKey;
        mParentFlags = parentFlags;
        mRepliedToKey = repliedToKey;
        mPriority = defaultPriority;
        mIsSticky = defaultSticky;
        mMessage = (EditText) findViewById(R.id.message);
        if (initialText != null) {
            mMessage.setText(initialText);
        }

        mKey = UUID.randomUUID().toString();

        final ImageButton submitButton = (ImageButton) findViewById(R.id.submit);

        mAttachmentContainer = (FrameLayout) findViewById(R.id.attachment_container);
        mAttachmentPreview = (ImageView) findViewById(R.id.attachment_preview);

        mAttachmentContainer.setOnClickListener(new SafeViewOnClickListener() {
            @Override
            public void safeOnClick(View v) {
                mAttachmentContainer.setVisibility(View.GONE);
                mHasImageSelected = false;
                mHasVideoSelected = false;
                initImageButtonsNavigation();
            }
        });

        submitButton.setOnClickListener(new SafeViewOnClickListener() {
            @Override
            public void safeOnClick(View v) {
                try {
                    if ("".equals(mMessage.getText().toString().trim()) && mButtons.size() == 0 && !mHasImageSelected
                            && !mHasVideoSelected) {
                        UIUtils.showLongToast(mActivity, mActivity.getString(R.string.message_or_buttons_required));
                        return;
                    }

                    final String me = mMainService.getIdentityStore().getIdentity().getEmail();
                    sendMessage(me);

                    mMainService.sendBroadcast(new Intent(MessagingPlugin.NEW_MESSAGE_QUEUED_TO_BACKLOG_INTENT), true, true);
                    if (mParentKey != null && !SystemUtils.isFlagEnabled(mParentFlags, MessagingPlugin.FLAG_DYNAMIC_CHAT))
                        mMessagingPlugin.ackThread(mParentKey);

                    Long[] buttonIds = mButtons.toArray(new Long[mButtons.size()]);
                    for (Long buttonId : buttonIds) {
                        final CannedButton cannedButton = mCannedButtons.getById(buttonId);
                        if (cannedButton != null) {
                            cannedButton.used();
                            mCannedButtons.setDirty(true);
                        }
                    }
                    if (mCannedButtons.isDirty())
                        mCannedButtons.sort();

                    mCannedButtons.save(mMainService.getConfigurationProvider());

                    if (mRepliedToKey != null)
                        dismissMessageOnReply(mRepliedToKey);

                    if (mFriendRecipients == null) {
                        hideKeyboard();
                        resetLayout();
                    } else {
                        Bundle b = new Bundle();
                        b.putString(HomeActivity.INTENT_KEY_LAUNCHINFO, HomeActivity.INTENT_VALUE_SHOW_NEW_MESSAGES);
                        b.putString(HomeActivity.INTENT_KEY_MESSAGE, mKey);


                        Intent intent = new Intent(mActivity, MessagingActivity.class);
                        intent.setAction(MainActivity.ACTION_NOTIFICATION_MESSAGE_UPDATES);
                        intent.putExtras(b);
                        intent.setFlags(MainActivity.FLAG_NEW_STACK);
                        mActivity.startActivity(intent);
                    }
                } catch (Exception e) {
                    L.bug(e);
                }
            }
        });

        _5_DP_IN_PX = UIUtils.convertDipToPixels(mActivity, 5);
        _30_DP_IN_PX = UIUtils.convertDipToPixels(mActivity, 30);
        _60_DP_IN_PX = UIUtils.convertDipToPixels(mActivity, 60);
        final int displayWidth = UIUtils.getDisplayWidth(mActivity);
        mMaxImageButtonsOnScreen = displayWidth / _60_DP_IN_PX;
        L.i("displayWidth: " + displayWidth);
        L.i("mMaxImageButtonsOnScreen: " + mMaxImageButtonsOnScreen);

        loadCannedButtons();

        final LinearLayout optionButtons = (LinearLayout) findViewById(R.id.imageButtons);
        if (mFriendRecipients == null) {
            mMessage.setOnFocusChangeListener(new OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    optionButtons.setVisibility(View.VISIBLE);
                    initImageButtonsNavigation();
                    mMessage.setOnFocusChangeListener(null);
                }
            });
        } else {
            optionButtons.setVisibility(View.VISIBLE);
            initImageButtonsNavigation();
        }
    }

    private void resetLayout() {
        mKey = UUID.randomUUID().toString();

        mMessage.setText("");
        mMessage.clearFocus();
        mButtons = new LinkedHashSet<Long>();
        mSelectedButton = NO_BUTTON_SELECTED;
        mHasImageSelected = false;
        mHasVideoSelected = false;
        mAttachmentContainer.setVisibility(View.GONE);

        final LinearLayout optionButtons = (LinearLayout) findViewById(R.id.imageButtons);
        optionButtons.setVisibility(View.GONE);
        mMessage.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                optionButtons.setVisibility(View.VISIBLE);
                initImageButtonsNavigation();
                mMessage.setOnFocusChangeListener(null);
            }
        });
    }

    public void showKeyboard() {
        UIUtils.showKeyboard(mActivity);
    }

    public void hideKeyboard() {
        UIUtils.hideKeyboard(mActivity, mMessage);
    }

    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case PICK_IMAGE:
                if (resultCode == Activity.RESULT_OK) {
                    if (mUriSavedFile == null) {
                        setupUploadFile("png", false);
                    }
                    if (data != null && data.getData() != null) {
                        final Uri selectedImage = data.getData();
                        L.d("selectedImage: " + selectedImage.toString());
                        copyImageFile(selectedImage);
                    } else {
                        mUploadFileExtenstion = AttachmentViewerActivity.CONTENT_TYPE_PNG;
                        setPictureSelected();
                    }
                }
                break;
            case PICK_VIDEO:
                if (resultCode == Activity.RESULT_OK) {
                    if (mUriSavedFile == null) {
                        setupUploadFile("mp4", false);
                    }
                    final ContentResolver cr = mActivity.getContentResolver();
                    mUploadFileExtenstion = AttachmentViewerActivity.CONTENT_TYPE_VIDEO_MP4;
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

            case PICK_BUTTON:
                if (resultCode == Activity.RESULT_OK) {
                    try {
                        mCannedButtons = (CannedButtons) Pickler.createObjectFromPickle(data.getByteArrayExtra(SendMessageButtonActivity.CANNED_BUTTONS));
                        mButtons = new LinkedHashSet<Long>();
                        long[] buttons = data.getLongArrayExtra(SendMessageButtonActivity.BUTTONS);
                        if (buttons != null) {
                            for (long l : buttons) {
                                mButtons.add(l);
                            }
                        }
                    } catch (Exception e) {
                        L.bug(e);
                    }
                }
                break;

        }
    }

    private void loadCannedButtons() {
        final Configuration cfg = mMainService.getConfigurationProvider().getConfiguration(CONFIGKEY);

        final String serializedButtons = cfg.get(CANNED_BUTTONS, "");
        if (!"".equals(serializedButtons)) {
            try {
                mCannedButtons = (CannedButtons) Pickler.createObjectFromPickle(Base64.decode(serializedButtons));
            } catch (PickleException e) {
                L.bug(e);
            }
        }
        if (mCannedButtons == null) {
            mCannedButtons = new CannedButtons();
            mCannedButtons.add(new CannedButton(1, mActivity.getString(R.string.yes), null, -1));
            mCannedButtons.add(new CannedButton(2, mActivity.getString(R.string.no), null, -2));
            mCannedButtons.add(new CannedButton(3, mActivity.getString(R.string.maybe), null, -3));
            mCannedButtons.add(new CannedButton(4, mActivity.getString(R.string.like), null, -4));
            mCannedButtons.add(new CannedButton(5, mActivity.getString(R.string.dont_like), null, -5));
            mCannedButtons.add(new CannedButton(6, mActivity.getString(R.string.no_idea), null, -6));
            mCannedButtons.sort();
            mCannedButtons.save(mMainService.getConfigurationProvider());
        }
    }

    private void initImageButtonsNavigation() {
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

        if (mHasImageSelected || mHasVideoSelected) {
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

        if (mImageButtons.size() > mMaxImageButtonsOnScreen) {
            for (int i = 0; i < mMaxImageButtonsOnScreen - 1; i++) {
                int key = mImageButtons.get(i);
                ImageView iv = generateImageView(key, View.VISIBLE);
                optionButtons.addView(iv);
            }
            ImageView iv = generateImageView(IMAGE_BUTTON_MORE, View.VISIBLE);
            optionButtons.addView(iv);
        } else {
            for (int i = 0; i < mImageButtons.size(); i++) {
                int key = mImageButtons.get(i);
                ImageView iv = generateImageView(key, View.VISIBLE);
                optionButtons.addView(iv);
            }
            if (mImageButtons.size() < mMaxImageButtonsOnScreen) {
                for (int i = mMaxImageButtonsOnScreen - 1; i > mImageButtons.size(); i--) {
                    L.i("Adding space");
                    ImageView iv = generateImageView(IMAGE_BUTTON_PADDING, View.INVISIBLE);
                    optionButtons.addView(iv);
                }
            }
        }
    }

    private ImageView generateImageView(final int imageButton, final int visible) {
        ImageView iv = new ImageView(mActivity);
        iv.setVisibility(visible);
        final int imageResourse = getImageResourceForKey(imageButton);
        if (imageResourse != 0)
            iv.setImageResource(imageResourse);
        iv.setOnClickListener(new SafeViewOnClickListener() {
            @Override
            public void safeOnClick(View v) {
                processOnClickListenerForKey(imageButton);
            }
        });

        if (IMAGE_BUTTON_TEXT == imageButton) {
            iv.setColorFilter(UIUtils.imageColorFilter(getResources().getColor(R.color.mc_divider_gray)));
        }

        iv.setPadding(_5_DP_IN_PX, _5_DP_IN_PX, _5_DP_IN_PX, _5_DP_IN_PX);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(_30_DP_IN_PX, _30_DP_IN_PX, 1.0f);
        iv.setLayoutParams(lp);
        return iv;
    }

    private int getImageResourceForKey(final int key) {
        if (IMAGE_BUTTON_TEXT == key) {
            return R.drawable.fa_font;
        } else if (IMAGE_BUTTON_BUTTONS == key) {
            return R.drawable.fa_list;
        } else if (IMAGE_BUTTON_PICTURE == key) {
            return R.drawable.fa_camera;
        } else if (IMAGE_BUTTON_VIDEO == key) {
            return R.drawable.fa_video_camera;
        } else if (IMAGE_BUTTON_PRIORITY == key) {
            if (mPriority == Message.PRIORITY_HIGH) {
                return R.drawable.fa_priority_2;
            } else if (mPriority == Message.PRIORITY_URGENT) {
                return R.drawable.fa_priority_3;
            } else if (mPriority == Message.PRIORITY_URGENT_WITH_ALARM) {
                return R.drawable.fa_priority_4;
            } else {
                return R.drawable.fa_priority_1;
            }
        } else if (IMAGE_BUTTON_STICKY == key) {
            if (mIsSticky) {
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

    private boolean setupUploadFile(String ext, boolean cleanupPrevious) {
        File file;
        try {
            file = getTmpUploadFileLocation(ext);
            mTmpUploadFile = file;
        } catch (IOException e) {
            L.d(e);
            UIUtils.showLongToast(mActivity, mActivity.getString(R.string.unable_to_read_write_sd_card));
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
                if (mActivity.askPermissionIfNeeded(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        PERMISSION_REQUEST_CAMERA, continueRunnable, null))
                    return;
                continueRunnable.run();
            }
        };
        if (mActivity.askPermissionIfNeeded(Manifest.permission.CAMERA, PERMISSION_REQUEST_CAMERA,
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

        final Intent chooserIntent = Intent.createChooser(galleryIntent, mActivity.getString(R.string.select_source));

        if (mActivity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA) && mMainService.isPermitted(Manifest
                .permission.CAMERA)) {
            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mUriSavedFile);
            cameraIntent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{cameraIntent});
        }

        mActivity.startActivityForResult(chooserIntent, PICK_IMAGE);
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

        final Intent chooserIntent = Intent.createChooser(galleryIntent, mActivity.getString(R.string.select_source));
        if (mActivity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA) && mMainService.isPermitted(Manifest
                .permission.CAMERA)) {
            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_VIDEO_CAPTURE);
            cameraIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{cameraIntent});
        }

        mActivity.startActivityForResult(chooserIntent, PICK_VIDEO);
    }

    private void copyVideoFile(final ContentResolver cr, final Uri selectedVideo) {
        final ProgressDialog progressDialog = new ProgressDialog(mActivity);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage(mActivity.getString(R.string.processing));
        progressDialog.setCancelable(false);
        progressDialog.show();

        new SafeAsyncTask<Object, Object, Boolean>() {
            @Override
            protected Boolean safeDoInBackground(Object... params) {
                L.d("Processing video: " + selectedVideo.toString());
                try {
                    if (mTmpUploadFile.getAbsolutePath().equals(selectedVideo.getPath())) {
                        return true;
                    } else {
                        InputStream is = cr.openInputStream(selectedVideo);
                        if (is != null) {
                            try {
                                OutputStream out = new FileOutputStream(mTmpUploadFile);
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
                    UIUtils.showLongToast(mActivity, mActivity.getString(R.string.error_please_try_again));
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
        final ContentResolver cr = mActivity.getContentResolver();
        final ProgressDialog progressDialog = new ProgressDialog(mActivity);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage(mActivity.getString(R.string.processing));
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
                        mUploadFileExtenstion = AttachmentViewerActivity.CONTENT_TYPE_PNG;
                    } else {
                        mUploadFileExtenstion = AttachmentViewerActivity.CONTENT_TYPE_JPEG;
                    }

                    if (mTmpUploadFile.getAbsolutePath().equals(selectedImage.getPath())) {
                        return true;
                    } else {
                        InputStream is = cr.openInputStream(selectedImage);
                        if (is != null) {
                            try {
                                OutputStream out = new FileOutputStream(mTmpUploadFile);
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
                    UIUtils.showLongToast(mActivity, mActivity.getString(R.string.error_please_try_again));
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
            UIUtils.showLongToast(mActivity, mActivity.getString(R.string.error_please_try_again));
            return;
        }

        Bitmap bitmap = ImageHelper.getBitmapFromFile(mUriSavedFile.getPath(), options);

        Drawable d = new BitmapDrawable(Resources.getSystem(), bitmap);
        mAttachmentPreview.setImageDrawable(d);
        mAttachmentContainer.setVisibility(View.VISIBLE);

        mHasImageSelected = true;
        initImageButtonsNavigation();
    }

    private void setVideoSelected() {
        if (!new File(mUriSavedFile.getPath()).exists()) {
            UIUtils.showLongToast(mActivity, mActivity.getString(R.string.error_please_try_again));
            return;
        }
        Bitmap bitmap = UIUtils.createVideoThumbnail(mActivity, mUriSavedFile.getPath(),
                UIUtils.convertDipToPixels(mActivity, 200));
        Drawable d = new BitmapDrawable(Resources.getSystem(), bitmap);
        mAttachmentPreview.setImageDrawable(d);
        mAttachmentContainer.setVisibility(View.VISIBLE);

        mHasVideoSelected = true;
        initImageButtonsNavigation();
    }

    private File getTmpUploadFileLocation(String ext) throws IOException {
        File imagesFolder = getImagesFolder();
        File image = new File(imagesFolder, "tmpUploadFile." + ext);
        File nomedia = new File(imagesFolder, ".nomedia");
        nomedia.createNewFile();
        return image;
    }

    private File getImagesFolder() throws IOException {
        File imagesFolder = new File(IOUtils.getExternalFilesDirectory(mActivity), "images");
        if (!imagesFolder.exists() && !imagesFolder.mkdirs()) {
            throw new IOException(mActivity.getString(R.string.unable_to_create_images_directory, mActivity.getString(R.string.app_name)));
        }
        return imagesFolder;
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

        private List<PickMoreItem> values;

        public ListAdapter(Context context, List<PickMoreItem> values) {
            super(context, -1, values);
            this.values = values;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final PickMoreItem item = this.values.get(position);
            final View itemView;
            if (convertView != null && convertView instanceof RelativeLayout)
                itemView = convertView;
            else
                itemView = mActivity.getLayoutInflater().inflate(R.layout.more_item, null);

            ImageView itemAvatar = (ImageView) itemView.findViewById(R.id.item_avatar);
            itemAvatar.setImageResource(item.iv);

            TextView itemName = (TextView) itemView.findViewById(R.id.item_name);
            itemName.setText(item.title);
            itemName.setTextColor(getResources().getColor(android.R.color.primary_text_light));
            itemView.setTag(item);
            return itemView;
        }

    }

    private void processOnClickListenerForKey(final int key) {

        if (IMAGE_BUTTON_TEXT == key) {

        } else if (IMAGE_BUTTON_BUTTONS == key) {
            hideKeyboard();
            try {
                Intent intent = new Intent(mActivity, SendMessageButtonActivity.class);
                intent.putExtra(SendMessageButtonActivity.CANNED_BUTTONS, Pickler.getPickleFromObject(mCannedButtons));
                long[] primitiveLongArray = new long[mButtons.size()];
                Long[] longArray = mButtons.toArray(new Long[mButtons.size()]);
                for (int i =0; i < longArray.length; i++) {
                    primitiveLongArray[i] = longArray[i].longValue();
                }
                intent.putExtra(SendMessageButtonActivity.BUTTONS, primitiveLongArray);
                mActivity.startActivityForResult(intent, PICK_BUTTON);
            } catch (Exception e) {
                L.bug(e);
            }
        } else if (IMAGE_BUTTON_PICTURE == key) {
            hideKeyboard();
            getNewPicture();
        } else if (IMAGE_BUTTON_VIDEO == key) {
            hideKeyboard();
            getNewVideo();

        } else if (IMAGE_BUTTON_PRIORITY == key) {
            hideKeyboard();
            final Dialog dialog = new Dialog(mActivity);
            dialog.setContentView(R.layout.msg_priority_picker); // todo ruben
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
                        mPriority = Message.PRIORITY_HIGH;
                    } else if (priorityUrgentBtn.isChecked()) {
                        mPriority = Message.PRIORITY_URGENT;
                    } else if (priorityUrgentWithAlarmBtn.isChecked()) {
                        mPriority = Message.PRIORITY_URGENT_WITH_ALARM;
                    } else {
                        mPriority = Message.PRIORITY_NORMAL;
                    }

                    dialog.dismiss();
                    initImageButtonsNavigation();
                }
            });

            priorityNormalBtn.setChecked(false);
            priorityHighBtn.setChecked(false);
            priorityUrgentBtn.setChecked(false);
            priorityUrgentWithAlarmBtn.setChecked(false);

            if (mPriority == Message.PRIORITY_HIGH) {
                priorityHighBtn.setChecked(true);
            } else if (mPriority == Message.PRIORITY_URGENT) {
                priorityUrgentBtn.setChecked(true);
            } else if (mPriority == Message.PRIORITY_URGENT_WITH_ALARM) {
                priorityUrgentWithAlarmBtn.setChecked(true);
            } else {
                priorityNormalBtn.setChecked(true);
            }

            dialog.show();
        } else if (IMAGE_BUTTON_STICKY == key) {
            hideKeyboard();
            final Dialog dialog = new Dialog(mActivity);
            dialog.setContentView(R.layout.msg_sticky_picker); // todo ruben
            dialog.setTitle(R.string.sticky);
            Button savePriorityBtn = (Button) dialog.findViewById(R.id.ok);
            final RadioButton stickyDisabled = ((RadioButton) dialog.findViewById(R.id.sticky_disabled));
            final RadioButton stickyEnabled = ((RadioButton) dialog.findViewById(R.id.sticky_enabled));

            savePriorityBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mIsSticky = stickyEnabled.isChecked();

                    dialog.dismiss();
                    initImageButtonsNavigation();
                }
            });

            stickyDisabled.setChecked(false);
            stickyEnabled.setChecked(false);

            if (mIsSticky) {
                stickyEnabled.setChecked(true);
            } else {
                stickyDisabled.setChecked(true);
            }

            dialog.show();
        } else if (IMAGE_BUTTON_MORE == key) {final Dialog dialog = new Dialog(mActivity);
            hideKeyboard();
            dialog.setContentView(R.layout.msg_more_picker);
            dialog.setTitle(R.string.more);
            final ListView pickMsgMore = (ListView) dialog.findViewById(R.id.pick_msg_more);

            pickMsgMore.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
                    dialog.dismiss();
                    PickMoreItem item = (PickMoreItem) view.getTag();
                    processOnClickListenerForKey(item.imageButtonKey);
                }
            });

            List<PickMoreItem> items = new ArrayList<PickMoreItem>();
            for (int i = mMaxImageButtonsOnScreen - 2; i < mImageButtons.size(); i++) {
                int k = mImageButtons.get(i);
                String t = "";
                if (k == IMAGE_BUTTON_TEXT) {
                    t = mActivity.getString(R.string.title_message);
                } else if (k == IMAGE_BUTTON_BUTTONS) {
                    t = mActivity.getString(R.string.title_buttons);
                } else if (k == IMAGE_BUTTON_PICTURE) {
                    t = mActivity.getString(R.string.title_new_message_image);
                } else if (k == IMAGE_BUTTON_VIDEO) {
                    t = mActivity.getString(R.string.title_new_message_video);
                } else if (k == IMAGE_BUTTON_PRIORITY) {
                    t = mActivity.getString(R.string.priority);
                } else if (k == IMAGE_BUTTON_STICKY) {
                    t = mActivity.getString(R.string.sticky);
                } else {
                    L.d("Could not find more text for key: " + key);
                }

                items.add(new PickMoreItem(k, getImageResourceForKey(k), t));
            }
            pickMsgMore.setAdapter(new ListAdapter(mActivity, items));

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

    private void sendMessage(final String me) throws Exception {

        com.mobicage.rogerthat.util.system.T.UI();
        final SendMessageRequestTO request = new SendMessageRequestTO();
        request.flags = MessagingPlugin.FLAG_ALLOW_DISMISS | MessagingPlugin.FLAG_ALLOW_CUSTOM_REPLY
                | MessagingPlugin.FLAG_ALLOW_REPLY;

        if (mParentFlags != 0) {
            // The following flags need to be copied from the parent message.
            request.flags |= (mParentFlags & MessagingPlugin.FLAG_DYNAMIC_CHAT)
                    | (mParentFlags & MessagingPlugin.FLAG_NOT_REMOVABLE)
                    | (mParentFlags & MessagingPlugin.FLAG_ALLOW_CHAT_BUTTONS)
                    | (mParentFlags & MessagingPlugin.FLAG_ALLOW_CHAT_PICTURE)
                    | (mParentFlags & MessagingPlugin.FLAG_ALLOW_CHAT_VIDEO)
                    | (mParentFlags & MessagingPlugin.FLAG_ALLOW_CHAT_PRIORITY)
                    | (mParentFlags & MessagingPlugin.FLAG_ALLOW_CHAT_STICKY);
        }

        if (SystemUtils.isFlagEnabled(request.flags, MessagingPlugin.FLAG_DYNAMIC_CHAT)) {
            if (mIsSticky) {
                request.flags |= MessagingPlugin.FLAG_CHAT_STICKY;
            }
        }

        if (mRecipientStyle == TO)
            request.flags |= MessagingPlugin.FLAG_ALLOW_REPLY_ALL | MessagingPlugin.FLAG_SHARED_MEMBERS;

        request.timeout = 0;
        request.key = mKey;
        request.parent_key = mParentKey;
        request.message = mMessage.getText().toString();
        request.priority = mPriority;
        Long[] btnIds = mButtons.toArray(new Long[mButtons.size()]);
        List<ButtonTO> buttons = new ArrayList<ButtonTO>();
        for (int i = 0; i < btnIds.length; i++) {
            Long buttonId = btnIds[i];
            CannedButton cannedButton = mCannedButtons.getById(buttonId);
            if (cannedButton == null)
                continue;
            ButtonTO button = new ButtonTO();
            button.id = buttonId.toString();
            button.caption = cannedButton.getCaption();
            button.action = cannedButton.getAction();
            buttons.add(button);
        }
        request.buttons = buttons.toArray(new ButtonTO[buttons.size()]);
        if (mParentKey == null) {
            request.members = new String[mFriendRecipients.length + 1];
            for (int i = 0; i < request.members.length - 1 ; i++) {
                request.members[i] = mFriendRecipients[i];
            }
            request.members[request.members.length - 1] = me;
        } else
            request.members = new String[0]; // Server calculates members in case of reply
        long selectedButton = mSelectedButton;
        if (selectedButton == NO_BUTTON_SELECTED) {
            request.sender_reply = null;
        } else {
            request.sender_reply = String.valueOf(selectedButton);
        }

        SafeRunnable storeMessageRunnable = new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                com.mobicage.rogerthat.util.system.T.BIZZ();
                storeMessage(me, request);
            }
        };

        if (mHasImageSelected || mHasVideoSelected) {
            AttachmentTO att = new AttachmentTO();
            att.download_url = mKey;
            att.name = "";
            att.size = mTmpUploadFile.length();
            att.content_type = mUploadFileExtenstion;
            if (att.size == 0)
                att.size = -1;
            request.attachments = new AttachmentTO[] { att };
            mMessagingPlugin.putSendMessageRequest(mKey, request);

            String downloadUrlHash = mMessagingPlugin.attachmentDownloadUrlHash(mKey);
            File attachmentsDir;
            try {
                attachmentsDir = mMessagingPlugin.attachmentsDir(mParentKey == null ? mKey : mParentKey,
                        mKey);
            } catch (IOException e) {
                L.d("Unable to create attachment directory", e);
                UIUtils.showAlertDialog(mMainService, "", R.string.unable_to_read_write_sd_card);
                return;
            }

            File attachmentFile = new File(attachmentsDir, downloadUrlHash);
            if (!mTmpUploadFile.renameTo(attachmentFile)) {
                try {
                    IOUtils.copyFile(mTmpUploadFile, attachmentFile);
                    mTmpUploadFile.delete();
                } catch (IOException e) {
                    L.d("Unable to move file to attachment directory");
                    UIUtils.showAlertDialog(mMainService, "", R.string.unable_to_read_write_sd_card);
                    return;
                }
            }

            try {
                // Try to generate a thumbnail
                mMessagingPlugin.createAttachmentThumbnail(attachmentFile.getAbsolutePath(), mHasImageSelected,
                        mHasImageSelected);
            } catch (Exception e) {
                L.e("Failed to generate attachment thumbnail", e);
            }

            mMainService.postAtFrontOfBIZZHandler(storeMessageRunnable);
            mMessagingPlugin.startUploadingFile(attachmentFile, mParentKey, mKey, null, 0, false,
                    mUploadFileExtenstion);

        } else {
            request.attachments = new AttachmentTO[0];
            sendMessage(request);
            mMainService.postAtFrontOfBIZZHandler(storeMessageRunnable);
        }
    }

    public void sendMessage(final SendMessageRequestTO request) throws Exception {
        sendMessage(request, mParentKey, mKey, mMessagingPlugin, mMainService);
    }

    public static void sendMessage(final SendMessageRequestTO request, final String parentKey,
                                   final String key, final MessagingPlugin messagingPlugin,
                                   final MainService mainService) throws Exception {
        com.mobicage.rogerthat.util.system.T.dontCare();

        SafeRunnable sendMessageRunnable = new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                final SendMessageResponseHandler responseHandler = new SendMessageResponseHandler();
                responseHandler.setKey(key);
                responseHandler.setParentKey(parentKey);
                boolean attachmentsUploaded = request.attachments != null && request.attachments.length > 0;
                responseHandler.setAttachmentsUploaded(attachmentsUploaded);
                if (attachmentsUploaded)
                    messagingPlugin.getStore().insertAttachments(request.attachments, key);
                com.mobicage.api.messaging.Rpc.sendMessage(responseHandler, request);
            }
        };

        if (com.mobicage.rogerthat.util.system.T.getThreadType() == com.mobicage.rogerthat.util.system.T.UI) {
            sendMessageRunnable.run();
        } else {
            mainService.postAtFrontOfUIHandler(sendMessageRunnable);
        }
    }

    public MessageTO storeMessage(final String me, final SendMessageRequestTO request) {
        com.mobicage.rogerthat.util.system.T.BIZZ();
        final MessageTO message = new MessageTO();
        message.key = mKey;
        message.sender = me;
        message.flags = request.flags;
        message.timeout = request.timeout;
        long currentTimeMillis = mMainService.currentTimeMillis();
        message.timestamp = currentTimeMillis / 1000;
        String parent_key = request.parent_key;
        message.parent_key = parent_key;
        message.message = request.message;
        message.buttons = request.buttons;
        String[] members;
        if (parent_key == null)
            members = request.members;
        else {
            MessageStore store = mMessagingPlugin.getStore();
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
                final long button = mSelectedButton;
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

        mMessagingPlugin.newMessage(message, true, true);
        return message;
    }

    private void dismissMessageOnReply(final String repliedToKey) {
        com.mobicage.rogerthat.util.system.T.UI();
        // Called when a reply is sent
        final MessageStore store = mMessagingPlugin.getStore();
        final MessageTO message = store.getPartialMessageByKey(repliedToKey);
        if (SystemUtils.isFlagEnabled(message.flags, MessagingPlugin.FLAG_ALLOW_DISMISS)
                && !SystemUtils.isFlagEnabled(message.flags, MessagingPlugin.FLAG_DYNAMIC_CHAT)
                && store.messageNeedsAnswerUI(repliedToKey)) {
            mMessagingPlugin.ackMessage(message, null, null, null, mActivity, null);
        }
    }
}

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

/*
 COPYRIGHT (C) 2011 MOBICAGE NV
 * ALL RIGHTS RESERVED.
 *
 * ALTHOUGH YOU MAY BE ABLE TO READ THE CONTENT OF THIS FILE, THIS FILE
 * CONTAINS CONFIDENTIAL INFORMATION OF MOBICAGE NV. YOU ARE NOT ALLOWED
 * TO MODIFY, REPRODUCE, DISCLOSE, PUBLISH OR DISTRIBUTE ITS CONTENT,
 * EMBED IT IN OTHER SOFTWARE, OR CREATE DERIVATIVE WORKS, UNLESS PRIOR
 * WRITTEN PERMISSION IS OBTAINED FROM MOBICAGE NV.
 *
 * THE COPYRIGHT NOTICE ABOVE DOES NOT EVIDENCE ANY ACTUAL OR INTENDED
 * PUBLICATION OF SUCH SOURCE CODE.
 *
 * @@license_version:1.4@@
 */

package com.mobicage.rogerthat.plugins.messaging.widgets;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.commonsware.cwac.cam2.Facing;
import com.mobicage.api.messaging.Rpc;
import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.plugins.messaging.AttachmentViewerActivity;
import com.mobicage.rogerthat.plugins.messaging.Message;
import com.mobicage.rogerthat.plugins.messaging.MessagingPlugin;
import com.mobicage.rogerthat.plugins.messaging.ServiceMessageDetailActivity;
import com.mobicage.rogerthat.util.ActivityUtils;
import com.mobicage.rogerthat.util.IOUtils;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeAsyncTask;
import com.mobicage.rogerthat.util.system.SafeDialogClick;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.ui.ImageHelper;
import com.mobicage.rogerthat.util.ui.UIUtils;
import com.mobicage.rpc.ResponseHandler;
import com.mobicage.to.messaging.forms.SubmitPhotoUploadFormRequestTO;
import com.mobicage.to.messaging.forms.SubmitPhotoUploadFormResponseTO;
import com.mobicage.to.messaging.forms.UnicodeWidgetResultTO;
import com.soundcloud.android.crop.Crop;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

public class PhotoUploadWidget extends Widget {

    private static final String USER_QUALITY = "user";
    private static final String BEST_QUALITY = "best";
    private ImageView mImagePreview;
    private Button mSourceButton;
    private static final int PICK_IMAGE = 1;
    private static final String USER_DEFINED_CROP_RATIO = "0x0";
    private static final Boolean TRUE = Boolean.TRUE;
    private Uri mUriSavedImage;
    private boolean mPhotoSelected;

    private String mQuality;
    private String mRatio;

    public PhotoUploadWidget(Context context) {
        super(context);
    }

    public PhotoUploadWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void initializeWidget() {
        mImagePreview = (ImageView) findViewById(R.id.image_preview);
        mSourceButton = (Button) findViewById(R.id.select_picture);
        mSourceButton.setText(R.string.get_picture);
        mPhotoSelected = false;

        mRatio = (String) mWidgetMap.get("ratio");
        mQuality = (String) mWidgetMap.get("quality");

        mSourceButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                final SafeRunnable runnableContinue = new SafeRunnable() {
                    @Override
                    protected void safeRun() throws Exception {
                        getPicture();
                    }
                };

                final SafeRunnable runnableCheckStorage = new SafeRunnable() {
                    @Override
                    protected void safeRun() throws Exception {
                        if (mActivity.askPermissionIfNeeded(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                ServiceMessageDetailActivity.PERMISSION_REQUEST_PHOTO_UPLOAD_WIDGET, runnableContinue, null))
                            return;
                        runnableContinue.run();
                    }
                };

                if (TRUE.equals(mWidgetMap.get("camera"))) {
                    if (mActivity.askPermissionIfNeeded(Manifest.permission.CAMERA,
                            ServiceMessageDetailActivity.PERMISSION_REQUEST_PHOTO_UPLOAD_WIDGET, runnableCheckStorage, null))
                        return;
                }

                runnableCheckStorage.run();
            }

            private void getPicture() {
                File image;
                try {
                    image = getTmpUploadPhotoLocation();
                } catch (IOException e) {
                    L.d(e.getMessage());
                    UIUtils.showLongToast(getContext(), e.getMessage());
                    return;
                }
                mUriSavedImage = Uri.fromFile(image);

                Intent cameraIntent = null;
                boolean hasCamera = mActivity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
                boolean hasCameraPermission = mActivity.getMainService().isPermitted(Manifest.permission.CAMERA);
                if (hasCamera && hasCameraPermission) {
                    cameraIntent = ActivityUtils.buildTakePictureIntent(mActivity, mUriSavedImage, Facing.BACK);
                }

                Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                galleryIntent.putExtra(MediaStore.EXTRA_OUTPUT, mUriSavedImage);
                galleryIntent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
                galleryIntent.setType("image/*");

                final Intent chooserIntent;
                if (TRUE.equals(mWidgetMap.get("gallery")) && TRUE.equals(mWidgetMap.get("camera"))) {
                    chooserIntent = Intent.createChooser(galleryIntent, mActivity.getString(R.string.select_source));
                    if (cameraIntent != null) {
                        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] { cameraIntent });
                    }

                    mActivity.startActivityForResult(chooserIntent, PICK_IMAGE);
                } else if (TRUE.equals(mWidgetMap.get("gallery"))) {
                    chooserIntent = Intent.createChooser(galleryIntent, mActivity.getString(R.string.select_source));
                    mActivity.startActivityForResult(chooserIntent, PICK_IMAGE);
                } else if (TRUE.equals(mWidgetMap.get("camera"))) {
                    if (cameraIntent != null) {
                        mActivity.startActivityForResult(cameraIntent, PICK_IMAGE);
                    } else if (!hasCamera) {
                        UIUtils.showDialog(mActivity, R.string.no_camera_available_title, R.string.no_camera_available);
                    } else if (!hasCameraPermission) {
                        String title = mActivity.getString(R.string.need_camera_permission_title);
                        String message = mActivity.getString(R.string.need_camera_permission);
                        SafeDialogClick onPositiveClick = new SafeDialogClick() {
                            @Override
                            public void safeOnClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                                Intent intent = new Intent();
                                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", mActivity.getPackageName(), null);
                                intent.setData(uri);
                                mActivity.startActivity(intent);
                            }
                        };
                        UIUtils.showDialog(mActivity, title, message, R.string.go_to_app_settings, onPositiveClick,
                                R.id.cancel, null);
                    }
                }
            }
        });
    }

    private void beginCrop(Uri source) {
        Uri outputUri = Uri.fromFile(new File(mActivity.getCacheDir(), "cropped"));
        if (!USER_DEFINED_CROP_RATIO.equals(mRatio)) {
            String[] quality = mRatio.split("x");
            int x = Integer.parseInt(quality[0]);
            int y = Integer.parseInt(quality[1]);
            new Crop(source).output(outputUri).withAspect(x, y).start(mActivity);
        } else {
            new Crop(source).output(outputUri).start(mActivity);
        }
    }

    private void handleCrop(int resultCode, Intent result) {
        if (resultCode == Activity.RESULT_OK) {
            File fImage;
            try {
                fImage = getTmpUploadPhotoLocation();
                IOUtils.copyFile(new File(Crop.getOutput(result).getPath()), fImage);
            } catch (IOException e) {
                L.d(e.getMessage());
                UIUtils.showLongToast(getContext(), e.getMessage());
                return;
            }

            handleSelection();
        } else if (resultCode == Crop.RESULT_ERROR) {
            UIUtils.showLongToast(getContext(), Crop.getError(result).getMessage());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Crop.REQUEST_CROP) {
            handleCrop(resultCode, data);
        } else if (requestCode == PICK_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null && data.getData() != null) {
                    if (mRatio == null) {
                        final Uri selectedImage = data.getData();

                        final ProgressDialog progressDialog = new ProgressDialog(mActivity);
                        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        progressDialog.setMessage(mActivity.getString(R.string.processing));
                        progressDialog.setCancelable(false);
                        progressDialog.show();

                        new SafeAsyncTask<Object, Object, Boolean>() {
                            @Override
                            protected Boolean safeDoInBackground(Object... params) {
                                L.d("Processing picture: " + selectedImage.getPath());
                                try {
                                    File tmpUploadFile = getTmpUploadPhotoLocation();
                                    if (tmpUploadFile.getAbsolutePath().equals(selectedImage.getPath())) {
                                        return true;
                                    } else {
                                        InputStream is = mActivity.getContentResolver().openInputStream(selectedImage);
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
                                    L.bug(
                                        "Unknown exception occured while processing picture: "
                                            + selectedImage.toString(), e);
                                }

                                return false;
                            };

                            @Override
                            protected void safeOnPostExecute(Boolean result) {
                                progressDialog.dismiss();
                                if (result) {
                                    handleSelection();
                                } else {
                                    UIUtils.showLongToast(getContext(), R.string.crop__pick_error);
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

                    } else {
                        beginCrop(data.getData());
                        return;
                    }
                } else {
                    if (mRatio == null) {
                        handleSelection();
                    } else {
                        beginCrop(mUriSavedImage);
                    }
                }
            }
        } else {
            L.bug("Unexpected request code in onActivityResult: " + requestCode);
        }
    }

    private void handleSelection() {
        if (mUriSavedImage == null) {
            L.bug("PhotoUploadWidget mUriSavedImage was null.. Creating now");
            File image;
            try {
                image = getTmpUploadPhotoLocation();
            } catch (IOException e) {
                L.d(e.getMessage());
                return;
            }
            mUriSavedImage = Uri.fromFile(image);
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;
        Bitmap bitmap = ImageHelper.getBitmapFromFile(mUriSavedImage.getPath(), options);

        Drawable d = new BitmapDrawable(Resources.getSystem(), bitmap);
        mImagePreview.setImageDrawable(d);

        if (!USER_QUALITY.equals(mQuality)) {
            if (BEST_QUALITY.equals(mQuality) || mQuality == null) {
                IOUtils.compressPicture(mUriSavedImage, 0);
            } else {
                IOUtils.compressPicture(mUriSavedImage, Long.parseLong(mQuality));
            }
        } else {
            pickSizePhoto(mUriSavedImage);
        }
        mPhotoSelected = true;
        mSourceButton.setText(R.string.change_picture);
    }

    private void pickSizePhoto(Uri uri) {
        File file = new File(uri.getPath());
        long size = file.length();
        long large = 600000;
        long medium = 200000;
        long small = 75000;
        final String[] items;
        final long[] sizes;

        if (size > 600000) {
            sizes = new long[] { size, large, medium, small };
            items = new String[] { mActivity.getString(R.string.size_actual) + " (" + size / 1000 + " KB)",
                mActivity.getString(R.string.size_large) + " (" + large / 1000 + " KB)",
                mActivity.getString(R.string.size_medium) + " (" + medium / 1000 + " KB)",
                mActivity.getString(R.string.size_small) + " (" + small / 1000 + " KB)" };
        } else if (size > 200000) {
            sizes = new long[] { size, medium, small };
            items = new String[] { mActivity.getString(R.string.size_actual) + " (" + size / 1000 + " KB)",
                mActivity.getString(R.string.size_medium) + " (" + medium / 1000 + " KB)",
                mActivity.getString(R.string.size_small) + " (" + small / 1000 + " KB)" };
        } else if (size > 75000) {
            sizes = new long[] { size, small };
            items = new String[] { mActivity.getString(R.string.size_actual) + " (" + size / 1000 + " KB)",
                mActivity.getString(R.string.size_small) + " (" + small / 1000 + " KB)" };
        } else {
            sizes = new long[] { size };
            items = new String[] { mActivity.getString(R.string.size_actual) + " (" + size / 1000 + " KB)" };
            // don't ask
        }
        if (size > 75000) {
            String title = mActivity.getString(R.string.select_size);
            SafeDialogClick itemsOnClickListener = new SafeDialogClick() {
                @Override
                public void safeOnClick(DialogInterface dialog, int id) {
                    IOUtils.compressPicture(mUriSavedImage, sizes[id]);
                }
            };
            UIUtils.showDialog(mActivity, title, null, items, itemsOnClickListener);
        }
    }

    @Override
    public void putValue() {
    }

    @Override
    public UnicodeWidgetResultTO getWidgetResult() {
        return null;
    }

    @Override
    public boolean proceedWithSubmit(final String buttonId) {
        File image = null;
        try {
            image = getTmpUploadPhotoLocation();
        } catch (IOException e) {
            if (Message.POSITIVE.equals(buttonId)) {
                L.d(e.getMessage());
                UIUtils.showLongToast(getContext(), e.getMessage());
                return false;
            }
        }

        if (Message.POSITIVE.equals(buttonId)) {
            boolean imageExists = image != null && image.exists();
            if (!mPhotoSelected || !imageExists) {
                UIUtils.showDialog(mActivity, R.string.no_photo_selected_tilte, R.string.no_photo_selected_summary);
                return false;
            }
        } else if (image != null) {
            image.delete();
        }
        return true;
    }

    @Override
    public void submit(final String buttonId, long timestamp) throws Exception {
        T.UI();
        final SubmitPhotoUploadFormRequestTO request = new SubmitPhotoUploadFormRequestTO();
        request.button_id = buttonId;
        request.message_key = mMessage.key;
        request.parent_message_key = mMessage.parent_key;
        request.timestamp = timestamp;
        final boolean isSentByJSMFR = (mMessage.flags & MessagingPlugin.FLAG_SENT_BY_JSMFR) == MessagingPlugin.FLAG_SENT_BY_JSMFR;

        if (Message.POSITIVE.equals(buttonId)) {
            final File image = getTmpUploadPhotoLocation();
            boolean showTransferring = mActivity.getMainService().getPlugin(MessagingPlugin.class)
                .startUploadingFile(image, mMessage, AttachmentViewerActivity.CONTENT_TYPE_JPEG);

            if (showTransferring) {
                mActivity.setTransfering(true);
                mActivity.showTransferingDialog();
            } else {
                // Not transferring because not on WIFI or no internet connection
                mActivity.finish();
            }
        } else if (isSentByJSMFR) {
            mPlugin.answerJsMfrMessage(mMessage, request.toJSONMap(),
                "com.mobicage.api.messaging.submitPhotoUploadForm", mActivity, mParentView);
        } else {
            Rpc.submitPhotoUploadForm(new ResponseHandler<SubmitPhotoUploadFormResponseTO>(), request);
        }
    }

    private File getTmpUploadPhotoLocation() throws IOException {
        Context c = getContext();
        File imagesFolder = new File(IOUtils.getExternalFilesDirectory(mActivity), "images");
        if (!imagesFolder.exists() && !imagesFolder.mkdirs()) {
            throw new IOException(c.getString(R.string.unable_to_create_images_directory,
                c.getString(R.string.app_name)));
        }
        File image = new File(imagesFolder, ".tmpPhotoUpload");
        File nomedia = new File(imagesFolder, ".nomedia");
        nomedia.createNewFile();
        return image;
    }

    public static String valueString(Context context, Map<String, Object> widget) {
        return (String) widget.get("value");
    }
}

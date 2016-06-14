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

package com.mobicage.rogerthat.plugins.scan;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.HomeActivity;
import com.mobicage.rogerthat.IdentityStore;
import com.mobicage.rogerthat.MainActivity;
import com.mobicage.rogerthat.MyIdentity;
import com.mobicage.rogerthat.ServiceBoundActivity;
import com.mobicage.rogerthat.plugins.friends.FriendsPlugin;
import com.mobicage.rogerthat.util.IOUtils;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeBroadcastReceiver;
import com.mobicage.rogerthat.util.system.SafeDialogInterfaceOnClickListener;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.system.SafeViewOnClickListener;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.ui.ImageHelper;
import com.mobicage.rogerthat.util.ui.UIUtils;
import com.mobicage.rpc.config.AppConstants;
import com.soundcloud.android.crop.Crop;
import com.soundcloud.android.crop.CropUtil;

public class ProfileActivity extends ServiceBoundActivity {

    private FriendsPlugin mFriendsPlugin;
    private BroadcastReceiver mBroadcastReceiver;
    private IdentityStore mIdentityStore;
    private MyIdentity mIdentity;
    private boolean mEditing = false;
    private final int AVATAR_SIZE = 150;
    private static final int PICK_IMAGE = 1;

    private Uri mUriSavedImage;
    private boolean mPhotoSelected;
    private int mPhoneExifRotation;
    private boolean mPhotoSelecting;

    private long mGender;
    private Calendar mBirthdateCalender;
    private boolean mNeedBirthdate = false;

    private boolean mShownAfterRegistration;
    public final static String INTENT_KEY_COMPLETE_PROFILE = "completeProfile";

    private final int PERMISSION_REQUEST_CAMERA = 1;

    private void updateProfileForEdit() {
        final Button updateBtn = (Button) findViewById(R.id.update_profile);
        final ImageView qrcode = ((ImageView) findViewById(R.id.qrcode));
        final RelativeLayout updateProfileName = ((RelativeLayout) findViewById(R.id.update_profile_name));
        final LinearLayout updateProfileAvatar = ((LinearLayout) findViewById(R.id.update_profile_avatar));
        final EditText newProfileName = ((EditText) findViewById(R.id.update_profile_name_value));
        final ImageView newProfileAvatar = ((ImageView) findViewById(R.id.update_profile_avatar_img));
        final Button updateAvatarBtn = (Button) findViewById(R.id.update_avatar);

        final ImageView updateProfileBirthdateIcon = ((ImageView) findViewById(R.id.profile_birthdate_edit));
        final ImageView updateProfileGenderIcon = ((ImageView) findViewById(R.id.profile_gender_edit));

        final RelativeLayout updateProfileBirthdate = ((RelativeLayout) findViewById(R.id.profile_birthdate));
        final RelativeLayout updateProfileGender = ((RelativeLayout) findViewById(R.id.profile_gender));

        if (mEditing) {
            updateBtn.setText(R.string.save_profile);

            qrcode.setVisibility(View.GONE);
            updateProfileName.setVisibility(View.VISIBLE);
            updateProfileAvatar.setVisibility(View.VISIBLE);

            updateProfileBirthdateIcon.setVisibility(View.VISIBLE);
            updateProfileGenderIcon.setVisibility(View.VISIBLE);
            updateProfileBirthdate.setBackgroundResource(android.R.drawable.edit_text);
            updateProfileGender.setBackgroundResource(android.R.drawable.edit_text);
            updateProfileName.setBackgroundResource(android.R.drawable.edit_text);

            updateProfileName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (newProfileName.requestFocus()) {
                        int pos = newProfileName.getText().length();
                        newProfileName.setSelection(pos);
                        UIUtils.showKeyboard(getApplicationContext());
                    }
                }
            });

            updateProfileBirthdate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Dialog dialog = new Dialog(ProfileActivity.this);
                    dialog.setContentView(R.layout.profile_update_birthdate);
                    dialog.setTitle(R.string.birthdate);
                    Button saveBirthdateBtn = (Button) dialog.findViewById(R.id.ok);
                    final DatePicker datepickerBirthdate = ((DatePicker) dialog.findViewById(R.id.birthdate_datepicker));
                    final TextView friendBirthdate = ((TextView) findViewById(R.id.profile_birthdate_text));

                    datepickerBirthdate.setDescendantFocusability(DatePicker.FOCUS_BLOCK_DESCENDANTS);

                    saveBirthdateBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mBirthdateCalender.set(Calendar.YEAR, datepickerBirthdate.getYear());
                            mBirthdateCalender.set(Calendar.MONTH, datepickerBirthdate.getMonth());
                            mBirthdateCalender.set(Calendar.DAY_OF_MONTH, datepickerBirthdate.getDayOfMonth());
                            String birthdateString = mIdentity.getDisplayBirthdate(mBirthdateCalender);
                            mNeedBirthdate = false;
                            friendBirthdate.setText(birthdateString);
                            dialog.dismiss();
                        }
                    });

                    datepickerBirthdate.init(mBirthdateCalender.get(Calendar.YEAR),
                        mBirthdateCalender.get(Calendar.MONTH), mBirthdateCalender.get(Calendar.DAY_OF_MONTH), null);
                    dialog.show();
                }
            });

            updateProfileGender.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Dialog dialog = new Dialog(ProfileActivity.this);
                    dialog.setContentView(R.layout.profile_update_gender);
                    dialog.setTitle(R.string.gender);
                    Button saveGenderBtn = (Button) dialog.findViewById(R.id.ok);
                    final RadioButton maleRadioButton = ((RadioButton) dialog.findViewById(R.id.gender_male));
                    final RadioButton femaleRadioButton = ((RadioButton) dialog.findViewById(R.id.gender_female));
                    final TextView friendGender = ((TextView) findViewById(R.id.profile_gender_text));

                    saveGenderBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (femaleRadioButton.isChecked()) {
                                mGender = MyIdentity.GENDER_FEMALE;
                            } else {
                                mGender = MyIdentity.GENDER_MALE;
                            }

                            if (mGender == MyIdentity.GENDER_FEMALE) {
                                friendGender.setText(R.string.female);
                            } else {
                                friendGender.setText(R.string.male);
                            }
                            dialog.dismiss();
                        }
                    });

                    if (mGender == MyIdentity.GENDER_FEMALE) {
                        maleRadioButton.setChecked(false);
                        femaleRadioButton.setChecked(true);
                    } else {
                        maleRadioButton.setChecked(true);
                        femaleRadioButton.setChecked(false);
                    }

                    dialog.show();
                }
            });

            OnClickListener newAvatarListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getNewAvatar(true);
                    UIUtils.hideKeyboard(getApplicationContext(), newProfileName);
                }
            };

            updateAvatarBtn.setOnClickListener(newAvatarListener);
            newProfileAvatar.setOnClickListener(newAvatarListener);

        } else {
            updateBtn.setText(R.string.edit_profile);
            if (AppConstants.FRIENDS_ENABLED)
                qrcode.setVisibility(View.VISIBLE);
            else
                qrcode.setVisibility(View.GONE);
            updateProfileName.setVisibility(View.GONE);
            updateProfileAvatar.setVisibility(View.GONE);

            updateProfileBirthdateIcon.setVisibility(View.GONE);
            updateProfileGenderIcon.setVisibility(View.GONE);
            updateProfileBirthdate.setBackgroundResource(0);
            updateProfileGender.setBackgroundResource(0);
            updateProfileName.setBackgroundResource(0);
            updateProfileBirthdate.setOnClickListener(null);
            updateProfileGender.setOnClickListener(null);

            final byte[] byteArray;
            if (mPhotoSelected) {
                Bitmap bm = BitmapFactory.decodeFile(mUriSavedImage.getPath(), null);
                bm = ImageHelper.rotateBitmap(bm, mPhoneExifRotation);

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bm.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byteArray = stream.toByteArray();
                File image;
                try {
                    image = getTmpUploadPhotoLocation();
                } catch (IOException e) {
                    L.d(e.getMessage());
                    UIUtils.showLongToast(getApplicationContext(), e.getMessage());
                    return;
                }
                image.delete();
                mPhotoSelected = false;
            } else {
                byteArray = null;
            }
            Date d = mBirthdateCalender.getTime();
            long epoch = d.getTime() / 1000;

            mFriendsPlugin.updateProfile(newProfileName.getText().toString(), byteArray, null, epoch, mGender,
                AppConstants.PROFILE_SHOW_GENDER_AND_BIRTHDATE, AppConstants.PROFILE_SHOW_GENDER_AND_BIRTHDATE);

            if (mShownAfterRegistration)
                finish();
        }
    }

    private void getNewAvatar(boolean checkPermission) {
        if (checkPermission) {
            final SafeRunnable continueRunnable = new SafeRunnable() {
                @Override
                protected void safeRun() throws Exception {
                    getNewAvatar(false);
                }
            };

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
            return;
        }
        mPhotoSelecting = true;
        File image;
        try {
            image = getTmpUploadPhotoLocation();
        } catch (IOException e) {
            L.d(e.getMessage());
            UIUtils.showLongToast(getApplicationContext(), e.getMessage());
            return;
        }
        image.delete();
        mUriSavedImage = Uri.fromFile(image);

        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mUriSavedImage);
        cameraIntent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());

        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryIntent.putExtra(MediaStore.EXTRA_OUTPUT, mUriSavedImage);
        galleryIntent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());
        galleryIntent.setType("image/*");

        PackageManager pm = getPackageManager();
        final Intent chooserIntent = Intent.createChooser(galleryIntent, getString(R.string.select_source));
        if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] { cameraIntent });
        }

        startActivityForResult(chooserIntent, PICK_IMAGE);

    }

    private Bitmap squeezeImage(Uri source) {
        Bitmap bm = Bitmap.createScaledBitmap(BitmapFactory.decodeFile(source.getPath()), AVATAR_SIZE, AVATAR_SIZE,
            false);
        try {
            FileOutputStream out = new FileOutputStream(mUriSavedImage.getPath());
            try {
                bm.compress(Bitmap.CompressFormat.PNG, 100, out);
            } finally {
                out.close();
            }
        } catch (Exception e1) {
            L.bug(e1);
            return null;
        }
        mPhoneExifRotation = CropUtil.getExifRotation(source.getPath());
        return ImageHelper.rotateBitmap(bm, mPhoneExifRotation);
    }

    private void beginCrop(Uri source) {
        Uri outputUri = Uri.fromFile(new File(getCacheDir(), "cropped"));
        new Crop(source).output(outputUri).withAspect(AVATAR_SIZE, AVATAR_SIZE).withMaxSize(AVATAR_SIZE, AVATAR_SIZE)
            .start(this);
    }

    private void handleCrop(int resultCode, Intent result) {
        if (resultCode == RESULT_OK) {
            final ImageView newProfileAvatar = ((ImageView) findViewById(R.id.update_profile_avatar_img));
            Bitmap newAvatar = squeezeImage(Crop.getOutput(result));
            if (newAvatar != null) {
                newProfileAvatar.setImageBitmap(newAvatar);
                mPhotoSelected = true;
            }
        } else if (resultCode == Crop.RESULT_ERROR) {
            UIUtils.showLongToast(this, Crop.getError(result).getMessage());
        }
        mPhotoSelecting = false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Crop.REQUEST_CROP) {
            handleCrop(resultCode, data);
        } else if (requestCode == PICK_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null && data.getData() != null) {
                    beginCrop(data.getData());
                } else {
                    beginCrop(mUriSavedImage);
                }
            }
        } else {
            L.bug("Unexpected request code in onActivityResult: " + requestCode);
        }
    }

    @Override
    protected void onServiceBound() {
        final IntentFilter filter = new IntentFilter(IdentityStore.IDENTITY_CHANGED_INTENT);
        mBroadcastReceiver = getBroadcastReceiver();
        registerReceiver(mBroadcastReceiver, filter);

        setContentView(R.layout.profile);

        boolean ageGenderSet = getIntent().getBooleanExtra(INTENT_KEY_COMPLETE_PROFILE, true);
        mShownAfterRegistration = !ageGenderSet;
        if (mShownAfterRegistration) {
            mService.addHighPriorityIntent(IdentityStore.IDENTITY_CHANGED_INTENT);
        }

        mFriendsPlugin = mService.getPlugin(FriendsPlugin.class);
        mIdentityStore = mService.getIdentityStore();

        updateView();
    }

    private void updateView() {
        mIdentity = mIdentityStore.getIdentity();
        final ImageView image = (ImageView) findViewById(R.id.friend_avatar);
        final ImageView newProfileAvatar = ((ImageView) findViewById(R.id.update_profile_avatar_img));

        final Bitmap avatarBitmap = mFriendsPlugin.getAvatarBitmap(mIdentity.getEmail());
        if (avatarBitmap == null) {
            Bitmap mbm = mFriendsPlugin.getMissingFriendAvatarBitmap();
            image.setImageBitmap(mbm);
            newProfileAvatar.setImageBitmap(mbm);
        } else {
            image.setImageBitmap(avatarBitmap);
            newProfileAvatar.setImageBitmap(avatarBitmap);
        }

        final TextView nameView = (TextView) findViewById(R.id.friend_name);
        nameView.setText(mIdentity.getDisplayName());
        final EditText newProfileName = ((EditText) findViewById(R.id.update_profile_name_value));
        newProfileName.setText(mIdentity.getDisplayName());

        final TextView emailView = (TextView) findViewById(R.id.email);
        emailView.setText(mIdentity.getDisplayEmail());

        final TextView friendBirthdate = ((TextView) findViewById(R.id.profile_birthdate_text));
        final TextView friendGender = ((TextView) findViewById(R.id.profile_gender_text));

        mBirthdateCalender = Calendar.getInstance();
        Long birthdate = mIdentity.getBirthdate();
        if (birthdate == null) {
            birthdate = Long.valueOf(0);
            mNeedBirthdate = true;
        }
        mBirthdateCalender.setTimeInMillis(birthdate * 1000);

        String birthdateString = mIdentity.getDisplayBirthdate();
        if (birthdateString == null) {
            birthdateString = getString(R.string.unknown);
        }
        friendBirthdate.setText(birthdateString);

        long myGender = mIdentity.getGender();
        mGender = myGender;
        if (mGender == MyIdentity.GENDER_UNDEFINED) {
            friendGender.setText(R.string.unknown);
        } else if (mGender == MyIdentity.GENDER_MALE) {
            friendGender.setText(R.string.male);
        } else {
            friendGender.setText(R.string.female);
        }

        if (!AppConstants.PROFILE_SHOW_GENDER_AND_BIRTHDATE) {
            findViewById(R.id.profile_birthdate).setVisibility(View.GONE);
            findViewById(R.id.profile_gender).setVisibility(View.GONE);
        }

        LinearLayout profileDataContainer = (LinearLayout) findViewById(R.id.profile_data);
        if (AppConstants.PROFILE_DATA_FIELDS.length > 0) {
            profileDataContainer.removeAllViews();
            profileDataContainer.setVisibility(View.VISIBLE);
            Map<String, String> profileData = mIdentity.getProfileDataDict();
            for (String k : AppConstants.PROFILE_DATA_FIELDS) {
                final LinearLayout ll = (LinearLayout) View.inflate(this, R.layout.profile_data_detail, null);
                final TextView tvKey = (TextView) ll.findViewById(R.id.profile_data_detail_key);
                final TextView tvVal = (TextView) ll.findViewById(R.id.profile_data_detail_value);

                String v = profileData == null ? null : profileData.get(k);
                if (v == null) {
                    v = getString(R.string.unknown);
                }
                tvKey.setText(k);
                tvVal.setText(v);

                profileDataContainer.addView(ll);
            }
        } else {
            profileDataContainer.setVisibility(View.GONE);
        }

        final Button updateBtn = (Button) findViewById(R.id.update_profile);
        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEditing && AppConstants.PROFILE_SHOW_GENDER_AND_BIRTHDATE) {
                    if ((mGender != MyIdentity.GENDER_MALE && mGender != MyIdentity.GENDER_FEMALE) || mNeedBirthdate) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
                        builder.setTitle(R.string.complete_your_profile);

                        StringBuilder errorMessage = new StringBuilder();
                        if (mNeedBirthdate) {
                            errorMessage.append(getString(R.string.missing_birthdate));
                            errorMessage.append("\n");
                        }

                        if (mGender != MyIdentity.GENDER_MALE && mGender != MyIdentity.GENDER_FEMALE) {
                            errorMessage.append(getString(R.string.missing_gender));
                        }

                        builder.setMessage(errorMessage);
                        builder.setCancelable(true);
                        builder.setPositiveButton(R.string.rogerthat, new SafeDialogInterfaceOnClickListener() {
                            @Override
                            public void safeOnClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        builder.create().show();

                        return;
                    }
                }

                updateProfileLayout(!mEditing);
            }
        });

        setNavigationBarVisible(AppConstants.SHOW_NAV_HEADER);
        if (mShownAfterRegistration) {
            L.d("mShownAfterRegistration: " + mShownAfterRegistration);
            updateProfileLayout(true);
            final Button completeProfileSkip = ((Button) findViewById(R.id.complete_profile_skip));

            image.setVisibility(View.GONE);
            nameView.setVisibility(View.GONE);
            emailView.setVisibility(View.GONE);
            completeProfileSkip.setVisibility(View.VISIBLE);

            completeProfileSkip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
            setNavigationBarTitle(R.string.complete_your_profile);
        } else {
            setNavigationBarTitle(R.string.profile);
            findViewById(R.id.navigation_bar_home_button).setOnClickListener(new SafeViewOnClickListener() {
                @Override
                public void safeOnClick(View v) {
                    Intent i = new Intent(ProfileActivity.this, HomeActivity.class);
                    i.setFlags(MainActivity.FLAG_CLEAR_STACK);
                    startActivity(i);
                    finish();
                }
            });
        }

        updateQRBitMap();
    }

    private void updateProfileLayout(boolean goToEditingMode) {
        mEditing = goToEditingMode;
        updateProfileForEdit();
        if (!mEditing) {
            final EditText newProfileName = ((EditText) findViewById(R.id.update_profile_name_value));
            UIUtils.hideKeyboard(getApplicationContext(), newProfileName);
        }
    }

    @Override
    protected void onServiceUnbound() {
        if (mBroadcastReceiver != null)
            unregisterReceiver(mBroadcastReceiver);
    }

    private SafeBroadcastReceiver getBroadcastReceiver() {
        return new SafeBroadcastReceiver() {
            @Override
            public String[] onSafeReceive(Context context, Intent intent) {
                T.UI();
                if (mEditing) {
                    return null;
                }
                if (IdentityStore.IDENTITY_CHANGED_INTENT.equals(intent.getAction())) {
                    updateView();
                }
                return new String[] { intent.getAction() };
            }
        };
    }

    private void updateQRBitMap() {
        ImageView imageView = ((ImageView) findViewById(R.id.qrcode));
        if (AppConstants.FRIENDS_ENABLED) {
            final Bitmap qrBitmap = mIdentity.getQRBitmap();
            if (qrBitmap != null) {
                imageView.setImageBitmap(qrBitmap);
            }
        } else {
            imageView.setVisibility(View.GONE);
        }
    }

    private File getTmpUploadPhotoLocation() throws IOException {
        File imagesFolder = getImagesFolder();
        File image = new File(imagesFolder, ".tmpGroupAvatar");
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

    @Override
    protected void onStop() {
        if (mShownAfterRegistration && !mPhotoSelecting) {
            L.d("goto home activity");
            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            intent.setAction(MainActivity.ACTION_COMPLETE_PROFILE_FINISHED);
            intent.setFlags(MainActivity.FLAG_CLEAR_STACK);
            startActivity(intent);
        }

        super.onStop();
    }

}
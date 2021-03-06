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

package com.mobicage.rogerthat.plugins.scan;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.commonsware.cwac.cam2.Facing;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;
import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.IdentityStore;
import com.mobicage.rogerthat.MainActivity;
import com.mobicage.rogerthat.MyIdentity;
import com.mobicage.rogerthat.ServiceBoundActivity;
import com.mobicage.rogerthat.plugins.friends.FriendsPlugin;
import com.mobicage.rogerthat.util.ActivityUtils;
import com.mobicage.rogerthat.util.IOUtils;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeBroadcastReceiver;
import com.mobicage.rogerthat.util.system.SafeDialogClick;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.ui.ImageHelper;
import com.mobicage.rogerthat.util.ui.UIUtils;
import com.mobicage.rpc.config.AppConstants;
import com.mobicage.rpc.config.LookAndFeelConstants;
import com.soundcloud.android.crop.Crop;
import com.soundcloud.android.crop.CropUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

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

    private int mGender;
    private Calendar mBirthdateCalender;
    private boolean mNeedBirthdate = false;

    private boolean mShownAfterRegistration;
    public final static String INTENT_KEY_COMPLETE_PROFILE = "completeProfile";

    private final int PERMISSION_REQUEST_CAMERA = 1;

    private void updateProfileForEdit(boolean shouldSave) {
        final RelativeLayout friendDetailHeader = ((RelativeLayout) findViewById(R.id.friend_detail_header));
        final LinearLayout updateProfileNameAndAvatar = ((LinearLayout) findViewById(R.id.update_profile_name_and_avatar));
        final EditText newProfileName = ((EditText) findViewById(R.id.update_profile_name_value));
        final FloatingActionButton newProfileAvatar = ((FloatingActionButton) findViewById(R.id.update_profile_avatar_img));
        final ImageButton newAvatarPreview = (ImageButton) findViewById(R.id.new_avatar_preview);
        newProfileAvatar.setImageDrawable(new IconicsDrawable(this, FontAwesome.Icon.faw_camera).color(Color.WHITE).sizeDp(24));

        final ImageView updateProfileBirthdateIcon = (ImageView) findViewById(R.id.profile_birthdate_edit);
        updateProfileBirthdateIcon.setImageDrawable(new IconicsDrawable(this, FontAwesome.Icon.faw_pencil).color(LookAndFeelConstants.getPrimaryIconColor(this)).sizeDp(18).paddingDp(2));
        final ImageView updateProfileGenderIcon = (ImageView) findViewById(R.id.profile_gender_edit);
        updateProfileGenderIcon.setImageDrawable(new IconicsDrawable(this, FontAwesome.Icon.faw_pencil).color(LookAndFeelConstants.getPrimaryIconColor(this)).sizeDp(18).paddingDp(2));

        final LinearLayout updateProfileBirthdate = (LinearLayout) findViewById(R.id.profile_birthdate);
        final LinearLayout updateProfileGender = (LinearLayout) findViewById(R.id.profile_gender);

        updateProfileGenderIcon.setVisibility(mEditing ? View.VISIBLE : View.INVISIBLE);
        if (mEditing) {
            friendDetailHeader.setVisibility(View.GONE);
            updateProfileNameAndAvatar.setVisibility(View.VISIBLE);
            updateProfileBirthdateIcon.setVisibility(View.VISIBLE);

            updateProfileBirthdate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final TextView friendBirthdate = ((TextView) findViewById(R.id.profile_birthdate_text));
                    final View dialogView = getLayoutInflater().inflate(R.layout.ds_date_picker, null);
                    final DatePicker datepickerBirthdate = (DatePicker) dialogView.findViewById(R.id.date_picker);
                    datepickerBirthdate.setDescendantFocusability(DatePicker.FOCUS_BLOCK_DESCENDANTS);
                    datepickerBirthdate.init(mBirthdateCalender.get(Calendar.YEAR),
                            mBirthdateCalender.get(Calendar.MONTH),
                            mBirthdateCalender.get(Calendar.DAY_OF_MONTH),
                            null);
                    SafeDialogClick onPositiveclick = new SafeDialogClick() {
                        @Override
                        public void safeOnClick(DialogInterface di, int id) {
                            mBirthdateCalender.set(Calendar.YEAR, datepickerBirthdate.getYear());
                            mBirthdateCalender.set(Calendar.MONTH, datepickerBirthdate.getMonth());
                            mBirthdateCalender.set(Calendar.DAY_OF_MONTH, datepickerBirthdate.getDayOfMonth());
                            String birthdateString = mIdentity.getDisplayBirthdate(mBirthdateCalender);
                            mNeedBirthdate = false;
                            friendBirthdate.setText(birthdateString);
                        }
                    };
                    UIUtils.showDialog(ProfileActivity.this, null, null, R.string.ok, onPositiveclick, R.string.cancel, null, dialogView);
                }
            });

            updateProfileGender.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final View dialogView = getLayoutInflater().inflate(R.layout.profile_update_gender, null);
                    final RadioButton maleRadioButton = ((RadioButton) dialogView.findViewById(R.id.gender_male));
                    final RadioButton femaleRadioButton = ((RadioButton) dialogView.findViewById(R.id.gender_female));
                    final RadioButton otherRadioButton = ((RadioButton) dialogView.findViewById(R.id.gender_other));
                    final TextView friendGender = ((TextView) findViewById(R.id.profile_gender_text));
                    maleRadioButton.setChecked(false);
                    femaleRadioButton.setChecked(false);
                    otherRadioButton.setChecked(false);
                    if (mGender == MyIdentity.GENDER_FEMALE) {
                        femaleRadioButton.setChecked(true);
                    } else if (mGender == MyIdentity.GENDER_MALE) {
                        maleRadioButton.setChecked(true);
                    } else if (mGender == MyIdentity.GENDER_CUSTOM_FACEBOOK) {
                        otherRadioButton.setChecked(true);
                    }
                    String title = getString(R.string.gender);
                    SafeDialogClick onPositiveClick = new SafeDialogClick() {
                        @Override
                        public void safeOnClick(DialogInterface di, int id) {
                            if (maleRadioButton.isChecked()) {
                                mGender = MyIdentity.GENDER_MALE;
                            } else if (femaleRadioButton.isChecked()) {
                                mGender = MyIdentity.GENDER_FEMALE;
                            } else {
                                mGender = MyIdentity.GENDER_CUSTOM_FACEBOOK;
                            }
                            final int genderTextResource = MyIdentity.getGenderTextResource(mGender);
                            friendGender.setText(genderTextResource);
                        }
                    };
                    UIUtils.showDialog(ProfileActivity.this, title, null, R.string.ok, onPositiveClick,
                            R.string.cancel, null, dialogView);
                }
            });

            OnClickListener newAvatarListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getNewAvatar(true);
                    UIUtils.hideKeyboard(getApplicationContext(), newProfileName);
                }
            };
            newProfileAvatar.setOnClickListener(newAvatarListener);
            newProfileAvatar.setVisibility(View.VISIBLE);
            newAvatarPreview.setOnClickListener(newAvatarListener);
            newAvatarPreview.setVisibility(View.GONE);

        } else {
            friendDetailHeader.setVisibility(View.VISIBLE);
            updateProfileNameAndAvatar.setVisibility(View.GONE);

            updateProfileBirthdateIcon.setVisibility(View.INVISIBLE);
            updateProfileBirthdate.setOnClickListener(null);
            updateProfileGender.setOnClickListener(null);

            if (shouldSave) {
                final byte[] byteArray;
                if (mPhotoSelected) {
                    Bitmap bm = BitmapFactory.decodeFile(mUriSavedImage.getPath(), null);
                    bm = ImageHelper.rotateBitmap(bm, mPhoneExifRotation);

                    final ImageView avatar = (ImageView) findViewById(R.id.friend_avatar);
                    avatar.setImageBitmap(ImageHelper.getRoundedCornerAvatar(bm));

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
            }

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
                            PERMISSION_REQUEST_CAMERA,
                            continueRunnable,
                            showMandatoryPermissionPopup(ProfileActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)))
                        return;
                    continueRunnable.run();
                }
            };
            if (askPermissionIfNeeded(Manifest.permission.CAMERA,
                    PERMISSION_REQUEST_CAMERA,
                    runnableCheckStorage,
                    showMandatoryPermissionPopup(ProfileActivity.this, Manifest.permission.CAMERA)))
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

        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryIntent.putExtra(MediaStore.EXTRA_OUTPUT, mUriSavedImage);
        galleryIntent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());
        galleryIntent.setType("image/*");

        PackageManager pm = getPackageManager();
        final Intent chooserIntent = Intent.createChooser(galleryIntent, getString(R.string.select_source));
        if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            Intent cameraIntent = ActivityUtils.buildTakePictureIntent(this, mUriSavedImage, Facing.FRONT);
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
            final Bitmap newAvatar = squeezeImage(Crop.getOutput(result));
            if (newAvatar != null) {
                mPhotoSelected = true;
                final FloatingActionButton newProfileAvatar = (FloatingActionButton) findViewById(R.id.update_profile_avatar_img);
                final ImageButton newAvatarPreview = (ImageButton) findViewById(R.id.new_avatar_preview);
                newProfileAvatar.setVisibility(View.GONE);
                newAvatarPreview.setVisibility(View.VISIBLE);
                newAvatarPreview.setImageBitmap(ImageHelper.getRoundedCornerAvatar(newAvatar));
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
        setActivityName("profile");

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

        final Bitmap avatarBitmap = mFriendsPlugin.getAvatarBitmap(mIdentity.getEmail());
        if (avatarBitmap == null) {
            Bitmap mbm = mFriendsPlugin.getMissingFriendAvatarBitmap();
            image.setImageBitmap(mbm);
        } else {
            image.setImageBitmap(avatarBitmap);
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

        mGender = mIdentity.getGender();
        friendGender.setText(MyIdentity.getGenderTextResource(mGender));

        if (!AppConstants.PROFILE_SHOW_GENDER_AND_BIRTHDATE) {
            findViewById(R.id.profile_birthdate).setVisibility(View.GONE);
            findViewById(R.id.profile_gender).setVisibility(View.GONE);
            findViewById(R.id.textview_profile_data_usage_info).setVisibility(View.GONE);
        }

        LinearLayout profileDataContainer = (LinearLayout) findViewById(R.id.profile_data);
        if (AppConstants.PROFILE_DATA_FIELDS.length > 0) {
            profileDataContainer.removeAllViews();
            profileDataContainer.setVisibility(View.VISIBLE);
            Map<String, String> profileData = mIdentity.getProfileDataDict();
            for (String k : AppConstants.PROFILE_DATA_FIELDS) {
                final LinearLayout ll = (LinearLayout) View.inflate(this, R.layout.profile_data_detail, null);

                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                layoutParams.setMargins(0, 10, 0, 0);

                final TextView tvKey = (TextView) ll.findViewById(R.id.profile_data_detail_key);
                final TextView tvVal = (TextView) ll.findViewById(R.id.profile_data_detail_value);

                String v = profileData == null ? null : profileData.get(k);
                if (v == null) {
                    v = getString(R.string.unknown);
                }
                tvKey.setText(k);
                tvKey.setTextColor(LookAndFeelConstants.getPrimaryColor(this));
                tvVal.setText(v);

                profileDataContainer.addView(ll, layoutParams);
            }
        } else {
            profileDataContainer.setVisibility(View.GONE);
        }

        if (mShownAfterRegistration) {
            L.d("mShownAfterRegistration: " + mShownAfterRegistration);
            updateProfileLayout(true, false);

            image.setVisibility(View.GONE);
            nameView.setVisibility(View.GONE);
            emailView.setVisibility(View.GONE);
            setTitle(R.string.complete_your_profile);
            setNavigationBarBurgerVisible(false);
            setNavigationBarIcon(null);
        } else {
            setTitle(R.string.profile);
        }

        updateQRBitMap();
    }

    private void updateProfileLayout(boolean goToEditingMode, boolean shouldSave) {
        mEditing = goToEditingMode;
        updateProfileForEdit(shouldSave);
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
        final Bitmap qrBitmap = mIdentity.getQRBitmap();
        if (qrBitmap != null) {
            imageView.setImageBitmap(qrBitmap);
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
            intent.setFlags(MainActivity.FLAG_CLEAR_STACK_SINGLE_TOP);
            startActivity(intent);
        }

        super.onStop();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            switch (item.getItemId()) {
                case R.id.edit_profile:
                    item.setVisible(!mEditing);
                    break;
                case R.id.save_profile:
                    item.setVisible(mEditing);
                    break;
                case R.id.cancel_edit_profile:
                    item.setVisible(mEditing);
                    break;
            }
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        T.UI();
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.edit_profile_menu, menu);
        addIconToMenuItem(menu, R.id.edit_profile, FontAwesome.Icon.faw_pencil);
        addIconToMenuItem(menu, R.id.save_profile, FontAwesome.Icon.faw_check);
        addIconToMenuItem(menu, R.id.cancel_edit_profile, FontAwesome.Icon.faw_times);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        T.UI();
        switch (item.getItemId()) {
            case R.id.edit_profile:
                updateProfileLayout(true, false);
                supportInvalidateOptionsMenu();
                return true;
            case R.id.save_profile:
                if (mEditing && AppConstants.PROFILE_SHOW_GENDER_AND_BIRTHDATE) {
                    final boolean needsGender = !MyIdentity.ALLOWED_GENDERS.contains(mGender);
                    if (needsGender || mNeedBirthdate) {
                        String title = getString(R.string.complete_your_profile);
                        StringBuilder errorMessage = new StringBuilder();
                        if (mNeedBirthdate) {
                            errorMessage.append(getString(R.string.missing_birthdate));
                            errorMessage.append("\n");
                        }

                        if (needsGender) {
                            errorMessage.append(getString(R.string.missing_gender));
                        }
                        UIUtils.showDialog(ProfileActivity.this, title, errorMessage.toString());
                        return true;
                    }
                }
                updateProfileLayout(false, true);
                supportInvalidateOptionsMenu();
                return true;
            case R.id.cancel_edit_profile:
                updateProfileLayout(false, false);
                supportInvalidateOptionsMenu();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

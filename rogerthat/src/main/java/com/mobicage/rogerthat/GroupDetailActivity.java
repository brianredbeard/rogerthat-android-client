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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.plugins.friends.Friend;
import com.mobicage.rogerthat.plugins.friends.FriendsPlugin;
import com.mobicage.rogerthat.plugins.friends.Group;
import com.mobicage.rogerthat.util.IOUtils;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeBroadcastReceiver;
import com.mobicage.rogerthat.util.system.SafeViewOnClickListener;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.ui.ImageHelper;
import com.mobicage.rogerthat.util.ui.SeparatedListAdapter;
import com.mobicage.rogerthat.util.ui.UIUtils;
import com.mobicage.rpc.config.AppConstants;
import com.soundcloud.android.crop.Crop;
import com.soundcloud.android.crop.CropUtil;

public class GroupDetailActivity extends ServiceBoundActivity {

    public final static String GUID = "guid";
    public final static String NEW_GROUP = "new_group";
    private Group mGroup;
    private List<String> mBackupMembers;
    private boolean mIsNewGroup;

    private FriendsPlugin mFriendsPlugin;
    private boolean mEditing = false;
    private final int AVATAR_SIZE = 150;
    private static final int PICK_IMAGE = 1;

    private Uri mUriSavedImage;
    private boolean mPhotoSelected;
    private int mPhoneExifRotation;

    private ListView mListView;
    private LinearLayout mLinearLayout;
    private Cursor mCursorFriends = null;
    private Cursor mCursorGroups = null;

    private void updateGroupForEdit() {
        T.UI();
        final Button saveBtn = (Button) findViewById(R.id.save_group);
        final ImageView editBtn = (ImageView) findViewById(R.id.edit_group);
        final RelativeLayout updateGroupName = ((RelativeLayout) findViewById(R.id.update_group_name));
        final LinearLayout updateGroupAvatar = ((LinearLayout) findViewById(R.id.update_group_avatar));
        final EditText newGroupName = ((EditText) findViewById(R.id.update_group_name_value));
        final ImageView newGroupAvatar = ((ImageView) findViewById(R.id.update_group_avatar_img));
        final Button updateAvatarBtn = (Button) findViewById(R.id.update_avatar);
        final Button cancelBtn = (Button) findViewById(R.id.cancel);

        final ImageView friendAvatar = (ImageView) findViewById(R.id.friend_avatar);
        final TextView friendName = (TextView) findViewById(R.id.friend_name);

        if (mEditing) {
            updateGroupName.setVisibility(View.VISIBLE);
            updateGroupAvatar.setVisibility(View.VISIBLE);
            updateGroupName.setBackgroundResource(android.R.drawable.edit_text);
            cancelBtn.setVisibility(View.VISIBLE);
            saveBtn.setVisibility(View.VISIBLE);
            editBtn.setVisibility(View.GONE);

            friendAvatar.setVisibility(View.GONE);
            friendName.setVisibility(View.GONE);

            updateGroupName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (newGroupName.requestFocus()) {
                        int pos = newGroupName.getText().length();
                        newGroupName.setSelection(pos);
                        UIUtils.showKeyboard(getApplicationContext());
                    }
                }
            });

            OnClickListener newAvatarListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getNewAvatar();
                    UIUtils.hideKeyboard(getApplicationContext(), newGroupName);
                }
            };

            updateAvatarBtn.setOnClickListener(newAvatarListener);
            newGroupAvatar.setOnClickListener(newAvatarListener);

        } else {
            updateGroupName.setVisibility(View.GONE);
            updateGroupAvatar.setVisibility(View.GONE);
            updateGroupName.setBackgroundResource(0);
            cancelBtn.setVisibility(View.GONE);
            saveBtn.setVisibility(View.GONE);
            editBtn.setVisibility(View.VISIBLE);

            friendAvatar.setVisibility(View.VISIBLE);
            friendName.setVisibility(View.VISIBLE);

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
                mGroup.avatar = byteArray;
            }
            mGroup.name = newGroupName.getText().toString();
            mFriendsPlugin.getStore().updateGroup(mGroup.guid, mGroup.name, mGroup.avatar, null);
            mFriendsPlugin.putGroup(mGroup);

            mBackupMembers = new ArrayList<String>(mGroup.members);

            Intent intent = new Intent(mIsNewGroup ? FriendsPlugin.GROUP_ADDED : FriendsPlugin.GROUP_MODIFIED);
            intent.putExtra("guid", mGroup.guid);
            mService.sendBroadcast(intent);
        }
    }

    private void getNewAvatar() {
        T.UI();
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
        T.UI();
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
        T.UI();
        Uri outputUri = Uri.fromFile(new File(getCacheDir(), "cropped"));
        new Crop(source).output(outputUri).withAspect(AVATAR_SIZE, AVATAR_SIZE).withMaxSize(AVATAR_SIZE, AVATAR_SIZE)
            .start(this);
    }

    private void handleCrop(int resultCode, Intent result) {
        T.UI();
        if (resultCode == RESULT_OK) {
            final ImageView newGroupAvatar = ((ImageView) findViewById(R.id.update_group_avatar_img));
            Bitmap newAvatar = squeezeImage(Crop.getOutput(result));
            if (newAvatar != null) {
                newGroupAvatar.setImageBitmap(newAvatar);
                mPhotoSelected = true;
            }
        } else if (resultCode == Crop.RESULT_ERROR) {
            UIUtils.showLongToast(this, Crop.getError(result).getMessage());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        T.UI();
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

    private final BroadcastReceiver mBroadcastReceiver = new SafeBroadcastReceiver() {
        @Override
        public String[] onSafeReceive(Context context, Intent intent) {
            T.UI();
            if (mGroup != null) {
                mGroup = mFriendsPlugin.getStore().getGroup(mGroup.guid);
                if (mGroup == null) {
                    finish();
                }
                updateView();
            }
            return new String[] { intent.getAction() };
        }
    };

    @Override
    protected void onServiceBound() {
        T.UI();
        registerReceiver(mBroadcastReceiver, new IntentFilter(FriendsPlugin.GROUPS_UPDATED));

        mLinearLayout = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.group_detail, null);
        setContentView(mLinearLayout);

        mFriendsPlugin = mService.getPlugin(FriendsPlugin.class);
        Intent intent = getIntent();
        String guid = intent.getStringExtra(GUID);
        mIsNewGroup = intent.getBooleanExtra(NEW_GROUP, false);
        if (mIsNewGroup) {
            updateGroupLayout(true);
        }
        mGroup = mFriendsPlugin.getStore().getGroup(guid);
        mBackupMembers = new ArrayList<String>(mGroup.members);
        updateView();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        T.UI();
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.group_detail_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        T.UI();
        switch (item.getItemId()) {
        case R.id.delete_group:
            deleteGroup();
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateView() {
        T.UI();
        final ImageView image = (ImageView) findViewById(R.id.friend_avatar);
        final ImageView newGroupAvatar = ((ImageView) findViewById(R.id.update_group_avatar_img));

        final Bitmap avatarBitmap = mFriendsPlugin.toGroupBitmap(mGroup.avatar);
        image.setImageBitmap(avatarBitmap);
        newGroupAvatar.setImageBitmap(avatarBitmap);

        final TextView nameView = (TextView) findViewById(R.id.friend_name);
        nameView.setText(mGroup.name);
        final EditText newGroupName = ((EditText) findViewById(R.id.update_group_name_value));
        newGroupName.setText(mGroup.name);

        final Button saveBtn = (Button) findViewById(R.id.save_group);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEditing) {
                    if (newGroupName.getText().length() == 0) {
                        new AlertDialog.Builder(GroupDetailActivity.this).setMessage(R.string.name_required)
                            .setPositiveButton(R.string.rogerthat, null).create().show();
                        return;
                    }

                    if (mGroup.members.size() == 0) {
                        new AlertDialog.Builder(GroupDetailActivity.this).setMessage(R.string.members_are_required)
                            .setPositiveButton(R.string.rogerthat, null).create().show();
                        return;
                    }
                }
                updateGroupLayout(!mEditing);
                // We need to remove all the views because of a bug in samsung galaxy S2
                // which does not remove items in the listview if smaller then the screen
                mLinearLayout.removeAllViews();
                mLinearLayout = (LinearLayout) LayoutInflater.from(GroupDetailActivity.this).inflate(
                    R.layout.group_detail, null);
                setContentView(mLinearLayout);

                // END
                updateView();
            }
        });

        final ImageView editBtn = (ImageView) findViewById(R.id.edit_group);
        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                T.UI();
                updateGroupLayout(!mEditing);
                updateView();
            }
        });

        final Button cancelBtn = (Button) findViewById(R.id.cancel);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                T.UI();
                if (mIsNewGroup) {
                    mFriendsPlugin.getStore().deleteGroup(mGroup.guid);

                    Intent intent = new Intent(FriendsPlugin.GROUP_REMOVED);
                    intent.putExtra("guid", mGroup.guid);
                    mService.sendBroadcast(intent);
                    mGroup = null;
                }
                finish();
            }
        });

        initListView();

        setNavigationBarVisible(AppConstants.SHOW_NAV_HEADER);
        setNavigationBarTitle(mGroup.name);
        findViewById(R.id.navigation_bar_home_button).setOnClickListener(new SafeViewOnClickListener() {
            @Override
            public void safeOnClick(View v) {
                T.UI();
                Intent i = new Intent(GroupDetailActivity.this, HomeActivity.class);
                i.setFlags(MainActivity.FLAG_CLEAR_STACK);
                startActivity(i);
                finish();
            }
        });
    }

    private void deleteGroup() {
        T.UI();
        mFriendsPlugin.getStore().deleteGroup(mGroup.guid);

        if (!mIsNewGroup)
            mFriendsPlugin.deleteGroup(mGroup.guid);

        Intent intent = new Intent(FriendsPlugin.GROUP_REMOVED);
        intent.putExtra("guid", mGroup.guid);
        mService.sendBroadcast(intent);

        finish();
    }

    private void updateGroupLayout(boolean goToEditingMode) {
        T.UI();
        mEditing = goToEditingMode;
        updateGroupForEdit();
        if (!mEditing) {
            final EditText newGroupName = ((EditText) findViewById(R.id.update_group_name_value));
            UIUtils.hideKeyboard(getApplicationContext(), newGroupName);
        }
    }

    @Override
    protected void onServiceUnbound() {
        T.UI();
        if (mGroup != null) {
            for (String member : mBackupMembers) {
                if (!mGroup.members.contains(member)) {
                    mFriendsPlugin.getStore().insertGroupMember(mGroup.guid, member);
                    mGroup.members.add(member);
                }
            }

            for (String member : mGroup.members) {
                if (!mBackupMembers.contains(member)) {
                    mFriendsPlugin.getStore().deleteGroupMember(mGroup.guid, member);
                    mGroup.members.remove(member);
                }
            }
            if (mGroup.members.size() == 0) {
                mFriendsPlugin.getStore().deleteGroup(mGroup.guid);
                if (!mIsNewGroup)
                    mFriendsPlugin.deleteGroup(mGroup.guid);

                Intent intent = new Intent(FriendsPlugin.GROUP_REMOVED);
                intent.putExtra("guid", mGroup.guid);
                mService.sendBroadcast(intent);
            }
        }

        if (mBroadcastReceiver != null)
            unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mCursorFriends != null) {
            stopManagingCursor(mCursorFriends);
        }
        if (mCursorGroups != null) {
            stopManagingCursor(mCursorGroups);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getWasPaused() && mCursorFriends != null) {
            startManagingCursor(mCursorFriends);
        }
        if (getWasPaused() && mCursorGroups != null) {
            startManagingCursor(mCursorGroups);
        }
    }

    private File getTmpUploadPhotoLocation() throws IOException {
        T.UI();
        File imagesFolder = getImagesFolder();
        File image = new File(imagesFolder, ".tmpGroupAvatar");
        File nomedia = new File(imagesFolder, ".nomedia");
        nomedia.createNewFile();
        return image;
    }

    private File getImagesFolder() throws IOException {
        T.UI();
        File imagesFolder = new File(IOUtils.getFilesDirectory(this), "images");
        if (!imagesFolder.exists() && !imagesFolder.mkdirs()) {
            throw new IOException(getString(R.string.unable_to_create_images_directory, getString(R.string.app_name)));
        }
        return imagesFolder;
    }

    private void setViewColors(View view, TextView name, boolean selected) {
        T.UI();
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

    private void initListView() {
        T.UI();
        mListView = (ListView) findViewById(R.id.group_listview);

        mCursorFriends = mFriendsPlugin.getStore().getUserFriendListCursor();
        startManagingCursor(mCursorFriends);

        FriendListAdapter fla = new FriendListAdapter(this, mCursorFriends, mFriendsPlugin.getStore(),
            new FriendListAdapter.ViewUpdater() {
                @Override
                public void update(View view) {
                    T.UI();
                    Friend friend = (Friend) view.getTag();
                    TextView name = (TextView) view.findViewById(R.id.friend_name);
                    boolean selected = mGroup.members.contains(friend.email);
                    setViewColors(view, name, selected);
                }
            }, mFriendsPlugin, false, null);

        mCursorGroups = mFriendsPlugin.getStore().getGroupMemberListCursor(mGroup.guid);
        startManagingCursor(mCursorGroups);

        GroupMemberListAdaptor gmla = new GroupMemberListAdaptor(this, mCursorGroups,
            mFriendsPlugin.getStore(), new GroupMemberListAdaptor.ViewUpdater() {
                @Override
                public void update(View view) {
                    T.UI();
                    Friend friend = (Friend) view.getTag();
                    TextView name = (TextView) view.findViewById(R.id.friend_name);
                    boolean selected = mGroup.members.contains(friend.email);
                    setViewColors(view, name, selected);
                }
            }, mFriendsPlugin, false);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
                T.UI();
                if (mEditing) {
                    try {
                        final TextView name = (TextView) view.findViewById(R.id.friend_name);

                        String newGroupNameBackup = ((EditText) findViewById(R.id.update_group_name_value)).getText()
                            .toString();
                        Drawable newGroupAvatarBackup = ((ImageView) findViewById(R.id.update_group_avatar_img))
                            .getDrawable();

                        final Friend friend = (Friend) view.getTag();
                        boolean selected = mGroup.members.contains(friend.email);
                        if (selected) {
                            mGroup.members.remove(friend.email);
                            mFriendsPlugin.getStore().deleteGroupMember(mGroup.guid, friend.email);
                        } else {
                            mGroup.members.add(friend.email);
                            mFriendsPlugin.getStore().insertGroupMember(mGroup.guid, friend.email);
                        }

                        int lastViewedPosition = mListView.getFirstVisiblePosition();
                        View v = mListView.getChildAt(0);
                        int topOffset = (v == null) ? 0 : v.getTop();

                        // We need to remove all the views because of a bug in samsung galaxy S2
                        // which does not remove items in the listview if smaller then the screen
                        mLinearLayout.removeAllViews();
                        mLinearLayout = (LinearLayout) LayoutInflater.from(GroupDetailActivity.this).inflate(
                            R.layout.group_detail, null);
                        setContentView(mLinearLayout);

                        updateGroupLayout(mEditing);
                        updateView();

                        if (mPhotoSelected) {
                            ((ImageView) findViewById(R.id.update_group_avatar_img))
                                .setImageDrawable(newGroupAvatarBackup);
                        }
                        ((EditText) findViewById(R.id.update_group_name_value)).setText(newGroupNameBackup);
                        // END

                        mListView.setSelectionFromTop(lastViewedPosition, topOffset);
                        setViewColors(view, name, !selected);
                    } catch (Exception e) {
                        L.bug(e);
                    }
                }
            }
        });

        SeparatedListAdapter adapter = new SeparatedListAdapter(this);

        adapter.addSection(mService.getString(R.string.members), gmla);
        if (mEditing)
            adapter.addSection(mService.getString(R.string.all_friends), fla);

        mListView.setAdapter(adapter);
    }
}
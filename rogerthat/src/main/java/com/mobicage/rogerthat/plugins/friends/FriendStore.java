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

package com.mobicage.rogerthat.plugins.friends;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDoneException;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.util.SparseIntArray;
import android.view.WindowManager;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.MainService;
import com.mobicage.rogerthat.util.TextUtils;
import com.mobicage.rogerthat.util.ZipUtils;
import com.mobicage.rogerthat.util.db.DatabaseManager;
import com.mobicage.rogerthat.util.db.MultiThreadedSQLStatement;
import com.mobicage.rogerthat.util.db.SimpleCache;
import com.mobicage.rogerthat.util.db.Transaction;
import com.mobicage.rogerthat.util.db.TransactionHelper;
import com.mobicage.rogerthat.util.db.TransactionWithoutResult;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.ui.ImageHelper;
import com.mobicage.to.friends.FriendCategoryTO;
import com.mobicage.to.friends.FriendTO;
import com.mobicage.to.friends.GetAvatarRequestTO;
import com.mobicage.to.friends.GetCategoryRequestTO;
import com.mobicage.to.friends.GetUserInfoResponseTO;
import com.mobicage.to.friends.ServiceMenuItemTO;
import com.mobicage.to.friends.UpdateFriendResponseTO;
import com.mobicage.to.service.GetMenuIconRequestTO;

import org.jivesoftware.smack.util.Base64;

import java.io.Closeable;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.mobicage.rogerthat.util.db.DBUtils.bindString;

public class FriendStore implements Closeable {

    public final static int SERVICE_ORGANIZATION_TYPE_UNSPECIFIED = -1;
    public final static int SERVICE_ORGANIZATION_TYPE_NON_PROFIT = 1;
    public final static int SERVICE_ORGANIZATION_TYPE_PROFIT = 2;
    public final static int SERVICE_ORGANIZATION_TYPE_CITY = 3;
    public final static int SERVICE_ORGANIZATION_TYPE_EMERGENCY = 4;

    public final static BitmapHolder FRIEND_WITHOUT_AVATAR = new BitmapHolder(null);

    private static class IllegalFriendVersionBumpException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public IllegalFriendVersionBumpException(String m) {
            super(m);
        }
    }

    private static class IllegalFriendVersionComparisonException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public IllegalFriendVersionComparisonException(long[] versionsInDB, long[] versionsFromServer) {
            super("Can not compare when versionsInDB " + versionsInDB + " length is greater than versionsFromServer "
                + versionsFromServer);
        }
    }

    private final SQLiteDatabase mDb;
    private final MainService mMainService;
    private final SimpleCache<String, BitmapHolder> mAvatarBitmapCacheUI;
    private final SimpleCache<String, String> mFriendNameCache;

    // Statements for UI thread
    private final SQLiteStatement mGetFriendCountUI;
    private final SQLiteStatement mGetLocationSharingFriendCountUI;
    private final SQLiteStatement mGetIsFriendUI;
    private final SQLiteStatement mGetEmailByEmailHashUI;
    private final SQLiteStatement mGetInvitationSecretUI;
    private final SQLiteStatement mDeleteInvitationSecretUI;
    private final SQLiteStatement mCountInvitationSecretUI;
    private final SQLiteStatement mInsertPendingInvitationUI;
    private final SQLiteStatement mRemovePendingInvitationUI;
    private final SQLiteStatement mGetStaticFlowUI;

    // Statements for HTTP thread
    private final SQLiteStatement mInsertFriendHTTP;
    private final SQLiteStatement mUpdateFriendAvatarHTTP;
    private final SQLiteStatement mGetFriendAvatarIdHTTP;
    private final SQLiteStatement mUpdateFriendHTTP;
    private final SQLiteStatement mUpdateFriendInfoHTTP;
    private final SQLiteStatement mUpdateFriendShareLocationHTTP;
    private final SQLiteStatement mUpdateFriendExistenceHTTP;
    private final SQLiteStatement mUpdateFriendExistenceAndClearVersionHTTP;
    private final SQLiteStatement mInsertInvitationSecretHTTP;
    private final SQLiteStatement mDeleteAllServiceMenusHTTP;
    private final SQLiteStatement mDeleteServiceMenuHTTP;
    private final SQLiteStatement mInsertServiceMenuHTTP;
    private final SQLiteStatement mCheckMenuIconAvailableHTTP;
    private final SQLiteStatement mInsertMenuIconHTTP;
    private final SQLiteStatement mDeleteMenuIconHTTP;
    private final SQLiteStatement mCountUnprocessedMessageForSenderHTTP;

    private final SQLiteStatement mCheckStaticFlowAvailableHTTP;
    private final MultiThreadedSQLStatement mInsertStaticFlow;
    private final SQLiteStatement mDeleteStaticFlowHTTP;

    private final SQLiteStatement mCategoryInsertHTTP;
    private final SQLiteStatement mCategoryExistsHTTP;

    private final SQLiteStatement mServiceApiCallInsertHTTP;
    private final SQLiteStatement mServiceApiCallSetResultHTTP;
    private final SQLiteStatement mServiceApiCallRemoveUI;

    private final SQLiteStatement mServiceUserDataUpdateBIZZ;
    private final SQLiteStatement mServiceAppDataUpdateBIZZ;
    private final SQLiteStatement mServiceDataUpdateBIZZ;

    private final SQLiteStatement mGetFriendVersionsBizz;
    private final SQLiteStatement mFriendSetVersionGetBizz;
    private final SQLiteStatement mFriendSetVersionSetBizz;
    private final SQLiteStatement mFriendSetDeleteFromBizz;
    private final SQLiteStatement mFriendSetInsertIntoBizz;
    private final SQLiteStatement mFriendSetContainsBizz;

    private final SQLiteStatement mFriendUpdateEmailHashBizz;

    private final MultiThreadedSQLStatement mInsertGroup;
    private final SQLiteStatement mUpdateGroupUI;
    private final SQLiteStatement mDeleteGroupUI;
    private final SQLiteStatement mDeleteGroupMembersUI;
    private final MultiThreadedSQLStatement mInsertGroupMember;
    private final SQLiteStatement mDeleteGroupMemberUI;
    private final SQLiteStatement mClearGroupBIZZ;
    private final SQLiteStatement mClearEmptyGroupBIZZ;
    private final SQLiteStatement mClearGroupMemberBIZZ;
    private final SQLiteStatement mClearGroupMemberByEmailBIZZ;
    private final SQLiteStatement mInsertGroupAvatarBIZZ;
    private final SQLiteStatement mInsertGroupAvatarHashBIZZ;

    private final SQLiteStatement mCountServicesByOrganizationType;

    private final MultiThreadedSQLStatement mGetFriendName;
    private final PhoneContacts mPhoneContacts;
    private final int mIconSize;

    public FriendStore(final DatabaseManager databaseManager, final MainService mainService) {
        T.UI();
        mDb = databaseManager.getDatabase();
        mMainService = mainService;

        mAvatarBitmapCacheUI = new SimpleCache<String, BitmapHolder>(50, 120);
        mFriendNameCache = new SimpleCache<String, String>(200, 120);

        mGetFriendCountUI = mDb.compileStatement(mMainService.getString(R.string.sql_friend_count));
        mGetLocationSharingFriendCountUI = mDb.compileStatement(mMainService
            .getString(R.string.sql_friend_count_friends_sharing_location));
        mGetFriendName = new MultiThreadedSQLStatement(mDb, new int[] { T.UI, T.BIZZ },
            mMainService.getString(R.string.sql_friend_get_name_by_email));

        mGetIsFriendUI = mDb.compileStatement(mMainService.getString(R.string.sql_friend_is_friend));
        mGetEmailByEmailHashUI = mDb.compileStatement(mMainService
            .getString(R.string.sql_friend_get_email_by_email_hash));

        mInsertFriendHTTP = mDb.compileStatement(mMainService.getString(R.string.sql_friend_insert));
        mUpdateFriendAvatarHTTP = mDb.compileStatement(mMainService.getString(R.string.sql_friend_update_avatar));
        mGetFriendAvatarIdHTTP = mDb.compileStatement(mMainService.getString(R.string.sql_friend_get_avatar_id));
        mUpdateFriendHTTP = mDb.compileStatement(mMainService.getString(R.string.sql_friend_update));
        mUpdateFriendInfoHTTP = mDb.compileStatement(mMainService.getString(R.string.sql_friend_update_info));
        mDeleteAllServiceMenusHTTP = mDb.compileStatement(mMainService
            .getString(R.string.sql_friend_clear_all_service_menu));
        mUpdateFriendShareLocationHTTP = mDb.compileStatement(mMainService
            .getString(R.string.sql_friend_update_share_location));
        mUpdateFriendExistenceHTTP = mDb.compileStatement(mMainService.getString(R.string.sql_friend_update_existence));
        mUpdateFriendExistenceAndClearVersionHTTP = mDb.compileStatement(mMainService
            .getString(R.string.sql_friend_update_existence_and_clear_version));

        mInsertInvitationSecretHTTP = mDb.compileStatement(mMainService
            .getString(R.string.sql_friend_invitation_secret_insert));
        mGetInvitationSecretUI = mDb
            .compileStatement(mMainService.getString(R.string.sql_friend_invitation_secret_get));
        mDeleteInvitationSecretUI = mDb.compileStatement(mMainService
            .getString(R.string.sql_friend_invitation_secret_delete));
        mCountInvitationSecretUI = mDb.compileStatement(mMainService
            .getString(R.string.sql_friend_invitation_secret_count));

        mInsertPendingInvitationUI = mDb.compileStatement(mMainService
            .getString(R.string.sql_friend_pending_invitation_insert));
        mRemovePendingInvitationUI = mDb.compileStatement(mMainService
            .getString(R.string.sql_friend_pending_invitation_remove));

        mDeleteServiceMenuHTTP = mDb.compileStatement(mMainService.getString(R.string.sql_friend_delete_service_menu));
        mInsertServiceMenuHTTP = mDb.compileStatement(mMainService.getString(R.string.sql_friend_insert_service_menu));
        mCheckMenuIconAvailableHTTP = mDb.compileStatement(mMainService
            .getString(R.string.sql_friend_check_menu_icon_available));
        mInsertMenuIconHTTP = mDb.compileStatement(mMainService.getString(R.string.sql_friend_insert_menu_icon));
        mDeleteMenuIconHTTP = mDb.compileStatement(mMainService.getString(R.string.sql_friend_delete_menu_icon));
        mCountUnprocessedMessageForSenderHTTP = mDb.compileStatement(mMainService
            .getString(R.string.sql_message_get_unprocessed_message_count_for_sender));

        mGetStaticFlowUI = mDb.compileStatement(mMainService.getString(R.string.sql_friend_static_flow_get));
        mCheckStaticFlowAvailableHTTP = mDb.compileStatement(mMainService
            .getString(R.string.sql_friend_static_flow_check_available));

        mInsertStaticFlow = new MultiThreadedSQLStatement(mDb, new int[] { T.UI, T.BIZZ },
            mMainService.getString(R.string.sql_friend_static_flow_insert));
        mDeleteStaticFlowHTTP = mDb.compileStatement(mMainService.getString(R.string.sql_friend_static_flow_delete));

        mCategoryInsertHTTP = mDb.compileStatement(mMainService.getString(R.string.sql_friend_category_insert));
        mCategoryExistsHTTP = mDb.compileStatement(mMainService.getString(R.string.sql_friend_category_exists));

        mServiceApiCallInsertHTTP = mDb.compileStatement(mMainService.getString(R.string.sql_service_api_call_insert));
        mServiceApiCallSetResultHTTP = mDb.compileStatement(mMainService
            .getString(R.string.sql_service_api_call_set_result));
        mServiceApiCallRemoveUI = mDb.compileStatement(mMainService.getString(R.string.sql_service_api_call_remove));

        mServiceUserDataUpdateBIZZ = mDb.compileStatement(mMainService.getString(R.string.sql_friend_set_user_data));
        mServiceAppDataUpdateBIZZ = mDb.compileStatement(mMainService.getString(R.string.sql_friend_set_app_data));
        mServiceDataUpdateBIZZ = mDb.compileStatement(mMainService.getString(R.string.sql_friend_set_data));

        mGetFriendVersionsBizz = mDb.compileStatement(mMainService.getString(R.string.sql_friend_get_versions));
        mFriendSetVersionGetBizz = mDb.compileStatement(mMainService.getString(R.string.sql_friendset_version_get));
        mFriendSetVersionSetBizz = mDb.compileStatement(mMainService.getString(R.string.sql_friendset_version_set));
        mFriendSetDeleteFromBizz = mDb.compileStatement(mMainService.getString(R.string.sql_friendset_delete_from));
        mFriendSetInsertIntoBizz = mDb.compileStatement(mMainService.getString(R.string.sql_friendset_insert_into));
        mFriendSetContainsBizz = mDb.compileStatement(mMainService.getString(R.string.sql_friendset_contains));

        mFriendUpdateEmailHashBizz = mDb.compileStatement(mainService.getString(R.string.sql_friend_update_email_hash));

        mInsertGroup = new MultiThreadedSQLStatement(mDb, new int[] { T.UI, T.BIZZ },
            mMainService.getString(R.string.sql_insert_group));
        mUpdateGroupUI = mDb.compileStatement(mMainService.getString(R.string.sql_update_group));
        mDeleteGroupUI = mDb.compileStatement(mMainService.getString(R.string.sql_delete_group));
        mDeleteGroupMembersUI = mDb.compileStatement(mMainService.getString(R.string.sql_delete_group_members));
        mInsertGroupMember = new MultiThreadedSQLStatement(mDb, new int[] { T.UI, T.BIZZ },
            mMainService.getString(R.string.sql_insert_group_member));
        mDeleteGroupMemberUI = mDb.compileStatement(mMainService.getString(R.string.sql_delete_group_member));
        mClearGroupBIZZ = mDb.compileStatement(mMainService.getString(R.string.sql_clear_group));
        mClearEmptyGroupBIZZ = mDb.compileStatement(mMainService.getString(R.string.sql_clear_empty_group));
        mClearGroupMemberBIZZ = mDb.compileStatement(mMainService.getString(R.string.sql_clear_group_member));
        mClearGroupMemberByEmailBIZZ = mDb.compileStatement(mMainService
            .getString(R.string.sql_clear_group_member_by_email));
        mInsertGroupAvatarBIZZ = mDb.compileStatement(mMainService.getString(R.string.sql_insert_group_avatar));
        mInsertGroupAvatarHashBIZZ = mDb
            .compileStatement(mMainService.getString(R.string.sql_insert_group_avatar_hash));

        mCountServicesByOrganizationType = mDb.compileStatement(mMainService
            .getString(R.string.sql_services_count_by_organization_type));

        mPhoneContacts = new PhoneContacts(mMainService.getContentResolver());

        DisplayMetrics metrics = new DisplayMetrics();
        ((WindowManager) mMainService.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metrics);
        mIconSize = 50 * metrics.widthPixels / 320;
    }

    void runInTransaction(final String name, final TransactionWithoutResult txn) {
        TransactionHelper.runInTransaction(mDb, name, txn);
    }

    private void closeSQLStatements() {
        T.UI();

        mGetFriendCountUI.close();
        mGetLocationSharingFriendCountUI.close();
        mGetIsFriendUI.close();
        mGetEmailByEmailHashUI.close();
        mGetInvitationSecretUI.close();
        mDeleteInvitationSecretUI.close();
        mCountInvitationSecretUI.close();
        mInsertPendingInvitationUI.close();
        mRemovePendingInvitationUI.close();
        mGetStaticFlowUI.close();

        mInsertFriendHTTP.close();
        mUpdateFriendAvatarHTTP.close();
        mGetFriendAvatarIdHTTP.close();
        mUpdateFriendHTTP.close();
        mUpdateFriendInfoHTTP.close();
        mUpdateFriendShareLocationHTTP.close();
        mUpdateFriendExistenceHTTP.close();
        mUpdateFriendExistenceAndClearVersionHTTP.close();
        mInsertInvitationSecretHTTP.close();
        mDeleteAllServiceMenusHTTP.close();
        mDeleteServiceMenuHTTP.close();
        mInsertServiceMenuHTTP.close();
        mCheckMenuIconAvailableHTTP.close();
        mInsertMenuIconHTTP.close();
        mDeleteMenuIconHTTP.close();
        mCountUnprocessedMessageForSenderHTTP.close();
        mCheckStaticFlowAvailableHTTP.close();
        mInsertStaticFlow.close();
        mDeleteStaticFlowHTTP.close();
        mCategoryInsertHTTP.close();
        mCategoryExistsHTTP.close();
        mServiceApiCallInsertHTTP.close();
        mServiceApiCallSetResultHTTP.close();
        mServiceApiCallRemoveUI.close();

        mServiceUserDataUpdateBIZZ.close();
        mServiceAppDataUpdateBIZZ.close();
        mServiceDataUpdateBIZZ.close();

        mGetFriendName.close();

        mGetFriendVersionsBizz.close();
        mFriendSetVersionGetBizz.close();
        mFriendSetVersionSetBizz.close();
        mFriendSetDeleteFromBizz.close();
        mFriendSetInsertIntoBizz.close();
        mFriendSetContainsBizz.close();

        mFriendUpdateEmailHashBizz.close();

        mInsertGroup.close();
        mUpdateGroupUI.close();
        mDeleteGroupUI.close();
        mDeleteGroupMembersUI.close();
        mInsertGroupMember.close();
        mDeleteGroupMemberUI.close();
        mClearGroupBIZZ.close();
        mClearEmptyGroupBIZZ.close();
        mClearGroupMemberBIZZ.close();
        mClearGroupMemberByEmailBIZZ.close();
        mInsertGroupAvatarBIZZ.close();
        mInsertGroupAvatarHashBIZZ.close();

        mCountServicesByOrganizationType.close();
    }

    @Override
    public void close() {
        T.UI();
        closeSQLStatements();
    }

    public long getCount() {
        T.UI();
        return mGetFriendCountUI.simpleQueryForLong();
    }

    public List<String> getEmails() {
        T.UI();
        final List<String> emails = new ArrayList<String>();

        final Cursor c = mDb.rawQuery(mMainService.getString(R.string.sql_friend_get_emails), null);
        try {
            if (c.moveToFirst()) {
                do {
                    emails.add(c.getString(0));
                } while (c.moveToNext());
            }
        } finally {
            c.close();
        }

        return emails;
    }

    public List<String> getEmails(long friendType) {
        T.dontCare();
        final List<String> emails = new ArrayList<String>();

        final Cursor c = mDb.rawQuery(mMainService.getString(R.string.sql_friend_get_emails_by_type),
            new String[] { friendType + "" });
        try {
            if (c.moveToFirst()) {
                do {
                    emails.add(c.getString(0));
                } while (c.moveToNext());
            }
        } finally {
            c.close();
        }

        return emails;
    }

    public long getNumberOfLocationSharingFriends() {
        T.UI();
        return mGetLocationSharingFriendCountUI.simpleQueryForLong();
    }

    public Friend getExistingFriend(final String email) {
        T.dontCare();
        final Cursor curs = mDb.rawQuery(mMainService.getString(R.string.sql_friend_get_existing_by_email),
            new String[] { email });
        try {
            if (!curs.moveToFirst())
                return null;
            return readFriendFromCursor(curs);
        } finally {
            curs.close();
        }
    }

    public Friend getFriend(final String email) {
        T.dontCare();
        final Cursor curs = mDb.rawQuery(mMainService.getString(R.string.sql_friend_get_by_email),
            new String[] { email });
        try {
            if (!curs.moveToFirst())
                return null;
            return readFriendFromCursor(curs);
        } finally {
            curs.close();
        }
    }

    // Get type of possibly deleted friend
    public int getFriendType(final String email) {
        T.dontCare();
        final SQLiteStatement stmt = mDb
            .compileStatement(mMainService.getString(R.string.sql_friend_get_type_by_email));
        stmt.bindString(1, email);

        try {
            return (int) stmt.simpleQueryForLong();
        } catch (SQLiteDoneException e) {
            return FriendsPlugin.FRIEND_TYPE_UNKNOWN;
        } finally {
            stmt.close();
        }
    }

    public long getUnprocessedMessagesForSender(String email) {
        T.UI();
        mCountUnprocessedMessageForSenderHTTP.bindString(1, email);
        return mCountUnprocessedMessageForSenderHTTP.simpleQueryForLong();
    }

    private byte[] getAvatar(String email) {
        T.dontCare();
        final Cursor curs = mDb.rawQuery(mMainService.getString(R.string.sql_friend_get_avatar_by_email),
            new String[] { email });
        try {
            if (!curs.moveToFirst()) {
                return null;
            }
            byte[] avatar = curs.getBlob(0);
            return avatar;
        } finally {
            curs.close();
        }
    }

    public Bitmap getAvatarBitmap(String email) {
        T.UI();

        final BitmapHolder bitmapHolder = mAvatarBitmapCacheUI.get(email);
        if (bitmapHolder != null)
            return bitmapHolder.bitmap; // Possibly returns null (e.g. for FRIEND_WITHOUT_AVATAR)

        final byte[] bitmapBytes = getAvatar(email);
        if (bitmapBytes == null) {
            mAvatarBitmapCacheUI.put(email, FRIEND_WITHOUT_AVATAR);
            return null;
        }

        final Bitmap bitmap = ImageHelper.getRoundedCornerAvatar(BitmapFactory.decodeByteArray(bitmapBytes, 0,
            bitmapBytes.length));

        mAvatarBitmapCacheUI.put(email, new BitmapHolder(bitmap));

        return bitmap;
    }

    public String getName(String email) {
        T.dontCare();

        String name;
        synchronized (mFriendNameCache) {
            name = mFriendNameCache.get(email);
        }

        if (name != null)
            return name;

        try {
            SQLiteStatement getFriendNameStatement = mGetFriendName.getStatementForThisThread();
            getFriendNameStatement.bindString(1, email);
            name = getFriendNameStatement.simpleQueryForString();

            synchronized (mFriendNameCache) {
                mFriendNameCache.put(email, name);
            }

            return name;

        } catch (SQLiteDoneException e) {
            return null;
        }
    }

    public String getFriendUpateSummary(final FriendTO friend, final long[] versionsInDB) {
        Map<String, Object> info = new HashMap<String, Object>();
        info.put("email", friend.email);
        info.put("localVersions", Arrays.toString(versionsInDB));
        info.put("serverVersions", Arrays.toString(friend.versions));
        return info.toString();
    }

    public UpdateFriendResponseTO shouldUpdateFriend(final FriendTO friend) {
        UpdateFriendResponseTO response = new UpdateFriendResponseTO();
        if (!friendSetContains(friend.email)) {
            response.updated = false;
            response.reason = friend.email + " is not in the local friendSet";
            return response;
        } else {
            long[] versionsInDB = getFriendVersions(friend.email);

            if (versionsInDB.length > friend.versions.length) {
                response.updated = false;
                response.reason = "There are more versions in the local DB than versions on the server. "
                    + getFriendUpateSummary(friend, versionsInDB);
                return response;
            } else {
                // Check that versions in DB are not greater than versions from server
                int comparisonResult = compareFriendVersions(versionsInDB, friend.versions);
                if (versionsInDB.length < friend.versions.length) {
                    if (comparisonResult == 1) {
                        // versionsInDB > versionsFromServer
                        throw new IllegalFriendVersionBumpException(
                            "Version length difference between local DB and server, "
                                + "AND one or more versions in local DB are greater than versions on the server. "
                                + getFriendUpateSummary(friend, versionsInDB));
                    } else {
                        // We can continue. A version field has been added. Eg from [1, 1] to [1, 1, 0].
                        response.updated = true;
                        response.reason = null;
                        return response;
                    }
                } else {
                    // versionsInDB.length == friend.versions.length
                    if (comparisonResult == -1) {
                        // versionsInDB < versionsFromServer
                        response.updated = true;
                        response.reason = null;
                        return response;
                    } else {
                        response.updated = false;
                        response.reason = "One or more versions in local DB are greater than or equal to versions on "
                            + "the server. " + getFriendUpateSummary(friend, versionsInDB);
                        return response;
                    }
                }
            }
        }
    }

    public void addInvitedService(final Friend service) {
        T.BIZZ();
        insertFriend(service, Friend.INVITE_PENDING);
        if (service.avatar != null) {
            updateFriendAvatar(service.email, service.avatar);
        }
    }

    public void setFriendExistence(String email, int existence) {
        T.BIZZ();
        boolean isDelete = (existence == Friend.DELETION_PENDING || existence == Friend.DELETED);

        @SuppressWarnings("resource")
        final SQLiteStatement stmt = isDelete ? mUpdateFriendExistenceAndClearVersionHTTP : mUpdateFriendExistenceHTTP;

        stmt.bindLong(1, existence);
        stmt.bindString(2, email);
        stmt.execute();
    }

    public void deleteFriend(final String email) {
        T.BIZZ();
        setFriendExistence(email, Friend.DELETED);
        deleteServiceMenuForFriend(email);
    }

    public boolean isFriend(final String email) {
        T.UI();
        mGetIsFriendUI.bindString(1, email);
        return mGetIsFriendUI.simpleQueryForLong() != 0;
    }

    public int getExistence(final String email) {
        final Cursor cursor = mDb.rawQuery(mMainService.getString(R.string.sql_friend_get_existence),
            new String[] { email });
        try {
            if (!cursor.moveToFirst()) {
                return Friend.NOT_FOUND;
            }
            return cursor.getInt(0);
        } finally {
            cursor.close();
        }
    }

    public UpdateFriendResponseTO updateFriend(final FriendTO friend) {
        T.BIZZ();

        UpdateFriendResponseTO response = TransactionHelper.runInTransaction(mDb, "updateFriend",
            new Transaction<UpdateFriendResponseTO>() {
                @Override
                protected UpdateFriendResponseTO run() {
                    UpdateFriendResponseTO response = shouldUpdateFriend(friend);
                    if (!response.updated) {
                        return response;
                    }

                    requestFriendCategoryIfNeeded(friend);

                    final boolean userDataUpdated;
                    final boolean serviceDataUpdated;

                    // Check if service data has changed
                    if (friend.type == FriendsPlugin.FRIEND_TYPE_SERVICE) {
                        String[] data = getServiceData(friend.email);
                        if (data != null) {
                            userDataUpdated = !android.text.TextUtils.equals(data[0], friend.userData);
                            serviceDataUpdated = !android.text.TextUtils.equals(data[1], friend.appData);
                        } else {
                            userDataUpdated = false;
                            serviceDataUpdated = false;
                        }
                    } else {
                        userDataUpdated = false;
                        serviceDataUpdated = false;
                    }

                    final String friendDisplayName;
                    if (!TextUtils.isEmptyOrWhitespace(friend.name)) {
                        friendDisplayName = friend.name;
                    } else if (!TextUtils.isEmptyOrWhitespace(friend.qualifiedIdentifier)) {
                        friendDisplayName = friend.qualifiedIdentifier;
                    } else {
                        friendDisplayName = friend.email;
                    }
                    mUpdateFriendHTTP.bindString(1, friendDisplayName);
                    mUpdateFriendHTTP.bindLong(2, friend.avatarId);
                    mUpdateFriendHTTP.bindLong(3, friend.shareLocation ? 1 : 0);
                    mUpdateFriendHTTP.bindLong(4, friend.sharesLocation ? 1 : 0);
                    mUpdateFriendHTTP.bindLong(5, friend.type);
                    if (friend.description == null)
                        mUpdateFriendHTTP.bindNull(6);
                    else
                        mUpdateFriendHTTP.bindString(6, friend.description);

                    if (TextUtils.isEmptyOrWhitespace(friend.descriptionBranding))
                        mUpdateFriendHTTP.bindNull(7);
                    else
                        mUpdateFriendHTTP.bindString(7, friend.descriptionBranding);

                    if (friend.pokeDescription == null)
                        mUpdateFriendHTTP.bindNull(8);
                    else
                        mUpdateFriendHTTP.bindString(8, friend.pokeDescription);

                    if (friend.actionMenu == null || TextUtils.isEmptyOrWhitespace(friend.actionMenu.branding))
                        mUpdateFriendHTTP.bindNull(9);
                    else
                        mUpdateFriendHTTP.bindString(9, friend.actionMenu.branding);

                    if (friend.actionMenu == null || TextUtils.isEmptyOrWhitespace(friend.actionMenu.phoneNumber))
                        mUpdateFriendHTTP.bindNull(10);
                    else
                        mUpdateFriendHTTP.bindString(10, friend.actionMenu.phoneNumber);

                    boolean hasNoShareImageUrl = friend.actionMenu == null
                        || TextUtils.isEmptyOrWhitespace(friend.actionMenu.shareImageUrl);
                    mUpdateFriendHTTP.bindLong(11, hasNoShareImageUrl ? 0 : 1);

                    mUpdateFriendHTTP.bindLong(12, friend.generation);

                    if (hasNoShareImageUrl)
                        mUpdateFriendHTTP.bindNull(13);
                    else
                        mUpdateFriendHTTP.bindString(13, friend.actionMenu.shareImageUrl);

                    if (friend.actionMenu == null || TextUtils.isEmptyOrWhitespace(friend.actionMenu.shareDescription))
                        mUpdateFriendHTTP.bindNull(14);
                    else
                        mUpdateFriendHTTP.bindString(14, friend.actionMenu.shareDescription);

                    if (friend.actionMenu == null || TextUtils.isEmptyOrWhitespace(friend.actionMenu.shareCaption))
                        mUpdateFriendHTTP.bindNull(15);
                    else
                        mUpdateFriendHTTP.bindString(15, friend.actionMenu.shareCaption);

                    if (friend.actionMenu == null || TextUtils.isEmptyOrWhitespace(friend.actionMenu.shareLinkUrl))
                        mUpdateFriendHTTP.bindNull(16);
                    else
                        mUpdateFriendHTTP.bindString(16, friend.actionMenu.shareLinkUrl);

                    if (TextUtils.isEmptyOrWhitespace(friend.qualifiedIdentifier))
                        mUpdateFriendHTTP.bindNull(17);
                    else
                        mUpdateFriendHTTP.bindString(17, friend.qualifiedIdentifier);

                    if (friend.actionMenu == null || TextUtils.isEmptyOrWhitespace(friend.actionMenu.aboutLabel))
                        mUpdateFriendHTTP.bindNull(18);
                    else
                        mUpdateFriendHTTP.bindString(18, friend.actionMenu.aboutLabel);

                    if (friend.actionMenu == null || TextUtils.isEmptyOrWhitespace(friend.actionMenu.messagesLabel))
                        mUpdateFriendHTTP.bindNull(19);
                    else
                        mUpdateFriendHTTP.bindString(19, friend.actionMenu.messagesLabel);

                    if (friend.actionMenu == null || TextUtils.isEmptyOrWhitespace(friend.actionMenu.callLabel))
                        mUpdateFriendHTTP.bindNull(20);
                    else
                        mUpdateFriendHTTP.bindString(20, friend.actionMenu.callLabel);

                    if (friend.actionMenu == null || TextUtils.isEmptyOrWhitespace(friend.actionMenu.shareLabel))
                        mUpdateFriendHTTP.bindNull(21);
                    else
                        mUpdateFriendHTTP.bindString(21, friend.actionMenu.shareLabel);

                    if (friend.actionMenu == null || TextUtils.isEmptyOrWhitespace(friend.actionMenu.callConfirmation))
                        mUpdateFriendHTTP.bindNull(22);
                    else
                        mUpdateFriendHTTP.bindString(22, friend.actionMenu.callConfirmation);

                    if (TextUtils.isEmptyOrWhitespace(friend.userData))
                        mUpdateFriendHTTP.bindNull(23);
                    else
                        mUpdateFriendHTTP.bindString(23, friend.userData);

                    if (TextUtils.isEmptyOrWhitespace(friend.appData))
                        mUpdateFriendHTTP.bindNull(24);
                    else
                        mUpdateFriendHTTP.bindString(24, friend.appData);

                    if (TextUtils.isEmptyOrWhitespace(friend.category_id))
                        mUpdateFriendHTTP.bindNull(25);
                    else
                        mUpdateFriendHTTP.bindString(25, friend.category_id);

                    if (TextUtils.isEmptyOrWhitespace(friend.broadcastFlowHash))
                        mUpdateFriendHTTP.bindNull(26);
                    else
                        mUpdateFriendHTTP.bindString(26, friend.broadcastFlowHash);

                    mUpdateFriendHTTP.bindLong(27, friend.organizationType);
                    mUpdateFriendHTTP.bindLong(28, friend.callbacks);
                    mUpdateFriendHTTP.bindLong(29, friend.flags);
                    mUpdateFriendHTTP.bindString(30, getStringFromVersions(friend.versions));

                    if (TextUtils.isEmptyOrWhitespace(friend.profileData))
                        mUpdateFriendHTTP.bindNull(31);
                    else
                        mUpdateFriendHTTP.bindString(31, friend.profileData);

                    if (TextUtils.isEmptyOrWhitespace(friend.contentBrandingHash))
                        mUpdateFriendHTTP.bindNull(32);
                    else
                        mUpdateFriendHTTP.bindString(32, friend.contentBrandingHash);

                    bindString(mUpdateFriendHTTP, 33, getActions(friend));

                    // Where clause
                    mUpdateFriendHTTP.bindString(34, friend.email);
                    mUpdateFriendHTTP.execute();

                    mFriendNameCache.put(friend.email, friendDisplayName);

                    rebuildServiceMenu(friend, true);

                    TransactionHelper.onTransactionCommitted(new SafeRunnable() {

                        @Override
                        protected void safeRun() throws Exception {
                            if (userDataUpdated || serviceDataUpdated) {
                                broadcastServiceDataUpdated(friend.email, userDataUpdated, serviceDataUpdated);
                            }

                            downloadAvatar(friend);
                        }
                    });
                    return response;
                }
            });

        return response;
    }

    public void updateFriendInfo(final String email, final GetUserInfoResponseTO result) {
        TransactionHelper.runInTransaction(mDb, "updateFriendInfo", new TransactionWithoutResult() {
            @Override
            protected void run() {
                if (TextUtils.isEmptyOrWhitespace(result.name))
                    mUpdateFriendInfoHTTP.bindNull(1);
                else
                    mUpdateFriendInfoHTTP.bindString(1, result.name);
                mUpdateFriendInfoHTTP.bindLong(2, result.avatar_id);
                mUpdateFriendInfoHTTP.bindBlob(3, Base64.decode(result.avatar));
                mUpdateFriendInfoHTTP.bindLong(4, result.type);

                if (TextUtils.isEmptyOrWhitespace(result.description))
                    mUpdateFriendInfoHTTP.bindNull(5);
                else
                    mUpdateFriendInfoHTTP.bindString(5, result.description);

                if (TextUtils.isEmptyOrWhitespace(result.descriptionBranding))
                    mUpdateFriendInfoHTTP.bindNull(6);
                else
                    mUpdateFriendInfoHTTP.bindString(6, result.descriptionBranding);

                if (TextUtils.isEmptyOrWhitespace(result.qualifiedIdentifier))
                    mUpdateFriendInfoHTTP.bindNull(7);
                else
                    mUpdateFriendInfoHTTP.bindString(7, result.qualifiedIdentifier);

                if (TextUtils.isEmptyOrWhitespace(result.profileData))
                    mUpdateFriendInfoHTTP.bindNull(8);
                else
                    mUpdateFriendInfoHTTP.bindString(8, result.profileData);

                // Where clause
                mUpdateFriendInfoHTTP.bindString(9, email);
                mUpdateFriendInfoHTTP.execute();
            }
        });
    }

    public void updateFriendShareLocation(String email, boolean shareLocation) {
        T.BIZZ();
        mUpdateFriendShareLocationHTTP.bindLong(1, shareLocation ? 1 : 0);
        mUpdateFriendShareLocationHTTP.bindString(2, email);
        mUpdateFriendShareLocationHTTP.execute();
    }

    public boolean storeFriend(final FriendTO friend, final byte[] avatar, final boolean force) {
        return TransactionHelper.runInTransaction(mDb, "storeFriend", new Transaction<Boolean>() {
            @Override
            protected Boolean run() {
                final boolean updated = force || friendSetContains(friend.email);
                if (updated) {
                    deleteServiceMenuForFriend(friend.email);
                    insertFriend(friend, friend.existence);
                    rebuildServiceMenu(friend, false);
                    if (avatar == null) {
                        downloadAvatar(friend);
                    } else {
                        updateFriendAvatar(friend.email, avatar);
                    }
                }
                return updated;
            }
        });
    }

    public void scrub() {
        scrubMenuIcons();
        scrubStaticFlows();
    }

    public void storeMenuIcon(final byte[] icon, final String iconHash) {
        T.BIZZ();
        mInsertMenuIconHTTP.bindString(1, iconHash);
        mInsertMenuIconHTTP.bindBlob(2, icon);
        mInsertMenuIconHTTP.execute();
    }

    public boolean isMenuIconAvailable(String iconHash) {
        mCheckMenuIconAvailableHTTP.bindString(1, iconHash);
        return mCheckMenuIconAvailableHTTP.simpleQueryForLong() != 0;
    }

    public boolean isStaticFlowAvailable(String staticFlowHash) {
        mCheckStaticFlowAvailableHTTP.bindString(1, staticFlowHash);
        return mCheckStaticFlowAvailableHTTP.simpleQueryForLong() != 0;
    }

    public void storeStaticFlow(final String staticFlow, final String staticFlowHash) {
        T.dontCare();
        SQLiteStatement stmt = mInsertStaticFlow.getStatementForThisThread();
        stmt.bindString(1, staticFlowHash);
        stmt.bindBlob(2, Base64.decode(staticFlow));
        stmt.execute();
    }

    private ServiceMenu getMenuDetails(String email) {
        final Cursor curz = mDb.rawQuery(mMainService.getString(R.string.sql_friend_get_menu_details),
            new String[] { email });
        try {
            if (!curz.moveToFirst()) {
                return null;
            }
            ServiceMenu menu = new ServiceMenu();
            menu.staticFlowBrandings = new String[0];
            menu.itemList = new ArrayList<ServiceMenuItem>();
            menu.items = new ServiceMenuItemTO[0];

            menu.phoneNumber = curz.getString(0);
            menu.branding = curz.getString(1);
            menu.name = curz.getString(2);
            menu.share = curz.getLong(3) != 0;
            menu.maxPage = curz.getLong(4);
            menu.generation = curz.getLong(5);
            menu.shareImageUrl = curz.getString(6);
            menu.shareDescription = curz.getString(7);
            menu.shareCaption = curz.getString(8);
            menu.shareLinkUrl = curz.getString(9);
            menu.aboutLabel = curz.getString(10);
            menu.messagesLabel = curz.getString(11);
            menu.callLabel = curz.getString(12);
            menu.shareLabel = curz.getString(13);
            menu.callConfirmation = curz.getString(14);
            return menu;
        } finally {
            curz.close();
        }
    }

    private <T extends ServiceMenuItem> T readMenuItemFromCursor(T smi, Cursor curs, Integer page) {
        int i = 0;
        smi.coords = new long[]{curs.getInt(i++), curs.getInt(i++), page == null ? curs.getInt(i++) : page};
        smi.label = curs.getString(i++);
        smi.icon = curs.getBlob(i++);
        smi.screenBranding = curs.getString(i++);
        smi.staticFlowHash = curs.getString(i++);
        smi.hashedTag = curs.getString(i++);
        smi.requiresWifi = curs.getLong(i++) == 1;
        smi.runInBackground = curs.getLong(i++) == 1;
        smi.action = curs.getLong(i++);
        return smi;
    }

    private void populateMenuItems(String email, ServiceMenu menu, Integer page) {
        final Cursor curs;
        if (page == null) {
            curs = mDb.rawQuery(mMainService.getString(R.string.sql_friend_get_full_menu), new String[] { email });
        } else {
            curs = mDb
                .rawQuery(mMainService.getString(R.string.sql_friend_get_menu), new String[] { email, "" + page });
        }
        try {
            if (!curs.moveToFirst()) {
                return;
            }
            do {
                menu.itemList.add(readMenuItemFromCursor(new ServiceMenuItem(), curs, page));
            } while (curs.moveToNext());
        } finally {
            curs.close();
        }
        menu.items = menu.itemList.toArray(new ServiceMenuItemTO[menu.itemList.size()]);
    }

    public ServiceMenuItemDetails getMenuItem(final String email, final String hashedTag) {
        final Cursor curs = mDb.rawQuery(mMainService.getString(R.string.sql_friend_get_smi_by_tag), new
                String[]{email, hashedTag});
        try {
            if (!curs.moveToFirst()) {
                return null;
            }

            ServiceMenuItemDetails smi = readMenuItemFromCursor(new ServiceMenuItemDetails(), curs, null);
            smi.menuGeneration = curs.getLong(10);
            return smi;
        } finally {
            curs.close();
        }
    }

    public ServiceMenu getMenu(String email, Integer page) {
        ServiceMenu menu = getMenuDetails(email);
        if (menu != null) {
            populateMenuItems(email, menu, page);
        }
        return menu;
    }

    public void addMenuDetails(Friend service) {
        ServiceMenu menu = getMenuDetails(service.email);
        if (menu == null) {
            menu = new ServiceMenu();
            menu.staticFlowBrandings = new String[0];
            menu.itemList = new ArrayList<ServiceMenuItem>();
            menu.items = new ServiceMenuItemTO[0];
            service.actionMenu = menu;
            return;
        }
        service.actionMenu = menu;
        populateMenuItems(service.email, menu, null);
    }

    private void scrubMenuIcons() {
        final Cursor curs = mDb.rawQuery(mMainService.getString(R.string.sql_friend_menu_icon_usage), new String[0]);
        try {
            if (!curs.moveToFirst()) {
                return;
            }
            do {
                String iconHash = curs.getString(0);
                long used = curs.getLong(1);
                if (used == 0) {
                    L.d("Deleting icon with hash " + iconHash);
                    mDeleteMenuIconHTTP.bindString(1, iconHash);
                    mDeleteMenuIconHTTP.execute();
                } else {
                    L.d("Icon with hash " + iconHash + " is used " + used + " times.");
                }
            } while (curs.moveToNext());
        } finally {
            curs.close();
        }
    }

    private void scrubStaticFlows() {
        final Cursor curs = mDb.rawQuery(mMainService.getString(R.string.sql_friend_static_flow_usage), new String[0]);
        try {
            if (!curs.moveToFirst()) {
                return;
            }
            do {
                String hash = curs.getString(0);
                long usedInSmi = curs.getLong(1);
                long usedInMfr = curs.getLong(2);
                L.d("Static flow with hash " + hash + " is used " + (usedInSmi + usedInMfr) + " times.");
                if (usedInSmi == 0 && usedInMfr == 0) {
                    L.d("Deleting static flow with hash " + hash);
                    mDeleteStaticFlowHTTP.bindString(1, hash);
                    mDeleteStaticFlowHTTP.execute();
                }
            } while (curs.moveToNext());
        } finally {
            curs.close();
        }
    }

    public String getStaticFlow(final String staticFlowHash) throws IOException {
        T.UI();
        final Cursor curs = mDb.rawQuery(mMainService.getString(R.string.sql_friend_static_flow_get),
            new String[] { staticFlowHash });

        try {
            if (!curs.moveToFirst())
                return null;

            return ZipUtils.decompressToString(curs.getBlob(0));
        } finally {
            curs.close();
        }
    }

    public String[] getServiceData(final String email) {
        T.dontCare();
        final Cursor curs = mDb.rawQuery(mMainService.getString(R.string.sql_friend_data_get), new String[] { email });

        try {
            if (!curs.moveToFirst())
                return null;

            String userDataString = curs.getString(0);
            String appDataString = curs.getString(1);

            return new String[] { userDataString, appDataString };
        } finally {
            curs.close();
        }
    }

    private void deleteServiceMenuForFriend(final String email) {
        mDeleteServiceMenuHTTP.bindString(1, email);
        mDeleteServiceMenuHTTP.execute();
    }

    private void rebuildServiceMenu(final FriendTO friend, final boolean cleanup) {
        T.BIZZ();
        if (cleanup) {
            deleteServiceMenuForFriend(friend.email);
        }

        if (friend.actionMenu == null || friend.actionMenu.items == null)
            return;

        for (ServiceMenuItemTO item : friend.actionMenu.items) {
            mInsertServiceMenuHTTP.bindString(1, friend.email);
            mInsertServiceMenuHTTP.bindLong(2, item.coords[0]);
            mInsertServiceMenuHTTP.bindLong(3, item.coords[1]);
            mInsertServiceMenuHTTP.bindLong(4, item.coords[2]);
            mInsertServiceMenuHTTP.bindString(5, item.label);
            mInsertServiceMenuHTTP.bindString(6, item.iconHash);
            if (item.screenBranding == null)
                mInsertServiceMenuHTTP.bindNull(7);
            else
                mInsertServiceMenuHTTP.bindString(7, item.screenBranding);
            if (item.staticFlowHash == null)
                mInsertServiceMenuHTTP.bindNull(8);
            else
                mInsertServiceMenuHTTP.bindString(8, item.staticFlowHash);
            if (item.hashedTag == null)
                mInsertServiceMenuHTTP.bindNull(9);
            else
                mInsertServiceMenuHTTP.bindString(9, item.hashedTag);
            mInsertServiceMenuHTTP.bindLong(10, item.requiresWifi ? 1 : 0);
            mInsertServiceMenuHTTP.bindLong(11, item.runInBackground ? 1 : 0);
            mInsertServiceMenuHTTP.bindLong(12, item.action);
            mInsertServiceMenuHTTP.execute();

            if (!isMenuIconAvailable(item.iconHash)) {
                // Download menu icon
                GetMenuIconRequestTO request = new GetMenuIconRequestTO();
                request.service = friend.email;
                request.coords = item.coords;
                request.size = mIconSize;
                GetMenuIconResponseHandler respHandler = new GetMenuIconResponseHandler();
                respHandler.setEmail(friend.email);
                respHandler.setIconHash(item.iconHash);
                try {
                    com.mobicage.api.services.Rpc.getMenuIcon(respHandler, request);
                } catch (Exception e) {
                    L.bug(e);
                }
            }

            if (!TextUtils.isEmptyOrWhitespace(item.staticFlowHash)) {
                if (!isStaticFlowAvailable(item.staticFlowHash)) {
                    // Download static flow
                    mMainService.getPlugin(FriendsPlugin.class).requestStaticFlow(friend.email, item);
                }
            }
        }

        if (cleanup) {
            scrubMenuIcons();
            scrubStaticFlows();
        }
    }

    private void downloadAvatar(final FriendTO friend) {
        T.BIZZ();

        if (friend.avatarId == -1)
            // Friend does not have an avatar
            return;

        if (friend.avatarHash != null) {
            // Friend avatarHash is defined on server
            final byte[] localAvatarBytes = getAvatar(friend.email);
            if (localAvatarBytes != null) {
                try {
                    // XXX we could optimize this and store the SHA256 of the avatar in the db
                    final MessageDigest digester = MessageDigest.getInstance("SHA256");
                    digester.update(localAvatarBytes);
                    final String oldAvatarHash = TextUtils.toHex(digester.digest());

                    if (oldAvatarHash.equals(friend.avatarHash))
                        return;

                } catch (NoSuchAlgorithmException e) {
                    L.bug(e);
                }
            }
        }

        // Friend avatarHash not defined on server, or different from the one we have on client
        mMainService.postOnUIHandler(new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                final GetAvatarRequestTO request = new GetAvatarRequestTO();
                request.avatarId = friend.avatarId;
                request.size = 50 * mMainService.getScreenScale();
                final GetAvatarResponseHandler responseHandler = new GetAvatarResponseHandler();
                responseHandler.setFriendEmail(friend.email);
                try {
                    com.mobicage.api.friends.Rpc.getAvatar(responseHandler, request);
                } catch (Exception e) {
                    L.bug("Cannot download friend avatar", e);
                }
            }
        });

    }

    public Cursor getFriendsByCategoryListCursor(final String categoryId) {
        return mDb.rawQuery(mMainService.getString(R.string.sql_friends_by_category_cursor),
            new String[] { categoryId });
    }

    public Cursor getServiceFriendListCursor() {
        return mDb.rawQuery(mMainService.getString(R.string.sql_service_friend_list_cursor), null);
    }

    public Cursor getServiceFriendListCursor(int organizationType) {
        if (organizationType == SERVICE_ORGANIZATION_TYPE_UNSPECIFIED) {
            return getServiceFriendListCursor();
        }
        return mDb.rawQuery(mMainService.getString(R.string.sql_services_get_organization_type),
            new String[] { organizationType + "" });
    }

    public Cursor getUserFriendListCursor() {
        return mDb.rawQuery(mMainService.getString(R.string.sql_user_friend_list_cursor), null);
    }

    public long countServicesByOrganizationType(int organizationType) {
        mCountServicesByOrganizationType.bindLong(1, organizationType);
        return mCountServicesByOrganizationType.simpleQueryForLong();
    }

    public SparseIntArray countServicesGroupedByOrganizationType() {
        SparseIntArray result = new SparseIntArray();

        final Cursor cursor = mDb.rawQuery(
            mMainService.getString(R.string.sql_services_count_grouped_by_organization_type), null);

        try {
            if (cursor.moveToFirst()) {
                do {
                    int organizationType = cursor.getInt(0);
                    int count = cursor.getInt(1);
                    result.put(organizationType, count);
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }

        return result;
    }

    public List<String> getServicesByOrganizationType(int organizationType) {
        T.dontCare();
        List<String> emails = new ArrayList<String>();

        final Cursor cursor = mDb.rawQuery(mMainService.getString(R.string.sql_services_get_organization_type),
            new String[] { organizationType + "" });

        try {
            if (cursor.moveToFirst()) {
                do {
                    emails.add(cursor.getString(1));
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }

        return emails;
    }

    public Friend readFriendFromCursor(Cursor cursor) {
        T.dontCare();
        boolean hasCategoryId = cursor.getColumnIndex("cat_id") != -1;
        if (hasCategoryId || cursor.getColumnIndex("share_location") != -1) {
            Friend friend = new Friend();
            friend.email = cursor.getString(1);
            friend.name = cursor.getString(2);
            friend.shareLocation = cursor.getLong(3) == 1;
            friend.sharesLocation = cursor.getLong(4) == 1;
            friend.avatar = cursor.getBlob(5);
            friend.existenceStatus = cursor.getInt(6);
            friend.type = cursor.getInt(7);
            friend.description = cursor.getString(8);
            friend.descriptionBranding = cursor.getString(9);
            friend.pokeDescription = cursor.getString(10);
            friend.qualifiedIdentifier = cursor.getString(11);
            friend.organizationType = cursor.getLong(12);
            friend.callbacks = cursor.getLong(13);
            friend.flags = cursor.getLong(14);
            friend.profileData = cursor.getString(15);
            friend.contentBrandingHash = cursor.getString(16);
            friend.actions = cursor.getString(17);

            if (hasCategoryId) {
                final String emailOrCategoryId = cursor.getString(18);
                if (!friend.email.equals(emailOrCategoryId)) {
                    friend.category = new FriendCategory();
                    friend.category.id = friend.category_id = emailOrCategoryId;
                    friend.category.name = cursor.getString(19);
                    friend.category.avatar = cursor.getBlob(20);
                    friend.category.friendCount = cursor.getInt(21);
                }
            }

            return friend;
        } else {
            return mPhoneContacts.fromCursor(cursor);
        }

    }

    public void saveCategory(FriendCategoryTO category) {
        mCategoryInsertHTTP.bindString(1, category.guid);
        mCategoryInsertHTTP.bindString(2, category.name);
        mCategoryInsertHTTP.bindBlob(3, Base64.decode(category.avatar));
        mCategoryInsertHTTP.execute();
    }

    private boolean categoryExists(String categoryId) {
        mCategoryExistsHTTP.bindString(1, categoryId);
        return mCategoryExistsHTTP.simpleQueryForLong() > 0;
    }

    private void requestFriendCategoryIfNeeded(final FriendTO friend) {
        if (friend.category_id != null && !categoryExists(friend.category_id)) {
            mMainService.postOnUIHandler(new SafeRunnable() {
                @Override
                protected void safeRun() throws Exception {
                    GetCategoryRequestTO request = new GetCategoryRequestTO();
                    request.category_id = friend.category_id;
                    GetCategoryResponseHandler responseHandler = new GetCategoryResponseHandler();
                    try {
                        com.mobicage.api.friends.Rpc.getCategory(responseHandler, request);
                    } catch (Exception e) {
                        L.bug("Cannot download friend avatar", e);
                    }
                }
            });

        }
    }

    /**
     * private convenience function to add a friend to the friend table. !! should only be called from a transactional
     * context which also updates the generation. !!
     * 
     * @param friend
     * @param existence
     */
    private void insertFriend(final FriendTO friend, long existence) {
        T.BIZZ();

        requestFriendCategoryIfNeeded(friend);

        final byte[] hashBytes = EmailHashCalculator.calculateEmailHash(friend.email, friend.type);

        mInsertFriendHTTP.bindString(1, friend.email);
        if (TextUtils.isEmptyOrWhitespace(friend.name)) {
            mInsertFriendHTTP.bindString(2, friend.email);
        } else {
            mInsertFriendHTTP.bindString(2, friend.name);
        }
        mInsertFriendHTTP.bindLong(3, friend.avatarId);
        mInsertFriendHTTP.bindLong(4, friend.shareLocation ? 1 : 0);
        mInsertFriendHTTP.bindLong(5, friend.sharesLocation ? 1 : 0);
        mInsertFriendHTTP.bindLong(6, existence);
        mInsertFriendHTTP.bindLong(7, friend.type);
        mInsertFriendHTTP.bindBlob(8, hashBytes);
        if (TextUtils.isEmptyOrWhitespace(friend.description))
            mInsertFriendHTTP.bindNull(9);
        else
            mInsertFriendHTTP.bindString(9, friend.description);

        if (TextUtils.isEmptyOrWhitespace(friend.descriptionBranding))
            mInsertFriendHTTP.bindNull(10);
        else
            mInsertFriendHTTP.bindString(10, friend.descriptionBranding);

        if (TextUtils.isEmptyOrWhitespace(friend.pokeDescription))
            mInsertFriendHTTP.bindNull(11);
        else
            mInsertFriendHTTP.bindString(11, friend.pokeDescription);

        if (friend.actionMenu == null || TextUtils.isEmptyOrWhitespace(friend.actionMenu.branding))
            mInsertFriendHTTP.bindNull(12);
        else
            mInsertFriendHTTP.bindString(12, friend.actionMenu.branding);

        if (friend.actionMenu == null || TextUtils.isEmptyOrWhitespace(friend.actionMenu.phoneNumber))
            mInsertFriendHTTP.bindNull(13);
        else
            mInsertFriendHTTP.bindString(13, friend.actionMenu.phoneNumber);

        boolean hasNoShareImageUrl = friend.actionMenu == null
            || TextUtils.isEmptyOrWhitespace(friend.actionMenu.shareImageUrl);
        mInsertFriendHTTP.bindLong(14, hasNoShareImageUrl ? 0 : 1);

        mInsertFriendHTTP.bindLong(15, friend.generation);

        if (hasNoShareImageUrl)
            mInsertFriendHTTP.bindNull(16);
        else
            mInsertFriendHTTP.bindString(16, friend.actionMenu.shareImageUrl);

        if (friend.actionMenu == null || TextUtils.isEmptyOrWhitespace(friend.actionMenu.shareDescription))
            mInsertFriendHTTP.bindNull(17);
        else
            mInsertFriendHTTP.bindString(17, friend.actionMenu.shareDescription);

        if (friend.actionMenu == null || TextUtils.isEmptyOrWhitespace(friend.actionMenu.shareCaption))
            mInsertFriendHTTP.bindNull(18);
        else
            mInsertFriendHTTP.bindString(18, friend.actionMenu.shareCaption);

        if (friend.actionMenu == null || TextUtils.isEmptyOrWhitespace(friend.actionMenu.shareLinkUrl))
            mInsertFriendHTTP.bindNull(19);
        else
            mInsertFriendHTTP.bindString(19, friend.actionMenu.shareLinkUrl);

        if (TextUtils.isEmptyOrWhitespace(friend.qualifiedIdentifier))
            mInsertFriendHTTP.bindNull(20);
        else
            mInsertFriendHTTP.bindString(20, friend.qualifiedIdentifier);

        if (friend.actionMenu == null || TextUtils.isEmptyOrWhitespace(friend.actionMenu.aboutLabel))
            mInsertFriendHTTP.bindNull(21);
        else
            mInsertFriendHTTP.bindString(21, friend.actionMenu.aboutLabel);

        if (friend.actionMenu == null || TextUtils.isEmptyOrWhitespace(friend.actionMenu.messagesLabel))
            mInsertFriendHTTP.bindNull(22);
        else
            mInsertFriendHTTP.bindString(22, friend.actionMenu.messagesLabel);

        if (friend.actionMenu == null || TextUtils.isEmptyOrWhitespace(friend.actionMenu.callLabel))
            mInsertFriendHTTP.bindNull(23);
        else
            mInsertFriendHTTP.bindString(23, friend.actionMenu.callLabel);

        if (friend.actionMenu == null || TextUtils.isEmptyOrWhitespace(friend.actionMenu.shareLabel))
            mInsertFriendHTTP.bindNull(24);
        else
            mInsertFriendHTTP.bindString(24, friend.actionMenu.shareLabel);

        if (friend.actionMenu == null || TextUtils.isEmptyOrWhitespace(friend.actionMenu.callConfirmation))
            mInsertFriendHTTP.bindNull(25);
        else
            mInsertFriendHTTP.bindString(25, friend.actionMenu.callConfirmation);

        if (TextUtils.isEmptyOrWhitespace(friend.userData))
            mInsertFriendHTTP.bindNull(26);
        else
            mInsertFriendHTTP.bindString(26, friend.userData);

        if (TextUtils.isEmptyOrWhitespace(friend.appData))
            mInsertFriendHTTP.bindNull(27);
        else
            mInsertFriendHTTP.bindString(27, friend.appData);

        if (TextUtils.isEmptyOrWhitespace(friend.category_id))
            mInsertFriendHTTP.bindNull(28);
        else
            mInsertFriendHTTP.bindString(28, friend.category_id);

        if (TextUtils.isEmptyOrWhitespace(friend.broadcastFlowHash))
            mInsertFriendHTTP.bindNull(29);
        else
            mInsertFriendHTTP.bindString(29, friend.broadcastFlowHash);

        mInsertFriendHTTP.bindLong(30, friend.organizationType);
        mInsertFriendHTTP.bindLong(31, friend.callbacks);
        mInsertFriendHTTP.bindLong(32, friend.flags);
        mInsertFriendHTTP.bindString(33, getStringFromVersions(friend.versions));

        if (TextUtils.isEmptyOrWhitespace(friend.profileData))
            mInsertFriendHTTP.bindNull(34);
        else
            mInsertFriendHTTP.bindString(34, friend.profileData);

        if (TextUtils.isEmptyOrWhitespace(friend.contentBrandingHash))
            mInsertFriendHTTP.bindNull(35);
        else
            mInsertFriendHTTP.bindString(35, friend.contentBrandingHash);

        bindString(mInsertFriendHTTP, 36, getActions(friend));

        mInsertFriendHTTP.execute();
    }

    private String getActions(FriendTO friend) {
        if (friend.actionMenu == null || friend.actionMenu.items == null)
            return null;

        List<ServiceMenuItemTO> order = new ArrayList<>();
        for (ServiceMenuItemTO smi : friend.actionMenu.items) {
            if (smi.action > 0) {
                order.add(smi);
            }
        }
        if (order.size() == 0)
            return null;
        Collections.sort(order, comparator);
        List<String> actions = new ArrayList<>();
        for (ServiceMenuItemTO smi : order) {
            actions.add(smi.label);
        }
        return android.text.TextUtils.join(" - ", actions);
    }

    private final Comparator<ServiceMenuItemTO> comparator = new Comparator<ServiceMenuItemTO>() {
        @Override
        public int compare(ServiceMenuItemTO item1, ServiceMenuItemTO item2) {
            return item1.action > item2.action ? -1 : 1;
        }
    };

    void updateFriendAvatar(final String friendEmail, final byte[] avatarBytes) {
        T.BIZZ();
        mUpdateFriendAvatarHTTP.bindBlob(1, avatarBytes);
        mUpdateFriendAvatarHTTP.bindString(2, friendEmail);
        mUpdateFriendAvatarHTTP.execute();

        mMainService.postOnUIHandler(new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                T.UI();
                mAvatarBitmapCacheUI.delete(friendEmail);
                final Intent intent = new Intent(FriendsPlugin.FRIEND_AVATAR_CHANGED_INTENT);
                intent.putExtra("email", friendEmail);
                mMainService.sendBroadcast(intent);
            }
        });
    }

    public String getEmailByEmailHash(final byte[] emailHash) {
        T.UI();
        try {
            mGetEmailByEmailHashUI.bindBlob(1, emailHash);
            return mGetEmailByEmailHashUI.simpleQueryForString();
        } catch (SQLiteDoneException e) {
            // Did not find email
            return null;
        }
    }

    public void insertInvitationSecrets(final String[] secrets) {
        T.BIZZ();
        TransactionHelper.runInTransaction(mDb, "insertInvitationSecrets", new TransactionWithoutResult() {
            @Override
            protected void run() {
                for (String secret : secrets) {
                    mInsertInvitationSecretHTTP.bindString(1, secret);
                    mInsertInvitationSecretHTTP.execute();
                }
            }
        });
    }

    public String popInvitationSecret() {
        T.UI();
        return TransactionHelper.runInTransaction(mDb, "popInvitationSecret", new Transaction<String>() {
            @Override
            protected String run() {
                String secret;
                try {
                    secret = mGetInvitationSecretUI.simpleQueryForString();
                } catch (SQLiteDoneException sqliteDoneException) {
                    L.d("There are no invitation secrets anymore!");
                    return null;
                }

                mDeleteInvitationSecretUI.bindString(1, secret);
                mDeleteInvitationSecretUI.execute();
                return secret;
            }
        });
    }

    public long countInvitationSecrets() {
        T.UI();
        long count = mCountInvitationSecretUI.simpleQueryForLong();
        return count;
    }

    public List<String> getPendingInvitations() {
        T.UI();
        List<String> invitations = new ArrayList<String>();

        final Cursor cursor = mDb.rawQuery(mMainService.getString(R.string.sql_friend_pending_invitation_list), null);

        try {
            if (cursor.moveToFirst()) {
                do {
                    invitations.add(cursor.getString(0));
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }

        return invitations;
    }

    public void insertPendingInvitation(String invitee) {
        T.UI();
        mInsertPendingInvitationUI.bindString(1, invitee);
        mInsertPendingInvitationUI.execute();
    }

    public void removePendingInvitation(String invitee) {
        T.BIZZ();
        mRemovePendingInvitationUI.bindString(1, invitee);
        mRemovePendingInvitationUI.execute();
    }

    public long insertServiceApiCall(final String service, final String item, final String method, final String tag,
        final long status) {
        T.BIZZ();
        int i = 1;
        mServiceApiCallInsertHTTP.bindString(i++, service);
        mServiceApiCallInsertHTTP.bindString(i++, item);
        mServiceApiCallInsertHTTP.bindString(i++, method);
        mServiceApiCallInsertHTTP.bindString(i++, tag);
        mServiceApiCallInsertHTTP.bindLong(i++, status);
        return mServiceApiCallInsertHTTP.executeInsert();
    }

    public ServiceApiCallbackResult setServiceApiCallResult(final long id, final String error, final String result,
        final long status) {
        T.BIZZ();
        return TransactionHelper.runInTransaction(mDb, "setServiceApiCallResult",
            new Transaction<ServiceApiCallbackResult>() {

                @Override
                protected ServiceApiCallbackResult run() {
                    ServiceApiCallbackResult r;
                    final Cursor curs = mDb.rawQuery(mMainService.getString(R.string.sql_service_api_call_get_by_id),
                        new String[] { Long.toString(id) });
                    try {
                        if (!curs.moveToFirst())
                            return null;
                        int i = 0;
                        final String service = curs.getString(i++);
                        final String item = curs.getString(i++);
                        final String method = curs.getString(i++);
                        final String tag = curs.getString(i++);
                        r = new ServiceApiCallbackResult(id, service, item, method, result, error, tag);
                    } finally {
                        curs.close();
                    }

                    int i = 1;
                    if (result == null) {
                        mServiceApiCallSetResultHTTP.bindNull(i++);
                    } else {
                        mServiceApiCallSetResultHTTP.bindString(i++, result);
                    }
                    if (error == null) {
                        mServiceApiCallSetResultHTTP.bindNull(i++);
                    } else {
                        mServiceApiCallSetResultHTTP.bindString(i++, error);
                    }
                    mServiceApiCallSetResultHTTP.bindLong(i++, status);
                    mServiceApiCallSetResultHTTP.bindLong(i++, id);
                    mServiceApiCallSetResultHTTP.execute();
                    return r;
                }
            });
    }

    public List<ServiceApiCallbackResult> getServiceApiCallbackResultsByItem(final String service, final String item) {
        T.dontCare();
        List<ServiceApiCallbackResult> results = new ArrayList<ServiceApiCallbackResult>();

        final Cursor cursor = mDb.rawQuery(mMainService.getString(R.string.sql_service_api_call_get_results),
            new String[] { service, item });
        try {
            if (cursor.moveToFirst()) {
                do {
                    int i = 0;
                    final long id = cursor.getLong(i++);
                    final String method = cursor.getString(i++);
                    final String result = cursor.getString(i++);
                    final String error = cursor.getString(i++);
                    final String tag = cursor.getString(i++);
                    results.add(new ServiceApiCallbackResult(id, service, item, method, result, error, tag));
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }

        return results;
    }

    public void removeServiceApiCall(final long id) {
        T.UI();
        mServiceApiCallRemoveUI.bindLong(1, id);
        mServiceApiCallRemoveUI.execute();
    }

    private void updateUserData(String serviceEmail, String userDataJsonString) {
        T.BIZZ();
        if (userDataJsonString == null) {
            mServiceUserDataUpdateBIZZ.bindNull(1);
        } else {
            mServiceUserDataUpdateBIZZ.bindString(1, userDataJsonString);
        }
        mServiceUserDataUpdateBIZZ.bindString(2, serviceEmail);
        mServiceUserDataUpdateBIZZ.execute();
    }

    private void updateAppData(String serviceEmail, String appDataJsonString) {
        T.BIZZ();
        if (appDataJsonString == null) {
            mServiceAppDataUpdateBIZZ.bindNull(1);
        } else {
            mServiceAppDataUpdateBIZZ.bindString(1, appDataJsonString);
        }
        mServiceAppDataUpdateBIZZ.bindString(2, serviceEmail);
        mServiceAppDataUpdateBIZZ.execute();
    }

    public void updateServiceData(String serviceEmail, String userDataJsonString, String appDataJsonString,
        boolean mustBroadcastIntent) {
        T.BIZZ();
        final boolean userDataUpdated = userDataJsonString != null;
        final boolean appDataUpdated = appDataJsonString != null;

        if (userDataUpdated && appDataUpdated) {
            mServiceDataUpdateBIZZ.bindString(1, userDataJsonString);
            mServiceDataUpdateBIZZ.bindString(1, appDataJsonString);
            mServiceDataUpdateBIZZ.bindString(3, serviceEmail);
            mServiceDataUpdateBIZZ.execute();
        } else if (userDataUpdated) {
            updateUserData(serviceEmail, userDataJsonString);
        } else if (appDataUpdated) {
            updateAppData(serviceEmail, appDataJsonString);
        } else {
            return;
        }

        if (mustBroadcastIntent) {
            broadcastServiceDataUpdated(serviceEmail, userDataUpdated, appDataUpdated);
        }
    }

    private void broadcastServiceDataUpdated(final String email, final boolean userDataUpdated,
        final boolean serviceDataUpdated) {
        Intent intent = new Intent(FriendsPlugin.SERVICE_DATA_UPDATED);
        intent.putExtra("email", email);
        intent.putExtra("user_data", userDataUpdated);
        intent.putExtra("service_data", serviceDataUpdated);
        mMainService.sendBroadcast(intent);
    }

    public ServiceMenuItemDetails getBroadcastServiceMenuItem(String email) {
        final Cursor cursor = mDb.rawQuery(mMainService.getString(R.string.sql_friend_get_broadcast_flow_for_mfr),
            new String[] { email });
        try {
            if (!cursor.moveToFirst()) {
                return null;
            }
            final ServiceMenuItemDetails smi = new ServiceMenuItemDetails();
            smi.coords = new long[]{cursor.getLong(0), cursor.getLong(1), cursor.getLong(2)};
            smi.staticFlowHash = cursor.getString(3);
            smi.hashedTag = cursor.getString(4);
            smi.menuGeneration = cursor.getLong(5);
            smi.label = cursor.getString(6);
            return smi;

        } finally {
            cursor.close();
        }
    }

    public long getFriendSetVersion() {
        T.BIZZ();
        final long version = mFriendSetVersionGetBizz.simpleQueryForLong();
        L.d("FriendSet version is " + version);
        return version;
    }

    public void updateFriendSetVersion(final long version) {
        T.BIZZ();
        mFriendSetVersionSetBizz.bindLong(1, version);
        mFriendSetVersionSetBizz.execute();
    }

    public void deleteFromFriendSet(final String email) {
        T.BIZZ();
        mFriendSetDeleteFromBizz.bindString(1, email);
        mFriendSetDeleteFromBizz.execute();
    }

    public void insertFriendIntoFriendSet(final String email) {
        T.BIZZ();
        mFriendSetInsertIntoBizz.bindString(1, email);
        mFriendSetInsertIntoBizz.execute();
    }

    public List<String> getFriendSet() {
        T.BIZZ();
        List<String> emails = new ArrayList<String>();

        final Cursor cursor = mDb.rawQuery(mMainService.getString(R.string.sql_friendset_get), null);

        try {
            if (cursor.moveToFirst()) {
                do {
                    emails.add(cursor.getString(0));
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }

        return emails;
    }

    public boolean friendSetContains(final String email) {
        mFriendSetContainsBizz.bindString(1, email);
        return mFriendSetContainsBizz.simpleQueryForLong() > 0;
    }

    public long[] getFriendVersions(final String email) {
        mGetFriendVersionsBizz.bindString(1, email);
        try {
            return getVersionsFromString(mGetFriendVersionsBizz.simpleQueryForString());
        } catch (SQLiteDoneException ex) {
            return new long[0];
        }
    }

    private static long[] getVersionsFromString(String versionsString) {
        if (TextUtils.isEmptyOrWhitespace(versionsString))
            return new long[0];

        final String[] versionStrings = versionsString.split(",");
        final long[] versions = new long[versionStrings.length];

        for (int i = 0; i < versionStrings.length; i++) {
            versions[i] = Long.parseLong(versionStrings[i]);
        }
        return versions;
    }

    private static String getStringFromVersions(long[] versions) {
        if (versions == null) {
            return "";
        }

        final StringBuilder sb = new StringBuilder();
        boolean firstTime = true;
        for (long v : versions) {
            if (firstTime) {
                firstTime = false;
            } else {
                sb.append(",");
            }
            sb.append(v);
        }

        return sb.toString();
    }

    private static int compareFriendVersions(final long[] versionsInDB, final long[] versionsFromServer) {
        T.dontCare();
        L.d("Comparing " + Arrays.toString(versionsInDB) + " with " + Arrays.toString(versionsFromServer));
        if (versionsInDB.length > versionsFromServer.length) {
            throw new IllegalFriendVersionComparisonException(versionsInDB, versionsFromServer);
        } else if (versionsInDB.length == 0) {
            return -1;
        }

        for (int i = 0; i < versionsInDB.length; i++) {
            long versionInDB = Long.valueOf(versionsInDB[i]);
            long versionFromServer = Long.valueOf(versionsFromServer[i]);

            if (versionInDB > versionFromServer) {
                return 1;
            } else if (versionInDB < versionFromServer) {
                return -1;
            }
        }

        return 0;
    }

    public Cursor getGroupListCursor() {
        return mDb.rawQuery(mMainService.getString(R.string.sql_group_list_cursor), null);
    }

    public Group readGroupFromCursor(Cursor cursor) {
        T.dontCare();
        Group g = new Group();
        g.guid = cursor.getString(1);
        g.name = cursor.getString(2);
        g.avatar = cursor.getBlob(3);
        g.avatarHash = cursor.getString(4);
        g.members = new ArrayList<String>();

        return g;
    }

    public Cursor getGroupMemberListCursor(String guid) {
        return mDb.rawQuery(mMainService.getString(R.string.sql_group_member_list_group), new String[] { guid });
    }

    public Friend readGroupMemberFromCursor(Cursor cursor) {
        T.UI();
        String email = cursor.getString(5);
        if (email == null)
            return null;
        return getExistingFriend(email);
    }

    public Group getGroup(String guid) {
        T.UI();
        Group g = null;
        final Cursor cursor = mDb.rawQuery(mMainService.getString(R.string.sql_get_group), new String[] { guid });

        try {
            if (cursor.moveToFirst()) {
                g = new Group();
                g.guid = cursor.getString(0);
                g.name = cursor.getString(1);
                g.avatar = cursor.getBlob(2);
                g.avatarHash = cursor.getString(3);
                g.members = new ArrayList<String>();
                do {
                    String member = cursor.getString(4);
                    if (member != null)
                        g.members.add(member);
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }

        return g;
    }

    public void clearGroups() {
        T.BIZZ();
        TransactionHelper.runInTransaction(mDb, "clearGroups", new TransactionWithoutResult() {
            @Override
            protected void run() {
                mClearGroupBIZZ.execute();
                mClearGroupMemberBIZZ.execute();
            }
        });
    }

    public void clearEmptyGroup() {
        T.BIZZ();
        mClearEmptyGroupBIZZ.execute();
    }

    public void clearGroupMember(String email) {
        T.BIZZ();
        mClearGroupMemberByEmailBIZZ.bindString(1, email);
        mClearGroupMemberByEmailBIZZ.execute();
    }

    public void insertGroupAvatar(String avatarHash, byte[] avatar) {
        T.BIZZ();
        mInsertGroupAvatarBIZZ.bindBlob(1, avatar);
        mInsertGroupAvatarBIZZ.bindString(2, avatarHash);
        mInsertGroupAvatarBIZZ.execute();
    }

    public void insertGroupAvatarHash(String guid, String avatarHash) {
        T.BIZZ();
        mInsertGroupAvatarHashBIZZ.bindString(1, avatarHash);
        mInsertGroupAvatarHashBIZZ.bindString(2, guid);
        mInsertGroupAvatarHashBIZZ.execute();
    }

    public void insertGroup(String guid, String name, byte[] avatar, String avatarHash) {
        T.dontCare(); // T.UI or T.BIZZ
        SQLiteStatement stmt = mInsertGroup.getStatementForThisThread();
        stmt.bindString(1, guid);
        stmt.bindString(2, name);
        if (avatar != null)
            stmt.bindBlob(3, avatar);
        else
            stmt.bindNull(3);
        if (avatarHash != null)
            stmt.bindString(4, avatarHash);
        else
            stmt.bindNull(4);
        stmt.execute();
    }

    public void updateGroup(String guid, String name, byte[] avatar, String avatarHash) {
        T.UI();
        mUpdateGroupUI.bindString(1, name);
        if (avatar != null)
            mUpdateGroupUI.bindBlob(2, avatar);
        else
            mUpdateGroupUI.bindNull(2);
        if (avatarHash != null)
            mUpdateGroupUI.bindString(3, avatarHash);
        else
            mUpdateGroupUI.bindNull(3);
        mUpdateGroupUI.bindString(4, guid);
        mUpdateGroupUI.execute();
    }

    public void deleteGroup(final String guid) {
        T.UI();
        TransactionHelper.runInTransaction(mDb, "deleteGroup", new TransactionWithoutResult() {
            @Override
            protected void run() {
                mDeleteGroupUI.bindString(1, guid);
                mDeleteGroupUI.execute();

                mDeleteGroupMembersUI.bindString(1, guid);
                mDeleteGroupMembersUI.execute();
            }
        });
    }

    public void insertGroupMember(String guid, String email) {
        T.dontCare(); // T.UI or T.BIZZ
        SQLiteStatement stmt = mInsertGroupMember.getStatementForThisThread();
        stmt.bindString(1, guid);
        stmt.bindString(2, email);
        stmt.execute();
    }

    public void deleteGroupMember(String guid, String email) {
        T.UI();
        mDeleteGroupMemberUI.bindString(1, guid);
        mDeleteGroupMemberUI.bindString(2, email);
        mDeleteGroupMemberUI.execute();
    }

    private void updateEmailHash(final String email, final long friendType) {
        T.BIZZ();
        final byte[] hash = EmailHashCalculator.calculateEmailHash(email, friendType);
        mFriendUpdateEmailHashBizz.bindBlob(1, hash);
        mFriendUpdateEmailHashBizz.bindString(2, email);
        mFriendUpdateEmailHashBizz.execute();
    }

    public void updateEmailHashesForAllFriends() {
        T.BIZZ();
        TransactionHelper.runInTransaction(mDb, "updateEmailHashesForAllFriends", new TransactionWithoutResult() {
            @Override
            protected void run() {
                for (final String email : getEmails(FriendsPlugin.FRIEND_TYPE_SERVICE)) {
                    updateEmailHash(email, FriendsPlugin.FRIEND_TYPE_SERVICE);
                }
                for (final String email : getEmails(FriendsPlugin.FRIEND_TYPE_USER)) {
                    updateEmailHash(email, FriendsPlugin.FRIEND_TYPE_USER);
                }
            }
        });
    }
}

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

package com.mobicage.rogerthat.plugins.friends;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.mobicage.api.friends.Rpc;
import com.mobicage.models.properties.profiles.PublicKeyTO;
import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.ContactListHelper;
import com.mobicage.rogerthat.IdentityStore;
import com.mobicage.rogerthat.MainService;
import com.mobicage.rogerthat.MyIdentity;
import com.mobicage.rogerthat.UserDetailActivity;
import com.mobicage.rogerthat.config.ConfigurationProvider;
import com.mobicage.rogerthat.plugins.MobicagePlugin;
import com.mobicage.rogerthat.plugins.history.HistoryItem;
import com.mobicage.rogerthat.plugins.messaging.BrandingMgr;
import com.mobicage.rogerthat.plugins.scan.GetServiceActionInfoRH;
import com.mobicage.rogerthat.plugins.scan.GetUserInfoResponseHandler;
import com.mobicage.rogerthat.plugins.scan.ProcessScanActivity;
import com.mobicage.rogerthat.plugins.trackme.TrackmePlugin;
import com.mobicage.rogerthat.util.RegexPatterns;
import com.mobicage.rogerthat.util.TextUtils;
import com.mobicage.rogerthat.util.db.DatabaseManager;
import com.mobicage.rogerthat.util.db.TransactionHelper;
import com.mobicage.rogerthat.util.db.TransactionWithoutResult;
import com.mobicage.rogerthat.util.geo.GeoLocationProvider;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.net.NetworkConnectivityManager;
import com.mobicage.rogerthat.util.system.SafeDialogClick;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.system.SystemUtils;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.ui.ImageHelper;
import com.mobicage.rogerthat.util.ui.UIUtils;
import com.mobicage.rpc.ResponseHandler;
import com.mobicage.rpc.config.CloudConstants;
import com.mobicage.to.friends.AckInvitationByInvitationSecretRequestTO;
import com.mobicage.to.friends.AckInvitationByInvitationSecretResponseTO;
import com.mobicage.to.friends.BecameFriendsRequestTO;
import com.mobicage.to.friends.BecameFriendsResponseTO;
import com.mobicage.to.friends.BreakFriendshipRequestTO;
import com.mobicage.to.friends.DeleteGroupRequestTO;
import com.mobicage.to.friends.DeleteGroupResponseTO;
import com.mobicage.to.friends.FindFriendRequestTO;
import com.mobicage.to.friends.FindRogerthatUsersViaEmailRequestTO;
import com.mobicage.to.friends.FindRogerthatUsersViaFacebookRequestTO;
import com.mobicage.to.friends.FriendTO;
import com.mobicage.to.friends.GetFriendEmailsRequestTO;
import com.mobicage.to.friends.GetFriendInvitationSecretsRequestTO;
import com.mobicage.to.friends.GetGroupAvatarRequestTO;
import com.mobicage.to.friends.GetGroupsRequestTO;
import com.mobicage.to.friends.GetUserInfoRequestTO;
import com.mobicage.to.friends.InviteFriendRequestTO;
import com.mobicage.to.friends.LogInvitationSecretSentRequestTO;
import com.mobicage.to.friends.LogInvitationSecretSentResponseTO;
import com.mobicage.to.friends.PutGroupRequestTO;
import com.mobicage.to.friends.RequestShareLocationRequestTO;
import com.mobicage.to.friends.ServiceMenuItemTO;
import com.mobicage.to.friends.ShareLocationRequestTO;
import com.mobicage.to.friends.UpdateFriendRequestTO;
import com.mobicage.to.friends.UpdateFriendResponseTO;
import com.mobicage.to.friends.UpdateFriendSetRequestTO;
import com.mobicage.to.friends.UpdateFriendSetResponseTO;
import com.mobicage.to.friends.UpdateGroupsRequestTO;
import com.mobicage.to.friends.UpdateGroupsResponseTO;
import com.mobicage.to.location.GetFriendLocationRequestTO;
import com.mobicage.to.location.GetFriendLocationResponseTO;
import com.mobicage.to.location.GetFriendsLocationResponseTO;
import com.mobicage.to.service.FindServiceRequestTO;
import com.mobicage.to.service.FindServiceResponseTO;
import com.mobicage.to.service.GetServiceActionInfoRequestTO;
import com.mobicage.to.service.GetStaticFlowRequestTO;
import com.mobicage.to.service.PokeServiceRequestTO;
import com.mobicage.to.service.PokeServiceResponseTO;
import com.mobicage.to.service.ReceiveApiCallResultRequestTO;
import com.mobicage.to.service.ReceiveApiCallResultResponseTO;
import com.mobicage.to.service.SendApiCallRequestTO;
import com.mobicage.to.service.SendApiCallResponseTO;
import com.mobicage.to.service.ShareServiceRequestTO;
import com.mobicage.to.service.ShareServiceResponseTO;
import com.mobicage.to.service.StartServiceActionRequestTO;
import com.mobicage.to.service.StartServiceActionResponseTO;
import com.mobicage.to.service.UpdateUserDataRequestTO;
import com.mobicage.to.service.UpdateUserDataResponseTO;
import com.mobicage.to.system.EditProfileRequestTO;
import com.mobicage.to.system.EditProfileResponseTO;
import com.mobicage.to.system.IdentityTO;
import com.mobicage.to.system.SetSecureInfoRequestTO;
import com.mobicage.to.system.SetSecureInfoResponseTO;
import com.mobicage.to.system.SettingsTO;

import org.jivesoftware.smack.util.Base64;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

public class FriendsPlugin implements MobicagePlugin {

    public static final String FRIEND_MARKED_FOR_REMOVAL_INTENT = "com.mobicage.rogerthat.plugins.friends.FRIEND_MARKED_FOR_REMOVAL_INTENT";
    public static final String FRIEND_REMOVED_INTENT = "com.mobicage.rogerthat.plugins.friends.FRIEND_REMOVED";
    public static final String FRIEND_UPDATE_INTENT = "com.mobicage.rogerthat.plugins.friends.FRIEND_MODIFIED";
    public static final String FRIEND_ADDED_INTENT = "com.mobicage.rogerthat.plugins.friends.FRIEND_ADDED";
    public static final String FRIENDS_LIST_REFRESHED = "com.mobicage.rogerthat.plugins.friends.FRIENDS_LIST_REFRESHED";
    public static final String FRIEND_LOCATION_RECEIVED_INTENT = "com.mobicage.rogerthat.plugins.friends.FRIEND_LOCATION_RECEIVED";
    public static final String FRIEND_AVATAR_CHANGED_INTENT = "com.mobicage.rogerthat.plugins.friends.FRIEND_AVATAR_CHANGED";
    public static final String FRIEND_QR_CODE_RECEIVED_INTENT = "com.mobicage.rogerthat.plugins.friends.FRIEND_QR_CODE_RECEIVED";
    public static final String ADDRESSBOOK_SCANNED_INTENT = "com.mobicage.rogerthat.plugins.friends.ADDRESSBOOK_SCANNED";
    public static final String ADDRESSBOOK_SCAN_FAILED_INTENT = "com.mobicage.rogerthat.plugins.friends.ADDRESSBOOK_SCAN_FAILED";
    public static final String FACEBOOK_SCANNED_INTENT = "com.mobicage.rogerthat.plugins.friends.FACEBOOK_SCANNED";
    public static final String FACEBOOK_SCAN_FAILED_INTENT = "com.mobicage.rogerthat.plugins.friends.FACEBOOK_SCAN_FAILED";
    public static final String SERVICE_SEARCH_RESULT_INTENT = "com.mobicage.rogerthat.plugins.friends.SERVICE_SEARCH_RESULT_INTENT";
    public static final String SERVICE_SEARCH_FAILED_INTENT = "com.mobicage.rogerthat.plugins.friends.SERVICE_SEARCH_FAILED_INTENT";
    public static final String FRIEND_SEARCH_RESULT_INTENT = "com.mobicage.rogerthat.plugins.friends.FRIEND_SEARCH_RESULT_INTENT";
    public static final String FRIEND_SEARCH_FAILED_INTENT = "com.mobicage.rogerthat.plugins.friends.FRIEND_SEARCH_FAILED_INTENT";
    public static final String SERVICE_API_CALL_ANSWERED_INTENT = "com.mobicage.rogerthat.plugins.friends.SERVICE_API_CALL_ANSWERED";
    public static final String SERVICE_DATA_UPDATED = "com.mobicage.rogerthat.plugins.friends.SERVICE_DATA_UPDATED";
    public static final String BEACON_IN_REACH = "com.mobicage.rogerthat.plugins.friends.BEACON_IN_REACH";
    public static final String BEACON_OUT_OF_REACH = "com.mobicage.rogerthat.plugins.friends.BEACON_OUT_OF_REACH";
    public final static String FRIEND_INFO_RECEIVED_INTENT = "com.mobicage.rogerthat.plugins.friends.FRIEND_INFO_RECEIVED";
    public static final String SERVICE_ACTION_INFO_RECEIVED_INTENT = "com.mobicage.rogerthat.plugins.friends.SERVICE_ACTION_INFO_RECEIVED";
    public static final String FRIENDS_PLUGIN_MUST_DO_FULL_REFRESH_INTENT = "com.mobicage.rogerthat.plugins.friends.FRIENDS_PLUGIN_MUST_DO_FULL_REFRESH_INTENT";
    public static final String FRIENDS_PLUGIN_MUST_GET_INVITATION_SECRETS = "com.mobicage.rogerthat.plugins.friends.FRIENDS_PLUGIN_MUST_GET_INVITATION_SECRETS";
    public static final String FRIENDS_PLUGIN_MUST_CHECK_IDENTITY_SHORTURL = "com.mobicage.rogerthat.plugins.friends.FRIENDS_PLUGIN_MUST_CHECK_IDENTITY_SHORTURL";
    public static final String FRIENDS_PLUGIN_MUST_REFRESH_IDENTITY = "com.mobicage.rogerthat.plugins.friends.FRIENDS_PLUGIN_MUST_REFRESH_IDENTITY";
    public static final String FRIENDS_PLUGIN_MUST_REFRESH_IDENTITY_QR_CODE = "com.mobicage.rogerthat.plugins.friends.FRIENDS_PLUGIN_MUST_REFRESH_IDENTITY_QR_CODE";
    public static final String FRIENDS_PLUGIN_MUST_UPDATE_EMAIL_HASHES = "com.mobicage.rogerthat.plugins.friends.FRIENDS_PLUGIN_MUST_UPDATE_EMAIL_HASHES";
    public static final String FRIENDS_PLUGIN_MUST_MIGRATE_FRIEND_DATA = "com.mobicage.rogerthat.plugins.friends.FRIENDS_PLUGIN_MUST_MIGRATE_FRIEND_DATA";

    public static final String FRIENDS_PLUGIN_MUST_GET_GROUPS = "com.mobicage.rogerthat.plugins.friends.FRIENDS_PLUGIN_MUST_GET_GROUPS";
    public static final String GROUPS_UPDATED = "com.mobicage.rogerthat.plugins.friends.GROUPS_UPDATED";
    public static final String GROUP_ADDED = "com.mobicage.rogerthat.plugins.friends.GROUP_ADDED";
    public static final String GROUP_MODIFIED = "com.mobicage.rogerthat.plugins.friends.GROUP_MODIFIED";
    public static final String GROUP_REMOVED = "com.mobicage.rogerthat.plugins.friends.GROUP_REMOVED";
    public static final String STATIC_FLOW_AVAILABLE_INTENT = "com.mobicage.rogerthat.plugins.friends.STATIC_FLOW_AVAILABLE_INTENT";
    public static final String SYSTEM_FRIEND = "dashboard@rogerth.at";
    public static final String SEARCH_STRING = "SEARCH_STRING";
    public static final String SEARCH_RESULT = "SEARCH_RESULT";
    public static final int ME = 1;
    public static final int FRIEND = 4;
    public static final int NON_FRIEND = 8;
    public static final int SYSTEM = 16;
    public static final int FRIEND_TYPE_UNKNOWN = -1; // return this when type is asked of non-friend
    public static final int FRIEND_TYPE_USER = 1;
    public static final int FRIEND_TYPE_SERVICE = 2;
    public static final long SERVICE_API_CALL_STATUS_SENT = 0;
    public static final long SERVICE_API_CALL_STATUS_ANSWERED = 1;
    public static final long SERVICE_CALLBACK_FRIEND_INVITE_RESULT = 1;
    public static final long SERVICE_CALLBACK_FRIEND_INVITED = 2;
    public static final long SERVICE_CALLBACK_FRIEND_BROKE_UP = 4;
    public static final long SERVICE_CALLBACK_FRIEND_IN_REACH = 512;
    public static final long SERVICE_CALLBACK_FRIEND_OUT_OF_REACH = 1024;
    public static final long SERVICE_CALLBACK_MESSAGING_RECEIVED = 8;
    public static final long SERVICE_CALLBACK_MESSAGING_POKE = 16;
    public static final long SERVICE_CALLBACK_MESSAGING_ACKNOWLEDGED = 128;
    public static final long SERVICE_CALLBACK_MESSAGING_FLOW_MEMBER_RESULT = 64;
    public static final long SERVICE_CALLBACK_MESSAGING_FORM_ACKNOWLEDGED = 32;
    public static final long SERVICE_CALLBACK_SYSTEM_API_CALL = 256;
    public static final long FRIEND_NOT_REMOVABLE = 1;
    private static final String CONFIGKEY = "com.mobicage.rogerthat.plugins.friends";
    // XXX: these values should be generated in stubs
    private static final long MODIFIED_FRIEND_STATUS = 2;
    private final ConfigurationProvider mConfigProvider;
    private final FriendStore mStore;
    private final MainService mMainService;
    private final BrandingMgr mBrandingMgr;
    private final NetworkConnectivityManager mNetworkConnectivityManager;
    private final FriendHistory mHistory;
    private final Bitmap mMissingGroupAvatarBitmap;
    private final Bitmap mMissingFriendAvatarBitmap;
    private final Bitmap mMissingNonFriendAvatarBitmap;
    private final Bitmap mSystemFriendAvatar;
    private final GeoLocationProvider mGeoProvider;
    private final TrackmePlugin mTrackmePlugin;
    private final Map<String, JSONArray> mDisabledBroadcastTypesCache = new HashMap<>();
    private FindServiceResponseTO mLastResponse;

    public FriendsPlugin(final DatabaseManager pDatabaseManager, final ConfigurationProvider pConfigProvider,
                         final MainService mainService, final NetworkConnectivityManager connectivityManager,
                         final BrandingMgr brandingMgr, final GeoLocationProvider pGeoProvider) {
        T.UI();
        mStore = new FriendStore(pDatabaseManager, mainService);
        mConfigProvider = pConfigProvider;
        mGeoProvider = pGeoProvider;
        mMainService = mainService;

        mMainService.addHighPriorityIntent(FRIEND_QR_CODE_RECEIVED_INTENT);
        mMainService.addHighPriorityIntent(ADDRESSBOOK_SCAN_FAILED_INTENT);
        mMainService.addHighPriorityIntent(ADDRESSBOOK_SCANNED_INTENT);
        mMainService.addHighPriorityIntent(FACEBOOK_SCAN_FAILED_INTENT);
        mMainService.addHighPriorityIntent(FACEBOOK_SCANNED_INTENT);
        mMainService.addHighPriorityIntent(SERVICE_SEARCH_FAILED_INTENT);
        mMainService.addHighPriorityIntent(SERVICE_SEARCH_RESULT_INTENT);
        mMainService.addHighPriorityIntent(FRIEND_ADDED_INTENT);
        mMainService.addHighPriorityIntent(FRIEND_MARKED_FOR_REMOVAL_INTENT);
        mMainService.addHighPriorityIntent(SERVICE_API_CALL_ANSWERED_INTENT);

        mMainService.addHighPriorityIntent(GROUPS_UPDATED);
        mMainService.addHighPriorityIntent(GROUP_ADDED);
        mMainService.addHighPriorityIntent(GROUP_MODIFIED);
        mMainService.addHighPriorityIntent(GROUP_REMOVED);

        mBrandingMgr = brandingMgr;
        mNetworkConnectivityManager = connectivityManager;
        mHistory = new FriendHistory(this, mainService);
        mMissingGroupAvatarBitmap = ImageHelper.getRoundedCornerAvatar(BitmapFactory.decodeResource(
                mMainService.getResources(), R.drawable.group_60));
        mMissingFriendAvatarBitmap = ImageHelper.getRoundedCornerAvatar(BitmapFactory.decodeResource(
                mMainService.getResources(), R.drawable.unknown_avatar));
        mMissingNonFriendAvatarBitmap = ImageHelper.getRoundedCornerAvatar(BitmapFactory.decodeResource(
                mMainService.getResources(), R.drawable.unknown_avatar_non_friend));
        mSystemFriendAvatar = ImageHelper.getRoundedCornerAvatar(BitmapFactory.decodeResource(
                mMainService.getResources(), R.drawable.ic_dashboard));

        mTrackmePlugin = mMainService.getPlugin(TrackmePlugin.class);
    }

    public FriendStore getStore() {
        T.dontCare();
        return mStore;
    }

    public BrandingMgr getBrandingMgr() {
        T.dontCare();
        return mBrandingMgr;
    }

    public void setLastSearchResult(final String searchString, final FindServiceResponseTO response) {
        T.dontCare();
        mMainService.runOnUIHandler(new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                mLastResponse = response;
                Intent intent = new Intent(SERVICE_SEARCH_RESULT_INTENT);
                intent.putExtra(SEARCH_STRING, searchString);
                mMainService.sendBroadcast(intent);
            }
        });
    }

    public FindServiceResponseTO getLastSearchResult() {
        T.UI();
        try {
            return mLastResponse;
        } finally {
            mLastResponse = null;
        }
    }

    public static void broadcastServiceDataUpdated(final MainService mainservice, final String email, final boolean
            userDataUpdated,
                                                   final boolean serviceDataUpdated) {
        if (userDataUpdated || serviceDataUpdated) {
            Intent intent = new Intent(SERVICE_DATA_UPDATED);
            intent.putExtra("email", email);
            intent.putExtra("user_data", userDataUpdated);
            intent.putExtra("service_data", serviceDataUpdated);
            mainservice.sendBroadcast(intent);
        }
    }

    @Override
    public void initialize() {
        T.UI();
        reconfigure();
        initCallReceiver();
        mConfigProvider.registerListener(CONFIGKEY, this);

        // ugly hack to run this initialization _after_ all plugins have been initialized
        // XXX: build support in MainService framework
        mMainService.postOnUIHandler(new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                T.UI();
                mMainService.postOnBIZZHandler(new SafeRunnable() {
                    @Override
                    protected void safeRun() throws Exception {
                        T.BIZZ();
                        final Set<String> pluginDBUpdates = mMainService.getPluginDBUpdates(FriendsPlugin.class);
                        final boolean mustDoRefresh = pluginDBUpdates
                                .contains(FRIENDS_PLUGIN_MUST_DO_FULL_REFRESH_INTENT);
                        boolean justRegistered = mStore.getFriendSetVersion() < 0;
                        if (justRegistered || mustDoRefresh) {
                            requestFriendSet(mustDoRefresh, justRegistered);
                        }
                        if (mustDoRefresh) {
                            mMainService.clearPluginDBUpdate(FriendsPlugin.class,
                                    FRIENDS_PLUGIN_MUST_DO_FULL_REFRESH_INTENT);
                        }

                        if (pluginDBUpdates.contains(FRIENDS_PLUGIN_MUST_GET_INVITATION_SECRETS)) {
                            mMainService.postOnUIHandler(new SafeRunnable() {
                                @Override
                                protected void safeRun() throws Exception {
                                    requestInvitationSecrets();
                                }
                            });
                            updateToNewStyleShortLink();
                            mMainService.clearPluginDBUpdate(FriendsPlugin.class,
                                    FRIENDS_PLUGIN_MUST_GET_INVITATION_SECRETS);
                        }

                        if (pluginDBUpdates.contains(FRIENDS_PLUGIN_MUST_CHECK_IDENTITY_SHORTURL)) {
                            checkIdentityShortURL();
                            mMainService.clearPluginDBUpdate(FriendsPlugin.class,
                                    FRIENDS_PLUGIN_MUST_CHECK_IDENTITY_SHORTURL);
                        }

                        if (pluginDBUpdates.contains(FRIENDS_PLUGIN_MUST_REFRESH_IDENTITY)) {
                            refreshIdentityOnUIHandler();
                            mMainService.clearPluginDBUpdate(FriendsPlugin.class, FRIENDS_PLUGIN_MUST_REFRESH_IDENTITY);
                        }

                        if (pluginDBUpdates.contains(FRIENDS_PLUGIN_MUST_REFRESH_IDENTITY_QR_CODE)) {
                            IdentityStore store = mMainService.getIdentityStore();
                            MyIdentity myIdentity = store.getIdentity();
                            store.refreshQR(myIdentity.getEmail());
                            mMainService.clearPluginDBUpdate(FriendsPlugin.class, FRIENDS_PLUGIN_MUST_REFRESH_IDENTITY_QR_CODE);
                        }

                        if (pluginDBUpdates.contains(FRIENDS_PLUGIN_MUST_GET_GROUPS)) {
                            requestGroups();
                            mMainService.clearPluginDBUpdate(FriendsPlugin.class, FRIENDS_PLUGIN_MUST_GET_GROUPS);
                        }

                        if (pluginDBUpdates.contains(FRIENDS_PLUGIN_MUST_UPDATE_EMAIL_HASHES)) {
                            mStore.updateEmailHashesForAllFriends();
                            mMainService.clearPluginDBUpdate(FriendsPlugin.class,
                                    FRIENDS_PLUGIN_MUST_UPDATE_EMAIL_HASHES);
                        }

                        if (pluginDBUpdates.contains(FRIENDS_PLUGIN_MUST_MIGRATE_FRIEND_DATA)) {
                            mStore.migrateFriendData();
                            mMainService.clearPluginDBUpdate(FriendsPlugin.class,
                                    FRIENDS_PLUGIN_MUST_MIGRATE_FRIEND_DATA);
                        }
                    }
                });
            }
        });
    }

    private void updateToNewStyleShortLink() {
        // Update old-style short link (s/) to new-style short link (M/)
        MyIdentity identity = mMainService.getIdentityStore().getIdentity();
        if (identity != null) {
            String oldLink = identity.getShortLink();
            if (oldLink != null
                    && oldLink.toLowerCase(Locale.US).startsWith(ProcessScanActivity.SHORT_HTTPS_URL_PREFIX)) {
                String newLink = ProcessScanActivity.SHORT_HTTPS_URL_PREFIX.replace("s/", "M/")
                        + oldLink.substring(ProcessScanActivity.SHORT_HTTPS_URL_PREFIX.length());
                mMainService.getIdentityStore().updateShortUrl(newLink);
            }
        }
    }

    private void checkIdentityShortURL() {
        // Special code to deal with URLs such as https://rogerth.at/M//ABCD
        // which facebook cuts down to https://rogerth.at/M/ABCD
        MyIdentity identity = mMainService.getIdentityStore().getIdentity();
        if (identity != null && identity.getShortLink() != null) {
            Matcher m = RegexPatterns.IDENTITY_SHORT_URL.matcher(identity.getShortLink());
            if (m.matches()) {
                String code = m.group(1);
                if (code.contains("/")) {
                    refreshIdentityOnUIHandler();
                }
            }
        }
    }

    private void refreshIdentityOnUIHandler() {
        mMainService.postOnUIHandler(new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                mMainService.getIdentityStore().refreshIdentity();
            }
        });
    }

    public void requestFriendSet(final boolean force, final boolean recalculateMessagesShowInList) {
        T.dontCare();
        mMainService.postOnUIHandler(new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                T.UI();
                final GetFriendEmailsRequestTO request = new GetFriendEmailsRequestTO();
                final GetFriendEmailsResponseHandler responseHandler = new GetFriendEmailsResponseHandler();
                responseHandler.setForce(force);
                responseHandler.setRecalculateMessagesShowInList(recalculateMessagesShowInList);
                try {
                    com.mobicage.api.friends.Rpc.getFriendEmails(responseHandler, request);
                } catch (Exception e) {
                    L.bug(e);
                }
            }
        });
    }

    public void requestFriend(final String email, final boolean force, final boolean isLast,
                              final boolean recalculateMessagesShowInList) {
        T.dontCare();
        mMainService.postOnUIHandler(new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                T.UI();
                final com.mobicage.to.friends.GetFriendRequestTO request = new com.mobicage.to.friends.GetFriendRequestTO();
                request.email = email;
                request.avatar_size = 50 * mMainService.getScreenScale();
                final GetFriendResponseHandler responseHandler = new GetFriendResponseHandler();
                responseHandler.setForce(force);
                responseHandler.setRecalculateMessagesShowInList(recalculateMessagesShowInList);
                responseHandler.setIsLast(isLast);
                try {
                    com.mobicage.api.friends.Rpc.getFriend(responseHandler, request);
                } catch (Exception e) {
                    L.bug(e);
                }
            }
        });
    }

    public void storeNewFriend(final FriendTO newFriend, byte[] avatar, boolean force) {
        T.BIZZ();
        mStore.storeFriend(newFriend, avatar, force);

        TransactionHelper.onTransactionCommitted(new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                mBrandingMgr.queue(newFriend);
            }
        });
    }

    public UpdateFriendSetResponseTO updateFriendSet(final List<String> newFriendSet, final long version,
                                                     final boolean force, final boolean recalculateMessagesShowInList, final FriendTO newFriend) {
        T.BIZZ();
        final UpdateFriendSetResponseTO response = new UpdateFriendSetResponseTO();
        final List<String> addedFriends = new ArrayList<String>();
        final List<String> removedFriends = new ArrayList<String>();

        mStore.runInTransaction("updateFriendSet", new TransactionWithoutResult() {
            @Override
            protected void run() {
                T.BIZZ();
                long versionInDB;

                if (!force && (versionInDB = mStore.getFriendSetVersion()) >= version) {
                    response.updated = false;
                    response.reason = "Version in local DB (" + versionInDB + ") >= version on server (" + version
                            + ")";
                    return;
                }

                final List<String> oldFriendSet = mStore.getFriendSet();

                // Get the difference between the new and the old set
                addedFriends.addAll(newFriendSet);
                addedFriends.removeAll(oldFriendSet);
                L.d("Added friends: " + addedFriends);

                removedFriends.addAll(oldFriendSet);
                removedFriends.removeAll(newFriendSet);
                L.d("Removed friends: " + removedFriends);

                if (!force && addedFriends.size() == 0 && removedFriends.size() == 0) {
                    response.updated = false;
                    response.reason = "The new and old friendSets are identical";
                } else {
                    for (String removedFriend : removedFriends) {
                        mStore.deleteFromFriendSet(removedFriend);
                        mStore.deleteFriend(removedFriend);
                        mStore.clearGroupMember(removedFriend);

                        mTrackmePlugin.deleteBeaconDiscovery(removedFriend);
                    }
                    mStore.clearEmptyGroup();

                    for (String addedFriend : addedFriends) {
                        mStore.insertFriendIntoFriendSet(addedFriend);
                    }

                    response.updated = true;
                }

                if (newFriend != null) {
                    storeNewFriend(newFriend, null, true); // using force to prevent extra db query
                    response.updated = true;
                    response.reason = null;
                }

                mStore.updateFriendSetVersion(version);
            }
        });

        List<String> friendsToRequest = force ? newFriendSet : addedFriends;
        if (response.updated && newFriend != null) {
            if (friendsToRequest.contains(newFriend.email)) {
                friendsToRequest.remove(newFriend.email);
            }

            Intent intent = new Intent(FRIEND_ADDED_INTENT);
            intent.putExtra("email", newFriend.email);
            mMainService.sendBroadcast(intent);
        }

        for (String friend : friendsToRequest) {
            requestFriend(friend, force, friendsToRequest.indexOf(friend) == friendsToRequest.size() - 1,
                    recalculateMessagesShowInList);
        }

        for (String removedFriend : removedFriends) {
            Intent intent = new Intent(FRIEND_REMOVED_INTENT);
            intent.putExtra("email", removedFriend);
            mMainService.sendBroadcast(intent);

            mHistory.putRemoveFriendInHistory(removedFriend);
        }

        return response;
    }

    private void initCallReceiver() {
        com.mobicage.rpc.CallReceiver.comMobicageCapiServicesIClientRpc = new com.mobicage.capi.services.IClientRpc() {
            @Override
            public ReceiveApiCallResultResponseTO receiveApiCallResult(ReceiveApiCallResultRequestTO request)
                    throws Exception {
                T.BIZZ();
                final ServiceApiCallbackResult r = mStore.setServiceApiCallResult(request.id, request.error,
                        request.result, SERVICE_API_CALL_STATUS_ANSWERED);

                if (r != null) {
                    Intent intent = new Intent(SERVICE_API_CALL_ANSWERED_INTENT);
                    intent.putExtra("service", r.service);
                    intent.putExtra("item", r.item);
                    mMainService.sendBroadcast(intent);
                } else {
                    // Double execution?
                    L.bug("Could not find service_api_call with id " + request.id);
                }

                return new ReceiveApiCallResultResponseTO();
            }

            @Override
            public UpdateUserDataResponseTO updateUserData(UpdateUserDataRequestTO request) throws Exception {
                if (request.data != null) {
                    FriendsPlugin.this.updateUserData(request.service, request.type,
                            (Map<String, Object>) JSONValue.parse(request.data));
                } else if (request.user_data != null) {
                    replaceUserData(request.service, request.user_data, null);
                } else if (request.app_data != null) {
                    replaceUserData(request.service, null, request.app_data);
                }

                return new UpdateUserDataResponseTO();
            }
        };

        com.mobicage.rpc.CallReceiver.comMobicageCapiFriendsIClientRpc = new com.mobicage.capi.friends.IClientRpc() {
            @Override
            public UpdateFriendResponseTO updateFriend(final UpdateFriendRequestTO request) throws Exception {
                T.BIZZ();
                UpdateFriendResponseTO response;
                if (request.status != MODIFIED_FRIEND_STATUS) {
                    response = new UpdateFriendResponseTO();
                    response.updated = false;
                    response.reason = "Ignoring updateFriend request because it's status is not STATUS_MODIFIED";
                } else if (request.friend == null) {
                    response = new UpdateFriendResponseTO();
                    response.updated = false;
                    response.reason = "friend was null";
                } else {
                    response = mStore.updateFriend(request.friend);
                    if (response.updated) {
                        mBrandingMgr.queue(request.friend);
                        Intent intent = new Intent(FRIEND_UPDATE_INTENT);
                        intent.putExtra("email", request.friend.email);
                        mMainService.sendBroadcast(intent);
                        mHistory.putUpdateFriendInHistory(request.friend.email);
                    }
                }

                if (!response.updated) {
                    L.d(response.reason);
                }
                return response;
            }

            @Override
            public UpdateFriendSetResponseTO updateFriendSet(UpdateFriendSetRequestTO request) throws Exception {
                return FriendsPlugin.this.updateFriendSet(Arrays.asList(request.friends), request.version, false,
                        false, request.added_friend);
            }

            @Override
            public BecameFriendsResponseTO becameFriends(BecameFriendsRequestTO request) throws Exception {
                mHistory.putFriendBecameFriendWithInHistory(request.user, request.friend.name, request.friend.email);
                return null;
            }

            @Override
            public UpdateGroupsResponseTO updateGroups(UpdateGroupsRequestTO request) throws Exception {
                requestGroups();
                return new UpdateGroupsResponseTO();
            }
        };
    }

    public void replaceUserData(final String serviceEmail, final String userData, final String appData) {
        replaceUserData(serviceEmail, userData == null ? null : (Map<String, Object>) JSONValue.parse(userData),
                appData == null ? null : (Map<String, Object>) JSONValue.parse(appData));
    }

    public void replaceUserData(final String serviceEmail, final Map<String, Object> userData, final Map<String,
            Object> appData) {
        final boolean userDataUpdated = userData != null;
        final boolean appDataUpdated = appData != null;
        if (userDataUpdated)  {
            mStore.replaceUserData(serviceEmail, FriendStore.FRIEND_DATA_TYPE_USER, userData);
        }
        if (appDataUpdated)  {
            mStore.replaceUserData(serviceEmail, FriendStore.FRIEND_DATA_TYPE_APP, appData);
        }
        broadcastServiceDataUpdated(mMainService, serviceEmail, userDataUpdated, appDataUpdated);
    }

    public void updateUserData(final String serviceEmail, final String type, final Map<String, Object> data) {
        mStore.updateUserData(serviceEmail, type, data);

        broadcastServiceDataUpdated(mMainService, serviceEmail, FriendStore.FRIEND_DATA_TYPE_USER.equals(type),
                FriendStore.FRIEND_DATA_TYPE_APP.equals(type));
    }

    @Override
    public void destroy() {
        T.UI();
        mConfigProvider.unregisterListener(CONFIGKEY, this);
        com.mobicage.rpc.CallReceiver.comMobicageCapiServicesIClientRpc = null;
        com.mobicage.rpc.CallReceiver.comMobicageCapiFriendsIClientRpc = null;
        mBrandingMgr.close();
        mStore.close();
    }

    @Override
    public void processSettings(SettingsTO settings) {
        T.UI();
    }

    @Override
    public void reconfigure() {
        T.UI();
    }

    public boolean updateFriendShareLocation(final String email, final boolean shareLocation) {
        T.UI();
        ShareLocationRequestTO request = new ShareLocationRequestTO();
        request.friend = email;
        request.enabled = shareLocation;
        try {
            L.d("updateFriendShareLocation");
            Rpc.shareLocation(new ShareLocationResponseHandler(), request);
        } catch (Exception e) {
            L.bug("Error while sending share location rpc request", e);
            return false;
        }
        mMainService.postOnBIZZHandler(new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                T.BIZZ();
                mStore.updateFriendShareLocation(email, shareLocation);
            }
        });
        return true;
    }

    public boolean requestFriendShareLocation(final String email, final String message) {
        T.UI();
        RequestShareLocationRequestTO request = new RequestShareLocationRequestTO();
        request.friend = email;
        request.message = message;
        try {
            Rpc.requestShareLocation(new RequestShareLocationResponseHandler(), request);
        } catch (Exception e) {
            L.bug("Error while sending request share location rpc request", e);
            return false;
        }
        return true;
    }

    // Function has following behaviour:
    // 1. mark friend as removed in db
    // 2. send break friendship message to server
    public boolean scheduleFriendRemoval(final String email) {
        T.UI();

        BreakFriendshipRequestTO request = new BreakFriendshipRequestTO();
        request.friend = email;
        try {
            Rpc.breakFriendShip(new BreakFriendshipResponseHandler(), request);
        } catch (Exception e) {
            L.bug("Error while sending break friendship rpc request", e);
            return false;
        }

        mMainService.postOnBIZZHandler(new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                T.BIZZ();
                mStore.clearGroupMember(email);
                mStore.clearEmptyGroup();

                mTrackmePlugin.deleteBeaconDiscovery(email);

                mStore.setFriendExistence(email, Friend.DELETION_PENDING);
                Intent intent = new Intent(FRIEND_MARKED_FOR_REMOVAL_INTENT);
                intent.putExtra("email", email);
                mMainService.sendBroadcast(intent);
            }
        });

        return true;
    }

    public boolean inviteService(final Friend service) {
        T.UI();
        return inviteService(service, true);
    }

    public boolean inviteService(final Friend service, boolean showNotification) {
        T.UI();
        if (inviteFriend(service.email, null, service.getDisplayName(), showNotification)) {
            mMainService.postOnBIZZHandler(new SafeRunnable() {
                @Override
                protected void safeRun() throws Exception {
                    mStore.addInvitedService(service);
                }
            });
            Intent intent = new Intent(FRIEND_ADDED_INTENT);
            intent.putExtra("email", service.email);
            mMainService.sendBroadcast(intent);
            return true;
        }
        return false;
    }

    public boolean inviteFriend(final String emailOrHash, final String message, final String friendName,
                                final boolean showNotification) {
        T.UI();
        if (TextUtils.isEmptyOrWhitespace(emailOrHash)) {
            L.bug("Trying to invite friend with email '" + emailOrHash + "'");
            return false;
        }
        InviteFriendRequestTO request = new InviteFriendRequestTO();
        request.email = emailOrHash;
        request.message = message;
        try {
            Rpc.invite(new InviteResponseHandler(emailOrHash), request);
        } catch (Exception e) {
            L.bug("Error while sending invite friend rpc request", e);
            return false;
        }
        if (showNotification)
            UIUtils.showLongToast(mMainService, mMainService.getString(R.string.invitation_sent_successfully));

        mStore.insertPendingInvitation(emailOrHash);

        mMainService.putInHistoryLog(
                mMainService.getString(R.string.invited_activity_record, (friendName == null) ? emailOrHash : friendName),
                HistoryItem.INFO);

        return true;
    }

    public boolean searchService(final String searchString, final int organizationType, final String cursor, final String hashedTag) {
        FindServiceRequestTO request = new FindServiceRequestTO();
        request.search_string = searchString;
        request.geo_point = mMainService.isPermitted(Manifest.permission.ACCESS_FINE_LOCATION) ? mGeoProvider
                .getLastKnownGeoPointWithTimestampTO() : null;
        request.organization_type = organizationType;
        request.cursor = cursor;
        request.avatar_size = 50 * mMainService.getScreenScale();
        request.hashed_tag = hashedTag;

        try {
            com.mobicage.api.services.Rpc.findService(new FindServiceResponseHandler(searchString), request);
        } catch (Exception e) {
            L.bug("Error while searching service with search_string " + searchString, e);
            return false;
        }

        return true;
    }

    public boolean searchFriend(final String searchString, final String cursor) {
        FindFriendRequestTO request = new FindFriendRequestTO();
        request.search_string = searchString;
        request.cursor = cursor;
        request.avatar_size = 50 * mMainService.getScreenScale();

        try {
            com.mobicage.api.friends.Rpc.findFriend(new FindFriendResponseHandler(searchString), request);
        } catch (Exception e) {
            L.bug("Error while searching friend with search_string " + searchString, e);
            return false;
        }

        return true;
    }

    public boolean shareService(final String serviceEmail, final String recipientEmail) {
        if (TextUtils.isEmptyOrWhitespace(recipientEmail)) {
            L.bug("Trying to recommend service '" + serviceEmail + "' to friend with email '" + recipientEmail + "'");
            return false;
        }

        ShareServiceRequestTO request = new ShareServiceRequestTO();
        request.recipient = recipientEmail;
        request.service_email = serviceEmail;

        try {
            com.mobicage.api.services.Rpc.shareService(new ResponseHandler<ShareServiceResponseTO>(), request);
        } catch (Exception e) {
            L.bug("Error while recommending service " + serviceEmail + " to " + recipientEmail, e);
            return false;
        }

        return true;
    }

    public boolean pokeService(final String serviceEmail, final String hashedTag, String contextMatch) {
        T.UI();
        PokeServiceRequestTO request = new PokeServiceRequestTO();
        request.email = serviceEmail;
        request.hashed_tag = hashedTag;
        request.context = contextMatch;
        request.timestamp = System.currentTimeMillis() / 1000;
        try {
            com.mobicage.api.services.Rpc.pokeService(new ResponseHandler<PokeServiceResponseTO>(), request);
        } catch (Exception e) {
            L.bug("Error while starting poke " + hashedTag + " for service " + serviceEmail, e);
            return false;
        }
        mHistory.putServicePokedInHistory(serviceEmail, hashedTag);
        return true;
    }

    public boolean startAction(final String serviceEmail, final String action, String contextMatch) {
        T.UI();
        StartServiceActionRequestTO request = new StartServiceActionRequestTO();
        request.email = serviceEmail;
        request.action = action;
        request.context = contextMatch;
        request.timestamp = System.currentTimeMillis() / 1000;
        try {
            com.mobicage.api.services.Rpc.startAction(new ResponseHandler<StartServiceActionResponseTO>(), request);
        } catch (Exception e) {
            L.bug("Error while starting service action " + action + " for service " + serviceEmail, e);
            return false;
        }
        mHistory.putServicePokedInHistory(serviceEmail, action);
        return true;
    }

    public boolean getServiceActionInfo(final String userCode, final String action) {
        T.UI();
        GetServiceActionInfoRH rh = new GetServiceActionInfoRH();
        rh.setAction(action);
        rh.setCode(userCode);

        GetServiceActionInfoRequestTO request = new GetServiceActionInfoRequestTO();
        request.code = userCode;
        request.action = action;
        try {
            com.mobicage.api.services.Rpc.getActionInfo(rh, request);
        } catch (Exception e) {
            L.bug("Error while requesting info of service action " + action + " for service " + userCode, e);
            return false;
        }

        return true;
    }

    public boolean scheduleSingleFriendLocationRetrieval(final String email) {
        T.UI();
        if (!mNetworkConnectivityManager.isConnected())
            return false;
        GetFriendLocationRequestTO request = new GetFriendLocationRequestTO();
        request.friend = email;
        try {
            com.mobicage.api.location.Rpc
                    .getFriendLocation(new ResponseHandler<GetFriendLocationResponseTO>(), request);
        } catch (Exception e) {
            L.bug("Error while sending get friend location rpc request", e);
            return false;
        }
        return true;
    }

    public boolean scheduleAllFriendsLocationRetrieval() {
        T.UI();
        if (!mNetworkConnectivityManager.isConnected())
            return false;
        try {
            com.mobicage.api.location.Rpc.getFriendLocations(new ResponseHandler<GetFriendsLocationResponseTO>(), null);
        } catch (Exception e) {
            L.bug("Error while sending get friends location rpc request", e);
            return false;
        }
        return true;
    }

    public Bitmap getAvatarBitmap(String email) {
        return getAvatarBitmap(email, false, -1);
    }

    public Bitmap getAvatarBitmap(String email, int size) {
        return getAvatarBitmap(email, false, size);
    }

    public Bitmap getAvatarBitmap(String email, boolean showAddFriendIfNotFriend, int size) {
        return getAvatarBitmap(email, showAddFriendIfNotFriend, null, size);
    }

    public Bitmap getAvatarBitmap(String email, boolean showAddFriendIfNotFriend, SafeRunnable friendNotFoundRunnable, int size) {
        T.dontCare();
        MyIdentity identity = mMainService.getIdentityStore().getIdentity();
        if (identity.getEmail().equals(email))
            return identity.getAvatarBitmap();

        if (SYSTEM_FRIEND.equals(email)) {
            return mSystemFriendAvatar;
        } else {
            final Bitmap avatarBitmap = mStore.getAvatarBitmap(email, size);
            if (avatarBitmap == null) {
                if (friendNotFoundRunnable != null) {
                    friendNotFoundRunnable.run();
                }

                if (!showAddFriendIfNotFriend) {
                    return mMissingFriendAvatarBitmap;
                } else {
                    final int contactType = getContactType(email);
                    if ((contactType & FriendsPlugin.FRIEND) == FriendsPlugin.FRIEND
                            || (contactType & FriendsPlugin.ME) == FriendsPlugin.ME)
                        return mMissingFriendAvatarBitmap;
                    else
                        return mMissingNonFriendAvatarBitmap;
                }
            }
            return avatarBitmap;
        }
    }

    public void requestUserInfo(String email, boolean storeInDB) {
        final GetUserInfoRequestTO request = new GetUserInfoRequestTO();
        request.code = email;
        request.allow_cross_app = false;

        final GetUserInfoResponseHandler handler = new GetUserInfoResponseHandler();
        handler.setCode(email);
        handler.setStoreInDB(storeInDB);

        try {
            com.mobicage.api.friends.Rpc.getUserInfo(handler, request);
        } catch (Exception e) {
            L.bug(e);
        }
    }

    public String getName(String email) {
        T.dontCare();
        if (SYSTEM_FRIEND.equals(email)) {
            if (CloudConstants.isRogerthatApp()) {
                return mMainService.getString(R.string.rogerthat_system);
            } else {
                return mMainService.getString(R.string.app_name);
            }
        }
        MyIdentity identity = mMainService.getIdentityStore().getIdentity();
        if (identity.getEmail().equals(email))
            return identity.getName();
        String name = mStore.getName(email);
        if (name == null)
            return email;
        else
            return name;
    }

    public int getContactType(String email) {
        MyIdentity identity = mMainService.getIdentityStore().getIdentity();
        if (identity.getEmail().equals(email))
            return ME;
        if (SYSTEM_FRIEND.equals(email))
            return SYSTEM;
        return mStore.isFriend(email) ? FRIEND : NON_FRIEND;
    }

    public FriendHistory getHistory() {
        return mHistory;
    }

    public Bitmap getMissingGroupAvatarBitmap() {
        return mMissingGroupAvatarBitmap;
    }

    public Bitmap getMissingFriendAvatarBitmap() {
        return mMissingFriendAvatarBitmap;
    }

    public void updateFriendAvatar(final String friendEmail, final byte[] avatarBytes) {
        T.BIZZ();
        mStore.updateFriendAvatar(friendEmail, avatarBytes);
    }

    public String getEmailByEmailHash(final byte[] emailHash) {
        T.UI();
        final String email = mStore.getEmailByEmailHash(emailHash);
        if (email != null)
            return email;
        final MyIdentity identity = mMainService.getIdentityStore().getIdentity();
        if (Arrays.equals(identity.getEmailHash(), emailHash)) {
            return identity.getEmail();
        }
        return null;
    }

    public void launchDetailActivity(final Activity activity, final String email) {
        final Friend friend = getStore().getExistingFriend(email);
        if (friend != null) {
            final Intent intent = new Intent(activity,
                    (friend.type == FriendsPlugin.FRIEND_TYPE_USER) ? UserDetailActivity.class
                            : ServiceActionMenuActivity.class);
            intent.putExtra("email", email);
            activity.startActivity(intent);
        }
    }

    public boolean requestInvitationSecrets() {
        T.UI();
        try {
            // This is a 'single call'
            Rpc.getFriendInvitationSecrets(new GetFriendInvitationSecretsResponseHandler(),
                    new GetFriendInvitationSecretsRequestTO());
        } catch (Exception e) {
            L.bug("Error while sending get friend invitation secrets rpc request", e);
            return false;
        }
        return true;
    }

    public String popInvitationSecret() {
        T.UI();
        String secret = mStore.popInvitationSecret();
        if (mStore.countInvitationSecrets() <= 10) {
            // Amount of secrets after the pop will be 10 or less ...
            requestInvitationSecrets();
        }
        return secret;
    }

    public boolean ackInvitationBySecret(String invitorCode, String secret) {
        T.UI();
        AckInvitationByInvitationSecretRequestTO request = new AckInvitationByInvitationSecretRequestTO();
        request.invitor_code = invitorCode;
        request.secret = secret;
        try {
            Rpc.ackInvitationByInvitationSecret(new ResponseHandler<AckInvitationByInvitationSecretResponseTO>(),
                    request);
        } catch (Exception e) {
            L.bug("Error while sending ack invitation by invitation secret rpc request", e);
            return false;
        }

        return true;
    }

    public boolean logInvitationSecretSent(String secret, String phoneNumber) {
        T.UI();
        LogInvitationSecretSentRequestTO request = new LogInvitationSecretSentRequestTO();
        request.phone_number = phoneNumber;
        request.secret = secret;
        request.timestamp = mMainService.currentTimeMillis() / 1000;
        try {
            Rpc.logInvitationSecretSent(new ResponseHandler<LogInvitationSecretSentResponseTO>(), request);
        } catch (Exception e) {
            L.bug("Error while sending ack invitation by invitation secret rpc request", e);
            return false;
        }

        return true;
    }

    public void storeMenuIcon(String iconHash, byte[] icon, String email) {
        T.BIZZ();
        mStore.storeMenuIcon(icon, iconHash);
        Intent intent = new Intent(FRIEND_UPDATE_INTENT);
        intent.putExtra("email", email);
        mMainService.sendBroadcast(intent);
    }

    public boolean isMenuIconAvailable(String iconHash) {
        return mStore.isMenuIconAvailable(iconHash);
    }

    public boolean isStaticFlowAvailable(String staticFlowHash) {
        return mStore.isStaticFlowAvailable(staticFlowHash);
    }

    public void requestStaticFlow(String email, ServiceMenuItemTO item) {
        GetStaticFlowRequestTO request = new GetStaticFlowRequestTO();
        request.service = email;
        request.coords = item.coords;
        request.staticFlowHash = item.staticFlowHash;
        GetStaticFlowResponseHandler rh = new GetStaticFlowResponseHandler();
        rh.setEmail(email);
        rh.setStaticFlowHash(item.staticFlowHash);
        try {
            com.mobicage.api.services.Rpc.getStaticFlow(rh, request);
        } catch (Exception e) {
            L.bug(e);
        }
    }

    public boolean findRogerthatUsersViaAddressBook() {
        T.UI();
        FindRogerthatUsersViaEmailRequestTO req = new FindRogerthatUsersViaEmailRequestTO();
        List<String> emails = ContactListHelper.getEmailAddresses(mMainService);

        req.email_addresses = emails.toArray(new String[emails.size()]);
        L.d("findRogerthatUsersViaEmail: " + emails);

        try {
            Rpc.findRogerthatUsersViaEmail(new FindUsersViaEmailResponeHandler(), req);
        } catch (Exception e) {
            L.bug(e);
            return false;
        }

        return true;
    }

    public boolean findRogerthatUsersViaFacebook(String fbAccessToken) {
        T.UI();
        FindRogerthatUsersViaFacebookRequestTO req = new FindRogerthatUsersViaFacebookRequestTO();
        req.access_token = fbAccessToken;

        try {
            Rpc.findRogerthatUsersViaFacebook(new FindUsersViaFacebookResponeHandler(), req);
        } catch (Exception e) {
            L.bug(e);
            return false;
        }

        return true;
    }

    public void removeFriendFromList(final Activity activity, final Friend friend) {
        T.UI();
        final boolean isService = friend.type == FriendsPlugin.FRIEND_TYPE_SERVICE;
        final String title = activity.getString(isService ? R.string.remove_service : R.string.remove_friend);
        final String message = activity.getString(isService ? R.string.confirm_remove_service : R.string
                .confirm_remove_friend, friend.getDisplayName());
        final String positiveBtn = activity.getString(isService ? R.string.unfollow : R.string.remove_friend);
        final String negativeButtonCaption = activity.getString(R.string.cancel);
        SafeDialogClick positiveClick = new SafeDialogClick() {
            @Override
            public void safeOnClick(DialogInterface dialog, int id) {
                if (!scheduleFriendRemoval(friend.email)) {
                    UIUtils.showLongToast(activity, (activity.getString(isService ? R.string.service_remove_failed
                            : R.string.friend_remove_failed)));
                    L.d("removeFriend failed");
                } else {
                    L.d("removeFriend succeeded");
                }
                dialog.dismiss();
            }
        };
        UIUtils.showDialog(activity, title, message, positiveBtn, positiveClick, negativeButtonCaption, null);
    }

    public void removeGroupFromList(final Activity activity, final Group group) {
        T.UI();
        final String title = activity.getString(R.string.delete_group);
        final String message = activity.getString(R.string.delete_group_message, group.name);
        final String positiveCaption = activity.getString(R.string.delete);
        final String negativeCaption = activity.getString(R.string.cancel);
        final SafeDialogClick positiveClick = new SafeDialogClick() {
            @Override
            public void safeOnClick(DialogInterface dialog, int id) {
                mStore.deleteGroup(group.guid);
                deleteGroup(group.guid);

                Intent intent = new Intent(FriendsPlugin.GROUP_REMOVED);
                intent.putExtra("guid", group.guid);
                mMainService.sendBroadcast(intent);
                L.d("deleteGroup succeeded");
                dialog.dismiss();
            }
        };
        UIUtils.showDialog(activity, title, message, positiveCaption, positiveClick, negativeCaption, null);
    }

    public void updateProfile(final String newProfileName, final byte[] newAvatar, final String accessToken,
                              final long birthdate, final long gender, final boolean hasBirthdate, final boolean hasGender) {
        T.dontCare();

        SafeRunnable handler = new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {

                IdentityStore identityStore = mMainService.getIdentityStore();
                MyIdentity tempIdentity = identityStore.getIdentity();
                String newName = newProfileName == null ? tempIdentity.getName() : newProfileName;

                IdentityTO identity = new IdentityTO();
                identity.avatarId = tempIdentity.getAvatarId();
                identity.email = tempIdentity.getEmail();
                identity.qualifiedIdentifier = tempIdentity.getQualifiedIdentifier();
                identity.name = newName;

                if (hasBirthdate) {
                    identity.hasBirthdate = hasBirthdate;
                    identity.birthdate = birthdate;
                } else {
                    identity.hasBirthdate = tempIdentity.hasBirthdate();
                    identity.birthdate = tempIdentity.getBirthdate() == null ? 0 : tempIdentity.getBirthdate();
                }

                if (hasGender) {
                    identity.hasGender = hasGender;
                    identity.gender = gender;
                } else {
                    identity.hasGender = tempIdentity.hasBirthdate();
                    identity.gender = tempIdentity.getGender();
                }

                identity.profileData = tempIdentity.getProfileData();

                identityStore.updateIdentity(identity, null, false);

                if (newAvatar != null)
                    identityStore.setAvatar(newAvatar);

                EditProfileRequestTO epr = new EditProfileRequestTO();
                epr.name = newName;
                epr.avatar = newAvatar == null ? null : Base64.encodeBytes(newAvatar);
                epr.access_token = accessToken;
                epr.birthdate = identity.birthdate;
                epr.gender = identity.gender;
                epr.has_birthdate = identity.hasBirthdate;
                epr.has_gender = identity.hasGender;

                try {
                    com.mobicage.api.system.Rpc.editProfile(new ResponseHandler<EditProfileResponseTO>(), epr);
                } catch (Exception e) {
                    L.bug("Failed to send EditProfileRequestTO", e);
                }
            }
        };

        if (T.getThreadType() == T.BIZZ) {
            handler.run();
        } else {
            mMainService.postOnBIZZHandler(handler);
        }
    }

    public void sendApiCall(final String serviceEmail, final String itemTagHash, final String method,
                            final String params, final String tag) {

        SafeRunnable handler = new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                T.BIZZ();
                long id = mStore.insertServiceApiCall(serviceEmail, itemTagHash, method, tag,
                        SERVICE_API_CALL_STATUS_SENT);

                SendApiCallRequestTO request = new SendApiCallRequestTO();
                request.id = id;
                request.method = method;
                request.params = params;
                request.service = serviceEmail;
                request.hashed_tag = itemTagHash;

                try {
                    com.mobicage.api.services.Rpc.sendApiCall(new ResponseHandler<SendApiCallResponseTO>(), request);
                } catch (Exception e) {
                    L.bug("Failed to send " + request, e);
                }
            }
        };

        if (T.getThreadType() == T.BIZZ) {
            handler.run();
        } else {
            mMainService.postOnBIZZHandler(handler);
        }
    }

    public void putUserData(final String serviceEmail, final String userDataJsonString, final boolean smart) {
        SafeRunnable handler = new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                T.BIZZ();
                final Map<String, Object> userData = userDataJsonString == null ? null :
                        (Map<String, Object>) JSONValue.parse(userDataJsonString);

                final UpdateUserDataRequestTO request = new UpdateUserDataRequestTO();
                request.service = serviceEmail;
                request.user_data = null;
                request.keys = new String[0];
                request.values = new String[0];
                if (smart) {
                    request.data = userDataJsonString;
                    request.type = FriendStore.FRIEND_DATA_TYPE_USER;
                    mStore.updateUserData(serviceEmail, FriendStore.FRIEND_DATA_TYPE_USER, userData);
                } else {
                    request.user_data = userDataJsonString;
                    if (userData != null) {
                        mStore.replaceUserData(serviceEmail, FriendStore.FRIEND_DATA_TYPE_USER, userData);
                    }
                }

                mDisabledBroadcastTypesCache.remove(serviceEmail);
                try {
                    com.mobicage.api.services.Rpc.updateUserData(new ResponseHandler<UpdateUserDataResponseTO>(),
                            request);
                } catch (Exception e) {
                    L.bug("Failed to send " + request, e);
                }
            }
        };

        if (T.getThreadType() == T.BIZZ) {
            handler.run();
        } else {
            mMainService.postOnBIZZHandler(handler);
        }
    }

    public boolean requestGroups() {
        T.dontCare();
        GetGroupsRequestTO request = new GetGroupsRequestTO();
        try {
            Rpc.getGroups(new GetGroupsResponseHandler(), request);
        } catch (Exception e) {
            L.bug("Error while sending get groups rpc request", e);
            return false;
        }
        return true;
    }

    public boolean requestGroupAvatar(String avatarHash) {
        T.dontCare();
        GetGroupAvatarRequestTO request = new GetGroupAvatarRequestTO();
        request.avatar_hash = avatarHash;
        request.size = 50 * mMainService.getScreenScale();
        try {
            Rpc.getGroupAvatar(new GetGroupAvatarResponseHandler(avatarHash), request);
        } catch (Exception e) {
            L.bug("Error while sending get group avatar rpc request", e);
            return false;
        }

        return true;
    }

    public boolean putGroup(Group group) {
        T.dontCare();
        PutGroupRequestTO request = new PutGroupRequestTO();
        request.guid = group.guid;
        request.name = group.name;
        if (group.avatar != null)
            request.avatar = Base64.encodeBytes(group.avatar, Base64.DONT_BREAK_LINES);
        else
            request.avatar = null;
        request.members = group.members.toArray(new String[group.members.size()]);
        try {
            Rpc.putGroup(new PutGroupResponseHandler(group.guid), request);
        } catch (Exception e) {
            L.bug("Error while sending put group rpc request", e);
            return false;
        }

        return true;
    }

    public boolean deleteGroup(String guid) {
        T.dontCare();
        DeleteGroupRequestTO request = new DeleteGroupRequestTO();
        request.guid = guid;
        try {
            Rpc.deleteGroup(new ResponseHandler<DeleteGroupResponseTO>(), request);
        } catch (Exception e) {
            L.bug("Error while sending delete group rpc request", e);
            return false;
        }

        return true;
    }

    public void setSecureInfo(final PublicKeyTO[] publicKeys) {
        T.dontCare();
        SetSecureInfoRequestTO request = new SetSecureInfoRequestTO();
        request.public_key = null;
        request.public_keys = publicKeys;
        try {
            com.mobicage.api.system.Rpc.setSecureInfo(new ResponseHandler<SetSecureInfoResponseTO>(), request);
        } catch (Exception e) {
            L.bug("Failed to send SetSecureInfoRequestTO", e);
        }
    }

    public Bitmap toFriendBitmap(byte[] bitmapBytes) {
        if (bitmapBytes == null) {
            return getMissingFriendAvatarBitmap();
        }
        final Bitmap bitmap = ImageHelper.getRoundedCornerAvatar(BitmapFactory.decodeByteArray(bitmapBytes, 0,
                bitmapBytes.length));
        return bitmap;
    }

    public Bitmap toGroupBitmap(byte[] bitmapBytes) {
        if (bitmapBytes == null) {
            return getMissingGroupAvatarBitmap();
        }
        final Bitmap bitmap = ImageHelper.getRoundedCornerAvatar(BitmapFactory.decodeByteArray(bitmapBytes, 0,
                bitmapBytes.length));
        return bitmap;
    }

    public Map<String, Object> getRogerthatUserAndServiceInfo(final String serviceEmail, final Friend serviceFriend) {
        return getRogerthatUserAndServiceInfo(serviceEmail, serviceFriend, null);
    }

    public Map<String, Object> getRogerthatUserAndServiceInfo(final String serviceEmail, final Friend serviceFriend,
                                                              final ServiceMenuItemInfo menuItem) {
        Map<String, Object> userData = mStore.getUserData(serviceEmail, FriendStore.FRIEND_DATA_TYPE_USER);
        Map<String, Object> appData = mStore.getUserData(serviceEmail, FriendStore.FRIEND_DATA_TYPE_APP);
        MyIdentity myIdentity = mMainService.getIdentityStore().getIdentity();

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("name", myIdentity.getDisplayName());

        final Locale locale = Locale.getDefault();
        String language = locale.getLanguage().toLowerCase(Locale.US);
        final String country = locale.getCountry();
        if (country != null) {
            language += "_" + country.toUpperCase(Locale.US);
        }
        userInfo.put("language", language);
        userInfo.put("avatarUrl", CloudConstants.CACHED_AVATAR_URL_PREFIX + myIdentity.getAvatarId());
        userInfo.put("account", myIdentity.getEmail());
        userInfo.put("data", userData);

        Map<String, Object> serviceInfo = new HashMap<>();
        if (serviceFriend != null) {
            serviceInfo.put("name", serviceFriend.getDisplayName());
            serviceInfo.put("email", serviceFriend.getDisplayEmail());
            serviceInfo.put("account", serviceFriend.email);
            serviceInfo.put("data", appData);
        }

        Map<String, Object> systemInfo = new HashMap<>();
        systemInfo.put("os", "android");
        systemInfo.put("version", SystemUtils.getAndroidVersion() + "");
        systemInfo.put("appVersion", MainService.getVersion(mMainService));
        systemInfo.put("appName", SystemUtils.getApplicationName(mMainService));
        systemInfo.put("appId", CloudConstants.APP_ID);

        Map<String, Object> info = new HashMap<>();
        info.put("user", userInfo);
        info.put("service", serviceInfo);
        info.put("system", systemInfo);
        info.put("menuItem", menuItem == null ? null : menuItem.toJSONMap());
        return info;
    }

    public void disableBroadcastType(final String serviceEmail, final String broadcastType) {
        disableBroadcastTypeInCache(serviceEmail, broadcastType);
        SafeRunnable handler = new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                T.BIZZ();
                String userDataJsonString = mStore.disableBroadcastType(serviceEmail, broadcastType);
                if (userDataJsonString != null) {
                    UpdateUserDataRequestTO request = new UpdateUserDataRequestTO();
                    request.service = serviceEmail;
                    request.user_data = userDataJsonString;
                    request.type = null;
                    request.keys = new String[0];
                    request.values = new String[0];
                    try {
                        com.mobicage.api.services.Rpc.updateUserData(new ResponseHandler<UpdateUserDataResponseTO>(),
                                request);
                    } catch (Exception e) {
                        L.bug("Failed to send " + request, e);
                    }
                }
            }
        };

        if (T.getThreadType() == T.BIZZ) {
            handler.run();
        } else {
            mMainService.postOnBIZZHandler(handler);
        }
    }

    public boolean isBroadcastTypeDisabled(final String serviceEmail, final String broadcastType) {
        if (!mDisabledBroadcastTypesCache.containsKey(serviceEmail)) {
            mDisabledBroadcastTypesCache.put(serviceEmail, mStore.getDisabledBroadcastTypes(serviceEmail));
        }
        return mDisabledBroadcastTypesCache.get(serviceEmail).contains(broadcastType);
    }

    private void disableBroadcastTypeInCache(final String serviceEmail, final String broadcastType) {
        if (!isBroadcastTypeDisabled(serviceEmail, broadcastType)) {
            mDisabledBroadcastTypesCache.get(serviceEmail).add(broadcastType);
        }
    }

    public void cleanupOldBrandings() {
        final long start = System.currentTimeMillis();

        File dir;
        try {
            dir = mBrandingMgr.getBrandingRootDirectory();
        } catch (Exception e) {
            L.d(e);
            return;
        }

        Set<String> brandings = mStore.listBrandingsInUse();

        final long lastWeek = start - (7L * 86400L * 1000L);
        long count = 0;
        long totalCount = 0;
        for (File file : dir.listFiles()) {
            String fileName = file.getName();
            if (!fileName.endsWith(".branding")) {
                continue;
            }
            totalCount += 1;
            if (file.lastModified() < lastWeek) {
                String brandingKey = fileName.substring(0, fileName.lastIndexOf("."));
                if (brandings.contains(brandingKey)) {
                    continue;
                }
                count += 1;
                if (!file.delete()) {
                    L.e("Failed to delete old branding file with name '" + fileName + "'");
                }
            }
        }

        final long elapsed = System.currentTimeMillis() - start;
        L.d("cleanupOldBrandings removed " + count + "/" + totalCount + " items in " + elapsed + " millis");
    }
}

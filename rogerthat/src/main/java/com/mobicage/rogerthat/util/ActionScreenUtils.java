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

package com.mobicage.rogerthat.util;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.view.inputmethod.InputMethodManager;

import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.MainService;
import com.mobicage.rogerthat.NavigationItem;
import com.mobicage.rogerthat.ServiceBoundActivity;
import com.mobicage.rogerthat.plugins.friends.FriendStore;
import com.mobicage.rogerthat.plugins.friends.FriendsPlugin;
import com.mobicage.rogerthat.plugins.friends.ServiceApiCallbackResult;
import com.mobicage.rogerthat.plugins.history.HistoryItem;
import com.mobicage.rogerthat.plugins.news.NewsItem;
import com.mobicage.rogerthat.plugins.news.NewsPlugin;
import com.mobicage.rogerthat.plugins.scan.GetUserInfoResponseHandler;
import com.mobicage.rogerthat.plugins.scan.ProcessScanActivity;
import com.mobicage.rogerthat.plugins.trackme.DiscoveredBeaconProximity;
import com.mobicage.rogerthat.plugins.trackme.TrackmePlugin;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.security.SecurityUtils;
import com.mobicage.rogerthat.util.system.SafeBroadcastReceiver;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.ui.TestUtils;
import com.mobicage.rogerthat.util.ui.UIUtils;
import com.mobicage.rpc.config.AppConstants;
import com.mobicage.rpc.config.CloudConstants;
import com.mobicage.rpc.newxmpp.XMPPKickChannel;
import com.mobicage.to.friends.GetUserInfoRequestTO;

import org.jivesoftware.smack.util.Base64;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.JSONValue;

import java.security.PublicKey;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActionScreenUtils {

    private ServiceBoundActivity mActivity;
    private MainService mMainService;
    private String mServiceEmail;
    private String mItemTagHash;
    private boolean mRunInBackground;
    private IntentCallback mCallback;

    private FriendsPlugin mFriendsPlugin;
    private NewsPlugin mNewsPlugin;
    private TrackmePlugin mTrackmePlugin;

    private MediaPlayer mSoundMediaPlayer = null;
    private HandlerThread mSoundThread = null;
    private Handler mSoundHandler = null;

    private int mLastBacklogStatus = 0;
    private boolean mIsStartedListening = false;

    public interface IntentCallback {
        boolean apiResult(ServiceApiCallbackResult result);
        void userDataUpdated(String userData);
        void serviceDataUpdated(String serviceData);
        void onBeaconInReach(Map<String, Object> beacon);
        void onBeaconOutOfReach(Map<String, Object> beacon);
        void qrCodeScanned(Map<String, Object> result);
        void onBackendConnectivityChanged(boolean connected);
        void newsReceived(long[] ids);
        void badgeUpdated(Map<String, Object> params);
    }

    private BroadcastReceiver mBroadcastReceiver = new SafeBroadcastReceiver() {
        @Override
        public String[] onSafeReceive(final Context context, final Intent intent) {
            if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                L.d("[BroadcastReceiver] Screen OFF");
                if (!mRunInBackground) {
                    mActivity.finish();
                }

            } else if (FriendsPlugin.SERVICE_API_CALL_ANSWERED_INTENT.equals(intent.getAction())) {
                if (mServiceEmail.equals(intent.getStringExtra("service"))
                        && mItemTagHash.equals(intent.getStringExtra("item"))) {

                    deliverAllApiResults();
                }

            } else if (FriendsPlugin.SERVICE_DATA_UPDATED.equals(intent.getAction())) {
                if (mServiceEmail.equals(intent.getStringExtra("email"))) {
                    if (intent.getBooleanExtra("user_data", false)) {
                        final Map<String, Object> userData = mFriendsPlugin.getStore().getUserData(mServiceEmail, FriendStore.FRIEND_DATA_TYPE_USER);
                        mCallback.userDataUpdated(JSONValue.toJSONString(userData));
                    }
                    if (intent.getBooleanExtra("service_data", false)) {
                        final Map<String, Object> appData = mFriendsPlugin.getStore().getUserData(mServiceEmail, FriendStore.FRIEND_DATA_TYPE_APP);
                        mCallback.serviceDataUpdated(JSONValue.toJSONString(appData));
                    }
                    return new String[] { FriendsPlugin.SERVICE_DATA_UPDATED };
                }

            } else if (FriendsPlugin.BEACON_IN_REACH.equals(intent.getAction())) {
                if (mServiceEmail.equals(intent.getStringExtra("email"))) {
                    final Map<String, Object> result = new HashMap<>();
                    result.put("uuid", intent.getStringExtra("uuid").toLowerCase());
                    result.put("major", "" + intent.getIntExtra("major", 0));
                    result.put("minor", "" + intent.getIntExtra("minor", 0));
                    result.put("tag", "" + intent.getStringExtra("tag"));
                    result.put("proximity", "" + intent.getIntExtra("proximity", 0));
                    mCallback.onBeaconInReach(result);
                }

            } else if (FriendsPlugin.BEACON_OUT_OF_REACH.equals(intent.getAction())) {
                if (mServiceEmail.equals(intent.getStringExtra("email"))) {
                    final Map<String, Object> result = new HashMap<String, Object>();
                    result.put("uuid", intent.getStringExtra("uuid").toLowerCase());
                    result.put("major", "" + intent.getIntExtra("major", 0));
                    result.put("minor", "" + intent.getIntExtra("minor", 0));
                    result.put("tag", "" + intent.getStringExtra("tag"));
                    result.put("proximity", "" + intent.getIntExtra("proximity", 0));
                    mCallback.onBeaconOutOfReach(result);
                }

            } else if (ProcessScanActivity.URL_REDIRECTION_DONE.equals(intent.getAction())) {

                final String rawUrl = intent.getStringExtra(ProcessScanActivity.RAWURL);
                final String emailHash = intent.getStringExtra(ProcessScanActivity.EMAILHASH);
                if (rawUrl != null) {
                    if (emailHash != null) {
                        if (intent.hasExtra(ProcessScanActivity.POKE_ACTION)) {
                            final Map<String, Object> result = new HashMap<String, Object>();
                            result.put("status", "resolved");
                            result.put("content", rawUrl);
                            mCallback.qrCodeScanned(result);

                        } else {
                            final GetUserInfoRequestTO request = new GetUserInfoRequestTO();
                            request.code = emailHash;
                            request.allow_cross_app = true;

                            final GetUserInfoResponseHandler handler = new GetUserInfoResponseHandler();
                            handler.setCode(emailHash);
                            handler.putStringExtra(ProcessScanActivity.RAWURL, rawUrl);
                            if (CloudConstants.isContentBrandingApp()) {
                                handler.setSendUserScanned(true);
                                handler.setServiceEmail(mServiceEmail);
                            }

                            try {
                                com.mobicage.api.friends.Rpc.getUserInfo(handler, request);
                            } catch (Exception e) {
                                // todo cordova finish(); ???
                                mMainService.putInHistoryLog(mActivity.getString(R.string.getuserinfo_failure), HistoryItem.ERROR);
                            }
                        }
                    } else {
                        Map<String, Object> result = new HashMap<String, Object>();
                        result.put("status", "resolved");
                        result.put("content", rawUrl);
                        mCallback.qrCodeScanned(result);
                    }
                }
                return new String[] { intent.getAction() };

            } else if (FriendsPlugin.FRIEND_INFO_RECEIVED_INTENT.equals(intent.getAction())) {
                final String rawUrl = intent.getStringExtra(ProcessScanActivity.RAWURL);
                if (rawUrl != null) {
                    final String emailHash = intent.getStringExtra(ProcessScanActivity.EMAILHASH);
                    if (emailHash != null) {
                        if (intent.getBooleanExtra(ProcessScanActivity.SUCCESS, true)) {
                            Map<String, Object> userDetails = new HashMap<String, Object>();
                            userDetails.put("email", intent.getStringExtra(ProcessScanActivity.EMAIL));
                            userDetails.put("name", intent.getStringExtra(ProcessScanActivity.NAME));
                            userDetails.put("appId", intent.getStringExtra(ProcessScanActivity.APP_ID));

                            Map<String, Object> result = new HashMap<String, Object>();
                            result.put("status", "resolved");
                            result.put("content", rawUrl);
                            result.put("userDetails", userDetails);
                            mCallback.qrCodeScanned(result);
                        } else {
                            Map<String, Object> result = new HashMap<String, Object>();
                            final String errorMessge = intent.getStringExtra(UIUtils.ERROR_MESSAGE);
                            result.put("status", "error");
                            result.put("content", errorMessge);
                            mCallback.qrCodeScanned(result);
                        }
                    } else {
                        Map<String, Object> result = new HashMap<String, Object>();
                        result.put("status", "resolved");
                        result.put("content", rawUrl);
                        mCallback.qrCodeScanned(result);
                    }
                }
                return new String[] { intent.getAction() };

            } else if (NewsPlugin.GET_NEWS_ITEMS_RECEIVED_INTENT.equals(intent.getAction())) {
                mCallback.newsReceived(intent.getLongArrayExtra("ids"));
                return new String[] { intent.getAction() };

            } else if (NewsPlugin.NEW_NEWS_ITEM_INTENT.equals(intent.getAction())) {
                mCallback.newsReceived(new long[] {intent.getLongExtra("id", -1)});
                return new String[] { intent.getAction() };

            } else if (MainService.UPDATE_BADGE_INTENT.equals(intent.getAction())) {
                String key = intent.getStringExtra("key");
                long count = intent.getLongExtra("count", 0);

                Map<String, Object> params = new HashMap<>();
                params.put("key", key);
                params.put("count", count);
                mCallback.badgeUpdated(params);
                return new String[] { intent.getAction() };
            }

            return null;
        }
    };

    private BroadcastReceiver mBroadcastReceiverBacklog = new SafeBroadcastReceiver() {
        @SuppressLint("DefaultLocale")
        @Override
        public String[] onSafeReceive(final Context context, final Intent intent) {
            if (XMPPKickChannel.INTENT_BACKLOG_CONNECTED.equals(intent.getAction())) {
                if (mLastBacklogStatus <= 0) {
                    mLastBacklogStatus = 1;
                    mCallback.onBackendConnectivityChanged(true);
                }
            } else if (XMPPKickChannel.INTENT_BACKLOG_DISCONNECTED.equals(intent.getAction())) {
                if (mLastBacklogStatus >= 0) {
                    mLastBacklogStatus = -1;
                    mCallback.onBackendConnectivityChanged(false);
                }
            } else {
                return null;
            }
            return new String[] { intent.getAction() };
        }
    };

    public ActionScreenUtils(ServiceBoundActivity activity, String serviceEmail, String itemTagHash, boolean runInBackground) {
        mActivity = activity;
        mMainService = activity.getMainService();
        mServiceEmail = serviceEmail;
        mItemTagHash = itemTagHash;
        mRunInBackground = runInBackground;
    }

    public void start(IntentCallback callback) {
        mFriendsPlugin = mMainService.getPlugin(FriendsPlugin.class);
        mNewsPlugin = mMainService.getPlugin(NewsPlugin.class);
        mCallback = callback;

        final IntentFilter intentFilter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        if (mServiceEmail != null) {
            intentFilter.addAction(FriendsPlugin.SERVICE_API_CALL_ANSWERED_INTENT);
            intentFilter.addAction(FriendsPlugin.SERVICE_DATA_UPDATED);
            intentFilter.addAction(FriendsPlugin.BEACON_IN_REACH);
            intentFilter.addAction(FriendsPlugin.BEACON_OUT_OF_REACH);
        }
        
        intentFilter.addAction(FriendsPlugin.FRIEND_INFO_RECEIVED_INTENT);
        intentFilter.addAction(ProcessScanActivity.URL_REDIRECTION_DONE);
        intentFilter.addAction(NewsPlugin.GET_NEWS_ITEMS_RECEIVED_INTENT);
        intentFilter.addAction(NewsPlugin.NEW_NEWS_ITEM_INTENT);
        intentFilter.addAction(MainService.UPDATE_BADGE_INTENT);

        mActivity.registerReceiver(mBroadcastReceiver, intentFilter);
        mIsStartedListening = true;
    }

    public void stop() {
        if (mIsStartedListening) {
            mActivity.unregisterReceiver(mBroadcastReceiver);
        }
    }

    public void startBacklogListener() {
        final IntentFilter intentFilter = new IntentFilter(XMPPKickChannel.INTENT_BACKLOG_CONNECTED);
        intentFilter.addAction(XMPPKickChannel.INTENT_BACKLOG_DISCONNECTED);
        mActivity.registerReceiver(mBroadcastReceiverBacklog, intentFilter);
    }

    public void stopBacklogListener() {
        mActivity.unregisterReceiver(mBroadcastReceiverBacklog);
    }

    public void logError(String serviceEmail, String itemLabel, long[] itemCoords, String error) {
        L.bug("ScreenBrandingException:\n- Exception logged by screenBranding of " + serviceEmail
                + "\n- Service menu item name: " + itemLabel + "\n- Service menu item coords: "
                + Arrays.toString(itemCoords) + "\n" + error);
    }

    public void deliverAllApiResults() {
        final FriendStore store = mFriendsPlugin.getStore();
        for (ServiceApiCallbackResult r : store.getServiceApiCallbackResultsByItem(mServiceEmail, mItemTagHash)) {
            if (mCallback.apiResult(r)) {
                store.removeServiceApiCall(r.id);
            }
        }
    }

    public List<DiscoveredBeaconProximity> getBeaconsInReach() {
        if (mTrackmePlugin == null) {
            mTrackmePlugin = mMainService.getPlugin(TrackmePlugin.class);
        }
        return mTrackmePlugin.getBeaconsInReach(mServiceEmail);
    }

    public void hideKeyboard(final IBinder windowToken)  {
        InputMethodManager inputManager = (InputMethodManager) mMainService.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(windowToken, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public String openActivity(final String actionType, final String action, final String title,
                               final String service, final boolean collapse) {

        Map<String, Object> params = new HashMap<>();
        params.put("collapse", collapse);

        NavigationItem ni = new NavigationItem(FontAwesome.Icon.faw_question_circle_o, actionType,
                action, title, service, 0, params);

        String errorMessage = ActivityUtils.canOpenNavigationItem(mActivity, ni);
        if (errorMessage != null) {
            return errorMessage;
        }
        Bundle extras = new Bundle();
        ActivityUtils.goToActivity(mActivity, ni, false, extras);
        return null;
    }

    public void playAudio(final String fileOnDisk) throws JSONException {
        if (mSoundHandler == null) {
            mSoundThread = new HandlerThread("rogerthat_actionscreenactivity_sound");
            mSoundThread.start();
            Looper looper = mSoundThread.getLooper();
            mSoundHandler = new Handler(looper);
        }
        mSoundHandler.post(new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                if (mSoundMediaPlayer != null) {
                    mSoundMediaPlayer.release();
                }
                mSoundMediaPlayer = new MediaPlayer();
                try {
                    mSoundMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mSoundMediaPlayer.setDataSource(fileOnDisk);
                    mSoundMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            mSoundMediaPlayer.start();
                        }
                    });
                    mSoundMediaPlayer.prepare();
                } catch (Exception e) {
                    L.i(e);
                }
            }
        });
    }

    public long countNews(final JSONObject params) {
        final String service = TextUtils.optString(params, "service", null);
        final String feedName = TextUtils.optString(params, "feed_name", "");
        return mNewsPlugin.getStore().countAllNewsItems(service, feedName);
    }

    public NewsItem getNews(final JSONObject params) {
        final long id = params.optLong("news_id", 0);
        return mNewsPlugin.getNewsItem(id);
    }

    public Map<String, Object> listNews(final JSONObject params) {
        final String service = TextUtils.optString(params, "service", null);
        final String cursor =  TextUtils.optString(params, "cursor", null);
        final String feedName =  TextUtils.optString(params, "feed_name", "");
        final long limit = params.optLong("limit", 10);
        return mNewsPlugin.listNewsItems(service, feedName, cursor, limit);
    }

    private void setupPin(final MainService.SecurityCallback sc) {
        mMainService.setupPin(sc);
    }

    public void createKeyPair(final JSONObject params, final MainService.SecurityCallback callback) {
        if (!AppConstants.Security.ENABLED) {
            String errorMessage = mActivity.getString(R.string.security_not_enabled);
            callback.onError("security_not_enabled", errorMessage);
            return;
        }

        final String keyAlgorithm = TextUtils.optString(params, "key_algorithm", null);
        final String keyName = TextUtils.optString(params, "key_name", null);
        final String message = TextUtils.optString(params, "message", null);
        final boolean forceCreate = params.optBoolean("force", false);
        final String seed = TextUtils.optString(params, "seed", null);

        if (!SecurityUtils.createKeyAlgorithmSupported(keyAlgorithm)) {
            String errorMessage = mActivity.getString(R.string.algorithm_not_supported);
            callback.onError("algorithm_not_supported", errorMessage);
            return;
        }

        if (SecurityUtils.hasKey(mMainService, "public", keyAlgorithm, keyName, null) && !forceCreate) {
            String errorMessage = mActivity.getString(R.string.key_already_exists);
            callback.onError("key_already_exists", errorMessage);
            return;
        }

        MainService.SecurityCallback sc = new MainService.SecurityCallback() {
            @Override
            public void onSuccess(Object result) {
                mMainService.createKeyPair(keyAlgorithm, keyName, message, seed, callback);
            }

            @Override
            public void onError(String code, String errorMessage) {
                callback.onError(code, errorMessage);
            }
        };

        if (!SecurityUtils.isPinSet(mMainService)) {
            setupPin(sc);
            return;
        }

        sc.onSuccess(null);
    }

    public void hasKeyPair(final JSONObject params, final MainService.SecurityCallback callback) throws JSONException {
        if (!AppConstants.Security.ENABLED) {
            String errorMessage = mActivity.getString(R.string.security_not_enabled);
            callback.onError("security_not_enabled", errorMessage);
            return;
        }

        final String keyAlgorithm = TextUtils.optString(params, "key_algorithm", null);
        final String keyName = TextUtils.optString(params, "key_name", null);
        final Long keyIndex = TextUtils.optLong(params, "key_index");
        callback.onSuccess(SecurityUtils.hasKey(mMainService, "public", keyAlgorithm, keyName, keyIndex));
    }

    public void getPublicKey(final JSONObject params, final MainService.SecurityCallback callback) throws JSONException {
        if (!AppConstants.Security.ENABLED) {
            String errorMessage = mActivity.getString(R.string.security_not_enabled);
            callback.onError("security_not_enabled", errorMessage);
            return;
        }

        final String keyAlgorithm = TextUtils.optString(params, "key_algorithm", null);
        final String keyName = TextUtils.optString(params, "key_name", null);
        final Long keyIndex = TextUtils.optLong(params, "key_index");

        if (!SecurityUtils.hasKey(mMainService, "public", keyAlgorithm, keyName, keyIndex)) {
            callback.onSuccess(null);
            return;
        }
        PublicKey publicKey = null;
        try {
            publicKey = SecurityUtils.getPublicKey(mMainService, keyAlgorithm, keyName, keyIndex);
        } catch (Exception e) {
            L.d("getPublicKey failed", e);
        }
        if (publicKey == null) {
            callback.onSuccess(null);
            return;
        }
        callback.onSuccess(Base64.encodeBytes(publicKey.getEncoded(), Base64.DONT_BREAK_LINES));
    }

    public void getSeed(final JSONObject params, final MainService.SecurityCallback callback) {
        if (!AppConstants.Security.ENABLED) {
            String errorMessage = mActivity.getString(R.string.security_not_enabled);
            callback.onError("security_not_enabled", errorMessage);
            return;
        }

        final String keyAlgorithm = TextUtils.optString(params, "key_algorithm", null);
        final String keyName = TextUtils.optString(params, "key_name", null);
        final String message = TextUtils.optString(params, "message", null);

        if (!SecurityUtils.hasKey(mMainService, "seed", keyAlgorithm, keyName, null)) {
            String errorMessage = mActivity.getString(R.string.key_not_found);
            callback.onError("key_not_found", errorMessage);
            return;
        }

        final String errorMessage = mActivity.getString(R.string.permission_denied_to_load_seed, mActivity.getString(R.string.settings), mActivity.getString
                (R.string.security), keyName);
        callback.onError("permission_denied", errorMessage);
        //mMainService.getSeed(keyAlgorithm, keyName, message, callback); // blocked for security reasons
    }

    public void listAddresses(final JSONObject params, final MainService.SecurityCallback callback) {
        if (!AppConstants.Security.ENABLED) {
            String errorMessage = mActivity.getString(R.string.security_not_enabled);
            callback.onError("security_not_enabled", errorMessage);
            return;
        }

        final String keyAlgorithm = TextUtils.optString(params, "key_algorithm", null);
        final String keyName = TextUtils.optString(params, "key_name", null);

        try {
            callback.onSuccess(SecurityUtils.listAddress(mMainService, keyAlgorithm, keyName));
        } catch (Exception e) {
            L.d("SecurityUtils.listAddress failed", e);
            String errorMessage = mActivity.getString(R.string.unknown_error_occurred);
            callback.onError("unknown_error_occurred", errorMessage);
        }
    }

    public void getAddress(final JSONObject params, final MainService.SecurityCallback callback) {
        if (!AppConstants.Security.ENABLED) {
            String errorMessage = mActivity.getString(R.string.security_not_enabled);
            callback.onError("security_not_enabled", errorMessage);
            return;
        }

        final String keyAlgorithm = TextUtils.optString(params, "key_algorithm", null);
        final String keyName = TextUtils.optString(params, "key_name", null);
        final long keyIndex = params.optLong("key_index", 0);
        final String message = TextUtils.optString(params, "message", null);

        if (SecurityUtils.hasKey(mMainService, "address", keyAlgorithm, keyName, keyIndex)) {
            try {
                callback.onSuccess(SecurityUtils.getAddress(mMainService, keyAlgorithm, keyName, keyIndex));
            } catch (Exception e) {
                L.d("SecurityUtils.getAddress failed", e);
                String errorMessage = mActivity.getString(R.string.unknown_error_occurred);
                callback.onError("unknown_error_occurred", errorMessage);
                return;
            }
            return;
        }

        if (!SecurityUtils.hasKey(mMainService, "seed", keyAlgorithm, keyName, null)) {
            String errorMessage = mActivity.getString(R.string.key_not_found);
            callback.onError("key_not_found", errorMessage);
            return;
        }

        mMainService.getAddress(keyAlgorithm, keyName, keyIndex, message, callback);
    }

    public void signPayload(final JSONObject params, final String payload, final MainService.SecurityCallback callback) throws JSONException {
        if (!AppConstants.Security.ENABLED) {
            String errorMessage = mActivity.getString(R.string.security_not_enabled);
            callback.onError("security_not_enabled", errorMessage);
            return;
        }

        final String keyAlgorithm = TextUtils.optString(params, "key_algorithm", null);
        final String keyName = TextUtils.optString(params, "key_name", null);
        final Long keyIndex = TextUtils.optLong(params, "key_index");
        final String message = TextUtils.optString(params, "message", null);
        final boolean forcePin = params.optBoolean("force_pin", false);
        final boolean hashPayload = params.optBoolean("hash_payload", true);

        if (!SecurityUtils.hasKey(mMainService, "public", keyAlgorithm, keyName, keyIndex)) {
            String errorMessage = mActivity.getString(R.string.key_not_found);
            callback.onError("key_not_found", errorMessage);
            return;
        }

        byte[] payloadData = null;
        try {
            if (hashPayload) {
                payloadData = SecurityUtils.getPayload(keyAlgorithm, Base64.decode(payload));
            } else {
                payloadData = Base64.decode(payload);
            }
        } catch (Exception e) {
            L.d("Failed to get payload data", e);
        }
        if (payloadData == null) {
            String errorMessage = mActivity.getString(R.string.unknown_error_occurred);
            callback.onError("unknown_error_occurred", errorMessage);
            return;
        }
        mMainService.sign(keyAlgorithm, keyName, keyIndex, message, payloadData, forcePin, callback);
    }

    public void verifySignedPayload(final JSONObject params, final MainService.SecurityCallback callback) throws JSONException {
        if (!AppConstants.Security.ENABLED) {
            String errorMessage = mActivity.getString(R.string.security_not_enabled);
            callback.onError("security_not_enabled", errorMessage);
            return;
        }

        final String keyAlgorithm = TextUtils.optString(params, "key_algorithm", null);
        final String keyName = TextUtils.optString(params, "key_name", null);
        final Long keyIndex = TextUtils.optLong(params, "key_index");
        final String payload = TextUtils.optString(params, "payload", null);
        final String payloadSignature = TextUtils.optString(params, "payload_signature", null);

        if (!SecurityUtils.hasKey(mMainService, "public", keyAlgorithm, keyName, keyIndex)) {
            String errorMessage = mActivity.getString(R.string.key_not_found);
            callback.onError("key_not_found", errorMessage);
            return;
        }

        byte[] payloadData = null;
        try {
            payloadData = SecurityUtils.getPayload(keyAlgorithm, Base64.decode(payload));
        } catch (Exception e) {
            L.d("SecurityUtils.getPayload failed", e);
        }
        if (payloadData == null) {
            String errorMessage = mActivity.getString(R.string.unknown_error_occurred);
            callback.onError("unknown_error_occurred", errorMessage);
            return;
        }

        final byte[] payloadDataSignature = Base64.decode(payloadSignature);
        boolean valid = mMainService.validate(keyAlgorithm, keyName, keyIndex, payloadData, payloadDataSignature);
        callback.onSuccess(valid);
    }
}

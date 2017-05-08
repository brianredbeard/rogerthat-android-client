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
import com.mobicage.rogerthat.plugins.scan.GetUserInfoResponseHandler;
import com.mobicage.rogerthat.plugins.scan.ProcessScanActivity;
import com.mobicage.rogerthat.plugins.trackme.DiscoveredBeaconProximity;
import com.mobicage.rogerthat.plugins.trackme.TrackmePlugin;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeBroadcastReceiver;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.ui.UIUtils;
import com.mobicage.rpc.config.CloudConstants;
import com.mobicage.rpc.newxmpp.XMPPKickChannel;
import com.mobicage.to.friends.GetUserInfoRequestTO;

import org.json.JSONException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActionScreenUtils {

    private ServiceBoundActivity mContext;
    private MainService mMainService;
    private String mServiceEmail;
    private String mItemTagHash;
    private boolean mRunInBackground;
    private IntentCallback mCallback;

    private FriendsPlugin mFriendsPlugin;
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
    }

    private BroadcastReceiver mBroadcastReceiver = new SafeBroadcastReceiver() {
        @Override
        public String[] onSafeReceive(final Context context, final Intent intent) {
            if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                L.d("[BroadcastReceiver] Screen OFF");
                if (!mRunInBackground) {
                    mContext.finish();
                }

            } else if (FriendsPlugin.SERVICE_API_CALL_ANSWERED_INTENT.equals(intent.getAction())) {
                if (mServiceEmail.equals(intent.getStringExtra("service"))
                        && mItemTagHash.equals(intent.getStringExtra("item"))) {

                    deliverAllApiResults();
                }

            } else if (FriendsPlugin.SERVICE_DATA_UPDATED.equals(intent.getAction())) {
                if (mServiceEmail.equals(intent.getStringExtra("email"))) {
                    final String[] data = mFriendsPlugin.getStore().getServiceData(mServiceEmail);
                    if (intent.getBooleanExtra("user_data", false)) {
                        mCallback.userDataUpdated(data[0]);
                    }
                    if (intent.getBooleanExtra("service_data", false)) {
                        mCallback.serviceDataUpdated(data[1]);
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
                                mMainService.putInHistoryLog(mContext.getString(R.string.getuserinfo_failure), HistoryItem.ERROR);
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

    public ActionScreenUtils(ServiceBoundActivity context, String serviceEmail, String itemTagHash, boolean runInBackground) {
        mContext = context;
        mMainService = context.getMainService();
        mServiceEmail = serviceEmail;
        mItemTagHash = itemTagHash;
        mRunInBackground = runInBackground;
    }

    public void start(IntentCallback callback) {
        mFriendsPlugin = mMainService.getPlugin(FriendsPlugin.class);
        mCallback = callback;

        final IntentFilter intentFilter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        intentFilter.addAction(FriendsPlugin.SERVICE_API_CALL_ANSWERED_INTENT);
        intentFilter.addAction(FriendsPlugin.SERVICE_DATA_UPDATED);
        intentFilter.addAction(FriendsPlugin.BEACON_IN_REACH);
        intentFilter.addAction(FriendsPlugin.BEACON_OUT_OF_REACH);
        intentFilter.addAction(FriendsPlugin.FRIEND_INFO_RECEIVED_INTENT);
        intentFilter.addAction(ProcessScanActivity.URL_REDIRECTION_DONE);

        mContext.registerReceiver(mBroadcastReceiver, intentFilter);
        mIsStartedListening = true;
    }

    public void stop() {
        if (mIsStartedListening) {
            mContext.unregisterReceiver(mBroadcastReceiver);
        }
    }

    public void startBacklogListener() {
        final IntentFilter intentFilter = new IntentFilter(XMPPKickChannel.INTENT_BACKLOG_CONNECTED);
        intentFilter.addAction(XMPPKickChannel.INTENT_BACKLOG_DISCONNECTED);
        mContext.registerReceiver(mBroadcastReceiverBacklog, intentFilter);
    }

    public void stopBacklogListener() {
        mContext.unregisterReceiver(mBroadcastReceiverBacklog);
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

    public String openActivity(final String actionType, final String action, final String title) {
        NavigationItem ni = new NavigationItem(FontAwesome.Icon.faw_question_circle_o, actionType, action, title, false);

        String errorMessage = ActivityUtils.canOpenNavigationItem(mContext, ni);
        if (errorMessage != null) {
            return errorMessage;
        }
        Bundle extras = new Bundle();
        ActivityUtils.goToActivity(mContext, ni, false, extras);
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
}

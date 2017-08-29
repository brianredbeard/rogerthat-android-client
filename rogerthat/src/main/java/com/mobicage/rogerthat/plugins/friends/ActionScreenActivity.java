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
package com.mobicage.rogerthat.plugins.friends;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginResult;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.github.barteksc.pdfviewer.PDFView;
import com.google.zxing.client.android.CaptureActivity;
import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.MainService;
import com.mobicage.rogerthat.ServiceBoundActivity;
import com.mobicage.rogerthat.plugins.messaging.AttachmentViewerActivity;
import com.mobicage.rogerthat.plugins.messaging.BrandingFailureException;
import com.mobicage.rogerthat.plugins.messaging.BrandingMgr;
import com.mobicage.rogerthat.plugins.messaging.BrandingMgr.BrandingResult;
import com.mobicage.rogerthat.plugins.messaging.Message;
import com.mobicage.rogerthat.plugins.messaging.MessagingPlugin;
import com.mobicage.rogerthat.plugins.scan.ScanCommunication;
import com.mobicage.rogerthat.plugins.scan.ScanTabActivity;
import com.mobicage.rogerthat.util.ActionScreenUtils;
import com.mobicage.rogerthat.util.FacebookUtils;
import com.mobicage.rogerthat.util.FacebookUtils.PermissionType;
import com.mobicage.rogerthat.util.TextUtils;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeBroadcastReceiver;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.system.SystemUtils;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.system.TaggedWakeLock;
import com.mobicage.rogerthat.util.ui.UIUtils;
import com.mobicage.rpc.config.CloudConstants;

import org.jivesoftware.smack.util.Base64;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.json.simple.JSONValue;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class ActionScreenActivity extends ServiceBoundActivity {

    public static final String BRANDING_KEY = "branding_key";
    public static final String SERVICE_EMAIL = "service_email";
    public static final String ITEM_TAG_HASH = "item_tag_hash";
    public static final String ITEM_LABEL = "item_label";
    public static final String ITEM_COORDS = "item_coords";
    public static final String CONTEXT_MATCH = "context_match";
    public static final String RUN_IN_BACKGROUND = "run_in_background";
    public static final String CONTEXT = "context";

    private static final String POKE = "poke://";

    private static final String FACEBOOK_TYPE_CANCEL = "CANCEL";
    private static final String FACEBOOK_TYPE_ERROR = "ERROR";
    private static final Map<String, Object> FACEBOOK_MAP_CANCEL = new HashMap<String, Object>();

    private final int PERMISSION_REQUEST_CAMERA = 1;

    private boolean mIsHtmlContent = true;
    private boolean mInfoSet = false;
    private boolean mApiResultHandlerSet = false;
    private boolean mJavascriptBackBtnListener = false;
    private String mCurrentBackPressedId = null;
    private long mLasttimeBackPressed = 0;

    private WebView mBranding;
    private String mBrandingKey;
    private String mContextMatch = "";
    private String mServiceEmail;
    private String mItemTagHash;
    private String mItemLabel;
    private long[] mItemCoords;
    private Friend mServiceFriend;
    private boolean mRunInBackground;

    private volatile BrandingResult mBrandingResult = null;
    private TaggedWakeLock mWakeLock = null;
    private boolean mWakelockEnabled = false;

    private MessagingPlugin mMessagingPlugin;
    private FriendsPlugin mFriendsPlugin;
    private Poker<ActionScreenActivity> mPoker;

    private QRCodeScanner mQRCodeScanner = null;
    private boolean mQRCodeScannerOpen = false;
    private ScanCommunication mScanCommunication = null;

    private boolean mIsListeningBacklogConnectivityChanged = false;

    private ActionScreenUtils mActionScreenUtils;

    static {
        FACEBOOK_MAP_CANCEL.put("type", FACEBOOK_TYPE_CANCEL);
    }

    private class JSInterface extends Object {

        private final ActionScreenActivity mContext;

        private JSInterface(ActionScreenActivity context) {
            this.mContext = context;
        }

        @JavascriptInterface
        @SuppressWarnings("unused")
        public void hideKeyboard() {
            mActionScreenUtils.hideKeyboard(mContext.getCurrentFocus().getWindowToken());
        }

        @JavascriptInterface
        @SuppressWarnings("unused")
        public void sendApiCall(final String method, final String params, final String tag) {
            mFriendsPlugin.sendApiCall(mServiceEmail, mItemTagHash, method, params, tag);
        }

        @JavascriptInterface
        @SuppressWarnings("unused")
        public int version() {
            return 1;
        }

        @JavascriptInterface
        @SuppressWarnings("unused")
        public void invoke(final String action, final String parameters) throws JSONException {
            JSONObject params = parameters == null ? null : (JSONObject) new JSONTokener(parameters).nextValue();
            L.i("Received action '" + action + "' with params " + params);

            // FACEBOOK
            if ("facebook/login".equals(action)) {
                if (params == null) {
                    L.w("Expected params != null");
                    return;
                }
                facebookLogin(params.getString("id"), params.getString("properties"));
            } else if ("facebook/post".equals(action)) {
                if (params == null) {
                    L.w("Expected params != null");
                    return;
                }
                facebookPost(params.getString("id"), params.getString("postParams"));
            } else if ("facebook/ticker".equals(action)) {
                if (params == null) {
                    L.w("Expected params != null");
                    return;
                }
                facebookTicker(params.getString("id"), params.getString("type"), params.getString("postParams"));
            }
            // BACK
            else if ("back/registerListener".equals(action)) {
                mJavascriptBackBtnListener = true;
            } else if ("back/unregisterListener".equals(action)) {
                mJavascriptBackBtnListener = false;
            } else if ("back/backPressedCallback".equals(action)) {
                backPressedCallback(params);
            }
            // LOG
            else if ("log/".equals(action)) {
                logError(params);
            }
            // API
            else if ("api/resultHandlerConfigured".equals(action)) {
                mApiResultHandlerSet = true;
                mActionScreenUtils.deliverAllApiResults();
            }
            // USER
            else if ("user/put".equals(action)) {
                putUserData(params);
            }
            // SERVICE
            else if ("service/getBeaconsInReach".equals(action)) {
                getBeaconsInReach(params);
            }
            // SYSTEM
            else if ("system/onBackendConnectivityChanged".equals(action)) {
                onBackendConnectivityChanged(params);
            }
            // UTIL
            else if ("util/isConnectedToInternet".equals(action)) {
                isConnectedToInternet(params);
            } else if ("util/playAudio".equals(action)) {
                playAudio(params);
            } else if ("util/open".equals(action)) {
                openActivity(params);
            }
            // MESSAGING
            else if ("message/open".equals(action)) {
                openMessage(params);
            }
            // CAMERA
            else if ("camera/startScanningQrCode".equals(action)) {
                startScanningQrCode(params);
            } else if ("camera/stopScanningQrCode".equals(action)) {
                stopScanningQrCode(params);
            }
            // SECURITY
            else if ("security/createKeyPair".equals(action)) {
                createKeyPair(params);
            } else if ("security/hasKeyPair".equals(action)) {
                hasKeyPair(params);
            } else if ("security/getPublicKey".equals(action)) {
                getPublicKey(params);
            } else if ("security/getSeed".equals(action)) {
                getSeed(params);
            } else if ("security/getAddress".equals(action)) {
                getAddress(params);
            } else if ("security/sign".equals(action)) {
                signPayload(params);
            } else if ("security/verify".equals(action)) {
                verifySignedPayload(params);
            } else {
                L.d("Invoke did not handle any function");
            }
        }

        private void putUserData(final JSONObject params) throws JSONException {
            if (params == null) {
                L.w("Expected params != null");
                return;
            }
            mFriendsPlugin.putUserData(mServiceEmail, params.getString("u"));
        }

        private void getBeaconsInReach(final JSONObject params) throws JSONException {
            if (params == null) {
                L.w("Expected params != null");
                return;
            }
            String requestId = params.getString("id");
            Map<String, Object> result = new HashMap<String, Object>();
            result.put("beacons", mActionScreenUtils.getBeaconsInReach());
            deliverResult(requestId, result, null);
        }

        private void onBackendConnectivityChanged(final JSONObject params) throws JSONException {
            if (params == null) {
                L.w("Expected params != null");
                return;
            }
            String requestId = params.getString("id");
            Map<String, Object> result = new HashMap<String, Object>();
            result.put("connected", mService.isBacklogConnected());
            deliverResult(requestId, result, null);

            if (!mIsListeningBacklogConnectivityChanged) {
                mActionScreenUtils.startBacklogListener();
                mIsListeningBacklogConnectivityChanged = true;
            }
        }

        private void isConnectedToInternet(final JSONObject params) throws JSONException {
            if (params == null) {
                L.w("Expected params != null");
                return;
            }
            String requestId = params.getString("id");
            Map<String, Object> result = new HashMap<String, Object>();
            boolean wifiConnected = mService.getNetworkConnectivityManager().isWifiConnected();
            result.put("connectedToWifi", wifiConnected);
            result.put("connected", wifiConnected
                    || mService.getNetworkConnectivityManager().isMobileDataConnected());
            deliverResult(requestId, result, null);
        }

        private void logError(final JSONObject params) throws JSONException {
            if (params == null) {
                L.w("Expected params != null");
                return;
            }
            final String e = params.getString("e");
            if (e != null) {
                mActionScreenUtils.logError(mServiceEmail, mItemLabel, mItemCoords, e);
            }
        }

        private void backPressedCallback(final JSONObject params) throws JSONException {
            if (params == null) {
                L.w("Expected params != null");
                return;
            }
            if (mCurrentBackPressedId != null && mCurrentBackPressedId.equals(params.getString("requestId"))) {

                mCurrentBackPressedId = null;
                final boolean handled = "true".equalsIgnoreCase(params.getString("handled"));
                L.d("Javascript " + (handled ? "handled" : "did not handle") + " backPressed");
                if (!handled && !isFinishing()) {
                    if (T.getThreadType() == T.UI) {
                        finish();
                    } else {
                        mService.postAtFrontOfUIHandler(new SafeRunnable() {
                            @Override
                            protected void safeRun() throws Exception {
                                finish();
                            }
                        });
                    }
                }
            }
        }

        private void startScanningQrCode(final JSONObject params) throws JSONException {
            if (params == null) {
                L.w("Expected params != null");
                return;
            }
            final String requestId = params.getString("id");
            String cameraType = params.getString("camera_type");

            if (!QRCodeScanner.CAMERA_TYPES.contains(cameraType)) {
                Map<String, Object> e = new HashMap<>();
                e.put("code", "unsupported_camera_type");
                String errorMessage = getString(R.string.unsupported_camera_type);
                e.put("message", errorMessage);
                e.put("exception", errorMessage); // deprecated
                deliverResult(requestId, null, e);
                return;
            }

            int cameraPermission = ContextCompat.checkSelfPermission(ActionScreenActivity.this, Manifest.permission.CAMERA);
            if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
                final SafeRunnable continueRunnable = new SafeRunnable() {
                    @Override
                    protected void safeRun() throws Exception {
                        startScanningQrCode(params);
                    }
                };
                final SafeRunnable cancelRunnable = new SafeRunnable() {
                    @Override
                    protected void safeRun() throws Exception {
                        Map<String, Object> e = new HashMap<>();
                        e.put("code", "camera_permission_was_not_granted");
                        String errorMessage = getString(R.string.camera_permission_was_not_granted);
                        e.put("message", errorMessage);
                        e.put("exception", errorMessage); // deprecated
                        deliverResult(requestId, null, e);
                    }
                };
                if (askPermissionIfNeeded(Manifest.permission.CAMERA, PERMISSION_REQUEST_CAMERA,
                        continueRunnable, cancelRunnable))
                    return;
            }

            if (mQRCodeScanner == null) {
                if (mQRCodeScannerOpen) {
                    Map<String, Object> e = new HashMap<>();
                    e.put("code", "camera_was_already_open");
                    String errorMessage = getString(R.string.camera_was_already_open);
                    e.put("message", errorMessage);
                    e.put("exception", errorMessage); // deprecated
                    deliverResult(requestId, null, e);
                    return;
                }
                mQRCodeScannerOpen = true;
                SystemUtils.showZXingActivity(ActionScreenActivity.this, ScanTabActivity.MARKET_INSTALL_RESULT,
                        ScanTabActivity.ZXING_SCAN_RESULT);

                deliverResult(requestId, null, null);
                return;
            }

            if (mQRCodeScanner.cameraManager.isOpen()) {
                Map<String, Object> e = new HashMap<>();
                e.put("code", "camera_was_already_open");
                String errorMessage = getString(R.string.camera_was_already_open);
                e.put("message", errorMessage);
                e.put("exception", errorMessage); // deprecated
                deliverResult(requestId, null, e);
                return;
            }

            if (Camera.getNumberOfCameras() == 1) {
                mQRCodeScanner.currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
            } else {
                if (mQRCodeScanner.CAMERA_TYPE_BACK.equals(cameraType)) {
                    mQRCodeScanner.currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
                } else {
                    mQRCodeScanner.currentCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
                }
            }

            if (mQRCodeScanner.hasSurface == true
                    && (mQRCodeScanner.surfaceHolder != null || mQRCodeScanner.surfaceTexture != null)) {
                if (mBranding.getVisibility() == View.VISIBLE) {
                    if (T.getThreadType() == T.UI) {
                        mQRCodeScanner.startScanningForQRCodes();
                    } else {
                        mService.postAtFrontOfUIHandler(new SafeRunnable() {
                            @Override
                            protected void safeRun() throws Exception {
                                mQRCodeScanner.startScanningForQRCodes();
                            }
                        });
                    }
                }
            }
            deliverResult(requestId, null, null);
        }

        private void stopScanningQrCode(final JSONObject params) throws JSONException {
            if (params == null) {
                L.w("Expected params != null");
                return;
            }
            String requestId = params.getString("id");
            String cameraType = params.getString("camera_type");

            if (!QRCodeScanner.CAMERA_TYPES.contains(cameraType)) {
                Map<String, Object> e = new HashMap<>();
                e.put("code", "unsupported_camera_type");
                String errorMessage = getString(R.string.unsupported_camera_type);
                e.put("message", errorMessage);
                e.put("exception", errorMessage); // deprecated
                deliverResult(requestId, null, e);
                return;
            }

            if (mQRCodeScanner == null) {
                mQRCodeScannerOpen = false;
                Intent intent = new Intent(CaptureActivity.FINISH_INTENT);
                sendBroadcast(intent);
            } else {
                mQRCodeScanner.stopScanningForQRCodes();
            }

            deliverResult(requestId, null, null);
        }

        private void playAudio(final JSONObject params) throws JSONException {
            if (params == null) {
                L.w("Expected params != null");
                return;
            }
            String requestId = params.getString("id");
            final String url = params.getString("url");
            String fileOnDisk = "file://" + mBrandingResult.dir.getAbsolutePath() + "/" + url;
            mActionScreenUtils.playAudio(fileOnDisk);
            Map<String, Object> result = new HashMap<>();
            deliverResult(requestId, result, null);
        }

        private void openActivity(final JSONObject params) throws JSONException {
            if (params == null) {
                L.w("Expected params != null");
                return;
            }
            final String requestId = params.getString("id");
            final String actionType = params.has("action_type") ? params.getString("action_type") : null;
            final String action = params.has("action") ? params.getString("action") : null;
            final String title = params.has("title") ? params.getString("title") : null;

            String errorMessage = mActionScreenUtils.openActivity(actionType, action, title);
            Map<String, Object> e = null;
            if (errorMessage != null) {
                e = new HashMap<>();
                e.put("code", "unknown_error_occurred");
                e.put("message", errorMessage);
                e.put("exception", errorMessage); // deprecated
                return;
            }
            deliverResult(requestId, null, e);
        }

        private void openMessage(final JSONObject params) throws JSONException {
            if (params == null) {
                L.w("Expected params != null");
                return;
            }
            final String requestId = params.getString("id");
            final String messageKey = params.getString("message_key");

            final Message message = mMessagingPlugin.getStore().getMessageByKey(messageKey, true);
            Map<String, Object> error = null;
            if (message == null) {
                error = new HashMap<>();
                error.put("type", "MessageNotFound");
            } else {
                mMessagingPlugin.showMessage(ActionScreenActivity.this, message, false, null, false);
            }

            deliverResult(requestId, null, error);
        }

        private void createKeyPair(final JSONObject params) throws JSONException {
            if (params == null) {
                L.w("Expected params != null");
                return;
            }
            final String requestId = params.getString("id");

            MainService.SecurityCallback sc = new MainService.SecurityCallback() {
                @Override
                public void onSuccess(Object result) {
                    Map<String, Object> r = (Map<String, Object>) result;
                    deliverResult(requestId, r, null);
                }

                @Override
                public void onError(String code, String errorMessage) {
                    Map<String, Object> e = new HashMap<>();
                    e.put("code", code);
                    e.put("message", errorMessage);
                    e.put("exception", errorMessage); // deprecated
                    deliverResult(requestId, null, e);
                }
            };

            mActionScreenUtils.createKeyPair(params, sc);
        }

        private void hasKeyPair(final JSONObject params) throws JSONException {
            if (params == null) {
                L.w("Expected params != null");
                return;
            }
            final String requestId = params.getString("id");

            MainService.SecurityCallback sc = new MainService.SecurityCallback() {
                @Override
                public void onSuccess(Object result) {
                    boolean exists = (boolean) result;
                    Map<String, Object> r = new HashMap<>();
                    r.put("exists", exists);
                    deliverResult(requestId, r, null);
                }

                @Override
                public void onError(String code, String errorMessage) {
                    Map<String, Object> e = new HashMap<>();
                    e.put("code", code);
                    e.put("message", errorMessage);
                    e.put("exception", errorMessage); // deprecated
                    deliverResult(requestId, null, e);
                }
            };

            mActionScreenUtils.hasKeyPair(params, sc);
        }

        private void getPublicKey(final JSONObject params) throws JSONException {
            if (params == null) {
                L.w("Expected params != null");
                return;
            }
            final String requestId = params.getString("id");

            MainService.SecurityCallback sc = new MainService.SecurityCallback() {
                @Override
                public void onSuccess(Object result) {
                    String publicKeyString = (String) result;
                    if (publicKeyString == null) {
                        Map<String, Object> e = new HashMap<>();
                        e.put("code", "key_not_found");
                        String errorMessage = getString(R.string.key_not_found);
                        e.put("message", errorMessage);
                        e.put("exception", errorMessage); // deprecated
                        deliverResult(requestId, null, e);
                    } else {
                        Map<String, Object> r = new HashMap<>();
                        r.put("public_key", publicKeyString);
                        deliverResult(requestId, r, null);
                    }
                }

                @Override
                public void onError(String code, String errorMessage) {
                    Map<String, Object> e = new HashMap<>();
                    e.put("code", code);
                    e.put("message", errorMessage);
                    e.put("exception", errorMessage); // deprecated
                    deliverResult(requestId, null, e);
                }
            };

            mActionScreenUtils.getPublicKey(params, sc);
        }

        private void getSeed(final JSONObject params) throws JSONException {
            if (params == null) {
                L.w("Expected params != null");
                return;
            }
            final String requestId = params.getString("id");

            MainService.SecurityCallback sc = new MainService.SecurityCallback() {
                @Override
                public void onSuccess(Object result) {
                    String seed = (String) result;
                    Map<String, Object> r = new HashMap<>();
                    r.put("seed", seed);
                    deliverResult(requestId, r, null);
                }

                @Override
                public void onError(String code, String errorMessage) {
                    Map<String, Object> e = new HashMap<>();
                    e.put("code", code);
                    e.put("message", errorMessage);
                    e.put("exception", errorMessage); // deprecated
                    deliverResult(requestId, null, e);
                }
            };

            mActionScreenUtils.getSeed(params, sc);
        }

        private void getAddress(final JSONObject params) throws JSONException {
            if (params == null) {
                L.w("Expected params != null");
                return;
            }
            final String requestId = params.getString("id");

            MainService.SecurityCallback sc = new MainService.SecurityCallback() {
                @Override
                public void onSuccess(Object result) {
                    String address = (String) result;
                    Map<String, Object> r = new HashMap<>();
                    r.put("address", address);
                    deliverResult(requestId, r, null);
                }

                @Override
                public void onError(String code, String errorMessage) {
                    Map<String, Object> e = new HashMap<>();
                    e.put("code", code);
                    e.put("message", errorMessage);
                    e.put("exception", errorMessage); // deprecated
                    deliverResult(requestId, null, e);
                }
            };

            mActionScreenUtils.getAddress(params, sc);
        }

        private void signPayload(final JSONObject params) throws JSONException {
            if (params == null) {
                L.w("Expected params != null");
                return;
            }
            final String requestId = params.getString("id");
            final String payload = TextUtils.optString(params, "payload", null);

            MainService.SecurityCallback sc = new MainService.SecurityCallback() {
                @Override
                public void onSuccess(Object result) {
                    try {
                        byte[] payloadSignature = (byte[]) result;
                        Map<String, Object> r = new HashMap<>();
                        r.put("payload", payload);
                        r.put("payload_signature", Base64.encodeBytes(payloadSignature, Base64.DONT_BREAK_LINES));
                        deliverResult(requestId, r, null);
                    } catch (Exception exc) {
                        L.bug("signPayload onSuccess exception", exc);
                        Map<String, Object> e = new HashMap<>();
                        e.put("code", "unknown_error_occurred");
                        String errorMessage = getString(R.string.unknown_error_occurred);
                        e.put("message", errorMessage);
                        e.put("exception", errorMessage); // deprecated
                        deliverResult(requestId, null, e);
                    }
                }

                @Override
                public void onError(String code, String errorMessage) {
                    Map<String, Object> e = new HashMap<>();
                    e.put("code", code);
                    e.put("message", errorMessage);
                    e.put("exception", errorMessage); // deprecated
                    deliverResult(requestId, null, e);
                }
            };

            mActionScreenUtils.signPayload(params, payload, sc);
        }

        private void verifySignedPayload(final JSONObject params) throws JSONException {
            if (params == null) {
                L.w("Expected params != null");
                return;
            }
            final String requestId = params.getString("id");

            MainService.SecurityCallback sc = new MainService.SecurityCallback() {
                @Override
                public void onSuccess(Object result) {
                    boolean valid = (boolean) result;
                    Map<String, Object> r = new HashMap<>();
                    r.put("valid", valid);
                    deliverResult(requestId, r, null);
                }

                @Override
                public void onError(String code, String errorMessage) {
                    Map<String, Object> e = new HashMap<>();
                    e.put("code", code);
                    e.put("message", errorMessage);
                    e.put("exception", errorMessage); // deprecated
                    deliverResult(requestId, null, e);
                }
            };

            mActionScreenUtils.verifySignedPayload(params, sc);
        }
    }

    private TaggedWakeLock newWakeLock() {
        TaggedWakeLock wl = TaggedWakeLock.newTaggedWakeLock(mService, PowerManager.FULL_WAKE_LOCK,
            "ACTIONSCREEN WakeLock");
        return wl;
    }

    @SuppressLint({"SetJavaScriptEnabled", "JavascriptInterface"})
    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (CloudConstants.isContentBrandingApp()) {
            super.setTheme(android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        }
        super.onCreate(savedInstanceState);
        if (this instanceof ContentBrandingActionScreenActivity) {
            setContentViewWithoutNavigationBar(R.layout.action_screen);
        } else {
            setContentView(R.layout.action_screen);
        }

        mBranding = (WebView) findViewById(R.id.branding);
        WebSettings brandingSettings = mBranding.getSettings();
        brandingSettings.setJavaScriptEnabled(true);
        brandingSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            brandingSettings.setAllowFileAccessFromFileURLs(true);
        }

        if (CloudConstants.isContentBrandingApp()) {
            int cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
            if (cameraPermission == PackageManager.PERMISSION_GRANTED) {
                mQRCodeScanner = QRCodeScanner.getInstance(this);
                final LinearLayout previewHolder = (LinearLayout) findViewById(R.id.preview_view);
                previewHolder.addView(mQRCodeScanner.view);
            }
            mBranding.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    initFullScreenForContentBranding();
                }
            });
        }

        final View brandingFooter = findViewById(R.id.branding_footer_container);

        if (CloudConstants.isContentBrandingApp()) {
            final ImageView brandingFooterClose = (ImageView) findViewById(R.id.branding_footer_close);
            final TextView brandingFooterText = (TextView) findViewById(R.id.branding_footer_text);
            brandingFooterText.setText(getString(R.string.back));
            brandingFooterClose.setColorFilter(UIUtils.imageColorFilter(ContextCompat.getColor(this,
                R.color.mc_homescreen_text)));

            brandingFooter.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mQRCodeScanner != null) {
                        mQRCodeScanner.onResume();
                    }
                    brandingFooter.setVisibility(View.GONE);
                    mBranding.setVisibility(View.VISIBLE);
                }
            });
        }

        final RelativeLayout openPreview = (RelativeLayout) findViewById(R.id.preview_holder);

        openPreview.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mQRCodeScanner != null) {
                    mQRCodeScanner.previewHolderClicked();
                }
            }
        });

        mBranding.addJavascriptInterface(new JSInterface(this), "__rogerthat__");
        mBranding.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                String message = consoleMessage.message();
                int lineNumber = consoleMessage.lineNumber();
                String sourceID = consoleMessage.sourceId();
                String level = consoleMessage.messageLevel().toString();
                if (sourceID != null) {
                    try {
                        sourceID = new File(sourceID).getName();
                    } catch (Exception e) {
                        L.d("Could not get fileName of sourceID: " + sourceID, e);
                    }
                }
                if (mIsHtmlContent) {
                    L.i("[BRANDING] " + level + ": " + sourceID + ":" + lineNumber + " | " + message);
                } else {
                    L.d("[BRANDING] " + level + ": " + sourceID + ":" + lineNumber + " | " + message);
                }
                return true;
            }
        });
        mBranding.setWebViewClient(new WebViewClient() {
            private boolean isExternalUrl(String url) {
                for (String regularExpression : mBrandingResult.externalUrlPatterns) {
                    if (url.matches(regularExpression)) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                L.i("Branding is loading url: " + url);
                Uri uri = Uri.parse(url);
                String lowerCaseUrl = url.toLowerCase();
                if (lowerCaseUrl.startsWith("tel:") || lowerCaseUrl.startsWith("mailto:") || isExternalUrl(url)) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                    return true;
                } else if (lowerCaseUrl.startsWith(POKE)) {
                    String tag = url.substring(POKE.length());
                    poke(tag);
                    return true;
                } else if (lowerCaseUrl.startsWith("http://") || lowerCaseUrl.startsWith("https://")) {
                    if (mQRCodeScanner != null) {
                        mQRCodeScanner.onPause();
                    }
                    CustomTabsIntent.Builder customTabsBuilder = new CustomTabsIntent.Builder();
                    CustomTabsIntent customTabsIntent = customTabsBuilder.build();
                    customTabsIntent.launchUrl(ActionScreenActivity.this, uri);
                    return true;
                }
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                L.i("onPageFinished " + url);
                if (!mInfoSet && mService != null && mIsHtmlContent) {
                    Map<String, Object> info = mFriendsPlugin.getRogerthatUserAndServiceInfo(mServiceEmail,
                        mServiceFriend);

                    executeJS(true, "if (typeof rogerthat !== 'undefined') rogerthat._setInfo(%s)",
                        JSONValue.toJSONString(info));
                    mInfoSet = true;
                }
            }


            @Override
            public WebResourceResponse shouldInterceptRequest (WebView view, String url) {
                L.i("Intercepting request: " + url);
                if (url.startsWith("data:")) {
                    return null;
                }

                final URL parsedUrl;
                try {
                     parsedUrl = new URL(url);
                } catch (MalformedURLException e) {
                    L.d("Webview tried to load malformed URL");
                    return new WebResourceResponse("text/plain", "UTF-8", null);
                }
                if (!parsedUrl.getProtocol().equals("file")) {
                    return null;
                }
                File urlPath = new File(parsedUrl.getPath());
                if (urlPath.getAbsolutePath().startsWith(mBrandingResult.dir.getAbsolutePath())) {
                    return null;
                }
                if (url.startsWith("file:///android_asset/")) {
                    return null;
                }
                L.d("404: Webview tries to load outside its sandbox.");
                return new WebResourceResponse("text/plain", "UTF-8", null);
            }
        });

        Intent intent = getIntent();
        mBrandingKey = intent.getStringExtra(BRANDING_KEY);
        mServiceEmail = intent.getStringExtra(SERVICE_EMAIL);
        mItemTagHash = intent.getStringExtra(ITEM_TAG_HASH);
        mItemLabel = intent.getStringExtra(ITEM_LABEL);
        mItemCoords = intent.getLongArrayExtra(ITEM_COORDS);
        mRunInBackground = intent.getBooleanExtra(RUN_IN_BACKGROUND, true);
        setTitle(mItemLabel);
        setActivityName("click|" + mItemTagHash);
    }

    @Override
    protected void onPause() {
        executeJS(false, "if (typeof rogerthat !== 'undefined') rogerthat._onPause()");
        super.onPause();

        if (mWakelockEnabled) {
            mWakeLock.release();
        }

        if (mBranding.getVisibility() == View.VISIBLE) {
            if (mQRCodeScanner != null) {
                mQRCodeScanner.onPause();
            }
        }
    }

    @Override
    protected void onResume() {
        executeJS(false, "if (typeof rogerthat !== 'undefined') rogerthat._onResume()");
        super.onResume();

        if (mWakelockEnabled) {
            mWakeLock.acquire();
        }

        if (mQRCodeScanner != null) {
            if (mQRCodeScanner.cameraManager == null) {
                mQRCodeScanner.startCamera();
            } else if (mBranding.getVisibility() == View.VISIBLE) {
                    mQRCodeScanner.onResume();
            }
        }

        if (CloudConstants.isContentBrandingApp()) {
            initFullScreenForContentBranding();
        }
    }

    @SuppressLint("NewApi")
    private void initFullScreenForContentBranding() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mBranding.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    private boolean deliverApiResult(ServiceApiCallbackResult r) {
        if (!mApiResultHandlerSet) {
            L.i("apiCallResultHandler not set, thus not delivering any api call responses.");
            return false;
        }

        // Using JSONValue.toJSONString on these string for null value support and quote encoding
        executeJS(false, "if (typeof rogerthat !== 'undefined') rogerthat.api._setResult(%s, %s, %s, %s)",
            JSONValue.toJSONString(r.method), JSONValue.toJSONString(r.result), JSONValue.toJSONString(r.error),
            JSONValue.toJSONString(r.tag));

        return true;
    }

    private void deliverFacebookResult(String requestId, Map<String, Object> result, Map<String, Object> error) {
        executeJS(false, "if (typeof rogerthat !== 'undefined') rogerthat.facebook._setResult('%s', %s, %s)",
            requestId, JSONValue.toJSONString(result), JSONValue.toJSONString(error));
    }

    private void deliverResult(String requestId, Map<String, Object> result, Map<String, Object> error) {
        T.dontCare();
        executeJS(false, "if (typeof rogerthat !== 'undefined') rogerthat._setResult('%s', %s, %s)", requestId,
            JSONValue.toJSONString(result), JSONValue.toJSONString(error));
    }

    protected void executeJS(final boolean force, final String jsCommandFormat, final Object... args) {
        if (T.getThreadType() == T.UI) {
            T.UI();
            String jsCommand = String.format(Locale.US, jsCommandFormat, args);
            if (mIsHtmlContent && (mInfoSet || force)) {
                L.d("Executing JS: " + jsCommand);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                    mBranding.evaluateJavascript(jsCommand, null);
                } else {
                    mBranding.loadUrl("javascript:" + jsCommand);
                }
            } else {
                L.d("NOT Executing JS: " + jsCommand);
            }
        } else {
            if (mService != null) {
                mService.postOnUIHandler(new SafeRunnable() {
                    @Override
                    protected void safeRun() throws Exception {
                        executeJS(force, jsCommandFormat, args);
                    }
                });
            }
        }
    }

    private void facebookLogin(final String requestId, final String permissionsStr) {
        if (!mService.getNetworkConnectivityManager().isConnected()) {
            Map<String, Object> e = new HashMap<String, Object>();
            e.put("type", FACEBOOK_TYPE_ERROR);
            e.put("code", "no_network_connection");
            e.put("exception", getString(R.string.registration_screen_instructions_check_network_not_available));
            deliverFacebookResult(requestId, null, e);
            return;
        }

        L.d("Authorizing with facebook with permissions: " + permissionsStr);
        final List<String> permissions = Arrays.asList(permissionsStr.split(","));

        FacebookUtils.ensureOpenSession(this, permissions, PermissionType.READ, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest
                        .GraphJSONObjectCallback() {

                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        if (response.getError() == null) {
                            deliverFacebookResult(requestId, (Map<String, Object>) JSONValue.parse(object.toString()),
                                    null);
                        } else {
                            L.e("Failed to execute fb request to /me\nResponse: " + response);
                            Map<String, Object> e = new HashMap<>();
                            e.put("type", FACEBOOK_TYPE_ERROR);
                            e.put("code", "unknown");
                            e.put("exception", "Failed to execute fb request to /me\nResponse: " + response
                                    .toString());
                            deliverFacebookResult(requestId, null, e);
                        }
                }
                }).executeAsync();
            }

            @Override
            public void onCancel() {
                deliverFacebookResult(requestId, null, FACEBOOK_MAP_CANCEL);
            }

            @Override
            public void onError(FacebookException exception) {
                Map<String, Object> e = new HashMap<>();
                e.put("type", FACEBOOK_TYPE_ERROR);
                e.put("code", "unknown");
                e.put("exception", exception.toString());
                L.d(exception.toString());
                deliverFacebookResult(requestId, null, e);
            }
        }, true);
    }

    private void facebookPost(final String requestId, final String postParamsStr) {
        if (!mService.getNetworkConnectivityManager().isConnected()) {
            Map<String, Object> e = new HashMap<String, Object>();
            e.put("type", FACEBOOK_TYPE_ERROR);
            e.put("code", "no_network_connection");
            e.put("exception", getString(R.string.registration_screen_instructions_check_network_not_available));
            deliverFacebookResult(requestId, null, e);
            return;
        }

        L.d("Posting on facebook with post params: " + postParamsStr);
        final Map<String, Object> postParams = (Map<String, Object>) JSONValue.parse(postParamsStr);
        final String link = (String) postParams.get("link");
        final String picture = (String) postParams.get("picture");

        final ShareLinkContent.Builder contentBuilder = new ShareLinkContent.Builder();
        contentBuilder.setContentUrl(link == null ? null : Uri.parse(link));
        contentBuilder.setImageUrl(picture == null ? null : Uri.parse(picture));
        contentBuilder.setContentTitle((String) postParams.get("caption"));
        contentBuilder.setContentDescription((String) postParams.get("description"));
        final ShareLinkContent content = contentBuilder.build();

        if (CloudConstants.DEBUG_LOGGING) {
            Map<String, String> params = new HashMap<>();
            params.put("content.contentDescription", content.getContentDescription());
            params.put("content.contentTitle", content.getContentTitle());
            params.put("content.contentURL", content.getContentUrl().toString());
            params.put("content.imageURL", content.getImageUrl().toString());
            L.d(params.toString());
        }

        final ShareDialog shareDialog = new ShareDialog(this);
        shareDialog.show(content, ShareDialog.Mode.AUTOMATIC);
        shareDialog.registerCallback(getFacebookCallbackManager(), new FacebookCallback<Sharer.Result>() {
            @Override
            public void onSuccess(Sharer.Result result) {
                if (result.getPostId() == null) {
                    onCancel();
                    return;
                }

                final Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("postId", result.getPostId());
                deliverFacebookResult(requestId, resultMap, null);
            }

            @Override
            public void onCancel() {
                deliverFacebookResult(requestId, null, FACEBOOK_MAP_CANCEL);
            }

            @Override
            public void onError(FacebookException exception) {
                Map<String, Object> e = new HashMap<String, Object>();
                e.put("type", FACEBOOK_TYPE_ERROR);
                e.put("code", "unknown");
                e.put("exception", exception.toString());
                L.d(exception.toString());
                deliverFacebookResult(requestId, null, e);
            }
        });
    }

    private void facebookTicker(final String requestId, final String type, final String postParamsStr) {
        if (!mService.getNetworkConnectivityManager().isConnected()) {
            Map<String, Object> e = new HashMap<>();
            e.put("type", FACEBOOK_TYPE_ERROR);
            e.put("code", "no_network_connection");
            e.put("exception", getString(R.string.registration_screen_instructions_check_network_not_available));
            deliverFacebookResult(requestId, null, e);
            return;
        }

        L.d("Ticker on facebook with post params: " + postParamsStr);

        FacebookUtils.ensureOpenSession(this, FacebookUtils.PUBLISH_PERMISSIONS, PermissionType.PUBLISH, new
                FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Bundle bundle = new Bundle();
                        @SuppressWarnings("unchecked")
                        final Map<String, Object> postParams = (Map<String, Object>) JSONValue.parse(postParamsStr);
                        for (Map.Entry<String, Object> entry : postParams.entrySet()) {
                            if (entry.getValue() instanceof String) {
                                bundle.putString(entry.getKey(), (String) entry.getValue());
                            }
                        }
                        L.d("Ticker type: " + type + "\nBundle: " + bundle);

                        new GraphRequest(AccessToken.getCurrentAccessToken(), type, bundle, HttpMethod.POST, new
                                GraphRequest.Callback() {

                            @Override
                            public void onCompleted(GraphResponse response) {
                                if (response == null || response.getJSONObject() == null) {
                                    L.w("Graph response is NULL");
                                    Map<String, Object> e = new HashMap<>();
                                    e.put("type", FACEBOOK_TYPE_ERROR);
                                    e.put("code", "unknown");
                                    e.put("exception", "Graph response is NULL");
                                    deliverFacebookResult(requestId, null, e);
                                    return;
                                }

                                final JSONObject jsonObject = response.getJSONObject();
                                if (jsonObject.has("postId")) {
                                    try {
                                        final Map<String, Object> result = new HashMap<>();
                                        result.put("postId", jsonObject.getString("id"));
                                        deliverFacebookResult(requestId, result, null);
                                    } catch (JSONException exc) {
                                        L.bug(exc);
                                        Map<String, Object> e = new HashMap<>();
                                        e.put("type", FACEBOOK_TYPE_ERROR);
                                        e.put("code", "unknown");
                                        e.put("exception", "Unexpected JSONException occurred");
                                        deliverFacebookResult(requestId, null, e);
                                    }
                                } else {
                                    deliverFacebookResult(requestId, null, FACEBOOK_MAP_CANCEL);
                                }
                            }
                                }).executeAsync();
                    }

                    @Override
                    public void onCancel() {
                        deliverFacebookResult(requestId, null, FACEBOOK_MAP_CANCEL);
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Map<String, Object> e = new HashMap<>();
                        e.put("type", FACEBOOK_TYPE_ERROR);
                        e.put("code", "unknown");
                        e.put("exception", exception.toString());
                        L.d(exception.toString());
                        deliverFacebookResult(requestId, null, e);
                    }
                }, true);
    }

    private void poke(String tag) {
        if (mPoker == null) {
            mPoker = new Poker<>(this, mServiceEmail);
        }

        mPoker.poke(tag, null);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onServiceBound() {


        if (CloudConstants.isContentBrandingApp()) {
            final IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(BrandingMgr.SERVICE_BRANDING_AVAILABLE_INTENT);
            intentFilter.addAction(FriendsPlugin.FRIEND_UPDATE_INTENT);
            registerReceiver(mBroadcastReceiver, intentFilter);
        }

        mMessagingPlugin = mService.getPlugin(MessagingPlugin.class);
        mFriendsPlugin = mService.getPlugin(FriendsPlugin.class);

        mActionScreenUtils = new ActionScreenUtils(this, mServiceEmail, mItemTagHash, mRunInBackground);
        mActionScreenUtils.start(mIntentCallback);
        displayBranding();

        if (mQRCodeScanner != null) {
            mQRCodeScanner.mainService = mService;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == ScanTabActivity.MARKET_INSTALL_RESULT) {
            mQRCodeScannerOpen = false;
            // No need to do anything
        } else if (requestCode == ScanTabActivity.ZXING_SCAN_RESULT) {
            mQRCodeScannerOpen = false;
            if (resultCode == RESULT_OK) {
                final String rawScanResult = intent.getStringExtra("SCAN_RESULT");
                if (rawScanResult != null) {
                    L.i("Scanned QR code: " + rawScanResult);
                    final Map<String, Object> result = new HashMap<String, Object>();
                    if (rawScanResult.toLowerCase(Locale.US).startsWith("http://")
                            || rawScanResult.toLowerCase(Locale.US).startsWith("https://")) {
                        if (mScanCommunication == null) {
                            mScanCommunication = new ScanCommunication(mService);
                        }
                        mScanCommunication.resolveUrl(rawScanResult);
                        result.put("status", "resolving");

                    } else {
                        result.put("status", "resolved");
                    }
                    result.put("content", rawScanResult);
                    executeJS(false, "if (typeof rogerthat !== 'undefined') rogerthat._qrCodeScanned(%s)",
                            JSONValue.toJSONString(result));

                } else {
                    Map<String, Object> result = new HashMap<String, Object>();
                    result.put("status", "error");
                    result.put("content", "An unknown error has occurred");
                    executeJS(false, "if (typeof rogerthat !== 'undefined') rogerthat._qrCodeScanned(%s)",
                            JSONValue.toJSONString(result));
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }

    @SuppressLint({ "SetJavaScriptEnabled", "Wakelock" })
    private void displayBranding() {
        try {
            mServiceFriend = mFriendsPlugin.getStore().getExistingFriend(mServiceEmail);
            mBrandingResult = mMessagingPlugin.getBrandingMgr().prepareBranding(mBrandingKey, mServiceFriend, true);
            WebSettings settings = mBranding.getSettings();
            settings.setJavaScriptEnabled(true);
            settings.setBlockNetworkImage(false);

            if (mBrandingResult.contentType != null
                && AttachmentViewerActivity.CONTENT_TYPE_PDF.equalsIgnoreCase(mBrandingResult.contentType)) {

                setContentView(R.layout.pdf_viewer);
                PDFView viewer = (PDFView) findViewById(R.id.pdfView);
                viewer.fromFile(new File(mBrandingResult.file.getAbsolutePath()))
                        .enableSwipe(true)
                        .enableDoubletap(true)
                        .load();

            } else {
                mIsHtmlContent = true;
                mBranding.loadUrl("file://"+mBrandingResult.file.getAbsolutePath());
                mBranding.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
                if (mBrandingResult.color != null) {
                    mBranding.setBackgroundColor(mBrandingResult.color);
                }
            }
            mWakelockEnabled = mBrandingResult.wakelockEnabeld;
            L.d("wakelockEnabeld: " + mWakelockEnabled);
            if (mWakelockEnabled && mWakeLock == null) {
                mWakeLock = newWakeLock();
                L.d("Acquiring wakelock " + mWakeLock.hashCode());
                mWakeLock.acquire();
            } else if (mWakeLock != null && !mWakelockEnabled) {
                mWakeLock.release();
            }

            switch (mBrandingResult.orientation) {
                case LANDSCAPE:
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    break;
                case PORTRAIT:
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    break;
                case DYNAMIC:
                default:
                    break;
            }
        } catch (BrandingFailureException e) {
            UIUtils.showLongToast(this, getString(R.string.failed_to_show_action_screen));
            finish();
            mMessagingPlugin.getBrandingMgr().queue(mServiceFriend);
            L.e("Could not display menu item with screen branding.", e);
            return;
        }

    }

    @Override
    protected void onServiceUnbound() {
        if (CloudConstants.isContentBrandingApp()) {
            unregisterReceiver(mBroadcastReceiver);
        }
        if (mIsListeningBacklogConnectivityChanged) {
            mActionScreenUtils.stopBacklogListener();
        }
        mActionScreenUtils.stop();
        if (mPoker != null) {
            mPoker.stop();
        }
    }

    private ActionScreenUtils.IntentCallback mIntentCallback = new ActionScreenUtils.IntentCallback() {
        @Override
        public boolean apiResult(ServiceApiCallbackResult result) {
            return deliverApiResult(result);
        }

        @Override
        public void userDataUpdated(String userData) {
            executeJS(false, "if (typeof rogerthat !== 'undefined') rogerthat._userDataUpdated(%s)", userData);
        }

        @Override
        public void serviceDataUpdated(String serviceData) {
            executeJS(false, "if (typeof rogerthat !== 'undefined') rogerthat._serviceDataUpdated(%s)", serviceData);
        }

        @Override
        public void onBeaconInReach(Map<String, Object> beacon) {
            executeJS(false, "if (typeof rogerthat !== 'undefined') rogerthat._onBeaconInReach(%s)", JSONValue.toJSONString(beacon));
        }

        @Override
        public void onBeaconOutOfReach(Map<String, Object> beacon) {
            executeJS(false, "if (typeof rogerthat !== 'undefined') rogerthat._onBeaconOutOfReach(%s)", JSONValue.toJSONString(beacon));
        }

        @Override
        public void qrCodeScanned(Map<String, Object> result) {
            executeJS(false, "if (typeof rogerthat !== 'undefined') rogerthat._qrCodeScanned(%s)", JSONValue.toJSONString(result));
        }

        @Override
        public void onBackendConnectivityChanged(boolean connected) {
            executeJS(false, "if (typeof rogerthat !== 'undefined') rogerthat._onBackendConnectivityChanged(%s)", connected);
        }
    };

    private BroadcastReceiver mBroadcastReceiver = new SafeBroadcastReceiver() {
        @SuppressLint("DefaultLocale")
        @Override
        public String[] onSafeReceive(final Context context, final Intent intent) {
            T.UI();
            if (BrandingMgr.SERVICE_BRANDING_AVAILABLE_INTENT.equals(intent.getAction())) {
                if (mServiceEmail.equals(intent.getStringExtra(BrandingMgr.SERVICE_EMAIL))) {
                    FriendStore friendStore = mFriendsPlugin.getStore();
                    Friend f = friendStore.getFriend(mServiceEmail);
                    if (!f.contentBrandingHash.equals(mBrandingKey)
                        && f.contentBrandingHash.equals(intent.getStringExtra(BrandingMgr.BRANDING_KEY))) {
                        mBrandingKey = f.contentBrandingHash;

                        if (mBranding.getVisibility() == View.VISIBLE) {
                            if (mQRCodeScanner != null) {
                                mQRCodeScanner.stopScanningForQRCodes();
                            }
                        }
                        mInfoSet = false;
                        displayBranding();
                    }
                }

            } else if (FriendsPlugin.FRIEND_UPDATE_INTENT.equals(intent.getAction())) {
                if (mServiceEmail.equals(intent.getStringExtra(BrandingMgr.SERVICE_EMAIL))) {
                    FriendStore friendStore = mFriendsPlugin.getStore();
                    Friend f = friendStore.getFriend(mServiceEmail);
                    if (!f.contentBrandingHash.equals(mBrandingKey)) {
                        BrandingMgr brandingMgr = mMessagingPlugin.getBrandingMgr();
                        try {
                            if (brandingMgr.isBrandingAvailable(f.contentBrandingHash)) {
                                mBrandingKey = f.contentBrandingHash;
                                if (mBranding.getVisibility() == View.VISIBLE) {
                                    if (mQRCodeScanner != null) {
                                        mQRCodeScanner.stopScanningForQRCodes();
                                    }
                                }
                                mInfoSet = false;
                                displayBranding();
                            }
                        } catch (BrandingFailureException e) {
                            L.d(e);
                        }
                    }
                }
            }

            return null;
        }

    };

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        L.i(getClass() + ": onConfigurationChanged");

        if (mBrandingResult != null) {
            L.d("New orientation: " + newConfig.orientation);
            switch (mBrandingResult.orientation) {
                case LANDSCAPE:
                    if (newConfig.orientation != Configuration.ORIENTATION_LANDSCAPE) {
                        L.d("Changing to SCREEN_ORIENTATION_LANDSCAPE");
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    }
                    if (mQRCodeScanner != null && mQRCodeScanner.cameraManager.isOpen()) {
                        mQRCodeScanner.stopScanningForQRCodes();
                        mQRCodeScanner.startScanningForQRCodes();
                    }
                    break;
                case PORTRAIT:
                    if (newConfig.orientation != Configuration.ORIENTATION_PORTRAIT) {
                        L.d("Changing to SCREEN_ORIENTATION_PORTRAIT");
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    }
                    if (mQRCodeScanner != null && mQRCodeScanner.cameraManager.isOpen()) {
                        mQRCodeScanner.stopScanningForQRCodes();
                        mQRCodeScanner.startScanningForQRCodes();
                    }
                    break;
                case DYNAMIC:
                default:
                    break;
            }
        }

        super.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onKeyDown(final int keyCode, final KeyEvent event) {
        long now = System.currentTimeMillis();
        if (mJavascriptBackBtnListener && keyCode == KeyEvent.KEYCODE_BACK && now - mLasttimeBackPressed > 500) {
            mLasttimeBackPressed = now;
            mCurrentBackPressedId = UUID.randomUUID().toString();
            final String context = mCurrentBackPressedId;
            executeJS(false, "if (typeof rogerthat !== 'undefined') rogerthat._backPressed('%s')",
                mCurrentBackPressedId);
            // javascript has 500 ms to answer
            mService.postDelayedOnUIHandler(new SafeRunnable() {
                @Override
                protected void safeRun() throws Exception {
                    if (context.equals(mCurrentBackPressedId)) {
                        L.w("Javascript did not process the backPressed event fast enough. Finishing...");
                        finish();
                    }
                }
            }, 500);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void finish() {
        T.UI();
        if (mBranding != null) {
            // Stop all javascript
            mBranding.loadUrl("about:blank");
        }
        super.finish();
    }
}

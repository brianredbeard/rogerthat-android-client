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
package com.mobicage.rogerthat.registration;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.ViewFlipper;

import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.Installation;
import com.mobicage.rogerthat.MainService;
import com.mobicage.rogerthat.OauthActivity;
import com.mobicage.rogerthat.config.Configuration;
import com.mobicage.rogerthat.config.ConfigurationProvider;
import com.mobicage.rogerthat.plugins.scan.ProcessScanActivity;
import com.mobicage.rogerthat.registration.AccountManager.Account;
import com.mobicage.rogerthat.util.FacebookUtils;
import com.mobicage.rogerthat.util.FacebookUtils.PermissionType;
import com.mobicage.rogerthat.util.GoogleServicesUtils;
import com.mobicage.rogerthat.util.GoogleServicesUtils.GCMRegistrationIdFoundCallback;
import com.mobicage.rogerthat.util.RegexPatterns;
import com.mobicage.rogerthat.util.TextUtils;
import com.mobicage.rogerthat.util.consent.ConsentProvider;
import com.mobicage.rogerthat.util.net.NetworkConnectivityManager;
import com.mobicage.rogerthat.util.security.SecurityUtils;
import com.mobicage.rogerthat.util.http.HTTPUtil;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeBroadcastReceiver;
import com.mobicage.rogerthat.util.system.SafeDialogClick;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.system.SafeViewOnClickListener;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.ui.FSListView;
import com.mobicage.rogerthat.util.ui.UIUtils;
import com.mobicage.rogerthat.util.ui.Wizard;
import com.mobicage.rpc.Credentials;
import com.mobicage.rpc.config.AppConstants;
import com.mobicage.rpc.config.CloudConstants;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

// TODO: this class still has lots of duplicated code

public class RegistrationActivity2 extends AbstractRegistrationActivity {

    private static final int PIN_LENGTH = 4;

    public static final String QRSCAN_CONFIGKEY = "QR_SCAN";
    public static final String OPENED_URL_CONFIGKEY = "opened_url";

    private static final int PERMISSION_REQUEST_GET_ACCOUNTS = 1;

    private static final int START_OAUTH_REQUEST_CODE = 1;

    private static final int[] NORMAL_WIDTH_ROGERTHAT_LOGOS = new int[] { R.id.rogerthat_logo,
            R.id.rogerthat_logo1, R.id.rogerthat_logo2, R.id.rogerthat_logo3, R.id.rogerthat_logo4,
            R.id.rogerthat_logo5};
    private static final int[] FULL_WIDTH_ROGERTHAT_LOGOS = new int[] { R.id.full_width_rogerthat_logo,
            R.id.full_width_rogerthat_logo1,  R.id.full_width_rogerthat_logo2, R.id.full_width_rogerthat_logo3,
            R.id.full_width_rogerthat_logo4, R.id.full_width_rogerthat_logo5};

    private Intent mNotYetProcessedIntent = null;

    private RegistrationWizard2 mWiz;
    private AutoCompleteTextView mEnterEmailAutoCompleteTextView;
    private EditText mEnterPinEditText;
    private HttpClient mHttpClient;

    private WebView mTOSWebview;

    private BroadcastReceiver mBroadcastReceiver = new SafeBroadcastReceiver() {
        @Override
        public String[] onSafeReceive(Context context, Intent intent) {
            T.UI();
            final String action = intent.getAction();
            if (INTENT_LOG_URL.equals(action)) {
                String url = intent.getStringExtra("url");
                int count = intent.getIntExtra("count", 0);
                sendRegistrationUrl(url, count);
            } else if (NetworkConnectivityManager.INTENT_NETWORK_UP.equals(action)) {
                int position = mWiz.getPosition();
                if (position == 1) { // tos
                    mTOSWebview = (WebView) findViewById(R.id.tos_webview);
                    mTOSWebview.loadUrl(AppConstants.TERMS_OF_SERVICE_URL);
                }
            }

            return null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        T.UI();
        init(this);
        setTitle(R.string.registration_title);

        mHttpClient = HTTPUtil.getHttpClient(HTTP_TIMEOUT, HTTP_RETRY_COUNT);

        final IntentFilter filter = new IntentFilter(INTENT_LOG_URL);
        filter.addAction(NetworkConnectivityManager.INTENT_NETWORK_UP);
        registerReceiver(mBroadcastReceiver, filter);

        // TODO: This has to be improved.
        // If the app relies on GCM the user should not be able to register.
        if (CloudConstants.USE_GCM_KICK_CHANNEL)
            GoogleServicesUtils.checkPlayServices(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // TODO: This has to be improved.
        // If the app relies on GCM the user should not be able to register.
        if (CloudConstants.USE_GCM_KICK_CHANNEL)
            GoogleServicesUtils.checkPlayServices(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        View v = findViewById(R.id.registration_viewFlipper);
        if (v != null)
            UIUtils.hideKeyboard(this, v);
        if (mService != null && mService.getRegisteredFromConfig()) {
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mServiceIsBound) {
            processIntent(getIntent());
        } else {
            mNotYetProcessedIntent = getIntent();
        }
    }

    private void processIntent(Intent intent) {
        Uri uri = intent.getData();
        if (uri != null) {
            String url = uri.toString();

            if (RegexPatterns.FRIEND_INVITE_WITH_SECRET_URL.matcher(url).matches()) {
                popupRegisterFirst(uri);

                String invitorCode = url.substring(ProcessScanActivity.URL_ROGERTHAT_PREFIX.length(), url.indexOf("?"));
                String secret = uri.getQueryParameter("s");
                Configuration cfg = mService.getConfigurationProvider().getConfiguration(RegistrationWizard2.CONFIGKEY);
                cfg.put(INVITOR_CODE_CONFIGKEY, invitorCode);
                cfg.put(INVITOR_SECRET_CONFIGKEY, secret);
                mService.getConfigurationProvider().updateConfigurationNow(RegistrationWizard2.CONFIGKEY, cfg);

            } else if (RegexPatterns.FRIEND_INVITE_URL.matcher(url).matches()
                    || RegexPatterns.SERVICE_INTERACT_URL.matcher(url).matches()) {
                popupRegisterFirst(uri);

                Configuration cfg = mService.getConfigurationProvider().getConfiguration(RegistrationWizard2.CONFIGKEY);
                cfg.put(OPENED_URL_CONFIGKEY, url);
                mService.getConfigurationProvider().updateConfigurationNow(RegistrationWizard2.CONFIGKEY, cfg);
            }
        }
    }

    private void popupRegisterFirst(Uri uri) {
        String userName = uri.getQueryParameter("u");
        if (userName != null)
            userName = userName.replaceAll("\\+", " ");
        String message = getString(R.string.friend_invitation_register_first, userName);
        UIUtils.showDialog(this, null, message);
    }

    private void showNotification() {
        mService.sendBroadcast(new Intent(RegistrationService.INTENT_SHOW_NOTIFICATION));
    }

    private void hideNotification() {
        mService.sendBroadcast(new Intent(RegistrationService.INTENT_HIDE_NOTIFICATION));
    }

    @Override
    protected void onDestroy() {
        closeWorkerThread();
        unregisterReceiver(mBroadcastReceiver);
        super.onDestroy();
    }

    @Override
    protected void onServiceBound() {
        T.UI();
        if (mNotYetProcessedIntent != null) {
            processIntent(mNotYetProcessedIntent);
            mNotYetProcessedIntent = null;
        }

        setContentViewWithoutNavigationBar(R.layout.registration2);

        final int[] visibleLogos;
        final int[] goneLogos;
        if (AppConstants.FULL_WIDTH_HEADERS) {
            visibleLogos = FULL_WIDTH_ROGERTHAT_LOGOS;
            goneLogos = NORMAL_WIDTH_ROGERTHAT_LOGOS;
            View viewFlipper = findViewById(R.id.registration_viewFlipper);
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) viewFlipper.getLayoutParams();
            params.setMargins(0, 0, 0, 0);
        } else {
            visibleLogos = NORMAL_WIDTH_ROGERTHAT_LOGOS;
            goneLogos = FULL_WIDTH_ROGERTHAT_LOGOS;
        }

        for (int id : visibleLogos)
            findViewById(id).setVisibility(View.VISIBLE);
        for (int id : goneLogos)
            findViewById(id).setVisibility(View.GONE);

        handleScreenOrientation(getResources().getConfiguration().orientation);

        initStartStep();
        initTOStep();
        initNotificationsStep();
        initOauthStep();
        initChooseLoginMethodStep();
        initEnterPinStep();
        initDevicesStep();

        mWiz = RegistrationWizard2.getWizard(mService);
        mWiz.setFlipper((ViewFlipper) findViewById(R.id.registration_viewFlipper));
        setWizard(mWiz);
        setFinishHandler();
        addStartHandler(); // 0
        addAgreeTOSHandler(); // 1
        addNotificationsHandler(); // 2
        addOauthMethodHandler(); // 3
        addChooseLoginMethodHandler(); // 4
        addEnterPinHandler(); // 5
        addRegisterDeviceHandler(); // 6
        mWiz.run();
        mWiz.setDeviceId(Installation.id(this));

        handleEnterEmail();

        if (CloudConstants.USE_GCM_KICK_CHANNEL && GoogleServicesUtils.checkPlayServices(this, true)) {
            GoogleServicesUtils.registerGCMRegistrationId(mService, new GCMRegistrationIdFoundCallback() {
                @Override
                public void idFound(String registrationId) {
                    setGCMRegistrationId(registrationId);
                }
            });
        }

        // unregister reason
        String reason = mService.getUnregisterReason();
        if (!TextUtils.isEmptyOrWhitespace(reason)) {
            SafeDialogClick onPositiveClick = new SafeDialogClick() {
                @Override
                public void safeOnClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                    mService.deleteUnregisterFile();
                }
            };
            UIUtils.showDialog(this, null, reason, getString(R.string.rogerthat), onPositiveClick,
                    null, null);
        }
    }

    private void initStartStep() {
        final Button startBtn = (Button) findViewById(R.id.registration_start);
        TextView rogerthatWelcomeTextView = (TextView) findViewById(R.id.rogerthat_welcome);
        if (CloudConstants.isEnterpriseApp()) {
            rogerthatWelcomeTextView.setText(getString(R.string.rogerthat_welcome_enterprise,
                    getString(R.string.app_name)));
        } else {
            rogerthatWelcomeTextView.setText(getString(R.string.registration_welcome_text_start_registration,
                    getString(R.string.app_name)));
        }

        startBtn.setOnClickListener(new SafeViewOnClickListener() {
            @Override
            public void safeOnClick(View v) {
                startBtn.setEnabled(false);
                sendRegistrationStep(RegistrationWizard2.REGISTRATION_STEP_CREATE_ACCOUNT);
                mWiz.proceedToNextPage();
            }
        });
    }

    private void initTOStep() {
        final Button tosAgreeBtn = (Button) findViewById(R.id.tos_agree);
        tosAgreeBtn.setOnClickListener(new SafeViewOnClickListener() {
            @Override
            public void safeOnClick(View v) {
                tosAgreeBtn.setEnabled(false);
                final CheckBox tosAgeCheckbox = (CheckBox) findViewById(R.id.tos_age);
                if (!tosAgeCheckbox.isChecked()) {
                    final String message = RegistrationActivity2.this.getString( R.string.tos_parent_consent);
                    final String positiveBtn = RegistrationActivity2.this.getString(R.string.grant_permission);
                    final String negativeButtonCaption = RegistrationActivity2.this.getString(R.string.abort);
                    SafeDialogClick positiveClick = new SafeDialogClick() {
                        @Override
                        public void safeOnClick(DialogInterface dialog, int id) {
                            T.UI();
                            dialog.dismiss();
                            mWiz.setTOSAge(ConsentProvider.TOS_AGE_PARENTAL);
                            sendRegistrationStep(RegistrationWizard2.REGISTRATION_STEP_TOS);
                            mWiz.proceedToNextPage();
                        }
                    };
                    SafeDialogClick negativeClick = new SafeDialogClick() {
                        @Override
                        public void safeOnClick(DialogInterface dialog, int id) {
                            T.UI();
                            dialog.dismiss();
                            tosAgreeBtn.setEnabled(true);
                        }
                    };
                    UIUtils.showDialog(RegistrationActivity2.this, null, message, positiveBtn, positiveClick, negativeButtonCaption, negativeClick);
                } else {
                    mWiz.setTOSAge(ConsentProvider.TOS_AGE_16);
                    mService.getConsentProvider().saveConsentForTOS();
                    sendRegistrationStep(RegistrationWizard2.REGISTRATION_STEP_TOS);
                    mWiz.proceedToNextPage();
                }
            }
        });
    }

    private void initNotificationsStep() {
        TextView notificationsTextView = (TextView) findViewById(R.id.notifications_text);
        final String header = getString(R.string.registration_notifications_header, getString(R.string.app_name));
        final String reason = getString(CloudConstants.isCityApp() ? R.string.registration_notification_types_city_app : R.string.registration_notification_types_general);
        notificationsTextView.setText(header + "\n\n" + reason);
        final Button notificationsButton = (Button) findViewById(R.id.notifications_continue);
        notificationsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notificationsButton.setEnabled(false);
                final String title = RegistrationActivity2.this.getString( R.string.notifications_consent_title);
                final String message = RegistrationActivity2.this.getString( R.string.notifications_consent_message);
                final String positiveBtn = RegistrationActivity2.this.getString(R.string.allow);
                final String negativeButtonCaption = RegistrationActivity2.this.getString(R.string.dont_allow);
                SafeDialogClick positiveClick = new SafeDialogClick() {
                    @Override
                    public void safeOnClick(DialogInterface dialog, int id) {
                        T.UI();
                        dialog.dismiss();
                        savePushNotifications(true);
                        sendRegistrationStep(RegistrationWizard2.REGISTRATION_STEP_NOTIFICATION_USAGE);
                        mWiz.proceedToNextPage();
                    }
                };
                SafeDialogClick negativeClick = new SafeDialogClick() {
                    @Override
                    public void safeOnClick(DialogInterface dialog, int id) {
                        T.UI();
                        dialog.dismiss();
                        savePushNotifications(false);
                        sendRegistrationStep(RegistrationWizard2.REGISTRATION_STEP_NOTIFICATION_USAGE);
                        mWiz.proceedToNextPage();
                    }
                };
                UIUtils.showDialog(RegistrationActivity2.this, title, message, positiveBtn, positiveClick, negativeButtonCaption, negativeClick);
            }
        });
    }

    private void initOauthStep() {
        View.OnClickListener oauthLoginListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getOauthRegistrationInfo();
            }
        };

        TextView oauthText = (TextView)findViewById(R.id.oauth_text);
        oauthText.setText(getString(R.string.authenticate_via_your_oauth_account, AppConstants.REGISTRATION_TYPE_OAUTH_DOMAIN));

        findViewById(R.id.login_via_oauth).setOnClickListener(oauthLoginListener);
    }

    private void initChooseLoginMethodStep() {
        TextView signupTextView = (TextView) findViewById(R.id.registration);
        signupTextView.setText(getString(CloudConstants.isCityApp() ? R.string.registration_city_app_sign_up : R
                .string.registration_sign_up, getString(R.string.app_name)));

        mEnterEmailAutoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.registration_enter_email);

        final Button emailButton = (Button) findViewById(R.id.login_via_email);
        View.OnClickListener emailLoginListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emailButton.setEnabled(false);
                sendRegistrationStep(RegistrationWizard2.REGISTRATION_STEP_EMAIL_LOGIN);
                mWiz.proceedToNextPage();
            }
        };
        emailButton.setOnClickListener(emailLoginListener);

        Button facebookButton = (Button) findViewById(R.id.login_via_fb);

        View.OnClickListener facebookLoginListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check network connectivity
                if (!mService.getNetworkConnectivityManager().isConnected()) {
                    UIUtils.showNoNetworkDialog(RegistrationActivity2.this);
                    return;
                }

                sendRegistrationStep(RegistrationWizard2.REGISTRATION_STEP_FACEBOOK_LOGIN);

                final FacebookCallback<LoginResult> fbCallback = new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        if (loginResult.getAccessToken().getPermissions().contains("email")) {
                            registerWithAccessToken(loginResult.getAccessToken().getToken());
                        } else {
                            String message = getString(R.string.facebook_registration_email_missing);
                            UIUtils.showDialog(RegistrationActivity2.this, null, message);
                        }
                    }

                    @Override
                    public void onCancel() {
                    }

                    @Override
                    public void onError(FacebookException error) {
                        L.bug("Facebook SDK error during registration", error);
                        UIUtils.showErrorPleaseRetryDialog(RegistrationActivity2.this);
                    }
                };

                List<String> permissions;
                if (AppConstants.PROFILE_SHOW_GENDER_AND_BIRTHDATE) {
                    permissions = Arrays.asList("email", "user_friends", "user_birthday");
                } else {
                    permissions = Arrays.asList("email", "user_friends");
                }
                FacebookUtils.ensureOpenSession(RegistrationActivity2.this, permissions, PermissionType.READ,
                        fbCallback, false);
            }
        };

        facebookButton.setOnClickListener(facebookLoginListener);

        final ImageButton getAccountsButton = (ImageButton) findViewById(R.id.get_accounts);
        if (configureEmailAutoComplete()) {
            // GET_ACCOUNTS permission is granted
            getAccountsButton.setVisibility(View.GONE);
        } else {
            getAccountsButton.setOnClickListener(new SafeViewOnClickListener() {
                @Override
                public void safeOnClick(View v) {
                    ActivityCompat.requestPermissions(RegistrationActivity2.this, new String[]{Manifest.permission
                            .GET_ACCOUNTS}, PERMISSION_REQUEST_GET_ACCOUNTS);
                }
            });
        }
    }

    private void initEnterPinStep() {
        mEnterPinEditText = (EditText) findViewById(R.id.registration_enter_pin);

        mEnterPinEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == PIN_LENGTH)
                    onPinEntered();
            }
        });

        Button activatePinButton = (Button) findViewById(R.id.registration_activate);
        activatePinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEnterPinEditText.getText().length() == PIN_LENGTH)
                    onPinEntered();
            }
        });
    }

    private void initDevicesStep() {
        final Button registerBtn = (Button) findViewById(R.id.registration_devices_register);
        final Button cancelBtn = (Button) findViewById(R.id.registration_devices_cancel);

        registerBtn.setEnabled(true);
        cancelBtn.setEnabled(true);

        registerBtn.setOnClickListener(new SafeViewOnClickListener() {
            @Override
            public void safeOnClick(View v) {
                registerBtn.setEnabled(false);
                cancelBtn.setEnabled(false);
                hideNotification();
                registerDevice();
            }
        });

        cancelBtn.setOnClickListener(new SafeViewOnClickListener() {
            @Override
            public void safeOnClick(View v) {
                registerBtn.setEnabled(false);
                cancelBtn.setEnabled(false);

                if (AppConstants.getRegistrationType() == AppConstants.REGISTRATION_TYPE_OAUTH) {
                    mWiz.proceedToPosition(3); // oauth
                } else {
                    mWiz.proceedToPosition(4); // choose login
                }
            }
        });
    }

    private void savePushNotifications(boolean enabled) {
        mWiz.setPushNotificationEnabled(enabled);
        mService.getConsentProvider().saveConsentForPushNotifications();
        final SharedPreferences options = PreferenceManager.getDefaultSharedPreferences(RegistrationActivity2.this);
        final SharedPreferences.Editor editor = options.edit();
        editor.putBoolean(MainService.PREFERENCE_PUSH_NOTIFICATIONS, enabled);
        final boolean success = editor.commit();
        L.d("savePushNotifications success: " + success);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_GET_ACCOUNTS:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    configureEmailAutoComplete(true);
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
        }
    }

    private void registerWithAccessToken(final String accessToken) {
        final String tosAge = mWiz.getTOSAge();
        final String pushNotifcationsEnabled = mWiz.getPushNotificationEnabled();
        final String timestamp = "" + mWiz.getTimestamp();
        final String deviceId = mWiz.getDeviceId();
        final String registrationId = mWiz.getRegistrationId();
        final String installId = mWiz.getInstallationId();
        // Make call to Rogerthat
        String message = getString(R.string.registration_activating_account, getString(R.string.app_name));
        final ProgressDialog progressDialog = showProgressDialog(message);
        final SafeRunnable showErrorDialog = new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                T.UI();
                progressDialog.dismiss();
                UIUtils.showDialog(RegistrationActivity2.this, null, R.string.registration_facebook_error);
            }
        };

        runOnWorker(new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                T.REGISTRATION();
                String version = "3";
                String signature = SecurityUtils.sha256(version + " " + installId + " " + timestamp + " " + deviceId + " "
                        + registrationId + " " + accessToken + CloudConstants.REGISTRATION_MAIN_SIGNATURE);

                HttpPost httppost = HTTPUtil.getHttpPost(mService, CloudConstants.REGISTRATION_FACEBOOK_URL);
                try {
                    List<NameValuePair> nameValuePairs = HTTPUtil.getRegistrationFormParams(mService);
                    nameValuePairs.add(new BasicNameValuePair("version", version));
                    nameValuePairs.add(new BasicNameValuePair("registration_time", timestamp));
                    nameValuePairs.add(new BasicNameValuePair("device_id", deviceId));
                    nameValuePairs.add(new BasicNameValuePair("registration_id", registrationId));
                    nameValuePairs.add(new BasicNameValuePair("signature", signature));
                    nameValuePairs.add(new BasicNameValuePair("install_id", installId));
                    nameValuePairs.add(new BasicNameValuePair("access_token", accessToken));
                    nameValuePairs.add(new BasicNameValuePair("tos_age", tosAge));
                    nameValuePairs.add(new BasicNameValuePair("push_notifications_enabled", pushNotifcationsEnabled));
                    nameValuePairs.add(new BasicNameValuePair("GCM_registration_id", getGCMRegistrationId()));

                    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                    // Execute HTTP Post Request
                    HttpResponse response = mHttpClient.execute(httppost);

                    int statusCode = response.getStatusLine().getStatusCode();
                    HttpEntity entity = response.getEntity();

                    if (entity == null) {
                        runOnUI(showErrorDialog);
                        return;
                    }

                    @SuppressWarnings("unchecked")
                    final Map<String, Object> responseMap = (Map<String, Object>) JSONValue
                            .parse(new InputStreamReader(entity.getContent()));

                    if (statusCode != 200 || responseMap == null) {
                        if (statusCode == 500 && responseMap != null) {
                            final String errorMessage = (String) responseMap.get("error");
                            if (errorMessage != null) {
                                runOnUI(new SafeRunnable() {
                                    @Override
                                    protected void safeRun() throws Exception {
                                        T.UI();
                                        progressDialog.dismiss();
                                        UIUtils.showDialog(RegistrationActivity2.this, null, errorMessage);
                                    }
                                });
                                return;
                            }
                        }
                        runOnUI(showErrorDialog);
                        return;
                    }
                    Boolean hasDevices = (Boolean) responseMap.get("has_devices");
                    final String email = (String) responseMap.get("email");
                    if (hasDevices) {
                        final JSONArray deviceNames = (JSONArray) responseMap.get("device_names");
                        runOnUI(new SafeRunnable() {
                            @Override
                            protected void safeRun() throws Exception {
                                T.UI();
                                progressDialog.dismiss();
                                hideNotification();
                                mWiz.setEmail(email);
                                mWiz.setDeviceNames(deviceNames);
                                mWiz.proceedToPosition(6); // devices
                            }
                        });
                    } else {
                        JSONObject account = (JSONObject) responseMap.get("account");
                        setAgeAndGenderSet((Boolean) responseMap.get("age_and_gender_set"));

                        final RegistrationInfo info = new RegistrationInfo(email, new Credentials((String) account
                                .get("account"), (String) account.get("password")));
                        runOnUI(new SafeRunnable() {
                            @Override
                            protected void safeRun() throws Exception {
                                T.UI();
                                mWiz.setEmail(email);
                                mWiz.save();
                                hideNotification();
                                tryConnect(
                                        progressDialog,
                                        1,
                                        getString(R.string.registration_establish_connection, email,
                                                getString(R.string.app_name)) + " ", info);
                            }
                        });
                    }
                } catch (Exception e) {
                    L.d(e);
                    runOnUI(showErrorDialog);
                }
            }
        });
    }

    private ProgressDialog showProgressDialog(String message) {
        return UIUtils.showProgressDialog(RegistrationActivity2.this, null, message, true, false);
    }

    private void getOauthRegistrationInfo() {
        final String timestamp = "" + mWiz.getTimestamp();
        final String deviceId = mWiz.getDeviceId();
        final String registrationId = mWiz.getRegistrationId();
        final String installId = mWiz.getInstallationId();
        // Make call to Rogerthat
        final ProgressDialog progressDialog = showProgressDialog(getString(R.string.loading));
        final SafeRunnable showErrorDialog = new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                T.UI();
                progressDialog.dismiss();
                UIUtils.showDialog(RegistrationActivity2.this, null, R.string.registration_error);
            }
        };

        runOnWorker(new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                T.REGISTRATION();
                String version = "1";
                String signature = SecurityUtils.sha256(version + " " + installId + " " + timestamp + " " + deviceId + " "
                        + registrationId + " " + "oauth" + CloudConstants.REGISTRATION_MAIN_SIGNATURE);

                HttpPost httppost = HTTPUtil.getHttpPost(mService, CloudConstants.REGISTRATION_OAUTH_INFO_URL);
                try {
                    List<NameValuePair> nameValuePairs = HTTPUtil.getRegistrationFormParams(mService);
                    nameValuePairs.add(new BasicNameValuePair("version", version));
                    nameValuePairs.add(new BasicNameValuePair("registration_time", timestamp));
                    nameValuePairs.add(new BasicNameValuePair("device_id", deviceId));
                    nameValuePairs.add(new BasicNameValuePair("registration_id", registrationId));
                    nameValuePairs.add(new BasicNameValuePair("signature", signature));
                    nameValuePairs.add(new BasicNameValuePair("install_id", installId));
                    nameValuePairs.add(new BasicNameValuePair("GCM_registration_id", getGCMRegistrationId()));

                    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                    // Execute HTTP Post Request
                    HttpResponse response = mHttpClient.execute(httppost);

                    int statusCode = response.getStatusLine().getStatusCode();
                    HttpEntity entity = response.getEntity();

                    if (entity == null) {
                        runOnUI(showErrorDialog);
                        return;
                    }

                    @SuppressWarnings("unchecked")
                    final Map<String, Object> responseMap = (Map<String, Object>) JSONValue
                            .parse(new InputStreamReader(entity.getContent()));

                    if (statusCode != 200 || responseMap == null) {
                        if (statusCode == 500 && responseMap != null) {
                            final String errorMessage = (String) responseMap.get("error");
                            if (errorMessage != null) {
                                runOnUI(new SafeRunnable() {
                                    @Override
                                    protected void safeRun() throws Exception {
                                        T.UI();
                                        progressDialog.dismiss();
                                        UIUtils.showDialog(RegistrationActivity2.this, null, errorMessage);
                                    }
                                });
                                return;
                            }
                        }
                        runOnUI(showErrorDialog);
                        return;
                    }

                    progressDialog.dismiss();

                    final String authorizeUrl = (String) responseMap.get("authorize_url");
                    final String scopes = (String) responseMap.get("scopes");
                    final String state = (String) responseMap.get("state");
                    final String client_id = (String) responseMap.get("client_id");

                    Intent intent = new Intent(RegistrationActivity2.this, OauthActivity.class);
                    intent.putExtra(OauthActivity.STATE, state);
                    intent.putExtra(OauthActivity.CLIENT_ID, client_id);
                    intent.putExtra(OauthActivity.OAUTH_URL, authorizeUrl);
                    intent.putExtra(OauthActivity.SCOPES, scopes);
                    intent.putExtra(OauthActivity.ALLOW_BACKPRESS, true);
                    startActivityForResult(intent, START_OAUTH_REQUEST_CODE);


                } catch (Exception e) {
                    L.d(e);
                    runOnUI(showErrorDialog);
                }
            }
        });
    }

    private void registerWithOauthCode(final String code, final String state) {
        final String tosAge = mWiz.getTOSAge();
        final String pushNotifcationsEnabled = mWiz.getPushNotificationEnabled();
        final String timestamp = "" + mWiz.getTimestamp();
        final String deviceId = mWiz.getDeviceId();
        final String registrationId = mWiz.getRegistrationId();
        final String installId = mWiz.getInstallationId();
        // Make call to Rogerthat
        final ProgressDialog progressDialog = showProgressDialog(getString(R.string.loading));
        final SafeRunnable showErrorDialog = new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                T.UI();
                progressDialog.dismiss();
                UIUtils.showDialog(RegistrationActivity2.this, null, R.string.registration_error);
            }
        };

        runOnWorker(new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                T.REGISTRATION();
                String version = "3";
                String signature = SecurityUtils.sha256(version + " " + installId + " " + timestamp + " " + deviceId + " "
                        + registrationId + " " + code + state + CloudConstants.REGISTRATION_MAIN_SIGNATURE);

                HttpPost httppost = HTTPUtil.getHttpPost(mService, CloudConstants.REGISTRATION_OAUTH_REGISTERED_URL);
                try {
                    List<NameValuePair> nameValuePairs = HTTPUtil.getRegistrationFormParams(mService);
                    nameValuePairs.add(new BasicNameValuePair("version", version));
                    nameValuePairs.add(new BasicNameValuePair("registration_time", timestamp));
                    nameValuePairs.add(new BasicNameValuePair("device_id", deviceId));
                    nameValuePairs.add(new BasicNameValuePair("registration_id", registrationId));
                    nameValuePairs.add(new BasicNameValuePair("signature", signature));
                    nameValuePairs.add(new BasicNameValuePair("install_id", installId));
                    nameValuePairs.add(new BasicNameValuePair("code", code));
                    nameValuePairs.add(new BasicNameValuePair("state", state));
                    nameValuePairs.add(new BasicNameValuePair("tos_age", tosAge));
                    nameValuePairs.add(new BasicNameValuePair("push_notifications_enabled", pushNotifcationsEnabled));
                    nameValuePairs.add(new BasicNameValuePair("GCM_registration_id", getGCMRegistrationId()));

                    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                    // Execute HTTP Post Request
                    HttpResponse response = mHttpClient.execute(httppost);

                    int statusCode = response.getStatusLine().getStatusCode();
                    HttpEntity entity = response.getEntity();

                    if (entity == null) {
                        runOnUI(showErrorDialog);
                        return;
                    }

                    @SuppressWarnings("unchecked")
                    final Map<String, Object> responseMap = (Map<String, Object>) JSONValue
                            .parse(new InputStreamReader(entity.getContent()));

                    if (statusCode != 200 || responseMap == null) {
                        if (statusCode == 500 && responseMap != null) {
                            final String errorMessage = (String) responseMap.get("error");
                            if (errorMessage != null) {
                                runOnUI(new SafeRunnable() {
                                    @Override
                                    protected void safeRun() throws Exception {
                                        T.UI();
                                        progressDialog.dismiss();
                                        UIUtils.showDialog(RegistrationActivity2.this, null, errorMessage);
                                    }
                                });
                                return;
                            }
                        }
                        runOnUI(showErrorDialog);
                        return;
                    }

                    Boolean hasDevices = (Boolean) responseMap.get("has_devices");
                    final String email = (String) responseMap.get("email");
                    if (hasDevices) {
                        final JSONArray deviceNames = (JSONArray) responseMap.get("device_names");
                        runOnUI(new SafeRunnable() {
                            @Override
                            protected void safeRun() throws Exception {
                                T.UI();
                                progressDialog.dismiss();
                                hideNotification();
                                mWiz.setEmail(email);
                                mWiz.setDeviceNames(deviceNames);
                                mWiz.proceedToPosition(6); // devices
                            }
                        });
                    } else {
                        JSONObject account = (JSONObject) responseMap.get("account");
                        setAgeAndGenderSet((Boolean) responseMap.get("age_and_gender_set"));

                        final RegistrationInfo info = new RegistrationInfo(email, new Credentials((String) account
                                .get("account"), (String) account.get("password")));
                        runOnUI(new SafeRunnable() {
                            @Override
                            protected void safeRun() throws Exception {
                                T.UI();
                                progressDialog.dismiss();
                                mWiz.setEmail(email);
                                mWiz.save();
                                hideNotification();
                                tryConnect(
                                        progressDialog,
                                        1,
                                        getString(R.string.registration_establish_connection, email,
                                                getString(R.string.app_name)) + " ", info);
                            }
                        });
                    }

                } catch (Exception e) {
                    L.d(e);
                    runOnUI(showErrorDialog);
                }
            }
        });
    }

    @Override
    public void onConfigurationChanged(android.content.res.Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        handleScreenOrientation(newConfig.orientation);
    }

    private void handleScreenOrientation(int orientation) {
        boolean isLandscape = orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE;
        for (int id : (AppConstants.FULL_WIDTH_HEADERS ? FULL_WIDTH_ROGERTHAT_LOGOS : NORMAL_WIDTH_ROGERTHAT_LOGOS)) {
            View view = findViewById(id);
            if (view != null) {
                view.setVisibility(isLandscape ? View.GONE : View.VISIBLE);
            }
        }
    }

    private boolean configureEmailAutoComplete() {
        return configureEmailAutoComplete(false);
    }

    private boolean configureEmailAutoComplete(boolean autoShowSuggestions) {
        T.UI();
        final boolean getAccountsPermissionWasGranted = mService.isPermitted(Manifest.permission.GET_ACCOUNTS);
        if (getAccountsPermissionWasGranted) {
            List<Account> accounts = new AccountManager(this).getAccounts();
            List<String> emails = new ArrayList<String>();
            for (Account account : accounts) {
                if (RegexPatterns.EMAIL.matcher(account.name).matches() && !emails.contains(account.name))
                    emails.add(account.name);
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.list_item, emails);
            mEnterEmailAutoCompleteTextView.setAdapter(adapter);
            mEnterEmailAutoCompleteTextView.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    requestPin();
                }
            });
            mEnterEmailAutoCompleteTextView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent event) {
                    mEnterEmailAutoCompleteTextView.setThreshold(1);
                    return false;
                }
            });
            if (autoShowSuggestions)
                mEnterEmailAutoCompleteTextView.showDropDown();
        }
        mEnterEmailAutoCompleteTextView.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO
                        || (event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent
                        .ACTION_DOWN)) {
                    requestPin();
                    return true;
                }
                return false;
            }
        });
        return getAccountsPermissionWasGranted;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == START_OAUTH_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                if (!TextUtils.isEmptyOrWhitespace(data.getStringExtra(OauthActivity.RESULT_CODE))) {
                    registerWithOauthCode(data.getStringExtra(OauthActivity.RESULT_CODE), data.getStringExtra(OauthActivity.RESULT_STATE));
                } else {
                    String message = data.getStringExtra(OauthActivity.RESULT_ERROR_MESSAGE);
                    UIUtils.showDialog(this, null, message);
                }
            }
        }
    }

    private void onPinEntered() {
        UIUtils.hideKeyboard(this, mEnterPinEditText);
        mEnterPinEditText.clearFocus();

        final String pin = mEnterPinEditText.getText().toString();
        // Validate pin code
        if (!RegexPatterns.PIN.matcher(pin).matches()) {
            UIUtils.showDialog(this, null, R.string.registration_invalid_pin);
            return;
        }
        // Make call to Rogerthat
        final String message = getString(R.string.registration_activating_account, getString(R.string.app_name));
        final ProgressDialog progressDialog = showProgressDialog(message);
        final SafeRunnable showErrorDialog = new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                T.UI();
                progressDialog.dismiss();
                UIUtils.showDialog(RegistrationActivity2.this, null, R.string.registration_sending_pin_error);
            }
        };
        final String email = mWiz.getEmail();
        final String tosAge = mWiz.getTOSAge();
        final String pushNotifcationsEnabled = mWiz.getPushNotificationEnabled();
        final String timestamp = "" + mWiz.getTimestamp();
        final String registrationId = mWiz.getRegistrationId();
        final String deviceId = mWiz.getDeviceId();

        runOnWorker(new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                T.REGISTRATION();
                String version = "3";
                String pinSignature = SecurityUtils.sha256(version + " " + email + " " + timestamp + " " + deviceId + " "
                        + registrationId + " " + pin + CloudConstants.REGISTRATION_PIN_SIGNATURE);

                HttpPost httppost = HTTPUtil.getHttpPost(mService, CloudConstants.REGISTRATION_PIN_URL);
                try {
                    List<NameValuePair> nameValuePairs = HTTPUtil.getRegistrationFormParams(mService);
                    nameValuePairs.add(new BasicNameValuePair("version", version));
                    nameValuePairs.add(new BasicNameValuePair("email", email));
                    nameValuePairs.add(new BasicNameValuePair("tos_age", tosAge));
                    nameValuePairs.add(new BasicNameValuePair("push_notifications_enabled", pushNotifcationsEnabled));
                    nameValuePairs.add(new BasicNameValuePair("registration_time", timestamp));
                    nameValuePairs.add(new BasicNameValuePair("device_id", deviceId));
                    nameValuePairs.add(new BasicNameValuePair("registration_id", registrationId));
                    nameValuePairs.add(new BasicNameValuePair("pin_code", pin));
                    nameValuePairs.add(new BasicNameValuePair("pin_signature", pinSignature));
                    nameValuePairs.add(new BasicNameValuePair("GCM_registration_id", getGCMRegistrationId()));

                    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                    // Execute HTTP Post Request
                    HttpResponse response = mHttpClient.execute(httppost);
                    int statusCode = response.getStatusLine().getStatusCode();
                    HttpEntity entity = response.getEntity();
                    if (statusCode != 200 || entity == null) {
                        runOnUI(showErrorDialog);
                        return;
                    }
                    @SuppressWarnings("unchecked")
                    final Map<String, Object> responseMap = (Map<String, Object>) org.json.simple.JSONValue
                            .parse(new InputStreamReader(entity.getContent()));
                    if ("success".equals(responseMap.get("result"))) {
                        Boolean hasDevices = (Boolean) responseMap.get("has_devices");
                        if (hasDevices) {
                            final JSONArray deviceNames = (JSONArray) responseMap.get("device_names");
                            runOnUI(new SafeRunnable() {
                                @Override
                                protected void safeRun() throws Exception {
                                    T.UI();
                                    progressDialog.dismiss();
                                    hideNotification();
                                    mWiz.setDeviceNames(deviceNames);
                                    mWiz.proceedToPosition(6); // devices
                                }
                            });
                        } else {
                                JSONObject account = (JSONObject) responseMap.get("account");
                                final RegistrationInfo info = new RegistrationInfo(email, new Credentials((String) account
                                        .get("account"), (String) account.get("password")));

                                setAgeAndGenderSet((Boolean) responseMap.get("age_and_gender_set"));
                                runOnUI(new SafeRunnable() {
                                    @Override
                                    protected void safeRun() throws Exception {
                                        T.UI();
                                        hideNotification();
                                        tryConnect(
                                                progressDialog,
                                                1,
                                                getString(R.string.registration_establish_connection, email,
                                                        getString(R.string.app_name)) + " ", info);
                                    }
                                });
                        }
                    } else {

                        final long attempts_left = (Long) responseMap.get("attempts_left");
                        runOnUI(new SafeRunnable() {
                            @Override
                            protected void safeRun() throws Exception {
                                T.UI();
                                progressDialog.dismiss();
                                if (attempts_left > 0) {
                                    String title = getString(R.string.registration_incorrect_pin_dialog_title);
                                    String message = getString(R.string.registration_incorrect_pin, email);
                                    UIUtils.showDialog(RegistrationActivity2.this, title, message);
                                    mEnterPinEditText.setText("");
                                } else {
                                    hideNotification();
                                    String message = getString(R.string.registration_no_attempts_left);
                                    String positiveCaption = getString(R.string.try_again);
                                    UIUtils.showDialog(RegistrationActivity2.this, null, message, positiveCaption, null, null, null);
                                    mWiz.reInit();
                                    mWiz.goBackToPrevious();
                                }
                            }
                        });

                    }
                } catch (Exception e) {
                    L.d(e);
                    runOnUI(showErrorDialog);
                }
            }
        });

    }

    private void requestPin() {
        final String email = mEnterEmailAutoCompleteTextView.getText().toString().toLowerCase().trim();

        // Validate input
        if (!RegexPatterns.EMAIL.matcher(email).matches()) {
            UIUtils.showDialog(RegistrationActivity2.this, null, R.string.registration_email_not_valid);
            return;
        }
        // Check network connectivity
        if (!mService.getNetworkConnectivityManager().isConnected()) {
            UIUtils.showNoNetworkDialog(this);
            return;
        }
        UIUtils.hideKeyboard(this, mEnterEmailAutoCompleteTextView);
        sendRegistrationRequest(email);
    }

    private void handleEnterEmail() {
        T.UI();
        final Button continueButton = (Button) findViewById(R.id.login_via_email);
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View button) {
                T.UI();
                requestPin();
            }
        });
    }

    private void addStartHandler() {
        mWiz.addPageHandler(new Wizard.PageHandler() {

            @Override
            public void pageDisplayed(Button back, Button next, ViewFlipper switcher) {
            }

            @Override
            public String getTitle() {
                return null;
            }

            @Override
            public boolean beforeNextClicked(Button back, Button next, ViewFlipper switcher) {
                return false;
            }

            @Override
            public boolean beforeBackClicked(Button back, Button next, ViewFlipper switcher) {
                return false;
            }
        });
    }

    private void addAgreeTOSHandler() {
        mWiz.addPageHandler(new Wizard.PageHandler() {

            @Override
            public void pageDisplayed(Button back, Button next, ViewFlipper switcher) {
                if (CloudConstants.isEnterpriseApp()) {
                    mWiz.proceedToNextPage();
                } else if (!mService.getNetworkConnectivityManager().isConnected()) {
                    int message = R.string.no_internet_connection_try_again;
                    UIUtils.showDialog(RegistrationActivity2.this, null, message);
                } else {
                    mTOSWebview = (WebView) findViewById(R.id.tos_webview);
                    mTOSWebview.loadUrl(AppConstants.TERMS_OF_SERVICE_URL);
                }
            }

            @Override
            public String getTitle() {
                return null;
            }

            @Override
            public boolean beforeNextClicked(Button back, Button next, ViewFlipper switcher) {
                if (mTOSWebview != null) {
                    mTOSWebview.loadUrl("about:blank");
                }
                return false;
            }

            @Override
            public boolean beforeBackClicked(Button back, Button next, ViewFlipper switcher) {
                return false;
            }
        });
    }

    private void addNotificationsHandler() {
        mWiz.addPageHandler(new Wizard.PageHandler() {

            @Override
            public void pageDisplayed(Button back, Button next, ViewFlipper switcher) {
            }

            @Override
            public String getTitle() {
                return null;
            }

            @Override
            public boolean beforeNextClicked(Button back, Button next, ViewFlipper switcher) {
                return false;
            }

            @Override
            public boolean beforeBackClicked(Button back, Button next, ViewFlipper switcher) {
                return false;
            }
        });
    }

    @SuppressWarnings("all")
    private void addChooseLoginMethodHandler() {
        mWiz.addPageHandler(new Wizard.PageHandler() {

            @Override
            public void pageDisplayed(Button back, Button next, ViewFlipper switcher) {
                mEnterEmailAutoCompleteTextView.setThreshold(1000); // Prevent popping up automatically

                if (AppConstants.FACEBOOK_APP_ID == null || !AppConstants.FACEBOOK_REGISTRATION) {
                    LinearLayout orLaylout = (LinearLayout) findViewById(R.id.or);
                    orLaylout.setVisibility(View.INVISIBLE);
                    Button facebookButton = (Button) findViewById(R.id.login_via_fb);
                    facebookButton.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public String getTitle() {
                return null;
            }

            @Override
            public boolean beforeNextClicked(Button back, Button next, ViewFlipper switcher) {
                return false;
            }

            @Override
            public boolean beforeBackClicked(Button back, Button next, ViewFlipper switcher) {
                return false;
            }
        });
    }

    private void addOauthMethodHandler() {
        mWiz.addPageHandler(new Wizard.PageHandler() {

            @Override
            public void pageDisplayed(Button back, Button next, ViewFlipper switcher) {
                if (AppConstants.getRegistrationType() == AppConstants.REGISTRATION_TYPE_OAUTH) {
                    mEnterEmailAutoCompleteTextView.setThreshold(1000); // Prevent popping up automatically
                } else {
                    mWiz.proceedToNextPage();
                }
            }

            @Override
            public String getTitle() {
                return null;
            }

            @Override
            public boolean beforeNextClicked(Button back, Button next, ViewFlipper switcher) {
                return false;
            }

            @Override
            public boolean beforeBackClicked(Button back, Button next, ViewFlipper switcher) {
                return false;
            }
        });
    }

    private void addEnterPinHandler() {
        mWiz.addPageHandler(new Wizard.PageHandler() {
            @Override
            public void pageDisplayed(Button back, Button next, ViewFlipper switcher) {
                mEnterPinEditText.setText("");
                mEnterEmailAutoCompleteTextView.setThreshold(1000); // Prevent popping up automatically

                ((TextView) findViewById(R.id.registration_pin_was_mailed)).setText(getString(R.string
                        .registration_activation_code_was_mailed, mWiz.getEmail()));
            }

            @Override
            public String getTitle() {
                return null;
            }

            @Override
            public boolean beforeNextClicked(Button back, Button next, ViewFlipper switcher) {
                return true;
            }

            @Override
            public boolean beforeBackClicked(Button back, Button next, ViewFlipper switcher) {
                return true;
            }
        });
    }

    private void addRegisterDeviceHandler() {
        mWiz.addPageHandler(new Wizard.PageHandler() {
            @Override
            public void pageDisplayed(Button back, Button next, ViewFlipper switcher) {
                mEnterEmailAutoCompleteTextView.setThreshold(1000); // Prevent popping up automatically
                final FSListView deviceList = (FSListView) findViewById(R.id.devices_list);
                final Button registerBtn = (Button) findViewById(R.id.registration_devices_register);
                final Button cancelBtn = (Button) findViewById(R.id.registration_devices_cancel);

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(RegistrationActivity2.this, R.layout.list_item, mWiz.getDeviceNames());
                deviceList.setAdapter(adapter);

                registerBtn.setEnabled(true);
                cancelBtn.setEnabled(true);

                ((TextView) findViewById(R.id.registration_devices_text)).setText(getString(R.string.device_unregister_others));
            }

            @Override
            public String getTitle() {
                return null;
            }

            @Override
            public boolean beforeNextClicked(Button back, Button next, ViewFlipper switcher) {
                return false;
            }

            @Override
            public boolean beforeBackClicked(Button back, Button next, ViewFlipper switcher) {
                return false;
            }
        });
    }

    private void setFinishHandler() {
        mWiz.setOnFinish(new SafeRunnable() {

            private void showPopup(Configuration cfg) {
                startMainActivity(false);
            }

            @Override
            protected void safeRun() throws Exception {
                T.UI();
                if (mEnterPinEditText != null) {
                    InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    mgr.hideSoftInputFromWindow(mEnterPinEditText.getWindowToken(), 0);
                }

                ConfigurationProvider configProvider = mService.getConfigurationProvider();
                Configuration cfg = configProvider.getConfiguration(RegistrationWizard2.CONFIGKEY);

                if (cfg != null && cfg.get(INVITOR_SECRET_CONFIGKEY, null) != null
                        && cfg.get(INVITOR_CODE_CONFIGKEY, null) != null) {
                    // User pressed an invitation link with secret
                    startMainActivity(true);
                } else {
                    showPopup(cfg);
                }
            }
        });
    }

    @Override
    protected void onServiceUnbound() {

    }

    private void sendRegistrationRequest(final String email) {
        final ProgressDialog progressDialog = showProgressDialog(getString(R.string.registration_sending_email, email));
        final SafeRunnable showErrorDialog = new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                T.UI();
                progressDialog.dismiss();
                UIUtils.showErrorPleaseRetryDialog(RegistrationActivity2.this);
            }
        };
        mWiz.reInit();
        final String timestamp = "" + mWiz.getTimestamp();
        final String registrationId = mWiz.getRegistrationId();
        final String deviceId = mWiz.getDeviceId();

        runOnWorker(new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                T.REGISTRATION();
                String version = "2";
                String requestSignature = SecurityUtils.sha256(version + email + " " + timestamp + " " + deviceId + " "
                    + registrationId + " " + CloudConstants.REGISTRATION_EMAIL_SIGNATURE);

                HttpPost httppost = HTTPUtil.getHttpPost(mService, CloudConstants.REGISTRATION_REQUEST_URL);
                try {
                    List<NameValuePair> nameValuePairs = HTTPUtil.getRegistrationFormParams(mService);
                    nameValuePairs.add(new BasicNameValuePair("version", version));
                    nameValuePairs.add(new BasicNameValuePair("email", email));
                    nameValuePairs.add(new BasicNameValuePair("registration_time", timestamp));
                    nameValuePairs.add(new BasicNameValuePair("device_id", deviceId));
                    nameValuePairs.add(new BasicNameValuePair("registration_id", registrationId));
                    nameValuePairs.add(new BasicNameValuePair("request_signature", requestSignature));
                    nameValuePairs.add(new BasicNameValuePair("install_id", mWiz.getInstallationId()));
                    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                    // Execute HTTP Post Request
                    HttpResponse response = mHttpClient.execute(httppost);

                    int statusCode = response.getStatusLine().getStatusCode();
                    if (statusCode == 200) {
                        runOnUI(new SafeRunnable() {
                            @Override
                            protected void safeRun() throws Exception {
                                T.UI();
                                mWiz.setEmail(email);
                                mWiz.save();
                                progressDialog.dismiss();
                                mWiz.proceedToNextPage();
                                showNotification();
                            }
                        });
                    } else if (statusCode == 502) {

                        final HttpEntity entity = response.getEntity();
                        runOnUI(new SafeRunnable() {
                            @Override
                            protected void safeRun() throws Exception {
                                T.UI();
                                progressDialog.dismiss();
                                String message = null;
                                if (entity != null) {
                                    @SuppressWarnings("unchecked")
                                    final Map<String, Object> responseMap = (Map<String, Object>) org.json.simple
                                            .JSONValue
                                            .parse(new InputStreamReader(entity.getContent()));

                                    if (responseMap != null) {
                                        String result = (String) responseMap.get("result");
                                        if (result != null) {
                                            message = result;
                                        }
                                    }
                                }

                                if (message == null) {
                                    message = getString(R.string.registration_email_not_valid);
                                }
                                UIUtils.showDialog(RegistrationActivity2.this, null, message);
                            }
                        });
                    } else {
                        runOnUI(showErrorDialog);
                    }

                } catch (ClientProtocolException e) {
                    L.d(e);
                    runOnUI(showErrorDialog);
                } catch (IOException e) {
                    L.d(e);
                    runOnUI(showErrorDialog);
                }
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            int position = mWiz.getPosition();
            // 5 == Enter activation code
            if (position == 5) {
                mWiz.goBack();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}

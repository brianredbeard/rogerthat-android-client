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

package com.mobicage.rogerthat.registration;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.Installation;
import com.mobicage.rogerthat.OauthActivity;
import com.mobicage.rogerthat.util.GoogleServicesUtils;
import com.mobicage.rogerthat.util.GoogleServicesUtils.GCMRegistrationIdFoundCallback;
import com.mobicage.rogerthat.util.TextUtils;
import com.mobicage.rogerthat.util.security.SecurityUtils;
import com.mobicage.rogerthat.util.http.HTTPUtil;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.ui.UIUtils;
import com.mobicage.rpc.Credentials;
import com.mobicage.rpc.config.AppConstants;
import com.mobicage.rpc.config.CloudConstants;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OauthRegistrationActivity extends AbstractRegistrationActivity {

    private static final int HTTP_RETRY_COUNT = 3;
    private static final int HTTP_TIMEOUT = 10000;


    private OauthRegistrationWizard mWiz;
    private HttpClient mHttpClient;
    private ProgressDialog mProgressDialog = null;

    private TextView mErrorTextView;
    private static final int START_OAUTH_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        T.UI();
        init(this);
        mHttpClient = HTTPUtil.getHttpClient(HTTP_TIMEOUT, HTTP_RETRY_COUNT);

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
    };

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mService != null && mService.getRegisteredFromConfig()) {
            finish();
        }
    }

    @Override
    protected void onStop() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        closeWorkerThread();
        super.onDestroy();
    }

    @Override
    protected void onServiceBound() {
        T.UI();
        setContentViewWithoutNavigationBar(R.layout.registration_full_oauth);

        mWiz = OauthRegistrationWizard.getWizard(mService, Installation.id(this));
        setWizard(mWiz);

        if (CloudConstants.USE_GCM_KICK_CHANNEL && GoogleServicesUtils.checkPlayServices(this, true)) {
            GoogleServicesUtils.registerGCMRegistrationId(mService, new GCMRegistrationIdFoundCallback() {
                @Override
                public void idFound(String registrationId) {
                    setGCMRegistrationId(registrationId);
                }
            });
        }

        mErrorTextView = (TextView) findViewById(R.id.error_text);
        findViewById(R.id.retry).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openOauthActivity();
            }
        });
        setFinishHandler();

        openOauthActivity();
    }

    @Override
    protected void onServiceUnbound() {
    }

    private void openOauthActivity() {
        if (!mService.getNetworkConnectivityManager().isConnected()) {
            mErrorTextView.setText(R.string.registration_screen_instructions_check_network_not_available);
            UIUtils.showNoNetworkDialog(this);
            return;
        }
        mErrorTextView.setText(R.string.loading);
        Intent intent = new Intent(OauthRegistrationActivity.this, OauthActivity.class);
        intent.putExtra(OauthActivity.OAUTH_URL, AppConstants.REGISTRATION_TYPE_OAUTH_URL);
        intent.putExtra(OauthActivity.BUILD_URL, false);
        intent.putExtra(OauthActivity.ALLOW_BACKPRESS, false);
        startActivityForResult(intent, START_OAUTH_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        T.UI();
        if (requestCode == START_OAUTH_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                if (!TextUtils.isEmptyOrWhitespace(data.getStringExtra(OauthActivity.RESULT_CODE))) {
                    registerWithOauthCode(data.getStringExtra(OauthActivity.RESULT_CODE), data.getStringExtra(OauthActivity.RESULT_STATE));
                } else {
                    String errorMessage = data.getStringExtra(OauthActivity.RESULT_ERROR_MESSAGE);
                    mErrorTextView.setText(errorMessage);
                    UIUtils.showDialog(OauthRegistrationActivity.this, null, errorMessage);
                }
            }
        }
    }

    private void setFinishHandler() {
        mWiz.setOnFinish(new SafeRunnable() {

            @Override
            protected void safeRun() throws Exception {
                T.UI();
                startMainActivity(false);
            }
        });
    }

    private void registerWithOauthCode(final String code, final String state) {
        final String timestamp = "" + mWiz.getTimestamp();
        final String deviceId = mWiz.getDeviceId();
        final String registrationId = mWiz.getRegistrationId();
        final String installId = mWiz.getInstallationId();
        // Make call to Rogerthat
        final ProgressDialog progressDialog = UIUtils.showProgressDialog(OauthRegistrationActivity.this, null,
                getString(R.string.loading), true, false);
        final SafeRunnable showErrorDialog = new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                T.UI();
                progressDialog.dismiss();
                UIUtils.showDialog(OauthRegistrationActivity.this, null, R.string.registration_error);
            }
        };

        runOnWorker(new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                T.REGISTRATION();
                String version = "1";
                String signature = SecurityUtils.sha256(version + " " + installId + " " + timestamp + " " + deviceId + " "
                        + registrationId + " " + code + state + CloudConstants.REGISTRATION_MAIN_SIGNATURE);

                HttpPost httppost = new HttpPost(CloudConstants.REGISTRATION_OAUTH_REGISTERED_URL);
                try {
                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(8);
                    nameValuePairs.add(new BasicNameValuePair("version", version));
                    nameValuePairs.add(new BasicNameValuePair("registration_time", timestamp));
                    nameValuePairs.add(new BasicNameValuePair("device_id", deviceId));
                    nameValuePairs.add(new BasicNameValuePair("registration_id", registrationId));
                    nameValuePairs.add(new BasicNameValuePair("signature", signature));
                    nameValuePairs.add(new BasicNameValuePair("platform", "android"));
                    nameValuePairs.add(new BasicNameValuePair("install_id", installId));
                    nameValuePairs.add(new BasicNameValuePair("language", Locale.getDefault().getLanguage()));
                    nameValuePairs.add(new BasicNameValuePair("country", Locale.getDefault().getCountry()));
                    nameValuePairs.add(new BasicNameValuePair("code", code));
                    nameValuePairs.add(new BasicNameValuePair("state", state));
                    nameValuePairs.add(new BasicNameValuePair("app_id", CloudConstants.APP_ID));
                    nameValuePairs.add(new BasicNameValuePair("use_xmpp_kick", CloudConstants.USE_XMPP_KICK_CHANNEL
                            + ""));
                    nameValuePairs.add(new BasicNameValuePair("GCM_registration_id", getGCMRegistrationId()));

                    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                    // Execute HTTP Post Request
                    HttpResponse response = mHttpClient.execute(httppost);

                    int statusCode = response.getStatusLine().getStatusCode();
                    HttpEntity entity = response.getEntity();

                    if (entity == null) {
                        runOnUI(new SafeRunnable() {
                            @Override
                            protected void safeRun() throws Exception {
                                mErrorTextView.setText(R.string.registration_error);
                                showErrorDialog.run();
                            }
                        });
                        return;
                    }

                    @SuppressWarnings("unchecked")
                    final Map<String, Object> responseMap = (Map<String, Object>) JSONValue
                            .parse(new InputStreamReader(entity.getContent()));


                    if (statusCode != 200 || responseMap == null) {

                        if (statusCode == 500 && responseMap != null) {
                            final String errorMessage = (String) responseMap.get("error");
                            if (errorMessage != null) {
                                progressDialog.dismiss();
                                runOnUI(new SafeRunnable() {
                                    @Override
                                    protected void safeRun() throws Exception {
                                        T.UI();
                                        mErrorTextView.setText(errorMessage);
                                        UIUtils.showDialog(OauthRegistrationActivity.this, null, errorMessage);
                                    }
                                });
                                return;
                            }
                        }
                        runOnUI(new SafeRunnable() {
                            @Override
                            protected void safeRun() throws Exception {
                                mErrorTextView.setText(R.string.registration_error);
                                showErrorDialog.run();
                            }
                        });
                        return;
                    }

                    JSONObject account = (JSONObject) responseMap.get("account");
                    final String email = (String) responseMap.get("email");
                    setAgeAndGenderSet((Boolean) responseMap.get("age_and_gender_set"));
                    final RegistrationInfo info = new RegistrationInfo(email, new Credentials((String) account
                            .get("account"), (String) account.get("password")));
                    runOnUI(new SafeRunnable() {
                        @Override
                        protected void safeRun() throws Exception {
                            T.UI();
                            mWiz.setEmail(email);
                            mWiz.save();
                            tryConnect(
                                    progressDialog,
                                    1,
                                    getString(R.string.registration_establish_connection, email,
                                            getString(R.string.app_name)) + " ", info);
                        }
                    });

                } catch (Exception e) {
                    L.d(e);
                    runOnUI(new SafeRunnable() {
                        @Override
                        protected void safeRun() throws Exception {
                            mErrorTextView.setText(R.string.registration_error);
                            showErrorDialog.run();
                        }
                    });
                }
            }
        });
    }
}

/*
 * Copyright 2017 Mobicage NV
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
 * @@license_version:1.2@@
 */
package com.mobicage.rogerthat.registration;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.Installation;
import com.mobicage.rogerthat.MainActivity;
import com.mobicage.rogerthat.MainService;
import com.mobicage.rogerthat.config.ConfigurationProvider;
import com.mobicage.rogerthat.plugins.friends.Friend;
import com.mobicage.rogerthat.plugins.friends.FriendsPlugin;
import com.mobicage.rogerthat.plugins.messaging.BrandingMgr;
import com.mobicage.rogerthat.util.GoogleServicesUtils;
import com.mobicage.rogerthat.util.GoogleServicesUtils.GCMRegistrationIdFoundCallback;
import com.mobicage.rogerthat.util.Security;
import com.mobicage.rogerthat.util.http.HTTPUtil;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeBroadcastReceiver;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.ui.Pausable;
import com.mobicage.rogerthat.util.ui.UIUtils;
import com.mobicage.rpc.Credentials;
import com.mobicage.rpc.config.CloudConstants;
import com.mobicage.rpc.newxmpp.XMPPConfigurationFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.Logger;
import org.jivesoftware.smack.XMPPConnection;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class YSAAARegistrationActivity extends AbstractRegistrationActivity {

    private static final int XMPP_CHECK_DELAY_MILLIS = 5000;
    private static final int XMPP_MAX_NUM_ATTEMPTS = 8;
    private static final int HTTP_RETRY_COUNT = 3;
    private static final int HTTP_TIMEOUT = 10000;

    private YSAAARegistrationWizard mWiz;
    private HttpClient mHttpClient;

    private TextView mStatusLbl;
    private ProgressBar mProgressBar;
    private Button mRetryBtn;
    private Timer mTimer;

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
    protected void onDestroy() {
        closeWorkerThread();
        super.onDestroy();
    }

    @Override
    protected void onServiceBound() {
        setContentView(R.layout.ysaaa_registration);

        mWiz = YSAAARegistrationWizard.getWizard(mService, Installation.id(this));
        setWizard(mWiz);
        if (CloudConstants.USE_GCM_KICK_CHANNEL && GoogleServicesUtils.checkPlayServices(this, true)) {
            GoogleServicesUtils.registerGCMRegistrationId(mService, new GCMRegistrationIdFoundCallback() {
                @Override
                public void idFound(String registrationId) {
                    setGCMRegistrationId(registrationId);
                }
            });
        }

        mStatusLbl = (TextView) findViewById(R.id.status);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mTimer = new Timer(false);
        mRetryBtn = (Button) findViewById(R.id.retry_button);
        mRetryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });

        final String[] receivingIntents = new String[] { FriendsPlugin.FRIENDS_LIST_REFRESHED,
            FriendsPlugin.FRIEND_UPDATE_INTENT, FriendsPlugin.FRIEND_ADDED_INTENT,
            BrandingMgr.SERVICE_BRANDING_AVAILABLE_INTENT, BrandingMgr.GENERIC_BRANDING_AVAILABLE_INTENT };

        IntentFilter filter = new IntentFilter();
        for (String action : receivingIntents)
            filter.addAction(action);
        registerReceiver(new SafeBroadcastReceiver() {
            @Override
            public String[] onSafeReceive(Context context, Intent intent) {
                T.UI();
                launchMainActivityAndFinishIfAppReady();
                return receivingIntents;
            };
        }, filter);

        register();
    }

    private void register() {
        mStatusLbl.setText(R.string.initializing);
        mProgressBar.setProgress(0);
        mProgressBar.setVisibility(View.VISIBLE);

        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                runOnUI(new SafeRunnable() {
                    @Override
                    protected void safeRun() throws Exception {
                        T.UI();
                        mProgressBar.setProgress(mProgressBar.getProgress() + 1);
                    }
                });
            }
        };
        mTimer.scheduleAtFixedRate(timerTask, 250, 250);

        mRetryBtn.setVisibility(View.GONE);
        if (!mService.getNetworkConnectivityManager().isConnected()) {
            mTimer.cancel();
            mProgressBar.setVisibility(View.GONE);
            mStatusLbl.setText(R.string.registration_screen_instructions_check_network_not_available);
            mRetryBtn.setVisibility(View.VISIBLE);
            UIUtils.showNoNetworkDialog(this);
            return;
        }

        final SafeRunnable showErrorDialog = new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                UIUtils.showErrorPleaseRetryDialog(YSAAARegistrationActivity.this);
                mTimer.cancel();
                mProgressBar.setVisibility(View.GONE);
                mStatusLbl.setText(R.string.error_please_try_again);
                mRetryBtn.setVisibility(View.VISIBLE);
            }
        };

        final String timestamp = "" + mWiz.getTimestamp();
        final String deviceId = mWiz.getDeviceId();
        final String registrationId = mWiz.getRegistrationId();
        final String installId = mWiz.getInstallationId();

        runOnWorker(new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                T.REGISTRATION();
                String version = "1";
                String signature = Security.sha256(version + " " + installId + " " + timestamp + " " + deviceId + " "
                    + registrationId + " " + CloudConstants.APP_SERVICE_GUID + CloudConstants.REGISTRATION_MAIN_SIGNATURE);

                HttpPost httppost = new HttpPost(CloudConstants.REGISTRATION_YSAAA_URL);
                try {
                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(13);
                    nameValuePairs.add(new BasicNameValuePair("version", version));
                    nameValuePairs.add(new BasicNameValuePair("platform", "android"));
                    nameValuePairs.add(new BasicNameValuePair("registration_time", timestamp));
                    nameValuePairs.add(new BasicNameValuePair("device_id", deviceId));
                    nameValuePairs.add(new BasicNameValuePair("registration_id", registrationId));
                    nameValuePairs.add(new BasicNameValuePair("signature", signature));
                    nameValuePairs.add(new BasicNameValuePair("install_id", installId));
                    nameValuePairs.add(new BasicNameValuePair("service", CloudConstants.APP_SERVICE_GUID));
                    nameValuePairs.add(new BasicNameValuePair("language", Locale.getDefault().getLanguage()));
                    nameValuePairs.add(new BasicNameValuePair("country", Locale.getDefault().getCountry()));
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
                                        UIUtils.showDialog(YSAAARegistrationActivity.this, null, errorMessage);

                                        mTimer.cancel();
                                        mProgressBar.setVisibility(View.GONE);
                                        mStatusLbl.setText(errorMessage);
                                        mRetryBtn.setVisibility(View.VISIBLE);
                                    }
                                });
                                return;
                            }
                        }
                        runOnUI(showErrorDialog);
                        return;
                    }
                    JSONObject account = (JSONObject) responseMap.get("account");
                    final String email = (String) responseMap.get("email");
                    final RegistrationInfo info = new RegistrationInfo(email, new Credentials((String) account
                        .get("account"), (String) account.get("password")));
                    runOnUI(new SafeRunnable() {
                        @Override
                        protected void safeRun() throws Exception {
                            T.UI();
                            mWiz.setEmail(email);
                            mWiz.save();
                            tryConnect(
                                1,
                                getString(R.string.registration_establish_connection, email,
                                    getString(R.string.app_name)) + " ", info);
                        }
                    });

                } catch (Exception e) {
                    L.d(e);
                    runOnUI(showErrorDialog);
                }
            }
        });
    }

    private void tryConnect(final int attempt, final String statusMessage, final RegistrationInfo info) {
        T.UI();
        final Pausable pausable = this;

        if (attempt > XMPP_MAX_NUM_ATTEMPTS) {
            mTimer.cancel();
            mProgressBar.setVisibility(View.GONE);
            mStatusLbl.setText(R.string.registration_error);
            mRetryBtn.setVisibility(View.VISIBLE);
            UIUtils.showDialog(YSAAARegistrationActivity.this, null, R.string.registration_error);
            mWiz.reInit();
            return;
        }
        L.d("Registration attempt #" + attempt);

        final String xmppServiceName = info.mCredentials.getXmppServiceName();
        final String xmppAccount = info.mCredentials.getXmppAccount();
        final String xmppPassword = info.mCredentials.getPassword();

        final ConfigurationProvider cp = mService.getConfigurationProvider();
        Runnable runnable = new SafeRunnable() {

            @Override
            public void safeRun() {
                T.REGISTRATION();
                try {
                    if (CloudConstants.USE_XMPP_KICK_CHANNEL) {
                        final ConnectionConfiguration xmppConfig = new XMPPConfigurationFactory(cp,
                            mService.getNetworkConnectivityManager(), null)
                            .getSafeXmppConnectionConfiguration(xmppServiceName);
                        final XMPPConnection xmppCon = new XMPPConnection(xmppConfig);

                        xmppCon.setLogger(new Logger() {
                            @Override
                            public void log(String message) {
                                L.d(message);
                            }
                        });
                        xmppCon.connect();
                        xmppCon.login(xmppAccount, xmppPassword);

                        final Thread t2 = new Thread(new SafeRunnable() {
                            @Override
                            protected void safeRun() throws Exception {
                                L.d("REG Before disconnect (on separate thread) - xmpp-" + xmppCon.hashCode());
                                xmppCon.disconnect();
                                L.d("REG After disconnect (on separate thread) - xmpp-" + xmppCon.hashCode());
                            }
                        });
                        t2.setDaemon(true);
                        t2.start();
                    }

                    postFinishRegistration(info.mCredentials.getUsername(), info.mCredentials.getPassword(),
                        CloudConstants.APP_SERVICE_GUID, null);

                    runOnUI(new SafeRunnable(pausable) {

                        @Override
                        protected void safeRun() throws Exception {
                            T.UI();
                            mWiz.setCredentials(info.mCredentials);

                            if (CloudConstants.USE_GCM_KICK_CHANNEL && !"".equals(getGCMRegistrationId())) {
                                GoogleServicesUtils.saveGCMRegistrationId(mService, getGCMRegistrationId());
                            }

                            mService.setCredentials(mWiz.getCredentials());
                            mService.setRegisteredInConfig(true);
                            final Intent launchServiceIntent = new Intent(YSAAARegistrationActivity.this,
                                MainService.class);
                            launchServiceIntent.putExtra(MainService.START_INTENT_JUST_REGISTERED, true);
                            launchServiceIntent.putExtra(MainService.START_INTENT_MY_EMAIL, mWiz.getEmail());
                            YSAAARegistrationActivity.this.startService(launchServiceIntent);

                            startCheckingIfAppReady(pausable);
                        }
                    });

                } catch (Exception e) {
                    L.d("Exception while trying to end the registration process", e);
                    runOnUI(new SafeRunnable(pausable) {

                        @Override
                        protected void safeRun() throws Exception {
                            T.UI();
                            tryConnect(attempt + 1, statusMessage, info);
                        }
                    });
                }
            }
        };
        if (attempt == 1) {
            runOnWorker(runnable);
        } else {
            runDelayedOnWorker(runnable, XMPP_CHECK_DELAY_MILLIS);
        }
    }

    private void startCheckingIfAppReady(final Pausable pausable) {
        runDelayedOnUI(new SafeRunnable(pausable) {
            @Override
            protected void safeRun() throws Exception {
                T.UI();
                launchMainActivityAndFinishIfAppReady();
            }
        }, 1000);
    }

    private void launchMainActivityAndFinishIfAppReady() {
        T.UI();

        Friend f = MainActivity.getFriendForYSAAAWhenReady(mService);
        if (f != null) {
            mTimer.cancel();
            mProgressBar.setProgress(100);

            runDelayedOnUI(new SafeRunnable() {
                @Override
                protected void safeRun() throws Exception {
                    T.UI();
                    Intent intent = new Intent(YSAAARegistrationActivity.this, MainActivity.class);
                    intent.setAction(MainActivity.ACTION_REGISTERED);
                    intent.setFlags(MainActivity.FLAG_CLEAR_STACK_SINGLE_TOP);
                    startActivity(intent);
                    YSAAARegistrationActivity.this.finish();
                }
            }, 1000);

        }
    }

    @SuppressWarnings("unchecked")
    private void postFinishRegistration(final String username, final String password, final String invitorCode,
        final String invitorSecret) throws ClientProtocolException, IOException {
        T.REGISTRATION();
        final String mobileInfo = getMobileInfo();
        HttpClient httpClient = HTTPUtil.getHttpClient();
        final HttpPost httpPost = new HttpPost(CloudConstants.REGISTRATION_FINISH_URL);
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
        httpPost.setHeader("User-Agent", MainService.getUserAgent(mService));
        List<NameValuePair> formParams = new ArrayList<NameValuePair>();
        formParams.add(new BasicNameValuePair("mobileInfo", mobileInfo));
        formParams.add(new BasicNameValuePair("account", username));
        formParams.add(new BasicNameValuePair("password", password));
        formParams.add(new BasicNameValuePair("app_id", CloudConstants.APP_ID));
        formParams.add(new BasicNameValuePair("accounts", ""));
        formParams.add(new BasicNameValuePair("invitor_code", invitorCode));
        formParams.add(new BasicNameValuePair("invitor_secret", invitorSecret));
        formParams.add(new BasicNameValuePair("beacons", ""));

        httpPost.setEntity(new UrlEncodedFormEntity(formParams, HTTP.UTF_8));
        L.d("before http final post");
        HttpResponse response = httpClient.execute(httpPost);
        L.d("after http final post");
        final int responseCode = response.getStatusLine().getStatusCode();
        if (responseCode != HttpStatus.SC_OK) {
            throw new IOException("HTTP request resulted in status code " + responseCode);
        }

        L.d("finish_registration call sent");
        HttpEntity httpEntity = response.getEntity();
        if (httpEntity == null) {
            throw new IOException("Response of '/unauthenticated/mobi/registration/finish' was null");
        }

        final Map<String, Object> responseMap = (Map<String, Object>) JSONValue.parse(new InputStreamReader(httpEntity
            .getContent()));
        if (responseMap == null) {
            throw new IOException("HTTP request responseMap was null");
        }
    }

    @Override
    protected void onServiceUnbound() {
    }
}

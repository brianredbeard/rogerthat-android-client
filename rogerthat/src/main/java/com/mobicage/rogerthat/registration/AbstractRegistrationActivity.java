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


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.telephony.TelephonyManager;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.MainActivity;
import com.mobicage.rogerthat.MainService;
import com.mobicage.rogerthat.ServiceBoundActivity;
import com.mobicage.rogerthat.config.Configuration;
import com.mobicage.rogerthat.config.ConfigurationProvider;
import com.mobicage.rogerthat.plugins.system.MobileInfo;
import com.mobicage.rogerthat.plugins.system.SystemPlugin;
import com.mobicage.rogerthat.util.GoogleServicesUtils;
import com.mobicage.rogerthat.util.http.HTTPUtil;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.security.SecurityUtils;
import com.mobicage.rogerthat.util.system.SafeAsyncTask;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.ui.Pausable;
import com.mobicage.rogerthat.util.ui.UIUtils;
import com.mobicage.rpc.Credentials;
import com.mobicage.rpc.config.AppConstants;
import com.mobicage.rpc.config.CloudConstants;
import com.mobicage.rpc.newxmpp.XMPPConfigurationFactory;
import com.mobicage.to.location.BeaconDiscoveredRequestTO;

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
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.net.ssl.SSLException;


public abstract class AbstractRegistrationActivity extends ServiceBoundActivity {

    public final static String INTENT_LOG_URL = "com.mobicage.rogerthat.registration.log_url";

    public static final int HTTP_RETRY_COUNT = 3;
    public static final int HTTP_TIMEOUT = 10000;

    public static final int XMPP_CHECK_DELAY_MILLIS = 5000;
    public static final int XMPP_MAX_NUM_ATTEMPTS = 8;

    public static final String INVITOR_CODE_CONFIGKEY = "invitor_code";
    public static final String INVITOR_SECRET_CONFIGKEY = "invitor_secret";

    private Activity mActivity;
    private AbstractRegistrationWizard mWizard;

    private Handler mUIHandler;

    private HandlerThread mWorkerThread;
    private Looper mWorkerLooper;
    private Handler mWorkerHandler;

    private String mGCMRegistrationId = "";
    private boolean mAgeAndGenderSet = true;
    private String mDiscoveredBeacons = null;

    public void init(Activity activity) {
        T.UI();
        mActivity = activity;
        mUIHandler = new Handler();
        T.setUIThread("RegistrationProcedureActivity.UI");

        mWorkerThread = new HandlerThread("rogerthat_registration_worker");
        mWorkerThread.start();
        mWorkerLooper = mWorkerThread.getLooper();
        mWorkerHandler = new Handler(mWorkerLooper);
        mWorkerHandler.post(new SafeRunnable() {
            @Override
            public void safeRun() {
                T.setRegistrationThread("RegistrationProcedureActivity.WORKER");
            }
        });

        startRegistrationService();
    }

    public void closeWorkerThread() {
        T.UI();
        final Looper looper = mWorkerHandler.getLooper();
        if (looper != null)
            looper.quit();

        try {
            mWorkerThread.join();
        } catch (InterruptedException e) {
            L.bug(e);
        }

        mWorkerHandler = null;
        mWorkerThread = null;
        T.resetRegistrationThreadId();
    }

    public void runOnWorker(Runnable runnable) {
        mWorkerHandler.post(runnable);
    }

    public void runDelayedOnWorker(Runnable runnable, int delayMillis) {
        mWorkerHandler.postDelayed(runnable, delayMillis);
    }

    public void runOnUI(Runnable runnable) {
        mUIHandler.post(runnable);
    }

    public void runDelayedOnUI(Runnable runnable, int delayMillis) {
        mUIHandler.postDelayed(runnable, delayMillis);
    }

    public String getMobileInfo() {
        T.REGISTRATION();
        MobileInfo info = SystemPlugin.gatherMobileInfo(mService);
        String json = JSONValue.toJSONString(info.toJSONMap());
        return json;
    }

    private void startRegistrationService() {
        startService(new Intent(this, RegistrationService.class));
    }

    private void stopRegistrationService() {
        stopService(new Intent(this, RegistrationService.class));
    }

    public void startMainActivity(boolean directly) {
        if (!directly && getDiscoveredBeacons() != null) {
            Intent intent = new Intent(mActivity, MainActivity.class);
            intent.setAction(MainActivity.ACTION_SHOW_DETECTED_BEACONS);
            intent.putExtra(DetectedBeaconActivity.EXTRA_DETECTED_BEACONS, getDiscoveredBeacons());
            intent.putExtra(DetectedBeaconActivity.EXTRA_AGE_AND_GENDER_SET, getAgeAndGenderSet());
            intent.setFlags(MainActivity.FLAG_CLEAR_STACK_SINGLE_TOP);
            startActivity(intent);
        } else if (!directly && AppConstants.PROFILE_SHOW_GENDER_AND_BIRTHDATE && !getAgeAndGenderSet()) {
            Intent intent = new Intent(mActivity, MainActivity.class);
            intent.setAction(MainActivity.ACTION_COMPLETE_PROFILE);
            intent.setFlags(MainActivity.FLAG_CLEAR_STACK_SINGLE_TOP);
            startActivity(intent);
        } else {
            Intent intent = new Intent(mActivity, MainActivity.class);
            intent.setAction(MainActivity.ACTION_REGISTERED);
            startActivity(intent);
        }

        mActivity.finish();
    }

    public void tryConnect(final ProgressDialog pd, final int attempt, final String statusMessage,
                            final RegistrationInfo info) {
        T.UI();
        final Pausable pausable = this;

        if (attempt > XMPP_MAX_NUM_ATTEMPTS) {
            pd.dismiss();
            String message = getString(R.string.registration_error);
            UIUtils.showDialog(mActivity, null, message, getString(R.string.try_again), null, null, null);
            mWizard.reInit();
            mWizard.goBackToPrevious();
            return;
        }
        pd.setMessage(statusMessage + attempt);
        if (!pd.isShowing())
            pd.show();
        L.d("Registration attempt #" + attempt);

        final String xmppServiceName = info.mCredentials.getXmppServiceName();
        final String xmppAccount = info.mCredentials.getXmppAccount();
        final String xmppPassword = info.mCredentials.getPassword();

        final ConfigurationProvider cp = mService.getConfigurationProvider();
        Configuration cfg = cp.getConfiguration(RegistrationWizard2.CONFIGKEY);
        final String invitorCode = (cfg == null) ? null : cfg.get(INVITOR_CODE_CONFIGKEY, null);
        final String invitorSecret = (cfg == null) ? null : cfg.get(INVITOR_SECRET_CONFIGKEY, null);

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
                            invitorCode, invitorSecret);

                    runOnUI(new SafeRunnable(pausable) {

                        @Override
                        protected void safeRun() throws Exception {
                            T.UI();
                            mWizard.setCredentials(info.mCredentials);

                            if (CloudConstants.USE_GCM_KICK_CHANNEL && !"".equals(mGCMRegistrationId)) {
                                GoogleServicesUtils.saveGCMRegistrationId(mService, mGCMRegistrationId);
                            }

                            mService.setCredentials(mWizard.getCredentials());
                            mService.setRegisteredInConfig(true);

                            final Intent launchServiceIntent = new Intent(mActivity, MainService.class);
                            launchServiceIntent.putExtra(MainService.START_INTENT_JUST_REGISTERED, true);
                            launchServiceIntent.putExtra(MainService.START_INTENT_MY_EMAIL, mWizard.getEmail());
                            mActivity.startService(launchServiceIntent);
                            stopRegistrationService();
                            pd.dismiss();

                            mWizard.finish(); // finish
                        }
                    });

                } catch (Exception e) {
                    L.d("Exception while trying to end the registration process", e);
                    runOnUI(new SafeRunnable(pausable) {

                        @Override
                        protected void safeRun() throws Exception {
                            T.UI();
                            tryConnect(pd, attempt + 1, statusMessage, info);
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

    public void sendRegistrationStep(final String step) {
        List<NameValuePair> extraParams = new ArrayList<>();
        sendRegistrationStep(step, extraParams);
    }

    public void sendRegistrationUrl(final String url, final int count) {
        List<NameValuePair> extraParams = new ArrayList<>();
        extraParams.add(new BasicNameValuePair("url", url));
        extraParams.add(new BasicNameValuePair("count", count + ""));
        sendRegistrationStep("log_url", extraParams);
    }

    public void sendRegistrationStep(final String step, final List<NameValuePair> extraParams) {
        new SafeAsyncTask<Object, Object, Object>() {

            @Override
            protected Object safeDoInBackground(Object... params) {
                final HttpPost httpPost = new HttpPost(CloudConstants.REGISTRATION_LOG_STEP_URL);
                httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
                httpPost.setHeader("User-Agent", MainService.getUserAgent(mActivity));
                List<NameValuePair> formParams = new ArrayList<NameValuePair>();
                formParams.add(new BasicNameValuePair("step", step));
                formParams.add(new BasicNameValuePair("install_id", mWizard.getInstallationId()));
                formParams.addAll(extraParams);

                UrlEncodedFormEntity entity;
                try {
                    entity = new UrlEncodedFormEntity(formParams, HTTP.UTF_8);
                } catch (UnsupportedEncodingException e) {
                    L.bug(e);
                    return false;
                }
                httpPost.setEntity(entity);
                L.d("Sending registration step: " + step);
                HttpResponse response;
                try {
                    response = HTTPUtil.getHttpClient(HTTP_TIMEOUT, HTTP_RETRY_COUNT).execute(httpPost);
                } catch (ClientProtocolException e) {
                    L.bug(e);
                    return false;
                } catch (SSLException e) {
                    L.bug(e);
                    return false;
                } catch (IOException e) {
                    L.e(e);
                    return false;
                }

                if (response.getEntity() != null) {
                    try {
                        response.getEntity().consumeContent();
                    } catch (IOException e) {
                        L.bug(e);
                        return false;
                    }
                }

                L.d("Registration step " + step + " sent");
                final int responseCode = response.getStatusLine().getStatusCode();
                if (responseCode != HttpStatus.SC_OK) {
                    L.bug("HTTP request resulted in status code " + responseCode);
                    return false;
                }

                return true;
            }

            @Override
            protected void safeOnPostExecute(Object result) {
            }

            @Override
            protected void safeOnCancelled(Object result) {
            }

            @Override
            protected void safeOnProgressUpdate(Object... values) {
            }

            @Override
            protected void safeOnPreExecute() {
            }

        }.execute();
    }

    @SuppressWarnings("unchecked")
    private void postFinishRegistration(final String username, final String password, final String invitorCode,
                                          final String invitorSecret) throws ClientProtocolException, IOException {
        T.REGISTRATION();
        final String mobileInfo = getMobileInfo();
        HttpClient httpClient = HTTPUtil.getHttpClient();
        final HttpPost httpPost = new HttpPost(CloudConstants.REGISTRATION_FINISH_URL);
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
        httpPost.setHeader("User-Agent", MainService.getUserAgent(mActivity));
        List<NameValuePair> formParams = new ArrayList<NameValuePair>();
        formParams.add(new BasicNameValuePair("mobileInfo", mobileInfo));
        formParams.add(new BasicNameValuePair("account", username));
        formParams.add(new BasicNameValuePair("password", password));
        formParams.add(new BasicNameValuePair("app_id", CloudConstants.APP_ID));

        formParams.add(new BasicNameValuePair("invitor_code", invitorCode));
        formParams.add(new BasicNameValuePair("invitor_secret", invitorSecret));

        org.json.simple.JSONArray beacons = new org.json.simple.JSONArray();
        if (mWizard.getDetectedBeacons() != null) {
            for (BeaconDiscoveredRequestTO bdr : mWizard.getDetectedBeacons()) {
                beacons.add(bdr.toJSONMap());
            }
        }
        formParams.add(new BasicNameValuePair("beacons", beacons.toString()));

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

        JSONArray beaconRegions = (JSONArray) responseMap.get("discovered_beacons");
        if (beaconRegions != null && beaconRegions.size() > 0) {
            mDiscoveredBeacons = JSONValue.toJSONString(beaconRegions);
        } else {
            mDiscoveredBeacons = null;
        }
    }

    public String getGCMRegistrationId() {
        return mGCMRegistrationId;
    }

    public void setGCMRegistrationId(String GCMRegistrationId) {
        mGCMRegistrationId = GCMRegistrationId;
    }

    public void setWizard(AbstractRegistrationWizard wizard) {
        mWizard = wizard;
    }

    public String getDiscoveredBeacons() {
        return mDiscoveredBeacons;
    }

    public boolean getAgeAndGenderSet() {
        return mAgeAndGenderSet;
    }

    public void setAgeAndGenderSet(boolean ageAndGenderSet) {
        mAgeAndGenderSet = ageAndGenderSet;
    }


    public void registerDevice() {
        T.UI();
        final HttpClient httpClient = HTTPUtil.getHttpClient();
        final String email = mWizard.getEmail();
        final String timestamp = "" + mWizard.getTimestamp();
        final String deviceId = mWizard.getDeviceId();
        final String registrationId = mWizard.getRegistrationId();
        // Make call to Rogerthat
        String message = getString(R.string.activating);
        final ProgressDialog progressDialog = UIUtils.showProgressDialog(mActivity, null, message, true, false);
        final SafeRunnable showErrorDialog = new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                T.UI();
                progressDialog.dismiss();
                UIUtils.showErrorPleaseRetryDialog(mActivity);
            }
        };

        runOnWorker(new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                T.REGISTRATION();
                String version = "3";
                String signature = SecurityUtils.sha256(version + " " + email + " " + timestamp + " " + deviceId + " "
                        + registrationId + " " + CloudConstants.REGISTRATION_MAIN_SIGNATURE);

                HttpPost httppost = new HttpPost(CloudConstants.REGISTRATION_REGISTER_DEVICE_URL);
                try {
                    List<NameValuePair> nameValuePairs = new ArrayList<>();
                    nameValuePairs.add(new BasicNameValuePair("version", version));
                    nameValuePairs.add(new BasicNameValuePair("registration_time", timestamp));
                    nameValuePairs.add(new BasicNameValuePair("device_id", deviceId));
                    nameValuePairs.add(new BasicNameValuePair("registration_id", registrationId));
                    nameValuePairs.add(new BasicNameValuePair("signature", signature));
                    nameValuePairs.add(new BasicNameValuePair("email", email));
                    nameValuePairs.add(new BasicNameValuePair("platform", "android"));
                    nameValuePairs.add(new BasicNameValuePair("language", Locale.getDefault().getLanguage()));
                    nameValuePairs.add(new BasicNameValuePair("country", Locale.getDefault().getCountry()));
                    nameValuePairs.add(new BasicNameValuePair("app_id", CloudConstants.APP_ID));
                    nameValuePairs.add(new BasicNameValuePair("use_xmpp_kick", CloudConstants.USE_XMPP_KICK_CHANNEL
                            + ""));
                    nameValuePairs.add(new BasicNameValuePair("GCM_registration_id", getGCMRegistrationId()));
                    nameValuePairs.add(new BasicNameValuePair("hardware_model", SystemPlugin.getDeviceModelName()));
                    TelephonyManager telephonyManager = (TelephonyManager) mService.getSystemService(Context.TELEPHONY_SERVICE);
                    if (telephonyManager != null) {
                        nameValuePairs.add(new BasicNameValuePair("sim_carrier_name", telephonyManager.getSimOperatorName()));
                    }


                    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                    // Execute HTTP Post Request
                    HttpResponse response = httpClient.execute(httppost);

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
                                        UIUtils.showDialog(mActivity, null, errorMessage);
                                    }
                                });
                                return;
                            }
                        }
                        runOnUI(showErrorDialog);
                        return;
                    }
                    JSONObject account = (JSONObject) responseMap.get("account");
                    setAgeAndGenderSet((Boolean) responseMap.get("age_and_gender_set"));

                    final RegistrationInfo info = new RegistrationInfo(email, new Credentials((String) account
                            .get("account"), (String) account.get("password")));
                    runOnUI(new SafeRunnable() {
                        @Override
                        protected void safeRun() throws Exception {
                            T.UI();
                            mWizard.setEmail(email);
                            mWizard.save();
                            tryConnect(
                                    progressDialog,
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
}

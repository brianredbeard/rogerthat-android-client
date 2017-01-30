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


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.MainActivity;
import com.mobicage.rogerthat.MainService;
import com.mobicage.rogerthat.ServiceBoundActivity;
import com.mobicage.rogerthat.config.Configuration;
import com.mobicage.rogerthat.config.ConfigurationProvider;
import com.mobicage.rogerthat.plugins.system.MobileInfo;
import com.mobicage.rogerthat.plugins.system.SystemPlugin;
import com.mobicage.rogerthat.registration.AccountManager.Account;
import com.mobicage.rogerthat.util.GoogleServicesUtils;
import com.mobicage.rogerthat.util.http.HTTPUtil;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.ui.Pausable;
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
import org.json.simple.JSONValue;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public abstract class AbstractRegistrationActivity extends ServiceBoundActivity {

    private static final int XMPP_CHECK_DELAY_MILLIS = 5000;
    private static final int XMPP_MAX_NUM_ATTEMPTS = 8;

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
    private List<Account> mAccounts;
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

            new AlertDialog.Builder(mActivity).setMessage(getString(R.string.registration_error))
                    .setCancelable(true).setPositiveButton(R.string.try_again, null).create().show();
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

        org.json.simple.JSONArray accounts = new org.json.simple.JSONArray();
        if (getAccounts() != null) {
            for (Account acc : getAccounts()) {
                Map<String, String> jacc = new LinkedHashMap<String, String>();
                jacc.put("type", acc.type);
                jacc.put("name", acc.name);
                accounts.add(jacc);
            }
        }
        formParams.add(new BasicNameValuePair("accounts", accounts.toString()));

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

    public void setAccounts(List<Account> accounts) {
        mAccounts = accounts;
    }

    public List<Account> getAccounts() {
        return mAccounts;
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
}

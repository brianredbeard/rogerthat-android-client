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

package com.mobicage.rogerthat;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.registration.RegistrationActivity2;
import com.mobicage.rogerthat.util.RegexPatterns;
import com.mobicage.rogerthat.util.consent.ConsentProvider;
import com.mobicage.rogerthat.util.http.HTTPUtil;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.net.NetworkConnectivityManager;
import com.mobicage.rogerthat.util.system.SafeAsyncTask;
import com.mobicage.rogerthat.util.system.SafeBroadcastReceiver;
import com.mobicage.rogerthat.util.system.SafeDialogClick;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.system.SafeViewOnClickListener;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.ui.UIUtils;
import com.mobicage.rpc.config.AppConstants;
import com.mobicage.rpc.config.CloudConstants;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.simple.JSONValue;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConsentTOSActivity extends ServiceBoundActivity {

    private WebView mTOSWebview;
    private ProgressDialog mProgressDialog;

    private BroadcastReceiver mBroadcastReceiver = new SafeBroadcastReceiver() {
        @Override
        public String[] onSafeReceive(Context context, Intent intent) {
            T.UI();
            final String action = intent.getAction();
             if (NetworkConnectivityManager.INTENT_NETWORK_UP.equals(action)) {
                mTOSWebview = (WebView) findViewById(R.id.tos_webview);
                mTOSWebview.loadUrl(AppConstants.TERMS_OF_SERVICE_URL);
            }

            return null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onServiceBound() {
        setContentView(R.layout.registration_tos);
        setTitle(R.string.terms_of_service);
        setNavigationBarBurgerVisible(false);
        setNavigationBarIcon(null);

        final IntentFilter filter = new IntentFilter(NetworkConnectivityManager.INTENT_NETWORK_UP);
        registerReceiver(mBroadcastReceiver, filter);

        if (!mService.getNetworkConnectivityManager().isConnected()) {
            int message = R.string.no_internet_connection_try_again;
            UIUtils.showDialog(this, null, message);
        } else {
            mTOSWebview = (WebView) findViewById(R.id.tos_webview);
            mTOSWebview.loadUrl(AppConstants.TERMS_OF_SERVICE_URL);
        }

        final Button tosAgreeBtn = (Button) findViewById(R.id.tos_agree);
        tosAgreeBtn.setOnClickListener(new SafeViewOnClickListener() {
            @Override
            public void safeOnClick(View v) {
                final CheckBox tosAgeCheckbox = (CheckBox) findViewById(R.id.tos_age);
                if (!tosAgeCheckbox.isChecked()) {
                    final String message = ConsentTOSActivity.this.getString( R.string.tos_parent_consent);
                    final String positiveBtn = ConsentTOSActivity.this.getString(R.string.grant_permission);
                    final String negativeButtonCaption = ConsentTOSActivity.this.getString(R.string.abort);
                    SafeDialogClick positiveClick = new SafeDialogClick() {
                        @Override
                        public void safeOnClick(DialogInterface dialog, int id) {
                            T.UI();
                            dialog.dismiss();
                            saveOnServer(ConsentProvider.TOS_AGE_PARENTAL);
                        }
                    };
                    SafeDialogClick negativeClick = new SafeDialogClick() {
                        @Override
                        public void safeOnClick(DialogInterface dialog, int id) {
                            T.UI();
                            dialog.dismiss();
                        }
                    };
                    UIUtils.showDialog(ConsentTOSActivity.this, null, message, positiveBtn, positiveClick, negativeButtonCaption, negativeClick);
                } else {
                    saveOnServer(ConsentProvider.TOS_AGE_16);
                }
            }
        });
    }

    @Override
    protected void onServiceUnbound() {
        unregisterReceiver(mBroadcastReceiver);
    }

    private void initializeProgressDialog() {
        if (mProgressDialog != null) {
            return;
        }
        mProgressDialog = new ProgressDialog(ConsentTOSActivity.this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setMessage(ConsentTOSActivity.this.getString(R.string.processing));
        mProgressDialog.setCancelable(false);
    }

    private void saveOnServer(final String age) {
        T.UI();
        initializeProgressDialog();

        new SafeAsyncTask<Object, Object, Map<String, Object>>() {
            @Override
            protected Map<String, Object> safeDoInBackground(Object... params) {
                final HttpPost request  = HTTPUtil.getHttpPost(ConsentTOSActivity.this, CloudConstants.ACCOUNT_CONSENT_URL);
                HTTPUtil.addCredentials(mService, request);

                final List<NameValuePair> nameValuePairs = new ArrayList<>();
                nameValuePairs.add(new BasicNameValuePair("consent_type", "tos"));
                nameValuePairs.add(new BasicNameValuePair("age", age));
                try {
                    request.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));
                } catch (UnsupportedEncodingException e) {
                    L.bug(e); // should never happen
                    return null;
                }

                try {
                    final HttpResponse response = HTTPUtil.getHttpClient().execute(request);
                    final int statusCode = response.getStatusLine().getStatusCode();
                    final HttpEntity entity = response.getEntity();
                    if (entity == null) {
                        mService.postOnUIHandler(new SafeRunnable() {
                            @Override
                            protected void safeRun() throws Exception {
                                UIUtils.showErrorPleaseRetryDialog(ConsentTOSActivity.this);
                            }
                        });
                        return null;
                    }
                    @SuppressWarnings("unchecked")
                    final Map<String, Object> responseMap = (Map<String, Object>) JSONValue.parse(new InputStreamReader(entity.getContent()));
                    if (statusCode != HttpStatus.SC_OK || responseMap == null) {
                        if (responseMap == null || responseMap.get("error") == null) {
                            mService.postOnUIHandler(new SafeRunnable() {
                                @Override
                                protected void safeRun() throws Exception {
                                    UIUtils.showErrorPleaseRetryDialog(ConsentTOSActivity.this);
                                }
                            });
                        } else {
                            final String errorMessage = (String) responseMap.get("error");
                            mService.postOnUIHandler(new SafeRunnable() {
                                @Override
                                protected void safeRun() throws Exception {
                                    UIUtils.showDialog(ConsentTOSActivity.this, null, errorMessage);
                                }
                            });
                        }
                        return null;
                    }

                    Map<String, Object> result = new HashMap<>();
                    result.put("message", responseMap.get("message"));
                    return result;

                } catch (IOException e) {
                    return null;
                }
            }

            @Override
            protected void onPreExecute() {
                T.UI();
                mProgressDialog.show();
            }

            @Override
            protected void onPostExecute(Map<String, Object> result) {
                T.UI();
                mProgressDialog.hide();
                if (result != null) {
                    final String message = (String) result.get("message");
                    if (message != null) {
                        AlertDialog dialog = UIUtils.showDialog(ConsentTOSActivity.this, null, message, new SafeDialogClick() {
                            @Override
                            public void safeOnClick(DialogInterface dialog, int id) {
                                saveInDB();
                            }
                        });
                        dialog.setCancelable(false);
                    } else {
                        saveInDB();
                    }
                }
            }
        }.execute();
    }

    private void saveInDB() {
        mService.getConsentProvider().saveConsentForTOS();
        Intent intent = new Intent(ConsentTOSActivity.this, MainActivity.class);
        intent.setFlags(MainActivity.FLAG_CLEAR_STACK_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}

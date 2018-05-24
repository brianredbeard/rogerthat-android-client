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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.util.RegexPatterns;
import com.mobicage.rogerthat.util.http.HTTPUtil;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeAsyncTask;
import com.mobicage.rogerthat.util.system.SafeDialogClick;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.ui.UIUtils;
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

public class ConsentPushNotificationsActivity extends ServiceBoundActivity {

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected void onServiceBound() {
        setContentView(R.layout.registration_notifications);
        setTitle(R.string.push_notifications);
        setNavigationBarBurgerVisible(false);
        setNavigationBarIcon(null);

        TextView notificationsTextView = (TextView) findViewById(R.id.notifications_text);
        final String header = getString(R.string.registration_notifications_header, getString(R.string.app_name));
        final String reason = getString(CloudConstants.isCityApp() ? R.string.registration_notification_types_city_app : R.string.registration_notification_types_general);
        notificationsTextView.setText(header + "\n\n" + reason);
        final Button notificationsButton = (Button) findViewById(R.id.notifications_continue);
        notificationsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String title = ConsentPushNotificationsActivity.this.getString( R.string.notifications_consent_title);
                final String message = ConsentPushNotificationsActivity.this.getString( R.string.notifications_consent_message);
                final String positiveBtn = ConsentPushNotificationsActivity.this.getString(R.string.allow);
                final String negativeButtonCaption = ConsentPushNotificationsActivity.this.getString(R.string.dont_allow);
                SafeDialogClick positiveClick = new SafeDialogClick() {
                    @Override
                    public void safeOnClick(DialogInterface dialog, int id) {
                        T.UI();
                        dialog.dismiss();
                        saveOnServer(true);
                    }
                };
                SafeDialogClick negativeClick = new SafeDialogClick() {
                    @Override
                    public void safeOnClick(DialogInterface dialog, int id) {
                        T.UI();
                        dialog.dismiss();
                        saveOnServer(false);
                    }
                };
                UIUtils.showDialog(ConsentPushNotificationsActivity.this, title, message, positiveBtn, positiveClick, negativeButtonCaption, negativeClick);
            }
        });
    }

    @Override
    protected void onServiceUnbound() {
    }

    private void initializeProgressDialog() {
        if (mProgressDialog != null) {
            return;
        }
        mProgressDialog = new ProgressDialog(ConsentPushNotificationsActivity.this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setMessage(ConsentPushNotificationsActivity.this.getString(R.string.processing));
        mProgressDialog.setCancelable(false);
    }

    private void saveOnServer(final boolean enabled) {
        T.UI();
        initializeProgressDialog();

        new SafeAsyncTask<Object, Object, Map<String, Object>>() {
            @Override
            protected Map<String, Object> safeDoInBackground(Object... params) {
                final HttpPost request  = HTTPUtil.getHttpPost(ConsentPushNotificationsActivity.this, CloudConstants.ACCOUNT_CONSENT_URL);
                HTTPUtil.addCredentials(mService, request);

                final List<NameValuePair> nameValuePairs = new ArrayList<>();
                nameValuePairs.add(new BasicNameValuePair("consent_type", "push_notifications"));
                nameValuePairs.add(new BasicNameValuePair("enabled", enabled ? "yes" : "no"));
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
                                UIUtils.showErrorPleaseRetryDialog(ConsentPushNotificationsActivity.this);
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
                                    UIUtils.showErrorPleaseRetryDialog(ConsentPushNotificationsActivity.this);
                                }
                            });
                        } else {
                            final String errorMessage = (String) responseMap.get("error");
                            mService.postOnUIHandler(new SafeRunnable() {
                                @Override
                                protected void safeRun() throws Exception {
                                    UIUtils.showDialog(ConsentPushNotificationsActivity.this, null, errorMessage);
                                }
                            });
                        }
                        return null;
                    }

                    Map<String, Object> result = new HashMap<>();
                    result.put("enabled", enabled);
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
                    final Boolean enabled = (Boolean) result.get("enabled");
                    final String message = (String) result.get("message");
                    if (message != null) {
                        AlertDialog dialog = UIUtils.showDialog(ConsentPushNotificationsActivity.this, null, message, new SafeDialogClick() {
                            @Override
                            public void safeOnClick(DialogInterface dialog, int id) {
                                saveInDB(enabled);
                            }
                        });
                        dialog.setCancelable(false);
                    } else {
                        saveInDB(enabled);
                    }
                }
            }
        }.execute();
    }

    private void saveInDB(final boolean enabled) {
        mService.getConsentProvider().saveConsentForPushNotifications();

        final SharedPreferences options = PreferenceManager.getDefaultSharedPreferences(ConsentPushNotificationsActivity.this);
        final SharedPreferences.Editor editor = options.edit();
        editor.putBoolean(MainService.PREFERENCE_PUSH_NOTIFICATIONS, enabled);
        final boolean success = editor.commit();
        L.d("savePushNotifications success: " + success);

        Intent intent = new Intent(ConsentPushNotificationsActivity.this, MainActivity.class);
        intent.setFlags(MainActivity.FLAG_CLEAR_STACK_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}

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
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.util.RegexPatterns;
import com.mobicage.rogerthat.util.TextUtils;
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
import java.util.List;
import java.util.Map;

public class DataDownloadActivity extends ServiceBoundActivity {


    private IdentityStore mIdentityStore;
    private MyIdentity mIdentity;
    private EditText mEmailView;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onServiceBound() {
        setContentView(R.layout.data_download);
        setTitle(R.string.data_download);

        mIdentityStore = mService.getIdentityStore();
        mIdentity = mIdentityStore.getIdentity();
        mEmailView = ((EditText) findViewById(R.id.data_download_email));
        mEmailView.setText(mIdentity.getDisplayEmail());


        final Button downloadButton = (Button) findViewById(R.id.data_download_button);
        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View button) {
                T.UI();
                requestDownload();
            }
        });
    }

    @Override
    protected void onServiceUnbound() {
    }

    private void requestDownload() {
        final String email = mEmailView.getText().toString().toLowerCase().trim();
        if (!RegexPatterns.EMAIL.matcher(email).matches()) {
            UIUtils.showDialog(DataDownloadActivity.this, null, R.string.registration_email_not_valid);
            return;
        }
        dataDownload(email);

    }

    private void initializeProgressDialog() {
        if (mProgressDialog != null) {
            return;
        }
        mProgressDialog = new ProgressDialog(DataDownloadActivity.this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setMessage(DataDownloadActivity.this.getString(R.string.processing));
        mProgressDialog.setCancelable(false);
    }

    private void dataDownload(final String email) {
        T.UI();
        initializeProgressDialog();

        new SafeAsyncTask<Object, Object, String>() {
            @Override
            protected String safeDoInBackground(Object... params) {
                final HttpPost request  = HTTPUtil.getHttpPost(DataDownloadActivity.this, CloudConstants.ACCOUNT_DATA_DOWNLOAD_URL);
                HTTPUtil.addCredentials(mService, request);

                final List<NameValuePair> nameValuePairs = new ArrayList<>();
                nameValuePairs.add(new BasicNameValuePair("data_export_email", email));
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
                                UIUtils.showErrorPleaseRetryDialog(DataDownloadActivity.this);
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
                                    UIUtils.showErrorPleaseRetryDialog(DataDownloadActivity.this);
                                }
                            });
                        } else {
                            final String errorMessage = (String) responseMap.get("error");
                            mService.postOnUIHandler(new SafeRunnable() {
                                @Override
                                protected void safeRun() throws Exception {
                                    UIUtils.showDialog(DataDownloadActivity.this, null, errorMessage);
                                }
                            });
                        }
                        return null;
                    }

                    final String message = (String) responseMap.get("message");
                    return message;

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
            protected void onPostExecute(String message) {
                T.UI();
                mProgressDialog.hide();
                if (message != null) {
                    AlertDialog dialog = UIUtils.showDialog(DataDownloadActivity.this, null, message, new SafeDialogClick() {
                        @Override
                        public void safeOnClick(DialogInterface dialog, int id) {
                            finish();
                        }
                    });
                    dialog.setCancelable(false);
                } else {
                    finish();
                }
            }
        }.execute();
    }
}

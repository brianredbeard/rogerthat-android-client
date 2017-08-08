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

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebViewClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.annotation.SuppressLint;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.util.OauthUtils;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rpc.config.CloudConstants;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class OauthActivity extends Activity {

    public static final String FINISHED_OAUTH = "com.mobicage.rogerthat.FINISHED_OAUTH";

    public static String ALLOW_BACKPRESS = "allow_backpress";
    public static String BUILD_URL = "build_url";
    public static String STATE = "state";
    public static String CLIENT_ID = "client_id";
    public static String OAUTH_URL = "oauth_url";
    public static String SCOPES = "scopes";

    public static String RESULT_STATE = "result_stae";
    public static String RESULT_CODE = "result_code";
    public static String RESULT_ERROR_MESSAGE = "result_error_message";
    public static String RESULT_SUCCESS = "result_success";

    private boolean mAllowBackpress;
    private boolean mBuildUrl;
    private String mState;
    private String mClientId;
    private String mOauthUrl;
    private String mScopes; // Comma separated

    private WebView mWebview;

    @SuppressLint({ "SetJavaScriptEnabled", "NewApi" })
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        setContentView(R.layout.oauth);

        mWebview = (WebView) findViewById(R.id.webview);

        Intent intent = getIntent();
        mAllowBackpress = intent.getBooleanExtra(OauthActivity.ALLOW_BACKPRESS, false);
        mBuildUrl = intent.getBooleanExtra(OauthActivity.BUILD_URL, true);
        mState = intent.getStringExtra(OauthActivity.STATE);
        mClientId = intent.getStringExtra(OauthActivity.CLIENT_ID);
        mOauthUrl = intent.getStringExtra(OauthActivity.OAUTH_URL);
        mScopes = intent.getStringExtra(OauthActivity.SCOPES);

        Uri.Builder builder = Uri.parse(mOauthUrl).buildUpon();
        if (mBuildUrl) {
            builder.appendQueryParameter("state", mState);
            builder.appendQueryParameter("client_id", mClientId);
            builder.appendQueryParameter("scope", mScopes);
            builder.appendQueryParameter("redirect_uri", OauthUtils.getCallbackUrl());
            builder.appendQueryParameter("response_type", "code");
        }

        final String url = builder.build().toString();


        WebSettings webviewSettings = mWebview.getSettings();
        webviewSettings.setJavaScriptEnabled(true);
        webviewSettings.setDomStorageEnabled(true);

        webviewSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);

        if (Build.VERSION.SDK_INT <= 18) {
            mWebview.getSettings().setSavePassword(false);
        }

        mWebview.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                L.d("shouldOverrideUrlLoading: " + url);
                interceptUrlCompat(view, url, true);
                return true;
            }

            private boolean interceptUrlCompat(WebView view, String url, boolean loadUrl) {
                String redirectUri = OauthUtils.getCallbackUrl();
                if (OauthUtils.isRedirectUriFound(url, redirectUri)) {
                    Uri uri = Uri.parse(url);

                    String code = uri.getQueryParameter("code");
                    String state = uri.getQueryParameter("state");
                    if (!TextUtils.isEmpty(code)) {
                        L.d("Code: " + code);
                        L.d("State: " + state);

                        Intent resultIntent = new Intent(OauthActivity.FINISHED_OAUTH);
                        resultIntent.putExtra(OauthActivity.RESULT_CODE, code);
                        resultIntent.putExtra(OauthActivity.RESULT_STATE, state);
                        resultIntent.putExtra(OauthActivity.RESULT_SUCCESS, true);
                        setResult(Activity.RESULT_OK, resultIntent);
                        finish();

                    } else {
                        String error = uri.getQueryParameter("error");
                        String errorDescription = uri.getQueryParameter("error_description");
                        L.d("error: " + error);
                        L.d("errorMsg: " + errorDescription);

                        Intent resultIntent = new Intent(OauthActivity.FINISHED_OAUTH);
                        resultIntent.putExtra(OauthActivity.RESULT_ERROR_MESSAGE, errorDescription);
                        resultIntent.putExtra(OauthActivity.RESULT_SUCCESS, false);
                        setResult(Activity.RESULT_OK, resultIntent);
                        finish();
                    }

                    return true;
                }
                if (loadUrl) {
                    view.loadUrl(url);
                }
                return false;
            }

        });

        mWebview.loadUrl(url);
        mWebview.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (mAllowBackpress) {
                    setResult(Activity.RESULT_CANCELED);
                    finish();
                } else if (mWebview.canGoBack()){
                    mWebview.goBack();
                }
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}

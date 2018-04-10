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

package com.mobicage.rogerthat.plugins.scan;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;

import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.MyIdentity;
import com.mobicage.rogerthat.NavigationItem;
import com.mobicage.rogerthat.ServiceBoundActivity;
import com.mobicage.rogerthat.cordova.CordovaSettings;
import com.mobicage.rogerthat.plugins.friends.ActionScreenActivity;
import com.mobicage.rogerthat.plugins.friends.FriendsPlugin;
import com.mobicage.rogerthat.plugins.history.HistoryItem;
import com.mobicage.rogerthat.util.ActivityUtils;
import com.mobicage.rogerthat.util.RegexPatterns;
import com.mobicage.rogerthat.util.TextUtils;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeBroadcastReceiver;
import com.mobicage.rogerthat.util.system.SafeDialogClick;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.ui.UIUtils;
import com.mobicage.rpc.config.AppConstants;
import com.mobicage.rpc.config.CloudConstants;
import com.mobicage.to.friends.GetUserInfoRequestTO;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;

public class ProcessScanActivity extends ServiceBoundActivity {

    public final static String URL_ROGERTHAT_PREFIX = AppConstants.APP_ID + "://q/i";
    public final static String URL_ROGERTHAT_SID_PREFIX = AppConstants.APP_ID + "://q/s/";

    // /s/ (lowercase s) is probably no longer used. Certainly server doesnt serve this path
    public final static String SHORT_HTTPS_URL_PREFIX = CloudConstants.HTTPS_BASE_URL + "/s/";

    public final static String SCAN_FRIEND_INVITE_PREFIX = CloudConstants.HTTPS_BASE_URL + "/q/i";
    public final static String SCAN_SERVICE_ACTION_PREFIX = CloudConstants.HTTPS_BASE_URL + "/q/s/";

    public static final String SCAN_RESULT = "SCAN_RESULT";

    private static final String INVITATION_SECRET_ARG = "s";
    private static final String INVITATION_INVITOR_ARG = "u";

    public final static String URL_REDIRECTION_DONE = "com.mobicage.rogerthat.plugins.scan.URL_REDIRECTION_DONE";

    public final static String NAME = "name";
    public final static String AVATAR = "avatar";
    public static final String APP_ID = "appId";
    public static final String EMAIL = "email";
    public static final String TYPE = "type";
    public static final String DESCRIPTION = "description";
    public static final String DESCRIPTION_BRANDING = "decriptionBranding";
    public static final String POKE_ACTION = "action";
    public static final String POKE_DESCRIPTION = "actionDescription";
    public static final String QUALIFIED_IDENTIFIER = "qualified_identifier";
    public static final String SUCCESS = "success";
    public static final String STATIC_FLOW = "staticFlow";
    public static final String STATIC_FLOW_HASH = "staticFlowHash";

    public static final String RAWURL = "rawUrl";
    public static final String EMAILHASH = "emailHash";
    public static final String URL = "url";

    private FriendsPlugin mFriendsPlugin;
    private ScanCommunication mScanCommunication;
    private ProgressDialog mProgressDialog;

    private String mExpectedEmailHash;
    private String mExpectedAction;

    private SafeBroadcastReceiver mBroadcastReceiver;

    @Override
    protected void onServiceBound() {
        T.UI();

        mFriendsPlugin = mService.getPlugin(FriendsPlugin.class);
        mBroadcastReceiver = getBroadcastReceiver();

        final IntentFilter filter = new IntentFilter(FriendsPlugin.FRIEND_INFO_RECEIVED_INTENT);
        filter.addAction(FriendsPlugin.SERVICE_ACTION_INFO_RECEIVED_INTENT);
        filter.addAction(URL_REDIRECTION_DONE);
        registerReceiver(mBroadcastReceiver, filter);

        Intent intent = getIntent();
        final String url = intent.getStringExtra(URL);
        final String emailHash = intent.getStringExtra(EMAILHASH);

        if (url == null && emailHash == null) {
            L.bug("url == null && emailHash == null");
            finish();
        } else {
            startSpinner(intent.getBooleanExtra(SCAN_RESULT, false));
            if (url != null)
                processUrl(url);
            else
                processEmailHash(emailHash);
        }
    }

    @Override
    protected void onServiceUnbound() {
        // do nothing
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBroadcastReceiver != null)
            unregisterReceiver(mBroadcastReceiver);
    }

    private SafeBroadcastReceiver getBroadcastReceiver() {
        return new SafeBroadcastReceiver() {
            @Override
            public String[] onSafeReceive(Context context, Intent intent) {
                T.UI();
                if (intent.getAction().equals(FriendsPlugin.FRIEND_INFO_RECEIVED_INTENT)) {
                    final String emailHash = intent.getStringExtra(EMAILHASH);
                    if (emailHash != null && emailHash.equals(mExpectedEmailHash)) {
                        abortProcessing();

                        if (intent.getBooleanExtra(ProcessScanActivity.SUCCESS, true)) {
                            final Intent inviteFriendIntent = new Intent(ProcessScanActivity.this,
                                InviteFriendActivity.class);
                            // Copy extra from other intent
                            for (String extra : new String[] { AVATAR, DESCRIPTION, DESCRIPTION_BRANDING, EMAIL,
                                EMAILHASH, NAME, QUALIFIED_IDENTIFIER }) {
                                inviteFriendIntent.putExtra(extra, intent.getStringExtra(extra));
                            }
                            inviteFriendIntent
                                .putExtra(TYPE, intent.getLongExtra(TYPE, FriendsPlugin.FRIEND_TYPE_USER));
                            startActivity(inviteFriendIntent);
                            finish();
                            return new String[] { intent.getAction() };
                        } else {
                            UIUtils.showErrorDialog(ProcessScanActivity.this, intent);
                        }

                    } else {
                        // ignore
                    }
                } else if (intent.getAction().equals(FriendsPlugin.SERVICE_ACTION_INFO_RECEIVED_INTENT)) {
                    if (mExpectedEmailHash != null && mExpectedEmailHash.equals(intent.getStringExtra(EMAILHASH))
                        && mExpectedAction != null && mExpectedAction.equals(intent.getStringExtra(POKE_ACTION))) {
                        abortProcessing();

                        if (intent.getBooleanExtra(SUCCESS, true)) {
                            final Intent serviceActionIntent = new Intent(ProcessScanActivity.this,
                                ServiceActionActivity.class);
                            // Copy extra from other intent
                            for (String extra : new String[] { AVATAR, DESCRIPTION, DESCRIPTION_BRANDING, EMAIL, NAME,
                                POKE_DESCRIPTION, QUALIFIED_IDENTIFIER, STATIC_FLOW, STATIC_FLOW_HASH }) {
                                serviceActionIntent.putExtra(extra, intent.getStringExtra(extra));
                            }
                            serviceActionIntent.putExtra(EMAILHASH, mExpectedEmailHash);
                            serviceActionIntent.putExtra(POKE_ACTION, mExpectedAction);
                            serviceActionIntent.setAction(FriendsPlugin.SERVICE_ACTION_INFO_RECEIVED_INTENT);
                            serviceActionIntent.putExtra(SUCCESS, true);
                            startActivity(serviceActionIntent);
                            finish();
                            // TODO: set success?

                            return new String[] { intent.getAction() };
                        } else {
                            UIUtils.showErrorDialog(ProcessScanActivity.this, intent);
                        }
                    } else {
                        // ignore
                    }
                } else if (intent.getAction().equals(URL_REDIRECTION_DONE)) {
                    final String emailHash = intent.getStringExtra(EMAILHASH);
                    if (intent.hasExtra(POKE_ACTION)) {
                        final String pokeAction = intent.getStringExtra(POKE_ACTION);
                        getServiceActionInfo(emailHash, pokeAction);
                    } else {
                        processEmailHash(emailHash);
                    }
                    return new String[] { intent.getAction() };
                }

                return null; // Intent was ignored
            }

        };
    }

    private void getServiceActionInfo(final String emailHash, final String pokeAction) {
        mExpectedEmailHash = emailHash;
        mExpectedAction = pokeAction;
        boolean success = mFriendsPlugin.getServiceActionInfo(mExpectedEmailHash, mExpectedAction);
        if (!success) {
            UIUtils.showLongToast(ProcessScanActivity.this, getString(R.string.scanner_communication_failure));
            finish();
        }
    }

    private void abortProcessing() {
        T.UI();
        if (mScanCommunication != null) {
            mScanCommunication.abort();
            mScanCommunication = null;
        }
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
        mExpectedEmailHash = null;
    }

    @Override
    public void finish() {
        abortProcessing();
        super.finish();
    }

    private void scannedUnknownQR(String msg, final Intent intent) {
        String title = getString(R.string.no_rogerthat_qr, msg, getString(R.string.app_name));
        String message = getString(R.string.no_rogerthat_qr, msg, getString(R.string.app_name));
        DialogInterface.OnCancelListener onCancelListener = new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                finish();
            }
        };
        SafeDialogClick onPositiveClick = new SafeDialogClick() {
            @Override
            public void safeOnClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                finish();
                startActivity(intent);
            }
        };
        SafeDialogClick onNegativeClick = new SafeDialogClick() {
            @Override
            public void safeOnClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                finish();
            }
        };

        UIUtils.showDialog(this, title, message, R.string.yes, onPositiveClick, R.string.no, onNegativeClick)
                .setOnCancelListener(onCancelListener);
        abortProcessing();
    }

    private void processUrl(final String url) {
        String trimmedLowerCaseUrl = url.trim().toLowerCase(Locale.US);
        if (trimmedLowerCaseUrl.startsWith(URL_ROGERTHAT_PREFIX)) {
            if (url.contains("?")) {
                processInvitation(url);
            } else {
                processEmailHash(url.substring(URL_ROGERTHAT_PREFIX.length()));
            }

        } else if (trimmedLowerCaseUrl.startsWith(URL_ROGERTHAT_SID_PREFIX)) {
            Matcher match = RegexPatterns.SERVICE_INTERACT_URL.matcher(url);
            match.matches();
            String emailHash = match.group(1);
            String sid = match.group(2);
            getServiceActionInfo(emailHash, sid);

        } else if (trimmedLowerCaseUrl.startsWith(SHORT_HTTPS_URL_PREFIX)) {
            mScanCommunication = new ScanCommunication(mService);
            mScanCommunication.resolveUrl(url);

        } else if (trimmedLowerCaseUrl.startsWith("http://") || trimmedLowerCaseUrl.startsWith("https://")) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            scannedUnknownQR(getString(R.string.qr_open_url, TextUtils.trimString(url, 100, true)), intent);

        } else if (trimmedLowerCaseUrl.startsWith("tel:")) {
            String number = url.substring(4);
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + number));
            scannedUnknownQR(getString(R.string.qr_call_tel, number), intent);

        } else {
            try {
                Map<String, Object> data = (Map<String, Object>) JSONValue.parse(url);
                String applicationTag = (String) data.get("t");
                JSONObject context = (JSONObject) data.get("c");

                if (CordovaSettings.APPS.contains(applicationTag) && context != null) {
                    NavigationItem ni = new NavigationItem(FontAwesome.Icon.faw_question_circle_o, "cordova", applicationTag, "");

                    String errorMessage = ActivityUtils.canOpenNavigationItem(this, ni);
                    if (errorMessage == null) {
                        Bundle extras = new Bundle();
                        extras.putString(ActionScreenActivity.CONTEXT, context.toJSONString());
                        ActivityUtils.goToActivity(this, ni, false, extras);
                        finish();
                        return;
                    }
                    L.d(errorMessage);
                }

            } catch (Exception e) {
                L.d("Failed to process url into an application", e);
            }
            L.d("Cannot process url " + url);
            UIUtils.showLongToast(this, getResources().getString(R.string.unrecognized_scan_result));
            finish();
        }
    }

    private void startSpinner(boolean isScan) {
        String title = getString(isScan ? R.string.processing_scan : R.string.processing);
        String message = getString(R.string.retrieving_information);
        mProgressDialog = UIUtils.showProgressDialog(this, title, message, true, true,
            new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    T.UI();
                    finish();
                }
            });
    }

    private void checkFriendByEmailHash(final String emailHash) {
        T.UI();

        final FriendsPlugin plugin = mService.getPlugin(FriendsPlugin.class);
        String email = null;

        try {
            email = plugin.getEmailByEmailHash(emailHash.getBytes("US-ASCII"));
        } catch (UnsupportedEncodingException e) {
            // should never happen
        }

        if (email != null) {
            final String friendName = plugin.getName(email);
            finish();
            if (friendName != null) {
                if (email.equals(mService.getIdentityStore().getIdentity().getEmail())) {
                    UIUtils.showLongToast(this, getString(R.string.scanned_yourself, getString(R.string.app_name)));
                } else {
                    showFriendDetailActivity(email, friendName);
                }
            } else {
                showFriendDetailActivity(email, email);
            }
        } else {
            mExpectedEmailHash = emailHash;
            requestFriendInfoByEmailHash(emailHash);
        }
    }

    private void showFriendDetailActivity(final String email, final String name) {
        mFriendsPlugin.launchDetailActivity(this, email);
        UIUtils.showLongToast(this, getString(R.string.already_friend, name));
    }

    private void requestFriendInfoByEmailHash(String emailHash) {

        final GetUserInfoRequestTO request = new GetUserInfoRequestTO();
        request.code = emailHash;
        request.allow_cross_app = false;

        final GetUserInfoResponseHandler handler = new GetUserInfoResponseHandler();
        handler.setCode(emailHash);

        try {
            com.mobicage.api.friends.Rpc.getUserInfo(handler, request);
        } catch (Exception e) {
            finish();
            mService.putInHistoryLog(getString(R.string.getuserinfo_failure), HistoryItem.ERROR);
        }

    }


    private void processEmailHash(final String emailHash) {
        if (emailHash != null) {
            checkFriendByEmailHash(emailHash);
        } else {
            finish();
            UIUtils.showErrorToast(this, getString(R.string.scanner_communication_failure));
        }
    }

    private void processInvitation(final String url) {
        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            L.bug(e);
            return;
        }

        String secret = null;
        String invitor = null;

        for (NameValuePair arg : URLEncodedUtils.parse(uri, "UTF-8")) {
            if (INVITATION_SECRET_ARG.equals(arg.getName())) {
                secret = arg.getValue();
            } else if (INVITATION_INVITOR_ARG.equals(arg.getName())) {
                invitor = arg.getValue();
            }

            if (secret != null && invitor != null) {
                break;
            }
        }

        if (secret == null || invitor == null) {
            L.d("I don't know how to process this URL: " + url);
        } else {
            String invitorEmailHash = url.substring(URL_ROGERTHAT_PREFIX.length(), url.indexOf("?"));
            if (!isMyEmailHash(invitorEmailHash)) {
                FriendsPlugin friendsPlugin = mService.getPlugin(FriendsPlugin.class);
                if (friendsPlugin.ackInvitationBySecret(invitorEmailHash, secret)) {
                    UIUtils.showLongToast(this, getString(R.string.invitation_successfully_accepted, invitor));
                } else {
                    UIUtils.showLongToast(this, getString(R.string.invitation_acception_failure));
                }
            }
        }
        finish();
    }

    private boolean isMyEmailHash(String otherEmailHash) {
        MyIdentity me = mService.getIdentityStore().getIdentity();
        if (me == null)
            return false;

        byte[] myEmailHash = me.getEmailHash();
        if (myEmailHash == null || myEmailHash.length == 0)
            return false;

        try {
            return myEmailHash.equals(otherEmailHash.getBytes("US-ASCII"));
        } catch (UnsupportedEncodingException e) {
            L.bug(e);
            return false;
        }
    }

}

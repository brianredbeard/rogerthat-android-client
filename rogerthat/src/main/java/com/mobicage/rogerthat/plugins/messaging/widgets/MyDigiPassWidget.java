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

package com.mobicage.rogerthat.plugins.messaging.widgets;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mobicage.api.messaging.Rpc;
import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.MainService;
import com.mobicage.rogerthat.plugins.messaging.Message;
import com.mobicage.rogerthat.plugins.messaging.MessagingPlugin;
import com.mobicage.rogerthat.plugins.messaging.MyDigiPassWidgetResult;
import com.mobicage.rogerthat.util.TextUtils;
import com.mobicage.rogerthat.util.http.HTTPUtil;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeAsyncTask;
import com.mobicage.rogerthat.util.system.SafeDialogClick;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.system.SafeViewOnClickListener;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.ui.UIUtils;
import com.mobicage.rpc.Credentials;
import com.mobicage.rpc.IncompleteMessageException;
import com.mobicage.rpc.ResponseHandler;
import com.mobicage.rpc.config.CloudConstants;
import com.mobicage.to.messaging.forms.SubmitMyDigiPassFormRequestTO;
import com.mobicage.to.messaging.forms.SubmitMyDigiPassFormResponseTO;
import com.vasco.mydigipass.sdk.MDPMobile;
import com.vasco.mydigipass.sdk.MDPResponse;
import com.vasco.mydigipass.sdk.OnMDPAuthenticationListener;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.jivesoftware.smack.util.Base64;
import org.json.simple.JSONValue;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MyDigiPassWidget extends Widget implements OnMDPAuthenticationListener {

    public static final String SCOPE_EMAIL = "email";
    public static final String SCOPE_PHONE = "phone";
    public static final String SCOPE_ADDRESS = "address";
    public static final String SCOPE_PROFILE = "profile";
    public static final String SCOPE_EID_PROFILE = "eid_profile";
    public static final String SCOPE_EID_ADDRESS = "eid_address";
    public static final String SCOPE_EID_PHOTO = "eid_photo";

    public static final String MDP_REDIRECT_URI = "mdp-" + CloudConstants.APP_ID + "://x-callback-url/mdp_callback";
    private static final int MAX_COLLAPSED_ROWS = 3;

    private boolean mCollapsed = true;
    private String mCurrentState = null;
    private String mScope;
    private List<String> mScopes;
    private MyDigiPassWidgetResult mResult;
    private List<MdpRow> mMdpData;
    private ImageView mImageView;
    private LinearLayout mAuthenticateView;
    private ProgressDialog mProgressDialog;
    private ListView mResultListView;
    private MdpAdapter mResultAdapter = new MdpAdapter();
    private Typeface mFontAwesomeTypeFace;
    private MDPMobile mMdpMobile;
    private TextView mMdpTextView;

    private static class MdpRow {
        public int faIcon;
        public String value;

        public MdpRow(int faIcon, String value) {
            super();
            this.faIcon = faIcon;
            this.value = value;
        }
    }

    private class MdpAdapter extends BaseAdapter {

        private ColorStateList mDefaultTextColor;

        @Override
        public int getCount() {
            T.UI();
            if (mMdpData == null)
                return 0;

            if (mCollapsed)
                return Math.min(MAX_COLLAPSED_ROWS, mMdpData.size());

            return mMdpData.size();
        }

        @Override
        public Object getItem(int position) {
            T.UI();
            return null;
        }

        @Override
        public long getItemId(int position) {
            T.UI();
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            T.UI();
            View v = convertView;
            if (v == null) {
                v = LayoutInflater.from(getContext()).inflate(R.layout.mdp_list_item, parent, false);
            }

            if (mFontAwesomeTypeFace == null) {
                mFontAwesomeTypeFace = new FontAwesome().getTypeface(mActivity);
            }

            final TextView faTextView = (TextView) v.findViewById(R.id.fa_text_view);
            final TextView textView = (TextView) v.findViewById(R.id.text_view);
            final TextView showMore = (TextView) v.findViewById(R.id.show_more);

            if (mDefaultTextColor == null) {
                mDefaultTextColor = textView.getTextColors();
            }

            if (isShowMore(position)) {
                faTextView.setVisibility(View.GONE);
                textView.setVisibility(View.GONE);
                showMore.setVisibility(View.VISIBLE);
            } else {
                faTextView.setVisibility(View.VISIBLE);
                textView.setVisibility(View.VISIBLE);
                showMore.setVisibility(View.GONE);

                final MdpRow row = mMdpData.get(position);
                faTextView.setText(row.faIcon);
                faTextView.setTypeface(mFontAwesomeTypeFace);
                textView.setText(row.value);
            }

            return v;
        }

        private boolean isShowMore(int position) {
            return mCollapsed && position == MAX_COLLAPSED_ROWS - 1 && mMdpData.size() > MAX_COLLAPSED_ROWS;
        }

        private void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (isShowMore(position)) {
                mCollapsed = false;
                mResultAdapter.notifyDataSetChanged();
                UIUtils.setListViewHeightBasedOnItems(mResultListView);
            }
        }
    }

    public MyDigiPassWidget(Context context) {
        super(context);
    }

    public MyDigiPassWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void initializeWidget() {
        mAuthenticateView = (LinearLayout) findViewById(R.id.authenticate_view);
        mAuthenticateView.setOnClickListener(new SafeViewOnClickListener() {
            @Override
            public void safeOnClick(View v) {
                authenticate();
            }
        });

        mProgressDialog = new ProgressDialog(mActivity);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setMessage(mActivity.getString(R.string.processing));
        mProgressDialog.setCancelable(false);

        mImageView = (ImageView) findViewById(R.id.image_view);
        mMdpTextView = (TextView) findViewById(R.id.mdp_text_view);
        mResultListView = (ListView) findViewById(R.id.list_view);
        mResultListView.setAdapter(mResultAdapter);
        mResultListView.setScrollContainer(false);
        mResultListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mResultAdapter.onItemClick(parent, view, position, id);
            }
        });

        mScope = (String) mWidgetMap.get("scope");
        if (TextUtils.isEmptyOrWhitespace(mScope)) {
            mScope = SCOPE_EID_PROFILE;
        }
        mScopes = Collections.unmodifiableList(Arrays.asList(mScope.split(" ")));

        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) mWidgetMap.get("value");
        if (result != null) {
            try {
                mResult = new MyDigiPassWidgetResult(result);
                showResult();
            } catch (IncompleteMessageException e) {
                L.bug(e); // Should never happen
            }
        }
    }

    @Override
    public void putValue() {
        T.UI();
        mWidgetMap.put("value", mResult == null ? null : mResult.toJSONMap());
    }

    @Override
    public MyDigiPassWidgetResult getWidgetResult() {
        return mResult;
    }

    @Override
    public boolean proceedWithSubmit(final String buttonId) {
        if (Message.POSITIVE.equals(buttonId)) {
            if (mResult == null) {
                String title = mActivity.getString(R.string.not_authenticated);
                String message = mActivity.getString(R.string.authenticate_first);
                String positiveCaption = mMdpTextView.getText().toString();
                String negativeCaption = mActivity.getString(R.string.cancel);
                SafeDialogClick onPositiveClick = new SafeDialogClick() {
                    @Override
                    public void safeOnClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        authenticate();
                    }
                };
                UIUtils.showDialog(mActivity, title, message, positiveCaption, onPositiveClick, negativeCaption, null);
                return false;
            }
        }
        return true;
    }

    @Override
    public void submit(String buttonId, long timestamp) throws Exception {
        T.UI();
        final SubmitMyDigiPassFormRequestTO request = new SubmitMyDigiPassFormRequestTO();
        request.button_id = buttonId;
        request.message_key = mMessage.key;
        request.parent_message_key = mMessage.parent_key;
        request.timestamp = timestamp;

        if (Message.POSITIVE.equals(buttonId)) {
            L.d("Submit MYDIGIPASS " + mWidgetMap);
            request.result = mResult;
        }

        final boolean isSentByJSMFR = (mMessage.flags & MessagingPlugin.FLAG_SENT_BY_JSMFR) == MessagingPlugin.FLAG_SENT_BY_JSMFR;
        if (isSentByJSMFR) {
            mPlugin.answerJsMfrMessage(mMessage, request.toJSONMap(),
                "com.mobicage.api.messaging.submitMyDigiPassForm", mActivity, mParentView);
        } else {
            Rpc.submitMyDigiPassForm(new ResponseHandler<SubmitMyDigiPassFormResponseTO>(), request);
        }
    }

    @SuppressWarnings("unchecked")
    public static String valueString(Context context, Map<String, Object> widget) {
        T.UI();
        final Map<String, Object> jsonResult = (Map<String, Object>) widget.get("value");
        if (jsonResult == null) {
            return "";
        }

        final MyDigiPassWidgetResult result;
        try {
            result = new MyDigiPassWidgetResult(jsonResult);
        } catch (IncompleteMessageException e) {
            L.bug(e);
            return "";
        }

        final List<String> parts = new ArrayList<String>();
        if (result.email != null) {
            parts.add(String.format("%s: %s", context.getString(R.string.email), result.email));
        }
        if (result.phone != null) {
            parts.add(String.format("%s: %s", context.getString(R.string.phone), result.phone));
        }
        if (result.address != null) {
            parts.add(String.format("%s: %s", context.getString(R.string.address), result.address.getDisplayValue()
                .replace("\n", ", ")));
        }
        if (result.profile != null) {
            parts.add(String.format("%s: %s", context.getString(R.string.profile), result.profile.getDisplayValue()));
        }
        if (result.eid_address != null) {
            parts.add(String.format("eID %s: %s", context.getString(R.string.address), result.eid_address
                .getDisplayValue().replace("\n", ", ")));
        }
        if (result.eid_profile != null) {
            parts.add(String.format("eID %s: %s", context.getString(R.string.profile),
                result.eid_profile.getDisplayValue()));
        }
        if (result.eid_photo != null) {
            parts.add(String.format("eID %s", context.getString(R.string.photo)));
        }
        return android.text.TextUtils.join("\n", parts);
    }

    private String getFormattedDateString(String dateStr) {
        if (TextUtils.isEmptyOrWhitespace(dateStr))
            return "";

        try {
            final Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dateStr);
            return DateFormat.getDateInstance(DateFormat.MEDIUM).format(date);
        } catch (ParseException e) {
            L.bug(e);
            return dateStr;
        }
    }

    private String getFormattedDateTimeString(String dateTimeStr) {
        if (TextUtils.isEmptyOrWhitespace(dateTimeStr))
            return "";

        try {
            // 2015-11-10T08:15:27.834Z
            final Date date = new SimpleDateFormat("yyyy-MM-ddTHH:mm:ss.SSSZ", Locale.US).parse(dateTimeStr);
            return DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(date);
        } catch (ParseException e) {
            L.bug(e);
            return dateTimeStr;
        }
    }

    private void addMdpRow(int faIcon, String value) {
        mMdpData.add(new MdpRow(faIcon, value));
    }

    private void showResult() {
        T.UI();
        mMdpData = new ArrayList<MyDigiPassWidget.MdpRow>();
        if (mScopes.contains(SCOPE_EID_PHOTO)) {
            mImageView.setVisibility(View.VISIBLE);
            final byte[] data = Base64.decode(mResult.eid_photo);
            mImageView.setImageBitmap(BitmapFactory.decodeByteArray(data, 0, data.length));
        } else {
            mImageView.setVisibility(View.GONE);
        }

        final Context ctx = getContext();
        if (mScopes.contains(SCOPE_EID_PROFILE)) {
            addMdpRow(R.string.fa_user, mResult.eid_profile.getDisplayName());
            addMdpRow(R.string.fa_transgender, mResult.eid_profile.getDisplayGender(ctx));
            addMdpRow(R.string.fa_birthday_cake, String.format("%s, %s",
                getFormattedDateString(mResult.eid_profile.date_of_birth), mResult.eid_profile.location_of_birth));
            if (!TextUtils.isEmptyOrWhitespace(mResult.eid_profile.noble_condition)) {
                addMdpRow(R.string.fa_black_tie, mResult.eid_profile.noble_condition);
            }
            addMdpRow(R.string.fa_hourglass_half, ctx.getString(R.string.valid_from_to,
                getFormattedDateString(mResult.eid_profile.validity_begins_at),
                getFormattedDateString(mResult.eid_profile.validity_ends_at)));
            addMdpRow(R.string.fa_flag, mResult.eid_profile.nationality);
            addMdpRow(R.string.fa_home, mResult.eid_profile.issuing_municipality);
            if (!TextUtils.isEmptyOrWhitespace(mResult.eid_profile.created_at)) {
                addMdpRow(R.string.fa_clock_o, getFormattedDateTimeString(mResult.eid_profile.created_at));
            }
            addMdpRow(R.string.fa_credit_card, mResult.eid_profile.getDisplayCardInfo(ctx));
        }

        if (mScopes.contains(SCOPE_EID_ADDRESS)) {
            addMdpRow(R.string.fa_home, mResult.eid_address.getDisplayValue());
        }

        if (mScopes.contains(SCOPE_EMAIL)) {
            addMdpRow(R.string.fa_envelope, mResult.email);
        }

        if (mScopes.contains(SCOPE_PHONE)) {
            addMdpRow(R.string.fa_phone, mResult.phone);
        }

        if (mScopes.contains(SCOPE_PROFILE)) {
            addMdpRow(R.string.fa_user, mResult.profile.getDisplayName());
            if (!TextUtils.isEmptyOrWhitespace(mResult.profile.preferred_locale)) {
                addMdpRow(R.string.fa_flag, mResult.profile.getDisplayLanguage());
            }
            if (!TextUtils.isEmptyOrWhitespace(mResult.profile.born_on)) {
                addMdpRow(R.string.fa_birthday_cake, getFormattedDateString(mResult.profile.born_on));
            }
        }

        if (mScopes.contains(SCOPE_ADDRESS)) {
            addMdpRow(R.string.fa_home, mResult.address.getDisplayValue());
        }

        mAuthenticateView.setVisibility(View.GONE);
        mResultListView.setVisibility(mMdpData.size() == 0 ? View.GONE : View.VISIBLE);
        mResultAdapter.notifyDataSetChanged();

        mActivity.getMainService().postOnUIHandler(new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                UIUtils.setListViewHeightBasedOnItems(mResultListView);
            }
        });
    }

    private void authenticate() {
        if (!mActivity.getMainService().getNetworkConnectivityManager().isConnected()) {
            UIUtils.showNoNetworkDialog(mActivity);
            return;
        }

        if (mMdpMobile == null) {
            mMdpMobile = new MDPMobile(mActivity);
            mMdpMobile.setMDPAuthenticationListener(this);
        }

        if (!mMdpMobile.isMdpInstalled()) {
            showInstallOrUpdateMdpDialog(mActivity.getString(R.string.install_mdp), R.string.install);
            return;
        }

        authorizeMDP();
    }

    @SuppressWarnings("deprecation")
    private void addCredentials(HttpRequestBase request) {
        final Credentials credentials = mActivity.getMainService().getCredentials();
        request.setHeader("X-MCTracker-User",
            Base64.encodeBytes(credentials.getUsername().getBytes(), Base64.DONT_BREAK_LINES));
        request.setHeader("X-MCTracker-Pass",
            Base64.encodeBytes(credentials.getPassword().getBytes(), Base64.DONT_BREAK_LINES));
    }

    @SuppressWarnings("deprecation")
    private void authorizeMDP() {
        T.UI();
        mCurrentState = null;

        new SafeAsyncTask<Object, Object, Boolean>() {
            @Override
            protected Boolean safeDoInBackground(Object... params) {
                final HttpGet request = new HttpGet(CloudConstants.MDP_SESSION_INIT_URL);
                request.setHeader("User-Agent", MainService.getUserAgent(mActivity));
                addCredentials(request);

                final InputStream responseContent;
                try {
                    final HttpResponse response = HTTPUtil.getHttpClient().execute(request);

                    final int statusCode = response.getStatusLine().getStatusCode();
                    if (statusCode != HttpStatus.SC_OK) {
                        return false;
                    }

                    responseContent = response.getEntity().getContent();
                } catch (IOException e) {
                    return false;
                }

                @SuppressWarnings("unchecked")
                final Map<String, Object> result = (Map<String, Object>) JSONValue.parse(new InputStreamReader(
                    responseContent));
                mCurrentState = (String) result.get("state");

                mMdpMobile.setRedirectUri(MDP_REDIRECT_URI);
                mMdpMobile.setClientId((String) result.get("client_id"));
                mMdpMobile.authenticate(mCurrentState, mScope, null);
                return true;
            }

            @Override
            protected void onPreExecute() {
                T.UI();
                mProgressDialog.show();
            }

            @Override
            protected void onPostExecute(Boolean result) {
                T.UI();
                if (!Boolean.TRUE.equals(result)) {
                    UIUtils.showErrorPleaseRetryDialog(mActivity);
                }
                mProgressDialog.hide();
            }

        }.execute();
    }

    @SuppressWarnings("deprecation")
    private void mdpAuthorized(final String state, final String authorizationCode) {
        T.UI();
        mProgressDialog.show();

        new SafeAsyncTask<Object, Object, Boolean>() {
            @Override
            protected Boolean safeDoInBackground(Object... params) {
                final HttpPost request = new HttpPost(CloudConstants.MDP_SESSION_AUTHORIZED_URL);
                addCredentials(request);

                final List<NameValuePair> formParams = new ArrayList<NameValuePair>();
                formParams.add(new BasicNameValuePair("scope", mScope));
                formParams.add(new BasicNameValuePair("state", state));
                formParams.add(new BasicNameValuePair("authorization_code", authorizationCode));

                final UrlEncodedFormEntity entity;
                try {
                    entity = new UrlEncodedFormEntity(formParams, HTTP.UTF_8);
                } catch (UnsupportedEncodingException e) {
                    L.bug(e); // should never happen
                    return false;
                }
                request.setEntity(entity);

                final InputStream responseContent;
                try {
                    final HttpResponse response = HTTPUtil.getHttpClient().execute(request);

                    final int statusCode = response.getStatusLine().getStatusCode();
                    if (statusCode != HttpStatus.SC_OK) {
                        mActivity.getMainService().postOnUIHandler(new SafeRunnable() {
                            @Override
                            protected void safeRun() throws Exception {
                                UIUtils.showErrorPleaseRetryDialog(mActivity);
                            }
                        });
                        return false;
                    }

                    responseContent = response.getEntity().getContent();
                } catch (IOException e) {
                    return false;
                }

                @SuppressWarnings("unchecked")
                final Map<String, Object> result = (Map<String, Object>) JSONValue.parse(new InputStreamReader(
                    responseContent));

                if (result == null) {
                    L.bug("mdp result is null for " + responseContent);
                    return false;
                }

                if (handleError(result)) {
                    return false;
                } else {
                    try {
                        mResult = new MyDigiPassWidgetResult(result);
                    } catch (IncompleteMessageException e) {
                        L.bug(e); // should never happen
                        return false;
                    }
                }

                return true;
            }

            @Override
            protected void onPreExecute() {
                T.UI();
                mProgressDialog.show();
            }

            @Override
            protected void onPostExecute(Boolean success) {
                T.UI();
                mProgressDialog.hide();
                if (Boolean.TRUE.equals(success)) {
                    if (mResult != null) {
                        showResult();
                    }
                }
            }
        }.execute();
    }

    private boolean handleError(final Map<String, Object> result) {
        final String error = (String) result.get("error");
        if (!TextUtils.isEmptyOrWhitespace(error)) {
            mActivity.getMainService().postOnUIHandler(new SafeRunnable() {
                @Override
                protected void safeRun() throws Exception {
                    if (Boolean.TRUE.equals(result.get("mdp_update_required"))) {
                        showInstallOrUpdateMdpDialog(error, R.string.update);
                    } else {
                        UIUtils.showDialog(mActivity, null, error);
                    }
                }
            });
            return true;
        }

        return false;
    }

    private void showInstallOrUpdateMdpDialog(final String message, final int positiveButton) {
        T.UI();
        SafeDialogClick onPositiveClick = new SafeDialogClick() {
            @Override
            public void safeOnClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                mMdpMobile.openStore();
            }
        };
        UIUtils.showDialog(mActivity, "MYDIGIPASS", message, positiveButton, onPositiveClick, R.string.cancel, null);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mMdpMobile != null) {
            mMdpMobile.handleResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onMDPAuthenticationSuccess(MDPResponse response) {
        if (mCurrentState == null || !mCurrentState.equals(response.getState())) {
            L.i(String.format("MDP state mismatch. Expected %s. Got %s.", mCurrentState, response.getState()));
            return;
        }

        mdpAuthorized(response.getState(), response.getAuthorizationCode());
    }

    @Override
    public void onMDPAuthenticationFail(MDPResponse response) {
        if (mCurrentState == null || !mCurrentState.equals(response.getState())) {
            L.i(String.format("MDP state mismatch. Expected %s. Got %s.", mCurrentState, response.getState()));
            return;
        }

        UIUtils.showErrorPleaseRetryDialog(mActivity);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mResult != null) {
            mActivity.getMainService().postDelayedOnUIHandler(new SafeRunnable() {
                @Override
                protected void safeRun() throws Exception {
                    UIUtils.setListViewHeightBasedOnItems(mResultListView);
                }
            }, 100);
        }
    }
}

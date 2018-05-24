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
package com.mobicage.rogerthat;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.support.customtabs.CustomTabsIntent;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.plugins.scan.ProfileActivity;
import com.mobicage.rogerthat.util.GoogleServicesUtils;
import com.mobicage.rogerthat.util.TextUtils;
import com.mobicage.rogerthat.util.http.HTTPUtil;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeAsyncTask;
import com.mobicage.rogerthat.util.system.SafeBroadcastReceiver;
import com.mobicage.rogerthat.util.system.SafeDialogClick;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.system.SafeViewOnClickListener;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.ui.UIUtils;
import com.mobicage.rpc.config.AppConstants;
import com.mobicage.rpc.config.CloudConstants;
import com.mobicage.rpc.config.LookAndFeelConstants;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.protocol.HTTP;
import org.json.simple.JSONValue;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class SettingsActivity extends ServiceBoundPreferenceActivity {

    private String mCustomAlarm = null;
    private String mCustomTitle = null;
    private ProgressDialog mProgressDialog;

    private static final int REQUESTCODE_PICK_ALARM = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.options);

        if (!AppConstants.FRIENDS_ENABLED) {
            // Hide "Invisible mode"
            Preference prefInvisibleMode = findPreference(MainService.PREFERENCE_TRACKING);
            PreferenceCategory generalCategory = (PreferenceCategory) findPreference("general");
            generalCategory.removePreference(prefInvisibleMode);
        }

        if (!AppConstants.Security.ENABLED) {
            // Hide "Security settings"
            Preference securityPref = findPreference(MainService.PREFERENCE_SECURITY);
            PreferenceCategory accountCategory = (PreferenceCategory) findPreference("account");
            accountCategory.removePreference(securityPref);
        }

        setContentView(R.layout.settings);

        final SharedPreferences options = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);
        mCustomAlarm = options.getString(MainService.PREFERENCE_ALARM_SOUND, null);
        mCustomTitle = options.getString(MainService.PREFERENCE_ALARM_TITLE, null);
        final Preference alarmPref = findPreference(MainService.PREFERENCE_ALARM_SOUND);
        alarmPref.setSummary((mCustomTitle == null) ? getString(R.string.alarm_sound_default) : mCustomTitle);
        alarmPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                showAlarmDialog(alarmPref);
                return true;
            }
        });

        final Preference profilePref = findPreference(MainService.PREFERENCE_MY_PROFILE);
        profilePref.setSummary(getString(R.string.view_my_profile));
        profilePref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(SettingsActivity.this, ProfileActivity.class);
                startActivity(intent);
                return true;
            }
        });

        if (AppConstants.Security.ENABLED) {
            final Preference securityPref = findPreference(MainService.PREFERENCE_SECURITY);
            securityPref.setSummary(getString(R.string.view_security_settings));
            securityPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(SettingsActivity.this, SecuritySettingsActivity.class);
                    startActivity(intent);
                    return true;
                }
            });
        }

        final Preference dataDownloadPref = findPreference(MainService.PREFERENCE_DATA_DOWNLOAD);
        dataDownloadPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(SettingsActivity.this, DataDownloadActivity.class);
                startActivity(intent);
                return true;
            }
        });

        final Preference logoutPref = findPreference(MainService.PREFERENCE_LOGOUT);
        logoutPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                final String title = SettingsActivity.this.getString(R.string.log_out);
                final String message = SettingsActivity.this.getString( R.string.are_you_sure_you_want_to_do_this);
                final String positiveBtn = SettingsActivity.this.getString(R.string.yes);
                final String negativeButtonCaption = SettingsActivity.this.getString(R.string.no);
                SafeDialogClick positiveClick = new SafeDialogClick() {
                    @Override
                    public void safeOnClick(DialogInterface dialog, int id) {
                        T.UI();
                        dialog.dismiss();
                        logoutAccount();
                    }
                };
                UIUtils.showDialog(SettingsActivity.this, title, message, positiveBtn, positiveClick, negativeButtonCaption, null);
                return true;
            }
        });

        final Preference deleteAccountPref = findPreference(MainService.PREFERENCE_DELETE_ACCOUNT);
        deleteAccountPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                final String title = SettingsActivity.this.getString(R.string.delete_account);
                final String message = SettingsActivity.this.getString( R.string.are_you_sure_you_want_to_do_this);
                final String positiveBtn = SettingsActivity.this.getString(R.string.yes);
                final String negativeButtonCaption = SettingsActivity.this.getString(R.string.no);
                SafeDialogClick positiveClick = new SafeDialogClick() {
                    @Override
                    public void safeOnClick(DialogInterface dialog, int id) {
                        T.UI();
                        dialog.dismiss();
                        deleteAccount();
                    }
                };
                UIUtils.showDialog(SettingsActivity.this, title, message, positiveBtn, positiveClick, negativeButtonCaption, null);
                return true;
            }
        });

        final Preference privacyPolicyPref = findPreference(MainService.PREFERENCE_PRIVACY_POLICY);
        privacyPolicyPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                openUrl(AppConstants.PRIVACY_POLICY_URL);
                return true;
            }
        });

        final Preference termsOfServicePref = findPreference(MainService.PREFERENCE_TERMS_OF_SERVICE);
        termsOfServicePref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                openUrl(AppConstants.TERMS_OF_SERVICE_URL);
                return true;
            }
        });

        Toolbar bar = (Toolbar) findViewById(R.id.toolbar);
        bar.setNavigationOnClickListener(new SafeViewOnClickListener() {
            @Override
            public void safeOnClick(View v) {
                onBackPressed();
            }
        });

        IntentFilter filter = new IntentFilter(MainService.PREFERENCES_UPDATE_INTENT);
        registerReceiver(mBroadcastReceiver, filter);

        setTitle(R.string.settings);
    }

    @Override
    protected void onServiceBound() {
        List<NavigationItem> items = new ArrayList<>();
        items.addAll(Arrays.asList(LookAndFeelConstants.getNavigationItems(mService)));
        items.addAll(Arrays.asList(LookAndFeelConstants.getNavigationFooterItems(mService)));
        boolean shouldHideStream = true;
        for (NavigationItem item : items) {
            if (item.action.equals("stream")) {
                shouldHideStream = false;
                break;
            }
        }
        if (shouldHideStream) {
            // Hide "Activity stream"
            Preference prefStream = findPreference(MainService.PREFERENCE_STREAM_ONLY_IMPORTANT);
            PreferenceCategory generalCategory = (PreferenceCategory) findPreference("general");
            generalCategory.removePreference(prefStream);
        }

        final Preference appInfoPref = findPreference(MainService.PREFERENCE_APP_INFO);
        appInfoPref.setSummary(GoogleServicesUtils.getAppVersion(mService));
        appInfoPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(SettingsActivity.this, AboutActivity.class);
                startActivity(intent);
                return true;
            }
        });
    }

    @Override
    protected void onServiceUnbound() {

    }

    private void showAlarmDialog(final Preference alarmPref) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_singlechoice);
        adapter.add(getString(R.string.alarm_sound_default));
        adapter.add(getString(R.string.alarm_sound_custom));

        AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
        builder.setTitle(R.string.alarm_sound);
        builder.setSingleChoiceItems(adapter, (mCustomAlarm == null) ? 0 : 1, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    mCustomAlarm = null;
                    mCustomTitle = null;
                } else if (which == 1) {
                    Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, R.string.alarm_sound_custom);
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL);
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
                    if (mCustomAlarm != null)
                        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(mCustomAlarm));
                    startActivityForResult(intent, REQUESTCODE_PICK_ALARM);
                }
            }
        });
        builder.setNegativeButton(R.string.cancel, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final SharedPreferences options = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);
                mCustomAlarm = options.getString(MainService.PREFERENCE_ALARM_SOUND, null);
                mCustomTitle = options.getString(MainService.PREFERENCE_ALARM_TITLE, null);
            }
        });
        builder.setPositiveButton(R.string.ok, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final SharedPreferences options = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);
                final SharedPreferences.Editor editor = options.edit();
                editor.putString(MainService.PREFERENCE_ALARM_SOUND, mCustomAlarm);
                editor.putString(MainService.PREFERENCE_ALARM_TITLE, mCustomTitle);

                L.d("Setting [" + MainService.PREFERENCE_ALARM_SOUND + "] to " + mCustomAlarm);
                L.d("Setting [" + MainService.PREFERENCE_ALARM_TITLE + "] to " + mCustomTitle);
                if (editor.commit()) {
                    L.d("Successfully updated preferences");
                    if (mCustomTitle == null) {
                        alarmPref.setSummary(R.string.alarm_sound_default);
                    } else {
                        alarmPref.setSummary(mCustomTitle);
                    }
                } else {
                    L.d("Failed to update preferences");
                }
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        int buttonId1 = getResources().getIdentifier("android:id/button1", null, null);
        ((Button) dialog.findViewById(buttonId1)).setTextColor(LookAndFeelConstants.getPrimaryColor(this));
        int buttonId2 = getResources().getIdentifier("android:id/button2", null, null);
        ((Button) dialog.findViewById(buttonId2)).setTextColor(LookAndFeelConstants.getPrimaryColor(this));
    }

    private void openUrl(String url) {
        CustomTabsIntent.Builder customTabsBuilder = new CustomTabsIntent.Builder();
        CustomTabsIntent customTabsIntent = customTabsBuilder.build();
        customTabsIntent.launchUrl(SettingsActivity.this, Uri.parse(url));
    }

    private void initializeProgressDialog() {
        if (mProgressDialog != null) {
            return;
        }
        mProgressDialog = new ProgressDialog(SettingsActivity.this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setMessage(SettingsActivity.this.getString(R.string.processing));
        mProgressDialog.setCancelable(false);
    }

    private void logoutAccount() {
        wipeDeviceOnSuccess(CloudConstants.ACCOUNT_LOGOUT_URL);
    }

    private void deleteAccount() {
        T.UI();
        wipeDeviceOnSuccess(CloudConstants.ACCOUNT_DELETE_URL);
    }

    private void wipeDeviceOnSuccess(final String url) {
        T.UI();
        initializeProgressDialog();

        new SafeAsyncTask<Object, Object, String>() {
            @Override
            protected String safeDoInBackground(Object... params) {
                final HttpPost request  = HTTPUtil.getHttpPost(SettingsActivity.this, url);
                HTTPUtil.addCredentials(mService, request);

                final List<NameValuePair> nameValuePairs = new ArrayList<>();
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
                                UIUtils.showErrorPleaseRetryDialog(SettingsActivity.this);
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
                                    UIUtils.showErrorPleaseRetryDialog(SettingsActivity.this);
                                }
                            });
                        } else {
                            final String errorMessage = (String) responseMap.get("error");
                            mService.postOnUIHandler(new SafeRunnable() {
                                @Override
                                protected void safeRun() throws Exception {
                                    UIUtils.showDialog(SettingsActivity.this, null, errorMessage);
                                }
                            });
                        }
                        return null;
                    }
                    final String reason = (String) responseMap.get("reason");
                    if (TextUtils.isEmptyOrWhitespace(reason)) {
                        return getString(R.string.device_was_unregistered);
                    }
                    return reason;

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
            protected void onPostExecute(String reason) {
                T.UI();
                mProgressDialog.hide();
                if (reason != null) {
                    mService.wipe(0, reason);
                }
            }
        }.execute();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
    }

    private final BroadcastReceiver mBroadcastReceiver = new SafeBroadcastReceiver() {
        @Override
        public String[] onSafeReceive(Context context, Intent intent) {
            T.UI();
            if (MainService.PREFERENCES_UPDATE_INTENT.equals(intent.getAction())) {
                SharedPreferences options = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);
                final boolean tracking = options.getBoolean(MainService.PREFERENCE_TRACKING, false);

                // I cannot get PreferenceActivity.onContentChanged() to work... so this poor mans solution to
                // dynamically
                // update checkboxes when location tracking setting changes on server
                ((CheckBoxPreference) findPreference(MainService.PREFERENCE_TRACKING)).setChecked(tracking);
            }
            return null;
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUESTCODE_PICK_ALARM && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            if (uri == null)
                return; // Nothing chosen

            mCustomAlarm = uri.toString();
            final Ringtone ringtone = RingtoneManager.getRingtone(this, uri);
            if (ringtone == null)
                return;
            mCustomTitle = ringtone.getTitle(this);
            if (mCustomTitle == null) {
                mCustomTitle = "<" + getString(R.string.alarm_sound_custom) + ">";
            }
        }
    }
}

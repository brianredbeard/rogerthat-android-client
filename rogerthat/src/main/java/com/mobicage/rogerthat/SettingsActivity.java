/*
 * Copyright 2016 Mobicage NV
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
 * @@license_version:1.1@@
 */
package com.mobicage.rogerthat;

import android.app.Activity;
import android.app.AlertDialog;
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
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.plugins.scan.ProfileActivity;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeBroadcastReceiver;
import com.mobicage.rogerthat.util.system.SafeViewOnClickListener;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rpc.config.AppConstants;

public class SettingsActivity extends ServiceBoundPreferenceActivity {

    private String mCustomAlarm = null;
    private String mCustomTitle = null;

    private static final int REQUESTCODE_PICK_ALARM = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.options);
        if (!AppConstants.FRIENDS_ENABLED) {
            // Hide Invisable mode
            Preference prefInvisibleMode = findPreference(MainService.PREFERENCE_TRACKING);
            ((PreferenceGroup) findPreference(MainService.PREFERENCES_KEY)).removePreference(prefInvisibleMode);
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
        // todo ruben nav bar
    }

    @Override
    protected void onServiceBound() {
        final Preference aboutPref = findPreference(MainService.PREFERENCE_ABOUT);
        aboutPref.setSummary(getString(R.string.about_version, mService.getMajorVersion(), mService.getMinorVersion()));
        aboutPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
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
        builder.create().show();
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

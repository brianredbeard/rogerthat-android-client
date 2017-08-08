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

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;
import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.security.SecurityUtils;
import com.mobicage.rogerthat.util.system.SafeDialogClick;
import com.mobicage.rogerthat.util.system.SafeViewOnClickListener;
import com.mobicage.rogerthat.util.ui.UIUtils;
import com.mobicage.rpc.config.AppConstants;
import com.mobicage.rpc.config.LookAndFeelConstants;

public class SecurityKeyActivity extends ServiceBoundActivity {

    public static final String KEY_ALGORITHM = "KEY_ALGORITHM";
    public static final String KEY_NAME = "KEY_NAME";
    public static final String SHOW_FINISHED_BUTTON = "SHOW_FINISHED_BUTTON ";

    private static final int[] RESOURCES = new int[]{R.id.security_settings_no_pin, R.id
            .security_settings_pin_result, R.id.spinner};

    private String mKeyAlgorithm;
    private String mKeyName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.security);

        final Intent intent = getIntent();
        mKeyAlgorithm = intent.getStringExtra(KEY_ALGORITHM);
        mKeyName = intent.getStringExtra(KEY_NAME);
        final boolean showFinishedBtn = intent.getBooleanExtra(SHOW_FINISHED_BUTTON, false);

        setContentView(R.layout.security_key);

        show(R.id.spinner);

        final ImageView header = (ImageView) findViewById(AppConstants.FULL_WIDTH_HEADERS ? R.id
                .full_width_rogerthat_logo : R.id.rogerthat_logo);
        header.setVisibility(View.VISIBLE);


        // Colours
        final int primaryIconColor = LookAndFeelConstants.getPrimaryColor(this);
        ((ImageView) findViewById(R.id.security_usage_icon)).setImageDrawable(new IconicsDrawable(this, FontAwesome
                .Icon.faw_lock).color(primaryIconColor).sizeDp(75));

        findViewById(R.id.setup_pin).setOnClickListener(new SafeViewOnClickListener() {
            @Override
            public void safeOnClick(View v) {
                setupPin();
            }
        });

        if (showFinishedBtn) {
            findViewById(R.id.backup_key_finished).setOnClickListener(new SafeViewOnClickListener() {
                @Override
                public void safeOnClick(View v) {
                    keyBackupFinished();
                }
            });

        } else {
            findViewById(R.id.backup_key_finished).setVisibility(View.GONE);
        }
    }

    @Override
    protected void onServiceBound() {
        if (!SecurityUtils.isPinSet(mService)) {
            L.d("No pin found. Setting up pin and creating key.");
            show(R.id.security_settings_no_pin);
        } else if (SecurityUtils.hasKey(mService, "private", mKeyAlgorithm, mKeyName, null)) {
            L.d("Private key already exists. Showing seed.");
            getSeed();
        } else {
            L.d("Key doesn't exist yet. Creating key.");
            createKey();
        }
    }

    @Override
    protected void onServiceUnbound() {
    }

    private void show(int visibleId) {
        for (int id : RESOURCES) {
            findViewById(id).setVisibility(id == visibleId ? View.VISIBLE : View.GONE);
        }
    }

    private void setupPin() {
        mService.setupPin(new MainService.SecurityCallback<String>() {
            @Override
            public void onSuccess(String result) {
                createKey();
            }

            @Override
            public void onError(String code, String errorMessage) {
                error(code, errorMessage);
            }
        });
    }

    private void getSeed() {
        mService.getSeed(mKeyAlgorithm, mKeyName, null, new MainService.SecurityCallback<String>() {
            @Override
            public void onSuccess(String seed) {
                showSeed(seed);
            }

            @Override
            public void onError(String code, String errorMessage) {
                error(code, errorMessage);
            }
        });
    }

    private void createKey() {
        mService.createKeyPair(mKeyAlgorithm, mKeyName, null, null,
                new MainService.SecurityCallback<MainService.CreateKeyPairResult>() {
                    @Override
                    public void onSuccess(MainService.CreateKeyPairResult r) {
                        showSeed(r.seed);
                    }

                    @Override
                    public void onError(String code, String errorMessage) {
                        error(code, errorMessage);
                    }
                });
    }

    private void showSeed(final String seed) {
        show(R.id.security_settings_pin_result);

        TextView explanationTextView = (TextView) findViewById(R.id.backup_security_key_instructions);
        explanationTextView.setText(getString(R.string.backup_security_key_instructions, AppConstants
                .Security.APP_KEY_NAME));

        TextView algorithmTextView = (TextView) findViewById(R.id.algorithm);
        algorithmTextView.setText(getString(R.string.algorithm) + ": " + mKeyAlgorithm);

        TextView keyNameTextView = (TextView) findViewById(R.id.key_name);
        keyNameTextView.setText(getString(R.string.key_name) + ": " + mKeyName);

        TextView seedTextView = (TextView) findViewById(R.id.seed);
        seedTextView.setText(seed);
    }

    private void keyBackupFinished() {
        final String message = getString(R.string.backup_security_key_later, getString(R.string.settings), getString
                (R.string.security), AppConstants.Security.APP_KEY_NAME);

        UIUtils.showDialog(SecurityKeyActivity.this, null, message, new SafeDialogClick() {
            @Override
            public void safeOnClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                setResult(RESULT_OK);
                finish();
            }
        });
    }

    private void error(String code, String errorMessage) {
        L.bug("{code=\"" + code + "\", errorMessage=\"" + errorMessage + "\"}");

        final Intent i = new Intent();
        i.putExtra("code", code);
        i.putExtra("errorMessage", errorMessage);
        setResult(RESULT_CANCELED, i);
        finish();
    }

    @Override
    public void finish() {
        super.finish();
    }
}

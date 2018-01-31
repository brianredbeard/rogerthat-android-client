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

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatButton;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.security.SecurityUtils;
import com.mobicage.rogerthat.util.system.SafeDialogClick;
import com.mobicage.rogerthat.util.system.SafeViewOnClickListener;
import com.mobicage.rogerthat.util.ui.UIUtils;
import com.mobicage.rpc.config.LookAndFeelConstants;

import static com.mobicage.rogerthat.SecurityKeyActivity.KEY_ALGORITHM;
import static com.mobicage.rogerthat.SecurityKeyActivity.KEY_NAME;

public class ImportSecurityKeyActivity extends ServiceBoundActivity implements AdapterView.OnItemSelectedListener, View.OnCreateContextMenuListener {

    private String mSelectedAlgorithm;
    private String mKeyName;

    private static final int[] RESOURCES = new int[]{R.id.security_settings_no_pin, R.id
            .security_settings_pin};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.import_key);
        setContentView(R.layout.security_key_import);

        TextView algorithmTextView = (TextView) findViewById(R.id.algorithm_text_view);
        algorithmTextView.setTextColor(LookAndFeelConstants.getPrimaryColor(this));

        final Spinner algorithmSpinner = (Spinner) findViewById(R.id.algorithm);
        algorithmSpinner.setAdapter(getArrayAdapter());
        algorithmSpinner.setOnItemSelectedListener(this);

        final AppCompatButton importKeyButton = (AppCompatButton) findViewById(R.id.import_key);
        importKeyButton.setOnClickListener(new SafeViewOnClickListener() {
            @Override
            public void safeOnClick(View v) {
                importKey();
            }
        });

        findViewById(R.id.setup_pin).setOnClickListener(new SafeViewOnClickListener() {
            @Override
            public void safeOnClick(View v) {
                setupPin();
            }
        });

        Intent intent = getIntent();

        String keyAlgorithm = intent.getStringExtra(KEY_ALGORITHM);
        if (keyAlgorithm != null) {
            mSelectedAlgorithm = keyAlgorithm;
            findViewById(R.id.algorithm_container).setVisibility(View.GONE);
        }
        mKeyName = intent.getStringExtra(KEY_NAME);
        if (mKeyName != null) {
            EditText keyNameView = (EditText) findViewById(R.id.key_name);
            keyNameView.setText(mKeyName);
            keyNameView.setEnabled(false);
        }

        if (mSelectedAlgorithm != null) {
            if (mKeyName == null) {
                findViewById(R.id.key_name).requestFocus();
            } else {
                findViewById(R.id.seed).requestFocus();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mKeyName == null) {
            UIUtils.hideKeyboard(this, findViewById(R.id.key_name));
        } else {
            UIUtils.hideKeyboard(this, findViewById(R.id.seed));
        }
    }

    @Override
    protected void onServiceBound() {
        if (!SecurityUtils.isPinSet(mService)) {
            L.d("No pin found. Setting up pin and creating key.");
            show(R.id.security_settings_no_pin);
        }
    }

    @Override
    protected void onServiceUnbound() {

    }

    private void setupPin() {
        mService.setupPin(new MainService.SecurityCallback<String>() {
            @Override
            public void onSuccess(String result) {
                show(R.id.security_settings_pin);
            }

            @Override
            public void onError(String code, String errorMessage) {
                error(code, errorMessage);
            }
        });
    }

    private void show(int visibleId) {
        for (int id : RESOURCES) {
            findViewById(id).setVisibility(id == visibleId ? View.VISIBLE : View.GONE);
        }
    }

    private void error(String code, String errorMessage) {
        if ("user_cancelled_pin_input".equals(code)) {
            L.e("{code=\"" + code + "\", errorMessage=\"" + errorMessage + "\"}");
        } else {
            L.bug("{code=\"" + code + "\", errorMessage=\"" + errorMessage + "\"}");
        }
    }

    protected void importKey() {
        String keyName = ((EditText) findViewById(R.id.key_name)).getText().toString();
        String seed = ((EditText) findViewById(R.id.seed)).getText().toString();
        mService.createKeyPair(mSelectedAlgorithm, keyName, null, seed, new MainService.SecurityCallback<MainService
                .CreateKeyPairResult>() {
            @Override
            public void onSuccess(MainService.CreateKeyPairResult result) {
                UIUtils.showLongToast(mService, R.string.import_key_succeeded);
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onError(String code, String errorMessage) {
                error(code, errorMessage);
                if (!"user_cancelled_pin_input".equals(code)) {
                    String message;
                    if ("unknown_error_occurred".equals(code)) {
                        message = getString(R.string.import_key_failed);
                    } else {
                        message = errorMessage;
                    }
                    UIUtils.showDialog(ImportSecurityKeyActivity.this, getString(R.string.activity_error), message, new SafeDialogClick() {
                        @Override
                        public void safeOnClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
                }
            }
        });
    }

    @NonNull
    private ArrayAdapter<String> getArrayAdapter() {
        mSelectedAlgorithm = SecurityUtils.PUBLIC_KEYS_ALGORITHMS.get(0);

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                SecurityUtils.PUBLIC_KEYS_ALGORITHMS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapter;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // First item is disable and it is used for hint
        if (position > 0) {
            mSelectedAlgorithm = (String) parent.getItemAtPosition(position);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        L.d("Nothing selected");
    }

}

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
import android.os.Bundle;
import android.support.annotation.NonNull;
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

public class ImportSecurityKeyActivity extends ServiceBoundActivity implements AdapterView.OnItemSelectedListener {

    private String mSelectedAlgorithm;

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

        findViewById(R.id.import_key).setOnClickListener(new SafeViewOnClickListener() {
            @Override
            public void safeOnClick(View v) {
                importKey();
            }
        });
    }

    @Override
    protected void onServiceBound() {

    }

    @Override
    protected void onServiceUnbound() {

    }

    private void error(String code, String errorMessage) {
        L.bug("{code=\"" + code + "\", errorMessage=\"" + errorMessage + "\"}");
    }

    protected void importKey() {
        String keyName = ((EditText) findViewById(R.id.key_name)).getText().toString();
        String seed = ((EditText) findViewById(R.id.seed)).getText().toString();
        mService.createKeyPair(mSelectedAlgorithm, keyName, null, seed, new MainService.SecurityCallback<MainService
                .CreateKeyPairResult>() {
            @Override
            public void onSuccess(MainService.CreateKeyPairResult result) {
                UIUtils.showLongToast(mService, R.string.import_key_succeeded);
                finish();
            }

            @Override
            public void onError(String code, String errorMessage) {
                error(code, errorMessage);
                UIUtils.showDialog(mService, getString(R.string.activity_error), getString(R.string
                        .import_key_failed), new SafeDialogClick() {
                    @Override
                    public void safeOnClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
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

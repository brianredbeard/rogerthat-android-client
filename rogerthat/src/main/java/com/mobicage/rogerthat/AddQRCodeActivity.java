/*
 * Copyright 2017 Mobicage NV
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
 * @@license_version:1.2@@
 */

package com.mobicage.rogerthat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.plugins.messaging.widgets.KeyboardType;
import com.mobicage.rogerthat.plugins.scan.ScanTabActivity;
import com.mobicage.rogerthat.plugins.system.QRCode;
import com.mobicage.rogerthat.plugins.system.SystemPlugin;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeDialogInterfaceOnClickListener;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.system.SafeViewOnClickListener;
import com.mobicage.rogerthat.util.system.SystemUtils;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.ui.UIUtils;

public class AddQRCodeActivity extends ServiceBoundActivity {

    private ProgressDialog mProgressDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.add_qr_code);
        setTitle(getString(R.string.scan_qr_code));

        findViewById(R.id.scan).setOnClickListener(new SafeViewOnClickListener() {
            @Override
            public void safeOnClick(View v) {
                showZXingActivity();
            }
        });
    }

    @Override
    protected void onServiceBound() {
    }

    @Override
    protected void onServiceUnbound() {
    }

    @Override
    protected void onStop() {
        hideProgressDialog();
        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        T.UI();
        if (resultCode == RESULT_OK) {
            // No need to do anything if requestCode == ScanTabActivity.MARKET_INSTALL_RESULT
            if (requestCode == ScanTabActivity.ZXING_SCAN_RESULT) {
                final String rawScanResult = intent.getStringExtra("SCAN_RESULT");
                if (rawScanResult == null) {
                    UIUtils.showLongToast(AddQRCodeActivity.this, getString(R.string.scanner_failure));
                } else {
                    L.d("Scanned " + rawScanResult);
                    // Ask for the QR code name
                    final View view = getLayoutInflater().inflate(R.layout.add_qr_code_name, null);
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this)
                            .setView(view)
                            .setTitle(R.string.qr_code_scanned)
                            .setPositiveButton(R.string.save, new SafeDialogInterfaceOnClickListener() {
                                @Override
                                public void safeOnClick(DialogInterface dialog, int which) {
                                    final EditText editText = (EditText) view.findViewById(R.id.qr_code_name);
                                    final SystemPlugin systemPlugin = mService.getPlugin(SystemPlugin.class);
                                    systemPlugin.createQRCode(new QRCode(editText.getText().toString(), rawScanResult));
                                    dialog.dismiss();
                                    finish();
                                }
                            });
                    builder.show();
                }
            }
        }

        hideProgressDialog();
    }

    private void showProgressDialog(final int title) {
        if (mProgressDialog == null) {
            mProgressDialog = ProgressDialog.show(this, null, getString(title), true, true, null);
        }
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    private void showZXingActivity() {
        final SafeRunnable onGranted = new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                showZXingActivity();
            }
        };

        if (askPermissionIfNeeded(Manifest.permission.CAMERA, 101, onGranted, null)) {
            return; // Permissions are being asked
        }

        showProgressDialog(R.string.opening_camera);

        final SafeRunnable runnable = new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                SystemUtils.showZXingActivity(AddQRCodeActivity.this, ScanTabActivity.MARKET_INSTALL_RESULT,
                        ScanTabActivity.ZXING_SCAN_RESULT);
            }
        };

        if (mServiceIsBound) {
            mService.postDelayedOnUIHandler(runnable, 250);
        } else {
            runnable.run();
        }
    }
}

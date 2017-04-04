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

package com.mobicage.rogerthat.plugins.scan;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.ServiceBoundActivity;
import com.mobicage.rogerthat.util.system.SafeBroadcastReceiver;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.system.SafeViewOnClickListener;
import com.mobicage.rogerthat.util.system.SystemUtils;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.ui.UIUtils;

public class ScanTabActivity extends ServiceBoundActivity {

    public static final int ZXING_SCAN_RESULT = 666;
    public static final int MARKET_INSTALL_RESULT = 667;
    public static final int MY_PERMISSIONS_REQUEST_CAMERA = 1;
    public static final String START_SCANNER_INTENT_ACTION = "START_SCANNER_INTENT_ACTION";
    public static final String PERMISSION_CAMERA_UPDATED = "PERMISSION_CAMERA_UPDATED";

    private boolean mFinishAfterScan = false;
    private static boolean sShowScannerOnResume = true;
    private boolean mForceDontShowScannerOnResume = false;
    private ProgressDialog mProgressDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onServiceBound() {
        T.UI();
        Intent intent = getIntent();
        mFinishAfterScan = START_SCANNER_INTENT_ACTION.equals(intent.getAction());

        setContentView(R.layout.scan_tab_activity);
        setActivityName("scan");

        final Button scanbutton = (Button) findViewById(R.id.scanbutton);
        scanbutton.setText(getString(R.string.scan_button, getString(R.string.app_name)));
        scanbutton.setOnClickListener(new SafeViewOnClickListener() {
            @Override
            public void safeOnClick(View v) {
                showZXingActivity();
            }
        });

        setTitle(R.string.scan);

        final IntentFilter intentFilter = new IntentFilter(PERMISSION_CAMERA_UPDATED);
        registerReceiver(mBroadcastReceiver, intentFilter);
    }

    private BroadcastReceiver mBroadcastReceiver = new SafeBroadcastReceiver() {
        @SuppressLint("DefaultLocale")
        @Override
        public String[] onSafeReceive(final Context context, final Intent intent) {
            T.UI();
            if (PERMISSION_CAMERA_UPDATED.equals(intent.getAction())) {
                String permissions [] = intent.getStringArrayExtra("permissions");
                int[] grantResults = intent.getIntArrayExtra("grantResults");
                onRequestPermissionsResult(MY_PERMISSIONS_REQUEST_CAMERA, permissions, grantResults);
                return new String[] { intent.getAction() };
            }

            return null;
        }

    };

    private void showZXingActivity() {
        int cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if(cameraPermission != PackageManager.PERMISSION_GRANTED) {
            if (sShowScannerOnResume) {
                sShowScannerOnResume = false;
                return;
            }

            Activity p = getParent();
            ActivityCompat.requestPermissions(p != null ? p : this, new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
            return;
        }
        sShowScannerOnResume = false;
        if (mProgressDialog == null) {
            mProgressDialog = ProgressDialog.show(this, null, getString(R.string.opening_camera), true, true, null);
        }

        SafeRunnable runnable = new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                SystemUtils.showZXingActivity(ScanTabActivity.this, MARKET_INSTALL_RESULT, ZXING_SCAN_RESULT);
            }
        };

        if (mServiceIsBound) {
            mService.postDelayedOnUIHandler(runnable, 250);
        } else {
            runnable.run();
        }
    }

    @Override
    protected void onServiceUnbound() {
        unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    protected void onStop() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mForceDontShowScannerOnResume) {
            sShowScannerOnResume = false;
            mForceDontShowScannerOnResume = false;
        } else {
            if (sShowScannerOnResume) {
                showZXingActivity();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        T.UI();
        if (resultCode == RESULT_OK) {
            if (requestCode == MARKET_INSTALL_RESULT) {
                // No need to do anything
            } else if (requestCode == ZXING_SCAN_RESULT) {
                final String rawScanResult = intent.getStringExtra("SCAN_RESULT");
                if (rawScanResult != null) {
                    final Intent launchIntent = new Intent(this, ProcessScanActivity.class);
                    launchIntent.putExtra(ProcessScanActivity.URL, rawScanResult);
                    launchIntent.putExtra(ProcessScanActivity.SCAN_RESULT, true);
                    startActivity(launchIntent);
                    mForceDontShowScannerOnResume = true;
                } else
                    UIUtils.showLongToast(ScanTabActivity.this, getString(R.string.scanner_failure));
            }
        }
        if (mFinishAfterScan) {
            sShowScannerOnResume = true;
            finish();
        } else {
            sShowScannerOnResume = false;
        }

        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        T.UI();
        if (requestCode == MY_PERMISSIONS_REQUEST_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showZXingActivity();
            } else {
                if (mFinishAfterScan) {
                    finish();
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        T.UI();
        sShowScannerOnResume = false;
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(android.content.res.Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        sShowScannerOnResume = false;
    }
}

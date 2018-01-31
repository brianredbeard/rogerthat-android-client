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

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.WriterException;
import com.google.zxing.client.android.Contents;
import com.google.zxing.client.android.Intents;
import com.google.zxing.client.android.encode.QRCodeEncoder;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.plugins.scan.ScanTabActivity;
import com.mobicage.rogerthat.plugins.system.QRCode;
import com.mobicage.rogerthat.plugins.system.SystemPlugin;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeBroadcastReceiver;
import com.mobicage.rogerthat.util.system.SafeDialogClick;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.ui.UIUtils;
import com.mobicage.rpc.config.AppConstants;

public class QRCodeActivity extends ServiceBoundActivity {

    private BroadcastReceiver mBroadcastReceiver;
    private IdentityStore mIdentityStore;
    private QRCode mCustomQRCode = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.qr_code);
        setActivityName("qrcode");

        final Intent intent = getIntent();
        final String qrContent = intent.getStringExtra("content");
        final String qrName = intent.getStringExtra("name");
        if (qrContent != null && qrName != null) {
            mCustomQRCode = new QRCode(qrName, qrContent);
            setTitle(mCustomQRCode.name);
        } else {
            setTitle(getString(R.string.passport, getString(R.string.app_name)));
        }
    }

    @Override
    protected void onServiceBound() {
        mIdentityStore = mService.getIdentityStore();

        final TextView headerTextView = (TextView) findViewById(R.id.loyalty_text);
        if (mCustomQRCode == null) {
            headerTextView.setText(getString(AppConstants.HOMESCREEN_QRCODE_HEADER, getString(R.string.app_name)));

            showIdentityQRCode();
        } else {
            headerTextView.setVisibility(View.GONE);
            showCustomQRCode();
        }

        mBroadcastReceiver = getBroadCastReceiver();
        IntentFilter intentFilter = new IntentFilter(IdentityStore.IDENTITY_CHANGED_INTENT);
        intentFilter.addAction(SystemPlugin.QR_CODE_ADDED_INTENT);
        registerReceiver(mBroadcastReceiver, intentFilter);
    }

    @Override
    protected void onServiceUnbound() {
        unregisterReceiver(mBroadcastReceiver);
    }

    private BroadcastReceiver getBroadCastReceiver() {
        return new SafeBroadcastReceiver() {

            @Override
            public String[] onSafeReceive(Context context, Intent intent) {
                final String action = intent.getAction();
                L.i("onSafeReceive: " + action);

                if (IdentityStore.IDENTITY_CHANGED_INTENT.equals(action)) {
                    showIdentityQRCode();
                } else if (SystemPlugin.QR_CODE_ADDED_INTENT.equals(action)) {
                    finish();
                    startActivity(new Intent(QRCodeActivity.this, QRCodesActivity.class));
                }

                return null;
            }

        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        T.UI();
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.qrcode_menu, menu);

        final boolean addItemVisible = getIntent().getBooleanExtra("show_add_button", true);
        final boolean deleteItemVisible = !addItemVisible && mCustomQRCode != null;
        final boolean scanItemVisible = mCustomQRCode == null;

        final MenuItem scanItem = menu.findItem(R.id.scan);
        scanItem.setVisible(scanItemVisible);
        addIconToMenuItem(scanItem, FontAwesome.Icon.faw_camera);

        final MenuItem addItem = menu.findItem(R.id.add_qr_code);
        addItem.setVisible(addItemVisible);
        addIconToMenuItem(addItem, FontAwesome.Icon.faw_plus);

        final MenuItem deleteItem = menu.findItem(R.id.delete_qr_code);
        deleteItem.setVisible(deleteItemVisible);
        addIconToMenuItem(deleteItem, FontAwesome.Icon.faw_trash);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        T.UI();
        switch (item.getItemId()) {
            case R.id.scan:
                Intent scanIntent = new Intent(this, ScanTabActivity.class);
                scanIntent.setAction(ScanTabActivity.START_SCANNER_INTENT_ACTION);
                startActivity(scanIntent);
                return true;
            case R.id.add_qr_code:
                Intent intent = new Intent(this, AddQRCodeActivity.class);
                startActivity(intent);
                return true;
            case R.id.delete_qr_code:
                String title = getString(R.string.delete_qr_code);
                String message = getString(R.string.confirm_delete_qr_code);
                String positiveCaption = getString(R.string.save);
                String negativeCaption = getString(R.string.no);
                SafeDialogClick positiveClick = new SafeDialogClick() {
                    @Override
                    public void safeOnClick(DialogInterface di, int id) {
                        mService.getPlugin(SystemPlugin.class).deleteQRCode(mCustomQRCode);
                        di.dismiss();
                        finish();
                    }
                };

                AlertDialog alertDialog = UIUtils.showDialog(this, title, message, positiveCaption,
                        positiveClick, negativeCaption, null);
                alertDialog.setCanceledOnTouchOutside(true);

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showIdentityQRCode() {
        MyIdentity mIdentity = mIdentityStore.getIdentity();
        final ImageView imageView = ((ImageView) findViewById(R.id.qrcode));
        final Bitmap qrBitmap = mIdentity.getQRBitmap();
        if (qrBitmap != null) {
            imageView.setImageBitmap(qrBitmap);
        }
    }

    private void showCustomQRCode() {
        final ImageView imageView = ((ImageView) findViewById(R.id.qrcode));

        Intent intent = new Intent();
        intent.setAction(Intents.Encode.ACTION);
        intent.putExtra(Intents.Encode.TYPE, Contents.Type.TEXT);
        intent.putExtra(Intents.Encode.DATA, mCustomQRCode.content);
        final Bitmap bitmap;
        try {
            QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(this, intent, UIUtils.getDisplayWidth(this) / 2, false);
            bitmap = qrCodeEncoder.encodeAsBitmap();
        } catch (WriterException e) {
            UIUtils.showLongToast(this, R.string.error_please_try_again);
            return;
        }
        imageView.setImageBitmap(bitmap);
    }

}

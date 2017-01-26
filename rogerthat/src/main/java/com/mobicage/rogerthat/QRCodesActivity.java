package com.mobicage.rogerthat;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.plugins.system.QRCode;
import com.mobicage.rogerthat.plugins.system.SystemPlugin;
import com.mobicage.rogerthat.util.system.SafeBroadcastReceiver;
import com.mobicage.rogerthat.util.system.SafeViewOnClickListener;
import com.mobicage.rogerthat.util.ui.UIUtils;

import thirdparty.nishantnair.FlowLayout;


public class QRCodesActivity extends ServiceBoundActivity {

    private SafeBroadcastReceiver mBroadcastReceiver = new SafeBroadcastReceiver() {
        @Override
        public String[] onSafeReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (SystemPlugin.QR_CODE_ADDED_INTENT.equals(action) || SystemPlugin.QR_CODE_DELETED_INTENT.equals(action)) {
                renderQRCodes();
            }

            return null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActivityName("qrcode");
        setTitle(R.string.my_ids);
        setContentView(R.layout.qr_codes);
    }

    @Override
    protected void onServiceBound() {
        renderQRCodes();
        IntentFilter intentFilter = new IntentFilter(SystemPlugin.QR_CODE_ADDED_INTENT);
        intentFilter.addAction(SystemPlugin.QR_CODE_DELETED_INTENT);
        registerReceiver(mBroadcastReceiver, intentFilter);
    }

    @Override
    protected void onServiceUnbound() {
        unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.qrcodes_menu, menu);
        addIconToMenuItem(menu, R.id.add_qr_code, FontAwesome.Icon.faw_plus);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_qr_code:
                Intent intent = new Intent(this, AddQRCodeActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void renderQRCodes() {
        final LinearLayout ll = (LinearLayout) findViewById(R.id.qr_codes_list);
        ll.removeAllViews();

        final int margin = UIUtils.convertDipToPixels(this, 20);
        final int width = UIUtils.getDisplayWidth(this) / 2;
        final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width, width / 2);
        layoutParams.setMargins(0, margin, 0, 0);

//        final FlowLayout flowLayout = (FlowLayout) findViewById(R.id.qr_codes);
//        flowLayout.removeAllViews();
//
//        final int width = (UIUtils.getDisplayWidth(this) - 3 * margin) / 2;
//        final FlowLayout.LayoutParams layoutParams = new FlowLayout.LayoutParams(width, width / 2, margin, margin);
//
        final QRCode defaultQR = new QRCode(getString(R.string.passport, getString(R.string.app_name)), null);
        ll.addView(createQRCodeView(defaultQR, layoutParams));
//
        final SystemPlugin systemPlugin = mService.getPlugin(SystemPlugin.class);
        for (QRCode qrCode : systemPlugin.listQRCodes()) {
//            flowLayout.addView(createQRCodeView(qrCode, layoutParams));
            ll.addView(createQRCodeView(qrCode, layoutParams));
        }
    }

    private View createQRCodeView(final QRCode qrCode, final LinearLayout.LayoutParams layoutParams) {
        final TextView textView = (TextView) getLayoutInflater().inflate(R.layout.qr_code_list_item, null);
        textView.setLayoutParams(layoutParams);
        textView.setOnClickListener(new SafeViewOnClickListener() {
            @Override
            public void safeOnClick(View v) {
                final Intent intent = new Intent(QRCodesActivity.this, QRCodeActivity.class);
                intent.putExtra("content", qrCode.content);
                intent.putExtra("name", qrCode.name);
                intent.putExtra("show_add_button", false);
                startActivity(intent);
            }
        });
        textView.setText(qrCode.name);
        return textView;
    }

}

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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeDialogClick;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.ui.UIUtils;
import com.mobicage.rpc.config.CloudConstants;

public class ConsentPushNotificationsActivity extends ServiceBoundActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected void onServiceBound() {
        setContentView(R.layout.registration_notifications);
        setTitle(R.string.push_notifications);
        setNavigationBarBurgerVisible(false);
        setNavigationBarIcon(null);

        TextView notificationsTextView = (TextView) findViewById(R.id.notifications_text);
        final String header = getString(R.string.registration_notifications_header, getString(R.string.app_name));
        final String reason = getString(CloudConstants.isCityApp() ? R.string.registration_notification_types_city_app : R.string.registration_notification_types_general);
        notificationsTextView.setText(header + "\n\n" + reason);
        final Button notificationsButton = (Button) findViewById(R.id.notifications_continue);
        notificationsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String title = ConsentPushNotificationsActivity.this.getString( R.string.notifications_consent_title);
                final String message = ConsentPushNotificationsActivity.this.getString( R.string.notifications_consent_message);
                final String positiveBtn = ConsentPushNotificationsActivity.this.getString(R.string.allow);
                final String negativeButtonCaption = ConsentPushNotificationsActivity.this.getString(R.string.dont_allow);
                SafeDialogClick positiveClick = new SafeDialogClick() {
                    @Override
                    public void safeOnClick(DialogInterface dialog, int id) {
                        T.UI();
                        dialog.dismiss();
                        saveInDB(true);
                    }
                };
                SafeDialogClick negativeClick = new SafeDialogClick() {
                    @Override
                    public void safeOnClick(DialogInterface dialog, int id) {
                        T.UI();
                        dialog.dismiss();
                        saveInDB(false);
                    }
                };
                UIUtils.showDialog(ConsentPushNotificationsActivity.this, title, message, positiveBtn, positiveClick, negativeButtonCaption, negativeClick);
            }
        });
    }

    @Override
    protected void onServiceUnbound() {
    }

    private void saveInDB(final boolean enabled) {
        mService.getConsentProvider().saveConsentForPushNotifications();

        final SharedPreferences options = PreferenceManager.getDefaultSharedPreferences(ConsentPushNotificationsActivity.this);
        final SharedPreferences.Editor editor = options.edit();
        editor.putBoolean(MainService.PREFERENCE_PUSH_NOTIFICATIONS, enabled);
        final boolean success = editor.commit();
        L.d("savePushNotifications success: " + success);

        Intent intent = new Intent(ConsentPushNotificationsActivity.this, MainActivity.class);
        intent.setFlags(MainActivity.FLAG_CLEAR_STACK_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}

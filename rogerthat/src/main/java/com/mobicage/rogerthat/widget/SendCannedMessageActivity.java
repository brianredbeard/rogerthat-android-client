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

package com.mobicage.rogerthat.widget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.ArrayAdapter;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.SendMessageWizard;
import com.mobicage.rogerthat.SendMessageWizardActivity;
import com.mobicage.rogerthat.ServiceBoundActivity;
import com.mobicage.rogerthat.config.ConfigurationProvider;
import com.mobicage.rogerthat.util.system.SafeBroadcastReceiver;
import com.mobicage.rogerthat.util.system.SafeDialogInterfaceOnClickListener;
import com.mobicage.rogerthat.util.ui.UIUtils;

public class SendCannedMessageActivity extends ServiceBoundActivity {

    private Dialog mDialog;
    private SafeBroadcastReceiver mBroadcastReceiver;

    @Override
    protected void onServiceUnbound() {
        unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    protected void onServiceBound() {
        mBroadcastReceiver = new SafeBroadcastReceiver() {
            @Override
            public String[] onSafeReceive(Context context, Intent intent) {

                if (SendMessageWizardActivity.CANNED_MESSAGE_SAVED.equals(intent.getAction())) {
                    showFullCannedMessages(intent.getStringExtra(SendMessageWizardActivity.CANNED_MESSAGE_NAME));

                } else if (SendMessageWizardActivity.CANNED_MESSAGE_CANCELED.equals(intent.getAction())
                    || CannedMessageSummaryActivity.CANNED_MESSAGE_CANCELED.equals(intent.getAction())) {
                    showFullCannedMessages(null);

                } else if (CannedMessageSummaryActivity.CANNED_MESSAGE_SENT.equals(intent.getAction())) {
                    UIUtils
                        .showLongToast(SendCannedMessageActivity.this, getString(R.string.successfully_sent_message));
                    finish();
                }
                return new String[] { intent.getAction() };
            }
        };

        IntentFilter filter = new IntentFilter(CannedMessageSummaryActivity.CANNED_MESSAGE_SENT);
        filter.addAction(CannedMessageSummaryActivity.CANNED_MESSAGE_CANCELED);
        filter.addAction(SendMessageWizardActivity.CANNED_MESSAGE_SAVED);
        filter.addAction(SendMessageWizardActivity.CANNED_MESSAGE_CANCELED);
        registerReceiver(mBroadcastReceiver, filter);

        showFullCannedMessages(null);
    }

    private void showFullCannedMessages(String firstItem) {
        final List<String> items = loadFullyCannedMessageWizards();
        Collections.sort(items);
        items.add(0, getString(R.string.configure_new_canned_message));
        if (items.contains(firstItem)) {
            // Add the newly added canned message on top
            items.remove(firstItem);
            items.add(1, firstItem);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.send_canned_message_dialog_title);
        builder.setAdapter(new ArrayAdapter<String>(this, R.layout.canned_message_title_item, items),
            new SafeDialogInterfaceOnClickListener() {
                @Override
                public void safeOnClick(DialogInterface dialog, final int which) {
                    mDialog.dismiss();
                    if (which == 0) {
                        createNewCannedMessage();
                    } else {
                        showMessageSummary(items.get(which));
                    }
                }
            });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                finish();
            }
        });
        mDialog = builder.create();
        mDialog.show();
    }

    private List<String> loadFullyCannedMessageWizards() {
        ConfigurationProvider cfgProvider = mService.getConfigurationProvider();

        List<String> cannedMessages = new ArrayList<String>(SendMessageWizard.getCannedMessages(cfgProvider));
        List<String> fullyCannedMessages = new ArrayList<String>(cannedMessages.size());

        for (String cannedMessage : cannedMessages) {
            SendMessageWizard wiz = SendMessageWizard.getWizard(this, cfgProvider, cannedMessage, false);

            // Only add cannedMessages with recipients and message/buttons
            if (wiz.getRecipients().size() > 0
                && ((wiz.getMessage() != null && wiz.getMessage().length() != 0) || wiz.getButtons().size() > 0)) {
                fullyCannedMessages.add(cannedMessage);
            }
        }
        return fullyCannedMessages;
    }

    private void createNewCannedMessage() {
        Intent intent = new Intent(this, SendMessageWizardActivity.class);
        intent.putExtra(SendMessageWizardActivity.SAVE_CANNED_MESSAGE_MODE, true);
        startActivity(intent);
    }

    private void showMessageSummary(final String cannedMessageName) {
        Intent intent = new Intent(this, CannedMessageSummaryActivity.class);
        intent.putExtra(CannedMessageSummaryActivity.CANNED_MESSAGE_NAME, cannedMessageName);
        startActivity(intent);
    }

}

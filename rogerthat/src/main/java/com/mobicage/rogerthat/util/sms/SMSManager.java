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
package com.mobicage.rogerthat.util.sms;

import java.io.Closeable;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;

public class SMSManager extends BroadcastReceiver implements Closeable {

    public static final int MY_PERMISSIONS_REQUEST_SMS = 669;

    private Activity mContext;
    protected SMSTraffic mSMSReceived;
    private SMSSentResult mSMSentResult;

    public interface SMSTraffic {
        void onSMS(String phoneNumber, byte[] message);
    }

    public interface SMSSentResult {
        void onSent(String phoneNumber, byte[] message, boolean success, String errorMessage);
    }

    public static final String SMS_SENT_INTENT = "com.mobicage.rogerthat.registration.SMS_SENT_INTENT";
    public static final String SMS_RECEIVED_INTENT = "com.mobicage.rogerthat.registration.SMS_RECEIVED_INTENT";
    protected static final short SMS_DEST_PORT = 11974;

    public SMSManager(Activity context) {
        mContext = context;
        IntentFilter filter = new IntentFilter(SMS_RECEIVED_INTENT);
        filter.addAction(SMS_SENT_INTENT);
        context.registerReceiver(this, filter);
    }

    protected void proccesIncommingPDU(byte[] pdu) {
        SmsMessage message = SmsMessage.createFromPdu(pdu);
        if (mSMSReceived != null)
            mSMSReceived.onSMS(message.getOriginatingAddress(), message.getUserData());
    }

    protected String getSMSErrorMessage(int errorCode) {
        switch (errorCode) {
        case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
            return "Generic failure";
        case SmsManager.RESULT_ERROR_NO_SERVICE:
            return "No service";
        case SmsManager.RESULT_ERROR_NULL_PDU:
            return "Null PDU";
        case SmsManager.RESULT_ERROR_RADIO_OFF:
            return "Radio off";
        default:
            return "Unknown error condition";
        }
    }

    public void sendSMS(String to, String message) {
        final SmsManager mgr = SmsManager.getDefault();
        mgr.sendMultipartTextMessage(to, null, mgr.divideMessage(message), null, null);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (SMS_SENT_INTENT.equals(intent.getAction())) {
            final boolean success;
            final String errorMessage;
            final int resultCode = getResultCode();
            switch (resultCode) {
            case Activity.RESULT_OK:
                success = true;
                errorMessage = null;
                break;
            default:
                success = false;
                errorMessage = getSMSErrorMessage(resultCode);
                break;
            }
            if (mSMSentResult != null) {
                mSMSentResult
                    .onSent(intent.getStringExtra("pn"), intent.getByteArrayExtra("me"), success, errorMessage);
            }
        } else if (SMS_RECEIVED_INTENT.equals(intent.getAction())) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdus = (Object[]) bundle.get("pdus");
                for (Object pdu : pdus) {
                    proccesIncommingPDU((byte[]) pdu);
                }
            }
        }
    }

    public void setOnSMSReceived(SMSTraffic received) {
        this.mSMSReceived = received;
    }

    public void setOnSMSentResult(SMSSentResult sentResult) {
        this.mSMSentResult = sentResult;
    }

    @Override
    public void close() {
        mContext.unregisterReceiver(this);
    }

}

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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.security.SecurityUtils;
import com.mobicage.rogerthat.widget.PinEntryListener;
import com.mobicage.rogerthat.widget.PinEntryView;
import com.mobicage.rogerthat.widget.PinKeyboardView;

public class SetupPinActivity extends ServiceBoundActivity implements PinEntryListener {

    public static String RESULT_VIA_MAINSERVICE = "result_via_mainservice";

    private TextView mMessage;
    private TextView mErrorMessage;
    private PinEntryView mPinEntryView;
    private PinKeyboardView mPinKeyboardView;

    private String mFirstPin = null;

    private boolean mResultViaMainService = false;
    private boolean mSendUpdates = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentViewWithoutNavigationBar(R.layout.enter_pin);

        Intent intent = getIntent();
        mResultViaMainService = intent.getBooleanExtra(RESULT_VIA_MAINSERVICE, false);

        mMessage = (TextView) findViewById(R.id.message);
        mMessage.setText(R.string.pin_setup_4_digit);
        mErrorMessage = (TextView) findViewById(R.id.error_message);
        mPinEntryView = (PinEntryView) findViewById(R.id.pinEntryView);
        mPinKeyboardView = (PinKeyboardView) findViewById(R.id.pinKeyboardView);
        mPinKeyboardView.setPinEntryView(mPinEntryView);
        mPinEntryView.setPinEntryListener(this);
    }

    @Override
    protected void onServiceBound() {
    }

    @Override
    protected void onServiceUnbound() {
    }

    public void onPinEntered(String pin) {
        if (!mSendUpdates) {
            return;
        }
        if (mFirstPin == null) {
            mFirstPin = pin;
            mPinEntryView.clearPinEntry();
            mMessage.setText(R.string.pin_confirm);
            mErrorMessage.setVisibility(View.GONE);
            return;
        } else if (mFirstPin.equals(pin)) {
            mSendUpdates = false;
            try{
                SecurityUtils.setPin(mService, pin);
                if (mResultViaMainService) {
                    mService.onSetupPinCompleted(pin);
                } else {
                    setResult(Activity.RESULT_OK);
                }
                finish();
                return;
            } catch (Exception e) {
                L.bug(e);
                //mService.wipe(0); // todo ruben better solution
            }
        } else {
            mErrorMessage.setText(R.string.pin_did_not_match);
        }
        shakePinEntry();
        mFirstPin = null;
        mPinEntryView.clearPinEntry();
        mMessage.setText(R.string.pin_setup_4_digit);
        mErrorMessage.setVisibility(View.VISIBLE);
    }

    public void onPinCancelled() {
        if (!mSendUpdates) {
            return;
        }
        mSendUpdates = false;
        if (mResultViaMainService) {
            mService.onSetupPinCompleted(null);
        } else {
            setResult(Activity.RESULT_CANCELED);
        }
        finish();
    }

    @Override
    public boolean onKeyDown(final int keyCode, final KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onPinCancelled();
        }
        return super.onKeyDown(keyCode, event);
    }

    public void onPinIncomplete() {
        shakePinEntry();
    }

    private void shakePinEntry() {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(400);

        Animation shake = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.shake);
        mPinEntryView.startAnimation(shake);
    }
}

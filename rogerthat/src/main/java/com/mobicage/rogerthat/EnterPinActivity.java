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
import com.mobicage.rogerthat.config.Configuration;
import com.mobicage.rogerthat.util.Security;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.time.TimeUtils;
import com.mobicage.rogerthat.widget.PinEntryListener;
import com.mobicage.rogerthat.widget.PinEntryView;
import com.mobicage.rogerthat.widget.PinKeyboardView;
import com.mobicage.rpc.config.AppConstants;

import java.util.Date;
import java.util.UUID;

public class EnterPinActivity extends ServiceBoundActivity implements PinEntryListener {

    private final static long SECOND = 1000;
    private final static long MINUTE = 60 * SECOND;
    private final static long HOUR = 60 * MINUTE;
    public static final String FINISHED_ENTER_PIN = "com.mobicage.rogerthat.FINISHED_ENTER_PIN";

    public static String MESSAGE = "message";
    public static String RESULT_VIA_MAINSERVICE = "result_via_mainservice";
    public static String UID = "uid";

    public static String RESULT = "result";

    private long mPinRetryCount;
    private long mPinTimeout;
    private TextView mErrorMessage;
    private PinEntryView mPinEntryView;
    private PinKeyboardView mPinKeyboardView;

    private boolean mResultViaMainService = false;
    private String mUid;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        setContentViewWithoutNavigationBar(R.layout.enter_pin);

        Intent intent = getIntent();
        String message = intent.getStringExtra(MESSAGE);
        mResultViaMainService = intent.getBooleanExtra(RESULT_VIA_MAINSERVICE, false);
        if (mResultViaMainService) {
            mUid = intent.getStringExtra(EnterPinActivity.UID);
        }

        ((TextView) findViewById(R.id.message)).setText(message);
        mErrorMessage = (TextView) findViewById(R.id.error_message);
        mPinEntryView = (PinEntryView) findViewById(R.id.pinEntryView);
        mPinKeyboardView = (PinKeyboardView) findViewById(R.id.pinKeyboardView);
        mPinKeyboardView.setPinEntryView(mPinEntryView);
        mPinEntryView.setPinEntryListener(this);
    }

    @Override
    protected void onServiceBound() {
        Configuration cfg = Security.getConfiguration(mService);
        mPinRetryCount = cfg.get(Security.CONFIG_PIN_RETRY_COUNT, 0);
        mPinTimeout = cfg.get(Security.CONFIG_PIN_TIMEOUT, 0);

        long currentTime = System.currentTimeMillis();
        if (currentTime <= mPinTimeout) {
            disablePinInput();
        }
    }

    @Override
    protected void onServiceUnbound() {
    }

    public void onPinEntered(final String pin) {
        T.UI();
        try{
            boolean isValidPin = Security.isValidPin(mService, pin);
            if (isValidPin) {
                if (mPinRetryCount != 0) {
                    Security.setPinRetry(mService, 0, 0);
                }
                if (mResultViaMainService) {
                    mService.onPinEntered(mUid, pin);
                } else {
                    Intent resultIntent = new Intent(FINISHED_ENTER_PIN);
                    resultIntent.putExtra(EnterPinActivity.RESULT, pin);
                    setResult(Activity.RESULT_OK, resultIntent);
                }
                finish();
                return;
            } else {
                mPinRetryCount += 1;
                if (mPinRetryCount >= 2) {
                    L.d("mPinRetryCount: " + mPinRetryCount);
                    long currentTime = System.currentTimeMillis();
                    mPinTimeout = currentTime + ((mPinRetryCount - 1) * AppConstants.SECURE_PIN_RETRY_INTERVAL * SECOND);
                    Security.setPinRetry(mService, mPinRetryCount - 1, mPinTimeout);
                    disablePinInput();
                } else {
                    shakePinEntry();
                    mErrorMessage.setVisibility(View.VISIBLE);
                    mErrorMessage.setText(R.string.pin_invalid);
                }
            }
        } catch (Exception e) {
            mService.processExceptionViaHTTP(e);
            mService.wipe(0);
        }
        mPinEntryView.clearPinEntry();
    }

    public void onPinCancelled() {
        if (mResultViaMainService) {
            mService.onPinCancelled(mUid);
        } else {
            Intent resultIntent = new Intent(FINISHED_ENTER_PIN);
            setResult(Activity.RESULT_CANCELED, resultIntent);
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

    private void disablePinInput() {
        long startRunnableIn = updateErrorMessageIfNeeded();
        long hoursDiffence = getHoursDiffence();
        if (hoursDiffence > 0) {
            mErrorMessage.setText(getString(R.string.try_again_in_hours, hoursDiffence));
        } else {
            long minutesDiffence = getMinutesDiffence();
            if (minutesDiffence > 0) {
                mErrorMessage.setText(getString(R.string.try_again_in_minutes, minutesDiffence));
            } else {
                long secondsDiffence = getSecondsDiffence();
                mErrorMessage.setText(getString(R.string.try_again_in_seconds, secondsDiffence));
            }
        }

        mErrorMessage.setVisibility(View.VISIBLE);

        SafeRunnable sr = new SafeRunnable() {

            @Override
            protected void safeRun() throws Exception {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                long secondsDiffence = getSecondsDiffence();
                if (secondsDiffence >= 1) {
                    disablePinInput();
                } else {
                    enablePinInput();
                }
            }
        };
        mService.postDelayedOnUIHandler(sr, startRunnableIn);

        mPinEntryView.setVisibility(View.INVISIBLE);
        mPinKeyboardView.setVisibility(View.INVISIBLE);
    }

    private void enablePinInput() {
        mErrorMessage.setVisibility(View.GONE);
        mPinEntryView.setVisibility(View.VISIBLE);
        mPinKeyboardView.setVisibility(View.VISIBLE);
    }

    private long updateErrorMessageIfNeeded() {
        final long currentTime = System.currentTimeMillis();
        if (mPinTimeout <= currentTime) {
            return 0;
        }

        long diff = mPinTimeout - currentTime;
        long hoursDifference = getHoursDiffence();
        if (hoursDifference >= 1) {
            return diff % HOUR;
        }
        long minutesDifference = getMinutesDiffence();
        if (minutesDifference >= 1) {
            return diff % MINUTE;
        }
        long secondsDifference = getSecondsDiffence();
        if (secondsDifference >= 1) {
            return SECOND;
        }

        return 0;
    }

    private long getHoursDiffence() {
        final long currentTime = System.currentTimeMillis();
        double t = (mPinTimeout - currentTime) / (double) HOUR;
        if (t < 1) {
            return 0;
        }
        return Math.round(t);
    }

    private long getMinutesDiffence() {
        final long currentTime = System.currentTimeMillis();
        double t = (mPinTimeout - currentTime) / (double) MINUTE;
        if (t < 1) {
            return 0;
        }
        return Math.round(t);
    }

    private long getSecondsDiffence() {
        final long currentTime = System.currentTimeMillis();
        double t = (mPinTimeout - currentTime) / (double) SECOND;
        if (t < 1) {
            return 0;
        }
        return Math.round(t);
    }
}

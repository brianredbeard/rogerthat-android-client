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
package com.mobicage.rogerthat.plugins.messaging;

import java.io.Closeable;
import java.io.IOException;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Vibrator;
import android.preference.PreferenceManager;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.MainService;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.ui.UIUtils;

public class AlertManager extends BroadcastReceiver implements Closeable, OnSharedPreferenceChangeListener {

    public static final int ALERT_FLAG_SILENT = 1;
    public static final int ALERT_FLAG_VIBRATE = 2;
    public static final int ALERT_FLAG_RING_5 = 4;
    public static final int ALERT_FLAG_RING_15 = 8;
    public static final int ALERT_FLAG_RING_30 = 16;
    public static final int ALERT_FLAG_RING_60 = 32;
    public static final int ALERT_FLAG_INTERVAL_5 = 64;
    public static final int ALERT_FLAG_INTERVAL_15 = 128;
    public static final int ALERT_FLAG_INTERVAL_30 = 256;
    public static final int ALERT_FLAG_INTERVAL_60 = 512;
    public static final int ALERT_FLAG_INTERVAL_300 = 1024;
    public static final int ALERT_FLAG_INTERVAL_900 = 2048;
    public static final int ALERT_FLAG_INTERVAL_3600 = 4096;

    public static final int[] RING_FLAGS = new int[] { ALERT_FLAG_RING_5, ALERT_FLAG_RING_15, ALERT_FLAG_RING_30,
        ALERT_FLAG_RING_60 };
    public static final int[] INTERVAL_FLAGS = new int[] { ALERT_FLAG_INTERVAL_5, ALERT_FLAG_INTERVAL_15,
        ALERT_FLAG_INTERVAL_30, ALERT_FLAG_INTERVAL_60, ALERT_FLAG_INTERVAL_300, ALERT_FLAG_INTERVAL_900,
        ALERT_FLAG_INTERVAL_3600 };

    private static final String RING_INTENT = "com.mobicage.rogerthat.plugins.messaging.AlertManager.RING";
    private static final String BEEP_INTENT = "com.mobicage.rogerthat.plugins.messaging.AlertManager.BEEP";

    private final MainService mMainService;
    private final MessageStore mStore;
    private final Vibrator mVibrator;
    private final AlarmManager mAlarmManager;
    private final HandlerThread mSoundAndVibrateThread;
    private final Handler mSoundAndVibrateHandler;

    // UI thread
    private int mRingIntervalTime;
    private int mIntervalTime;
    private PendingIntent mPendingRingIntent = null;
    private PendingIntent mPendingBeepIntent = null;
    private long mLastAnalyzeTimestamp;

    // Sound & vibrate thread
    private long mStartPlayingTimestamp = 0;
    private MediaPlayer mRingMediaPlayer;
    private MediaPlayer mBeepMediaPlayer;

    private volatile boolean mShouldVibrate;
    private volatile boolean mRingShouldVibrate;
    private volatile int mRingTime;
    private volatile boolean mVibrating = false;
    private volatile boolean mRinging = false;
    private volatile Uri mCustomAlarm = null;

    public AlertManager(MainService mainService, MessageStore store) {
        T.UI();
        mMainService = mainService;
        mStore = store;
        mAlarmManager = (AlarmManager) mMainService.getSystemService(Context.ALARM_SERVICE);
        mLastAnalyzeTimestamp = mainService.currentTimeMillis() / 1000;

        mSoundAndVibrateThread = new HandlerThread("rogerthat_branding_worker");
        mSoundAndVibrateThread.start();
        Looper looper = mSoundAndVibrateThread.getLooper();
        mSoundAndVibrateHandler = new Handler(looper);

        IntentFilter intentFilter = new IntentFilter(RING_INTENT);
        intentFilter.addAction(BEEP_INTENT);
        mMainService.registerReceiver(this, intentFilter);
        mVibrator = (Vibrator) mMainService.getSystemService(Context.VIBRATOR_SERVICE);

        final SharedPreferences options = PreferenceManager.getDefaultSharedPreferences(mainService);
        options.registerOnSharedPreferenceChangeListener(this);
        loadCustomAlarm(options);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (MainService.PREFERENCE_ALARM_SOUND.equals(key)) {
            loadCustomAlarm(sharedPreferences);
        }
    }

    private void loadCustomAlarm(SharedPreferences options) {
        String customAlarm = options.getString(MainService.PREFERENCE_ALARM_SOUND, null);
        if (customAlarm == null) {
            mCustomAlarm = null;
        } else {
            mCustomAlarm = Uri.parse(customAlarm);
        }
        L.d("Custom alarm: " + mCustomAlarm);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (RING_INTENT.equals(action)) {
            mPendingRingIntent = null;
            if (mRingTime == 0)
                return;
            startRinging(false);
        } else if (BEEP_INTENT.equals(action)) {
            mPendingBeepIntent = null;
            beep(false);
        }
    }

    @Override
    public void close() throws IOException {
        T.UI();
        mSoundAndVibrateHandler.post(new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                if (mRinging)
                    mRingMediaPlayer.stop();
                if (mRingMediaPlayer != null)
                    mRingMediaPlayer.release();
                if (mBeepMediaPlayer != null)
                    mBeepMediaPlayer.release();
                if (mVibrating)
                    mVibrator.cancel();
            }
        });

        mMainService.unregisterReceiver(this);

        Looper looper = mSoundAndVibrateThread.getLooper();
        if (looper != null) {
            looper.quit();
        }
        try {
            mSoundAndVibrateThread.join();
        } catch (InterruptedException e) {
            L.d(e);
        }
    }

    public void analyze(boolean alertNow, boolean updates4me) {
        T.UI();
        L.d(String.format("AlertManager.analyze(%s)", alertNow));
        int[] alertFlags = mStore.getAlertFlags(mLastAnalyzeTimestamp);
        float ringIntensity = 0;
        boolean isSilent = true;
        mRingShouldVibrate = false;
        mRingTime = 0;
        mRingIntervalTime = 0;
        mShouldVibrate = false;
        mIntervalTime = Integer.MAX_VALUE;
        for (int flags : alertFlags) {
            int ringTime = getRingTime(flags);
            int intervalTime = getInterval(flags);
            isSilent &= isFlagSet(flags, ALERT_FLAG_SILENT);

            if (ringTime != 0) {
                mRingShouldVibrate = isFlagSet(flags, ALERT_FLAG_VIBRATE);
                if (mRingTime == 0)
                    mRingTime = ringTime;
                if (intervalTime != 0) {
                    float tmp = (float) ringTime / (float) intervalTime;
                    if (tmp > ringIntensity) {
                        ringIntensity = tmp;
                        mRingTime = ringTime;
                        mRingIntervalTime = intervalTime;
                    }
                }
            } else {
                mShouldVibrate = isFlagSet(flags, ALERT_FLAG_VIBRATE);
                if (intervalTime != 0 && intervalTime < mIntervalTime)
                    mIntervalTime = intervalTime;
            }
        }

        mLastAnalyzeTimestamp = mMainService.currentTimeMillis() / 1000;

        if (mRingTime == 0) {
            if (mRinging)
                mSoundAndVibrateHandler.post(stopPlayingRunnable);
        } else {
            if (!mRinging) {
                startRinging(alertNow);
                return;
            }
        }
        if (!isSilent || updates4me)
            beep(alertNow || updates4me);
        else if (alertNow && isSilent && mShouldVibrate)
            mVibrator.vibrate(new long[] { 0L, 300L }, -1);

    }

    private void startRinging(final boolean now) {
        L.d(String.format("startRinging(%s)", now));
        L.d("mRingIntervalTime: " + mRingIntervalTime);
        if (now || mRingIntervalTime > 0) {
            mSoundAndVibrateHandler.post(new SafeRunnable() {
                @Override
                protected void safeRun() throws Exception {
                    if (!mRinging) {
                        mRingMediaPlayer = new MediaPlayer();
                        mRingMediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
                        AssetFileDescriptor fd = null;
                        boolean needsDefaultAlarm = true;
                        if (mCustomAlarm != null) {
                            try {
                                mRingMediaPlayer.setDataSource(mMainService, mCustomAlarm);
                                needsDefaultAlarm = false;
                            } catch (IOException e) {
                                // Do nothing - e.g. custom alarm might have been deleted from SD card
                            } catch (Exception e) {
                                L.bug(e);
                            }
                        }
                        if (needsDefaultAlarm) {
                            fd = mMainService.getResources().openRawResourceFd(R.raw.ringtone);
                            mRingMediaPlayer.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(),
                                fd.getDeclaredLength());
                        }

                        mRingMediaPlayer.prepare();
                        mRingMediaPlayer.setLooping(true);

                        mStartPlayingTimestamp = System.currentTimeMillis() / 1000;
                        mRingMediaPlayer.start();
                        if (fd != null)
                            fd.close();
                        L.d("**** STARTED RINGING ****");
                        mRinging = true;
                    }
                    if (mRingShouldVibrate && !mVibrating) {
                        L.d("**** STARTED VIBRATING ****");
                        mVibrator.vibrate(new long[] { 0L, 1000L, 1000L, 1000L, 5000L }, 4);
                        mVibrating = true;
                    }
                    mSoundAndVibrateHandler.removeCallbacks(stopPlayingRunnable);
                    mSoundAndVibrateHandler.postDelayed(stopPlayingRunnable, mRingTime * 1000);
                }
            });
        }
    }

    private void stopRinging() {
        mRingMediaPlayer.stop();
        mRingMediaPlayer.release();
        mRingMediaPlayer = null;
        mRinging = false;
        L.d("**** STOPPED RINGING ****");
        if (mVibrating) {
            L.d("**** STOPPED VIBRATING ****");
            mVibrator.cancel();
            mVibrating = false;
        }
        mMainService.postOnUIHandler(new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                scheduleNextRing();
            }
        });
    }

    private SafeRunnable stopPlayingRunnable = new SafeRunnable() {
        @Override
        protected void safeRun() throws Exception {
            if (mRinging) {
                long playTime = mStartPlayingTimestamp + mRingTime - (System.currentTimeMillis() / 1000);
                if (playTime <= 0) {
                    stopRinging();
                } else {
                    mMainService.postDelayedOnUIHandler(stopPlayingRunnable, playTime * 1000);
                }
            }
        }
    };

    private void scheduleNextRing() {
        T.UI();
        if (mRingIntervalTime == 0)
            return;
        if (mPendingRingIntent != null) {
            mPendingRingIntent.cancel();
            mPendingRingIntent = null;
        }
        mPendingRingIntent = PendingIntent.getBroadcast(mMainService, 0, new Intent(RING_INTENT), 0);
        mAlarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (mRingIntervalTime * 1000),
            mPendingRingIntent);
    }

    private boolean isRogerthatTopActivity() {
        T.UI();
        final Activity topActivity = UIUtils.getTopActivity(mMainService);
        if (topActivity == null)
            return false;
        return mMainService.getPackageName().equals(topActivity.getPackageName());
    }

    private void beep(boolean now) {
        T.UI();

        final boolean shallMakeSound = !(mMainService.getScreenIsOn() && isRogerthatTopActivity())
            && (now || mIntervalTime != Integer.MAX_VALUE);
        final boolean shallVibrate = now || mIntervalTime != Integer.MAX_VALUE;
        mSoundAndVibrateHandler.post(new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                if (!mRinging) {
                    if (shallMakeSound) {
                        L.d("****  BEEP ****");
                        if (mBeepMediaPlayer != null) {
                            mBeepMediaPlayer.release();
                        }
                        mBeepMediaPlayer = new MediaPlayer();
                        mBeepMediaPlayer.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
                        AssetFileDescriptor fd = mMainService.getResources().openRawResourceFd(R.raw.notification);
                        mBeepMediaPlayer.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(),
                            fd.getDeclaredLength());
                        mBeepMediaPlayer.prepare();
                        mBeepMediaPlayer.start();
                        fd.close();
                    }
                    if (shallVibrate && mShouldVibrate) {
                        L.d("****  VIBRATE ****");
                        mVibrator.vibrate(new long[] { 0L, 300L }, -1);
                    }
                }
                mMainService.postOnUIHandler(new SafeRunnable() {
                    @Override
                    protected void safeRun() throws Exception {
                        scheduleNextBeep();
                    }
                });
            }
        });
    }

    private void scheduleNextBeep() {
        T.UI();
        if (mIntervalTime == Integer.MAX_VALUE)
            return;
        if (mPendingBeepIntent != null) {
            mPendingBeepIntent.cancel();
            mPendingBeepIntent = null;
        }
        mPendingBeepIntent = PendingIntent.getBroadcast(mMainService, 0, new Intent(BEEP_INTENT), 0);
        mAlarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (mIntervalTime * 1000),
            mPendingBeepIntent);
    }

    private int getFlag(int flags, int[] flagSet) {
        for (int flag : flagSet) {
            if ((flag & flags) == flag)
                return flag;
        }
        return 0;
    }

    private boolean isFlagSet(int value, int flag) {
        return (value & flag) == flag;
    }

    private int getRingTime(int flags) {
        switch (getFlag(flags, RING_FLAGS)) {
        case ALERT_FLAG_RING_5:
            return 5;
        case ALERT_FLAG_RING_15:
            return 15;
        case ALERT_FLAG_RING_30:
            return 30;
        case ALERT_FLAG_RING_60:
            return 60;
        default:
            return 0;
        }
    }

    private int getInterval(int flags) {
        switch (getFlag(flags, INTERVAL_FLAGS)) {
        case ALERT_FLAG_INTERVAL_5:
            return 5;
        case ALERT_FLAG_INTERVAL_15:
            return 15;
        case ALERT_FLAG_INTERVAL_30:
            return 30;
        case ALERT_FLAG_INTERVAL_60:
            return 60;
        case ALERT_FLAG_INTERVAL_300:
            return 300;
        case ALERT_FLAG_INTERVAL_900:
            return 900;
        case ALERT_FLAG_INTERVAL_3600:
            return 3600;
        default:
            return 0;
        }
    }

}

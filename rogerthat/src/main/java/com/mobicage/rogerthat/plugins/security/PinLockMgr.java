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

package com.mobicage.rogerthat.plugins.security;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.MainService;
import com.mobicage.rogerthat.ServiceBound;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.security.SecurityUtils;

import java.util.ArrayList;
import java.util.List;


public class PinLockMgr implements ServiceBound, Application.ActivityLifecycleCallbacks {

    protected volatile MainService mService;
    private List<Activity> mActivities = new ArrayList<>();
    private boolean mIsPinSet = false;
    private boolean mShouldAskPin = true;

    public PinLockMgr(Application app) {
        app.registerActivityLifecycleCallbacks(this);
    }

    @Override
    public MainService getMainService() {
        return mService;
    }

    public PinLockMgr setMainService(MainService mainService) {
        mService = mainService;
        return this;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    }

    @Override
    public void onActivityStarted(Activity activity) {
        mActivities.add(activity);
        if (mShouldAskPin || mActivities.size() == 1) {
            if (activity instanceof NoPinUnlockActivity) {
                mShouldAskPin = true;
            } else {
                askPin();
            }
        }
    }

    @Override
    public void onActivityResumed(Activity activity) {
    }

    @Override
    public void onActivityPaused(Activity activity) {
    }

    @Override
    public void onActivityStopped(Activity activity) {
        mActivities.remove(activity);
        if (mActivities.size() == 0) {
            mShouldAskPin = true;
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
    }

    public void unregistered() {
        mIsPinSet = false;
    }

    private boolean isPinSet() {
        return mIsPinSet || SecurityUtils.isPinSet(mService);
    }

    private void askPin() {
        mShouldAskPin = false;

        if (!isPinSet()) {
            return;
        }

        L.i("Asking for the secure pin code");
        mService.askPin(mService.getString(R.string.enter_pin_to_unlock), new MainService.SecurityCallback<Object>() {
            @Override
            public void onSuccess(Object result) {
                L.i("Pin code entered successfully");
            }

            @Override
            public void onError(String code, String errorMessage) {
                L.w("Finishing all activities! Pin code was not entered successfully: %s", errorMessage);
                for (Activity activity : mActivities) {
                    activity.finish();
                }
            }
        });
    }

    public interface NoPinUnlockActivity {
    }

}

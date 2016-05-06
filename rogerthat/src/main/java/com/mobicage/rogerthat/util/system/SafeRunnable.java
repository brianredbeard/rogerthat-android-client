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

package com.mobicage.rogerthat.util.system;

import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.ui.Pausable;

public abstract class SafeRunnable implements Runnable {

    private final Pausable mPausable;
    protected volatile Object mTag;

    protected abstract void safeRun() throws Exception;

    public SafeRunnable() {
        mPausable = null;
        mTag = null;
    }

    public SafeRunnable(Pausable pausable) {
        mPausable = pausable;
    }

    public Object getTag() {
        return mTag;
    }

    @Override
    final public void run() {
        if (mPausable != null && mPausable.getPaused())
            mPausable.queue(this);
        else
            try {
                safeRun();
            } catch (Exception e) {
                L.bug("Exception in SafeRunnable", e);
            }
    }

}
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

import android.content.Context;
import android.os.PowerManager;

import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.time.TimeUtils;

public class TaggedWakeLock {

    private final String mTag;
    private final PowerManager.WakeLock mWakeLock;
    private final long mCreationTime;
    private final String mDescription;

    public TaggedWakeLock(String tag, PowerManager.WakeLock wakeLock) {
        mTag = tag;
        mWakeLock = wakeLock;
        mCreationTime = System.currentTimeMillis();
        mDescription = "[" + TimeUtils.getPrettyTimeString(null, mCreationTime) + "] " + mTag;
    }

    public void acquire() {
        L.d("Acquiring wakelock: " + mDescription);
        mWakeLock.acquire();
    }

    public void release() {
        L.d("Releasing wakelock: " + mDescription);
        mWakeLock.release();
    }

    public static TaggedWakeLock newTaggedWakeLock(Context context, int flags, String tag) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = pm.newWakeLock(flags, tag);
        return new TaggedWakeLock(tag, wakeLock);
    }

}
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

/*
 * E.g. backoffscheme min interval 1000, max 10000
 * ==> getDelay() returns 0, 1000, 2000, 4000, 8000, 8000, 8000, ...
 */

package com.mobicage.rpc.newxmpp;

public class BackoffScheme {

    private final long mMinInterval;
    private final long mMaxInterval;

    private long mCurrentInterval;

    public BackoffScheme(long minReconnectInterval, long maxReconnectInterval) {
        mMinInterval = minReconnectInterval;
        mMaxInterval = maxReconnectInterval;
        reset();
    }

    public void reset() {
        mCurrentInterval = 0;
    }

    public long getDelay() {

        if (mCurrentInterval == 0) {
            mCurrentInterval = mMinInterval;
            return 0;
        }

        long newInterval = mCurrentInterval * 2;
        if (newInterval < mMaxInterval)
            mCurrentInterval = newInterval;
        else
            mCurrentInterval = mMaxInterval;

        return mCurrentInterval;
    }
}

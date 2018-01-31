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
package com.mobicage.rogerthat.plugins.system;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.pickle.PickleException;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rpc.IResponse;
import com.mobicage.rpc.ResponseHandler;
import com.mobicage.to.system.HeartBeatResponseTO;

public class HeartBeatResponseHandler extends ResponseHandler<HeartBeatResponseTO> {

    private volatile long mRequestSubmissionTimestamp;

    public void setRequestSubmissionTimestamp(final long requestSubmissionTimestamp) {
        T.UI();
        this.mRequestSubmissionTimestamp = requestSubmissionTimestamp;
    }

    @Override
    public void readFromPickle(final int version, final DataInput in) throws IOException, PickleException {
        T.dontCare();
        super.readFromPickle(version, in);
        mRequestSubmissionTimestamp = in.readLong();
    }

    @Override
    public void writePickle(final DataOutput out) throws IOException {
        T.dontCare();
        super.writePickle(out);
        out.writeLong(mRequestSubmissionTimestamp);
    }

    @Override
    public void handle(final IResponse<HeartBeatResponseTO> response) {
        T.BIZZ();
        try {
            final long serverTimestamp = response.getResponse().systemTime * 1000;
            final long now = System.currentTimeMillis();
            if (now - mRequestSubmissionTimestamp < 5000) {
                final long localCorrelationTimestamp = now - (now - mRequestSubmissionTimestamp) / 2;
                final long adjustedTimeDiff = serverTimestamp - localCorrelationTimestamp;
                L.d("Setting adjusted time diff between server and client to " + adjustedTimeDiff + " millis");
                mMainService.setAdjustedTimeDiff(adjustedTimeDiff);
            }
        } catch (Exception e) {
            L.w("Heartbeat call resulted in failure!", e);
        }
    }

}

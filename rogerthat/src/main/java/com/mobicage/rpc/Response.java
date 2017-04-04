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

package com.mobicage.rpc;

import com.mobicage.rogerthat.util.system.T;

// XXX: cant we do without all those getters and setters?

public class Response<R> implements IResponse<R>, IResponseInitiator<R> {

    // Fields are written by IO thread, read by IO thread
    private volatile String mFunction;
    private volatile String mError;
    private volatile boolean mSuccess;
    private volatile String mCallId;
    private volatile R mResult;

    @Override
    public String getFunction() {
        T.BIZZ();
        return mFunction;
    }

    @Override
    public void setFunction(String function) {
        T.BIZZ();
        this.mFunction = function;
    }

    @Override
    public R getResponse() throws Exception {
        T.BIZZ();
        if (mSuccess)
            return mResult;
        throw new Exception(mError);
    }

    @Override
    public void setError(String value) {
        T.BIZZ();
        mError = value;
    }

    @Override
    public void setResult(R value) {
        T.BIZZ();
        mResult = value;
    }

    @Override
    public String getCallId() {
        T.BIZZ();
        return mCallId;
    }

    @Override
    public void setCallId(String callId) {
        T.BIZZ();
        mCallId = callId;
    }

    @Override
    public boolean getSuccess() {
        T.BIZZ();
        return mSuccess;
    }

    @Override
    public void setSuccess(boolean success) {
        T.BIZZ();
        this.mSuccess = success;
    }

}

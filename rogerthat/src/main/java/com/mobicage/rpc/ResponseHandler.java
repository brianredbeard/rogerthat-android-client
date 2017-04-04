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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.mobicage.rogerthat.MainService;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.pickle.PickleException;
import com.mobicage.rogerthat.util.system.T;

// XXX: should become an abstract class with an abstract handle and writePickle, readPickle method
// and we should have a default implementation which ignores the response

public class ResponseHandler<P> implements IResponseHandler<P> {

    protected final int CLASS_VERSION = 1;

    protected volatile MainService mMainService = null; // UI + IO thread
    protected volatile String mFunction = null; // UI + IO thread

    @Override
    public void handle(IResponse<P> response) {
        T.BIZZ();
        try {
            response.getResponse();
        } catch (Exception e) {
            L.d("Default response failed for function " + mFunction, e);
        }
    }

    @Override
    public void writePickle(DataOutput out) throws IOException {
        T.dontCare();
        out.writeUTF(mFunction);
    }

    @Override
    public final void setService(MainService pService) {
        T.dontCare();
        mMainService = pService;
    }

    @Override
    public final String getFunction() {
        T.dontCare();
        return mFunction;
    }

    @Override
    public int getPickleClassVersion() {
        T.dontCare();
        return CLASS_VERSION;
    }

    @Override
    public final void setFunction(String function) {
        T.dontCare();
        mFunction = function;
    }

    @Override
    public void readFromPickle(int version, DataInput in) throws IOException, PickleException {
        T.dontCare();
        mFunction = in.readUTF();
    }

}

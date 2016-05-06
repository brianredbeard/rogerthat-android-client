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

package com.mobicage.rogerthat.plugins.friends;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.json.simple.JSONValue;

import android.content.Intent;

import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.pickle.PickleException;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rpc.IResponse;
import com.mobicage.rpc.ResponseHandler;
import com.mobicage.to.service.FindServiceResponseTO;

public class FindServiceResponseHandler extends ResponseHandler<FindServiceResponseTO> {

    private String mSearchString;

    public FindServiceResponseHandler() {
    }

    public FindServiceResponseHandler(String searchString) {
        mSearchString = searchString;
    }

    @Override
    public void writePickle(DataOutput out) throws IOException {
        T.dontCare();
        super.writePickle(out);
        out.writeUTF(mSearchString);
    }

    @Override
    public void readFromPickle(int version, DataInput in) throws IOException, PickleException {
        T.dontCare();
        super.readFromPickle(version, in);
        mSearchString = in.readUTF();
    }

    @Override
    public void handle(IResponse<FindServiceResponseTO> result) {
        T.BIZZ();
        FindServiceResponseTO response;
        try {
            response = result.getResponse();
        } catch (Exception e) {
            L.d("FindService api call failed", e);
            mMainService.sendBroadcast(new Intent(FriendsPlugin.SERVICE_SEARCH_FAILED_INTENT));
            return;
        }

        Intent intent = new Intent(FriendsPlugin.SERVICE_SEARCH_RESULT_INTENT);
        intent.putExtra(ServiceSearchActivity.SEARCH_RESULT, JSONValue.toJSONString(response.toJSONMap()));
        intent.putExtra(ServiceSearchActivity.SEARCH_STRING, mSearchString);
        mMainService.sendBroadcast(intent);
    }

}
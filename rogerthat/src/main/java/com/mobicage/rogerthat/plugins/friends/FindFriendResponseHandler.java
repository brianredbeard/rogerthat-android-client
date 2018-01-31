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

package com.mobicage.rogerthat.plugins.friends;

import android.content.Intent;

import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.pickle.PickleException;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rpc.IResponse;
import com.mobicage.rpc.ResponseHandler;
import com.mobicage.to.friends.FindFriendResponseTO;

import org.json.simple.JSONValue;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class FindFriendResponseHandler extends ResponseHandler<FindFriendResponseTO> {

    private String mSearchString;

    public FindFriendResponseHandler() {
    }

    public FindFriendResponseHandler(String searchString) {
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
    public void handle(IResponse<FindFriendResponseTO> result) {
        T.BIZZ();
        FindFriendResponseTO response;
        try {
            response = result.getResponse();
        } catch (Exception e) {
            L.d("FindFriend api call failed", e);
            mMainService.sendBroadcast(new Intent(FriendsPlugin.FRIEND_SEARCH_FAILED_INTENT));
            return;
        }

        Intent intent = new Intent(FriendsPlugin.FRIEND_SEARCH_RESULT_INTENT);
        intent.putExtra(FriendsPlugin.SEARCH_RESULT, JSONValue.toJSONString(response.toJSONMap()));
        intent.putExtra(FriendsPlugin.SEARCH_STRING, mSearchString);
        mMainService.sendBroadcast(intent);
    }

}

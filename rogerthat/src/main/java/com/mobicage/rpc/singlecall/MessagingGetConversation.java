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

package com.mobicage.rpc.singlecall;

import java.util.Map;

import org.json.simple.JSONValue;

import com.mobicage.rogerthat.util.logging.L;

@SuppressWarnings("unchecked")
public class MessagingGetConversation extends SingleCall {

    public MessagingGetConversation(String function, String callBody) {
        super(function, callBody);
    }

    @Override
    public boolean isEqualToCallWithBody(String callBody) {
        String thisThread = (String) request.get("parent_message_key");
        if (thisThread == null) {
            L.e("parent_message_key is not supposed to be empty in " + this.function);
            return false;
        }

        Map<String, Object> otherCall = (Map<String, Object>) JSONValue.parse(callBody);
        Map<String, Object> otherArgs = (Map<String, Object>) otherCall.get("a");
        Map<String, Object> otherRequest = (Map<String, Object>) otherArgs.get("request");
        if (otherRequest == null)
            return false;

        String otherThread = (String) otherRequest.get("parent_message_key");
        return thisThread.equals(otherThread);
    }

}

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

package com.mobicage.rpc;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.json.simple.JSONValue;

import com.mobicage.rogerthat.MainService;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.system.T;

public class DefaultRpcHandler implements IRpcHandler {

    private final MainService mService;

    public DefaultRpcHandler(MainService pService) {
        T.UI();
        mService = pService;
    }

    @Override
    public void handle(final String function, final Map<String, Object> arguments,
        final IResponseHandler<?> responseHandler) {

        final String callId = UUID.randomUUID().toString();
        SafeRunnable handler = new SafeRunnable() {
            @Override
            public void safeRun() {
                T.BIZZ();
                final Map<String, Object> call = new LinkedHashMap<String, Object>();
                call.put("av", 1);
                call.put("f", function);
                call.put("ci", callId);
                call.put("a", arguments);
                long timestamp = mService.currentTimeMillis();
                call.put("t", timestamp / 1000);
                Rpc.submitter.call(callId, JSONValue.toJSONString(call), function, responseHandler, timestamp);
            }
        };
        if (T.getThreadType() == T.BIZZ && RpcCall.DO_NOT_POST_IF_ON_BIZZ_THREAD_FUNCTIONS.contains(function)) {
            handler.run();
        } else {
            mService.postOnBIZZHandler(handler);
        }
    }
}
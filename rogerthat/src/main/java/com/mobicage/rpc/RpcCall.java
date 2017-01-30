/*
 * Copyright 2017 Mobicage NV
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
 * @@license_version:1.2@@
 */
package com.mobicage.rpc;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.mobicage.rpc.singlecall.SingleCall;

public class RpcCall extends RpcItem {

    public static final List<String> WIFI_ONLY_FUNCTIONS = Arrays
        .asList(new String[] { "com.mobicage.api.messaging.uploadChunk" });

    public static final List<String> DO_NOT_POST_IF_ON_BIZZ_THREAD_FUNCTIONS = Arrays
        .asList(new String[] { "com.mobicage.api.messaging.uploadChunk" });

    public static RpcCall parse(Map<String, Object> parsedCall) {
        final Long timestamp;
        Object t = parsedCall.get("t");
        if (t instanceof Double)
            timestamp = ((Double) t).longValue() * 1000;
        else
            timestamp = (Long) t * 1000;
        final String function = (String) parsedCall.get("f");
        final String callId = (String) parsedCall.get("ci");
        @SuppressWarnings("unchecked")
        final Map<String, Object> arguments = (Map<String, Object>) parsedCall.get("a");
        return new RpcCall(callId, timestamp, function, arguments);
    }

    public static RpcCall parse(final String callId, final String packet) throws InvalidProtocolVersionException,
        ProtocolError {
        try {
            final java.util.Map<String, Object> parsedCall = RpcItem.parseAndValidate(packet, new String[] { "f", "a",
                "t" }, callId);

            return RpcCall.parse(parsedCall);
        } catch (ClassCastException e) {
            throw new ProtocolError(e);
        }
    }

    public static boolean isSingleCall(String function) {
        return SingleCall.SINGLE_CALL_FUNCTIONS.contains(function);
    }

    public static boolean isSpecialSingleCall(String function) {
        return SingleCall.SPECIAL_SINGLE_CALL_FUNCTIONS.contains(function);
    }

    public final String function;
    public final Map<String, Object> arguments;

    public RpcCall(final String callId, final long timestamp, final String function, final Map<String, Object> arguments) {
        super(callId, timestamp);
        this.function = function;
        this.arguments = arguments;
    }

}

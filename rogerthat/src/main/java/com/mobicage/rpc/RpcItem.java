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

public abstract class RpcItem {

    public final String callId;
    public final long timestamp;

    protected RpcItem(final String callId, final long timestamp) {
        this.callId = callId;
        this.timestamp = timestamp;
    }

    protected static java.util.Map<String, Object> parseAndValidate(final String packet, final String[] keys,
        final String callId) throws ProtocolError, InvalidProtocolVersionException {
        @SuppressWarnings("unchecked")
        final java.util.Map<String, Object> parsedCall = (java.util.Map<String, Object>) org.json.simple.JSONValue
            .parse(packet);

        if (!parsedCall.containsKey("av"))
            throw new ProtocolError();
        final Object av = parsedCall.get("av");
        if (!(av instanceof Long)) {
            throw new ProtocolError();
        }
        if (((Long) av) != 1)
            throw new InvalidProtocolVersionException();

        for (String key : keys) {
            if (!parsedCall.containsKey(key))
                throw new ProtocolError();
        }

        if (!parsedCall.containsKey("ci")) {
            throw new ProtocolError();
        }

        if (callId == null || !callId.equals(parsedCall.get("ci")))
            throw new ProtocolError();

        return parsedCall;
    }
}
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

import java.util.HashMap;
import java.util.Map;

public class RpcResult extends RpcItem {

    public final Object result;
    public final String error;
    public final boolean success;

    public RpcResult(final String callId, final long timestamp, final Object result) {
        super(callId, timestamp);
        this.result = result;
        this.success = true;
        this.error = null;
    }

    public RpcResult(final String callId, final long timestamp, final String error) {
        super(callId, timestamp);
        this.result = null;
        this.success = false;
        this.error = error;
    }

    public RpcResult(final RpcCall call, final Object result) {
        super(call.callId, call.timestamp);
        this.result = result;
        this.success = true;
        this.error = null;
    }

    public RpcResult(final RpcCall call, final String error) {
        super(call.callId, call.timestamp);
        this.result = null;
        this.success = false;
        this.error = error;
    }

    public String toJSON() {
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("av", 1);
        m.put("ci", callId);
        m.put("t", timestamp / 1000);
        m.put("s", success ? "success" : "fail");
        if (success)
            m.put("r", result);
        else
            m.put("e", error);
        return org.json.simple.JSONValue.toJSONString(m);
    }

    public static RpcResult parse(final String callId, final String packet) throws ProtocolError,
        InvalidProtocolVersionException {
        try {
            final java.util.Map<String, Object> parsedCall = RpcItem.parseAndValidate(packet,
                new String[] { "s", "t" }, callId);

            final Long timestamp = (Long) parsedCall.get("t");
            if ("success".equals(parsedCall.get("s"))) {
                if (!parsedCall.containsKey("r"))
                    throw new ProtocolError();
                final Object result = parsedCall.get("r");
                return new RpcResult(callId, timestamp, result);
            } else {
                if (!parsedCall.containsKey("e"))
                    throw new ProtocolError();
                final String error = (String) parsedCall.get("e");
                return new RpcResult(callId, timestamp, error);
            }
        } catch (ClassCastException e) {
            throw new ProtocolError();
        }
    }
}

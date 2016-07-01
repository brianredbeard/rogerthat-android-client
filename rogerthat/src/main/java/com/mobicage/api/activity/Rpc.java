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

package com.mobicage.api.activity;

public class Rpc {

    public static void logCall(com.mobicage.rpc.IResponseHandler<com.mobicage.to.activity.LogCallResponseTO> responseHandler,
            com.mobicage.to.activity.LogCallRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.activity.logCall", arguments, responseHandler);
    }

    public static void logLocations(com.mobicage.rpc.IResponseHandler<com.mobicage.to.activity.LogLocationsResponseTO> responseHandler,
            com.mobicage.to.activity.LogLocationsRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.activity.logLocations", arguments, responseHandler);
    }

}

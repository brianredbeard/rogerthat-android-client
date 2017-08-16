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

package com.mobicage.api.messaging.jsmfr;

public class Rpc {

    public static void flowStarted(com.mobicage.rpc.IResponseHandler<com.mobicage.to.messaging.jsmfr.FlowStartedResponseTO> responseHandler,
            com.mobicage.to.messaging.jsmfr.FlowStartedRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.messaging.jsmfr.flowStarted", arguments, responseHandler);
    }

    public static void messageFlowError(com.mobicage.rpc.IResponseHandler<com.mobicage.to.messaging.jsmfr.MessageFlowErrorResponseTO> responseHandler,
            com.mobicage.to.messaging.jsmfr.MessageFlowErrorRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.messaging.jsmfr.messageFlowError", arguments, responseHandler);
    }

    public static void messageFlowFinished(com.mobicage.rpc.IResponseHandler<com.mobicage.to.messaging.jsmfr.MessageFlowFinishedResponseTO> responseHandler,
            com.mobicage.to.messaging.jsmfr.MessageFlowFinishedRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.messaging.jsmfr.messageFlowFinished", arguments, responseHandler);
    }

    public static void messageFlowMemberResult(com.mobicage.rpc.IResponseHandler<com.mobicage.to.messaging.jsmfr.MessageFlowMemberResultResponseTO> responseHandler,
            com.mobicage.to.messaging.jsmfr.MessageFlowMemberResultRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.messaging.jsmfr.messageFlowMemberResult", arguments, responseHandler);
    }

    public static void newFlowMessage(com.mobicage.rpc.IResponseHandler<com.mobicage.to.messaging.jsmfr.NewFlowMessageResponseTO> responseHandler,
            com.mobicage.to.messaging.jsmfr.NewFlowMessageRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.messaging.jsmfr.newFlowMessage", arguments, responseHandler);
    }

}

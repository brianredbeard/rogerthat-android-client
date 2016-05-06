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

package com.mobicage.api.services;

public class Rpc {

    public static void findService(com.mobicage.rpc.IResponseHandler<com.mobicage.to.service.FindServiceResponseTO> responseHandler,
            com.mobicage.to.service.FindServiceRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.services.findService", arguments, responseHandler);
    }

    public static void getActionInfo(com.mobicage.rpc.IResponseHandler<com.mobicage.to.service.GetServiceActionInfoResponseTO> responseHandler,
            com.mobicage.to.service.GetServiceActionInfoRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.services.getActionInfo", arguments, responseHandler);
    }

    public static void getMenuIcon(com.mobicage.rpc.IResponseHandler<com.mobicage.to.service.GetMenuIconResponseTO> responseHandler,
            com.mobicage.to.service.GetMenuIconRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.services.getMenuIcon", arguments, responseHandler);
    }

    public static void getStaticFlow(com.mobicage.rpc.IResponseHandler<com.mobicage.to.service.GetStaticFlowResponseTO> responseHandler,
            com.mobicage.to.service.GetStaticFlowRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.services.getStaticFlow", arguments, responseHandler);
    }

    public static void pokeService(com.mobicage.rpc.IResponseHandler<com.mobicage.to.service.PokeServiceResponseTO> responseHandler,
            com.mobicage.to.service.PokeServiceRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.services.pokeService", arguments, responseHandler);
    }

    public static void pressMenuItem(com.mobicage.rpc.IResponseHandler<com.mobicage.to.service.PressMenuIconResponseTO> responseHandler,
            com.mobicage.to.service.PressMenuIconRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.services.pressMenuItem", arguments, responseHandler);
    }

    public static void sendApiCall(com.mobicage.rpc.IResponseHandler<com.mobicage.to.service.SendApiCallResponseTO> responseHandler,
            com.mobicage.to.service.SendApiCallRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.services.sendApiCall", arguments, responseHandler);
    }

    public static void shareService(com.mobicage.rpc.IResponseHandler<com.mobicage.to.service.ShareServiceResponseTO> responseHandler,
            com.mobicage.to.service.ShareServiceRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.services.shareService", arguments, responseHandler);
    }

    public static void startAction(com.mobicage.rpc.IResponseHandler<com.mobicage.to.service.StartServiceActionResponseTO> responseHandler,
            com.mobicage.to.service.StartServiceActionRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.services.startAction", arguments, responseHandler);
    }

    public static void updateUserData(com.mobicage.rpc.IResponseHandler<com.mobicage.to.service.UpdateUserDataResponseTO> responseHandler,
            com.mobicage.to.service.UpdateUserDataRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.services.updateUserData", arguments, responseHandler);
    }

}
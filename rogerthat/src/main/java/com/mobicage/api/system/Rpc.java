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

package com.mobicage.api.system;

public class Rpc {

    public static void editProfile(com.mobicage.rpc.IResponseHandler<com.mobicage.to.system.EditProfileResponseTO> responseHandler,
            com.mobicage.to.system.EditProfileRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.system.editProfile", arguments, responseHandler);
    }

    public static void getAppAsset(com.mobicage.rpc.IResponseHandler<com.mobicage.to.app.GetAppAssetResponseTO> responseHandler,
            com.mobicage.to.app.GetAppAssetRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.system.getAppAsset", arguments, responseHandler);
    }

    public static void getIdentity(com.mobicage.rpc.IResponseHandler<com.mobicage.to.system.GetIdentityResponseTO> responseHandler,
            com.mobicage.to.system.GetIdentityRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.system.getIdentity", arguments, responseHandler);
    }

    public static void getIdentityQRCode(com.mobicage.rpc.IResponseHandler<com.mobicage.to.system.GetIdentityQRCodeResponseTO> responseHandler,
            com.mobicage.to.system.GetIdentityQRCodeRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.system.getIdentityQRCode", arguments, responseHandler);
    }

    public static void getJsEmbedding(com.mobicage.rpc.IResponseHandler<com.mobicage.to.js_embedding.GetJSEmbeddingResponseTO> responseHandler,
            com.mobicage.to.js_embedding.GetJSEmbeddingRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.system.getJsEmbedding", arguments, responseHandler);
    }

    public static void heartBeat(com.mobicage.rpc.IResponseHandler<com.mobicage.to.system.HeartBeatResponseTO> responseHandler,
            com.mobicage.to.system.HeartBeatRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.system.heartBeat", arguments, responseHandler);
    }

    public static void logError(com.mobicage.rpc.IResponseHandler<com.mobicage.to.system.LogErrorResponseTO> responseHandler,
            com.mobicage.to.system.LogErrorRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.system.logError", arguments, responseHandler);
    }

    public static void saveSettings(com.mobicage.rpc.IResponseHandler<com.mobicage.to.system.SaveSettingsResponse> responseHandler,
            com.mobicage.to.system.SaveSettingsRequest request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.system.saveSettings", arguments, responseHandler);
    }

    public static void setMobilePhoneNumber(com.mobicage.rpc.IResponseHandler<com.mobicage.to.system.SetMobilePhoneNumberResponseTO> responseHandler,
            com.mobicage.to.system.SetMobilePhoneNumberRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.system.setMobilePhoneNumber", arguments, responseHandler);
    }

    public static void setSecureInfo(com.mobicage.rpc.IResponseHandler<com.mobicage.to.system.SetSecureInfoResponseTO> responseHandler,
            com.mobicage.to.system.SetSecureInfoRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.system.setSecureInfo", arguments, responseHandler);
    }

    public static void unregisterMobile(com.mobicage.rpc.IResponseHandler<com.mobicage.to.system.UnregisterMobileResponseTO> responseHandler,
            com.mobicage.to.system.UnregisterMobileRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.system.unregisterMobile", arguments, responseHandler);
    }

    public static void updateApplePushDeviceToken(com.mobicage.rpc.IResponseHandler<com.mobicage.to.system.UpdateApplePushDeviceTokenResponseTO> responseHandler,
            com.mobicage.to.system.UpdateApplePushDeviceTokenRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.system.updateApplePushDeviceToken", arguments, responseHandler);
    }

}

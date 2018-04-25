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

package com.mobicage.api.payment;

public class Rpc {

    public static void cancelPayment(com.mobicage.rpc.IResponseHandler<com.mobicage.to.payment.CancelPaymentResponseTO> responseHandler,
            com.mobicage.to.payment.CancelPaymentRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.payment.cancelPayment", arguments, responseHandler);
    }

    public static void confirmPayment(com.mobicage.rpc.IResponseHandler<com.mobicage.to.payment.ConfirmPaymentResponseTO> responseHandler,
            com.mobicage.to.payment.ConfirmPaymentRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.payment.confirmPayment", arguments, responseHandler);
    }

    public static void createAsset(com.mobicage.rpc.IResponseHandler<com.mobicage.to.payment.CreateAssetResponseTO> responseHandler,
            com.mobicage.to.payment.CreateAssetRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.payment.createAsset", arguments, responseHandler);
    }

    public static void createTransaction(com.mobicage.rpc.IResponseHandler<com.mobicage.to.payment.CreateTransactionResponseTO> responseHandler,
            com.mobicage.to.payment.CreateTransactionRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.payment.createTransaction", arguments, responseHandler);
    }

    public static void getPaymentAssets(com.mobicage.rpc.IResponseHandler<com.mobicage.to.payment.GetPaymentAssetsResponseTO> responseHandler,
            com.mobicage.to.payment.GetPaymentAssetsRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.payment.getPaymentAssets", arguments, responseHandler);
    }

    public static void getPaymentMethods(com.mobicage.rpc.IResponseHandler<com.mobicage.to.payment.GetPaymentMethodsResponseTO> responseHandler,
            com.mobicage.to.payment.GetPaymentMethodsRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.payment.getPaymentMethods", arguments, responseHandler);
    }

    public static void getPaymentProfile(com.mobicage.rpc.IResponseHandler<com.mobicage.to.payment.GetPaymentProfileResponseTO> responseHandler,
            com.mobicage.to.payment.GetPaymentProfileRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.payment.getPaymentProfile", arguments, responseHandler);
    }

    public static void getPaymentProviders(com.mobicage.rpc.IResponseHandler<com.mobicage.to.payment.GetPaymentProvidersResponseTO> responseHandler,
            com.mobicage.to.payment.GetPaymentProvidersRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.payment.getPaymentProviders", arguments, responseHandler);
    }

    public static void getPaymentTransactions(com.mobicage.rpc.IResponseHandler<com.mobicage.to.payment.GetPaymentTransactionsResponseTO> responseHandler,
            com.mobicage.to.payment.GetPaymentTransactionsRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.payment.getPaymentTransactions", arguments, responseHandler);
    }

    public static void getPendingPaymentDetails(com.mobicage.rpc.IResponseHandler<com.mobicage.to.payment.GetPendingPaymentDetailsResponseTO> responseHandler,
            com.mobicage.to.payment.GetPendingPaymentDetailsRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.payment.getPendingPaymentDetails", arguments, responseHandler);
    }

    public static void getPendingPaymentSignatureData(com.mobicage.rpc.IResponseHandler<com.mobicage.to.payment.GetPendingPaymentSignatureDataResponseTO> responseHandler,
            com.mobicage.to.payment.GetPendingPaymentSignatureDataRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.payment.getPendingPaymentSignatureData", arguments, responseHandler);
    }

    public static void getTargetInfo(com.mobicage.rpc.IResponseHandler<com.mobicage.to.payment.GetTargetInfoResponseTO> responseHandler,
            com.mobicage.to.payment.GetTargetInfoRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.payment.getTargetInfo", arguments, responseHandler);
    }

    public static void receivePayment(com.mobicage.rpc.IResponseHandler<com.mobicage.to.payment.ReceivePaymentResponseTO> responseHandler,
            com.mobicage.to.payment.ReceivePaymentRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.payment.receivePayment", arguments, responseHandler);
    }

    public static void verifyPaymentAsset(com.mobicage.rpc.IResponseHandler<com.mobicage.to.payment.VerifyPaymentAssetResponseTO> responseHandler,
            com.mobicage.to.payment.VerifyPaymentAssetRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.payment.verifyPaymentAsset", arguments, responseHandler);
    }

}

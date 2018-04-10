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

package com.mobicage.api.messaging;

public class Rpc {

    public static void ackMessage(com.mobicage.rpc.IResponseHandler<com.mobicage.to.messaging.AckMessageResponseTO> responseHandler,
            com.mobicage.to.messaging.AckMessageRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.messaging.ackMessage", arguments, responseHandler);
    }

    public static void deleteConversation(com.mobicage.rpc.IResponseHandler<com.mobicage.to.messaging.DeleteConversationResponseTO> responseHandler,
            com.mobicage.to.messaging.DeleteConversationRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.messaging.deleteConversation", arguments, responseHandler);
    }

    public static void getConversation(com.mobicage.rpc.IResponseHandler<com.mobicage.to.messaging.GetConversationResponseTO> responseHandler,
            com.mobicage.to.messaging.GetConversationRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.messaging.getConversation", arguments, responseHandler);
    }

    public static void getConversationAvatar(com.mobicage.rpc.IResponseHandler<com.mobicage.to.messaging.GetConversationAvatarResponseTO> responseHandler,
            com.mobicage.to.messaging.GetConversationAvatarRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.messaging.getConversationAvatar", arguments, responseHandler);
    }

    public static void lockMessage(com.mobicage.rpc.IResponseHandler<com.mobicage.to.messaging.LockMessageResponseTO> responseHandler,
            com.mobicage.to.messaging.LockMessageRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.messaging.lockMessage", arguments, responseHandler);
    }

    public static void markMessagesAsRead(com.mobicage.rpc.IResponseHandler<com.mobicage.to.messaging.MarkMessagesAsReadResponseTO> responseHandler,
            com.mobicage.to.messaging.MarkMessagesAsReadRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.messaging.markMessagesAsRead", arguments, responseHandler);
    }

    public static void sendMessage(com.mobicage.rpc.IResponseHandler<com.mobicage.to.messaging.SendMessageResponseTO> responseHandler,
            com.mobicage.to.messaging.SendMessageRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.messaging.sendMessage", arguments, responseHandler);
    }

    public static void submitAdvancedOrderForm(com.mobicage.rpc.IResponseHandler<com.mobicage.to.messaging.forms.SubmitAdvancedOrderFormResponseTO> responseHandler,
            com.mobicage.to.messaging.forms.SubmitAdvancedOrderFormRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.messaging.submitAdvancedOrderForm", arguments, responseHandler);
    }

    public static void submitAutoCompleteForm(com.mobicage.rpc.IResponseHandler<com.mobicage.to.messaging.forms.SubmitAutoCompleteFormResponseTO> responseHandler,
            com.mobicage.to.messaging.forms.SubmitAutoCompleteFormRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.messaging.submitAutoCompleteForm", arguments, responseHandler);
    }

    public static void submitDateSelectForm(com.mobicage.rpc.IResponseHandler<com.mobicage.to.messaging.forms.SubmitDateSelectFormResponseTO> responseHandler,
            com.mobicage.to.messaging.forms.SubmitDateSelectFormRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.messaging.submitDateSelectForm", arguments, responseHandler);
    }

    public static void submitFriendSelectForm(com.mobicage.rpc.IResponseHandler<com.mobicage.to.messaging.forms.SubmitFriendSelectFormResponseTO> responseHandler,
            com.mobicage.to.messaging.forms.SubmitFriendSelectFormRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.messaging.submitFriendSelectForm", arguments, responseHandler);
    }

    public static void submitGPSLocationForm(com.mobicage.rpc.IResponseHandler<com.mobicage.to.messaging.forms.SubmitGPSLocationFormResponseTO> responseHandler,
            com.mobicage.to.messaging.forms.SubmitGPSLocationFormRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.messaging.submitGPSLocationForm", arguments, responseHandler);
    }

    public static void submitMultiSelectForm(com.mobicage.rpc.IResponseHandler<com.mobicage.to.messaging.forms.SubmitMultiSelectFormResponseTO> responseHandler,
            com.mobicage.to.messaging.forms.SubmitMultiSelectFormRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.messaging.submitMultiSelectForm", arguments, responseHandler);
    }

    public static void submitMyDigiPassForm(com.mobicage.rpc.IResponseHandler<com.mobicage.to.messaging.forms.SubmitMyDigiPassFormResponseTO> responseHandler,
            com.mobicage.to.messaging.forms.SubmitMyDigiPassFormRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.messaging.submitMyDigiPassForm", arguments, responseHandler);
    }

    public static void submitOauthForm(com.mobicage.rpc.IResponseHandler<com.mobicage.to.messaging.forms.SubmitOauthFormResponseTO> responseHandler,
            com.mobicage.to.messaging.forms.SubmitOauthFormRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.messaging.submitOauthForm", arguments, responseHandler);
    }

    public static void submitPayForm(com.mobicage.rpc.IResponseHandler<com.mobicage.to.messaging.forms.SubmitPayFormResponseTO> responseHandler,
            com.mobicage.to.messaging.forms.SubmitPayFormRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.messaging.submitPayForm", arguments, responseHandler);
    }

    public static void submitPhotoUploadForm(com.mobicage.rpc.IResponseHandler<com.mobicage.to.messaging.forms.SubmitPhotoUploadFormResponseTO> responseHandler,
            com.mobicage.to.messaging.forms.SubmitPhotoUploadFormRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.messaging.submitPhotoUploadForm", arguments, responseHandler);
    }

    public static void submitRangeSliderForm(com.mobicage.rpc.IResponseHandler<com.mobicage.to.messaging.forms.SubmitRangeSliderFormResponseTO> responseHandler,
            com.mobicage.to.messaging.forms.SubmitRangeSliderFormRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.messaging.submitRangeSliderForm", arguments, responseHandler);
    }

    public static void submitSignForm(com.mobicage.rpc.IResponseHandler<com.mobicage.to.messaging.forms.SubmitSignFormResponseTO> responseHandler,
            com.mobicage.to.messaging.forms.SubmitSignFormRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.messaging.submitSignForm", arguments, responseHandler);
    }

    public static void submitSingleSelectForm(com.mobicage.rpc.IResponseHandler<com.mobicage.to.messaging.forms.SubmitSingleSelectFormResponseTO> responseHandler,
            com.mobicage.to.messaging.forms.SubmitSingleSelectFormRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.messaging.submitSingleSelectForm", arguments, responseHandler);
    }

    public static void submitSingleSliderForm(com.mobicage.rpc.IResponseHandler<com.mobicage.to.messaging.forms.SubmitSingleSliderFormResponseTO> responseHandler,
            com.mobicage.to.messaging.forms.SubmitSingleSliderFormRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.messaging.submitSingleSliderForm", arguments, responseHandler);
    }

    public static void submitTextBlockForm(com.mobicage.rpc.IResponseHandler<com.mobicage.to.messaging.forms.SubmitTextBlockFormResponseTO> responseHandler,
            com.mobicage.to.messaging.forms.SubmitTextBlockFormRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.messaging.submitTextBlockForm", arguments, responseHandler);
    }

    public static void submitTextLineForm(com.mobicage.rpc.IResponseHandler<com.mobicage.to.messaging.forms.SubmitTextLineFormResponseTO> responseHandler,
            com.mobicage.to.messaging.forms.SubmitTextLineFormRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.messaging.submitTextLineForm", arguments, responseHandler);
    }

    public static void uploadChunk(com.mobicage.rpc.IResponseHandler<com.mobicage.to.messaging.UploadChunkResponseTO> responseHandler,
            com.mobicage.to.messaging.UploadChunkRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.messaging.uploadChunk", arguments, responseHandler);
    }

}

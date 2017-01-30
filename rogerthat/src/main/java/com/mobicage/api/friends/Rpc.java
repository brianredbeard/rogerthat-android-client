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

package com.mobicage.api.friends;

public class Rpc {

    public static void ackInvitationByInvitationSecret(com.mobicage.rpc.IResponseHandler<com.mobicage.to.friends.AckInvitationByInvitationSecretResponseTO> responseHandler,
            com.mobicage.to.friends.AckInvitationByInvitationSecretRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.friends.ackInvitationByInvitationSecret", arguments, responseHandler);
    }

    public static void breakFriendShip(com.mobicage.rpc.IResponseHandler<com.mobicage.to.friends.BreakFriendshipResponseTO> responseHandler,
            com.mobicage.to.friends.BreakFriendshipRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.friends.breakFriendShip", arguments, responseHandler);
    }

    public static void deleteGroup(com.mobicage.rpc.IResponseHandler<com.mobicage.to.friends.DeleteGroupResponseTO> responseHandler,
            com.mobicage.to.friends.DeleteGroupRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.friends.deleteGroup", arguments, responseHandler);
    }

    public static void findFriend(com.mobicage.rpc.IResponseHandler<com.mobicage.to.friends.FindFriendResponseTO> responseHandler,
            com.mobicage.to.friends.FindFriendRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.friends.findFriend", arguments, responseHandler);
    }

    public static void findRogerthatUsersViaEmail(com.mobicage.rpc.IResponseHandler<com.mobicage.to.friends.FindRogerthatUsersViaEmailResponseTO> responseHandler,
            com.mobicage.to.friends.FindRogerthatUsersViaEmailRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.friends.findRogerthatUsersViaEmail", arguments, responseHandler);
    }

    public static void findRogerthatUsersViaFacebook(com.mobicage.rpc.IResponseHandler<com.mobicage.to.friends.FindRogerthatUsersViaFacebookResponseTO> responseHandler,
            com.mobicage.to.friends.FindRogerthatUsersViaFacebookRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.friends.findRogerthatUsersViaFacebook", arguments, responseHandler);
    }

    public static void getAvatar(com.mobicage.rpc.IResponseHandler<com.mobicage.to.friends.GetAvatarResponseTO> responseHandler,
            com.mobicage.to.friends.GetAvatarRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.friends.getAvatar", arguments, responseHandler);
    }

    public static void getCategory(com.mobicage.rpc.IResponseHandler<com.mobicage.to.friends.GetCategoryResponseTO> responseHandler,
            com.mobicage.to.friends.GetCategoryRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.friends.getCategory", arguments, responseHandler);
    }

    public static void getFriend(com.mobicage.rpc.IResponseHandler<com.mobicage.to.friends.GetFriendResponseTO> responseHandler,
            com.mobicage.to.friends.GetFriendRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.friends.getFriend", arguments, responseHandler);
    }

    public static void getFriendEmails(com.mobicage.rpc.IResponseHandler<com.mobicage.to.friends.GetFriendEmailsResponseTO> responseHandler,
            com.mobicage.to.friends.GetFriendEmailsRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.friends.getFriendEmails", arguments, responseHandler);
    }

    public static void getFriendInvitationSecrets(com.mobicage.rpc.IResponseHandler<com.mobicage.to.friends.GetFriendInvitationSecretsResponseTO> responseHandler,
            com.mobicage.to.friends.GetFriendInvitationSecretsRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.friends.getFriendInvitationSecrets", arguments, responseHandler);
    }

    public static void getFriends(com.mobicage.rpc.IResponseHandler<com.mobicage.to.friends.GetFriendsListResponseTO> responseHandler,
            com.mobicage.to.friends.GetFriendsListRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.friends.getFriends", arguments, responseHandler);
    }

    public static void getGroupAvatar(com.mobicage.rpc.IResponseHandler<com.mobicage.to.friends.GetGroupAvatarResponseTO> responseHandler,
            com.mobicage.to.friends.GetGroupAvatarRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.friends.getGroupAvatar", arguments, responseHandler);
    }

    public static void getGroups(com.mobicage.rpc.IResponseHandler<com.mobicage.to.friends.GetGroupsResponseTO> responseHandler,
            com.mobicage.to.friends.GetGroupsRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.friends.getGroups", arguments, responseHandler);
    }

    public static void getUserInfo(com.mobicage.rpc.IResponseHandler<com.mobicage.to.friends.GetUserInfoResponseTO> responseHandler,
            com.mobicage.to.friends.GetUserInfoRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.friends.getUserInfo", arguments, responseHandler);
    }

    public static void invite(com.mobicage.rpc.IResponseHandler<com.mobicage.to.friends.InviteFriendResponseTO> responseHandler,
            com.mobicage.to.friends.InviteFriendRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.friends.invite", arguments, responseHandler);
    }

    public static void logInvitationSecretSent(com.mobicage.rpc.IResponseHandler<com.mobicage.to.friends.LogInvitationSecretSentResponseTO> responseHandler,
            com.mobicage.to.friends.LogInvitationSecretSentRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.friends.logInvitationSecretSent", arguments, responseHandler);
    }

    public static void putGroup(com.mobicage.rpc.IResponseHandler<com.mobicage.to.friends.PutGroupResponseTO> responseHandler,
            com.mobicage.to.friends.PutGroupRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.friends.putGroup", arguments, responseHandler);
    }

    public static void requestShareLocation(com.mobicage.rpc.IResponseHandler<com.mobicage.to.friends.RequestShareLocationResponseTO> responseHandler,
            com.mobicage.to.friends.RequestShareLocationRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.friends.requestShareLocation", arguments, responseHandler);
    }

    public static void shareLocation(com.mobicage.rpc.IResponseHandler<com.mobicage.to.friends.ShareLocationResponseTO> responseHandler,
            com.mobicage.to.friends.ShareLocationRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.friends.shareLocation", arguments, responseHandler);
    }

    public static void userScanned(com.mobicage.rpc.IResponseHandler<com.mobicage.to.friends.UserScannedResponseTO> responseHandler,
            com.mobicage.to.friends.UserScannedRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.friends.userScanned", arguments, responseHandler);
    }

}

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

package com.mobicage.rpc;

public class ResponseReceiverHandler {

    @SuppressWarnings("unchecked")
    public static void handle(final RpcResult rpcr, final IResponseHandler<?> responseHandler) throws IncompleteMessageException {
        final java.lang.String function = responseHandler.getFunction();
        if ("com.mobicage.api.activity.logCall".equals(function)) {
            final Response<com.mobicage.to.activity.LogCallResponseTO> resp = new Response<com.mobicage.to.activity.LogCallResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToActivityLogCallResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.activity.LogCallResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.activity.logLocations".equals(function)) {
            final Response<com.mobicage.to.activity.LogLocationsResponseTO> resp = new Response<com.mobicage.to.activity.LogLocationsResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToActivityLogLocationsResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.activity.LogLocationsResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.friends.ackInvitationByInvitationSecret".equals(function)) {
            final Response<com.mobicage.to.friends.AckInvitationByInvitationSecretResponseTO> resp = new Response<com.mobicage.to.friends.AckInvitationByInvitationSecretResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToFriendsAckInvitationByInvitationSecretResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.friends.AckInvitationByInvitationSecretResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.friends.breakFriendShip".equals(function)) {
            final Response<com.mobicage.to.friends.BreakFriendshipResponseTO> resp = new Response<com.mobicage.to.friends.BreakFriendshipResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToFriendsBreakFriendshipResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.friends.BreakFriendshipResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.friends.deleteGroup".equals(function)) {
            final Response<com.mobicage.to.friends.DeleteGroupResponseTO> resp = new Response<com.mobicage.to.friends.DeleteGroupResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToFriendsDeleteGroupResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.friends.DeleteGroupResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.friends.findFriend".equals(function)) {
            final Response<com.mobicage.to.friends.FindFriendResponseTO> resp = new Response<com.mobicage.to.friends.FindFriendResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToFriendsFindFriendResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.friends.FindFriendResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.friends.findRogerthatUsersViaEmail".equals(function)) {
            final Response<com.mobicage.to.friends.FindRogerthatUsersViaEmailResponseTO> resp = new Response<com.mobicage.to.friends.FindRogerthatUsersViaEmailResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToFriendsFindRogerthatUsersViaEmailResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.friends.FindRogerthatUsersViaEmailResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.friends.findRogerthatUsersViaFacebook".equals(function)) {
            final Response<com.mobicage.to.friends.FindRogerthatUsersViaFacebookResponseTO> resp = new Response<com.mobicage.to.friends.FindRogerthatUsersViaFacebookResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToFriendsFindRogerthatUsersViaFacebookResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.friends.FindRogerthatUsersViaFacebookResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.friends.getAvatar".equals(function)) {
            final Response<com.mobicage.to.friends.GetAvatarResponseTO> resp = new Response<com.mobicage.to.friends.GetAvatarResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToFriendsGetAvatarResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.friends.GetAvatarResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.friends.getCategory".equals(function)) {
            final Response<com.mobicage.to.friends.GetCategoryResponseTO> resp = new Response<com.mobicage.to.friends.GetCategoryResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToFriendsGetCategoryResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.friends.GetCategoryResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.friends.getFriend".equals(function)) {
            final Response<com.mobicage.to.friends.GetFriendResponseTO> resp = new Response<com.mobicage.to.friends.GetFriendResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToFriendsGetFriendResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.friends.GetFriendResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.friends.getFriendEmails".equals(function)) {
            final Response<com.mobicage.to.friends.GetFriendEmailsResponseTO> resp = new Response<com.mobicage.to.friends.GetFriendEmailsResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToFriendsGetFriendEmailsResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.friends.GetFriendEmailsResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.friends.getFriendInvitationSecrets".equals(function)) {
            final Response<com.mobicage.to.friends.GetFriendInvitationSecretsResponseTO> resp = new Response<com.mobicage.to.friends.GetFriendInvitationSecretsResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToFriendsGetFriendInvitationSecretsResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.friends.GetFriendInvitationSecretsResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.friends.getFriends".equals(function)) {
            final Response<com.mobicage.to.friends.GetFriendsListResponseTO> resp = new Response<com.mobicage.to.friends.GetFriendsListResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToFriendsGetFriendsListResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.friends.GetFriendsListResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.friends.getGroupAvatar".equals(function)) {
            final Response<com.mobicage.to.friends.GetGroupAvatarResponseTO> resp = new Response<com.mobicage.to.friends.GetGroupAvatarResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToFriendsGetGroupAvatarResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.friends.GetGroupAvatarResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.friends.getGroups".equals(function)) {
            final Response<com.mobicage.to.friends.GetGroupsResponseTO> resp = new Response<com.mobicage.to.friends.GetGroupsResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToFriendsGetGroupsResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.friends.GetGroupsResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.friends.getUserInfo".equals(function)) {
            final Response<com.mobicage.to.friends.GetUserInfoResponseTO> resp = new Response<com.mobicage.to.friends.GetUserInfoResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToFriendsGetUserInfoResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.friends.GetUserInfoResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.friends.invite".equals(function)) {
            final Response<com.mobicage.to.friends.InviteFriendResponseTO> resp = new Response<com.mobicage.to.friends.InviteFriendResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToFriendsInviteFriendResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.friends.InviteFriendResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.friends.logInvitationSecretSent".equals(function)) {
            final Response<com.mobicage.to.friends.LogInvitationSecretSentResponseTO> resp = new Response<com.mobicage.to.friends.LogInvitationSecretSentResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToFriendsLogInvitationSecretSentResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.friends.LogInvitationSecretSentResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.friends.putGroup".equals(function)) {
            final Response<com.mobicage.to.friends.PutGroupResponseTO> resp = new Response<com.mobicage.to.friends.PutGroupResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToFriendsPutGroupResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.friends.PutGroupResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.friends.requestShareLocation".equals(function)) {
            final Response<com.mobicage.to.friends.RequestShareLocationResponseTO> resp = new Response<com.mobicage.to.friends.RequestShareLocationResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToFriendsRequestShareLocationResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.friends.RequestShareLocationResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.friends.shareLocation".equals(function)) {
            final Response<com.mobicage.to.friends.ShareLocationResponseTO> resp = new Response<com.mobicage.to.friends.ShareLocationResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToFriendsShareLocationResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.friends.ShareLocationResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.friends.userScanned".equals(function)) {
            final Response<com.mobicage.to.friends.UserScannedResponseTO> resp = new Response<com.mobicage.to.friends.UserScannedResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToFriendsUserScannedResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.friends.UserScannedResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.location.beaconDiscovered".equals(function)) {
            final Response<com.mobicage.to.location.BeaconDiscoveredResponseTO> resp = new Response<com.mobicage.to.location.BeaconDiscoveredResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToLocationBeaconDiscoveredResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.location.BeaconDiscoveredResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.location.beaconInReach".equals(function)) {
            final Response<com.mobicage.to.location.BeaconInReachResponseTO> resp = new Response<com.mobicage.to.location.BeaconInReachResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToLocationBeaconInReachResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.location.BeaconInReachResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.location.beaconOutOfReach".equals(function)) {
            final Response<com.mobicage.to.location.BeaconOutOfReachResponseTO> resp = new Response<com.mobicage.to.location.BeaconOutOfReachResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToLocationBeaconOutOfReachResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.location.BeaconOutOfReachResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.location.getBeaconRegions".equals(function)) {
            final Response<com.mobicage.to.beacon.GetBeaconRegionsResponseTO> resp = new Response<com.mobicage.to.beacon.GetBeaconRegionsResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToBeaconGetBeaconRegionsResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.beacon.GetBeaconRegionsResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.location.getFriendLocation".equals(function)) {
            final Response<com.mobicage.to.location.GetFriendLocationResponseTO> resp = new Response<com.mobicage.to.location.GetFriendLocationResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToLocationGetFriendLocationResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.location.GetFriendLocationResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.location.getFriendLocations".equals(function)) {
            final Response<com.mobicage.to.location.GetFriendsLocationResponseTO> resp = new Response<com.mobicage.to.location.GetFriendsLocationResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToLocationGetFriendsLocationResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.location.GetFriendsLocationResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.messaging.ackMessage".equals(function)) {
            final Response<com.mobicage.to.messaging.AckMessageResponseTO> resp = new Response<com.mobicage.to.messaging.AckMessageResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToMessagingAckMessageResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.messaging.AckMessageResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.messaging.deleteConversation".equals(function)) {
            final Response<com.mobicage.to.messaging.DeleteConversationResponseTO> resp = new Response<com.mobicage.to.messaging.DeleteConversationResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToMessagingDeleteConversationResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.messaging.DeleteConversationResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.messaging.getConversation".equals(function)) {
            final Response<com.mobicage.to.messaging.GetConversationResponseTO> resp = new Response<com.mobicage.to.messaging.GetConversationResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToMessagingGetConversationResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.messaging.GetConversationResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.messaging.getConversationAvatar".equals(function)) {
            final Response<com.mobicage.to.messaging.GetConversationAvatarResponseTO> resp = new Response<com.mobicage.to.messaging.GetConversationAvatarResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToMessagingGetConversationAvatarResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.messaging.GetConversationAvatarResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.messaging.lockMessage".equals(function)) {
            final Response<com.mobicage.to.messaging.LockMessageResponseTO> resp = new Response<com.mobicage.to.messaging.LockMessageResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToMessagingLockMessageResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.messaging.LockMessageResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.messaging.markMessagesAsRead".equals(function)) {
            final Response<com.mobicage.to.messaging.MarkMessagesAsReadResponseTO> resp = new Response<com.mobicage.to.messaging.MarkMessagesAsReadResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToMessagingMarkMessagesAsReadResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.messaging.MarkMessagesAsReadResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.messaging.sendMessage".equals(function)) {
            final Response<com.mobicage.to.messaging.SendMessageResponseTO> resp = new Response<com.mobicage.to.messaging.SendMessageResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToMessagingSendMessageResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.messaging.SendMessageResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.messaging.submitAdvancedOrderForm".equals(function)) {
            final Response<com.mobicage.to.messaging.forms.SubmitAdvancedOrderFormResponseTO> resp = new Response<com.mobicage.to.messaging.forms.SubmitAdvancedOrderFormResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToMessagingFormsSubmitAdvancedOrderFormResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.messaging.forms.SubmitAdvancedOrderFormResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.messaging.submitAutoCompleteForm".equals(function)) {
            final Response<com.mobicage.to.messaging.forms.SubmitAutoCompleteFormResponseTO> resp = new Response<com.mobicage.to.messaging.forms.SubmitAutoCompleteFormResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToMessagingFormsSubmitAutoCompleteFormResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.messaging.forms.SubmitAutoCompleteFormResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.messaging.submitDateSelectForm".equals(function)) {
            final Response<com.mobicage.to.messaging.forms.SubmitDateSelectFormResponseTO> resp = new Response<com.mobicage.to.messaging.forms.SubmitDateSelectFormResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToMessagingFormsSubmitDateSelectFormResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.messaging.forms.SubmitDateSelectFormResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.messaging.submitFriendSelectForm".equals(function)) {
            final Response<com.mobicage.to.messaging.forms.SubmitFriendSelectFormResponseTO> resp = new Response<com.mobicage.to.messaging.forms.SubmitFriendSelectFormResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToMessagingFormsSubmitFriendSelectFormResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.messaging.forms.SubmitFriendSelectFormResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.messaging.submitGPSLocationForm".equals(function)) {
            final Response<com.mobicage.to.messaging.forms.SubmitGPSLocationFormResponseTO> resp = new Response<com.mobicage.to.messaging.forms.SubmitGPSLocationFormResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToMessagingFormsSubmitGPSLocationFormResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.messaging.forms.SubmitGPSLocationFormResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.messaging.submitMultiSelectForm".equals(function)) {
            final Response<com.mobicage.to.messaging.forms.SubmitMultiSelectFormResponseTO> resp = new Response<com.mobicage.to.messaging.forms.SubmitMultiSelectFormResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToMessagingFormsSubmitMultiSelectFormResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.messaging.forms.SubmitMultiSelectFormResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.messaging.submitMyDigiPassForm".equals(function)) {
            final Response<com.mobicage.to.messaging.forms.SubmitMyDigiPassFormResponseTO> resp = new Response<com.mobicage.to.messaging.forms.SubmitMyDigiPassFormResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToMessagingFormsSubmitMyDigiPassFormResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.messaging.forms.SubmitMyDigiPassFormResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.messaging.submitOauthForm".equals(function)) {
            final Response<com.mobicage.to.messaging.forms.SubmitOauthFormResponseTO> resp = new Response<com.mobicage.to.messaging.forms.SubmitOauthFormResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToMessagingFormsSubmitOauthFormResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.messaging.forms.SubmitOauthFormResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.messaging.submitPhotoUploadForm".equals(function)) {
            final Response<com.mobicage.to.messaging.forms.SubmitPhotoUploadFormResponseTO> resp = new Response<com.mobicage.to.messaging.forms.SubmitPhotoUploadFormResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToMessagingFormsSubmitPhotoUploadFormResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.messaging.forms.SubmitPhotoUploadFormResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.messaging.submitRangeSliderForm".equals(function)) {
            final Response<com.mobicage.to.messaging.forms.SubmitRangeSliderFormResponseTO> resp = new Response<com.mobicage.to.messaging.forms.SubmitRangeSliderFormResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToMessagingFormsSubmitRangeSliderFormResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.messaging.forms.SubmitRangeSliderFormResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.messaging.submitRatingForm".equals(function)) {
            final Response<com.mobicage.to.messaging.forms.SubmitRatingFormResponseTO> resp = new Response<com.mobicage.to.messaging.forms.SubmitRatingFormResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToMessagingFormsSubmitRatingFormResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.messaging.forms.SubmitRatingFormResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.messaging.submitSignForm".equals(function)) {
            final Response<com.mobicage.to.messaging.forms.SubmitSignFormResponseTO> resp = new Response<com.mobicage.to.messaging.forms.SubmitSignFormResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToMessagingFormsSubmitSignFormResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.messaging.forms.SubmitSignFormResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.messaging.submitSingleSelectForm".equals(function)) {
            final Response<com.mobicage.to.messaging.forms.SubmitSingleSelectFormResponseTO> resp = new Response<com.mobicage.to.messaging.forms.SubmitSingleSelectFormResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToMessagingFormsSubmitSingleSelectFormResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.messaging.forms.SubmitSingleSelectFormResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.messaging.submitSingleSliderForm".equals(function)) {
            final Response<com.mobicage.to.messaging.forms.SubmitSingleSliderFormResponseTO> resp = new Response<com.mobicage.to.messaging.forms.SubmitSingleSliderFormResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToMessagingFormsSubmitSingleSliderFormResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.messaging.forms.SubmitSingleSliderFormResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.messaging.submitTextBlockForm".equals(function)) {
            final Response<com.mobicage.to.messaging.forms.SubmitTextBlockFormResponseTO> resp = new Response<com.mobicage.to.messaging.forms.SubmitTextBlockFormResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToMessagingFormsSubmitTextBlockFormResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.messaging.forms.SubmitTextBlockFormResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.messaging.submitTextLineForm".equals(function)) {
            final Response<com.mobicage.to.messaging.forms.SubmitTextLineFormResponseTO> resp = new Response<com.mobicage.to.messaging.forms.SubmitTextLineFormResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToMessagingFormsSubmitTextLineFormResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.messaging.forms.SubmitTextLineFormResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.messaging.uploadChunk".equals(function)) {
            final Response<com.mobicage.to.messaging.UploadChunkResponseTO> resp = new Response<com.mobicage.to.messaging.UploadChunkResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToMessagingUploadChunkResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.messaging.UploadChunkResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.messaging.jsmfr.flowStarted".equals(function)) {
            final Response<com.mobicage.to.messaging.jsmfr.FlowStartedResponseTO> resp = new Response<com.mobicage.to.messaging.jsmfr.FlowStartedResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToMessagingJsmfrFlowStartedResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.messaging.jsmfr.FlowStartedResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.messaging.jsmfr.messageFlowError".equals(function)) {
            final Response<com.mobicage.to.messaging.jsmfr.MessageFlowErrorResponseTO> resp = new Response<com.mobicage.to.messaging.jsmfr.MessageFlowErrorResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToMessagingJsmfrMessageFlowErrorResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.messaging.jsmfr.MessageFlowErrorResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.messaging.jsmfr.messageFlowFinished".equals(function)) {
            final Response<com.mobicage.to.messaging.jsmfr.MessageFlowFinishedResponseTO> resp = new Response<com.mobicage.to.messaging.jsmfr.MessageFlowFinishedResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToMessagingJsmfrMessageFlowFinishedResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.messaging.jsmfr.MessageFlowFinishedResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.messaging.jsmfr.messageFlowMemberResult".equals(function)) {
            final Response<com.mobicage.to.messaging.jsmfr.MessageFlowMemberResultResponseTO> resp = new Response<com.mobicage.to.messaging.jsmfr.MessageFlowMemberResultResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToMessagingJsmfrMessageFlowMemberResultResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.messaging.jsmfr.MessageFlowMemberResultResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.messaging.jsmfr.newFlowMessage".equals(function)) {
            final Response<com.mobicage.to.messaging.jsmfr.NewFlowMessageResponseTO> resp = new Response<com.mobicage.to.messaging.jsmfr.NewFlowMessageResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToMessagingJsmfrNewFlowMessageResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.messaging.jsmfr.NewFlowMessageResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.news.getNews".equals(function)) {
            final Response<com.mobicage.to.news.GetNewsResponseTO> resp = new Response<com.mobicage.to.news.GetNewsResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToNewsGetNewsResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.news.GetNewsResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.news.getNewsItems".equals(function)) {
            final Response<com.mobicage.to.news.GetNewsItemsResponseTO> resp = new Response<com.mobicage.to.news.GetNewsItemsResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToNewsGetNewsItemsResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.news.GetNewsItemsResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.news.saveNewsStatistics".equals(function)) {
            final Response<com.mobicage.to.news.SaveNewsStatisticsResponseTO> resp = new Response<com.mobicage.to.news.SaveNewsStatisticsResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToNewsSaveNewsStatisticsResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.news.SaveNewsStatisticsResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.payment.cancelPayment".equals(function)) {
            final Response<com.mobicage.to.payment.CancelPaymentResponseTO> resp = new Response<com.mobicage.to.payment.CancelPaymentResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToPaymentCancelPaymentResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.payment.CancelPaymentResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.payment.confirmPayment".equals(function)) {
            final Response<com.mobicage.to.payment.ConfirmPaymentResponseTO> resp = new Response<com.mobicage.to.payment.ConfirmPaymentResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToPaymentConfirmPaymentResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.payment.ConfirmPaymentResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.payment.createAsset".equals(function)) {
            final Response<com.mobicage.to.payment.CreateAssetResponseTO> resp = new Response<com.mobicage.to.payment.CreateAssetResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToPaymentCreateAssetResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.payment.CreateAssetResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.payment.getPaymentAssets".equals(function)) {
            final Response<com.mobicage.to.payment.GetPaymentAssetsResponseTO> resp = new Response<com.mobicage.to.payment.GetPaymentAssetsResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToPaymentGetPaymentAssetsResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.payment.GetPaymentAssetsResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.payment.getPaymentProfile".equals(function)) {
            final Response<com.mobicage.to.payment.GetPaymentProfileResponseTO> resp = new Response<com.mobicage.to.payment.GetPaymentProfileResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToPaymentGetPaymentProfileResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.payment.GetPaymentProfileResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.payment.getPaymentProviders".equals(function)) {
            final Response<com.mobicage.to.payment.GetPaymentProvidersResponseTO> resp = new Response<com.mobicage.to.payment.GetPaymentProvidersResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToPaymentGetPaymentProvidersResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.payment.GetPaymentProvidersResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.payment.getPaymentTransactions".equals(function)) {
            final Response<com.mobicage.to.payment.GetPaymentTransactionsResponseTO> resp = new Response<com.mobicage.to.payment.GetPaymentTransactionsResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToPaymentGetPaymentTransactionsResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.payment.GetPaymentTransactionsResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.payment.getPendingPaymentDetails".equals(function)) {
            final Response<com.mobicage.to.payment.GetPendingPaymentDetailsResponseTO> resp = new Response<com.mobicage.to.payment.GetPendingPaymentDetailsResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToPaymentGetPendingPaymentDetailsResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.payment.GetPendingPaymentDetailsResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.payment.getPendingPaymentSignatureData".equals(function)) {
            final Response<com.mobicage.to.payment.GetPendingPaymentSignatureDataResponseTO> resp = new Response<com.mobicage.to.payment.GetPendingPaymentSignatureDataResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToPaymentGetPendingPaymentSignatureDataResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.payment.GetPendingPaymentSignatureDataResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.payment.receivePayment".equals(function)) {
            final Response<com.mobicage.to.payment.ReceivePaymentResponseTO> resp = new Response<com.mobicage.to.payment.ReceivePaymentResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToPaymentReceivePaymentResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.payment.ReceivePaymentResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.payment.verifyPaymentAsset".equals(function)) {
            final Response<com.mobicage.to.payment.VerifyPaymentAssetResponseTO> resp = new Response<com.mobicage.to.payment.VerifyPaymentAssetResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToPaymentVerifyPaymentAssetResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.payment.VerifyPaymentAssetResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.services.findService".equals(function)) {
            final Response<com.mobicage.to.service.FindServiceResponseTO> resp = new Response<com.mobicage.to.service.FindServiceResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToServiceFindServiceResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.service.FindServiceResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.services.getActionInfo".equals(function)) {
            final Response<com.mobicage.to.service.GetServiceActionInfoResponseTO> resp = new Response<com.mobicage.to.service.GetServiceActionInfoResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToServiceGetServiceActionInfoResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.service.GetServiceActionInfoResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.services.getMenuIcon".equals(function)) {
            final Response<com.mobicage.to.service.GetMenuIconResponseTO> resp = new Response<com.mobicage.to.service.GetMenuIconResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToServiceGetMenuIconResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.service.GetMenuIconResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.services.getStaticFlow".equals(function)) {
            final Response<com.mobicage.to.service.GetStaticFlowResponseTO> resp = new Response<com.mobicage.to.service.GetStaticFlowResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToServiceGetStaticFlowResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.service.GetStaticFlowResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.services.pokeService".equals(function)) {
            final Response<com.mobicage.to.service.PokeServiceResponseTO> resp = new Response<com.mobicage.to.service.PokeServiceResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToServicePokeServiceResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.service.PokeServiceResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.services.pressMenuItem".equals(function)) {
            final Response<com.mobicage.to.service.PressMenuIconResponseTO> resp = new Response<com.mobicage.to.service.PressMenuIconResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToServicePressMenuIconResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.service.PressMenuIconResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.services.sendApiCall".equals(function)) {
            final Response<com.mobicage.to.service.SendApiCallResponseTO> resp = new Response<com.mobicage.to.service.SendApiCallResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToServiceSendApiCallResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.service.SendApiCallResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.services.shareService".equals(function)) {
            final Response<com.mobicage.to.service.ShareServiceResponseTO> resp = new Response<com.mobicage.to.service.ShareServiceResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToServiceShareServiceResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.service.ShareServiceResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.services.startAction".equals(function)) {
            final Response<com.mobicage.to.service.StartServiceActionResponseTO> resp = new Response<com.mobicage.to.service.StartServiceActionResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToServiceStartServiceActionResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.service.StartServiceActionResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.services.updateUserData".equals(function)) {
            final Response<com.mobicage.to.service.UpdateUserDataResponseTO> resp = new Response<com.mobicage.to.service.UpdateUserDataResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToServiceUpdateUserDataResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.service.UpdateUserDataResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.system.editProfile".equals(function)) {
            final Response<com.mobicage.to.system.EditProfileResponseTO> resp = new Response<com.mobicage.to.system.EditProfileResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToSystemEditProfileResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.system.EditProfileResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.system.getAppAsset".equals(function)) {
            final Response<com.mobicage.to.app.GetAppAssetResponseTO> resp = new Response<com.mobicage.to.app.GetAppAssetResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToAppGetAppAssetResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.app.GetAppAssetResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.system.getIdentity".equals(function)) {
            final Response<com.mobicage.to.system.GetIdentityResponseTO> resp = new Response<com.mobicage.to.system.GetIdentityResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToSystemGetIdentityResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.system.GetIdentityResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.system.getIdentityQRCode".equals(function)) {
            final Response<com.mobicage.to.system.GetIdentityQRCodeResponseTO> resp = new Response<com.mobicage.to.system.GetIdentityQRCodeResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToSystemGetIdentityQRCodeResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.system.GetIdentityQRCodeResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.system.getJsEmbedding".equals(function)) {
            final Response<com.mobicage.to.js_embedding.GetJSEmbeddingResponseTO> resp = new Response<com.mobicage.to.js_embedding.GetJSEmbeddingResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToJs_embeddingGetJSEmbeddingResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.js_embedding.GetJSEmbeddingResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.system.heartBeat".equals(function)) {
            final Response<com.mobicage.to.system.HeartBeatResponseTO> resp = new Response<com.mobicage.to.system.HeartBeatResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToSystemHeartBeatResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.system.HeartBeatResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.system.logError".equals(function)) {
            final Response<com.mobicage.to.system.LogErrorResponseTO> resp = new Response<com.mobicage.to.system.LogErrorResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToSystemLogErrorResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.system.LogErrorResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.system.saveSettings".equals(function)) {
            final Response<com.mobicage.to.system.SaveSettingsResponse> resp = new Response<com.mobicage.to.system.SaveSettingsResponse>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToSystemSaveSettingsResponse(rpcr.result));
            ((IResponseHandler<com.mobicage.to.system.SaveSettingsResponse>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.system.setMobilePhoneNumber".equals(function)) {
            final Response<com.mobicage.to.system.SetMobilePhoneNumberResponseTO> resp = new Response<com.mobicage.to.system.SetMobilePhoneNumberResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToSystemSetMobilePhoneNumberResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.system.SetMobilePhoneNumberResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.system.setSecureInfo".equals(function)) {
            final Response<com.mobicage.to.system.SetSecureInfoResponseTO> resp = new Response<com.mobicage.to.system.SetSecureInfoResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToSystemSetSecureInfoResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.system.SetSecureInfoResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.system.unregisterMobile".equals(function)) {
            final Response<com.mobicage.to.system.UnregisterMobileResponseTO> resp = new Response<com.mobicage.to.system.UnregisterMobileResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToSystemUnregisterMobileResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.system.UnregisterMobileResponseTO>) responseHandler).handle(resp);
        }
        if ("com.mobicage.api.system.updateApplePushDeviceToken".equals(function)) {
            final Response<com.mobicage.to.system.UpdateApplePushDeviceTokenResponseTO> resp = new Response<com.mobicage.to.system.UpdateApplePushDeviceTokenResponseTO>();
            resp.setError(rpcr.error);
            resp.setSuccess(rpcr.success);
            resp.setCallId(rpcr.callId);
            if (rpcr.success)
                resp.setResult(Parser.ComMobicageToSystemUpdateApplePushDeviceTokenResponseTO(rpcr.result));
            ((IResponseHandler<com.mobicage.to.system.UpdateApplePushDeviceTokenResponseTO>) responseHandler).handle(resp);
        }
    }

}

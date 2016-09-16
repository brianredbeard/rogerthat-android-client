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

public class CallReceiver {

    public static volatile com.mobicage.capi.friends.IClientRpc comMobicageCapiFriendsIClientRpc = null;
    public static volatile com.mobicage.capi.location.IClientRpc comMobicageCapiLocationIClientRpc = null;
    public static volatile com.mobicage.capi.messaging.IClientRpc comMobicageCapiMessagingIClientRpc = null;
    public static volatile com.mobicage.capi.news.IClientRpc comMobicageCapiNewsIClientRpc = null;
    public static volatile com.mobicage.capi.services.IClientRpc comMobicageCapiServicesIClientRpc = null;
    public static volatile com.mobicage.capi.system.IClientRpc comMobicageCapiSystemIClientRpc = null;

    public static IJSONable processCall(final RpcCall call) throws Exception {
        String function = call.function;
        if ("com.mobicage.capi.friends.becameFriends".equals(function)) {
            return comMobicageCapiFriendsIClientRpc.becameFriends(Parser.ComMobicageToFriendsBecameFriendsRequestTO(call.arguments.get("request")));
        }
        if ("com.mobicage.capi.friends.updateFriend".equals(function)) {
            return comMobicageCapiFriendsIClientRpc.updateFriend(Parser.ComMobicageToFriendsUpdateFriendRequestTO(call.arguments.get("request")));
        }
        if ("com.mobicage.capi.friends.updateFriendSet".equals(function)) {
            return comMobicageCapiFriendsIClientRpc.updateFriendSet(Parser.ComMobicageToFriendsUpdateFriendSetRequestTO(call.arguments.get("request")));
        }
        if ("com.mobicage.capi.friends.updateGroups".equals(function)) {
            return comMobicageCapiFriendsIClientRpc.updateGroups(Parser.ComMobicageToFriendsUpdateGroupsRequestTO(call.arguments.get("request")));
        }
        if ("com.mobicage.capi.location.deleteBeaconDiscovery".equals(function)) {
            return comMobicageCapiLocationIClientRpc.deleteBeaconDiscovery(Parser.ComMobicageToLocationDeleteBeaconDiscoveryRequestTO(call.arguments.get("request")));
        }
        if ("com.mobicage.capi.location.getLocation".equals(function)) {
            return comMobicageCapiLocationIClientRpc.getLocation(Parser.ComMobicageToLocationGetLocationRequestTO(call.arguments.get("request")));
        }
        if ("com.mobicage.capi.location.locationResult".equals(function)) {
            return comMobicageCapiLocationIClientRpc.locationResult(Parser.ComMobicageToLocationLocationResultRequestTO(call.arguments.get("request")));
        }
        if ("com.mobicage.capi.location.trackLocation".equals(function)) {
            return comMobicageCapiLocationIClientRpc.trackLocation(Parser.ComMobicageToLocationTrackLocationRequestTO(call.arguments.get("request")));
        }
        if ("com.mobicage.capi.location.updateBeaconRegions".equals(function)) {
            return comMobicageCapiLocationIClientRpc.updateBeaconRegions(Parser.ComMobicageToBeaconUpdateBeaconRegionsRequestTO(call.arguments.get("request")));
        }
        if ("com.mobicage.capi.messaging.conversationDeleted".equals(function)) {
            return comMobicageCapiMessagingIClientRpc.conversationDeleted(Parser.ComMobicageToMessagingConversationDeletedRequestTO(call.arguments.get("request")));
        }
        if ("com.mobicage.capi.messaging.endMessageFlow".equals(function)) {
            return comMobicageCapiMessagingIClientRpc.endMessageFlow(Parser.ComMobicageToMessagingEndMessageFlowRequestTO(call.arguments.get("request")));
        }
        if ("com.mobicage.capi.messaging.messageLocked".equals(function)) {
            return comMobicageCapiMessagingIClientRpc.messageLocked(Parser.ComMobicageToMessagingMessageLockedRequestTO(call.arguments.get("request")));
        }
        if ("com.mobicage.capi.messaging.newAdvancedOrderForm".equals(function)) {
            return comMobicageCapiMessagingIClientRpc.newAdvancedOrderForm(Parser.ComMobicageToMessagingFormsNewAdvancedOrderFormRequestTO(call.arguments.get("request")));
        }
        if ("com.mobicage.capi.messaging.newAutoCompleteForm".equals(function)) {
            return comMobicageCapiMessagingIClientRpc.newAutoCompleteForm(Parser.ComMobicageToMessagingFormsNewAutoCompleteFormRequestTO(call.arguments.get("request")));
        }
        if ("com.mobicage.capi.messaging.newDateSelectForm".equals(function)) {
            return comMobicageCapiMessagingIClientRpc.newDateSelectForm(Parser.ComMobicageToMessagingFormsNewDateSelectFormRequestTO(call.arguments.get("request")));
        }
        if ("com.mobicage.capi.messaging.newFriendSelectForm".equals(function)) {
            return comMobicageCapiMessagingIClientRpc.newFriendSelectForm(Parser.ComMobicageToMessagingFormsNewFriendSelectFormRequestTO(call.arguments.get("request")));
        }
        if ("com.mobicage.capi.messaging.newGPSLocationForm".equals(function)) {
            return comMobicageCapiMessagingIClientRpc.newGPSLocationForm(Parser.ComMobicageToMessagingFormsNewGPSLocationFormRequestTO(call.arguments.get("request")));
        }
        if ("com.mobicage.capi.messaging.newMessage".equals(function)) {
            return comMobicageCapiMessagingIClientRpc.newMessage(Parser.ComMobicageToMessagingNewMessageRequestTO(call.arguments.get("request")));
        }
        if ("com.mobicage.capi.messaging.newMultiSelectForm".equals(function)) {
            return comMobicageCapiMessagingIClientRpc.newMultiSelectForm(Parser.ComMobicageToMessagingFormsNewMultiSelectFormRequestTO(call.arguments.get("request")));
        }
        if ("com.mobicage.capi.messaging.newMyDigiPassForm".equals(function)) {
            return comMobicageCapiMessagingIClientRpc.newMyDigiPassForm(Parser.ComMobicageToMessagingFormsNewMyDigiPassFormRequestTO(call.arguments.get("request")));
        }
        if ("com.mobicage.capi.messaging.newPhotoUploadForm".equals(function)) {
            return comMobicageCapiMessagingIClientRpc.newPhotoUploadForm(Parser.ComMobicageToMessagingFormsNewPhotoUploadFormRequestTO(call.arguments.get("request")));
        }
        if ("com.mobicage.capi.messaging.newRangeSliderForm".equals(function)) {
            return comMobicageCapiMessagingIClientRpc.newRangeSliderForm(Parser.ComMobicageToMessagingFormsNewRangeSliderFormRequestTO(call.arguments.get("request")));
        }
        if ("com.mobicage.capi.messaging.newSignForm".equals(function)) {
            return comMobicageCapiMessagingIClientRpc.newSignForm(Parser.ComMobicageToMessagingFormsNewSignFormRequestTO(call.arguments.get("request")));
        }
        if ("com.mobicage.capi.messaging.newSingleSelectForm".equals(function)) {
            return comMobicageCapiMessagingIClientRpc.newSingleSelectForm(Parser.ComMobicageToMessagingFormsNewSingleSelectFormRequestTO(call.arguments.get("request")));
        }
        if ("com.mobicage.capi.messaging.newSingleSliderForm".equals(function)) {
            return comMobicageCapiMessagingIClientRpc.newSingleSliderForm(Parser.ComMobicageToMessagingFormsNewSingleSliderFormRequestTO(call.arguments.get("request")));
        }
        if ("com.mobicage.capi.messaging.newTextBlockForm".equals(function)) {
            return comMobicageCapiMessagingIClientRpc.newTextBlockForm(Parser.ComMobicageToMessagingFormsNewTextBlockFormRequestTO(call.arguments.get("request")));
        }
        if ("com.mobicage.capi.messaging.newTextLineForm".equals(function)) {
            return comMobicageCapiMessagingIClientRpc.newTextLineForm(Parser.ComMobicageToMessagingFormsNewTextLineFormRequestTO(call.arguments.get("request")));
        }
        if ("com.mobicage.capi.messaging.startFlow".equals(function)) {
            return comMobicageCapiMessagingIClientRpc.startFlow(Parser.ComMobicageToMessagingStartFlowRequestTO(call.arguments.get("request")));
        }
        if ("com.mobicage.capi.messaging.transferCompleted".equals(function)) {
            return comMobicageCapiMessagingIClientRpc.transferCompleted(Parser.ComMobicageToMessagingTransferCompletedRequestTO(call.arguments.get("request")));
        }
        if ("com.mobicage.capi.messaging.updateAdvancedOrderForm".equals(function)) {
            return comMobicageCapiMessagingIClientRpc.updateAdvancedOrderForm(Parser.ComMobicageToMessagingFormsUpdateAdvancedOrderFormRequestTO(call.arguments.get("request")));
        }
        if ("com.mobicage.capi.messaging.updateAutoCompleteForm".equals(function)) {
            return comMobicageCapiMessagingIClientRpc.updateAutoCompleteForm(Parser.ComMobicageToMessagingFormsUpdateAutoCompleteFormRequestTO(call.arguments.get("request")));
        }
        if ("com.mobicage.capi.messaging.updateDateSelectForm".equals(function)) {
            return comMobicageCapiMessagingIClientRpc.updateDateSelectForm(Parser.ComMobicageToMessagingFormsUpdateDateSelectFormRequestTO(call.arguments.get("request")));
        }
        if ("com.mobicage.capi.messaging.updateFriendSelectForm".equals(function)) {
            return comMobicageCapiMessagingIClientRpc.updateFriendSelectForm(Parser.ComMobicageToMessagingFormsUpdateFriendSelectFormRequestTO(call.arguments.get("request")));
        }
        if ("com.mobicage.capi.messaging.updateGPSLocationForm".equals(function)) {
            return comMobicageCapiMessagingIClientRpc.updateGPSLocationForm(Parser.ComMobicageToMessagingFormsUpdateGPSLocationFormRequestTO(call.arguments.get("request")));
        }
        if ("com.mobicage.capi.messaging.updateMessage".equals(function)) {
            return comMobicageCapiMessagingIClientRpc.updateMessage(Parser.ComMobicageToMessagingUpdateMessageRequestTO(call.arguments.get("request")));
        }
        if ("com.mobicage.capi.messaging.updateMessageMemberStatus".equals(function)) {
            return comMobicageCapiMessagingIClientRpc.updateMessageMemberStatus(Parser.ComMobicageToMessagingMemberStatusUpdateRequestTO(call.arguments.get("request")));
        }
        if ("com.mobicage.capi.messaging.updateMultiSelectForm".equals(function)) {
            return comMobicageCapiMessagingIClientRpc.updateMultiSelectForm(Parser.ComMobicageToMessagingFormsUpdateMultiSelectFormRequestTO(call.arguments.get("request")));
        }
        if ("com.mobicage.capi.messaging.updateMyDigiPassForm".equals(function)) {
            return comMobicageCapiMessagingIClientRpc.updateMyDigiPassForm(Parser.ComMobicageToMessagingFormsUpdateMyDigiPassFormRequestTO(call.arguments.get("request")));
        }
        if ("com.mobicage.capi.messaging.updatePhotoUploadForm".equals(function)) {
            return comMobicageCapiMessagingIClientRpc.updatePhotoUploadForm(Parser.ComMobicageToMessagingFormsUpdatePhotoUploadFormRequestTO(call.arguments.get("request")));
        }
        if ("com.mobicage.capi.messaging.updateRangeSliderForm".equals(function)) {
            return comMobicageCapiMessagingIClientRpc.updateRangeSliderForm(Parser.ComMobicageToMessagingFormsUpdateRangeSliderFormRequestTO(call.arguments.get("request")));
        }
        if ("com.mobicage.capi.messaging.updateSignForm".equals(function)) {
            return comMobicageCapiMessagingIClientRpc.updateSignForm(Parser.ComMobicageToMessagingFormsUpdateSignFormRequestTO(call.arguments.get("request")));
        }
        if ("com.mobicage.capi.messaging.updateSingleSelectForm".equals(function)) {
            return comMobicageCapiMessagingIClientRpc.updateSingleSelectForm(Parser.ComMobicageToMessagingFormsUpdateSingleSelectFormRequestTO(call.arguments.get("request")));
        }
        if ("com.mobicage.capi.messaging.updateSingleSliderForm".equals(function)) {
            return comMobicageCapiMessagingIClientRpc.updateSingleSliderForm(Parser.ComMobicageToMessagingFormsUpdateSingleSliderFormRequestTO(call.arguments.get("request")));
        }
        if ("com.mobicage.capi.messaging.updateTextBlockForm".equals(function)) {
            return comMobicageCapiMessagingIClientRpc.updateTextBlockForm(Parser.ComMobicageToMessagingFormsUpdateTextBlockFormRequestTO(call.arguments.get("request")));
        }
        if ("com.mobicage.capi.messaging.updateTextLineForm".equals(function)) {
            return comMobicageCapiMessagingIClientRpc.updateTextLineForm(Parser.ComMobicageToMessagingFormsUpdateTextLineFormRequestTO(call.arguments.get("request")));
        }
        if ("com.mobicage.capi.news.disableNews".equals(function)) {
            return comMobicageCapiNewsIClientRpc.disableNews(Parser.ComMobicageToNewsDisableNewsRequestTO(call.arguments.get("request")));
        }
        if ("com.mobicage.capi.services.receiveApiCallResult".equals(function)) {
            return comMobicageCapiServicesIClientRpc.receiveApiCallResult(Parser.ComMobicageToServiceReceiveApiCallResultRequestTO(call.arguments.get("request")));
        }
        if ("com.mobicage.capi.services.updateUserData".equals(function)) {
            return comMobicageCapiServicesIClientRpc.updateUserData(Parser.ComMobicageToServiceUpdateUserDataRequestTO(call.arguments.get("request")));
        }
        if ("com.mobicage.capi.system.forwardLogs".equals(function)) {
            return comMobicageCapiSystemIClientRpc.forwardLogs(Parser.ComMobicageToSystemForwardLogsRequestTO(call.arguments.get("request")));
        }
        if ("com.mobicage.capi.system.identityUpdate".equals(function)) {
            return comMobicageCapiSystemIClientRpc.identityUpdate(Parser.ComMobicageToSystemIdentityUpdateRequestTO(call.arguments.get("request")));
        }
        if ("com.mobicage.capi.system.unregisterMobile".equals(function)) {
            return comMobicageCapiSystemIClientRpc.unregisterMobile(Parser.ComMobicageToSystemUnregisterMobileRequestTO(call.arguments.get("request")));
        }
        if ("com.mobicage.capi.system.updateAvailable".equals(function)) {
            return comMobicageCapiSystemIClientRpc.updateAvailable(Parser.ComMobicageToSystemUpdateAvailableRequestTO(call.arguments.get("request")));
        }
        if ("com.mobicage.capi.system.updateJsEmbedding".equals(function)) {
            return comMobicageCapiSystemIClientRpc.updateJsEmbedding(Parser.ComMobicageToJs_embeddingUpdateJSEmbeddingRequestTO(call.arguments.get("request")));
        }
        if ("com.mobicage.capi.system.updateSettings".equals(function)) {
            return comMobicageCapiSystemIClientRpc.updateSettings(Parser.ComMobicageToSystemUpdateSettingsRequestTO(call.arguments.get("request")));
        }
        return null;
    }

}

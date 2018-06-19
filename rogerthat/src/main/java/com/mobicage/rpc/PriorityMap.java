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

package com.mobicage.rpc;

import java.util.HashSet;
import java.util.Set;

public class PriorityMap {
    private final static Set<String> sPrioritySet = new HashSet<String>();

    static {
        sPrioritySet.add("com.mobicage.api.activity.logCall");
        sPrioritySet.add("com.mobicage.api.activity.logLocations");
        sPrioritySet.add("com.mobicage.api.friends.ackInvitationByInvitationSecret");
        sPrioritySet.add("com.mobicage.api.friends.breakFriendShip");
        sPrioritySet.add("com.mobicage.api.friends.deleteGroup");
        sPrioritySet.add("com.mobicage.api.friends.findFriend");
        sPrioritySet.add("com.mobicage.api.friends.findRogerthatUsersViaEmail");
        sPrioritySet.add("com.mobicage.api.friends.findRogerthatUsersViaFacebook");
        sPrioritySet.add("com.mobicage.api.friends.getAvatar");
        sPrioritySet.add("com.mobicage.api.friends.getCategory");
        sPrioritySet.add("com.mobicage.api.friends.getFriend");
        sPrioritySet.add("com.mobicage.api.friends.getFriendEmails");
        sPrioritySet.add("com.mobicage.api.friends.getFriendInvitationSecrets");
        sPrioritySet.add("com.mobicage.api.friends.getFriends");
        sPrioritySet.add("com.mobicage.api.friends.getGroupAvatar");
        sPrioritySet.add("com.mobicage.api.friends.getGroups");
        sPrioritySet.add("com.mobicage.api.friends.getUserInfo");
        sPrioritySet.add("com.mobicage.api.friends.invite");
        sPrioritySet.add("com.mobicage.api.friends.logInvitationSecretSent");
        sPrioritySet.add("com.mobicage.api.friends.putGroup");
        sPrioritySet.add("com.mobicage.api.friends.requestShareLocation");
        sPrioritySet.add("com.mobicage.api.friends.shareLocation");
        sPrioritySet.add("com.mobicage.api.friends.userScanned");
        sPrioritySet.add("com.mobicage.api.location.getFriendLocation");
        sPrioritySet.add("com.mobicage.api.location.getFriendLocations");
        sPrioritySet.add("com.mobicage.api.messaging.ackMessage");
        sPrioritySet.add("com.mobicage.api.messaging.deleteConversation");
        sPrioritySet.add("com.mobicage.api.messaging.getConversation");
        sPrioritySet.add("com.mobicage.api.messaging.getConversationAvatar");
        sPrioritySet.add("com.mobicage.api.messaging.lockMessage");
        sPrioritySet.add("com.mobicage.api.messaging.markMessagesAsRead");
        sPrioritySet.add("com.mobicage.api.messaging.sendMessage");
        sPrioritySet.add("com.mobicage.api.messaging.submitAdvancedOrderForm");
        sPrioritySet.add("com.mobicage.api.messaging.submitAutoCompleteForm");
        sPrioritySet.add("com.mobicage.api.messaging.submitDateSelectForm");
        sPrioritySet.add("com.mobicage.api.messaging.submitFriendSelectForm");
        sPrioritySet.add("com.mobicage.api.messaging.submitGPSLocationForm");
        sPrioritySet.add("com.mobicage.api.messaging.submitMultiSelectForm");
        sPrioritySet.add("com.mobicage.api.messaging.submitMyDigiPassForm");
        sPrioritySet.add("com.mobicage.api.messaging.submitOauthForm");
        sPrioritySet.add("com.mobicage.api.messaging.submitPayForm");
        sPrioritySet.add("com.mobicage.api.messaging.submitPhotoUploadForm");
        sPrioritySet.add("com.mobicage.api.messaging.submitRangeSliderForm");
        sPrioritySet.add("com.mobicage.api.messaging.submitSignForm");
        sPrioritySet.add("com.mobicage.api.messaging.submitSingleSelectForm");
        sPrioritySet.add("com.mobicage.api.messaging.submitSingleSliderForm");
        sPrioritySet.add("com.mobicage.api.messaging.submitTextBlockForm");
        sPrioritySet.add("com.mobicage.api.messaging.submitTextLineForm");
        sPrioritySet.add("com.mobicage.api.messaging.uploadChunk");
        sPrioritySet.add("com.mobicage.api.messaging.jsmfr.flowStarted");
        sPrioritySet.add("com.mobicage.api.messaging.jsmfr.messageFlowError");
        sPrioritySet.add("com.mobicage.api.messaging.jsmfr.messageFlowFinished");
        sPrioritySet.add("com.mobicage.api.messaging.jsmfr.messageFlowMemberResult");
        sPrioritySet.add("com.mobicage.api.messaging.jsmfr.newFlowMessage");
        sPrioritySet.add("com.mobicage.api.news.getNews");
        sPrioritySet.add("com.mobicage.api.news.getNewsItems");
        sPrioritySet.add("com.mobicage.api.news.saveNewsStatistics");
        sPrioritySet.add("com.mobicage.api.payment.cancelPayment");
        sPrioritySet.add("com.mobicage.api.payment.confirmPayment");
        sPrioritySet.add("com.mobicage.api.payment.createAsset");
        sPrioritySet.add("com.mobicage.api.payment.createTransaction");
        sPrioritySet.add("com.mobicage.api.payment.getPaymentAssets");
        sPrioritySet.add("com.mobicage.api.payment.getPaymentProfile");
        sPrioritySet.add("com.mobicage.api.payment.getPaymentProviders");
        sPrioritySet.add("com.mobicage.api.payment.getPaymentTransactions");
        sPrioritySet.add("com.mobicage.api.payment.getPendingPaymentDetails");
        sPrioritySet.add("com.mobicage.api.payment.getPendingPaymentSignatureData");
        sPrioritySet.add("com.mobicage.api.payment.getTargetInfo");
        sPrioritySet.add("com.mobicage.api.payment.receivePayment");
        sPrioritySet.add("com.mobicage.api.payment.verifyPaymentAsset");
        sPrioritySet.add("com.mobicage.api.services.findService");
        sPrioritySet.add("com.mobicage.api.services.getActionInfo");
        sPrioritySet.add("com.mobicage.api.services.getMenuIcon");
        sPrioritySet.add("com.mobicage.api.services.getStaticFlow");
        sPrioritySet.add("com.mobicage.api.services.pokeService");
        sPrioritySet.add("com.mobicage.api.services.pressMenuItem");
        sPrioritySet.add("com.mobicage.api.services.sendApiCall");
        sPrioritySet.add("com.mobicage.api.services.shareService");
        sPrioritySet.add("com.mobicage.api.services.startAction");
        sPrioritySet.add("com.mobicage.api.services.updateUserData");
        sPrioritySet.add("com.mobicage.api.system.editProfile");
        sPrioritySet.add("com.mobicage.api.system.getAppAsset");
        sPrioritySet.add("com.mobicage.api.system.getEmbeddedApp");
        sPrioritySet.add("com.mobicage.api.system.getEmbeddedApps");
        sPrioritySet.add("com.mobicage.api.system.getIdentity");
        sPrioritySet.add("com.mobicage.api.system.getIdentityQRCode");
        sPrioritySet.add("com.mobicage.api.system.getJsEmbedding");
        sPrioritySet.add("com.mobicage.api.system.heartBeat");
        sPrioritySet.add("com.mobicage.api.system.logError");
        sPrioritySet.add("com.mobicage.api.system.saveSettings");
        sPrioritySet.add("com.mobicage.api.system.setMobilePhoneNumber");
        sPrioritySet.add("com.mobicage.api.system.setSecureInfo");
        sPrioritySet.add("com.mobicage.api.system.unregisterMobile");
        sPrioritySet.add("com.mobicage.api.system.updateApplePushDeviceToken");
    }

    public static boolean hasPriority(String function) {
        return sPrioritySet.contains(function);
    }

}

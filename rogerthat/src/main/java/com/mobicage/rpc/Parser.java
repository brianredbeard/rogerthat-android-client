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

import java.util.Map;

public class Parser {

    public static Void parseAsVoid(Object result) {
        return null;
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.models.properties.forms.AdvancedOrderCategory ComMobicageModelsPropertiesFormsAdvancedOrderCategory(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.models.properties.forms.AdvancedOrderCategory((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.models.properties.forms.AdvancedOrderItem ComMobicageModelsPropertiesFormsAdvancedOrderItem(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.models.properties.forms.AdvancedOrderItem((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.models.properties.forms.FormResult ComMobicageModelsPropertiesFormsFormResult(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.models.properties.forms.FormResult((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.models.properties.forms.MyDigiPassAddress ComMobicageModelsPropertiesFormsMyDigiPassAddress(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.models.properties.forms.MyDigiPassAddress((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.models.properties.forms.MyDigiPassEidAddress ComMobicageModelsPropertiesFormsMyDigiPassEidAddress(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.models.properties.forms.MyDigiPassEidAddress((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.models.properties.forms.MyDigiPassEidProfile ComMobicageModelsPropertiesFormsMyDigiPassEidProfile(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.models.properties.forms.MyDigiPassEidProfile((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.models.properties.forms.MyDigiPassProfile ComMobicageModelsPropertiesFormsMyDigiPassProfile(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.models.properties.forms.MyDigiPassProfile((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.models.properties.forms.WidgetResult ComMobicageModelsPropertiesFormsWidgetResult(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.models.properties.forms.WidgetResult((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.activity.CallRecordTO ComMobicageToActivityCallRecordTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.activity.CallRecordTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.activity.CellTowerTO ComMobicageToActivityCellTowerTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.activity.CellTowerTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.activity.GeoPointTO ComMobicageToActivityGeoPointTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.activity.GeoPointTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.activity.GeoPointWithTimestampTO ComMobicageToActivityGeoPointWithTimestampTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.activity.GeoPointWithTimestampTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.activity.LocationRecordTO ComMobicageToActivityLocationRecordTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.activity.LocationRecordTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.activity.LogCallRequestTO ComMobicageToActivityLogCallRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.activity.LogCallRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.activity.LogCallResponseTO ComMobicageToActivityLogCallResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.activity.LogCallResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.activity.LogLocationRecipientTO ComMobicageToActivityLogLocationRecipientTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.activity.LogLocationRecipientTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.activity.LogLocationsRequestTO ComMobicageToActivityLogLocationsRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.activity.LogLocationsRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.activity.LogLocationsResponseTO ComMobicageToActivityLogLocationsResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.activity.LogLocationsResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.activity.RawLocationInfoTO ComMobicageToActivityRawLocationInfoTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.activity.RawLocationInfoTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.beacon.BeaconRegionTO ComMobicageToBeaconBeaconRegionTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.beacon.BeaconRegionTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.beacon.GetBeaconRegionsRequestTO ComMobicageToBeaconGetBeaconRegionsRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.beacon.GetBeaconRegionsRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.beacon.GetBeaconRegionsResponseTO ComMobicageToBeaconGetBeaconRegionsResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.beacon.GetBeaconRegionsResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.beacon.UpdateBeaconRegionsRequestTO ComMobicageToBeaconUpdateBeaconRegionsRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.beacon.UpdateBeaconRegionsRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.beacon.UpdateBeaconRegionsResponseTO ComMobicageToBeaconUpdateBeaconRegionsResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.beacon.UpdateBeaconRegionsResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.friends.AckInvitationByInvitationSecretRequestTO ComMobicageToFriendsAckInvitationByInvitationSecretRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.friends.AckInvitationByInvitationSecretRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.friends.AckInvitationByInvitationSecretResponseTO ComMobicageToFriendsAckInvitationByInvitationSecretResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.friends.AckInvitationByInvitationSecretResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.friends.BecameFriendsRequestTO ComMobicageToFriendsBecameFriendsRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.friends.BecameFriendsRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.friends.BecameFriendsResponseTO ComMobicageToFriendsBecameFriendsResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.friends.BecameFriendsResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.friends.BreakFriendshipRequestTO ComMobicageToFriendsBreakFriendshipRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.friends.BreakFriendshipRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.friends.BreakFriendshipResponseTO ComMobicageToFriendsBreakFriendshipResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.friends.BreakFriendshipResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.friends.DeleteGroupRequestTO ComMobicageToFriendsDeleteGroupRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.friends.DeleteGroupRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.friends.DeleteGroupResponseTO ComMobicageToFriendsDeleteGroupResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.friends.DeleteGroupResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.friends.ErrorTO ComMobicageToFriendsErrorTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.friends.ErrorTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.friends.FacebookRogerthatProfileMatchTO ComMobicageToFriendsFacebookRogerthatProfileMatchTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.friends.FacebookRogerthatProfileMatchTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.friends.FindFriendItemTO ComMobicageToFriendsFindFriendItemTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.friends.FindFriendItemTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.friends.FindFriendRequestTO ComMobicageToFriendsFindFriendRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.friends.FindFriendRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.friends.FindFriendResponseTO ComMobicageToFriendsFindFriendResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.friends.FindFriendResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.friends.FindRogerthatUsersViaEmailRequestTO ComMobicageToFriendsFindRogerthatUsersViaEmailRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.friends.FindRogerthatUsersViaEmailRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.friends.FindRogerthatUsersViaEmailResponseTO ComMobicageToFriendsFindRogerthatUsersViaEmailResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.friends.FindRogerthatUsersViaEmailResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.friends.FindRogerthatUsersViaFacebookRequestTO ComMobicageToFriendsFindRogerthatUsersViaFacebookRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.friends.FindRogerthatUsersViaFacebookRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.friends.FindRogerthatUsersViaFacebookResponseTO ComMobicageToFriendsFindRogerthatUsersViaFacebookResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.friends.FindRogerthatUsersViaFacebookResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.friends.FriendCategoryTO ComMobicageToFriendsFriendCategoryTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.friends.FriendCategoryTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.friends.FriendRelationTO ComMobicageToFriendsFriendRelationTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.friends.FriendRelationTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.friends.FriendTO ComMobicageToFriendsFriendTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.friends.FriendTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.friends.GetAvatarRequestTO ComMobicageToFriendsGetAvatarRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.friends.GetAvatarRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.friends.GetAvatarResponseTO ComMobicageToFriendsGetAvatarResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.friends.GetAvatarResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.friends.GetCategoryRequestTO ComMobicageToFriendsGetCategoryRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.friends.GetCategoryRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.friends.GetCategoryResponseTO ComMobicageToFriendsGetCategoryResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.friends.GetCategoryResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.friends.GetFriendEmailsRequestTO ComMobicageToFriendsGetFriendEmailsRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.friends.GetFriendEmailsRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.friends.GetFriendEmailsResponseTO ComMobicageToFriendsGetFriendEmailsResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.friends.GetFriendEmailsResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.friends.GetFriendInvitationSecretsRequestTO ComMobicageToFriendsGetFriendInvitationSecretsRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.friends.GetFriendInvitationSecretsRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.friends.GetFriendInvitationSecretsResponseTO ComMobicageToFriendsGetFriendInvitationSecretsResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.friends.GetFriendInvitationSecretsResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.friends.GetFriendRequestTO ComMobicageToFriendsGetFriendRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.friends.GetFriendRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.friends.GetFriendResponseTO ComMobicageToFriendsGetFriendResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.friends.GetFriendResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.friends.GetFriendsListRequestTO ComMobicageToFriendsGetFriendsListRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.friends.GetFriendsListRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.friends.GetFriendsListResponseTO ComMobicageToFriendsGetFriendsListResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.friends.GetFriendsListResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.friends.GetGroupAvatarRequestTO ComMobicageToFriendsGetGroupAvatarRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.friends.GetGroupAvatarRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.friends.GetGroupAvatarResponseTO ComMobicageToFriendsGetGroupAvatarResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.friends.GetGroupAvatarResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.friends.GetGroupsRequestTO ComMobicageToFriendsGetGroupsRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.friends.GetGroupsRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.friends.GetGroupsResponseTO ComMobicageToFriendsGetGroupsResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.friends.GetGroupsResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.friends.GetUserInfoRequestTO ComMobicageToFriendsGetUserInfoRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.friends.GetUserInfoRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.friends.GetUserInfoResponseTO ComMobicageToFriendsGetUserInfoResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.friends.GetUserInfoResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.friends.GroupTO ComMobicageToFriendsGroupTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.friends.GroupTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.friends.InviteFriendRequestTO ComMobicageToFriendsInviteFriendRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.friends.InviteFriendRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.friends.InviteFriendResponseTO ComMobicageToFriendsInviteFriendResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.friends.InviteFriendResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.friends.LogInvitationSecretSentRequestTO ComMobicageToFriendsLogInvitationSecretSentRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.friends.LogInvitationSecretSentRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.friends.LogInvitationSecretSentResponseTO ComMobicageToFriendsLogInvitationSecretSentResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.friends.LogInvitationSecretSentResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.friends.PutGroupRequestTO ComMobicageToFriendsPutGroupRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.friends.PutGroupRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.friends.PutGroupResponseTO ComMobicageToFriendsPutGroupResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.friends.PutGroupResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.friends.RequestShareLocationRequestTO ComMobicageToFriendsRequestShareLocationRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.friends.RequestShareLocationRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.friends.RequestShareLocationResponseTO ComMobicageToFriendsRequestShareLocationResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.friends.RequestShareLocationResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.friends.ServiceMenuItemTO ComMobicageToFriendsServiceMenuItemTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.friends.ServiceMenuItemTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.friends.ServiceMenuTO ComMobicageToFriendsServiceMenuTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.friends.ServiceMenuTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.friends.ShareLocationRequestTO ComMobicageToFriendsShareLocationRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.friends.ShareLocationRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.friends.ShareLocationResponseTO ComMobicageToFriendsShareLocationResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.friends.ShareLocationResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.friends.UpdateFriendRequestTO ComMobicageToFriendsUpdateFriendRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.friends.UpdateFriendRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.friends.UpdateFriendResponseTO ComMobicageToFriendsUpdateFriendResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.friends.UpdateFriendResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.friends.UpdateFriendSetRequestTO ComMobicageToFriendsUpdateFriendSetRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.friends.UpdateFriendSetRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.friends.UpdateFriendSetResponseTO ComMobicageToFriendsUpdateFriendSetResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.friends.UpdateFriendSetResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.friends.UpdateGroupsRequestTO ComMobicageToFriendsUpdateGroupsRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.friends.UpdateGroupsRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.friends.UpdateGroupsResponseTO ComMobicageToFriendsUpdateGroupsResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.friends.UpdateGroupsResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.friends.UserScannedRequestTO ComMobicageToFriendsUserScannedRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.friends.UserScannedRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.friends.UserScannedResponseTO ComMobicageToFriendsUserScannedResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.friends.UserScannedResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.js_embedding.GetJSEmbeddingRequestTO ComMobicageToJs_embeddingGetJSEmbeddingRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.js_embedding.GetJSEmbeddingRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.js_embedding.GetJSEmbeddingResponseTO ComMobicageToJs_embeddingGetJSEmbeddingResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.js_embedding.GetJSEmbeddingResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.js_embedding.JSEmbeddingItemTO ComMobicageToJs_embeddingJSEmbeddingItemTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.js_embedding.JSEmbeddingItemTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.js_embedding.UpdateJSEmbeddingRequestTO ComMobicageToJs_embeddingUpdateJSEmbeddingRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.js_embedding.UpdateJSEmbeddingRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.js_embedding.UpdateJSEmbeddingResponseTO ComMobicageToJs_embeddingUpdateJSEmbeddingResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.js_embedding.UpdateJSEmbeddingResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.location.BeaconDiscoveredRequestTO ComMobicageToLocationBeaconDiscoveredRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.location.BeaconDiscoveredRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.location.BeaconDiscoveredResponseTO ComMobicageToLocationBeaconDiscoveredResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.location.BeaconDiscoveredResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.location.BeaconInReachRequestTO ComMobicageToLocationBeaconInReachRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.location.BeaconInReachRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.location.BeaconInReachResponseTO ComMobicageToLocationBeaconInReachResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.location.BeaconInReachResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.location.BeaconOutOfReachRequestTO ComMobicageToLocationBeaconOutOfReachRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.location.BeaconOutOfReachRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.location.BeaconOutOfReachResponseTO ComMobicageToLocationBeaconOutOfReachResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.location.BeaconOutOfReachResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.location.DeleteBeaconDiscoveryRequestTO ComMobicageToLocationDeleteBeaconDiscoveryRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.location.DeleteBeaconDiscoveryRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.location.DeleteBeaconDiscoveryResponseTO ComMobicageToLocationDeleteBeaconDiscoveryResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.location.DeleteBeaconDiscoveryResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.location.FriendLocationTO ComMobicageToLocationFriendLocationTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.location.FriendLocationTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.location.GetFriendLocationRequestTO ComMobicageToLocationGetFriendLocationRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.location.GetFriendLocationRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.location.GetFriendLocationResponseTO ComMobicageToLocationGetFriendLocationResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.location.GetFriendLocationResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.location.GetFriendsLocationRequestTO ComMobicageToLocationGetFriendsLocationRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.location.GetFriendsLocationRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.location.GetFriendsLocationResponseTO ComMobicageToLocationGetFriendsLocationResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.location.GetFriendsLocationResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.location.GetLocationErrorTO ComMobicageToLocationGetLocationErrorTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.location.GetLocationErrorTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.location.GetLocationRequestTO ComMobicageToLocationGetLocationRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.location.GetLocationRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.location.GetLocationResponseTO ComMobicageToLocationGetLocationResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.location.GetLocationResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.location.LocationResultRequestTO ComMobicageToLocationLocationResultRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.location.LocationResultRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.location.LocationResultResponseTO ComMobicageToLocationLocationResultResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.location.LocationResultResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.location.TrackLocationRequestTO ComMobicageToLocationTrackLocationRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.location.TrackLocationRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.location.TrackLocationResponseTO ComMobicageToLocationTrackLocationResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.location.TrackLocationResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.AckMessageRequestTO ComMobicageToMessagingAckMessageRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.AckMessageRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.AckMessageResponseTO ComMobicageToMessagingAckMessageResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.AckMessageResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.AttachmentTO ComMobicageToMessagingAttachmentTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.AttachmentTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.ButtonTO ComMobicageToMessagingButtonTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.ButtonTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.ConversationDeletedRequestTO ComMobicageToMessagingConversationDeletedRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.ConversationDeletedRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.ConversationDeletedResponseTO ComMobicageToMessagingConversationDeletedResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.ConversationDeletedResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.DeleteConversationRequestTO ComMobicageToMessagingDeleteConversationRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.DeleteConversationRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.DeleteConversationResponseTO ComMobicageToMessagingDeleteConversationResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.DeleteConversationResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.EndMessageFlowRequestTO ComMobicageToMessagingEndMessageFlowRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.EndMessageFlowRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.EndMessageFlowResponseTO ComMobicageToMessagingEndMessageFlowResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.EndMessageFlowResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.GetConversationAvatarRequestTO ComMobicageToMessagingGetConversationAvatarRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.GetConversationAvatarRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.GetConversationAvatarResponseTO ComMobicageToMessagingGetConversationAvatarResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.GetConversationAvatarResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.GetConversationRequestTO ComMobicageToMessagingGetConversationRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.GetConversationRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.GetConversationResponseTO ComMobicageToMessagingGetConversationResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.GetConversationResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.LockMessageRequestTO ComMobicageToMessagingLockMessageRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.LockMessageRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.LockMessageResponseTO ComMobicageToMessagingLockMessageResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.LockMessageResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.MarkMessagesAsReadRequestTO ComMobicageToMessagingMarkMessagesAsReadRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.MarkMessagesAsReadRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.MarkMessagesAsReadResponseTO ComMobicageToMessagingMarkMessagesAsReadResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.MarkMessagesAsReadResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.MemberStatusTO ComMobicageToMessagingMemberStatusTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.MemberStatusTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.MemberStatusUpdateRequestTO ComMobicageToMessagingMemberStatusUpdateRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.MemberStatusUpdateRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.MemberStatusUpdateResponseTO ComMobicageToMessagingMemberStatusUpdateResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.MemberStatusUpdateResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.MessageLockedRequestTO ComMobicageToMessagingMessageLockedRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.MessageLockedRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.MessageLockedResponseTO ComMobicageToMessagingMessageLockedResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.MessageLockedResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.MessageTO ComMobicageToMessagingMessageTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.MessageTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.NewMessageRequestTO ComMobicageToMessagingNewMessageRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.NewMessageRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.NewMessageResponseTO ComMobicageToMessagingNewMessageResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.NewMessageResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.SendMessageRequestTO ComMobicageToMessagingSendMessageRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.SendMessageRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.SendMessageResponseTO ComMobicageToMessagingSendMessageResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.SendMessageResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.StartFlowRequestTO ComMobicageToMessagingStartFlowRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.StartFlowRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.StartFlowResponseTO ComMobicageToMessagingStartFlowResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.StartFlowResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.TransferCompletedRequestTO ComMobicageToMessagingTransferCompletedRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.TransferCompletedRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.TransferCompletedResponseTO ComMobicageToMessagingTransferCompletedResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.TransferCompletedResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.UpdateMessageRequestTO ComMobicageToMessagingUpdateMessageRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.UpdateMessageRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.UpdateMessageResponseTO ComMobicageToMessagingUpdateMessageResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.UpdateMessageResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.UploadChunkRequestTO ComMobicageToMessagingUploadChunkRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.UploadChunkRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.UploadChunkResponseTO ComMobicageToMessagingUploadChunkResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.UploadChunkResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.AdvancedOrderFormMessageTO ComMobicageToMessagingFormsAdvancedOrderFormMessageTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.AdvancedOrderFormMessageTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.AdvancedOrderFormTO ComMobicageToMessagingFormsAdvancedOrderFormTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.AdvancedOrderFormTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.AdvancedOrderTO ComMobicageToMessagingFormsAdvancedOrderTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.AdvancedOrderTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.AdvancedOrderWidgetResultTO ComMobicageToMessagingFormsAdvancedOrderWidgetResultTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.AdvancedOrderWidgetResultTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.AutoCompleteFormMessageTO ComMobicageToMessagingFormsAutoCompleteFormMessageTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.AutoCompleteFormMessageTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.AutoCompleteFormTO ComMobicageToMessagingFormsAutoCompleteFormTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.AutoCompleteFormTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.AutoCompleteTO ComMobicageToMessagingFormsAutoCompleteTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.AutoCompleteTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.ChoiceTO ComMobicageToMessagingFormsChoiceTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.ChoiceTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.DateSelectFormMessageTO ComMobicageToMessagingFormsDateSelectFormMessageTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.DateSelectFormMessageTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.DateSelectFormTO ComMobicageToMessagingFormsDateSelectFormTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.DateSelectFormTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.DateSelectTO ComMobicageToMessagingFormsDateSelectTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.DateSelectTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.FloatListWidgetResultTO ComMobicageToMessagingFormsFloatListWidgetResultTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.FloatListWidgetResultTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.FloatWidgetResultTO ComMobicageToMessagingFormsFloatWidgetResultTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.FloatWidgetResultTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.FriendSelectFormMessageTO ComMobicageToMessagingFormsFriendSelectFormMessageTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.FriendSelectFormMessageTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.FriendSelectFormTO ComMobicageToMessagingFormsFriendSelectFormTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.FriendSelectFormTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.FriendSelectTO ComMobicageToMessagingFormsFriendSelectTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.FriendSelectTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.GPSLocationFormMessageTO ComMobicageToMessagingFormsGPSLocationFormMessageTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.GPSLocationFormMessageTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.GPSLocationFormTO ComMobicageToMessagingFormsGPSLocationFormTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.GPSLocationFormTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.GPSLocationTO ComMobicageToMessagingFormsGPSLocationTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.GPSLocationTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.LocationWidgetResultTO ComMobicageToMessagingFormsLocationWidgetResultTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.LocationWidgetResultTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.LongWidgetResultTO ComMobicageToMessagingFormsLongWidgetResultTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.LongWidgetResultTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.MultiSelectFormMessageTO ComMobicageToMessagingFormsMultiSelectFormMessageTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.MultiSelectFormMessageTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.MultiSelectFormTO ComMobicageToMessagingFormsMultiSelectFormTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.MultiSelectFormTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.MultiSelectTO ComMobicageToMessagingFormsMultiSelectTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.MultiSelectTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.MyDigiPassFormMessageTO ComMobicageToMessagingFormsMyDigiPassFormMessageTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.MyDigiPassFormMessageTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.MyDigiPassFormTO ComMobicageToMessagingFormsMyDigiPassFormTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.MyDigiPassFormTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.MyDigiPassTO ComMobicageToMessagingFormsMyDigiPassTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.MyDigiPassTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.MyDigiPassWidgetResultTO ComMobicageToMessagingFormsMyDigiPassWidgetResultTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.MyDigiPassWidgetResultTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.NewAdvancedOrderFormRequestTO ComMobicageToMessagingFormsNewAdvancedOrderFormRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.NewAdvancedOrderFormRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.NewAdvancedOrderFormResponseTO ComMobicageToMessagingFormsNewAdvancedOrderFormResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.NewAdvancedOrderFormResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.NewAutoCompleteFormRequestTO ComMobicageToMessagingFormsNewAutoCompleteFormRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.NewAutoCompleteFormRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.NewAutoCompleteFormResponseTO ComMobicageToMessagingFormsNewAutoCompleteFormResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.NewAutoCompleteFormResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.NewDateSelectFormRequestTO ComMobicageToMessagingFormsNewDateSelectFormRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.NewDateSelectFormRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.NewDateSelectFormResponseTO ComMobicageToMessagingFormsNewDateSelectFormResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.NewDateSelectFormResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.NewFriendSelectFormRequestTO ComMobicageToMessagingFormsNewFriendSelectFormRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.NewFriendSelectFormRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.NewFriendSelectFormResponseTO ComMobicageToMessagingFormsNewFriendSelectFormResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.NewFriendSelectFormResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.NewGPSLocationFormRequestTO ComMobicageToMessagingFormsNewGPSLocationFormRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.NewGPSLocationFormRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.NewGPSLocationFormResponseTO ComMobicageToMessagingFormsNewGPSLocationFormResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.NewGPSLocationFormResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.NewMultiSelectFormRequestTO ComMobicageToMessagingFormsNewMultiSelectFormRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.NewMultiSelectFormRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.NewMultiSelectFormResponseTO ComMobicageToMessagingFormsNewMultiSelectFormResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.NewMultiSelectFormResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.NewMyDigiPassFormRequestTO ComMobicageToMessagingFormsNewMyDigiPassFormRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.NewMyDigiPassFormRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.NewMyDigiPassFormResponseTO ComMobicageToMessagingFormsNewMyDigiPassFormResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.NewMyDigiPassFormResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.NewPhotoUploadFormRequestTO ComMobicageToMessagingFormsNewPhotoUploadFormRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.NewPhotoUploadFormRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.NewPhotoUploadFormResponseTO ComMobicageToMessagingFormsNewPhotoUploadFormResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.NewPhotoUploadFormResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.NewRangeSliderFormRequestTO ComMobicageToMessagingFormsNewRangeSliderFormRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.NewRangeSliderFormRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.NewRangeSliderFormResponseTO ComMobicageToMessagingFormsNewRangeSliderFormResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.NewRangeSliderFormResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.NewSignFormRequestTO ComMobicageToMessagingFormsNewSignFormRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.NewSignFormRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.NewSignFormResponseTO ComMobicageToMessagingFormsNewSignFormResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.NewSignFormResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.NewSingleSelectFormRequestTO ComMobicageToMessagingFormsNewSingleSelectFormRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.NewSingleSelectFormRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.NewSingleSelectFormResponseTO ComMobicageToMessagingFormsNewSingleSelectFormResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.NewSingleSelectFormResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.NewSingleSliderFormRequestTO ComMobicageToMessagingFormsNewSingleSliderFormRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.NewSingleSliderFormRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.NewSingleSliderFormResponseTO ComMobicageToMessagingFormsNewSingleSliderFormResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.NewSingleSliderFormResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.NewTextBlockFormRequestTO ComMobicageToMessagingFormsNewTextBlockFormRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.NewTextBlockFormRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.NewTextBlockFormResponseTO ComMobicageToMessagingFormsNewTextBlockFormResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.NewTextBlockFormResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.NewTextLineFormRequestTO ComMobicageToMessagingFormsNewTextLineFormRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.NewTextLineFormRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.NewTextLineFormResponseTO ComMobicageToMessagingFormsNewTextLineFormResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.NewTextLineFormResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.PhotoUploadFormMessageTO ComMobicageToMessagingFormsPhotoUploadFormMessageTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.PhotoUploadFormMessageTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.PhotoUploadFormTO ComMobicageToMessagingFormsPhotoUploadFormTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.PhotoUploadFormTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.PhotoUploadTO ComMobicageToMessagingFormsPhotoUploadTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.PhotoUploadTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.RangeSliderFormMessageTO ComMobicageToMessagingFormsRangeSliderFormMessageTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.RangeSliderFormMessageTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.RangeSliderFormTO ComMobicageToMessagingFormsRangeSliderFormTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.RangeSliderFormTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.RangeSliderTO ComMobicageToMessagingFormsRangeSliderTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.RangeSliderTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.SignFormMessageTO ComMobicageToMessagingFormsSignFormMessageTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.SignFormMessageTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.SignFormTO ComMobicageToMessagingFormsSignFormTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.SignFormTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.SignTO ComMobicageToMessagingFormsSignTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.SignTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.SingleSelectFormMessageTO ComMobicageToMessagingFormsSingleSelectFormMessageTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.SingleSelectFormMessageTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.SingleSelectFormTO ComMobicageToMessagingFormsSingleSelectFormTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.SingleSelectFormTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.SingleSelectTO ComMobicageToMessagingFormsSingleSelectTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.SingleSelectTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.SingleSliderFormMessageTO ComMobicageToMessagingFormsSingleSliderFormMessageTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.SingleSliderFormMessageTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.SingleSliderFormTO ComMobicageToMessagingFormsSingleSliderFormTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.SingleSliderFormTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.SingleSliderTO ComMobicageToMessagingFormsSingleSliderTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.SingleSliderTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.SubmitAdvancedOrderFormRequestTO ComMobicageToMessagingFormsSubmitAdvancedOrderFormRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.SubmitAdvancedOrderFormRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.SubmitAdvancedOrderFormResponseTO ComMobicageToMessagingFormsSubmitAdvancedOrderFormResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.SubmitAdvancedOrderFormResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.SubmitAutoCompleteFormRequestTO ComMobicageToMessagingFormsSubmitAutoCompleteFormRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.SubmitAutoCompleteFormRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.SubmitAutoCompleteFormResponseTO ComMobicageToMessagingFormsSubmitAutoCompleteFormResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.SubmitAutoCompleteFormResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.SubmitDateSelectFormRequestTO ComMobicageToMessagingFormsSubmitDateSelectFormRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.SubmitDateSelectFormRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.SubmitDateSelectFormResponseTO ComMobicageToMessagingFormsSubmitDateSelectFormResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.SubmitDateSelectFormResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.SubmitFriendSelectFormRequestTO ComMobicageToMessagingFormsSubmitFriendSelectFormRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.SubmitFriendSelectFormRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.SubmitFriendSelectFormResponseTO ComMobicageToMessagingFormsSubmitFriendSelectFormResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.SubmitFriendSelectFormResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.SubmitGPSLocationFormRequestTO ComMobicageToMessagingFormsSubmitGPSLocationFormRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.SubmitGPSLocationFormRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.SubmitGPSLocationFormResponseTO ComMobicageToMessagingFormsSubmitGPSLocationFormResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.SubmitGPSLocationFormResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.SubmitMultiSelectFormRequestTO ComMobicageToMessagingFormsSubmitMultiSelectFormRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.SubmitMultiSelectFormRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.SubmitMultiSelectFormResponseTO ComMobicageToMessagingFormsSubmitMultiSelectFormResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.SubmitMultiSelectFormResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.SubmitMyDigiPassFormRequestTO ComMobicageToMessagingFormsSubmitMyDigiPassFormRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.SubmitMyDigiPassFormRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.SubmitMyDigiPassFormResponseTO ComMobicageToMessagingFormsSubmitMyDigiPassFormResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.SubmitMyDigiPassFormResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.SubmitPhotoUploadFormRequestTO ComMobicageToMessagingFormsSubmitPhotoUploadFormRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.SubmitPhotoUploadFormRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.SubmitPhotoUploadFormResponseTO ComMobicageToMessagingFormsSubmitPhotoUploadFormResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.SubmitPhotoUploadFormResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.SubmitRangeSliderFormRequestTO ComMobicageToMessagingFormsSubmitRangeSliderFormRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.SubmitRangeSliderFormRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.SubmitRangeSliderFormResponseTO ComMobicageToMessagingFormsSubmitRangeSliderFormResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.SubmitRangeSliderFormResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.SubmitSignFormRequestTO ComMobicageToMessagingFormsSubmitSignFormRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.SubmitSignFormRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.SubmitSignFormResponseTO ComMobicageToMessagingFormsSubmitSignFormResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.SubmitSignFormResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.SubmitSingleSelectFormRequestTO ComMobicageToMessagingFormsSubmitSingleSelectFormRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.SubmitSingleSelectFormRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.SubmitSingleSelectFormResponseTO ComMobicageToMessagingFormsSubmitSingleSelectFormResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.SubmitSingleSelectFormResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.SubmitSingleSliderFormRequestTO ComMobicageToMessagingFormsSubmitSingleSliderFormRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.SubmitSingleSliderFormRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.SubmitSingleSliderFormResponseTO ComMobicageToMessagingFormsSubmitSingleSliderFormResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.SubmitSingleSliderFormResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.SubmitTextBlockFormRequestTO ComMobicageToMessagingFormsSubmitTextBlockFormRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.SubmitTextBlockFormRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.SubmitTextBlockFormResponseTO ComMobicageToMessagingFormsSubmitTextBlockFormResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.SubmitTextBlockFormResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.SubmitTextLineFormRequestTO ComMobicageToMessagingFormsSubmitTextLineFormRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.SubmitTextLineFormRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.SubmitTextLineFormResponseTO ComMobicageToMessagingFormsSubmitTextLineFormResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.SubmitTextLineFormResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.TextBlockFormMessageTO ComMobicageToMessagingFormsTextBlockFormMessageTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.TextBlockFormMessageTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.TextBlockFormTO ComMobicageToMessagingFormsTextBlockFormTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.TextBlockFormTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.TextBlockTO ComMobicageToMessagingFormsTextBlockTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.TextBlockTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.TextLineFormMessageTO ComMobicageToMessagingFormsTextLineFormMessageTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.TextLineFormMessageTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.TextLineFormTO ComMobicageToMessagingFormsTextLineFormTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.TextLineFormTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.TextLineTO ComMobicageToMessagingFormsTextLineTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.TextLineTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.UnicodeListWidgetResultTO ComMobicageToMessagingFormsUnicodeListWidgetResultTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.UnicodeListWidgetResultTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.UnicodeWidgetResultTO ComMobicageToMessagingFormsUnicodeWidgetResultTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.UnicodeWidgetResultTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.UpdateAdvancedOrderFormRequestTO ComMobicageToMessagingFormsUpdateAdvancedOrderFormRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.UpdateAdvancedOrderFormRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.UpdateAdvancedOrderFormResponseTO ComMobicageToMessagingFormsUpdateAdvancedOrderFormResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.UpdateAdvancedOrderFormResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.UpdateAutoCompleteFormRequestTO ComMobicageToMessagingFormsUpdateAutoCompleteFormRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.UpdateAutoCompleteFormRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.UpdateAutoCompleteFormResponseTO ComMobicageToMessagingFormsUpdateAutoCompleteFormResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.UpdateAutoCompleteFormResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.UpdateDateSelectFormRequestTO ComMobicageToMessagingFormsUpdateDateSelectFormRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.UpdateDateSelectFormRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.UpdateDateSelectFormResponseTO ComMobicageToMessagingFormsUpdateDateSelectFormResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.UpdateDateSelectFormResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.UpdateFriendSelectFormRequestTO ComMobicageToMessagingFormsUpdateFriendSelectFormRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.UpdateFriendSelectFormRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.UpdateFriendSelectFormResponseTO ComMobicageToMessagingFormsUpdateFriendSelectFormResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.UpdateFriendSelectFormResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.UpdateGPSLocationFormRequestTO ComMobicageToMessagingFormsUpdateGPSLocationFormRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.UpdateGPSLocationFormRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.UpdateGPSLocationFormResponseTO ComMobicageToMessagingFormsUpdateGPSLocationFormResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.UpdateGPSLocationFormResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.UpdateMultiSelectFormRequestTO ComMobicageToMessagingFormsUpdateMultiSelectFormRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.UpdateMultiSelectFormRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.UpdateMultiSelectFormResponseTO ComMobicageToMessagingFormsUpdateMultiSelectFormResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.UpdateMultiSelectFormResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.UpdateMyDigiPassFormRequestTO ComMobicageToMessagingFormsUpdateMyDigiPassFormRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.UpdateMyDigiPassFormRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.UpdateMyDigiPassFormResponseTO ComMobicageToMessagingFormsUpdateMyDigiPassFormResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.UpdateMyDigiPassFormResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.UpdatePhotoUploadFormRequestTO ComMobicageToMessagingFormsUpdatePhotoUploadFormRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.UpdatePhotoUploadFormRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.UpdatePhotoUploadFormResponseTO ComMobicageToMessagingFormsUpdatePhotoUploadFormResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.UpdatePhotoUploadFormResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.UpdateRangeSliderFormRequestTO ComMobicageToMessagingFormsUpdateRangeSliderFormRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.UpdateRangeSliderFormRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.UpdateRangeSliderFormResponseTO ComMobicageToMessagingFormsUpdateRangeSliderFormResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.UpdateRangeSliderFormResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.UpdateSignFormRequestTO ComMobicageToMessagingFormsUpdateSignFormRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.UpdateSignFormRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.UpdateSignFormResponseTO ComMobicageToMessagingFormsUpdateSignFormResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.UpdateSignFormResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.UpdateSingleSelectFormRequestTO ComMobicageToMessagingFormsUpdateSingleSelectFormRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.UpdateSingleSelectFormRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.UpdateSingleSelectFormResponseTO ComMobicageToMessagingFormsUpdateSingleSelectFormResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.UpdateSingleSelectFormResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.UpdateSingleSliderFormRequestTO ComMobicageToMessagingFormsUpdateSingleSliderFormRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.UpdateSingleSliderFormRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.UpdateSingleSliderFormResponseTO ComMobicageToMessagingFormsUpdateSingleSliderFormResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.UpdateSingleSliderFormResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.UpdateTextBlockFormRequestTO ComMobicageToMessagingFormsUpdateTextBlockFormRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.UpdateTextBlockFormRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.UpdateTextBlockFormResponseTO ComMobicageToMessagingFormsUpdateTextBlockFormResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.UpdateTextBlockFormResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.UpdateTextLineFormRequestTO ComMobicageToMessagingFormsUpdateTextLineFormRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.UpdateTextLineFormRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.forms.UpdateTextLineFormResponseTO ComMobicageToMessagingFormsUpdateTextLineFormResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.forms.UpdateTextLineFormResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.jsmfr.FlowStartedRequestTO ComMobicageToMessagingJsmfrFlowStartedRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.jsmfr.FlowStartedRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.jsmfr.FlowStartedResponseTO ComMobicageToMessagingJsmfrFlowStartedResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.jsmfr.FlowStartedResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.jsmfr.JsMessageFlowMemberRunTO ComMobicageToMessagingJsmfrJsMessageFlowMemberRunTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.jsmfr.JsMessageFlowMemberRunTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.jsmfr.MessageFlowErrorRequestTO ComMobicageToMessagingJsmfrMessageFlowErrorRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.jsmfr.MessageFlowErrorRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.jsmfr.MessageFlowErrorResponseTO ComMobicageToMessagingJsmfrMessageFlowErrorResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.jsmfr.MessageFlowErrorResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.jsmfr.MessageFlowFinishedRequestTO ComMobicageToMessagingJsmfrMessageFlowFinishedRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.jsmfr.MessageFlowFinishedRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.jsmfr.MessageFlowFinishedResponseTO ComMobicageToMessagingJsmfrMessageFlowFinishedResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.jsmfr.MessageFlowFinishedResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.jsmfr.MessageFlowMemberResultRequestTO ComMobicageToMessagingJsmfrMessageFlowMemberResultRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.jsmfr.MessageFlowMemberResultRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.jsmfr.MessageFlowMemberResultResponseTO ComMobicageToMessagingJsmfrMessageFlowMemberResultResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.jsmfr.MessageFlowMemberResultResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.jsmfr.NewFlowMessageRequestTO ComMobicageToMessagingJsmfrNewFlowMessageRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.jsmfr.NewFlowMessageRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.messaging.jsmfr.NewFlowMessageResponseTO ComMobicageToMessagingJsmfrNewFlowMessageResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.messaging.jsmfr.NewFlowMessageResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.news.AppNewsInfoTO ComMobicageToNewsAppNewsInfoTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.news.AppNewsInfoTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.news.AppNewsItemTO ComMobicageToNewsAppNewsItemTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.news.AppNewsItemTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.news.DisableNewsRequestTO ComMobicageToNewsDisableNewsRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.news.DisableNewsRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.news.DisableNewsResponseTO ComMobicageToNewsDisableNewsResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.news.DisableNewsResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.news.GetNewsItemsRequestTO ComMobicageToNewsGetNewsItemsRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.news.GetNewsItemsRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.news.GetNewsItemsResponseTO ComMobicageToNewsGetNewsItemsResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.news.GetNewsItemsResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.news.GetNewsRequestTO ComMobicageToNewsGetNewsRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.news.GetNewsRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.news.GetNewsResponseTO ComMobicageToNewsGetNewsResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.news.GetNewsResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.news.NewNewsRequestTO ComMobicageToNewsNewNewsRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.news.NewNewsRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.news.NewNewsResponseTO ComMobicageToNewsNewNewsResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.news.NewNewsResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.news.NewsActionButtonTO ComMobicageToNewsNewsActionButtonTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.news.NewsActionButtonTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.news.NewsSenderTO ComMobicageToNewsNewsSenderTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.news.NewsSenderTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.news.NewsStatisticsRequestTO ComMobicageToNewsNewsStatisticsRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.news.NewsStatisticsRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.news.NewsStatisticsResponseTO ComMobicageToNewsNewsStatisticsResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.news.NewsStatisticsResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.service.FindServiceCategoryTO ComMobicageToServiceFindServiceCategoryTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.service.FindServiceCategoryTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.service.FindServiceItemTO ComMobicageToServiceFindServiceItemTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.service.FindServiceItemTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.service.FindServiceRequestTO ComMobicageToServiceFindServiceRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.service.FindServiceRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.service.FindServiceResponseTO ComMobicageToServiceFindServiceResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.service.FindServiceResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.service.GetMenuIconRequestTO ComMobicageToServiceGetMenuIconRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.service.GetMenuIconRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.service.GetMenuIconResponseTO ComMobicageToServiceGetMenuIconResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.service.GetMenuIconResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.service.GetServiceActionInfoRequestTO ComMobicageToServiceGetServiceActionInfoRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.service.GetServiceActionInfoRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.service.GetServiceActionInfoResponseTO ComMobicageToServiceGetServiceActionInfoResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.service.GetServiceActionInfoResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.service.GetStaticFlowRequestTO ComMobicageToServiceGetStaticFlowRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.service.GetStaticFlowRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.service.GetStaticFlowResponseTO ComMobicageToServiceGetStaticFlowResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.service.GetStaticFlowResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.service.PokeServiceRequestTO ComMobicageToServicePokeServiceRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.service.PokeServiceRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.service.PokeServiceResponseTO ComMobicageToServicePokeServiceResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.service.PokeServiceResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.service.PressMenuIconRequestTO ComMobicageToServicePressMenuIconRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.service.PressMenuIconRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.service.PressMenuIconResponseTO ComMobicageToServicePressMenuIconResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.service.PressMenuIconResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.service.ReceiveApiCallResultRequestTO ComMobicageToServiceReceiveApiCallResultRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.service.ReceiveApiCallResultRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.service.ReceiveApiCallResultResponseTO ComMobicageToServiceReceiveApiCallResultResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.service.ReceiveApiCallResultResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.service.SendApiCallRequestTO ComMobicageToServiceSendApiCallRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.service.SendApiCallRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.service.SendApiCallResponseTO ComMobicageToServiceSendApiCallResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.service.SendApiCallResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.service.ShareServiceRequestTO ComMobicageToServiceShareServiceRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.service.ShareServiceRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.service.ShareServiceResponseTO ComMobicageToServiceShareServiceResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.service.ShareServiceResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.service.StartServiceActionRequestTO ComMobicageToServiceStartServiceActionRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.service.StartServiceActionRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.service.StartServiceActionResponseTO ComMobicageToServiceStartServiceActionResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.service.StartServiceActionResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.service.UpdateUserDataRequestTO ComMobicageToServiceUpdateUserDataRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.service.UpdateUserDataRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.service.UpdateUserDataResponseTO ComMobicageToServiceUpdateUserDataResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.service.UpdateUserDataResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.system.EditProfileRequestTO ComMobicageToSystemEditProfileRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.system.EditProfileRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.system.EditProfileResponseTO ComMobicageToSystemEditProfileResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.system.EditProfileResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.system.ForwardLogsRequestTO ComMobicageToSystemForwardLogsRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.system.ForwardLogsRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.system.ForwardLogsResponseTO ComMobicageToSystemForwardLogsResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.system.ForwardLogsResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.system.GetIdentityQRCodeRequestTO ComMobicageToSystemGetIdentityQRCodeRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.system.GetIdentityQRCodeRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.system.GetIdentityQRCodeResponseTO ComMobicageToSystemGetIdentityQRCodeResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.system.GetIdentityQRCodeResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.system.GetIdentityRequestTO ComMobicageToSystemGetIdentityRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.system.GetIdentityRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.system.GetIdentityResponseTO ComMobicageToSystemGetIdentityResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.system.GetIdentityResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.system.HeartBeatRequestTO ComMobicageToSystemHeartBeatRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.system.HeartBeatRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.system.HeartBeatResponseTO ComMobicageToSystemHeartBeatResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.system.HeartBeatResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.system.IdentityTO ComMobicageToSystemIdentityTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.system.IdentityTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.system.IdentityUpdateRequestTO ComMobicageToSystemIdentityUpdateRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.system.IdentityUpdateRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.system.IdentityUpdateResponseTO ComMobicageToSystemIdentityUpdateResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.system.IdentityUpdateResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.system.LogErrorRequestTO ComMobicageToSystemLogErrorRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.system.LogErrorRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.system.LogErrorResponseTO ComMobicageToSystemLogErrorResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.system.LogErrorResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.system.SaveSettingsRequest ComMobicageToSystemSaveSettingsRequest(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.system.SaveSettingsRequest((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.system.SaveSettingsResponse ComMobicageToSystemSaveSettingsResponse(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.system.SaveSettingsResponse((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.system.SetMobilePhoneNumberRequestTO ComMobicageToSystemSetMobilePhoneNumberRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.system.SetMobilePhoneNumberRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.system.SetMobilePhoneNumberResponseTO ComMobicageToSystemSetMobilePhoneNumberResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.system.SetMobilePhoneNumberResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.system.SetSecureInfoRequestTO ComMobicageToSystemSetSecureInfoRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.system.SetSecureInfoRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.system.SetSecureInfoResponseTO ComMobicageToSystemSetSecureInfoResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.system.SetSecureInfoResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.system.SettingsTO ComMobicageToSystemSettingsTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.system.SettingsTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.system.UnregisterMobileRequestTO ComMobicageToSystemUnregisterMobileRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.system.UnregisterMobileRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.system.UnregisterMobileResponseTO ComMobicageToSystemUnregisterMobileResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.system.UnregisterMobileResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.system.UpdateApplePushDeviceTokenRequestTO ComMobicageToSystemUpdateApplePushDeviceTokenRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.system.UpdateApplePushDeviceTokenRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.system.UpdateApplePushDeviceTokenResponseTO ComMobicageToSystemUpdateApplePushDeviceTokenResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.system.UpdateApplePushDeviceTokenResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.system.UpdateAvailableRequestTO ComMobicageToSystemUpdateAvailableRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.system.UpdateAvailableRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.system.UpdateAvailableResponseTO ComMobicageToSystemUpdateAvailableResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.system.UpdateAvailableResponseTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.system.UpdateSettingsRequestTO ComMobicageToSystemUpdateSettingsRequestTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.system.UpdateSettingsRequestTO((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    public static com.mobicage.to.system.UpdateSettingsResponseTO ComMobicageToSystemUpdateSettingsResponseTO(Object value) throws IncompleteMessageException {
        if (value == null)
            return null;
        return new com.mobicage.to.system.UpdateSettingsResponseTO((Map<String, Object>) value);
    }

}

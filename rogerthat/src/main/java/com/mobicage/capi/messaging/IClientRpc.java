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

package com.mobicage.capi.messaging;

public interface IClientRpc {

    com.mobicage.to.messaging.ConversationDeletedResponseTO conversationDeleted(com.mobicage.to.messaging.ConversationDeletedRequestTO request) throws java.lang.Exception;

    com.mobicage.to.messaging.EndMessageFlowResponseTO endMessageFlow(com.mobicage.to.messaging.EndMessageFlowRequestTO request) throws java.lang.Exception;

    com.mobicage.to.messaging.MessageLockedResponseTO messageLocked(com.mobicage.to.messaging.MessageLockedRequestTO request) throws java.lang.Exception;

    com.mobicage.to.messaging.forms.NewAdvancedOrderFormResponseTO newAdvancedOrderForm(com.mobicage.to.messaging.forms.NewAdvancedOrderFormRequestTO request) throws java.lang.Exception;

    com.mobicage.to.messaging.forms.NewAutoCompleteFormResponseTO newAutoCompleteForm(com.mobicage.to.messaging.forms.NewAutoCompleteFormRequestTO request) throws java.lang.Exception;

    com.mobicage.to.messaging.forms.NewDateSelectFormResponseTO newDateSelectForm(com.mobicage.to.messaging.forms.NewDateSelectFormRequestTO request) throws java.lang.Exception;

    com.mobicage.to.messaging.forms.NewFriendSelectFormResponseTO newFriendSelectForm(com.mobicage.to.messaging.forms.NewFriendSelectFormRequestTO request) throws java.lang.Exception;

    com.mobicage.to.messaging.forms.NewGPSLocationFormResponseTO newGPSLocationForm(com.mobicage.to.messaging.forms.NewGPSLocationFormRequestTO request) throws java.lang.Exception;

    com.mobicage.to.messaging.NewMessageResponseTO newMessage(com.mobicage.to.messaging.NewMessageRequestTO request) throws java.lang.Exception;

    com.mobicage.to.messaging.forms.NewMultiSelectFormResponseTO newMultiSelectForm(com.mobicage.to.messaging.forms.NewMultiSelectFormRequestTO request) throws java.lang.Exception;

    com.mobicage.to.messaging.forms.NewMyDigiPassFormResponseTO newMyDigiPassForm(com.mobicage.to.messaging.forms.NewMyDigiPassFormRequestTO request) throws java.lang.Exception;

    com.mobicage.to.messaging.forms.NewOauthFormResponseTO newOauthForm(com.mobicage.to.messaging.forms.NewOauthFormRequestTO request) throws java.lang.Exception;

    com.mobicage.to.messaging.forms.NewPhotoUploadFormResponseTO newPhotoUploadForm(com.mobicage.to.messaging.forms.NewPhotoUploadFormRequestTO request) throws java.lang.Exception;

    com.mobicage.to.messaging.forms.NewRangeSliderFormResponseTO newRangeSliderForm(com.mobicage.to.messaging.forms.NewRangeSliderFormRequestTO request) throws java.lang.Exception;

    com.mobicage.to.messaging.forms.NewRatingFormResponseTO newRatingForm(com.mobicage.to.messaging.forms.NewRatingFormRequestTO request) throws java.lang.Exception;

    com.mobicage.to.messaging.forms.NewSignFormResponseTO newSignForm(com.mobicage.to.messaging.forms.NewSignFormRequestTO request) throws java.lang.Exception;

    com.mobicage.to.messaging.forms.NewSingleSelectFormResponseTO newSingleSelectForm(com.mobicage.to.messaging.forms.NewSingleSelectFormRequestTO request) throws java.lang.Exception;

    com.mobicage.to.messaging.forms.NewSingleSliderFormResponseTO newSingleSliderForm(com.mobicage.to.messaging.forms.NewSingleSliderFormRequestTO request) throws java.lang.Exception;

    com.mobicage.to.messaging.forms.NewTextBlockFormResponseTO newTextBlockForm(com.mobicage.to.messaging.forms.NewTextBlockFormRequestTO request) throws java.lang.Exception;

    com.mobicage.to.messaging.forms.NewTextLineFormResponseTO newTextLineForm(com.mobicage.to.messaging.forms.NewTextLineFormRequestTO request) throws java.lang.Exception;

    com.mobicage.to.messaging.StartFlowResponseTO startFlow(com.mobicage.to.messaging.StartFlowRequestTO request) throws java.lang.Exception;

    com.mobicage.to.messaging.TransferCompletedResponseTO transferCompleted(com.mobicage.to.messaging.TransferCompletedRequestTO request) throws java.lang.Exception;

    com.mobicage.to.messaging.forms.UpdateAdvancedOrderFormResponseTO updateAdvancedOrderForm(com.mobicage.to.messaging.forms.UpdateAdvancedOrderFormRequestTO request) throws java.lang.Exception;

    com.mobicage.to.messaging.forms.UpdateAutoCompleteFormResponseTO updateAutoCompleteForm(com.mobicage.to.messaging.forms.UpdateAutoCompleteFormRequestTO request) throws java.lang.Exception;

    com.mobicage.to.messaging.forms.UpdateDateSelectFormResponseTO updateDateSelectForm(com.mobicage.to.messaging.forms.UpdateDateSelectFormRequestTO request) throws java.lang.Exception;

    com.mobicage.to.messaging.forms.UpdateFriendSelectFormResponseTO updateFriendSelectForm(com.mobicage.to.messaging.forms.UpdateFriendSelectFormRequestTO request) throws java.lang.Exception;

    com.mobicage.to.messaging.forms.UpdateGPSLocationFormResponseTO updateGPSLocationForm(com.mobicage.to.messaging.forms.UpdateGPSLocationFormRequestTO request) throws java.lang.Exception;

    com.mobicage.to.messaging.UpdateMessageResponseTO updateMessage(com.mobicage.to.messaging.UpdateMessageRequestTO request) throws java.lang.Exception;

    com.mobicage.to.messaging.MemberStatusUpdateResponseTO updateMessageMemberStatus(com.mobicage.to.messaging.MemberStatusUpdateRequestTO request) throws java.lang.Exception;

    com.mobicage.to.messaging.forms.UpdateMultiSelectFormResponseTO updateMultiSelectForm(com.mobicage.to.messaging.forms.UpdateMultiSelectFormRequestTO request) throws java.lang.Exception;

    com.mobicage.to.messaging.forms.UpdateMyDigiPassFormResponseTO updateMyDigiPassForm(com.mobicage.to.messaging.forms.UpdateMyDigiPassFormRequestTO request) throws java.lang.Exception;

    com.mobicage.to.messaging.forms.UpdateOauthFormResponseTO updateOauthForm(com.mobicage.to.messaging.forms.UpdateOauthFormRequestTO request) throws java.lang.Exception;

    com.mobicage.to.messaging.forms.UpdatePhotoUploadFormResponseTO updatePhotoUploadForm(com.mobicage.to.messaging.forms.UpdatePhotoUploadFormRequestTO request) throws java.lang.Exception;

    com.mobicage.to.messaging.forms.UpdateRangeSliderFormResponseTO updateRangeSliderForm(com.mobicage.to.messaging.forms.UpdateRangeSliderFormRequestTO request) throws java.lang.Exception;

    com.mobicage.to.messaging.forms.UpdateRatingFormResponseTO updateRatingForm(com.mobicage.to.messaging.forms.UpdateRatingFormRequestTO request) throws java.lang.Exception;

    com.mobicage.to.messaging.forms.UpdateSignFormResponseTO updateSignForm(com.mobicage.to.messaging.forms.UpdateSignFormRequestTO request) throws java.lang.Exception;

    com.mobicage.to.messaging.forms.UpdateSingleSelectFormResponseTO updateSingleSelectForm(com.mobicage.to.messaging.forms.UpdateSingleSelectFormRequestTO request) throws java.lang.Exception;

    com.mobicage.to.messaging.forms.UpdateSingleSliderFormResponseTO updateSingleSliderForm(com.mobicage.to.messaging.forms.UpdateSingleSliderFormRequestTO request) throws java.lang.Exception;

    com.mobicage.to.messaging.forms.UpdateTextBlockFormResponseTO updateTextBlockForm(com.mobicage.to.messaging.forms.UpdateTextBlockFormRequestTO request) throws java.lang.Exception;

    com.mobicage.to.messaging.forms.UpdateTextLineFormResponseTO updateTextLineForm(com.mobicage.to.messaging.forms.UpdateTextLineFormRequestTO request) throws java.lang.Exception;

}

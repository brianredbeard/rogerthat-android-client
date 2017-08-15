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

package com.mobicage.rogerthat.plugins.messaging;

import com.mobicage.rogerthat.MainService;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.to.messaging.ConversationDeletedRequestTO;
import com.mobicage.to.messaging.ConversationDeletedResponseTO;
import com.mobicage.to.messaging.EndMessageFlowRequestTO;
import com.mobicage.to.messaging.EndMessageFlowResponseTO;
import com.mobicage.to.messaging.MemberStatusUpdateRequestTO;
import com.mobicage.to.messaging.MemberStatusUpdateResponseTO;
import com.mobicage.to.messaging.MessageLockedRequestTO;
import com.mobicage.to.messaging.MessageLockedResponseTO;
import com.mobicage.to.messaging.NewMessageRequestTO;
import com.mobicage.to.messaging.NewMessageResponseTO;
import com.mobicage.to.messaging.StartFlowRequestTO;
import com.mobicage.to.messaging.StartFlowResponseTO;
import com.mobicage.to.messaging.TransferCompletedResponseTO;
import com.mobicage.to.messaging.UpdateMessageRequestTO;
import com.mobicage.to.messaging.UpdateMessageResponseTO;
import com.mobicage.to.messaging.forms.NewAdvancedOrderFormRequestTO;
import com.mobicage.to.messaging.forms.NewAdvancedOrderFormResponseTO;
import com.mobicage.to.messaging.forms.NewAutoCompleteFormRequestTO;
import com.mobicage.to.messaging.forms.NewAutoCompleteFormResponseTO;
import com.mobicage.to.messaging.forms.NewDateSelectFormRequestTO;
import com.mobicage.to.messaging.forms.NewDateSelectFormResponseTO;
import com.mobicage.to.messaging.forms.NewFriendSelectFormRequestTO;
import com.mobicage.to.messaging.forms.NewFriendSelectFormResponseTO;
import com.mobicage.to.messaging.forms.NewGPSLocationFormRequestTO;
import com.mobicage.to.messaging.forms.NewGPSLocationFormResponseTO;
import com.mobicage.to.messaging.forms.NewMultiSelectFormRequestTO;
import com.mobicage.to.messaging.forms.NewMultiSelectFormResponseTO;
import com.mobicage.to.messaging.forms.NewMyDigiPassFormRequestTO;
import com.mobicage.to.messaging.forms.NewMyDigiPassFormResponseTO;
import com.mobicage.to.messaging.forms.NewPhotoUploadFormRequestTO;
import com.mobicage.to.messaging.forms.NewPhotoUploadFormResponseTO;
import com.mobicage.to.messaging.forms.NewRangeSliderFormRequestTO;
import com.mobicage.to.messaging.forms.NewRangeSliderFormResponseTO;
import com.mobicage.to.messaging.forms.NewRatingFormRequestTO;
import com.mobicage.to.messaging.forms.NewRatingFormResponseTO;
import com.mobicage.to.messaging.forms.NewSignFormRequestTO;
import com.mobicage.to.messaging.forms.NewSignFormResponseTO;
import com.mobicage.to.messaging.forms.NewSingleSelectFormRequestTO;
import com.mobicage.to.messaging.forms.NewSingleSelectFormResponseTO;
import com.mobicage.to.messaging.forms.NewSingleSliderFormRequestTO;
import com.mobicage.to.messaging.forms.NewSingleSliderFormResponseTO;
import com.mobicage.to.messaging.forms.NewTextBlockFormRequestTO;
import com.mobicage.to.messaging.forms.NewTextBlockFormResponseTO;
import com.mobicage.to.messaging.forms.NewTextLineFormRequestTO;
import com.mobicage.to.messaging.forms.NewTextLineFormResponseTO;
import com.mobicage.to.messaging.forms.UpdateAdvancedOrderFormRequestTO;
import com.mobicage.to.messaging.forms.UpdateAdvancedOrderFormResponseTO;
import com.mobicage.to.messaging.forms.UpdateAutoCompleteFormRequestTO;
import com.mobicage.to.messaging.forms.UpdateAutoCompleteFormResponseTO;
import com.mobicage.to.messaging.forms.UpdateDateSelectFormRequestTO;
import com.mobicage.to.messaging.forms.UpdateDateSelectFormResponseTO;
import com.mobicage.to.messaging.forms.UpdateFriendSelectFormRequestTO;
import com.mobicage.to.messaging.forms.UpdateFriendSelectFormResponseTO;
import com.mobicage.to.messaging.forms.UpdateGPSLocationFormRequestTO;
import com.mobicage.to.messaging.forms.UpdateGPSLocationFormResponseTO;
import com.mobicage.to.messaging.forms.UpdateMultiSelectFormRequestTO;
import com.mobicage.to.messaging.forms.UpdateMultiSelectFormResponseTO;
import com.mobicage.to.messaging.forms.UpdateMyDigiPassFormRequestTO;
import com.mobicage.to.messaging.forms.UpdateMyDigiPassFormResponseTO;
import com.mobicage.to.messaging.forms.UpdatePhotoUploadFormRequestTO;
import com.mobicage.to.messaging.forms.UpdatePhotoUploadFormResponseTO;
import com.mobicage.to.messaging.forms.UpdateRangeSliderFormRequestTO;
import com.mobicage.to.messaging.forms.UpdateRangeSliderFormResponseTO;
import com.mobicage.to.messaging.forms.UpdateRatingFormRequestTO;
import com.mobicage.to.messaging.forms.UpdateRatingFormResponseTO;
import com.mobicage.to.messaging.forms.UpdateSignFormRequestTO;
import com.mobicage.to.messaging.forms.UpdateSignFormResponseTO;
import com.mobicage.to.messaging.forms.UpdateSingleSelectFormRequestTO;
import com.mobicage.to.messaging.forms.UpdateSingleSelectFormResponseTO;
import com.mobicage.to.messaging.forms.UpdateSingleSliderFormRequestTO;
import com.mobicage.to.messaging.forms.UpdateSingleSliderFormResponseTO;
import com.mobicage.to.messaging.forms.UpdateTextBlockFormRequestTO;
import com.mobicage.to.messaging.forms.UpdateTextBlockFormResponseTO;
import com.mobicage.to.messaging.forms.UpdateTextLineFormRequestTO;
import com.mobicage.to.messaging.forms.UpdateTextLineFormResponseTO;

public class MessagingCallReceiver implements com.mobicage.capi.messaging.IClientRpc {

    private final MessagingPlugin mPlugin;
    private final MainService mMainService;

    public MessagingCallReceiver(final MainService pMainService, final MessagingPlugin pPlugin) {
        T.UI();
        mPlugin = pPlugin;
        mMainService = pMainService;
    }

    @Override
    public NewMessageResponseTO newMessage(NewMessageRequestTO request) throws Exception {
        T.BIZZ();
        mPlugin.newMessage(request.message, false, false);
        NewMessageResponseTO response = new NewMessageResponseTO();
        response.received_timestamp = mMainService.currentTimeMillis() / 1000;
        return response;
    }

    @Override
    public MemberStatusUpdateResponseTO updateMessageMemberStatus(MemberStatusUpdateRequestTO request) throws Exception {
        T.BIZZ();
        if (!mPlugin.getBrandingMgr().queueIfNeeded("com.mobicage.capi.messaging.updateMessageMemberStatus", request,
            request.message)) {

            mPlugin.updateMemberStatus(request);
        }
        return null;
    }

    @Override
    public MessageLockedResponseTO messageLocked(MessageLockedRequestTO request) throws Exception {
        T.BIZZ();
        if (!mPlugin.getBrandingMgr().queueIfNeeded("com.mobicage.capi.messaging.messageLocked", request,
            request.message_key)) {

            mPlugin.messageLocked(request);
        }
        return null;
    }

    @Override
    public NewAutoCompleteFormResponseTO newAutoCompleteForm(NewAutoCompleteFormRequestTO request) throws Exception {
        T.BIZZ();
        mPlugin.newMessage(Message.fromFormMessage(request.form_message.toJSONMap()), false, false);
        NewAutoCompleteFormResponseTO response = new NewAutoCompleteFormResponseTO();
        response.received_timestamp = mMainService.currentTimeMillis() / 1000;
        return response;
    }

    @Override
    public NewDateSelectFormResponseTO newDateSelectForm(NewDateSelectFormRequestTO request) throws Exception {
        T.BIZZ();
        mPlugin.newMessage(Message.fromFormMessage(request.form_message.toJSONMap()), false, false);
        NewDateSelectFormResponseTO response = new NewDateSelectFormResponseTO();
        response.received_timestamp = mMainService.currentTimeMillis() / 1000;
        return response;
    }

    @Override
    public NewMultiSelectFormResponseTO newMultiSelectForm(NewMultiSelectFormRequestTO request) throws Exception {
        T.BIZZ();
        mPlugin.newMessage(Message.fromFormMessage(request.form_message.toJSONMap()), false, false);
        NewMultiSelectFormResponseTO response = new NewMultiSelectFormResponseTO();
        response.received_timestamp = mMainService.currentTimeMillis() / 1000;
        return response;
    }

    @Override
    public NewRangeSliderFormResponseTO newRangeSliderForm(NewRangeSliderFormRequestTO request) throws Exception {
        T.BIZZ();
        mPlugin.newMessage(Message.fromFormMessage(request.form_message.toJSONMap()), false, false);
        NewRangeSliderFormResponseTO response = new NewRangeSliderFormResponseTO();
        response.received_timestamp = mMainService.currentTimeMillis() / 1000;
        return response;
    }

    @Override
    public NewFriendSelectFormResponseTO newFriendSelectForm(NewFriendSelectFormRequestTO request) throws Exception { 
        T.BIZZ();
        mPlugin.newMessage(Message.fromFormMessage(request.form_message.toJSONMap()), false, false);
        NewFriendSelectFormResponseTO response = new NewFriendSelectFormResponseTO();
        response.received_timestamp = mMainService.currentTimeMillis() / 1000;
        return response;
    }

    @Override
    public NewSingleSelectFormResponseTO newSingleSelectForm(NewSingleSelectFormRequestTO request) throws Exception {
        T.BIZZ();
        mPlugin.newMessage(Message.fromFormMessage(request.form_message.toJSONMap()), false, false);
        NewSingleSelectFormResponseTO response = new NewSingleSelectFormResponseTO();
        response.received_timestamp = mMainService.currentTimeMillis() / 1000;
        return response;
    }

    @Override
    public NewSingleSliderFormResponseTO newSingleSliderForm(NewSingleSliderFormRequestTO request) throws Exception {
        T.BIZZ();
        mPlugin.newMessage(Message.fromFormMessage(request.form_message.toJSONMap()), false, false);
        NewSingleSliderFormResponseTO response = new NewSingleSliderFormResponseTO();
        response.received_timestamp = mMainService.currentTimeMillis() / 1000;
        return response;
    }

    @Override
    public NewTextBlockFormResponseTO newTextBlockForm(NewTextBlockFormRequestTO request) throws Exception {
        T.BIZZ();
        mPlugin.newMessage(Message.fromFormMessage(request.form_message.toJSONMap()), false, false);
        NewTextBlockFormResponseTO response = new NewTextBlockFormResponseTO();
        response.received_timestamp = mMainService.currentTimeMillis() / 1000;
        return response;
    }

    @Override
    public NewTextLineFormResponseTO newTextLineForm(NewTextLineFormRequestTO request) throws Exception {
        T.BIZZ();
        mPlugin.newMessage(Message.fromFormMessage(request.form_message.toJSONMap()), false, false);
        NewTextLineFormResponseTO response = new NewTextLineFormResponseTO();
        response.received_timestamp = mMainService.currentTimeMillis() / 1000;
        return response;
    }

    @Override
    public NewPhotoUploadFormResponseTO newPhotoUploadForm(NewPhotoUploadFormRequestTO request) throws Exception {
        T.BIZZ();
        mPlugin.newMessage(Message.fromFormMessage(request.form_message.toJSONMap()), false, false);
        NewPhotoUploadFormResponseTO response = new NewPhotoUploadFormResponseTO();
        response.received_timestamp = mMainService.currentTimeMillis() / 1000;
        return response;
    }

    @Override
    public NewGPSLocationFormResponseTO newGPSLocationForm(NewGPSLocationFormRequestTO request) throws Exception {
        T.BIZZ();
        mPlugin.newMessage(Message.fromFormMessage(request.form_message.toJSONMap()), false, false);
        NewGPSLocationFormResponseTO response = new NewGPSLocationFormResponseTO();
        response.received_timestamp = mMainService.currentTimeMillis() / 1000;
        return response;
    }

    @Override
    public NewMyDigiPassFormResponseTO newMyDigiPassForm(NewMyDigiPassFormRequestTO request) throws Exception {
        T.BIZZ();
        mPlugin.newMessage(Message.fromFormMessage(request.form_message.toJSONMap()), false, false);
        NewMyDigiPassFormResponseTO response = new NewMyDigiPassFormResponseTO();
        response.received_timestamp = mMainService.currentTimeMillis() / 1000;
        return response;
    }

    @Override
    public NewAdvancedOrderFormResponseTO newAdvancedOrderForm(NewAdvancedOrderFormRequestTO request) throws Exception {
        T.BIZZ();
        mPlugin.newMessage(Message.fromFormMessage(request.form_message.toJSONMap()), false, false);
        NewAdvancedOrderFormResponseTO response = new NewAdvancedOrderFormResponseTO();
        response.received_timestamp = mMainService.currentTimeMillis() / 1000;
        return response;
    }

    @Override
    public NewSignFormResponseTO newSignForm(NewSignFormRequestTO request) throws Exception {
        mPlugin.newMessage(Message.fromFormMessage(request.form_message.toJSONMap()), false, false);
        NewSignFormResponseTO response = new NewSignFormResponseTO();
        response.received_timestamp = mMainService.currentTimeMillis() / 1000;
        return response;
    }

    @Override
    public NewRatingFormResponseTO newRatingForm(NewRatingFormRequestTO request) throws Exception {
        T.BIZZ();
        mPlugin.newMessage(Message.fromFormMessage(request.form_message.toJSONMap()), false, false);
        NewRatingFormResponseTO response = new NewRatingFormResponseTO();
        response.received_timestamp = mMainService.currentTimeMillis() / 1000;
        return response;
    }

    @Override
    public StartFlowResponseTO startFlow(StartFlowRequestTO request) throws Exception {
        T.BIZZ();
        mPlugin.startFlow(request);
        return new StartFlowResponseTO();
    }

    @Override
    public UpdateAutoCompleteFormResponseTO updateAutoCompleteForm(UpdateAutoCompleteFormRequestTO request)
        throws Exception {
        T.BIZZ();
        if (!mPlugin.getBrandingMgr().queueIfNeeded("com.mobicage.capi.messaging.updateAutoCompleteForm", request,
            request.message_key)) {

            mPlugin.updateForm(request.parent_message_key, request.message_key, request.result, request.button_id,
                request.received_timestamp, request.acked_timestamp);
        }
        return new UpdateAutoCompleteFormResponseTO();
    }

    @Override
    public UpdateDateSelectFormResponseTO updateDateSelectForm(UpdateDateSelectFormRequestTO request) throws Exception {
        T.BIZZ();
        if (!mPlugin.getBrandingMgr().queueIfNeeded("com.mobicage.capi.messaging.updateDateSelectForm", request,
            request.message_key)) {

            mPlugin.updateForm(request.parent_message_key, request.message_key, request.result, request.button_id,
                request.received_timestamp, request.acked_timestamp, "date");
        }
        return new UpdateDateSelectFormResponseTO();
    }

    @Override
    public UpdateMultiSelectFormResponseTO updateMultiSelectForm(UpdateMultiSelectFormRequestTO request)
        throws Exception {
        T.BIZZ();
        if (!mPlugin.getBrandingMgr().queueIfNeeded("com.mobicage.capi.messaging.updateMultiSelectForm", request,
            request.message_key)) {

            mPlugin.updateForm(request.parent_message_key, request.message_key, request.result, request.button_id,
                request.received_timestamp, request.acked_timestamp);
        }
        return new UpdateMultiSelectFormResponseTO();
    }

    @Override
    public UpdateRangeSliderFormResponseTO updateRangeSliderForm(UpdateRangeSliderFormRequestTO request)
        throws Exception {
        T.BIZZ();
        if (!mPlugin.getBrandingMgr().queueIfNeeded("com.mobicage.capi.messaging.updateRangeSliderForm", request,
            request.message_key)) {

            mPlugin.updateRangeSliderForm(request.parent_message_key, request.message_key, request.result,
                request.button_id, request.received_timestamp, request.acked_timestamp);
        }
        return new UpdateRangeSliderFormResponseTO();
    }

    @Override
    public UpdateFriendSelectFormResponseTO updateFriendSelectForm(UpdateFriendSelectFormRequestTO request)
            throws Exception {
        T.BIZZ();
        if (!mPlugin.getBrandingMgr().queueIfNeeded("com.mobicage.capi.messaging.updateFriendSelectForm", request,
                request.message_key)) {

            mPlugin.updateForm(request.parent_message_key, request.message_key, request.result, request.button_id,
                    request.received_timestamp, request.acked_timestamp);
        }
        return new UpdateFriendSelectFormResponseTO();
    }

    @Override
    public UpdateSingleSelectFormResponseTO updateSingleSelectForm(UpdateSingleSelectFormRequestTO request)
            throws Exception {
        T.BIZZ();
        if (!mPlugin.getBrandingMgr().queueIfNeeded("com.mobicage.capi.messaging.updateSingleSelectForm", request,
                request.message_key)) {

            mPlugin.updateForm(request.parent_message_key, request.message_key, request.result, request.button_id,
                    request.received_timestamp, request.acked_timestamp);
        }
        return new UpdateSingleSelectFormResponseTO();
    }

    @Override
    public UpdateSingleSliderFormResponseTO updateSingleSliderForm(UpdateSingleSliderFormRequestTO request)
        throws Exception {
        T.BIZZ();
        if (!mPlugin.getBrandingMgr().queueIfNeeded("com.mobicage.capi.messaging.updateSingleSliderForm", request,
            request.message_key)) {

            mPlugin.updateForm(request.parent_message_key, request.message_key, request.result, request.button_id,
                request.received_timestamp, request.acked_timestamp);
        }
        return new UpdateSingleSliderFormResponseTO();
    }

    @Override
    public UpdateTextBlockFormResponseTO updateTextBlockForm(UpdateTextBlockFormRequestTO request) throws Exception {
        T.BIZZ();
        if (!mPlugin.getBrandingMgr().queueIfNeeded("com.mobicage.capi.messaging.updateTextBlockForm", request,
            request.message_key)) {

            mPlugin.updateForm(request.parent_message_key, request.message_key, request.result, request.button_id,
                request.received_timestamp, request.acked_timestamp);
        }
        return new UpdateTextBlockFormResponseTO();
    }

    @Override
    public UpdateTextLineFormResponseTO updateTextLineForm(UpdateTextLineFormRequestTO request) throws Exception {
        T.BIZZ();
        if (!mPlugin.getBrandingMgr().queueIfNeeded("com.mobicage.capi.messaging.updateTextLineForm", request,
            request.message_key)) {

            mPlugin.updateForm(request.parent_message_key, request.message_key, request.result, request.button_id,
                request.received_timestamp, request.acked_timestamp);
        }
        return new UpdateTextLineFormResponseTO();
    }

    @Override
    public UpdatePhotoUploadFormResponseTO updatePhotoUploadForm(UpdatePhotoUploadFormRequestTO request)
        throws Exception {
        T.BIZZ();
        if (!mPlugin.getBrandingMgr().queueIfNeeded("com.mobicage.capi.messaging.updatePhotoUploadForm", request,
            request.message_key)) {

            mPlugin.updateForm(request.parent_message_key, request.message_key, request.result, request.button_id,
                request.received_timestamp, request.acked_timestamp);
        }
        return new UpdatePhotoUploadFormResponseTO();
    }

    @Override
    public UpdateGPSLocationFormResponseTO updateGPSLocationForm(UpdateGPSLocationFormRequestTO request)
        throws Exception {
        T.BIZZ();
        if (!mPlugin.getBrandingMgr().queueIfNeeded("com.mobicage.capi.messaging.updateGPSLocationForm", request,
            request.message_key)) {

            mPlugin.updateGPSLocationForm(request.parent_message_key, request.message_key, request.result,
                request.button_id, request.received_timestamp, request.acked_timestamp);
        }
        return new UpdateGPSLocationFormResponseTO();
    }

    @Override
    public UpdateMyDigiPassFormResponseTO updateMyDigiPassForm(UpdateMyDigiPassFormRequestTO request) throws Exception {
        T.BIZZ();
        if (!mPlugin.getBrandingMgr().queueIfNeeded("com.mobicage.capi.messaging.updateMyDigiPassForm", request,
            request.message_key)) {

            mPlugin.updateMyDigiPassForm(request.parent_message_key, request.message_key, request.result,
                request.button_id, request.received_timestamp, request.acked_timestamp);
        }
        return new UpdateMyDigiPassFormResponseTO();
    }

    @Override
    public UpdateAdvancedOrderFormResponseTO updateAdvancedOrderForm(UpdateAdvancedOrderFormRequestTO request)
        throws Exception {
        if (!mPlugin.getBrandingMgr().queueIfNeeded("com.mobicage.capi.messaging.updateAdvancedOrderForm", request,
            request.message_key)) {

            mPlugin.updateAdvancedOrderForm(request.parent_message_key, request.message_key, request.result,
                request.button_id, request.received_timestamp, request.acked_timestamp);
        }
        return new UpdateAdvancedOrderFormResponseTO();
    }

    @Override
    public UpdateSignFormResponseTO updateSignForm(UpdateSignFormRequestTO request) throws Exception {
        if (!mPlugin.getBrandingMgr().queueIfNeeded("com.mobicage.capi.messaging.updateSignForm", request,
                request.message_key)) {

            mPlugin.updateForm(request.parent_message_key, request.message_key, request.result, request.button_id,
                    request.received_timestamp, request.acked_timestamp);
        }
        return new UpdateSignFormResponseTO();
    }

    @Override
    public UpdateRatingFormResponseTO updateRatingForm(UpdateRatingFormRequestTO request) throws Exception {
        if (!mPlugin.getBrandingMgr().queueIfNeeded("com.mobicage.capi.messaging.updateRatingForm", request,
                request.message_key)) {

            mPlugin.updateRatingForm(request.parent_message_key, request.message_key, request.result, request.button_id,
                    request.received_timestamp, request.acked_timestamp);
        }
        return new UpdateRatingFormResponseTO();
    }

    @Override
    public ConversationDeletedResponseTO conversationDeleted(ConversationDeletedRequestTO request) throws Exception {
        T.BIZZ();
        mPlugin.conversationDeleted(request.parent_message_key);
        return new ConversationDeletedResponseTO();
    }

    @Override
    public EndMessageFlowResponseTO endMessageFlow(EndMessageFlowRequestTO request) throws Exception {
        mPlugin.endMessageFlow(request.parent_message_key, request.wait_for_followup);
        return new EndMessageFlowResponseTO();
    }

    @Override
    public TransferCompletedResponseTO transferCompleted(com.mobicage.to.messaging.TransferCompletedRequestTO request)
        throws java.lang.Exception {
        mPlugin.setTransferCompleted(request.parent_message_key, request.message_key, request.result_url);
        return new TransferCompletedResponseTO();
    }

    @Override
    public UpdateMessageResponseTO updateMessage(UpdateMessageRequestTO request) throws Exception {
        if (request.message_key == null
            || !mPlugin.getBrandingMgr().queueIfNeeded("com.mobicage.capi.messaging.updateMessage", request,
                request.message_key)) {

            mPlugin.updateMessage(request);
        }

        return new UpdateMessageResponseTO();
    }
}

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

package com.mobicage.rogerthat.plugins.news;

import com.mobicage.rogerthat.MainService;
import com.mobicage.rogerthat.plugins.messaging.Message;
import com.mobicage.rogerthat.plugins.messaging.MessagingPlugin;
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
import com.mobicage.to.news.DisableNewsRequestTO;
import com.mobicage.to.news.DisableNewsResponseTO;

public class NewsCallReceiver implements com.mobicage.capi.news.IClientRpc {

    private final NewsPlugin mPlugin;
    private final MainService mMainService;

    public NewsCallReceiver(final MainService pMainService, final NewsPlugin pPlugin) {
        T.UI();
        mPlugin = pPlugin;
        mMainService = pMainService;
    }

    @Override
    public DisableNewsResponseTO disableNews(DisableNewsRequestTO request) throws Exception {
        T.BIZZ();
        DisableNewsResponseTO response = new DisableNewsResponseTO();
        // todo ruben
        return response;
    }

}

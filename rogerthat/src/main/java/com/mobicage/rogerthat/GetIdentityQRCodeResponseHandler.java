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

package com.mobicage.rogerthat;

import org.jivesoftware.smack.util.Base64;

import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rpc.IResponse;
import com.mobicage.rpc.ResponseHandler;
import com.mobicage.to.system.GetIdentityQRCodeResponseTO;

public class GetIdentityQRCodeResponseHandler extends ResponseHandler<GetIdentityQRCodeResponseTO> {

    @Override
    public void handle(final IResponse<GetIdentityQRCodeResponseTO> response) {
        T.BIZZ();
        try {
            GetIdentityQRCodeResponseTO response_result = response.getResponse();
            mMainService.getIdentityStore().setQR(Base64.decode(response_result.qrcode), response_result.shortUrl);
        } catch (Exception e) {
            L.d(e);
        }
    }
}

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
package com.mobicage.rogerthat.plugins.friends;

import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rpc.IResponse;
import com.mobicage.rpc.ResponseHandler;
import com.mobicage.to.friends.ShareLocationResponseTO;

public class ShareLocationResponseHandler extends ResponseHandler<ShareLocationResponseTO> {

    @Override
    public void handle(final IResponse<ShareLocationResponseTO> response) {
        T.BIZZ();
        try {
            response.getResponse();
        } catch (Exception e) {
            L.d("Share location api call failed", e);
        }
    }
}

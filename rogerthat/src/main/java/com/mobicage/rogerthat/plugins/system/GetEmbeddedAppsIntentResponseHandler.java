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
package com.mobicage.rogerthat.plugins.system;

import android.content.Intent;

import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rpc.IResponse;
import com.mobicage.rpc.ResponseHandler;
import com.mobicage.to.app.GetEmbeddedAppsResponseTO;

import org.json.simple.JSONValue;

public class GetEmbeddedAppsIntentResponseHandler extends ResponseHandler<GetEmbeddedAppsResponseTO> {

    @Override
    public void handle(final IResponse<GetEmbeddedAppsResponseTO> response) {
        T.BIZZ();
        Intent intent = new Intent();
        try {

            try {
                GetEmbeddedAppsResponseTO result = response.getResponse();
                intent.setAction(SystemPlugin.GET_EMBEDDED_APPS_RESULT_INTENT);
                intent.putExtra("json", JSONValue.toJSONString(result.toJSONMap()));
            } catch (Exception e) {
                L.d(e);
                intent.setAction(SystemPlugin.GET_EMBEDDED_APPS_FAILED_INTENT);
                intent.putExtra("error", e.getMessage());
            }
        } catch (Exception e) {
            L.w("GetEmbeddedApps call resulted in failure!", e);
        }
        mMainService.sendBroadcast(intent);
    }

}

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


import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rpc.IResponse;
import com.mobicage.rpc.ResponseHandler;
import com.mobicage.to.app.GetAppAssetResponseTO;

public class GetAppAssetResponseHandler extends ResponseHandler<GetAppAssetResponseTO> {

    @Override
    public void handle(final IResponse<GetAppAssetResponseTO> response) {
        T.BIZZ();
        try {
            final SystemPlugin systemPlugin = mMainService.getPlugin(SystemPlugin.class);
            GetAppAssetResponseTO asset = response.getResponse();
            systemPlugin.updateAppAsset(asset.kind, asset.url, asset.scale_x);
        } catch (Exception e) {
            L.w("GetAppAsset call resulted in failure!", e);
        }
    }
}

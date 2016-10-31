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
            systemPlugin.updateAppAsset(asset.kind, asset.url);
        } catch (Exception e) {
            L.w("GetAppAsset call resulted in failure!", e);
        }
    }
}

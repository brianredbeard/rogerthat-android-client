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

package com.mobicage.rogerthat.plugins.scan;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import android.content.Intent;

import com.mobicage.rogerthat.plugins.friends.FriendsPlugin;
import com.mobicage.rogerthat.plugins.messaging.BrandingMgr;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.pickle.PickleException;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rpc.IResponse;
import com.mobicage.rpc.ResponseHandler;
import com.mobicage.to.service.GetServiceActionInfoResponseTO;

public class GetServiceActionInfoRH extends ResponseHandler<GetServiceActionInfoResponseTO> {

    private String mCode;
    private String mAction;

    public void setCode(String code) {
        this.mCode = code;
    }

    public void setAction(String action) {
        this.mAction = action;
    }

    @Override
    public void writePickle(DataOutput out) throws IOException {
        T.dontCare();
        super.writePickle(out);
        out.writeUTF(mCode);
        out.writeUTF(mAction);
    }

    @Override
    public void readFromPickle(int version, DataInput in) throws IOException, PickleException {
        T.dontCare();
        super.readFromPickle(version, in);
        mCode = in.readUTF();
        mAction = in.readUTF();
    }

    @Override
    public void handle(final IResponse<GetServiceActionInfoResponseTO> response) {
        T.BIZZ();
        try {
            final GetServiceActionInfoResponseTO resp = response.getResponse();

            Intent intent = new Intent(FriendsPlugin.SERVICE_ACTION_INFO_RECEIVED_INTENT);
            intent.putExtra(ProcessScanActivity.EMAILHASH, mCode);
            intent.putExtra(ProcessScanActivity.POKE_ACTION, mAction);
            intent.putExtra(ProcessScanActivity.POKE_DESCRIPTION, resp.actionDescription);
            intent.putExtra(ProcessScanActivity.AVATAR, resp.avatar);
            intent.putExtra(ProcessScanActivity.DESCRIPTION, resp.description);
            intent.putExtra(ProcessScanActivity.DESCRIPTION_BRANDING, resp.descriptionBranding);
            intent.putExtra(ProcessScanActivity.EMAIL, resp.email);
            intent.putExtra(ProcessScanActivity.NAME, resp.name);
            intent.putExtra(ProcessScanActivity.QUALIFIED_IDENTIFIER, resp.qualifiedIdentifier);
            intent.putExtra(ProcessScanActivity.STATIC_FLOW, resp.staticFlow);
            intent.putExtra(ProcessScanActivity.STATIC_FLOW_HASH, resp.staticFlowHash);

            final BrandingMgr brandingMgr = (mMainService.getPlugin(FriendsPlugin.class)).getBrandingMgr();
            for (String branding : resp.staticFlowBrandings)
                brandingMgr.queueGenericBranding(branding);

            if (resp.error == null) {
                intent.putExtra(ProcessScanActivity.SUCCESS, true);
            } else {
                intent.putExtra(ProcessScanActivity.SUCCESS, false);
                intent.putExtra(ProcessScanActivity.ERROR_MESSAGE, resp.error.message);
                intent.putExtra(ProcessScanActivity.ERROR_TITLE, resp.error.title);
                intent.putExtra(ProcessScanActivity.ERROR_CAPTION, resp.error.caption);
                intent.putExtra(ProcessScanActivity.ERROR_ACTION, resp.error.action);
            }

            mMainService.sendBroadcast(intent);

        } catch (Exception e) {
            L.d("GetServiceActionInfo failed", e);
            Intent intent = new Intent(FriendsPlugin.SERVICE_ACTION_INFO_RECEIVED_INTENT);
            intent.putExtra(ProcessScanActivity.EMAILHASH, mCode);
            intent.putExtra(ProcessScanActivity.POKE_ACTION, mAction);
            intent.putExtra(ProcessScanActivity.SUCCESS, false);
            mMainService.sendBroadcast(intent);
        }
    }
}

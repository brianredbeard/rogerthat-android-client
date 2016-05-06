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

package com.mobicage.rogerthat.plugins.trackme;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.pickle.PickleException;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rpc.IResponse;
import com.mobicage.rpc.ResponseHandler;
import com.mobicage.to.location.BeaconDiscoveredResponseTO;

public class BeaconDiscoveredResponseHandler extends ResponseHandler<BeaconDiscoveredResponseTO> {

    private String mUuid;
    private int mMajor;
    private int mMinor;
    private int mProximity;

    public void setUUID(String uuid) {
        mUuid = uuid;
    }

    public void setMajor(int major) {
        mMajor = major;
    }

    public void setMinor(int minor) {
        mMinor = minor;
    }

    public void setProximity(int proximity) {
        mProximity = proximity;
    }

    @Override
    public void writePickle(DataOutput out) throws IOException {
        T.dontCare();
        super.writePickle(out);
        out.writeUTF(mUuid);
        out.writeInt(mMajor);
        out.writeInt(mMinor);
        out.writeInt(mProximity);
    }

    @Override
    public void readFromPickle(int version, DataInput in) throws IOException, PickleException {
        T.dontCare();
        super.readFromPickle(version, in);
        mUuid = in.readUTF();
        mMajor = in.readInt();
        mMinor = in.readInt();
        mProximity = in.readInt();
    }

    @Override
    public void handle(IResponse<BeaconDiscoveredResponseTO> response) {
        T.BIZZ();
        TrackmePlugin trackmePlugin = mMainService.getPlugin(TrackmePlugin.class);
        TrackmeStore trackmeStore = trackmePlugin.getStore();
        String beaconName = TrackmePlugin.getBeaconName(mMajor, mMinor);

        BeaconDiscoveredResponseTO resp;
        try {
            resp = response.getResponse();
        } catch (Exception e) {
            L.d("Beacon discovered failed", e);
            trackmeStore.deleteBeaconDiscovery(mUuid, beaconName);
            return;
        }
        if (resp.friend_email != null) {
            trackmeStore.updateBeaconDiscovery(mUuid, beaconName, resp.friend_email, resp.tag);
        } else {
            L.d("Beacon not coupled yet");
            trackmeStore.deleteBeaconDiscovery(mUuid, beaconName);
        }
    }
}
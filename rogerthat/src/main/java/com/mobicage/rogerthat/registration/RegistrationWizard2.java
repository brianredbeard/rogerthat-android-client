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
package com.mobicage.rogerthat.registration;

import android.Manifest;
import android.content.Intent;

import com.mobicage.rogerthat.MainService;
import com.mobicage.rogerthat.config.Configuration;
import com.mobicage.rogerthat.config.ConfigurationProvider;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.pickle.Pickle;
import com.mobicage.rogerthat.util.pickle.PickleException;
import com.mobicage.rogerthat.util.pickle.Pickler;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.ui.Wizard;
import com.mobicage.rpc.Credentials;
import com.mobicage.rpc.IncompleteMessageException;
import com.mobicage.to.beacon.BeaconRegionTO;
import com.mobicage.to.beacon.GetBeaconRegionsResponseTO;
import com.mobicage.to.location.BeaconDiscoveredRequestTO;

import org.jivesoftware.smack.util.Base64;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.EOFException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class RegistrationWizard2 extends AbstractRegistrationWizard {

    public final static String INTENT_GOT_BEACON_REGIONS = "com.mobicage.rogerthat.registration.gotBeaconReagions";

    public final static String CONFIGKEY = "Registration";
    private final static String CONFIG_PICKLED_WIZARD_KEY = "RegistrationWizard2";

    public static final String REGISTRATION_STEP_AGREED_TOS = "1";
    public static final String REGISTRATION_STEP_FACEBOOK_LOGIN = "2a";
    public static final String REGISTRATION_STEP_EMAIL_LOGIN = "2b";

    private GetBeaconRegionsResponseTO mBeaconRegions = null;
    private Set<String> mDetectedBeacons = null;

    private final static Integer PICKLE_CLASS_VERSION = 3;

    public static RegistrationWizard2 getWizard(final MainService mainService) {
        T.UI();
        final ConfigurationProvider cfgProvider = mainService.getConfigurationProvider();
        RegistrationWizard2 wiz = null;
        final Configuration cfg = cfgProvider.getConfiguration(CONFIGKEY);
        final String serializedWizard = cfg.get(CONFIG_PICKLED_WIZARD_KEY, "");
        if (!"".equals(serializedWizard)) {
            try {
                wiz = (RegistrationWizard2) Pickler.createObjectFromPickle(Base64.decode(serializedWizard));
            } catch (PickleException e) {
                L.bug(e);
            }
        }

        if (wiz == null) {
            wiz = new RegistrationWizard2();
            wiz.init(mainService);
        } else if (wiz.mBeaconRegions == null) {
            wiz.requestBeaconRegions(mainService);
        }

        wiz.setPersister(new Wizard.Persister() {
            @Override
            public void save(Wizard wiz) {
                T.UI();
                String serializedWizard;
                try {
                    serializedWizard = Base64.encodeBytes(Pickler.getPickleFromObject(wiz));
                } catch (PickleException e) {
                    L.bug(e);
                    return;
                }
                Configuration cfg = new Configuration();
                cfg.put(CONFIG_PICKLED_WIZARD_KEY, serializedWizard);
                cfgProvider.updateConfigurationNow(CONFIGKEY, cfg);
            }

            @Override
            public void clear(Wizard wiz) {
                Configuration cfg = new Configuration();
                cfg.put(CONFIG_PICKLED_WIZARD_KEY, "");
                cfgProvider.updateConfigurationNow(CONFIGKEY, cfg);
            }
        });
        return wiz;
    }

    @Override
    public int getPickleClassVersion() {
        return PICKLE_CLASS_VERSION;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void writePickle(DataOutput out) throws IOException {
        T.UI();
        super.writePickle(out);
        boolean set = getCredentials() != null;
        out.writeBoolean(set);
        if (set) {
            out.writeInt(getCredentials().getPickleClassVersion());
            getCredentials().writePickle(out);
        }
        set = getEmail() != null;
        out.writeBoolean(set);
        if (set)
            out.writeUTF(getEmail());
        out.writeLong(getTimestamp());
        out.writeUTF(getRegistrationId());
        out.writeBoolean(getInGoogleAuthenticationProcess());
        out.writeUTF(getInstallationId());
        out.writeUTF(getDeviceId());
        set = mBeaconRegions != null;
        out.writeBoolean(set);
        if (set)
            out.writeUTF(JSONValue.toJSONString(mBeaconRegions.toJSONMap()));
        set = mDetectedBeacons != null;
        out.writeBoolean(set);
        if (set) {
            JSONArray db1 = new JSONArray();
            for (String db : mDetectedBeacons) {
                db1.add(db);
            }
            out.writeUTF(JSONValue.toJSONString(db1));
        }
        set = getDeviceNames() != null;
        out.writeBoolean(set);
        if (set) {
            out.writeUTF(JSONValue.toJSONString(getDeviceNames()));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void readFromPickle(int version, DataInput in) throws IOException, PickleException {
        T.UI();
        super.readFromPickle(version, in);
        boolean set = in.readBoolean();
        if (set)
            setCredentials(new Credentials(new Pickle(in.readInt(), in)));
        set = in.readBoolean();
        setEmail(set ? in.readUTF() : null);
        setTimestamp(in.readLong());
        setRegistrationId(in.readUTF());
        setInGoogleAuthenticationProcess(in.readBoolean());
        setInstallationId(in.readUTF());
        // A version bump was forgotten when serializing mDeviceId, so we need a try/catch
        try {
            setDeviceId(in.readUTF());
        } catch (EOFException e) {
            setDeviceId(null);
        }
        if (version >= 2) {
            try {
                set = in.readBoolean();
                mBeaconRegions = set ? new GetBeaconRegionsResponseTO((Map<String, Object>) JSONValue.parse(in
                    .readUTF())) : null;

                set = in.readBoolean();
                if (set) {
                    String detectedBeacons = in.readUTF();
                    JSONArray db1 = (JSONArray) JSONValue.parse(detectedBeacons);
                    if (db1 != null) {
                        mDetectedBeacons = new HashSet<String>();
                        for (int i = 0; i < db1.size(); i++) {
                            mDetectedBeacons.add((String) db1.get(i));
                        }
                    } else {
                        mDetectedBeacons = null;
                    }
                } else {
                    mDetectedBeacons = null;
                }
            } catch (IncompleteMessageException e) {
                L.bug(e);
            }
        }

        if (version >= 3) {
            set = in.readBoolean();
            if (set) {
                String deviceNames = in.readUTF();
                JSONArray db1 = (JSONArray) JSONValue.parse(deviceNames);
                if (db1 != null) {
                    setDeviceNames(db1);
                }
            }
        }
    }


    public BeaconRegionTO[] getBeaconRegions() {
        if (mBeaconRegions == null)
            return null;
        return mBeaconRegions.regions;
    }

    public Set<BeaconDiscoveredRequestTO> getDetectedBeacons() {
        Set<BeaconDiscoveredRequestTO> detectedBeacons = new HashSet<BeaconDiscoveredRequestTO>();
        if (mDetectedBeacons != null) {
            for (String beacon : mDetectedBeacons) {
                BeaconDiscoveredRequestTO bdr = new BeaconDiscoveredRequestTO();
                String[] b = beacon.split("\\|", 2);
                bdr.uuid = b[0];
                bdr.name = b[1];
                detectedBeacons.add(bdr);
            }
        }
        return detectedBeacons;
    }

    public void addDetectedBeacon(String uuid, int major, int minor) {
        if (mDetectedBeacons == null) {
            mDetectedBeacons = new HashSet<String>();
        }
        mDetectedBeacons.add(getBeaconProximityKey(uuid, major, minor));
    }

    private String getBeaconProximityKey(String uuid, int major, int minor) {
        return uuid + "|" + major + "|" + minor;
    }

    public void init(final MainService mainService) {
        T.UI();
        setInstallationId(UUID.randomUUID().toString());
        reInit();
        requestBeaconRegions(mainService);
   }

    public void requestBeaconRegions(final MainService mainService) {
        sendInstallationId(mainService);
    }

    @Override
    protected void processBeaconRegions(GetBeaconRegionsResponseTO response, MainService mainService) {
        if (!mainService.isPermitted(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            return;
        }
        mBeaconRegions = response;
        mainService.sendBroadcast(new Intent(INTENT_GOT_BEACON_REGIONS));
    }

    public void reInit() {
        T.UI();
        setTimestamp(System.currentTimeMillis() / 1000);
        setRegistrationId(UUID.randomUUID().toString());
        save();
    }
}

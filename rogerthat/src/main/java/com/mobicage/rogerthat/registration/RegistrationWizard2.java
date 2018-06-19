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

import org.jivesoftware.smack.util.Base64;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.EOFException;
import java.io.IOException;
import java.util.UUID;

public class RegistrationWizard2 extends AbstractRegistrationWizard {

    public final static String CONFIGKEY = "Registration";
    private final static String CONFIG_PICKLED_WIZARD_KEY = "RegistrationWizard2";

    public static final String REGISTRATION_STEP_CREATE_ACCOUNT = "1";
    public static final String REGISTRATION_STEP_LOCATION_USAGE = "1a";
    public static final String REGISTRATION_STEP_NOTIFICATION_USAGE = "1b";
    public static final String REGISTRATION_STEP_TOS = "1c";
    public static final String REGISTRATION_STEP_FACEBOOK_LOGIN = "2a";
    public static final String REGISTRATION_STEP_EMAIL_LOGIN = "2b";

    private final static Integer PICKLE_CLASS_VERSION = 4;

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
        set = false; // old beaconRegions
        out.writeBoolean(set);
        set = false; // old detectedBeacons
        out.writeBoolean(set);
        set = getDeviceNames() != null;
        out.writeBoolean(set);
        if (set) {
            out.writeUTF(JSONValue.toJSONString(getDeviceNames()));
        }
        set = getTOSAge() != null;
        out.writeBoolean(set);
        if (set)
            out.writeUTF(getTOSAge());
        set = getPushNotificationEnabled() != null;
        out.writeBoolean(set);
        if (set)
            out.writeUTF(getPushNotificationEnabled());
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
            set = in.readBoolean(); // old beaconRegions
            if (set) {
                in.readUTF();
            }
            set = in.readBoolean(); // old detectedBeacons
            if (set) {
                in.readUTF();
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

        if (version >= 4) {
            set = in.readBoolean();
            setTOSAge(set ? in.readUTF() : null);
            set = in.readBoolean();
            setPushNotificationEnabled(set ? in.readUTF() : null);
        }
    }

    public void init(final MainService mainService) {
        T.UI();
        setInstallationId(UUID.randomUUID().toString());
        reInit();
   }

    public void reInit() {
        T.UI();
        setTimestamp(System.currentTimeMillis() / 1000);
        setRegistrationId(UUID.randomUUID().toString());
        save();
    }
}

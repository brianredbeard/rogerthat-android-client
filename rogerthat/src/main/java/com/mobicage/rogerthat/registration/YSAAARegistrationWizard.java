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
import com.mobicage.rpc.Credentials;

import org.jivesoftware.smack.util.Base64;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.UUID;

public class YSAAARegistrationWizard extends AbstractRegistrationWizard {

    private final static String CONFIG_PICKLED_WIZARD_KEY = "YSAAARegistrationWizard";

    private ConfigurationProvider mCfgProvider;
    private final static Integer PICKLE_CLASS_VERSION = 1;

    public static YSAAARegistrationWizard getWizard(final MainService mainService, final String deviceId) {
        T.UI();
        ConfigurationProvider configProvider = mainService.getConfigurationProvider();
        YSAAARegistrationWizard wiz = null;
        final Configuration cfg = configProvider.getConfiguration(RegistrationWizard2.CONFIGKEY);
        final String serializedWizard = cfg.get(CONFIG_PICKLED_WIZARD_KEY, "");
        if (!"".equals(serializedWizard)) {
            try {
                wiz = (YSAAARegistrationWizard) Pickler.createObjectFromPickle(Base64.decode(serializedWizard));
            } catch (PickleException e) {
                L.bug(e);
            }
        }

        if (wiz == null) {
            wiz = new YSAAARegistrationWizard();
            wiz.setDeviceId(deviceId);
            wiz.setConfigProvider(configProvider);
            wiz.init(mainService);
        } else {
            wiz.setConfigProvider(configProvider);
        }

        return wiz;
    }

    @Override
    public int getPickleClassVersion() {
        return PICKLE_CLASS_VERSION;
    }

    public void save() {
        T.UI();
        String serializedWizard;

        try {
            serializedWizard = Base64.encodeBytes(Pickler.getPickleFromObject(this));
        } catch (PickleException e) {
            L.bug(e);
            return;
        }
        Configuration cfg = new Configuration();
        cfg.put(CONFIG_PICKLED_WIZARD_KEY, serializedWizard);
        mCfgProvider.updateConfigurationNow(RegistrationWizard2.CONFIGKEY, cfg);
    }

    public void clear() {
        T.UI();
        Configuration cfg = new Configuration();
        cfg.put(CONFIG_PICKLED_WIZARD_KEY, "");
        mCfgProvider.updateConfigurationNow(RegistrationWizard2.CONFIGKEY, cfg);
    }

    @Override
    public void writePickle(DataOutput out) throws IOException {
        T.UI();
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
        out.writeUTF(getInstallationId());
        out.writeUTF(getDeviceId());
    }

    @Override
    public void readFromPickle(int version, DataInput in) throws IOException, PickleException {
        T.UI();
        boolean set = in.readBoolean();
        if (set)
            setCredentials(new Credentials(new Pickle(in.readInt(), in)));
        set = in.readBoolean();
        setEmail(set ? in.readUTF() : null);
        setTimestamp(in.readLong());
        setRegistrationId(in.readUTF());
        setInstallationId(in.readUTF());
        setDeviceId(in.readUTF());
    }

    private void setConfigProvider(ConfigurationProvider configProvider) {
        mCfgProvider = configProvider;
    }

    public void init(final MainService mainService) {
        T.UI();
        setInstallationId(UUID.randomUUID().toString());
        sendInstallationId(mainService);
        reInit();
    }

    public void reInit() {
        T.UI();
        setTimestamp(System.currentTimeMillis() / 1000);
        setRegistrationId(UUID.randomUUID().toString());
        save();
    }
}

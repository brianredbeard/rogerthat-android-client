/*
 * Copyright 2017 Mobicage NV
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
 * @@license_version:1.2@@
 */
package com.mobicage.rogerthat.registration;

import com.mobicage.rogerthat.MainService;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.ui.Wizard;
import com.mobicage.rpc.Credentials;
import com.mobicage.to.location.BeaconDiscoveredRequestTO;

import java.util.Set;

public abstract class AbstractRegistrationWizard extends Wizard {

    private Credentials mCredentials = null;
    private String mEmail = null;
    private long mTimestamp = 0;
    private String mRegistrationId = null;
    private boolean mInGoogleAuthenticationProcess = false;
    private String mInstallationId = null;
    private String mDeviceId = null;

    public Credentials getCredentials() {
        T.UI();
        return mCredentials;
    }

    public void setCredentials(final Credentials credentials) {
        T.UI();
        mCredentials = credentials;
    }

    public String getEmail() {
        T.UI();
        return mEmail;
    }

    public void setEmail(final String email) {
        T.UI();
        mEmail = email;
    }

    public long getTimestamp() {
        return mTimestamp;
    }

    public void setTimestamp(long timestamp) {
        mTimestamp = timestamp;
    }

    public String getRegistrationId() {
        return mRegistrationId;
    }

    public void setRegistrationId(String registrationId) {
        mRegistrationId = registrationId;
    }

    public boolean getInGoogleAuthenticationProcess() {
        return mInGoogleAuthenticationProcess;
    }

    public void setInGoogleAuthenticationProcess(boolean inGoogleAuthenticationProcess) {
        mInGoogleAuthenticationProcess = inGoogleAuthenticationProcess;
    }

    public String getInstallationId() {
        return mInstallationId;
    }

    public void setInstallationId(String installationId) {
        mInstallationId = installationId;
    }

    public String getDeviceId() {
        return mDeviceId;
    }

    public void setDeviceId(final String deviceId) {
        T.UI();
        mDeviceId = deviceId;
    }

    public abstract Set<BeaconDiscoveredRequestTO> getDetectedBeacons();

    public abstract void init(final MainService mainService);

    public abstract  void reInit();
}

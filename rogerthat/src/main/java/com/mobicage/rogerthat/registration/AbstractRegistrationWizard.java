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
import com.mobicage.rogerthat.util.http.HTTPUtil;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeAsyncTask;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.ui.Wizard;
import com.mobicage.rpc.Credentials;
import com.mobicage.rpc.config.CloudConstants;
import com.mobicage.to.beacon.GetBeaconRegionsResponseTO;
import com.mobicage.to.location.BeaconDiscoveredRequestTO;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public abstract class AbstractRegistrationWizard extends Wizard {

    private Credentials mCredentials = null;
    private String mEmail = null;
    private long mTimestamp = 0;
    private String mRegistrationId = null;
    private String mInstallationId = null;
    private boolean mInGoogleAuthenticationProcess = false;
    private String mDeviceId = null;
    private JSONArray mDeviceNames = null;

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

    public JSONArray getDeviceNames() {
        T.UI();
        return mDeviceNames;
    }

    public void setDeviceNames(JSONArray deviceNames) {
        T.UI();
        mDeviceNames = deviceNames;
    }

    public abstract void init(final MainService mainService);

    public abstract void reInit();

    protected void sendInstallationId(final MainService mainService) {
        if (mInstallationId == null) {
            throw new IllegalStateException("Installation id should be set!");
        }

        new SafeAsyncTask<Object, Object, Boolean>() {

            @SuppressWarnings("unchecked")
            @Override
            protected Boolean safeDoInBackground(Object... params) {
                try {
                    HttpClient httpClient = HTTPUtil.getHttpClient(10000, 3);
                    final HttpPost httpPost = HTTPUtil.getHttpPost(mainService, CloudConstants.REGISTRATION_REGISTER_INSTALL_URL);
                    List<NameValuePair> formParams = HTTPUtil.getRegistrationFormParams(mainService);
                    formParams.add(new BasicNameValuePair("version", MainService.getVersion(mainService)));
                    formParams.add(new BasicNameValuePair("install_id", getInstallationId()));

                    UrlEncodedFormEntity entity;
                    try {
                        entity = new UrlEncodedFormEntity(formParams, HTTP.UTF_8);
                    } catch (UnsupportedEncodingException e) {
                        L.bug(e);
                        return true;
                    }

                    httpPost.setEntity(entity);
                    L.d("Sending installation id: " + getInstallationId());
                    try {
                        HttpResponse response = httpClient.execute(httpPost);
                        L.d("Installation id sent");
                        int statusCode = response.getStatusLine().getStatusCode();
                        if (statusCode != HttpStatus.SC_OK) {
                            L.e("HTTP request resulted in status code " + statusCode);
                            return false;
                        }
                        HttpEntity httpEntity = response.getEntity();
                        if (httpEntity == null) {
                            L.e("Response of '" + CloudConstants.REGISTRATION_REGISTER_INSTALL_URL + "' was null");
                            return false;
                        }

                        final Map<String, Object> responseMap = (Map<String, Object>) JSONValue
                                .parse(new InputStreamReader(httpEntity.getContent()));
                        if (responseMap == null) {
                            L.e("HTTP request responseMap was null");
                            return false;
                        }

                        if (!"success".equals(responseMap.get("result"))) {
                            L.e("HTTP request result was not 'success' but: " + responseMap.get("result"));
                            return false;
                        }

                        JSONObject beaconRegions = (JSONObject) responseMap.get("beacon_regions");
                        processBeaconRegions(new GetBeaconRegionsResponseTO(beaconRegions), mainService);
                        return true;

                    } catch (ClientProtocolException e) {
                        L.bug(e);
                        return false;
                    } catch (IOException e) {
                        L.bug(e);
                        return false;
                    }
                } catch (Exception e) {
                    L.bug(e);
                    return false;
                }
            }

        }.execute();
    }

    protected void processBeaconRegions(GetBeaconRegionsResponseTO response, MainService mainService) {
        // Can be overridden by registration wizards that are interested in this
    }


}

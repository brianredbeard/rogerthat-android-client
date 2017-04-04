/*
 * Copyright 2017 GIG Technology NV
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
 * @@license_version:1.3@@
 */
package com.mobicage.rogerthat.registration;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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
import org.jivesoftware.smack.util.Base64;
import org.json.simple.JSONValue;

import com.mobicage.rogerthat.MainService;
import com.mobicage.rogerthat.config.Configuration;
import com.mobicage.rogerthat.config.ConfigurationProvider;
import com.mobicage.rogerthat.util.http.HTTPUtil;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.pickle.Pickle;
import com.mobicage.rogerthat.util.pickle.PickleException;
import com.mobicage.rogerthat.util.pickle.Pickleable;
import com.mobicage.rogerthat.util.pickle.Pickler;
import com.mobicage.rogerthat.util.system.SafeAsyncTask;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rpc.Credentials;
import com.mobicage.rpc.config.CloudConstants;
import com.mobicage.to.location.BeaconDiscoveredRequestTO;

public class YSAAARegistrationWizard extends AbstractRegistrationWizard {

    private final static String CONFIG_PICKLED_WIZARD_KEY = "YSAAARegistrationWizard";

    private ConfigurationProvider mCfgProvider;
    private boolean mInstallationIdSent = false;
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
        reInit();
        new SafeAsyncTask<Object, Object, Object>() {

            @SuppressWarnings("unchecked")
            @Override
            protected Object safeDoInBackground(Object... params) {
                try {
                    HttpClient httpClient = HTTPUtil.getHttpClient(10000, 3);
                    final HttpPost httpPost = new HttpPost(CloudConstants.REGISTRATION_REGISTER_INSTALL_URL);
                    httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
                    httpPost.setHeader("User-Agent", MainService.getUserAgent(mainService));
                    List<NameValuePair> formParams = new ArrayList<NameValuePair>();
                    formParams.add(new BasicNameValuePair("version", MainService.getVersion(mainService)));
                    formParams.add(new BasicNameValuePair("install_id", getInstallationId()));
                    formParams.add(new BasicNameValuePair("platform", "android"));
                    formParams.add(new BasicNameValuePair("language", Locale.getDefault().getLanguage()));
                    formParams.add(new BasicNameValuePair("country", Locale.getDefault().getCountry()));
                    formParams.add(new BasicNameValuePair("app_id", CloudConstants.APP_ID));

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
                            L.e("Response of '/unauthenticated/mobi/registration/register_install' was null");
                            return false;
                        }

                        final Map<String, Object> responseMap = (Map<String, Object>) JSONValue
                            .parse(new InputStreamReader(httpEntity.getContent()));
                        if (responseMap == null) {
                            L.e("HTTP request responseMap was null");
                            return false;
                        }

                        if ("success".equals(responseMap.get("result"))) {
                        } else {
                            L.e("HTTP request result was not 'success' but: " + responseMap.get("result"));
                            return false;
                        }
                    } catch (ClientProtocolException e) {
                        L.bug(e);
                        return false;
                    } catch (IOException e) {
                        L.bug(e);
                        return false;
                    }

                    return true;
                } catch (Exception e) {
                    L.bug(e);
                    return false;
                }
            }

            @Override
            protected void safeOnPostExecute(Object result) {
                T.UI();
                Boolean b = (Boolean) result;
                if (mInstallationIdSent) {
                    mInstallationIdSent = b;
                    save();
                }
            }

            @Override
            protected void safeOnCancelled(Object result) {
            }

            @Override
            protected void safeOnProgressUpdate(Object... values) {
            }

            @Override
            protected void safeOnPreExecute() {
            }

        }.execute();
    }

    public void reInit() {
        T.UI();
        setTimestamp(System.currentTimeMillis() / 1000);
        setRegistrationId(UUID.randomUUID().toString());
        save();
    }

    public Set<BeaconDiscoveredRequestTO> getDetectedBeacons() {
        return null;
    }
}

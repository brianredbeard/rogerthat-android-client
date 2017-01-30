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

package com.mobicage.rpc.newxmpp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URLEncoder;
import java.nio.channels.SocketChannel;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.util.DNSUtil.HostAddress;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.os.SystemClock;

import com.mobicage.rogerthat.App;
import com.mobicage.rogerthat.config.Configuration;
import com.mobicage.rogerthat.config.ConfigurationProvider;
import com.mobicage.rogerthat.util.IOUtils;
import com.mobicage.rogerthat.util.TextUtils;
import com.mobicage.rogerthat.util.http.HTTPUtil;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.net.NetworkConnectivityManager;
import com.mobicage.rpc.SDCardLogger;
import com.mobicage.rpc.config.CloudConstants;

public class XMPPConfigurationFactory {

    private final static int XMPP_MAX_GET_CONFIG_MILLIS = 5 * 1000;
    private final static long XMPP_MAX_CONNECT_MILLIS = 30 * 1000;
    private final static long XMPP_MAX_TRY_PREFERRED_PORT_MILLIS = 3 * 1000;
    private final static long XMPP_POLLING_INTERVAL_MILLIS = 250;

    private final static int XMPP_DEFAULT_PORT = 5222;

    private final static String SRV_CONFIG_KEY = "srv_config";

    private final SDCardLogger mLogger;
    private final NetworkConnectivityManager mConnectivityManager;
    private final ConfigurationProvider mConfigurationProvider;

    public XMPPConfigurationFactory(ConfigurationProvider configurationProvider, NetworkConnectivityManager connMgr,
        SDCardLogger logger) {
        mConnectivityManager = connMgr;
        mLogger = logger;
        mConfigurationProvider = configurationProvider;
    }

    private void buglog(String s) {
        if (mLogger != null)
            mLogger.bug(s);
        else
            L.bug(s);
    }

    private void debuglog(String s) {
        if (mLogger != null)
            mLogger.d(s);
        else
            L.d(s);
    }

    // Note: this method does a HTTP call to rogerth.at to get the DNS SRV records and can hang for a while !
    private ConnectionConfiguration getBasicXMPPConfigurationFromConfigProvider(final String xmppServiceName) {
        String result = getSRVConfig();
        L.d("getSRVConfig|" + result + "|");
        return configurationToXMPPConnectionConfiguration(xmppServiceName, result);
    }

    @SuppressLint("DefaultLocale")
    private ConnectionConfiguration configurationToXMPPConnectionConfiguration(final String xmppServiceName,
        String result) {
        // Ignore HTML
        if (TextUtils.isEmptyOrWhitespace(result) || result.toUpperCase().contains("<HTML")) {
            L.d("Received HTML in DNS SRV call. Ignoring...");
            return null;
        }

        try {
            // Using this constructor because it skips DNSSRV resolving
            final ConnectionConfiguration config = new ConnectionConfiguration(null, 0, xmppServiceName);
            JSONArray jArray = new JSONArray(result);
            HostAddress[] hosts = new HostAddress[jArray.length()];
            for (int i = 0; i < jArray.length(); i++) {
                JSONObject jo = jArray.getJSONObject(i);
                hosts[i] = new HostAddress(jo.getString("ip"), jo.getInt("port"));
            }
            if (hosts.length == 0) {
                debuglog("Parsed empty configuration");
                return null;
            }

            config.setHosts(hosts);
            pimpXMPPConfig(config);
            return config;
        } catch (JSONException e1) {
            L.bug(e1);
            return null;
        }
    }

    private ConnectionConfiguration getBasicXMPPConfigurationFromCloud(final String xmppServiceName, final String email) {
        HttpClient httpClient = HTTPUtil.getHttpClient(XMPP_MAX_GET_CONFIG_MILLIS, 1);
        HttpGet httpGet;
        try {
            httpGet = new HttpGet(CloudConstants.HTTPS_LOAD_SRV_RECORDS + URLEncoder.encode(email, "UTF-8"));
        } catch (UnsupportedEncodingException e1) {
            L.bug(e1);
            httpGet = new HttpGet(CloudConstants.HTTPS_LOAD_SRV_RECORDS);
        }
        InputStream inputStream = null;
        try {
            L.d("before http get src records");
            HttpResponse response = httpClient.execute(httpGet);
            L.d("after http get");

            final int responseCode = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            if (responseCode != HttpStatus.SC_OK || entity == null) {
                throw new IOException("HTTP request resulted in status code " + responseCode);
            }

            inputStream = entity.getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 100);
            StringBuilder sb = new StringBuilder();

            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            String result = sb.toString().replaceAll("\\s+", "");

            ConnectionConfiguration config = configurationToXMPPConnectionConfiguration(xmppServiceName, result);
            if (config != null)
                updateSRVConfig(result);
            return config;
        } catch (ClientProtocolException e) {
            L.e(e);
        } catch (IOException e) {
            L.e(e);
        } finally {
            try {
                if (inputStream != null)
                    inputStream.close();
            } catch (Exception e) {
                L.e(e);
            }
        }
        return null;

    }

    private boolean copyAsset(String fromAssetPath, String toPath) {
        try {
            InputStream in = App.getContext().getAssets().open(fromAssetPath);
            try {
                final File dest = new File(toPath);
                dest.createNewFile();
                OutputStream out = new FileOutputStream(dest);
                try {
                    IOUtils.copy(in, out, 1024);
                    out.flush();
                } finally {
                    out.close();
                }
            } finally {
                in.close();
            }
            return true;
        } catch (Exception e) {
            L.e(e);
            return false;
        }
    }

    private void pimpXMPPConfig(final ConnectionConfiguration config) {
        XMPPConnection.DEBUG_ENABLED = CloudConstants.XMPP_DEBUG;

        if (CloudConstants.USE_TRUSTSTORE) {
            config.setVerifyChainEnabled(true);
            config.setSelfSignedCertificateEnabled(true);
            config.setVerifyRootCAEnabled(true);
            config.setNotMatchingDomainCheckEnabled(true);
            config.setExpiredCertificatesCheckEnabled(false);

            File f1 = new File(App.getContext().getCacheDir() + "/truststore.bks");
            if (!f1.exists()) {
                copyAsset("truststore.bks", f1.getPath());
            }
            config.setSecurityMode(SecurityMode.required);

            config.setTruststoreType("BKS");
            config.setTruststorePassword("rogerthat");
            config.setTruststorePath(f1.getPath());
        } else {
            if (CloudConstants.XMPP_MUST_VALIDATE_SSL_CERTIFICATE) {
                config.setVerifyChainEnabled(true);
                config.setSelfSignedCertificateEnabled(false);
                config.setVerifyRootCAEnabled(true);
                config.setNotMatchingDomainCheckEnabled(true);
                config.setExpiredCertificatesCheckEnabled(true);

                config.setSecurityMode(SecurityMode.required);
            } else {
                L.w("XMPP SSL checks are disabled. NEVER USE THIS IN PRODUCTION !");

                config.setVerifyChainEnabled(false);
                config.setSelfSignedCertificateEnabled(true);
                config.setVerifyRootCAEnabled(false);
                config.setNotMatchingDomainCheckEnabled(false);
                config.setExpiredCertificatesCheckEnabled(false);

                config.setSecurityMode(SecurityMode.enabled);
            }

            final String trustStorePath = System.getProperty("javax.net.ssl.trustStore");
            config.setTruststorePath(trustStorePath);
            config.setTruststoreType(KeyStore.getDefaultType());
        }

        config.setSendPresence(true);
        config.setRosterLoadedAtLogin(false);
        config.setReconnectionAllowed(true);
    }

    public ConnectionConfiguration getSafeXmppConnectionConfiguration(final String xmppServiceName)
        throws XMPPConfigurationException {
        return getSafeXmppConnectionConfiguration(xmppServiceName, "", false);
    }

    public ConnectionConfiguration getSafeXmppConnectionConfiguration(final String xmppServiceName, final String email,
        final boolean hasFailed) throws XMPPConfigurationException {

        debuglog("Creating new XMPP connection");

        if (!mConnectivityManager.isConnected()) {
            debuglog("No network.");
            throw new XMPPConfigurationException("No network.");
        }

        final ConnectionConfiguration xmppConfig;
        if (hasFailed) {
            debuglog("Getting config from cloud because previous connection attempt failed.");
            xmppConfig = getBasicXMPPConfigurationFromCloud(xmppServiceName, email);
        } else {
            debuglog("Getting config from config provider.");
            ConnectionConfiguration tmpXmppConfig = getBasicXMPPConfigurationFromConfigProvider(xmppServiceName);
            if (tmpXmppConfig == null) {
                debuglog("Getting config from cloud because config provider is empty.");
                xmppConfig = getBasicXMPPConfigurationFromCloud(xmppServiceName, email);
            } else
                xmppConfig = tmpXmppConfig;
        }

        if (xmppConfig == null) {
            debuglog("No xmpp configuration found.");
            throw new XMPPConfigurationException("No xmpp configuration found.");
        }

        final HostAddress[] hostAddresses = xmppConfig.getHosts();
        if (hostAddresses.length == 0) {
            debuglog("Error: did not receive any XMPP DNS SRV record");
            throw new XMPPConfigurationException("Did not find any XMPP DNS SRV record");
        } else if (hostAddresses.length == 1 && xmppServiceName.equals(hostAddresses[0].getHost())
            && XMPP_DEFAULT_PORT == hostAddresses[0].getPort()) {
            buglog("Using fallback value for DNS SRV (but network is up): " + hostAddresses[0]);
        }

        debuglog("Found XMPP DNS SRV records:");
        for (int i = hostAddresses.length - 1; i >= 0; i--) {
            debuglog("- host = " + hostAddresses[i]);
        }

        final int preferredXMPPPort = hostAddresses[hostAddresses.length - 1].getPort();
        debuglog("Preferred XMPP port is " + preferredXMPPPort);

        // Do non-blocking TCP connect attempts
        Map<HostAddress, SocketChannel> allChannels = new HashMap<HostAddress, SocketChannel>();
        Map<HostAddress, SocketChannel> remainingChannels = new HashMap<HostAddress, SocketChannel>();
        for (int i = hostAddresses.length - 1; i >= 0; i--) {
            final HostAddress ha = hostAddresses[i];
            try {
                SocketChannel sChannel = SocketChannel.open();
                allChannels.put(ha, sChannel);
                sChannel.configureBlocking(false);
                sChannel.connect(new InetSocketAddress(ha.getHost(), ha.getPort()));
                remainingChannels.put(ha, sChannel);
            } catch (IOException e) {
                // Cannot connect to one socket ; let's not drop others
                debuglog("Ignoring socket due to connection error: " + ha);
            }
        }

        if (remainingChannels.size() == 0) {
            debuglog("Error: could not connect to any of the XMPP DNS SRV records");
            debuglog("Closing attempted TCP sockets");
            for (SocketChannel sc : allChannels.values()) {
                try {
                    sc.close();
                } catch (IOException e) {
                }
            }
            debuglog("All attempted TCP sockets are closed now");
            throw new XMPPConfigurationException("Error: could not connect to any of the XMPP DNS SRV records");
        }

        final long starttime = SystemClock.elapsedRealtime();

        HostAddress goodHostAddress = null;
        while (true) {

            Iterator<Entry<HostAddress, SocketChannel>> iter = remainingChannels.entrySet().iterator();
            while (iter.hasNext()) {
                Entry<HostAddress, SocketChannel> e = iter.next();
                final HostAddress ha = e.getKey();
                final SocketChannel sc = e.getValue();
                try {
                    if (sc.finishConnect()) {
                        if (sc.isConnected()) {
                            debuglog("Successful TCP connection to " + ha);
                            iter.remove();
                            if (goodHostAddress != null) {
                                // We already found a host
                                // Pick this better one only if the one we found already was not using the
                                // preferred XMPP port, and the new one does have the preferred XMPP port
                                if (goodHostAddress.getPort() != preferredXMPPPort && ha.getPort() == preferredXMPPPort) {
                                    goodHostAddress = ha;
                                    debuglog("Found better host " + goodHostAddress);
                                } else {
                                    debuglog("Old host was better: " + goodHostAddress);
                                }
                            } else {
                                goodHostAddress = ha;
                                debuglog("Selecting host " + goodHostAddress);
                            }
                        } else {
                            debuglog("Failed TCP connection to " + ha);
                            iter.remove();
                        }
                    }
                } catch (IOException ex) {
                    // Error during finishConnect()
                    debuglog("TCP connection timeout to " + ha);
                    iter.remove();
                }
            }

            final long now = SystemClock.elapsedRealtime();
            if (goodHostAddress != null && goodHostAddress.getPort() == preferredXMPPPort) {
                debuglog("Found responsive XMPP host with preferred port " + preferredXMPPPort);
                break;
            }
            if (remainingChannels.size() == 0) {
                debuglog("No more XMPP hosts to check");
                break;
            }
            if (now > starttime + XMPP_MAX_CONNECT_MILLIS) {
                debuglog("Timeout trying to find responsive XMPP host");
                break;
            }
            if (goodHostAddress != null) {
                if (now > starttime + XMPP_MAX_TRY_PREFERRED_PORT_MILLIS) {
                    // XXX: would be better to wait at most N seconds (e.g. 2) AFTER the first successful connection
                    // happened (to a non preferred port)
                    debuglog("Give up looking for responsive XMPP host with preferred port.");
                    break;
                }
                boolean stillWaitingForConnectionWithPreferredPort = false;
                for (HostAddress ha : remainingChannels.keySet()) {
                    if (ha.getPort() == preferredXMPPPort) {
                        stillWaitingForConnectionWithPreferredPort = true;
                        break;
                    }
                }
                if (!stillWaitingForConnectionWithPreferredPort) {
                    debuglog("No more responsive XMPP hosts with preferred port to wait for.");
                    break;
                }
            }

            debuglog("Sleeping " + XMPP_POLLING_INTERVAL_MILLIS + "ms while trying to connect to XMPP");
            try {
                Thread.sleep(XMPP_POLLING_INTERVAL_MILLIS);
            } catch (InterruptedException ex) {
                throw new XMPPConfigurationException("Interrupt during Thread.sleep()");
            }
        }

        debuglog("Closing attempted TCP sockets");
        for (SocketChannel sc : allChannels.values()) {
            try {
                sc.close();
            } catch (IOException e) {
            }
        }
        debuglog("All attempted TCP sockets are closed now");

        if (goodHostAddress == null) {
            debuglog("Did not find a good host address and hasfailed: " + hasFailed);
            clearSRVConfig();
            if (hasFailed)
                throw new XMPPConfigurationException("Could not connect to any of the XMPP targets");
            else
                return getSafeXmppConnectionConfiguration(xmppServiceName, email, true);
        }

        debuglog("Using XMPP host " + goodHostAddress);
        xmppConfig.setHosts(new HostAddress[] { goodHostAddress });
        return xmppConfig;
    }

    private void clearSRVConfig() {
        Configuration cfg = mConfigurationProvider.getConfiguration(SRV_CONFIG_KEY);
        if (cfg != null) {
            cfg.put(SRV_CONFIG_KEY, "");
            mConfigurationProvider.updateConfigurationNow(SRV_CONFIG_KEY, cfg);
        }
    }

    private void updateSRVConfig(final String srvConfig) {
        if (srvConfig != null) {
            Configuration cfg = new Configuration();
            cfg.put(SRV_CONFIG_KEY, srvConfig);
            mConfigurationProvider.updateConfigurationNow(SRV_CONFIG_KEY, cfg);
        }
    }

    private String getSRVConfig() {
        Configuration cfg = mConfigurationProvider.getConfiguration(SRV_CONFIG_KEY);
        return cfg.get(SRV_CONFIG_KEY, null);
    }
}

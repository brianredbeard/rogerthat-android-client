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

package com.mobicage.rogerthat.plugins.news;

import android.os.SystemClock;

import com.mobicage.rogerthat.config.Configuration;
import com.mobicage.rogerthat.config.ConfigurationProvider;
import com.mobicage.rogerthat.util.TextUtils;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.net.NetworkConnectivityManager;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rpc.SDCardLogger;
import com.mobicage.rpc.config.CloudConstants;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.util.DNSUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class NewsConfigurationFactory {

    private final static long NEWS_MAX_CONNECT_MILLIS = 30 * 1000;
    private final static long News_MAX_TRY_PREFERRED_PORT_MILLIS = 3 * 1000;
    private final static long NEWS_POLLING_INTERVAL_MILLIS = 2000;

    private final static String NEWS_CONFIG_KEY = "com.mobicage.rogerthat.plugins.news.config";

    private final SDCardLogger mLogger;
    private final NetworkConnectivityManager mConnectivityManager;
    private final ConfigurationProvider mConfigurationProvider;

    public NewsConfigurationFactory(ConfigurationProvider configurationProvider, NetworkConnectivityManager connMgr,
                                    SDCardLogger logger) {
        mConnectivityManager = connMgr;
        mLogger = logger;
        mConfigurationProvider = configurationProvider;
    }


    private void debuglog(String s) {
        if (mLogger != null)
            mLogger.d(s);
        else
            L.d(s);
    }

    private ConnectionConfiguration getBasicNewsConfigurationFromConfigProvider() {
        String result = getNewsConfig();
        L.d("getNewsConfig|" + result + "|");
        return configurationToNewsConnectionConfiguration(result);
    }

    private ConnectionConfiguration configurationToNewsConnectionConfiguration(String result) {
        // Ignore HTML
        if (TextUtils.isEmptyOrWhitespace(result) || result.toUpperCase().contains("<HTML")) {
            debuglog("Received HTML in News config call. Ignoring...");
            return null;
        }

        try {
            final ConnectionConfiguration config = new ConnectionConfiguration(null, 0);
            JSONArray jArray = new JSONArray(result);
            DNSUtil.HostAddress[] hosts = new DNSUtil.HostAddress[jArray.length()];
            for (int i = 0; i < jArray.length(); i++) {
                JSONObject jo = jArray.getJSONObject(i);
                hosts[i] = new DNSUtil.HostAddress(jo.getString("ip"), jo.getInt("port"));
            }
            if (hosts.length == 0) {
                debuglog("Parsed empty configuration");
                return null;
            }

            config.setHosts(hosts);
            return config;
        } catch (JSONException e1) {
            L.bug(e1);
            return null;
        }
    }

    private ConnectionConfiguration getBasicNewsConfigurationFromCloud() throws NewsConfigurationConnectionException {
        BufferedReader reader = null;
        try {
            URL url = new URL(CloudConstants.HTTPS_LOAD_NEWS_CONFIG);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            final int responseCode = con.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new NewsConfigurationConnectionException("HTTP request resulted in status code " + responseCode);
            }

            reader = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"), 100);
            StringBuilder sb = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            String result = sb.toString().replaceAll("\\s+", "");

            ConnectionConfiguration config = configurationToNewsConnectionConfiguration(result);
            if (config != null)
                updateNewsConfig(result);
            return config;
        } catch (IOException e) {
            L.e(e);
            throw new NewsConfigurationConnectionException(e.getMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    L.bug(e);
                }
            }
        }
    }

    public DNSUtil.HostAddress getSafeNewsConnectionHost(final boolean hasFailed) throws NewsConfigurationException, NewsConfigurationConnectionException {
        T.NEWS();
        debuglog("Creating new News connection");

        if (!mConnectivityManager.isConnected()) {
            debuglog("No network.");
            throw new NewsConfigurationConnectionException("No network.");
        }

        final ConnectionConfiguration newsConfig;
        if (hasFailed) {
            debuglog("Getting config from cloud because previous connection attempt failed.");
            newsConfig = getBasicNewsConfigurationFromCloud();
        } else {
            debuglog("Getting config from config provider.");
            ConnectionConfiguration tmpNewsConfig = getBasicNewsConfigurationFromConfigProvider();
            if (tmpNewsConfig == null) {
                debuglog("Getting config from cloud because config provider is empty.");
                newsConfig = getBasicNewsConfigurationFromCloud();
            } else
                newsConfig = tmpNewsConfig;
        }

        if (newsConfig == null) {
            debuglog("No News configuration found.");
            throw new NewsConfigurationException("No News configuration found.");
        }

        final DNSUtil.HostAddress[] hostAddresses = newsConfig.getHosts();
        if (hostAddresses.length == 0) {
            debuglog("Error: did not receive any News config record");
            throw new NewsConfigurationException("Did not find any News config record");
        }

        debuglog("Found News config records:");
        for (int i = hostAddresses.length - 1; i >= 0; i--) {
            debuglog("- host = " + hostAddresses[i]);
        }

        final int preferredNewsPort = hostAddresses[hostAddresses.length - 1].getPort();
        debuglog("Preferred News port is " + preferredNewsPort);

        // Do non-blocking TCP connect attempts
        Map<DNSUtil.HostAddress, SocketChannel> allChannels = new HashMap<>();
        Map<DNSUtil.HostAddress, SocketChannel> remainingChannels = new HashMap<>();
        for (int i = hostAddresses.length - 1; i >= 0; i--) {
            final DNSUtil.HostAddress ha = hostAddresses[i];
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
            debuglog("Error: could not connect to any of the News config records");
            debuglog("Closing attempted TCP sockets");
            for (SocketChannel sc : allChannels.values()) {
                try {
                    sc.close();
                } catch (IOException ignored) {
                }
            }
            debuglog("All attempted TCP sockets are closed now");
            throw new NewsConfigurationConnectionException("Error: could not connect to any of the News config records");
        }

        final long startTime = SystemClock.elapsedRealtime();

        DNSUtil.HostAddress goodHostAddress = null;
        while (true) {

            Iterator<Map.Entry<DNSUtil.HostAddress, SocketChannel>> iter = remainingChannels.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<DNSUtil.HostAddress, SocketChannel> e = iter.next();
                final DNSUtil.HostAddress hostAddress = e.getKey();
                final SocketChannel sc = e.getValue();
                try {
                    if (sc.finishConnect()) {
                        if (sc.isConnected()) {
                            debuglog("Successful TCP connection to " + hostAddress);
                            iter.remove();
                            if (goodHostAddress != null) {
                                // We already found a host
                                // Pick this better one only if the one we found already was not using the
                                // preferred News port, and the new one does have the preferred News port
                                if (goodHostAddress.getPort() != preferredNewsPort && hostAddress.getPort() == preferredNewsPort) {
                                    goodHostAddress = hostAddress;
                                    debuglog("Found better host " + goodHostAddress);
                                } else {
                                    debuglog("Old host was better: " + goodHostAddress);
                                }
                            } else {
                                goodHostAddress = hostAddress;
                                debuglog("Selecting host " + goodHostAddress);
                            }
                        } else {
                            debuglog("Failed TCP connection to " + hostAddress);
                            iter.remove();
                        }
                    }
                } catch (IOException ex) {
                    // Error during finishConnect()
                    debuglog("TCP connection timeout to " + hostAddress);
                    iter.remove();
                }
            }

            final long now = SystemClock.elapsedRealtime();
            if (goodHostAddress != null && goodHostAddress.getPort() == preferredNewsPort) {
                debuglog("Found responsive News host with preferred port " + preferredNewsPort);
                break;
            }
            if (remainingChannels.size() == 0) {
                debuglog("No more News hosts to check");
                break;
            }
            if (now > startTime + NEWS_MAX_CONNECT_MILLIS) {
                debuglog("Timeout trying to find responsive News host");
                break;
            }
            if (goodHostAddress != null) {
                if (now > startTime + News_MAX_TRY_PREFERRED_PORT_MILLIS) {
                    debuglog("Give up looking for responsive News host with preferred port.");
                    break;
                }
                boolean stillWaitingForConnectionWithPreferredPort = false;
                for (DNSUtil.HostAddress ha : remainingChannels.keySet()) {
                    if (ha.getPort() == preferredNewsPort) {
                        stillWaitingForConnectionWithPreferredPort = true;
                        break;
                    }
                }
                if (!stillWaitingForConnectionWithPreferredPort) {
                    debuglog("No more responsive News hosts with preferred port to wait for.");
                    break;
                }
            }

            debuglog("Sleeping " + NEWS_POLLING_INTERVAL_MILLIS + "ms while trying to connect to News channel");
            try {
                Thread.sleep(NEWS_POLLING_INTERVAL_MILLIS);
            } catch (InterruptedException ex) {
                throw new NewsConfigurationException("Interrupt during Thread.sleep()");
            }
        }

        debuglog("Closing attempted TCP sockets");
        for (SocketChannel sc : allChannels.values()) {
            try {
                sc.close();
            } catch (IOException ignored) {
            }
        }
        debuglog("All attempted TCP sockets are closed now");

        if (goodHostAddress == null) {
            debuglog("Did not find a good host address and hasfailed: " + hasFailed);
            clearNewsConfig();
            if (hasFailed)
                throw new NewsConfigurationConnectionException("Could not connect to any of the News targets");
            else
                return getSafeNewsConnectionHost(true);
        }

        debuglog("Using News host " + goodHostAddress);
        newsConfig.setHosts(new DNSUtil.HostAddress[]{goodHostAddress});
        return newsConfig.getHosts()[0];
    }

    private void clearNewsConfig() {
        Configuration cfg = mConfigurationProvider.getConfiguration(NEWS_CONFIG_KEY);
        if (cfg != null) {
            cfg.put(NEWS_CONFIG_KEY, "");
            mConfigurationProvider.updateConfigurationNow(NEWS_CONFIG_KEY, cfg);
        }
    }

    private void updateNewsConfig(final String newsConfig) {
        if (newsConfig != null) {
            Configuration cfg = new Configuration();
            cfg.put(NEWS_CONFIG_KEY, newsConfig);
            mConfigurationProvider.updateConfigurationNow(NEWS_CONFIG_KEY, cfg);
        }
    }

    private String getNewsConfig() {
        Configuration cfg = mConfigurationProvider.getConfiguration(NEWS_CONFIG_KEY);
        return cfg.get(NEWS_CONFIG_KEY, null);
    }
}

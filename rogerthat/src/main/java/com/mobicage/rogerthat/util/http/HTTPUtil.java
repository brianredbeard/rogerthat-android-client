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
package com.mobicage.rogerthat.util.http;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;

import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

import com.mobicage.rogerthat.App;
import com.mobicage.rpc.config.CloudConstants;

public class HTTPUtil {

    private static KeyStore sTrustStore = null;

    public static HttpClient getHttpClient() {
        return getHttpClient(35000, 0);
    }

    public static HttpClient getHttpClient(int timeout, int retryCount) {
        return getHttpClient(timeout, timeout, retryCount);
    }

    public static HttpClient getHttpClient(int connectionTimeout, int socketTimeout, final int retryCount) {
        final HttpParams params = new BasicHttpParams();

        HttpConnectionParams.setStaleCheckingEnabled(params, true);
        HttpConnectionParams.setConnectionTimeout(params, connectionTimeout);
        HttpConnectionParams.setSoTimeout(params, socketTimeout);

        HttpClientParams.setRedirecting(params, false);

        final DefaultHttpClient httpClient = new DefaultHttpClient(params);

        if (shouldUseTruststore()) {
            KeyStore trustStore = loadTrustStore();

            SSLSocketFactory socketFactory;
            try {
                socketFactory = new SSLSocketFactory(null, null, trustStore);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            socketFactory.setHostnameVerifier(SSLSocketFactory.STRICT_HOSTNAME_VERIFIER);

            Scheme sch = new Scheme("https", socketFactory, CloudConstants.HTTPS_PORT);
            httpClient.getConnectionManager().getSchemeRegistry().register(sch);
        }

        if (retryCount > 0) {
            httpClient.setHttpRequestRetryHandler(new HttpRequestRetryHandler() {
                @Override
                public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
                    return executionCount < retryCount;
                }
            });
        }
        return httpClient;
    }

    @SuppressWarnings("unused")
    private static boolean shouldUseTruststore() {
        return CloudConstants.USE_TRUSTSTORE && CloudConstants.HTTPS_BASE_URL.startsWith("https://");
    }

    private static KeyStore loadTrustStore() {
        if (sTrustStore == null) {
            String keyStorePassword = "rogerthat";
            try {
                final KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                InputStream instream = App.getContext().getAssets().open("truststore.bks");
                try {
                    keyStore.load(instream, keyStorePassword.toCharArray());
                } finally {
                    instream.close();
                }
                sTrustStore = keyStore;
            } catch (Exception e) {
                throw new RuntimeException("Could not load keyStore from assets dir", e);
            }
        }
        return sTrustStore;
    }
}

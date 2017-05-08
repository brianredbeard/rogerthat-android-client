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

package com.mobicage.rogerthat.plugins.scan;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.CoreProtocolPNames;

import android.content.Intent;

import com.mobicage.rogerthat.MainService;
import com.mobicage.rogerthat.plugins.friends.EmailHashCalculator;
import com.mobicage.rogerthat.util.GoogleServicesUtils;
import com.mobicage.rogerthat.util.http.HTTPUtil;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.system.T;

public class ScanCommunication {

    private final static int HTTP_MOVED_PERMANENTLY = 301;
    private final static int HTTP_MOVED_TEMPORARILY = 302;
    private final static String USER_AGENT = "Rogerthat Android";

    private final MainService mMainService;

    private HttpGet mHttpGetRequest;

    public ScanCommunication(MainService service) {
        mMainService = service;
    }

    public void abort() {
        mMainService.postOnBIZZHandler(new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                T.BIZZ();
                if (mHttpGetRequest != null)
                    mHttpGetRequest.abort();
            }
        });
    }

    private Header getUserAgentHeader() {
        final String userAgentHeaderValue = USER_AGENT + " (" + GoogleServicesUtils.getAppVersion(mMainService) + ")";
        return new BasicHeader(CoreProtocolPNames.USER_AGENT, userAgentHeaderValue);
    }

    public void resolveUrl(final String rawUrl) {
        // Connect to HTTPS://ROGERTH.AT/S/ABCDE
        // Retrieve redirect address

        mMainService.postOnBIZZHandler(new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                T.BIZZ();

                abort();

                final Intent resultIntent = new Intent(ProcessScanActivity.URL_REDIRECTION_DONE);
                resultIntent.putExtra(ProcessScanActivity.RAWURL, rawUrl);

                try {
                    mHttpGetRequest = new HttpGet(rawUrl);
                    mHttpGetRequest.setHeader(getUserAgentHeader());

                    final HttpResponse response = HTTPUtil.getHttpClient().execute(mHttpGetRequest);
                    final int responseStatusCode = response.getStatusLine().getStatusCode();

                    if (responseStatusCode == HTTP_MOVED_PERMANENTLY || responseStatusCode == HTTP_MOVED_TEMPORARILY) {
                        final Header locationHeader = response.getLastHeader("Location");
                        if (locationHeader != null) {
                            final String fullUrl = locationHeader.getValue();
                            if (fullUrl.toLowerCase().startsWith(ProcessScanActivity.SCAN_FRIEND_INVITE_PREFIX)) {
                                final int qsIndex = fullUrl.indexOf('?');
                                final String emailHash;
                                if (qsIndex == -1)
                                    emailHash = fullUrl.substring(ProcessScanActivity.SCAN_FRIEND_INVITE_PREFIX
                                        .length());
                                else
                                    emailHash = fullUrl.substring(
                                        ProcessScanActivity.SCAN_FRIEND_INVITE_PREFIX.length(), qsIndex);

                                if (emailHash.length() == EmailHashCalculator.HASH_LENGTH) {
                                    resultIntent.putExtra(ProcessScanActivity.EMAILHASH, emailHash);
                                } else {
                                    L.bug("Error - unexpected scan redirect to " + fullUrl);
                                }
                            } else if (fullUrl.toLowerCase().startsWith(ProcessScanActivity.SCAN_SERVICE_ACTION_PREFIX)) {
                                final String suffix = fullUrl.substring(ProcessScanActivity.SCAN_SERVICE_ACTION_PREFIX
                                    .length());

                                String[] splitted = suffix.split("/");
                                if (splitted.length != 2) {
                                    throw new Exception("Can not parse result of ServiceActionInfo scan: " + fullUrl);
                                }
                                resultIntent.putExtra(ProcessScanActivity.EMAILHASH, splitted[0]);
                                resultIntent.putExtra(ProcessScanActivity.POKE_ACTION, splitted[1]);
                            }
                        }
                    }
                } catch (Exception e) {
                    L.d(e);
                } finally {
                    mMainService.sendBroadcast(resultIntent);
                }

            }
        });
    }
}

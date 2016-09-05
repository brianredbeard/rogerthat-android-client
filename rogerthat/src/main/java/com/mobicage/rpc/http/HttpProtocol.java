/*
 * Copyright 2016 Mobicage NV
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
 * @@license_version:1.1@@
 */

package com.mobicage.rpc.http;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.HttpClient;
import org.json.JSONObject;

import android.database.sqlite.SQLiteConstraintException;

import com.mobicage.rogerthat.MainService;
import com.mobicage.rogerthat.config.Configuration;
import com.mobicage.rogerthat.config.ConfigurationProvider;
import com.mobicage.rogerthat.util.http.HTTPUtil;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.pickle.PickleException;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rpc.CallReceiver;
import com.mobicage.rpc.IJSONable;
import com.mobicage.rpc.IResponseHandler;
import com.mobicage.rpc.ResponseReceiverHandler;
import com.mobicage.rpc.RpcCall;
import com.mobicage.rpc.RpcResult;
import com.mobicage.rpc.SDCardLogger;

public class HttpProtocol {

    public class ProtocolDetails {
        public long serverTime;
        public boolean more;
    }

    private static final long SECOND = 1000;
    private static final long HOUR = 3600 * 1000;
    private static final long DAY = 24 * HOUR;

    public static final long MESSAGE_LINGER_INTERVAL = 20 * DAY;
    public static final long MESSAGE_ALLOWED_FUTURE_TIME_INTERVAL = DAY;
    public static final long PACKET_GRACE_TIME = 30 * SECOND;
    public static final long MESSAGE_RETENTION_INTERVAL = DAY + MESSAGE_LINGER_INTERVAL;
    public static final long DUPLICATE_AVOIDANCE_RETENTION_INTERVAL = DAY;

    public static final String AP_CONFIG_KEY = "ap";

    private String mAlternativeUrl;

    private final List<String> mResponsIDsToSend;
    private final List<String> mAckIDsToSend;
    private final HttpBacklog mBacklog;
    private final MainService mMainService;
    private final ConfigurationProvider mCfgProvider;
    private final SDCardLogger mLogger;

    // Owned by HTTP thread (= NetworkHandler in HttpCommunicator)
    private HttpClient mHttpClient;

    public HttpProtocol(MainService mainService, HttpBacklog backlog, ConfigurationProvider cfgProvider,
        SDCardLogger logger) {
        T.BIZZ();
        mMainService = mainService;
        mBacklog = backlog;
        mCfgProvider = cfgProvider;
        mLogger = logger;
        mResponsIDsToSend = new ArrayList<String>();
        mAckIDsToSend = new ArrayList<String>();

        Configuration cfg = cfgProvider.getConfiguration(AP_CONFIG_KEY);
        if (cfg != null) {
            String alt = cfg.get(AP_CONFIG_KEY, null);
            if (alt != null && alt.length() != 0) {
                mAlternativeUrl = alt;
            }
        }
    }

    public List<String> getResponseIDsToSend() {
        T.BIZZ();
        return mResponsIDsToSend;
    }

    public List<String> getAckIDsToSend() {
        T.BIZZ();
        return mAckIDsToSend;
    }

    public void addResponseId(String callid) {
        T.BIZZ();
        mResponsIDsToSend.add(callid);
    }

    public void addAckId(String callid) {
        T.BIZZ();
        mAckIDsToSend.add(callid);
    }

    public String getAlternativeUrl() {
        return mAlternativeUrl;
    }

    public void clearAlternativeUrl() {
        mAlternativeUrl = null;
        Configuration cfg = mCfgProvider.getConfiguration(AP_CONFIG_KEY);
        if (cfg != null) {
            cfg.put(AP_CONFIG_KEY, "");
            mCfgProvider.updateConfigurationNow(AP_CONFIG_KEY, cfg);
        }
    }

    /*
     * Return whether server has more to send
     */
    public ProtocolDetails processIncomingMessagesString(String s) {
        T.BIZZ();
        ProtocolDetails pd = new ProtocolDetails();
        pd.more = false;
        pd.serverTime = 0;
        try {

            L.d("incoming messages: " + s);

            org.json.JSONObject obj = new org.json.JSONObject(s);

            Long serverTime = obj.getLong("t");
            if (serverTime != null) {
                pd.serverTime = serverTime;
            }

            String alt = obj.getString("ap");
            if (alt != null && !alt.equals(mAlternativeUrl)) {
                Configuration cfg = new Configuration();
                cfg.put(AP_CONFIG_KEY, alt);
                mCfgProvider.updateConfigurationNow(AP_CONFIG_KEY, cfg);
                mAlternativeUrl = alt;
            }

            Integer protocolVersion = obj.getInt("av");
            if (protocolVersion == null || protocolVersion != 1) {
                L.bug("Invalid HTTP protocol version - " + protocolVersion);
                return pd;
            }

            Boolean hasMore = obj.getBoolean("more");
            if (hasMore == null) {
                L.bug("\"more\" not present in HTTP response");
                return pd;
            }
            pd.more = hasMore;

            org.json.JSONArray responses = obj.optJSONArray("r");
            if (responses != null) {
                for (int i = 0; i < responses.length(); i++) {
                    // XXX: avoid parsing into string and back
                    JSONObject respObj = responses.getJSONObject(i);
                    String callid = respObj.getString("ci");
                    String responseStr = responses.getString(i);
                    RpcResult result = RpcResult.parse(callid, responseStr);
                    process(result);
                }
            }

            org.json.JSONArray calls = obj.optJSONArray("c");
            if (calls != null) {
                for (int i = 0; i < calls.length(); i++) {
                    // XXX: avoid parsing into string and back
                    JSONObject callObj = calls.getJSONObject(i);
                    String callid = callObj.getString("ci");
                    String callStr = calls.getString(i);
                    RpcCall call = RpcCall.parse(callid, callStr);
                    process(call);
                }
            }

            org.json.JSONArray acks = obj.optJSONArray("a");
            if (acks != null) {
                for (int i = 0; i < acks.length(); i++) {
                    String callid = acks.getString(i);
                    processAck(callid);
                }
            }

            return pd;

        } catch (Exception e) {
            L.bug(e);
            return pd;
        }
    }

    private void process(RpcCall call) {
        L.d("Process incoming call " + call.function + " / " + call.callId);

        try {
            mBacklog.insertNewIncomingCallInDb(call);
        } catch (SQLiteConstraintException e) {
            if (mBacklog.hasBodyForCallId(call.callId)) {
                onCallProcessed(call.callId);
                return;
            } else {
                L.d("Duplicate processing: " + call.callId);
            }
        } catch (Exception e) {
            L.bug(e);
            return;
        }

        RpcResult rpcResult;
        try {
            IJSONable result = CallReceiver.processCall(call);
            rpcResult = new RpcResult(call, result == null ? null : result.toJSONMap());
        } catch (Exception e) {
            rpcResult = new RpcResult(call, e.getMessage() + "\n" + L.getStackTraceString(e));
            L.bug(e);
        }

        mBacklog.updateBody(rpcResult);

        onCallProcessed(call.callId);
        return;
    }

    private void process(RpcResult response) {

        T.BIZZ();

        final IResponseHandler<?> responseHandler;

        try {
            responseHandler = mBacklog.getResponseHandler(response.callId);
            if (responseHandler == null) {
                L.d("Ignoring incoming result for unknown callId " + response.callId);
                return;
            } else {
                L.d("Process incoming response " + responseHandler.getFunction() + " / " + response.callId);
                responseHandler.setService(mMainService);
                ResponseReceiverHandler.handle(response, responseHandler);
                mBacklog.deleteItem(response.callId);
                onResponseProcessed(response.callId);
            }
        } catch (PickleException e) {
            L.bug("Could not unpickle original call context for " + response.callId, e);
        } catch (Exception e) {
            L.bug("Error during response processing for " + response.callId, e);
        }

    }

    private void processAck(String callid) {
        L.d("Process incoming ack " + callid);
        mBacklog.freezeRetention(callid);
        return;
    }

    private void onCallProcessed(String callid) {
        T.BIZZ();
        mResponsIDsToSend.add(callid);
    }

    private void onResponseProcessed(String callid) {
        T.BIZZ();
        mAckIDsToSend.add(callid);
    }

    public HttpClient getHttpClient() {
        // Runs on network thread (NetworkHandler in HttpCommunicator
        if (mHttpClient == null) {
            mHttpClient = HTTPUtil.getHttpClient();
            if (mLogger != null)
                mLogger.d("Creating new HttpClient for HTTP communication cycle.");
        }
        return mHttpClient;
    }

}

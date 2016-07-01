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

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.CountDownLatch;

import javax.net.ssl.SSLException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.StringEntity;
import org.jivesoftware.smack.util.Base64;
import org.json.JSONArray;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.PowerManager;

import com.mobicage.rogerthat.MainService;
import com.mobicage.rogerthat.config.ConfigurationProvider;
import com.mobicage.rogerthat.util.db.DatabaseManager;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.pickle.PickleException;
import com.mobicage.rogerthat.util.system.SafeBroadcastReceiver;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.system.SystemUtils;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.system.TaggedWakeLock;
import com.mobicage.rpc.Credentials;
import com.mobicage.rpc.IResponseHandler;
import com.mobicage.rpc.RpcCall;
import com.mobicage.rpc.SDCardLogger;
import com.mobicage.rpc.config.CloudConstants;

public class HttpCommunicator {

    public static final String INTENT_HTTP_SUCCESSFUL = "com.mobicage.rpc.http.HttpCommunicator.SUCCESS";
    public static final String INTENT_HTTP_FAILURE = "com.mobicage.rpc.http.HttpCommunicator.FAILURE";
    public static final String INTENT_HTTP_START_OUTGOING_CALLS = "com.mobicage.rpc.http.HttpCommunicator.STARTOUTGOINGCALLS";
    public static final String INTENT_HTTP_START_OUTGOING_CALLS_WIFI = "filterOnWifiOnly";
    private static final String INTENT_SHOULD_RETRY_COMMUNICATION = "com.mobicage.rpc.http.SHOULD_RETRY_COMMUNICATION";
    private static final long COMMUNICATION_RETRY_INTERVAL = 300000; // 5 minutes

    private interface CommunicationResultHandler {

        void handle(final int resultCode);

    }

    private final static int STATUS_COMMUNICATION_FINISHED_WORK_DONE = 1; // Finished; we effectively did HTTP
    // communication
    private final static int STATUS_COMMUNICATION_CONTINUE = 2;
    private final static int STATUS_COMMUNICATION_SERVER_HAS_MORE = 3;
    private final static int STATUS_COMMUNICATION_ERROR = 4;
    private final static int STATUS_COMMUNICATION_FINISHED_NO_WORK_DONE = 5; // Finished; but we have not done HTTP
    // communication
    private final static String[] STATUS_COMMUNICATION_STRING_ARRAY = new String[] { "ILLEGAL STATUS",
        "STATUS_COMMUNICATION_FINISHED_WORK_DONE", "STATUS_COMMUNICATION_CONTINUE",
        "STATUS_COMMUNICATION_SERVER_HAS_MORE", "STATUS_COMMUNICATION_ERROR",
        "STATUS_COMMUNICATION_FINISHED_NO_WORK_DONE" };

    private final static int MAX_COMMUNICATION_CYCLES = 100; // Use -1 for infinite
    private final static int MAX_PACKET_SIZE = 200 * 1024;

    // Final
    private final Object mFinishedLock;
    private final Object mStateMachineLock;
    private final String mBase64Username;
    private final String mBase64Password;
    private final MainService mMainService;
    private final ConfigurationProvider mCfgProvider;
    private final SafeRunnable mStartStashingHandler;
    private final SafeRunnable mStopStashingHandler;
    private final SDCardLogger mSDCardLogger;
    private final AlarmManager mAlarmManager;

    // Use on HTTP thread
    private/* final */HttpBacklog mBacklog;

    // Stop doing work asap
    private volatile boolean mMustFinish = false;

    // New work is posted
    private volatile boolean mNewCallsInBacklog = false;

    // Current http request; we can abort it
    private volatile HttpPost mHttpPostRequest;

    // Protected by mStateMachineLock
    private boolean mIsCommunicating = false;
    private boolean mKickReceived = false;
    private boolean mLastCycleSuccess = true;

    // Owned by HTTP thread
    private String mCurrentServerUrl = null;
    private HandlerThread mNetworkWorkerThread;
    private Handler mNetworkHandler;

    private boolean mWifiOnlyEnabled;

    private final SafeBroadcastReceiver mBroadcastReceiver = new SafeBroadcastReceiver() {
        @Override
        public String[] onSafeReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                // check if items in backlog
                scheduleCommunication(true, "Network connectivity changed");
            } else {
                scheduleCommunication(true, "Previous communication attempt failed");
            }
            return null;
        }
    };

    @SuppressWarnings("serial")
    private final class ServerRespondedWrongHTTPCodeException extends Exception {
        public ServerRespondedWrongHTTPCodeException(String string) {
            super(string);
        }
    };

    private abstract class LoopCompleteHandlerRunnable extends SafeRunnable {

        // Owned by BIZZ thread
        private boolean mSuccess = false;

        public void run(boolean success) {
            T.BIZZ();
            mSuccess = success;
            run();
        }

        public boolean getSuccess() {
            T.BIZZ();
            return mSuccess;
        }

    }

    private abstract class WakeLockReleaseRunnable extends SafeRunnable {
    }

    public HttpCommunicator(final MainService mainService, final Context context,
        final DatabaseManager databaseManager, final Credentials credentials, final ConfigurationProvider cfgProvider,
        final SafeRunnable onStartStashingHandler, final SafeRunnable onStopStashingHandler,
        final SDCardLogger sdcardLogger, final boolean wifiOnlySettingEnabled) {
        T.UI();

        mMainService = mainService;
        mCfgProvider = cfgProvider;
        mFinishedLock = new Object();
        mStateMachineLock = new Object();
        mStartStashingHandler = onStartStashingHandler;
        mStopStashingHandler = onStopStashingHandler;
        mWifiOnlyEnabled = wifiOnlySettingEnabled;

        mSDCardLogger = sdcardLogger;

        mAlarmManager = (AlarmManager) mMainService.getSystemService(Context.ALARM_SERVICE);

        // Create network thread for actual network communication
        mNetworkWorkerThread = new HandlerThread("rogerthat_net_worker");
        mNetworkWorkerThread.start();
        final Looper looper = mNetworkWorkerThread.getLooper();
        mNetworkHandler = new Handler(looper);
        mNetworkHandler.post(new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                debugLog("HTTP network thread id is " + android.os.Process.myTid());
            }
        });

        final CountDownLatch latch = new CountDownLatch(1);
        mMainService.postAtFrontOfBIZZHandler(new SafeRunnable() {
            @Override
            public void safeRun() {
                T.BIZZ();
                // For simplicity, I want to construct backlog on HTTP thread
                // This way backlog is 100% on HTTP thread
                mBacklog = new HttpBacklog(context, databaseManager);
                latch.countDown();
            }
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            bugLog(e);
        }

        mBase64Username = Base64.encodeBytes(credentials.getUsername().getBytes(), Base64.DONT_BREAK_LINES);
        mBase64Password = Base64.encodeBytes(credentials.getPassword().getBytes(), Base64.DONT_BREAK_LINES);

        mMainService.addHighPriorityIntent(HttpCommunicator.INTENT_HTTP_START_OUTGOING_CALLS);

        if (CloudConstants.USE_GCM_KICK_CHANNEL) {
            final IntentFilter filter = new IntentFilter();
            filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            filter.addAction(INTENT_SHOULD_RETRY_COMMUNICATION);
            mMainService.registerReceiver(mBroadcastReceiver, filter);
        }
    }

    public void setWifiOnlyEnabled(boolean wifiOnlyEnabled) {
        this.mWifiOnlyEnabled = wifiOnlyEnabled;
    }

    public boolean isBusy() {
        synchronized (mStateMachineLock) {
            return !mLastCycleSuccess || mIsCommunicating;
        }
    }

    protected void bugLog(String s) {
        if (mSDCardLogger == null) {
            L.bug(s);
        } else {
            mSDCardLogger.bug(s);
        }
    }

    protected void bugLog(Exception e) {
        if (mSDCardLogger == null) {
            L.bug(e);
        } else {
            mSDCardLogger.bug(e);
        }
    }

    protected void bugLog(String s, Exception e) {
        if (mSDCardLogger == null) {
            L.bug(s, e);
        } else {
            mSDCardLogger.bug(s, e);
        }
    }

    protected void debugLog(String s, Exception e) {
        if (mSDCardLogger == null) {
            L.d(s, e);
        } else {
            mSDCardLogger.d(s, e);
        }
    }

    protected void debugLog(String s) {
        if (mSDCardLogger == null) {
            L.d(s);
        } else {
            mSDCardLogger.d(s);
        }
    }

    protected void debugLog(Exception e) {
        if (mSDCardLogger == null) {
            L.d(e);
        } else {
            mSDCardLogger.d(e);
        }
    }

    public void close() {
        T.UI();

        mMustFinish = true;

        // XXX: here we could be nicer e.g. wait 2 seconds for clean finish
        // e.g. synchronized(mFinishedLock) { try { mFinishedLock.wait(); } ...

        final Looper looper = mNetworkWorkerThread.getLooper();
        if (looper != null) {
            looper.quit();
        }
        if (mHttpPostRequest != null)
            mHttpPostRequest.abort();
        try {
            // XXX: can this cause ANR?
            mNetworkWorkerThread.join();
        } catch (InterruptedException e) {
            bugLog(e);
        }
        mNetworkHandler = null;
        mNetworkWorkerThread = null;

        final CountDownLatch latch = new CountDownLatch(1);
        mMainService.postAtFrontOfBIZZHandler(new SafeRunnable() {
            @Override
            public void safeRun() {
                T.BIZZ();
                mBacklog.close();
                latch.countDown();
            }
        });

        if (CloudConstants.USE_GCM_KICK_CHANNEL) {
            mMainService.unregisterReceiver(mBroadcastReceiver);
        }

        // XXX: can cause ANR
        try {
            latch.await();
        } catch (InterruptedException e) {
            bugLog(e);
        }

    }

    // ///////////////////////////////////////////////////////////////////

    public void scheduleCommunication(final boolean force, final String reason) {
        T.dontCare();
        debugLog("Schedule HTTP communication force: " + force + " - reason: " + reason);
        synchronized (mStateMachineLock) {
            if (mIsCommunicating) {
                // Already communicating. Work will be picked up automatically
                debugLog("Skipping duplicate communication in scheduleCommunication()");
                mKickReceived = true;
                return;
            } else {
                mStartStashingHandler.run();
            }
        }
        // Must start new communication cycle
        final TaggedWakeLock wakeLock = newWakeLock();
        debugLog("Acquiring wakelock " + wakeLock.hashCode());
        wakeLock.acquire();
        mMainService.postOnBIZZHandler(new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                T.BIZZ();
                communicate(force, new WakeLockReleaseRunnable() {

                    private boolean mHasRun = false;

                    @Override
                    protected void safeRun() throws Exception {
                        T.BIZZ();
                        if (!mHasRun) {
                            debugLog("Releasing wakelock " + wakeLock.hashCode());
                            mHasRun = true;
                            wakeLock.release();
                        } else {
                            debugLog("Skipping duplicate release of wakelock " + wakeLock.hashCode());
                        }
                    }
                });
            }
        });
    }

    /*
     * Execute HTTP request/response/request/response/... communication cycle
     * 
     * force == true if we have to do at least 1 communication because we know that there is or might be data ready on
     * the server
     * 
     * force == false if the initiative to kick communication is caused by a client call. It might have been processed
     * by a previous communication run. The volatile flag newCallsInBacklog will tell us
     */
    private void communicate(final boolean force, final WakeLockReleaseRunnable wakeLockReleaseRunnable) {
        T.BIZZ();
        synchronized (mStateMachineLock) {
            if (mIsCommunicating) {
                debugLog("Skipping duplicate communcation in communicate() method");
                mKickReceived = true;
                wakeLockReleaseRunnable.run();
                return;
            }
        }
        try {
            if (mMustFinish) {
                debugLog("Leaving HTTP communicate(.) because mustFinish==true");
                wakeLockReleaseRunnable.run();
                return;
            }
            if (!force && !mNewCallsInBacklog) {
                debugLog("Leaving HTTP communicate(.) because force==false and newCallsInBacklog==false");
                wakeLockReleaseRunnable.run();
                return;
            }
            synchronized (mStateMachineLock) {
                mLastCycleSuccess = false;
            }
            if (!mMainService.getNetworkConnectivityManager().isConnected()) {
                debugLog("Leaving HTTP communicate(.) because network is not connected");
                if (CloudConstants.USE_GCM_KICK_CHANNEL)
                    scheduleRetryCommunication();
                wakeLockReleaseRunnable.run();
                return;
            }
        } catch (Exception e) {
            bugLog("Exception caught while investigating if we need to communicate!", e);
            wakeLockReleaseRunnable.run();
            return;
        }

        final SafeRunnable finallyHandler = new SafeRunnable() {
            private boolean mHasRun = false;

            @Override
            protected void safeRun() throws Exception {
                T.BIZZ();
                if (!mHasRun) {
                    mHasRun = true;
                    try {
                        synchronized (mStateMachineLock) {
                            debugLog("Setting mIsCommunicating from " + mIsCommunicating + " to false");
                            mIsCommunicating = false;
                            mStopStashingHandler.run();
                            if (mKickReceived) {
                                // There might be a last kick which we have not processed yet
                                scheduleCommunication(true, "Kick received during busy HTTP communication cycle.");
                            }
                        }
                        synchronized (mFinishedLock) {
                            mFinishedLock.notifyAll();
                        }
                    } finally {
                        wakeLockReleaseRunnable.run();
                    }
                } else {
                    debugLog("finallyHandler: not running because mHasRun==true");
                }
            }
        };

        final LoopCompleteHandlerRunnable loopCompleteHandler = new LoopCompleteHandlerRunnable() {

            @Override
            protected void safeRun() throws Exception {
                T.BIZZ();
                try {
                    if (!mMustFinish)
                        mBacklog.doRetentionCleanup();

                    final boolean success = getSuccess();
                    synchronized (mStateMachineLock) {
                        mLastCycleSuccess = success;
                    }
                    if (success)
                        broadcastHttpSuccess();
                } finally {
                    finallyHandler.run();
                }
            }
        };

        try {
            final HttpProtocol protocol = new HttpProtocol(mMainService, mBacklog, mCfgProvider, mSDCardLogger);

            if (protocol.getAlternativeUrl() == null) {
                mCurrentServerUrl = CloudConstants.JSON_RPC_URL;
            } else {
                mCurrentServerUrl = protocol.getAlternativeUrl();
            }

            startCommunicationCycle(protocol, 1, STATUS_COMMUNICATION_SERVER_HAS_MORE, loopCompleteHandler,
                finallyHandler, wakeLockReleaseRunnable); // force first round-trip

        } catch (Exception e) {
            finallyHandler.run();
            bugLog("Exception in communicate.", e);
        }

    }

    private void startCommunicationCycle(final HttpProtocol protocol, final int loopCount, final int status,
        final LoopCompleteHandlerRunnable loopCompleteHandler, final SafeRunnable finallyRunnable,
        final WakeLockReleaseRunnable wakeLockReleaseRunnable) {
        try {
            debugLog("Starting HTTP communication loop " + loopCount);
            boolean mustActOnKick;
            synchronized (mStateMachineLock) {
                // No matter what the current backlog state is, we MUST communicate if one
                // of the following conditions is met:
                // * server responded that there is more
                // * we received a kick after the start of the last communication cycle
                mustActOnKick = mKickReceived;
                mKickReceived = false;
            }

            final boolean cachedNewCallsInBacklog = mNewCallsInBacklog;
            mNewCallsInBacklog = false; // we know that at least all work that is in the backlog *now*, will be
            // processed
            doCommunication(protocol, (status == STATUS_COMMUNICATION_SERVER_HAS_MORE) || mustActOnKick, loopCount,
                wakeLockReleaseRunnable, new CommunicationResultHandler() {
                    @Override
                    public void handle(final int newStatus) {
                        T.BIZZ();

                        debugLog("CommunicationResultHandler received status " + newStatus + " = "
                            + STATUS_COMMUNICATION_STRING_ARRAY[newStatus]);

                        if (newStatus == STATUS_COMMUNICATION_ERROR) {
                            debugLog("Error communicating to server");
                            if (cachedNewCallsInBacklog)
                                mNewCallsInBacklog = true;
                            broadcastHttpError();
                            loopCompleteHandler.run(false);

                            if (CloudConstants.USE_GCM_KICK_CHANNEL) {
                                scheduleRetryCommunication();
                            }

                            // Note: if we have received a kick in the meantime, we will actually retry
                            // in the finallyHandler

                            // TODO: improvement would be to set mKickReceived to true in case we have
                            // network connectivity at this point in time. However, a backoff scheme is needed

                            return;
                        }

                        if ((newStatus == STATUS_COMMUNICATION_FINISHED_NO_WORK_DONE || newStatus == STATUS_COMMUNICATION_FINISHED_WORK_DONE)
                            && !mNewCallsInBacklog) {
                            // We can finish communication cycle unless someone has kicked us during
                            // the last communication step
                            final boolean exitLoop;
                            synchronized (mStateMachineLock) {
                                if (!mKickReceived) {
                                    debugLog("Successfully finished HTTP communication cycle with server");
                                    exitLoop = true;
                                } else {
                                    exitLoop = false;
                                }
                            }
                            if (exitLoop) {
                                loopCompleteHandler.run(newStatus == STATUS_COMMUNICATION_FINISHED_WORK_DONE);
                                return;
                            }

                        }

                        final int newLoopCount = loopCount + 1;

                        if (!((MAX_COMMUNICATION_CYCLES < 0) || (newLoopCount <= MAX_COMMUNICATION_CYCLES))) {
                            bugLog("Reached max amount of HTTP communication cycles: " + MAX_COMMUNICATION_CYCLES);
                            loopCompleteHandler.run(true);
                            return;
                        }

                        if (mMustFinish) {
                            debugLog("Forced finish communication loop");
                            loopCompleteHandler.run(false);
                            return;
                        }

                        startCommunicationCycle(protocol, newLoopCount, newStatus, loopCompleteHandler,
                            finallyRunnable, wakeLockReleaseRunnable);
                    }
                });
        } catch (Exception e) {
            // XXX: should we run finallyRunnable here ? It will set mIsCommunicating:=false
            finallyRunnable.run();
            bugLog("Exception in startCommunicationCycle", e);
        }
    }

    private void broadcastHttpSuccess() {
        debugLog("Broadcast HTTP success");
        final Intent intent = new Intent(INTENT_HTTP_SUCCESSFUL);
        mMainService.sendBroadcast(intent);
    }

    private void broadcastHttpError() {
        debugLog("Broadcast HTTP error");
        final Intent intent = new Intent(INTENT_HTTP_FAILURE);
        mMainService.sendBroadcast(intent);
    }

    private void broadcastHtppStartOutgoingCalls(boolean filterOnWifiOnly) {
        debugLog("Broadcast HTTP start outgoing calls");
        final Intent intent = new Intent(INTENT_HTTP_START_OUTGOING_CALLS);
        intent.putExtra(INTENT_HTTP_START_OUTGOING_CALLS_WIFI, filterOnWifiOnly);
        mMainService.sendBroadcast(intent);

    }

    private void doCommunication(final HttpProtocol protocol, final boolean force, final int loopCount,
        final WakeLockReleaseRunnable wakeLockReleaseRunnable, final CommunicationResultHandler resultHandler) {
        T.BIZZ();

        debugLog("doCommunication - force = " + force + " - loopCount = " + loopCount);
        boolean filterOnWifiOnly = mWifiOnlyEnabled && !mMainService.getNetworkConnectivityManager().isWifiConnected();
        broadcastHtppStartOutgoingCalls(filterOnWifiOnly);
        final String callsJSONString = getJSONRepresentationForOutgoingCalls(filterOnWifiOnly);
        final boolean hasNoOutgoingCallsInBacklog = callsJSONString.equals("[]");

        if (!force && protocol.getAckIDsToSend().size() == 0 && protocol.getResponseIDsToSend().size() == 0
            && hasNoOutgoingCallsInBacklog) {
            resultHandler.handle(STATUS_COMMUNICATION_FINISHED_NO_WORK_DONE);
            return;
        }

        synchronized (mStateMachineLock) {
            if (mIsCommunicating && loopCount == 1) {
                debugLog("Skipping duplicate first loop of doCommunication()");
                mKickReceived = true;
                wakeLockReleaseRunnable.run();
                return;
            } else {
                debugLog("Setting mIsCommunicating from " + mIsCommunicating + " to true for loop " + loopCount);
                mIsCommunicating = true;
            }
        }

        final StringBuilder sb = new StringBuilder("{\"av\":1, \"c\":");
        sb.append(getJSONRepresentationForOutgoingCalls(filterOnWifiOnly));
        sb.append(", \"r\":");
        sb.append(getJSONRepresentationForOutgoingResponses(protocol));
        sb.append(", \"a\":");
        sb.append(getJSONRepresentationForOutgoingAcks(protocol));
        sb.append("}");

        protocol.getAckIDsToSend().clear();
        protocol.getResponseIDsToSend().clear();

        mNetworkHandler.post(new Runnable() {
            @Override
            public void run() {
                String tmpResponse = null;
                long before = 0;
                long after = 0;
                try {
                    before = System.currentTimeMillis();
                    tmpResponse = doSynchronousRequest(protocol.getHttpClient(), sb.toString());
                    after = System.currentTimeMillis();
                    debugLog("HTTP request (loop=" + loopCount + ") finished in " + (after - before) + " millis");
                } catch (UnknownHostException e) {
                    debugLog(e.getMessage());
                } catch (HttpHostConnectException e) {
                    debugLog(e.getMessage());
                } catch (ServerRespondedWrongHTTPCodeException e) {
                    debugLog(e.getMessage(), e);
                } catch (SSLException e) {
                    debugLog(e);
                } catch (SocketTimeoutException e) {
                    debugLog(e);
                } catch (IOException e) {
                    debugLog(e);
                } catch (Exception e) {
                    bugLog(e);
                }
                final String response = tmpResponse;
                final long fbefore = before;
                final long fafter = after;
                mMainService.postOnBIZZHandler(new SafeRunnable() {
                    @Override
                    protected void safeRun() throws Exception {
                        T.BIZZ();
                        if (response == null) {
                            redoCommunicationForError(protocol, force, loopCount, wakeLockReleaseRunnable,
                                resultHandler);
                            return;
                        }
                        HttpProtocol.ProtocolDetails pd = protocol.processIncomingMessagesString(response);
                        if (pd.serverTime != 0) {
                            final long serverTimestamp = pd.serverTime * 1000;
                            if (fafter - fbefore < 5000) {
                                final long localCorrelationTimestamp = fafter - (fafter - fbefore) / 2;
                                final long adjustedTimeDiff = serverTimestamp - localCorrelationTimestamp;
                                mMainService.setAdjustedTimeDiff(adjustedTimeDiff);
                            }
                        }
                        if (pd.more) {
                            resultHandler.handle(STATUS_COMMUNICATION_SERVER_HAS_MORE);
                            return;
                        }
                        if (hasNoOutgoingCallsInBacklog && protocol.getAckIDsToSend().size() == 0
                            && protocol.getResponseIDsToSend().size() == 0) {
                            resultHandler.handle(STATUS_COMMUNICATION_FINISHED_WORK_DONE);
                            return;
                        }
                        resultHandler.handle(STATUS_COMMUNICATION_CONTINUE);
                    }
                });
            }
        });
    }

    private void redoCommunicationForError(final HttpProtocol protocol, final boolean force, final int loopCount,
        final WakeLockReleaseRunnable wakeLockReleaseRunnable, final CommunicationResultHandler resultHandler) {
        if (!mCurrentServerUrl.equals(CloudConstants.JSON_RPC_URL)) {
            debugLog("Failover from " + mCurrentServerUrl + " to " + CloudConstants.JSON_RPC_URL);
            protocol.clearAlternativeUrl();
            mCurrentServerUrl = CloudConstants.JSON_RPC_URL;

            // loopCount + 1 otherwise failover won't work since doCommunication will see that mIsCommunicating == true
            doCommunication(protocol, force, loopCount + 1, wakeLockReleaseRunnable, resultHandler);
        } else {
            resultHandler.handle(STATUS_COMMUNICATION_ERROR);
        }
    }

    private String getJSONRepresentationForOutgoingCalls(boolean filterOnWifiOnly) {
        T.BIZZ();
        StringBuilder sb = new StringBuilder("[");
        HttpBacklogStreamer streamer = mBacklog.getStreamer(filterOnWifiOnly);
        boolean needComma = false;
        try {
            HttpBacklogItem item;
            while ((item = streamer.next()) != null) {
                if (needComma)
                    sb.append(", ");
                else
                    needComma = true;
                sb.append(item.body);
                if (sb.length() > MAX_PACKET_SIZE)
                    break;
            }
        } finally {
            streamer.close();
        }
        sb.append("]");
        return sb.toString();
    }

    private String getJSONRepresentationForOutgoingResponses(HttpProtocol protocol) {
        T.BIZZ();
        StringBuilder sb = new StringBuilder("[");
        boolean needComma = false;
        for (String callid : protocol.getResponseIDsToSend()) {
            String body = mBacklog.getBodyForCallId(callid);
            if (body != null) {
                if (needComma)
                    sb.append(", ");
                else
                    needComma = true;
                sb.append(body);

            } else {
                bugLog("Could not find body for backlog response item " + callid);
            }

        }
        sb.append("]");
        return sb.toString();
    }

    private String getJSONRepresentationForOutgoingAcks(HttpProtocol protocol) {
        T.BIZZ();
        return new JSONArray(protocol.getAckIDsToSend()).toString();
    }

    private String doSynchronousRequest(HttpClient httpClient, String requestString)
        throws ServerRespondedWrongHTTPCodeException, ClientProtocolException, UnsupportedEncodingException,
        IOException {

        L.d("Sending HTTP request to " + mCurrentServerUrl + " : " + requestString);
        mHttpPostRequest = new HttpPost(mCurrentServerUrl);
        mHttpPostRequest.setHeader("Content-type", "application/json-rpc; charset=\"utf-8\"");
        mHttpPostRequest.setHeader("X-MCTracker-User", mBase64Username);
        mHttpPostRequest.setHeader("X-MCTracker-Pass", mBase64Password);

        // XXX: improve performance - accept zipped encoding

        mHttpPostRequest.setEntity(new StringEntity(requestString, "UTF-8"));

        final HttpResponse response = httpClient.execute(mHttpPostRequest);

        final int responseCode = response.getStatusLine().getStatusCode();

        if (responseCode == HttpStatus.SC_OK) {
            HttpEntity responseEntity = response.getEntity();
            InputStream is = responseEntity.getContent();
            return SystemUtils.convertStreamToString(is);
        } else {
            HttpEntity responseEntity = response.getEntity();
            InputStream is = responseEntity.getContent();
            debugLog("Error - server responded HTTP code: " + responseCode + "\n"
                + SystemUtils.convertStreamToString(is));
            throw new ServerRespondedWrongHTTPCodeException("HTTP response code: " + responseCode);
        }

    }

    public void addOutgoingCall(final HttpBacklogItem item, final boolean priority, final String function,
        final IResponseHandler<?> responseHandler) throws PickleException {
        T.BIZZ();
        mBacklog.addOutgoingCall(item, priority, function, responseHandler,
            RpcCall.WIFI_ONLY_FUNCTIONS.contains(function));
        mNewCallsInBacklog = true;
    }

    private TaggedWakeLock newWakeLock() {
        return TaggedWakeLock.newTaggedWakeLock(mMainService, PowerManager.PARTIAL_WAKE_LOCK, "HTTP WakeLock");
    }

    private void scheduleRetryCommunication() {
        final Intent intent = new Intent(INTENT_SHOULD_RETRY_COMMUNICATION);
        long triggerAtMillis = System.currentTimeMillis() + COMMUNICATION_RETRY_INTERVAL;
        mAlarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis,
            PendingIntent.getBroadcast(mMainService, 0, intent, 0));
    }

}

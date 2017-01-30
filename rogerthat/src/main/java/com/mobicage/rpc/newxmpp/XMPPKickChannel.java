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

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicInteger;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionCreationListener;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.Logger;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.IQ.Type;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.provider.ProviderManager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.PowerManager;
import android.os.SystemClock;

import com.mobicage.rogerthat.MainService;
import com.mobicage.rogerthat.config.ConfigurationProvider;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.net.NetworkConnectivityManager;
import com.mobicage.rogerthat.util.system.SafeBroadcastReceiver;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.system.SystemUtils;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.system.TaggedWakeLock;
import com.mobicage.rpc.Credentials;
import com.mobicage.rpc.SDCardLogger;
import com.mobicage.rpc.config.CloudConstants;
import com.mobicage.rpc.http.HttpCommunicator;

public class XMPPKickChannel {

    private class PingLock {
        boolean success = false;
    }

    public final static String INTENT_BACKLOG_CONNECTED = "com.mobicage.rpc.newxmpp.XMPPKickChannel.INTENT_BACKLOG_CONNECTED";
    public final static String INTENT_BACKLOG_DISCONNECTED = "com.mobicage.rpc.newxmpp.XMPPKickChannel.INTENT_BACKLOG_DISCONNECTED";

    private final static String REMOTE_INTENT_KICK_HTTP = "kickHTTP";

    private final static String INTENT_PING_ALARM = "com.mobicage.rpc.newxmpp.XMPPKickChannel.PING_ALARM";
    private final static String INTENT_BACKOFF_FIRED = "com.mobicage.rpc.newxmpp.XMPPKickChannel.BACKOFF_FIRED";

    private final static long SECOND = 1000;
    private final static long MINUTE = 60 * SECOND;
    private final static long XMPP_CONNECTION_VALIDATION_PING_TIMEOUT = 5 * SECOND;
    private final static long XMPP_PING_INTERVAL = AlarmManager.INTERVAL_FIFTEEN_MINUTES;

    private final static String DEVICE_RESOURCE = "a";

    private final static long XMPP_MIN_BACKOFF_INTERVAL = 1 * SECOND;
    private final static long XMPP_MAX_BACKOFF_INTERVAL = 5 * MINUTE;

    private final MainService mMainService;
    private final ConnectionListener mConnectionListener;
    private final SafeBroadcastReceiver mBroadcastReceiver;
    private final PendingIntent mAlarmIntent;
    private final BackoffScheme mBackoffScheme;
    private final AtomicInteger mNumScheduledRunnables;
    private final Credentials mCredentials;
    private final SDCardLogger mLogger;
    private final NetworkConnectivityManager mConnectivityManager;
    private final AlarmManager mAlarmManager;
    private final PingLock mPingLock;
    private final ConfigurationProvider mConfigurationProvider;

    private volatile boolean mMustFinish = false;

    // At startup, or whenever we have had XMPP trouble, or whenever we have had
    // HTTP trouble, we might have missed an XMPP kick or have missed some HTTP data
    // from the server.
    //
    // Therefore, whenever we have proof of a working XMPP connection
    // (e.g. freshly connected & logged in; or successful client-server-client ping)
    // we should kick HTTP
    private volatile boolean mShouldDoHTTPConnectOnSuccessfulPing = true;

    // Owned by IO thread
    private XMPPConnection mXMPPConnection;

    // Protected by lock
    private PendingIntent mBackoffIntent;

    static {
        T.UI();

        ProviderManager pm = ProviderManager.getInstance();
        pm.addIQProvider("ping", "urn:xmpp:ping", Ping.ping);

        XMPPConnection.addConnectionCreationListener(new ConnectionCreationListener() {
            @Override
            public void connectionCreated(final Connection connection) {
                StringBuilder sb = new StringBuilder("XMPP connection created -");
                sb.append(" host:");
                sb.append(connection.getHost());
                sb.append(" port:");
                sb.append(connection.getPort());
                sb.append(" service:");
                sb.append(connection.getServiceName());
                sb.append(" connectionID:");
                sb.append(connection.getConnectionID());
                L.d(sb.toString());
            }
        });

    }

    public XMPPKickChannel(final MainService mainService, final Credentials credentials, final SDCardLogger sdcardLogger) {
        T.UI();
        mPingLock = new PingLock();
        mMainService = mainService;
        mLogger = sdcardLogger;
        mConnectivityManager = mMainService.getNetworkConnectivityManager();
        mAlarmManager = (AlarmManager) mMainService.getSystemService(Context.ALARM_SERVICE);
        mConfigurationProvider = mMainService.getConfigurationProvider();

        mConnectionListener = createConnectionListener();

        mAlarmIntent = PendingIntent.getBroadcast(mMainService, 0, new Intent(INTENT_PING_ALARM), 0);

        mBackoffScheme = new BackoffScheme(XMPP_MIN_BACKOFF_INTERVAL, XMPP_MAX_BACKOFF_INTERVAL);

        mNumScheduledRunnables = new AtomicInteger(0);

        mCredentials = credentials;

        mBroadcastReceiver = new SafeBroadcastReceiver() {
            @Override
            public String[] onSafeReceive(Context context, Intent intent) {
                T.UI();

                final String action = intent.getAction();
                debugLog("XMPPKickChannel received intent: " + action);

                boolean mustRunAnalysis = false;

                if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                    mustRunAnalysis = true;
                    logNetworkState();
                } else if (action.equals(INTENT_PING_ALARM)) {
                    mustRunAnalysis = true;
                } else if (action.equals(INTENT_BACKOFF_FIRED)) {
                    mustRunAnalysis = true;
                    cancelBackoffAlarm();
                } else if (action.equals(HttpCommunicator.INTENT_HTTP_FAILURE)) {
                    mShouldDoHTTPConnectOnSuccessfulPing = true;
                } else if (action.equals(HttpCommunicator.INTENT_HTTP_SUCCESSFUL)) {
                    // We received this because we just finished a successful HTTP communication.
                    // This makes a good opportunity to try to reconnect XMPP (in case XMPP is currently down)
                    mShouldDoHTTPConnectOnSuccessfulPing = false;
                    mMainService.postOnIOHandler(new SafeRunnable() {
                        @Override
                        protected void safeRun() throws Exception {
                            T.IO();
                            // Analyze on IO thread since mXMPPConnection is owned by IO thread
                            if (mXMPPConnection == null || !mXMPPConnection.isConnected()) {
                                debugLog("Schedule analysis because I received HTTP success, and XMPP is currently not connected.");
                                scheduleAnalysis(0);
                            }
                        }
                    });

                } else {
                    debugLog("Error - received unexpected intent in XMPP Kick Channel: action=" + action);
                }
                if (mustRunAnalysis) {
                    debugLog("SCHEDULE ANALYSIS NOW - received intent " + action);
                    scheduleAnalysis(0);
                }
                return null;
            }
        };

        final IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(INTENT_PING_ALARM);
        filter.addAction(INTENT_BACKOFF_FIRED);
        filter.addAction(HttpCommunicator.INTENT_HTTP_FAILURE);
        filter.addAction(HttpCommunicator.INTENT_HTTP_SUCCESSFUL);
        mainService.registerReceiver(mBroadcastReceiver, filter);

        setPingAlarm();
    }

    private void bugLog(String s) {
        if (mLogger == null) {
            L.bug(s);
        } else {
            mLogger.bug(s);
        }
    }

    private void debugLog(String s, Exception e) {
        if (mLogger == null) {
            L.d(s, e);
        } else {
            mLogger.d(s, e);
        }
    }

    private void debugLog(String s) {
        if (mLogger == null) {
            L.d(s);
        } else {
            mLogger.d(s);
        }
    }

    private void debugLog(Exception e) {
        if (mLogger == null) {
            L.d(e);
        } else {
            mLogger.d(e);
        }
    }

    public void teardown() {
        T.UI();
        cancelPingAlarm();
        cancelBackoffAlarm();
        mMainService.unregisterReceiver(mBroadcastReceiver);
        mMustFinish = true;
        debugLog("SCHEDULE ANALYSIS NOW (teardown)");
        scheduleAnalysis(0);
    }

    private void cancelPingAlarm() {
        T.UI();
        mAlarmManager.cancel(mAlarmIntent);
    }

    private void setPingAlarm() {
        T.UI();

        if (SystemUtils.isRunningOnRealDevice(mMainService)) {
            mAlarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime()
                + XMPP_PING_INTERVAL, XMPP_PING_INTERVAL, mAlarmIntent);
        }
    }

    private synchronized void setBackoffAlarm(long delayMillis) {

        T.IO();

        if (mBackoffIntent != null) {
            bugLog("Skipped duplicate scheduling of backoff intent - delayMillis is " + delayMillis);
            return;
        }

        mBackoffIntent = PendingIntent.getBroadcast(mMainService, 0, new Intent(INTENT_BACKOFF_FIRED), 0);
        mAlarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + delayMillis,
            mBackoffIntent);
    }

    private synchronized void cancelBackoffAlarm() {
        if (mBackoffIntent != null) {
            mAlarmManager.cancel(mBackoffIntent);
            mBackoffIntent = null;
        }
    }

    private synchronized boolean isBackoffScheduled() {
        return mBackoffIntent != null;
    }

    private TaggedWakeLock newWakeLock() {
        TaggedWakeLock wl = TaggedWakeLock.newTaggedWakeLock(mMainService, PowerManager.PARTIAL_WAKE_LOCK,
            "XMPP WakeLock");
        return wl;
    }

    private void disconnectNow() {
        T.IO();
        if (mXMPPConnection != null) {
            mXMPPConnection.removeConnectionListener(mConnectionListener);

            // We need to disconnect XMPP on a separate thread
            // since we have seen cases where XMPP disconnect took over 2 hours.
            // This blocked the IO thread for a long time, causing missed kicks for hours.
            final XMPPConnection con = mXMPPConnection;
            final Thread t = new Thread(new SafeRunnable() {
                @Override
                protected void safeRun() throws Exception {
                    debugLog("disconnectNow(): Before disconnect (on separate thread) - xmpp-" + con.hashCode());
                    con.disconnect();
                    debugLog("disconnectNow(): After disconnect (on separate thread) - xmpp-" + con.hashCode());
                }
            });
            t.setDaemon(true);
            t.start();
            mXMPPConnection = null;
        } else {
            debugLog("disconnectNow(): doing nothing since mXMPPConnection was already null");
        }
    }

    public void start() {
        debugLog("XMPPKickChannel.start()");
        scheduleAnalysis(0);
    }

    public boolean isConnected() {
        if (mXMPPConnection != null && mXMPPConnection.isConnected()) {
            return true;
        }
        return false;
    }

    private void scheduleAnalysis(long delayMillis) {
        T.dontCare();
        debugLog("schedule analysis - delayMillis: " + delayMillis);

        // If 0 or 1 analysisrunnables are posted (or busy running), we add 1
        // If 2 or more are posted (or busy running), no need to add one, since after this point in time
        // we are certain that at least one analysisrunnable will run, evaluating the conditions to reconnect

        final int currentValue = mNumScheduledRunnables.get();

        if (currentValue < 2) {

            if (delayMillis == 0) {
                final int newValue = mNumScheduledRunnables.incrementAndGet();
                debugLog("schedule analysis without delay - num scheduled runnables went from " + currentValue + " to "
                    + newValue);
                final TaggedWakeLock wakeLock = newWakeLock();
                wakeLock.acquire();
                debugLog("scheduleAnalysis: acquired wakelock wl-" + wakeLock.hashCode());
                final SafeRunnable analysisRunnable = getAnalysisRunnable(wakeLock);
                debugLog("Posting analysisRunnable on IO thread: id run-" + analysisRunnable.hashCode());
                mMainService.postOnIOHandler(analysisRunnable);
            } else {
                debugLog("Posting Backoff Alarm within " + delayMillis + " millis - mNumScheduledRunnables is "
                    + currentValue);
                setBackoffAlarm(delayMillis);
            }
        } else {
            debugLog("Skipping unnecessary post of AnalysisRunnable - num scheduled runnables was " + currentValue);
        }
    }

    private SafeRunnable getAnalysisRunnable(final TaggedWakeLock acquiredWakeLock) {
        T.dontCare();
        return new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                T.IO();

                debugLog("Running analysis runnable with id run-" + hashCode());
                logNetworkState();

                TaggedWakeLock theWakeLock = acquiredWakeLock;
                if (theWakeLock == null) {
                    theWakeLock = newWakeLock();
                    theWakeLock.acquire();
                    debugLog("analysisRunnable.run(): acquired wakelock wl-" + theWakeLock.hashCode());
                } else {
                    debugLog("analysisRunnable.run(): already protected by wakelock wl-" + theWakeLock.hashCode());
                }

                try {

                    if (mMustFinish || !(mMainService.getNetworkConnectivityManager().isConnected())) {
                        disconnectNow();
                        return;
                    }

                    if (mXMPPConnection != null && mXMPPConnection.isConnected()) {
                        // XMPP is connected

                        debugLog("XMPPConnection instance xmpp-" + mXMPPConnection.hashCode() + " is still connected.");

                        if (validateConnection()) {
                            // Ping test successful
                            if (mShouldDoHTTPConnectOnSuccessfulPing) {
                                mShouldDoHTTPConnectOnSuccessfulPing = false;
                                mMainService.kickHttpCommunication(true, "Successful XMPP ping");
                            }
                        } else {
                            // Ping test not successful
                            mShouldDoHTTPConnectOnSuccessfulPing = true;
                            disconnectNow();
                            debugLog("SCHEDULE ANALYSIS NOW (failed ping test)");
                            scheduleAnalysis(0);
                        }
                    } else {
                        // XMPP is not connected
                        try {

                            final XMPPConfigurationFactory fact = new XMPPConfigurationFactory(mConfigurationProvider,
                                mConnectivityManager, mLogger);

                            String email = mMainService.getIdentityStore().getIdentity().getEmail();
                            final ConnectionConfiguration xmppConfig = fact.getSafeXmppConnectionConfiguration(
                                mCredentials.getXmppServiceName(), email, false);
                            mXMPPConnection = new XMPPConnection(xmppConfig);
                            if (CloudConstants.DEBUG_LOGGING)
                                mXMPPConnection.setLogger(new Logger() {
                                    @Override
                                    public void log(String message) {
                                        L.d(message);
                                    }
                                });
                            mXMPPConnection.addPacketListener(createXMPPPacketListener(), createXMPPPPacketFilter());

                            debugLog("before connect - xmpp-" + mXMPPConnection.hashCode());
                            mXMPPConnection.connect();
                            debugLog("after connect - xmpp-" + mXMPPConnection.hashCode());

                            final Class<? extends XMPPConnection> connClass = mXMPPConnection.getClass();
                            final Field socketField = connClass.getDeclaredField("socket");
                            socketField.setAccessible(true);
                            debugLog("XMPP connected to " + socketField.get(mXMPPConnection).toString());
                            socketField.setAccessible(false);

                            final String xmppResource = DEVICE_RESOURCE + "_" + System.currentTimeMillis();
                            debugLog("XMPP attempting to log in as " + mCredentials.getXmppAccount() + "@"
                                + mCredentials.getXmppServiceName() + "/" + xmppResource);
                            mXMPPConnection.login(mCredentials.getXmppAccount(), mCredentials.getPassword(),
                                xmppResource);
                            debugLog("XMPP logged in");
                            mXMPPConnection.addConnectionListener(mConnectionListener);
                            mBackoffScheme.reset();
                            cancelBackoffAlarm();

                            debugLog("kicking HTTP after successful XMPP connect");
                            mMainService.kickHttpCommunication(true, "new XMPP connection created");

                            final Intent intent = new Intent(INTENT_BACKLOG_CONNECTED);
                            mMainService.sendBroadcast(intent);

                        } catch (Exception e) {

                            // XXX: in case of authentication failure, we should retry much slower
                            // Look for string in error message:
                            // "SASL authentication failed using mechanism DIGEST-MD5"

                            debugLog("XMPP Connection failure", e);

                            disconnectNow();
                            if (mNumScheduledRunnables.get() == 1 && !isBackoffScheduled()) {
                                // I am the only one scheduled; let's do backoff
                                long delay = mBackoffScheme.getDelay();
                                debugLog("SCHEDULE ANALYSIS WITH DELAY " + delay);
                                scheduleAnalysis(delay);
                            } else {
                                debugLog("Not doing XMPP connection backoff. another AnalysisRunnable is already scheduled");
                            }
                        }
                    }
                } finally {
                    int n = mNumScheduledRunnables.decrementAndGet();
                    debugLog("num scheduled runnables is " + n);
                    debugLog("Releasing XMPP wakelock wl-" + theWakeLock.hashCode());
                    theWakeLock.release();
                }

            }
        };
    }

    /*
     * XMPP ping test. Blocks at most XMPP_CONNECTION_VALIDATION_PING_TIMEOUT millis.
     * 
     * Result is true if connection is usable. False if ping test fails.
     */
    private boolean validateConnection() {
        T.IO();

        debugLog("validateConnection");

        if (mMustFinish)
            return false;

        if (mXMPPConnection == null || !mXMPPConnection.isConnected())
            return false;

        synchronized (mPingLock) {
            mPingLock.success = false;
            Ping ping = new Ping();
            ping.setTo(CloudConstants.XMPP_DOMAIN);
            long sendTimestamp = SystemClock.elapsedRealtime();
            try {
                mXMPPConnection.sendPacket(ping);
            } catch (IllegalStateException e) {
                debugLog("validate XMPP connection failed - illegal state " + e.getMessage());
                return false;
            }
            debugLog("Sent ping packet with id " + ping.getPacketID());

            try {
                mPingLock.wait(XMPP_CONNECTION_VALIDATION_PING_TIMEOUT);
            } catch (InterruptedException e) {
                debugLog(e);
                return false;
            }

            if (mPingLock.success) {
                debugLog("Detected XMPP activity within " + (SystemClock.elapsedRealtime() - sendTimestamp)
                    + " millis.");
            } else {
                debugLog("Received no ping result within " + XMPP_CONNECTION_VALIDATION_PING_TIMEOUT + " millis.");
            }

            return mPingLock.success;
        }
    }

    private PacketFilter createXMPPPPacketFilter() {
        T.IO();
        return new PacketFilter() {
            @Override
            public boolean accept(Packet packet) {

                L.d("Received packet: " + packet.toXML());

                if (mMustFinish)
                    return false;
                synchronized (mPingLock) {
                    mPingLock.success = true;
                    mPingLock.notify();
                }
                if (packet instanceof IQ && packet.getFrom().equals(CloudConstants.XMPP_DOMAIN)) {
                    return true;
                }
                if (!packet.getFrom().equalsIgnoreCase(CloudConstants.XMPP_KICK_COMPONENT)) {
                    logSkippedPacket(packet);
                    return false;
                }
                if ((packet instanceof Message) || (packet instanceof Ping))
                    return true;
                logSkippedPacket(packet);
                return false;
            }
        };
    }

    private PacketListener createXMPPPacketListener() {
        T.IO();
        return new PacketListener() {

            @Override
            public void processPacket(final Packet packet) {

                if (mMustFinish)
                    return;

                if (packet instanceof Message) {
                    String body = ((Message) packet).getBody();
                    if (body != null && body.equals(REMOTE_INTENT_KICK_HTTP)) {
                        debugLog("XMPPKickChannel received remote command:" + REMOTE_INTENT_KICK_HTTP);
                        mMainService.kickHttpCommunication(true, "XMPP kick received");
                        return;
                    }
                }

                if (packet instanceof Ping) {
                    Ping pingPacket = (Ping) packet;
                    Type pingType = pingPacket.getType();
                    if (pingType == IQ.Type.GET) {
                        mMainService.postOnIOHandler(new SafeRunnable() {
                            @Override
                            protected void safeRun() throws Exception {
                                T.IO();
                                debugLog("Received incoming ping packet");
                                if (mXMPPConnection != null) {
                                    mXMPPConnection.sendPacket(Ping.createResultIQ((Ping) packet));
                                    debugLog("Sent ping response");
                                }
                            }
                        });
                        return;
                    }
                }

                logSkippedPacket(packet);
                return;

            }

        };

    }

    private void logSkippedPacket(Packet packet) {
        L.d("Skipped incoming packet: " + packet.toXML());
    }

    private ConnectionListener createConnectionListener() {
        T.UI();
        return new ConnectionListener() {

            @Override
            public void reconnectionSuccessful() {
                debugLog("XMPP reconnection successful");
            }

            @Override
            public void reconnectionFailed(Exception e) {
                debugLog("XMPP reconnection failed", e);
            }

            @Override
            public void reconnectingIn(int seconds) {
                debugLog("XMPP reconnecting in " + seconds);
            }

            @Override
            public void connectionClosedOnError(Exception e) {
                debugLog("XMPP Connection Closed with Error: " + e.getMessage());
                logNetworkState();
                mShouldDoHTTPConnectOnSuccessfulPing = true;
                debugLog("SCHEDULE ANALYSIS NOW (connection closed on error)");
                scheduleAnalysis(0);

                final Intent intent = new Intent(INTENT_BACKLOG_DISCONNECTED);
                mMainService.sendBroadcast(intent);
            }

            @Override
            public void connectionClosed() {
                debugLog("XMPP connection closed");
                logNetworkState();
                mShouldDoHTTPConnectOnSuccessfulPing = true;

                final Intent intent = new Intent(INTENT_BACKLOG_DISCONNECTED);
                mMainService.sendBroadcast(intent);
            }
        };
    }

    private void logNetworkState() {
        debugLog(mConnectivityManager.getNetworkState());
    }

    public void logToXmppAccount(final String to, final String s) {
        final SafeRunnable runnable = new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                T.IO();
                if (mXMPPConnection != null && mXMPPConnection.isConnected()) {
                    Message message = new Message(to, Message.Type.chat);
                    message.setBody(s);
                    mXMPPConnection.sendPacket(message);
                }
            }
        };
        if (T.getThreadType() == T.IO) {
            runnable.run();
        } else {
            mMainService.postOnIOHandler(runnable);
        }
    }
}

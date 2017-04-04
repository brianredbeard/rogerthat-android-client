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

package com.mobicage.rogerthat.util.logging;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.simple.JSONObject;

import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import com.mobicage.api.system.Rpc;
import com.mobicage.rogerthat.MainService;
import com.mobicage.rogerthat.registration.RegistrationWizard2;
import com.mobicage.rogerthat.util.http.HTTPUtil;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.system.SystemUtils;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rpc.ResponseHandler;
import com.mobicage.rpc.config.CloudConstants;
import com.mobicage.to.system.LogErrorRequestTO;
import com.mobicage.to.system.LogErrorResponseTO;

/**
 * Logging class to android.util.Log all messages with logtag "MC"
 * 
 */
public class L {

    public final static String SEPARATOR = "------------------------------------------------------";
    public final static String TAG = "MC";
    private final static String BUG = "Bug!";
    private final static int BLOB_PIECE_SIZE = 120;
    private final static String BLOB_LINE_PREFIX = "   ";

    private final static long MAX_SEND_RATE = 1000 * 60 * 60; // 1 hour

    private static volatile long sLastErrorLogTimestamp = 0;
    private static volatile MainService sMainService;

    public static void dBlob(String blob) {
        String[] logLines = blob.split("\\r?\\n");
        L.d(L.SEPARATOR);
        for (String logLine : logLines) {
            int numPieces = 1 + (logLine.length() / BLOB_PIECE_SIZE);
            for (int i = 0; i < numPieces; i++) {
                if (i < numPieces - 1) {
                    L.d(BLOB_LINE_PREFIX + logLine.substring(i * BLOB_PIECE_SIZE, (i + 1) * BLOB_PIECE_SIZE));
                } else {
                    L.d(BLOB_LINE_PREFIX + logLine.substring(i * BLOB_PIECE_SIZE));
                }
            }
        }
        L.d(L.SEPARATOR);
    }

    // Android-only log, not to SD
    public static void dd(String s) {
        Log.d(TAG, s);
    }

    // Android-only log, not to SD
    public static void dd(Throwable t) {
        Log.d(TAG, "", t);
    }

    // Android-only log, not to SD
    public static void dd(String s, Throwable t) {
        Log.d(TAG, s, t);
    }

    public static void highlight(String s) {
        d(SEPARATOR);
        d(s);
        d(SEPARATOR);
    }

    public static void bug(String s) {
        s = BUG + '\n' + s;
        Log.d(TAG, s);
        logToServer(s, null);
        logToXmpp("BUG", s, null);
    }

    public static void bug(Throwable t) {
        Log.d(TAG, BUG, t);
        logToServer(null, t);
        logToXmpp("BUG", "", t);
    }

    private static void setErrorMessageOnLogErrorRequest(Throwable t, LogErrorRequestTO request) {
        request.errorMessage = getStackTraceString(t);
    }

    public static String getStackTraceString(Throwable t) {
        final Writer w = new StringWriter();
        final PrintWriter pw = new PrintWriter(w);
        t.printStackTrace(pw);
        return w.toString();
    }

    public static void bug(String s, Throwable t) {
        s = BUG + '\n' + s;
        Log.d(TAG, s, t);
        logToServer(s, t);
        logToXmpp("BUG", s, t);
    }

    private static void logToServer(final String s, final Throwable t) {
        try {
            if (sMainService == null)
                return;
            final long currentTimeMillis = sMainService.currentTimeMillis();
            if (currentTimeMillis - sLastErrorLogTimestamp < MAX_SEND_RATE)
                return;
            sLastErrorLogTimestamp = currentTimeMillis;
            try {
                throw new Exception();
            } catch (final Exception e) {
                sMainService.postOnUIHandler(new SafeRunnable() {
                    @Override
                    protected void safeRun() throws Exception {
                        if (sMainService.getRegisteredFromConfig()) {
                            if (sMainService == null)
                                return;
                            LogErrorRequestTO request = new LogErrorRequestTO();
                            request.description = s;
                            request.platform = 1;
                            request.timestamp = currentTimeMillis / 1000;
                            request.mobicageVersion = (sMainService.isDebug() ? "-" : "")
                                    + sMainService.getMajorVersion() + "." + sMainService.getMinorVersion();
                            request.platformVersion = Build.FINGERPRINT + " (-) " + SystemUtils.getAndroidVersion()
                                    + " (-) " + Build.MODEL;
                            setErrorMessageOnLogErrorRequest(t == null ? e : t, request);
                            Rpc.logError(new ResponseHandler<LogErrorResponseTO>(), request);
                        } else {
                            new AsyncTask<Object, Object, Object>() {
                                @Override
                                protected Object doInBackground(Object... params) {
                                    if (sMainService == null)
                                        return null;
                                    try {
                                        HttpPost httpPostRequest = new HttpPost(CloudConstants.LOG_ERROR_URL);
                                        httpPostRequest.setHeader("Content-Type", "application/x-www-form-urlencoded");
                                        httpPostRequest.setHeader("User-Agent", MainService.getUserAgent(sMainService));

                                        RegistrationWizard2 wiz = RegistrationWizard2.getWizard(sMainService);

                                        List<NameValuePair> formParams = new ArrayList<NameValuePair>();
                                        formParams.add(new BasicNameValuePair("install_id", wiz.getInstallationId()));
                                        formParams.add(new BasicNameValuePair("device_id", wiz.getDeviceId()));
                                        formParams.add(new BasicNameValuePair("language", Locale.getDefault().getLanguage()));
                                        formParams.add(new BasicNameValuePair("country", Locale.getDefault().getCountry()));
                                        formParams.add(new BasicNameValuePair("description", s));
                                        formParams.add(new BasicNameValuePair("platform", "1"));
                                        formParams.add(new BasicNameValuePair("platform_version", ""
                                                + SystemUtils.getAndroidVersion()));
                                        formParams.add(new BasicNameValuePair("timestamp", "" + System.currentTimeMillis()
                                                / 1000));
                                        formParams.add(new BasicNameValuePair("mobicage_version", MainService
                                                .getVersion(sMainService)));
                                        formParams.add(new BasicNameValuePair("error_message",
                                                getStackTraceString(t == null ? e : t)));

                                        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formParams, HTTP.UTF_8);
                                        httpPostRequest.setEntity(entity);
                                        HTTPUtil.getHttpClient().execute(httpPostRequest);
                                    } catch (Exception e1) {
                                        L.d("Error while posting error to server", e1);
                                    }
                                    return null;
                                }
                            }.execute();
                        }
                    }
                });
            }
        } catch (Exception e) {
            L.d("Error while posting error to server", e);
        }
    }

    public static void d(String s) {
        if (CloudConstants.DEBUG_LOGGING)
            Log.d(TAG, s);

        logToXmpp("D", s, null);
    }

    public static void d(Throwable t) {
        if (CloudConstants.DEBUG_LOGGING)
            Log.d(TAG, "", t);

        logToXmpp("D", "", t);
    }

    public static void d(String s, Throwable t) {
        if (CloudConstants.DEBUG_LOGGING)
            Log.d(TAG, s, t);

        logToXmpp("D", s, t);
    }

    public static void e(String s) {
        Log.e(TAG, s);
        logToXmpp("E", s, null);
    }

    public static void e(Throwable t) {
        Log.e(TAG, "", t);
        logToXmpp("E", "", t);
    }

    public static void e(String s, Throwable t) {
        Log.e(TAG, s, t);
        logToXmpp("E", s, t);
    }

    public static void i(String s) {
        Log.i(TAG, s);
        logToXmpp("I", s, null);
    }

    public static void i(Throwable t) {
        Log.i(TAG, "", t);
        logToXmpp("I", "", t);
    }

    public static void i(String s, Throwable t) {
        Log.i(TAG, s, t);
        logToXmpp("I", s, t);
    }

    public static void v(String s) {
        Log.v(TAG, s);
    }

    public static void v(Throwable t) {
        Log.v(TAG, "", t);
    }

    public static void v(String s, Throwable t) {
        if (CloudConstants.DEBUG_LOGGING)
            Log.v(TAG, s, t);
    }

    public static void w(String s) {
        Log.w(TAG, s);
        logToXmpp("W", s, null);
    }

    public static void w(Throwable t) {
        Log.w(TAG, "", t);
        logToXmpp("W", "", t);
    }

    public static void w(String s, Throwable t) {
        Log.w(TAG, s, t);
        logToXmpp("W", s, t);
    }

    public static void setContext(MainService mainService) {
        sMainService = mainService;
    }

    public static void logStackTrace() {
        try {
            throw new Exception("Debug Exception");
        } catch (Exception e) {
            L.d(e);
        }
    }

    public static class LogForwarder implements Runnable {

        private class LogItem {

            private final long timestamp;
            private final String threadName;
            private final String logLevel;
            private final String logLine;
            private final Throwable throwable;

            LogItem(final String logLevel, final String logLine, final Throwable throwable) {
                this.timestamp = sMainService == null ? System.currentTimeMillis() : sMainService.currentTimeMillis();
                this.threadName = T.getThreadName();
                this.logLevel = logLevel;
                this.logLine = logLine;
                this.throwable = throwable;
            }
        }

        private boolean forwarding;
        private String jid;
        private Thread forwardingThread;
        private final BlockingDeque<LogItem> queue;
        private final LogItem stopper = new LogItem(null, null, null);

        LogForwarder() {
            this.forwarding = false;
            this.queue = new LinkedBlockingDeque<LogItem>();
        }

        public void start(final String jid) {
            synchronized (this) {
                if (this.forwarding)
                    return;
                this.jid = jid;
                this.forwarding = true;
                this.queue.clear();
                this.forwardingThread = new Thread(this);
                this.forwardingThread.setDaemon(true);
                this.forwardingThread.start();
            }
        }

        public void stop() {
            synchronized (this) {
                if (!this.forwarding)
                    return;
                this.forwarding = false;
                this.queue.add(this.stopper);
                this.forwardingThread = null;
            }
        }

        public void queue(final String logLevel, final String logLine, final Throwable throwable) {
            if (!this.forwarding)
                return;
            this.queue.add(new LogItem(logLevel, logLine, throwable));
        }

        @SuppressWarnings("unchecked")
        @Override
        public void run() {
            thread_loop: while (true) {
                LogItem li;
                // Wait and take 1 item from the queue
                try {
                    li = queue.take();
                } catch (InterruptedException e) {
                    break thread_loop;
                }
                if (li == this.stopper)
                    break thread_loop;

                final StringBuilder sb = new StringBuilder();
                formatLogLine(li, sb);

                empty_queue_loop: while (true) { // Loop until all items are taken from the queue
                    li = queue.poll();
                    if (li != null) {
                        if (li == this.stopper)
                            break thread_loop;
                        sb.append('\n');
                        formatLogLine(li, sb);
                    } else
                        break empty_queue_loop;
                }

                JSONObject result = new JSONObject();
                result.put("jid", this.jid);
                result.put("message", sb.toString());

                ByteArrayEntity bae = new ByteArrayEntity(result.toJSONString().getBytes(Charset.forName("UTF-8")));
                HttpPost post = new HttpPost(CloudConstants.DEBUG_LOG_URL);
                post.setEntity(bae);
                try {
                    HTTPUtil.getHttpClient().execute(post);
                } catch (ClientProtocolException e) {
                    // Cannot log due to prevent log loops
                } catch (IOException e) {
                    // Cannot log due to prevent log loops
                }
            }
        }

        private void formatLogLine(LogItem li, StringBuilder sb) {
            sb.append(li.timestamp).append(' ').append(li.threadName).append(' ').append(li.logLevel).append(" - ")
                .append(li.logLine);
            if (li.throwable != null) {
                sb.append('\n').append(getStackTraceString(li.throwable));
            }
        }
    }

    private final static LogForwarder logForwarder = new LogForwarder();

    public static LogForwarder getLogForwarder() {
        return logForwarder;
    }

    private static void logToXmpp(final String logLevel, final String logLine, final Throwable throwable) {
        logForwarder.queue(logLevel, logLine, throwable);
    }
}

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

package com.mobicage.rpc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import android.content.Context;
import android.os.Environment;

import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SystemUtils;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rpc.config.AppConstants;
import com.mobicage.rpc.config.CloudConstants;

public class SDCardLogger {

    private static final String LOGPATH = AppConstants.APP_ID;

    private final Context mContext;
    private final String mFilename;
    private final String mLogPrefix;
    private final boolean mEnabled;

    private BufferedWriter mWriter;

    public SDCardLogger(final Context context, final String filename, final String logPrefix) {
        mContext = context;
        mFilename = filename;
        mLogPrefix = logPrefix;
        mEnabled = isEnabled(context);
        if (mEnabled) {
            d("====================================================");
            d("RPC logging enabled to file /sdcard/" + LOGPATH + "/" + mFilename);
        }
    }

    @SuppressWarnings("unused")
    private boolean isEnabled(final Context context) {
        return CloudConstants.DEBUG_LOGGING && SystemUtils.isRunningOnRealDevice(context);
    }

    public synchronized void close() {
        try {
            if (mWriter != null) {
                mWriter.close();
            }
        } catch (IOException e) {
            L.bug(e);
        }
        mWriter = null;
    }

    public void d(String s) {
        d(s, null);
    }

    public void d(Exception e) {
        d("Exception", e);
    }

    public synchronized void d(String s, Exception e) {
        final String logBody = logToSDCard(s, e);
        L.d(mLogPrefix + logBody, e);
    }

    public void bug(String s) {
        bug(s, null);
    }

    public void bug(Exception e) {
        bug("Exception", e);
    }

    public synchronized void bug(String s, Exception e) {
        final String logBody = logToSDCard(s, e);
        L.bug(mLogPrefix + logBody, e);
    }

    private String logToSDCard(String s, Exception e) {
        final long epoch = System.currentTimeMillis();
        final String date = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS").format(new java.util.Date(epoch));
        final String logBody = " (" + android.os.Process.myPid() + "/" + android.os.Process.myTid() + "/"
            + getThreadStr(T.getThreadType()) + ") " + s;

        if (mEnabled) {
            if (mWriter == null) {
                openFile();
            }
            try {
                if (mWriter != null) {
                    mWriter.write(date + logBody + "\n");
                    if (e != null)
                        mWriter.write(L.getStackTraceString(e) + "\n");
                }
                mWriter.flush();
            } catch (Exception e2) {
                // do nothing
            }
        }
        return logBody;
    }

    private String getThreadStr(int threadType) {
        switch (threadType) {
        case T.UI:
            return "UI";
        case T.IO:
            return "IO";
        case T.BIZZ:
            return "BIZZ";
        default:
            return "??";
        }
    }

    private void openFile() {
        try {
            mWriter = new BufferedWriter(new FileWriter(new File(mkdirs(LOGPATH), mFilename), true));
        } catch (IOException e) {
            // do nothing
        }
    }

    private File mkdirs(String dir) throws IOException {
        if (!SystemUtils.isSDCardAvailable(mContext)) {
            throw new IOException("SD card is not mounted");
        }
        File sdcard = Environment.getExternalStorageDirectory();
        File mydir = new File(sdcard, dir);
        if (!mydir.isDirectory()) {
            boolean success = mydir.mkdirs();
            if (!success) {
                throw new IOException("Cannot create dir " + mydir.getAbsolutePath());
            }
        }
        return mydir;
    }

}

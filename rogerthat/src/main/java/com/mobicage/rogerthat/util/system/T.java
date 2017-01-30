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

package com.mobicage.rogerthat.util.system;

import android.os.HandlerThread;

import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.ui.TestUtils;

public class T {

    private final static int UNINITIALIZED = -100;

    private volatile static int sUIThreadId = UNINITIALIZED;
    private volatile static int sIOThreadId = UNINITIALIZED;
    private volatile static int sRegistrationThreadId = UNINITIALIZED;
    private volatile static int sBIZZThreadId = UNINITIALIZED;

    private volatile static int sPid = UNINITIALIZED;

    public final static int UI = 1;
    public final static int IO = 2;
    public final static int REGISTRATION = 3;
    public final static int BIZZ = 5;

    public static void checkTidLegality(int tid) {
        if (tid == -1)
            throw new RuntimeException("Illegal thread id. Thread is probably not yet started.");
    }

    public static String description() {
        return "UI tid = " + sUIThreadId + " / IO tid = " + sIOThreadId + " / REGISTRATION tid = "
            + sRegistrationThreadId + " / HTTP tid = " + sBIZZThreadId;
    }

    public static void setUIThread(String where) {
        int myTid = android.os.Process.myTid();
        checkTidLegality(myTid);
        if (sUIThreadId == UNINITIALIZED) {
            sUIThreadId = myTid;
            L.d(where + " setting UI thread id to " + sUIThreadId);
            if (sUIThreadId == sIOThreadId || sUIThreadId == sRegistrationThreadId || sUIThreadId == sBIZZThreadId)
                L.bug("UI thread id already used! " + description());
        } else if (myTid != sUIThreadId)
            L.bug("UI Thread ID not as expected myTid(" + myTid + ") but expected (" + sUIThreadId + ")");
        setPid(where);
    }

    public static void setIOThread(String where, HandlerThread thread) {
        int myTid = thread.getThreadId();
        checkTidLegality(myTid);
        if (sIOThreadId == UNINITIALIZED) {
            sIOThreadId = myTid;
            L.d(where + " setting IO thread id to " + sIOThreadId);
            if (sIOThreadId == sUIThreadId || sIOThreadId == sRegistrationThreadId || sIOThreadId == sBIZZThreadId)
                L.bug("IO thread id already used! " + description());
        } else if (myTid != sIOThreadId)
            L.bug("IO Thread ID not as expected myTid(" + myTid + ") but expected (" + sIOThreadId + ")");
        setPid(where);
    }

    public static void resetIOThreadId() {
        T.UI();
        L.d("Resetting IO Thread Id");
        sIOThreadId = UNINITIALIZED;
    }

    public static void setRegistrationThread(String where) {
        int myTid = android.os.Process.myTid();
        checkTidLegality(myTid);
        if (sRegistrationThreadId == UNINITIALIZED) {
            sRegistrationThreadId = myTid;
            L.d(where + " setting registration thread id to " + sRegistrationThreadId);
            if (sRegistrationThreadId == sUIThreadId || sRegistrationThreadId == sIOThreadId
                || sRegistrationThreadId == sBIZZThreadId)
                L.bug("REGISTRATION thread id already used! " + description());
        } else if (myTid != sRegistrationThreadId)
            L.bug("Registration Thread ID not as expected myTid(" + myTid + ") but expected (" + sRegistrationThreadId
                + ")");
        setPid(where);
    }

    public static void resetRegistrationThreadId() {
        T.UI();
        L.d("Resetting Register Thread Id");
        sRegistrationThreadId = UNINITIALIZED;
    }

    public static void setBizzThread(String where, HandlerThread thread) {
        int myTid = thread.getThreadId();
        checkTidLegality(myTid);
        if (sBIZZThreadId == UNINITIALIZED) {
            sBIZZThreadId = myTid;
            L.d(where + " setting HTTP thread id to " + sBIZZThreadId);
            if (sBIZZThreadId == sUIThreadId || sBIZZThreadId == sIOThreadId || sBIZZThreadId == sRegistrationThreadId)
                L.bug("HTTP thread id already used! " + description());
        } else if (myTid != sBIZZThreadId)
            L.bug("HTTP Thread ID not as expected myTid(" + myTid + ") but expected (" + sBIZZThreadId + ")");
        setPid(where);
    }

    public static void resetBizzThreadId() {
        T.UI();
        L.d("Resetting HTTP Thread Id");
        sBIZZThreadId = UNINITIALIZED;
    }

    private static void setPid(String where) {
        int myPid = android.os.Process.myPid();
        if (sPid == UNINITIALIZED) {
            sPid = myPid;
            L.d(where + " setting PID to " + sPid);
        } else {
            if (myPid != sPid) {
                L.bug(where + " PID not as expected myPid(" + myPid + ") but expected (" + sPid + ")");
            }
        }
    }

    // Assert that this code is executed on UI thread and in process with
    // correct pid
    public static void UI() {
        checkTid(UI);
        checkPid();
    }

    // Assert that this code is executed on IO thread and in process with
    // correct pid
    public static void IO() {
        checkTid(IO);
        checkPid();
    }

    // Assert that this code is executed on registration thread and in process
    // with correct pid
    public static void REGISTRATION() {
        checkTid(REGISTRATION);
        checkPid();
    }

    public static void BIZZ() {
        checkTid(BIZZ);
        checkPid();
    }

    // Assert that this code is executed in process with correct pid
    public static void dontCare() {
        checkPid();
    }

    public static int getThreadType() {
        int myTid = android.os.Process.myTid();
        if (myTid == sUIThreadId)
            return UI;
        if (myTid == sRegistrationThreadId)
            return REGISTRATION;
        if (myTid == sBIZZThreadId)
            return BIZZ;
        if (myTid == sIOThreadId)
            return IO;
        return -1;
    }

    public static String getThreadName() {
        int myTid = android.os.Process.myTid();
        if (myTid == sUIThreadId)
            return "UI  ";
        if (myTid == sRegistrationThreadId)
            return "REG ";
        if (myTid == sBIZZThreadId)
            return "BIZZ";
        if (myTid == sIOThreadId)
            return "IO  ";
        return "????";
    }

    private static void checkTid(int threadType) {
        if (TestUtils.isRunningTest()) {
            return;
        }
        int myTid = android.os.Process.myTid();
        switch (threadType) {
        case UI:
            if (myTid != sUIThreadId)
                logStacktrace("UI thread error");
            break;
        case IO:
            if (myTid != sIOThreadId)
                logStacktrace("IO thread error");
            break;
        case REGISTRATION:
            if (myTid != sRegistrationThreadId)
                logStacktrace("Registration thread error");
            break;
        case BIZZ:
            if (myTid != sBIZZThreadId)
                logStacktrace("HTTP thread error");
            break;
        default:
            L.bug("Illegal thread type in checkTid(): " + threadType);
            break;
        }

    }

    private static void checkPid() {
        int myPid = android.os.Process.myPid();
        if (myPid != sPid) {
            logStacktrace("Pid error");
        }
    }

    private static Exception logStacktrace(String message) {
        try {
            throw new Exception();
        } catch (Exception e) {
            L.d(message, e);
            return e;
        }
    }

    public static void TO_BE_ANALYZED() {
        logStacktrace("WARNING - should do threading analysis!!!");
    }
}

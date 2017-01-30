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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Environment;

import com.google.zxing.client.android.CaptureActivity;
import com.google.zxing.client.android.Intents;
import com.mobicage.rogerthat.util.IOUtils;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rpc.config.CloudConstants;

public class SystemUtils {

    private final static int EMULATOR_MODE_NOT_INITIALIZED = 0;
    private final static int EMULATOR_MODE_EMULATOR = 1;
    private final static int EMULATOR_MODE_REAL_DEVICE = 2;

    private volatile static int sEmulatorMode = EMULATOR_MODE_NOT_INITIALIZED;

    private static void initEmulator(Context pContext) {
        T.dontCare();
        if (sEmulatorMode == EMULATOR_MODE_NOT_INITIALIZED) {
            if ("google_sdk".equals(Build.PRODUCT) || "sdk".equals(Build.PRODUCT)) {
                sEmulatorMode = EMULATOR_MODE_EMULATOR;
            } else {
                sEmulatorMode = EMULATOR_MODE_REAL_DEVICE;
            }
        }
    }

    public static boolean isRunningInEmulator(Context pContext) {
        T.dontCare();
        initEmulator(pContext);
        return (sEmulatorMode == EMULATOR_MODE_EMULATOR);
    }

    public static boolean isRunningOnRealDevice(Context pContext) {
        T.dontCare();
        initEmulator(pContext);
        return (sEmulatorMode == EMULATOR_MODE_REAL_DEVICE);
    }

    public static boolean isSDCardAvailable(Context pContext) {
        T.dontCare();
        if (IOUtils.shouldCheckExternalStorageAvailable()) {
            return isRunningInEmulator(pContext) || Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
        } else {
            return true;
        }
    }

    public static Intent getActionDialIntent(Context pContext) {
        T.dontCare();
        Intent callIntent = new Intent(Intent.ACTION_DIAL);
        PackageManager manager = pContext.getPackageManager();
        List<ResolveInfo> infos = manager.queryIntentActivities(callIntent, 0);
        return infos.size() == 0 ? null : callIntent;
    }

    // Method copied from http://www.kodejava.org/examples/266.html
    public static String convertStreamToString(InputStream is) throws IOException {
        /*
         * To convert the InputStream to String we use the Reader.read(char[] buffer) method. We iterate until the
         * Reader return -1 which means there's no more data to read. We use the StringWriter class to produce the
         * string.
         */
        if (is != null) {
            Writer writer = new StringWriter();

            char[] buffer = new char[1024];
            try {
                Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 1024);
                int n;
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }
            } finally {
                is.close();
            }
            return writer.toString();
        } else {
            return "";
        }
    }

    // End copied

    // Copied from http://www.exampledepot.com/egs/java.io/DeleteDir.html
    // Deletes all files and subdirectories under dir.
    // Returns true if all deletions were successful.
    // If a deletion fails, the method stops attempting to delete and returns false.
    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (String element : children) {
                boolean success = deleteDir(new File(dir, element));
                if (!success) {
                    return false;
                }
            }
        }

        // The directory is now empty so delete it
        return dir.delete();
    }

    // End copied

    public static void showZXingActivity(final Activity activity, final int installResult, final int scanResult) {
        final Intent intent = new Intent(activity, CaptureActivity.class);
        intent.setAction(Intents.Scan.ACTION);
        intent.putExtra(Intents.Scan.MODE, Intents.Scan.QR_CODE_MODE);
        activity.startActivityForResult(intent, scanResult);
    }

    @SuppressWarnings("deprecation")
    public static int getAndroidVersion() {
        return Integer.parseInt(Build.VERSION.SDK);
    }

    public static String getApplicationName(final Context context) {
        final ApplicationInfo applicationInfo = context.getApplicationInfo();
        return context.getString(applicationInfo.labelRes);
    }

    public static void logIntentFlags(Intent intent) {
        if (!CloudConstants.DEBUG_LOGGING)
            return;
        L.d(intent.toString());
        final int flags = intent.getFlags();
        if (isFlagEnabled(flags, Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT))
            L.d("FLAG_ACTIVITY_BROUGHT_TO_FRONT");
        if (isFlagEnabled(flags, Intent.FLAG_ACTIVITY_CLEAR_TOP))
            L.d("FLAG_ACTIVITY_CLEAR_TOP");
        if (isFlagEnabled(flags, Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET))
            L.d("FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET");
        if (isFlagEnabled(flags, Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS))
            L.d("FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS");
        if (isFlagEnabled(flags, Intent.FLAG_ACTIVITY_FORWARD_RESULT))
            L.d("FLAG_ACTIVITY_FORWARD_RESULT");
        if (isFlagEnabled(flags, Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY))
            L.d("FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY");
        if (isFlagEnabled(flags, Intent.FLAG_ACTIVITY_MULTIPLE_TASK))
            L.d("FLAG_ACTIVITY_MULTIPLE_TASK");
        if (isFlagEnabled(flags, Intent.FLAG_ACTIVITY_NEW_TASK))
            L.d("FLAG_ACTIVITY_NEW_TASK");
        if (isFlagEnabled(flags, Intent.FLAG_ACTIVITY_NO_ANIMATION))
            L.d("FLAG_ACTIVITY_NO_ANIMATION");
        if (isFlagEnabled(flags, Intent.FLAG_ACTIVITY_NO_HISTORY))
            L.d("FLAG_ACTIVITY_NO_HISTORY");
        if (isFlagEnabled(flags, Intent.FLAG_ACTIVITY_NO_USER_ACTION))
            L.d("FLAG_ACTIVITY_NO_USER_ACTION");
        if (isFlagEnabled(flags, Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP))
            L.d("FLAG_ACTIVITY_PREVIOUS_IS_TOP");
        if (isFlagEnabled(flags, Intent.FLAG_ACTIVITY_REORDER_TO_FRONT))
            L.d("FLAG_ACTIVITY_REORDER_TO_FRONT");
        if (isFlagEnabled(flags, Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED))
            L.d("FLAG_ACTIVITY_RESET_TASK_IF_NEEDED");
        if (isFlagEnabled(flags, Intent.FLAG_ACTIVITY_SINGLE_TOP))
            L.d("FLAG_ACTIVITY_SINGLE_TOP");
        L.d("Extras: " + intent.getExtras() + "\n\n\n");
    }

    public static boolean isFlagEnabled(long flags, long flag) {
        return (flags & flag) == flag;
    }
}

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

package com.mobicage.rogerthat;

import android.content.Context;
import android.support.multidex.MultiDexApplication;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.Iconics;
import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.util.http.HTTPUtil;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeAsyncTask;
import com.mobicage.rogerthat.util.system.SystemUtils;
import com.mobicage.rpc.Credentials;
import com.mobicage.rpc.config.CloudConstants;

import org.altbeacon.beacon.powersave.BackgroundPowerSaver;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.jivesoftware.smack.util.Base64;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class App extends MultiDexApplication implements Thread.UncaughtExceptionHandler {

    private static Context sContext;

    @SuppressWarnings("unused")
    private BackgroundPowerSaver mBackgroundPowerSaver;

    @Override
    public void onCreate() {
        super.onCreate();

        Thread.setDefaultUncaughtExceptionHandler(this);
        sContext = this;

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/lato_regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        // Initialize the SDK before executing any other operations,
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

        // Simply constructing this class and holding a reference to it in your custom Application class enables auto
        // battery saving of about 60%
        if (android.os.Build.VERSION.SDK_INT >= 18) {
            mBackgroundPowerSaver = new BackgroundPowerSaver(this);
        }
        Iconics.registerFont(new FontAwesome());
    }

    public static Context getContext() {
        return sContext;
    }

    public static File getExceptionsDir(Context context) {
        return new File(context.getFilesDir(), "exceptions");
    }

    @SuppressWarnings("unchecked")
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        if (CloudConstants.DEBUG_LOGGING) {
            L.e("Uncaught exception", ex);
        }

        final JSONObject error = new JSONObject();
        error.put("description", "Uncaught exception");
        error.put("language", Locale.getDefault().getLanguage());
        error.put("country", Locale.getDefault().getCountry());
        error.put("platform", "1");
        error.put("timestamp", "" + System.currentTimeMillis() / 1000);

        try {
            error.put("error_message", L.getStackTraceString(ex));
        } catch (Throwable t) {
            if (CloudConstants.DEBUG_LOGGING) {
                L.e(t);
            }
            try {
                error.put("error_message", "Failed to get stacktrace of exception: " + ex);
            } catch (Throwable t2) { // too bad... just ignore
                if (CloudConstants.DEBUG_LOGGING) {
                    L.e(t2);
                }
            }
        }

        try {
            error.put("device_id", Installation.id(this));
        } catch (Throwable t) { // too bad... just ignore
            if (CloudConstants.DEBUG_LOGGING) {
                L.e(t);
            }
        }

        try {
            error.put("platform_version", "" + SystemUtils.getAndroidVersion());
        } catch (Throwable t) { // too bad... just ignore
            if (CloudConstants.DEBUG_LOGGING) {
                L.e(t);
            }
        }

        try {
            error.put("mobicage_version", MainService.getVersion(sContext));
        } catch (Throwable t) { // too bad... just ignore
            if (CloudConstants.DEBUG_LOGGING) {
                L.e(t);
            }
        }

        final File exceptionsDir = getExceptionsDir(this);
        File errorFile = new File(exceptionsDir, "" + System.currentTimeMillis());

        if (CloudConstants.DEBUG_LOGGING) {
            L.w("Writing uncaught exception to file: " + errorFile + "\n" + error);
        }

        try {
            exceptionsDir.mkdirs();

            FileWriter fw = new FileWriter(errorFile);
            try {
                error.writeJSONString(fw);
            } finally {
                fw.close();
            }
        } catch (Throwable t) {
            if (CloudConstants.DEBUG_LOGGING) {
                L.e(t);
            }
            errorFile = null;
        }

        final File theErrorFile = errorFile;

        new SafeAsyncTask<Object, Object, Object>() {
            @Override
            protected Object safeDoInBackground(Object... params) {
                boolean success = logErrorToServer(error);
                if (!success) {
                    System.exit(1);
                    return null;
                }

                if (CloudConstants.DEBUG_LOGGING) {
                    L.w("Successfully sent client error");
                }

                if (theErrorFile != null) {
                    if (CloudConstants.DEBUG_LOGGING) {
                        L.w("Removing error file " + theErrorFile);
                    }
                    theErrorFile.delete();
                }
                System.exit(1);
                return null;
            }

        }.execute();

        // Maybe the app is in a state in which it can not schedule Tasks anymore. Let's give up within 2 seconds.
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
        }
        System.exit(1);
    }

    static boolean logErrorToServer(final JSONObject error) {
        Credentials credentials = null;
        MainService mainService = MainService.getInstance();
        if (mainService != null)
            try {
                credentials = mainService.getCredentials();
            } catch (Exception e) {
                L.d("Could not load credentials", e);
            }

        final HttpPost httpPost = new HttpPost(CloudConstants.LOG_ERROR_URL);
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
        if (credentials != null) {
            httpPost.setHeader("X-MCTracker-User",
                Base64.encodeBytes(credentials.getUsername().getBytes(), Base64.DONT_BREAK_LINES));
            httpPost.setHeader("X-MCTracker-Pass",
                Base64.encodeBytes(credentials.getPassword().getBytes(), Base64.DONT_BREAK_LINES));
        }

        if (CloudConstants.DEBUG_LOGGING) {
            L.w("Sending error to " + httpPost.getURI());
        }

        final List<NameValuePair> formParams = new ArrayList<NameValuePair>();
        for (Iterator<?> iterator = error.keySet().iterator(); iterator.hasNext();) {
            String key = (String) iterator.next();
            String object = (String) error.get(key);
            formParams.add(new BasicNameValuePair(key, object));
        }

        UrlEncodedFormEntity entity;
        try {
            entity = new UrlEncodedFormEntity(formParams, HTTP.UTF_8);
        } catch (UnsupportedEncodingException e) {
            return false;
        }
        httpPost.setEntity(entity);
        try {
            HTTPUtil.getHttpClient().execute(httpPost);
        } catch (Exception e) {
            return false; // The error will be put in backlog when MainService starts
        }

        return true;
    }
}

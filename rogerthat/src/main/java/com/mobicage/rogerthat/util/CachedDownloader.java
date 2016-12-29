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
package com.mobicage.rogerthat.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.HttpClientParams;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.PowerManager;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.MainService;
import com.mobicage.rogerthat.config.Configuration;
import com.mobicage.rogerthat.config.ConfigurationProvider;
import com.mobicage.rogerthat.util.http.HTTPUtil;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeAsyncTask;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.system.SystemUtils;
import com.mobicage.rogerthat.util.system.T;

public class CachedDownloader {

    private final int DOWNLOAD_SUCCESS = 0;
    private final int DOWNLOAD_FAILED = 1;
    private final int DOWNLOAD_CANCELLED = 2;

    private class DownloadTask extends SafeAsyncTask<String, Integer, Integer> {

        private PowerManager.WakeLock mWakeLock;
        private String mUrlHash;
        private String mUrl;
        private File mFile;

        public DownloadTask(String urlHash, String url, File file) {
            mUrlHash = urlHash;
            mUrl = url;
            mFile = file;
        }

        @Override
        protected void safeOnCancelled(Integer result) {
            L.d("onCancelled: " + result);
            onPostExecute(DOWNLOAD_CANCELLED);
        }

        @Override
        protected Integer safeDoInBackground(String... params) {
            final String url = params[0];
            try {
                final HttpClient httpClient = HTTPUtil.getHttpClient(0, 0); // no timeout
                HttpClientParams.setRedirecting(httpClient.getParams(), true);
                final HttpGet httpGet = new HttpGet(url);

                if (isCancelled()) {
                    return DOWNLOAD_CANCELLED;
                }

                final HttpResponse response = httpClient.execute(httpGet);

                if (isCancelled()) {
                    return DOWNLOAD_CANCELLED;
                }

                final int statusCode = response.getStatusLine().getStatusCode();
                L.d(url + " --> status code: " + statusCode);
                if (statusCode != HttpStatus.SC_OK) {
                    return DOWNLOAD_FAILED;
                }

                final HttpEntity responseEntity = response.getEntity();
                final long fileLength = responseEntity.getContentLength();
                final InputStream input = responseEntity.getContent();
                try {
                    final OutputStream output = new FileOutputStream(mFile);
                    try {
                        final byte data[] = new byte[4096];
                        long total = 0;
                        int count;
                        while ((count = input.read(data)) != -1) {
                            if (isCancelled()) {
                                return DOWNLOAD_CANCELLED;
                            }
                            total += count;
                            if (fileLength > 0)
                                publishProgress((int) (total * 100 / fileLength));
                            output.write(data, 0, count);
                        }
                    } finally {
                        output.close();
                    }
                } finally {
                    input.close();
                }
            } catch (IOException e) {
                L.e(e);
                return DOWNLOAD_FAILED;
            }
            return DOWNLOAD_SUCCESS;
        }

        @Override
        protected void safeOnPreExecute() {
            // take CPU lock to prevent CPU from going off if the user
            // presses the power button during download
            PowerManager pm = (PowerManager) mMainService.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
            mWakeLock.acquire();
        }

        @Override
        protected void safeOnPostExecute(Integer result) {
            mWakeLock.release();
            L.d("onPostExecute: " + result);
            if (result == DOWNLOAD_FAILED) {
                removeAttachment();
            } else if (result == DOWNLOAD_CANCELLED) {
                removeAttachment();
            } else {
                Intent intent = new Intent(CACHED_DOWNLOAD_AVAILABLE_INTENT);
                intent.putExtra("hash", mUrlHash);
                intent.putExtra("url", mUrl);
                mMainService.sendBroadcast(intent);
            }
            mQueue.remove(mUrlHash);
        }

        private void removeAttachment() {
            if (mFile.exists() && !SystemUtils.deleteDir(mFile)) {
                L.bug("Could not delete cached download file. hash '" + mUrlHash + "'");
            }
        }
    }

    public static final String CONFIGKEY = "com.mobicage.rogerthat.util.CachedDownloader";
    public static final String CONFIG_LAST_CLEANUP = "lastCleanup";
    public static final String CACHED_DOWNLOAD_AVAILABLE_INTENT = "com.mobicage.rogerthat.util.CACHED_DOWNLOAD_AVAILABLE_INTENT";

    private static CachedDownloader mInstance = null;

    private boolean mExternalStorageWriteable = false;
    private MainService mMainService = null;
    private List<String> mQueue = new ArrayList<String>();

    private CachedDownloader(MainService mainService) {
        this.mMainService = mainService;
        if (IOUtils.shouldCheckExternalStorageAvailable()) {
            String state = Environment.getExternalStorageState();
            if (Environment.MEDIA_MOUNTED.equals(state)) {
                this.mExternalStorageWriteable = true;
            } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
                this.mExternalStorageWriteable = false;
            } else {
                this.mExternalStorageWriteable = false;
            }
        } else {
            this.mExternalStorageWriteable = true;
        }
    }

    public static CachedDownloader getInstance(MainService mainService) {
        if (mInstance == null) {
            mInstance = new CachedDownloader(mainService);
        }
        return mInstance;
    }

    private String getHash(String url) {
        return Security.sha256(url);
    }

    private void createDirIfNotExists(File file) throws Exception {
        T.dontCare();
        if (!file.exists()) {
            if (!file.mkdir())
                throw new Exception("Failed to create directory '" + file.getAbsolutePath() + "'");
        }
    }

    public boolean isStorageAvailable() {
        return mExternalStorageWriteable;
    }

    private File getCachedDownloadDir() {
        File file = IOUtils.getFilesDirectory(mMainService);
        try {
            createDirIfNotExists(file);
            file = new File(file, "cached");
            createDirIfNotExists(file);
            file = new File(file, "downloads");
            createDirIfNotExists(file);
            return file;
        } catch (Exception e) {
            L.d(e);
            return null;
        }
    }

    public File getCachedFilePath(final String url) {
        final String urlHash = getHash(url);
        File file = getCachedDownloadDir();
        final File file2 = new File(file, urlHash);

        if (file2.exists()) {
            updateModificationDate(file2);
            return file2;
        }

        if (mQueue.contains(urlHash)) {
            return null;
        }

        mMainService.runOnUIHandler(new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                final DownloadTask downloadTask = new DownloadTask(urlHash, url, file2);
                mQueue.add(urlHash);
                downloadTask.execute(url);
            }
        });

        return null;
    }

    private void updateModificationDate(File file) {
        file.setLastModified(System.currentTimeMillis());
    }

    public void cleanupOldCachedDownloads() {
        if (mExternalStorageWriteable) {
            File dir = getCachedDownloadDir();
            if (dir != null) {
                final long lastMonth = System.currentTimeMillis() - (30L * 86400L * 1000L);
                for (File file : dir.listFiles()) {
                    if (file.lastModified() < lastMonth) {
                        if (!file.delete()) {
                            L.bug("Failed to delete old cached file with urlHash '" + file.getName() + "'");
                        }
                    }
                }
            }
        }
        mMainService.postOnUIHandler(new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                ConfigurationProvider configProvider = mMainService.getConfigurationProvider();
                if (configProvider != null) {
                    Configuration cfg = configProvider.getConfiguration(CONFIGKEY);
                    cfg.put(CONFIG_LAST_CLEANUP, System.currentTimeMillis());
                    configProvider.updateConfigurationLater(CONFIGKEY, cfg);
                }
            }
        });
    }
}

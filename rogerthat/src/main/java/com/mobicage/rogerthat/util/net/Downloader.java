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

package com.mobicage.rogerthat.util.net;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.os.Environment;

import com.mobicage.rogerthat.MainService;
import com.mobicage.rogerthat.util.IOUtils;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.system.SystemUtils;
import com.mobicage.rogerthat.util.system.T;

// XXX: would be nice to have timeout
// XXX: should be killable

public class Downloader implements Closeable {

    private static final String TMP_DOWNLOAD_DIR = "rogerthat/downloads/tmp";
    private static final long TMP_CLEANUP_INTERVAL_MILLIS = 60 * 60 * 1000;

    private static final int BUFFER_SIZE = 4096;
    private static final long DOWNLOAD_RETRY_MILLIS = 60 * 1000;

    private final MainService mService;

    private volatile boolean mustFinish;

    private boolean mDownloadInProgress; // Owned by IO thread

    public Downloader(MainService service) {
        T.UI();
        mService = service;
        mustFinish = false;
    }

    @Override
    public void close() {
        T.UI();
        mustFinish = true;
    }

    public void scheduleDownload(final String url, final String dir, final String prefix, final String fileExtension,
        final IDownloadCallback callback) {
        T.dontCare();
        L.d("Scheduling download for " + url);
        mService.postOnIOHandler(getDownloaderRunnable(url, dir, prefix, fileExtension, callback));
    }

    private SafeRunnable getDownloaderRunnable(final String url, final String dir, final String prefix,
        final String fileExtension, final IDownloadCallback callback) {
        T.dontCare();
        return new SafeRunnable() {
            @Override
            public void safeRun() {
                scheduleDownloadFromIOThread(url, dir, prefix, fileExtension, callback);
            }
        };
    }

    private void scheduleDownloadFromIOThread(final String url, final String dir, final String prefix,
        final String fileExtension, final IDownloadCallback callback) {
        T.IO();
        if (mustFinish)
            return;
        boolean success = false;
        try {
            if (mDownloadInProgress) {
                mService.postDelayedOnIOHandler(getDownloaderRunnable(url, dir, prefix, fileExtension, callback),
                    DOWNLOAD_RETRY_MILLIS);
                return;
            }
            mDownloadInProgress = true;
            File file = downloadFile(new URL(url), dir, prefix, fileExtension);
            if (mustFinish)
                return;
            if (file != null) {
                callback.downloadReady(url, file);
                success = true;
            }
        } catch (Exception e) {
            L.d("Error during download", e);
        }
        mDownloadInProgress = false;
        if (!success) {
            callback.downloadFailed(url, IDownloadCallback.ERROR_FAILED);
        }
    }

    private File mkdirs(String dir) throws IOException {
        T.IO();
        if (!SystemUtils.isSDCardAvailable(mService)) {
            throw new IOException("SD card is not mounted");
        }

        File mydir = new File(IOUtils.getFilesDirectory(mService), dir);
        if (!mydir.isDirectory()) {
            boolean success = mydir.mkdirs();
            if (!success) {
                throw new IOException("Cannot create dir " + mydir.getAbsolutePath());
            }
        }
        return mydir;
    }

    private File createEmptyFileForDownload(String dir, String prefix, String fileExtension) throws IOException {

        T.IO();

        File mydir = mkdirs(dir);

        long now = System.currentTimeMillis();
        String filename = prefix + '.' + now + '.' + fileExtension;
        File file = new File(mydir, filename);

        if (file.exists()) {
            file.delete();
        }

        return file;
    }

    private File downloadFile(final URL url, final String dir, final String prefix, final String fileExtension) {

        T.IO();

        File file = null;
        File targetFile = null;
        boolean success = false;

        try {
            file = createEmptyFileForDownload(TMP_DOWNLOAD_DIR, prefix, fileExtension);
            final HttpURLConnection c = (HttpURLConnection) url.openConnection();
            c.setRequestMethod("GET");
            c.setDoOutput(true);
            c.connect();

            OutputStream fos = new FileOutputStream(file);
            InputStream in = c.getInputStream();

            try {
                final byte[] buffer = new byte[BUFFER_SIZE];
                int len;
                while (!mustFinish && (len = in.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
            } finally {
                try {
                    fos.close();
                } catch (IOException e) {
                    L.bug(e);
                }
                try {
                    in.close();
                } catch (IOException e) {
                    L.bug(e);
                }
            }

            if (mustFinish)
                return null;

            targetFile = new File(mkdirs(dir), file.getName());
            success = file.renameTo(targetFile);

        } catch (Exception e) {
            L.d("Error downloading file", e);
            if (file != null) {
                try {
                    file.delete();
                } catch (Exception ex) {
                    // Do nothing
                }
            }

        }

        return success ? targetFile : null;
    }

    public void scheduleCleanupTmpFolder() {
        T.UI();
        mService.postOnIOHandler(new SafeRunnable() {
            @Override
            public void safeRun() {
                if (SystemUtils.isSDCardAvailable(mService)) {
                    final File mydir = new File(IOUtils.getFilesDirectory(mService), TMP_DOWNLOAD_DIR);
                    if (mydir.isDirectory()) {
                        for (final File file : mydir.listFiles()) {
                            try {
                                L.d("Deleting file " + file.getAbsolutePath());
                                file.delete();
                            } catch (Exception e) {
                                L.d("Could not delete file " + file.getAbsolutePath(), e);
                            }
                        }
                    }
                }
                mService.postDelayedOnIOHandler(this, TMP_CLEANUP_INTERVAL_MILLIS);
            }
        });

    }

}
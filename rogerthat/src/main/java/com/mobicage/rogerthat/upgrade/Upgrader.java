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

package com.mobicage.rogerthat.upgrade;

import java.io.Closeable;
import java.io.File;
import java.io.FilenameFilter;
import java.util.StringTokenizer;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

import com.mobicage.rogerthat.MainService;
import com.mobicage.rogerthat.util.IOUtils;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.net.Downloader;
import com.mobicage.rogerthat.util.net.IDownloadCallback;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.system.SystemUtils;
import com.mobicage.rogerthat.util.system.T;

// XXX: at startup check versions in apk downloaddir and cleanup
// XXX: when download fails, we lose the knowledge that there is a new apk -- store this info & retry later

public class Upgrader implements Closeable {

    private final static String APK_DOWNLOAD_DIR = "rogerthat/downloads/apk";
    private final static String PREFIX = "mobicage";
    private final static String FILE_EXTENSION = "apk";

    private final static String INSTALL_INTENT_TYPE = "application/vnd.android.package-archive";

    private final MainService mService;
    private final Downloader mDownloader;

    public Upgrader(final MainService service) {
        T.UI();
        mService = service;
        mDownloader = new Downloader(mService);
        mDownloader.scheduleCleanupTmpFolder();
    }

    @Override
    public void close() {
        T.UI();
        mDownloader.close();
    }

    public void scheduleUpgradeToApk(final String url, final int majorVersion, final int minorVersion) {
        T.UI();
        final String prefix = PREFIX + '.' + majorVersion + '.' + minorVersion;
        mDownloader.scheduleDownload(url, APK_DOWNLOAD_DIR, prefix, FILE_EXTENSION, mDownloadCallback);
    }

    private void upgradeFromFile(final File file) {
        T.IO();
        final Uri uri = Uri.fromFile(file);
        final Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, INSTALL_INTENT_TYPE);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mService.startActivity(intent);
    }

    private IDownloadCallback mDownloadCallback = new IDownloadCallback() {

        @Override
        public void downloadFailed(final String url, final int errorCode) {
            T.IO();
            L.d("Failed to download apk from " + url);

        }

        @Override
        public void downloadReady(final String url, final File file) {
            T.IO();
            upgradeFromFile(file);
        }

    };

    public boolean mustUpgrade(final long oldMajorVersion, final long oldMinorVersion, final long newMajorVersion,
        final long newMinorVersion) {
        T.dontCare();
        boolean mustUpgrade = false;

        if (newMajorVersion > oldMajorVersion) {
            mustUpgrade = true;
        } else if ((newMajorVersion == oldMajorVersion) && (newMinorVersion > oldMinorVersion)) {
            mustUpgrade = true;
        }

        return mustUpgrade;
    }

    public void scheduleCleanupOldApks(final long pMajorVersion, final long pMinorVersion) {
        T.dontCare();
        mService.postOnIOHandler(new SafeRunnable() {
            @Override
            public void safeRun() {
                cleanupOldApks(pMajorVersion, pMinorVersion);
            }
        });
    }

    private void cleanupOldApks(final long pMajorVersion, final long pMinorVersion) {
        T.IO();
        if (SystemUtils.isSDCardAvailable(mService)) {
            final File mydir = new File(IOUtils.getFilesDirectory(mService), APK_DOWNLOAD_DIR);
            if (mydir.isDirectory()) {
                final FilenameFilter filter = new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String pathname) {
                        boolean success = false;
                        try {
                            final StringTokenizer t = new StringTokenizer(pathname, ".");
                            final String prefix = t.nextToken();
                            final long majorVersion = Long.valueOf(t.nextToken());
                            final long minorVersion = Long.valueOf(t.nextToken());
                            String extension = null;
                            while (t.hasMoreTokens()) {
                                extension = t.nextToken();
                            }
                            if (prefix.equals(PREFIX) && FILE_EXTENSION.equals(extension)
                                && !mustUpgrade(pMajorVersion, pMinorVersion, majorVersion, minorVersion)) {
                                success = true;
                            } else {
                                L.d("Not deleting file in apk folder: " + pathname);
                            }
                        } catch (Exception e) {
                            L.d(e);
                        }
                        return success;
                    }
                };
                final File[] filesToBeDeleted = mydir.listFiles(filter);
                for (final File file : filesToBeDeleted) {
                    try {
                        L.d("Deleting file " + file.getAbsolutePath());
                        file.delete();
                    } catch (Exception e) {
                        L.d("Could not delete file " + file.getAbsolutePath(), e);
                    }
                }
            }
        }
    }
}

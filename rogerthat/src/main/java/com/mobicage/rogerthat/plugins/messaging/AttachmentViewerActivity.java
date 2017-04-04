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
package com.mobicage.rogerthat.plugins.messaging;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.github.barteksc.pdfviewer.PDFView;
import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.ServiceBoundActivity;
import com.mobicage.rogerthat.util.TextUtils;
import com.mobicage.rogerthat.util.http.HTTPUtil;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeAsyncTask;
import com.mobicage.rogerthat.util.system.SystemUtils;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.ui.UIUtils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.HttpClientParams;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class AttachmentViewerActivity extends ServiceBoundActivity {

    private final int DOWNLOAD_SUCCESS = 0;
    private final int DOWNLOAD_FAILED = 1;
    private final int DOWNLOAD_CANCELLED = 2;

    private class DownloadTask extends SafeAsyncTask<String, Integer, Integer> {

        private AttachmentViewerActivity mContext;
        private PowerManager.WakeLock mWakeLock;

        public DownloadTask(AttachmentViewerActivity context) {
            this.mContext = context;
        }

        @Override
        protected void safeOnCancelled(Integer result) {
            L.d("onCancelled: " + result);
            onPostExecute(mContext.DOWNLOAD_CANCELLED);
        }

        @Override
        protected Integer safeDoInBackground(String... params) {
            final String url = params[0];
            try {
                final HttpClient httpClient = HTTPUtil.getHttpClient(0, 0); // no timeout
                HttpClientParams.setRedirecting(httpClient.getParams(), true);
                final HttpGet httpGet = new HttpGet(url);

                if (isCancelled()) {
                    return mContext.DOWNLOAD_CANCELLED;
                }

                final HttpResponse response = httpClient.execute(httpGet);

                if (isCancelled()) {
                    return mContext.DOWNLOAD_CANCELLED;
                }

                final int statusCode = response.getStatusLine().getStatusCode();
                L.d(url + " --> status code: " + statusCode);
                if (statusCode != HttpStatus.SC_OK) {
                    return mContext.DOWNLOAD_FAILED;
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
                                return mContext.DOWNLOAD_CANCELLED;
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
                return mContext.DOWNLOAD_FAILED;
            }
            return mContext.DOWNLOAD_SUCCESS;
        }

        @Override
        protected void safeOnPreExecute() {
            // take CPU lock to prevent CPU from going off if the user
            // presses the power button during download
            PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
            mWakeLock.acquire();
            mProgressDialog.show();
        }

        @Override
        protected void safeOnProgressUpdate(Integer... progress) {
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setMax(100);
            mProgressDialog.setProgress(progress[0]);
        }

        @Override
        protected void safeOnPostExecute(Integer result) {
            mWakeLock.release();
            mProgressDialog.dismiss();
            L.d("onPostExecute: " + result);
            if (result == mContext.DOWNLOAD_FAILED) {
                UIUtils.showErrorPleaseRetryDialog(mContext);
                removeAttachment();
            } else if (result == mContext.DOWNLOAD_CANCELLED) {
                removeAttachment();
                mContext.finish();
            } else {
                Toast.makeText(mContext, R.string.file_downloaded, Toast.LENGTH_SHORT).show();
                updateView(false);
            }
        }

        private void removeAttachment() {
            if (mFile.exists() && !SystemUtils.deleteDir(mFile)) {
                L.bug("Could not delete attachment file. threadKey: " + mThreadKey + " messageKey: " + mMessageKey
                    + " downloadUrl: " + mDownloadUrl);
            }
        }
    }

    public final static String CONTENT_TYPE_JPEG = "image/jpeg";
    public final static String CONTENT_TYPE_PNG = "image/png";
    public final static String CONTENT_TYPE_PDF = "application/pdf";
    public final static String CONTENT_TYPE_VIDEO_QUICKTIME = "video/quicktime"; // deprecated
    public final static String CONTENT_TYPE_VIDEO_MP4 = "video/mp4";

    private final static List<String> SUPPORTED_CONTENT_TYPES = Arrays.asList(CONTENT_TYPE_JPEG, CONTENT_TYPE_PNG,
        CONTENT_TYPE_PDF, CONTENT_TYPE_VIDEO_QUICKTIME, CONTENT_TYPE_VIDEO_MP4);

    private MessagingPlugin mMessagingPlugin;

    private String mThreadKey;
    private String mMessageKey;
    private String mContentType;
    private String mDownloadUrl;
    private String mName;
    private File mAttachmentsDir;
    private String mDownloadUrlHash;
    private boolean mGenerateThumbnail;

    private File mFile;
    private WebView mWebview;
    private VideoView mVideoview;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onPause() {
        if (mVideoview != null) {
            mVideoview.pause();
        }
        super.onPause();
    }

    @Override
    protected void onServiceBound() {
        T.UI();
        mProgressDialog = UIUtils.showProgressDialog(this, null, getString(R.string.downloading), true, true, null,
                ProgressDialog.STYLE_HORIZONTAL, false);

        mMessagingPlugin = mService.getPlugin(MessagingPlugin.class);

        final Intent intent = getIntent();
        mThreadKey = intent.getStringExtra("thread_key");
        mMessageKey = intent.getStringExtra("message");
        mContentType = intent.getStringExtra("content_type");
        mDownloadUrl = intent.getStringExtra("download_url");
        mName = intent.getStringExtra("name");
        mDownloadUrlHash = intent.getStringExtra("download_url_hash");
        mGenerateThumbnail = intent.getBooleanExtra("generate_thumbnail", false);

        if (mContentType.toLowerCase(Locale.US).startsWith("video/")) {
            setContentView(R.layout.file_viewer_video);
            mVideoview = (VideoView) findViewById(R.id.videoView);
        } else {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            setContentView(R.layout.file_viewer);
            mWebview = (WebView) findViewById(R.id.webview);

            mWebview.setWebChromeClient(new WebChromeClient() {
                @Override
                public void onConsoleMessage(String message, int lineNumber, String sourceID) {
                    if (sourceID != null) {
                        try {
                            sourceID = new File(sourceID).getName();
                        } catch (Exception e) {
                            L.d("Could not get fileName of sourceID: " + sourceID, e);
                        }
                    }
                    L.d(sourceID + ":" + lineNumber + " | " + message);
                }
            });

            mWebview.setWebViewClient(new WebViewClient() {

                @Override
                public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                    L.d(failingUrl + ":" + errorCode + " | " + description);
                }
            });

            if (!TextUtils.isEmptyOrWhitespace(mName)) {
                setTitle(mName);
            }
        }

        try {
            mAttachmentsDir = mMessagingPlugin.attachmentsDir(mThreadKey, null);
        } catch (IOException e) {
            L.d("Unable to create attachment directory", e);
            UIUtils.showDialog(this, "", R.string.unable_to_read_write_sd_card);
            return;
        }

        mFile = new File(mAttachmentsDir, mDownloadUrlHash);

        if (mMessagingPlugin.attachmentExists(mAttachmentsDir, mDownloadUrlHash)) {
            updateView(false);
        } else {
            try {
                mAttachmentsDir = mMessagingPlugin.attachmentsDir(mThreadKey, mMessageKey);
            } catch (IOException e) {
                L.d("Unable to create attachment directory", e);
                UIUtils.showDialog(this, "", R.string.unable_to_read_write_sd_card);
                return;
            }

            mFile = new File(mAttachmentsDir, mDownloadUrlHash);

            if (mMessagingPlugin.attachmentExists(mAttachmentsDir, mDownloadUrlHash)) {
                updateView(false);
            } else {
                downloadAttachment();
            }
        }
    }

    @Override
    protected void onServiceUnbound() {
        T.UI();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void finish() {
        if (mWebview != null) {
            // Stop all javascript
            mWebview.loadUrl("about:blank");
        }
        super.finish();
    }

    @SuppressLint({ "SetJavaScriptEnabled" })
    protected void updateView(boolean isUpdate) {
        T.UI();

        if (!isUpdate && mGenerateThumbnail) {
            File thumbnail = new File(mFile.getAbsolutePath() + ".thumb");
            if (!thumbnail.exists()) {
                boolean isImage = mContentType.toLowerCase(Locale.US).startsWith("image/");
                boolean isVideo = !isImage && mContentType.toLowerCase(Locale.US).startsWith("video/");
                try {
                    // Try to generate a thumbnail
                    mMessagingPlugin.createAttachmentThumbnail(mFile.getAbsolutePath(), isImage, isVideo);
                } catch (Exception e) {
                    L.e("Failed to generate attachment thumbnail", e);
                }
            }
        }

        final String fileOnDisk = "file://" + mFile.getAbsolutePath();

        if (mContentType.toLowerCase(Locale.US).startsWith("video/")) {
            MediaController mediacontroller = new MediaController(this);
            mediacontroller.setAnchorView(mVideoview);

            Uri video = Uri.parse(fileOnDisk);
            mVideoview.setMediaController(mediacontroller);
            mVideoview.setVideoURI(video);

            mVideoview.requestFocus();
            mVideoview.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mVideoview.start();
                }
            });

            mVideoview.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    L.e("Could not play video, what " + what + ", extra " + extra + ", content_type " + mContentType
                        + ", and url " + mDownloadUrl);
                    UIUtils.showErrorPleaseRetryDialog(AttachmentViewerActivity.this);
                    return true;
                }
            });

        } else if (CONTENT_TYPE_PDF.equalsIgnoreCase(mContentType)) {
            setContentView(R.layout.pdf_viewer);
            PDFView viewer = (PDFView) findViewById(R.id.pdfView);
            viewer.fromFile(mFile)
                    .enableSwipe(true)
                    .enableDoubletap(true)
                    .load();
        } else {
            WebSettings settings = mWebview.getSettings();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                settings.setAllowFileAccessFromFileURLs(true);
            }
            settings.setBuiltInZoomControls(true);
            settings.setLoadWithOverviewMode(true);
            settings.setUseWideViewPort(true);

            if (mContentType.toLowerCase(Locale.US).startsWith("image/")) {
                String html = "<html><head></head><body><img style=\"width: 100%;\" src=\"" + fileOnDisk
                    + "\"></body></html>";
                mWebview.loadDataWithBaseURL("", html, "text/html", "utf-8", "");
            } else {
                mWebview.loadUrl(fileOnDisk);
            }
        }
        L.d("File on disk: " + fileOnDisk);
    }

    public static boolean supportsContentType(String contentType) {
        return SUPPORTED_CONTENT_TYPES.contains(contentType);
    }

    private void downloadAttachment() {
        final DownloadTask downloadTask = new DownloadTask(AttachmentViewerActivity.this);
        downloadTask.execute(mDownloadUrl);
        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                L.d("mProgressDialog.setOnCancelListener");
                downloadTask.cancel(true);
            }
        });
    }
}

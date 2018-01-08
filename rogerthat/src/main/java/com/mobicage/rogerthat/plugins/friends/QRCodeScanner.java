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
package com.mobicage.rogerthat.plugins.friends;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.zxing.Result;
import com.google.zxing.client.android.QRCodeScannerHandler;
import com.google.zxing.client.android.QRCodeScannerViewfinderView;
import com.google.zxing.client.android.camera.QRCodeScannerCameraManager;
import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.MainService;
import com.mobicage.rogerthat.plugins.scan.ScanCommunication;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.system.SystemUtils;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.ui.UIUtils;

import org.json.simple.JSONValue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public abstract class QRCodeScanner {

    public static final String CAMERA_TYPE_FRONT = "front";
    public static final String CAMERA_TYPE_BACK = "back";
    public static final Set<String> CAMERA_TYPES = new HashSet<String>(Arrays.asList(new String[] {
        CAMERA_TYPE_FRONT, CAMERA_TYPE_BACK }));

    public QRCodeScannerCameraManager cameraManager;
    public QRCodeScannerHandler handler;
    public QRCodeScannerViewfinderView viewfinderView;

    protected int currentCameraId = 0;

    protected boolean scanningForQRCodes = false; // Controlled by startScanningForQRCodes / stopScanningForQRCodes
    protected boolean wasScanningForQRCodes = false; // Flag used to restore scanning after onPause / onResume sequence

    protected ActionScreenActivity activity = null;
    protected SurfaceTexture surfaceTexture = null;
    protected SurfaceHolder surfaceHolder = null;

    protected MainService mainService = null;
    protected View view = null;
    protected boolean hasSurface = false;

    private boolean mShowPreviewForQRCodes = false; // Controlled by showPreviewForQRCodes / hidePreviewForQRCodes
    private int mPreviewSizeW = 0;
    private int mPreviewSizeH = 0;
    private int mPreviewSizeSmallW = 0;
    private int mPreviewSizeSmallH = 0;

    private ScanCommunication mScanCommunication;
    private long mLastUpdatePreviewSize = 0;

    public static QRCodeScanner getInstance(final ActionScreenActivity ctx) {
        String className = QRCodeScanner.class.getPackage().getName() + ".";
        if (SystemUtils.getAndroidVersion() >= 14) {
            className += "QRCodeScannerTextureView";
        } else {
            className += "QRCodeScannerSurfaceView";
        }
        try {
            Class<?> detectorClass = Class.forName(className);
            QRCodeScanner cp = (QRCodeScanner) detectorClass.getConstructors()[0].newInstance();
            if (cp == null) {
                return null;
            }
            cp.activity = ctx;
            cp.view = cp.createView(ctx);

            final Point displaySize = UIUtils.getDisplaySize(ctx);
            L.d("displayWidth: " + displaySize.x);
            L.d("displayHeight: " + displaySize.y);

            cp.mPreviewSizeW = displaySize.x / 2;
            cp.mPreviewSizeH = displaySize.y / 2;
            cp.mPreviewSizeSmallW = displaySize.x / 5;
            cp.mPreviewSizeSmallH = displaySize.y / 5;

            L.d("mPreviewSizeSmallW: " + cp.mPreviewSizeSmallW);
            L.d("mPreviewSizeSmallH: " + cp.mPreviewSizeSmallH);
            L.d("previewSizeW: " + cp.mPreviewSizeW);
            L.d("previewSizeH: " + cp.mPreviewSizeH);

            return cp;
        } catch (Exception e) {
            L.bug("QRCodeScanner.getInstance", e);
            return null;
        }
    }

    protected void onPause() {
        wasScanningForQRCodes = scanningForQRCodes;
        if (scanningForQRCodes) {
            stopScanningForQRCodes();
        }
    }

    private SafeRunnable closeBigPreviewRunnable = new SafeRunnable() {
        @Override
        public void safeRun() {
            T.UI();
            long ctm = System.currentTimeMillis();
            if (mLastUpdatePreviewSize <= (ctm - 59800)) {
                mLastUpdatePreviewSize = ctm;
                updatePreviewSize(false);
            }
        }
    };

    protected void previewHolderClicked() {
        if (mShowPreviewForQRCodes) {
            updatePreviewSize(false);
        } else {
            updatePreviewSize(true);
        }
    }

    protected void showOpenPreviewForQrCodes() {
        updatePreviewSize(false);
    }

    protected void hideOpenPreviewForQrCodes() {
        final RelativeLayout sh = (RelativeLayout) activity.findViewById(R.id.preview_holder);
        final FrameLayout.LayoutParams lp_sh = (FrameLayout.LayoutParams) sh.getLayoutParams();
        lp_sh.leftMargin = -mPreviewSizeW;
        sh.setLayoutParams(lp_sh);
    }

    private void updatePreviewSize(final boolean isOpen) {
        final RelativeLayout sh = (RelativeLayout) activity.findViewById(R.id.preview_holder);
        final FrameLayout.LayoutParams lp_sh = (FrameLayout.LayoutParams) sh.getLayoutParams();

        final ImageView previewImg = (ImageView) activity.findViewById(R.id.preview_img);

        lp_sh.leftMargin = 0;

        if (isOpen) {
            lp_sh.height = mPreviewSizeH;
            lp_sh.width = mPreviewSizeW;
            previewImg.setImageResource(R.drawable.minus);
        } else {
            lp_sh.height = mPreviewSizeSmallH;
            lp_sh.width = mPreviewSizeSmallW;
            previewImg.setImageResource(R.drawable.plus);
        }
        sh.setLayoutParams(lp_sh);

        mShowPreviewForQRCodes = isOpen;

        mLastUpdatePreviewSize = System.currentTimeMillis();
        if (mShowPreviewForQRCodes && mainService != null) {
            mainService.postDelayedOnUIHandler(closeBigPreviewRunnable, 60000);
        }
    }

    public void setResult(int resultCode, Intent data) {
        activity.setResult(resultCode, data);
        activity.finish();
    }

    public void handleDecode(Result rawResult, Bitmap barcode) {
        stopScanningForQRCodes();

        final String content = rawResult.getText();
        L.i("Scanned QR code: " + content);

        if (content == null) {
            return;
        }

        final Map<String, Object> result = new HashMap<String, Object>();
        if (content.toLowerCase(Locale.US).startsWith("http://")
            || content.toLowerCase(Locale.US).startsWith("https://")) {
            if (mScanCommunication == null) {
                mScanCommunication = new ScanCommunication(activity.getMainService());
            }
            mScanCommunication.resolveUrl(content);
            result.put("status", "resolving");

        } else {
            result.put("status", "resolved");
        }
        result.put("content", content);
        activity.executeJS(false, "if (typeof rogerthat !== 'undefined') rogerthat._qrCodeScanned(%s)",
            JSONValue.toJSONString(result));
    }

    public void drawViewfinder() {
        viewfinderView.drawViewfinder();
    }

    protected abstract View createView(Context ctx);

    protected abstract void startCamera();

    protected abstract void startScanningForQRCodes();

    protected abstract void stopScanningForQRCodes();

}

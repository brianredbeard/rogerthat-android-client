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
package com.mobicage.rogerthat.plugins.friends;

import java.io.IOException;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;

import com.google.zxing.client.android.DecodeFormatManager;
import com.google.zxing.client.android.QRCodeScannerHandler;
import com.google.zxing.client.android.QRCodeScannerViewfinderView;
import com.google.zxing.client.android.camera.QRCodeScannerTextureViewCameraManager;
import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.T;

@TargetApi(14)
public class QRCodeScannerTextureView extends QRCodeScanner implements TextureView.SurfaceTextureListener {

    @Override
    protected View createView(final Context ctx) {
        TextureView tv = new TextureView(ctx);
        tv.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        tv.setOpaque(false);
        tv.setAlpha((float) 0.6);
        ActionScreenActivity a = (ActionScreenActivity) ctx;
        a.getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        return tv;
    }

    @Override
    protected void startCamera() {
        T.UI();
        L.i("TextureView.startCamera()");

        cameraManager = new QRCodeScannerTextureViewCameraManager(activity);
        viewfinderView = (QRCodeScannerViewfinderView) activity.findViewById(R.id.viewfinder_view);
        viewfinderView.setCameraManager(cameraManager);
        handler = null;

        TextureView tv = (TextureView) view;
        tv.setSurfaceTextureListener(this);
    }

    @Override
    protected void startScanningForQRCodes() {
        T.UI();
        L.i("TextureView.startScanningForQRCodes()");
        if (surfaceTexture == null) {
            throw new IllegalStateException("No surfaceTexture provided");
        }
        if (cameraManager.isOpen()) {
            L.d("initCamera() while already open. Ignoring...");
            return;
        }
        try {
            cameraManager.setManualCameraId(currentCameraId);
            ((QRCodeScannerTextureViewCameraManager) cameraManager).openDriver(surfaceTexture);
            if (handler == null) {
                handler = new QRCodeScannerHandler(this, DecodeFormatManager.QR_CODE_FORMATS, null, cameraManager);
            }
            scanningForQRCodes = true;

            showOpenPreviewForQrCodes();

        } catch (IOException ioe) {
            L.bug("Unexpected IOException initializing TextureView camera", ioe);
        } catch (RuntimeException e) {
            L.bug("Unexpected RuntimeException initializing TextureView camera", e);
        }
    }

    @Override
    protected void stopScanningForQRCodes() {
        T.UI();
        L.i("TextureView.stopScanningForQRCodes()");

        if (cameraManager.isOpen()) {
            L.i("closing");
            handler.quitSynchronously();
            cameraManager.closeDriver();
            handler = null;
            scanningForQRCodes = false;

            hideOpenPreviewForQrCodes();
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        L.i("onSurfaceTextureAvailable");
        if (!hasSurface) {
            hasSurface = true;
        }
        surfaceTexture = surface;

        if (wasScanningForQRCodes) {
            startScanningForQRCodes();
            wasScanningForQRCodes = false;
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        L.i("onSurfaceTextureSizeChanged");
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        L.i("onSurfaceTextureDestroyed");
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        // L.i("onSurfaceTextureUpdated");
    }
}

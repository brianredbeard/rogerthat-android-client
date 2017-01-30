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
package com.mobicage.rogerthat.plugins.friends;

import java.io.IOException;

import android.content.Context;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

import com.google.zxing.client.android.DecodeFormatManager;
import com.google.zxing.client.android.QRCodeScannerHandler;
import com.google.zxing.client.android.QRCodeScannerViewfinderView;
import com.google.zxing.client.android.camera.QRCodeScannerSurfaceViewCameraManager;
import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.T;

public class QRCodeScannerSurfaceView extends QRCodeScanner implements SurfaceHolder.Callback {

    @Override
    protected View createView(final Context ctx) {
        SurfaceView sv = new SurfaceView(ctx);
        sv.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        return sv;
    }

    @Override
    protected void startCamera() {
        T.UI();
        L.i("SurfaceView.startCamera()");

        cameraManager = new QRCodeScannerSurfaceViewCameraManager(activity);
        viewfinderView = (QRCodeScannerViewfinderView) activity.findViewById(R.id.viewfinder_view);
        viewfinderView.setCameraManager(cameraManager);
        handler = null;

        SurfaceView surfaceView = (SurfaceView) view;
        SurfaceHolder sh = surfaceView.getHolder();
        sh.addCallback(this);
        sh.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    protected void startScanningForQRCodes() {
        T.UI();
        L.i("SurfaceView.startScanningForQRCodes()");

        if (surfaceHolder == null) {
            throw new IllegalStateException("No SurfaceHolder provided");
        }
        if (cameraManager.isOpen()) {
            L.d("initCamera() while already open. Ignoring...");
            return;
        }
        try {
            cameraManager.setManualCameraId(currentCameraId);
            ((QRCodeScannerSurfaceViewCameraManager) cameraManager).openDriver(surfaceHolder);
            if (handler == null) {
                handler = new QRCodeScannerHandler(this, DecodeFormatManager.QR_CODE_FORMATS, null, cameraManager);
            }
            scanningForQRCodes = true;

            showOpenPreviewForQrCodes();

        } catch (IOException ioe) {
            L.bug("Unexpected RuntimeException initializing SurfaceView camera", ioe);
        } catch (RuntimeException e) {
            L.bug("Unexpected RuntimeException initializing SurfaceView camera", e);
        }
    }

    @Override
    protected void stopScanningForQRCodes() {
        T.UI();
        L.i("SurfaceView.closeCamera()");

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
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        L.i("surfaceChanged");
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        L.i("surfaceCreated");
        if (holder == null) {
            L.bug("*** WARNING *** surfaceCreated() gave us a null surface!");
        }
        if (!hasSurface) {
            hasSurface = true;
            surfaceHolder = holder;
        }

        if (wasScanningForQRCodes) {
            startScanningForQRCodes();
            wasScanningForQRCodes = false;
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder arg0) {
        L.i("surfaceDestroyed");
    }
}

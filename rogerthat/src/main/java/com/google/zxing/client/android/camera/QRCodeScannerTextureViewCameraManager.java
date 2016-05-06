/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.zxing.client.android.camera;

import java.io.IOException;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;

import com.google.zxing.client.android.camera.open.OpenCameraInterface;
import com.mobicage.rogerthat.util.logging.L;

/**
 * This object wraps the Camera service object and expects to be the only one talking to it. The implementation
 * encapsulates the steps needed to take preview-sized images, which are used for both preview and decoding.
 * 
 * @author dswitkin@google.com (Daniel Switkin)
 */
@SuppressLint("NewApi")
public class QRCodeScannerTextureViewCameraManager extends QRCodeScannerCameraManager {

    public QRCodeScannerTextureViewCameraManager(Context context) {
        super(context);
    }

    /**
     * Opens the camera driver and initializes the hardware parameters.
     * 
     * @param holder
     *            The surface object which the camera will draw preview frames into.
     * @throws IOException
     *             Indicates the camera driver failed to open.
     */
    public synchronized void openDriver(SurfaceTexture holder) throws IOException {
        Camera theCamera = camera;
        if (theCamera == null) {
            theCamera = OpenCameraInterface.open(requestedCameraId);
            if (theCamera == null) {
                throw new IOException();
            }
            camera = theCamera;
        }

        theCamera.setPreviewTexture(holder);
        if (!initialized) {
            initialized = true;
            configManager.initFromCameraParameters(theCamera);
        }

        Camera.Parameters parameters = theCamera.getParameters();
        String parametersFlattened = parameters == null ? null : parameters.flatten(); // Save these, temporarily
        try {
            configManager.setDesiredCameraParameters(theCamera, false);
        } catch (RuntimeException re) {
            // Driver failed
            L.e("Camera rejected parameters. Setting only minimal safe-mode parameters");
            L.i("Resetting to saved camera params: " + parametersFlattened);
            // Reset:
            if (parametersFlattened != null) {
                parameters = theCamera.getParameters();
                parameters.unflatten(parametersFlattened);
                try {
                    theCamera.setParameters(parameters);
                    configManager.setDesiredCameraParameters(theCamera, true);
                } catch (RuntimeException re2) {
                    // Well, darn. Give up
                    L.e("Camera rejected even safe-mode parameters! No configuration");
                }
            }
        }

    }
}

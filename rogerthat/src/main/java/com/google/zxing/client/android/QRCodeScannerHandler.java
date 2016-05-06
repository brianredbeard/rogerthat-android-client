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

package com.google.zxing.client.android;

import java.util.Collection;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.google.zxing.client.android.camera.QRCodeScannerCameraManager;
import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.plugins.friends.QRCodeScanner;
import com.mobicage.rogerthat.util.logging.L;

/**
 * This class handles all the messaging which comprises the state machine for capture.
 * 
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class QRCodeScannerHandler extends Handler {

    private final QRCodeScanner mQRCodeScanner;
    private final QRCodeScannerDecodeThread decodeThread;
    private State state;
    private final QRCodeScannerCameraManager cameraManager;

    private enum State {
        PREVIEW, SUCCESS, DONE
    }

    public QRCodeScannerHandler(QRCodeScanner qrCodeScanner, Collection<BarcodeFormat> decodeFormats,
        String characterSet, QRCodeScannerCameraManager cameraManager) {
        this.mQRCodeScanner = qrCodeScanner;
        decodeThread = new QRCodeScannerDecodeThread(qrCodeScanner, decodeFormats, characterSet,
            new QRCodeScannerViewfinderResultPointCallback(qrCodeScanner.viewfinderView));
        decodeThread.start();
        state = State.SUCCESS;

        // Start ourselves capturing previews and decoding.
        this.cameraManager = cameraManager;
        cameraManager.startPreview();
        restartPreviewAndDecode();
    }

    @Override
    public void handleMessage(Message message) {
        switch (message.what) {
        case R.id.restart_preview:
            L.i("Got restart preview message");
            restartPreviewAndDecode();
            break;
        case R.id.decode_succeeded:
            L.i("Got decode succeeded message");
            state = State.SUCCESS;
            Bundle bundle = message.getData();
            Bitmap barcode = bundle == null ? null : (Bitmap) bundle.getParcelable(DecodeThread.BARCODE_BITMAP);
            mQRCodeScanner.handleDecode((Result) message.obj, barcode);
            break;
        case R.id.decode_failed:
            // We're decoding as fast as possible, so when one decode fails, start another.
            state = State.PREVIEW;
            cameraManager.requestPreviewFrame(decodeThread.getHandler(), R.id.decode);
            break;
        case R.id.return_scan_result:
            L.i("Got return scan result message");
            mQRCodeScanner.setResult(Activity.RESULT_OK, (Intent) message.obj);
            break;
        }
    }

    public void quitSynchronously() {
        state = State.DONE;
        cameraManager.stopPreview();
        Message quit = Message.obtain(decodeThread.getHandler(), R.id.quit);
        quit.sendToTarget();
        try {
            // Wait at most half a second; should be enough time, and onPause() will timeout quickly
            decodeThread.join(500L);
        } catch (InterruptedException e) {
            // continue
        }

        // Be absolutely sure we don't send any queued up messages
        removeMessages(R.id.decode_succeeded);
        removeMessages(R.id.decode_failed);
    }

    private void restartPreviewAndDecode() {
        if (state == State.SUCCESS) {
            state = State.PREVIEW;
            cameraManager.requestPreviewFrame(decodeThread.getHandler(), R.id.decode);
            mQRCodeScanner.drawViewfinder();
        }
    }

}

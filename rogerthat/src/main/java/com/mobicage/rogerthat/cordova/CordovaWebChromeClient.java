/*
 * Copyright 2018 GIG Technology NV
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
 * @@license_version:1.4@@
 */

package com.mobicage.rogerthat.cordova;

import android.webkit.ConsoleMessage;

import com.mobicage.rogerthat.util.logging.L;

import org.apache.cordova.engine.SystemWebChromeClient;
import org.apache.cordova.engine.SystemWebViewEngine;

import java.io.File;

public class CordovaWebChromeClient extends SystemWebChromeClient {
    public CordovaWebChromeClient(SystemWebViewEngine parentEngine) {
        super(parentEngine);
    }

    @Override
    public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
        String message = consoleMessage.message();
        int lineNumber = consoleMessage.lineNumber();
        String sourceID = consoleMessage.sourceId();
        String level = consoleMessage.messageLevel().toString();
        if (sourceID != null) {
            try {
                sourceID = new File(sourceID).getName();
            } catch (Exception e) {
                L.d("Could not get fileName of sourceID: " + sourceID, e);
            }
        }
        L.d("[BRANDING] " + level + ": " + sourceID + ":" + lineNumber + " | " + message);
        return true;
    }
}

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

package com.mobicage.rogerthat.plugins.messaging.mfr;

import android.support.annotation.NonNull;

import com.eclipsesource.v8.V8;
import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Object;
import com.mobicage.rogerthat.util.logging.L;

public class JSConsole {
    private V8 v8;
    private V8Object json;
    private String prefix;

    JSConsole(@NonNull V8 v8, @NonNull String prefix) {
        this.v8 = v8;
        this.json = v8.getObject("JSON");
        this.prefix = prefix;
    }

    public void release() {
        this.json.release();
    }

    public void debug(final Object... objects) {
        L.d(this.stringify(objects));
    }

    public void log(final Object... objects) {
        L.i(this.stringify(objects));
    }

    public void warn(final Object... objects) {
        L.w(this.stringify(objects));
    }

    public void error(final Object... objects) {
        L.e(this.stringify(objects));
    }

    private String stringify(final Object... objects) {
        StringBuilder builder = new StringBuilder(this.prefix);
        for (Object object : objects) {
            if (object instanceof V8Object) {
                V8Object obj = (V8Object) object;
                // Check if it's an Error object, and if so log the error stack
                if (obj.contains("stack") && obj.contains("message")) {
                    builder.append(obj.get("stack"));
                } else {
                    V8Array params = new V8Array(this.v8).push(object);
                    String jsonResult = this.json.executeStringFunction("stringify", params);
                    params.release();
                    builder.append(jsonResult);
                }
            } else {
                builder.append(object);
            }
        }
        return builder.toString();
    }
}

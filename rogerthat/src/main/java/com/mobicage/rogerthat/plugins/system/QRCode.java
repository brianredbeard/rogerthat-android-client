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

package com.mobicage.rogerthat.plugins.system;

/**
 * Created by bart on 25/01/2017.
 */

public class QRCode {
    public final String name;
    public final String content;

    public QRCode(String name, String content) {
        this.name = name;
        this.content = content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        QRCode qrCode = (QRCode) o;

        if (name != null ? !name.equals(qrCode.name) : qrCode.name != null) return false;
        return content != null ? content.equals(qrCode.content) : qrCode.content == null;

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (content != null ? content.hashCode() : 0);
        return result;
    }
}

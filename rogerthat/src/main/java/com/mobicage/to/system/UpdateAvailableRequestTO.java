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

package com.mobicage.to.system;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class UpdateAvailableRequestTO implements com.mobicage.rpc.IJSONable {

    public String downloadUrl;
    public long majorVersion;
    public long minorVersion;
    public String releaseNotes;

    public UpdateAvailableRequestTO() {
    }

    public UpdateAvailableRequestTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("downloadUrl")) {
            Object val = json.get("downloadUrl");
            this.downloadUrl = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.system.UpdateAvailableRequestTO object is missing field 'downloadUrl'");
        }
        if (json.containsKey("majorVersion")) {
            Object val = json.get("majorVersion");
            this.majorVersion = ((Long) val).longValue();
        } else {
            throw new IncompleteMessageException("com.mobicage.to.system.UpdateAvailableRequestTO object is missing field 'majorVersion'");
        }
        if (json.containsKey("minorVersion")) {
            Object val = json.get("minorVersion");
            this.minorVersion = ((Long) val).longValue();
        } else {
            throw new IncompleteMessageException("com.mobicage.to.system.UpdateAvailableRequestTO object is missing field 'minorVersion'");
        }
        if (json.containsKey("releaseNotes")) {
            Object val = json.get("releaseNotes");
            this.releaseNotes = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.system.UpdateAvailableRequestTO object is missing field 'releaseNotes'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("downloadUrl", this.downloadUrl);
        obj.put("majorVersion", this.majorVersion);
        obj.put("minorVersion", this.minorVersion);
        obj.put("releaseNotes", this.releaseNotes);
        return obj;
    }

}
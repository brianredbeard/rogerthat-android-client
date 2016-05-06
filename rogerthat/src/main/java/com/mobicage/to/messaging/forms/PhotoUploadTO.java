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

package com.mobicage.to.messaging.forms;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class PhotoUploadTO implements com.mobicage.rpc.IJSONable {

    public boolean camera;
    public boolean gallery;
    public String quality;
    public String ratio;

    public PhotoUploadTO() {
    }

    public PhotoUploadTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("camera")) {
            Object val = json.get("camera");
            this.camera = ((Boolean) val).booleanValue();
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.forms.PhotoUploadTO object is missing field 'camera'");
        }
        if (json.containsKey("gallery")) {
            Object val = json.get("gallery");
            this.gallery = ((Boolean) val).booleanValue();
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.forms.PhotoUploadTO object is missing field 'gallery'");
        }
        if (json.containsKey("quality")) {
            Object val = json.get("quality");
            this.quality = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.forms.PhotoUploadTO object is missing field 'quality'");
        }
        if (json.containsKey("ratio")) {
            Object val = json.get("ratio");
            this.ratio = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.messaging.forms.PhotoUploadTO object is missing field 'ratio'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("camera", this.camera);
        obj.put("gallery", this.gallery);
        obj.put("quality", this.quality);
        obj.put("ratio", this.ratio);
        return obj;
    }

}
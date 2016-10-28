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

package com.mobicage.to.app;

import com.mobicage.rpc.IncompleteMessageException;

import java.util.LinkedHashMap;
import java.util.Map;

public class GetAppAssetResponseTO implements com.mobicage.rpc.IJSONable {

    public String kind;
    public String url;

    public GetAppAssetResponseTO() {
    }

    public GetAppAssetResponseTO(Map<String, Object> json) throws IncompleteMessageException {
        if (json.containsKey("kind")) {
            Object val = json.get("kind");
            this.kind = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.app.GetAppAssetResponseTO object is missing field 'kind'");
        }
        if (json.containsKey("url")) {
            Object val = json.get("url");
            this.url = (String) val;
        } else {
            throw new IncompleteMessageException("com.mobicage.to.app.GetAppAssetResponseTO object is missing field 'url'");
        }
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("kind", this.kind);
        obj.put("url", this.url);
        return obj;
    }

}
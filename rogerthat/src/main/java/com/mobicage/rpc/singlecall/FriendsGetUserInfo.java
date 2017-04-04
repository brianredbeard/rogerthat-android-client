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

package com.mobicage.rpc.singlecall;

import java.util.List;
import java.util.Map;

import com.mobicage.rogerthat.util.logging.L;

@SuppressWarnings("unchecked")
public class FriendsGetUserInfo extends SingleCall {

    public FriendsGetUserInfo(String function, String callBody) {
        super(function, callBody);
    }

    @Override
    public boolean isEqualToCallWithBody(String callBody) {
        List<Map<String, Object>> recipients = (List<Map<String, Object>>) request.get("recipients");
        if (recipients == null) {
            L.e("recipients is not supposed to be empty in " + this.function);
            return false;
        }

        // Return TRUE if a target is not a TRACKING target (TRACKING targets are >1000)
        for (Map<String, Object> recipient : recipients) {
            final Long target = (Long) recipient.get("target");
            return target == null || target < 1000;
        }
        return true;
    }

}

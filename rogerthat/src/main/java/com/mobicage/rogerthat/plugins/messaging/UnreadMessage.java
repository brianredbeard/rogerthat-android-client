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

package com.mobicage.rogerthat.plugins.messaging;

public class UnreadMessage {
    public String key;
    public String message;
    public String friendName;
    public String friendEmail;

    UnreadMessage(String key, String message, String friendName, String friendEmail) {
        this.key = key;
        this.message = message;
        this.friendName = friendName;
        this.friendEmail = friendEmail;
    }

}

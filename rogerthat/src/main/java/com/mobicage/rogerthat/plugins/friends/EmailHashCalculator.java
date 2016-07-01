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

package com.mobicage.rogerthat.plugins.friends;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.jivesoftware.smack.util.Base64;

import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rpc.config.AppConstants;
import com.mobicage.rpc.config.CloudConstants;

public class EmailHashCalculator {

    public static final int HASH_LENGTH = 44;

    public static byte[] calculateEmailHash(String email, final long friendType) {
        try {
            if (friendType == FriendsPlugin.FRIEND_TYPE_USER && !"rogerthat".equals(AppConstants.APP_ID)) {
                email += ":" + AppConstants.APP_ID;
            }
            final String content = String.format(CloudConstants.EMAIL_HASH_ENCRYPTION_KEY, email);
            final byte[] digest = MessageDigest.getInstance("SHA-256").digest(content.getBytes("US-ASCII"));
            final String result = Base64.encodeBytes(digest, Base64.DONT_BREAK_LINES);
            return result.replace('+', '.').replace('=', '-').replace('/', '_').getBytes("US-ASCII");
        } catch (NoSuchAlgorithmException e) {
            L.bug(e);
            return null;
        } catch (UnsupportedEncodingException e) {
            L.bug(e);
            return null;
        }
    }

}

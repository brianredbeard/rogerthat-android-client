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

package com.mobicage.rogerthat.util;

import android.net.Uri;
import android.text.*;

import com.mobicage.rpc.config.CloudConstants;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class OauthUtils {

    public static String getCallbackUrl() {
        return "oauth-" + CloudConstants.APP_ID + "://x-callback-url";
    }

    public static boolean isRedirectUriFound(String uri, String redirectUri) {
        Uri u;
        Uri r;
        try {
            u = Uri.parse(uri);
            r = Uri.parse(redirectUri);
        } catch (NullPointerException e) {
            return false;
        }
        if (u == null || r == null) {
            return false;
        }
        boolean rOpaque = r.isOpaque();
        boolean uOpaque = u.isOpaque();
        if (rOpaque != uOpaque) {
            return false;
        }
        if (rOpaque) {
            return android.text.TextUtils.equals(uri, redirectUri);
        }
        if (!android.text.TextUtils.equals(r.getScheme(), u.getScheme())) {
            return false;
        }
        if (!android.text.TextUtils.equals(r.getAuthority(), u.getAuthority())) {
            return false;
        }
        if (r.getPort() != u.getPort()) {
            return false;
        }
        if (!android.text.TextUtils.isEmpty(r.getPath()) && !android.text.TextUtils.equals(r.getPath(), u.getPath())) {
            return false;
        }
        Set<String> paramKeys = getQueryParameterNames(r);
        for (String key : paramKeys) {
            if (!android.text.TextUtils.equals(r.getQueryParameter(key), u.getQueryParameter(key))) {
                return false;
            }
        }
        String frag = r.getFragment();
        if (!android.text.TextUtils.isEmpty(frag)
                && !android.text.TextUtils.equals(frag, u.getFragment())) {
            return false;
        }
        return true;
    }

    public static Set<String> getQueryParameterNames(Uri uri) {
        if (uri.isOpaque()) {
            throw new UnsupportedOperationException("This isn't a hierarchical URI.");
        }

        String query = uri.getEncodedQuery();
        if (query == null) {
            return Collections.emptySet();
        }

        Set<String> names = new LinkedHashSet<String>();
        int start = 0;
        do {
            int next = query.indexOf('&', start);
            int end = (next == -1) ? query.length() : next;

            int separator = query.indexOf('=', start);
            if (separator > end || separator == -1) {
                separator = end;
            }

            String name = query.substring(start, separator);
            names.add(Uri.decode(name));

            // Move start to end of name
            start = end + 1;
        } while (start < query.length());

        return Collections.unmodifiableSet(names);
    }
}

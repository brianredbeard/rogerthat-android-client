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

package com.mobicage.rogerthat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract.CommonDataKinds.Email;

import com.mobicage.rogerthat.util.TextUtils;

public abstract class ContactListHelper {

    public static List<String> getEmailAddresses(Context context) {
        final Cursor cursor = context.getContentResolver().query(Email.CONTENT_URI, new String[] { Email.DATA }, null,
            null, null);
        if (cursor == null)
            return new ArrayList<String>();

        final Set<String> emails = new HashSet<String>();
        final int emailIndex = cursor.getColumnIndex(Email.DATA);
        while (cursor.moveToNext()) {
            // This would allow you get several email addresses
            String email = cursor.getString(emailIndex);
            if (!TextUtils.isEmptyOrWhitespace(email))
                emails.add(email);
        }
        cursor.close();

        List<String> emailList = new ArrayList<String>(emails);
        Collections.sort(emailList);
        return emailList;
    }

}

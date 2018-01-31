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
package com.mobicage.rogerthat.plugins.friends;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.util.TextUtils;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeRunnable;

public class PhoneContacts {

    protected static final String IS_EMAIL_CURSOR = "is_email_cursor";
    protected static final String IS_PHONE_CURSOR = "is_phone_cursor";

    protected ContentResolver mResolver;

    public class ContactMethod {
        public String label;
        public String data;
        public int kind;
        public SafeRunnable onClick;

        @Override
        public String toString() {
            return label;
        }
    }

    public PhoneContacts(ContentResolver resolver) {
        super();
        this.mResolver = resolver;
    }

    public List<ContactMethod> getEmailAddresses(Contact contact) {
        final List<ContactMethod> methods = new ArrayList<ContactMethod>();
        final Cursor cursor = mResolver.query(Email.CONTENT_URI, new String[] { Email.DATA },
            Email.CONTACT_ID + " = ?", new String[] { contact.id + "" }, null);
        if (cursor == null)
            return methods;

        try {
            if (cursor.moveToFirst()) {
                do {
                    final ContactMethod m = new ContactMethod();
                    final String email = cursor.getString(cursor.getColumnIndex(Email.DATA));
                    m.label = "E-mail: " + email;
                    m.data = email;
                    methods.add(m);
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }
        return methods;
    }

    public List<ContactMethod> getPhoneNumbers(Contact contact, Context context) {
        final List<ContactMethod> methods = new ArrayList<ContactMethod>();
        Cursor cursor = mResolver.query(Phone.CONTENT_URI, new String[] { Phone.TYPE, Phone.NUMBER }, Phone.CONTACT_ID
            + " = ?", new String[] { contact.id + "" }, null);
        if (cursor == null)
            return methods;

        try {
            if (cursor.moveToFirst()) {
                do {
                    ContactMethod m = new ContactMethod();
                    m.kind = cursor.getInt(cursor.getColumnIndex(Phone.TYPE));
                    final String number = cursor.getString(cursor.getColumnIndex(Phone.NUMBER));
                    m.label = "SMS: " + number;
                    switch (m.kind) {
                    case Phone.TYPE_MOBILE:
                        m.label += " " + context.getString(R.string.label_mobile);
                        break;
                    case Phone.TYPE_HOME:
                        m.label += " " + context.getString(R.string.label_home);
                        break;
                    case Phone.TYPE_WORK:
                        m.label += " " + context.getString(R.string.label_work);
                        break;
                    }
                    m.data = number;
                    methods.add(m);
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }
        return methods;
    }

    public Cursor getContactsPerPhone(List<Integer> filteredOutContactIds) {
        String[] projection = new String[] { Phone.CONTACT_ID, Contacts.DISPLAY_NAME, Contacts.PHOTO_ID, Phone._ID,
            Phone.NUMBER };

        StringBuilder f = new StringBuilder();
        f.append(Contacts.DISPLAY_NAME).append(" is not null and ").append(Phone.NUMBER).append(" is not null");
        if (filteredOutContactIds != null)
            for (int id : filteredOutContactIds)
                f.append(" and ").append(Phone.CONTACT_ID).append(" != ").append(id);

        Cursor cursor = mResolver.query(Phone.CONTENT_URI, projection, f.toString(), null, Contacts.DISPLAY_NAME
            + " COLLATE NOCASE");
        if (cursor != null)
            cursor.getExtras().putBoolean(IS_PHONE_CURSOR, true);
        return cursor;
    }

    public Cursor getContactsPerEmail(String[] filteredOutEmails, List<Integer> filteredOutContactIds) {
        StringBuilder f = new StringBuilder();
        f.append(Contacts.DISPLAY_NAME).append(" is not null and ").append(Email.DATA).append(" is not null and ")
            .append(Email.DATA).append(" != ''");
        if (filteredOutEmails != null)
            for (int i = 0; i < filteredOutEmails.length; i++)
                f.append(" and ").append(Email.DATA).append(" != ?");

        if (filteredOutContactIds != null)
            for (int id : filteredOutContactIds)
                f.append(" and ").append(Email.CONTACT_ID).append(" != ").append(id);

        String[] projection = new String[] { Email.CONTACT_ID, Contacts.DISPLAY_NAME, Contacts.PHOTO_ID, Email._ID,
            Email.DATA };

        Cursor cursor = mResolver.query(Email.CONTENT_URI, projection, f.toString(), filteredOutEmails,
            Contacts.DISPLAY_NAME + " COLLATE NOCASE");
        if (cursor != null)
            cursor.getExtras().putBoolean(IS_EMAIL_CURSOR, true);
        return cursor;
    }

    public Cursor getContactByEmail(String email) {
        String[] projection = new String[] { Email.CONTACT_ID, Contacts.DISPLAY_NAME, Contacts.PHOTO_ID, Email._ID,
            Email.DATA };

        Cursor c = mResolver.query(Email.CONTENT_URI, projection, Email.DATA + " = ?", new String[] { email },
            Contacts.DISPLAY_NAME + " COLLATE NOCASE");
        if (c != null)
            c.getExtras().putBoolean(IS_EMAIL_CURSOR, true);
        return c;
    }

    public String getPhoneContactsSortField() {
        return Contacts.DISPLAY_NAME;
    }

    public Contact fromCursor(Cursor cursor) {
        Contact contact = new Contact();
        contact.name = cursor.getString(cursor.getColumnIndex(Contacts.DISPLAY_NAME));
        contact.avatarId = cursor.getLong(cursor.getColumnIndex(Contacts.PHOTO_ID));

        if (cursor.getExtras().getBoolean(IS_EMAIL_CURSOR, false)) {
            contact.email = cursor.getString(cursor.getColumnIndex(Email.DATA));
            if (TextUtils.isEmptyOrWhitespace(contact.email)) {
                L.bug("Found a contact with email '" + contact.email + "' (name=" + contact.name + ", id=" + contact.id
                    + ")");
            }
            contact.id = cursor.getInt(cursor.getColumnIndex(Email.CONTACT_ID));
        } else if (cursor.getExtras().getBoolean(IS_PHONE_CURSOR, false)) {
            contact.primaryPhoneNumber = cursor.getString(cursor.getColumnIndex(Phone.NUMBER));
            contact.id = cursor.getInt(cursor.getColumnIndex(Phone.CONTACT_ID));
        } else {
            contact.id = cursor.getInt(cursor.getColumnIndex(Contacts._ID));
        }
        return contact;
    }

    public Cursor getAllContacts() {
        String[] projection = new String[] { Contacts._ID, Contacts.DISPLAY_NAME, Contacts.PHOTO_ID };
        Uri contacts = Contacts.CONTENT_URI;
        return mResolver.query(contacts, projection, Contacts.DISPLAY_NAME + " is not null", null,
            Contacts.DISPLAY_NAME + " COLLATE NOCASE");
    }

    public Bitmap getAvatar(Contact contact) {
        if (contact.avatarId > 0) {
            String[] projection = new String[] { Photo.PHOTO };
            Uri uri = ContentUris.withAppendedId(Data.CONTENT_URI, contact.avatarId);
            Cursor c = mResolver.query(uri, projection, null, null, null);
            if (c != null) {
                try {
                    if (c.moveToFirst()) {
                        byte[] photoBytes = c.getBlob(0);
                        if (photoBytes != null) {
                            return BitmapFactory.decodeByteArray(photoBytes, 0, photoBytes.length);
                        }
                    }
                } finally {
                    c.close();
                }
            }
        }

        if (contact.id > 0) {
            Uri uri = ContentUris.withAppendedId(Contacts.CONTENT_URI, contact.id);
            InputStream input = Contacts.openContactPhotoInputStream(mResolver, uri);
            if (input != null) {
                return BitmapFactory.decodeStream(input);
            }
        }

        return null;
    }

}

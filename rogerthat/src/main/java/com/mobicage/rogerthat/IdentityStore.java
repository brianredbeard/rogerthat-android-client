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

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.util.db.DatabaseManager;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.to.friends.GetAvatarRequestTO;
import com.mobicage.to.system.GetIdentityQRCodeRequestTO;
import com.mobicage.to.system.IdentityTO;

import java.io.Closeable;

public class IdentityStore implements Closeable {

    public final static String IDENTITY_CHANGED_INTENT = "com.mobicage.rogerthat.plugins.identity.CHANGED";

    private final SQLiteDatabase mDb;
    private final MainService mMainService;

    private volatile MyIdentity mIdentityCache = null;
    private final Object mIdentityLock = new Object();

    private final SQLiteStatement mUpdate;
    private final SQLiteStatement mUpdateAvatar;
    private final SQLiteStatement mUpdateQR;

    private final SQLiteStatement mUpdateShortUrl;

    public IdentityStore(final DatabaseManager databaseManager, final MainService mainService, final String forceEmail) {
        T.UI();
        mDb = databaseManager.getDatabase();
        mMainService = mainService;

        mUpdate = mDb.compileStatement(mMainService.getString(R.string.sql_update_identity));
        mUpdateAvatar = mDb.compileStatement(mMainService.getString(R.string.sql_update_identity_avatar));
        mUpdateQR = mDb.compileStatement(mMainService.getString(R.string.sql_update_identity_qr_code));
        mUpdateShortUrl = mDb.compileStatement(mMainService.getString(R.string.sql_update_identity_short_url));

        if (forceEmail != null) {
            // we have just registered and get our own email address
            mUpdate.bindString(1, forceEmail);
            mUpdate.bindString(2, forceEmail); // use email as name
            mUpdate.bindLong(4, -1); // set dummy avatar_id
            mUpdate.bindNull(5); // birthday
            mUpdate.bindNull(6); // gender
            mUpdate.bindNull(7); // profileData
            mUpdate.execute();
        }
    }

    public void refreshIdentity() {
        final GetIdentityResponseHandler rh1 = new GetIdentityResponseHandler();
        try {
            com.mobicage.api.system.Rpc.getIdentity(rh1, null);
        } catch (Exception e) {
            L.bug(e);
        }
    }

    public void updateIdentity(final IdentityTO identity, final String shortUrl) {
        updateIdentity(identity, shortUrl, true);
    }

    public void updateIdentity(final IdentityTO identity, final String shortUrl, final boolean updateAvatar) {
        T.BIZZ();

        mUpdate.bindString(1, identity.email);
        if (identity.name != null)
            mUpdate.bindString(2, identity.name);
        else
            mUpdate.bindNull(2);
        if (identity.qualifiedIdentifier != null)
            mUpdate.bindString(3, identity.qualifiedIdentifier);
        else
            mUpdate.bindNull(3);
        mUpdate.bindLong(4, identity.avatarId);

        if (identity.hasBirthdate)
            mUpdate.bindLong(5, identity.birthdate);
        else
            mUpdate.bindNull(5);

        if (identity.hasGender)
            mUpdate.bindLong(6, identity.gender);
        else
            mUpdate.bindNull(6);

        if (identity.profileData != null)
            mUpdate.bindString(7, identity.profileData);
        else
            mUpdate.bindNull(7);

        mUpdate.execute();

        updateShortUrl(shortUrl);

        clearCache();
        broadcastIdentityChangedIntent();

        if (identity.avatarId != -1 && updateAvatar) {
            // XXX: can be optimized if we use avatar hash to determine if avatar has changed
            mMainService.postOnUIHandler(new SafeRunnable() {
                @Override
                protected void safeRun() throws Exception {
                    T.UI();
                    final GetAvatarRequestTO request = new GetAvatarRequestTO();
                    request.avatarId = identity.avatarId;
                    request.size = 50 * mMainService.getScreenScale();
                    final GetMyOwnAvatarResponseHandler handler = new GetMyOwnAvatarResponseHandler();
                    try {
                        com.mobicage.api.friends.Rpc.getAvatar(handler, request);
                    } catch (Exception e) {
                        L.d(e);
                    }
                }
            });
        }

    }

    public void updateShortUrl(final String shortUrl) {
        if (shortUrl != null) {
            mUpdateShortUrl.bindString(1, shortUrl);
            mUpdateShortUrl.execute();

            clearCache();
        }
    }

    public void setAvatar(byte[] avatarBytes) {
        T.BIZZ();
        mUpdateAvatar.bindBlob(1, avatarBytes);
        mUpdateAvatar.execute();
        clearCache();
        broadcastIdentityChangedIntent();
    }

    public void setQR(byte[] qrBytes, String shortUrl) {
        T.BIZZ();
        mUpdateQR.bindBlob(1, qrBytes);
        mUpdateQR.bindString(2, shortUrl);
        mUpdateQR.execute();

        clearCache();
        broadcastIdentityChangedIntent();
    }

    private void broadcastIdentityChangedIntent() {
        T.dontCare();
        Intent intent = new Intent(IDENTITY_CHANGED_INTENT);
        mMainService.sendBroadcast(intent);
    }

    public MyIdentity getIdentity() {
        T.dontCare();
        synchronized (mIdentityLock) {
            if (mIdentityCache != null)
                return mIdentityCache;
        }

        boolean lazyLoadQR = false;
        MyIdentity tmpIdentity;
        final Cursor curs = mDb.rawQuery(mMainService.getString(R.string.sql_get_identity), null);
        try {
            if (!curs.moveToFirst()) {
                L.bug("No identity found in database");
                return null;
            }
            final String email = curs.getString(0);
            final String name = curs.getString(1);
            final byte[] avatarBytes = curs.getBlob(2);
            final byte[] qrBytes = curs.getBlob(3);
            final String shortLink = curs.getString(4);
            final String qualifiedIdentifier = curs.getString(5);
            final long avatarId = curs.getLong(6);
            Long birthdate = null;
            if (!curs.isNull(7))
                birthdate = curs.getLong(7);
            Integer gender = null;
            if (!curs.isNull(8))
                gender = curs.getInt(8);
            String profileData = curs.getString(9);
            if ((qrBytes == null || shortLink == null) && !"dummy".equals(email))
                lazyLoadQR = true;
            tmpIdentity = new MyIdentity(email, name, avatarBytes, qrBytes, shortLink, qualifiedIdentifier, avatarId,
                birthdate, gender, profileData, mMainService);
        } finally {
            curs.close();
        }

        if (lazyLoadQR)
            refreshQR(tmpIdentity.getEmail());

        mIdentityCache = tmpIdentity;

        return tmpIdentity;
    }

    private void clearCache() {
        synchronized (mIdentityLock) {
            mIdentityCache = null;
        }
    }

    public void refreshQR(final String myEmail) {
        mMainService.postOnUIHandler(new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                final GetIdentityQRCodeRequestTO req2 = new GetIdentityQRCodeRequestTO();
                req2.email = myEmail;
                final GetIdentityQRCodeResponseHandler rh2 = new GetIdentityQRCodeResponseHandler();
                com.mobicage.api.system.Rpc.getIdentityQRCode(rh2, req2);
            }
        });
    }

    @Override
    public void close() {
        T.UI();
        mUpdate.close();
        mUpdateAvatar.close();
        mUpdateQR.close();
        mUpdateShortUrl.close();
    }

}

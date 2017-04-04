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

package com.mobicage.rogerthat.plugins.trackme;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.MainService;
import com.mobicage.rogerthat.util.TextUtils;
import com.mobicage.rogerthat.util.db.DatabaseManager;
import com.mobicage.rogerthat.util.db.TransactionHelper;
import com.mobicage.rogerthat.util.db.TransactionWithoutResult;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.to.beacon.BeaconRegionTO;

public class TrackmeStore implements Closeable {

    private final SQLiteStatement mInsertBeaconDiscovery;
    private final SQLiteStatement mGetBeaconDiscovery;
    private final SQLiteStatement mUpdateBeaconDiscovery;
    private final SQLiteStatement mDeleteBeaconDiscoveryByEmail;
    private final SQLiteStatement mDeleteBeaconDiscoveryByUUIDAndName;
    private final SQLiteStatement mClearBeaconRegions;
    private final SQLiteStatement mInsertBeaconRegion;

    private final MainService mMainService;
    private final SQLiteDatabase mDb;

    public TrackmeStore(final MainService mainService, final DatabaseManager dbManager) {
        T.UI();
        mMainService = mainService;
        mDb = dbManager.getDatabase();

        mInsertBeaconDiscovery = mDb.compileStatement(mMainService.getString(R.string.sql_insert_beacon_discovery));
        mGetBeaconDiscovery = mDb.compileStatement(mMainService.getString(R.string.sql_get_beacon_discovery));
        mUpdateBeaconDiscovery = mDb.compileStatement(mMainService.getString(R.string.sql_update_beacon_discovery));
        mDeleteBeaconDiscoveryByEmail = mDb.compileStatement(mMainService
            .getString(R.string.sql_delete_beacon_discovery_by_email));
        mDeleteBeaconDiscoveryByUUIDAndName = mDb.compileStatement(mMainService
            .getString(R.string.sql_delete_beacon_discovery_by_uuid_and_name));

        mClearBeaconRegions = mDb.compileStatement(mMainService.getString(R.string.sql_clear_beacon_regions));
        mInsertBeaconRegion = mDb.compileStatement(mMainService.getString(R.string.sql_insert_beacon_region));
    }

    @Override
    public void close() {
        T.UI();
        mInsertBeaconDiscovery.close();
        mGetBeaconDiscovery.close();
        mUpdateBeaconDiscovery.close();
        mDeleteBeaconDiscoveryByEmail.close();
        mDeleteBeaconDiscoveryByUUIDAndName.close();

        mClearBeaconRegions.close();
        mInsertBeaconRegion.close();
    }

    public void saveBeaconDiscovery(String uuid, String beaconName) {
        T.dontCare();
        final long timestamp = mMainService.currentTimeMillis() / 1000;
        mInsertBeaconDiscovery.bindString(1, uuid);
        mInsertBeaconDiscovery.bindString(2, beaconName);
        mInsertBeaconDiscovery.bindLong(3, timestamp);
        mInsertBeaconDiscovery.execute();
    }

    public boolean beaconDiscoveryExists(String uuid, String beaconName) {
        T.dontCare();
        Cursor bc = mDb.rawQuery(mMainService.getString(R.string.sql_get_beacon_discovery), new String[] { uuid,
            beaconName });
        try {
            if (!bc.moveToFirst())
                return false;
        } finally {
            bc.close();
        }
        return true;
    }

    public void updateBeaconDiscovery(String uuid, String beaconName, String friendEmail, String tag) {
        T.dontCare();
        if (!TextUtils.isEmptyOrWhitespace(friendEmail))
            mUpdateBeaconDiscovery.bindString(1, friendEmail);
        else
            mUpdateBeaconDiscovery.bindNull(1);
        if (!TextUtils.isEmptyOrWhitespace(tag))
            mUpdateBeaconDiscovery.bindString(2, tag);
        else
            mUpdateBeaconDiscovery.bindNull(2);
        mUpdateBeaconDiscovery.bindString(3, uuid);
        mUpdateBeaconDiscovery.bindString(4, beaconName);
        mUpdateBeaconDiscovery.execute();
    }

    public void deleteBeaconDiscovery(String friendEmail) {
        T.dontCare();
        mDeleteBeaconDiscoveryByEmail.bindString(1, friendEmail);
        mDeleteBeaconDiscoveryByEmail.execute();
    }

    public void deleteBeaconDiscovery(String uuid, String beaconName) {
        T.dontCare();
        mDeleteBeaconDiscoveryByUUIDAndName.bindString(1, uuid);
        mDeleteBeaconDiscoveryByUUIDAndName.bindString(2, beaconName);
        mDeleteBeaconDiscoveryByUUIDAndName.execute();
    }

    public Map<String, Object> getFriendConnectedToBeaconDiscovery(String uuid, String beaconName) {
        T.dontCare();
        Map<String, Object> fc = new HashMap<String, Object>();
        Cursor bc = mDb.rawQuery(mMainService.getString(R.string.sql_get_friend_connected_on_beacon_discovery),
            new String[] { uuid, beaconName });
        try {
            if (!bc.moveToFirst())
                return null;
            fc.put("email", bc.getString(0));
            fc.put("tag", bc.getString(1));
            fc.put("callbacks", bc.getLong(2));
        } finally {
            bc.close();
        }
        return fc;
    }

    public void setBeaconsRegions(final BeaconRegionTO[] regions) {
        TransactionHelper.runInTransaction(mDb, "setBeaconsRegions", new TransactionWithoutResult() {

            @Override
            protected void run() {
                mClearBeaconRegions.execute();
                for (BeaconRegionTO b : regions) {
                    mInsertBeaconRegion.bindString(1, b.uuid);

                    if (b.has_major)
                        mInsertBeaconRegion.bindLong(2, b.major);
                    else
                        mInsertBeaconRegion.bindNull(2);

                    if (b.has_minor)
                        mInsertBeaconRegion.bindLong(3, b.minor);
                    else
                        mInsertBeaconRegion.bindNull(3);

                    mInsertBeaconRegion.execute();
                }
            }
        });
    }

    public List<BeaconRegion> getBeaconRegions() {
        final List<BeaconRegion> regions = new ArrayList<BeaconRegion>();

        Cursor bc = mDb.rawQuery(mMainService.getString(R.string.sql_get_beacon_regions), null);
        try {
            if (!bc.moveToFirst())
                return regions;
            while (true) {
                BeaconRegion br = new BeaconRegion();
                br.uuid = bc.getString(0);
                br.has_major = !bc.isNull(1);
                br.major = br.has_major ? bc.getLong(1) : -1;
                br.has_minor = !bc.isNull(2);
                br.minor = br.has_minor ? bc.getLong(2) : -1;
                regions.add(br);
                if (!bc.moveToNext())
                    break;
            }

        } finally {
            bc.close();
        }
        return regions;
    }
}

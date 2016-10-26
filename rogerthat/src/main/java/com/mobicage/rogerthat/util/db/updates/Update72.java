package com.mobicage.rogerthat.util.db.updates;

import android.database.sqlite.SQLiteDatabase;

import com.mobicage.rogerthat.MainService;
import com.mobicage.rogerthat.plugins.friends.FriendsPlugin;
import com.mobicage.rogerthat.plugins.system.SystemPlugin;


public class Update72 implements IDbUpdater {

    @Override
    public void preUpdate(MainService mainService, SQLiteDatabase db) {
    }

    @Override
    public void postUpdate(MainService mainService, SQLiteDatabase db) {
        mainService.registerPluginDBUpdate(FriendsPlugin.class,
                FriendsPlugin.FRIENDS_PLUGIN_MUST_DO_FULL_REFRESH_INTENT);
        mainService.registerPluginDBUpdate(SystemPlugin.class, SystemPlugin.SYSTEM_PLUGIN_MUST_DOWNLOAD_ASSETS);
    }
}

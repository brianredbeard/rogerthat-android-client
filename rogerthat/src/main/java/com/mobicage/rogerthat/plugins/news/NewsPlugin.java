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

package com.mobicage.rogerthat.plugins.news;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.view.ViewGroup;

import com.mobicage.api.messaging.Rpc;
import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.HomeActivity;
import com.mobicage.rogerthat.MainActivity;
import com.mobicage.rogerthat.MainService;
import com.mobicage.rogerthat.ServiceBound;
import com.mobicage.rogerthat.config.Configuration;
import com.mobicage.rogerthat.config.ConfigurationProvider;
import com.mobicage.rogerthat.plugins.MobicagePlugin;
import com.mobicage.rogerthat.plugins.friends.Friend;
import com.mobicage.rogerthat.plugins.friends.FriendsPlugin;
import com.mobicage.rogerthat.plugins.history.HistoryStore;
import com.mobicage.rogerthat.plugins.messaging.AlertManager;
import com.mobicage.rogerthat.plugins.messaging.AttachmentDownload;
import com.mobicage.rogerthat.plugins.messaging.BrandingMgr;
import com.mobicage.rogerthat.plugins.messaging.FriendsThreadActivity;
import com.mobicage.rogerthat.plugins.messaging.GetConversationResponseHandler;
import com.mobicage.rogerthat.plugins.messaging.LockMessageResponseHandler;
import com.mobicage.rogerthat.plugins.messaging.Message;
import com.mobicage.rogerthat.plugins.messaging.MessageHistory;
import com.mobicage.rogerthat.plugins.messaging.MessageUpdateNotAllowedException;
import com.mobicage.rogerthat.plugins.messaging.MessagingCallReceiver;
import com.mobicage.rogerthat.plugins.messaging.SendMessageResponseHandler;
import com.mobicage.rogerthat.plugins.messaging.ServiceMessageDetailActivity;
import com.mobicage.rogerthat.plugins.messaging.ServiceThreadActivity;
import com.mobicage.rogerthat.plugins.messaging.StartFlowRequest;
import com.mobicage.rogerthat.plugins.messaging.UploadChunkResponseHandler;
import com.mobicage.rogerthat.plugins.messaging.mfr.EmptyStaticFlowException;
import com.mobicage.rogerthat.plugins.messaging.mfr.JsMfr;
import com.mobicage.rogerthat.plugins.messaging.mfr.MessageFlowRun;
import com.mobicage.rogerthat.plugins.messaging.widgets.Widget;
import com.mobicage.rogerthat.util.IOUtils;
import com.mobicage.rogerthat.util.Security;
import com.mobicage.rogerthat.util.TextUtils;
import com.mobicage.rogerthat.util.ZipUtils;
import com.mobicage.rogerthat.util.db.DatabaseManager;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeBroadcastReceiver;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.system.SystemUtils;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.ui.ImageHelper;
import com.mobicage.rogerthat.util.ui.SendMessageView;
import com.mobicage.rogerthat.util.ui.UIUtils;
import com.mobicage.rpc.CallReceiver;
import com.mobicage.rpc.IJSONable;
import com.mobicage.rpc.IncompleteMessageException;
import com.mobicage.rpc.ResponseHandler;
import com.mobicage.rpc.config.CloudConstants;
import com.mobicage.rpc.http.HttpCommunicator;
import com.mobicage.to.messaging.AttachmentTO;
import com.mobicage.to.messaging.ButtonTO;
import com.mobicage.to.messaging.DeleteConversationRequestTO;
import com.mobicage.to.messaging.DeleteConversationResponseTO;
import com.mobicage.to.messaging.GetConversationRequestTO;
import com.mobicage.to.messaging.LockMessageRequestTO;
import com.mobicage.to.messaging.MarkMessagesAsReadRequestTO;
import com.mobicage.to.messaging.MarkMessagesAsReadResponseTO;
import com.mobicage.to.messaging.MemberStatusTO;
import com.mobicage.to.messaging.MemberStatusUpdateRequestTO;
import com.mobicage.to.messaging.MessageLockedRequestTO;
import com.mobicage.to.messaging.MessageTO;
import com.mobicage.to.messaging.SendMessageRequestTO;
import com.mobicage.to.messaging.StartFlowRequestTO;
import com.mobicage.to.messaging.UpdateMessageRequestTO;
import com.mobicage.to.messaging.UploadChunkRequestTO;
import com.mobicage.to.messaging.forms.AdvancedOrderWidgetResultTO;
import com.mobicage.to.messaging.forms.FloatListWidgetResultTO;
import com.mobicage.to.messaging.forms.FloatWidgetResultTO;
import com.mobicage.to.messaging.forms.LocationWidgetResultTO;
import com.mobicage.to.messaging.forms.LongWidgetResultTO;
import com.mobicage.to.messaging.forms.MyDigiPassWidgetResultTO;
import com.mobicage.to.messaging.forms.SubmitPhotoUploadFormRequestTO;
import com.mobicage.to.messaging.forms.SubmitPhotoUploadFormResponseTO;
import com.mobicage.to.messaging.forms.UnicodeListWidgetResultTO;
import com.mobicage.to.messaging.forms.UnicodeWidgetResultTO;
import com.mobicage.to.messaging.jsmfr.FlowStartedRequestTO;
import com.mobicage.to.news.GetNewsItemsRequestTO;
import com.mobicage.to.news.GetNewsRequestTO;
import com.mobicage.to.system.SettingsTO;

import org.jivesoftware.smack.util.Base64;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.JSONValue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class NewsPlugin implements MobicagePlugin {

    public static final String GET_NEWS_RECEIVED_INTENT = "com.mobicage.rogerthat.plugins.news.GET_NEWS_RECEIVED_INTENT";
    public static final String GET_NEWS_ITEMS_RECEIVED_INTENT = "com.mobicage.rogerthat.plugins.news.GET_NEWS_ITEMS_RECEIVED_INTENT";

    private final MainService mMainService;
    private final NewsStore mStore;
    private NewsCallReceiver mNewsCallReceiver;

    public NewsPlugin(final MainService pMainService, final DatabaseManager pDatabaseManager) {
        T.UI();
        mMainService = pMainService;
        mStore = new NewsStore(pDatabaseManager, pMainService);
    }

    @Override
    public void destroy() {
        T.UI();
        try {
            mStore.close();
        } catch (IOException e) {
            L.bug(e);
        }
        CallReceiver.comMobicageCapiNewsIClientRpc = null;
    }

    @Override
    public void initialize() {
        T.UI();
        reconfigure();

        mNewsCallReceiver = new NewsCallReceiver(mMainService, this);
        CallReceiver.comMobicageCapiNewsIClientRpc = mNewsCallReceiver;
    }

    @Override
    public void reconfigure() {
        T.UI();
    }

    @Override
    public void processSettings(SettingsTO settings) {
        // not used
    }

    public NewsStore getStore() {
        return mStore;
    }

    public void getNews() {
        SafeRunnable runnable = new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                final GetNewsResponseHandler responseHandler = new GetNewsResponseHandler();
                responseHandler.setUUID("todo ruben");

                GetNewsRequestTO request = new GetNewsRequestTO();
                request.cursor = null;

                com.mobicage.api.news.Rpc.getNews(responseHandler, request);
            }
        };

        if (com.mobicage.rogerthat.util.system.T.getThreadType() == com.mobicage.rogerthat.util.system.T.BIZZ) {
            runnable.run();
        } else {
            mMainService.postAtFrontOfBIZZHandler(runnable);
        }
    }

    public void getNewsItems(final long[] ids) {
        SafeRunnable runnable = new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                final GetNewsItemsResponseHandler responseHandler = new GetNewsItemsResponseHandler();
                GetNewsItemsRequestTO request = new GetNewsItemsRequestTO();
                request.ids = ids;

                com.mobicage.api.news.Rpc.getNewsItems(responseHandler, request);
            }
        };

        if (com.mobicage.rogerthat.util.system.T.getThreadType() == com.mobicage.rogerthat.util.system.T.BIZZ) {
            runnable.run();
        } else {
            mMainService.postAtFrontOfBIZZHandler(runnable);
        }
    }
}

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

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.config.Configuration;
import com.mobicage.rogerthat.config.ConfigurationProvider;
import com.mobicage.rogerthat.plugins.MobicagePlugin;
import com.mobicage.rogerthat.plugins.friends.FriendsPlugin;
import com.mobicage.rogerthat.plugins.history.HistoryPlugin;
import com.mobicage.rogerthat.plugins.messaging.BrandingMgr;
import com.mobicage.rogerthat.plugins.messaging.MessagingPlugin;
import com.mobicage.rogerthat.plugins.news.NewsPlugin;
import com.mobicage.rogerthat.plugins.payment.PaymentPlugin;
import com.mobicage.rogerthat.plugins.security.SecurityPlugin;
import com.mobicage.rogerthat.plugins.system.SystemPlugin;
import com.mobicage.rogerthat.plugins.trackme.TrackmePlugin;
import com.mobicage.rogerthat.upgrade.Upgrader;
import com.mobicage.rogerthat.util.CachedDownloader;
import com.mobicage.rogerthat.util.GoogleServicesUtils;
import com.mobicage.rogerthat.util.IOUtils;
import com.mobicage.rogerthat.util.TextUtils;
import com.mobicage.rogerthat.util.consent.ConsentProvider;
import com.mobicage.rogerthat.util.db.DatabaseManager;
import com.mobicage.rogerthat.util.geo.GeoLocationProvider;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.net.NetworkConnectivityManager;
import com.mobicage.rogerthat.util.security.SecurityUtils;
import com.mobicage.rogerthat.util.security.ed25519.Ed25519;
import com.mobicage.rogerthat.util.system.SafeAsyncTask;
import com.mobicage.rogerthat.util.system.SafeBroadcastReceiver;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.system.SystemUtils;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.ui.UIUtils;
import com.mobicage.rpc.CallReceiver;
import com.mobicage.rpc.Credentials;
import com.mobicage.rpc.DefaultRpcHandler;
import com.mobicage.rpc.IRequestSubmitter;
import com.mobicage.rpc.IResponseHandler;
import com.mobicage.rpc.PriorityMap;
import com.mobicage.rpc.ResponseHandler;
import com.mobicage.rpc.Rpc;
import com.mobicage.rpc.SDCardLogger;
import com.mobicage.rpc.SaveSettingsResponseHandler;
import com.mobicage.rpc.config.AppConstants;
import com.mobicage.rpc.config.CloudConstants;
import com.mobicage.rpc.http.HttpBacklog;
import com.mobicage.rpc.http.HttpBacklogItem;
import com.mobicage.rpc.http.HttpCommunicator;
import com.mobicage.rpc.newxmpp.XMPPKickChannel;
import com.mobicage.to.app.EmbeddedAppTO;
import com.mobicage.to.app.UpdateAppAssetRequestTO;
import com.mobicage.to.app.UpdateAppAssetResponseTO;
import com.mobicage.to.app.UpdateEmbeddedAppRequestTO;
import com.mobicage.to.app.UpdateEmbeddedAppResponseTO;
import com.mobicage.to.app.UpdateEmbeddedAppsRequestTO;
import com.mobicage.to.app.UpdateEmbeddedAppsResponseTO;
import com.mobicage.to.app.UpdateLookAndFeelRequestTO;
import com.mobicage.to.app.UpdateLookAndFeelResponseTO;
import com.mobicage.to.js_embedding.UpdateJSEmbeddingRequestTO;
import com.mobicage.to.js_embedding.UpdateJSEmbeddingResponseTO;
import com.mobicage.to.system.ForwardLogsRequestTO;
import com.mobicage.to.system.ForwardLogsResponseTO;
import com.mobicage.to.system.IdentityUpdateRequestTO;
import com.mobicage.to.system.IdentityUpdateResponseTO;
import com.mobicage.to.system.LogErrorRequestTO;
import com.mobicage.to.system.LogErrorResponseTO;
import com.mobicage.to.system.PushNotificationSettingsTO;
import com.mobicage.to.system.SaveSettingsRequest;
import com.mobicage.to.system.SettingsTO;
import com.mobicage.to.system.UnregisterMobileRequestTO;
import com.mobicage.to.system.UnregisterMobileResponseTO;
import com.mobicage.to.system.UpdateEmbeddedAppTranslationsRequestTO;
import com.mobicage.to.system.UpdateEmbeddedAppTranslationsResponseTO;
import com.mobicage.to.system.UpdateSettingsRequestTO;
import com.mobicage.to.system.UpdateSettingsResponseTO;

import org.jivesoftware.smack.util.Base64;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.mobicage.rpc.config.LookAndFeelConstants.removeLookAndFeel;

public class MainService extends Service implements TimeProvider {

    public final static String PREFERENCES_UPDATE_INTENT = "com.mobicage.rogerthat.PREFERENCES_UPDATE";
    private final static String INTENT_SHOULD_CLEANUP = "com.mobicage.rogerthat.INTENT_SHOULD_CLEANUP";

    private final static String PREFERENCES_READY = "ready";
    public final static String PREFERENCE_EMAIL = "email";

    public final static String PREFERENCE_TRACKING = "tracking";
    public final static String PREFERENCE_STREAM_ONLY_IMPORTANT = "stream_only_important";
    public final static String PREFERENCE_UPLOAD_PHOTO_WIFI = "upload_photo_wifi";

    public final static String PREFERENCE_PUSH_NOTIFICATIONS = "push_notifications";
    public final static String PREFERENCE_ALARM_SOUND = "alarm_sound";
    public final static String PREFERENCE_ALARM_TITLE = "alarm_title";

    public final static String PREFERENCE_MY_PROFILE = "my_profile";
    public final static String PREFERENCE_SECURITY = "security";
    public final static String PREFERENCE_DATA_DOWNLOAD = "data_download";
    public final static String PREFERENCE_LOGOUT = "log_out";
    public final static String PREFERENCE_DELETE_ACCOUNT = "delete_account";

    public final static String PREFERENCE_APP_INFO = "app_info";
    public final static String PREFERENCE_PRIVACY_POLICY = "privacy_policy";
    public final static String PREFERENCE_TERMS_OF_SERVICE = "terms_of_service";

    private final static String CONFIGKEY = "com.mobicage.rogerthat";

    private final static int CONFIGVERSION = 1;
    private final static String CONFIG_VERSION_KEY = "configVersion";
    private final static int CONFIG_VERSION_NOT_INITIALIZED = -1;

    private final static String CONFIG_SETTINGS_VERSION_KEY = "settingsVersion";
    private final static int CONFIG_SETTINGS_VERSION_DEFAULT_VALUE = -1;

    private final static String CONFIG_LEGACY_SERVICE_ENABLED_KEY = "serviceEnabled";

    private final static String CONFIG_REGISTERED_KEY = "isRegistered";
    private final static String CONFIG_BACKGROUND_ENABLED_KEY = "isEnabled";

    private final static String CONFIG_PASSWORD_KEY = "password";
    private final static String CONFIG_USERNAME_KEY = "username";

    private final static String CONFIG_ADJUSTED_TIME_DIFF_KEY = "adjusted_time_diff";
    private final static long CONFIG_ADJUSTED_TIME_DIFF_DEFAULT_VALUE = 0;

    public final static String CONFIG_GCM = "gcm";
    public final static String CONFIG_GCM_APP_VERSION_KEY = "app_version";
    public final static String CONFIG_GCM_REGISTRATION_ID_KEY = "registration_id";

    public final static String START_INTENT_FROM_ONCREATE_KEY = "fromOnCreate";
    public final static String START_INTENT_BOOTTIME_EXTRAS_KEY = "atBootTime";
    public final static String START_INTENT_BACKGROUND_DATA_SETTING_CHANGED_EXTRAS_KEY = "bgDataSetting";
    public final static String START_INTENT_JUST_REGISTERED = "justRegistered";
    public final static String START_INTENT_MY_EMAIL = "myEmail";
    public final static String START_INTENT_GCM = "gcm";

    public final static String CLOSE_ACTIVITY_INTENT = "com.mobicage.rogerthat.CLOSE_ACTIVITY_INTENT";
    public final static String UPDATE_BADGE_INTENT = "com.mobicage.rogerthat.UPDATE_BADGE_INTENT";

    private final static long WIPE_DELAY_MILLIS = 5 * 1000;

    private volatile static MainService current = null;

    protected Map<String, Long> badges = new HashMap<>();

    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // "FINAL" STATE DEFINED IN ONCREATE

    private int mMajorVersion;
    private int mMinorVersion;
    private boolean mIsDebug;

    private Handler mUIHandler;
    private Handler mIOHandler;
    private HandlerThread mIOWorkerThread;
    private Handler mBizzHandler;
    private HandlerThread mBizzWorkerThread;
    private Handler mNewsHandler;
    private HandlerThread mNewsWorkerThread;
    private List<Intent> mIntentStash;
    private List<SafeRunnable> mRunnableStash;
    private Set<String> mHighPriorityIntents;
    private Integer mScreenScale;

    private IBinder mBinder;

    private DatabaseManager mDatabaseManager;

    private ConfigurationProvider mConfigProvider;

    private long mCurrentSettingsVersion;

    private GeoLocationProvider mGeoLocationProvider;
    private ConsentProvider mConsentProvider;

    private NetworkConnectivityManager mNetworkConnectivityManager;

    private SafeRunnable mAwakeLoggerRunnable;

    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // "FINAL" STATE EXECUTED AT MOST ONCE (in onCreate or in onStart)

    private HttpCommunicator mHttpCommunicator; // UI
    private XMPPKickChannel mXmppKickChannel; // UI
    private Upgrader mUpgrader; // UI
    private SDCardLogger mHTTPSDCardLogger; // UI
    private SDCardLogger mXMPPSDCardLogger; // UI
    private SDCardLogger mLOCSDCardLogger;

    private volatile boolean mIsBacklogRunning = false;

    // XXX: make these 2 non volatile
    private volatile IdentityStore mIdentityStore; // UI -> volatile due to unclean threaded usage
    private volatile ConcurrentMap<String, MobicagePlugin> mPlugins; // UI -> volatile due to unclean threaded usage

    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Mutable shared state

    private volatile long mAdjustedTimeDiff = 0; // written and modified on UI thread; read on all threads
    private boolean mMustWipePersistenceInOnDestroy = false; // UI thread
    private String mWipeReason = null; // UI thread

    private boolean mScreenIsOn = false;

    private final List<SecurityItem> mQueue = Collections.synchronizedList(new ArrayList<SecurityItem>());
    private SecurityCallback mSetupPinCallback;
    private String mPin;
    private Map<String, PrivateKey> mPrivateKeys = new HashMap<>();
    private Map<String, PublicKey> mPublicKeys = new HashMap<>();
    private boolean mEnterPinActivityActive = false;
    private boolean mShouldClearPin = false;

    private Credentials mCredentials;
    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private NotificationHelper mNotificationHelper;

    public static MainService getInstance() {
        return current;
    }

    public static String getInternalIntentPermission(Context context) {
        return context.getPackageName() + ".permission.internal_intent";
    }

    @Override
    public void onCreate() {
        super.onCreate();
        L.d(getPackageName() + "::MainService.OnCreate");
        this.mNotificationHelper = new NotificationHelper(this);
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
            mIsDebug = (info.applicationInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) == ApplicationInfo.FLAG_DEBUGGABLE;
        } catch (Exception e) {
            Log.e(L.TAG, "Forcing debug=false");
            mIsDebug = false;
        }

        T.setUIThread("MainService.onCreate()");
        L.setContext(this);

        patchIPSettingsForDNSSRV();

        mIntentStash = new ArrayList<Intent>();
        mHighPriorityIntents = new HashSet<String>();
        mHighPriorityIntents.add(CLOSE_ACTIVITY_INTENT);
        mRunnableStash = new ArrayList<SafeRunnable>();

        readVersion();

        mUIHandler = new Handler();
        createIOWorkerThread();
        createHttpWorkerThread();
        if (AppConstants.NEWS_ENABLED) {
            createNewsWorkerThread();
        }

        mBinder = new MainBinder();

        setupDatabase();
        setupConfiguration();

        boolean debugLoggingEnabled = isDebugLoggingEnabled();
        if (debugLoggingEnabled) {
            mLOCSDCardLogger = new SDCardLogger(MainService.this, "location_log.txt", "LOC");
        }
        mGeoLocationProvider = new GeoLocationProvider(this, mConfigProvider, mLOCSDCardLogger);
        mConsentProvider = new ConsentProvider(mConfigProvider);

        setupNetworkConnectivityManager();
        setupRPC();

        startAwakeLogger();

        if (debugLoggingEnabled) {
            mHTTPSDCardLogger = new SDCardLogger(MainService.this, "httplog.txt", "HTTP");
            mXMPPSDCardLogger = new SDCardLogger(MainService.this, "xmpplog.txt", "XMPP");
        }

        final boolean mustInitializeAndStartService = getRegisteredFromConfig();

        if (mustInitializeAndStartService) {
            setupNetworkProtocol();
            initializeService(null);
            startMainService(START_INTENT_FROM_ONCREATE_KEY);
        }

        processUncaughtExceptions(mustInitializeAndStartService);

        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(INTENT_SHOULD_CLEANUP);
        registerReceiver(mBroadcastReceiver, filter);

        final PowerManager pow = (PowerManager) getSystemService(POWER_SERVICE);
        mScreenIsOn = pow.isScreenOn();

        hideLogForwardNotification();

        App.getContext().getPinLockMgr().setMainService(this);

        // This should remain the last line of this method.
        current = this;
    }

    public NotificationHelper getNotificationHelper() {
        return mNotificationHelper;
    }

    public void registerPluginDBUpdate(final Class<?> pluginClass, final String update) {
        T.dontCare();
        postOnUIHandler(new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                final String key = pluginClass.getName();
                final Configuration config = mConfigProvider.getConfiguration(key);
                config.put(update, true);
                mConfigProvider.updateConfigurationNow(key, config);
            }
        });
    }

    public Set<String> getPluginDBUpdates(final Class<?> pluginClass) {
        T.dontCare();
        final String key = pluginClass.getName();
        final Configuration config = mConfigProvider.getConfiguration(key);
        Set<String> updates = new HashSet<String>();
        for (String bkey : config.getBooleanKeys()) {
            if (config.get(bkey, false)) {
                updates.add(bkey);
            }
        }
        return updates;
    }

    public void clearPluginDBUpdate(final Class<?> pluginClass, final String update) {
        T.dontCare();
        final String key = pluginClass.getName();
        final Configuration config = mConfigProvider.getConfiguration(key);
        config.put(update, false);
        mConfigProvider.updateConfigurationNow(key, config);
    }

    private boolean isDebugLoggingEnabled() {
        return CloudConstants.DEBUG_LOGGING && SystemUtils.isRunningOnRealDevice(this);
    }

    private void logHTTP(String s) {
        if (mHTTPSDCardLogger == null) {
            L.d(s);
        } else {
            mHTTPSDCardLogger.d(s);
        }
    }

    private final BroadcastReceiver mBroadcastReceiver = new SafeBroadcastReceiver() {

        @Override
        public String[] onSafeReceive(Context context, Intent intent) {
            T.UI();
            if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
                mScreenIsOn = true;
            } else if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                mScreenIsOn = false;
            } else if (INTENT_SHOULD_CLEANUP.equals(intent.getAction())) {
                cleanup(true);
            }
            return null;
        }

    };

    public boolean isDebug() {
        return mIsDebug;
    }

    public boolean getScreenIsOn() {
        T.UI();
        return mScreenIsOn;
    }

    public void addHighPriorityIntent(String action) {
        mHighPriorityIntents.add(action);
    }

    @Override
    public void sendBroadcast(final Intent intent) {
        sendBroadcast(intent, false, true);
    }

    public void sendBroadcast(final Intent intent, final boolean now, final boolean internalOnly) {
        sendBroadcast(intent, now, true, true);
    }

    private void sendBroadcast(final Intent intent, final boolean now, final boolean addCreationTimestamp,
        final boolean internalOnly) {
        if (T.getThreadType() == T.UI) {
            T.UI();
            if (addCreationTimestamp) {
                intent.putExtra(SafeBroadcastReceiver.INTENT_CREATION_TIMESTAMP, System.currentTimeMillis());
            }
            if (mIsBacklogRunning && !mHighPriorityIntents.contains(intent.getAction())) {
                if (now) {
                    if (internalOnly)
                        super.sendBroadcast(intent, getInternalIntentPermission(this));
                    else
                        super.sendBroadcast(intent);
                    logHTTP("********* BROADCASTING INTENT ********** " + intent.getAction());
                } else {
                    mIntentStash.add(intent);
                    logHTTP("********* STASHED INTENT ********** " + intent.getAction());
                }
            } else {
                if (internalOnly)
                    super.sendBroadcast(intent, getInternalIntentPermission(this));
                else
                    super.sendBroadcast(intent);
                logHTTP("********* BROADCASTING INTENT ********** " + intent.getAction());
            }
        } else {
            mUIHandler.post(new SafeRunnable() {
                @Override
                protected void safeRun() throws Exception {
                    T.UI();
                    sendBroadcast(intent, now, internalOnly);
                }
            });
        }
    }

    private void setupNetworkProtocol() {
        T.UI();
        Credentials credentials = getCredentials();
        final SharedPreferences options = PreferenceManager.getDefaultSharedPreferences(MainService.this);
        boolean wifiOnlySettingEnabled = options.getBoolean(PREFERENCE_UPLOAD_PHOTO_WIFI, false);

        mHttpCommunicator = new HttpCommunicator(this, this, mDatabaseManager, credentials, mConfigProvider,
            new SafeRunnable() {
                // On Start communicating
                @Override
                protected void safeRun() throws Exception {
                    T.dontCare();
                    mIsBacklogRunning = true;
                    logHTTP("********* START STASHING **********");
                }
            }, new SafeRunnable() {
                // On Stop communicating
                @Override
                protected void safeRun() throws Exception {
                    T.dontCare();
                    mIsBacklogRunning = false;
                    logHTTP("********* STOPPED STASHING **********");

                    mUIHandler.post(new SafeRunnable() {
                        @Override
                        protected void safeRun() throws Exception {
                            T.UI();
                            if (mMustWipePersistenceInOnDestroy) {
                                sendBroadcast(new Intent(CLOSE_ACTIVITY_INTENT));
                                stopSelf();
                            } else {
                                purgeStashedIntents();
                                purgeStashedRunnables();
                            }
                        }
                    });
                }
            }, mHTTPSDCardLogger, wifiOnlySettingEnabled);
        if (CloudConstants.USE_XMPP_KICK_CHANNEL)
            mXmppKickChannel = new XMPPKickChannel(this, credentials, mXMPPSDCardLogger);
    }

    private void purgeStashedIntents() {
        T.UI();
        List<Intent> stash = new ArrayList<Intent>(mIntentStash);
        mIntentStash.clear();

        Collections.reverse(stash);
        for (Intent intent : stash) {
            sendBroadcast(intent, true, false);
        }
    }

    private void purgeStashedRunnables() {
        T.UI();
        List<SafeRunnable> stash = new ArrayList<SafeRunnable>(mRunnableStash);
        mRunnableStash.clear();
        for (SafeRunnable safeRunnable : stash) {
            postOnUIHandlerWhenBacklogIsReady(safeRunnable);
        }
    }

    private void teardownNetworkProtocol() {
        T.UI();
        if (mXmppKickChannel != null)
            mXmppKickChannel.teardown();
        if (mHttpCommunicator != null)
            mHttpCommunicator.close();
    }

    private void initializeService(String forceMyEmail) {
        T.UI();

        L.d("MainService.initializeService()");

        loadIdentity(forceMyEmail);
        setupServices();
        if (forceMyEmail != null)
            mIdentityStore.refreshIdentity();

        mUpgrader = new Upgrader(MainService.this);
        mUpgrader.scheduleCleanupOldApks(mMajorVersion, mMinorVersion);
    }

    public class MainBinder extends Binder {
        public MainService getService() {
            T.UI();
            return MainService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        T.UI();
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        T.UI();
        L.d("MainService bound to " + (intent.hasExtra("clazz") ? intent.getStringExtra("clazz") : "unkown"));
    }

    @Override
    public boolean onUnbind(Intent intent) {
        T.UI();
        L.d("MainService unbound from " + (intent.hasExtra("clazz") ? intent.getStringExtra("clazz") : "unkown"));
        return true;
    }

    private void startMainService(String why) {
        T.UI();
        Intent launchServiceIntent = new Intent(this, MainService.class);
        launchServiceIntent.putExtra(why, true);
        startService(launchServiceIntent);
    }

    public void setAdjustedTimeDiff(final long adjustedTimeDiff) {
        T.dontCare();
        if (Math.abs(adjustedTimeDiff - mAdjustedTimeDiff) > 5000) {
            postOnUIHandler(new SafeRunnable() {
                @Override
                protected void safeRun() throws Exception {
                    T.UI();
                    mAdjustedTimeDiff = adjustedTimeDiff;
                    final Configuration cfg = new Configuration();
                    cfg.put(CONFIG_ADJUSTED_TIME_DIFF_KEY, adjustedTimeDiff);
                    mConfigProvider.updateConfigurationLater(CONFIGKEY, cfg);
                    L.d("Setting adjusted time diff between server and client to " + adjustedTimeDiff + " millis");
                }
            });
        }
    }

    public long getAdjustedTimeDiff() {
        return this.mAdjustedTimeDiff;
    }

    @Override
    public long currentTimeMillis() {
        T.dontCare();
        return System.currentTimeMillis() + mAdjustedTimeDiff;
    }

    public NetworkConnectivityManager getNetworkConnectivityManager() {
        T.dontCare();
        return mNetworkConnectivityManager;
    }

    private void initSettings() {
        T.UI();

        final Configuration cfg = mConfigProvider.getConfiguration(CONFIGKEY);
        mCurrentSettingsVersion = cfg.get(CONFIG_SETTINGS_VERSION_KEY, CONFIG_SETTINGS_VERSION_DEFAULT_VALUE);
        mAdjustedTimeDiff = cfg.get(CONFIG_ADJUSTED_TIME_DIFF_KEY, CONFIG_ADJUSTED_TIME_DIFF_DEFAULT_VALUE);
        L.d("Initializing adjusted time diff with " + mAdjustedTimeDiff + " millis");

        final SharedPreferences options = PreferenceManager.getDefaultSharedPreferences(this);
        options.registerOnSharedPreferenceChangeListener(mPreferencesListener);
    }

    private void startAwakeLogger() {
        T.UI();
        mAwakeLoggerRunnable = new SafeRunnable() {
            @Override
            public void safeRun() {
                T.UI();
                L.d("I am awake");
                postDelayedOnUIHandler(this, 120000);
            }
        };
        postOnUIHandler(mAwakeLoggerRunnable);
    }

    private void stopAwakeLogger() {
        T.UI();
        removeFromUIHandler(mAwakeLoggerRunnable);
    }

    private void setupDatabase() {
        T.UI();
        mDatabaseManager = new DatabaseManager(this);
    }

    private void createIOWorkerThread() {
        T.UI();
        mIOWorkerThread = new HandlerThread("rogerthat_io_worker");
        mIOWorkerThread.start();
        final Looper looper = mIOWorkerThread.getLooper();
        mIOHandler = new Handler(looper);
        T.setIOThread("MainService.createIOWorkerThread()", mIOWorkerThread);
    }

    private void createHttpWorkerThread() {
        T.UI();
        mBizzWorkerThread = new HandlerThread("rogerthat_http_worker");
        mBizzWorkerThread.start();
        final Looper looper = mBizzWorkerThread.getLooper();
        mBizzHandler = new Handler(looper);
        T.setBizzThread("HttpCommunicator", mBizzWorkerThread);
    }

    private void destroyIOWorkerThread() {
        T.UI();
        final Looper looper = mIOWorkerThread.getLooper();
        if (looper != null) {
            looper.quit();
        }

        try {
            // XXX: can this cause ANR?
            mIOWorkerThread.join();
        } catch (InterruptedException e) {
            L.bug(e);
        }

        mIOHandler = null;
        mIOWorkerThread = null;
        T.resetIOThreadId();
    }

    private void destroyHttpWorkerThread() {
        T.UI();
        final Looper looper = mBizzWorkerThread.getLooper();
        if (looper != null)
            looper.quit();

        try {
            mBizzWorkerThread.join();
        } catch (InterruptedException e) {
            L.bug(e);
        }

        mBizzHandler = null;
        mBizzWorkerThread = null;
        T.resetBizzThreadId();
    }

    private void createNewsWorkerThread() {
        T.UI();
        mNewsWorkerThread = new HandlerThread("rogerthat_news_worker");
        mNewsWorkerThread.start();
        final Looper looper = mNewsWorkerThread.getLooper();
        mNewsHandler = new Handler(looper);
        T.setNewsThread("MainService.createNewsWorkerThread()", mNewsWorkerThread);
    }

    private void destroyNewsWorkerThread() {
        T.UI();
        final Looper looper = mNewsWorkerThread.getLooper();
        if (looper != null)
            looper.quit();

        try {
            mNewsWorkerThread.join();
        } catch (InterruptedException e) {
            L.bug(e);
        }

        mNewsHandler = null;
        mNewsWorkerThread = null;
        T.resetNewsThreadId();
    }

    public static String getVersion(Context context) {
        if (context != null) {
            try {
                return context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_META_DATA).versionName;
            } catch (NameNotFoundException e) {
                L.bug(e);
            }
        }
        return "1.0.0.BUG";
    }

    public static String getUserAgent(Context context) {
        return CloudConstants.APP_ID + "/" + getVersion(context);
    }

    private void readVersion() {
        T.UI();
        try {
            // versionName = "1.0.950.A" or "1.0.950.AD"
            final StringTokenizer st = new StringTokenizer(getVersion(this), ".");
            if (st.countTokens() == 4) {
                st.nextToken();
                mMajorVersion = Integer.valueOf(st.nextToken());
                mMinorVersion = Integer.valueOf(st.nextToken());
            }
            L.d("Major version is " + mMajorVersion + " / Minor version is " + mMinorVersion);
            return;
        } catch (Exception e) {
            L.d(e);
        }
        L.d("Could not retrieve package version");
        mMajorVersion = 1;
        mMinorVersion = 1;
    }

    @Override
    public void onDestroy() {
        // This should remain the first line of this method
        current = null;

        T.UI();
        super.onDestroy();

        unregisterReceiver(mBroadcastReceiver);

        L.setContext(null);
        L.d("MainService.onDestroy()");

        teardownNetworkProtocol();

        if (mUpgrader != null)
            mUpgrader.close();

        disable();

        stopAwakeLogger();

        teardownRPC();

        teardownNetworkConnectivityManager();
        teardownGeoLocationProvider();
        teardownConsentProvider();


        teardownConfiguration();

        if (mMustWipePersistenceInOnDestroy) {
            L.d("Wiping database and SharedPreferences in MainService.onDestroy");
            mDatabaseManager.wipeAndClose();
            destroyPreferences();
            removeLookAndFeel(this);
            String reason = mWipeReason;
            if (TextUtils.isEmptyOrWhitespace(reason)) {
                reason = getString(R.string.device_was_unregistered);
            }
            createUnregisterFile(reason);
            showUnregisterNotification(reason);
        } else {
            mDatabaseManager.close();
        }

        if (AppConstants.NEWS_ENABLED) {
            destroyNewsWorkerThread();
        }

        destroyHttpWorkerThread();
        destroyIOWorkerThread();

        mIsBacklogRunning = false;
        mIntentStash.clear();
        mIntentStash = null;
        mRunnableStash.clear();
        mRunnableStash = null;
        mHighPriorityIntents.clear();
        mHighPriorityIntents = null;
        if (mXMPPSDCardLogger != null)
            mXMPPSDCardLogger.close();
        if (mHTTPSDCardLogger != null)
            mHTTPSDCardLogger.close();
        if (mLOCSDCardLogger != null)
            mLOCSDCardLogger.close();
    }

    private void teardownGeoLocationProvider() {
        T.UI();
        mGeoLocationProvider.close();
    }

    private void teardownConsentProvider() {
        T.UI();
        mConsentProvider.close();
    }

    public ConsentProvider getConsentProvider() {
        T.UI();
        return mConsentProvider;
    }

    private void setupNetworkConnectivityManager() {
        T.UI();
        mNetworkConnectivityManager = new NetworkConnectivityManager(this);
        mNetworkConnectivityManager.startListening();
    }

    private void teardownNetworkConnectivityManager() {
        T.UI();
        if (mNetworkConnectivityManager != null) {
            mNetworkConnectivityManager.teardown();
        }
    }

    private void setupRPC() {
        T.UI();

        Rpc.handler = new DefaultRpcHandler(this);

        Rpc.submitter = new IRequestSubmitter() {

            @Override
            public void call(final String callid, final String body, final String function,
                final IResponseHandler<?> responseHandler, long timestamp) {
                T.BIZZ();

                if (mHttpCommunicator == null) {
                    return;
                }

                try {
                    final HttpBacklogItem item = new HttpBacklogItem();
                    item.callid = callid;
                    item.calltype = HttpBacklog.MESSAGETYPE_CALL;
                    item.timestamp = timestamp;
                    item.body = (body != null) ? body : "";
                    final boolean hasPriority = PriorityMap.hasPriority(function);
                    mHttpCommunicator.addOutgoingCall(item, hasPriority, function, responseHandler);
                    kickHttpCommunication(false, "Outgoing call " + function);
                } catch (Exception e) {
                    L.bug("Error making outgoing call " + callid + " / " + function, e);
                }

            }

        };

    }

    private void teardownRPC() {
        T.UI();
        Rpc.submitter = null;
        Rpc.handler = null;
    }

    private void setupConfiguration() {
        T.UI();
        mConfigProvider = new ConfigurationProvider(this, mDatabaseManager);
        updateConfigFormat();
        initSettings();
    }

    private void teardownConfiguration() {
        T.UI();
        final SharedPreferences options = PreferenceManager.getDefaultSharedPreferences(this);
        options.unregisterOnSharedPreferenceChangeListener(mPreferencesListener);
        if (mConfigProvider != null) {
            mConfigProvider.close();
            mConfigProvider = null;
        }
    }

    public ConfigurationProvider getConfigurationProvider() {
        T.UI();
        return mConfigProvider;
    }

    private void createPlugins() {
        T.UI();

        mPlugins = new ConcurrentHashMap<String, MobicagePlugin>(10, 10, 2);
        BrandingMgr brandingMgr = BrandingMgr.createBrandingMgr(mConfigProvider, this);

        // History (aka Activity) plugin
        MobicagePlugin historyPlugin = new com.mobicage.rogerthat.plugins.history.HistoryPlugin(this, mDatabaseManager);
        mPlugins.put(HistoryPlugin.class.toString(), historyPlugin);

        // client->server call to post heartbeat
        // JSEmbedding
        MobicagePlugin systemPlugin = new com.mobicage.rogerthat.plugins.system.SystemPlugin(this, mConfigProvider,
            mNetworkConnectivityManager, brandingMgr, mDatabaseManager);
        mPlugins.put(SystemPlugin.class.toString(), systemPlugin);

        // client->server call to post location record (interval based)
        MobicagePlugin trackmePlugin = new com.mobicage.rogerthat.plugins.trackme.TrackmePlugin(mConfigProvider, this,
            mGeoLocationProvider, mLOCSDCardLogger, mDatabaseManager);
        mPlugins.put(TrackmePlugin.class.toString(), trackmePlugin);

        // operations on friends and friend lists
        MobicagePlugin friendsPlugin = new com.mobicage.rogerthat.plugins.friends.FriendsPlugin(mDatabaseManager,
            mConfigProvider, this, mNetworkConnectivityManager, brandingMgr, mGeoLocationProvider);
        mPlugins.put(FriendsPlugin.class.toString(), friendsPlugin);

        // magic messages
        MobicagePlugin messagingPlugin = new com.mobicage.rogerthat.plugins.messaging.MessagingPlugin(mConfigProvider,
            this, mDatabaseManager, brandingMgr);
        mPlugins.put(MessagingPlugin.class.toString(), messagingPlugin);

        // operations on news
        if (AppConstants.NEWS_ENABLED) {
            MobicagePlugin newsPlugin = new com.mobicage.rogerthat.plugins.news.NewsPlugin(this, mConfigProvider, mDatabaseManager);
            mPlugins.put(NewsPlugin.class.toString(), newsPlugin);
        }

        MobicagePlugin securityPlugin = new com.mobicage.rogerthat.plugins.security.SecurityPlugin(this, mConfigProvider, mDatabaseManager);
        mPlugins.put(SecurityPlugin.class.toString(), securityPlugin);

        MobicagePlugin paymentPlugin = new com.mobicage.rogerthat.plugins.payment.PaymentPlugin(this, mConfigProvider, mDatabaseManager);
        mPlugins.put(PaymentPlugin.class.toString(), paymentPlugin);

        for (MobicagePlugin plugin : mPlugins.values())
            plugin.initialize();

        brandingMgr.initialize(mConfigProvider, this);

        onPluginsInitialized();
    }

    private void onPluginsInitialized() {
        T.UI();
        L.d("All plugins are initialized");
        initPreferences(); // XXX: ugly - happens somewhere else as well - needs cleanup
    }

    private void destroyPlugins() {
        T.UI();
        L.d("DESTROY PLUGINS");
        if (mPlugins != null) {
            for (MobicagePlugin plugin : mPlugins.values()) {
                try {
                    plugin.destroy();
                } catch (Exception e) {
                    L.d(e);
                }
            }
            mPlugins.clear();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startid) {
        T.UI();
        super.onStartCommand(intent, flags, startid);

        final Bundle extras = intent == null ? null : intent.getExtras();
        boolean launchedAtBootTime = false;
        boolean launchedByBgDataSettingChange = false;
        boolean launchedByOnCreate = false;
        boolean launchedByJustRegistered = false;
        boolean launchedByGCM = false;
        try {
            if (extras != null) {
                launchedAtBootTime = extras.getBoolean(START_INTENT_BOOTTIME_EXTRAS_KEY, false);
                launchedByBgDataSettingChange = extras.getBoolean(
                    START_INTENT_BACKGROUND_DATA_SETTING_CHANGED_EXTRAS_KEY, false);
                launchedByOnCreate = extras.getBoolean(START_INTENT_FROM_ONCREATE_KEY, false);
                launchedByJustRegistered = extras.getBoolean(START_INTENT_JUST_REGISTERED, false);
                launchedByGCM = extras.getBoolean(START_INTENT_GCM, false);
            }

            if (launchedByGCM) {
                kickHttpCommunication(true, "Incomming GCM");
                return START_STICKY;
            }

            final boolean isRegisteredInConfig = getRegisteredFromConfig();
            L.d("MainService.onStart \n  isIntentNull = " + (intent == null) + "\n  isRegisteredInConfig = "
                + isRegisteredInConfig + "\n  launchedAtBootTime = " + launchedAtBootTime
                + "\n  launchedByBgDataSettingChange = " + launchedByBgDataSettingChange + "\n  launchedByOnCreate = "
                + launchedByOnCreate + "\n  launchedByJustRegistered = " + launchedByJustRegistered);

            if (launchedByJustRegistered) {
                setupNetworkProtocol();
                final String myEmail = extras == null ? null : extras.getString(START_INTENT_MY_EMAIL);
                initializeService(myEmail);
            }

            if (!isRegisteredInConfig) {
                L.d("MainService.onStart() - stopping service immediately");
                stopSelf();
                return START_NOT_STICKY;
            }

            // start networking machinery
            if (CloudConstants.USE_XMPP_KICK_CHANNEL)
                mXmppKickChannel.start();
            getPlugin(SystemPlugin.class).doHeartbeat();

            if (CloudConstants.USE_GCM_KICK_CHANNEL)
                GoogleServicesUtils.registerGCMRegistrationId(this, null);

            if (launchedByOnCreate) {
                kickHttpCommunication(true, "We just got started");
            }

            cleanup(false);
            PendingIntent cleanupDownloadsIntent = PendingIntent.getBroadcast(this, 0, new Intent(
                    INTENT_SHOULD_CLEANUP), 0);

            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
                + AlarmManager.INTERVAL_DAY * 7, AlarmManager.INTERVAL_DAY * 7, cleanupDownloadsIntent);

            return START_STICKY;
        } finally {
            if (launchedByGCM) {
                GCMReveiver.completeWakefulIntent(intent);
            }
        }
    }

    private void cleanup(boolean force) {
        if (!force) {
            long lastWeek = System.currentTimeMillis() - (7 * 86400 * 1000);
            Configuration cfg = mConfigProvider.getConfiguration(CachedDownloader.CONFIGKEY);
            long lastCleanupEpoch = cfg.get(CachedDownloader.CONFIG_LAST_CLEANUP, 0);
            if (lastCleanupEpoch > lastWeek) {
                return;
            }
        }

        cleanupOldCachedFiles();
        cleanupOldBrandings();
    }

    private void cleanupOldCachedFiles() {
        final CachedDownloader cachedDownloader = CachedDownloader.getInstance(this);
        postOnBIZZHandler(new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                cachedDownloader.cleanupOldCachedDownloads();
            }
        });
    }

    private void cleanupOldBrandings() {
        if (CloudConstants.isContentBrandingApp()) {
            return;
        }
        if (CloudConstants.isYSAAA()) {
            return;
        }

        postOnBIZZHandler(new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                getPlugin(FriendsPlugin.class).cleanupOldBrandings();
            }
        });
    }

    private void setupServices() {
        T.UI();
        createPlugins();
        setupSystemRPC();
        restoreBadgeValues();
    }

    private void loadIdentity(String forceEmail) {
        T.UI();
        mIdentityStore = new IdentityStore(mDatabaseManager, this, forceEmail);
    }

    private void closeIdentity() {
        T.UI();
        if (mIdentityStore != null)
            mIdentityStore.close();
    }

    public IdentityStore getIdentityStore() {
        T.dontCare();
        return mIdentityStore;
    }

    private void disable() {
        T.UI();
        teardownSystemRPC();
        destroyPlugins();
        closeIdentity();
    }

    public void wipe(long delay, String reason) {
        T.UI();
        mMustWipePersistenceInOnDestroy = true;
        mWipeReason = reason;
        if (!mIsBacklogRunning) {
            sendBroadcast(new Intent(CLOSE_ACTIVITY_INTENT));
            stopSelf();
        }

        App.getContext().getPinLockMgr().unregistered();
    }

    public Credentials getCredentials() {
        if (mCredentials == null) {
            mCredentials = getCredentials(mConfigProvider);
        }
        return mCredentials;
    }

    public static Credentials getCredentials(ConfigurationProvider configProvider) {
        T.dontCare();
        final Configuration cfg = configProvider.getConfiguration(CONFIGKEY);
        final String username = cfg.get(CONFIG_USERNAME_KEY, null);
        final String password = cfg.get(CONFIG_PASSWORD_KEY, null);

        final Credentials credentials;
        if ((username != null) && (password != null)) {
            credentials = new Credentials(username, password);
        } else {
            credentials = null;
        }
        return credentials;
    }

    public void setCredentials(Credentials credentials) {
        T.UI();
        Configuration cfg = new Configuration();
        cfg.put(CONFIG_USERNAME_KEY, credentials.getUsername());
        cfg.put(CONFIG_PASSWORD_KEY, credentials.getPassword());
        mConfigProvider.updateConfigurationNow(CONFIGKEY, cfg);
    }

    public void setRegisteredInConfig(final boolean isRegistered) {
        T.UI();
        final Configuration cfg = new Configuration();
        cfg.put(CONFIG_REGISTERED_KEY, isRegistered);
        mConfigProvider.updateConfigurationLater(CONFIGKEY, cfg);
    }

    public boolean getRegisteredFromConfig() {
        T.UI();
        boolean registered = mConfigProvider.getConfiguration(CONFIGKEY).get(CONFIG_REGISTERED_KEY, false);
        return registered;
    }

    public void processSettings(SettingsTO settings, boolean updatePreferences) {
        T.UI();
        long newSettingsVersion = settings.version;
        if (newSettingsVersion > mCurrentSettingsVersion) {
            mCurrentSettingsVersion = newSettingsVersion;
            mConfigProvider.dispatchNewSettings(settings);

            if (updatePreferences) {
                setPreferences(settings);
            }
        } else {
            L.d("MainService not updating settings. Current version " + mCurrentSettingsVersion + " received version "
                + newSettingsVersion);
        }
    }

    private File getUnregisterReasonFile() {
        File file = IOUtils.getFilesDirectory(this);
        return new File(file, "unregister_reason.txt");
    }

    public String getUnregisterReason() {
        try {
            File f = getUnregisterReasonFile();
            if (!f.exists()) {
                return null;
            }
            FileInputStream fis = new FileInputStream(f);
            try {
                return SystemUtils.convertStreamToString(fis);
            } finally {
                fis.close();
            }
        } catch (Exception e) {
            L.e("Failed to getUnregisterReason", e);
            return null;
        }
    }

    public boolean deleteUnregisterFile() {
        File file = getUnregisterReasonFile();
        if (file.exists()) {
            return file.delete();
        }
        return true;
    }

    private void createUnregisterFile(String reason) {
        T.UI();
        try {
            SystemUtils.writeStringToFile(getUnregisterReasonFile(), reason);
        } catch (Exception e) {
            L.e("Failed to createUnregisterFile", e);
        }
    }

    private void showUnregisterNotification(String reason) {
        T.UI();
        UIUtils.showLongToast(this, reason);
    }

    private void setupSystemRPC() {
        T.UI();
        CallReceiver.comMobicageCapiSystemIClientRpc = new com.mobicage.capi.system.IClientRpc() {

            @Override
            public ForwardLogsResponseTO forwardLogs(ForwardLogsRequestTO request) throws Exception {

                if (request.jid == null) {
                    hideLogForwardNotification();
                    L.getLogForwarder().stop();
                } else {
                    showLogForwardNotification(request.jid);
                    L.getLogForwarder().start(request.jid);
                }

                return new ForwardLogsResponseTO();
            }

            @Override
            public IdentityUpdateResponseTO identityUpdate(final IdentityUpdateRequestTO request) throws Exception {
                T.BIZZ();
                L.d("Server called identityUpdate()");
                getIdentityStore().updateIdentity(request.identity, null);

                return new IdentityUpdateResponseTO();
            }

            @Override
            public UnregisterMobileResponseTO unregisterMobile(final UnregisterMobileRequestTO request)
                throws Exception {
                T.BIZZ();
                postOnUIHandler(new SafeRunnable() {
                    @Override
                    protected void safeRun() throws Exception {
                        L.d("Server called unregisterMobile()");
                        wipe(WIPE_DELAY_MILLIS, request.reason);
                    }
                });

                return null;
            }

            @Override
            public UpdateAppAssetResponseTO updateAppAsset(UpdateAppAssetRequestTO request) throws Exception {
                getPlugin(SystemPlugin.class).updateAppAsset(request.kind, request.url, request.scale_x);
                return new UpdateAppAssetResponseTO();
            }

            @Override
            public UpdateEmbeddedAppTranslationsResponseTO updateEmbeddedAppTranslations(UpdateEmbeddedAppTranslationsRequestTO request) throws java.lang.Exception {
                getPlugin(SystemPlugin.class).updateEmbeddedAppTranslations(request.translations);
                return new UpdateEmbeddedAppTranslationsResponseTO();
            }

            @Override
            public UpdateEmbeddedAppResponseTO updateEmbeddedApp(UpdateEmbeddedAppRequestTO request) throws Exception {
                getPlugin(SystemPlugin.class).updateEmbeddedApp(new EmbeddedAppTO(request.toJSONMap()));
                return new UpdateEmbeddedAppResponseTO();
            }

            @Override
            public UpdateEmbeddedAppsResponseTO updateEmbeddedApps(UpdateEmbeddedAppsRequestTO request) throws Exception {
                getPlugin(SystemPlugin.class).updateEmbeddedApps(request.embedded_apps);
                return new UpdateEmbeddedAppsResponseTO();
            }

            @Override
            public UpdateJSEmbeddingResponseTO updateJsEmbedding(UpdateJSEmbeddingRequestTO request) throws Exception {
                getPlugin(SystemPlugin.class).updateJSEmbeddedPackets(request.items);
                return new UpdateJSEmbeddingResponseTO();
            }

            @Override
            public UpdateLookAndFeelResponseTO updateLookAndFeel(UpdateLookAndFeelRequestTO request) throws Exception {
                getPlugin(SystemPlugin.class).updateLookAndFeel(request);
                return new UpdateLookAndFeelResponseTO();
            }

            @Override
            public UpdateSettingsResponseTO updateSettings(final UpdateSettingsRequestTO request) throws Exception {
                T.BIZZ();
                postOnUIHandler(new SafeRunnable() {
                    @Override
                    protected void safeRun() throws Exception {
                        processSettings(request.settings, true);
                    }
                });
                return null;
            }
        };
    }

    private void showLogForwardNotification(final String jid) {
        String title = getString(R.string.debug);
        String message = getString(R.string.logs_forwarded, jid, getString(R.string.app_name));
        int notificationId = R.integer.forwarding_logs;
        boolean withSound = true;
        boolean withVibration = false;
        boolean withLight = false;
        boolean autoCancel = false;
        int icon = R.drawable.notification_icon;
        int notificationNumber = 0;
        String action = null;
        String extra = null;
        String extraData = null;
        String tickerText = null;
        long timestamp = currentTimeMillis();
        this.getNotificationHelper().doNotification(title, message, notificationId, action, withSound, withVibration,
                withLight, autoCancel, icon, notificationNumber, null, tickerText, timestamp,
                Notification.PRIORITY_DEFAULT, null, null, null,
                NotificationCompat.CATEGORY_PROGRESS, NotificationChannelId.DEFAULT);
    }

    private void hideLogForwardNotification() {
        this.getNotificationHelper().cancelNotification(R.integer.forwarding_logs);
    }

    public void processExceptionViaHTTP(Exception ex) {
        if (CloudConstants.DEBUG_LOGGING) {
            L.e("processExceptionViaHTTP", ex);
        }

        final JSONObject error = new JSONObject();
        error.put("description", "processExceptionViaHTTP");
        error.put("language", Locale.getDefault().getLanguage());
        error.put("country", Locale.getDefault().getCountry());
        error.put("platform", "1");
        error.put("timestamp", "" + System.currentTimeMillis() / 1000);

        try {
            error.put("error_message", L.getStackTraceString(ex));
        } catch (Throwable t) {
            if (CloudConstants.DEBUG_LOGGING) {
                L.e(t);
            }
            try {
                error.put("error_message", "Failed to get stacktrace of exception: " + ex);
            } catch (Throwable t2) { // too bad... just ignore
                if (CloudConstants.DEBUG_LOGGING) {
                    L.e(t2);
                }
            }
        }

        try {
            error.put("device_id", Installation.id(this));
        } catch (Throwable t) { // too bad... just ignore
            if (CloudConstants.DEBUG_LOGGING) {
                L.e(t);
            }
        }

        try {
            error.put("platform_version", "" + SystemUtils.getAndroidVersion());
        } catch (Throwable t) { // too bad... just ignore
            if (CloudConstants.DEBUG_LOGGING) {
                L.e(t);
            }
        }

        try {
            error.put("mobicage_version", getVersion(this));
        } catch (Throwable t) { // too bad... just ignore
            if (CloudConstants.DEBUG_LOGGING) {
                L.e(t);
            }
        }

        new SafeAsyncTask<Object, Object, Object>() {
            @Override
            protected Object safeDoInBackground(Object... params) {
                try {
                    App.logErrorToServer(error);
                } catch (Exception e) {
                    L.e(e);
                    return null;
                }
                return null;
            }

        }.execute();
    }

    private void processUncaughtExceptions(boolean isRegistered) {
        final File[] exceptionFiles = App.getExceptionsDir(this).listFiles();
        if (exceptionFiles == null) {
            return; // exceptionDirs does not exist ==> no exception has been logged yet.
        }

        for (final File f : exceptionFiles) {
            try {
                JSONParser parser = new JSONParser();
                final JSONObject error = (JSONObject) parser.parse(new FileReader(f));
                if (isRegistered) {
                    LogErrorRequestTO request = new LogErrorRequestTO();
                    request.description = (String) error.get("description");
                    request.platform = Long.parseLong((String) error.get("platform"));
                    request.timestamp = Long.parseLong((String) error.get("timestamp"));
                    request.mobicageVersion = (String) error.get("mobicage_version");
                    request.platformVersion = (String) error.get("platform_version");
                    request.errorMessage = (String) error.get("error_message");
                    com.mobicage.api.system.Rpc.logError(new ResponseHandler<LogErrorResponseTO>(), request);
                    f.delete();
                } else {
                    new SafeAsyncTask<Object, Object, Object>() {
                        @Override
                        protected Object safeDoInBackground(Object... params) {
                            try {
                                App.logErrorToServer(error);
                            } catch (Exception e) {
                                L.e(e);
                                return null;
                            }
                            f.delete();
                            return null;
                        }

                    }.execute();
                }
            } catch (Exception e) {
                if (CloudConstants.DEBUG_LOGGING) {
                    L.e(e);
                }
            }
        }
    }

    private void teardownSystemRPC() {
        T.UI();
        CallReceiver.comMobicageCapiSystemIClientRpc = null;
    }

    public int getMajorVersion() {
        T.dontCare();
        return mMajorVersion;
    }

    public int getMinorVersion() {
        T.dontCare();
        return mMinorVersion;
    }

    public <E extends MobicagePlugin> E getPlugin(Class<E> plugin) {
        T.dontCare();
        if (mPlugins == null)
            return null;
        @SuppressWarnings("unchecked")
        final E thePlugin = (E) mPlugins.get(plugin.toString());
        if (thePlugin == null) {
            L.bug("Requesting plugin that is not yet initialized! " + plugin.toString());
        }
        return thePlugin;
    }

    private void updateConfigFormat() {
        T.UI();
        final Configuration cfg = mConfigProvider.getConfiguration(CONFIGKEY);
        final long cfgVersion = cfg.get(CONFIG_VERSION_KEY, CONFIG_VERSION_NOT_INITIALIZED);
        if (cfgVersion == CONFIG_VERSION_NOT_INITIALIZED) {
            L.d("MainService. Transforming config version " + cfgVersion + " into version " + CONFIGVERSION);
            cfg.put(CONFIG_VERSION_KEY, CONFIGVERSION);
            final boolean isRegistered = cfg.get(CONFIG_LEGACY_SERVICE_ENABLED_KEY, false);
            cfg.put(CONFIG_REGISTERED_KEY, isRegistered);
            cfg.put(CONFIG_BACKGROUND_ENABLED_KEY, true);
        }
        mConfigProvider.updateConfigurationNow(CONFIGKEY, cfg);
    }

    public void postAtFrontOfUIHandler(SafeRunnable r) {
        mUIHandler.postAtFrontOfQueue(r);
    }

    public void postOnUIHandler(SafeRunnable r) {
        mUIHandler.post(r);
    }

    public void postDelayedOnUIHandler(SafeRunnable r, long delayMillis) {
        mUIHandler.postDelayed(r, delayMillis);
    }

    public void postOnUIHandlerWhenBacklogIsReady(final SafeRunnable safeRunnable) {
        T.dontCare();
        if (mIsBacklogRunning) {
            mUIHandler.post(new SafeRunnable() {
                @Override
                protected void safeRun() throws Exception {
                    T.UI();
                    mRunnableStash.add(safeRunnable);
                }
            });
        } else {
            mUIHandler.post(safeRunnable);
        }
    }

    public void removeFromUIHandler(SafeRunnable r) {
        mUIHandler.removeCallbacks(r);
    }

    public void postAtFrontOfIOHandler(SafeRunnable r) {
        mIOHandler.postAtFrontOfQueue(r);
    }

    public void postOnIOHandler(SafeRunnable r) {
        mIOHandler.post(r);
    }

    public void postDelayedOnIOHandler(SafeRunnable r, long delayMillis) {
        mIOHandler.postDelayed(r, delayMillis);
    }

    public void removeFromIOHandler(SafeRunnable r) {
        mIOHandler.removeCallbacks(r);
    }

    public void postAtFrontOfBIZZHandler(SafeRunnable r) {
        mBizzHandler.postAtFrontOfQueue(r);
    }

    public void postOnBIZZHandler(SafeRunnable r) {
        mBizzHandler.post(r);
    }

    public void runOnBIZZHandler(SafeRunnable runnable) {
        if (com.mobicage.rogerthat.util.system.T.getThreadType() == com.mobicage.rogerthat.util.system.T.BIZZ) {
            runnable.run();
        } else {
            postOnBIZZHandler(runnable);
        }
    }

    public void runOnBIZZHandlerNow(SafeRunnable runnable) {
        if (com.mobicage.rogerthat.util.system.T.getThreadType() == com.mobicage.rogerthat.util.system.T.BIZZ) {
            runnable.run();
        } else {
            postAtFrontOfBIZZHandler(runnable);
        }
    }

    public void runOnUIHandlerNow(SafeRunnable runnable) {
        if (com.mobicage.rogerthat.util.system.T.getThreadType() == com.mobicage.rogerthat.util.system.T.UI) {
            runnable.run();
        } else {
            postAtFrontOfUIHandler(runnable);
        }
    }

    public void runOnUIHandler(SafeRunnable runnable) {
        if (com.mobicage.rogerthat.util.system.T.getThreadType() == com.mobicage.rogerthat.util.system.T.UI) {
            runnable.run();
        } else {
            postOnUIHandler(runnable);
        }
    }

    public void postDelayedOnBIZZHandler(SafeRunnable r, long delayMillis) {
        mBizzHandler.postDelayed(r, delayMillis);
    }

    public void removeFromBIZZHandler(SafeRunnable r) {
        mBizzHandler.removeCallbacks(r);
    }


    public void postAtFrontOfNewsHandler(SafeRunnable r) {
        mNewsHandler.postAtFrontOfQueue(r);
    }

     public void postOnNewsHandler(SafeRunnable r) {
        mNewsHandler.post(r);
    }

     public void runOnNewsHandler(SafeRunnable runnable) {
        if (com.mobicage.rogerthat.util.system.T.getThreadType() == com.mobicage.rogerthat.util.system.T.NEWS) {
            runnable.run();
        } else {
            postOnNewsHandler(runnable);
        }
    }

     public void runOnNewsHandlerNow(SafeRunnable runnable) {
        if (com.mobicage.rogerthat.util.system.T.getThreadType() == com.mobicage.rogerthat.util.system.T.NEWS) {
            runnable.run();
        } else {
            postAtFrontOfNewsHandler(runnable);
       }
    }

    // Owned by UI thread
    private final SharedPreferences.OnSharedPreferenceChangeListener mPreferencesListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            T.UI();
            if (mPlugins != null) {
                TrackmePlugin trackmePlugin = getPlugin(TrackmePlugin.class);

                if (PREFERENCE_UPLOAD_PHOTO_WIFI.equals(key)) {
                    mHttpCommunicator.setWifiOnlyEnabled(sharedPreferences.getBoolean(key, false));

                } else if (PREFERENCE_TRACKING.equals(key)){
                    boolean trackingEnabled = sharedPreferences.getBoolean(key, true);
                    // immediately change the setting - even if it does not make it yet to the server
                    trackmePlugin.setEnabled(trackingEnabled);
                    saveSettings(trackingEnabled, null);

                } else if (PREFERENCE_PUSH_NOTIFICATIONS.equals(key)) {
                    boolean trackingEnabled = sharedPreferences.getBoolean(PREFERENCE_TRACKING, true);
                    boolean pushNotificationsEnabled = sharedPreferences.getBoolean(key, false);
                    saveSettings(trackingEnabled, pushNotificationsEnabled);
                }
            }
        }
    };

    private void saveSettings(final boolean tracking, final Boolean pushNotifcations) {
        SaveSettingsRequest request = new SaveSettingsRequest();
        request.callLogging = false;
        request.tracking = tracking;
        if (pushNotifcations == null) {
            request.push_notifications = null;
        } else {
            request.push_notifications = new PushNotificationSettingsTO();
            request.push_notifications.enabled = pushNotifcations;
        }

        try {
            com.mobicage.api.system.Rpc.saveSettings(new SaveSettingsResponseHandler(), request);
        } catch (Exception e) {
            UIUtils.showLongToast(MainService.this, getString(R.string.update_settings_failure));
            L.bug("Could not send saveSettings call", e);
        }
    }

    private void initPreferences() {
        T.UI();
        final SharedPreferences options = PreferenceManager.getDefaultSharedPreferences(this);
        if (!options.getBoolean(PREFERENCES_READY, false) || !options.contains(PREFERENCE_EMAIL)) {
            setPreferences();
        }
    }

    private void setPreferences() {
        T.UI();
        TrackmePlugin trackmePlugin = getPlugin(TrackmePlugin.class);

        SettingsTO settings = new SettingsTO();
        settings.geoLocationTracking = trackmePlugin.isLocationSharingEnabled();

        setPreferences(settings);
    }

    private void setPreferences(SettingsTO settings) {
        T.UI();
        try {
            final SharedPreferences options = PreferenceManager.getDefaultSharedPreferences(this);
            final SharedPreferences.Editor editor = options.edit();
            editor.putBoolean(PREFERENCES_READY, true);
            editor.putBoolean(PREFERENCE_TRACKING, settings.geoLocationTracking);
            editor.putString(PREFERENCE_EMAIL, getIdentityStore().getIdentity().getEmail());
            final boolean success = editor.commit();

            if (success)
                L.d("Successfully updated preferences");
            else
                L.d("Failed to update preferences");

            final Intent intent = new Intent(MainService.PREFERENCES_UPDATE_INTENT);
            sendBroadcast(intent);
        } catch (Exception e) {
            L.bug(e);
        }
    }

    private void destroyPreferences() {
        T.UI();
        final SharedPreferences options = PreferenceManager.getDefaultSharedPreferences(this);
        final SharedPreferences.Editor editor = options.edit();
        editor.clear();
        final boolean success = editor.commit();
        if (success)
            L.d("Successfully cleared preferences");
        else
            L.d("Failed to clear preferences");
    }

    // if force == true, communication will happen, except if there is no network
    // if force == false, phone might do optimization (e.g. it knows that call has already been sent by previous
    // communication cycle)
    public void kickHttpCommunication(boolean force, final String reason) {
        T.dontCare();
        if (mHttpCommunicator != null) {
            // Do not use intent since we want wakelock
            mHttpCommunicator.scheduleCommunication(force, reason);
        }
    }

    // logLevel is HistoryItem.FATAL / ERROR / WARNING / INFO / DEBUG
    public void putInHistoryLog(String text, int logLevel) {
        getPlugin(HistoryPlugin.class).addHistoryLog(text, logLevel);
    }

    private void patchIPSettingsForDNSSRV() {
        // Work around FROYO bug which causes DNS SRV to fail
        // http://code.google.com/p/android/issues/detail?id=9431
        java.lang.System.setProperty("java.net.preferIPv4Stack", "true");
        java.lang.System.setProperty("java.net.preferIPv6Addresses", "false");
    }

    public boolean isBatteryPowered() {
        boolean plugged = false;
        Intent intent = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if (intent != null) {
            Bundle extras = intent.getExtras();
            if ((extras != null) && (extras.containsKey("plugged"))) {
                int status = extras.getInt("plugged");
                plugged = status == BatteryManager.BATTERY_PLUGGED_AC || status == BatteryManager.BATTERY_PLUGGED_USB;
            }
        }
        return !plugged;
    }

    public int getScreenScale() {
        // Based on UIScreen.scale on iOS
        if (mScreenScale == null) {
            switch (getResources().getDisplayMetrics().densityDpi) {
            case DisplayMetrics.DENSITY_LOW:
                mScreenScale = 1;
                break;
            case DisplayMetrics.DENSITY_MEDIUM:
                mScreenScale = 2;
                break;
            case DisplayMetrics.DENSITY_HIGH:
                mScreenScale = 3;
                break;
            default:
                mScreenScale = 2;
                break;
            }
        }

        return mScreenScale;
    }

    public boolean isBacklogConnected() {
        if (CloudConstants.USE_XMPP_KICK_CHANNEL) {
            if (mXmppKickChannel != null) {
                return mXmppKickChannel.isConnected();
            }
        }
        return false;
    }

    public boolean isPermitted(final String permission) {
        boolean granted = ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
        L.i(permission + " granted: " + granted);
        return granted;
    }

    private abstract static class SecurityItem {
        public String uid;
        public String type;
        public boolean active;
        public String keyAlgorithm;
        public String keyName;
        public String message;
        public SecurityCallback callback;

        public abstract boolean canExecuteFromQueue(String pin, Map<String, PrivateKey> privateKeys);
    }

    private static class KeyPairSecurityItem extends SecurityItem {

        public String seed;

        public KeyPairSecurityItem(String keyAlgorithm, String keyName, String message, String seed, SecurityCallback<CreateKeyPairResult> callback) {
            this.uid = UUID.randomUUID().toString();
            this.type = "keypair";
            this.keyAlgorithm = keyAlgorithm;
            this.keyName = keyName;
            this.message = message;
            this.seed = seed;
            this.active = false;
            this.callback = callback;
        }

        public boolean canExecuteFromQueue(String pin, Map<String, PrivateKey> privateKeys) {
            return pin != null;
        }
    }

    private static class SeedSecurityItem extends SecurityItem {

        public SeedSecurityItem(String keyAlgorithm, String keyName, String message, final SecurityCallback<String> callback) {
            this.uid = UUID.randomUUID().toString();
            this.type = "seed";
            this.keyAlgorithm = keyAlgorithm;
            this.keyName = keyName;
            this.message = message;
            this.active = false;
            this.callback = callback;
        }

        public boolean canExecuteFromQueue(String pin, Map<String, PrivateKey> privateKeys) {
            return false;
        }
    }

    private static class AddressSecurityItem extends SecurityItem {

        public Long keyIndex;


        public AddressSecurityItem(String keyAlgorithm, String keyName, long keyIndex, String message, final SecurityCallback<String> callback) {
            this.uid = UUID.randomUUID().toString();
            this.type = "address";
            this.keyAlgorithm = keyAlgorithm;
            this.keyName = keyName;
            this.keyIndex = keyIndex;
            this.message = message;
            this.active = false;
            this.callback = callback;
        }

        public boolean canExecuteFromQueue(String pin, Map<String, PrivateKey> privateKeys) {
            return pin != null;
        }
    }

    private static class SignSecurityItem extends SecurityItem {

        public Long keyIndex;
        public byte[] payload;
        public boolean forcePin;

        public SignSecurityItem(String keyAlgorithm, String keyName, Long keyIndex, String message, byte[] payload, boolean forcePin, final SecurityCallback<String> callback) {
            this.uid = UUID.randomUUID().toString();
            this.type = "sign";
            this.keyAlgorithm = keyAlgorithm;
            this.keyName = keyName;
            this.keyIndex = keyIndex;
            this.message = message;
            this.payload = payload;
            this.forcePin = forcePin;
            this.active = false;
            this.callback = callback;
        }

        public boolean canExecuteFromQueue(String pin, Map<String, PrivateKey> privateKeys) {
            final String key = SecurityUtils.getKeyString(this.keyAlgorithm, this.keyName, this.keyIndex);
            if (!privateKeys.containsKey(key) || this.forcePin) {
                return false;
            }
            return true;
        }
    }

    private static class AskPinSecurityItem extends SecurityItem {
        public AskPinSecurityItem(String message, SecurityCallback<Object> callback) {
            this.uid = UUID.randomUUID().toString();
            this.type = "ask_pin";
            this.message = message;
            this.callback = callback;
        }

        @Override
        public boolean canExecuteFromQueue(String pin, Map<String, PrivateKey> privateKeys) {
            return true;
        }
    }

    private void scheduleClearPin() {
        postDelayedOnUIHandler(new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                if (mQueue.size() == 0) {
                    mPin = null;
                    mPrivateKeys = new HashMap<>();
                    mPublicKeys = new HashMap<>();
                    mShouldClearPin = false;
                } else {
                    mShouldClearPin = true;
                }
            }
        }, 1000 * AppConstants.Security.PIN_INTERVAL);
    }

    public void onSetupPinCompleted(final String pin) {
        T.UI();
        if (pin == null) {
            String errorMessage = getString(R.string.user_cancelled_pin_input);
            mSetupPinCallback.onError("user_cancelled_pin_input", errorMessage);
        } else {
            mPin = pin;
            scheduleClearPin();
            mSetupPinCallback.onSuccess(null);
        }
        mSetupPinCallback = null;
    }

    public void onPinEntered(final String uid, final String pin) {
        T.UI();
        mPin = pin;
        scheduleClearPin();
        mEnterPinActivityActive = false;

        SecurityItem si = dequeueSecurityItem(uid);
        if (si == null) {
            return;
        }
        executeSecurityItem(si, true);
    }

    public void onPinCancelled(final String uid) {
        T.UI();
        mEnterPinActivityActive = false;
        SecurityItem si = dequeueSecurityItem(uid);
        if (si != null) {
            String errorMessage = getString(R.string.user_cancelled_pin_input);
            si.callback.onError("user_cancelled_pin_input", errorMessage);
        }
        clearQueue();
    }

    public interface SecurityCallback<T> {
        void onSuccess(T result);
        void onError(String code, String errorMessage);
    }

    public class CreateKeyPairResult {
        public String seed;
        public String publicKey;
    }

    public void setupPin(final SecurityCallback<String> callback) {
        if (mSetupPinCallback != null) {
            L.d("Already setting up pin code.");
            String errorMessage = getString(R.string.setup_pin_was_open);
            callback.onError("setup_pin_was_open", errorMessage);
            return;
        }
        mSetupPinCallback = callback;

        Intent intent = new Intent(this, SetupPinActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(SetupPinActivity.RESULT_VIA_MAINSERVICE, true);
        startActivity(intent);
    }

    public boolean pinInMemory() {
        return mPin != null;
    }

    public void askPin(final String message, final SecurityCallback<Object> callback) {
        T.dontCare();
        runOnUIHandlerNow(new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                queueSecurityItem(new AskPinSecurityItem(message, callback));
            }
        });
    }

    public void createKeyPair(final String keyAlgorithm, final String keyName, final String message, final String seed, final SecurityCallback<CreateKeyPairResult> callback) {
        T.dontCare();
        runOnUIHandlerNow(new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                queueSecurityItem(new KeyPairSecurityItem(keyAlgorithm, keyName, message, seed, callback));
            }
        });
    }

    public void getSeed(final String keyAlgorithm, final String keyName, final String message, final SecurityCallback<String> callback) {
        T.dontCare();
        runOnUIHandlerNow(new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                queueSecurityItem(new SeedSecurityItem(keyAlgorithm, keyName, message, callback));
            }
        });
    }

    public void getAddress(final String keyAlgorithm, final String keyName, final long keyIndex, final String message, final SecurityCallback<String> callback) {
        T.dontCare();
        runOnUIHandlerNow(new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                queueSecurityItem(new AddressSecurityItem(keyAlgorithm, keyName, keyIndex, message, callback));
            }
        });
    }

    public void sign(final String keyAlgorithm, final String keyName, final Long keyIndex, final String message, final byte[] payload, final boolean forcePin, final SecurityCallback<String> callback) {
        T.dontCare();
        runOnUIHandlerNow(new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                queueSecurityItem(new SignSecurityItem(keyAlgorithm, keyName, keyIndex, message, payload, forcePin, callback));
            }
        });
    }

    public boolean validate(final String keyAlgorithm, final String keyName, final Long keyIndex, final byte[] payload, final byte[] payloadSignature) {
        T.UI();
        try {
            return validateSignature(keyAlgorithm, keyName, keyIndex, payload, payloadSignature);
        } catch (Exception e) {
            L.d(e);
            return false;
        }
    }

    private void queueSecurityItem(SecurityItem si) {
        T.UI();
        boolean shouldEnterPin = false;
        if ("keypair".equals(si.type) || "address".equals(si.type)) {
            if (mPin == null) {
                shouldEnterPin = true;
            }
        } else if ("seed".equals(si.type)) {
            shouldEnterPin = true;

        } else if ("sign".equals(si.type)) {
            SignSecurityItem ssi = (SignSecurityItem) si;
            if (mPin == null || ssi.forcePin) {
                shouldEnterPin = true;
            }
        } else if ("ask_pin".equals(si.type)) {
            shouldEnterPin = true;
        }

        if (shouldEnterPin) {
            if (!mEnterPinActivityActive) {
                mEnterPinActivityActive = true;
                Intent intent = new Intent(this, EnterPinActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(EnterPinActivity.RESULT_VIA_MAINSERVICE, true);
                intent.putExtra(EnterPinActivity.UID, si.uid);
                if (TextUtils.isEmptyOrWhitespace(si.message)) {
                    intent.putExtra(EnterPinActivity.MESSAGE, getString(R.string.pin_required_continue));
                } else {
                    intent.putExtra(EnterPinActivity.MESSAGE, si.message);
                }
                startActivity(intent);
                si.active = true;
            }
            mQueue.add(si);
        } else {
            executeSecurityItem(si, false);
        }
    }

    private SecurityItem getNextSecurityItemToSign() {
        T.UI();
        for (SecurityItem item : mQueue) {
            if (!item.active) {
                mQueue.remove(item);
                return item;
            }
        }
        return null;
    }

    private void executeSecurityItem(SecurityItem si, boolean fromQueue) {
        if ("keypair".equals(si.type)) {
            executeCreateKeyPair((KeyPairSecurityItem) si);

        } else if ("seed".equals(si.type)) {
            executeGetSeed((SeedSecurityItem) si);

        } else if ("address".equals(si.type)) {
            executeGetAddress((AddressSecurityItem) si);

        } else if ("sign".equals(si.type)) {
            executeSign((SignSecurityItem) si);

        } else if ("ask_pin".equals(si.type)) {
            si.callback.onSuccess(null);
        }

        if (fromQueue && !mEnterPinActivityActive) {
            SecurityItem nextSi = getNextSecurityItemToSign();
            if (nextSi != null) {
                executeSecurityItem(nextSi, true);
            } else if (mShouldClearPin) {
                mPin = null;
                mPrivateKeys = new HashMap<>();
                mPublicKeys = new HashMap<>();
                mShouldClearPin = false;
            }
        }
    }

    private void executeCreateKeyPair(KeyPairSecurityItem si) {
        try {
            String publicKeyString = SecurityUtils.createKeyPair(this, mPin, si.keyAlgorithm, si.keyName, si.seed);
            String seed;
            if (TextUtils.isEmptyOrWhitespace(si.seed)) {
                seed = SecurityUtils.getSeed(this, mPin, si.keyAlgorithm, si.keyName);
            } else {
                seed = si.seed;
            }

            final CreateKeyPairResult r = new CreateKeyPairResult();
            r.publicKey = publicKeyString;
            r.seed = seed;
            si.callback.onSuccess(r);
        } catch (Exception e) {
            String errorMessage;
            String errorCode;
            if (e.getMessage() != null && e.getMessage().contains("unknown word")) {
                errorCode = "unknown_word";
                errorMessage = getString(R.string.invalid_word, e.getMessage().split("unknown word: ")[1]);
            } else {
                L.bug("Failed to executeCreateKeyPair", e);
                errorCode = "unknown_error_occurred";
                errorMessage = getString(R.string.unknown_error_occurred);
            }
            si.callback.onError(errorCode, errorMessage);
        }
    }

    private void executeGetSeed(SeedSecurityItem si) {
        T.UI();
        try {
            String seed = SecurityUtils.getSeed(this, mPin, si.keyAlgorithm, si.keyName);
            si.callback.onSuccess(seed);
        } catch (Exception e) {
            L.bug("Failed to executeGetSeed", e);
            String errorMessage = getString(R.string.unknown_error_occurred);
            si.callback.onError("unknown_error_occurred", errorMessage);
        }
    }

    private void executeGetAddress(AddressSecurityItem si) {
        T.UI();
        try {
            String address = SecurityUtils.getAddress(this, mPin, si.keyAlgorithm, si.keyName, si.keyIndex);
            si.callback.onSuccess(address);
        } catch (Exception e) {
            L.bug("Failed to executeGetAddress", e);
            String errorMessage = getString(R.string.unknown_error_occurred);
            si.callback.onError("unknown_error_occurred", errorMessage);
        }
    }

    private void executeSign(SignSecurityItem si) {
        T.UI();
        try {
            byte[] payloadSignatureBytes = signValue(si.keyAlgorithm, si.keyName, si.keyIndex, si.payload);
            String payloadSignature;
            if (Ed25519.ALGORITHM.equals(si.keyAlgorithm)) {
                payloadSignature = SecurityUtils.lowercaseHash(payloadSignatureBytes);
            } else {
                payloadSignature = Base64.encodeBytes(payloadSignatureBytes, Base64.DONT_BREAK_LINES);
            }

            si.callback.onSuccess(payloadSignature);
        } catch (Exception e) {
            L.bug("Failed to executeSign", e);
            String errorMessage = getString(R.string.unknown_error_occurred);
            si.callback.onError("unknown_error_occurred", errorMessage);
        }
    }

    private void clearQueue() {
        T.UI();
        for (int i= mQueue.size() - 1; i >= 0; i--) {
            SecurityItem si = mQueue.get(i);
            if (!si.active) {
                if (!si.canExecuteFromQueue(mPin, mPrivateKeys)) {
                    String errorMessage = getString(R.string.user_cancelled_pin_input);
                    si.callback.onError("user_cancelled_pin_input", errorMessage);
                    mQueue.remove(si);
                }
            }
        }

        if (mQueue.size() != 0) {
            for (SecurityItem si : mQueue) {
                if (!si.active) {
                    executeSecurityItem(si, true);
                    break;
                }
            }
        }
    }

    private SecurityItem dequeueSecurityItem(String uid) {
        T.UI();
        for (SecurityItem item : mQueue) {
            if (item.uid.equals(uid)) {
                mQueue.remove(item);
                return item;
            }
        }
        return null;
    }

    private byte[] signValue(final String keyAlgorithm, final String keyName, final Long keyIndex, byte[] payload) throws Exception {
        T.UI();
        final String key = SecurityUtils.getKeyString(keyAlgorithm, keyName, keyIndex);
        if (!mPrivateKeys.containsKey(key)) {
            mPrivateKeys.put(key, SecurityUtils.getPrivateKey(this, mPin, key));
        }
        Signature s = SecurityUtils.getSignature(keyAlgorithm);
        if (s == null) {
            return null;
        }
        s.initSign(mPrivateKeys.get(key));
        s.update(payload);
        return s.sign();
    }

    private boolean validateSignature(final String keyAlgorithm, final String keyName, final Long keyIndex, byte[] payload, byte[] payloadSignature) throws Exception {
        T.UI();
        final String key = SecurityUtils.getKeyString(keyAlgorithm, keyName, keyIndex);
        if (!mPublicKeys.containsKey(key)) {
            mPublicKeys.put(key, SecurityUtils.getPublicKey(this, keyAlgorithm, keyName, keyIndex));
        }
        Signature s = SecurityUtils.getSignature(keyAlgorithm);
        if (s == null) {
            return false;
        }
        s.initVerify(mPublicKeys.get(key));
        s.update(payload);
        return s.verify(payloadSignature);
    }

    private void restoreBadgeValues() {
        MessagingPlugin messagingPlugin = getPlugin(MessagingPlugin.class);
        badges.put("messages", messagingPlugin.getBadgeCount());
        if (AppConstants.NEWS_ENABLED) {
            NewsPlugin newsPlugin = getPlugin(NewsPlugin.class);
            for (String feedName : newsPlugin.getFeedNames()) {
                String key = newsPlugin.getFeedKey(feedName);
                badges.put(key, newsPlugin.getBadgeCount(feedName));
            }
        }
    }

    public Map<String, Long> getBadges() {
        return badges;
    }

}

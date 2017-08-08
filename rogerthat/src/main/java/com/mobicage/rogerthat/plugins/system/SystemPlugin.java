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

package com.mobicage.rogerthat.plugins.system;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.telephony.TelephonyManager;

import com.mobicage.api.system.Rpc;
import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.MainService;
import com.mobicage.rogerthat.config.Configuration;
import com.mobicage.rogerthat.config.ConfigurationProvider;
import com.mobicage.rogerthat.cordova.CordovaSettings;
import com.mobicage.rogerthat.plugins.MobicagePlugin;
import com.mobicage.rogerthat.plugins.messaging.BrandingFailureException;
import com.mobicage.rogerthat.plugins.messaging.BrandingMgr;
import com.mobicage.rogerthat.util.TextUtils;
import com.mobicage.rogerthat.util.db.DatabaseManager;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.net.NetworkConnectivityManager;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.system.SystemUtils;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.ui.UIUtils;
import com.mobicage.rpc.config.LookAndFeelConstants;
import com.mobicage.to.app.GetAppAssetRequestTO;
import com.mobicage.to.app.UpdateAppAssetRequestTO;
import com.mobicage.to.app.UpdateLookAndFeelRequestTO;
import com.mobicage.to.js_embedding.JSEmbeddingItemTO;
import com.mobicage.to.system.EmbeddedAppTranslationsTO;
import com.mobicage.to.system.HeartBeatRequestTO;
import com.mobicage.to.system.SettingsTO;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

public class SystemPlugin implements MobicagePlugin {

    public static final String ASSET_CHAT_BACKGROUND = "ChatBackgroundImage";
    public static final String SYSTEM_PLUGIN_MUST_REFRESH_JS_EMBEDDING = "com.mobicage.rogerthat.plugins.system.SYSTEM_PLUGIN_MUST_REFRESH_JS_EMBEDDING";
    public static final String SYSTEM_PLUGIN_MUST_DOWNLOAD_ASSETS = "com.mobicage.rogerthat.plugins.system.SYSTEM_PLUGIN_MUST_DOWNLOAD_ASSETS";
    public static final String ASSET_AVAILABLE_INTENT = "com.mobicage.rogerthat.plugins.system.ASSET_AVAILABLE_INTENT";
    public static final String ASSET_KIND = "asset_kind";
    public static final String LOOK_AND_FEEL_UPDATED_INTENT = "com.mobicage.rogerthat.plugins.system.LOOK_AND_FEEL_UPDATED_INTENT";
    public static final String QR_CODE_ADDED_INTENT = "QR_CODE_ADDED_INTENT";
    public static final String QR_CODE_DELETED_INTENT = "QR_CODE_DELETED_INTENT ";

    private static final String CONFIGKEY = "com.mobicage.rogerthat.plugins.system";
    private static final String HEARTBEAT_INFO = "heartbeat_info";

    private static final String CONFIG_WIFI_ONLY_DOWNLOADS = "wifiOnlyDownloads";

    private final ConfigurationProvider mConfigProvider;
    private final NetworkConnectivityManager mNetworkConnectivityManager;
    private final MainService mMainService;
    private final BrandingMgr mBrandingMgr;

    private final SystemStore mStore;
    private boolean mWifiOnlyDownloads = false;


    public SystemPlugin(final MainService mainService, ConfigurationProvider pConfigProvider,
        NetworkConnectivityManager pNetworkConnectivityManager, final BrandingMgr brandingMgr,
        final DatabaseManager dbManager) {
        T.UI();
        mMainService = mainService;
        mConfigProvider = pConfigProvider;
        mNetworkConnectivityManager = pNetworkConnectivityManager;
        mBrandingMgr = brandingMgr;
        mStore = new SystemStore(mainService, dbManager);
    }

    public BrandingMgr getBrandingMgr() {
        T.dontCare();
        return mBrandingMgr;
    }

    public void doHeartbeat() {
        T.UI();

        MobileInfo info = gatherMobileInfo(mMainService);

        Configuration cfg = mConfigProvider.getConfiguration(CONFIGKEY);
        String heartBeatInfo = info.getFingerPrint();
        if (heartBeatInfo.equals(cfg.get(HEARTBEAT_INFO, null)))
            return;

        final long now = System.currentTimeMillis();
        final HeartBeatResponseHandler rh = new HeartBeatResponseHandler();
        rh.setRequestSubmissionTimestamp(now);

        HeartBeatRequestTO request = new HeartBeatRequestTO();

        // general info
        request.buildFingerPrint = Build.FINGERPRINT;
        request.flushBackLog = false;
        request.networkState = mNetworkConnectivityManager.getNetworkState();
        request.timestamp = now / 1000;

        // app info
        request.appType = info.app.type;
        request.majorVersion = info.app.majorVersion;
        request.minorVersion = info.app.minorVersion;
        request.product = info.app.name;

        // sim card info
        request.simCountry = info.sim.isoCountryCode;
        request.simCountryCode = info.sim.mobileCountryCode;
        request.simCarrierCode = info.sim.mobileNetworkCode;
        request.simCarrierName = info.sim.carrierName;

        // net info
        request.netCountry = info.network.isoCountryCode;
        request.netCountryCode = info.network.mobileCountryCode;
        request.netCarrierCode = info.network.mobileNetworkCode;
        request.netCarrierName = info.network.carrierName;

        // locale info
        request.localeCountry = info.locale.country;
        request.localeLanguage = info.locale.language;

        // timeZone info
        request.timezone = info.timeZone.abbrevation;
        request.timezoneDeltaGMT = info.timeZone.secondsFromGMT;

        // device info
        request.deviceModelName = info.device.modelName;
        request.SDKVersion = info.device.osVersion;

        request.embeddedApps = CordovaSettings.APPS.toArray(new String[CordovaSettings.APPS.size()]);

        try {
            L.d("Heartbeating to server");
            com.mobicage.api.system.Rpc.heartBeat(rh, request);

            cfg.put(HEARTBEAT_INFO, heartBeatInfo);
            mConfigProvider.updateConfigurationNow(CONFIGKEY, cfg);
        } catch (Exception e) {
            L.d("Cannot heartbeat", e);
        }
    }

    @Override
    public void destroy() {
        T.UI();
        mConfigProvider.unregisterListener(CONFIGKEY, this);
        mBrandingMgr.close();
    }

    @Override
    public void processSettings(SettingsTO settings) {
        T.UI();
        final Configuration cfg = mConfigProvider.getConfiguration(CONFIGKEY);

        mWifiOnlyDownloads = settings.wifiOnlyDownloads;
        cfg.put(CONFIG_WIFI_ONLY_DOWNLOADS, mWifiOnlyDownloads);

        mConfigProvider.updateConfigurationLater(CONFIGKEY, cfg);
    }

    @Override
    public void reconfigure() {
        T.UI();
    }

    public SystemStore getStore() {
        T.dontCare();
        return mStore;
    }

    @Override
    public void initialize() {
        T.UI();
        mWifiOnlyDownloads = mConfigProvider.getConfiguration(CONFIGKEY).get(CONFIG_WIFI_ONLY_DOWNLOADS, false);

        mConfigProvider.registerListener(CONFIGKEY, this);

        // ugly hack to run this initialization _after_ all plugins have been initialized
        // XXX: build support in MainService framework
        mMainService.postOnUIHandler(new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                T.UI();
                mMainService.postOnBIZZHandler(new SafeRunnable() {
                    @Override
                    protected void safeRun() throws Exception {
                        Set<String> pluginDBUpdates = mMainService.getPluginDBUpdates(SystemPlugin.class);
                        if (pluginDBUpdates.contains(
                            SYSTEM_PLUGIN_MUST_REFRESH_JS_EMBEDDING)) {

                            refreshJsEmdedding();

                            mMainService.clearPluginDBUpdate(SystemPlugin.class,
                                SYSTEM_PLUGIN_MUST_REFRESH_JS_EMBEDDING);
                        } else if (pluginDBUpdates.contains(SYSTEM_PLUGIN_MUST_DOWNLOAD_ASSETS)) {
                            GetAppAssetRequestTO getAppAssetRequestTO = new GetAppAssetRequestTO();
                            getAppAssetRequestTO.kind = ASSET_CHAT_BACKGROUND;
                            final GetAppAssetResponseHandler responseHandler = new GetAppAssetResponseHandler();
                            Rpc.getAppAsset(responseHandler, getAppAssetRequestTO);
                            mMainService.clearPluginDBUpdate(SystemPlugin.class, SYSTEM_PLUGIN_MUST_DOWNLOAD_ASSETS);
                        }

                    }
                });
            }
        });
    }

    public void refreshJsEmdedding() {
        // Set an empty array in the DB to clear all packets
        updateJSEmbeddedPackets(new JSEmbeddingItemTO[0]);

        final com.mobicage.to.js_embedding.GetJSEmbeddingRequestTO request = new com.mobicage.to.js_embedding.GetJSEmbeddingRequestTO();
        final GetJSEmbeddingResponseHandler responseHandler = new GetJSEmbeddingResponseHandler();
        try {
            com.mobicage.api.system.Rpc.getJsEmbedding(responseHandler, request);
        } catch (Exception e) {
            L.bug(e);
        }
    }

    public boolean getWifiOnlyDownloads() {
        return mWifiOnlyDownloads;
    }

    public Map<String, JSEmbedding> getJSEmbeddedPackets() {
        return mStore.getJSEmbeddedPackets();
    }

    public void updateJSEmbeddedPacket(final String name, final String embeddingHash, final long status) {
        mStore.updateJSEmbeddedPacket(name, embeddingHash, status);
    }

    public void updateJSEmbeddedPackets(final JSEmbeddingItemTO[] packets) {
        Map<String, JSEmbedding> oldPackets = getJSEmbeddedPackets();
        List<JSEmbeddingItemTO> packetsToDownload = new ArrayList<JSEmbeddingItemTO>();
        for (final JSEmbeddingItemTO packet : packets) {
            JSEmbedding s = oldPackets.get(packet.name);
            oldPackets.remove(packet.name);
            if (!(s != null && s.getEmeddingHash().equals(packet.hash) && s.getStatus() == JSEmbedding.STATUS_AVAILABLE)) {
                updateJSEmbeddedPacket(packet.name, packet.hash, JSEmbedding.STATUS_UNAVAILABLE);
                packetsToDownload.add(packet);
            }
        }
        for (final JSEmbeddingItemTO packet : packetsToDownload) {
            mBrandingMgr.queue(packet);
        }

        for (String key : oldPackets.keySet()) {
            mStore.deleteJSEmbeddedPacket(key);
            mBrandingMgr.cleanupJSEmbeddingPacket(key);
        }
    }

    public static MobileInfo gatherMobileInfo(MainService mainService) {
        T.dontCare();
        MobileInfo info = new MobileInfo();

        // Application info
        info.app.majorVersion = mainService.getMajorVersion();
        info.app.minorVersion = mainService.getMinorVersion();
        info.app.name = mainService.getString(R.string.app_name) + " Android";
        info.app.type = 4;

        // Carrier info
        TelephonyManager telephonyManager = (TelephonyManager) mainService.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null) {
            info.sim.carrierName = telephonyManager.getSimOperatorName();
            info.sim.mobileNetworkCode = telephonyManager.getSimOperator();
            info.sim.mobileCountryCode = null;
            info.sim.isoCountryCode = telephonyManager.getSimCountryIso();

            // Result may be unreliable on CDMA networks
            if (telephonyManager.getPhoneType() != TelephonyManager.PHONE_TYPE_CDMA) {
                info.network.carrierName = telephonyManager.getNetworkOperatorName();
                info.network.mobileNetworkCode = telephonyManager.getNetworkOperator();
                info.network.mobileCountryCode = null;
                info.network.isoCountryCode = telephonyManager.getNetworkCountryIso();
            }
        }

        // Device info
        info.device.modelName = SystemUtils.isRunningInEmulator(mainService) ? "Android emulator" : Build.MODEL;
        info.device.osVersion = SystemUtils.getAndroidVersion() + "";

        // Locale info
        Locale locale = Locale.getDefault();
        info.locale.country = locale.getCountry();
        info.locale.language = locale.getLanguage();

        // TimeZone info
        TimeZone timeZone = TimeZone.getDefault();
        info.timeZone.abbrevation = timeZone.getID();
        info.timeZone.secondsFromGMT = timeZone.getRawOffset() / 1000;

        return info;
    }

    public void updateAppAsset(String kind, String url, float scaleX) {
        if (url == null) {
            File file = null;
            try {
                file = mBrandingMgr.getAssetFile(mMainService, kind);
            } catch (BrandingFailureException e) {
                L.bug(e);
            }
            if (file != null) {
                boolean fileDeleted = file.delete();
                L.d("Asset " + kind + " deleted:" + fileDeleted);

                Intent intent = new Intent(ASSET_AVAILABLE_INTENT);
                intent.putExtra(ASSET_KIND, kind);
                mMainService.sendBroadcast(intent);
            }

        } else {
            UpdateAppAssetRequestTO packet = new UpdateAppAssetRequestTO();
            packet.kind = kind;
            packet.url = url;
            packet.scale_x = scaleX;

            float pictureSize = UIUtils.getDisplayWidth(mMainService) * packet.scale_x;
            mBrandingMgr.queue(packet, packet.url + "=s" + Math.min(1600, Math.round(pictureSize)));
        }
    }

    public static Bitmap getAppAsset(Context context, String kind) {
        Bitmap bitmap = null;
        try {
            File file = BrandingMgr.getAssetFile(context, kind);

            if (file.exists()) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                options.inJustDecodeBounds = false;
                bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            }
        } catch (BrandingFailureException e) {
            L.bug(e);
        }
        return bitmap;
    }

    public void createQRCode(final QRCode qrCode) {
        mStore.insertQR(qrCode);

        Intent intent = new Intent(QR_CODE_ADDED_INTENT);
        intent.putExtra("content", qrCode.content);
        intent.putExtra("name", qrCode.name);
        mMainService.sendBroadcast(intent);
    }

    public void deleteQRCode(final QRCode qrCode) {
        mStore.deleteQR(qrCode);

        Intent intent = new Intent(QR_CODE_DELETED_INTENT);
        intent.putExtra("content", qrCode.content);
        intent.putExtra("name", qrCode.name);
        mMainService.sendBroadcast(intent);
    }

    public List<QRCode> listQRCodes() {
        return mStore.listQRs();
    }

    public void updateLookAndFeel(UpdateLookAndFeelRequestTO request) {
        LookAndFeelConstants.saveDynamicLookAndFeel(mMainService, request.look_and_feel);

        String kind = LookAndFeelConstants.getAssetKindOfHeaderImage();
        if (request.look_and_feel == null
                || TextUtils.isEmptyOrWhitespace(request.look_and_feel.homescreen.header_image_url)) {
            File file = null;
            try {
                file = mBrandingMgr.getAssetFile(mMainService, kind);
            } catch (BrandingFailureException e) {
                L.bug(e);
            }
            if (file != null) {
                boolean fileDeleted = file.delete();
                L.d("Asset " + kind + " deleted:" + fileDeleted);

                Intent intent = new Intent(ASSET_AVAILABLE_INTENT);
                intent.putExtra(ASSET_KIND, kind);
                mMainService.sendBroadcast(intent);
            }
        } else {
            UpdateAppAssetRequestTO packet = new UpdateAppAssetRequestTO();
            packet.kind = kind;
            packet.url = request.look_and_feel.homescreen.header_image_url;
            packet.scale_x = 0;

            mBrandingMgr.queue(packet, request.look_and_feel.homescreen.header_image_url);
        }

        mMainService.sendBroadcast(new Intent(LOOK_AND_FEEL_UPDATED_INTENT));
    }

    public void updateEmbeddedAppTranslations(final EmbeddedAppTranslationsTO[] translations) {
        for (final EmbeddedAppTranslationsTO t : translations) {
            mStore.insertEmbeddedAppTranslations(t.embedded_app, t.translations);
        }
    }
}

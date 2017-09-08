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

package com.mobicage.rogerthat.cordova;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.ServiceBoundActivity;
import com.mobicage.rogerthat.plugins.friends.ActionScreenActivity;
import com.mobicage.rogerthat.plugins.friends.Friend;
import com.mobicage.rogerthat.plugins.friends.FriendsPlugin;
import com.mobicage.rogerthat.plugins.friends.ServiceMenuItemInfo;
import com.mobicage.rogerthat.plugins.messaging.BrandingFailureException;
import com.mobicage.rogerthat.plugins.messaging.BrandingMgr;
import com.mobicage.rogerthat.plugins.messaging.MessagingPlugin;
import com.mobicage.rogerthat.plugins.payment.PaymentPlugin;
import com.mobicage.rogerthat.plugins.system.SystemPlugin;
import com.mobicage.rogerthat.util.ActionScreenUtils;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.ui.UIUtils;

import org.apache.cordova.ConfigXmlParser;
import org.apache.cordova.CordovaInterfaceImpl;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaWebViewImpl;
import org.apache.cordova.PluginManager;
import org.apache.cordova.engine.SystemWebView;
import org.apache.cordova.engine.SystemWebViewEngine;
import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class CordovaActionScreenActivity extends ServiceBoundActivity {

    public static enum CordovaAppType { BRANDING, EMBEDDED_APP }

    public static final String EMBEDDED_APP = "EMBEDDED_APP";
    public static final String TITLE = ActionScreenActivity.ITEM_LABEL;

    protected SystemWebView mBranding;
    protected CordovaWebView mWebInterface;
    protected CordovaInterfaceImpl mCordovaInterface = new CordovaInterfaceImpl(this);

    protected CordovaAppType mType;
    protected String mBrandingKey;
    protected String mItemLabel;
    protected long[] mItemCoords;
    protected String mServiceEmail;
    protected String mItemTagHash;
    protected Friend mServiceFriend;
    protected boolean mRunInBackground;
    protected String mContext;
    protected String mEmbeddedApp;

    protected MessagingPlugin mMessagingPlugin;
    protected FriendsPlugin mFriendsPlugin;
    protected SystemPlugin mSystemPlugin;
    protected PaymentPlugin mPaymentPlugin;
    protected ActionScreenUtils mActionScreenUtils;

    protected volatile BrandingMgr.BrandingResult mBrandingResult = null;

    public String getServiceEmail() {
        return mServiceEmail;
    }

    public String getItemLabel() {
        return mItemLabel;
    }

    public long[] getItemCoords() {
        return mItemCoords;
    }

    public String getItemTagHash() {
        return mItemTagHash;
    }

    public Friend getServiceFriend() {
        return mServiceFriend;
    }

    public String getContext() {
        return mContext;
    }

    public ServiceMenuItemInfo getServiceMenuItem() {
        return new ServiceMenuItemInfo(mItemLabel, mItemTagHash);
    }

    public MessagingPlugin getMessagingPlugin() {
        return mMessagingPlugin;
    }

    public FriendsPlugin getFriendsPlugin() {
        return mFriendsPlugin;
    }

    public SystemPlugin getSystemPlugin() {
        return mSystemPlugin;
    }

    public PaymentPlugin getPaymentPlugin() {
        return mPaymentPlugin;
    }

    public ActionScreenUtils getActionScreenUtils() {
        return mActionScreenUtils;
    }

    public BrandingMgr.BrandingResult getBrandingResult() {
        return mBrandingResult;
    }

    public String getEmbeddedApp() {
        return mEmbeddedApp;
    }

    public Drawable getSplashScreenDrawable() {
        if (mType == CordovaAppType.EMBEDDED_APP) {
            try {
                InputStream ims = getAssets().open("cordova-apps/" + mEmbeddedApp + "/resources/splash.png");
                return Drawable.createFromStream(ims, null);
            } catch (IOException ioe) {
                L.e(ioe);
                return null;
            }
        } else {
            String file = new File(mBrandingResult.dir, "resources/splash.png").getAbsolutePath();
            return Drawable.createFromPath(file);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        mEmbeddedApp = intent.getStringExtra(EMBEDDED_APP);

        mBrandingKey = intent.getStringExtra(ActionScreenActivity.BRANDING_KEY);
        mServiceEmail = intent.getStringExtra(ActionScreenActivity.SERVICE_EMAIL);
        mItemTagHash = intent.getStringExtra(ActionScreenActivity.ITEM_TAG_HASH);
        mItemLabel = intent.getStringExtra(ActionScreenActivity.ITEM_LABEL);
        mItemCoords = intent.getLongArrayExtra(ActionScreenActivity.ITEM_COORDS);
        mRunInBackground = intent.getBooleanExtra(ActionScreenActivity.RUN_IN_BACKGROUND, true);
        mContext = intent.getStringExtra(ActionScreenActivity.CONTEXT);

        mType = mEmbeddedApp == null ? CordovaAppType.BRANDING : CordovaAppType.EMBEDDED_APP;

        setContentViewWithoutNavigationBar(R.layout.cordova_action_screen);

        final int configId = getCordovaConfigId();
        final ConfigXmlParser parser = new ConfigXmlParser();
        parser.parse(this.getResources().getXml(configId));

        mBranding = (SystemWebView) findViewById(R.id.branding);
        mWebInterface = new CordovaWebViewImpl(new SystemWebViewEngine(mBranding));
        mWebInterface.init(mCordovaInterface, parser.getPluginEntries(), parser.getPreferences());

        setTitle(mItemLabel);
        setActivityName("click|" + mItemTagHash);
    }

    protected int getCordovaConfigId() {
        final String filename = getCordovaConfigFilename();

        // Copied from ConfigXmlParser.java, but modified the config filename
        // First checking the class namespace for config.xml
        int id = this.getResources().getIdentifier(filename, "xml", this.getClass().getPackage().getName());
        if (id == 0) {
            // If we couldn't find config.xml there, we'll look in the namespace from AndroidManifest.xml
            id = this.getResources().getIdentifier(filename, "xml", this.getPackageName());
            if (id == 0) {
                L.bug("res/xml/" + filename + ".xml is missing!");
            }
        }
        return id;
    }

    protected String getCordovaConfigFilename() {
        if (mType == CordovaAppType.EMBEDDED_APP) {
            return "cordova_" + mEmbeddedApp.replace('-', '_') + "_config";
        }
        return "cordova_config";
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        PluginManager pluginManager = mWebInterface.getPluginManager();
        if (pluginManager != null) {
            pluginManager.onDestroy();
        }
        mWebInterface.clearHistory();
        mWebInterface.clearCache();
        mWebInterface.loadUrl("about:blank");
        mWebInterface = null;
    }

    @Override
    protected void onServiceUnbound() {
    }

    @Override
    protected void onServiceBound() {
        mMessagingPlugin = mService.getPlugin(MessagingPlugin.class);
        mFriendsPlugin = mService.getPlugin(FriendsPlugin.class);
        mSystemPlugin = mService.getPlugin(SystemPlugin.class);
        mPaymentPlugin = mService.getPlugin(PaymentPlugin.class);

        mActionScreenUtils = new ActionScreenUtils(this, mServiceEmail, mItemTagHash, mRunInBackground);
        if (mType == CordovaAppType.EMBEDDED_APP) {
            displayEmbeddedApp();
        } else {
            displayBranding();
        }
    }

    private void displayEmbeddedApp() {
        String brandingFile = "file:///android_asset/cordova-apps/" + mEmbeddedApp + "/index.html";
        L.d("Loading " + brandingFile);
        mBranding.loadUrl(brandingFile);
    }

    private void displayBranding() {
        try {
            mServiceFriend = mFriendsPlugin.getStore().getExistingFriend(mServiceEmail);
            mBrandingResult = mMessagingPlugin.getBrandingMgr().prepareBranding(mBrandingKey, mServiceFriend, true);

            mBranding.loadUrl("file://" + mBrandingResult.file.getAbsolutePath());

            switch (mBrandingResult.orientation) {
                case LANDSCAPE:
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    break;
                case PORTRAIT:
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    break;
                case DYNAMIC:
                default:
                    break;
            }
        } catch (BrandingFailureException e) {
            UIUtils.showLongToast(this, getString(R.string.failed_to_show_action_screen));
            finish();
            mMessagingPlugin.getBrandingMgr().queue(mServiceFriend);
            L.e("Could not display menu item with screen branding.", e);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        L.i(getClass() + ": onConfigurationChanged");

        if (mBrandingResult != null) {
            L.d("New orientation: " + newConfig.orientation);
            switch (mBrandingResult.orientation) {
                case LANDSCAPE:
                    if (newConfig.orientation != Configuration.ORIENTATION_LANDSCAPE) {
                        L.d("Changing to SCREEN_ORIENTATION_LANDSCAPE");
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    }
                    break;
                case PORTRAIT:
                    if (newConfig.orientation != Configuration.ORIENTATION_PORTRAIT) {
                        L.d("Changing to SCREEN_ORIENTATION_PORTRAIT");
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    }
                    break;
                case DYNAMIC:
                default:
                    break;
            }
        }

        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        L.i(this.getClass().getName() + ".onActivityResult: requestCode = " + requestCode);
        mCordovaInterface.onActivityResult(requestCode, resultCode, intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        L.i(this.getClass().getName() + ".onRequestPermissionsResult");
        try {
            mCordovaInterface.onRequestPermissionResult(requestCode, permissions, grantResults);
        } catch (JSONException e) {
            L.w("JSONException: Parameters provided to onRequestPermissionResult are not valid", e);
        }
    }
}

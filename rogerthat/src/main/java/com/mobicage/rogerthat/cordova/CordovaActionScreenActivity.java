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
import android.os.Bundle;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.ServiceBoundActivity;
import com.mobicage.rogerthat.plugins.friends.ActionScreenActivity;
import com.mobicage.rogerthat.plugins.friends.Friend;
import com.mobicage.rogerthat.plugins.friends.FriendsPlugin;
import com.mobicage.rogerthat.plugins.messaging.BrandingFailureException;
import com.mobicage.rogerthat.plugins.messaging.BrandingMgr;
import com.mobicage.rogerthat.plugins.messaging.MessagingPlugin;
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

public class CordovaActionScreenActivity extends ServiceBoundActivity {

    private SystemWebView mBranding;
    private CordovaWebView mWebInterface;
    private CordovaInterfaceImpl mCordovaInterface = new CordovaInterfaceImpl(this);
    private String mBrandingKey;
    private String mItemLabel;
    private long[] mItemCoords;
    private String mServiceEmail;
    private String mItemTagHash;
    private Friend mServiceFriend;
    private boolean mRunInBackground;

    private MessagingPlugin mMessagingPlugin;
    private FriendsPlugin mFriendsPlugin;

    private volatile BrandingMgr.BrandingResult mBrandingResult = null;

    private ActionScreenUtils mActionScreenUtils;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.cordova_action_screen);

        ConfigXmlParser parser = new ConfigXmlParser();
        parser.parse(this);

        mBranding = (SystemWebView) findViewById(R.id.branding);
        mWebInterface = new CordovaWebViewImpl(new SystemWebViewEngine(mBranding));
        mWebInterface.init(mCordovaInterface, parser.getPluginEntries(), parser.getPreferences());

        Intent intent = getIntent();
        mBrandingKey = intent.getStringExtra(ActionScreenActivity.BRANDING_KEY);
        mServiceEmail = intent.getStringExtra(ActionScreenActivity.SERVICE_EMAIL);
        mItemTagHash = intent.getStringExtra(ActionScreenActivity.ITEM_TAG_HASH);
        mItemLabel = intent.getStringExtra(ActionScreenActivity.ITEM_LABEL);
        mItemCoords = intent.getLongArrayExtra(ActionScreenActivity.ITEM_COORDS);
        mRunInBackground = intent.getBooleanExtra(ActionScreenActivity.RUN_IN_BACKGROUND, true);
        setTitle(mItemLabel);
        setActivityName("click|" + mItemTagHash);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        PluginManager pluginManager = mWebInterface.getPluginManager();
        if(pluginManager != null) {
            pluginManager.onDestroy();
        }
        mWebInterface.clearHistory();
        mWebInterface.clearCache();
        mWebInterface.loadUrl("about:blank");
        mWebInterface = null;
    }

    @Override
    protected void onServiceBound() {
        mMessagingPlugin = mService.getPlugin(MessagingPlugin.class);
        mFriendsPlugin = mService.getPlugin(FriendsPlugin.class);
        mActionScreenUtils = new ActionScreenUtils(this, mServiceEmail, mItemTagHash, mRunInBackground);
        displayBranding();
    }

    @Override
    protected void onServiceUnbound() {
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

    public MessagingPlugin getMessagingPlugin() {
        return mMessagingPlugin;
    }

    public FriendsPlugin getFriendsPlugin() {
        return mFriendsPlugin;
    }

    public BrandingMgr.BrandingResult getBrandingResult() {
        return mBrandingResult;
    }

    public ActionScreenUtils getActionScreenUtils() {
        return mActionScreenUtils;
    }

    protected void onActivityResult(final int requestCode, final int resultCode, final Intent intent) {
        L.i("CordovaActionScreenActivity.onActivityResult requestCode -> " + requestCode);
        super.onActivityResult(requestCode, resultCode, intent);
        mCordovaInterface.onActivityResult(requestCode, resultCode, intent);
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[],
                                           int[] grantResults) {
        L.i("CordovaActionScreenActivity.onRequestPermissionsResult");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        try {
            mCordovaInterface.onRequestPermissionResult(requestCode, permissions, grantResults);
        }
        catch (JSONException e) {
            L.d( "JSONException: Parameters fed into the method are not valid", e);
        }
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
            return;
        }
    }
}

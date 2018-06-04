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

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.plugins.messaging.BrandingMgr;
import com.mobicage.rogerthat.plugins.system.SystemPlugin;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeBroadcastReceiver;
import com.mobicage.rogerthat.util.ui.UIUtils;
import com.mobicage.rpc.IncompleteMessageException;
import com.mobicage.to.app.EmbeddedAppTO;
import com.mobicage.to.app.GetEmbeddedAppsResponseTO;

import org.json.simple.JSONValue;

import java.util.Arrays;
import java.util.Map;

public class ChooseEmbeddedAppActivity extends ServiceBoundActivity implements EmbeddedAppFragment.OnListFragmentInteractionListener {

    public static String RESULT_KEY = "ChooseEmbeddedAppActivity.result";
    ProgressBar mProgressBar;
    private BroadcastReceiver mBroadcastReceiver;
    private EmbeddedAppFragment mEmbeddedAppFragment;
    private EmbeddedAppTO mChosenEmbeddedApp = null;
    private ProgressDialog mEmbeddedAppDownloadSpinner = null;
    private SystemPlugin mSystemPlugin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_embedded_app);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mBroadcastReceiver = getBroadcastReceiver();
        registerReceiver(mBroadcastReceiver, getIntentFilter());
        this.mProgressBar = (ProgressBar) findViewById(R.id.loading_progress_bar);
        mEmbeddedAppFragment = (EmbeddedAppFragment) getSupportFragmentManager().findFragmentById(R.id.embedded_app_fragment);
    }

    @Override
    protected void onServiceBound() {
        mSystemPlugin = mService.getPlugin(SystemPlugin.class);
    }

    @Override
    protected void onServiceUnbound() {
        unregisterReceiver(mBroadcastReceiver);
    }

    private BroadcastReceiver getBroadcastReceiver() {
        return new SafeBroadcastReceiver() {
            @Override
            public String[] onSafeReceive(Context context, Intent intent) {
                final String action = intent.getAction();
                if (action == null) {
                    return new String[]{intent.getAction()};
                }
                try {
                    switch (action) {
                        case SystemPlugin.GET_EMBEDDED_APPS_RESULT_INTENT:
                            Map<String, Object> result = (Map<String, Object>) JSONValue.parse(intent.getStringExtra("json"));
                            showEmbeddedApps(new GetEmbeddedAppsResponseTO(result));
                            break;
                        case SystemPlugin.GET_EMBEDDED_APPS_FAILED_INTENT:
                            String error = intent.getStringExtra("error");
                            UIUtils.showDialog(ChooseEmbeddedAppActivity.this, getString(R.string.activity_error), error);
                            break;
                        case BrandingMgr.EMBEDDED_APP_AVAILABLE_INTENT:
                            String id = intent.getStringExtra("id");
                            if (mChosenEmbeddedApp != null && mChosenEmbeddedApp.name.equals(id)) {
                                mEmbeddedAppDownloadSpinner.dismiss();
                                mEmbeddedAppDownloadSpinner = null;
                                ChooseEmbeddedAppActivity.this.setEmbeddedAppResult(mChosenEmbeddedApp);
                            }
                    }
                } catch (IncompleteMessageException e) {
                    L.bug(e);
                }
                return new String[]{intent.getAction()};
            }
        };
    }

    private void showEmbeddedApps(GetEmbeddedAppsResponseTO response) {
        mProgressBar.setVisibility(View.GONE);
        mEmbeddedAppFragment.setAdapter(new EmbeddedAppRecyclerViewAdapter(Arrays.asList(response.embedded_apps),
                mEmbeddedAppFragment.getInteractionListener()));
    }

    private IntentFilter getIntentFilter() {
        final IntentFilter filter = new IntentFilter();
        filter.addAction(SystemPlugin.GET_EMBEDDED_APPS_RESULT_INTENT);
        filter.addAction(SystemPlugin.GET_EMBEDDED_APPS_FAILED_INTENT);
        filter.addAction(BrandingMgr.EMBEDDED_APP_AVAILABLE_INTENT);
        return filter;
    }

    @Override
    public void onListFragmentInteraction(EmbeddedAppTO embeddedAppTO) {
        boolean downloaded = this.ensureEmbeddedAppIsDownloaded(embeddedAppTO);
        if (downloaded) {
            this.setEmbeddedAppResult(embeddedAppTO);
        }
    }

    /**
     * @param embeddedAppTO
     * @return Whether or not the latest version of this embedded app is already downloaded
     */
    private boolean ensureEmbeddedAppIsDownloaded(@NonNull EmbeddedAppTO embeddedAppTO) {
        long version = mSystemPlugin.getStore().getEmbeddedAppVersion(embeddedAppTO.name);
        if (version < embeddedAppTO.version || !mSystemPlugin.getBrandingMgr().embeddedAppExists(embeddedAppTO.name)) {
            // Show loading spinner until embedded app is downloaded. (via onEmbeddedAppAvailable callback)
            mEmbeddedAppDownloadSpinner = UIUtils.showProgressDialog(this, null, getString(R.string.loading), true, false);
            mChosenEmbeddedApp = embeddedAppTO;
            mSystemPlugin.getEmbeddedApp(embeddedAppTO.name);
            return false;
        } else {
            mChosenEmbeddedApp = null;
            return true;
        }
    }

    private void setEmbeddedAppResult(@NonNull EmbeddedAppTO embeddedAppTO) {
        Intent intent = new Intent();
        intent.putExtra(RESULT_KEY, JSONValue.toJSONString(embeddedAppTO.toJSONMap()));
        setResult(Activity.RESULT_OK, intent);
        finish();
    }
}

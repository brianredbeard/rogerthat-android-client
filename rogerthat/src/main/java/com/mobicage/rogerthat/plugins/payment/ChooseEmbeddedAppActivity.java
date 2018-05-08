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

package com.mobicage.rogerthat.plugins.payment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.ServiceBoundActivity;
import com.mobicage.rogerthat.plugins.system.SystemPlugin;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeBroadcastReceiver;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.ui.UIUtils;
import com.mobicage.rpc.IncompleteMessageException;
import com.mobicage.to.app.EmbeddedAppTO;
import com.mobicage.to.app.GetEmbeddedAppsResponseTO;

import org.json.simple.JSONValue;

import java.util.Map;

public class ChooseEmbeddedAppActivity extends ServiceBoundActivity {

    public static String RESULT_KEY = "result";
    ProgressBar mProgressBar;
    private EmbeddedAppTO mPickedEmbeddedApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_embedded_app);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        registerReceiver(getBroadcastReceiver(), getIntentFilter());
        this.mProgressBar = (ProgressBar) findViewById(R.id.loading_progress_bar);
    }

    @Override
    protected void onServiceBound() {
    }

    @Override
    protected void onServiceUnbound() {

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
                            UIUtils.showDialog(mService, getString(R.string.activity_error), error);
                            break;
                    }
                } catch (IncompleteMessageException e) {
                    L.bug(e);
                }
                return new String[]{intent.getAction()};
            }
        };
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        T.UI();
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.send_message_button_menu, menu);
        addIconToMenuItem(menu, R.id.save, FontAwesome.Icon.faw_check);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        T.UI();

        switch (item.getItemId()) {
            case R.id.save:
                if (mPickedEmbeddedApp != null) {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra(RESULT_KEY, JSONValue.toJSONString(mPickedEmbeddedApp.toJSONMap()));
                    setResult(Activity.RESULT_OK, resultIntent);

                } else {
                    setResult(Activity.RESULT_CANCELED);
                }
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showEmbeddedApps(GetEmbeddedAppsResponseTO response) {
        L.i("Received payment providers");
        mProgressBar.setVisibility(View.GONE);
        // TODO show list of payment providers
        // TODO onclick set mPickedEmbeddedApp
    }

    private IntentFilter getIntentFilter() {
        final IntentFilter filter = new IntentFilter();
        filter.addAction(SystemPlugin.GET_EMBEDDED_APPS_RESULT_INTENT);
        filter.addAction(SystemPlugin.GET_EMBEDDED_APPS_FAILED_INTENT);
        return filter;
    }
}

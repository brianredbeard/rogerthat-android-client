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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.ServiceBoundActivity;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeBroadcastReceiver;
import com.mobicage.rogerthat.util.system.SafeDialogClick;
import com.mobicage.rogerthat.util.ui.UIUtils;
import com.mobicage.rpc.IncompleteMessageException;
import com.mobicage.to.payment.GetPaymentMethodsRequestTO;
import com.mobicage.to.payment.GetPaymentMethodsResponseTO;
import com.mobicage.to.payment.PayMethodTO;
import com.mobicage.to.payment.PaymentProviderMethodsTO;

import org.json.simple.JSONValue;

import java.util.ArrayList;
import java.util.Map;


@SuppressWarnings("unchecked")
public class ChoosePaymentMethodActivity extends ServiceBoundActivity {
    public static final String PICKED_PAYMENT_METHOD = "com.mobicage.rogerthat.plugins.payment.PICKED_PAYMENT_METHOD";
    public static final String RESULT_DATA_KEY = "json";
    GetPaymentMethodsRequestTO mRequest;
    ProgressBar mProgressBar;
    ListView mListView;
    ArrayAdapter<PaymentProviderMethod> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_payment_method);
        BroadcastReceiver broadcastReceiver = getBroadcastReceiver();
        final IntentFilter filter = getIntentFilter();
        registerReceiver(broadcastReceiver, filter);
        try {
            mRequest = new GetPaymentMethodsRequestTO((Map<String, Object>) JSONValue.parse(getIntent().getStringExtra("request")));
        } catch (IncompleteMessageException e) {
            L.bug(e);
        }
        this.mProgressBar = (ProgressBar) findViewById(R.id.loading_progress_bar);
        this.mListView = (ListView) findViewById(R.id.payment_methods_list);
        this.mAdapter = new PaymentMethodsAdapter(this);
        this.mListView.setAdapter(this.mAdapter);
        this.mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                finishWithResult((PaymentProviderMethod) mListView.getItemAtPosition(position));
            }
        });
    }

    private void finishWithResult(PaymentProviderMethod method) {
        Intent intent = new Intent(PICKED_PAYMENT_METHOD);
        intent.putExtra(RESULT_DATA_KEY, JSONValue.toJSONString(method.toJSONMap()));
        setResult(RESULT_OK, intent);
        finish();
    }

    private IntentFilter getIntentFilter() {
        final IntentFilter filter = new IntentFilter();
        filter.addAction(PaymentPlugin.GET_PAYMENT_METHODS_RESULT_INTENT);
        filter.addAction(PaymentPlugin.GET_PAYMENT_METHODS_FAILED_INTENT);
        return filter;
    }

    @Override
    protected void onServiceBound() {
        PaymentPlugin paymentPlugin = mService.getPlugin(PaymentPlugin.class);
        if (mService.getNetworkConnectivityManager().isConnected()) {
            paymentPlugin.getPaymentMethods(mRequest);
        } else {
            SafeDialogClick onClick = new SafeDialogClick() {
                @Override
                public void safeOnClick(DialogInterface dialog, int id) {
                    finish();
                }
            };
            UIUtils.showDialog(mService, getString(R.string.no_internet_connection), getString(R.string.no_internet_connection_try_again), onClick);
        }
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
                        case PaymentPlugin.GET_PAYMENT_METHODS_RESULT_INTENT:
                            showPaymentMethods(new GetPaymentMethodsResponseTO((Map<String, Object>) JSONValue.parse(intent.getStringExtra("json"))));
                            break;
                        case PaymentPlugin.GET_PAYMENT_METHODS_FAILED_INTENT:
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

    private void showPaymentMethods(GetPaymentMethodsResponseTO response) {
        ArrayList resultMethods = new ArrayList();
        for (PaymentProviderMethodsTO providerMethod : response.methods) {
            for (PayMethodTO method : providerMethod.methods) {
                resultMethods.add(new PaymentProviderMethod(method, providerMethod.provider));
            }
        }
        this.mAdapter.clear();
        this.mAdapter.addAll(resultMethods);
        this.mAdapter.notifyDataSetChanged();
        mListView.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
    }

}

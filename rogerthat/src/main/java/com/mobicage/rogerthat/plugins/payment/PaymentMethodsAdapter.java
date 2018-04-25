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

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.mobicage.rogerth.at.R;


class PaymentMethodsAdapter extends ArrayAdapter<PaymentProviderMethod> {

    private static class ViewHolder {
        TextView name;
        TextView amount;
    }

    public PaymentMethodsAdapter(@NonNull Context context) {
        super(context, R.layout.payment_method_item);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final PaymentProviderMethod providerMethod = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        final ViewHolder viewHolder; // view lookup cache stored in tag
        if (convertView == null) {
            // If there's no view to re-use, inflate a brand new view for row
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.payment_method_item, parent, false);
            viewHolder.name = (TextView) convertView.findViewById(R.id.provider_name);
            viewHolder.amount = (TextView) convertView.findViewById(R.id.amount);
            // Cache the viewHolder object inside the fresh view
            convertView.setTag(viewHolder);
        } else {
            // View is being recycled, retrieve the viewHolder object from tag
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.name.setText(providerMethod.provider.name);
        viewHolder.amount.setText(getAmount(providerMethod));

        return convertView;
    }

    private String getAmount(PaymentProviderMethod method) {
        final double amount = method.method.amount / Math.pow(10, method.method.precision);
        return String.format("%s %s", amount, method.method.currency);
    }
}

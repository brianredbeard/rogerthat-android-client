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
package com.mobicage.rogerthat;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mobicage.rogerth.at.R;
import com.mobicage.rpc.config.AppConstants;
import com.mobicage.rpc.config.CloudConstants;

public class AboutActivity extends AbstractAboutActivity {

    @Override
    protected void onServiceBound() {
        super.onServiceBound();

        ImageView header = (ImageView) findViewById(AppConstants.FULL_WIDTH_HEADERS ? R.id.full_width_rogerthat_logo
            : R.id.rogerthat_logo);
        header.setVisibility(View.VISIBLE);
        ImageView aboutProvidedLogo = (ImageView) findViewById(R.id.about_provided_logo);
        aboutProvidedLogo.setImageResource(R.drawable.about_footer);

        TextView aboutInfoRogerthat = (TextView) findViewById(R.id.rogerthat_about_info);
        TextView headerSlogan = (TextView) findViewById(R.id.rogerthat_slogan);
        TextView aboutProvidedText = (TextView) findViewById(R.id.about_provided_text);

        if (CloudConstants.isEnterpriseApp()) {
            aboutInfoRogerthat.setText(getString(R.string.about_more_info_enterprise, getString(R.string.app_name)));
            headerSlogan.setVisibility(View.GONE);
            aboutProvidedText.setText(getString(R.string.about_provided_by_enterprise, getString(R.string.app_name)));
        } else {
            aboutInfoRogerthat.setText(getString(R.string.about_more_info, getString(R.string.app_name)));
            headerSlogan.setVisibility(View.VISIBLE);
            headerSlogan.setText(R.string.slogan);
            aboutProvidedText.setText(getString(R.string.about_provided_by, getString(R.string.app_name)));
        }
    }

    @Override
    protected void onServiceUnbound() {
    }
}

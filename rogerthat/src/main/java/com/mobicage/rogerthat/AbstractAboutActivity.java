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
package com.mobicage.rogerthat;

import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.plugins.messaging.MessagingPlugin;
import com.mobicage.rpc.config.AppConstants;

public class AbstractAboutActivity extends ServiceBoundActivity {

    @Override
    protected void onServiceBound() {
        setContentView(R.layout.about);

        Button visitRogerthat = (Button) findViewById(R.id.about_btn_visit_rogerthat);
        visitRogerthat.setText(AppConstants.ABOUT_WEBSITE);
        visitRogerthat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(AppConstants.ABOUT_WEBSITE_URL));
                startActivity(i);
            }
        });

        RelativeLayout emailRogerthat = (RelativeLayout) findViewById(R.id.about_btn_email_rogerthat);
        TextView emailLabel = (TextView) findViewById(R.id.about_email_label);
        emailLabel.setText(AppConstants.ABOUT_EMAIL);
        emailRogerthat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent emailIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(MessagingPlugin.ANDROID_MAILTO_PREFIX
                    + AppConstants.ABOUT_EMAIL));
                startActivity(emailIntent);
            }
        });

        RelativeLayout twitterRogerthat = (RelativeLayout) findViewById(R.id.about_btn_twitter_rogerthat);
        TextView twitterLabel = (TextView) findViewById(R.id.about_twitter_label);
        twitterLabel.setText(AppConstants.ABOUT_TWITTER);
        twitterRogerthat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(AppConstants.ABOUT_TWITTER_URL));
                startActivity(i);
            }
        });

        RelativeLayout facebookRogerthat = (RelativeLayout) findViewById(R.id.about_btn_facebook_rogerthat);
        TextView facebookLabel = (TextView) findViewById(R.id.about_facebook_label);
        facebookLabel.setText(AppConstants.ABOUT_FACEBOOK);
        facebookRogerthat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(AppConstants.ABOUT_FACEBOOK_URL));
                startActivity(i);
            }
        });
    }

    @Override
    protected void onServiceUnbound() {
    }
}

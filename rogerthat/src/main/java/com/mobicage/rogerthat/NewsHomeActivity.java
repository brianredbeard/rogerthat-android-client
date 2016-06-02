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

import android.os.Bundle;
import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.util.TextUtils;
import com.mobicage.rogerthat.widget.ServiceBoundNavigationDrawerActivity;

public class NewsHomeActivity extends ServiceBoundNavigationDrawerActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homescreen_news);

        //Change font
        TextUtils.overrideFonts(this, findViewById(android.R.id.content));
    }

    @Override
    protected void onServiceBound() {
    }

    @Override
    protected void onServiceUnbound() {
    }
}


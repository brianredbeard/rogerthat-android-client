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
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.util.system.SafeViewOnClickListener;

public class NewsActivity extends ServiceBoundActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.news);
        setActivityName("news");

        Button testBtn = (Button) findViewById(R.id.btn_test);
        testBtn.setOnClickListener(new SafeViewOnClickListener() {
            @Override
            public void safeOnClick(View v) {
                Toast.makeText(NewsActivity.this, "This is a TEST", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onServiceBound() {

    }

    @Override
    protected void onServiceUnbound() {

    }
}
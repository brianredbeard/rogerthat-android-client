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

package com.mobicage.rogerthat.test.screenshots;

import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.mobicage.rogerth.at.R;
import com.mobicage.rpc.config.AppConstants;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import tools.fastlane.screengrab.Screengrab;
import tools.fastlane.screengrab.UiAutomatorScreenshotStrategy;
import tools.fastlane.screengrab.locale.LocaleTestRule;


@SuppressWarnings({"ConstantConditions"})
@RunWith(AndroidJUnit4.class)
public class TestNewsActivity {

    @ClassRule
    public static final LocaleTestRule localeTestRule = new LocaleTestRule();

    @Rule
    public ActivityTestRule<FakeNewsActivity> activityTestRule = new ActivityTestRule<>(FakeNewsActivity.class, true, false);

    @Before
    public void setup() throws Throwable {
        Screengrab.setDefaultScreenshotStrategy(new UiAutomatorScreenshotStrategy());
    }

    @Test
    public void takeNewsScreenshot() {

        if (AppConstants.HOME_ACTIVITY_LAYOUT == R.layout.news) {
            Context targetContext = InstrumentationRegistry.getInstrumentation()
                    .getTargetContext();
            Intent intent = new Intent(targetContext, FakeNewsActivity.class);
            intent.putExtra("show_drawer_icon", true);
            activityTestRule.launchActivity(intent);
            // Ensure the drawer is closed because for some reason it is opened sometimes
            activityTestRule.getActivity().closeNavigationView();
            Screengrab.screenshot("news");
        }
    }
}

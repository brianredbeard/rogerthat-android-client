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

package com.mobicage.rogerthat.test.screenshots;

import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.mobicage.rogerthat.MainActivity;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import tools.fastlane.screengrab.FalconScreenshotStrategy;
import tools.fastlane.screengrab.Screengrab;
import tools.fastlane.screengrab.locale.LocaleTestRule;

@RunWith(AndroidJUnit4.class)
public class TestTakeHomescreenScreenshot {

    @ClassRule
    public static final LocaleTestRule localeTestRule = new LocaleTestRule();

    @Rule
    public ActivityTestRule<MainActivity> activityTestRule = new ActivityTestRule<>(MainActivity.class, true, false);

    @Test
    public void takeHomeScreenScreenshot() {
        // homescreen will be news with the sidebar open for apps with news and else the old homescreen with icons
        Context targetContext = InstrumentationRegistry.getInstrumentation()
                .getTargetContext();
        Intent intent = new Intent(targetContext, MainActivity.class);
        activityTestRule.launchActivity(intent);
        Screengrab.setDefaultScreenshotStrategy(new FalconScreenshotStrategy(activityTestRule.getActivity()));
        Screengrab.screenshot("homescreen");
    }
}

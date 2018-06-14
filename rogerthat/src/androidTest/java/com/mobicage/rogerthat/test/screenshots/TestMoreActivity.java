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

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.MoreActivity;
import com.mobicage.rpc.config.AppConstants;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import tools.fastlane.screengrab.FalconScreenshotStrategy;
import tools.fastlane.screengrab.Screengrab;
import tools.fastlane.screengrab.locale.LocaleTestRule;

@RunWith(JUnit4.class)
public class TestMoreActivity {
    @ClassRule
    public static final LocaleTestRule localeTestRule = new LocaleTestRule();

    @Rule
    public ActivityTestRule<MoreActivity> activityTestRule = new ActivityTestRule<>(MoreActivity.class, true, false);

    @SuppressWarnings("ConstantConditions")
    @Test
    public void takeMoreActivityScreenshot(){

        if (AppConstants.HOME_ACTIVITY_LAYOUT != R.layout.news && AppConstants.HOME_ACTIVITY_LAYOUT != R.layout.messaging) {
            Context targetContext = InstrumentationRegistry.getInstrumentation()
                    .getTargetContext();
            Intent intent = new Intent(targetContext, MoreActivity.class);
            activityTestRule.launchActivity(intent);
            Screengrab.setDefaultScreenshotStrategy(new FalconScreenshotStrategy(activityTestRule.getActivity()));
            Screengrab.screenshot("more");
        }
    }
}

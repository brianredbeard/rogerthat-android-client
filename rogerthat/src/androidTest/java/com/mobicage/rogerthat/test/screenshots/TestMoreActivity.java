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

package com.mobicage.rogerthat.test.screenshots;

import android.support.test.rule.ActivityTestRule;
import android.test.UiThreadTest;

import com.mobicage.rogerthat.MoreActivity;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import tools.fastlane.screengrab.Screengrab;
import tools.fastlane.screengrab.locale.LocaleTestRule;

public class TestMoreActivity {
    @ClassRule
    public static final LocaleTestRule localeTestRule = new LocaleTestRule();

    @Rule
    public ActivityTestRule<MoreActivity> activityTestRule = new ActivityTestRule<>(MoreActivity.class);

    @Test
    @UiThreadTest
    public void takeMoreActivityScreenshot(){
        Screengrab.screenshot("more");
    }
}

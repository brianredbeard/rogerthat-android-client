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

package com.mobicage.rogerthat.test.disabled_tests;

import android.support.test.annotation.UiThreadTest;
import android.support.test.espresso.web.sugar.Web;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.AddFriendsActivity;
import com.mobicage.rogerthat.MyIdentity;
import com.mobicage.rogerthat.util.logging.L;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import tools.fastlane.screengrab.Screengrab;
import tools.fastlane.screengrab.locale.LocaleTestRule;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static android.support.test.espresso.web.sugar.Web.onWebView;
import static com.mobicage.rogerthat.test.ui_test_helpers.UiTestHelpers.waitUntilExists;

@RunWith(AndroidJUnit4.class)
public class TestTakeFacebookScreenshot {
    public static class TestAddFriendsActivity extends AddFriendsActivity {
        public boolean isIdentityFetched() {
            if (!mServiceIsBound)
                return false;

            MyIdentity identity = getMyIdentity();
            return identity != null && identity.getShortLink() != null;

        }
    }

    @ClassRule
    public static final LocaleTestRule localeTestRule = new LocaleTestRule();

    @Rule
    public ActivityTestRule<TestAddFriendsActivity> activityTestRule = new ActivityTestRule<>(TestAddFriendsActivity.class);

    @Test
    @UiThreadTest
    public void takeFacebookScreenshotsMain() throws InterruptedException {
        onView(withId(R.id.facebook_layout))
                .perform(click());
        while(!activityTestRule.getActivity().isIdentityFetched()){
            Thread.sleep(500);
            L.d("waiting for identity to be fetched");
        }
        onView(withId(R.id.add_via_facebook_button))
                .perform(click());
        onView(isRoot())
                .perform(waitUntilExists(withText(R.string.post), 2000));
        // Wait for animation
        Thread.sleep(250);
        Screengrab.screenshot("facebook_2");
        onView(withText(R.string.post))
                .perform(click());
        Web.WebInteraction view = null;
        int attempts = 0;
        while (view == null) {
            try {
                L.i("Waiting for facebook webview");
                Thread.sleep(250);
                view = onWebView();
                view.forceJavascriptEnabled();
            } catch (Exception exception) {
                view = null;
            }
            attempts ++;
            if (attempts > 50) {
                throw new RuntimeException("Couldn't find facebook webview after waiting 12 seconds.");
            }
        }
        view.forceJavascriptEnabled();
    }
}

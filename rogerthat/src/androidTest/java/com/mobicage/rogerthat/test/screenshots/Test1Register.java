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

import android.Manifest;
import android.app.Activity;
import android.support.test.annotation.UiThreadTest;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.PerformException;
import android.support.test.espresso.ViewAction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.WindowManager;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.MainService;
import com.mobicage.rogerthat.registration.RegistrationActivity2;
import com.mobicage.rogerthat.test.ui_test_helpers.PermissionsRule;
import com.mobicage.rogerthat.util.logging.L;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import tools.fastlane.screengrab.locale.LocaleTestRule;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.isDialog;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
public class Test1Register {
    @ClassRule
    public static final LocaleTestRule localeTestRule = new LocaleTestRule();

    @Rule
    public ActivityTestRule<RegistrationActivity2> activityTestRule = new ActivityTestRule<>(RegistrationActivity2.class);
    @Rule
    public final PermissionsRule permissionsRule = new PermissionsRule(
            new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.ACCESS_COARSE_LOCATION});

    private void execWithRetries(int resId, ViewAction action, int retryCount) throws InterruptedException {
        int tries = 0;
        while (true) {
            tries++;
            try {
                onView(withId(resId))
                        .check(matches(isDisplayed()))
                        .perform(scrollTo(), action);
                break;
            } catch (NoMatchingViewException | PerformException error) {
                if (tries > retryCount) {
                    throw error;
                } else {
                    Thread.sleep(100);
                }
            }
        }
    }

    @UiThreadTest
    @Before
    public void setup() throws Throwable {
        L.d("Unlocking screen");
        final Activity activity = activityTestRule.getActivity();
        activityTestRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
            }
        });
    }
    @Test
    public void testRegister() throws InterruptedException {
        MainService mainService = activityTestRule.getActivity().getMainService();
        boolean isRegistered = mainService.getRegisteredFromConfig();
        // User already registered. Skip this test. (For screenshot tests)
        if(isRegistered){
            return;
        }
        // Click the 'Start registration' button
        try {
            onView(withId(R.id.registration_start)).perform(click());
        } catch (PerformException ignored) {
        }
        try {
            // Accept terms of use
            onView(withId(R.id.tos_age)).perform(click());
            onView(withId(R.id.tos_agree)).perform(click());
        } catch (PerformException ex) {
            L.i("Not clicking 'agree to TOS' because it was already clicked'");
        }
        // Accept notification usage
        execWithRetries(R.id.notifications_continue, click(), 50);
        // Click 'allow' in the push notification dialog
        onView(withId(android.R.id.button1)).inRoot(isDialog()).perform(click());
        // Fill in email field
        try {
            onView(withId(R.id.registration_enter_email))
                    .perform(typeText("apple.review@rogerth.at"));
            Espresso.closeSoftKeyboard();
            onView(withId(R.id.login_via_email)).perform(click());
        } catch (PerformException ignored) {
        }
        execWithRetries(R.id.registration_enter_pin, typeText("0666"), 100);
        // Keep activity open until we are properly registered.
        while (!mainService.getRegisteredFromConfig()) {
            try {
                onView(withId(R.id.registration_devices_register))
                        .perform(click());
            } catch (PerformException ex) {
                L.i("Not clicking 'Unregister other devices'");
            } catch (NoMatchingViewException e) {
                L.i("devices view not found");
            }
            Thread.sleep(100);
        }
    }
}

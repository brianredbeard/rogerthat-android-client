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

import android.support.test.espresso.Espresso;
import android.support.test.espresso.PerformException;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.registration.RegistrationActivity2;
import com.mobicage.rogerthat.util.logging.L;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import tools.fastlane.screengrab.locale.LocaleTestRule;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.mobicage.rogerthat.test.ui_test_helpers.UiTestHelpers.waitUntilExists;

@RunWith(AndroidJUnit4.class)
public class Test1Register {
    @ClassRule
    public static final LocaleTestRule localeTestRule = new LocaleTestRule();

    @Rule
    public ActivityTestRule<RegistrationActivity2> activityTestRule = new ActivityTestRule<>(RegistrationActivity2.class);

    @Test
    public void testRegister() throws InterruptedException {
        // This test requires that the facebook app is NOT installed on the device the tests run on

        boolean isRegistered = activityTestRule.getActivity().getMainService().getRegisteredFromConfig();
        // User already registered. Skip this test. (For screenshot tests)
        if(isRegistered){
            return;
        }
        try {
            onView(withId(R.id.registration_agree_tos))
                    .perform(click());
        } catch (PerformException ex) {
            L.i("Not clicking 'agree to TOS because it was already clicked'");
        }
        // Fill in email field
        onView(withId(R.id.registration_enter_email))
                .perform(typeText("apple.review@rogerth.at"));
        Espresso.closeSoftKeyboard();
        onView(withId(R.id.login_via_email))
                .perform(click());
        onView(isRoot())
                .perform(waitUntilExists(withId(R.id.registration_enter_pin), 2000));
        onView(withId(R.id.registration_enter_pin))
                .perform(typeText("0666"));
        while(!activityTestRule.getActivity().isFinishing()){
            Thread.sleep(250);
        }
        // Allow the activity to finish
        Thread.sleep(1000);
    }
}

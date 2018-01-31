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

package com.mobicage.rogerthat.test.ui_test_helpers;

import android.os.Build;
import android.support.test.InstrumentationRegistry;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * This rule adds selected permissions to test app
 */

public class PermissionsRule implements TestRule {

    private final String[] permissions;

    public PermissionsRule(String[] permissions) {
        this.permissions = permissions;
    }

    @Override
    public Statement apply(final Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {

                allowPermissions();

                base.evaluate();
            }
        };
    }

    private void allowPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String permission : permissions) {
                InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand(
                        "pm grant " + InstrumentationRegistry.getTargetContext().getPackageName()
                                + " " + permission);
            }
        }
    }

    private void revokePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String permission : permissions) {
                InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand(
                        "pm revoke " + InstrumentationRegistry.getTargetContext().getPackageName()
                                + " " + permission);
            }
        }
    }
}

package com.mobicage.rogerthat.util.ui;

import java.util.concurrent.atomic.AtomicBoolean;

public class TestUtils {
    private static AtomicBoolean isRunningTest;

    public static synchronized boolean isRunningTest() {
        if (null == isRunningTest) {
            boolean istest;

            try {
                Class.forName("com.mobicage.rogerthat.test.screenshots.FakeNewsActivity");
                istest = true;
            } catch (ClassNotFoundException e) {
                istest = false;
            }

            isRunningTest = new AtomicBoolean(istest);
        }

        return isRunningTest.get();
    }
}
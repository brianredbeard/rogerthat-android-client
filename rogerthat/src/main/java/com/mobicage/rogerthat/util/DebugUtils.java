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

package com.mobicage.rogerthat.util;

import com.mobicage.rogerthat.util.logging.L;

import java.util.concurrent.Callable;

public class DebugUtils {

    public static void profile(String action, Runnable r) {
        long start = System.currentTimeMillis();
        try {
            r.run();
        } finally {
            L.d(">=== Executed " + action + " in " + (System.currentTimeMillis() - start) + " ms ===<");
        }
    }

    public static <E> E profile(String action, Callable<E> r) {
        long start = System.currentTimeMillis();
        try {
            try {
                return r.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } finally {
            L.d(">=== Executed " + action + " in " + (System.currentTimeMillis() - start) + " ms ===<");
        }
    }

}

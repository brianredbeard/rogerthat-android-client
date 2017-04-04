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

package com.mobicage.rogerthat.plugins.messaging.mfr;

import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Scriptable;

public class SandboxContextFactory extends ContextFactory {

    @SuppressWarnings("deprecation")
    private static class MyContext extends Context {
        long startTime;
    }

    static {
        // Initialize GlobalFactory with custom factory
        ContextFactory.initGlobal(new SandboxContextFactory());
    }

    @Override
    protected Context makeContext() {
        MyContext cx = new MyContext();
        cx.setWrapFactory(new SandboxWrapFactory());
        cx.setClassShutter(new SandboxClassShutter());
        cx.setLanguageVersion(Context.VERSION_1_2);
        cx.setOptimizationLevel(-1);
        // Make Rhino runtime to call observeInstructionCount
        // each 10000 bytecode instructions
        cx.setInstructionObserverThreshold(10000);
        return cx;
    }

    @Override
    public boolean hasFeature(Context cx, int featureIndex) {
        // Turn on maximum compatibility with MSIE scripts
        switch (featureIndex) {
        case Context.FEATURE_NON_ECMA_GET_YEAR:
            return true;

        case Context.FEATURE_MEMBER_EXPR_AS_FUNCTION_NAME:
            return true;

        case Context.FEATURE_RESERVED_KEYWORD_AS_IDENTIFIER:
            return true;

        case Context.FEATURE_PARENT_PROTO_PROPERTIES:
            return false;
        }
        return super.hasFeature(cx, featureIndex);
    }

    @Override
    protected void observeInstructionCount(Context cx, int instructionCount) {
        MyContext mcx = (MyContext) cx;
        long currentTime = System.currentTimeMillis();
        if (currentTime - mcx.startTime > 5 * 1000) {
            // More then 5 seconds from Context creation time:
            // it is time to stop the script.
            MyContext.reportError("Execution time exceeded");
            throw new java.lang.ThreadDeath();
        }
    }

    @Override
    protected Object doTopCall(Callable callable, Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
        MyContext mcx = (MyContext) cx;
        mcx.startTime = System.currentTimeMillis();
        return super.doTopCall(callable, cx, scope, thisObj, args);
    }
}

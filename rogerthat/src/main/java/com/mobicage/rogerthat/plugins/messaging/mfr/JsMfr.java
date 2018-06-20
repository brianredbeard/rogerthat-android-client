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

package com.mobicage.rogerthat.plugins.messaging.mfr;

import android.content.Intent;
import android.support.annotation.NonNull;

import com.eclipsesource.v8.V8;
import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Function;
import com.eclipsesource.v8.V8Object;
import com.eclipsesource.v8.V8ScriptExecutionException;
import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.MainService;
import com.mobicage.rogerthat.MyIdentity;
import com.mobicage.rogerthat.plugins.friends.Friend;
import com.mobicage.rogerthat.plugins.friends.FriendsPlugin;
import com.mobicage.rogerthat.plugins.messaging.MessagingPlugin;
import com.mobicage.rogerthat.util.TextUtils;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.CompletionHandler;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rpc.CallReceiver;
import com.mobicage.rpc.ResponseHandler;
import com.mobicage.rpc.Rpc;
import com.mobicage.rpc.RpcCall;
import com.mobicage.to.messaging.jsmfr.MessageFlowErrorRequestTO;
import com.mobicage.to.messaging.jsmfr.MessageFlowErrorResponseTO;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unchecked")
public class JsMfr {

    private MainService mService;
    private static V8 sV8Runtime;  // Must only be accessed from BIZZ thread
    private static JSConsole sJSConsole;  // Must only be accessed from BIZZ thread
    private static long sV8LastUsedTime;
    private static int FIVE_MINUTES = 300000;

    @SuppressWarnings("serial")
    private static class JsMfrError extends Exception {
        public String name;
        public String message;
        public String stack;
        public String jsCommand;

        public JsMfrError(String name, String message, String stack) {
            this.name = name;
            this.message = message;
            this.stack = stack;
        }
    }

    private static class JsMfrWebviewTag {
        public String parentMessageKey;
        public String context;
        public String jsCommand;

        public JsMfrWebviewTag(String parentMessageKey, String context, String jsCommand) {
            this.parentMessageKey = parentMessageKey;
            this.context = context;
            this.jsCommand = jsCommand;
        }
    }

    public static void executeMfr(final MessageFlowRun mfr, final Map<String, Object> userInput,
                                  final MainService mainService, final boolean throwIfNotReady) throws EmptyStaticFlowException {
        final JsMfr jsMfr = new JsMfr();
        jsMfr.mService = mainService;

        final FriendsPlugin friendsPlugin = jsMfr.mService.getPlugin(FriendsPlugin.class);
        final String htmlString;
        try {
            htmlString = friendsPlugin.getStore().getStaticFlow(mfr.staticFlowHash);
        } catch (IOException e) {
            L.bug("Failed to unzip static flow " + mfr.staticFlowHash, e);
            return;
        }
        if (TextUtils.isEmptyOrWhitespace(htmlString)) {
            if (throwIfNotReady) {
                throw new EmptyStaticFlowException(mainService.getString(R.string.this_screen_is_not_yet_downloaded_check_network));
            }
            return;
        }

        mainService.postOnBIZZHandler(new SafeRunnable() {
            @Override
            protected void safeRun() {
                Map<String, Object> request = (Map<String, Object>) userInput.get("request");

                String serviceEmail = (String) request.get("service");
                if (TextUtils.isEmptyOrWhitespace(serviceEmail)) {
                    serviceEmail = (String) request.get("email");
                }
                String context = (String) request.get("context");

                final JSONObject state;
                if (mfr.state == null) {
                    state = new JSONObject();
                } else {
                    state = (JSONObject) JSONValue.parse(mfr.state);
                }

                MyIdentity myIdentity = mainService.getIdentityStore().getIdentity();
                if (!state.containsKey("member")) {
                    state.put("member", myIdentity.getEmail());
                }

                Friend serviceFriend = null;
                if (serviceEmail != null && !state.containsKey("user")) {
                    serviceFriend = friendsPlugin.getStore().getExistingFriend(serviceEmail);
                }

                if (serviceFriend != null) {
                    Map<String, Object> info = friendsPlugin.getRogerthatUserAndServiceInfo(serviceEmail, serviceFriend);
                    state.put("user", info.get("user"));
                    state.put("service", info.get("service"));
                    state.put("system", info.get("system"));
                } else if (!state.containsKey("user")) {
                    Map<String, Object> empty = new HashMap();
                    state.put("user", empty);
                    state.put("service", empty);
                    state.put("system", empty);
                }

                Map<String, Object> system = (Map<String, Object>) state.get("system");
                if (system != null) {
                    system.put("internet", getInternetInfoMap(mainService));
                }

                final JSONObject ui = (JSONObject) JSONValue.parse(JSONValue.toJSONString(userInput));

                final String appVersion = MainService.getVersion(mainService);

                L.d("Executing JSMFR command");

                String JS_PATTERN = "(?s)<script language=\"javascript\" type=\"text/javascript\">\\s(.*)</script>";
                Pattern jsPattern = Pattern.compile(JS_PATTERN);
                Matcher matcher = jsPattern.matcher(htmlString);
                List<String> jsCode = new ArrayList();

                while (matcher.find()) {
                    jsCode.add(matcher.group(1));
                }

                String command = jsCode.get(0);
                long startTime = System.currentTimeMillis();
                sV8LastUsedTime = startTime;
                if (sV8Runtime == null) {
                    sV8Runtime = createV8Runtime();
                    L.d(String.format("Creating v8 runtime took %sms", System.currentTimeMillis() - startTime));
                }
                long createTime = System.currentTimeMillis();
                try {
                    V8Object script = (V8Object) sV8Runtime.executeScript(command);
                    L.d(String.format("Script execution took %sms", System.currentTimeMillis() - createTime));
                    long functionStartTime = System.currentTimeMillis();
                    String jsonState = state.toJSONString();
                    String jsonUi = ui.toJSONString();
                    long scriptRunTimeStart = System.currentTimeMillis();
                    L.d(String.format("JSON formatting took %sms", scriptRunTimeStart - functionStartTime));

                    V8Function transitionFunction = (V8Function) sV8Runtime.getObject("transition");
                    V8Array parameters = new V8Array(sV8Runtime);
                    parameters.push(transitionFunction);
                    parameters.push(appVersion);
                    parameters.push(jsonState);
                    parameters.push(jsonUi);
                    parameters.push(mainService.getAdjustedTimeDiff());
                    String rs;
                    try {
                        rs = sV8Runtime.executeStringFunction("mc_run_ext", parameters);
                    } finally {
                        transitionFunction.release();
                        parameters.release();
                        script.release();
                    }
                    L.d(String.format("Executing function took %sms", System.currentTimeMillis() - scriptRunTimeStart));

                    if (rs != null) {
                        long startParsingTime = System.currentTimeMillis();
                        try {
                            jsMfr.processResult((JSONObject) JSONValue.parse(rs));
                        } catch (Exception e) {
                            String stack = String.format("%s: %s\n%s", e.getClass(), e.getMessage(), L.getStackTraceString(e));
                            final String jsCommand = String.format("mc_run_ext(transition, \"%s\", %s, %s, %s)", appVersion,
                                    state.toJSONString(), JSONValue.toJSONString(userInput), mainService.getAdjustedTimeDiff());
                            final JsMfrWebviewTag tag = new JsMfrWebviewTag(mfr.parentKey, context, jsCommand);
                            jsMfr.handleException(new JsMfrError("JSONDecodeError", "Can not decode JSON:\n" + rs, stack), tag);
                        }
                        L.d(String.format("Processing JSMFR result took %sms", System.currentTimeMillis() - startParsingTime));
                    } else {
                        L.d("FlowCodeResult: NULL");
                    }

                } catch (V8ScriptExecutionException e) {
                    String sourceLine = e.getSourceLine();
                    // Avoid logging the entire file in case of minified js
                    if (sourceLine.length() > 200) {
                        int endColumn = Math.min(sourceLine.length(), e.getEndColumn() + 100);
                        sourceLine = sourceLine.substring(e.getStartColumn(), endColumn);
                    }
                    L.bug("Error while executing JSMFR\nStack: %s\nSource: %s", e.getJSStackTrace(), sourceLine);
                } catch (Exception e) {
                    L.bug("JsMfr Exception", e);
                } finally {
                    mainService.postDelayedOnBIZZHandler(new SafeRunnable() {
                        @Override
                        protected void safeRun() {
                            releaseV8Runtime();
                        }
                    }, FIVE_MINUTES);
                    L.d(String.format("JSMFR command took %sms in total", System.currentTimeMillis() - startTime));
                }
            }
        });
    }


    private static void releaseV8Runtime() {
        long fiveMinutesAgo = System.currentTimeMillis() - FIVE_MINUTES;
        if (sV8LastUsedTime <= fiveMinutesAgo) {
            L.d("Releasing V8 runtime");
            sJSConsole.release();
            sV8Runtime.release();
            sJSConsole = null;
            sV8Runtime = null;
        }
    }

    private void processResult(final JSONObject result) throws Exception {
        L.d("Processing JSMFR result");
        if ((Boolean) result.get("success")) {
            final Map<String, Object> realResult = (Map<String, Object>) result.get("result");
            final Map<String, Object> newState = (Map<String, Object>) realResult.get("newstate");
            final JSONArray serverActions = (JSONArray) realResult.get("server_actions");
            final JSONArray localActions = (JSONArray) realResult.get("local_actions");

            final MessageFlowRun mfr = new MessageFlowRun();
            mfr.parentKey = (String) ((Map<String, Object>) newState.get("run")).get("parent_message_key");
            mfr.state = JSONObject.toJSONString(newState);
            mfr.staticFlowHash = (String) newState.get("static_flow_hash");

            mService.postAtFrontOfBIZZHandler(new SafeRunnable() {
                @Override
                protected void safeRun() throws Exception {
                    MessagingPlugin plugin = mService.getPlugin(MessagingPlugin.class);
                    plugin.getStore().saveMessageFlowRun(mfr);
                    for (RpcCall rpcCall : createRpcCallsFromActions(localActions)) {
                        CallReceiver.processCall(rpcCall);
                    }
                }
            });

            mService.postOnBIZZHandler(new SafeRunnable() {
                @Override
                protected void safeRun() {
                    for (RpcCall rpcCall : createRpcCallsFromActions(serverActions)) {
                        Rpc.call(rpcCall.function, rpcCall.arguments, new ResponseHandler());
                    }
                }
            });

        } else {
            String errMessage = (String) result.get("errmessage");
            String errName = (String) result.get("errname");
            String errStack = (String) result.get("errstack");

            throw new JsMfrError(errName, errMessage, errStack);
        }
    }

    public static void executeFormResultValidation(final String serviceEmail, final String parentKey,
                                                   final String javascriptValidationCode, final Map<String, Object> formResult, final MainService mainService,
                                                   @NonNull final CompletionHandler<String> completionHandler) {
        T.BIZZ();
        final JsMfr jsMfr = new JsMfr();
        jsMfr.mService = mainService;
        FriendsPlugin friendsPlugin = jsMfr.mService.getPlugin(FriendsPlugin.class);

        final JSONObject rt = new JSONObject();
        Friend serviceFriend = friendsPlugin.getStore().getExistingFriend(serviceEmail);

        if (serviceFriend != null) {
            Map<String, Object> info = friendsPlugin.getRogerthatUserAndServiceInfo(serviceEmail, serviceFriend);
            rt.put("user", info.get("user"));
            rt.put("service", info.get("service"));

            final Map<String, Object> system = (Map<String, Object>) info.get("system");
            system.put("internet", getInternetInfoMap(mainService));
            rt.put("system", system);
        } else {
            rt.put("user", null);
            rt.put("service", null);
            rt.put("system", null);
        }

        sV8LastUsedTime = System.currentTimeMillis();
        if (sV8Runtime == null) {
            sV8Runtime = createV8Runtime();
        }
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(mainService.getAssets().open("mfr/validation.js")));
            StringBuilder sb = new StringBuilder();
            try {
                String line = br.readLine();

                while (line != null) {
                    sb.append(line);
                    sb.append("\n");
                    line = br.readLine();
                }
            } finally {
                br.close();
            }
            V8Object script = (V8Object) sV8Runtime.executeScript(sb.toString());
            V8Function validateFunction = (V8Function) sV8Runtime.getObject("validation");
            V8Array parameters = new V8Array(sV8Runtime);
            parameters.push(validateFunction);
            parameters.push(rt.toJSONString());
            parameters.push(JSONValue.toJSONString(formResult));
            parameters.push(javascriptValidationCode);
            parameters.push(mainService.getAdjustedTimeDiff());
            String rs;
            try {
                // TODO this might be faster when not json stringfying the result and converting V8Object to JSONObject here instead
                rs = sV8Runtime.executeStringFunction("mc_run_ext", parameters);
            } finally {
                validateFunction.release();
                parameters.release();
                script.release();
            }

            if (rs != null) {
                try {
                    String validationResult = jsMfr.processJavascriptFormResultValidationResult((JSONObject) JSONValue.parse(rs));
                    completionHandler.run(validationResult);
                } catch (Exception e) {
                    final String jsCommand = String.format("mc_run_ext(validation, %s, %s, %s)", rt.toJSONString(),
                            JSONValue.toJSONString(formResult), javascriptValidationCode);
                    final JsMfrWebviewTag tag = new JsMfrWebviewTag(parentKey, null, jsCommand);
                    jsMfr.handleException(e, tag);
                    completionHandler.run(null);
                }
                return;
            } else {
                L.d("JavascriptFormResultValidationResult: NULL");
            }

        } catch (V8ScriptExecutionException e) {
            String sourceLine = e.getSourceLine();
            // Avoid logging the entire file in case of minified js
            if (sourceLine.length() > 200) {
                int endColumn = Math.min(sourceLine.length(), e.getEndColumn() + 100);
                sourceLine = sourceLine.substring(e.getStartColumn(), endColumn);
            }
            L.bug("Error while executing JS validation\nStack: %s\nSource: %s", e.getJSStackTrace(), sourceLine);
        } catch (Exception e) {
            L.bug("JavascriptFormResultValidation Exception", e);
        } finally {
            mainService.postDelayedOnBIZZHandler(new SafeRunnable() {
                @Override
                protected void safeRun() {
                    releaseV8Runtime();
                }
            }, FIVE_MINUTES);
        }
        completionHandler.run(null);
    }

    private static V8 createV8Runtime() {
        V8 runtime = V8.createV8Runtime();
        // Add console callbacks
        JSConsole console = new JSConsole(runtime, "[FLOW] ");
        sJSConsole = console;
        V8Object v8Console = new V8Object(runtime);
        runtime.add("console", v8Console);
        v8Console.registerJavaMethod(console, "debug", "debug", new Class<?>[]{Object[].class});
        v8Console.registerJavaMethod(console, "log", "info", new Class<?>[]{Object[].class});
        v8Console.registerJavaMethod(console, "log", "log", new Class<?>[]{Object[].class});
        v8Console.registerJavaMethod(console, "warn", "warn", new Class<?>[]{Object[].class});
        v8Console.registerJavaMethod(console, "error", "error", new Class<?>[]{Object[].class});
        v8Console.release();
        return runtime;
    }

    @NonNull
    private static Map<String, Object> getInternetInfoMap(MainService mainService) {
        final boolean isWifiConnected = mainService.getNetworkConnectivityManager().isWifiConnected();
        final boolean isConnected = isWifiConnected || mainService.getNetworkConnectivityManager()
                .isConnected();
        final Map<String, Object> internet = new HashMap<>();
        internet.put("connected", isConnected);
        internet.put("wifi", isWifiConnected);
        return internet;
    }

    private String processJavascriptFormResultValidationResult(final JSONObject result) throws Exception {
        L.d("Got JS Validation result");

        if ((Boolean) result.get("success")) {
            final Map<String, Object> realResult = (Map<String, Object>) result.get("result");
            final JSONArray serverActions = (JSONArray) realResult.get("server_actions");
            final Object returnValue = realResult.get("return_value");

            mService.postOnBIZZHandler(new SafeRunnable() {
                @Override
                protected void safeRun() {
                    for (RpcCall rpcCall : createRpcCallsFromActions(serverActions)) {
                        Rpc.call(rpcCall.function, rpcCall.arguments, new ResponseHandler());
                    }
                }
            });

            if (returnValue != null && returnValue instanceof String) {
                return (String) returnValue;
            }

        } else {
            String errMessage = (String) result.get("errmessage");
            String errName = (String) result.get("errname");
            String errStack = (String) result.get("errstack");

            throw new JsMfrError(errName, errMessage, errStack);
        }
        return null;
    }

    private void handleException(Exception e, final JsMfrWebviewTag tag) {

        if (e instanceof JsMfrError) {
            final JsMfrError e2 = (JsMfrError) e;
            e2.jsCommand = tag.jsCommand;
            logJsMfrError(e2);
        } else {
            L.bug(e);
        }

        Intent intent = new Intent(MessagingPlugin.MESSAGE_JSMFR_ERROR);
        intent.putExtra("parent_message_key", tag.parentMessageKey);
        intent.putExtra("context", tag.context);
        mService.sendBroadcast(intent);
    }

    private List<RpcCall> createRpcCallsFromActions(JSONArray actions) {
        List<RpcCall> rpcCalls = new ArrayList<RpcCall>(actions.size());
        for (int i = 0; i < actions.size(); i++) {
            rpcCalls.add(RpcCall.parse((Map<String, Object>) actions.get(i)));
        }
        return rpcCalls;
    }

    private void logJsMfrError(final JsMfrError error) {
        mService.postOnUIHandler(new SafeRunnable() {
            @Override
            protected void safeRun() throws Exception {
                T.UI();

                MessageFlowErrorRequestTO request = new MessageFlowErrorRequestTO();
                request.description = error.name;
                request.errorMessage = error.message;
                request.jsCommand = error.jsCommand;
                request.mobicageVersion = (mService.isDebug() ? "-" : "") + mService.getMajorVersion() + "."
                    + mService.getMinorVersion();
                request.platform = 1;
                request.stackTrace = error.stack;
                request.timestamp = mService.currentTimeMillis() / 1000;

                ResponseHandler<MessageFlowErrorResponseTO> rh = new ResponseHandler<MessageFlowErrorResponseTO>();
                try {
                    com.mobicage.api.messaging.jsmfr.Rpc.messageFlowError(rh, request);
                } catch (Exception e) {
                    L.bug(e);
                }
            }
        });
    }

}

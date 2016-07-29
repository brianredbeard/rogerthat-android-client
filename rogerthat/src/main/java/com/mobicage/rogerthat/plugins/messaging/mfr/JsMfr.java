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

package com.mobicage.rogerthat.plugins.messaging.mfr;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.mozilla.javascript.Callable;
import org.mozilla.javascript.ConsString;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.UniqueTag;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.MainService;
import com.mobicage.rogerthat.MyIdentity;
import com.mobicage.rogerthat.plugins.friends.Friend;
import com.mobicage.rogerthat.plugins.friends.FriendsPlugin;
import com.mobicage.rogerthat.plugins.messaging.MessagingPlugin;
import com.mobicage.rogerthat.util.TextUtils;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rpc.CallReceiver;
import com.mobicage.rpc.ResponseHandler;
import com.mobicage.rpc.Rpc;
import com.mobicage.rpc.RpcCall;
import com.mobicage.to.messaging.jsmfr.MessageFlowErrorRequestTO;
import com.mobicage.to.messaging.jsmfr.MessageFlowErrorResponseTO;

@SuppressWarnings("unchecked")
public class JsMfr {

    private MainService mService;

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

    public static Scriptable toScriptable(Context context, Scriptable scope, Map<String, Object> object) {
        Scriptable result = context.newObject(scope);
        for (String key : object.keySet() ) {
            Object value = object.get(key);
            if (value == null || value instanceof String || value instanceof Number || value instanceof Boolean) {
                ScriptableObject.putProperty(result, key, value);
            } else if (value instanceof List) {
                ScriptableObject.putProperty(result, key, toScriptable(context, scope, (List)value));
            } else if (value instanceof Map) {
                ScriptableObject.putProperty(result, key, toScriptable(context, scope, (Map)value));
            } else
                throw new NoSuchElementException();
        }
        return result;
    }

    public static Scriptable toScriptable(Context context, Scriptable scope, List object) {
        Scriptable result = context.newArray(scope, object.size());
        int counter = 0;
        for (Object value : (List)object) {
            if (value == null || value instanceof String || value instanceof Number || value instanceof Boolean) {
                ScriptableObject.putProperty(result, counter++, value);
            } else if (value instanceof List) {
                ScriptableObject.putProperty(result, counter++, toScriptable(context, scope, (List) value));
            } else if (value instanceof Map) {
                ScriptableObject.putProperty(result, counter++, toScriptable(context, scope, (Map) value));
            } else
                throw new NoSuchElementException();
        }
        return result;
    }

    public static JSONObject serializeNativeObject(NativeObject object){
        JSONObject result = new JSONObject();
        Object[] propIds = NativeObject.getPropertyIds(object);
        for(Object propId: propIds) {
            String key = propId.toString();
            Object value = NativeObject.getProperty(object, key);
            if (value instanceof Callable || value instanceof UniqueTag) {
            } else if (value instanceof NativeArray) {
                result.put(key, JsMfr.serializeNativeArray((NativeArray) value));
            } else if (value instanceof NativeObject) {
                result.put(key, JsMfr.serializeNativeObject((NativeObject) value));
            } else if (value instanceof Double) {
                long longValue = ((Double) value).longValue();
                if (value.equals(new Long(longValue).doubleValue()))
                    result.put(key, longValue);
                else
                    result.put(key, value);
            } else if (value instanceof Float) {
                long longValue = ((Float) value).longValue();
                if (value.equals(new Long(longValue).floatValue()))
                    result.put(key, longValue);
                else
                    result.put(key, value);
            } else if (value == null || value instanceof String || value instanceof Number || value instanceof  Boolean) {
                result.put(key, value);
            } else if (value instanceof ConsString) {
                result.put(key, value.toString());
            } else
                L.d(value.getClass().getName() + ": "+ value.toString());
        }
        return result;
    }

    public static JSONArray serializeNativeArray(NativeArray object){
        JSONArray result = new JSONArray();
        for(Object value: object) {
            if (value instanceof NativeArray) {
                result.add(JsMfr.serializeNativeArray((NativeArray) value));
            } else if (value instanceof NativeObject) {
                result.add(JsMfr.serializeNativeObject((NativeObject) value));
            } else if (value instanceof Double) {
                long longValue = ((Double) value).longValue();
                if (value.equals(new Long(longValue).doubleValue()))
                    result.add(longValue);
                else
                    result.add(value);
            } else if (value instanceof Float) {
                long longValue = ((Float) value).longValue();
                if (value.equals(new Long(longValue).floatValue()))
                    result.add(longValue);
                else
                    result.add(value);
            } else if (value == null || value instanceof String || value instanceof Number || value instanceof  Boolean) {
                result.add(value);
            } else if (value instanceof ConsString) {
                result.add(value.toString());
            } else
                L.d(value.getClass().getName() + ": "+ value.toString());
        }
        return result;
    }

    @SuppressLint("SetJavaScriptEnabled")
    public static void executeMfr(final MessageFlowRun mfr, final Map<String, Object> userInput,
        final MainService mainService, final boolean throwIfNotReady) throws EmptyStaticFlowException {
        T.UI();

        final JsMfr jsMfr = new JsMfr();
        jsMfr.mService = mainService;

        FriendsPlugin friendsPlugin = jsMfr.mService.getPlugin(FriendsPlugin.class);
        final String htmlString;
        try {
            htmlString = friendsPlugin.getStore().getStaticFlow(mfr.staticFlowHash);
        } catch (IOException e) {
            L.bug("Failed to unzip static flow " + mfr.staticFlowHash, e);
            return;
        }
        if (throwIfNotReady && TextUtils.isEmptyOrWhitespace(htmlString)) {
            throw new EmptyStaticFlowException(
                mainService.getString(R.string.this_screen_is_not_yet_downloaded_check_network));
        }
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

            final Map<String, Object> system = (Map<String, Object>) info.get("system");
            system.put("internet", getInternetInfoMap(mainService));
            state.put("system", system);
        } else if (!state.containsKey("user")) {
            Map<String, Object> empty = new HashMap<String, Object>();
            state.put("user", empty);
            state.put("service", empty);
            state.put("system", empty);
        }

        final JSONObject ui = (JSONObject) JSONValue.parse(JSONValue.toJSONString(userInput));

        final String appVersion = MainService.getVersion(mainService);
        final String jsCommand = String.format("mc_run_ext(transition, \"%s\", %s, %s)", appVersion,
            state.toJSONString(), JSONValue.toJSONString(userInput));

        L.d("Executing JSMFR command");

        final JsMfrWebviewTag tag = new JsMfrWebviewTag(mfr.parentKey, context, jsCommand);

        mainService.postOnBIZZHandler(new SafeRunnable() {
            @Override
            protected void safeRun() {
                SandboxContextFactory contextFactory = new SandboxContextFactory();
                Context cx = contextFactory.makeContext();
                contextFactory.enterContext(cx);
                ScriptableObject prototype = cx.initStandardObjects();
                prototype.setParentScope(null);
                Scriptable scope = cx.newObject(prototype);
                scope.setPrototype(prototype);
                try {
                    String JS_PATTERN = "(?s)<script language=\"javascript\" type=\"text/javascript\">\\s(.*)</script>";
                    Pattern jsPattern = Pattern.compile(JS_PATTERN);
                    Matcher matcher = jsPattern.matcher(htmlString);
                    List<String> jsCode = new ArrayList<String>();

                    while (matcher.find()) {
                        jsCode.add(matcher.group(1));
                    }

                    String command = jsCode.get(0);
                    cx.evaluateString(scope, command, null, -1, null);
                    L.d("FlowCodeEvalSucces");

                    Object rs = null;
                    Object runTag = scope.get("mc_run_ext", scope);
                    Object transitionTag = scope.get("transition", scope);

                    L.d("version: " + appVersion);

                    if (runTag != UniqueTag.NOT_FOUND) {
                        Function runFct = (Function) runTag;
                        Function transitionFct = (Function) transitionTag;

                        final Object scriptableState;
                        final Object scriptableUI;
                        if (command.contains("returnScriptable")) {
                            L.d("converting state to scriptable");
                            scriptableState = toScriptable(cx, scope, state);
                            L.d("converting ui to scriptable");
                            scriptableUI = toScriptable(cx, scope, ui);
                        } else {
                            L.d("converting state to json");
                            scriptableState = state.toJSONString();
                            L.d("converting ui to json");
                            scriptableUI = ui.toJSONString();
                        }
                        L.d("calling doTopCall");
                        ScriptableObject.putProperty(scope, "returnScriptable", true);
                        rs = contextFactory.doTopCall(runFct, cx, scope, scope, new Object[] { transitionFct,
                            appVersion, scriptableState, scriptableUI});

                        L.d("FlowCodeResult");
                    } else {
                        // Should not be possible
                        L.d("Function not found");
                    }

                    if (rs != null) {
                        if (rs instanceof String) {
                            try {
                                jsMfr.processResult((JSONObject) JSONValue.parse((String)rs), tag);
                            } catch (Exception e) {
                                jsMfr.handleException(new JsMfrError("JSONDecodeError", "Can not decode JSON:\n" + (String)rs, e.getClass() + ": "
                                        + e.getMessage() + "\n" + L.getStackTraceString(e)), tag);
                            }
                        } else {
                            jsMfr.processResult(serializeNativeObject((NativeObject)rs), tag);
                        }
                    } else {
                        L.d("FlowCodeResult: NULL");
                    }

                } catch (Exception e) {
                    L.bug("JsMfr Exception", e);
                } finally {
                    Context.exit();
                }
            }
        });

    }

    void processResult(final JSONObject result, final JsMfrWebviewTag tag) {
        try {
            L.d("Got JSMFR result");

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
                    protected void safeRun() throws Exception {
                        for (RpcCall rpcCall : createRpcCallsFromActions(serverActions)) {
                            Rpc.call(rpcCall.function, rpcCall.arguments, new ResponseHandler<Object>());
                        }
                    }
                });

            } else {
                String errMessage = (String) result.get("errmessage");
                String errName = (String) result.get("errname");
                String errStack = (String) result.get("errstack");

                throw new JsMfrError(errName, errMessage, errStack);
            }

        } catch (Exception e) {
            handleException(e, tag);
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    public static String executeFormResultValidation(final String serviceEmail, final String parentKey,
        final String javascriptValidationCode, final Map<String, Object> formResult, final MainService mainService) {
        T.UI();

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

        final String jsCommand = String.format("mc_run_ext(validation, %s, %s, %s)", rt.toJSONString(),
            JSONValue.toJSONString(formResult), javascriptValidationCode);

        L.d("Executing JS Validation command");

        final JsMfrWebviewTag tag = new JsMfrWebviewTag(parentKey, null, jsCommand);

        SandboxContextFactory contextFactory = new SandboxContextFactory();
        Context cx = contextFactory.makeContext();
        contextFactory.enterContext(cx);
        ScriptableObject prototype = cx.initStandardObjects();
        prototype.setParentScope(null);
        Scriptable scope = cx.newObject(prototype);
        scope.setPrototype(prototype);
        try {
            cx.evaluateReader(scope, new InputStreamReader(mainService.getAssets().open("mfr/validation.js")), null,
                -1, null);

            L.d("JavascriptFormResultValidationEvalSucces");

            Object rs = null;
            Object runTag = scope.get("mc_run_ext", scope);
            Object validationTag = scope.get("validation", scope);

            if (runTag != UniqueTag.NOT_FOUND && validationTag != UniqueTag.NOT_FOUND) {
                Function runFct = (Function) runTag;
                Function validationFct = (Function) validationTag;

                L.d("converting rt to scriptable");
                final Scriptable scriptableRT = toScriptable(cx, scope, rt);
                L.d("converting result to scriptable");
                final Scriptable scriptableResult = toScriptable(cx, scope, formResult);
                L.d("calling doTopCall");
                rs = contextFactory.doTopCall(runFct, cx, scope, scope, new Object[] { validationFct,
                        scriptableRT, scriptableResult, javascriptValidationCode });
                L.d("JavascriptFormResultValidationResult");
            } else {
                // Should not be possible
                L.d("Function not found");
            }

            if (rs != null) {
                return jsMfr.processJavascriptFormResultValidationResult(serializeNativeObject((NativeObject)rs), tag);
            } else {
                L.d("JavascriptFormResultValidationResult: NULL");
            }

        } catch (Exception e) {
            L.bug("JavascriptFormResultValidation Exception", e);
        } finally {
            Context.exit();
        }

        return null;
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

    String processJavascriptFormResultValidationResult(final JSONObject result, final JsMfrWebviewTag tag) {
        try {
            L.d("Got JS Validation result");

            if ((Boolean) result.get("success")) {
                final Map<String, Object> realResult = (Map<String, Object>) result.get("result");
                final JSONArray serverActions = (JSONArray) realResult.get("server_actions");
                final Object returnValue = realResult.get("return_value");

                mService.postOnBIZZHandler(new SafeRunnable() {
                    @Override
                    protected void safeRun() throws Exception {
                        for (RpcCall rpcCall : createRpcCallsFromActions(serverActions)) {
                            Rpc.call(rpcCall.function, rpcCall.arguments, new ResponseHandler<Object>());
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

        } catch (JsMfrError e) {
            handleException(e, tag);
        } catch (Exception e) {
            handleException(e, tag);
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

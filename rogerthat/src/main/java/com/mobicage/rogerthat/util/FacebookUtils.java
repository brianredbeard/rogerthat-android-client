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

package com.mobicage.rogerthat.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;

import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.mobicage.rogerthat.MainService;
import com.mobicage.rogerthat.MyIdentity;
import com.mobicage.rogerthat.ServiceBoundActivity;
import com.mobicage.rogerthat.plugins.friends.FriendsPlugin;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeAsyncTask;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rpc.config.AppConstants;

public final class FacebookUtils {

    public static final List<String> PUBLISH_PERMISSIONS = Arrays.asList("publish_actions");
    private static boolean sProfileUpdated = false;

    public enum PermissionType {
        READ, PUBLISH
    }

    public static void ensureOpenSession(final ServiceBoundActivity activity, final List<String> permissions, final
    PermissionType permissionType, final FacebookCallback<LoginResult> fbCallback, final boolean isRegistered) {
        final AccessToken currentAccessToken = AccessToken.getCurrentAccessToken();
        if (currentAccessToken != null && currentAccessToken.getPermissions().containsAll(permissions) &&
                !currentAccessToken.isExpired()) {
            L.d("Facebook session is already open with all required permissions");
            fbCallback.onSuccess(new LoginResult(currentAccessToken, new HashSet<>(permissions), null));
            if (isRegistered && shouldUpdateProfile(activity.getMainService())) {
                updateProfile(activity.getMainService(), currentAccessToken);
            }
            return;
        }

        final LoginManager fbLoginMgr = LoginManager.getInstance();
        fbLoginMgr.registerCallback(activity.getFacebookCallbackManager(), new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                fbCallback.onSuccess(loginResult);
                if (isRegistered && shouldUpdateProfile(activity.getMainService())) {
                    updateProfile(activity.getMainService(), currentAccessToken);
                }
            }

            @Override
            public void onCancel() {
                fbCallback.onCancel();
            }

            @Override
            public void onError(FacebookException error) {
                fbCallback.onError(error);
            }
        });

        switch (permissionType) {
            case READ:
                fbLoginMgr.logInWithReadPermissions(activity, permissions);
                break;
            case PUBLISH:
                fbLoginMgr.logInWithPublishPermissions(activity, permissions);
                break;
        }
    }

    private static boolean shouldUpdateProfile(final MainService mainService) {
        if (sProfileUpdated)
            return false;

        if (mainService == null || mainService.getIdentityStore() == null)
            return false; // saw this happening once

        MyIdentity identity = mainService.getIdentityStore().getIdentity();
        if (identity.getName().contains(" at ")) {
            return true;
        }

        if (AppConstants.PROFILE_SHOW_GENDER_AND_BIRTHDATE) {
            if (!identity.hasBirthdate())
                return true;
            if (!identity.hasGender())
                return true;
        }

        return false;
    }

    private static void updateProfile(final MainService mainService, final AccessToken fbAccessToken) {
        T.dontCare();
        GraphRequest request = GraphRequest.newMeRequest(fbAccessToken, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject jsonObject, GraphResponse response) {
                if (response.getError() != null) {
                    L.w("Could not update profile from FB: " + response.getError().getErrorMessage());
                    return;
                }

                if (jsonObject == null) {
                    L.w("No JSON object return for /me graph request.");
                    return;
                }

                try {
                    String newName = jsonObject.has("name") ? jsonObject.getString("name") : null;
                    L.d("FacebookUtils updateProfile: " + newName);
                    boolean hasBirthdate = false;
                    long birthdate = 0;
                    try {
                        String birthdateString = jsonObject.getString("birthday");
                        String[] birthday = birthdateString.split("/");
                        Calendar c = Calendar.getInstance();
                        c.set(Integer.parseInt(birthday[2]), Integer.parseInt(birthday[0]) - 1,
                                Integer.parseInt(birthday[1]));
                        Date d = c.getTime();
                        birthdate = d.getTime() / 1000;
                        hasBirthdate = true;
                    } catch (JSONException e) {
                        L.d("No birthday in jsonObject");
                    }
                    boolean hasGender = false;
                    long gender = MyIdentity.GENDER_UNDEFINED;
                    try {
                        String genderString = jsonObject.getString("gender");
                        if ("female".equalsIgnoreCase(genderString)) {
                            gender = MyIdentity.GENDER_FEMALE;
                        } else {
                            gender = MyIdentity.GENDER_MALE;
                        }
                        hasGender = true;
                    } catch (JSONException e) {
                        L.d("No gender in jsonObject");
                    }

                    final String pictureUrl = "https://graph.facebook.com/" + jsonObject.getString("id") + "/picture";
                    downloadAvatarAndUpdateProfile(pictureUrl, newName, mainService, fbAccessToken.getToken(),
                            birthdate, gender, hasBirthdate, hasGender);
                } catch (JSONException e) {
                    L.bug("JSONObject: " + jsonObject.toString(), e);
                }
            }
        });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,picture,name,gender,birthday");
        request.setParameters(parameters);
        request.executeAsync();
    }

    public static void downloadAvatarAndUpdateProfile(final String src, final String newName,
        final MainService mainService, final String accessToken, final long birthdate, final long gender,
        final boolean hasBirthdate, final boolean hasGender) {
        new SafeAsyncTask<Object, Object, Object>() {
            @Override
            protected Object safeDoInBackground(Object... params) {
                try {
                    return BitmapFactory.decodeStream((InputStream) new URL((String) params[0]).getContent());
                } catch (Exception e) {
                    L.bug("Could not download Facebook avatar: " + src, e);
                }
                return null;
            };

            @Override
            protected void safeOnPostExecute(Object result) {
                Bitmap bitmap = (Bitmap) result;
                if (bitmap != null) {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);

                    final FriendsPlugin friendsPlugin = mainService.getPlugin(FriendsPlugin.class);
                    friendsPlugin.updateProfile(newName, stream.toByteArray(), accessToken, birthdate, gender,
                        hasBirthdate, hasGender);
                    sProfileUpdated = true;
                }
            }

            @Override
            protected void safeOnCancelled(Object result) {
            }

            @Override
            protected void safeOnProgressUpdate(Object... values) {
            }

            @Override
            protected void safeOnPreExecute() {
            }
        }.execute(src);
    }
}

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
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import com.facebook.FacebookOperationCanceledException;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.Session.Builder;
import com.facebook.Session.NewPermissionsRequest;
import com.facebook.Session.OpenRequest;
import com.facebook.SessionState;
import com.facebook.model.GraphObject;
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

    public static enum PermissionType {
        READ, PUBLISH
    };

    public static void ensureOpenSession(final ServiceBoundActivity activity, final List<String> permissions,
        final PermissionType permissionType, final Session.StatusCallback statusCallback, final boolean isRegistered) {
        final Session activeSession = Session.getActiveSession();
        if (activeSession == null || !activeSession.isOpened()) {
            openNewActiveSession(activity, statusCallback, permissions, permissionType, isRegistered);
        } else {
            // Check if accessToken is not expired
            final MainService mainService = activity.getMainService();
            Bundle bundle = new Bundle();
            final String accessToken = activeSession.getAccessToken();
            bundle.putString("access_token", accessToken);
            Request request = new Request(activeSession, "/me", bundle, HttpMethod.GET, new Request.Callback() {
                @Override
                public void onCompleted(Response response) {
                    GraphObject graphObject = response.getGraphObject();
                    if (graphObject == null && response.getError().getRequestStatusCode() == 400) {
                        L.w("Access token expired");
                        openNewActiveSession(activity, statusCallback, permissions, permissionType, isRegistered);
                    } else {
                        // Check if active session contains required permissions
                        if (!requestPermissionsIfNeeded(activity, permissions, permissionType, activeSession,
                            statusCallback)) {
                            statusCallback.call(activeSession, activeSession.getState(), null);
                        }

                        if (isRegistered && graphObject != null) {
                            updateProfile(mainService, graphObject.getInnerJSONObject(), accessToken);
                        }
                    }
                }

            });
            request.executeAsync();
        }
    }

    private static void openNewActiveSession(final ServiceBoundActivity activity,
        final Session.StatusCallback statusCallback, final List<String> permissions,
        final PermissionType permissionType, final boolean isRegistered) {

        final MainService mainService = activity.getMainService();

        Session session = new Builder(activity).build();
        Session.setActiveSession(session);

        List<String> readPermissions;
        if (permissionType == PermissionType.PUBLISH) {
            // Must first request basic read permission, and later request additional publish permissions
            readPermissions = Arrays.asList(new String[] { "email" });
        } else {
            readPermissions = permissions;
        }
        session.openForRead(new OpenRequest(activity).setPermissions(readPermissions).setCallback(
            new Session.StatusCallback() {
                boolean alreadyAskedPermissions = false;

                @Override
                public void call(Session session, SessionState state, Exception exception) {
                    L.d("activeSession FB StatusCallback: \nSessionState = " + state + "\nException: " + exception,
                        exception);
                    if (session != Session.getActiveSession()) {
                        session.removeCallback(this);
                    } else if (exception != null) {
                        session.removeCallback(this);
                        if (!(exception instanceof FacebookOperationCanceledException)) {
                            L.e(exception);
                        }
                        statusCallback.call(session, state, exception);
                    } else if (session.isOpened()) {
                        if (!alreadyAskedPermissions
                            && requestPermissionsIfNeeded(activity, permissions, permissionType, session, this)) {
                        } else {
                            session.removeCallback(this);
                            statusCallback.call(session, state, exception);
                        }
                        alreadyAskedPermissions = true;

                        if (isRegistered && shouldUpdateProfile(mainService)) {
                            Bundle bundle = new Bundle();
                            final String accessToken = session.getAccessToken();
                            bundle.putString("access_token", accessToken);
                            Request request = new Request(session, "/me", bundle, HttpMethod.GET,
                                new Request.Callback() {
                                    @Override
                                    public void onCompleted(Response response) {
                                        GraphObject graphObject = response.getGraphObject();
                                        if (graphObject != null) {
                                            updateProfile(mainService, graphObject.getInnerJSONObject(), accessToken);
                                        }
                                    }
                                });
                            request.executeAsync();
                        }
                    }
                }
            }));
    }

    private static boolean requestPermissionsIfNeeded(final ServiceBoundActivity activity,
        final List<String> permissions, final PermissionType permissionType, Session session,
        Session.StatusCallback statusCallback) {
        // Check if we have all required permissions
        if (!session.getPermissions().containsAll(permissions)) {
            L.d("Requesting additional permissions: " + permissions);
            if (PermissionType.READ.equals(permissionType)) {
                session.requestNewReadPermissions(new NewPermissionsRequest(activity, permissions));
            } else if (PermissionType.PUBLISH.equals(permissionType)) {
                session.requestNewPublishPermissions(new NewPermissionsRequest(activity, permissions));
            } else {
                L.bug("Unexpected PermissionType: " + permissionType);
            }

            session.addCallback(statusCallback);
            return true;
        }
        return false;
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

    private static void updateProfile(final MainService mainService, JSONObject jsonObject, final String accessToken) {
        T.dontCare();
        if (shouldUpdateProfile(mainService)) {
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

                downloadAvatarAndUpdateProfile("https://graph.facebook.com/" + jsonObject.getString("id") + "/picture",
                    newName, mainService, accessToken, birthdate, gender, hasBirthdate, hasGender);
            } catch (JSONException e) {
                L.bug("JSONObject: " + jsonObject.toString(), e);
            }
        }
    }

    public static void downloadAvatarAndUpdateProfile(final String src, final String newName,
        final MainService mainService, final String accessToken, final long birthdate, final long gender,
        final boolean hasBirthdate, final boolean hasGender) {
        new SafeAsyncTask<Object, Object, Object>() {
            @Override
            protected Object safeDoInBackground(Object... params) {
                try {
                    return BitmapFactory.decodeStream((InputStream) new URL((String) params[0]).getContent());
                } catch (MalformedURLException e) {
                    L.bug("Could not download Facebook avatar: " + src, e);
                } catch (IOException e) {
                    L.bug("Could not download Facebook avatar: " + src, e);
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
            };
        }.execute(src);
    }
}
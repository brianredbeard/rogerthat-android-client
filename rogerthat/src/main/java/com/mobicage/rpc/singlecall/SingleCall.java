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

package com.mobicage.rpc.singlecall;

import org.json.simple.JSONValue;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class SingleCall {

    public final static String SINGLE_CALL_SERVCICES_START_ACTION = "com.mobicage.api.services.startAction";
    public final static String SINGLE_CALL_FRIENDS_GET_FRIEND = "com.mobicage.api.friends.getFriend";
    public final static String SINGLE_CALL_FRIENDS_GET_USERINFO = "com.mobicage.api.friends.getUserInfo";
    public final static String SINGLE_CALL_FRIENDS_GET_CATEGORY = "com.mobicage.api.friends.getCategory";
    public final static String SINGLE_CALL_FRIENDS_INVITE = "com.mobicage.api.friends.invite";
    public final static String SINGLE_CALL_MESSAGING_GET_CONVERSATION = "com.mobicage.api.messaging.getConversation";
    public final static String SINGLE_CALL_MESSAGING_GET_CONVERSATION_AVATAR = "com.mobicage.api.messaging.getConversationAvatar";
    public final static String SINGLE_CALL_ACTIVITY_LOG_LOCATIONS = "com.mobicage.api.activity.logLocations";

    public final static Set<String> SINGLE_CALL_FUNCTIONS = new HashSet<String>();
    public final static Set<String> SPECIAL_SINGLE_CALL_FUNCTIONS = new HashSet<String>();

    protected String function;
    protected Map<String, Object> request;

    static {
        SINGLE_CALL_FUNCTIONS.add("com.mobicage.api.friends.findRogerthatUsersViaEmail");
        SINGLE_CALL_FUNCTIONS.add("com.mobicage.api.friends.findRogerthatUsersViaFacebook");
        SINGLE_CALL_FUNCTIONS.add("com.mobicage.api.friends.getFriends");
        SINGLE_CALL_FUNCTIONS.add("com.mobicage.api.friends.getFriendInvitationSecrets");
        SINGLE_CALL_FUNCTIONS.add("com.mobicage.api.location.getFriendLocation");
        SINGLE_CALL_FUNCTIONS.add("com.mobicage.api.location.getFriendLocations");
        SINGLE_CALL_FUNCTIONS.add("com.mobicage.api.system.getIdentity");
        SINGLE_CALL_FUNCTIONS.add("com.mobicage.api.system.getIdentityQRCode");
        SINGLE_CALL_FUNCTIONS.add("com.mobicage.api.system.heartBeat");
        SINGLE_CALL_FUNCTIONS.add("com.mobicage.api.system.saveSettings");
        SINGLE_CALL_FUNCTIONS.add("com.mobicage.api.system.unregisterMobile");
        SINGLE_CALL_FUNCTIONS.add("com.mobicage.api.news.getNews");

        SPECIAL_SINGLE_CALL_FUNCTIONS.add(SINGLE_CALL_SERVCICES_START_ACTION);
        SPECIAL_SINGLE_CALL_FUNCTIONS.add(SINGLE_CALL_FRIENDS_GET_FRIEND);
        SPECIAL_SINGLE_CALL_FUNCTIONS.add(SINGLE_CALL_FRIENDS_GET_USERINFO);
        SPECIAL_SINGLE_CALL_FUNCTIONS.add(SINGLE_CALL_FRIENDS_GET_CATEGORY);
        SPECIAL_SINGLE_CALL_FUNCTIONS.add(SINGLE_CALL_FRIENDS_INVITE);
        SPECIAL_SINGLE_CALL_FUNCTIONS.add(SINGLE_CALL_MESSAGING_GET_CONVERSATION);
        SPECIAL_SINGLE_CALL_FUNCTIONS.add(SINGLE_CALL_MESSAGING_GET_CONVERSATION_AVATAR);
        SPECIAL_SINGLE_CALL_FUNCTIONS.add(SINGLE_CALL_ACTIVITY_LOG_LOCATIONS);
    }

    @SuppressWarnings("unchecked")
    protected SingleCall(String function, String callBody) {
        this.function = function;
        Map<String, Object> call = (Map<String, Object>) JSONValue.parse(callBody);
        Map<String, Object> args = (Map<String, Object>) call.get("a");
        this.request = (Map<String, Object>) args.get("request");
    }

    public abstract boolean isEqualToCallWithBody(String callBody);

    public static SingleCall singleCallForFunction(String function, String callBody) {
        if (SINGLE_CALL_SERVCICES_START_ACTION.equals(function)) {
            return new ServicesStartAction(function, callBody);
        }
        if (SINGLE_CALL_FRIENDS_GET_FRIEND.equals(function)) {
            return new FriendsGetFriend(function, callBody);
        }
        if (SINGLE_CALL_FRIENDS_GET_USERINFO.equals(function)) {
            return new FriendsGetUserInfo(function, callBody);
        }
        if (SINGLE_CALL_FRIENDS_GET_CATEGORY.equals(function)) {
            return new FriendsGetCategory(function, callBody);
        }
        if (SINGLE_CALL_FRIENDS_INVITE.equals(function)) {
            return new FriendsInvite(function, callBody);
        }
        if (SINGLE_CALL_MESSAGING_GET_CONVERSATION.equals(function)) {
            return new MessagingGetConversation(function, callBody);
        }
        if (SINGLE_CALL_MESSAGING_GET_CONVERSATION_AVATAR.equals(function)) {
            return new MessagingGetConversationAvatar(function, callBody);
        }
        if (SINGLE_CALL_ACTIVITY_LOG_LOCATIONS.equals(function)) {
            return new ActivityLogLocations(function, callBody);
        }
        return null;
    }
}

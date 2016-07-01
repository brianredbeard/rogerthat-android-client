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

package com.mobicage.rogerthat.plugins.messaging;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.MainService;
import com.mobicage.rogerthat.plugins.friends.FriendsPlugin;
import com.mobicage.rogerthat.plugins.history.HistoryItem;
import com.mobicage.rogerthat.plugins.history.HistoryPlugin;
import com.mobicage.rogerthat.util.TextUtils;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.to.messaging.ButtonTO;
import com.mobicage.to.messaging.MemberStatusTO;
import com.mobicage.to.messaging.MemberStatusUpdateRequestTO;
import com.mobicage.to.messaging.MessageTO;

public class MessageHistory {

    private final static int HISTORY_MESSAGE_CUTOFF = 100;

    private final MainService mService;
    private final MessageStore mStore;
    private final FriendsPlugin mFriendsPlugin;
    private final HistoryPlugin mHistoryPlugin;

    MessageHistory(MainService service, MessageStore store) {
        mService = service;
        mStore = store;
        mFriendsPlugin = mService.getPlugin(com.mobicage.rogerthat.plugins.friends.FriendsPlugin.class);
        mHistoryPlugin = mService.getPlugin(HistoryPlugin.class);
    }

    /*
     * I clicked on a quick reply button in phone UI.
     * 
     * Two possibilities:
     * 
     * 1. I am the message sender
     * 
     * 2. Someone else is the message sender
     */
    void putMessageAckInHistory(String messageKey, String buttonId) {
        T.dontCare();

        final String myEmail = myEmail();
        final String myName = myName();

        final HistoryItem item = new HistoryItem();
        item.timestampMillis = System.currentTimeMillis();
        item.reference = messageKey;
        item.friendReference = myEmail;

        final MessageTO message = mStore.getFullMessageByKey(messageKey);
        if (message == null) {
            L.bug("Could not find message [" + messageKey + "]");
            return;
        }
        item.parameters.put(HistoryItem.PARAM_MESSAGE_CONTENT,
            TextUtils.trimString(message.message, HISTORY_MESSAGE_CUTOFF, true));

        item.parameters.put(HistoryItem.PARAM_MESSAGE_FROM, myName);

        final String caption = getCaptionForButtonId(message, buttonId);

        if (caption == null) {
            // dismiss
            item.type = HistoryItem.MESSAGE_DISMISSED_BY_ME;
        } else {
            item.parameters.put(HistoryItem.PARAM_MESSAGE_QUICK_REPLY_BUTTON, caption);
            if (message.sender.equals(myEmail)) {
                // I sent quick reply to myself
                item.type = HistoryItem.QUICK_REPLY_SENT_FOR_ME;
                item.parameters.put(HistoryItem.PARAM_MESSAGE_TO, myName);
            } else {
                // I sent quick reply to someone else
                item.type = HistoryItem.QUICK_REPLY_SENT_FOR_OTHER;
                item.parameters.put(HistoryItem.PARAM_MESSAGE_TO, mFriendsPlugin.getName(message.sender));
            }
        }

        mHistoryPlugin.addHistoryItem(item);
    }

    String getCaptionForButtonId(MessageTO message, String buttonId) {
        T.dontCare();
        String caption = null;
        for (final ButtonTO button : message.buttons) {
            if (button.id.equals(buttonId)) {
                caption = button.caption;
                break;
            }
        }
        return caption;
    }

    /*
     * Message updated over the web.
     * 
     * A number of possibilities
     * 
     * 1. Message update is not a quick-reply-button-click: we ignore it in the activity view
     * 
     * 2. I clicked quick reply button in web view on message sent by myself
     * 
     * 3. I clicked quick reply button in web view on message sent by other
     * 
     * 4. Other clicked quick reply button in web view on message sent by myself
     * 
     * 5. Other clicked quick reply button in web view on message sent by other
     */
    void putMessageUpdateInHistory(MemberStatusUpdateRequestTO request) {
        T.dontCare();

        // We are not interested in status updates such as 'phone X has received message'
        if ((request.status & MessagingPlugin.STATUS_ACKED) != MessagingPlugin.STATUS_ACKED)
            return;

        final HistoryItem item = new HistoryItem();
        item.timestampMillis = System.currentTimeMillis();
        item.reference = request.message;
        item.friendReference = request.member;

        final MessageTO message = mStore.getFullMessageByKey(request.message);
        if (message == null) {
            L.bug("Could not find message [" + request.message + "]");
            return;
        }
        item.parameters.put(HistoryItem.PARAM_MESSAGE_CONTENT,
            TextUtils.trimString(message.message, HISTORY_MESSAGE_CUTOFF, true));

        final String caption = getCaptionForButtonId(message, request.button_id);

        if (caption != null)
            item.parameters.put(HistoryItem.PARAM_MESSAGE_QUICK_REPLY_BUTTON, caption);

        final String myEmail = myEmail();
        final String myName = myName();
        final boolean updatedByMe = request.member.equals(myEmail);
        if (updatedByMe) {
            item.parameters.put(HistoryItem.PARAM_MESSAGE_FROM, myName);
            if (caption == null) {
                item.type = HistoryItem.MESSAGE_DISMISSED_BY_ME;
            } else {
                if (message.sender.equals(myEmail)) {
                    item.type = HistoryItem.QUICK_REPLY_SENT_FOR_ME;
                    item.parameters.put(HistoryItem.PARAM_MESSAGE_TO, myName);
                } else {
                    item.type = HistoryItem.QUICK_REPLY_SENT_FOR_OTHER;
                    item.parameters.put(HistoryItem.PARAM_MESSAGE_TO, mFriendsPlugin.getName(message.sender));
                }
            }
        } else {
            item.parameters.put(HistoryItem.PARAM_MESSAGE_FROM, mFriendsPlugin.getName(request.member));
            if (caption == null) {
                item.type = HistoryItem.MESSAGE_DISMISSED_BY_OTHER;
            } else {
                if (message.sender.equals(myEmail)) {
                    item.type = HistoryItem.QUICK_REPLY_RECEIVED_FOR_ME;
                    item.parameters.put(HistoryItem.PARAM_MESSAGE_TO, myName);
                } else {
                    item.type = HistoryItem.QUICK_REPLY_RECEIVED_FOR_OTHER;
                    item.parameters.put(HistoryItem.PARAM_MESSAGE_TO, mFriendsPlugin.getName(message.sender));
                }
            }
        }

        mHistoryPlugin.addHistoryItem(item);

    }

    void putMessageInHistory(MessageTO message) {
        T.dontCare();
        final HistoryItem item = new HistoryItem();
        item.timestampMillis = System.currentTimeMillis();
        item.reference = message.key;
        item.friendReference = message.sender;

        final String historyMessage = TextUtils.trimString(message.message, HISTORY_MESSAGE_CUTOFF, true);
        item.parameters.put(HistoryItem.PARAM_MESSAGE_CONTENT, historyMessage);

        final String myEmail = myEmail();
        final String myName = myName();

        final StringBuilder to = new StringBuilder();
        boolean toNeedsComma = false;
        if (message.sender.equals(myEmail)) {
            item.parameters.put(HistoryItem.PARAM_MESSAGE_FROM, myName);
            if (message.parent_key == null) {
                item.type = HistoryItem.MESSAGE_SENT;
            } else {
                item.type = HistoryItem.REPLY_SENT;
            }
        } else {
            item.parameters.put(HistoryItem.PARAM_MESSAGE_FROM, mFriendsPlugin.getName(message.sender));
            to.append(mService.getString(R.string.__me_as_recipient));
            toNeedsComma = true;
            if (message.parent_key == null) {
                item.type = HistoryItem.MESSAGE_RECEIVED;
            } else {
                item.type = HistoryItem.REPLY_RECEIVED;
            }
        }

        for (MemberStatusTO member : message.members) {
            final String memberEmail = member.member;
            if (memberEmail.equals(message.sender) || memberEmail.equals(myEmail))
                continue;
            if (toNeedsComma)
                to.append(", ");
            else
                toNeedsComma = true;
            to.append(mFriendsPlugin.getName(memberEmail));
        }

        if (message.parent_key != null) {
            final MessageTO parentMessage = mStore.getFullMessageByKey(message.parent_key);
            item.parameters.put(HistoryItem.PARAM_MESSAGE_PARENT_CONTENT,
                TextUtils.trimString(parentMessage.message, HISTORY_MESSAGE_CUTOFF, true));
        }

        item.parameters.put(HistoryItem.PARAM_MESSAGE_TO, to.toString());

        mHistoryPlugin.addHistoryItem(item);
    }

    void putMessageLockedInHistory(String messagekey) {
        T.dontCare();
        final HistoryItem item = new HistoryItem();
        item.timestampMillis = System.currentTimeMillis();

        final MessageTO message = mStore.getFullMessageByKey(messagekey);
        if (message == null) {
            L.bug("Cannot find message " + messagekey);
            return;
        }
        // Locking always happens by sender
        item.friendReference = message.sender;
        item.reference = messagekey;
        item.parameters.put(HistoryItem.PARAM_MESSAGE_CONTENT,
            TextUtils.trimString(message.message, HISTORY_MESSAGE_CUTOFF, true));
        if (message.sender.equals(myEmail())) {
            item.parameters.put(HistoryItem.PARAM_MESSAGE_FROM, myName());
            item.type = HistoryItem.MESSAGE_LOCKED_BY_ME;
        } else {
            item.parameters.put(HistoryItem.PARAM_MESSAGE_FROM, mFriendsPlugin.getName(message.sender));
            item.type = HistoryItem.MESSAGE_LOCKED_BY_OTHER;
        }

        // If sender has made a choice, put it in activity log
        for (final MemberStatusTO memberStatus : message.members) {
            if (memberStatus.member.equals(message.sender)) {
                if (memberStatus.button_id != null) {
                    final String buttonCaption = getCaptionForButtonId(message, memberStatus.button_id);
                    item.parameters.put(HistoryItem.PARAM_MESSAGE_QUICK_REPLY_BUTTON, buttonCaption);
                }
                break;
            }
        }

        mHistoryPlugin.addHistoryItem(item);
    }

    public void putQuickReplyUndoneInHistory(MessageTO message, MemberStatusTO newStatus) {
        T.dontCare();
        final HistoryItem item = new HistoryItem();
        item.timestampMillis = System.currentTimeMillis();
        item.type = HistoryItem.QUICK_REPLY_UNDONE;
        item.reference = message.key;
        item.friendReference = newStatus.member;

        String memberName = newStatus.member.equals(myEmail()) ? myName() : mFriendsPlugin.getName(newStatus.member);
        item.parameters.put(HistoryItem.PARAM_MESSAGE_FROM, memberName);

        for (ButtonTO button : message.buttons) {
            if (button.id.equals(newStatus.button_id)) {
                item.parameters.put(HistoryItem.PARAM_MESSAGE_QUICK_REPLY_BUTTON, button.caption);
                break;
            }
        }

        mHistoryPlugin.addHistoryItem(item);
    }

    public void deleteMessageFromHistory(String threadKey) {
        mHistoryPlugin.deleteHistoryItem(threadKey);
    }

    void updateMessageTmpKeyInHistory(String oldKey, String newKey) {
        mHistoryPlugin.updateHistoryItemReference(oldKey, newKey);
    }

    private String myName() {
        return mService.getIdentityStore().getIdentity().getName();
    }

    private String myEmail() {
        return mService.getIdentityStore().getIdentity().getEmail();
    }
}

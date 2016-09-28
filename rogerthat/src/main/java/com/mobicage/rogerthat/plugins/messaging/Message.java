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

import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rpc.IncompleteMessageException;
import com.mobicage.to.messaging.ButtonTO;
import com.mobicage.to.messaging.MemberStatusTO;
import com.mobicage.to.messaging.MessageTO;

import java.util.Map;

@SuppressWarnings("unchecked")
public class Message extends MessageTO {

    public static String POSITIVE = "positive";
    public static String NEGATIVE = "negative";

    public static String UNIT_VALUE = "<value/>";
    public static String UNIT_LOW_VALUE = "<low_value/>";
    public static String UNIT_HIGH_VALUE = "<high_value/>";

    public final static String MC_TEL_PREFIX = "tel://";
    public final static String MC_HTTP_PREFIX = "http://";
    public final static String MC_HTTPS_PREFIX = "https://";
    public final static String MC_GEO_PREFIX = "geo://";
    public final static String MC_CONFIRM_PREFIX = "confirm://";
    public final static String MC_MAILTO_PREFIX = "mailto://";
    public final static String MC_SMI_PREFIX = "smi://";

    public static int BLUE_BUTTON_COLOR = 0xFF6DB6F2;
    public static int GREEN_BUTTON_COLOR = 0xFFA4C14D;
    public static int RED_BUTTON_COLOR = 0xFF989898; // 0xFFCC0000;
    public static int BLUEGRAY_BUTTON_COLOR = 0xFF808099;
    public static int REDGRAY_BUTTON_COLOR = 0xFFCCCCCC; // 0xFF998080;
    public static int GREENGRAY_BUTTON_COLOR = 0xFF809980;

    public boolean dirty;
    public boolean needsMyAnswer;
    public String recipients;
    public long recipients_status;
    public boolean threadDirty;
    public long replyCount;
    public long unreadCount;
    public String lastThreadMessage;
    public Map<String, Object> form;
    public boolean threadNeedsMyAnswer;
    public boolean threadShowInList;

    public boolean hasTempKey;

    public final static int PRIORITY_NORMAL = 1;
    public final static int PRIORITY_HIGH = 2;
    public final static int PRIORITY_URGENT = 3;
    public final static int PRIORITY_URGENT_WITH_ALARM = 4;

    public Message() {
    }

    public Message(Map<String, Object> json) throws IncompleteMessageException {
        super(json);
        this.form = ((Map<String, Object>) json.get("form"));
    }

    public static Message fromFormMessage(Map<String, Object> form) {
        Message msg = new Message();
        msg.alert_flags = ((Long) form.get("alert_flags")).longValue();
        msg.branding = (String) form.get("branding");
        msg.flags = ((Long) form.get("flags")).longValue();
        msg.form = ((Map<String, Object>) form.get("form"));
        msg.key = (String) form.get("key");
        msg.message = (String) form.get("message");
        msg.parent_key = (String) form.get("parent_key");
        msg.sender = (String) form.get("sender");
        msg.timeout = 0;
        msg.timestamp = ((Long) form.get("timestamp")).longValue();
        msg.context = (String) form.get("context");
        msg.thread_size = (Long) form.get("thread_size");
        msg.broadcast_type = (String) form.get("broadcast_type");

        ButtonTO posBtn = new ButtonTO();
        posBtn.caption = (String) msg.form.get("positive_button");
        posBtn.id = POSITIVE;
        String posConf = (String) msg.form.get("positive_confirmation");
        if (posConf != null)
            posBtn.action = MC_CONFIRM_PREFIX + posConf;
        Long posUiFlags = (Long) msg.form.get("positive_button_ui_flags");
        if (posUiFlags != null)
            posBtn.ui_flags = posUiFlags.longValue();

        ButtonTO negBtn = new ButtonTO();
        negBtn.caption = (String) msg.form.get("negative_button");
        negBtn.id = NEGATIVE;
        String negConf = (String) msg.form.get("negative_confirmation");
        if (negConf != null)
            negBtn.action = MC_CONFIRM_PREFIX + negConf;
        Long negUiFlags = (Long) msg.form.get("negative_button_ui_flags");
        if (negUiFlags != null)
            negBtn.ui_flags = negUiFlags.longValue();

        msg.buttons = new ButtonTO[] { posBtn, negBtn };
        try {
            msg.members = new MemberStatusTO[] { new MemberStatusTO((Map<String, Object>) form.get("member")) };
        } catch (IncompleteMessageException e) {
            // Will never happen
            L.bug(e);
        }

        if (form.containsKey("attachments")) {
            try {
                org.json.simple.JSONArray attachments = (org.json.simple.JSONArray) form.get("attachments");
                msg.attachments = new com.mobicage.to.messaging.AttachmentTO[attachments.size()];
                for (int i = 0; i < attachments.size(); i++) {
                    Object item = attachments.get(i);
                    if (item != null) {
                        msg.attachments[i] = new com.mobicage.to.messaging.AttachmentTO((Map<String, Object>) item);
                    }
                }
            } catch (IncompleteMessageException e) {
                // Will never happen
                L.bug(e);
            }
        } else {
            msg.attachments = new com.mobicage.to.messaging.AttachmentTO[0];
        }

        return msg;
    }

    public int numRecipients() {
        return MessageMemberStatusSummaryEncoding.decodeNumNonSenderMembers(recipients_status);
    }

    public int numReceived() {
        return MessageMemberStatusSummaryEncoding.decodeNumNonSenderMembersReceived(recipients_status);
    }

    public int numDismissed() {
        return MessageMemberStatusSummaryEncoding.decodeNumNonSenderMembersDismissed(recipients_status);
    }

    public int numQuickReplied() {
        return MessageMemberStatusSummaryEncoding.decodeNumNonSenderMembersQuickReplied(recipients_status);
    }

    public int numAcked() {
        return numDismissed() + numQuickReplied();
    }

    public String getThreadKey() {
        return parent_key == null ? key : parent_key;
    }

    public ButtonTO getButton(String buttonId) {
        for (ButtonTO btn : this.buttons)
            if (btn.id.equals(buttonId))
                return btn;
        return null;
    }

    public MemberStatusTO getMemberStatus(String email) {
        for (MemberStatusTO ms : this.members)
            if (ms.member.equals(email))
                return ms;
        return null;
    }

    @Override
    public Map<String, Object> toJSONMap() {
        Map<String, Object> map = super.toJSONMap();
        map.put("form", form);
        return map;
    }

}

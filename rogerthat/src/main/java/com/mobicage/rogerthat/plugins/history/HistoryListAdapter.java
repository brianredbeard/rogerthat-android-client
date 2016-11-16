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

package com.mobicage.rogerthat.plugins.history;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.plugins.friends.FriendsPlugin;
import com.mobicage.rogerthat.plugins.messaging.Message;
import com.mobicage.rogerthat.plugins.messaging.MessagingPlugin;
import com.mobicage.rogerthat.plugins.scan.ProcessScanActivity;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.time.TimeUtils;

// XXX: red line should not move in case we rotate phone

public class HistoryListAdapter extends CursorAdapter {

    private final LayoutInflater mLayoutInflater;
    private final Context mContext;
    private final HistoryStore mStore;
    private final MessagingPlugin mMessagingPlugin;
    private final FriendsPlugin mFriendsPlugin;
    private long mLastReadItemID;

    public HistoryListAdapter(Context context, Cursor cursor, HistoryStore store, MessagingPlugin messagingPlugin,
        FriendsPlugin friendsPlugin, long lastReadItemID) {
        super(context, cursor, false);
        T.UI();
        mContext = context;
        mStore = store;
        mMessagingPlugin = messagingPlugin;
        mFriendsPlugin = friendsPlugin;
        mLayoutInflater = LayoutInflater.from(mContext);
        mLastReadItemID = lastReadItemID;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        T.UI();

        final View view;

        if (convertView == null) {
            view = mLayoutInflater.inflate(R.layout.activity_list_row, parent, false);
        } else {
            view = convertView;
        }

        final Cursor cursor = getCursor();
        if (!cursor.moveToPosition(position)) {
            throw new IllegalStateException("couldn't move cursor to position " + position);
        }
        final HistoryItem item = mStore.getCurrentHistoryItem(cursor);

        final StringTuple historyText = createHistoryText(item);

        final TextView tv1 = (TextView) view.findViewById(R.id.activity_list_row_text1);
        tv1.setText(historyText.s1);
        tv1.setTextColor(ContextCompat.getColorStateList(mContext, android.R.color.primary_text_light));

        final TextView tv2 = (TextView) view.findViewById(R.id.activity_list_row_text2);
        if (historyText.s2 == null)
            tv2.setVisibility(View.GONE);
        else {
            tv2.setText(historyText.s2);
            tv2.setVisibility(View.VISIBLE);
            tv2.setTextColor(ContextCompat.getColorStateList(mContext, android.R.color.secondary_text_light));
        }

        final TextView tv3 = (TextView) view.findViewById(R.id.activity_list_row_text3);
        if (historyText.s3 == null)
            tv3.setVisibility(View.GONE);
        else {
            tv3.setText(historyText.s3);
            tv3.setVisibility(View.VISIBLE);
            tv3.setTextColor(ContextCompat.getColorStateList(mContext, android.R.color.secondary_text_light));
        }

        updateDividerLine(view, item);

        final int image = getImage(item);
        final ImageView iv = (ImageView) view.findViewById(R.id.activity_list_row_icon);
        if (image != -1) {
            iv.setImageResource(image);
        } else {
            iv.setImageResource(0);
        }

        final TextView timestampTextView = (TextView) view.findViewById(R.id.timestamp);
        timestampTextView.setText(TimeUtils.getHumanTime(mContext, item.timestampMillis, false));

        view.setTag(item);
        return view;
    }

    void updateDividerLine(final View v, final HistoryItem item) {
        T.UI();
        if (item._id == mLastReadItemID) {
            v.findViewById(R.id.top_divider_line).setVisibility(View.VISIBLE);
        } else {
            v.findViewById(R.id.top_divider_line).setVisibility(View.GONE);
        }
    }

    private Message getMessage(String key) {
        return mMessagingPlugin.getStore().getMessageByKey(key);
    }

    void startItemDetailActivity(Activity activity, HistoryItem item) {
        T.UI();
        switch (item.type) {

        case HistoryItem.MESSAGE_SENT:
        case HistoryItem.MESSAGE_RECEIVED:
        case HistoryItem.QUICK_REPLY_RECEIVED_FOR_ME:
        case HistoryItem.QUICK_REPLY_RECEIVED_FOR_OTHER:
        case HistoryItem.QUICK_REPLY_SENT_FOR_ME:
        case HistoryItem.QUICK_REPLY_SENT_FOR_OTHER:
        case HistoryItem.QUICK_REPLY_UNDONE:
        case HistoryItem.MESSAGE_LOCKED_BY_ME:
        case HistoryItem.MESSAGE_LOCKED_BY_OTHER:
        case HistoryItem.MESSAGE_DISMISSED_BY_ME:
        case HistoryItem.MESSAGE_DISMISSED_BY_OTHER: {
            Message message = getMessage(item.reference);
            mMessagingPlugin.showMessage(activity, message, message.sender);
            break;
        }

        case HistoryItem.REPLY_SENT:
        case HistoryItem.REPLY_RECEIVED: {
            // Show message thread scrolled to this offset
            Message message = getMessage(item.reference);
            mMessagingPlugin.showMessage(activity, message, message.sender);
            break;
        }

        case HistoryItem.SERVICE_POKED:
        case HistoryItem.FRIEND_ADDED:
        case HistoryItem.FRIEND_UPDATED:
        case HistoryItem.LOCATION_SHARING_MY_LOCATION_SENT: {
            mFriendsPlugin.launchDetailActivity(activity, item.reference);
            break;
        }

        case HistoryItem.FRIEND_BECAME_FRIEND: {
            final Intent intent = new Intent(activity, ProcessScanActivity.class);
            intent.putExtra(ProcessScanActivity.EMAILHASH, item.reference);
            activity.startActivity(intent);
            break;
        }

        case HistoryItem.FRIEND_REMOVED:
        case HistoryItem.DEBUG:
        case HistoryItem.INFO:
        case HistoryItem.WARNING:
        case HistoryItem.ERROR:
        case HistoryItem.FATAL: {
            // do nothing
            break;
        }

        default: {
            L.bug("History item detail requested for unknown item type " + item.type);
        }

        }
    }

    private int getImage(HistoryItem item) {
        T.UI();
        switch (item.type) {

        case HistoryItem.FATAL:
        case HistoryItem.ERROR:
        case HistoryItem.WARNING:
        case HistoryItem.QUICK_REPLY_UNDONE:
            return R.drawable.act_error;
        case HistoryItem.INFO:
        case HistoryItem.DEBUG:
        case HistoryItem.SERVICE_POKED:
            return R.drawable.act_info;

        case HistoryItem.FRIEND_ADDED:
            return R.drawable.act_friend_plus;
        case HistoryItem.FRIEND_REMOVED:
            return R.drawable.act_friend_minus;
        case HistoryItem.FRIEND_UPDATED:
            return R.drawable.act_friend_updated;
        case HistoryItem.FRIEND_BECAME_FRIEND:
            return R.drawable.act_friend_added_friend;

        case HistoryItem.LOCATION_SHARING_MY_LOCATION_SENT:
            return R.drawable.act_location_sent;

        case HistoryItem.MESSAGE_DISMISSED_BY_ME:
            return R.drawable.act_msg_dismissed_by_me;
        case HistoryItem.MESSAGE_DISMISSED_BY_OTHER:
            return R.drawable.act_msg_dismissed_by_other;
        case HistoryItem.MESSAGE_LOCKED_BY_ME:
            return R.drawable.act_msg_locked_by_me;
        case HistoryItem.MESSAGE_LOCKED_BY_OTHER:
            return R.drawable.act_msg_locked_by_other;
        case HistoryItem.MESSAGE_RECEIVED:
            return R.drawable.act_msg_new_received;
        case HistoryItem.MESSAGE_SENT:
            return R.drawable.act_msg_new_sent;
        case HistoryItem.REPLY_RECEIVED:
            return R.drawable.act_msg_reply_received;
        case HistoryItem.REPLY_SENT:
            return R.drawable.act_msg_reply_sent;
        case HistoryItem.QUICK_REPLY_RECEIVED_FOR_ME:
        case HistoryItem.QUICK_REPLY_RECEIVED_FOR_OTHER:
            return R.drawable.act_msg_quickreply_received;
        case HistoryItem.QUICK_REPLY_SENT_FOR_ME:
        case HistoryItem.QUICK_REPLY_SENT_FOR_OTHER:
            return R.drawable.act_msg_quickreply_sent;

        default:
            L.bug("Unexpected history item type");
            return R.drawable.act_error;
        }
    }

    private class StringTuple {
        String s1;
        String s2;
        String s3;

        private StringTuple(final String s1, final String s2, final String s3) {
            this.s1 = s1;
            this.s2 = s2;
            this.s3 = s3;
        }
    }

    private StringTuple createHistoryText(HistoryItem item) {
        T.UI();
        String s1 = null, s2 = null, s3 = null;
        switch (item.type) {
        case HistoryItem.MESSAGE_SENT:
            s1 = "> " + item.parameters.get(HistoryItem.PARAM_MESSAGE_TO);
            s2 = item.parameters.get(HistoryItem.PARAM_MESSAGE_CONTENT);
            break;
        case HistoryItem.MESSAGE_RECEIVED:
            s1 = item.parameters.get(HistoryItem.PARAM_MESSAGE_FROM);
            s2 = item.parameters.get(HistoryItem.PARAM_MESSAGE_CONTENT);
            break;
        case HistoryItem.REPLY_SENT:
            s1 = "> " + item.parameters.get(HistoryItem.PARAM_MESSAGE_TO);
            s2 = item.parameters.get(HistoryItem.PARAM_MESSAGE_PARENT_CONTENT);
            s3 = item.parameters.get(HistoryItem.PARAM_MESSAGE_CONTENT);
            break;
        case HistoryItem.REPLY_RECEIVED:
            s1 = item.parameters.get(HistoryItem.PARAM_MESSAGE_FROM);
            s2 = item.parameters.get(HistoryItem.PARAM_MESSAGE_PARENT_CONTENT);
            s3 = item.parameters.get(HistoryItem.PARAM_MESSAGE_CONTENT);
            break;
        case HistoryItem.QUICK_REPLY_RECEIVED_FOR_ME:
            s1 = item.parameters.get(HistoryItem.PARAM_MESSAGE_FROM);
            s2 = item.parameters.get(HistoryItem.PARAM_MESSAGE_CONTENT);
            s3 = item.parameters.get(HistoryItem.PARAM_MESSAGE_QUICK_REPLY_BUTTON);
            break;
        case HistoryItem.QUICK_REPLY_RECEIVED_FOR_OTHER:
            s1 = item.parameters.get(HistoryItem.PARAM_MESSAGE_FROM);
            s2 = item.parameters.get(HistoryItem.PARAM_MESSAGE_CONTENT);
            s3 = item.parameters.get(HistoryItem.PARAM_MESSAGE_QUICK_REPLY_BUTTON);
            break;
        case HistoryItem.QUICK_REPLY_SENT_FOR_ME:
            s1 = mContext.getString(R.string.__me_as_sender);
            s2 = item.parameters.get(HistoryItem.PARAM_MESSAGE_CONTENT);
            s3 = item.parameters.get(HistoryItem.PARAM_MESSAGE_QUICK_REPLY_BUTTON);
            break;
        case HistoryItem.QUICK_REPLY_SENT_FOR_OTHER:
            s1 = "> " + item.parameters.get(HistoryItem.PARAM_MESSAGE_TO);
            s2 = item.parameters.get(HistoryItem.PARAM_MESSAGE_CONTENT);
            s3 = item.parameters.get(HistoryItem.PARAM_MESSAGE_QUICK_REPLY_BUTTON);
            break;
        case HistoryItem.MESSAGE_LOCKED_BY_ME: {
            s1 = mContext.getString(R.string.__me_as_sender);
            s2 = item.parameters.get(HistoryItem.PARAM_MESSAGE_CONTENT);
            final String chosenButton = item.parameters.get(HistoryItem.PARAM_MESSAGE_QUICK_REPLY_BUTTON);
            if (chosenButton != null) {
                s3 = chosenButton;
            }
            break;
        }
        case HistoryItem.MESSAGE_LOCKED_BY_OTHER: {
            s1 = item.parameters.get(HistoryItem.PARAM_MESSAGE_FROM);
            s2 = item.parameters.get(HistoryItem.PARAM_MESSAGE_CONTENT);
            final String chosenButton = item.parameters.get(HistoryItem.PARAM_MESSAGE_QUICK_REPLY_BUTTON);
            if (chosenButton != null) {
                s3 = chosenButton;
            }
            break;
        }
        case HistoryItem.MESSAGE_DISMISSED_BY_ME: {
            s1 = mContext.getString(R.string.__me_as_sender);
            s2 = mContext.getString(R.string.activity_message_dismissed);
            s3 = item.parameters.get(HistoryItem.PARAM_MESSAGE_CONTENT);
            break;
        }
        case HistoryItem.MESSAGE_DISMISSED_BY_OTHER: {
            s1 = item.parameters.get(HistoryItem.PARAM_MESSAGE_FROM);
            s2 = mContext.getString(R.string.activity_message_dismissed);
            s3 = item.parameters.get(HistoryItem.PARAM_MESSAGE_CONTENT);
            break;
        }
        case HistoryItem.QUICK_REPLY_UNDONE: {
            s1 = item.parameters.get(HistoryItem.PARAM_MESSAGE_FROM);
            s2 = mContext.getString(R.string.activity_quick_reply_undone);
            final String chosenButton = item.parameters.get(HistoryItem.PARAM_MESSAGE_QUICK_REPLY_BUTTON);
            if (chosenButton == null) {
                s3 = mContext.getString(R.string.activity_answer_reverted);
            } else {
                s3 = mContext.getString(R.string.activity_answer_reverted_to, chosenButton);
            }
            break;
        }
        case HistoryItem.FRIEND_ADDED: {
            s1 = item.parameters.get(HistoryItem.PARAM_FRIEND_NAME);
            s2 = mContext.getString(R.string.activity_new_friend);
            break;
        }
        case HistoryItem.FRIEND_REMOVED: {
            s1 = item.parameters.get(HistoryItem.PARAM_FRIEND_NAME);
            s2 = mContext.getString(R.string.activity_removed_friend);
            break;
        }
        case HistoryItem.FRIEND_UPDATED: {
            s1 = item.parameters.get(HistoryItem.PARAM_FRIEND_NAME);
            s2 = mContext.getString(R.string.activity_updated_friend);
            break;
        }
        case HistoryItem.LOCATION_SHARING_MY_LOCATION_SENT: {
            s1 = item.parameters.get(HistoryItem.PARAM_FRIEND_NAME);
            s2 = mContext.getString(R.string.activity_requested_location);
            break;
        }
        case HistoryItem.FRIEND_BECAME_FRIEND: {
            s1 = item.parameters.get(HistoryItem.PARAM_FRIEND_NAME);
            s2 = mContext.getString(R.string.activity_became_friend_with,
                item.parameters.get(HistoryItem.PARAM_FRIENDS_FRIEND_NAME));
            break;
        }
        case HistoryItem.SERVICE_POKED: {
            s1 = item.parameters.get(HistoryItem.PARAM_FRIEND_NAME);
            s2 = mContext.getString(R.string.activity_service_poked);
            break;
        }
        case HistoryItem.FATAL:
        case HistoryItem.ERROR:
        case HistoryItem.WARNING:
        case HistoryItem.INFO:
        case HistoryItem.DEBUG: {
            s1 = mContext.getString(R.string.activity_info);
            s2 = item.parameters.get(HistoryItem.PARAM_LOG_LINE);
            break;
        }
        default:
            L.bug("Error unknown history item type " + item.type);
            s1 = mContext.getString(R.string.activity_error);
            break;
        }

        final StringTuple result = new StringTuple(s1, s2, s3);
        return result;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        L.bug("Should not come here");
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        L.bug("Should not come here");
        return null;
    }

    protected void setLastReadItem(final long itemID) {
        T.UI();
        mLastReadItemID = itemID;
    }

    long getLastReadItem() {
        T.UI();
        return mLastReadItemID;
    }

}

package com.mobicage.rogerthat.plugins.messaging.widgets;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.mobicage.api.messaging.Rpc;
import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.AddFriendsActivity;
import com.mobicage.rogerthat.plugins.friends.Friend;
import com.mobicage.rogerthat.plugins.friends.FriendStore;
import com.mobicage.rogerthat.plugins.friends.FriendsPlugin;
import com.mobicage.rogerthat.plugins.messaging.Message;
import com.mobicage.rogerthat.plugins.messaging.MessagingPlugin;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeViewOnClickListener;
import com.mobicage.rogerthat.util.ui.UIUtils;
import com.mobicage.rpc.ResponseHandler;
import com.mobicage.rpc.config.AppConstants;
import com.mobicage.to.messaging.forms.SubmitFriendSelectFormRequestTO;
import com.mobicage.to.messaging.forms.SubmitFriendSelectFormResponseTO;
import com.mobicage.to.messaging.forms.UnicodeListWidgetResultTO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FriendSelectWidget extends Widget {

    private FriendsPlugin mFriendsPlugin;
    private FriendStore mFriendStore;
    private FriendSelectAdapter mAdapter;
    private Cursor mCursor;

    private boolean mMultiSelect = false;
    private boolean mSelectionRequired = true;
    private Set<String> mSelectedFriends = new HashSet<>();

    public FriendSelectWidget(Context context) {
        super(context);
    }

    public FriendSelectWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void initializeWidget() {
        if (mWidgetMap.containsKey("multi_select")) {
            mMultiSelect = (Boolean) mWidgetMap.get("multi_select");
        }
        if (mWidgetMap.containsKey("selection_required")) {
            mSelectionRequired = (Boolean) mWidgetMap.get("selection_required");
        }

        @SuppressWarnings("unchecked")
        final List<String> values = (List<String>) mWidgetMap.get("values");
        if (values != null) {
            mSelectedFriends.addAll(values);
        }

        mFriendsPlugin = mActivity.getMainService().getPlugin(FriendsPlugin.class);
        mFriendStore = mFriendsPlugin.getStore();
        mCursor = mFriendStore.getUserFriendListCursor();

        mAdapter = new FriendSelectAdapter(mCursor);

        final ListView listView = (ListView) findViewById(R.id.list_view);
        listView.setAdapter(mAdapter);
        initUI(false);
    }

    private void initUI(boolean isRefresh) {
        final ListView listView = (ListView) findViewById(R.id.list_view);

        final int friendCount = mCursor.getCount();
        L.d("User has " + friendCount + " friend(s)");

        if (friendCount == 0) {
            findViewById(R.id.no_friends_layout).setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);

            final TextView noFriendsTextView = (TextView) findViewById(R.id.no_friends_found);
            noFriendsTextView.setTextColor(mTextColor);
            noFriendsTextView.setText(mActivity.getString(R.string.no_friends_found, mActivity.getString(R.string
                    .app_name)));

            final Button noFriendsButton = (Button) findViewById(R.id.invite_button);
            switch (AppConstants.FRIENDS_CAPTION) {
                case COLLEAGUES:
                    noFriendsButton.setText(R.string.find_colleagues);
                    break;
                case CONTACTS:
                    noFriendsButton.setText(R.string.find_contacts);
                    break;
                case FRIENDS:
                default:
                    noFriendsButton.setText(R.string.invite_friends_short);
                    break;
            }

            noFriendsButton.setOnClickListener(new SafeViewOnClickListener() {
                public void safeOnClick(View v) {
                    mActivity.startActivity(new Intent(mActivity, AddFriendsActivity.class));
                }
            });
            noFriendsButton.setEnabled(isEnabled());
        } else {
            findViewById(R.id.no_friends_layout).setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);

            if (friendCount == 1) {
                if (!isRefresh) {
                    // pre-select the friend
                    try {
                        mCursor.moveToFirst();
                        mSelectedFriends.add(mFriendStore.readFriendFromCursor(mCursor).email);
                    } catch (Exception e) {
                        L.bug(e);
                    }
                }
            }
        }

        UIUtils.setListViewHeightBasedOnItems(listView, -1);
    }

    @Override
    public void putValue() {
        final ArrayList<String> values = new ArrayList<>(mSelectedFriends);
        Collections.sort(values);
        mWidgetMap.put("values", values);
    }

    @SuppressWarnings("UnusedParameters")
    public static String valueString(Context context, Map<String, Object> widget) {
        @SuppressWarnings("unchecked")
        final List<String> values = (List<String>) widget.get("values");
        if (values != null && values.size() != 0) {
            return TextUtils.join("\n", values);
        }
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public UnicodeListWidgetResultTO getWidgetResult() {
        UnicodeListWidgetResultTO result = new UnicodeListWidgetResultTO();
        final List<String> values = (List<String>) mWidgetMap.get("values");
        result.values = values.toArray(new String[values.size()]);
        return result;
    }

    @Override
    public boolean proceedWithSubmit(final String buttonId) {
        if (mSelectionRequired && Message.POSITIVE.equals(buttonId) && mSelectedFriends.size() == 0) {
            UIUtils.showAlertDialog(mActivity, R.string.activity_error, R.string.friend_selection_is_required);
            return false;
        }
        return true;
    }

    @Override
    public void submit(String buttonId, long timestamp) throws Exception {
        final SubmitFriendSelectFormRequestTO request = new SubmitFriendSelectFormRequestTO();
        request.button_id = buttonId;
        request.message_key = mMessage.key;
        request.parent_message_key = mMessage.parent_key;
        request.timestamp = timestamp;
        if (Message.POSITIVE.equals(buttonId)) {
            request.result = getWidgetResult();
        }
        if ((mMessage.flags & MessagingPlugin.FLAG_SENT_BY_JSMFR) == MessagingPlugin.FLAG_SENT_BY_JSMFR) {
            mPlugin.answerJsMfrMessage(mMessage, request.toJSONMap(),
                    "com.mobicage.api.messaging.submitFriendSelectForm", mActivity, mParentView);
        } else {
            Rpc.submitFriendSelectForm(new ResponseHandler<SubmitFriendSelectFormResponseTO>(), request);
        }
    }

    // XXX: We only need this implementation when the widget doesn't completely reload anymore on friend update intents
    public String[] onBroadcastReceived(Intent intent) {
        final String action = intent.getAction();
        boolean refreshNeeded = true;

        if (FriendsPlugin.FRIEND_REMOVED_INTENT.equals(action)) {
            final String email = intent.getStringExtra("email");
            refreshNeeded = mSelectedFriends.remove(email);
        }

        if (refreshNeeded) {
            initUI(true);
            mAdapter.notifyDataSetChanged();
            return new String[]{FriendsPlugin.FRIEND_ADDED_INTENT, FriendsPlugin.FRIEND_UPDATE_INTENT,
                    FriendsPlugin.FRIENDS_LIST_REFRESHED};
        }

        return null;
    }

    private class FriendSelectAdapter extends CursorAdapter implements CheckBox.OnCheckedChangeListener {

        public FriendSelectAdapter(final Cursor cursor) {
            super(mActivity, cursor);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            L.bug("Should not come here");
            return null;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            L.bug("Should not come here");
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (!mCursor.moveToPosition(position)) {
                throw new IllegalStateException("couldn't move cursor to position " + position);
            }

            if (convertView == null) {
                convertView = LayoutInflater.from(mActivity).inflate(R.layout.select_friend, parent, false);
            }

            final Friend friend = mFriendStore.readFriendFromCursor(mCursor);
            final ImageView image = (ImageView) convertView.findViewById(R.id.friend_avatar);
            image.setImageBitmap(mFriendsPlugin.toFriendBitmap(friend.avatar));
            final TextView name = (TextView) convertView.findViewById(R.id.friend_name);
            name.setText(friend.getDisplayName());

            final CheckBox checkbox = (CheckBox) convertView.findViewById(R.id.friend_checkbox);
            checkbox.setVisibility(mMultiSelect ? View.VISIBLE : View.GONE);
            final RadioButton radioButton = (RadioButton) convertView.findViewById(R.id.friend_radio_button);

            radioButton.setVisibility(mMultiSelect ? View.GONE : View.VISIBLE);
            final CompoundButton compoundBtn = mMultiSelect ? checkbox : radioButton;
            compoundBtn.setTag(friend.email);
            compoundBtn.setOnCheckedChangeListener(null); // avoid multiple calls to notifyDataSetChanged()
            compoundBtn.setChecked(mSelectedFriends.contains(friend.email));
            compoundBtn.setOnCheckedChangeListener(this);
            compoundBtn.setEnabled(FriendSelectWidget.this.isEnabled());

            convertView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (FriendSelectWidget.this.isEnabled()) {
                        compoundBtn.toggle();
                    }
                }
            });

            return convertView;
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            final String email = (String) buttonView.getTag();
            if (isChecked) {
                if (!mMultiSelect) {
                    mSelectedFriends.clear();
                }
                mSelectedFriends.add(email);
            } else {
                mSelectedFriends.remove(email);
            }
            L.d("Selected friends: " + mSelectedFriends);
            notifyDataSetChanged();
        }
    }

}

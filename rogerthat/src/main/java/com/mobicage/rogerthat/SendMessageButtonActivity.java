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

package com.mobicage.rogerthat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;
import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.plugins.friends.Friend;
import com.mobicage.rogerthat.plugins.friends.FriendsPlugin;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.pickle.Pickler;
import com.mobicage.rogerthat.util.system.SafeDialogInterfaceOnClickListener;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.system.SafeViewOnClickListener;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.ui.UIUtils;
import com.mobicage.rpc.config.AppConstants;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SendMessageButtonActivity extends ServiceBoundActivity {

    private static final Pattern actionPattern = Pattern.compile("^(tel://|geo://|https?://)(.*)$");

    public static String CANNED_BUTTONS = "cannedbuttons";
    public static String BUTTONS = "buttons";
    public static final long NO_BUTTON_SELECTED = -1;

    private static final int PICK_CONTACT = 1;
    private static final int GET_LOCATION = 2;

    private ListView mButtonsListView;
    private EditText mActionView;
    private EditText mCaptionView;

    private Set<Long> mButtons = new LinkedHashSet<Long>();
    private CannedButtons mCannedButtons = null;
    private long mSelectedButton = NO_BUTTON_SELECTED;



    @Override
    protected void onServiceBound() {
        setContentViewWithoutNavigationBar(R.layout.send_message_button);
        setActivityName("send_message_button");
        setTitle(R.string.title_buttons);

        mCaptionView = (EditText) findViewById(R.id.button_caption);
        mActionView = (EditText) findViewById(R.id.button_action);

        Intent intent = getIntent();
        try {
            mCannedButtons = (CannedButtons) Pickler.createObjectFromPickle(intent.getByteArrayExtra(CANNED_BUTTONS));
            mButtons = new LinkedHashSet<Long>();
            long[] buttons = intent.getLongArrayExtra(BUTTONS);
            if (buttons != null) {
                for (long l : buttons) {
                    mButtons.add(l);
                }
            }
        } catch (Exception e) {
            L.bug(e);
            setResult(Activity.RESULT_CANCELED);
            finish();
        }

        initButtonsList();
    }

    @Override
    protected void onServiceUnbound() {

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                setResult(Activity.RESULT_CANCELED);
                finish();
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (!mServiceIsBound) {
            addOnServiceBoundRunnable(new SafeRunnable() {
                @Override
                protected void safeRun() throws Exception {
                    onActivityResult(requestCode, resultCode, data);
                }
            });
            return;
        }

        switch (requestCode) {
            case PICK_CONTACT:
                if (resultCode == Activity.RESULT_OK) {
                    Uri contactData = data.getData();
                    Cursor c = managedQuery(contactData, null, null, null, null);
                    if (c.moveToFirst()) {
                        try {
                            String number = c.getString(c
                                    .getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            String name = c.getString(c
                                    .getColumnIndexOrThrow(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME));
                            mActionView.setText("tel://" + number);
                            if (mCaptionView.getText().equals(""))
                                mCaptionView.setText(getString(R.string.caption_call, new Object[]{name}));
                        } catch (IllegalArgumentException e) {
                            L.bug("Could not get phone number from list.", e);
                        }
                    }
                }
                break;
            case GET_LOCATION:
                if (resultCode == Activity.RESULT_OK) {
                    mActionView.setText("geo://" + data.getDoubleExtra("latitude", 0) + ","
                            + data.getDoubleExtra("longitude", 0));
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        T.UI();
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.send_message_button_menu, menu);
        menu.getItem(0).setIcon(new IconicsDrawable(this).icon(FontAwesome.Icon.faw_check).color(Color.DKGRAY).sizeDp(18));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        T.UI();

        switch (item.getItemId()) {
            case R.id.save:
                try {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra(CANNED_BUTTONS, Pickler.getPickleFromObject(mCannedButtons));
                    long[] primitiveLongArray = new long[mButtons.size()];
                    Long[] longArray = mButtons.toArray(new Long[mButtons.size()]);
                    for (int i =0; i < longArray.length; i++) {
                        primitiveLongArray[i] = longArray[i].longValue();
                    }
                    resultIntent.putExtra(BUTTONS, primitiveLongArray);
                    setResult(Activity.RESULT_OK, resultIntent);
                } catch (Exception e) {
                    L.bug(e);
                    setResult(Activity.RESULT_CANCELED);
                }
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class CannedButtonAdapter extends BaseAdapter {

        private final CannedButtons mButtons;

        public CannedButtonAdapter(CannedButtons buttons) {
            mButtons = buttons;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final View view;
            if (convertView == null)
                view = SendMessageButtonActivity.this.getLayoutInflater().inflate(R.layout.canned_button_item, parent,
                        false);
            else
                view = convertView;
            CannedButton item = mButtons.get(position);
            setButtonItemView(view, item);
            return view;
        }

        @Override
        public int getCount() {
            return mButtons.size();
        }

        @Override
        public Object getItem(int position) {
            return mButtons.get(position);
        }

        @Override
        public long getItemId(int position) {
            return mButtons.get(position).getId();
        }
    }

    private void initButtonsList() {
        final LinearLayout buttons = (LinearLayout) findViewById(R.id.buttons);
        buttons.removeAllViews();
        for (Long buttonId : mButtons) {
            CannedButton button = mCannedButtons.getById(buttonId);
            if (button == null)
                continue;
            button.setSelected(true);
            Button b = addButton(buttons, button);
            if (mSelectedButton == button.getId())
                selectButton(b);
        }
        mButtonsListView = (ListView) findViewById(R.id.button_list);
        final CannedButtonAdapter cannedButtonAdapter = new CannedButtonAdapter(mCannedButtons);
        mButtonsListView.setAdapter(cannedButtonAdapter);
        mButtonsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
                try {
                    final CannedButton cannedButton = (CannedButton) view.getTag();
                    cannedButton.setSelected(!cannedButton.isSelected());
                    setButtonItemView(view, cannedButton);
                    if (cannedButton.isSelected()) {
                        addButton(buttons, cannedButton);
                        mButtons.add(cannedButton.getId());
                    } else {
                        removeSelectedButton(buttons, cannedButton);
                    }
                } catch (Exception e) {
                    L.bug(e);
                }
            }
        });
        mButtonsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            private boolean handled = false;

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    final CannedButton cannedButton = (CannedButton) view.getTag();
                    new AlertDialog.Builder(SendMessageButtonActivity.this)
                            .setMessage(getString(R.string.remove_canned_button, cannedButton.getCaption()))
                            .setPositiveButton(R.string.yes, new SafeDialogInterfaceOnClickListener() {
                                @Override
                                public void safeOnClick(DialogInterface dialog, int which) {
                                    handled = false;
                                    mCannedButtons.remove(cannedButton);
                                    if (cannedButton.isSelected()) {
                                        removeSelectedButton(buttons, cannedButton);
                                    }
                                    cannedButtonAdapter.notifyDataSetChanged();
                                }
                            }).setNegativeButton(R.string.no, new SafeDialogInterfaceOnClickListener() {
                        @Override
                        public void safeOnClick(DialogInterface dialog, int which) {
                            handled = true;
                        }
                    }).create().show();
                    return handled;
                } catch (Exception e) {
                    L.bug(e);
                    return false;
                }
            }
        });
        Button addButton = (Button) findViewById(R.id.add_button);
        addButton.setOnClickListener(new SafeViewOnClickListener() {
            @Override
            public void safeOnClick(View v) {
                final View dialog = getLayoutInflater().inflate(R.layout.new_button_dialog, null);
                mCaptionView = (EditText) dialog.findViewById(R.id.button_caption);
                mActionView = (EditText) dialog.findViewById(R.id.button_action);
                final Button actionHelp = (Button) dialog.findViewById(R.id.action_help_button);
                final RadioButton telRadio = (RadioButton) dialog.findViewById(R.id.action_tel);
                final RadioButton geoRadio = (RadioButton) dialog.findViewById(R.id.action_geo);
                final RadioButton wwwRadio = (RadioButton) dialog.findViewById(R.id.action_www);
                telRadio.setChecked(true);
                telRadio.setOnClickListener(new SafeViewOnClickListener() {
                    @Override
                    public void safeOnClick(View v) {
                        mActionView.setText("tel://");
                        actionHelp.setVisibility(View.VISIBLE);
                    }
                });
                geoRadio.setOnClickListener(new SafeViewOnClickListener() {
                    @Override
                    public void safeOnClick(View v) {
                        mActionView.setText("geo://");
                        actionHelp.setVisibility(View.VISIBLE);
                    }
                });
                wwwRadio.setOnClickListener(new SafeViewOnClickListener() {
                    @Override
                    public void safeOnClick(View v) {
                        mActionView.setText("http://");
                        actionHelp.setVisibility(View.GONE);
                    }
                });
                actionHelp.setOnClickListener(new SafeViewOnClickListener() {
                    @Override
                    public void safeOnClick(View v) {
                        if (telRadio.isChecked()) {
                            Intent intent = new Intent(Intent.ACTION_PICK,
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                            startActivityForResult(intent, PICK_CONTACT);
                        } else if (geoRadio.isChecked()) {
                            Intent intent = new Intent(SendMessageButtonActivity.this, GetLocationActivity.class);
                            startActivityForResult(intent, GET_LOCATION);
                        }
                    }
                });
                AlertDialog alertDialog = new AlertDialog.Builder(SendMessageButtonActivity.this)
                        .setTitle(R.string.create_button_title).setView(dialog)
                        .setPositiveButton(getString(R.string.ok), new SafeDialogInterfaceOnClickListener() {
                            @Override
                            public void safeOnClick(DialogInterface di, int which) {
                                Matcher action = actionPattern.matcher(mActionView.getText());
                                if (!action.matches()) {
                                    UIUtils.showLongToast(SendMessageButtonActivity.this,
                                            getString(R.string.action_not_valid));
                                    return;
                                }
                                String caption = mCaptionView.getText().toString();
                                if ("".equals(caption.trim())) {
                                    UIUtils.showLongToast(SendMessageButtonActivity.this,
                                            getString(R.string.caption_required));
                                    return;
                                }
                                CannedButton cannedButton = new CannedButton(caption, "".equals(action.group(2)) ? null
                                        : action.group());
                                mCannedButtons.add(cannedButton);
                                cannedButton.setSelected(true);
                                cannedButtonAdapter.notifyDataSetChanged();
                                addButton(buttons, cannedButton);
                                mButtons.add(cannedButton.getId());
                            }
                        }).setNegativeButton(getString(R.string.cancel), new SafeDialogInterfaceOnClickListener() {
                            @Override
                            public void safeOnClick(DialogInterface dialog, int which) {
                            }
                        }).create();
                alertDialog.setCanceledOnTouchOutside(true);
                alertDialog.show();

            }
        });
    }

    private void setButtonItemView(final View view, CannedButton item) {
        TextView captionTextView = (TextView) view.findViewById(R.id.caption);
        TextView actionTextView = (TextView) view.findViewById(R.id.action);
        Resources resources = getResources();
        captionTextView.setTextColor(resources.getColor(android.R.color.primary_text_light));
        actionTextView.setTextColor(resources.getColor(android.R.color.secondary_text_light));
        ImageView statusImageView = (ImageView) view.findViewById(R.id.status);
        statusImageView.setImageDrawable(new IconicsDrawable(this, FontAwesome.Icon.faw_check).color(Color.WHITE).sizeDp(12));
        if (item.isSelected()) {
            statusImageView.setVisibility(View.VISIBLE);
        } else {
            statusImageView.setVisibility(View.GONE);
        }
        captionTextView.setText(item.getCaption());
        String action = item.getAction();
        if (action == null) {
            actionTextView.setVisibility(View.GONE);
            captionTextView.setPadding(0, 13, 0, 13);
        } else {
            actionTextView.setVisibility(View.VISIBLE);
            actionTextView.setText(action);
            captionTextView.setPadding(0, 0, 0, 0);
        }
        view.setTag(item);
    }

    private Button addButton(final LinearLayout buttons, final CannedButton cannedButton) {
        final Button button = new Button(SendMessageButtonActivity.this);
        button.setText(cannedButton.getCaption());
        button.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        button.setTag(cannedButton);
        button.setOnClickListener(new SafeViewOnClickListener() {
            @Override
            public void safeOnClick(View v) {

                if (mSelectedButton == cannedButton.getId())
                    mSelectedButton = NO_BUTTON_SELECTED;

                cannedButton.setSelected(false);
                View view = mButtonsListView.findViewWithTag(cannedButton);
                setButtonItemView(view, cannedButton);
                buttons.removeView(button);
                mButtons.remove(cannedButton.getId());
            }
        });
        button.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                try {
                    boolean selected = cannedButton.getId() == mSelectedButton;
                    if (!selected) {
                        selectButton(button);
                        mSelectedButton = cannedButton.getId();
                        for (int i = 0; i < buttons.getChildCount(); i++) {
                            View view = buttons.getChildAt(i);
                            if (!(view instanceof Button) || view == button)
                                continue;
                            Button b = (Button) view;
                            unSelectButton(b);
                        }
                    } else {
                        unSelectButton(button);
                        mSelectedButton = NO_BUTTON_SELECTED;
                    }
                    return true;
                } catch (Exception e) {
                    L.bug(e);
                    return false;
                }
            }
        });
        buttons.addView(button);
        HorizontalScrollView scroller = (HorizontalScrollView) findViewById(R.id.buttons_scroller);
        scroller.smoothScrollTo(buttons.getWidth(), 0);
        return button;
    }

    private void selectButton(final Button button) {
        ImageView statusImageView = (ImageView) button.findViewById(R.id.status);
        statusImageView.setVisibility(View.VISIBLE);
    }

    private void unSelectButton(final Button button) {
        ImageView statusImageView = (ImageView) button.findViewById(R.id.status);
        statusImageView.setVisibility(View.GONE);
    }

    private void removeSelectedButton(final LinearLayout buttons, final CannedButton cannedButton) {

        if (mSelectedButton== cannedButton.getId())
            mSelectedButton = NO_BUTTON_SELECTED;

        Button button = (Button) buttons.findViewWithTag(cannedButton);
        buttons.removeView(button);
        mButtons.remove(cannedButton.getId());
    }
}

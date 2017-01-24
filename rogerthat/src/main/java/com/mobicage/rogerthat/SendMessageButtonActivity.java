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
import android.graphics.Color;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;
import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.pickle.Pickler;
import com.mobicage.rogerthat.util.system.SafeDialogInterfaceOnClickListener;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.system.SafeViewOnClickListener;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.ui.UIUtils;

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

    CannedButtonAdapter mCannedButtonAdapter;
    private ListView mButtonsListView;
    private EditText mActionView;
    private EditText mCaptionView;

    private Set<Long> mButtons = new LinkedHashSet<Long>();
    private CannedButtons mCannedButtons = null;

    @Override
    protected void onServiceBound() {
        setContentView(R.layout.send_message_button);
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

        FloatingActionButton floatingActionButton = ((FloatingActionButton) findViewById(R.id.add));
        floatingActionButton.setImageDrawable(new IconicsDrawable(this, FontAwesome.Icon.faw_plus).color(Color.WHITE).sizeDp(24));
        floatingActionButton.setOnClickListener(new SafeViewOnClickListener() {
            @Override
            public void safeOnClick(View v) {
                addButton();
            }
        });

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
                            mActionView.setText(number);
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
                    mActionView.setText(data.getDoubleExtra("latitude", 0) + "," + data.getDoubleExtra("longitude", 0));
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
        for (Long buttonId : mButtons) {
            CannedButton button = mCannedButtons.getById(buttonId);
            if (button == null)
                continue;
            button.setSelected(true);
        }
        mButtonsListView = (ListView) findViewById(R.id.button_list);
        mCannedButtonAdapter = new CannedButtonAdapter(mCannedButtons);
        mButtonsListView.setAdapter(mCannedButtonAdapter);
        mButtonsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
                try {
                    final CannedButton cannedButton = (CannedButton) view.getTag();
                    cannedButton.setSelected(!cannedButton.isSelected());
                    setButtonItemView(view, cannedButton);
                    if (cannedButton.isSelected()) {
                        mButtons.add(cannedButton.getId());
                    } else {
                        mButtons.remove(cannedButton.getId());
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
                                    mCannedButtonAdapter.notifyDataSetChanged();
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
    }

    private void setButtonItemView(final View view, CannedButton item) {
        TextView captionTextView = (TextView) view.findViewById(R.id.caption);
        TextView actionTextView = (TextView) view.findViewById(R.id.action);
        Resources resources = getResources();
        captionTextView.setTextColor(resources.getColor(android.R.color.primary_text_light));
        actionTextView.setTextColor(resources.getColor(android.R.color.secondary_text_light));
        ImageView statusImageView = (ImageView) view.findViewById(R.id.status);
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

    private void addButton() {
        final View dialog = getLayoutInflater().inflate(R.layout.new_button_dialog, null);
        mCaptionView = (EditText) dialog.findViewById(R.id.button_caption);
        mActionView = (EditText) dialog.findViewById(R.id.button_action);
        final ImageButton actionHelpButton = (ImageButton) dialog.findViewById(R.id.action_help_button);
        final RadioButton noneRadio = (RadioButton) dialog.findViewById(R.id.action_none);
        final RadioButton telRadio = (RadioButton) dialog.findViewById(R.id.action_tel);
        final RadioButton geoRadio = (RadioButton) dialog.findViewById(R.id.action_geo);
        final RadioButton wwwRadio = (RadioButton) dialog.findViewById(R.id.action_www);
        noneRadio.setChecked(true);
        mActionView.setVisibility(View.GONE);
        actionHelpButton.setVisibility(View.GONE);
        final int iconColor = ContextCompat.getColor(mService, R.color.mc_primary_icon);
        actionHelpButton.setImageDrawable(new IconicsDrawable(mService, FontAwesome.Icon.faw_address_book_o).color(iconColor).sizeDp(24));
        noneRadio.setOnClickListener(new SafeViewOnClickListener() {
            @Override
            public void safeOnClick(View v) {
                mActionView.setVisibility(View.GONE);
                actionHelpButton.setVisibility(View.GONE);
            }
        });
        telRadio.setOnClickListener(new SafeViewOnClickListener() {
            @Override
            public void safeOnClick(View v) {
                mActionView.setText("");
                mActionView.setVisibility(View.VISIBLE);
                mActionView.setInputType(InputType.TYPE_CLASS_PHONE);
                actionHelpButton.setVisibility(View.VISIBLE);
                actionHelpButton.setImageDrawable(new IconicsDrawable(mService, FontAwesome.Icon.faw_address_book_o).color(iconColor).sizeDp(24));
            }
        });
        geoRadio.setOnClickListener(new SafeViewOnClickListener() {
            @Override
            public void safeOnClick(View v) {
                mActionView.setText("");
                mActionView.setVisibility(View.VISIBLE);
                mActionView.setInputType(InputType.TYPE_CLASS_TEXT);
                actionHelpButton.setVisibility(View.VISIBLE);
                actionHelpButton.setImageDrawable(new IconicsDrawable(mService, FontAwesome.Icon.faw_map_marker).color(iconColor).sizeDp(24));
            }
        });
        wwwRadio.setOnClickListener(new SafeViewOnClickListener() {
            @Override
            public void safeOnClick(View v) {
                mActionView.setText("http://");
                mActionView.setVisibility(View.VISIBLE);
                mActionView.setInputType(InputType.TYPE_CLASS_TEXT);
                actionHelpButton.setVisibility(View.GONE);
            }
        });
        actionHelpButton.setOnClickListener(new SafeViewOnClickListener() {
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
                        String caption = mCaptionView.getText().toString();
                        if ("".equals(caption.trim())) {
                            UIUtils.showLongToast(SendMessageButtonActivity.this,
                                    getString(R.string.caption_required));
                            return;
                        }

                        CannedButton cannedButton;
                        if (!noneRadio.isChecked()) {
                            String actionText = mActionView.getText().toString();
                            if ("".equals(caption.trim())) {
                                UIUtils.showLongToast(SendMessageButtonActivity.this,
                                        getString(R.string.action_not_valid));
                                return;
                            }
                            if (telRadio.isChecked()) {
                                actionText = "tel://" + actionText;
                            } else if (geoRadio.isChecked()) {
                                actionText = "geo://" + actionText;
                            }

                            Matcher action = actionPattern.matcher(actionText);
                            if (!action.matches()) {
                                UIUtils.showLongToast(SendMessageButtonActivity.this,
                                        getString(R.string.action_not_valid));
                                return;
                            }
                            cannedButton = new CannedButton(caption, "".equals(action.group(2)) ? null : action.group());

                        } else {
                            cannedButton = new CannedButton(caption, null);
                        }

                        mCannedButtons.add(cannedButton);
                        cannedButton.setSelected(true);
                        mCannedButtonAdapter.notifyDataSetChanged();
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
}

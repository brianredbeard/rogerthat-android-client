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

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.net.Uri.Builder;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AlphabetIndexer;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.ViewFlipper;

import com.facebook.AccessToken;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.config.Configuration;
import com.mobicage.rogerthat.plugins.friends.Contact;
import com.mobicage.rogerthat.plugins.friends.FriendsPlugin;
import com.mobicage.rogerthat.plugins.friends.PhoneContacts;
import com.mobicage.rogerthat.plugins.scan.ScanTabActivity;
import com.mobicage.rogerthat.util.FacebookUtils;
import com.mobicage.rogerthat.util.FacebookUtils.PermissionType;
import com.mobicage.rogerthat.util.RegexPatterns;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.sms.SMSManager;
import com.mobicage.rogerthat.util.system.SafeAsyncTask;
import com.mobicage.rogerthat.util.system.SafeBroadcastReceiver;
import com.mobicage.rogerthat.util.system.SafeDialogInterfaceOnClickListener;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.system.SafeViewOnClickListener;
import com.mobicage.rogerthat.util.system.SystemUtils;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.ui.ImageHelper;
import com.mobicage.rogerthat.util.ui.SeparatedListAdapter;
import com.mobicage.rogerthat.util.ui.Slider;
import com.mobicage.rogerthat.util.ui.Slider.Swiper;
import com.mobicage.rogerthat.util.ui.UIUtils;
import com.mobicage.rpc.IncompleteMessageException;
import com.mobicage.rpc.config.AppConstants;
import com.mobicage.rpc.config.CloudConstants;
import com.mobicage.to.friends.FacebookRogerthatProfileMatchTO;
import com.mobicage.to.friends.FindRogerthatUsersViaEmailResponseTO;
import com.mobicage.to.friends.FindRogerthatUsersViaFacebookResponseTO;

import org.json.simple.JSONValue;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddFriendsActivity extends ServiceBoundActivity {

    public static final String CONFIG = "com.mobicage.rogerthat.plugins.friends.ADD_FRIENDS";
    public static final String AB_RESULT = "AB_RESULT";
    public static final String FB_RESULT = "FB_RESULT";
    public static final String FB_POST_ON_WALL = "FB_POST_ON_WALL";

    public static final String INTENT_KEY_LAUNCHINFO = "INTENT_KEY_LAUNCHINFO";
    public static final String INTENT_VALUE_SHOW_CONTACTS = "SHOW_CONTACTS";
    public static final String INTENT_VALUE_SHOW_FACEBOOK = "SHOW_FACEBOOK";

    public static final int MY_PERMISSION_REQUEST_READ_CONTACTS = 1;

    private static final int TYPE_FB_MATCH = 4;
    private static final int TYPE_AB_MATCH = 1;
    private static final int TYPE_MAIL = 2;
    private static final int TYPE_SMS = 3;

    private ViewFlipper mViewFlipper;
    private GestureDetector mGestureScanner;
    private FriendsPlugin mFriendsPlugin;
    private PhoneContacts mPhoneContacts;
    private BroadcastReceiver mBroadcastReceiver;
    private List<String> mPendingInvites;
    private List<String> mCurrentInvites;
    private Configuration mCfg;
    private boolean mFbPageConfigured = false;
    private final Map<String, Bitmap> mFbAvatars = new HashMap<String, Bitmap>();

    private Cursor mCursorEmail;
    private Cursor mPhoneCursor;

    private Contact mCurrentContact = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        T.UI();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_friends);

        final int btnText;
        switch (AppConstants.FRIENDS_CAPTION) {
        case COLLEAGUES:
            btnText = R.string.invite_colleague;
            break;
        case CONTACTS:
            btnText = R.string.invite_contact;
            break;
        case FRIENDS:
        default:
            btnText = R.string.invite_friend;
            break;
        }
        ((Button) findViewById(R.id.add_via_email_button)).setText(btnText);

        mViewFlipper = (ViewFlipper) findViewById(R.id.view_flipper);

        Swiper leftSwiper = new Swiper() {
            @Override
            public Intent onSwipe() {
                int i = mViewFlipper.getDisplayedChild();
                if (i < mViewFlipper.getChildCount()) {
                    displayTab(i + 1);
                }
                return null;
            }
        };
        Swiper rightSwiper = new Swiper() {
            @Override
            public Intent onSwipe() {
                int i = mViewFlipper.getDisplayedChild();
                if (i > 0) {
                    displayTab(i - 1);
                }
                return null;
            }
        };
        mGestureScanner = new GestureDetector(new Slider(this, this, leftSwiper, rightSwiper));

        Intent intent = getIntent();
        if (intent.hasExtra(INTENT_KEY_LAUNCHINFO)) {
            if (intent.getStringExtra(INTENT_KEY_LAUNCHINFO).equals(INTENT_VALUE_SHOW_CONTACTS)) {
                displayTab(0);
            } else if (intent.getStringExtra(INTENT_KEY_LAUNCHINFO).equals(INTENT_VALUE_SHOW_FACEBOOK)) {
                displayTab(1);
            }
        }

        ((TextView) findViewById(R.id.add_via_contacts_description)).setText(getString(
            R.string.add_via_contacts_description, getString(R.string.app_name)));

        ((TextView) findViewById(R.id.add_via_facebook_description)).setText(getString(
            R.string.add_via_facebook_description, getString(R.string.app_name)));
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent e) {
        if (mGestureScanner != null) {
            mGestureScanner.onTouchEvent(e);
        }
        return super.dispatchTouchEvent(e);
    }

    @Override
    public void finish() {
        Configuration cfg = mService.getConfigurationProvider().getConfiguration(CONFIG);
        cfg.put(AB_RESULT, "");
        cfg.put(FB_RESULT, "");
        mService.getConfigurationProvider().updateConfigurationNow(CONFIG, cfg);

        super.finish();
    }

    protected MyIdentity getMyIdentity() {
        return mService.getIdentityStore().getIdentity();
    }

    private BroadcastReceiver getBroadcastReceiver() {
        T.UI();
        return new SafeBroadcastReceiver() {
            @Override
            public String[] onSafeReceive(Context context, Intent intent) {
                T.UI();
                if (FriendsPlugin.ADDRESSBOOK_SCANNED_INTENT.equals(intent.getAction())) {
                    mCfg = mService.getConfigurationProvider().getConfiguration(CONFIG);
                    configureContactsView();
                } else if (FriendsPlugin.ADDRESSBOOK_SCAN_FAILED_INTENT.equals(intent.getAction())) {
                    displayContactsPage(3, null);
                } else if (FriendsPlugin.FACEBOOK_SCANNED_INTENT.equals(intent.getAction())) {
                    mFbPageConfigured = false;
                    mCfg = mService.getConfigurationProvider().getConfiguration(CONFIG);
                    configureFacebookView();
                } else if (FriendsPlugin.FACEBOOK_SCAN_FAILED_INTENT.equals(intent.getAction())) {
                    displayContactsPage(3, getString(R.string.error_find_from_facebook, getString(R.string.app_name)));
                }
                return new String[] { intent.getAction() }; // ignore intents (with this action) older than this one
            };
        };
    }

    @Override
    protected void onServiceBound() {
        T.UI();
        mFriendsPlugin = mService.getPlugin(FriendsPlugin.class);
        mPhoneContacts = new PhoneContacts(getContentResolver());
        mPendingInvites = mFriendsPlugin.getStore().getPendingInvitations();
        mCurrentInvites = new ArrayList<String>();
        mCfg = mService.getConfigurationProvider().getConfiguration(CONFIG);
        configureTabs();
        configureContactsView();
        configureFacebookView();
        configureQrScanView();
        configureMailView();

        mBroadcastReceiver = getBroadcastReceiver();

        IntentFilter filter = new IntentFilter();
        filter.addAction(FriendsPlugin.ADDRESSBOOK_SCANNED_INTENT);
        filter.addAction(FriendsPlugin.ADDRESSBOOK_SCAN_FAILED_INTENT);
        filter.addAction(FriendsPlugin.FACEBOOK_SCANNED_INTENT);
        filter.addAction(FriendsPlugin.FACEBOOK_SCAN_FAILED_INTENT);
        registerReceiver(mBroadcastReceiver, filter);

        setTitle(getNavigationBarTitle());
    }

    private int getNavigationBarTitle() {
        switch (AppConstants.FRIENDS_CAPTION) {
        case COLLEAGUES:
            return R.string.find_colleagues;
        case CONTACTS:
            return R.string.find_contacts;
        case FRIENDS:
        default:
            return R.string.invite_friends_short;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (getWasPaused() && mCursorEmail != null) {
            startManagingCursor(mCursorEmail);
        }
        if (getWasPaused() && mPhoneCursor != null) {
            startManagingCursor(mPhoneCursor);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mCursorEmail != null) {
            stopManagingCursor(mCursorEmail);
        }
        if (mPhoneCursor != null) {
            stopManagingCursor(mPhoneCursor);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onServiceUnbound() {
        T.UI();
        unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    public void onConfigurationChanged(android.content.res.Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        int qrPage = 2;
        boolean qrPageShown = mViewFlipper.getDisplayedChild() == qrPage;
        mViewFlipper.removeViewAt(qrPage);
        mViewFlipper.addView(getLayoutInflater().inflate(R.layout.add_friends_via_qr_scan, null), qrPage);
        configureQrScanView();
        if (qrPageShown) {
            mViewFlipper.setDisplayedChild(qrPage);
        }
    }

    private void displayTab(final int tab) {
        mViewFlipper.setDisplayedChild(tab);

        final int[] indicators = new int[] { R.id.contacts_indicator, R.id.facebook_indicator, R.id.qr_scan_indicator,
            R.id.mail_indicator };
        for (int i = 0; i < indicators.length; i++) {
            findViewById(indicators[i]).setVisibility(i == tab ? View.VISIBLE : View.INVISIBLE);
        }
    }

    private void configureTabs() {
        T.UI();
        final int[] views = new int[] { R.id.contacts_layout, R.id.facebook_layout, R.id.qr_scan_layout,
            R.id.mail_layout };

        for (int v = 0; v < views.length; v++) {
            final int x = v;
            if (views[v] == R.id.facebook_layout && AppConstants.FACEBOOK_APP_ID == null) {
                findViewById(views[v]).setVisibility(View.GONE);
                continue;
            }
            findViewById(views[v]).setOnClickListener(new SafeViewOnClickListener() {
                @Override
                public void safeOnClick(View v) {
                    if (x != mViewFlipper.getDisplayedChild()) {
                        displayTab(x);
                        if (views[x] == R.id.facebook_layout && !mFbPageConfigured) {
                            configureFacebookView();
                        }
                    }
                }
            });
        }
    }

    private void configureContactsView() {
        T.UI();
        String jsonResult = null;
        if (mCfg != null) {
            jsonResult = mCfg.get(AB_RESULT, "");
        }

        if ("".equals(jsonResult)) {
            displayContactsPage(0, null);
        } else {
            displayContactsPage(2, jsonResult);
        }
    }

    private void displayContactsPage(int child, Object context) {
        T.UI();
        SafeViewOnClickListener btnOnClickListener = new SafeViewOnClickListener() {
            @Override
            public void safeOnClick(final View v) {
                if (!mService.isPermitted(Manifest.permission.READ_CONTACTS)) {
                    ActivityCompat.requestPermissions(AddFriendsActivity.this, new String[]{Manifest.permission.READ_CONTACTS},
                            MY_PERMISSION_REQUEST_READ_CONTACTS);
                    return;
                }

                if (mFriendsPlugin.findRogerthatUsersViaAddressBook()) {
                    displayContactsPage(1, null);
                } else {
                    displayContactsPage(3, null);
                }
            }
        };

        switch (child) {
        case 0:
            ((Button) findViewById(R.id.add_via_contacts_button)).setOnClickListener(btnOnClickListener);
            break;
        case 1: // Static page with only text and spinner
            break;
        case 2:
            displayContactsResult((String) context);
            break;
        case 3:
            ((Button) findViewById(R.id.add_via_contacts_try_again)).setOnClickListener(btnOnClickListener);
            break;
        default:
            break;
        }
        ((ViewFlipper) findViewById(R.id.add_via_contacts_view_flipper)).setDisplayedChild(child);
    }

    private void displayContactsResult(String jsonResult) {
        T.UI();
        UIUtils.cancelNotification(this, R.integer.ab_scan_finished);

        @SuppressWarnings("unchecked")
        Map<String, Object> responseMap = (Map<String, Object>) JSONValue.parse(jsonResult);
        FindRogerthatUsersViaEmailResponseTO responseTO;
        try {
            responseTO = new FindRogerthatUsersViaEmailResponseTO(responseMap);
        } catch (IncompleteMessageException e) {
            L.bug(e);
            displayContactsPage(3, null);
            return;
        }

        // Get the e-mails of my Rogerthat friends
        List<String> rtFriendEmails = mFriendsPlugin.getStore().getEmails();
        rtFriendEmails.add(getMyIdentity().getEmail());

        // Get list of Contacts for the matched results
        List<Contact> matches = new ArrayList<Contact>(responseTO.matched_addresses.length);
        for (String email : responseTO.matched_addresses) {
            if (rtFriendEmails.contains(email))
                continue; // We are already friend with the found match

            Cursor c = mPhoneContacts.getContactByEmail(email);
            if (c == null) {
                L.d("getContactByEmail cursor is null!");
                continue;
            }

            try {
                if (c.moveToFirst()) {
                    Contact contact = mPhoneContacts.fromCursor(c);
                    matches.add(contact);
                }
            } finally {
                c.close();
            }
        }

        // Get contact the IDs for my Rogerthat friends (they do not have to be shown in the list)
        List<Integer> rtFriendInContacts = new ArrayList<Integer>();
        for (String email : rtFriendEmails) {
            Cursor c = mPhoneContacts.getContactByEmail(email);
            if (c == null) {
                L.d("getContactByEmail cursor is null!");
                continue;
            }

            try {
                if (c.moveToFirst()) {
                    Contact contact = mPhoneContacts.fromCursor(c);
                    L.d("Rogerthat friend " + email + " also found in contacts");
                    rtFriendInContacts.add(contact.id);
                }
            } finally {
                c.close();
            }
        }

        SeparatedListAdapter adapter = new SeparatedListAdapter(this);

        String resultTitle;
        final int l = responseTO.matched_addresses.length;
        if (l == 0) {
            resultTitle = getString(R.string.contacts_found_none, getString(R.string.app_name));
        } else if (l == 1) {
            resultTitle = getString(R.string.contacts_found_1, getString(R.string.app_name));
        } else {
            resultTitle = getString(R.string.contacts_found_more, l, getString(R.string.app_name));
        }

        ContactsListAdapter matchesAdapter = new ContactsListAdapter(matches, TYPE_AB_MATCH, R.string.add,
            R.string.sent);
        adapter.addSection(resultTitle, matchesAdapter);

        String[] rogerthatEmails = rtFriendEmails.toArray(new String[rtFriendEmails.size()]);
        mCursorEmail = mPhoneContacts.getContactsPerEmail(rogerthatEmails, rtFriendInContacts);
        startManagingCursor(mCursorEmail);

        if (mCursorEmail != null && mCursorEmail.getCount() > 0) {
            ContactsCursorAdapter adapter2 = new ContactsCursorAdapter(mCursorEmail, TYPE_MAIL, R.string.invite,
                R.string.invited);
            final int text;
            switch (AppConstants.FRIENDS_CAPTION) {
            case COLLEAGUES:
                text = R.string.invite_colleagues_via_email;
                break;
            case CONTACTS:
                text = R.string.invite_contacts_via_email;
                break;
            case FRIENDS:
            default:
                text = R.string.invite_via_email;
                break;
            }
            adapter.addSection(getString(text), adapter2);
        } else if (mCursorEmail == null) {
            L.d("getContactsPerEmail cursor is null!");
        }

        if (SystemUtils.getActionDialIntent(this) != null) {
            mPhoneCursor = mPhoneContacts.getContactsPerPhone(rtFriendInContacts);
            startManagingCursor(mPhoneCursor);

            if (mPhoneCursor != null && mPhoneCursor.getCount() > 0) {
                ContactsCursorAdapter adapter2 = new ContactsCursorAdapter(mPhoneCursor, TYPE_SMS, R.string.invite,
                    R.string.invited);
                final int text;
                switch (AppConstants.FRIENDS_CAPTION) {
                case COLLEAGUES:
                    text = R.string.invite_colleagues_via_sms;
                    break;
                case CONTACTS:
                    text = R.string.invite_contacts_via_sms;
                    break;
                case FRIENDS:
                default:
                    text = R.string.invite_via_sms;
                    break;
                }
                adapter.addSection(getString(text), adapter2);
            } else if (mPhoneCursor == null) {
                L.d("getContactsPerPhone cursor is null!");
            }
        }

        ListView list = (ListView) findViewById(R.id.add_via_contacts_result);
        list.setAdapter(adapter);
    }

    private void configureFacebookView() {
        T.UI();
        if (!mFbPageConfigured) {
            String jsonResult = null;
            if (mCfg != null) {
                jsonResult = mCfg.get(FB_RESULT, "");
            }

            if ("".equals(jsonResult)) {
                displayFacebookPage(0, null);
                mFbPageConfigured = true;
            } else if (mViewFlipper.getDisplayedChild() == 1) {
                displayFacebookPage(2, jsonResult);
                mFbPageConfigured = true;
            }
        }
    }

    private void displayFacebookErrorPage(Throwable e) {
        T.UI();
        if (e != null)
            L.bug(e);
        displayFacebookPage(3, getString(R.string.error_load_facebook_friends));
    }

    private void findRogerthatUsersViaFacebook() {
        T.UI();
        if (mFriendsPlugin.findRogerthatUsersViaFacebook(AccessToken.getCurrentAccessToken().getToken())) {
            displayFacebookPage(1, null);
        } else {
            displayFacebookPage(3, getString(R.string.error_find_from_facebook, getString(R.string.app_name)));
        }
    }

    private void askToPostOnWall() {
        AlertDialog.Builder builder = new AlertDialog.Builder(AddFriendsActivity.this);
        builder.setMessage(getString(R.string.fb_ask_post_on_wall, getString(R.string.app_name)));
        builder.setPositiveButton(R.string.post, new SafeDialogInterfaceOnClickListener() {
            @Override
            public void safeOnClick(DialogInterface dialog, int which) {
                if (!mService.getNetworkConnectivityManager().isConnected()) {
                    UIUtils.showNoNetworkDialog(AddFriendsActivity.this);
                    return;
                }
                postOnWall();
                mCfg.put(FB_POST_ON_WALL, false);
                mService.getConfigurationProvider().updateConfigurationNow(CONFIG, mCfg);
            }
        });
        builder.setNegativeButton(R.string.cancel, new SafeDialogInterfaceOnClickListener() {
            @Override
            public void safeOnClick(DialogInterface dialog, int which) {
                findRogerthatUsersViaFacebook();
                mCfg.put(FB_POST_ON_WALL, false);
                mService.getConfigurationProvider().updateConfigurationNow(CONFIG, mCfg);
            }
        });
        builder.create().show();
    }

    private void postOnWall() {
        String myEmailHash = new String(getMyIdentity().getEmailHash());
        String picture = CloudConstants.HTTPS_BASE_URL + "/invite?code=" + myEmailHash;
        Uri identityUri = Uri.parse(getMyIdentity().getShortLink());
        Builder b = identityUri.buildUpon();
        b.appendQueryParameter("target", "fbwall");
        b.appendQueryParameter("from", "phone");
        Uri link = b.build();
        String caption = getString(R.string.fb_wall_post_caption, getString(R.string.app_name));
        String description = getString(R.string.fb_wall_post_description, getString(R.string.app_name));

        final ShareLinkContent.Builder contentBuilder = new ShareLinkContent.Builder();
        contentBuilder.setContentUrl(link);
        contentBuilder.setImageUrl(Uri.parse(picture));
        contentBuilder.setContentTitle(caption);
        contentBuilder.setContentDescription(description);
        final ShareLinkContent content = contentBuilder.build();

        if (CloudConstants.DEBUG_LOGGING) {
            Map<String, String> params = new HashMap<>();
            params.put("content.contentDescription", content.getContentDescription());
            params.put("content.contentTitle", content.getContentTitle());
            params.put("content.contentURL", content.getContentUrl().toString());
            params.put("content.imageURL", content.getImageUrl().toString());
            L.d(params.toString());
        }

        final ShareDialog shareDialog = new ShareDialog(AddFriendsActivity.this);
        shareDialog.show(content, ShareDialog.Mode.AUTOMATIC);
        shareDialog.registerCallback(getFacebookCallbackManager(), new FacebookCallback<Sharer.Result>() {
            @Override
            public void onSuccess(Sharer.Result result) {
                findRogerthatUsersViaFacebook();
            }

            @Override
            public void onCancel() {
                findRogerthatUsersViaFacebook();
            }

            @Override
            public void onError(FacebookException error) {
                findRogerthatUsersViaFacebook();
            }
        });
    }

    private void displayFacebookPage(final int child, final Object context) {
        T.UI();
        SafeViewOnClickListener btnOnclickListener = new SafeViewOnClickListener() {
            @Override
            public void safeOnClick(final View v) {
                FacebookUtils.ensureOpenSession(AddFriendsActivity.this, Arrays.asList("email", "user_friends"),
                        PermissionType.READ, new FacebookCallback<LoginResult>() {
                            @Override
                            public void onSuccess(LoginResult loginResult) {
                                if (getMyIdentity().getShortLink() != null && mCfg.get(FB_POST_ON_WALL, true)) {
                                    askToPostOnWall();
                                } else {
                                    findRogerthatUsersViaFacebook();
                                }
                            }

                            @Override
                            public void onCancel() {
                            }

                            @Override
                            public void onError(FacebookException error) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(AddFriendsActivity.this);
                                builder.setCancelable(true);
                                builder.setMessage(R.string.error_recommend_on_fb);
                                builder.setPositiveButton(R.string.rogerthat, null);
                                builder.create().show();
                            }
                        }, true);
            }
        };

        switch (child) {
        case 0:
            findViewById(R.id.add_via_facebook_button).setOnClickListener(btnOnclickListener);
            break;

        case 1: // Static page with only text and spinner
            break;
        case 2:
            displayFacebookResult((String) context);
            break;
        case 3:
            ((TextView) findViewById(R.id.add_via_facebook_error)).setText((String) context);
            findViewById(R.id.add_via_facebook_try_again).setOnClickListener(btnOnclickListener);
            break;
        default:
            break;
        }
        ((ViewFlipper) findViewById(R.id.add_via_facebook_view_flipper)).setDisplayedChild(child);
    }

    @SuppressWarnings("unchecked")
    private void displayFacebookResult(final String jsonResult) {
        T.UI();
        UIUtils.cancelNotification(this, R.integer.fb_scan_finished);

        Map<String, Object> responseMap = (Map<String, Object>) JSONValue.parse(jsonResult);
        final FindRogerthatUsersViaFacebookResponseTO responseTO;
        try {
            responseTO = new FindRogerthatUsersViaFacebookResponseTO(responseMap);
        } catch (IncompleteMessageException e) {
            displayFacebookErrorPage(e);
            return;
        }

        final List<Contact> matches = new ArrayList<Contact>();

        for (FacebookRogerthatProfileMatchTO match : responseTO.matches) {
            matches.add(new FacebookContact(match));
        }
        displayFacebookResultListView(matches);
    }

    private void displayFacebookResultListView(List<Contact> matches) {
        T.UI();
        findViewById(R.id.add_via_facebook_spinner).setVisibility(View.GONE);
        ListView listView = (ListView) findViewById(R.id.add_via_facebook_result);
        listView.setVisibility(View.VISIBLE);

        SeparatedListAdapter adapter = new SeparatedListAdapter(this);

        String resultTitle;
        final int l = matches.size();
        if (l == 0) {
            resultTitle = getString(R.string.fb_friends_found_none, getString(R.string.app_name));
        } else if (l == 1) {
            resultTitle = getString(R.string.fb_friends_found_1, getString(R.string.app_name));
        } else {
            resultTitle = getString(R.string.fb_friends_found_more, l, getString(R.string.app_name));
        }

        ContactsListAdapter matchesAdapter = new ContactsListAdapter(matches, TYPE_FB_MATCH, R.string.add,
            R.string.sent);
        adapter.addSection(resultTitle, matchesAdapter);

        listView.setAdapter(adapter);
    }

    private void configureQrScanView() {
        T.UI();
        final Button btn = (Button) findViewById(R.id.add_via_qr_scan_button);
        btn.setText(getString(R.string.scan_passport, getString(R.string.app_name)));
        btn.setOnClickListener(new SafeViewOnClickListener() {
            @Override
            public void safeOnClick(View v) {
                Intent i = new Intent(AddFriendsActivity.this, ScanTabActivity.class);
                i.setAction(ScanTabActivity.START_SCANNER_INTENT_ACTION);
                startActivity(i);
            }
        });

        final ImageView imgView = (ImageView) findViewById(R.id.my_qr);
        imgView.setImageBitmap(mService.getIdentityStore().getIdentity().getQRBitmap());
    }

    private void configureMailView() {
        T.UI();
        final AutoCompleteTextView emailText = (AutoCompleteTextView) findViewById(R.id.add_via_email_text_field);
        emailText.setAdapter(new ArrayAdapter<String>(this, R.layout.list_item, new ArrayList<String>()));
        emailText.setThreshold(1);

        if (mService.isPermitted(Manifest.permission.READ_CONTACTS)) {
            mService.postAtFrontOfBIZZHandler(new SafeRunnable() {

                @SuppressWarnings("unchecked")
                @Override
                protected void safeRun() throws Exception {
                    L.d("AddFriendsActivity getEmailAddresses");
                    List<String> emailList = ContactListHelper.getEmailAddresses(AddFriendsActivity.this);
                    ArrayAdapter<String> a = (ArrayAdapter<String>) emailText.getAdapter();
                    for (int i = 0; i < emailList.size(); i++) {
                        a.add(emailList.get(i));
                    }
                    a.notifyDataSetChanged();
                    L.d("AddFriendsActivity gotEmailAddresses");
                }
            });
        }

        final SafeViewOnClickListener onClickListener = new SafeViewOnClickListener() {
            @Override
            public void safeOnClick(View v) {
                String email = emailText.getText().toString().trim();
                if (RegexPatterns.EMAIL.matcher(email).matches()) {
                    if (mFriendsPlugin.inviteFriend(email, null, null, true)) {
                        emailText.setText(null);
                        UIUtils.hideKeyboard(AddFriendsActivity.this, emailText);
                    } else {
                        UIUtils.showLongToast(AddFriendsActivity.this, getString(R.string.friend_invite_failed));
                    }
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(AddFriendsActivity.this);
                    builder.setMessage(R.string.registration_email_not_valid);
                    builder.setPositiveButton(R.string.rogerthat, null);
                    builder.create().show();
                }
            }
        };
        ((Button) findViewById(R.id.add_via_email_button)).setOnClickListener(onClickListener);

        emailText.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE
                        || (event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                    onClickListener.onClick(view);
                    return true;
                }
                return false;
            }
        });
    }

    private void inviteViaEmail(final Contact contact) {
        T.UI();
        mFriendsPlugin.inviteFriend(contact.email, null, contact.getDisplayName(), true);
    }

    private void inviteViaSMS(final Contact contact) {
        T.UI();
        MyIdentity identity = mService.getIdentityStore().getIdentity();
        String shortLink = identity.getShortLink();
        String installLink = CloudConstants.HTTPS_BASE_URL + CloudConstants.INSTALL_URL + "?a=" + AppConstants.APP_ID;
        String body;
        if (shortLink == null) {
            body = getString(R.string.friend_sms_invitation_no_url, contact.name, installLink,
                getString(R.string.app_name));
        } else {
            Uri shortLinkUri = Uri.parse(shortLink);
            String shortLinkLC = shortLinkUri.getScheme().toLowerCase() + "://" + shortLinkUri.getHost().toLowerCase();
            shortLink = shortLinkLC + shortLink.substring(shortLinkLC.length());

            String secret = mFriendsPlugin.popInvitationSecret();
            if (secret != null) {
                shortLink += "?s=" + secret;
                mFriendsPlugin.logInvitationSecretSent(secret, contact.email);
            }

            body = getString(R.string.friend_sms_invitation, contact.name, installLink, shortLink,
                getString(R.string.app_name));
        }
        final SMSManager smsManager = new SMSManager(this);
        try {
            smsManager.sendSMS(contact.primaryPhoneNumber, body);
        } finally {
            smsManager.close();
        }
        UIUtils.showLongToast(mService, mService.getString(R.string.invitation_sent_successfully));

        mFriendsPlugin.getStore().insertPendingInvitation(contact.primaryPhoneNumber);
    }

    private Bitmap getAvatar(final Contact c, int type) {
        Bitmap avatar;
        switch (type) {
        case TYPE_AB_MATCH:
        case TYPE_SMS:
        case TYPE_MAIL:
            avatar = mPhoneContacts.getAvatar(c);
            break;
        case TYPE_FB_MATCH:
            final FacebookContact fbc = (FacebookContact) c;
            if (mFbAvatars.containsKey(fbc.email)) {
                avatar = mFbAvatars.get(fbc.email);
            } else {
                avatar = null;
                // Load avatar in the background
                L.d("Downloading fb avatar: " + fbc.pictureUrl);
                mFbAvatars.put(fbc.email, null);
                new SafeAsyncTask<Object, Object, Object>() {
                    @Override
                    protected Object safeDoInBackground(Object... params) {
                        try {
                            return BitmapFactory.decodeStream((InputStream) new URL((String) params[0]).getContent());
                        } catch (MalformedURLException e) {
                            L.bug("Could not download Facebook avatar: " + fbc.pictureUrl, e);
                        } catch (IOException e) {
                            L.bug("Could not download Facebook avatar: " + fbc.pictureUrl, e);
                        } catch (Exception e) {
                            L.bug("Could not download Facebook avatar: " + fbc.pictureUrl, e);
                        }
                        return null;
                    };

                    @Override
                    protected void safeOnPostExecute(Object result) {
                        Bitmap bitmap = (Bitmap) result;
                        if (bitmap != null) {
                            mFbAvatars.put(fbc.email, bitmap);
                            ListView listView = (ListView) findViewById(R.id.add_via_facebook_result);
                            BaseAdapter adapter = (BaseAdapter) listView.getAdapter();
                            adapter.notifyDataSetChanged();
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
                }.execute(fbc.pictureUrl);
            }
            break;
        default:
            avatar = null;
            break;
        }
        if (avatar == null)
            avatar = mFriendsPlugin.getMissingFriendAvatarBitmap();
        else
            avatar = ImageHelper.getRoundedCornerAvatar(avatar);
        return avatar;
    }

    private void setContactOnView(final Contact c, final View v, final int type, final int btnDisabledText,
        final int btnEnabledText) {
        Bitmap avatar = getAvatar(c, type);

        ((ImageView) v.findViewById(R.id.avatar)).setImageBitmap(avatar);

        ((TextView) v.findViewById(R.id.name)).setText(c.name);

        final TextView contactMethod = (TextView) v.findViewById(R.id.secondary_label);
        contactMethod.setVisibility(type == TYPE_FB_MATCH ? View.GONE : View.VISIBLE);
        contactMethod.setText(type == TYPE_SMS ? c.primaryPhoneNumber : c.email);

        final Button btn = (Button) v.findViewById(R.id.invite_button);
        btn.setText(mPendingInvites.contains(c.email) ? btnDisabledText : btnEnabledText);
        btn.setEnabled(!mCurrentInvites.contains(c.email));
        btn.setOnClickListener(new SafeViewOnClickListener() {

            @Override
            public void safeOnClick(View v) {
                btn.setEnabled(false);
                btn.setText(btnDisabledText);
                if (type == TYPE_SMS) {
                    if (!mService.isPermitted(Manifest.permission.SEND_SMS)) {
                        mCurrentContact = c;
                        ActivityCompat.requestPermissions(AddFriendsActivity.this, new String[]{Manifest.permission.SEND_SMS}, SMSManager.MY_PERMISSIONS_REQUEST_SMS);
                        return;
                    }
                    inviteViaSMS(c);
                } else {
                    inviteViaEmail(c);
                }
                mPendingInvites.add(c.email);
                mCurrentInvites.add(c.email);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        T.UI();
        if (requestCode == SMSManager.MY_PERMISSIONS_REQUEST_SMS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                inviteViaSMS(mCurrentContact);
                mCurrentContact = null;
            }
        } else if (requestCode == MY_PERMISSION_REQUEST_READ_CONTACTS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mFriendsPlugin.findRogerthatUsersViaAddressBook()) {
                    displayContactsPage(1, null);
                } else {
                    displayContactsPage(3, null);
                }
            }
        }
    }

    private class ContactsCursorAdapter extends CursorAdapter implements SectionIndexer {

        private final int mType;
        private final int mBtnEnabledText;
        private final int mBtnDisabledText;
        private final AlphabetIndexer mAlphaIndexer;

        public ContactsCursorAdapter(Cursor c, int type, int btnEnabledText, int btnDisabledText) {
            super(AddFriendsActivity.this, c, false);
            mType = type;
            mBtnEnabledText = btnEnabledText;
            mBtnDisabledText = btnDisabledText;
            mAlphaIndexer = new AlphabetIndexer(c, 1, "ABCDEFGHIJKLMNOPQRSTUVWXYZ");
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
        public View getView(int position, View convertView, ViewGroup parent) {
            T.UI();
            View v = convertView;
            if (v == null) {
                v = getLayoutInflater().inflate(R.layout.add_friends_list_item, parent, false);
            }

            getCursor().moveToPosition(position);
            final Contact c = mPhoneContacts.fromCursor(getCursor());

            setContactOnView(c, v, mType, mBtnDisabledText, mBtnEnabledText);
            return v;
        }

        @Override
        public int getPositionForSection(int section) {
            return mAlphaIndexer.getPositionForSection(section);
        }

        @Override
        public int getSectionForPosition(int position) {
            return mAlphaIndexer.getSectionForPosition(position);
        }

        @Override
        public Object[] getSections() {
            return mAlphaIndexer.getSections();
        }

    }

    private class ContactsListAdapter extends BaseAdapter {

        private final List<Contact> mContacts;
        private final int mType;
        private final int mBtnEnabledText;
        private final int mBtnDisabledText;

        public ContactsListAdapter(List<Contact> contacts, int type, int btnEnabledText, int btnDisabledText) {
            mContacts = contacts;
            mType = type;
            mBtnEnabledText = btnEnabledText;
            mBtnDisabledText = btnDisabledText;
        }

        @Override
        public int getCount() {
            T.UI();
            return mContacts.size();
        }

        @Override
        public Object getItem(int position) {
            T.UI();
            return null;
        }

        @Override
        public long getItemId(int position) {
            T.UI();
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            T.UI();
            View v = convertView;
            if (v == null) {
                v = getLayoutInflater().inflate(R.layout.add_friends_list_item, parent, false);
            }

            final Contact c = mContacts.get(position);

            setContactOnView(c, v, mType, mBtnDisabledText, mBtnEnabledText);
            return v;
        }
    }

    private class FacebookContact extends Contact {

        public String pictureUrl;

        public FacebookContact(FacebookRogerthatProfileMatchTO matchTO) {
            this.email = matchTO.rtId;
            this.name = matchTO.fbName;
            this.pictureUrl = matchTO.fbPicture;
        }
    }
}

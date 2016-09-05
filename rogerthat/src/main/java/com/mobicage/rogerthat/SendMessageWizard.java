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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.jivesoftware.smack.util.Base64;

import android.content.Context;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.config.Configuration;
import com.mobicage.rogerthat.config.ConfigurationProvider;
import com.mobicage.rogerthat.plugins.messaging.Message;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.pickle.PickleException;
import com.mobicage.rogerthat.util.pickle.Pickler;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.ui.Wizard;

public class SendMessageWizard extends Wizard {

    private static final class Persister implements Wizard.Persister {
        private final ConfigurationProvider cfgProvider;

        private Persister(ConfigurationProvider cfgProvider) {
            this.cfgProvider = cfgProvider;
        }

        @Override
        public void save(Wizard wiz) {
            save(wiz, null);
        }

        public void save(Wizard wiz, String instance) {
            T.UI();
            String serializedWizard;
            try {
                serializedWizard = Base64.encodeBytes(Pickler.getPickleFromObject(wiz));
            } catch (PickleException e) {
                L.bug(e);
                return;
            }
            Configuration cfg = new Configuration();
            String cfgItem = CONFIG_PICKLED_WIZARD_KEY;
            if (instance != null)
                cfgItem += instance;
            cfg.put(cfgItem, serializedWizard);
            cfgProvider.updateConfigurationNow(CONFIGKEY, cfg);
            ((SendMessageWizard) wiz).getCannedButtons().save(cfgProvider);
        }

        @Override
        public void clear(Wizard wiz) {
            // no need to clear
        }
    }

    public static final String CONFIGKEY = "SEND_NEW_MESSAGE_WIZARD";
    public static final String CONFIG_PICKLED_WIZARD_KEY = "CANNED_MESSAGE";
    static final String CANNED_BUTTONS = "CANNED_BUTTONS";

    public static final int TO = 1;
    public static final int BCC = 2;
    public static final long NO_BUTTON_SELECTED = -1;

    private Set<String> mRecipients = new LinkedHashSet<String>();
    private Set<String> mGroupRecipients = new LinkedHashSet<String>();
    private int mRecipientStyle = TO;
    private String mMessage = "";
    private CannedButtons mCannedButtons = null;
    private Set<Long> mButtons = new LinkedHashSet<Long>();
    private long mSelectedButton = NO_BUTTON_SELECTED;

    private boolean mHasImageSelected = false;
    private boolean mHasVideoSelected = false;
    private String mUploadFileExtenstion = null;
    private File mTmpUploadFile = null;

    private long mPriority = Message.PRIORITY_NORMAL;
    private boolean mIsSticky = false;

    public static SendMessageWizard getWizard(final Context context, final ConfigurationProvider cfgProvider,
        final String instance, boolean forceNewWizard) {
        T.UI();
        SendMessageWizard wiz = null;
        final Configuration cfg = cfgProvider.getConfiguration(CONFIGKEY);
        if (!forceNewWizard) {
            String cfgItem = CONFIG_PICKLED_WIZARD_KEY;
            if (instance != null)
                cfgItem += instance;
            final String serializedWizard = cfg.get(cfgItem, "");
            if (!"".equals(serializedWizard)) {
                try {
                    wiz = (SendMessageWizard) Pickler.createObjectFromPickle(Base64.decode(serializedWizard));
                } catch (PickleException e) {
                    L.bug(e);
                }
            }
        }

        if (wiz == null)
            wiz = new SendMessageWizard();

        wiz.setPersister(new Persister(cfgProvider));

        loadCannedButtons(context, cfgProvider, wiz, cfg);

        return wiz;
    }

    public static Set<String> getCannedMessages(final ConfigurationProvider cfgProvider) {
        T.UI();
        final Configuration cfg = cfgProvider.getConfiguration(CONFIGKEY);
        Set<String> canned = new HashSet<String>();
        int length = CONFIG_PICKLED_WIZARD_KEY.length();
        for (String can : cfg.getStringKeys()) {
            if (can.length() > length && can.startsWith(CONFIG_PICKLED_WIZARD_KEY)) {
                canned.add(can.substring(length));
            }
        }
        return canned;
    }

    @Override
    public void writePickle(DataOutput out) throws IOException {
        T.UI();
        super.writePickle(out);
        out.writeInt(2); // version
        out.writeInt(mRecipients.size());
        for (String recipient : mRecipients) {
            out.writeUTF(recipient);
        }
        out.writeInt(mRecipientStyle);
        out.writeUTF(mMessage);
        out.writeInt(mButtons.size());
        for (Long buttonId : mButtons) {
            out.writeLong(buttonId.longValue());
        }
        out.writeLong(mSelectedButton);
        out.writeInt(mGroupRecipients.size());
        for (String group : mGroupRecipients) {
            out.writeUTF(group);
        }
    }

    @Override
    public void readFromPickle(int version, DataInput in) throws IOException, PickleException {
        T.UI();
        super.readFromPickle(version, in);
        int v = in.readInt(); // version
        mRecipients.clear();
        int recipientCount = in.readInt();
        for (int i = 0; i < recipientCount; i++) {
            mRecipients.add(in.readUTF());
        }
        mRecipientStyle = in.readInt();
        mMessage = in.readUTF();
        int buttonCount = in.readInt();
        for (int i = 0; i < buttonCount; i++) {
            mButtons.add(in.readLong());
        }
        mSelectedButton = in.readLong();
        if (v > 1) {
            int groupCount = in.readInt();
            for (int i = 0; i < groupCount; i++) {
                mGroupRecipients.add(in.readUTF());
            }
        }
    }

    public void save(String name) {
        ((Persister) getPersister()).save(this, name);
    }

    public Set<String> getRecipients() {
        return mRecipients;
    }

    public Set<String> getGroupRecipients() {
        return mGroupRecipients;
    }

    public void setRecipientStyle(int recipientStyle) {
        if (!(recipientStyle == TO || recipientStyle == BCC))
            throw new IllegalArgumentException();
        this.mRecipientStyle = recipientStyle;
        this.save();
    }

    public int getRecipientStyle() {
        return mRecipientStyle;
    }

    public void setMessage(String message) {
        this.mMessage = message;
        this.save();
    }

    public String getMessage() {
        return mMessage;
    }

    public Set<Long> getButtons() {
        return mButtons;
    }

    public CannedButtons getCannedButtons() {
        return mCannedButtons;
    }

    public void setSelectedButton(long mSelectedButton) {
        this.mSelectedButton = mSelectedButton;
    }

    public long getSelectedButton() {
        return mSelectedButton;
    }

    public void setHasImageSelected(boolean hasImageSelected) {
        this.mHasImageSelected = hasImageSelected;
    }

    public boolean getHasImageSelected() {
        return mHasImageSelected;
    }

    public String getUploadFileExtenstion() {
        return mUploadFileExtenstion;
    }

    public void setUploadFileExtenstion(String extenstion) {
        this.mUploadFileExtenstion = extenstion;
    }

    public File getTmpUploadFile() {
        return mTmpUploadFile;
    }

    public void setTmpUploadFile(File f) {
        this.mTmpUploadFile = f;
    }

    public void setHasVideoSelected(boolean hasVideoSelected) {
        this.mHasVideoSelected = hasVideoSelected;
    }

    public boolean getHasVideoSelected() {
        return mHasVideoSelected;
    }

    public void setPriority(long priority) {
        this.mPriority = priority;
    }

    public long getPriority() {
        return mPriority;
    }

    public void setIsSticky(boolean isSticky) {
        this.mIsSticky = isSticky;
    }

    public boolean getIsSticky() {
        return mIsSticky;
    }

    private static void loadCannedButtons(final Context context, final ConfigurationProvider cfgProvider,
        SendMessageWizard wiz, final Configuration cfg) {
        final String serializedButtons = cfg.get(CANNED_BUTTONS, "");
        if (!"".equals(serializedButtons)) {
            try {
                wiz.mCannedButtons = (CannedButtons) Pickler.createObjectFromPickle(Base64.decode(serializedButtons));
            } catch (PickleException e) {
                L.bug(e);
            }
        }
        if (wiz.mCannedButtons == null) {
            wiz.mCannedButtons = new CannedButtons();
            wiz.mCannedButtons.add(new CannedButton(1, context.getString(R.string.yes), null, -1));
            wiz.mCannedButtons.add(new CannedButton(2, context.getString(R.string.no), null, -2));
            wiz.mCannedButtons.add(new CannedButton(3, context.getString(R.string.maybe), null, -3));
            wiz.mCannedButtons.add(new CannedButton(4, context.getString(R.string.like), null, -4));
            wiz.mCannedButtons.add(new CannedButton(5, context.getString(R.string.dont_like), null, -5));
            wiz.mCannedButtons.add(new CannedButton(6, context.getString(R.string.no_idea), null, -6));
            wiz.mCannedButtons.sort();
            wiz.mCannedButtons.save(cfgProvider);
        }
    }
}

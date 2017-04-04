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
package com.mobicage.rogerthat;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.jivesoftware.smack.util.Base64;

import com.mobicage.rogerthat.config.Configuration;
import com.mobicage.rogerthat.config.ConfigurationProvider;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.pickle.PickleException;
import com.mobicage.rogerthat.util.pickle.Pickleable;
import com.mobicage.rogerthat.util.pickle.Pickler;
import com.mobicage.rogerthat.util.ui.SendMessageView;

public class CannedButtons implements Pickleable {

    private List<CannedButton> mButtons = new ArrayList<CannedButton>();
    private boolean mDirty = false;

    @Override
    public void writePickle(DataOutput out) throws IOException {
        out.writeInt(mButtons.size());
        for (CannedButton button : mButtons) {
            out.writeLong(button.getId());
            out.writeUTF(button.getCaption());
            String action = button.getAction();
            out.writeBoolean(action == null);
            if (action != null)
                out.writeUTF(action);
            out.writeInt(button.getUsed());
        }
    }

    @Override
    public void readFromPickle(int version, DataInput in) throws IOException, PickleException {
        int buttonCount = in.readInt();
        for (int i = 0; i < buttonCount; i++) {
            mButtons.add(new CannedButton(in.readLong(), in.readUTF(), in.readBoolean() ? null : in.readUTF(), in
                .readInt()));
        }
    }

    @Override
    public int getPickleClassVersion() {
        return 1;
    }

    public void sort() {
        Collections.sort(mButtons, new Comparator<CannedButton>() {
            @Override
            public int compare(CannedButton left, CannedButton right) {
                if (left.getUsed() == right.getUsed()) {
                    return left.getCaption().compareTo(right.getCaption());
                } else
                    return left.getUsed() > right.getUsed() ? -1 : 0;
            }
        });
    }

    public void add(CannedButton button) {
        mDirty = true;
        mButtons.add(0, button);
    }

    public CannedButton[] toArray() {
        return mButtons.toArray(new CannedButton[mButtons.size()]);
    }

    public void setDirty(boolean dirty) {
        this.mDirty = dirty;
    }

    public boolean isDirty() {
        return mDirty;
    }

    public void save(final ConfigurationProvider cfgProvider) {
        if (!mDirty)
            return;
        final String serializedButtons;
        try {
            serializedButtons = Base64.encodeBytes(Pickler.getPickleFromObject(this));
        } catch (PickleException e) {
            L.bug(e);
            return;
        }
        Configuration cfg = new Configuration();
        cfg.put(SendMessageView.CANNED_BUTTONS, serializedButtons);
        cfgProvider.updateConfigurationNow(SendMessageView.CONFIGKEY, cfg);
    }

    public CannedButton getById(long id) {
        for (CannedButton button : mButtons) {
            if (button.getId() == id)
                return button;
        }
        return null;
    }

    public CannedButton get(int position) {
        return mButtons.get(position);
    }

    public int size() {
        return mButtons.size();
    }

    public void remove(CannedButton button) {
        for (int i = 0; i < mButtons.size(); i++) {
            CannedButton b = mButtons.get(i);
            if (b.getId() == button.getId()) {
                mButtons.remove(i);
                mDirty = true;
                return;
            }
        }
    }
}

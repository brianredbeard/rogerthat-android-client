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

public class CannedButton {

    private long mId = 0;
    private String mCaption = null;
    private String mAction = null;
    private int mUsed = 0;
    private boolean mSelected = false;

    public CannedButton(String caption, String action) {
        this(System.currentTimeMillis(), caption, action, 0);
    }

    public CannedButton(long id, String caption, String action, int used) {
        mId = id;
        mCaption = caption;
        mAction = action;
        mUsed = used;
    }

    public void setCaption(String caption) {
        this.mCaption = caption;
    }

    public String getCaption() {
        return mCaption;
    }

    public void setAction(String action) {
        this.mAction = action;
    }

    public String getAction() {
        return mAction;
    }

    public int getUsed() {
        return mUsed;
    }

    public long getId() {
        return mId;
    }

    public void used() {
        // We have some predefined buttons which initially have usage count<0 (actual usage count 0, but force ordering)
        if (mUsed <= 0)
            mUsed = 1;
        else
            mUsed++;
    }

    public void setSelected(boolean selected) {
        this.mSelected = selected;
    }

    public boolean isSelected() {
        return mSelected;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null || !(other instanceof CannedButton))
            return false;
        return getId() == ((CannedButton) other).getId();
    }

    @Override
    public int hashCode() {
        return Long.valueOf(getId()).hashCode();
    }

}
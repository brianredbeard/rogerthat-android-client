/*
 * Copyright 2017 Mobicage NV
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
 * @@license_version:1.2@@
 */
package com.mobicage.rogerthat.util.ui;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.pickle.PickleException;
import com.mobicage.rogerthat.util.pickle.Pickleable;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.system.SafeViewOnClickListener;
import com.mobicage.rogerthat.util.system.T;

public class Wizard implements Pickleable {

    public interface Persister {
        void save(Wizard wiz);

        void clear(Wizard wiz);
    }

    public interface PageHandler {
        /**
         * The following method is called before the switcher is instructed to move to the previous page.
         * 
         * @return true if the switcher can navigate to the previous view, false if the current view should remain.
         */
        boolean beforeBackClicked(Button back, Button next, ViewFlipper switcher);

        /**
         * The following method is called before the switcher is instructed to move to the next page
         * 
         * @return true if the switcher can navigate to the next view, false if the current view should remain.
         */
        boolean beforeNextClicked(Button back, Button next, ViewFlipper switcher);

        /**
         * Called when the page is displayed
         */
        void pageDisplayed(Button back, Button next, ViewFlipper switcher);

        /**
         * Returns the title for this page
         * 
         * @return
         */
        String getTitle();
    }

    // Pickable fields
    private int mPosition = 0;

    // Non pickled fields
    private Persister mPersister;
    private Button mNextButton;
    private Button mBackButton;
    private ViewFlipper mFlipper;
    private TextView mTitleBar;
    private final List<PageHandler> mHandlers = new ArrayList<PageHandler>();
    private SafeRunnable mOnFinish;

    public void setPersister(Persister persister) {
        T.UI();
        this.mPersister = persister;
    }

    public void setFlipper(ViewFlipper flipper) {
        T.UI();
        this.mFlipper = flipper;
        this.mFlipper.setOutAnimation(flipper.getContext(), android.R.anim.fade_out);
        this.mFlipper.setInAnimation(flipper.getContext(), android.R.anim.fade_in);
    }

    public void setBackButton(Button backButton) {
        T.UI();
        this.mBackButton = backButton;
        setPreviousHandler();
    }

    public void setNextButton(Button nextButton) {
        T.UI();
        this.mNextButton = nextButton;
        setNextHandler();
    }

    public void setTitleBar(TextView titleBar) {
        T.UI();
        this.mTitleBar = titleBar;
    }

    public void setOnFinish(SafeRunnable runnable) {
        T.UI();
        this.mOnFinish = runnable;
    }

    public void addPageHandler(PageHandler ph) {
        T.UI();
        mHandlers.add(ph);
    }

    public void setPosition(int position) {
        T.UI();
        mPosition = position;
    }

    @Override
    public void writePickle(DataOutput out) throws IOException {
        T.UI();
        out.writeInt(mPosition);
    }

    @Override
    public void readFromPickle(int version, DataInput in) throws IOException, PickleException {
        T.UI();
        mPosition = in.readInt();
    }

    @Override
    public int getPickleClassVersion() {
        T.UI();
        return 1;
    }

    public void run() {
        T.UI();
        mFlipper.setDisplayedChild(mPosition);
        handleDisplay();
    }

    public void proceedToPosition(int position) {
        L.d("proceedToPosition: " + position);
        if (mPosition != position) {
            if (position >= mHandlers.size()) {
                finish();
            } else {
                mPosition = position;
                mFlipper.setDisplayedChild(mPosition);
                handleDisplay();
            }
        }

    }

    public void proceedToNextPage() {
        T.UI();
        proceedToNextPage(mHandlers.get(mPosition));
    }

    public void proceedToNextPage(PageHandler currentHandler) {
        T.UI();
        if (mPosition == mHandlers.size() - 1) {
            finish();
        } else {
            mFlipper.showNext();
            mPosition++;
            handleDisplay();
            save();
        }
    }

    public void goBackToPrevious() {
        T.UI();
        goBackToPrevious(mHandlers.get(mPosition));
    }

    public void goBackToPrevious(PageHandler currentHandler) {
        T.UI();
        mFlipper.showPrevious();
        mPosition--;
        handleDisplay();
        save();
    }

    public void save() {
        T.UI();
        if (mPersister != null) {
            mPersister.save(this);
        }
    }

    public void clear() {
        T.UI();
        if (mPersister != null) {
            mPersister.clear(this);
        }
    }

    public int getPosition() {
        return mPosition;
    }

    protected Persister getPersister() {
        return mPersister;
    }

    private void updateButtons() {
        T.UI();
        if (mBackButton != null)
            mBackButton.setEnabled(true);
        if (mNextButton != null) {
            mNextButton.setEnabled(true);
            mNextButton.setText(mPosition == mHandlers.size() - 1 ? R.string.finishButton : R.string.nextButton);
        }
    }

    private void handleDisplay() {
        T.UI();
        updateButtons();
        PageHandler pageHandler = mHandlers.get(mPosition);
        pageHandler.pageDisplayed(mBackButton, mNextButton, mFlipper);
        if (mTitleBar != null)
            mTitleBar.setText(pageHandler.getTitle());
    }

    public void goBack() {
        T.UI();
        final PageHandler currentHandler = mHandlers.get(mPosition);
        if (mPosition == 0) {
            currentHandler.beforeBackClicked(mBackButton, mNextButton, mFlipper);
            return;
        }
        if (currentHandler.beforeBackClicked(mBackButton, mNextButton, mFlipper)) {
            goBackToPrevious(currentHandler);
        }

    }

    public void finish() {
        T.UI();
        if (mOnFinish != null) {
            mOnFinish.run();
        }
    }

    private void setPreviousHandler() {
        T.UI();
        mBackButton.setOnClickListener(new SafeViewOnClickListener() {
            @Override
            public void safeOnClick(View v) {
                goBack();
            }
        });
    }

    private void setNextHandler() {
        T.UI();
        mNextButton.setOnClickListener(new SafeViewOnClickListener() {
            @Override
            public void safeOnClick(View v) {
                T.UI();
                PageHandler currentHandler = mHandlers.get(mPosition);
                if (currentHandler.beforeNextClicked(mBackButton, mNextButton, mFlipper)) {
                    proceedToNextPage(currentHandler);
                }
            }

        });
    }

}

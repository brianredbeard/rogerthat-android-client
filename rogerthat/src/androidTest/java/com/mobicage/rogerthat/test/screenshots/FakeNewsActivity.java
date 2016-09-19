package com.mobicage.rogerthat.test.screenshots;

import com.mobicage.rogerthat.NewsActivity;

public class FakeNewsActivity extends NewsActivity {

    @Override
    protected void setListAdapater() {
        mListAdapter = new FakeNewsListAdapter(this);
        mListView.setAdapter(mListAdapter);
    }
}
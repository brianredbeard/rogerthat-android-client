package com.mobicage.rogerthat.test.screenshots;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.NewsActivity;
import com.mobicage.rogerthat.plugins.news.NewsItem;
import com.mobicage.to.news.NewsActionButtonTO;
import com.mobicage.to.news.NewsSenderTO;

import java.util.ArrayList;
import java.util.List;

// Also used in util.ui.TestUtils to see if we are currently running test or not
public class FakeNewsActivity extends NewsActivity {

    private NewsItem createNewsItem(int id, int title, int message, int label, String email,
                                    int sender, NewsActionButtonTO[] buttons, boolean rogered,
                                    boolean pinned) {
        NewsItem newsItem = new NewsItem();
        newsItem.id = id;
        newsItem.rogered = rogered;
        newsItem.pinned = pinned;
        newsItem.deleted = false;
        newsItem.dirty = false;
        newsItem.title = getString(title);
        newsItem.message = getString(message);
        newsItem.label = getString(label);
        newsItem.users_that_rogered = new String[]{};
        newsItem.version = 1;
        NewsSenderTO newsSender = new NewsSenderTO();
        newsSender.avatar_id = 4934311790772224L;
        newsSender.email = email;
        newsSender.name = getString(sender);
        newsItem.sender = newsSender;
        newsItem.buttons = buttons;
        return newsItem;
    }

    private NewsActionButtonTO[] createNewsActionButtons(int... captions) {
        NewsActionButtonTO[] buttons = new NewsActionButtonTO[captions.length];

        for (int i = 0; i < captions.length; i++) {
            NewsActionButtonTO button = new NewsActionButtonTO();
            button.caption = getString(captions[i]);
            buttons[i] = button;
        }
        return buttons;
    }

    @Override
    protected void setListAdapter() {
        final List<NewsItem> items = new ArrayList<>();

        NewsActionButtonTO[] buttons = createNewsActionButtons(R.string.rogerthat, R.string.follow);
        items.add(createNewsItem(1, R.string.sample_news_item_title_1,
                R.string.sample_news_item_message_1, R.string.news, "sample.service@rogerthat.net",
                R.string.sample_news_item_sender_1, buttons, true, false));
        NewsActionButtonTO[] buttons2 = createNewsActionButtons(R.string.rogerthat, R.string.follow, R.string.reserve);
        items.add(createNewsItem(2, R.string.sample_news_item_title_2,
                R.string.sample_news_item_message_2, R.string.news, "service@rogerthat.net",
                R.string.sample_news_item_sender_2, buttons2, false, true));

        mListAdapter = new NewsListAdapter(this) {
            @Override
            protected NewsItem getNewsItem(int position) {
                return items.get(position);
            }

            @Override
            public long getItemId(int position) {
                return items.get(position).id;
            }

            @Override
            public int getCount() {
                return items.size();
            }
        };
        mListView.setAdapter(mListAdapter);
    }
}
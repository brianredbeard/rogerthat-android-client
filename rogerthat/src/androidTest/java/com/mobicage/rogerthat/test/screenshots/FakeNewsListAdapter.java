package com.mobicage.rogerthat.test.screenshots;

import android.content.Context;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.NewsActivity;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.to.news.BaseNewsItemTO;
import com.mobicage.to.news.NewsActionButtonTO;
import com.mobicage.to.news.NewsSenderTO;

import java.util.ArrayList;
import java.util.List;

public class FakeNewsListAdapter extends NewsActivity.NewsListAdapter {
    private List<BaseNewsItemTO> mItems;

    public FakeNewsListAdapter(Context context) {
        super(context, null, null);
        T.UI();
        mItems = new ArrayList<>();

        NewsActionButtonTO[] buttons = createNewsActionButtons(context, R.string.rogerthat, R.string.follow);
        mItems.add(createNewsItem(context, 1, R.string.sample_news_item_title_1,
                R.string.sample_news_item_message_1, "sample.service@rogerthat.net",
                R.string.sample_news_item_sender_1, buttons));
        NewsActionButtonTO[] buttons2 = createNewsActionButtons(context, R.string.rogerthat, R.string.follow, R.string.reserve);
        mItems.add(createNewsItem(context, 2, R.string.sample_news_item_title_2,
                R.string.sample_news_item_message_2, "service@rogerthat.net",
                R.string.sample_news_item_sender_2, buttons2));

    }

    private BaseNewsItemTO createNewsItem(Context context, int id, int title, int message, String email, int sender, NewsActionButtonTO[] buttons) {
        BaseNewsItemTO newsItem = new BaseNewsItemTO();
        newsItem.id = id;
        newsItem.title = context.getString(title);
        newsItem.message = context.getString(message);
        NewsSenderTO newsSender = new NewsSenderTO();
        newsSender.avatar_id = 4934311790772224L;
        newsSender.email = email;
        newsSender.name = context.getString(sender);
        newsItem.sender = newsSender;
        newsItem.buttons = buttons;
        return newsItem;
    }

    private NewsActionButtonTO[] createNewsActionButtons(Context context, int... captions) {
        NewsActionButtonTO[] buttons = new NewsActionButtonTO[captions.length];

        for (int i = 0; i < captions.length; i++) {
            NewsActionButtonTO button = new NewsActionButtonTO();
            button.caption = context.getString(captions[i]);
            buttons[i] = button;
        }
        return buttons;
    }

    @Override
    protected BaseNewsItemTO getNewsItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mItems.get(position).id;
    }

    @Override
    public int getCount() {
        return mItems.size();
    }
}

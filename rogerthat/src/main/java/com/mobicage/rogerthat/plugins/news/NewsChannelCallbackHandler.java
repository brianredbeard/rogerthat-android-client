package com.mobicage.rogerthat.plugins.news;

import com.mobicage.rogerthat.MainService;
import com.mobicage.to.news.AppNewsItemTO;

import java.util.Map;

public interface NewsChannelCallbackHandler {

    void newsRogerUpdate(long newsId, String friendEmail);

    MainService getMainService();

    void newsPush(AppNewsItemTO newsItem);

    /**
     * @param statsMap Key = news id, value = reach.
     */
    void newsReadUpdate(Map<Long, Long> statsMap);
}
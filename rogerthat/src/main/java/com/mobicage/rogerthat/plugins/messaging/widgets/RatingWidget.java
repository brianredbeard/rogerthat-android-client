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

package com.mobicage.rogerthat.plugins.messaging.widgets;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.mobicage.api.messaging.Rpc;
import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.plugins.messaging.Message;
import com.mobicage.rogerthat.plugins.messaging.MessagingPlugin;
import com.mobicage.rpc.IncompleteMessageException;
import com.mobicage.rpc.ResponseHandler;
import com.mobicage.rpc.config.LookAndFeelConstants;
import com.mobicage.to.messaging.forms.RatingTO;
import com.mobicage.models.properties.forms.RatingTopic;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.to.messaging.forms.RatingWidgetResultTO;
import com.mobicage.to.messaging.forms.SubmitRatingFormRequestTO;
import com.mobicage.to.messaging.forms.SubmitRatingFormResponseTO;

public class RatingWidget extends Widget {

    private RatingTO rating = null;
    private TopicsAdapter topicsAdapter;
    private RatingWidgetResultTO mResult;

    public RatingWidget(Context context) {
        super(context);
    }

    public RatingWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private class TopicsAdapter extends ArrayAdapter<RatingTopic> {

        public TopicsAdapter(Context context, RatingTopic[] topics) {
            super(context, 0, topics);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.widget_rating_topic, parent, false);
            }

            RatingTopic currentTopic = getItem(position);
            TextView titleText = (TextView) convertView.findViewById(R.id.title_text);
            TextView questionText = (TextView) convertView.findViewById(R.id.question_text);
            titleText.setTypeface(Typeface.DEFAULT_BOLD);
            titleText.setText(currentTopic.title);
            RatingBar ratingBar = (RatingBar) convertView.findViewById(R.id.rating_bar);
            ratingBar.setRating(currentTopic.score);

            if (currentTopic.question == null) {
                questionText.setVisibility(View.GONE);
                ratingBar.setEnabled(false);
            } else if (!currentTopic.question.trim().equals("")){
                questionText.setText(currentTopic.question);
            }

            int color = LookAndFeelConstants.getPrimaryColor(mActivity);
            ratingBar.getProgressDrawable().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
            ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                @Override
                public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                    getItem(position).score = rating;
                }
            });

            return convertView;
        }
    }

    @Override
    public void initializeWidget() {
        T.UI();

        try {
            rating = new RatingTO(mWidgetMap);

            if (!isEnabled()) {
                Map<String, Object> value = (Map<String, Object>)mWidgetMap.get("value");
                if (value != null)
                    rating = new RatingTO(value);
            }

            LinearLayout ratingContainer = (LinearLayout) findViewById(R.id.rating_container);
            topicsAdapter = new TopicsAdapter(mActivity, rating.topics);
            for (int i = 0; i < topicsAdapter.getCount(); i++) {
                ratingContainer.addView(topicsAdapter.getView(i, null, null));
            }
        } catch (IncompleteMessageException e) {
            L.bug(e);
        }
    }

    @Override
    public void putValue() {
        try {
            // result is the same as RatingTO
            // just update scores
            mResult = new RatingWidgetResultTO(rating.toJSONMap());
            mWidgetMap.put("value", rating.toJSONMap());
        } catch (IncompleteMessageException e) {
            L.bug(e);
        }
    }

    @Override
    public RatingWidgetResultTO getWidgetResult() {
        return mResult;
    }


    @Override
    public void submit(final String buttonId, long timestamp) throws Exception {
        T.UI();
        final SubmitRatingFormRequestTO request = new SubmitRatingFormRequestTO();
        request.button_id = buttonId;
        request.message_key = mMessage.key;
        request.parent_message_key = mMessage.parent_key;
        request.timestamp = timestamp;

        if (Message.POSITIVE.equals(buttonId)) {
            request.result = getWidgetResult();
            L.d("Submit Rating " + mWidgetMap);
        }

        if ((mMessage.flags & MessagingPlugin.FLAG_SENT_BY_JSMFR) == MessagingPlugin.FLAG_SENT_BY_JSMFR)
            mPlugin.answerJsMfrMessage(mMessage, request.toJSONMap(),
                    "com.mobicage.api.messaging.submitRatingForm", mActivity, mParentView);
        else
            Rpc.submitRatingForm(new ResponseHandler<SubmitRatingFormResponseTO>(), request);
    }

    public static String valueString(Context context, Map<String, Object> widget) {
        @SuppressWarnings("unchecked")
        final Map<String, Object> jsonResult = (Map<String, Object>) widget.get("value");
        if (jsonResult == null) {
            return "";
        }

        final RatingWidgetResultTO result;
        try {
            result = new RatingWidgetResultTO(jsonResult);
        } catch (IncompleteMessageException e) {
            L.bug(e);
            return "";
        }

        final List<String> parts = new ArrayList<String>();
        for (int i = 0; i < result.topics.length; i++) {
            RatingTopic topic = result.topics[i];
            if (i != 0) {
                parts.add("");
            }
            parts.add(String.format("%s: %s", topic.title, topic.score));
        }
        return android.text.TextUtils.join("\n", parts);
    }
}

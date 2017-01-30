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
package com.mobicage.rogerthat.plugins.messaging;

import android.content.Context;
import android.content.Intent;

import com.mobicage.rogerthat.util.ui.Slider;

public class RightSwiper implements Slider.Swiper {

    private final MessagingPlugin mMessagingPlugin;
    private final String mParentMessageKey;
    private final Context mContext;
    private final String mMemberFilter;

    public RightSwiper(Context context, MessagingPlugin messagingPlugin, String parentMessageKey, String memberFilter) {
        mContext = context;
        mMessagingPlugin = messagingPlugin;
        mParentMessageKey = parentMessageKey;
        mMemberFilter = memberFilter;
    }

    @Override
    public Intent onSwipe() {
        return mMessagingPlugin.getPreviousMessageThreadActivityIntent(mContext, mParentMessageKey, mMemberFilter);
    }

}

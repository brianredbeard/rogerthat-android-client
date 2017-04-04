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
package com.mobicage.rogerthat.plugins.messaging;

import android.widget.ListView;

import com.mobicage.rogerthat.util.system.SystemUtils;

public abstract class Scroller {

    protected ListView mListView = null;

    public static Scroller getInstance() {
        String className = Scroller.class.getPackage().getName() + ".";
        if (SystemUtils.getAndroidVersion() < 8) {
            className += "ScrollerMinusAPI8";
        } else {
            className += "ScrollerAPI8Plus";
        }
        try {
            Class<?> detectorClass = Class.forName(className);
            return (Scroller) detectorClass.getConstructors()[0].newInstance();
        } catch (Exception e) {
            return null;
        }
    }

    public void setListView(ListView listView) {
        this.mListView = listView;
    }

    public abstract void scrollToPosition(int position);

}

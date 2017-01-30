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

package com.mobicage.rogerthat.plugins.history;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.HashMap;

import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.pickle.PickleException;
import com.mobicage.rogerthat.util.pickle.Pickleable;
import com.mobicage.rogerthat.util.system.T;

public class HistoryItem {

    // keys for parameter dict
    public final static String PARAM_MESSAGE_CONTENT = "message";
    public final static String PARAM_MESSAGE_PARENT_CONTENT = "parentmessage";
    public final static String PARAM_MESSAGE_FROM = "from";
    public final static String PARAM_MESSAGE_TO = "to";
    public final static String PARAM_MESSAGE_QUICK_REPLY_BUTTON = "qrbutton";

    public final static String PARAM_FRIEND_NAME = "friendname";
    public final static String PARAM_FRIENDS_FRIEND_NAME = "friendsFriendName";

    public final static String PARAM_POKE_ACTION = "pokeAction";

    public final static String PARAM_LOG_LINE = "log";

    // type enum
    public final static int DEBUG = 1;
    public final static int INFO = 2;
    public final static int WARNING = 3;
    public final static int ERROR = 4;
    public final static int FATAL = 5;
    public final static int MAX_HISTORY_LOG_VALUE = FATAL;

    public final static int MESSAGE_RECEIVED = 100;
    public final static int MESSAGE_SENT = 101;
    public final static int QUICK_REPLY_RECEIVED_FOR_ME = 102;
    public final static int QUICK_REPLY_RECEIVED_FOR_OTHER = 103;
    public final static int QUICK_REPLY_SENT_FOR_ME = 104;
    public final static int QUICK_REPLY_SENT_FOR_OTHER = 105;
    public final static int MESSAGE_LOCKED_BY_ME = 106;
    public final static int MESSAGE_LOCKED_BY_OTHER = 107;
    public final static int REPLY_RECEIVED = 108;
    public final static int REPLY_SENT = 109;
    public final static int MESSAGE_DISMISSED_BY_ME = 111;
    public final static int MESSAGE_DISMISSED_BY_OTHER = 112;
    public final static int QUICK_REPLY_UNDONE = 113;

    public final static int FRIEND_ADDED = 200;
    public final static int FRIEND_REMOVED = 201;
    public final static int FRIEND_UPDATED = 202;
    public final static int FRIEND_BECAME_FRIEND = 203;
    public final static int SERVICE_POKED = 204;

    public final static int LOCATION_SHARING_MY_LOCATION_SENT = 301;

    // time millis
    public long timestampMillis;

    // enum
    // can be used for filtering
    public int type;

    // (nullable) reference - typically friend email (e.g. friend actions)
    // or message key (message actions)
    // can be used for filtering
    public String reference;

    // (nullable) reference to friend - used for icon; used for filtering activity wrt a certain friend
    public String friendReference;

    // dictionary of string/string parameters
    // cannot be used for filtering
    public final PickleableStringMap parameters = new PickleableStringMap();

    public long _id;

    @SuppressWarnings("serial")
    public static class PickleableStringMap extends HashMap<String, String> implements Pickleable {

        private final static int PICKLE_CLASS_VERSION = 1;

        @Override
        public int getPickleClassVersion() {
            return PICKLE_CLASS_VERSION;
        }

        @Override
        public void readFromPickle(int version, DataInput in) throws IOException, PickleException {
            if (version != PICKLE_CLASS_VERSION) {
                L.bug("Wrong pickle class version " + version + " expecting " + PICKLE_CLASS_VERSION);
                return;
            }
            int numItems = in.readInt();
            for (int i = 0; i < numItems; i++) {
                put(in.readUTF(), in.readUTF());
            }
        }

        @Override
        public void writePickle(DataOutput out) throws IOException {
            T.dontCare();
            out.writeInt(size());
            for (String key : keySet()) {
                String value = get(key);
                if (value == null) {
                    L.bug("Error pickling History item - null value for parameter " + key);
                }
                out.writeUTF(key);
                out.writeUTF(get(key));
            }
        }
    }

}

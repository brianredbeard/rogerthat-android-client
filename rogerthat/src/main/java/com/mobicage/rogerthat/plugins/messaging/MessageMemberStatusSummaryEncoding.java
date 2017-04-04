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

// We encode message member status summary in one long.
// Four numbers are encoded:
// - num non-sender members
// - num non-sender members that have received the message (in web view or in phone view)
// - num non-sender members that have quick-replied to the message
// - num non-sender members that have dismissed the message
// 
// see getNumNonSender**Members for semantics of the detailed offset and length of the bits
//
// max value of each encoded int is 0x7fff (= 32,767)

public class MessageMemberStatusSummaryEncoding {

    public static final long ERROR = -2;

    public static long encodeMessageMemberSummary(final int numNonSenderMembers, final int numNonSenderMembersReceived,
        final int numNonSenderMembersQuickReplied, final int numNonSenderMembersDismissed) {

        // Note: if they send messages to more than 0x7fff (= 32767) members at once, then we're strong busy

        final long newStatus = (((long) numNonSenderMembers) & 0x7fff)
            | ((((long) numNonSenderMembersReceived) & 0x7fff) << 16)
            | ((((long) numNonSenderMembersQuickReplied) & 0x7fff) << 32)
            | ((((long) numNonSenderMembersDismissed) & 0x7fff) << 48);

        return newStatus;

    }

    public static int decodeNumNonSenderMembers(long memberStatusSummary) {
        return (int) (memberStatusSummary & 0x7fff);
    }

    public static int decodeNumNonSenderMembersReceived(long memberStatusSummary) {
        return (int) ((memberStatusSummary >> 16) & 0x7fff);
    }

    public static int decodeNumNonSenderMembersQuickReplied(long memberStatusSummary) {
        return (int) ((memberStatusSummary >> 32) & 0x7fff);
    }

    public static int decodeNumNonSenderMembersDismissed(long memberStatusSummary) {
        return (int) ((memberStatusSummary >> 48) & 0x7fff);
    }

}

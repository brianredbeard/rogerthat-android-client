/*
 * Copyright 2016 Mobicage NV
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
 * @@license_version:1.1@@
 */

package com.mobicage.rogerthat.util.time;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import android.content.Context;
import android.text.format.DateFormat;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.util.logging.L;

public class TimeUtils {

    private final static String TODAY = "Today";

    private final static long SECOND = 1000;
    private final static long MINUTE = 60 * SECOND;
    private final static long HOUR = 60 * MINUTE;

    public static class TimeStatus {
        public TimeStatus(boolean pIsEnabledNow, long pNextEventTimeMillis) {
            mIsEnabledNow = pIsEnabledNow;
            mNextEventTimeMillis = pNextEventTimeMillis;
        }

        public final boolean mIsEnabledNow;
        public final long mNextEventTimeMillis;
    }

    // In Mobicage we use a mask to define allowed days
    // Calendar.MONDAY ----> 0000 0001
    // Calendar.TUESDAY ---> 0000 0010
    // Calendar.WEDNESDAY -> 0000 0100
    // Calendar.THURSDAY --> 0000 1000
    // Calendar.FRIDAY ----> 0001 0000
    // Calendar.SATURDAY --> 0010 0000
    // Calendar.SUNDAY ----> 0100 0000
    private final static int[] CALENDAR_DAY_MAPPING = new int[] { -1, 0x40, 0x01, 0x02, 0x04, 0x08, 0x10, 0x20 };

    // All this stuff is required for dealing with daylight savings time
    private static long getTimeMillisAtOffsetToday(TimeZone pTimeZone, long pCurrentTimeMillis, long pTimeOffsetMillis) {
        GregorianCalendar cal = new GregorianCalendar(pTimeZone);
        cal.setTimeInMillis(pCurrentTimeMillis);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        pTimeOffsetMillis /= 1000;
        int seconds = (int) (pTimeOffsetMillis % 60);
        pTimeOffsetMillis /= 60;
        int minutes = (int) (pTimeOffsetMillis % 60);
        pTimeOffsetMillis /= 60;
        int hours = (int) pTimeOffsetMillis;

        if (hours > 24) {
            L.bug("Illegal time offset " + pTimeOffsetMillis);
            hours = 23;
        }

        cal.set(Calendar.HOUR_OF_DAY, hours);
        cal.set(Calendar.MINUTE, minutes);
        cal.set(Calendar.SECOND, seconds);
        long resultTimeMillis = cal.getTimeInMillis();

        return resultTimeMillis;

    }

    /**
     * Calculate next time when there might be a policy change.
     * 
     * Further optimizations are possible e.g. wake up on the correct day (current code wakes up on next day in any
     * case)
     * 
     */
    public static TimeStatus getAuthorizedTimeInfo(TimeZone tz, long pCurrentTimeMillis, long pDayMask,
        long pDailyFromTimeSeconds, long pDailyTillTimeSeconds) {
        try {

            GregorianCalendar cal = new GregorianCalendar(tz);
            cal.setTimeInMillis(pCurrentTimeMillis);

            // Take care of daylight savings time trouble
            long dailyFromTimeMillis = pDailyFromTimeSeconds * 1000;
            long dailyTillTimeMillis = pDailyTillTimeSeconds * 1000;

            // Case 1. Day is not allowed
            int javaDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
            int mobicageDayOfWeekMask = CALENDAR_DAY_MAPPING[javaDayOfWeek];
            boolean retryTomorrow = ((mobicageDayOfWeekMask & pDayMask) == 0);

            // Case 2. An allowed day, but the allowed time interval has passed
            if (!retryTomorrow) {
                long todayTillTimeMillis = getTimeMillisAtOffsetToday(tz, pCurrentTimeMillis, dailyTillTimeMillis);
                retryTomorrow = (pCurrentTimeMillis > todayTillTimeMillis);
            }

            if (retryTomorrow) {
                // Wake up at beginning of next timeslot
                cal.add(Calendar.DAY_OF_MONTH, 1);
                long wakeUpTimeMillis = getTimeMillisAtOffsetToday(tz, cal.getTimeInMillis(), dailyFromTimeMillis);
                return new TimeStatus(false, wakeUpTimeMillis + 1001);
            }

            // Case 3. We are in the allowed time interval on an allowed day
            // Trigger today at end of allowed interval
            long fromTimeTodayMillis = getTimeMillisAtOffsetToday(tz, pCurrentTimeMillis, dailyFromTimeMillis);
            if (pCurrentTimeMillis >= fromTimeTodayMillis) {
                long wakeUpTimeMillis = getTimeMillisAtOffsetToday(tz, cal.getTimeInMillis(), dailyTillTimeMillis);
                return new TimeStatus(true, wakeUpTimeMillis + 1001);
            }

            // Case 4. Trigger later today at beginning of time interval
            long wakeUpTimeMillis = getTimeMillisAtOffsetToday(tz, pCurrentTimeMillis, dailyFromTimeMillis);
            return new TimeStatus(false, wakeUpTimeMillis + 1001);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public static String getPrettyTimeString(TimeZone tz, long timeMillis) {
        if (tz == null)
            tz = TimeZone.getDefault();
        GregorianCalendar cal = new GregorianCalendar(tz);
        cal.setTimeInMillis(timeMillis);

        return cal.get(Calendar.YEAR) + "." + (cal.get(Calendar.MONTH) + 1) + "." + cal.get(Calendar.DAY_OF_MONTH)
            + " " + cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE) + ":" + cal.get(Calendar.SECOND)
            + "." + cal.get(Calendar.MILLISECOND);
    }

    public static long getGMTOffsetMillis() {
        return TimeZone.getDefault().getRawOffset();
    }

    public static boolean getFormatIsFirstMonthThenDay(final Context context) {
        try {
            for (char c : android.text.format.DateFormat.getDateFormatOrder(context)) {
                if (c == 'd') {
                    return false;
                }
                if (c == 'M') {
                    return true;
                }
            }
        } catch (Exception e) {
            L.w("Exception while determining if date or month should be shown first.", e);
        }
        // Should not come here
        return true;
    }

    public static Locale getLocaleForDateDisplay() {
        // Fall back to Locale.US when full month format is numeric
        String month = new SimpleDateFormat("MMMM", Locale.getDefault()).format(new Date());
        return (month.matches("[0-9]")) ? Locale.US : Locale.getDefault();
    }

    public static String getDayStrOrToday(final Context context, final long timeUTCMillis) {
        final TimeZone tz = TimeZone.getDefault();
        final GregorianCalendar cal = new GregorianCalendar(tz);
        cal.setTimeInMillis(timeUTCMillis);

        final GregorianCalendar nowCal = new GregorianCalendar(tz);
        nowCal.setTimeInMillis(new Date().getTime());

        final boolean isToday = (nowCal.get(Calendar.YEAR) == cal.get(Calendar.YEAR))
            && (nowCal.get(Calendar.DAY_OF_YEAR) == cal.get(Calendar.DAY_OF_YEAR));

        if (isToday) {
            return TODAY;
        } else {
            return getDayStr(context, new Date(timeUTCMillis), true);
        }
    }

    /**
     * Get representation of date representation based on Locale
     * 
     * @return Sep 7 or September 7 in US. 7 sep or 7 september in Belgium
     */
    private static String getDayStr(final Context context, final Date date, final boolean shortFormat) {
        String monthFormat = shortFormat ? "MMM" : "MMMM";
        String pattern = getFormatIsFirstMonthThenDay(context) ? monthFormat + " d" : "d " + monthFormat;
        return new SimpleDateFormat(pattern, getLocaleForDateDisplay()).format(date);
    }

    public static String getHumanTime(final Context context, final long pTimeUTCMillis, final boolean showMinutes) {
        return getHumanTime(context, new Date().getTime(), pTimeUTCMillis, showMinutes);
    }

    public static long startRunnableToUpdateTimeIn(final Context context, final long pTimeUTCMillis) {
        final long pNowTimeUTCMillis = new Date().getTime();

        Calendar nowCalendar = Calendar.getInstance();
        nowCalendar.setTimeInMillis(pNowTimeUTCMillis);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(pTimeUTCMillis);

        boolean onSameDay = (nowCalendar.get(Calendar.YEAR) == calendar.get(Calendar.YEAR))
            && (nowCalendar.get(Calendar.DAY_OF_YEAR) == calendar.get(Calendar.DAY_OF_YEAR));

        if (!onSameDay)
            return 0;

        long hoursDifference = (pNowTimeUTCMillis - pTimeUTCMillis) / HOUR;
        if (hoursDifference >= 1)
            return 0;

        long minutesDifference = (pNowTimeUTCMillis - pTimeUTCMillis) / MINUTE;
        if (minutesDifference >= 0) {
            long secondsDifference = (pNowTimeUTCMillis - pTimeUTCMillis) / SECOND;
            return (60 - (secondsDifference % 60)) * 1000;
        }
        return 0;
    }

    public static String getHumanTime(final Context context, final long pNowTimeUTCMillis, final long pTimeUTCMillis,
        final boolean showMinutes) {
        Date date = new Date(pTimeUTCMillis);

        Calendar nowCalendar = Calendar.getInstance();
        nowCalendar.setTimeInMillis(pNowTimeUTCMillis);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(pTimeUTCMillis);

        boolean onSameDay = (nowCalendar.get(Calendar.YEAR) == calendar.get(Calendar.YEAR))
            && (nowCalendar.get(Calendar.DAY_OF_YEAR) == calendar.get(Calendar.DAY_OF_YEAR));

        if (onSameDay) {
            if (showMinutes) {
                long hoursDifference = (pNowTimeUTCMillis - pTimeUTCMillis) / HOUR;
                if (hoursDifference >= 1)
                    return DateFormat.getTimeFormat(context).format(date);

                long minutesDifference = (pNowTimeUTCMillis - pTimeUTCMillis) / MINUTE;
                if (minutesDifference >= 0) {
                    return context.getString(R.string.x_min_ago, minutesDifference + 1);
                }
            }

            return DateFormat.getTimeFormat(context).format(date);

        } else {
            return getDayTimeStr(context, pTimeUTCMillis, true);
        }
    }

    public static String getDayTimeStr(final Context context, final long pTimeUTCMillis) {
        return getDayTimeStr(context, pTimeUTCMillis, false);
    }

    private static String getDayTimeStr(final Context context, final long pTimeUTCMillis, boolean shortFormat) {
        Date date = new Date(pTimeUTCMillis);
        return getDayStr(context, date, shortFormat) + ", " + DateFormat.getTimeFormat(context).format(date);
    }

    // /////////////////////////////////////////////////////////////////////////////////////
    //
    // TEST CODE BELOW THIS POINT
    //
    // SHOULD BE TRANSFORMED INTO UNIT TESTS
    //
    // /////////////////////////////////////////////////////////////////////////////////////

    private static void showDSTStatus(Calendar cal) {
        // Test code
        System.out.println(cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE) + ":"
            + cal.get(Calendar.SECOND) + " - In DST: " + cal.getTimeZone().inDaylightTime(cal.getTime()));
    }

    private static void daylightSavingsBehaviourTest() {
        // Test code
        TimeZone tz = TimeZone.getDefault();
        GregorianCalendar cal = new GregorianCalendar(tz);

        cal.set(Calendar.YEAR, 2010);
        cal.set(Calendar.MONTH, Calendar.OCTOBER);
        cal.set(Calendar.DAY_OF_MONTH, 30);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        showDSTStatus(cal);

        cal.add(Calendar.HOUR, 1);
        showDSTStatus(cal);
        long baseTime = cal.getTimeInMillis();

        cal.set(Calendar.HOUR_OF_DAY, 2);
        long twoAMTime = cal.getTimeInMillis();
        showDSTStatus(cal);
        System.out.println("Between midnigth and 2am there are " + (twoAMTime - baseTime) / 3600000 + " hours");

        cal.set(Calendar.HOUR_OF_DAY, 1);
        int h1 = cal.get(Calendar.HOUR_OF_DAY);
        showDSTStatus(cal);

        cal.add(Calendar.HOUR_OF_DAY, 1);
        int h2 = cal.get(Calendar.HOUR_OF_DAY);
        showDSTStatus(cal);

        cal.add(Calendar.HOUR_OF_DAY, 1);
        int h3 = cal.get(Calendar.HOUR_OF_DAY);
        showDSTStatus(cal);

        cal.add(Calendar.HOUR_OF_DAY, 1);
        int h4 = cal.get(Calendar.HOUR_OF_DAY);
        showDSTStatus(cal);

        System.out.println("hours: " + h1 + " / " + h2 + " / " + h3 + " / " + h4);

        cal.set(Calendar.YEAR, 2010);
        cal.set(Calendar.MONTH, Calendar.MARCH);
        cal.set(Calendar.DAY_OF_MONTH, 28);
        cal.set(Calendar.HOUR_OF_DAY, 2);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        showDSTStatus(cal);

    }

    private static int testnum = 1;

    private static void printLine() {
        // Test code

        System.out.println("-------------------------");
    }

    private static void test(TimeStatus ts, boolean b, long l) {

        // Test code

        if ((ts.mIsEnabledNow == b) && (ts.mNextEventTimeMillis == l)) {
            System.out.println("Test " + testnum + ". SUCCESS");
        } else {
            System.out.println("Test " + testnum + ". FAILURE. Expected (" + b + ", " + l + ") - Got ("
                + ts.mIsEnabledNow + ", " + ts.mNextEventTimeMillis + ")");
        }
        testnum++;
    }

    public static void main(String[] args) {

        // Test code

        daylightSavingsBehaviourTest();

        TimeZone tz = TimeZone.getTimeZone("UTC");
        final long MILLIS_IN_DAY = 86400000;
        final int SECONDS_IN_DAY = 86400;
        final int OFFSET = 1001; // millis after start of interval
        test(getAuthorizedTimeInfo(tz, 0, 0, 0, SECONDS_IN_DAY - 1), false, MILLIS_IN_DAY + OFFSET);
        test(getAuthorizedTimeInfo(tz, MILLIS_IN_DAY + MILLIS_IN_DAY / 2, 0, 0, SECONDS_IN_DAY - 1), false, 2
            * MILLIS_IN_DAY + OFFSET);
        test(getAuthorizedTimeInfo(tz, MILLIS_IN_DAY + MILLIS_IN_DAY / 10, 0x3f, 0, SECONDS_IN_DAY / 2), true,
            MILLIS_IN_DAY + SECONDS_IN_DAY / 2 * 1000 + OFFSET);
        test(
            getAuthorizedTimeInfo(tz, MILLIS_IN_DAY + MILLIS_IN_DAY / 10, 0x3f, SECONDS_IN_DAY / 2, SECONDS_IN_DAY - 1),
            false, MILLIS_IN_DAY + MILLIS_IN_DAY / 2 + OFFSET);
        test(
            getAuthorizedTimeInfo(tz, MILLIS_IN_DAY + MILLIS_IN_DAY / 10, 0x3f, SECONDS_IN_DAY / 20, SECONDS_IN_DAY / 2),
            true, MILLIS_IN_DAY + SECONDS_IN_DAY / 2 * 1000 + OFFSET);
        test(
            getAuthorizedTimeInfo(tz, MILLIS_IN_DAY + MILLIS_IN_DAY / 20, 0x3f, SECONDS_IN_DAY / 10, SECONDS_IN_DAY / 2),
            false, MILLIS_IN_DAY + MILLIS_IN_DAY / 10 + OFFSET);

        // Jan 1, 1970 is a Thursday
        printLine();
        test(getAuthorizedTimeInfo(tz, MILLIS_IN_DAY / 2, 0x3f, 0, SECONDS_IN_DAY - 1), true, MILLIS_IN_DAY - 1000
            + OFFSET);
        test(getAuthorizedTimeInfo(tz, MILLIS_IN_DAY / 2, 0x00, 0, SECONDS_IN_DAY - 1), false, MILLIS_IN_DAY + OFFSET);
        test(getAuthorizedTimeInfo(tz, MILLIS_IN_DAY / 2, 0x01, 0, SECONDS_IN_DAY - 1), false, MILLIS_IN_DAY + OFFSET);
        test(getAuthorizedTimeInfo(tz, MILLIS_IN_DAY / 2, 0x02, 0, SECONDS_IN_DAY - 1), false, MILLIS_IN_DAY + OFFSET);
        test(getAuthorizedTimeInfo(tz, MILLIS_IN_DAY / 2, 0x04, 0, SECONDS_IN_DAY - 1), false, MILLIS_IN_DAY + OFFSET);
        test(getAuthorizedTimeInfo(tz, MILLIS_IN_DAY / 2, 0x08, 0, SECONDS_IN_DAY - 1), true, MILLIS_IN_DAY - 1000
            + OFFSET);
        test(getAuthorizedTimeInfo(tz, MILLIS_IN_DAY / 2, 0x10, 0, SECONDS_IN_DAY - 1), false, MILLIS_IN_DAY + OFFSET);
        test(getAuthorizedTimeInfo(tz, MILLIS_IN_DAY / 2, 0x20, 0, SECONDS_IN_DAY - 1), false, MILLIS_IN_DAY + OFFSET);
        test(getAuthorizedTimeInfo(tz, MILLIS_IN_DAY / 2, 0x40, 0, SECONDS_IN_DAY - 1), false, MILLIS_IN_DAY + OFFSET);
        test(getAuthorizedTimeInfo(tz, MILLIS_IN_DAY / 2, 0x40, SECONDS_IN_DAY / 2, SECONDS_IN_DAY - 1), false,
            MILLIS_IN_DAY + MILLIS_IN_DAY / 2 + OFFSET);
        test(getAuthorizedTimeInfo(tz, MILLIS_IN_DAY / 2 - 1, 0x08, SECONDS_IN_DAY / 2, SECONDS_IN_DAY - 1), false,
            MILLIS_IN_DAY / 2 + OFFSET);
        test(getAuthorizedTimeInfo(tz, MILLIS_IN_DAY / 2, 0x08, SECONDS_IN_DAY / 2, SECONDS_IN_DAY - 1), true,
            MILLIS_IN_DAY - 1000 + OFFSET);
        test(getAuthorizedTimeInfo(tz, MILLIS_IN_DAY / 2 + 1, 0x08, SECONDS_IN_DAY / 2, SECONDS_IN_DAY - 1), true,
            MILLIS_IN_DAY - 1000 + OFFSET);

        // Oct 20, 2010 is a Wednesday
        printLine();
        GregorianCalendar today = new GregorianCalendar(tz);
        today.set(Calendar.YEAR, 2010);
        today.set(Calendar.MONTH, Calendar.OCTOBER);
        today.set(Calendar.DAY_OF_MONTH, 20);
        today.set(Calendar.HOUR_OF_DAY, 21);
        today.set(Calendar.MINUTE, 40);
        today.set(Calendar.SECOND, 43);
        today.set(Calendar.MILLISECOND, 102);
        long nowMillis = today.getTimeInMillis();

        // Oct 21, 2010 is a Thursday
        GregorianCalendar tomorrow = new GregorianCalendar(tz);
        tomorrow.set(Calendar.YEAR, 2010);
        tomorrow.set(Calendar.MONTH, Calendar.OCTOBER);
        tomorrow.set(Calendar.DAY_OF_MONTH, 21);
        tomorrow.set(Calendar.HOUR_OF_DAY, 0);
        tomorrow.set(Calendar.MINUTE, 0);
        tomorrow.set(Calendar.SECOND, 0);
        tomorrow.set(Calendar.MILLISECOND, 0);
        long tomorrowMillis = tomorrow.getTimeInMillis();
        test(getAuthorizedTimeInfo(tz, nowMillis, 0x04, 0, SECONDS_IN_DAY - 1), true, tomorrowMillis - 1000 + OFFSET);
        test(getAuthorizedTimeInfo(tz, nowMillis, 0, 0, SECONDS_IN_DAY - 1), false, tomorrowMillis + OFFSET);
        test(getAuthorizedTimeInfo(tz, nowMillis, 0, 1000, 2000), false, tomorrowMillis + 1000 * 1000 + OFFSET);
        test(getAuthorizedTimeInfo(tz, nowMillis, 0x3f, 21 * 3600, 22 * 3600), true, tomorrowMillis - 2 * 3600 * 1000
            + OFFSET);
        test(getAuthorizedTimeInfo(tz, nowMillis, 0x3f, 10 * 3600, 21 * 3600), false, tomorrowMillis + 10 * 3600 * 1000
            + OFFSET);
        test(getAuthorizedTimeInfo(tz, nowMillis, 0x3f, 22 * 3600, 23 * 3600), false, tomorrowMillis - 2 * 3600 * 1000
            + OFFSET);

        printLine();

        // Oct 31 is DST switch day in CET timezone
        // First test Oct 30, 11:30 in a policy which is enabled between 00:00
        // and 12:00
        tz = TimeZone.getTimeZone("CET");
        GregorianCalendar dstTestDate = new GregorianCalendar(tz);
        dstTestDate.set(Calendar.YEAR, 2010);
        dstTestDate.set(Calendar.MONTH, Calendar.OCTOBER);
        dstTestDate.set(Calendar.DAY_OF_MONTH, 30);
        dstTestDate.set(Calendar.HOUR_OF_DAY, 1);
        dstTestDate.set(Calendar.MINUTE, 0);
        dstTestDate.set(Calendar.SECOND, 0);
        dstTestDate.set(Calendar.MILLISECOND, 0);
        // It is Oct 30, 2010 at 01:00 hence 11 hours to go till end of interval
        test(getAuthorizedTimeInfo(tz, dstTestDate.getTimeInMillis(), 0x7f, 0, SECONDS_IN_DAY / 2), true,
            dstTestDate.getTimeInMillis() + ((11 * 60) * 60 * 1000) + 1001);

        // It is Oct 31, 2010 at 01:00 hence 12 hours to go till end of interval
        dstTestDate.set(Calendar.DAY_OF_MONTH, 31);
        test(getAuthorizedTimeInfo(tz, dstTestDate.getTimeInMillis(), 0x7f, 0, SECONDS_IN_DAY / 2), true,
            dstTestDate.getTimeInMillis() + ((12 * 60) * 60 * 1000) + 1001);

        // It is Oct 31, 2010 at 23:00 - policy ends at 23:59:59 (+ 1001 millis)
        dstTestDate.set(Calendar.HOUR_OF_DAY, 23);
        test(getAuthorizedTimeInfo(tz, dstTestDate.getTimeInMillis(), 0x7f, 0, SECONDS_IN_DAY - 1), true,
            dstTestDate.getTimeInMillis() + (60 * 60 * 1000) + 1);

        dstTestDate.set(Calendar.YEAR, 2011);
        dstTestDate.set(Calendar.MONTH, Calendar.MARCH);
        dstTestDate.set(Calendar.DAY_OF_MONTH, 27);
        dstTestDate.set(Calendar.HOUR_OF_DAY, 3);
        dstTestDate.set(Calendar.MINUTE, 59);
        dstTestDate.set(Calendar.SECOND, 59);
        dstTestDate.set(Calendar.MILLISECOND, 0);
        test(getAuthorizedTimeInfo(tz, dstTestDate.getTimeInMillis(), 0x7f, 4 * 60 * 60, 5 * 60 * 60), false,
            dstTestDate.getTimeInMillis() + 2001);
        dstTestDate.set(Calendar.HOUR_OF_DAY, 4);
        test(getAuthorizedTimeInfo(tz, dstTestDate.getTimeInMillis(), 0x7f, 4 * 60 * 60, 5 * 60 * 60), true,
            dstTestDate.getTimeInMillis() + 2001);
        dstTestDate.set(Calendar.HOUR_OF_DAY, 1);
        dstTestDate.set(Calendar.MINUTE, 0);
        dstTestDate.set(Calendar.SECOND, 0);
        // On Mar 27, 2011: between 01:00 and 04:00 there are 2 hours
        test(getAuthorizedTimeInfo(tz, dstTestDate.getTimeInMillis(), 0x7f, 4 * 60 * 60, 5 * 60 * 60), false,
            dstTestDate.getTimeInMillis() + (2 * 60 * 60 * 1000) + 1001);
        dstTestDate.set(Calendar.DAY_OF_MONTH, 28);

        // On Mar 28, 2011: between 01:00 and 04:00 there are 3 hours
        test(getAuthorizedTimeInfo(tz, dstTestDate.getTimeInMillis(), 0x7f, 4 * 60 * 60, 5 * 60 * 60), false,
            dstTestDate.getTimeInMillis() + (3 * 60 * 60 * 1000) + 1001);

    }
}

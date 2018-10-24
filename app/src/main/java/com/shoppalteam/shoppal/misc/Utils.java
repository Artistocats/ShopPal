package com.shoppalteam.shoppal.misc;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.math.BigDecimal;

public final class Utils {
    public static final BigDecimal SECONDS_PER_HOUR = BigDecimal.valueOf(DateTimeConstants.SECONDS_PER_HOUR);
    public static final BigDecimal HOURS_PER_DAY = BigDecimal.valueOf(DateTimeConstants.HOURS_PER_DAY);
    public static final DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    public static final int DIGITS_PRECISION = 16;

    public static String getNow() {
        return dateTimeToString(DateTime.now());
    }

    public static DateTime getDateTimeNow() {
        return stringToDateTime(getNow());
    }


    public static Duration getDuration(String start, String end) {
        DateTime startDateTime = stringToDateTime(start);
        DateTime endDateTime = stringToDateTime(end);

        return new Duration(startDateTime, endDateTime);
    }

    public static DateTime stringToDateTime(String s) {
        if(s!=null)
            return new DateTime(formatter.parseDateTime(s));
        else
            return null;
    }

    public static String dateTimeToString(DateTime dt) {
        if(dt!=null)
            return dt.toString(formatter);
        else
            return null;
    }
}

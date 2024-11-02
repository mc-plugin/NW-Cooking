package com.neoworld.cooking.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DateUtils {

    public static String DEFAULT_DATE_FORMAT = "yyyy.MM.dd/HH:mm:ss";
    public static String HOUR_DATE_FORMAT = "HH:mm:ss";

    public static String getDateStr(long timeStamp, String format) {
        if (format==null || format.equals("")) return null;
        Date date = new Date(timeStamp);
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        return formatter.format(date);
    }

    public static String getTimerFormat(long milliseconds, String format) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("GMT"));
        calendar.setTimeInMillis(milliseconds);

        SimpleDateFormat sdf = new SimpleDateFormat(format);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

        return sdf.format(calendar.getTime());
    }
}

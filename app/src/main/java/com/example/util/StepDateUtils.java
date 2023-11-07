package com.example.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

class StepDateUtils {

    private static ThreadLocal<SimpleDateFormat> SIMPLE_DATE_FORMAT = new ThreadLocal<>();

    private static SimpleDateFormat getDateFormat() {
        SimpleDateFormat df = SIMPLE_DATE_FORMAT.get();
        if (df == null) {
            df = new SimpleDateFormat();
            SIMPLE_DATE_FORMAT.set(df);
        }
        return df;
    }


    public static String getCurrentDate(String pattern) {
        getDateFormat().applyPattern(pattern);
        Date date = new Date(System.currentTimeMillis());
        return getDateFormat().format(date);
    }

    private static long getDateMillis(String dateString, String pattern) {
        long millionSeconds = 0;
        getDateFormat().applyPattern(pattern);
        try {
            millionSeconds = getDateFormat().parse(dateString).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return millionSeconds;
    }


    private static String dateFormat(long millis, String pattern) {
        getDateFormat().applyPattern(pattern);
        Date date = new Date(millis);
        return getDateFormat().format(date);
    }


    private static String dateFormat(String dateString, String oldPattern, String newPattern) {
        long millis = getDateMillis(dateString, oldPattern);
        if (0 == millis) {
            return dateString;
        }
        return dateFormat(millis, newPattern);
    }

}

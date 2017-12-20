package com.orengesunshine.chatory.util;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateTimeUtils {

    public static final String TAG = DateTimeUtils.class.getSimpleName();

    private static String pattern = "yyyy/MM/dd HH:mm";

    public static Date getDateFromString(String sDate){
        SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.US);
        try {
            return format.parse(sDate);
        } catch (ParseException e) {
            Log.d(TAG, "getDateFromString: ");
            return null;
        }
    }
}

package com.orengesunshine.chatory.util;

import android.util.Log;

import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateTimeUtils {

    public static final String TAG = DateTimeUtils.class.getSimpleName();

    private static String pattern = "yyyy/MM/dd HH:mm";
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd(EEE)",new Locale("en","US"));

    public static Date getDateFromString(String sDate){
        SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.US);
        try {
            return format.parse(sDate);
        } catch (ParseException e) {
            Log.d(TAG, "getDateFromString: ");
            return null;
        }
    }

    public static boolean isDate(String line){
        dateFormat.setLenient(false); //has to match day of week, go wrong with time zone if not set?
        try {
            dateFormat.parse(line);
            return true;
        } catch (ParseException e) {
            //means it's not a date so return false
            return false;
        }
    }

    public static boolean isAOlderDateThanB(String dateA,String dateB){
        try {
            Date a = dateFormat.parse(dateA);
            Date b = dateFormat.parse(dateB);
            return a.before(b);
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean isTime(String line){
        SimpleDateFormat format = new SimpleDateFormat("h:mm",new Locale("en","US"));
        try {
            format.parse(line);
            return true;
        } catch (ParseException e) {
            //means it's not time so return false

            return false;
        }
    }
}

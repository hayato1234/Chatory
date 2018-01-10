package com.orengesunshine.chatory.util;

import android.content.SharedPreferences;
import android.net.Uri;

import com.orengesunshine.chatory.ChatoryApp;

public class PrefUtil {

    private static final String ICON_URI = "icon_uri";

    public static void saveString(String key, String value){
        SharedPreferences.Editor editor = ChatoryApp.getSharedPreferences().edit();
        editor.putString(key,value);
        editor.apply();
    }

    public static String getString(String key,String defaultValue){
        return ChatoryApp.getSharedPreferences().getString(key,defaultValue);
    }
    public static String getString(String key){
        return ChatoryApp.getSharedPreferences().getString(key,null);
    }

    public static void deleteString(String key){
        SharedPreferences.Editor editor = ChatoryApp.getSharedPreferences().edit();
        editor.remove(key);
        editor.apply();
    }

    public static void saveBoolean(String key, boolean value){
        SharedPreferences.Editor editor = ChatoryApp.getSharedPreferences().edit();
        editor.putBoolean(key,value);
        editor.apply();
    }

    public static boolean getBoolean(String key,boolean defaultValue){
        return ChatoryApp.getSharedPreferences().getBoolean(key,defaultValue);
    }
    public static boolean getBoolean(String key){
        return ChatoryApp.getSharedPreferences().getBoolean(key,false);
    }

    public static void saveIconUri(String name, Uri uri){
        if (uri!=null){
            saveString(ICON_URI+name,uri.toString());
        }
    }

    public static String getIconUri(String name){
        return getString(ICON_URI+name,null);
    }

}

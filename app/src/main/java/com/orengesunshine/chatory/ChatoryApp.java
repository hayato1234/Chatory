package com.orengesunshine.chatory;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class ChatoryApp extends Application {

    private static ChatoryApp mApp;
    private SharedPreferences mPrefs;


    @Override
    public void onCreate() {
        super.onCreate();
        mApp = this;
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
    }

    public static ChatoryApp getmApp(){
        return mApp;
    }

    public static SharedPreferences getSharedPreferences(){
        return getmApp().mPrefs;
    }


}

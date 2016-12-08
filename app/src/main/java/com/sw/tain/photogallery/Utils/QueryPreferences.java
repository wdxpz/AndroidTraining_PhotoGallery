package com.sw.tain.photogallery.Utils;

import android.content.ContentValues;
import android.content.Context;
import android.preference.PreferenceManager;

/**
 * Created by home on 2016/12/5.
 */

public class QueryPreferences {
    private static final String PREF_SEARCH_QUERY = "search query";
    private static final String PREF_IS_SERICE_ON = "is service on";
    public static String getQueryPreferences(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_SEARCH_QUERY, null);
    }

    public static void setQueryPreferences(Context context, String query){
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(PREF_SEARCH_QUERY, query)
                .apply();
    }

    public static boolean getServiceOnPreference(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PREF_IS_SERICE_ON, false);
    }

    public static void setServiceOnPreference(Context context, boolean isOn){
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(PREF_IS_SERICE_ON, isOn)
                .apply();
    }
}

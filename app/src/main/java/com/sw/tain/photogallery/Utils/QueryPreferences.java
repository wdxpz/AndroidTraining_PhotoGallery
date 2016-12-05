package com.sw.tain.photogallery.Utils;

import android.content.ContentValues;
import android.content.Context;
import android.preference.PreferenceManager;

/**
 * Created by home on 2016/12/5.
 */

public class QueryPreferences {
    private static final String PREF_SEARCH_QUERY = "search query";
    public static String getQueryPreferences(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_SEARCH_QUERY, null);
    }

    public static void setQueryPreferences(Context context, String query){
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(PREF_SEARCH_QUERY, query)
                .apply();
    }
}

package com.sw.tain.photogallery.Reciever;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.sw.tain.photogallery.Service.PollService;
import com.sw.tain.photogallery.Utils.QueryPreferences;

/**
 * Created by home on 2016/12/8.
 */

public class StartupReciever extends BroadcastReceiver {
    private static final String TAG = "StartupReciever";

    @Override
    public void onReceive(Context context, Intent intent) {
        PollService.startPollService(context, QueryPreferences.getServiceOnPreference(context));
        Log.d(TAG, "Startup Reciever startup!");
    }
}

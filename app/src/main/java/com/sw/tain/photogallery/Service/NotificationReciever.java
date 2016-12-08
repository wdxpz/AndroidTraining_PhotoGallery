package com.sw.tain.photogallery.Service;

import android.app.Activity;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

/**
 * Created by home on 2016/12/8.
 */

public class NotificationReciever extends BroadcastReceiver {
    private static final String TAG = "NotificationReciever";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "NotificationReciever got the broadcast");
        if(getResultCode()!= Activity.RESULT_OK){
            Log.d(TAG, "show notification canceled!");
            return;
        }
        Log.d(TAG, "show notification!");
        Notification notification = intent.getParcelableExtra(PollService.NOTIFICATION);
        NotificationManagerCompat nmc = NotificationManagerCompat.from(context);
        nmc.notify(0, notification);

    }
}

package com.sw.tain.photogallery.Utils;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.net.ConnectivityManagerCompat;
import android.util.Log;

import com.sw.tain.photogallery.PhotoGalleryPagerActivity;

/**
 * Created by home on 2016/12/6.
 */

public class PollService extends IntentService {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    private static final String TAG = "Poll Service";
    private static final int POLL_INTERVAL = 100;
    public PollService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if(!isNetworkAvailableAndConnected()){
            return;
        }
        Log.d(TAG, "Poll service started with intent!");

        Intent i = PhotoGalleryPagerActivity.newIntent(this, 3);
        PendingIntent pt = PendingIntent.getActivity(this, 0, i, 0); //PendingIntent.getActivity different with PendingIntent.getService

        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("Poll Service")
                .setContentInfo("Poll Service Started!")
                .setTicker("Poll")
                .setSmallIcon(android.R.drawable.ic_menu_report_image)
                .setContentIntent(pt)
                .setAutoCancel(true)
                .build();

        NotificationManagerCompat nm = NotificationManagerCompat.from(this);
        nm.notify(0, notification);


    }

    public static Intent newIntent(Context context){
        Intent i = new Intent(context, PollService.class);
        return i;
    }

    public static void startPollService(Context context, boolean isOn){
        Intent i = PollService.newIntent(context);
        PendingIntent pt = PendingIntent.getService(context, 0, i, 0);

        AlarmManager alarmManager = (AlarmManager)context.getSystemService(ALARM_SERVICE);
        if(isOn){
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), POLL_INTERVAL, pt);
        }else
        {
            alarmManager.cancel(pt);
            pt.cancel();
        }
    }

    public static boolean isPollSeriveOn(Context context){
        Intent i = PollService.newIntent(context);
        PendingIntent pt = PendingIntent.getService(context, 0, i, PendingIntent.FLAG_NO_CREATE);

        return pt!=null;
    }

    private boolean isNetworkAvailableAndConnected() {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);

        boolean isNetworkAvailable = cm.getActiveNetworkInfo()!=null;
        boolean isNetworkConnected = isNetworkAvailable && cm.getActiveNetworkInfo().isConnected();
        return isNetworkConnected;
    }
}

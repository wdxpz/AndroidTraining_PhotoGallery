package com.sw.tain.photogallery.Service;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.util.List;

/**
 * Created by home on 2016/12/6.
 * refer http://blog.csdn.net/zhangyongfeiyong/article/details/52130413 for detail of JobService, JobScheduler
 */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class PollJobService extends JobService {
    private static final String TAG = "Poll JobService";
    private static final int JOB_ID = 1;
    private JobParameters mParas;
    private PollTask mTask;


    @Override
    public boolean onStartJob(JobParameters params) {

        mTask = new PollTask();
        mTask.execute(params);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        if(mTask!=null){
            mTask.cancel(true);
        }
        return false;
    }

    public static void setPollJobServiceOn(Context context, boolean isOn){
//        JobScheduler js = (JobScheduler)context.getSystemService(JOB_SCHEDULER_SERVICE);
        JobScheduler js = (JobScheduler)context.getSystemService(context.JOB_SCHEDULER_SERVICE);

        JobInfo jbInfo = new JobInfo.Builder(JOB_ID, new ComponentName(context, PollJobService.class))
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
                .setPersisted(true)     //必须设置app的"android.permission.RECEIVE_BOOT_COMPLETED"权限
                .setPeriodic(1000)
                .build();

        if(isOn){
            js.schedule(jbInfo);
        }else{
            js.cancel(JOB_ID);
        }
    }

    public static boolean isPollJobServiceOn(Context context){
//        JobScheduler js = (JobScheduler)context.getSystemService(JOB_SCHEDULER_SERVICE);

        JobScheduler js = (JobScheduler)context.getSystemService(context.JOB_SCHEDULER_SERVICE);
        List<JobInfo> jobList = js.getAllPendingJobs();
        for(JobInfo job : jobList){
            if(job.getId()==JOB_ID) {
                return true;
            }
        }
        return false;
    }

    protected class PollTask extends AsyncTask<JobParameters, Void, Void>{

        @Override
        protected Void doInBackground(JobParameters... params) {
            mParas = params[0];
            Log.d(TAG, "Poll JobService started!");

            jobFinished(mParas, false);
            return null;
        }
    }
}

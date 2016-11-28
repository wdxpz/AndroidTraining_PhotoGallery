package com.sw.tain.photogallery;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.sw.tain.photogallery.Utils.FlickerFetcher;

public class PhotoGalleryActivity extends AppCompatActivity {




//    @Override
//    protected Fragment CreateFragment() {
//
//       return PhotoGalleryFragment.newInstance(0);
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);
//        new AsyncGalleryTask().execute();
    }

    private void startPagerActivity(int pagenumber){
        Intent intent = PhotoGalleryPagerActivity.newIntent(this, pagenumber);
        startActivity(intent);
    }

//    private class AsyncGalleryTask extends AsyncTask<Void, Void, Integer> {
//
//        @Override
//        protected Integer doInBackground(Void... params) {
//            return new FlickerFetcher().FetchTotoalPages();
//        }
//
//        @Override
//        protected void onPostExecute(Integer i) {
//            super.onPostExecute(i);
//            startPagerActivity(i);
//        }
//    }
}

package com.sw.tain.photogallery;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.sw.tain.photogallery.Utils.FlickerFetcher;

import java.util.UUID;

/**
 * Created by home on 2016/11/24.
 */

public class PhotoGalleryPagerActivity extends AppCompatActivity {

    private static final String GALLERY_PAGE_NUM = "page_numb";
    private static final String TOTAL_PAGE_NUMBER = "total_page_number";
    private ViewPager mViewPager;
    private int mPageNum;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_gallery_pager);

        Intent intent = getIntent();
        if(intent != null){
            mPageNum = intent.getIntExtra(TOTAL_PAGE_NUMBER, 0);
        }


        mViewPager = (ViewPager)findViewById(R.id.activity_photo_gallery_pager);
        mViewPager.setOffscreenPageLimit(0);

        FragmentManager fm = getSupportFragmentManager();
        mViewPager.setAdapter(new FragmentStatePagerAdapter(fm) {
            @Override
            public int getCount() {
                return mPageNum;
            }

            @Override
            public Fragment getItem(int position) {
                //if(position==0) position=1;
                PhotoGalleryFragment f = PhotoGalleryFragment.newInstance(position);
                return f;
            }
        });


    }

    public static Intent newIntent(Context packageContext, int pages){
        Intent intent = new Intent(packageContext, PhotoGalleryPagerActivity.class);
        intent.putExtra(TOTAL_PAGE_NUMBER, pages);
        return intent;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(GALLERY_PAGE_NUM, mPageNum);
    }

}

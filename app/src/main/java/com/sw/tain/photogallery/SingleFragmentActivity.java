package com.sw.tain.photogallery;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by home on 2016/10/26.
 */

public abstract class SingleFragmentActivity extends AppCompatActivity {

    protected abstract Fragment CreateFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_fragment);
        setContentView(getLayoutRes());

        FragmentManager fm = getSupportFragmentManager();
        Fragment f = fm.findFragmentById(R.id.fragment_container);

        if(f==null){
            f = CreateFragment();
            fm.beginTransaction().add(R.id.fragment_container, f).commit();
        }
    }

    @LayoutRes
    protected int getLayoutRes(){
        return R.layout.activity_fragment;
    }
}

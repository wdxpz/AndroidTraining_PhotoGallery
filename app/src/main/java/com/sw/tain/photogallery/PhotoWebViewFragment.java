package com.sw.tain.photogallery;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.sw.tain.photogallery.Utils.GalleryItem;

/**
 * Created by home on 2016/12/8.
 */

public class PhotoWebViewFragment extends Fragment {
    private WebView mWebView;
    private ProgressBar mProgressBar;

    public static PhotoWebViewFragment newInstance(Uri photoUri) {

        Bundle args = new Bundle();
        args.putParcelable(GalleryItem.PHOTO_URI, photoUri);

        PhotoWebViewFragment fragment = new PhotoWebViewFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragement_photo_web_view, container, false);

        mProgressBar = (ProgressBar)v.findViewById(R.id.progressBar);
        mProgressBar.setMax(100);

        mWebView = (WebView)v.findViewById(R.id.fragment_photo_web_view);
        mWebView.getSettings().setJavaScriptEnabled(true);
        //如果不重写shouldOverrideUrlLoading, WebView的默认行为将直接打开浏览器
        mWebView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return false;
            }
        });

        mWebView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if(newProgress==100){
                    mProgressBar.setVisibility(View.GONE);
                }else{
                    mProgressBar.setVisibility(View.VISIBLE);
                    mProgressBar.setProgress(newProgress);
                }
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                AppCompatActivity activity = (AppCompatActivity)getActivity();
                activity.getSupportActionBar().setTitle(title);
            }
        });
        mWebView.loadUrl((getArguments().getParcelable(GalleryItem.PHOTO_URI)).toString());




        return v;
    }
}

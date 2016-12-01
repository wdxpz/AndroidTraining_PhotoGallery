package com.sw.tain.photogallery.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.util.LruCache;

import com.sw.tain.photogallery.PhotoGalleryFragment;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static android.R.attr.bitmap;
import static android.R.attr.defaultHeight;
import static android.R.attr.elegantTextHeight;

/**
 * Created by home on 2016/11/29.
 */

/**
*refer http://blog.csdn.net/lmj623565791/article/details/38377229,
* http://blog.csdn.net/lmj623565791/article/details/47079737/
* for concept of Looper, Handler, Message, and HandlerThread

*refer ImageLoader in http://blog.csdn.net/lmj623565791/article/details/38476887 for LruCache and image resampling
*http://blog.sina.com.cn/s/blog_5da93c8f0102uxee.html for LruCache

*use LocalFile, LruCache, network to 三级缓存图片机制，refer: http://blog.sina.com.cn/s/blog_5da93c8f0102uxee.html
 */

public class ThumbnailDownloader extends HandlerThread {
    private static final String TAG = "ThumbernailDownloader";
    private static final int MSG_THUMBNAIL_DOWLNOAD_INTO_VIEW = 0;
    private static final int MSG_THUMBNAIL_DOWLNOAD_ONLY = 1;
    private final Handler mMainThreadHandler;
    private  FileUtils mLocalCache;
    private  MemoryCacheUtils mMemoryCache;
    public Handler mRequestHandler;
    private boolean mIsQuit = false;
    private ThumbnailDownloaderListner mThumbnailDownloaderListner;
    private Set<String> mDonwloadSet = Collections.synchronizedSet(new HashSet<String>());

    public interface ThumbnailDownloaderListner{
//        void onThumbnailDownloaderListner(T target, Bitmap bitmap);
//        boolean isDownlaodedMatchView(T target, String url);
        void onThumbnailDownloaderListner(String url, Bitmap bitmap);
    }

    public void setThumbnailDownloaderListner(ThumbnailDownloaderListner lisnter){
        mThumbnailDownloaderListner = lisnter;
    }

    public ThumbnailDownloader(Context context, Handler mainThreadHandler) {
        super(TAG);
        mMainThreadHandler = mainThreadHandler;

        mMemoryCache = new MemoryCacheUtils();

        mLocalCache = new FileUtils(context);

    }





    public void downloadImage(final String itemUrl) {

        final Bitmap bitmap = getCacheBitmap(itemUrl);
        if(bitmap!=null){
            //return bitmap;
            updateUI(itemUrl, bitmap);
        }else{
            if(mDonwloadSet.contains(itemUrl)){
                return;
            }
            mRequestHandler.obtainMessage(MSG_THUMBNAIL_DOWLNOAD_INTO_VIEW, itemUrl).sendToTarget();
        }
    }

    public Bitmap getCacheBitmap(String url) {
        String subUrl = url.replaceAll("[^\\w]", "");
        Bitmap bitmap = mMemoryCache.getBitmapFromLruCache(subUrl);
        if(bitmap!=null){
            Log.d("Downloader", "loaded from memory!");
            return bitmap;
        }
        if(mLocalCache.isFileExists(subUrl) && mLocalCache.getFileSize(subUrl)!=0){
            bitmap = mLocalCache.getBitmap(subUrl);
            mMemoryCache.addBitmapToLruCache(subUrl, bitmap);
            Log.d("Downloader", "loaded from local storage!");
            return bitmap;
        }
        return null;
    }


    @Override
    protected void onLooperPrepared() {
        super.onLooperPrepared();
        mRequestHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case MSG_THUMBNAIL_DOWLNOAD_INTO_VIEW:
//                        Log.d(TAG, "handle download request with url: " + mRequestMap.get(target));
//                        T target = (T) msg.obj;
//                        handleRequest(target);
                        String url = (String)msg.obj;

                        Log.d(TAG, "handle download request with url: " + url);
                        handleRequest(url);
                        break;
                    default:
                        return;
                }
            }
        };
    }

    private void handleRequest(String url){

        Bitmap bitmap = downloadImageFromUrl(url);
        if(bitmap != null){
            String subUrl = url.replaceAll("[^\\w]", "");
            mMemoryCache.addBitmapToLruCache(subUrl, bitmap);
            mLocalCache.saveBitmap(subUrl, bitmap);
            mDonwloadSet.add(url);
            updateUI(url, bitmap);
        }

    }

    private void updateUI(final String url, final Bitmap bitmap) {
        mMainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mIsQuit) {
                    return;
                }
                mThumbnailDownloaderListner.onThumbnailDownloaderListner(url, bitmap);
            }
        });
    }

    private Bitmap downloadImageFromUrl(String url){

        Bitmap bitmap=null;

        if(url == null) return null;

        byte[] bitmapByteArray = new byte[0];
        try {
            bitmapByteArray = new FlickerFetcher().getUrlByteArray(url);
            bitmap = BitmapFactory.decodeByteArray(bitmapByteArray, 0, bitmapByteArray.length);
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "downloaded failed! with url: " + url);
            mRequestHandler.obtainMessage(MSG_THUMBNAIL_DOWLNOAD_INTO_VIEW, url).sendToTarget();
        }

//        bitmap = new FlickerFetcher().getBitmap(url);
        Log.d(TAG, "downloaded from url: " + url);

        return bitmap;
    }

//    private void handleRequest(final T target) {
//        final String url = mRequestMap.get(target);
//        Bitmap bitmap;
//
//
//        if((bitmap = mMemoryCache.getBitmapFromLruCache(url))==null){
//            if((bitmap = mLocalCache.getLocalCache(url))==null){
//                if(!mDownloadingSet.contains(url))
//                    bitmap = downloadImage(url);
//                else
//                    while(mDownloadingSet.contains(url)){
//
//                    }
//            }else{
//                Log.d("Donwloader", "loaded from local storage!");
//            }
//        }else{
//            Log.d("Donwloader", "loaded from memory!");
//        }
//
//        final Bitmap bitmap2 = bitmap;
//        if(!mThumbnailDownloaderListner.isDownlaodedMatchView(target, url))
//        {
//            Log.d(TAG, "downloaded not match the view: /n"
//                    + "view tag: " + ((PhotoGalleryFragment.GalleryHolder) target).mThumbnailImageView.getTag().toString() + "/n"
//                    + "downloaded url: "+ url);
//        }else{
//            mMainThreadHandler.post(new Runnable() {
//                @Override
//                public void run() {
//                    if (mIsQuit || mRequestMap.get(target) != url) {
//                        return;
//                    }
//                    mRequestMap.remove(target);
//                    mThumbnailDownloaderListner.onThumbnailDownloaderListner(target, bitmap2);
//                }
//            });
//        }
//    }

    @Override
    public boolean quit() {
        mIsQuit = true;
        return super.quit();
    }

    public void clearQueue(){
        mRequestHandler.removeMessages(MSG_THUMBNAIL_DOWLNOAD_INTO_VIEW);
    }

    public void clear(){
        mRequestHandler.removeMessages(MSG_THUMBNAIL_DOWLNOAD_INTO_VIEW);
        mMemoryCache.clearCache();
        mLocalCache.deleteFiles();
    }
}

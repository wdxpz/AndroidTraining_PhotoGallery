package com.sw.tain.photogallery.Utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by home on 2016/11/29.
 */

public class ThumbnailDownloader<T> extends HandlerThread {
    private static final String TAG = "ThumbernailDownloader";
    private static final int MSG_THUMBNAIL_DOWLNOAD = 0;
    private final Handler mMainThreadHandler;
    private ConcurrentMap<T, String> mRequestMap = new ConcurrentHashMap<>();
    private Handler mRequestHandler;
    private boolean mIsQuit = false;
    private ThumbnailDownloaderListner<T> mThumbnailDownloaderListner;

    public ThumbnailDownloader(Handler mainThreadHandler) {
        super(TAG);
        mMainThreadHandler = mainThreadHandler;
    }

    @Override
    public boolean quit() {
        mIsQuit = true;
        return super.quit();
    }

    public void enqueueThumbnail(T galleryHolder, String itemUrl) {
        Log.d(TAG, "get thumbnail request with url: " + itemUrl);
        if(itemUrl==null){
            mRequestMap.remove(galleryHolder);
        }else{
            mRequestMap.put(galleryHolder, itemUrl);
            mRequestHandler.obtainMessage(MSG_THUMBNAIL_DOWLNOAD, galleryHolder).sendToTarget();
        }
    }

    @Override
    protected void onLooperPrepared() {
        super.onLooperPrepared();
        mRequestHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                T target = (T) msg.obj;
                Log.d(TAG, "handle download request with url: " + mRequestMap.get(target));
                handeRequest(target);
            }
        };
    }

    private void handeRequest(final T target) {
        final String url = mRequestMap.get(target);

        if(url == null) return;

        try {
            byte[] bitmapByteArray = new FlickerFetcher().getUrlByteArray(mRequestMap.get(target));
            final Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapByteArray, 0, bitmapByteArray.length);
            Log.d(TAG, "bitmap downloaded!");
            mMainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    if(mIsQuit || mRequestMap.get(target)!=url){
                        return;
                    }
                    mRequestMap.remove(target);
                    mThumbnailDownloaderListner.onThumbnailDownloaderListner(target, bitmap);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "bitmap downloade failed!");
        }


    }

    public interface ThumbnailDownloaderListner<T>{
        void onThumbnailDownloaderListner(T target, Bitmap bitmap);
    }

    public void setThumbnailDownloaderListner(ThumbnailDownloaderListner<T> lisnter){
        mThumbnailDownloaderListner = lisnter;
    }

    public void clearQueue(){
        mRequestHandler.removeMessages(MSG_THUMBNAIL_DOWLNOAD);
    }
}

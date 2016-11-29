package com.sw.tain.photogallery.Utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.util.LruCache;

import com.sw.tain.photogallery.PhotoGalleryFragment;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by home on 2016/11/29.
 */

/**
*refer http://blog.csdn.net/lmj623565791/article/details/38377229,
* http://blog.csdn.net/lmj623565791/article/details/47079737/
* for concept of Looper, Handler, Message, and HandlerThread

*refer ImageLoader in http://blog.csdn.net/lmj623565791/article/details/38476887 for LruCache and image resampling
*http://blog.sina.com.cn/s/blog_5da93c8f0102uxee.html for LruCache

*use LocalCacheUtils, LruCache, network to 三级缓存图片机制，refer: http://blog.sina.com.cn/s/blog_5da93c8f0102uxee.html
 */

public class ThumbnailDownloader<T> extends HandlerThread {
    private static final String TAG = "ThumbernailDownloader";
    private static final int MSG_THUMBNAIL_DOWLNOAD = 0;
    private final Handler mMainThreadHandler;
    private final LruCache<String, Bitmap> mLruCache;
    private ConcurrentMap<T, String> mRequestMap = new ConcurrentHashMap<>();
    private Handler mRequestHandler;
    private boolean mIsQuit = false;
    private ThumbnailDownloaderListner<T> mThumbnailDownloaderListner;

    public ThumbnailDownloader(Handler mainThreadHandler) {
        super(TAG);
        mMainThreadHandler = mainThreadHandler;
        mLruCache = initLruCache();
    }

    private LruCache<String, Bitmap> initLruCache() {

        // 获取应用程序最大可用内存
        int maxMemory = (int) (Runtime.getRuntime().maxMemory()/1024);
        int cacheSize = maxMemory / 8;
        LruCache<String, Bitmap> lruCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String url, Bitmap value) {
                //return value.getRowBytes() * value.getHeight();
                return value.getByteCount() / 1024;
            }
        };
        return lruCache;
    }

    private Bitmap getBitmapFromLruCache(String url) {
        return mLruCache.get(url);
    }

    private void addBitmapToLruCache(String url, Bitmap bitmap){
        if (getBitmapFromLruCache(url) == null)
        {
            if (bitmap != null)
                mLruCache.put(url, bitmap);
        }
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

        final Bitmap bitmap;


        if(getBitmapFromLruCache(url) != null){
            bitmap = getBitmapFromLruCache(url);
            Log.d(TAG, "bitmap loaded from LruCache!");
        }else
        {
            byte[] bitmapByteArray = new byte[0];
            try {
                bitmapByteArray = new FlickerFetcher().getUrlByteArray(mRequestMap.get(target));
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "bitmap downloade failed! with url: " + mRequestMap.get(target));
            }
            bitmap = BitmapFactory.decodeByteArray(bitmapByteArray, 0, bitmapByteArray.length);
            Log.d(TAG, "bitmap downloaded from url: " + mRequestMap.get(target));
            if(bitmap != null) addBitmapToLruCache(url, bitmap);
        }
        PhotoGalleryFragment.GalleryHolder holder = (PhotoGalleryFragment.GalleryHolder)target;
        if(!holder.mThumbnailImageView.getTag().toString().equals(url))
        {
            Log.d(TAG, "downloaed not match the view");
        }else
        {
            mMainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mIsQuit || mRequestMap.get(target) != url) {
                        return;
                    }
                    mRequestMap.remove(target);
                    mThumbnailDownloaderListner.onThumbnailDownloaderListner(target, bitmap);
                }
            });
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
        mLruCache.evictAll();
    }
}

package com.sw.tain.photogallery.Utils;

import android.graphics.Bitmap;
import android.util.LruCache;

/**
 * Created by home on 2016/11/30.
 */

public class MemoryCacheUtils {

    private  static LruCache<String, Bitmap> mLruCache;

    public MemoryCacheUtils() {
        initLruCache();
    }

    private void initLruCache() {

        // 获取应用程序最大可用内存
        int maxMemory = (int) (Runtime.getRuntime().maxMemory());
        int cacheSize = maxMemory / 8;
        mLruCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String url, Bitmap value) {
                return value.getRowBytes() * value.getHeight();
            }
        };
    }

    public Bitmap getBitmapFromLruCache(String url) {
        if(url==null) return null;
        return mLruCache.get(url);
    }

    public void addBitmapToLruCache(String url, Bitmap bitmap){
        if (getBitmapFromLruCache(url) == null && bitmap != null)
        {
            mLruCache.put(url, bitmap);
        }
    }

    public void clearCache(){
        mLruCache.evictAll();
    }
}

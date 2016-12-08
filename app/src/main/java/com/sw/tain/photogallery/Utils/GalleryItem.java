package com.sw.tain.photogallery.Utils;

import android.net.Uri;

/**
 * Created by home on 2016/11/23.
 */

public class GalleryItem {
    private String mCaption;
    private String mId;
    private String mUrl;
    private String mOwner;

    public static final String PHOTO_URI = "photo uri";

    public String getCaption() {
        return mCaption;
    }

    public void setCaption(String caption) {
        mCaption = caption;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    @Override
    public String toString() {
        return mCaption;
    }

    public String getOwner() {
        return mOwner;
    }

    public void setOwner(String owner) {
        mOwner = owner;
    }

    public Uri getPhotoUri() {
        return Uri.parse("http://www.flickr.com/photos")
                .buildUpon()
                .appendPath(getOwner())
                .appendPath(getId())
                .build();
    }

}

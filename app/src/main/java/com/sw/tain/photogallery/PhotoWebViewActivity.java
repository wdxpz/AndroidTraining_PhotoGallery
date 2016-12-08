package com.sw.tain.photogallery;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;

import com.sw.tain.photogallery.Utils.GalleryItem;

import static android.provider.CalendarContract.CalendarCache.URI;

/**
 * Created by home on 2016/12/8.
 */

public class PhotoWebViewActivity extends SingleFragmentActivity {



    public static Intent newIntent(Context context, Uri photoUri){
        Intent intent = new Intent(context, PhotoWebViewActivity.class);
        intent.putExtra(GalleryItem.PHOTO_URI, photoUri);
        return intent;
    }
    @Override
    protected Fragment CreateFragment() {
        return PhotoWebViewFragment.newInstance((Uri)(getIntent().getParcelableExtra(GalleryItem.PHOTO_URI)));
    }
}

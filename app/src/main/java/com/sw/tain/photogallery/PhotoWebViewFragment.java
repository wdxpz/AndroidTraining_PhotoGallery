package com.sw.tain.photogallery;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.sw.tain.photogallery.Utils.GalleryItem;

/**
 * Created by home on 2016/12/8.
 */

public class PhotoWebViewFragment extends Fragment {
    public static PhotoWebViewFragment newInstance(Uri photoUri) {

        Bundle args = new Bundle();
        args.putParcelable(GalleryItem.PHOTO_URI, photoUri);

        PhotoWebViewFragment fragment = new PhotoWebViewFragment();
        fragment.setArguments(args);
        return fragment;
    }
}

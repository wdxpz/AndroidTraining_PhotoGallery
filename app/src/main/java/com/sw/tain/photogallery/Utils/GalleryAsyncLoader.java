package com.sw.tain.photogallery.Utils;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.sw.tain.photogallery.PhotoGalleryFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by home on 2016/11/28.
 */

//refer http://blog.csdn.net/murphykwu/article/details/35288477 for the implementation of AsyncTaskLoader

public class GalleryAsyncLoader extends AsyncTaskLoader<List<GalleryItem>> {
    private int mPageNumber;
    private List<GalleryItem> mData;

    // The observer could be anything so long as it is able to detect content changes  
    // and report them to the loader with a call to onContentChanged(). For example,  
    //  if you were writing a Loader which loads a list of all installed applications  
    //  on the device, the observer could be a BroadcastReceiver that listens for the  
    // ACTION_PACKAGE_ADDED intent, and calls onContentChanged() on the particular   
    // Loader whenever the receiver detects that a new application has been installed.  
    // Please don’t hesitate to leave a comment if you still find this confusing! :)  
    //private SampleObserver mObserver;

    public GalleryAsyncLoader(Context context, int pagenumber) {
        super(context);
        mPageNumber = pagenumber;
    }

    @Override
    public List<GalleryItem> loadInBackground() {
        List<GalleryItem> galleryList;


        galleryList =  new FlickerFetcher().FetchItem(mPageNumber+1);
        if(galleryList == null) galleryList = new ArrayList<>();
        return galleryList;
    }


    @Override
    public void deliverResult(List<GalleryItem> data) {
 //       super.deliverResult(data);
        if(isReset()){
            // The Loader has been reset; ignore the result and invalidate the data.  
            releaseResources(data);
            return;
        }

        // Hold a reference to the old data so it doesn't get garbage collected.  
        // We must protect it until the new data has been delivered.  
        List<GalleryItem> oldData = mData;
        mData=data;

        if(isStarted()){
            // If the Loader is in a started state, deliver the results to the  
            // client. The superclass method does this for us.  
            super.deliverResult(data);
        }

        // Invalidate the old data as we don't need it any more.  
        if(oldData!=null && !oldData.equals(data)) {
            releaseResources(oldData);
        }
    }

    @Override
    protected void onStartLoading() {
//        super.onStartLoading();
        if(mData!=null){
            // Deliver any previously loaded data immediately.  
            deliverResult(mData);
        }

        // Begin monitoring the underlying data source.  
//        if(mObserver==null){
//            mObserver=new SampleObserver();
//            // TODO: register the observer  
//        }

        if(takeContentChanged()||mData==null){
            // When the observer detects a change, it should call onContentChanged()  
            // on the Loader, which will cause the next call to takeContentChanged()  
            // to return true. If this is ever the case (or if the current data is  
            // null), we force a new load.  
            forceLoad();
            }
    }

    @Override
    protected void onStopLoading() {
//        super.onStopLoading();
        // The Loader is in a stopped state, so we should attempt to cancel the   
        // current load (if there is one).  
        cancelLoad();

        // Note that we leave the observer as is. Loaders in a stopped state  
        // should still monitor the data source for changes so that the Loader  
        // will know to force a new load if it is ever started again.  
    }

    @Override
    protected void onReset() {
//        super.onReset();
        // Ensure the loader has been stopped.  
        onStopLoading();
        // At this point we can release the resources associated with 'mData'.  
        if(mData!=null){
            releaseResources(mData);
            mData=null;
        }
        // The Loader is being reset, so we should stop monitoring for changes.  
//        if(mObserver!=null){
//            // TODO: unregister the observer  
//            mObserver=null;
//        }
    }

    @Override
    public void onCanceled(List<GalleryItem> data) {
        super.onCanceled(data);

        // The load has been canceled, so we should release the resources  
        // associated with 'data'.  
        releaseResources(mData);

    }

    private void releaseResources(List<GalleryItem> data){
        // For a simple List, there is nothing to do. For something like a Cursor, we   
        // would close it in this method. All resources associated with the Loader  
        // should be released here.  
    }

}

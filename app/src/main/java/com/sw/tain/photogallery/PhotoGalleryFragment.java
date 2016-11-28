package com.sw.tain.photogallery;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sw.tain.photogallery.Utils.FlickerFetcher;
import com.sw.tain.photogallery.Utils.GalleryAsyncLoader;
import com.sw.tain.photogallery.Utils.GalleryItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by home on 2016/11/23.
 */
public class PhotoGalleryFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<GalleryItem>>{
    private static final String TAG = "PhotoGalleryFragment";
    private static final String GALLERY_PAGE_NUM = "pager_number";
    private List<GalleryItem> mGalleryList = new ArrayList<>();
    private GalleryAdapter mAdapter;

    private RecyclerView mRecyclerView;
    public int mGalleryPageNum;
    private TextView mTextView;

    private boolean mIsVisible=false;
    private boolean mIsCreated=false;


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        
        if(isVisibleToUser){
            mIsVisible = true;
        }else{
            mIsVisible = false;
        }


    }


    public static PhotoGalleryFragment newInstance(int page){
        Bundle bundle = new Bundle();
        bundle.putSerializable(GALLERY_PAGE_NUM, page);

        PhotoGalleryFragment f = new PhotoGalleryFragment();
        f.setArguments(bundle);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
 //       setRetainInstance(true);

        Bundle args = getArguments();
        if(args != null){
            mGalleryPageNum = args.getInt(GALLERY_PAGE_NUM);
        }
        mIsCreated = true;

        LoaderManager loadManager = getLoaderManager();
        loadManager.initLoader(0, null, this);
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_photo_gallery_recyclerview, container, false);
        mRecyclerView = (RecyclerView)v.findViewById(R.id.recyclerview_photo_gallery);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));

        mTextView = (TextView)v.findViewById(R.id.textView_page_number);
        mTextView.setText("page: " + mGalleryPageNum );
//       mTextView.setVisibility(View.INVISIBLE);


        updateAdapter();

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateAdapter();
    }

    private void updateAdapter(){
  //      if(isAdded())
//        if(mAdapter!=null){
//            mAdapter.notifyDataSetChanged();
//        }else
        {
            mAdapter = new GalleryAdapter(mGalleryList);
            mRecyclerView.setAdapter(mAdapter);
        }

    }
//refer http://blog.csdn.net/murphykwu/article/details/35287883 for implementation of LoadManager
    @Override
    public Loader onCreateLoader(int id, Bundle args) {

        GalleryAsyncLoader loader = new GalleryAsyncLoader(this.getActivity(), mGalleryPageNum);
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<List<GalleryItem>> loader, List<GalleryItem> data) {
        mGalleryList = data;
        updateAdapter();
    }


    @Override
    public void onLoaderReset(Loader loader) {

        mGalleryList = null;
        updateAdapter();
    }

    private class GalleryHolder extends RecyclerView.ViewHolder{

        private TextView mTitleTextView;

        public GalleryHolder(View itemView) {
            super(itemView);

            mTitleTextView = (TextView)itemView;
        }

        public void bindView(GalleryItem item) {
            mTitleTextView.setText(item.getCaption());
        }
    }

    private class GalleryAdapter extends RecyclerView.Adapter<GalleryHolder>{

        private List<GalleryItem> mList;

        public GalleryAdapter(List<GalleryItem> list) {
            mList = list;
        }

        @Override
        public GalleryHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(getActivity()).inflate(android.R.layout.simple_list_item_1, parent, false);
            return new GalleryHolder(v);
        }

        @Override
        public void onBindViewHolder(GalleryHolder holder, int position) {
            GalleryItem item = mList.get(position);
            holder.bindView(item);
        }

        @Override
        public int getItemCount() {
            return mList.size();
        }

    }



}

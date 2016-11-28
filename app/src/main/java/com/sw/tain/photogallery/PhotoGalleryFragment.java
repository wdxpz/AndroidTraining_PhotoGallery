package com.sw.tain.photogallery;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sw.tain.photogallery.Utils.FlickerFetcher;
import com.sw.tain.photogallery.Utils.GalleryItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by home on 2016/11/23.
 */
public class PhotoGalleryFragment extends Fragment{
    private static final String TAG = "PhotoGalleryFragment";
    private static final String GALLERY_PAGE_NUM = "pager_number";
    private List<GalleryItem> mGalleryList = new ArrayList<>();
    private GalleryAdapter mAdapter;

    private RecyclerView mRecyclerView;
    private int mGalleryPageNum;
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
        loadData();
    }

    private void loadData() {
//        if(mIsVisible && mIsCreated){
        if(mIsVisible){
            if(mTask==null){
                mTask = new FlickerAsynTask();
                mTask.execute();
            }else{
                updateAdapter();
            }
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
        setRetainInstance(true);

        Bundle args = getArguments();
        if(args != null){
            mGalleryPageNum = args.getInt(GALLERY_PAGE_NUM);
        }
        mIsCreated = true;
        loadData();
    }
    private  FlickerAsynTask mTask;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_photo_gallery_recyclerview, container, false);
        mRecyclerView = (RecyclerView)v.findViewById(R.id.recyclerview_photo_gallery);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));

        mTextView = (TextView)v.findViewById(R.id.textView_page_number);
        mTextView.setText("page: " + mGalleryPageNum );
//       mTextView.setVisibility(View.INVISIBLE);


 //       updateAdapter();

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateAdapter();
    }

    private void updateAdapter(){

        if(isAdded()){
            mAdapter = new GalleryAdapter(mGalleryList);
            mRecyclerView.setAdapter(mAdapter);
        }
        if(mAdapter!=null)
            mAdapter.notifyDataSetChanged();
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

    private class FlickerAsynTask extends AsyncTask<Void, Void, List<GalleryItem>>{



        @Override
        protected List<GalleryItem> doInBackground(Void... params) {
            List<GalleryItem> galleryList = new ArrayList<>();


//            try {
//                String s = new FlickerFetcher().getUrlString("http://www.baidu.com");
//            } catch (IOException e) {
//                e.printStackTrace();
//            }


            galleryList =  new FlickerFetcher().FetchItem(mGalleryPageNum+1);


            return galleryList;
        }

        @Override
        protected void onPostExecute(List<GalleryItem> galleryItems) {
            super.onPostExecute(galleryItems);

            mGalleryList = galleryItems;
            updateAdapter();
        }
    }


}

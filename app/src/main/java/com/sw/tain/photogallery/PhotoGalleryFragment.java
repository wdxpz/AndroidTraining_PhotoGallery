package com.sw.tain.photogallery;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.sw.tain.photogallery.Utils.FlickerFetcher;
import com.sw.tain.photogallery.Utils.GalleryItem;
import com.sw.tain.photogallery.Service.PollService;
import com.sw.tain.photogallery.Utils.QueryPreferences;
import com.sw.tain.photogallery.Utils.ThumbnailDownloader;

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
    private  FlickerAsynTask mTask;
    private Handler mHandler;
    private ThumbnailDownloader mThumbnailDonwloader;
    private GridLayoutManager mLayoutManager;
    private int mFirstVisibleItem;
    private int mVisualItemCount;
    private boolean mIsFirstEnter = true;
    private SearchView mSearchView;

    public static PhotoGalleryFragment newInstance(int page){
        Bundle bundle = new Bundle();
        bundle.putSerializable(GALLERY_PAGE_NUM, page);

        PhotoGalleryFragment f = new PhotoGalleryFragment();
        f.setArguments(bundle);
        return f;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        
        if(isVisibleToUser){
            mIsVisible = true;
        }else{
            mIsVisible = false;
        }
        loadData(null);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);

        Bundle args = getArguments();
        if(args != null){
            mGalleryPageNum = args.getInt(GALLERY_PAGE_NUM);
        }
        mIsCreated = true;
        loadData(null);


        mHandler = new Handler();
        mThumbnailDonwloader = new ThumbnailDownloader(getActivity(), mHandler);
        mThumbnailDonwloader.setThumbnailDownloaderListner(new ThumbnailDownloader.ThumbnailDownloaderListner() {

//            @Override
//            public void onThumbnailDownloaderListner(GalleryHolder target, Bitmap bitmap) {
//                target.bindDrawalbe(bitmap);
//            }
//
//            @Override
//            public boolean isDownlaodedMatchView(GalleryHolder target, String url) {
//                return target.mThumbnailImageView.getTag().toString().equals(url);
//            }

            @Override
            public void onThumbnailDownloaderListner(String url, Bitmap bitmap) {
                ImageView view = (ImageView)mRecyclerView.findViewWithTag(url);
                if(view == null) return;
                view.setImageBitmap(bitmap);
                mAdapter.notifyDataSetChanged();

            }
        });
        mThumbnailDonwloader.start();
        mThumbnailDonwloader.getLooper();

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_photo_gallery_recyclerview, container, false);
        mRecyclerView = (RecyclerView)v.findViewById(R.id.recyclerview_photo_gallery);
        mLayoutManager = new GridLayoutManager(getActivity(), 3);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemViewCacheSize(20);
        mRecyclerView.setDrawingCacheEnabled(true);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
                mFirstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();
                //mVisualItemCount = mLayoutManager.getItemCount(); //getItemCount()返回的是总的item数
                mVisualItemCount = mLayoutManager.getChildCount();

                if(mIsFirstEnter && mVisualItemCount>0){
                    showImage(mFirstVisibleItem, mVisualItemCount);
                    mIsFirstEnter = false;
                }
            }



            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
//                super.onScrollStateChanged(recyclerView, newState);
                if(newState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE){

                    showImage(mFirstVisibleItem, mVisualItemCount);
                }
                else{
                    mThumbnailDonwloader.interrupt();
                    mThumbnailDonwloader.clearQueue();
                }
            }
        });


        mTextView = (TextView)v.findViewById(R.id.textView_page_number);
        mTextView.setText("page: " + mGalleryPageNum );
//      mTextView.setVisibility(View.INVISIBLE);


        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_gallery_item_fragment, menu);

        MenuItem item = menu.findItem(R.id.gallery_fragment_bar_search);
        //SearchView的资源定义文档不能为：android:actionViewClass="android.support.v7.widget.SearchView"
        //必须为app:actionViewClass="android.support.v7.widget.SearchView"，否则返回的SearchView为空
        mSearchView = (SearchView)item.getActionView();
        String preQueryStr = QueryPreferences.getQueryPreferences(getActivity());
        if(preQueryStr!=null){
            mSearchView.setQueryHint(preQueryStr);
        }
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d(TAG, "search query: " + query);
                if(query!=null || !query.trim().equals("")){
                    QueryPreferences.setQueryPreferences(getActivity(), query);
                    mThumbnailDonwloader.interrupt();
                    mThumbnailDonwloader.clearQueue();
                    mIsFirstEnter = true;

                    //清空当前RecyclerView
                    mGalleryList = new ArrayList<GalleryItem>();
                    updateAdapter();
                    //加载搜索数据
                    loadData(query);
                    //收起搜索视图
                    mSearchView.clearFocus();//收起键盘
                    mSearchView.onActionViewCollapsed();//收起SearchView视图
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                return false;
            }
        });

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            if(PollJobService.isPollJobServiceOn(getActivity())){
//                menu.findItem(R.id.gallery_fragment_start_poll_service).setTitle("Stop Poll Service");
//            }else{
//                menu.findItem(R.id.gallery_fragment_start_poll_service).setTitle("Start Poll Service");
//            }
//        }

        if(PollService.isPollSeriveOn(getActivity())){
            if(PollService.isPollSeriveOn(getActivity())){
                menu.findItem(R.id.gallery_fragment_start_poll_service).setTitle("Stop Poll Service");
            }else{
                menu.findItem(R.id.gallery_fragment_start_poll_service).setTitle("Start Poll Service");
            }
        }
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.gallery_fragment_clear_search:
                QueryPreferences.setQueryPreferences(getActivity(), null);
                mSearchView.setQueryHint(null);
                loadData(null);
                break;
            case R.id.gallery_fragment_start_poll_service:
                //IntentService to realize backend service thread
                PollService.startPollService(getActivity(), !PollService.isPollSeriveOn(getActivity()));
                                //JobService and JobScheduler to realize customized backend service thread
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                    PollJobService.setPollJobServiceOn(getActivity(),!PollJobService.isPollJobServiceOn(getActivity()));
//                }
                getActivity().invalidateOptionsMenu();//让选项菜单失效从而重绘菜单
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateAdapter();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mThumbnailDonwloader.clear();
        mThumbnailDonwloader.quit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mThumbnailDonwloader.clear();
    }

    private void loadData(@Nullable String query) {

        if(mIsVisible && mIsCreated){
            //若不加mIsCreated判断，则首次传入FlickAsynTask(...)的getActivity()的值会为空

            if(query==null){
                mTask = new FlickerAsynTask(getActivity());
            }else{
                mTask = new FlickerAsynTask(getActivity(), query);
            }
            mTask.execute();
            updateAdapter();
        }
    }

    private void updateAdapter(){

        if(isAdded()){
            mAdapter = new GalleryAdapter(mGalleryList);
            mRecyclerView.setAdapter(mAdapter);
        }
//        if(mAdapter!=null)
//            mAdapter.notifyDataSetChanged();
    }

    private void showImage(int firstVisibleItem, int visualItemCount) {
        for(int i=firstVisibleItem; i<firstVisibleItem+visualItemCount; i++){
            String url = mGalleryList.get(i).getUrl();
            if(url == null) continue;
            mThumbnailDonwloader.downloadImage(url);
        }
    }


    public class GalleryHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
//        private TextView mTitleTextView;
//            mTitleTextView = (TextView)itemView;
        public ImageView mThumbnailImageView;
        private GalleryItem mPhotoItem;

        public GalleryHolder(View itemView) {
            super(itemView);
            mThumbnailImageView = (ImageView)itemView.findViewById(R.id.grid_item_image_view);

            mThumbnailImageView.setOnClickListener(this);
        }

//        public void bindView(GalleryItem item) {
//            mTitleTextView.setText(item.getCaption());
//        }

        public void bindDrawalbe(Bitmap bitmap) {
            mThumbnailImageView.setImageBitmap(bitmap);
        }

        public void bindGalleryItem(GalleryItem item){
//            Picasso.with(getActivity())
//                    .load(item.getUrl())
//                    .into(mThumbnailImageView);
            mPhotoItem = item;
        }

        @Override
        public void onClick(View v) {
            if(mPhotoItem==null) return;
            Intent i = new Intent(Intent.ACTION_VIEW, mPhotoItem.getPhotoUri());
            startActivity(i);
        }

    }

    private class GalleryAdapter extends RecyclerView.Adapter<GalleryHolder>{

        private static final int PRELOAD_CACHE_SIZE = 10;
        private List<GalleryItem> mList;

        public GalleryAdapter(List<GalleryItem> list) {
            mList = list;
        }

        @Override
        public GalleryHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            //View v = LayoutInflater.from(getActivity()).inflate(android.R.layout.simple_list_item_1, parent, false);
            View v = LayoutInflater.from(getActivity()).inflate(R.layout.grid_item_thumbnail_image_view, parent, false);
            return new GalleryHolder(v);
        }

        @Override
        public void onBindViewHolder(GalleryHolder holder, int position) {
//           holder.bindGalleryItem(item); //by Picasso

            holder.mThumbnailImageView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_action_name));
            holder.bindGalleryItem(null);
            if(mList==null) return;

            GalleryItem item = mList.get(position);

//            holder.bindDrawalbe(null);
//            holder.mThumbnailImageView.setTag(item.getUrl());
//            mThumbnailDonwloader.load(holder, item.getUrl());

            String url = item.getUrl();
            holder.mThumbnailImageView.setTag(url);
            Bitmap bitmap = mThumbnailDonwloader.getCacheBitmap(url);
            if(bitmap!=null){
                holder.mThumbnailImageView.setImageBitmap(bitmap);
                holder.bindGalleryItem(item);
            }
        }

        @Override
        public int getItemCount() {
            return mList.size();
        }

    }

    private class FlickerAsynTask extends AsyncTask<Void, Void, List<GalleryItem>>{


        private final String mQueryStr;
        private ProgressDialog mProgrssDialog;
        private Context mContext;

        public FlickerAsynTask(Context context) {
            mQueryStr = null;
            mContext = context;
        }


        public FlickerAsynTask(Context context, String query) {
            mQueryStr = query;
            mContext = context;
        }

        @Override
        protected List<GalleryItem> doInBackground(Void... params) {
            List<GalleryItem> galleryList;


            if(mQueryStr==null) {
                galleryList =  new FlickerFetcher().FetchItem(mGalleryPageNum+1);
            }else{
                galleryList =  new FlickerFetcher().searchItem(mQueryStr);
            }
            if(galleryList==null)  galleryList = new ArrayList<>();

            return galleryList;
        }

        @Override
        protected void onPostExecute(List<GalleryItem> galleryItems) {
            super.onPostExecute(galleryItems);

            mGalleryList = galleryItems;
            mProgrssDialog.dismiss();
            updateAdapter();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            mProgrssDialog = new ProgressDialog(mContext);
            mProgrssDialog.setMessage("Loading...");
            mProgrssDialog.show();
        }
    }

    private MyReciever mReciever = new MyReciever();
    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter(PollService.ACTION_SHOW_NOTIFICATION);
        //如果不给Reciever添加权限，可以收到本应用发出的设有权限的broadcast，并且会接收其他应用发送的broadcast
//        getActivity().registerReceiver(mReciever, intentFilter);
        getActivity().registerReceiver(mReciever, intentFilter, PollService.PERM_PRIVATE, null);
    }

    //若在onCreate, onDestroy中调用registerReceiver, unegisterReceiver, 则必须使用getActivity.getApplicationConetxt.register...
    @Override
    public void onStop() {
        super.onStop();
        getActivity().unregisterReceiver(mReciever);
    }

    private class MyReciever extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            setResultCode(Activity.RESULT_CANCELED);
            Log.d(TAG, "receiver gets broadcast: " + intent.getAction());
            Log.d(TAG, "cancel notification!");
        }
    }

}

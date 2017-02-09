package com.blackzheng.me.piebald.ui;

import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.blackzheng.me.piebald.App;
import com.blackzheng.me.piebald.R;
import com.blackzheng.me.piebald.api.UnsplashAPI;
import com.blackzheng.me.piebald.api.UnsplashService;
import com.blackzheng.me.piebald.dao.PhotoCollectionDataHelper;
import com.blackzheng.me.piebald.data.ImageCacheManager;
import com.blackzheng.me.piebald.model.Collection;
import com.blackzheng.me.piebald.model.Photo;
import com.blackzheng.me.piebald.ui.adapter.ContentAdapter;
import com.blackzheng.me.piebald.ui.listener.OnDoubleClickListener;
import com.blackzheng.me.piebald.util.Constants;
import com.blackzheng.me.piebald.util.Decoder;
import com.blackzheng.me.piebald.util.DensityUtils;
import com.blackzheng.me.piebald.util.DrawableUtil;
import com.blackzheng.me.piebald.util.LogHelper;
import com.blackzheng.me.piebald.util.ResourceUtil;
import com.blackzheng.me.piebald.util.StringUtil;
import com.malinskiy.superrecyclerview.OnMoreListener;
import com.malinskiy.superrecyclerview.SuperRecyclerView;

import java.util.List;
import java.util.Random;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * 用于显示相册集的界面
 */
public class CollectionActivity extends BaseActivity implements
        LoaderManager.LoaderCallbacks<Cursor>, OnMoreListener, ContentAdapter.OnItemClickLitener, OnDoubleClickListener {

    private static final String TAG = LogHelper.makeLogTag(CollectionActivity.class);
    public static final String ID = "id";
    public static final String IS_CURATED = "isCurated";
    private static final String NOT_OLD_ID = "not_old_id";

    private int mTotal;
    private boolean isCurated;
    private String mId, mTitle, mDescription, mPubTime, mCurator,mCuratorUserName, mCuratorProfileURL, mProfileURL;
    private int mDefaultColor;
    private Collection mCollection;
    private String mOldId;
    private int mPage;
    private PhotoCollectionDataHelper mDataHelper;
    private ContentAdapter mAdapter;
    private int mLastPage = 1;//最后一页的页数

    private Toolbar mToolbar;
    private CollapsingToolbarLayout title;
    private TextView description, pub_time, total, curator;
    private ImageView profile;
    private SuperRecyclerView list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection);
        mOldId = NOT_OLD_ID;
        initViews();
        getCollection();
        if(mCollection == null){
            getJson();
        }
        else{
            initialData(mCollection);
            setupHeaderView();
            startLoadingPhotos();
        }
        registerDoubleClickListener(mToolbar, this);//给toolbar注册双击监听
    }

    //实现双击toolbar回到列表顶部
    public static void registerDoubleClickListener(View view, final OnDoubleClickListener listener){
        if(listener==null) return;
        view.setOnClickListener(new View.OnClickListener() {
            private static final int DOUBLE_CLICK_TIME = 350;        //双击间隔时间350毫秒
            private boolean waitDouble = true;

            private Handler handler = new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    listener.OnSingleClick((View)msg.obj);
                }

            };

            //等待双击
            public void onClick(final View v) {
                if(waitDouble){
                    waitDouble = false;        //与执行双击事件
                    new Thread(){

                        public void run() {
                            try {
                                Thread.sleep(DOUBLE_CLICK_TIME);
                            } catch (InterruptedException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }    //等待双击时间，否则执行单击事件
                            if(!waitDouble){
                                //如果过了等待事件还是预执行双击状态，则视为单击
                                waitDouble = true;
                                Message msg = handler.obtainMessage();
                                msg.obj = v;
                                handler.sendMessage(msg);
                            }
                        }

                    }.start();
                }else{
                    waitDouble = true;
                    listener.OnDoubleClick(v);    //执行双击
                }
            }
        });
    }

    private void getJson(){
        Subscription subscription = getObservable() .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Collection>() {
                    @Override
                    public void call(Collection collection) {
                        LogHelper.d(TAG, "getJson: getJson " + collection.id);
                        initialData(collection);
                        setupHeaderView();
                        startLoadingPhotos();
                        Collection.addToCache(collection);
                    }
                }, ERRORACTION);
        addSubscription(subscription);
    }
    private Observable<Collection> getObservable(){
        UnsplashService unsplashService = UnsplashAPI.getInstance().getUnsplashService();
        return isCurated ? unsplashService.getCuratedCollection(mId, UnsplashAPI.CLIENT_ID) : unsplashService.getFeatureCollection(mId, UnsplashAPI.CLIENT_ID);
    }

    private void getCollection() {
        Intent intent = getIntent();
        mId = intent.getStringExtra(ID);
        isCurated = Boolean.parseBoolean(intent.getStringExtra(IS_CURATED));
        mCollection = Collection.getFromCache(mId);
    }

    private void startLoadingPhotos() {
        getSupportLoaderManager().initLoader(2, null, this);
        loadData(1);
    }

    private void loadData(final int next) {
        UnsplashService service = UnsplashAPI.getInstance().getUnsplashService();
        Observable<List<Photo>> observable;
        observable = isCurated ? service.getCuratedPhotos(mId, next, UnsplashAPI.CLIENT_ID) : service.getFeaturePhotos(mId, next, UnsplashAPI.CLIENT_ID);
        Subscription subscription = observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Func1<List<Photo>, List<Photo>>() {
                    @Override
                    public List<Photo> call(List<Photo> photos) {
                        if(photos.isEmpty())   //已经拉到页尾了，隐藏进度圈
                            list.hideMoreProgress();
                        return photos;
                    }
                })
                .observeOn(Schedulers.io())
                .subscribe(new Action1<List<Photo>>() {
                    @Override
                    public void call(List<Photo> photos) {
                        LogHelper.d(TAG, "getJson: loadData " + next);
                        if(!photos.isEmpty()){
                            String newId = photos.get(0).id;
                            if (mOldId != null && !mOldId.equals(newId)) {  //avoid loading the same content repeatedly
                                if(mPage < 2)
                                    mDataHelper.deleteAll();
                                mDataHelper.bulkInsert(photos);
                            }
                        }
                    }
                }, ERRORACTION);
        addSubscription(subscription);
    }

    private void setupHeaderView() {
        title.setTitle(mTitle);
        if(mDescription != null && !mDescription.isEmpty())
            description.setText("\"" + mDescription + "\"");
        pub_time.setText(mPubTime);
        total.setText(ResourceUtil.getStringFromRes(this, R.string.total_photos) + " " + mTotal);
        curator.setText(mCurator);
        ImageCacheManager.loadImageWithBlur(Decoder.decodeURL(mProfileURL), profile, mDefaultColor, title);
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CollectionActivity.this, UserAlbumActivity.class);
                intent.putExtra(UserAlbumActivity.USERNAME, mCuratorUserName);
                intent.putExtra(UserAlbumActivity.NAME, mCurator);
                intent.putExtra(UserAlbumActivity.PROFILE_IMAGE_URL, mCuratorProfileURL);
                startActivity(intent);
            }
        });
    }

    private void initialData(Collection collection) {
        mTitle = collection.title;
        mDescription = collection.description;
        mPubTime = ResourceUtil.getStringFromRes(this, R.string.published_at) + StringUtil.dateFormat(collection.published_at);
        mTotal = collection.total_photos;
        mCurator =  Decoder.decodeStr(collection.user.name);
        mCuratorUserName = collection.user.username;
        mCuratorProfileURL = collection.user.profile_image.large;
        mProfileURL = collection.user.profile_image.medium;
        mDataHelper = new PhotoCollectionDataHelper(App.getContext(), mId);
        mLastPage = (int) Math.ceil(mTotal * 1.0 / 10); //最后一页的页号
        mDefaultColor = DrawableUtil.getDefaultColors()[new Random().nextInt(5)];
    }

    private void initViews() {

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        initActionBar(mToolbar);
        //这里给Toolbar设置MarginTop是为了防止Toolbar被状态栏挡住
        Toolbar.MarginLayoutParams lp = (Toolbar.MarginLayoutParams)mToolbar.getLayoutParams();
        //在4.4以下状态栏不透明，故没有遮挡的问题存在
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //在6.0及以上版本，statusbar的高度是24dp,在6.0以下是25dp
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
                lp.topMargin = DensityUtils.dip2px(this, 25);
            else
                lp.topMargin = DensityUtils.dip2px(this, 24);
        }
        mToolbar.setLayoutParams(lp);
        title = (CollapsingToolbarLayout) findViewById(R.id.collapsing_layout);
        //设置CollapsingToolbarLayout的展开的折叠字体，使其与其他字体一致
        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/RobotoCondensed-Bold.ttf");
        title.setCollapsedTitleTypeface(tf);
        title.setExpandedTitleTypeface(tf);

        description = (TextView) findViewById(R.id.description);
        pub_time = (TextView) findViewById(R.id.pub_time);
        total = (TextView) findViewById(R.id.total);
        curator = (TextView) findViewById(R.id.curator);
        profile = (ImageView) findViewById(R.id.profile);
        list = (SuperRecyclerView) findViewById(R.id.list);

        mAdapter = new ContentAdapter(this, null, R.layout.collection_item);
        mAdapter.setOnItemClickLitener(this);
        RecyclerView.LayoutManager layoutManager = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ?
                new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL) : new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        list.setLayoutManager(layoutManager);
        list.setAdapter(mAdapter);
        list.setupMoreListener(this, 1);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ImageCacheManager.cancelDisplayingTask(profile);
    }
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return mDataHelper.getCursorLoader();
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mPage = (int) Math.ceil(data.getCount() * 1.0 / 10) + 1;
        if (data.getCount() != 0 && data.moveToFirst()) {
            mOldId = data.getString(1);
        }
        Log.d("test", "cursor count:" + data.getCount());
        mAdapter.changeCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.changeCursor(null);
    }

    @Override
    public void onMoreAsked(int overallItemsCount, int itemsBeforeMore, int maxLastVisiblePosition) {
        if (mPage > 1 && mPage <= mLastPage)
            loadData(mPage);
        else
            list.hideMoreProgress();
    }

    @Override
    public void onItemClick(View view, Photo photo, int position, int type) {
        Intent intent = null;
        LogHelper.d(TAG, "onItem Click: " + type);
        switch (type){
            case Constants.TYPE_PHOTO:
                intent = new Intent(this, PhotoDetailActivity.class);
                intent.putExtra(PhotoDetailActivity.PHOTO_ID, photo.id);
                intent.putExtra(PhotoDetailActivity.DOWNLOAD_URL, photo.links.download);
                startActivity(intent);
                break;
            case Constants.TYPE_PROFILE:
                intent = new Intent(this, UserAlbumActivity.class);
                intent.putExtra(UserAlbumActivity.USERNAME, photo.user.username);
                intent.putExtra(UserAlbumActivity.NAME, photo.user.name);
                intent.putExtra(UserAlbumActivity.PROFILE_IMAGE_URL, photo.user.profile_image.large);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    @Override
    public void onItemLongClick(View view, Photo photo, int position, int type) {

    }

    @Override
    public void OnSingleClick(View v) {

    }

    @Override
    public void OnDoubleClick(View v) {
        list.getRecyclerView().smoothScrollToPosition(0);
    }
}

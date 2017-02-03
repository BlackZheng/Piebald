package com.blackzheng.me.piebald.ui;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.toolbox.ImageLoader;
import com.blackzheng.me.piebald.App;
import com.blackzheng.me.piebald.R;
import com.blackzheng.me.piebald.api.UnsplashAPI;
import com.blackzheng.me.piebald.dao.PhotoCollectionDataHelper;
import com.blackzheng.me.piebald.data.GsonRequest;
import com.blackzheng.me.piebald.data.ImageCacheManager;
import com.blackzheng.me.piebald.model.Collection;
import com.blackzheng.me.piebald.model.Photo;
import com.blackzheng.me.piebald.ui.adapter.ContentAdapter;
import com.blackzheng.me.piebald.util.Decoder;
import com.blackzheng.me.piebald.util.DensityUtils;
import com.blackzheng.me.piebald.util.DrawableUtil;
import com.blackzheng.me.piebald.util.ResourceUtil;
import com.blackzheng.me.piebald.util.StringUtil;
import com.blackzheng.me.piebald.util.TaskUtils;
import com.google.gson.reflect.TypeToken;
import com.malinskiy.superrecyclerview.OnMoreListener;
import com.malinskiy.superrecyclerview.SuperRecyclerView;

import java.util.List;
import java.util.Random;

/**
 * 用于显示相册集的界面
 */
public class CollectionActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor>, OnMoreListener, ContentAdapter.OnItemClickLitener {

    public static final String ID = "id";
    private static final String NOT_OLD_ID = "not_old_id";

    private int mId, mTotal;
    private boolean isCurated;
    private String mTitle, mDescription, mPubTime, mCurator, mProfileURL;
    private int mDefaultColor;
    private Collection mCollection;
    private String mOldId;
    private int mPage;
    private PhotoCollectionDataHelper mDataHelper;
    private ContentAdapter mAdapter;
    private String mRequestURL;
    private int mLastPage = 1;//最后一页的页数
    private ImageLoader.ImageContainer mProfileRequest;

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
        initialData();
        setupHeaderView();
        startLoadingPhotos();
    }

    private void getCollection() {
        Intent intent = getIntent();
        mId = intent.getIntExtra(ID, 0);
        mCollection = Collection.getFromCache(mId);
    }

    private void startLoadingPhotos() {
        getSupportLoaderManager().initLoader(2, null, this);
        loadData(1);
    }

    private void loadData(int next) {
        Log.d("test", String.format(mRequestURL, String.valueOf(mId), String.valueOf(next)));
        executeRequest(
                new GsonRequest(String.format(mRequestURL, String.valueOf(mId), String.valueOf(next)), new TypeToken<List<Photo>>() {}.getType(), responseListener(), errorListener()));
    }



    private void setupHeaderView() {
        title.setTitle(mTitle);
        if(mDescription != null && !mDescription.isEmpty())
            description.setText("\"" + Decoder.decodeStr(mDescription) + "\"");
        pub_time.setText(mPubTime);
        total.setText(ResourceUtil.getStringFromRes(this, R.string.total_photos) + " " + mTotal);
        curator.setText(mCurator);
        mProfileRequest = ImageCacheManager.loadImage(Decoder.decodeURL(mProfileURL), ImageCacheManager.getProfileListenerWithBlur(profile, mDefaultColor, mDefaultColor, title));
    }

    private void initialData() {
        isCurated = mCollection.curated;
        mTitle = mCollection.title;
        mDescription = mCollection.description;
        mPubTime = ResourceUtil.getStringFromRes(this, R.string.published_at) + StringUtil.dateFormat(mCollection.published_at);
        mTotal = mCollection.total_photos;
        mCurator =  Decoder.decodeStr(mCollection.user.name) ;
        mProfileURL = mCollection.user.profile_image.medium;
        mDataHelper = new PhotoCollectionDataHelper(App.getContext(), String.valueOf(mId));
        mRequestURL = isCurated ? UnsplashAPI.GET_CURATED_PHOTOS : UnsplashAPI.GET_FEATURED_PHOTOS;
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
        list.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));
        list.setAdapter(mAdapter);
        list.setupMoreListener(this, 1);
    }

    private Response.Listener<List<Photo>> responseListener() {

        return new Response.Listener<List<Photo>>() {
            @Override
            public void onResponse(final List<Photo> response) {
                Log.d("test", "size: " + response.size() + "");
                if(response.size() < 1){    //已经拉到页尾了，隐藏进度圈
                    list.hideMoreProgress();
                    return;
                }
                final String newId = response.get(0).id;
                TaskUtils.executeAsyncTask(new AsyncTask<Object, Object, Object>() {

                    @Override
                    protected Object doInBackground(Object... params) {
                        if (mOldId != null && !mOldId.equals(newId)) {  //avoid loading the same content repeatedly
                            if(mPage < 2)
                                mDataHelper.deleteAll();
                            mDataHelper.bulkInsert(response);
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Object o) {
                        super.onPostExecute(o);
                    }
                });


            }
        };
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mProfileRequest.cancelRequest();
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
    public void onItemClick(View view, Photo photo, int position) {
        Intent intent = new Intent(this, PhotoDetailActivity.class);
        intent.putExtra(PhotoDetailActivity.PHOTO_ID, photo.id);
        intent.putExtra(PhotoDetailActivity.DOWNLOAD_URL, photo.links.download);
        startActivity(intent);
    }

    @Override
    public void onItemLongClick(View view, Photo photo, int position) {

    }
}

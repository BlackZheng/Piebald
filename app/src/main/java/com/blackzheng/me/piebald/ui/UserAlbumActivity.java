package com.blackzheng.me.piebald.ui;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.blackzheng.me.piebald.App;
import com.blackzheng.me.piebald.R;
import com.blackzheng.me.piebald.api.UnsplashAPI;
import com.blackzheng.me.piebald.dao.UserAlbumDataHelper;
import com.blackzheng.me.piebald.data.ImageCacheManager;
import com.blackzheng.me.piebald.model.Photo;
import com.blackzheng.me.piebald.model.User;
import com.blackzheng.me.piebald.ui.adapter.UserAlbumAdapter;
import com.blackzheng.me.piebald.util.Decoder;
import com.blackzheng.me.piebald.util.DensityUtils;
import com.blackzheng.me.piebald.util.DrawableUtil;
import com.blackzheng.me.piebald.util.LogHelper;
import com.blackzheng.me.piebald.util.ResourceUtil;
import com.malinskiy.superrecyclerview.OnMoreListener;
import com.malinskiy.superrecyclerview.SuperRecyclerView;

import java.util.List;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class UserAlbumActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor>, OnMoreListener, UserAlbumAdapter.OnItemClickLitener {

    private static final String TAG = LogHelper.makeLogTag(UserAlbumActivity.class);
    public static final String NAME = "name";
    public static final String USERNAME = "username";
    public static final String PROFILE_IMAGE_URL = "profile_image_url";
    private static final String NOT_OLD_ID = "not_old_id";

    private Toolbar mToolbar;
    private CollapsingToolbarLayout title;
    private CircleImageView profile;
    private TextView likes, location, bio;
    private SuperRecyclerView list;
    private ImageView locIcon;

    private String mUsername;
    private String mName;
    private String mProfileImageUrl;
    private int mDefaultColor;
    private UserAlbumAdapter mAdapter;
    private User mUser;
    private UserAlbumDataHelper mDataHelper;
    private int mPage;
    private String mOldId;
    private int mLastPage = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_album);
        initialData();
        initViews();
        setupTitleAndProfile();
        if  (mUser != null){
            setLastPage(mUser.total_photos);
            setupLikesAndLocationAndBio(mUser);
        }
        else {
            getUserJson(mUsername);
        }
        startLoadingPhotos();
    }

    private void getUserJson(String username){
        LogHelper.d(TAG, "getUserJson");
        Subscription subscription = UnsplashAPI.getInstance().getUnsplashService().getUserByUsername(username, UnsplashAPI.CLIENT_ID)
            . subscribeOn(Schedulers.io())
            . observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Action1<User>() {
                @Override
                public void call(User user) {
                    LogHelper.d(TAG,"getUserJson: onNext");
                    setLastPage(user.total_photos);
                    User.addToCache(user);
                    setupLikesAndLocationAndBio(user);
                }
            }, ERRORACTION);
        addSubscription(subscription);
    }
    private void setupLikesAndLocationAndBio(User user) {
        likes.setText(ResourceUtil.getStringFromRes(this, R.string.be_liked) + " " + user.total_likes + " " + ResourceUtil.getStringFromRes(this, R.string.times));
        if(user.location != null){
            locIcon.setVisibility(View.VISIBLE);
            location.setText(Decoder.decodeStr(user.location));
        }
        if(user.bio != null)
            bio.setText(Decoder.decodeStr(" \" " + user.bio + " \""));
    }

    private void setupTitleAndProfile() {
        title.setTitle(Decoder.decodeStr(mName));
        mDefaultColor = DrawableUtil.getDefaultColors()[new Random().nextInt(5)];
        ImageCacheManager.loadImageWithBlur(Decoder.decodeURL(mProfileImageUrl), profile, mDefaultColor, title);

    }

    private void initViews() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);

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
        
        title = (CollapsingToolbarLayout) findViewById(R.id.collapsing_layout) ;
        //设置CollapsingToolbarLayout的展开的折叠字体，使其与其他字体一致
        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/RobotoCondensed-Bold.ttf");
        title.setCollapsedTitleTypeface(tf);
        title.setExpandedTitleTypeface(tf);

        profile = (CircleImageView) findViewById(R.id.profile);
        likes = (TextView) findViewById(R.id.likes);
        location = (TextView) findViewById(R.id.location);
        bio = (TextView) findViewById(R.id.bio);
        locIcon = (ImageView) findViewById(R.id.loc_icon);
        list = (SuperRecyclerView) findViewById(R.id.list);
        list.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        initActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        mAdapter = new UserAlbumAdapter(this, null);
        mAdapter.setOnItemClickLitener(this);
        list.setAdapter(mAdapter);
        list.setupMoreListener(this, 1);

    }

    private void initialData() {
        Intent intent = getIntent();
        mUsername = intent.getStringExtra(USERNAME);
        mName = intent.getStringExtra(NAME);
        mProfileImageUrl = intent.getStringExtra(PROFILE_IMAGE_URL);
        mDataHelper = new UserAlbumDataHelper(App.getContext(), mUsername);
        mOldId = NOT_OLD_ID;
        mUser = User.getFromCache(mUsername);
    }

    private void setLastPage(int total){
        mLastPage = (int) Math.ceil(total * 1.0 / 10);

    }

    private void startLoadingPhotos(){
        getSupportLoaderManager().initLoader(1, null, this);
        loadData(1);
    }

    private void loadData(int next) {
        Subscription subscription = UnsplashAPI.getInstance().getUnsplashService().getPhotosByUser(mUsername, next, UnsplashAPI.CLIENT_ID)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new Action1<List<Photo>>() {
                    @Override
                    public void call(List<Photo> photos) {
                        LogHelper.d(TAG, "loadData: onNext");
                        String newId = photos.get(0).id;
                        if (mOldId != null && !mOldId.equals(newId)) {  //avoid loading the same content repeatedly
                            if(mPage < 2)
                                mDataHelper.deleteAll();
                            mDataHelper.bulkInsert(photos);
                        }
                    }
                }, ERRORACTION);
        addSubscription(subscription);
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
        mAdapter.changeCursor(data);
    }

    @Override
    public void onLoaderReset(Loader loader) {
        mAdapter.changeCursor(null);
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

    @Override
    public void onMoreAsked(int overallItemsCount, int itemsBeforeMore, int maxLastVisiblePosition) {
        if (mPage > 1 && mPage <= mLastPage)
            loadData(mPage);
        else
            list.hideMoreProgress();
    }
}

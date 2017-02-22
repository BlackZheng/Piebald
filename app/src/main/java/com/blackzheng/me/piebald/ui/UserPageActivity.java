package com.blackzheng.me.piebald.ui;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.blackzheng.me.piebald.App;
import com.blackzheng.me.piebald.R;
import com.blackzheng.me.piebald.api.UnsplashAPI;
import com.blackzheng.me.piebald.dao.UserAlbumDataHelper;
import com.blackzheng.me.piebald.data.ImageCacheManager;
import com.blackzheng.me.piebald.model.User;
import com.blackzheng.me.piebald.ui.fragment.ContentFragment;
import com.blackzheng.me.piebald.ui.fragment.UserCollectionsFragment;
import com.blackzheng.me.piebald.ui.fragment.UserPhotosFragment;
import com.blackzheng.me.piebald.util.Decoder;
import com.blackzheng.me.piebald.util.DensityUtils;
import com.blackzheng.me.piebald.util.DrawableUtil;
import com.blackzheng.me.piebald.util.LogHelper;
import com.blackzheng.me.piebald.util.ResourceUtil;
import com.umeng.analytics.MobclickAgent;

import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class UserPageActivity extends BaseActivity {

    private static final String TAG = LogHelper.makeLogTag(UserPageActivity.class);
    public static final String NAME = "name";
    public static final String USERNAME = "username";
    public static final String USER_ID = "user_id";
    public static final String PROFILE_IMAGE_URL = "profile_image_url";

    private Toolbar mToolbar;
    private CollapsingToolbarLayout title;
    private CircleImageView profile;
    private TextView likes, location, bio;
    private ImageView locIcon;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    private String mUsername;
    private String mUserId;
    private String mName;
    private String mProfileImageUrl;
    private int mDefaultColor;
    private UserPagerAdapter adapter;
    private User mUser;
    private String[] pageTitles;
    private ContentFragment[] pageFragments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_album);
        initialData();
        initViews();
        setupTitleAndProfile();
        if  (mUser != null){
            setupLikesAndLocationAndBio(mUser);
        }
        else {
            getUserJson(mUsername);
        }
        startLoadingPhotos();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        LogHelper.d(TAG, "onNewIntent:" + intent);
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
            location.setVisibility(View.VISIBLE);
        }
        if(user.bio != null)
            bio.setText(Decoder.decodeStr(" \" " + user.bio + " \""));
        //number indication
        pageTitles[0] = pageTitles[0] + " " + user.total_photos ;
        pageTitles[1] = pageTitles[1] + " " + user.total_collections;
        adapter.notifyDataSetChanged();
    }

    private void setupTitleAndProfile() {
        title.setTitle(Decoder.decodeStr(mName));
        mDefaultColor = DrawableUtil.getDefaultColors()[new Random().nextInt(5)];
        ImageCacheManager.loadImageWithBlur(Decoder.decodeURL(mProfileImageUrl), profile, mDefaultColor, title);
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
        
        title = (CollapsingToolbarLayout) findViewById(R.id.collapsing_layout) ;
        //设置CollapsingToolbarLayout的展开的折叠字体，使其与其他字体一致
        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/RobotoCondensed-Bold.ttf");
        title.setCollapsedTitleTypeface(tf);
        title.setExpandedTitleTypeface(tf);
        adapter = new UserPagerAdapter(getSupportFragmentManager());
        profile = (CircleImageView) findViewById(R.id.profile);
        likes = (TextView) findViewById(R.id.likes);
        location = (TextView) findViewById(R.id.location);
        bio = (TextView) findViewById(R.id.bio);
        locIcon = (ImageView) findViewById(R.id.loc_icon);
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        viewPager = (ViewPager) findViewById(R.id.viewPager);

    }

    private void initialData() {
        Intent intent = getIntent();
        mUsername = intent.getStringExtra(USERNAME);
        mUserId = intent.getStringExtra(USER_ID);
        mName = intent.getStringExtra(NAME);
        mProfileImageUrl = intent.getStringExtra(PROFILE_IMAGE_URL);
        mUser = User.getFromCache(mUsername);
        pageTitles = new String[]{ResourceUtil.getStringFromRes(this, R.string.photos), ResourceUtil.getStringFromRes(this, R.string.collections)};
        pageFragments = new ContentFragment[]{UserPhotosFragment.newInstance("UserPhotos", mUsername), UserCollectionsFragment.newInstance("UserCollections", mUsername, mUserId)};
    }

    private void startLoadingPhotos(){
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(getClass().getName());
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(getClass().getName());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ImageCacheManager.cancelDisplayingTask(profile);
    }

    class UserPagerAdapter extends FragmentPagerAdapter {

        public UserPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return pageFragments[position];
        }

        @Override
        public int getCount() {
            return pageTitles.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return pageTitles[position];
        }
    }
}

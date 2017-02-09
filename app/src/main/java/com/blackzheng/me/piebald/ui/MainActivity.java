package com.blackzheng.me.piebald.ui;


import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.blackzheng.me.piebald.App;
import com.blackzheng.me.piebald.BuildConfig;
import com.blackzheng.me.piebald.R;
import com.blackzheng.me.piebald.api.UnsplashAPI;
import com.blackzheng.me.piebald.ui.fragment.CategoryContentFragment;
import com.blackzheng.me.piebald.ui.fragment.CollectionsFragment;
import com.blackzheng.me.piebald.ui.fragment.ContentFragment;
import com.blackzheng.me.piebald.ui.listener.OnDoubleClickListener;
import com.blackzheng.me.piebald.util.Constants;
import com.blackzheng.me.piebald.util.DensityUtils;
import com.blackzheng.me.piebald.util.LogHelper;
import com.blackzheng.me.piebald.util.ResourceUtil;
import com.blackzheng.me.piebald.util.ToastUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.umeng.analytics.MobclickAgent;

import net.youmi.android.AdManager;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends BaseActivity implements OnDoubleClickListener{
    private static final String TAG = LogHelper.makeLogTag(MainActivity.class);

    public static final String LATEST = ResourceUtil.getStringFromRes(App.getContext(), R.string.latest);
    public static final String BUILDINGS = ResourceUtil.getStringFromRes(App.getContext(), R.string.buildings);
    public static final String FOOD_AND_DRINK = ResourceUtil.getStringFromRes(App.getContext(), R.string.foodndrink);
    public static final String NATURE = ResourceUtil.getStringFromRes(App.getContext(), R.string.nature);
    public static final String PEOPLE = ResourceUtil.getStringFromRes(App.getContext(), R.string.people);
    public static final String TECHNOLOGY = ResourceUtil.getStringFromRes(App.getContext(), R.string.technology);
    public static final String OBJECTS = ResourceUtil.getStringFromRes(App.getContext(), R.string.objects);
    public static final String FEATURED = ResourceUtil.getStringFromRes(App.getContext(), R.string.featured);
    public static final String CURATED = ResourceUtil.getStringFromRes(App.getContext(), R.string.curated);


    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private Toolbar mToolbar;
    private ContentFragment mContentFragment;
    private String mCategory = LATEST;//default category
    private MenuItem currentMenuItem;
    private RelativeLayout splashView;
    private LinearLayout adLayout;
    private NavigationView navView;
    private ImageView headerImage;
    private Drawable currentHeaderDrawable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogHelper.d(TAG, "onCreate");
        setContentView(R.layout.activity_main);
        initSplashView();//初始化启动页面
        initAD();
        MainActivityPermissionsDispatcher.requestPermissionWithCheck(this); //请求权限

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        initActionBar(mToolbar);
        registerDoubleClickListener(mToolbar, this);//给toolbar注册双击监听
        initDrawer();//初始化侧滑菜单
        currentMenuItem = navView.getMenu().findItem(R.id.latest);
        switchFragment(currentMenuItem);
        currentMenuItem.setChecked(true);
    }

    private void initAD() {
        AdManager.getInstance(this).init(Constants.YOUMI_APP_ID, Constants.YOUMI_SECRET, false, false);
//        adLayout = (LinearLayout)findViewById(R.id.adLayout);
//        View adView = BannerManager.getInstance(this).getBanner(this);
//        adLayout.addView(adView);
    }

    private void initSplashView() {
        splashView = (RelativeLayout) findViewById(R.id.splash_view);
        AlphaAnimation animation=new AlphaAnimation(1.0f,1.0f);
        animation.setDuration(2000);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                splashView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                splashView.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        splashView.setAnimation(animation);
    }

    @NeedsPermission({Manifest.permission.READ_PHONE_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void requestPermission(){
        LogHelper.d(TAG, "Granted Photo Permission");
    }
    @OnShowRationale({Manifest.permission.READ_PHONE_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void showRationaleForCamera(final PermissionRequest request) {
        new AlertDialog.Builder(this)
                .setMessage(R.string.phone_permission_showRationale)
                .setPositiveButton(R.string.button_allow, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        request.proceed();
                    }
                })
                .setNegativeButton(R.string.button_deny, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        request.cancel();
                    }
                })
                .show();
    }

    private void initDrawer(){
        currentHeaderDrawable = ContextCompat.getDrawable(this, R.mipmap.default_drawer_header);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.dl_left);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.drawer_open, R.string.drawer_close);
        mDrawerToggle.syncState();

        navView = (NavigationView) findViewById(R.id.nav_view);
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                LogHelper.d(TAG,"NavigationItemSelected: "+ "groupId: " + item.getGroupId() + ",itemId: " + item.getItemId() + ", itemTitle:" + item.getTitle());
                if(currentMenuItem != null && item != currentMenuItem){
                    switchFragment(item);
                    currentMenuItem = item;
                }
                mDrawerLayout.closeDrawers();
                return true;
            }
        });
        int[][] state = new int[][]{
                new int[]{-android.R.attr.state_checked}, // unchecked
                new int[]{android.R.attr.state_checked}  // pressed
        };
        int[] color = new int[]{
                Color.BLACK, ContextCompat.getColor(this,R.color.colorPrimary)};
        navView.setItemTextColor(new ColorStateList(state, color));

        headerImage = (ImageView) navView.getHeaderView(0).findViewById(R.id.drawer_header);
        refreshHeaderImage(headerImage);
        navView.getHeaderView(0).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogHelper.d(TAG, "click drawer");
                MobclickAgent.onEvent(MainActivity.this,"click drawer head view");
                refreshHeaderImage(headerImage);
            }
        });
    }
    private void refreshHeaderImage(ImageView imageView){
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .showImageOnLoading(currentHeaderDrawable)
                .showImageOnFail(currentHeaderDrawable)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .considerExifParams(false)
                .build();
        ImageLoader.getInstance().displayImage(String.format(UnsplashAPI.GET_RANDOM_PHOTOS, DensityUtils.dip2px(this, 280f), DensityUtils.dip2px(this, 200f))
                , imageView, options, new SimpleImageLoadingListener(){
                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        super.onLoadingComplete(imageUri, view, loadedImage);
                        currentHeaderDrawable = new BitmapDrawable(getResources(), loadedImage);
                    }
                });
    }
    private void switchFragment(MenuItem item){
        String title = item.getTitle().toString();
        switch (item.getGroupId()){
            case R.id.g1:
                mContentFragment = CategoryContentFragment.newInstance(title);
                break;
            case R.id.g2:
                mContentFragment = CollectionsFragment.newInstance(title);
                break;

        }
        getSupportActionBar().setTitle(title);
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_from_left, R.anim.slide_out_to_right)
                .replace(R.id.content_frame, mContentFragment)
                .commitAllowingStateLoss();
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }else
            super.onBackPressed();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogHelper.d(TAG, "onDestroy");
        ImageLoader.getInstance().stop();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LogHelper.d(TAG, "onConfigurationChanged");
        mDrawerToggle.onConfigurationChanged(newConfig);
        switchFragment(currentMenuItem);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void OnSingleClick(View v) {

    }

    @Override
    public void OnDoubleClick(View v) {
        mContentFragment.getRecyclerView().smoothScrollToPosition(0);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // NOTE: delegate the permission handling to generated method
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }
}
